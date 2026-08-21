package com.cjl.java_ai_agent.task.activity;

import com.cjl.java_ai_agent.task.model.TaskStatus;

import java.time.Instant;

public record TaskActivityView(
        long id,
        String taskId,
        String actor,
        TaskActivityType type,
        TaskStatus beforeStatus,
        TaskStatus afterStatus,
        String details,
        Instant createdAt
) {
    public static TaskActivityView from(TaskActivityEntity entity) {
        return new TaskActivityView(entity.getId(), entity.getTaskId(), entity.getActor(), entity.getType(),
                entity.getBeforeStatus(), entity.getAfterStatus(), entity.getDetails(), entity.getCreatedAt());
    }
}
