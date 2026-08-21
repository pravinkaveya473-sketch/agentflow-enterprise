package com.cjl.java_ai_agent.agent.service;

import com.cjl.java_ai_agent.agent.dto.*;
import com.cjl.java_ai_agent.agent.model.*;
import com.cjl.java_ai_agent.agent.repository.*;
import com.cjl.java_ai_agent.agent.runtime.AgentRunContext;
import com.cjl.java_ai_agent.common.exception.BusinessException;
import com.cjl.java_ai_agent.infrastructure.redis.AgentRunCache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

@Service
public class AgentRunStore {
    private final AgentRunRepository runs;
    private final AgentStepRepository steps;
    private final AgentRunCache cache;

    public AgentRunStore(AgentRunRepository runs, AgentStepRepository steps, AgentRunCache cache) {
        this.runs = runs;
        this.steps = steps;
        this.cache = cache;
    }

    @Transactional
    public void create(AgentRunContext context) {
        runs.save(new AgentRunEntity(context.id(), context.conversationId(), context.username(),
                context.userMessage(), blank(context.idempotencyKey())));
    }

    @Transactional
    public void persist(AgentRunContext context) {
        AgentRunEntity entity = runs.findById(context.id()).orElseThrow();
        entity.update(context.status(), context.finalContent(), context.errorMessage(),
                context.modelCalls(), context.toolCalls(), context.completedAt());
        runs.save(entity);

        // 只追加本轮新增步骤，避免每次模型调用都删除并重插全部审计记录。
        long persistedCount = steps.countByRunId(context.id());
        var allSteps = context.steps();
        if (persistedCount < allSteps.size()) {
            steps.saveAll(allSteps.subList(Math.toIntExact(persistedCount), allSteps.size()));
            steps.flush();
        }

        AgentRunView view = toView(entity);
        // 只有 MySQL 事务真正提交后才更新 Redis，避免数据库回滚但缓存已成功的脏状态。
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                cache.put(view);
            }
        });
    }

    @Transactional(readOnly = true)
    public AgentRunView view(String id) {
        return cache.get(id).orElseGet(() -> {
            AgentRunEntity entity = runs.findById(id)
                    .orElseThrow(() -> new BusinessException("NOT_FOUND", "AgentRun不存在：" + id));
            AgentRunView view = toView(entity);
            cache.put(view);
            return view;
        });
    }

    @Transactional(readOnly = true)
    public Optional<AgentRunView> findByIdempotencyKey(String key) {
        return key == null || key.isBlank()
                ? Optional.empty()
                : runs.findByIdempotencyKey(key).map(this::toView);
    }

    private AgentRunView toView(AgentRunEntity entity) {
        return new AgentRunView(entity.getId(), entity.getConversationId(), entity.getUsername(),
                entity.getUserMessage(), entity.getStatus(), entity.getFinalContent(),
                entity.getErrorMessage(), entity.getModelCallCount(), entity.getToolCallCount(),
                entity.getStartedAt(), entity.getCompletedAt(),
                steps.findAllByRunIdOrderBySequenceAsc(entity.getId()).stream()
                        .map(AgentStepView::from).toList());
    }

    private String blank(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
