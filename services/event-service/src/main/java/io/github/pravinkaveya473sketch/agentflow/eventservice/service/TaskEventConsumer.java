package io.github.pravinkaveya473sketch.agentflow.eventservice.service;

import io.github.pravinkaveya473sketch.agentflow.events.TaskLifecycleEvent;
import io.github.pravinkaveya473sketch.agentflow.eventservice.model.TaskEventRecord;
import io.github.pravinkaveya473sketch.agentflow.eventservice.repository.TaskEventRecordRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskEventConsumer {
    private final TaskEventRecordRepository repository;

    public TaskEventConsumer(TaskEventRecordRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "${app.kafka.task-topic:agentflow.task.lifecycle.v1}")
    @Transactional
    public void consume(TaskLifecycleEvent event) {
        if (repository.existsById(event.eventId())) {
            return;
        }
        try {
            repository.saveAndFlush(new TaskEventRecord(event));
        }
        catch (DataIntegrityViolationException duplicateDelivery) {
            // Kafka is at-least-once; the event_id primary key makes re-delivery idempotent.
        }
    }
}
