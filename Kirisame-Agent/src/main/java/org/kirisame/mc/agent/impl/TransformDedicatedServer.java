package org.kirisame.mc.agent.impl;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import org.kirisame.mc.agent.Transform;
import org.kirisame.mc.api.agent.AgentMessageBus;

import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class TransformDedicatedServer extends Transform {
    @Override
    public AgentBuilder apply(Instrumentation inst, AgentBuilder builder) {
        return builder.type(ElementMatchers.named("net.minecraft.server.dedicated.DedicatedServer"))
                .transform(new AgentBuilder.Transformer() {
                    @Override
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {
                        return builder.visit(
                                Advice.to(initServerExitAdvice.class).on(ElementMatchers.named("initServer").and(ElementMatchers.returns(boolean.class))
                                        .and(ElementMatchers.isProtected()))
                        );
                    }
                })
                ;
    }

    static class initServerExitAdvice {
        @Advice.OnMethodExit
        public static void afterInitServer(@Advice.Return boolean status){
            AgentMessageBus.post(".serverInitReturn",status);
        }
    }
}
