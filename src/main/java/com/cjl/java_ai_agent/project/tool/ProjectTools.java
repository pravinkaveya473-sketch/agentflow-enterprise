package com.cjl.java_ai_agent.project.tool;

import com.cjl.java_ai_agent.project.dto.ProjectView;
import com.cjl.java_ai_agent.project.service.ProjectService;
import com.cjl.java_ai_agent.security.CurrentUserService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class ProjectTools {

    private final ProjectService projects;
    private final CurrentUserService currentUser;

    public ProjectTools(ProjectService projects, CurrentUserService currentUser) {
        this.projects = projects;
        this.currentUser = currentUser;
    }

    @Tool(description = "创建企业项目。项目名称必填，项目描述可选。")
    public ProjectToolResponse createProject(
            @ToolParam(description = "项目名称") String name,
            @ToolParam(description = "项目描述，可为空", required = false) String description,
            @ToolParam(description = "幂等键，可为空", required = false) String idempotencyKey) {
        try {
            return ProjectToolResponse.project("项目创建成功", ProjectView.from(projects.create(
                    currentUser.username(), name, description, idempotencyKey)));
        }
        catch (RuntimeException exception) {
            return ProjectToolResponse.failure("PROJECT_CREATE_FAILED", exception.getMessage());
        }
    }

    @Tool(description = "查询当前登录用户的全部项目。")
    public ProjectToolResponse listProjects() {
        try {
            return ProjectToolResponse.list(projects.list(currentUser.username()).stream()
                    .map(ProjectView::from)
                    .toList());
        }
        catch (RuntimeException exception) {
            return ProjectToolResponse.failure("PROJECT_LIST_FAILED", exception.getMessage());
        }
    }

    @Tool(description = "归档项目。归档后不能在该项目中创建新任务。")
    public ProjectToolResponse archiveProject(String projectId) {
        try {
            return ProjectToolResponse.project("项目已归档",
                    ProjectView.from(projects.archive(currentUser.username(), projectId)));
        }
        catch (RuntimeException exception) {
            return ProjectToolResponse.failure("PROJECT_ARCHIVE_FAILED", exception.getMessage());
        }
    }
}
