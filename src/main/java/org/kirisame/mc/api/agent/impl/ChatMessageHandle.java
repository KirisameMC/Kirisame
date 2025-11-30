package org.kirisame.mc.api.agent.impl;

import org.kirisame.mc.api.agent.AgentMessageHandle;
import org.kirisame.mc.api.agent.AgentMessageLabel;
import org.kirisame.mc.event.EventBus;
import org.kirisame.mc.event.impl.reflect.ChatMessageEvent;

import java.util.Map;

@AgentMessageLabel(name = "ChatMessage")
public class ChatMessageHandle implements AgentMessageHandle {
    @Override
    public Object handle(Object message) {
        if (message instanceof Map<?, ?> map){
            EventBus.post(new ChatMessageEvent(map.get("connection"), map.get("message")));
        }
        return null;
    }
}
