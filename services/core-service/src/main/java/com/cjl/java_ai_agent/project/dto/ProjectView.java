package com.cjl.java_ai_agent.project.dto;

import com.cjl.java_ai_agent.project.model.ProjectEntity;
import com.cjl.java_ai_agent.project.model.ProjectStatus;

import java.time.Instant;

public record ProjectView(
        String id,
        String name,
        String description,
        ProjectStatus status,
        long version,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProjectView from(ProjectEntity project) {
        return new ProjectView(project.getId(), project.getName(), project.getDescription(),
                project.getStatus(), project.getVersion(), project.getCreatedAt(), project.getUpdatedAt());
    }
}
