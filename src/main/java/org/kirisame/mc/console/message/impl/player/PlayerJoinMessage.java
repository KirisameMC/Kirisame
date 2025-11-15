package org.kirisame.mc.console.message.impl.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.kirisame.mc.console.message.Message;

@Getter @AllArgsConstructor
public class PlayerJoinMessage extends Message {
    String playerName;
}
