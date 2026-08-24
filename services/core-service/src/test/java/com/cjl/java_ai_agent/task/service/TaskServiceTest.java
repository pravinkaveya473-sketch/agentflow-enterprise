package com.cjl.java_ai_agent.task.service;

import com.cjl.java_ai_agent.common.exception.BusinessException;
import com.cjl.java_ai_agent.config.AppProperties;
import com.cjl.java_ai_agent.infrastructure.messaging.TaskEventOutbox;
import com.cjl.java_ai_agent.project.service.ProjectService;
import com.cjl.java_ai_agent.security.AppUserEntity;
import com.cjl.java_ai_agent.security.AppUserRepository;
import com.cjl.java_ai_agent.security.Role;
import com.cjl.java_ai_agent.task.activity.TaskActivityService;
import com.cjl.java_ai_agent.task.model.TaskEntity;
import com.cjl.java_ai_agent.task.model.TaskPriority;
import com.cjl.java_ai_agent.task.model.TaskStatus;
import com.cjl.java_ai_agent.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock TaskRepository repository;
    @Mock ProjectService projects;
    @Mock TaskActivityService activities;
    @Mock AppUserRepository users;
    @Mock TaskEventOutbox outbox;

    private TaskService service;

    @BeforeEach
    void setUp() {
        AppProperties properties = new AppProperties(
                new AppProperties.Ai("Asia/Shanghai"),
                null, null, null, null, null, null, null);
        service = new TaskService(repository, projects, activities, users, outbox, properties);
    }

    @Test
    void shouldRejectUnknownAssigneeBeforePersistingTask() {
        when(users.findByUsername("missing-user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(
                "owner", null, "测试任务", null, "missing-user",
                LocalDateTime.now().plusDays(1), TaskPriority.NORMAL, "key-1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("负责人不存在");
    }

    @Test
    void assigneeShouldBeAbleToStartAssignedTask() {
        TaskEntity task = new TaskEntity(
                "task-1", "owner", null, "测试任务", null, "assignee",
                LocalDateTime.now().plusDays(1), TaskPriority.HIGH, null);
        when(repository.findAccessible("task-1", "assignee")).thenReturn(Optional.of(task));
        when(repository.saveAndFlush(any(TaskEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskEntity started = service.start("assignee", "task-1");

        assertThat(started.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        verify(activities).record("task-1", "assignee", com.cjl.java_ai_agent.task.activity.TaskActivityType.STARTED,
                TaskStatus.CREATED, TaskStatus.IN_PROGRESS, "开始执行任务");
        verify(outbox).append("TASK_STARTED", started, TaskStatus.CREATED);
    }

    @Test
    void listShouldIncludeOwnedAndAssignedTasksThroughAccessibleQuery() {
        when(repository.searchAccessible("alice", null, TaskStatus.BLOCKED)).thenReturn(List.of());

        assertThat(service.list("alice", null, TaskStatus.BLOCKED)).isEmpty();

        verify(repository).searchAccessible("alice", null, TaskStatus.BLOCKED);
    }

    @Test
    void shouldAcceptEnabledExistingAssigneeWhenCreatingTask() {
        when(users.findByUsername("alice")).thenReturn(Optional.of(
                new AppUserEntity("user-1", "alice", "hash", Role.USER)));
        when(repository.saveAndFlush(any(TaskEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskEntity created = service.create(
                "owner", null, "测试任务", "说明", "alice",
                LocalDateTime.now().plusDays(1), TaskPriority.HIGH, null);

        assertThat(created.getAssigneeUsername()).isEqualTo("alice");
        assertThat(created.getStatus()).isEqualTo(TaskStatus.CREATED);
    }
}
