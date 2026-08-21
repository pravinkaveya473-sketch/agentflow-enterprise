package com.cjl.java_ai_agent.agent.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="agent_run")
public class AgentRunEntity {
    @Id private String id;
    @Column(name="conversation_id",nullable=false) private String conversationId;
    @Column(nullable=false) private String username;
    @Column(name="user_message",nullable=false,columnDefinition="TEXT") private String userMessage;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private AgentRunStatus status;
    @Column(name="final_content",columnDefinition="MEDIUMTEXT") private String finalContent;
    @Column(name="error_message",columnDefinition="TEXT") private String errorMessage;
    @Column(name="model_call_count",nullable=false) private int modelCallCount;
    @Column(name="tool_call_count",nullable=false) private int toolCallCount;
    @Column(name="idempotency_key") private String idempotencyKey;
    @Column(name="started_at",nullable=false) private Instant startedAt;
    @Column(name="completed_at") private Instant completedAt;
    @Version @Column(nullable=false) private long version;
    protected AgentRunEntity(){}
    public AgentRunEntity(String id,String conversationId,String username,String userMessage,String idempotencyKey){
        this.id=id;this.conversationId=conversationId;this.username=username;this.userMessage=userMessage;
        this.idempotencyKey=idempotencyKey;this.status=AgentRunStatus.RUNNING;this.startedAt=Instant.now();
    }
    public void update(AgentRunStatus status,String finalContent,String errorMessage,int modelCalls,int toolCalls,Instant completedAt){
        this.status=status;this.finalContent=finalContent;this.errorMessage=errorMessage;
        this.modelCallCount=modelCalls;this.toolCallCount=toolCalls;this.completedAt=completedAt;
    }
    public String getId(){return id;} public String getConversationId(){return conversationId;} public String getUsername(){return username;}
    public String getUserMessage(){return userMessage;} public AgentRunStatus getStatus(){return status;}
    public String getFinalContent(){return finalContent;} public String getErrorMessage(){return errorMessage;}
    public int getModelCallCount(){return modelCallCount;} public int getToolCallCount(){return toolCallCount;}
    public String getIdempotencyKey(){return idempotencyKey;} public Instant getStartedAt(){return startedAt;} public Instant getCompletedAt(){return completedAt;} public long getVersion(){return version;}
}
