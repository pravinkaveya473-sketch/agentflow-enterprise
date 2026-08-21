package com.cjl.java_ai_agent.agent.service;

import com.cjl.java_ai_agent.agent.dto.AgentRunView;
import com.cjl.java_ai_agent.agent.dto.TaskAgentResponse;
import com.cjl.java_ai_agent.agent.model.AgentRunStatus;
import com.cjl.java_ai_agent.agent.runtime.AgentRunContext;
import com.cjl.java_ai_agent.common.exception.BusinessException;
import com.cjl.java_ai_agent.config.AppProperties;
import com.cjl.java_ai_agent.infrastructure.redis.IdempotencyService;
import com.cjl.java_ai_agent.infrastructure.redis.RateLimitService;
import com.cjl.java_ai_agent.knowledge.KnowledgeTools;
import com.cjl.java_ai_agent.memory.ConversationIdService;
import com.cjl.java_ai_agent.observability.AgentMetrics;
import com.cjl.java_ai_agent.project.tool.ProjectTools;
import com.cjl.java_ai_agent.security.CurrentUserService;
import com.cjl.java_ai_agent.task.tool.TaskTools;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 企业 Agent Harness 的核心运行时。
 *
 * <p>与“直接调用 ChatClient 并让框架自动完成全部工具循环”不同，
 * 本类显式控制每一轮：模型调用 -> Tool Call -> Java 工具执行 -> 工具结果回传 -> 下一轮模型调用。</p>
 *
 * <p>显式循环让应用能够加入模型/工具次数上限、总超时、幂等、限流、审计、指标、
 * 对话记忆以及后续的人类审批与暂停恢复能力。</p>
 */
@Service
public class TaskAgentService {

    private static final Logger log = LoggerFactory.getLogger(TaskAgentService.class);

    private final ChatClient chatClient;
    private final TaskTools taskTools;
    private final ProjectTools projectTools;
    private final KnowledgeTools knowledgeTools;
    private final ToolCallingManager toolCallingManager;
    private final AgentRunStore runStore;
    private final ChatMemory chatMemory;
    private final CurrentUserService currentUser;
    private final ConversationIdService conversationIds;
    private final IdempotencyService idempotency;
    private final RateLimitService rateLimit;
    private final AppProperties properties;
    private final AgentMetrics metrics;
    private final ZoneId zoneId;

    public TaskAgentService(
            @Qualifier("agentChatClient") ChatClient chatClient,
            TaskTools taskTools,
            ProjectTools projectTools,
            KnowledgeTools knowledgeTools,
            ToolCallingManager toolCallingManager,
            AgentRunStore runStore,
            ChatMemory chatMemory,
            CurrentUserService currentUser,
            ConversationIdService conversationIds,
            IdempotencyService idempotency,
            RateLimitService rateLimit,
            AppProperties properties,
            AgentMetrics metrics) {
        this.chatClient = chatClient;
        this.taskTools = taskTools;
        this.projectTools = projectTools;
        this.knowledgeTools = knowledgeTools;
        this.toolCallingManager = toolCallingManager;
        this.runStore = runStore;
        this.chatMemory = chatMemory;
        this.currentUser = currentUser;
        this.conversationIds = conversationIds;
        this.idempotency = idempotency;
        this.rateLimit = rateLimit;
        this.properties = properties;
        this.metrics = metrics;
        this.zoneId = ZoneId.of(properties.ai().defaultZoneId());
    }

