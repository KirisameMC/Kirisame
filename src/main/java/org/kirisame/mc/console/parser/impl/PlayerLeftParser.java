package org.kirisame.mc.console.parser.impl;

import org.kirisame.mc.console.message.impl.player.PlayerLeftMessage;
import org.kirisame.mc.console.parser.Parser;

import java.util.regex.Matcher;

public class PlayerLeftParser extends Parser<PlayerLeftMessage> {
    @Override
    public String getRule() {
        return "^(.*?) left the game$";
    }

    @Override
    public PlayerLeftMessage parse(String input) {
        Matcher matcher = getPattern().matcher(input);
        if (matcher.find()){
            return new PlayerLeftMessage(matcher.group(1));
        }
        return null;
    }
}
