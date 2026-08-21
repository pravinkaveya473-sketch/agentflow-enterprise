package com.cjl.java_ai_agent.knowledge;

import com.cjl.java_ai_agent.common.response.ApiResponse;
import com.cjl.java_ai_agent.security.CurrentUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/v1/knowledge")
public class KnowledgeController {
    private final KnowledgeService service; private final CurrentUserService currentUser;
    public KnowledgeController(KnowledgeService service,CurrentUserService currentUser){this.service=service;this.currentUser=currentUser;}
    @PostMapping(value="/{knowledgeBase}/documents",consumes="multipart/form-data")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponse<KnowledgeDocumentView>> upload(@PathVariable String knowledgeBase,@RequestPart("file") MultipartFile file){
        String user=currentUser.username();return ResponseEntity.ok(ApiResponse.success(service.ingest(user,knowledgeBase,file,user)));
    }
    @GetMapping("/{knowledgeBase}/documents")
    public ResponseEntity<ApiResponse<List<KnowledgeDocumentView>>> list(@PathVariable String knowledgeBase){return ResponseEntity.ok(ApiResponse.success(service.list(currentUser.username(),knowledgeBase)));}
    @PostMapping("/{knowledgeBase}/ask")
    public ResponseEntity<ApiResponse<RagAnswer>> ask(@PathVariable String knowledgeBase,@Valid @RequestBody AskRequest request){return ResponseEntity.ok(ApiResponse.success(service.ask(currentUser.username(),knowledgeBase,request.question())));}
    public record AskRequest(@NotBlank String question){}
}
