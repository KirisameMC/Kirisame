package org.kirisame.mc.api.agent.impl;

import org.kirisame.mc.api.agent.AgentMessageHandle;
import org.kirisame.mc.api.agent.AgentMessageLabel;
import org.kirisame.mc.event.EventBus;
import org.kirisame.mc.event.impl.reflect.TickEvent;

@AgentMessageLabel(name = "Tick")
public class TickMessageHandle implements AgentMessageHandle {
    @Override
    public Object handle(Object message) {
        EventBus.post(new TickEvent(message));
        return null;
    }
}
