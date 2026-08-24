package com.cjl.java_ai_agent.infrastructure.messaging;

import com.cjl.java_ai_agent.task.model.TaskEntity;
import com.cjl.java_ai_agent.task.model.TaskStatus;
import org.springframework.stereotype.Service;

@Service
public class TaskEventOutbox {
    private final TaskEventOutboxRepository repository;

    public TaskEventOutbox(TaskEventOutboxRepository repository) {
        this.repository = repository;
    }

    public void append(String eventType, TaskEntity task, TaskStatus beforeStatus) {
        repository.save(TaskEventOutboxEntity.of(eventType, task, beforeStatus));
    }
}
