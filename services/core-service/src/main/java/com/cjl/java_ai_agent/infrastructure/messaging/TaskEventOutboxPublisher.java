package com.cjl.java_ai_agent.infrastructure.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
public class TaskEventOutboxPublisher {
    private final TaskEventOutboxRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public TaskEventOutboxPublisher(TaskEventOutboxRepository repository,
                                    KafkaTemplate<String, Object> kafkaTemplate,
                                    @Value("${app.kafka.task-topic:agentflow.task.lifecycle.v1}") String topic) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Scheduled(fixedDelayString = "${app.kafka.outbox-publish-interval:1s}")
    @Transactional
    public void publishPending() {
        for (TaskEventOutboxEntity outbox : repository.findTop100ByPublishedAtIsNullOrderByCreatedAtAsc()) {
            try {
                kafkaTemplate.send(topic, outbox.getAggregateId(), outbox.toEvent())
                        .get(5, TimeUnit.SECONDS);
                outbox.markPublished();
            }
            catch (Exception exception) {
                outbox.markFailed(exception);
            }
        }
    }
}