    public TaskAgentResponse run(String conversationId, String userMessage, String idempotencyKey) {
        String username = currentUser.username();

        // 先限流，避免单个用户把昂贵的大模型调用和 Tomcat 工作线程全部占满。
        rateLimit.check("agent:" + username);

        // 数据库使用“用户 + 幂等键”作为作用域，防止不同用户使用相同键时相互冲突。
        String scopedIdempotencyKey = scopeIdempotencyKey(username, idempotencyKey);
        var completedRun = runStore.findByIdempotencyKey(scopedIdempotencyKey);
        if (completedRun.isPresent()) {
            return toResponse(completedRun.get());
        }

        String runId = java.util.UUID.randomUUID().toString();

        // Redis SET NX 提供快速并发拦截；MySQL 唯一约束仍是最终一致性防线。
        idempotency.reserve("agent:" + username, idempotencyKey, runId);

        ConversationIdService.ConversationRef conversation = conversationIds.resolve(conversationId);
        AgentRunContext run = new AgentRunContext(
                runId,
                conversation.externalId(),
                username,
                userMessage,
                scopedIdempotencyKey);

        runStore.create(run);
        Timer.Sample timer = metrics.start();

        try {
            ToolCallback[] callbacks = Stream.of(
                            ToolCallbacks.from(projectTools),
                            ToolCallbacks.from(taskTools),
                            ToolCallbacks.from(knowledgeTools))
                    .flatMap(Arrays::stream)
                    .toArray(ToolCallback[]::new);

            ChatOptions options = ToolCallingChatOptions.builder()
                    .toolCallbacks(callbacks)
                    .build();

            // Redis ChatMemory 中使用内部租户化 ID；接口返回的仍是用户可见会话 ID。
            List<Message> messages = new ArrayList<>(chatMemory.get(conversation.internalId()));
            messages.add(new UserMessage(buildPrompt(userMessage)));

            Prompt prompt = new Prompt(messages, options);
            ChatClientResponse response = callModel(prompt, run);
            Set<String> recordedToolResponseIds = new HashSet<>();

            while (hasToolCalls(response)) {
                checkLimits(run);

                /*
                 * 工具执行有副作用。执行前必须至少保留一次模型调用额度，
                 * 否则可能出现“工具已经执行，但没有额度生成最终回答”的半完成状态。
                 */
                if (run.modelCalls() >= properties.agent().maxModelCalls() - 1) {
                    throw new BusinessException(
                            "AGENT_LIMIT",
                            "模型调用额度不足以执行工具并生成最终回答");
                }

                ChatResponse modelResponse = requireResponse(response);
                AssistantMessage assistant = modelResponse.getResult().getOutput();

                for (AssistantMessage.ToolCall toolCall : assistant.getToolCalls()) {
                    run.toolRequest(toolCall.id(), toolCall.name(), toolCall.arguments());
                    metrics.toolCall(toolCall.name());
                }
                runStore.persist(run);

                /*
                 * ToolCallingManager 根据模型提供的工具名和 JSON 参数找到 ToolCallback，
                 * 反序列化参数并执行真实 Java 方法，再生成 ToolResponseMessage。
                 */
                ToolExecutionResult execution =
                        toolCallingManager.executeToolCalls(prompt, modelResponse);

                recordToolResults(execution, run, recordedToolResponseIds);
                runStore.persist(run);

                if (execution.returnDirect()) {
                    String directResult = latestToolResult(execution);
                    run.complete(directResult);
                    saveMemory(conversation.internalId(), userMessage, directResult);
                    runStore.persist(run);
                    metrics.finish(timer, "COMPLETED");
                    return toResponse(runStore.view(runId));
                }

                // conversationHistory 已包含原始消息、模型 Tool Call 与 Java 工具结果。
                prompt = new Prompt(execution.conversationHistory(), options);
                response = callModel(prompt, run);
            }

            String content = finalContent(response);
            run.complete(content);
            saveMemory(conversation.internalId(), userMessage, content);
            runStore.persist(run);
            metrics.finish(timer, "COMPLETED");

            log.info("Agent运行完成 runId={}, modelCalls={}, toolCalls={}",
                    runId, run.modelCalls(), run.toolCalls());
            return toResponse(runStore.view(runId));
        }
        catch (BusinessException exception) {
            AgentRunStatus status = switch (exception.getCode()) {
                case "AGENT_LIMIT" -> AgentRunStatus.LIMIT_EXCEEDED;
                case "AGENT_TIMEOUT" -> AgentRunStatus.TIMED_OUT;
                default -> AgentRunStatus.FAILED;
            };
            run.fail(status, exception.getMessage());
            runStore.persist(run);
            metrics.finish(timer, status.name());
            throw exception;
        }
        catch (RuntimeException exception) {
            run.fail(AgentRunStatus.FAILED, exception.getMessage());
            runStore.persist(run);
            metrics.finish(timer, "FAILED");
            throw new BusinessException("AI_SERVICE_ERROR", "Agent运行失败", exception);
        }
    }

