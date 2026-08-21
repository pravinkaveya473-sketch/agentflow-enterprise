package com.cjl.java_ai_agent.agent.runtime;

import com.cjl.java_ai_agent.agent.model.*;
import java.time.Instant;
import java.util.*;

/** 单次请求内的可变状态；持久化由 AgentRunStore 负责。 */
public final class AgentRunContext {
    private final String id,conversationId,username,userMessage,idempotencyKey;
    private final Instant startedAt=Instant.now();
    private final List<AgentStepEntity> steps=new ArrayList<>();
    private final Map<String,Integer> toolSignatures=new HashMap<>();
    private int modelCalls,toolCalls;
    private AgentRunStatus status=AgentRunStatus.RUNNING;
    private String finalContent,errorMessage;
    private Instant completedAt;
    public AgentRunContext(String id,String conversationId,String username,String userMessage,String idempotencyKey){this.id=id;this.conversationId=conversationId;this.username=username;this.userMessage=userMessage;this.idempotencyKey=idempotencyKey;}
    public void modelCall(){modelCalls++;step(AgentStepType.MODEL_CALL,null,null,"第 "+modelCalls+" 次模型调用");}
    public void toolRequest(String callId,String name,String args){toolCalls++;step(AgentStepType.TOOL_REQUEST,callId,name,args);String sig=name+":"+args;int count=toolSignatures.merge(sig,1,Integer::sum);if(count>2)throw new com.cjl.java_ai_agent.common.exception.BusinessException("AGENT_LIMIT","检测到重复工具调用："+name);}
    public void toolResult(String callId,String name,String result){step(AgentStepType.TOOL_RESULT,callId,name,result);}
    public void complete(String content){status=AgentRunStatus.COMPLETED;finalContent=content;completedAt=Instant.now();step(AgentStepType.FINAL_RESPONSE,null,null,content);}
    public void fail(AgentRunStatus value,String message){status=value;errorMessage=message;completedAt=Instant.now();step(AgentStepType.ERROR,null,null,message);}
    private void step(AgentStepType type,String callId,String name,String content){steps.add(new AgentStepEntity(id,steps.size()+1,type,callId,name,content));}
    public String id(){return id;} public String conversationId(){return conversationId;} public String username(){return username;} public String userMessage(){return userMessage;} public String idempotencyKey(){return idempotencyKey;}
    public Instant startedAt(){return startedAt;} public int modelCalls(){return modelCalls;} public int toolCalls(){return toolCalls;} public AgentRunStatus status(){return status;}
    public String finalContent(){return finalContent;} public String errorMessage(){return errorMessage;} public Instant completedAt(){return completedAt;} public List<AgentStepEntity> steps(){return List.copyOf(steps);}
}
