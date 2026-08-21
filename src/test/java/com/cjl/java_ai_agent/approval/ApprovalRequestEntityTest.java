package com.cjl.java_ai_agent.approval;

import com.cjl.java_ai_agent.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApprovalRequestEntityTest {

    @Test
    void shouldApproveAndConsumeOnlyOnce() {
        ApprovalRequestEntity approval = approval(Instant.now().plusSeconds(60));

        approval.approve("reviewer", "同意删除");
        approval.consume();
        assertEquals(ApprovalStatus.CONSUMED, approval.getStatus());

        assertThrows(BusinessException.class, approval::consume);
    }

    @Test
    void shouldRejectExpiredApproval() {
        ApprovalRequestEntity approval = approval(Instant.now().minusSeconds(1));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> approval.approve("reviewer", "同意"));
        assertEquals("INVALID_APPROVAL_STATE", exception.getCode());
        assertEquals(ApprovalStatus.EXPIRED, approval.getStatus());
    }

    private ApprovalRequestEntity approval(Instant expiresAt) {
        return new ApprovalRequestEntity("approval-1", "run-1", "DELETE_TASK", "task-1",
                "cjl", "删除任务", "{}", expiresAt);
    }
}
