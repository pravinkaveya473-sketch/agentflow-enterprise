package com.cjl.java_ai_agent.task.activity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskActivityRepository extends JpaRepository<TaskActivityEntity, Long> {
    List<TaskActivityEntity> findAllByTaskIdOrderByCreatedAtAsc(String taskId);
}
