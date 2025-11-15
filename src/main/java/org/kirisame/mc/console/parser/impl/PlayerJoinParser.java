package org.kirisame.mc.console.parser.impl;

import org.kirisame.mc.console.message.impl.player.PlayerJoinMessage;
import org.kirisame.mc.console.parser.Parser;

import java.util.regex.Matcher;

public class PlayerJoinParser extends Parser<PlayerJoinMessage> {
    @Override
    public String getRule() {
        return "^(.*?) joined the game$";
    }

    @Override
    public PlayerJoinMessage parse(String input) {
        Matcher matcher = getPattern().matcher(input);
        if (matcher.find()){
            return new PlayerJoinMessage(matcher.group(1));
        }
        return null;
    }
}
