package org.kirisame.mc.event.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.kirisame.mc.console.message.ConsoleMessage;
import org.kirisame.mc.event.Event;

@AllArgsConstructor @Getter
public class ConsoleMessageEvent extends Event {
    final ConsoleMessage message;
}
