package com.cjl.java_ai_agent.approval;

import com.cjl.java_ai_agent.common.response.ApiResponse;
import com.cjl.java_ai_agent.security.CurrentUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/approvals")
@PreAuthorize("hasAnyRole('APPROVER','ADMIN')")
public class ApprovalController {
    private final ApprovalService service;
    private final CurrentUserService currentUser;
    public ApprovalController(ApprovalService service, CurrentUserService currentUser) { this.service=service; this.currentUser=currentUser; }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<ApprovalView>>> pending() { return ResponseEntity.ok(ApiResponse.success(service.pending())); }
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<ApprovalView>> approve(@PathVariable String id, @Valid @RequestBody DecisionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.approve(id, currentUser.username(), request.comment())));
    }
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<ApprovalView>> reject(@PathVariable String id, @Valid @RequestBody DecisionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.reject(id, currentUser.username(), request.comment())));
    }
    public record DecisionRequest(@NotBlank String comment) {}
}
