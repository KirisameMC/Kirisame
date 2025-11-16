package org.kirisame.mc.agent.impl.playerlist;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

public class PlayerListTransform {
    public static AgentBuilder apply(AgentBuilder builder) {
        return builder
                .type(ElementMatchers.named("net.minecraft.server.players.PlayerList"))
                .transform((b,td,cl,jm,pd)->
                        b.visit(
                                Advice.to(PlayerListAdvice.class).on(ElementMatchers.named("placeNewPlayer"))
                        ));
    }
}
