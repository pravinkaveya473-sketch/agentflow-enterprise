package com.cjl.java_ai_agent.dashboard;

import com.cjl.java_ai_agent.agent.repository.AgentRunRepository;
import com.cjl.java_ai_agent.approval.ApprovalRepository;
import com.cjl.java_ai_agent.approval.ApprovalStatus;
import com.cjl.java_ai_agent.project.repository.ProjectRepository;
import com.cjl.java_ai_agent.security.CurrentUserService;
import com.cjl.java_ai_agent.task.model.TaskStatus;
import com.cjl.java_ai_agent.task.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DashboardService {

    private final ProjectRepository projects;
    private final TaskRepository tasks;
    private final AgentRunRepository runs;
    private final ApprovalRepository approvals;
    private final CurrentUserService currentUser;

    public DashboardService(ProjectRepository projects, TaskRepository tasks,
                            AgentRunRepository runs, ApprovalRepository approvals,
                            CurrentUserService currentUser) {
        this.projects = projects;
        this.tasks = tasks;
        this.runs = runs;
        this.approvals = approvals;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public DashboardOverview overview() {
        String username = currentUser.username();
        var accessibleTasks = tasks.searchAccessible(username, null, null);
        Map<String, Long> tasksByStatus = new LinkedHashMap<>();
        for (TaskStatus status : TaskStatus.values()) {
            long count = accessibleTasks.stream().filter(task -> task.getStatus() == status).count();
            tasksByStatus.put(status.name(), count);
        }

        long pendingApprovals = currentUser.hasRole("APPROVER") || currentUser.hasRole("ADMIN")
                ? approvals.countByStatus(ApprovalStatus.PENDING)
                : 0;

        var recentRuns = runs.findTop10ByUsernameOrderByStartedAtDesc(username).stream()
                .map(run -> new DashboardOverview.AgentRunSummary(
                        run.getId(), run.getStatus().name(), abbreviate(run.getUserMessage()),
                        run.getModelCallCount(), run.getToolCallCount(),
                        run.getStartedAt(), run.getCompletedAt()))
                .toList();

        return new DashboardOverview(
                username,
                projects.findAllByOwnerUsernameOrderByCreatedAtDesc(username).size(),
                accessibleTasks.size(),
                pendingApprovals,
                tasksByStatus,
                recentRuns,
                Instant.now());
    }

    private String abbreviate(String value) {
        if (value == null || value.length() <= 160) {
            return value;
        }
        return value.substring(0, 157) + "...";
    }
}
