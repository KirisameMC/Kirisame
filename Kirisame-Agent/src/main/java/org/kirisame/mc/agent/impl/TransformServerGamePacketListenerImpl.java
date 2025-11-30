package org.kirisame.mc.agent.impl;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import org.kirisame.mc.agent.Transform;
import org.kirisame.mc.api.agent.AgentMessageBus;

import java.lang.instrument.Instrumentation;
import java.util.Map;

public class TransformServerGamePacketListenerImpl extends Transform {
    @Override
    public AgentBuilder apply(Instrumentation inst, AgentBuilder builder) {
        return setBuilder(builder).setClassName("net.minecraft.server.network.ServerGamePacketListenerImpl")
                .mixinMethod("broadcastChatMessage", Interceptor.class).getBuilder();
    }

    public static class Interceptor {
        @Advice.OnMethodExit
        public static void intercept(@Advice.This Object connection , @Advice.Argument(0) Object message) {
            try {
                AgentMessageBus.post("ChatMessage", Map.of("connection", connection, "message", message));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
