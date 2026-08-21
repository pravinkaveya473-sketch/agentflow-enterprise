package com.cjl.java_ai_agent.agent.dto;
import com.cjl.java_ai_agent.agent.model.AgentRunStatus;
import java.time.Instant;
import java.util.List;
public record AgentRunView(String runId,String conversationId,String username,String userMessage,AgentRunStatus status,
 String finalContent,String errorMessage,int modelCallCount,int toolCallCount,Instant startedAt,Instant completedAt,List<AgentStepView> steps){}
