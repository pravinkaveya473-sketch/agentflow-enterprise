package io.github.pravinkaveya473sketch.agentflow.eventservice.model;

import io.github.pravinkaveya473sketch.agentflow.events.TaskLifecycleEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TaskEventRecordTest {
    @Test
    void mapsStableEventIdentityForIdempotentConsumption() {
        Instant occurredAt = Instant.parse("2026-08-24T00:00:00Z");
        TaskLifecycleEvent event = new TaskLifecycleEvent(
                "event-1", "TASK_COMPLETED", "task-1", "alice", "project-1",
                "Ship release", "IN_PROGRESS", "COMPLETED", 3L, occurredAt);

        TaskEventRecord record = new TaskEventRecord(event);

        assertThat(record.getEventId()).isEqualTo("event-1");
        assertThat(record.getOwnerUsername()).isEqualTo("alice");
        assertThat(record.getAggregateVersion()).isEqualTo(3L);
        assertThat(record.getOccurredAt()).isEqualTo(occurredAt);
    }
}
