package org.kirisame.mc.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.matcher.ElementMatchers;
import org.kirisame.mc.agent.impl.playerlist.PlayerListTransform;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.function.UnaryOperator;

public class Agent {
    static final List<UnaryOperator<AgentBuilder>> transformers = List.of(
            PlayerListTransform::apply
    );

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

        for (UnaryOperator<AgentBuilder> transformer : transformers) {
            builder = transformer.apply(builder);
        }

        builder.installOn(inst);
    }

}
