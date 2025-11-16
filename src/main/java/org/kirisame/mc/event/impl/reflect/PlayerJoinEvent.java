package org.kirisame.mc.event.impl.reflect;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.kirisame.mc.event.Event;

@AllArgsConstructor @Getter
public class PlayerJoinEvent extends Event {
    Object serverPlayer;
}
