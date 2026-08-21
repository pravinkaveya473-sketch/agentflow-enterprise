package com.cjl.java_ai_agent.infrastructure.redis;

import com.cjl.java_ai_agent.agent.dto.AgentRunView;
import com.cjl.java_ai_agent.config.AppProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AgentRunCache {
    private static final Logger log= LoggerFactory.getLogger(AgentRunCache.class);
    private final StringRedisTemplate redis; private final ObjectMapper mapper; private final AppProperties properties;
    public AgentRunCache(StringRedisTemplate redis,ObjectMapper mapper,AppProperties properties){this.redis=redis;this.mapper=mapper;this.properties=properties;}
    public void put(AgentRunView view){
        try { redis.opsForValue().set("agent:run:"+view.runId(), mapper.writeValueAsString(view), properties.agent().runCacheTtl()); }
        catch (JsonProcessingException ex){ log.warn("AgentRun缓存序列化失败",ex); }
    }
    public Optional<AgentRunView> get(String id){
        String json=redis.opsForValue().get("agent:run:"+id); if(json==null)return Optional.empty();
        try{return Optional.of(mapper.readValue(json,AgentRunView.class));}catch(JsonProcessingException ex){return Optional.empty();}
    }
}
