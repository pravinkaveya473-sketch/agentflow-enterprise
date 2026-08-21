package com.cjl.java_ai_agent.observability;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;

@Component
public class AgentMetrics {
    private final MeterRegistry registry;
    public AgentMetrics(MeterRegistry registry){this.registry=registry;}
    public Timer.Sample start(){return Timer.start(registry);}
    public void finish(Timer.Sample sample,String status){sample.stop(Timer.builder("agent.run.duration").tag("status",status).register(registry));registry.counter("agent.run.total","status",status).increment();}
    public void modelCall(){registry.counter("agent.model.calls").increment();}
    public void toolCall(String tool){registry.counter("agent.tool.calls","tool",tool).increment();}
}
