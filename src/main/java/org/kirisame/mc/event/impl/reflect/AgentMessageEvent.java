package org.kirisame.mc.event.impl.reflect;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.kirisame.mc.event.Event;

@AllArgsConstructor @Getter
public class AgentMessageEvent extends Event {
    final String label;
    final Object message;
}
