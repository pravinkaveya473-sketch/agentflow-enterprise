package com.cjl.java_ai_agent.infrastructure.messaging;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskEventOutboxRepository extends JpaRepository<TaskEventOutboxEntity, String> {
    List<TaskEventOutboxEntity> findTop100ByPublishedAtIsNullOrderByCreatedAtAsc();
}
