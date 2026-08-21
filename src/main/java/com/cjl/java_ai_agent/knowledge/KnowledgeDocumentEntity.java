package com.cjl.java_ai_agent.knowledge;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="knowledge_document")
public class KnowledgeDocumentEntity {
    @Id private String id;
    @Column(name="knowledge_base",nullable=false) private String knowledgeBase;
    @Column(name="tenant_id",nullable=false) private String tenantId;
    @Column(nullable=false) private String filename;
    @Column(name="content_type") private String contentType;
    @Column(name="file_size",nullable=false) private long fileSize;
    @Column(name="storage_path",nullable=false,length=1000) private String storagePath;
    @Column(nullable=false,length=64) private String sha256;
    @Column(name="chunk_count",nullable=false) private int chunkCount;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private KnowledgeDocumentStatus status;
    @Column(name="uploaded_by",nullable=false) private String uploadedBy;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    @Column(name="updated_at",nullable=false) private Instant updatedAt;
    protected KnowledgeDocumentEntity(){}
    public KnowledgeDocumentEntity(String id,String knowledgeBase,String tenantId,String filename,String contentType,long fileSize,String storagePath,String sha256,String uploadedBy){
        this.id=id;this.knowledgeBase=knowledgeBase;this.tenantId=tenantId;this.filename=filename;this.contentType=contentType;this.fileSize=fileSize;this.storagePath=storagePath;this.sha256=sha256;this.uploadedBy=uploadedBy;this.status=KnowledgeDocumentStatus.PROCESSING;this.createdAt=Instant.now();this.updatedAt=this.createdAt;
    }
    public void ready(int count){this.chunkCount=count;this.status=KnowledgeDocumentStatus.READY;this.updatedAt=Instant.now();}
    public void failed(){this.status=KnowledgeDocumentStatus.FAILED;this.updatedAt=Instant.now();}
    public String getId(){return id;} public String getKnowledgeBase(){return knowledgeBase;} public String getTenantId(){return tenantId;} public String getFilename(){return filename;} public String getContentType(){return contentType;} public long getFileSize(){return fileSize;} public String getStoragePath(){return storagePath;} public String getSha256(){return sha256;} public int getChunkCount(){return chunkCount;} public KnowledgeDocumentStatus getStatus(){return status;} public String getUploadedBy(){return uploadedBy;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
