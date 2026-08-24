package com.cjl.java_ai_agent.evaluation;
import com.cjl.java_ai_agent.common.response.ApiResponse;import org.springframework.http.ResponseEntity;import org.springframework.security.access.prepost.PreAuthorize;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/evaluations") @PreAuthorize("hasRole('ADMIN')")
public class EvaluationController{private final EvaluationService service;public EvaluationController(EvaluationService service){this.service=service;}@PostMapping("/task-agent")public ResponseEntity<ApiResponse<EvaluationService.EvaluationView>> run(){return ResponseEntity.ok(ApiResponse.success(service.runTaskAgentEvaluation()));}}
