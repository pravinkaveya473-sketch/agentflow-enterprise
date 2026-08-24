package com.cjl.java_ai_agent.task.activity;

import com.cjl.java_ai_agent.task.model.TaskStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskActivityService {

    private final TaskActivityRepository repository;

    public TaskActivityService(TaskActivityRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void record(String taskId, String actor, TaskActivityType type,
                       TaskStatus beforeStatus, TaskStatus afterStatus, String details) {
        repository.save(new TaskActivityEntity(taskId, actor, type, beforeStatus, afterStatus, details));
    }

    @Transactional(readOnly = true)
    public List<TaskActivityView> list(String taskId) {
        return repository.findAllByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(TaskActivityView::from)
                .toList();
    }
}
