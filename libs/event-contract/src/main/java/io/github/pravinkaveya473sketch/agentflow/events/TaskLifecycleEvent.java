package io.github.pravinkaveya473sketch.agentflow.events;

import java.time.Instant;

public record TaskLifecycleEvent(
        String eventId,
        String eventType,
        String aggregateId,
        String ownerUsername,
        String projectId,
        String taskName,
        String beforeStatus,
        String afterStatus,
        long aggregateVersion,
        Instant occurredAt
) {
}
