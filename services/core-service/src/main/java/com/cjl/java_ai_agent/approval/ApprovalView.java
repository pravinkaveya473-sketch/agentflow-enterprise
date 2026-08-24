package com.cjl.java_ai_agent.approval;

import java.time.Instant;

public record ApprovalView(
        String id,
        String runId,
        String actionType,
        String resourceId,
        String requestedBy,
        ApprovalStatus status,
        String reason,
        String payloadJson,
        String decidedBy,
        String decisionComment,
        Instant requestedAt,
        Instant decidedAt,
        Instant consumedAt,
        Instant expiresAt,
        long version
) {
    public static ApprovalView from(ApprovalRequestEntity entity) {
        return new ApprovalView(entity.getId(), entity.getRunId(), entity.getActionType(), entity.getResourceId(),
                entity.getRequestedBy(), entity.getStatus(), entity.getReason(), entity.getPayloadJson(),
                entity.getDecidedBy(), entity.getDecisionComment(), entity.getRequestedAt(),
                entity.getDecidedAt(), entity.getConsumedAt(), entity.getExpiresAt(), entity.getVersion());
    }
}
