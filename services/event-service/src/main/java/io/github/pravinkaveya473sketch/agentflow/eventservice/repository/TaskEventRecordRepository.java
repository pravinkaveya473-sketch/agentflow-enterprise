package io.github.pravinkaveya473sketch.agentflow.eventservice.repository;

import io.github.pravinkaveya473sketch.agentflow.eventservice.model.TaskEventRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskEventRecordRepository extends JpaRepository<TaskEventRecord, String> {
    List<TaskEventRecord> findTop100ByOwnerUsernameOrderByOccurredAtDesc(String ownerUsername);
}
