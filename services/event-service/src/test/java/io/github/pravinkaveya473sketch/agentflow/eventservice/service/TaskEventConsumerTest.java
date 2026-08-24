package io.github.pravinkaveya473sketch.agentflow.eventservice.service;

import io.github.pravinkaveya473sketch.agentflow.events.TaskLifecycleEvent;
import io.github.pravinkaveya473sketch.agentflow.eventservice.repository.TaskEventRecordRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskEventConsumerTest {
    @Test
    void ignoresAnAlreadyConsumedEvent() {
        TaskEventRecordRepository repository = mock(TaskEventRecordRepository.class);
        TaskLifecycleEvent event = new TaskLifecycleEvent(
                "event-1", "TASK_CREATED", "task-1", "chenjiale", null,
                "Kafka integration", null, "CREATED", 0L, Instant.parse("2026-08-24T08:00:00Z"));
        when(repository.existsById("event-1")).thenReturn(true);

        new TaskEventConsumer(repository).consume(event);

        verify(repository, never()).saveAndFlush(org.mockito.ArgumentMatchers.any());
    }
}
