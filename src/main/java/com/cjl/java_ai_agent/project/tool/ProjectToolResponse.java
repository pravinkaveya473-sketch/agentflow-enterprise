package com.cjl.java_ai_agent.project.tool;

import com.cjl.java_ai_agent.project.dto.ProjectView;

import java.util.List;

public record ProjectToolResponse(
        boolean success,
        String code,
        String message,
        ProjectView project,
        List<ProjectView> projects
) {
    public static ProjectToolResponse project(String message, ProjectView project) {
        return new ProjectToolResponse(true, "SUCCESS", message, project, List.of());
    }

    public static ProjectToolResponse list(List<ProjectView> projects) {
        return new ProjectToolResponse(true, "SUCCESS", "查询成功", null, List.copyOf(projects));
    }

    public static ProjectToolResponse failure(String code, String message) {
        return new ProjectToolResponse(false, code, message, null, List.of());
    }
}
