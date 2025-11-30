package org.kirisame.mc.agent;

import lombok.Getter;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;

public abstract class Transform {
    public abstract AgentBuilder apply(Instrumentation inst, AgentBuilder builder);

    @Getter
    AgentBuilder builder;
    String className;

    public Transform setBuilder(AgentBuilder builder){
        this.builder = builder;
        return this;
    }

    public Transform setClassName(String className){
        this.className = className;
        return this;
    }

    public Transform mixinMethod(String methodName, Class<?> methodClass){
        builder = builder.type(ElementMatchers.named(className))
                .transform((b,td,cl,jm,pd)->b.visit(
                        Advice.to(methodClass).on(ElementMatchers.named(methodName))));
        return this;
    }
}
