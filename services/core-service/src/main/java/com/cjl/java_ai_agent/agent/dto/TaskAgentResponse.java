package com.cjl.java_ai_agent.agent.dto;
import com.cjl.java_ai_agent.agent.model.AgentRunStatus;
import java.time.Instant;
public record TaskAgentResponse(String runId,String conversationId,AgentRunStatus status,String content,int modelCallCount,int toolCallCount,Instant completedAt){}
