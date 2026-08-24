package com.cjl.java_ai_agent.approval;

import com.cjl.java_ai_agent.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

@Entity
@Table(name = "approval_request")
public class ApprovalRequestEntity {

    @Id
    private String id;

    @Column(name = "run_id")
    private String runId;

    @Column(name = "action_type", nullable = false, length = 64)
    private String actionType;

    @Column(name = "resource_id", length = 128)
    private String resourceId;

    @Column(name = "requested_by", nullable = false, length = 64)
    private String requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ApprovalStatus status;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(name = "payload_json", columnDefinition = "MEDIUMTEXT")
    private String payloadJson;

    @Column(name = "decided_by", length = 64)
    private String decidedBy;

    @Column(name = "decision_comment", length = 500)
    private String decisionComment;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected ApprovalRequestEntity() {
    }

    public ApprovalRequestEntity(String id, String runId, String actionType, String resourceId,
                                 String requestedBy, String reason, String payloadJson, Instant expiresAt) {
        this.id = id;
        this.runId = runId;
        this.actionType = actionType;
        this.resourceId = resourceId;
        this.requestedBy = requestedBy;
        this.reason = reason;
        this.payloadJson = payloadJson;
        this.status = ApprovalStatus.PENDING;
        this.requestedAt = Instant.now();
        this.expiresAt = expiresAt;
    }

    public void approve(String by, String comment) {
        ensurePending();
        status = ApprovalStatus.APPROVED;
        decidedBy = by;
        decisionComment = comment;
        decidedAt = Instant.now();
    }

    public void reject(String by, String comment) {
        ensurePending();
        status = ApprovalStatus.REJECTED;
        decidedBy = by;
        decisionComment = comment;
        decidedAt = Instant.now();
    }

    public void consume() {
        expireIfNeeded();
        if (status != ApprovalStatus.APPROVED) {
            throw new BusinessException("APPROVAL_REQUIRED", "审批未通过或已失效，当前状态=" + status);
        }
        status = ApprovalStatus.CONSUMED;
        consumedAt = Instant.now();
    }

    public boolean expireIfNeeded() {
        if ((status == ApprovalStatus.PENDING || status == ApprovalStatus.APPROVED)
                && !expiresAt.isAfter(Instant.now())) {
            status = ApprovalStatus.EXPIRED;
            return true;
        }
        return false;
    }

    private void ensurePending() {
        expireIfNeeded();
        if (status != ApprovalStatus.PENDING) {
            throw new BusinessException("INVALID_APPROVAL_STATE", "审批当前状态=" + status);
        }
    }

    public String getId() { return id; }
    public String getRunId() { return runId; }
    public String getActionType() { return actionType; }
    public String getResourceId() { return resourceId; }
    public String getRequestedBy() { return requestedBy; }
    public ApprovalStatus getStatus() { return status; }
    public String getReason() { return reason; }
    public String getPayloadJson() { return payloadJson; }
    public String getDecidedBy() { return decidedBy; }
    public String getDecisionComment() { return decisionComment; }
    public Instant getRequestedAt() { return requestedAt; }
    public Instant getDecidedAt() { return decidedAt; }
    public Instant getConsumedAt() { return consumedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public long getVersion() { return version; }
}
