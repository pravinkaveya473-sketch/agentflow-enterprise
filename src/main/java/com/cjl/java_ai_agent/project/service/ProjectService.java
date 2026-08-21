package com.cjl.java_ai_agent.project.service;

import com.cjl.java_ai_agent.common.exception.BusinessException;
import com.cjl.java_ai_agent.project.model.ProjectEntity;
import com.cjl.java_ai_agent.project.repository.ProjectRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository repository;

    public ProjectService(ProjectRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ProjectEntity create(String owner, String name, String description, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existing = repository.findByOwnerUsernameAndIdempotencyKey(owner, idempotencyKey.trim());
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        return repository.saveAndFlush(new ProjectEntity(UUID.randomUUID().toString(), owner, name,
                description, blankToNull(idempotencyKey)));
    }

    @Transactional(readOnly = true)
    public List<ProjectEntity> list(String owner) {
        return repository.findAllByOwnerUsernameOrderByCreatedAtDesc(owner);
    }

    @Transactional(readOnly = true)
    public ProjectEntity require(String owner, String id) {
        return repository.findByIdAndOwnerUsername(id, owner)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "项目不存在或不属于当前用户，projectId=" + id));
    }

    @Transactional
    public ProjectEntity update(String owner, String id, String name, String description) {
        ProjectEntity project = require(owner, id);
        project.rename(name, description);
        return flushSafely(project);
    }

    @Transactional
    public ProjectEntity archive(String owner, String id) {
        ProjectEntity project = require(owner, id);
        project.archive();
        return flushSafely(project);
    }

    @Transactional
    public ProjectEntity restore(String owner, String id) {
        ProjectEntity project = require(owner, id);
        project.restore();
        return flushSafely(project);
    }

    private ProjectEntity flushSafely(ProjectEntity project) {
        try {
            return repository.saveAndFlush(project);
        }
        catch (OptimisticLockingFailureException exception) {
            throw new BusinessException("CONCURRENT_MODIFICATION", "项目已被其他请求修改，请刷新后重试", exception);
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
