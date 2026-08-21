package com.cjl.java_ai_agent.project.model;

import com.cjl.java_ai_agent.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

/**
 * 企业项目聚合根。任务可以归属项目，项目归档后禁止继续创建新任务。
 */
@Entity
@Table(name = "project")
public class ProjectEntity {

    @Id
    private String id;

    @Column(name = "owner_username", nullable = false, length = 64)
    private String ownerUsername;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ProjectStatus status;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "idempotency_key", length = 128)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProjectEntity() {
    }

    public ProjectEntity(String id, String ownerUsername, String name, String description,
                         String idempotencyKey) {
        this.id = requireText(id, "项目ID不能为空");
        this.ownerUsername = requireText(ownerUsername, "项目所有者不能为空");
        rename(name, description);
        this.status = ProjectStatus.ACTIVE;
        this.idempotencyKey = blankToNull(idempotencyKey);
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void rename(String name, String description) {
        ensureActive();
        String normalizedName = requireText(name, "项目名称不能为空");
        if (normalizedName.length() > 128) {
            throw new BusinessException("VALIDATION_ERROR", "项目名称不能超过128个字符");
        }
        if (description != null && description.length() > 1000) {
            throw new BusinessException("VALIDATION_ERROR", "项目描述不能超过1000个字符");
        }
        this.name = normalizedName;
        this.description = blankToNull(description);
        this.updatedAt = Instant.now();
    }

    public void archive() {
        ensureActive();
        this.status = ProjectStatus.ARCHIVED;
        this.updatedAt = Instant.now();
    }

    public void restore() {
        if (status != ProjectStatus.ARCHIVED) {
            throw new BusinessException("INVALID_PROJECT_STATE", "只有 ARCHIVED 状态项目才能恢复");
        }
        this.status = ProjectStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void ensureActive() {
        if (status == ProjectStatus.ARCHIVED) {
            throw new BusinessException("INVALID_PROJECT_STATE", "项目已归档，不能继续修改或创建任务");
        }
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
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ProjectStatus getStatus() { return status; }
    public long getVersion() { return version; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
