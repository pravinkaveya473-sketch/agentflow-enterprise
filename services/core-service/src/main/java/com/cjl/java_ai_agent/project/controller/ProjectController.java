package com.cjl.java_ai_agent.project.controller;

import com.cjl.java_ai_agent.common.response.ApiResponse;
import com.cjl.java_ai_agent.project.dto.ProjectView;
import com.cjl.java_ai_agent.project.service.ProjectService;
import com.cjl.java_ai_agent.security.CurrentUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService service;
    private final CurrentUserService currentUser;

    public ProjectController(ProjectService service, CurrentUserService currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectView>> create(
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success(ProjectView.from(service.create(currentUser.username(),
                request.name(), request.description(), idempotencyKey))));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectView>>> list() {
        return ResponseEntity.ok(ApiResponse.success(service.list(currentUser.username()).stream()
                .map(ProjectView::from)
                .toList()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectView>> update(@PathVariable String id,
                                                            @Valid @RequestBody UpdateProjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success(ProjectView.from(service.update(currentUser.username(), id,
                request.name(), request.description()))));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<ProjectView>> archive(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(ProjectView.from(service.archive(currentUser.username(), id))));
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<ProjectView>> restore(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(ProjectView.from(service.restore(currentUser.username(), id))));
    }

    public record CreateProjectRequest(
            @NotBlank @Size(max = 128) String name,
            @Size(max = 1000) String description
    ) {}

    public record UpdateProjectRequest(
            @NotBlank @Size(max = 128) String name,
            @Size(max = 1000) String description
    ) {}
}
