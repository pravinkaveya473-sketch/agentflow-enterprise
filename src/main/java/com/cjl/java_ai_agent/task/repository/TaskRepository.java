package com.cjl.java_ai_agent.task.repository;

import com.cjl.java_ai_agent.task.model.TaskEntity;
import com.cjl.java_ai_agent.task.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<TaskEntity, String> {

    Optional<TaskEntity> findByIdAndOwnerUsername(String id, String ownerUsername);

    Optional<TaskEntity> findByOwnerUsernameAndIdempotencyKey(String ownerUsername, String idempotencyKey);

    @Query("""
            select task from TaskEntity task
            where task.id = :id
              and (task.ownerUsername = :username or task.assigneeUsername = :username)
            """)
    Optional<TaskEntity> findAccessible(@Param("id") String id, @Param("username") String username);

    @Query("""
            select task from TaskEntity task
            where (task.ownerUsername = :username or task.assigneeUsername = :username)
              and (:projectId is null or task.projectId = :projectId)
              and (:status is null or task.status = :status)
            order by task.createdAt desc
            """)
    List<TaskEntity> searchAccessible(@Param("username") String username,
                                      @Param("projectId") String projectId,
                                      @Param("status") TaskStatus status);
}
