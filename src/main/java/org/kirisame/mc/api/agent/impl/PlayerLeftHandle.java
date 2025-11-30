package org.kirisame.mc.api.agent.impl;

import org.kirisame.mc.api.agent.AgentMessageHandle;
import org.kirisame.mc.api.agent.AgentMessageLabel;
import org.kirisame.mc.event.EventBus;
import org.kirisame.mc.event.impl.reflect.PlayerLeftEvent;

@AgentMessageLabel(name = "PlayerLeft")
public class PlayerLeftHandle implements AgentMessageHandle {
    @Override
    public Object handle(Object message) {
        EventBus.post(new PlayerLeftEvent(message));
        return null;
    }
}
