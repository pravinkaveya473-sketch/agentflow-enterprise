package com.cjl.java_ai_agent.task.model;

import com.cjl.java_ai_agent.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskEntityTest {

    @Test
    void shouldFollowValidLifecycle() {
        TaskEntity task = task("task-1");

        assertEquals(TaskStatus.CREATED, task.getStatus());
        task.start();
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        task.complete();
        assertEquals(TaskStatus.COMPLETED, task.getStatus());
    }

    @Test
    void shouldBlockAndResumeTask() {
        TaskEntity task = task("task-2");
        task.start();

        task.block("等待数据库权限");
        assertEquals(TaskStatus.BLOCKED, task.getStatus());
        assertEquals("等待数据库权限", task.getBlockedReason());

        task.resume();
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        assertNull(task.getBlockedReason());
    }

    @Test
    void shouldRejectCompletingCreatedTask() {
        TaskEntity task = task("task-3");

        BusinessException exception = assertThrows(BusinessException.class, task::complete);
        assertEquals("INVALID_TASK_STATE", exception.getCode());
    }

    @Test
    void shouldRejectEditingTerminalTask() {
        TaskEntity task = task("task-4");
        task.cancel();

        BusinessException exception = assertThrows(BusinessException.class,
                () -> task.update("新名称", null, "cjl", null, TaskPriority.NORMAL));
        assertEquals("INVALID_TASK_STATE", exception.getCode());
    }

    private TaskEntity task(String id) {
        return new TaskEntity(id, "cjl", "project-1", "学习 Java Agent", "项目驱动学习",
                "cjl", LocalDateTime.now().plusDays(1), TaskPriority.HIGH, "idem-" + id);
    }
}
