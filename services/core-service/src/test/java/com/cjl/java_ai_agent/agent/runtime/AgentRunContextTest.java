package com.cjl.java_ai_agent.agent.runtime;

import com.cjl.java_ai_agent.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AgentRunContextTest {

    @Test
    void shouldDetectRepeatedToolCall() {
        AgentRunContext context = new AgentRunContext(
                "run-1", "conversation-1", "cjl", "测试", "idem");

        context.toolRequest("call-1", "listTasks", "{}");
        context.toolRequest("call-2", "listTasks", "{}");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> context.toolRequest("call-3", "listTasks", "{}"));

        assertEquals("AGENT_LIMIT", exception.getCode());
    }
}
