package io.github.pravinkaveya473sketch.agentflow.eventservice.web;

import io.github.pravinkaveya473sketch.agentflow.eventservice.model.TaskEventRecord;
import io.github.pravinkaveya473sketch.agentflow.eventservice.repository.TaskEventRecordRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
public class TaskEventController {
    private final TaskEventRecordRepository repository;

    public TaskEventController(TaskEventRecordRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<TaskEventRecord> list(Authentication authentication) {
        return repository.findTop100ByOwnerUsernameOrderByOccurredAtDesc(authentication.getName());
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<TaskEventRecord> get(@PathVariable String eventId, Authentication authentication) {
        return repository.findById(eventId)
                .filter(event -> event.getOwnerUsername().equals(authentication.getName()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
