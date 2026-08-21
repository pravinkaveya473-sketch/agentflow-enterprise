package com.cjl.java_ai_agent.task.tool;

import com.cjl.java_ai_agent.approval.ApprovalService;
import com.cjl.java_ai_agent.security.CurrentUserService;
import com.cjl.java_ai_agent.task.dto.TaskView;
import com.cjl.java_ai_agent.task.model.TaskPriority;
import com.cjl.java_ai_agent.task.model.TaskStatus;
import com.cjl.java_ai_agent.task.service.ApprovedTaskDeletionService;
import com.cjl.java_ai_agent.task.service.TaskService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Component
public class TaskTools {

    private final TaskService tasks;
    private final CurrentUserService currentUser;
    private final ApprovalService approvals;
    private final ApprovedTaskDeletionService approvedDeletion;

    public TaskTools(TaskService tasks, CurrentUserService currentUser, ApprovalService approvals,
                     ApprovedTaskDeletionService approvedDeletion) {
        this.tasks = tasks;
        this.currentUser = currentUser;
        this.approvals = approvals;
        this.approvedDeletion = approvedDeletion;
    }

    @Tool(description = "创建任务。可关联真实项目ID；截止时间格式 yyyy-MM-dd'T'HH:mm:ss；优先级 LOW/NORMAL/HIGH/URGENT。")
    public TaskToolResponse createTask(
            @ToolParam(description = "项目ID，可为空", required = false) String projectId,
            @ToolParam(description = "任务名称") String taskName,
            @ToolParam(description = "任务描述，可为空", required = false) String description,
            @ToolParam(description = "负责人用户名，可为空，默认当前用户", required = false) String assigneeUsername,
            @ToolParam(description = "截止时间，可为空", required = false) String deadline,
            @ToolParam(description = "优先级，可为空", required = false) TaskPriority priority,
            @ToolParam(description = "幂等键，可为空", required = false) String idempotencyKey) {
        try {
            return TaskToolResponse.task("任务创建成功", TaskView.from(tasks.create(currentUser.username(),
                    projectId, taskName, description, assigneeUsername, parse(deadline), priority, idempotencyKey)));
        }
        catch (RuntimeException exception) {
            return TaskToolResponse.failure("TASK_CREATE_FAILED", exception.getMessage());
        }
    }

    @Tool(description = "查询当前登录用户的任务。projectId 和 status 可用于筛选。")
    public TaskToolResponse listTasks(
            @ToolParam(description = "项目ID，可为空", required = false) String projectId,
            @ToolParam(description = "任务状态，可为空", required = false) TaskStatus status) {
        try {
            return TaskToolResponse.list(tasks.list(currentUser.username(), projectId, status).stream()
                    .map(TaskView::from)
                    .toList());
        }
        catch (RuntimeException exception) {
            return TaskToolResponse.failure("TASK_LIST_FAILED", exception.getMessage());
        }
    }

    @Tool(description = "更新未完成任务的名称、描述、负责人、截止时间和优先级。")
    public TaskToolResponse updateTask(
            String taskId,
            String taskName,
            @ToolParam(description = "任务描述，可为空", required = false) String description,
            @ToolParam(description = "负责人用户名，可为空", required = false) String assigneeUsername,
            @ToolParam(description = "截止时间，可为空", required = false) String deadline,
            @ToolParam(description = "优先级，可为空", required = false) TaskPriority priority) {
        return execute("任务已更新", () -> tasks.update(currentUser.username(), taskId, taskName, description,
                assigneeUsername, parse(deadline), priority));
    }

    @Tool(description = "开始 CREATED 状态任务。必须使用真实任务ID。")
    public TaskToolResponse startTask(String taskId) {
        return execute("任务已开始", () -> tasks.start(currentUser.username(), taskId));
    }

    @Tool(description = "将 CREATED 或 IN_PROGRESS 状态任务标记为 BLOCKED，必须提供阻塞原因。")
    public TaskToolResponse blockTask(String taskId, String reason) {
        return execute("任务已阻塞", () -> tasks.block(currentUser.username(), taskId, reason));
    }

    @Tool(description = "恢复 BLOCKED 状态任务，恢复后状态为 IN_PROGRESS。")
    public TaskToolResponse resumeTask(String taskId) {
        return execute("任务已恢复", () -> tasks.resume(currentUser.username(), taskId));
    }

    @Tool(description = "完成 IN_PROGRESS 状态任务。CREATED 状态必须先调用 startTask。")
    public TaskToolResponse completeTask(String taskId) {
        return execute("任务已完成", () -> tasks.complete(currentUser.username(), taskId));
    }

    @Tool(description = "取消未完成任务。")
    public TaskToolResponse cancelTask(String taskId) {
        return execute("任务已取消", () -> tasks.cancel(currentUser.username(), taskId));
    }

    @Tool(description = "永久删除任务，属于高风险操作。没有审批ID时先创建审批并停止；审批通过后携带 approvalId 再调用。")
    public TaskToolResponse deleteTask(
            String taskId,
            @ToolParam(description = "已批准的审批ID；首次请求删除时为空", required = false) String approvalId) {
        String username = currentUser.username();
        try {
            tasks.require(username, taskId);
            if (approvalId == null || approvalId.isBlank()) {
                String id = approvals.request(null, "DELETE_TASK", taskId, username,
                        "永久删除任务需要人工审批", "{\"taskId\":\"" + taskId + "\"}").getId();
                return TaskToolResponse.approval(id,
                        "删除任务需要审批，请审批后使用 approvalId=" + id + " 再次执行");
            }
            approvedDeletion.delete(approvalId, taskId, username);
            return TaskToolResponse.task("任务已永久删除", null);
        }
        catch (RuntimeException exception) {
            return TaskToolResponse.failure("TASK_DELETE_FAILED", exception.getMessage());
        }
    }

    private TaskToolResponse execute(String message,
                                     java.util.function.Supplier<com.cjl.java_ai_agent.task.model.TaskEntity> action) {
        try {
            return TaskToolResponse.task(message, TaskView.from(action.get()));
        }
        catch (RuntimeException exception) {
            return TaskToolResponse.failure("TASK_OPERATION_FAILED", exception.getMessage());
        }
    }

    private LocalDateTime parse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        }
        catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("截止时间格式应为 yyyy-MM-dd'T'HH:mm:ss");
        }
    }
}
