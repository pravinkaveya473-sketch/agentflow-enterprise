package com.cjl.java_ai_agent.approval;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface ApprovalRepository extends JpaRepository<ApprovalRequestEntity, String> {
    List<ApprovalRequestEntity> findAllByStatusOrderByRequestedAtAsc(ApprovalStatus status);
    List<ApprovalRequestEntity> findAllByStatusInAndExpiresAtLessThanEqual(
            Collection<ApprovalStatus> statuses, Instant expiresAt);
}
