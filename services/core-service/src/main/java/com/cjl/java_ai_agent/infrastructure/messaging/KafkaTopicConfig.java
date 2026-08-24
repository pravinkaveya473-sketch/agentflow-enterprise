package com.cjl.java_ai_agent.infrastructure.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration(proxyBeanMethods = false)
public class KafkaTopicConfig {
    @Bean
    NewTopic taskLifecycleTopic() {
        return TopicBuilder.name("agentflow.task.lifecycle.v1").partitions(3).replicas(1).build();
    }
}
