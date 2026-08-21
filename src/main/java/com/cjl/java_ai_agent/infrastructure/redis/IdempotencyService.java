package com.cjl.java_ai_agent.infrastructure.redis;

import com.cjl.java_ai_agent.common.exception.BusinessException;
import com.cjl.java_ai_agent.config.AppProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IdempotencyService {
    private final StringRedisTemplate redis;
    private final AppProperties properties;

    public IdempotencyService(StringRedisTemplate redis, AppProperties properties) {
        this.redis=redis; this.properties=properties;
    }

    public Optional<String> find(String scope, String key) {
        if (key == null || key.isBlank()) return Optional.empty();
        return Optional.ofNullable(redis.opsForValue().get(redisKey(scope,key)));
    }

    public void reserve(String scope, String key, String value) {
        if (key == null || key.isBlank()) return;
        Boolean ok=redis.opsForValue().setIfAbsent(redisKey(scope,key), value, properties.idempotency().ttl());
        if (!Boolean.TRUE.equals(ok)) {
            throw new BusinessException("IDEMPOTENCY_CONFLICT", "相同幂等键的请求正在执行或已经完成");
        }
    }

    private String redisKey(String scope,String key) { return "idem:"+scope+":"+key; }
}
