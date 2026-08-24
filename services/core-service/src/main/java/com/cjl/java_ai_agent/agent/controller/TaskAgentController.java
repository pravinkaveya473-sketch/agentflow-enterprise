package com.cjl.java_ai_agent.agent.controller;

import com.cjl.java_ai_agent.agent.dto.*;
import com.cjl.java_ai_agent.agent.service.TaskAgentService;
import com.cjl.java_ai_agent.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agents/task")
public class TaskAgentController {
    private final TaskAgentService service;
    public TaskAgentController(TaskAgentService service){this.service=service;}
    @PostMapping("/runs")
    public ResponseEntity<ApiResponse<TaskAgentResponse>> run(@RequestHeader(value="X-Conversation-Id",required=false) String conversationId,
                                                               @RequestHeader(value="X-Idempotency-Key",required=false) String idempotencyKey,
                                                               @Valid @RequestBody TaskAgentRequest request){
        return ResponseEntity.ok(ApiResponse.success(service.run(conversationId,request.message(),idempotencyKey)));
    }
    @GetMapping("/runs/{runId}")
    public ResponseEntity<ApiResponse<AgentRunView>> get(@PathVariable String runId){return ResponseEntity.ok(ApiResponse.success(service.getRun(runId)));}
}
