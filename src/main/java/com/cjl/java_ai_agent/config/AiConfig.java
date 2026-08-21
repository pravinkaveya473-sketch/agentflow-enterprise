package com.cjl.java_ai_agent.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class AiConfig {

    @Bean("generalChatClient")
    ChatClient generalChatClient(ChatClient.Builder builder) {
        return builder.defaultSystem("你是企业级 Java AI Agent 平台助手。默认中文回答，不伪造工具结果。")
                .build();
    }

    @Bean("agentChatClient")
    ChatClient agentChatClient(ChatClient.Builder builder) {
        return builder.defaultSystem("""
                你是企业级任务与知识 Agent。
                规则：
                1. 对创建、查询或归档项目，以及创建、查询、更新、开始、阻塞、恢复、完成、取消或删除任务的请求，必须调用真实工具。
                2. 只有工具返回 success=true 才能声称操作成功。
                3. 不得编造项目ID、任务ID、知识来源、审批状态或工具结果。
                4. 缺少必需参数时先追问，不得猜测。
                5. 删除任务属于高风险操作，必须获得有效审批。
                6. 用户询问企业知识时优先调用知识检索工具并给出来源。
                7. 工具失败后根据真实错误决定修正、追问或停止。
                8. 创建项目内任务前必须使用真实 projectId；任务阻塞必须记录具体原因。
                9. 任务所有者可更新、取消和申请删除任务；负责人可查看并执行开始、阻塞、恢复和完成操作。
                """).build();
    }

    @Bean("ragChatClient")
    ChatClient ragChatClient(ChatClient.Builder builder) {
        return builder.defaultSystem("""
                你是企业知识库问答助手。只能根据提供的检索上下文回答。
                上下文不足时明确回答“知识库中没有足够依据”，不得编造。
                回答结尾列出使用的来源文件。
                """).build();
    }

    @Bean
    ChatMemory chatMemory(ChatMemoryRepository repository, AppProperties properties) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(properties.memory().maxMessages())
                .build();
    }
}
