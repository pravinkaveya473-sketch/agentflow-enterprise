package io.github.pravinkaveya473sketch.agentflow.eventservice.model;

import io.github.pravinkaveya473sketch.agentflow.events.TaskLifecycleEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "task_event_record")
public class TaskEventRecord {
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
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
    @Column(name = "consumed_at", nullable = false)
    private Instant consumedAt;

    protected TaskEventRecord() {
    }

    public TaskEventRecord(TaskLifecycleEvent event) {
        this.eventId = event.eventId();
        this.eventType = event.eventType();
        this.aggregateId = event.aggregateId();
        this.ownerUsername = event.ownerUsername();
        this.projectId = event.projectId();
        this.taskName = event.taskName();
        this.beforeStatus = event.beforeStatus();
        this.afterStatus = event.afterStatus();
        this.aggregateVersion = event.aggregateVersion();
        this.occurredAt = event.occurredAt();
        this.consumedAt = Instant.now();
    }

    public String getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public String getAggregateId() { return aggregateId; }
    public String getOwnerUsername() { return ownerUsername; }
    public String getProjectId() { return projectId; }
    public String getTaskName() { return taskName; }
    public String getBeforeStatus() { return beforeStatus; }
    public String getAfterStatus() { return afterStatus; }
    public long getAggregateVersion() { return aggregateVersion; }
    public Instant getOccurredAt() { return occurredAt; }
    public Instant getConsumedAt() { return consumedAt; }
}
