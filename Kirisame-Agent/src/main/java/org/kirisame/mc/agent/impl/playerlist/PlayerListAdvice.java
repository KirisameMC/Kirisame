package org.kirisame.mc.agent.impl.playerlist;

import net.bytebuddy.asm.Advice;
import org.kirisame.mc.api.agent.AgentMessageBus;

public class PlayerListAdvice {
    @Advice.OnMethodExit
    public static void onEnter(@Advice.Argument(1) Object serverPlayer){
        AgentMessageBus.post("PlayerJoin",serverPlayer);
    }
}
