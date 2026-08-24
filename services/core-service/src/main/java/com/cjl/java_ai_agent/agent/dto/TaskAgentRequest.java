package com.cjl.java_ai_agent.agent.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record TaskAgentRequest(@NotBlank(message="用户请求不能为空") @Size(max=4000,message="用户请求不能超过4000字符") String message){}
