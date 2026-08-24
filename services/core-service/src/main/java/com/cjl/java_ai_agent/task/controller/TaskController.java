package com.cjl.java_ai_agent.task.controller;

import com.cjl.java_ai_agent.common.response.ApiResponse;
import com.cjl.java_ai_agent.security.CurrentUserService;
import com.cjl.java_ai_agent.task.activity.TaskActivityService;
import com.cjl.java_ai_agent.task.activity.TaskActivityView;
import com.cjl.java_ai_agent.task.dto.TaskView;
import com.cjl.java_ai_agent.task.model.TaskPriority;
import com.cjl.java_ai_agent.task.model.TaskStatus;
import com.cjl.java_ai_agent.task.service.TaskService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService service;
    private final TaskActivityService activities;
    private final CurrentUserService currentUser;

    public TaskController(TaskService service, TaskActivityService activities, CurrentUserService currentUser) {
        this.service = service;
        this.activities = activities;
        this.currentUser = currentUser;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskView>> create(
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreateTaskRequest request) {
        return ResponseEntity.ok(ApiResponse.success(TaskView.from(service.create(currentUser.username(),
                request.projectId(), request.name(), request.description(), request.assigneeUsername(),
                request.deadline(), request.priority(), idempotencyKey))));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskView>>> list(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) TaskStatus status) {
        return ResponseEntity.ok(ApiResponse.success(service.list(currentUser.username(), projectId, status).stream()
                .map(TaskView::from)
                .toList()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskView>> update(@PathVariable String id,
                                                         @Valid @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(ApiResponse.success(TaskView.from(service.update(currentUser.username(), id,
                request.name(), request.description(), request.assigneeUsername(), request.deadline(),
                request.priority()))));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ApiResponse<TaskView>> start(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(TaskView.from(service.start(currentUser.username(), id))));
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<ApiResponse<TaskView>> block(@PathVariable String id,
                                                        @Valid @RequestBody BlockTaskRequest request) {
        return ResponseEntity.ok(ApiResponse.success(TaskView.from(service.block(currentUser.username(), id,
                request.reason()))));
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<ApiResponse<TaskView>> resume(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(TaskView.from(service.resume(currentUser.username(), id))));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<TaskView>> complete(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(TaskView.from(service.complete(currentUser.username(), id))));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<TaskView>> cancel(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(TaskView.from(service.cancel(currentUser.username(), id))));
    }

    @GetMapping("/{id}/activities")
    public ResponseEntity<ApiResponse<List<TaskActivityView>>> activities(@PathVariable String id) {
        service.requireAccessible(currentUser.username(), id);
        return ResponseEntity.ok(ApiResponse.success(activities.list(id)));
    }

    public record CreateTaskRequest(
            @Size(max = 64) String projectId,
            @NotBlank @Size(max = 255) String name,
            @Size(max = 2000) String description,
            @Size(max = 64) String assigneeUsername,
            LocalDateTime deadline,
            TaskPriority priority
    ) {}

    public record UpdateTaskRequest(
            @NotBlank @Size(max = 255) String name,
            @Size(max = 2000) String description,
            @Size(max = 64) String assigneeUsername,
            LocalDateTime deadline,
            TaskPriority priority
    ) {}

    public record BlockTaskRequest(@NotBlank @Size(max = 500) String reason) {}
}
