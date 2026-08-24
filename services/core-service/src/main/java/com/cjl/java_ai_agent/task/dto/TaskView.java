package com.cjl.java_ai_agent.task.dto;

import com.cjl.java_ai_agent.task.model.TaskEntity;
import com.cjl.java_ai_agent.task.model.TaskPriority;
import com.cjl.java_ai_agent.task.model.TaskStatus;

import java.time.Instant;

public record TaskView(
        String id,
        String ownerUsername,
        String projectId,
        String name,
        String description,
        String assigneeUsername,
        String deadline,
        TaskPriority priority,
        TaskStatus status,
        String blockedReason,
        long version,
        Instant createdAt,
        Instant updatedAt
) {
    public static TaskView from(TaskEntity task) {
        return new TaskView(task.getId(), task.getOwnerUsername(), task.getProjectId(), task.getName(),
                task.getDescription(), task.getAssigneeUsername(),
                task.getDeadline() == null ? null : task.getDeadline().toString(),
                task.getPriority(), task.getStatus(), task.getBlockedReason(), task.getVersion(),
                task.getCreatedAt(), task.getUpdatedAt());
    }
}
