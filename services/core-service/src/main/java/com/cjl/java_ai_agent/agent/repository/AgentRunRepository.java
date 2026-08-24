package com.cjl.java_ai_agent.agent.repository;
import com.cjl.java_ai_agent.agent.model.AgentRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AgentRunRepository extends JpaRepository<AgentRunEntity, String> {
    Optional<AgentRunEntity> findByIdempotencyKey(String key);
    List<AgentRunEntity> findTop10ByUsernameOrderByStartedAtDesc(String username);
}
