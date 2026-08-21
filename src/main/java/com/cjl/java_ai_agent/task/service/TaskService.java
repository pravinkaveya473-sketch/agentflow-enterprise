package com.cjl.java_ai_agent.task.service;

import com.cjl.java_ai_agent.common.exception.BusinessException;
import com.cjl.java_ai_agent.project.service.ProjectService;
import com.cjl.java_ai_agent.security.AppUserRepository;
import com.cjl.java_ai_agent.task.activity.TaskActivityService;
import com.cjl.java_ai_agent.task.activity.TaskActivityType;
import com.cjl.java_ai_agent.task.model.TaskEntity;
import com.cjl.java_ai_agent.task.model.TaskPriority;
import com.cjl.java_ai_agent.task.model.TaskStatus;
import com.cjl.java_ai_agent.task.repository.TaskRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository repository;
    private final ProjectService projects;
    private final TaskActivityService activities;
    private final AppUserRepository users;
    private final ZoneId zoneId;

    public TaskService(TaskRepository repository,
                       ProjectService projects,
                       TaskActivityService activities,
                       AppUserRepository users,
                       com.cjl.java_ai_agent.config.AppProperties properties) {
        this.repository = repository;
        this.projects = projects;
        this.activities = activities;
        this.users = users;
        this.zoneId = ZoneId.of(properties.ai().defaultZoneId());
    }

    @Transactional
    public TaskEntity create(String owner, String projectId, String name, String description,
                             String assigneeUsername, LocalDateTime deadline,
                             TaskPriority priority, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existing = repository.findByOwnerUsernameAndIdempotencyKey(owner, idempotencyKey.trim());
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        validateDeadline(deadline);
        validateProject(owner, projectId);
        String validatedAssignee = validateAssignee(owner, assigneeUsername);

        TaskEntity task = repository.saveAndFlush(new TaskEntity(UUID.randomUUID().toString(), owner,
                blankToNull(projectId), name, description, validatedAssignee, deadline, priority,
                blankToNull(idempotencyKey)));
        activities.record(task.getId(), owner, TaskActivityType.CREATED, null, task.getStatus(),
                "创建任务：" + task.getName());
        return task;
    }

    /** 兼容原有 Agent Tool 和测试调用。 */
    @Transactional
    public TaskEntity create(String owner, String name, LocalDateTime deadline,
                             TaskPriority priority, String idempotencyKey) {
        return create(owner, null, name, null, owner, deadline, priority, idempotencyKey);
    }

    @Transactional(readOnly = true)
    public List<TaskEntity> list(String owner, String projectId, TaskStatus status) {
        return repository.searchAccessible(owner, blankToNull(projectId), status);
    }

    @Transactional(readOnly = true)
    public List<TaskEntity> list(String owner) {
        return list(owner, null, null);
    }

    @Transactional
    public TaskEntity update(String owner, String id, String name, String description,
                             String assigneeUsername, LocalDateTime deadline, TaskPriority priority) {
        validateDeadline(deadline);
        TaskEntity task = require(owner, id);
        String validatedAssignee = validateAssignee(owner, assigneeUsername);
        TaskStatus before = task.getStatus();
        task.update(name, description, validatedAssignee, deadline, priority);
        TaskEntity saved = flushSafely(task);
        activities.record(id, owner, TaskActivityType.UPDATED, before, saved.getStatus(),
                "更新任务基本信息");
        return saved;
    }

    @Transactional
    public TaskEntity start(String owner, String id) {
        TaskEntity task = requireAccessible(owner, id);
        TaskStatus before = task.getStatus();
        task.start();
        TaskEntity saved = flushSafely(task);
        activities.record(id, owner, TaskActivityType.STARTED, before, saved.getStatus(), "开始执行任务");
        return saved;
    }

    @Transactional
    public TaskEntity block(String owner, String id, String reason) {
        TaskEntity task = requireAccessible(owner, id);
        TaskStatus before = task.getStatus();
        task.block(reason);
        TaskEntity saved = flushSafely(task);
        activities.record(id, owner, TaskActivityType.BLOCKED, before, saved.getStatus(), reason);
        return saved;
    }

    @Transactional
    public TaskEntity resume(String owner, String id) {
        TaskEntity task = requireAccessible(owner, id);
        TaskStatus before = task.getStatus();
        task.resume();
        TaskEntity saved = flushSafely(task);
        activities.record(id, owner, TaskActivityType.RESUMED, before, saved.getStatus(), "解除阻塞并恢复执行");
        return saved;
    }

    @Transactional
    public TaskEntity complete(String owner, String id) {
        TaskEntity task = requireAccessible(owner, id);
        TaskStatus before = task.getStatus();
        task.complete();
        TaskEntity saved = flushSafely(task);
        activities.record(id, owner, TaskActivityType.COMPLETED, before, saved.getStatus(), "完成任务");
        return saved;
    }

    @Transactional
    public TaskEntity cancel(String owner, String id) {
        TaskEntity task = require(owner, id);
        TaskStatus before = task.getStatus();
        task.cancel();
        TaskEntity saved = flushSafely(task);
        activities.record(id, owner, TaskActivityType.CANCELLED, before, saved.getStatus(), "取消任务");
        return saved;
    }

    @Transactional
    public void delete(String owner, String id) {
        TaskEntity task = require(owner, id);
        activities.record(id, owner, TaskActivityType.DELETED, task.getStatus(), null,
                "通过审批永久删除任务：" + task.getName());
        repository.delete(task);
        repository.flush();
    }

    @Transactional(readOnly = true)
    public TaskEntity require(String owner, String id) {
        return repository.findByIdAndOwnerUsername(id, owner)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "任务不存在或不属于当前用户，taskId=" + id));
    }

    @Transactional(readOnly = true)
    public TaskEntity requireAccessible(String username, String id) {
        return repository.findAccessible(id, username)
                .orElseThrow(() -> new BusinessException("NOT_FOUND",
                        "任务不存在或当前用户无权访问，taskId=" + id));
    }

    private String validateAssignee(String owner, String assigneeUsername) {
        String candidate = blankToNull(assigneeUsername);
        if (candidate == null) {
            return owner;
        }
        var user = users.findByUsername(candidate)
                .orElseThrow(() -> new BusinessException("INVALID_ASSIGNEE",
                        "负责人不存在：" + candidate));
        if (!user.isEnabled()) {
            throw new BusinessException("INVALID_ASSIGNEE", "负责人账号已禁用：" + candidate);
        }
        return candidate;
    }

    private void validateProject(String owner, String projectId) {
        if (projectId == null || projectId.isBlank()) {
            return;
        }
        projects.require(owner, projectId).ensureActive();
    }

    private void validateDeadline(LocalDateTime deadline) {
        if (deadline != null && deadline.isBefore(ZonedDateTime.now(zoneId).toLocalDateTime())) {
            throw new BusinessException("VALIDATION_ERROR", "任务截止时间不能早于当前时间");
        }
    }

    private TaskEntity flushSafely(TaskEntity task) {
        try {
            return repository.saveAndFlush(task);
        }
        catch (OptimisticLockingFailureException exception) {
            throw new BusinessException("CONCURRENT_MODIFICATION", "任务已被其他请求修改，请刷新后重试", exception);
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
