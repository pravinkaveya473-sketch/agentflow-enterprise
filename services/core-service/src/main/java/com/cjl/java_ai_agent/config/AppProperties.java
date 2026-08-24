package com.cjl.java_ai_agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Ai ai,
        Agent agent,
        Memory memory,
        Rag rag,
        Security security,
        Idempotency idempotency,
        RateLimit rateLimit,
        Approval approval
) {
    public record Ai(String defaultZoneId) {}
    public record Agent(int maxModelCalls, int maxToolCalls, Duration maxDuration, Duration runCacheTtl) {}
    public record Memory(int maxMessages) {}
    public record Rag(int topK, double similarityThreshold, int maxContextCharacters,
                      long maxExtractedCharacters, List<String> allowedExtensions, String uploadDir) {}
    public record Security(String jwtSecret, String jwtIssuer, Duration accessTokenTtl,
                           String adminUsername, String adminPassword) {}
    public record Idempotency(Duration ttl) {}
    public record RateLimit(int requests, Duration window) {}
    public record Approval(Duration ttl) {}
}
