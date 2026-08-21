package com.cjl.java_ai_agent.dashboard;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record DashboardOverview(
        String username,
        long projectCount,
        long taskCount,
        long pendingApprovalCount,
        Map<String, Long> tasksByStatus,
        List<AgentRunSummary> recentAgentRuns,
        Instant generatedAt
) {
    public record AgentRunSummary(
            String runId,
            String status,
            String request,
            int modelCalls,
            int toolCalls,
            Instant startedAt,
            Instant completedAt
    ) {}
}
