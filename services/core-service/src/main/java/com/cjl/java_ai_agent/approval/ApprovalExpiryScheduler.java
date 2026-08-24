package com.cjl.java_ai_agent.approval;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ApprovalExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(ApprovalExpiryScheduler.class);
    private final ApprovalService approvalService;

    public ApprovalExpiryScheduler(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @Scheduled(fixedDelayString = "${app.approval.expiry-scan-interval:60s}")
    public void expireApprovals() {
        int expired = approvalService.expireDueApprovals();
        if (expired > 0) {
            log.info("已自动过期审批数量={}", expired);
        }
    }
}
