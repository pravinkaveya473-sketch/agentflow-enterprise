package com.cjl.java_ai_agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class AgentFlowApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgentFlowApplication.class, args);
    }
}
