package org.kirisame.mc.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.matcher.ElementMatchers;
import org.kirisame.mc.api.agent.AgentStatus;

import java.lang.instrument.Instrumentation;
import java.util.Arrays;

public class Agent {
    static boolean loaded = false;

    public static void premain(String args, Instrumentation inst){
        if (loaded) return;
        loaded = true;
        System.out.println("Kirisame Agent Loading");
        AgentBuilder builder = new AgentBuilder.Default()
                .ignore(ElementMatchers.nameStartsWith("net.bytebuddy."))
                .ignore(ElementMatchers.nameStartsWith("org.slf4j."))
                .ignore(ElementMatchers.nameStartsWith("org.apache.logging."))
                .ignore(ElementMatchers.nameStartsWith("org.tinylog."))
                .ignore(ElementMatchers.nameStartsWith("java."))
                .ignore(ElementMatchers.nameStartsWith("jdk."))
                .ignore(ElementMatchers.nameStartsWith("com.intellij."))
                .ignore(ElementMatchers.nameStartsWith("sun."))
                .ignore(ElementMatchers.nameStartsWith("javax."))
                .ignore(ElementMatchers.nameStartsWith("org.kirisame.mc.agent"))
                .disableClassFormatChanges();

        for (Transform transform : TransformList.transformers) {
            builder = transform.apply(inst, builder);
        }

        AgentBuilder finalBuilder = builder;
        new Thread(()->{
            while (Arrays.stream(inst.getAllLoadedClasses()).anyMatch(c->c.getName().equals("org.kirisame.mc.api.agent.AgentStatus"))){
                Thread.onSpinWait();
            }
            AgentStatus.setBuilder(finalBuilder);
            AgentStatus.setInst(inst);
        }).start();
    }

}
