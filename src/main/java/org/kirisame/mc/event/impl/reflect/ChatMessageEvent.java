package org.kirisame.mc.event.impl.reflect;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.kirisame.mc.event.Event;

@AllArgsConstructor @Getter
public class ChatMessageEvent extends Event {
    final Object connection;
    final Object message;
}
