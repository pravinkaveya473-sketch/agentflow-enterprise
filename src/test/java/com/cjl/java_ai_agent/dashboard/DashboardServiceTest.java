package com.cjl.java_ai_agent.dashboard;

import com.cjl.java_ai_agent.agent.model.AgentRunEntity;
import com.cjl.java_ai_agent.agent.repository.AgentRunRepository;
import com.cjl.java_ai_agent.approval.ApprovalRepository;
import com.cjl.java_ai_agent.approval.ApprovalStatus;
import com.cjl.java_ai_agent.project.model.ProjectEntity;
import com.cjl.java_ai_agent.project.repository.ProjectRepository;
import com.cjl.java_ai_agent.security.CurrentUserService;
import com.cjl.java_ai_agent.task.model.TaskEntity;
import com.cjl.java_ai_agent.task.model.TaskPriority;
import com.cjl.java_ai_agent.task.repository.TaskRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardServiceTest {

    @Test
    void aggregatesOnlyCurrentUsersOperationalData() {
        ProjectRepository projects = mock(ProjectRepository.class);
        TaskRepository tasks = mock(TaskRepository.class);
        AgentRunRepository runs = mock(AgentRunRepository.class);
        ApprovalRepository approvals = mock(ApprovalRepository.class);
        CurrentUserService currentUser = mock(CurrentUserService.class);

        when(currentUser.username()).thenReturn("operator");
        when(currentUser.hasRole("APPROVER")).thenReturn(true);
        when(projects.findAllByOwnerUsernameOrderByCreatedAtDesc("operator"))
                .thenReturn(List.of(new ProjectEntity("p-1", "operator", "Release", "", null)));
        TaskEntity created = new TaskEntity("t-1", "operator", "Ship release", null,
                TaskPriority.HIGH, null);
        TaskEntity active = new TaskEntity("t-2", "operator", "Verify release", null,
                TaskPriority.NORMAL, null);
        active.start();
        when(tasks.searchAccessible("operator", null, null)).thenReturn(List.of(created, active));
        when(approvals.countByStatus(ApprovalStatus.PENDING)).thenReturn(2L);
        when(runs.findTop10ByUsernameOrderByStartedAtDesc("operator"))
                .thenReturn(List.of(new AgentRunEntity("r-1", "c-1", "operator", "Create a release plan", null)));

        DashboardOverview overview = new DashboardService(projects, tasks, runs, approvals, currentUser).overview();

        assertThat(overview.username()).isEqualTo("operator");
        assertThat(overview.projectCount()).isEqualTo(1);
        assertThat(overview.taskCount()).isEqualTo(2);
        assertThat(overview.pendingApprovalCount()).isEqualTo(2);
        assertThat(overview.tasksByStatus()).containsEntry("CREATED", 1L).containsEntry("IN_PROGRESS", 1L);
        assertThat(overview.recentAgentRuns()).hasSize(1);
    }
}
