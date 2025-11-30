package org.kirisame.mc.api.plugin;

import net.bytebuddy.agent.builder.AgentBuilder;

import java.lang.instrument.Instrumentation;

public interface KirisameTransform {
    AgentBuilder apply(Instrumentation inst, AgentBuilder builder);
}
