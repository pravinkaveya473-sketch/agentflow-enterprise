package com.cjl.java_ai_agent.infrastructure.messaging;

import com.cjl.java_ai_agent.task.model.TaskEntity;
import com.cjl.java_ai_agent.task.model.TaskPriority;
import com.cjl.java_ai_agent.task.model.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TaskEventOutboxEntityTest {
    @Test
    void deletedEventMustNotExposeAnAfterStatus() {
        TaskEntity task = new TaskEntity("task-1", "alice", "project-1", "Delete me", null,
                "alice", LocalDateTime.now().plusDays(1), TaskPriority.NORMAL, null);

        TaskEventOutboxEntity outbox = TaskEventOutboxEntity.of("TASK_DELETED", task, TaskStatus.CREATED);

        assertThat(outbox.toEvent().eventId()).isNotBlank();
        assertThat(outbox.toEvent().beforeStatus()).isEqualTo("CREATED");
        assertThat(outbox.toEvent().afterStatus()).isNull();
    }
}
