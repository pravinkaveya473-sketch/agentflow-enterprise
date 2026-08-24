package com.cjl.java_ai_agent.infrastructure.redis;

import com.cjl.java_ai_agent.common.exception.BusinessException;
import com.cjl.java_ai_agent.config.AppProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RateLimitService {
    private static final DefaultRedisScript<Long> SCRIPT = new DefaultRedisScript<>("""
            local current = redis.call('INCR', KEYS[1])
            if current == 1 then redis.call('PEXPIRE', KEYS[1], ARGV[1]) end
            return current
            """, Long.class);
    private final StringRedisTemplate redis;
    private final AppProperties properties;

    public RateLimitService(StringRedisTemplate redis, AppProperties properties) { this.redis=redis; this.properties=properties; }

    public void check(String subject) {
        long windowMs=properties.rateLimit().window().toMillis();
        long bucket=System.currentTimeMillis()/windowMs;
        Long count=redis.execute(SCRIPT, List.of("rate:"+subject+":"+bucket), String.valueOf(windowMs));
        if (count != null && count > properties.rateLimit().requests()) {
            throw new BusinessException("RATE_LIMITED", "请求过于频繁，请稍后重试");
        }
    }
}
