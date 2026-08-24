package com.cjl.java_ai_agent.approval;

import com.cjl.java_ai_agent.common.exception.BusinessException;
import com.cjl.java_ai_agent.config.AppProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ApprovalService {

    private final ApprovalRepository repository;
    private final AppProperties properties;

    public ApprovalService(ApprovalRepository repository, AppProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    @Transactional
    public ApprovalRequestEntity request(String runId, String action, String resourceId, String requester,
                                         String reason, String payloadJson) {
        return repository.save(new ApprovalRequestEntity(UUID.randomUUID().toString(), runId, action, resourceId,
                requester, reason, payloadJson, Instant.now().plus(properties.approval().ttl())));
    }

    @Transactional
    public ApprovalView approve(String id, String decidedBy, String comment) {
        ApprovalRequestEntity request = require(id);
        request.approve(decidedBy, comment);
        return ApprovalView.from(request);
    }

    @Transactional
    public ApprovalView reject(String id, String decidedBy, String comment) {
        ApprovalRequestEntity request = require(id);
        request.reject(decidedBy, comment);
        return ApprovalView.from(request);
    }

    @Transactional
    public void consumeApproved(String id, String action, String resourceId, String requester) {
        ApprovalRequestEntity request = require(id);
        if (!request.getRequestedBy().equals(requester)
                || !request.getActionType().equals(action)
                || !Objects.equals(request.getResourceId(), resourceId)) {
            throw new BusinessException("APPROVAL_REQUIRED", "审批与当前操作不匹配");
        }
        request.consume();
    }

    @Transactional
    public List<ApprovalView> pending() {
        expireDueApprovals();
        return repository.findAllByStatusOrderByRequestedAtAsc(ApprovalStatus.PENDING).stream()
                .map(ApprovalView::from)
                .toList();
    }

    @Transactional
    public int expireDueApprovals() {
        List<ApprovalRequestEntity> due = repository.findAllByStatusInAndExpiresAtLessThanEqual(
                List.of(ApprovalStatus.PENDING, ApprovalStatus.APPROVED), Instant.now());
        int changed = 0;
        for (ApprovalRequestEntity request : due) {
            if (request.expireIfNeeded()) {
                changed++;
            }
        }
        if (changed > 0) {
            repository.saveAll(due);
        }
        return changed;
    }

    private ApprovalRequestEntity require(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "审批不存在：" + id));
    }
}
