package com.cjl.java_ai_agent.project.repository;

import com.cjl.java_ai_agent.project.model.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<ProjectEntity, String> {
    Optional<ProjectEntity> findByIdAndOwnerUsername(String id, String ownerUsername);
    Optional<ProjectEntity> findByOwnerUsernameAndIdempotencyKey(String ownerUsername, String idempotencyKey);
    List<ProjectEntity> findAllByOwnerUsernameOrderByCreatedAtDesc(String ownerUsername);
}
