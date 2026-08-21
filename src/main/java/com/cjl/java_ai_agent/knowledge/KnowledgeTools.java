package com.cjl.java_ai_agent.knowledge;

import com.cjl.java_ai_agent.security.CurrentUserService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeTools {
    private final KnowledgeService service; private final CurrentUserService currentUser;
    public KnowledgeTools(KnowledgeService service,CurrentUserService currentUser){this.service=service;this.currentUser=currentUser;}
    @Tool(description="从当前用户的企业知识库检索并回答问题。必须返回真实来源；knowledgeBase 默认 general。")
    public RagAnswer searchKnowledge(String query,@ToolParam(description="知识库名称，默认 general",required=false) String knowledgeBase){
        return service.ask(currentUser.username(),knowledgeBase==null||knowledgeBase.isBlank()?"general":knowledgeBase,query);
    }
}
