package com.cjl.java_ai_agent.memory;

import com.cjl.java_ai_agent.common.response.ApiResponse;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/memory")
public class MemoryController {
    private final ChatMemory memory;
    private final ConversationIdService conversationIds;

    public MemoryController(ChatMemory memory, ConversationIdService conversationIds) {
        this.memory = memory;
        this.conversationIds = conversationIds;
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<List<MemoryMessageView>>> get(@PathVariable String conversationId) {
        String internalId = conversationIds.resolve(conversationId).internalId();
        return ResponseEntity.ok(ApiResponse.success(
                memory.get(internalId).stream().map(MemoryMessageView::from).toList()));
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<Void>> clear(@PathVariable String conversationId) {
        memory.clear(conversationIds.resolve(conversationId).internalId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    public record MemoryMessageView(String type, String text) {
        static MemoryMessageView from(Message message) {
            return new MemoryMessageView(message.getMessageType().name(), message.getText());
        }
    }
}
