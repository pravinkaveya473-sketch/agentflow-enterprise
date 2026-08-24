package com.cjl.java_ai_agent.common.response;

import java.time.Instant;

/** 项目自定义的统一业务响应，不属于 Spring AI。 */
public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data,
        Instant timestamp
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "SUCCESS", "请求成功", data, Instant.now());
    }

    public static <T> ApiResponse<T> failure(String code, String message) {
        return new ApiResponse<>(false, code, message, null, Instant.now());
    }
}
