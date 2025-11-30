package org.kirisame.mc.agent.impl;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import org.kirisame.mc.agent.Transform;
import org.kirisame.mc.api.agent.AgentMessageBus;

import java.lang.instrument.Instrumentation;

public class TransformPlayerList extends Transform {
    @Override
    public AgentBuilder apply(Instrumentation inst, AgentBuilder builder) {
        setBuilder(builder).setClassName("net.minecraft.server.players.PlayerList")
                .mixinMethod("placeNewPlayer", PlayerJoinAdvice.class)
                .mixinMethod("remove", PlayerLeftAdvice.class);
        return getBuilder();
    }

    static class PlayerJoinAdvice {
        @Advice.OnMethodExit
        public static void onEnter(@Advice.Argument(1) Object serverPlayer){
            try {
                AgentMessageBus.post("PlayerJoin",serverPlayer);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    static class PlayerLeftAdvice {
        @Advice.OnMethodExit
        public static void onEnter(@Advice.Argument(0) Object serverPlayer){
            try {
                AgentMessageBus.post("PlayerLeft",serverPlayer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
