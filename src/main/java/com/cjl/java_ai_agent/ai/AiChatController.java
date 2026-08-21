package com.cjl.java_ai_agent.ai;

import com.cjl.java_ai_agent.common.response.ApiResponse;
import com.cjl.java_ai_agent.memory.ConversationIdService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ai")
public class AiChatController {
    private final ChatClient chatClient;
    private final ChatMemory memory;
    private final ConversationIdService conversationIds;

    public AiChatController(@Qualifier("generalChatClient") ChatClient chatClient,
                            ChatMemory memory,
                            ConversationIdService conversationIds) {
        this.chatClient = chatClient;
        this.memory = memory;
        this.conversationIds = conversationIds;
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatReply>> chat(
            @RequestHeader(value = "X-Conversation-Id", required = false) String conversationId,
            @Valid @RequestBody ChatRequest request) {

        ConversationIdService.ConversationRef conversation = conversationIds.resolve(conversationId);
        List<Message> messages = new ArrayList<>(memory.get(conversation.internalId()));
        messages.add(new UserMessage(request.message()));

        String content = chatClient.prompt().messages(messages).call().content();
        memory.add(conversation.internalId(), new UserMessage(request.message()));
        memory.add(conversation.internalId(), new AssistantMessage(content));

        return ResponseEntity.ok(ApiResponse.success(
                new ChatReply(conversation.externalId(), content)));
    }

    public record ChatRequest(@NotBlank String message) {}
    public record ChatReply(String conversationId, String content) {}
}
