package com.cjl.java_ai_agent.evaluation;

import com.cjl.java_ai_agent.agent.dto.AgentRunView;
import com.cjl.java_ai_agent.agent.model.AgentStepType;
import com.cjl.java_ai_agent.agent.service.TaskAgentService;
import com.cjl.java_ai_agent.security.CurrentUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;import java.nio.charset.StandardCharsets;import java.time.Instant;import java.util.*;

@Service
public class EvaluationService {
 private final TaskAgentService agent;private final EvaluationRunRepository repository;private final CurrentUserService currentUser;private final ObjectMapper mapper;
 public EvaluationService(TaskAgentService agent,EvaluationRunRepository repository,CurrentUserService currentUser,ObjectMapper mapper){this.agent=agent;this.repository=repository;this.currentUser=currentUser;this.mapper=mapper;}
 public EvaluationView runTaskAgentEvaluation(){
  Instant start=Instant.now();List<CaseResult> results=new ArrayList<>();
  for(EvaluationCase c:loadCases()){
   try{
    var response=agent.run("eval-"+UUID.randomUUID(),c.input(),"eval-"+c.id()+"-"+UUID.randomUUID());
    AgentRunView run=agent.getRun(response.runId());List<String> actual=run.steps().stream().filter(s->s.type()==AgentStepType.TOOL_REQUEST).map(s->s.toolName()).toList();
    boolean pass=isSubsequence(c.expectedTools(),actual);results.add(new CaseResult(c.id(),c.input(),c.expectedTools(),actual,pass,response.content(),null));
   }catch(Exception ex){results.add(new CaseResult(c.id(),c.input(),c.expectedTools(),List.of(),false,null,ex.getMessage()));}
  }
  int passed=(int)results.stream().filter(CaseResult::passed).count();String id=UUID.randomUUID().toString();
  try{repository.save(new EvaluationRunEntity(id,"TASK_AGENT",results.size(),passed,mapper.writeValueAsString(results),currentUser.username(),start,Instant.now()));}catch(Exception ex){throw new IllegalStateException(ex);}
  return new EvaluationView(id,results.size(),passed,results.isEmpty()?0:(double)passed/results.size(),results);
 }
 private List<EvaluationCase> loadCases(){
  try(BufferedReader reader=new BufferedReader(new InputStreamReader(new ClassPathResource("evaluation/task-agent-cases.jsonl").getInputStream(),StandardCharsets.UTF_8))){return reader.lines().filter(l->!l.isBlank()).map(line->{try{return mapper.readValue(line,EvaluationCase.class);}catch(Exception ex){throw new IllegalStateException(ex);}}).toList();}catch(IOException ex){throw new IllegalStateException(ex);}
 }
 private boolean isSubsequence(List<String> expected,List<String> actual){int i=0;for(String tool:actual)if(i<expected.size()&&expected.get(i).equals(tool))i++;return i==expected.size();}
 public record EvaluationCase(String id,String input,List<String> expectedTools){}
 public record CaseResult(String id,String input,List<String> expectedTools,List<String> actualTools,boolean passed,String response,String error){}
 public record EvaluationView(String evaluationRunId,int totalCases,int passedCases,double passRate,List<CaseResult> cases){}
}
