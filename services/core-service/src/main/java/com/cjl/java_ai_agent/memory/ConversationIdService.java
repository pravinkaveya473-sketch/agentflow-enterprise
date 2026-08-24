package com.cjl.java_ai_agent.memory;

import com.cjl.java_ai_agent.common.exception.BusinessException;
import com.cjl.java_ai_agent.security.CurrentUserService;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 将外部会话 ID 映射为“用户名:会话ID”，防止不同用户读取或清理彼此的 Chat Memory。
 */
@Service
public class ConversationIdService {
    private final CurrentUserService currentUser;

    public ConversationIdService(CurrentUserService currentUser) {
        this.currentUser = currentUser;
    }

    public ConversationRef resolve(String externalId) {
        String visible = externalId == null || externalId.isBlank()
                ? UUID.randomUUID().toString()
                : validate(externalId);
        return new ConversationRef(visible, currentUser.username() + ":" + visible);
    }

    private String validate(String value) {
        if (!value.matches("[A-Za-z0-9._-]{1,100}")) {
            throw new BusinessException("VALIDATION_ERROR", "会话ID格式不合法");
        }
        return value;
    }

    public record ConversationRef(String externalId, String internalId) {}
}
