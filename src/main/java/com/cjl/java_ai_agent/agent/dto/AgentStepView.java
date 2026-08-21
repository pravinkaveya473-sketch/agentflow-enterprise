package com.cjl.java_ai_agent.agent.dto;
import com.cjl.java_ai_agent.agent.model.*;
import java.time.Instant;
public record AgentStepView(int sequence,AgentStepType type,String toolCallId,String toolName,String content,Instant createdAt){
 public static AgentStepView from(AgentStepEntity e){return new AgentStepView(e.getSequence(),e.getType(),e.getToolCallId(),e.getToolName(),e.getContent(),e.getCreatedAt());}
}
