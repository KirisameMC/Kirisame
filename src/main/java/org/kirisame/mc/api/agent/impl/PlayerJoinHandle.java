package org.kirisame.mc.api.agent.impl;

import org.kirisame.mc.api.agent.AgentMessageHandle;
import org.kirisame.mc.api.agent.AgentMessageLabel;
import org.kirisame.mc.event.EventBus;
import org.kirisame.mc.event.impl.reflect.PlayerJoinEvent;

@AgentMessageLabel(name = "PlayerJoin")
public class PlayerJoinHandle implements AgentMessageHandle {
    @Override
    public Object handle(Object message) {
        EventBus.post(new PlayerJoinEvent(message));
        return null;
    }
}
