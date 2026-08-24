package com.cjl.java_ai_agent.infrastructure.messaging;

import com.cjl.java_ai_agent.task.model.TaskEntity;
import com.cjl.java_ai_agent.task.model.TaskStatus;
import io.github.pravinkaveya473sketch.agentflow.events.TaskLifecycleEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "task_event_outbox")
public class TaskEventOutboxEntity {
    @Id
    @Column(name = "event_id", length = 64)
    private String eventId;
    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;
    @Column(name = "aggregate_id", nullable = false, length = 64)
    private String aggregateId;
    @Column(name = "owner_username", nullable = false, length = 64)
    private String ownerUsername;
    @Column(name = "project_id", length = 64)
    private String projectId;
    @Column(name = "task_name", nullable = false, length = 255)
    private String taskName;
    @Column(name = "before_status", length = 32)
    private String beforeStatus;
    @Column(name = "after_status", length = 32)
    private String afterStatus;
    @Column(name = "aggregate_version", nullable = false)
    private long aggregateVersion;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "published_at")
    private Instant publishedAt;
    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;
    @Column(name = "last_error", length = 1000)
    private String lastError;

    protected TaskEventOutboxEntity() {
    }

    public static TaskEventOutboxEntity of(String eventType, TaskEntity task, TaskStatus beforeStatus) {
        TaskEventOutboxEntity outbox = new TaskEventOutboxEntity();
        outbox.eventId = UUID.randomUUID().toString();
        outbox.eventType = eventType;
        outbox.aggregateId = task.getId();
        outbox.ownerUsername = task.getOwnerUsername();
        outbox.projectId = task.getProjectId();
        outbox.taskName = task.getName();
        outbox.beforeStatus = beforeStatus == null ? null : beforeStatus.name();
        outbox.afterStatus = "TASK_DELETED".equals(eventType) ? null : task.getStatus().name();
        outbox.aggregateVersion = task.getVersion();
        outbox.createdAt = Instant.now();
        return outbox;
    }

    public TaskLifecycleEvent toEvent() {
        return new TaskLifecycleEvent(eventId, eventType, aggregateId, ownerUsername, projectId,
                taskName, beforeStatus, afterStatus, aggregateVersion, createdAt);
    }

    public void markPublished() {
        this.publishedAt = Instant.now();
        this.lastError = null;
    }

    public void markFailed(Exception exception) {
        this.attemptCount++;
        String message = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
        this.lastError = message.substring(0, Math.min(message.length(), 1000));
    }

    public String getEventId() { return eventId; }
    public String getAggregateId() { return aggregateId; }
    public Instant getPublishedAt() { return publishedAt; }
    public int getAttemptCount() { return attemptCount; }
    public String getLastError() { return lastError; }
}
