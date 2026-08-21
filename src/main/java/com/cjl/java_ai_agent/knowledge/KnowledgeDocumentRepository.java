package com.cjl.java_ai_agent.knowledge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocumentEntity,String>{
 Optional<KnowledgeDocumentEntity> findByTenantIdAndKnowledgeBaseAndSha256(String tenantId,String knowledgeBase,String sha256);
 List<KnowledgeDocumentEntity> findAllByTenantIdAndKnowledgeBaseOrderByCreatedAtDesc(String tenantId,String knowledgeBase);
}
