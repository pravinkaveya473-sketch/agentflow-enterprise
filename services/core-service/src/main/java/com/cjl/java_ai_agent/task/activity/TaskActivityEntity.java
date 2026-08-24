package com.cjl.java_ai_agent.task.activity;

import com.cjl.java_ai_agent.task.model.TaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "task_activity")
public class TaskActivityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false, length = 64)
    private String taskId;

    @Column(nullable = false, length = 64)
    private String actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 32)
    private TaskActivityType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "before_status", length = 32)
    private TaskStatus beforeStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "after_status", length = 32)
    private TaskStatus afterStatus;

    @Column(length = 1000)
    private String details;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TaskActivityEntity() {
    }

    public TaskActivityEntity(String taskId, String actor, TaskActivityType type,
                              TaskStatus beforeStatus, TaskStatus afterStatus, String details) {
        this.taskId = taskId;
        this.actor = actor;
        this.type = type;
        this.beforeStatus = beforeStatus;
        this.afterStatus = afterStatus;
        this.details = details;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getTaskId() { return taskId; }
    public String getActor() { return actor; }
    public TaskActivityType getType() { return type; }
    public TaskStatus getBeforeStatus() { return beforeStatus; }
    public TaskStatus getAfterStatus() { return afterStatus; }
    public String getDetails() { return details; }
    public Instant getCreatedAt() { return createdAt; }
}
