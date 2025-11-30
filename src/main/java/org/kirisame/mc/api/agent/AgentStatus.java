package org.kirisame.mc.api.agent;

import lombok.Getter;
import lombok.Setter;
import net.bytebuddy.agent.builder.AgentBuilder;

import java.lang.instrument.Instrumentation;

public class AgentStatus {
    @Setter @Getter
    static Instrumentation inst;

    @Getter @Setter
    static AgentBuilder builder;
}
