package com.cjl.java_ai_agent.agent.repository;

import com.cjl.java_ai_agent.agent.model.AgentStepEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgentStepRepository extends JpaRepository<AgentStepEntity, Long> {
    List<AgentStepEntity> findAllByRunIdOrderBySequenceAsc(String runId);
    long countByRunId(String runId);
}
