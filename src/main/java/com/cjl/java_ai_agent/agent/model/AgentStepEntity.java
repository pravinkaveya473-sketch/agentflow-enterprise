package com.cjl.java_ai_agent.agent.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="agent_step", uniqueConstraints=@UniqueConstraint(name="uk_agent_step_sequence",columnNames={"run_id","sequence_no"}))
public class AgentStepEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="run_id",nullable=false) private String runId;
    @Column(name="sequence_no",nullable=false) private int sequence;
    @Enumerated(EnumType.STRING) @Column(name="step_type",nullable=false) private AgentStepType type;
    @Column(name="tool_call_id") private String toolCallId;
    @Column(name="tool_name") private String toolName;
    @Column(columnDefinition="MEDIUMTEXT") private String content;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    protected AgentStepEntity(){}
    public AgentStepEntity(String runId,int sequence,AgentStepType type,String toolCallId,String toolName,String content){
        this.runId=runId;this.sequence=sequence;this.type=type;this.toolCallId=toolCallId;this.toolName=toolName;this.content=content;this.createdAt=Instant.now();
    }
    public Long getId(){return id;} public String getRunId(){return runId;} public int getSequence(){return sequence;}
    public AgentStepType getType(){return type;} public String getToolCallId(){return toolCallId;} public String getToolName(){return toolName;}
    public String getContent(){return content;} public Instant getCreatedAt(){return createdAt;}
}
