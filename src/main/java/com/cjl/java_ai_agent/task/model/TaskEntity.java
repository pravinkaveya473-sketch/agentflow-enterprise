package com.cjl.java_ai_agent.task.model;

import com.cjl.java_ai_agent.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * 企业任务聚合根。状态迁移只能通过领域方法完成，避免 Controller、Tool 或 Agent 绕过规则。
 */
@Entity
@Table(name = "task")
public class TaskEntity {

    @Id
    private String id;

    @Column(name = "owner_username", nullable = false, length = 64)
    private String ownerUsername;

    @Column(name = "project_id", length = 64)
    private String projectId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(name = "assignee_username", length = 64)
    private String assigneeUsername;

    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TaskPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TaskStatus status;

    @Column(name = "blocked_reason", length = 500)
    private String blockedReason;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "idempotency_key", length = 128)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected TaskEntity() {
    }

    public TaskEntity(String id, String ownerUsername, String projectId, String name, String description,
                      String assigneeUsername, LocalDateTime deadline, TaskPriority priority,
                      String idempotencyKey) {
        this.id = requireText(id, "任务ID不能为空");
        this.ownerUsername = requireText(ownerUsername, "任务所有者不能为空");
        this.projectId = blankToNull(projectId);
        applyDetails(name, description, assigneeUsername, deadline, priority);
        this.status = TaskStatus.CREATED;
        this.idempotencyKey = blankToNull(idempotencyKey);
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    /** 兼容旧测试和迁移前调用。 */
    public TaskEntity(String id, String ownerUsername, String name, LocalDateTime deadline,
                      TaskPriority priority, String idempotencyKey) {
        this(id, ownerUsername, null, name, null, ownerUsername, deadline, priority, idempotencyKey);
    }

    public void update(String name, String description, String assigneeUsername,
                       LocalDateTime deadline, TaskPriority priority) {
        ensureNotTerminal();
        applyDetails(name, description, assigneeUsername, deadline, priority);
        this.updatedAt = Instant.now();
    }

    public void start() {
        if (status != TaskStatus.CREATED) {
            throw invalidState("只有 CREATED 状态的任务才能开始");
        }
        status = TaskStatus.IN_PROGRESS;
        blockedReason = null;
        updatedAt = Instant.now();
    }

    public void block(String reason) {
        if (status != TaskStatus.CREATED && status != TaskStatus.IN_PROGRESS) {
            throw invalidState("只有 CREATED 或 IN_PROGRESS 状态的任务才能阻塞");
        }
        String normalizedReason = requireText(reason, "阻塞原因不能为空");
        if (normalizedReason.length() > 500) {
            throw new BusinessException("VALIDATION_ERROR", "阻塞原因不能超过500个字符");
        }
        status = TaskStatus.BLOCKED;
        blockedReason = normalizedReason;
        updatedAt = Instant.now();
    }

    public void resume() {
        if (status != TaskStatus.BLOCKED) {
            throw invalidState("只有 BLOCKED 状态的任务才能恢复");
        }
        status = TaskStatus.IN_PROGRESS;
        blockedReason = null;
        updatedAt = Instant.now();
    }

    public void complete() {
        if (status != TaskStatus.IN_PROGRESS) {
            throw invalidState("只有 IN_PROGRESS 状态的任务才能完成");
        }
        status = TaskStatus.COMPLETED;
        blockedReason = null;
        updatedAt = Instant.now();
    }

    public void cancel() {
        ensureNotTerminal();
        status = TaskStatus.CANCELLED;
        blockedReason = null;
        updatedAt = Instant.now();
    }

    private void applyDetails(String name, String description, String assigneeUsername,
                              LocalDateTime deadline, TaskPriority priority) {
        String normalizedName = requireText(name, "任务名称不能为空");
        if (normalizedName.length() > 255) {
            throw new BusinessException("VALIDATION_ERROR", "任务名称不能超过255个字符");
        }
        if (description != null && description.length() > 2000) {
            throw new BusinessException("VALIDATION_ERROR", "任务描述不能超过2000个字符");
        }
        this.name = normalizedName;
        this.description = blankToNull(description);
        this.assigneeUsername = blankToNull(assigneeUsername) == null
                ? ownerUsername
                : assigneeUsername.trim();
        this.deadline = deadline;
        this.priority = priority == null ? TaskPriority.NORMAL : priority;
    }

    private void ensureNotTerminal() {
        if (status == TaskStatus.COMPLETED || status == TaskStatus.CANCELLED) {
            throw invalidState("终态任务不能继续修改");
        }
    }

    private BusinessException invalidState(String message) {
        return new BusinessException("INVALID_TASK_STATE", message + "，当前状态=" + status);
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("VALIDATION_ERROR", message);
        }
        return value.trim();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public String getId() { return id; }
    public String getOwnerUsername() { return ownerUsername; }
    public String getProjectId() { return projectId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getAssigneeUsername() { return assigneeUsername; }
    public LocalDateTime getDeadline() { return deadline; }
    public TaskPriority getPriority() { return priority; }
    public TaskStatus getStatus() { return status; }
    public String getBlockedReason() { return blockedReason; }
    public long getVersion() { return version; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