    public AgentRunView getRun(String runId) {
        AgentRunView view = runStore.view(runId);
        if (!view.username().equals(currentUser.username()) && !currentUser.hasRole("ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException("无权查看该运行记录");
        }
        return view;
    }

    /** 每次只请求一次模型；工具循环由外层 while 显式驱动。 */
    private ChatClientResponse callModel(Prompt prompt, AgentRunContext run) {
        checkLimits(run);
        run.modelCall();
        metrics.modelCall();
        runStore.persist(run);

        return chatClient.prompt(prompt)
                .advisors(AdvisorParams.toolCallingAdvisorAutoRegister(false))
                .call()
                .chatClientResponse();
    }

    private void checkLimits(AgentRunContext run) {
        if (run.modelCalls() >= properties.agent().maxModelCalls()) {
            throw new BusinessException("AGENT_LIMIT", "模型调用次数达到上限");
        }
        if (run.toolCalls() > properties.agent().maxToolCalls()) {
            throw new BusinessException("AGENT_LIMIT", "工具调用次数超过上限");
        }
        if (Duration.between(run.startedAt(), Instant.now())
                .compareTo(properties.agent().maxDuration()) > 0) {
            throw new BusinessException("AGENT_TIMEOUT", "Agent运行超时");
        }
    }

    private void recordToolResults(ToolExecutionResult execution,
                                   AgentRunContext run,
                                   Set<String> recordedIds) {
        for (Message message : execution.conversationHistory()) {
            if (!(message instanceof ToolResponseMessage toolMessage)) {
                continue;
            }
            for (ToolResponseMessage.ToolResponse response : toolMessage.getResponses()) {
                if (recordedIds.add(response.id())) {
                    run.toolResult(response.id(), response.name(), response.responseData());
                }
            }
        }
    }

    private boolean hasToolCalls(ChatClientResponse response) {
        return response != null
                && response.chatResponse() != null
                && response.chatResponse().hasToolCalls();
    }

    private ChatResponse requireResponse(ChatClientResponse response) {
        if (response == null || response.chatResponse() == null
                || response.chatResponse().getResult() == null) {
            throw new BusinessException("AI_SERVICE_ERROR", "模型未返回有效响应");
        }
        return response.chatResponse();
    }

    private String finalContent(ChatClientResponse response) {
        String text = requireResponse(response).getResult().getOutput().getText();
        if (text == null || text.isBlank()) {
            throw new BusinessException("AI_SERVICE_ERROR", "模型最终回答为空");
        }
        return text;
    }

    private String latestToolResult(ToolExecutionResult execution) {
        List<Message> history = execution.conversationHistory();
        for (int index = history.size() - 1; index >= 0; index--) {
            if (history.get(index) instanceof ToolResponseMessage message
                    && !message.getResponses().isEmpty()) {
                return message.getResponses().getLast().responseData();
            }
        }
        return "工具执行完成";
    }

    private void saveMemory(String internalConversationId, String userMessage, String assistantMessage) {
        chatMemory.add(internalConversationId, new UserMessage(userMessage));
        chatMemory.add(internalConversationId, new AssistantMessage(assistantMessage));
    }

    private String buildPrompt(String message) {
        return """
                当前时间：%s
                当前时区：%s
                当前登录用户：%s

                用户请求：
                %s
                """.formatted(
                ZonedDateTime.now(zoneId).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                zoneId,
                currentUser.username(),
                message);
    }

    private String scopeIdempotencyKey(String username, String key) {
        return key == null || key.isBlank() ? null : username + ":" + key;
    }

    private TaskAgentResponse toResponse(AgentRunView view) {
        return new TaskAgentResponse(
                view.runId(),
                view.conversationId(),
                view.status(),
                view.finalContent(),
                view.modelCallCount(),
                view.toolCallCount(),
                view.completedAt());
    }
}
