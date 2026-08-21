package com.cjl.java_ai_agent.knowledge;
import java.time.Instant;
public record KnowledgeDocumentView(String id,String knowledgeBase,String filename,String contentType,long fileSize,int chunkCount,KnowledgeDocumentStatus status,String uploadedBy,Instant createdAt){
 public static KnowledgeDocumentView from(KnowledgeDocumentEntity e){return new KnowledgeDocumentView(e.getId(),e.getKnowledgeBase(),e.getFilename(),e.getContentType(),e.getFileSize(),e.getChunkCount(),e.getStatus(),e.getUploadedBy(),e.getCreatedAt());}
}
