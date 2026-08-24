package com.cjl.java_ai_agent.knowledge;

import com.cjl.java_ai_agent.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 只负责短数据库事务。文件解析、Embedding 和向量写入不得包在数据库长事务中。
 */
@Service
public class KnowledgeDocumentPersistenceService {
    private final KnowledgeDocumentRepository repository;

    public KnowledgeDocumentPersistenceService(KnowledgeDocumentRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<KnowledgeDocumentEntity> duplicate(String tenantId, String knowledgeBase, String sha256) {
        return repository.findByTenantIdAndKnowledgeBaseAndSha256(tenantId, knowledgeBase, sha256);
    }

    @Transactional
    public KnowledgeDocumentEntity create(KnowledgeDocumentEntity entity) {
        return repository.save(entity);
    }

    @Transactional
    public KnowledgeDocumentView markReady(String id, int chunkCount) {
        KnowledgeDocumentEntity entity = require(id);
        entity.ready(chunkCount);
        return KnowledgeDocumentView.from(entity);
    }

    @Transactional
    public void markFailed(String id) {
        KnowledgeDocumentEntity entity = require(id);
        entity.failed();
    }

    @Transactional(readOnly = true)
    public List<KnowledgeDocumentView> list(String tenantId, String knowledgeBase) {
        return repository.findAllByTenantIdAndKnowledgeBaseOrderByCreatedAtDesc(tenantId, knowledgeBase)
                .stream().map(KnowledgeDocumentView::from).toList();
    }

    private KnowledgeDocumentEntity require(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "知识文档不存在：" + id));
    }
}
