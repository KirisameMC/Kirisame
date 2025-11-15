package org.kirisame.mc.console.parser.impl;

import org.kirisame.mc.console.message.impl.ServerDoneMessage;
import org.kirisame.mc.console.parser.Parser;

import java.time.Duration;
import java.util.regex.Matcher;

public class ServerDoneParser extends Parser<ServerDoneMessage> {
    @Override
    public String getRule() {
        return "Done \\((\\d+(?:\\.\\d+)?)s\\)! For help, type \"help\"";
    }

    @Override
    public ServerDoneMessage parse(String input) {
        Matcher matcher = getPattern().matcher(input);
        if (matcher.find()){
            String timeInSeconds = matcher.group(1);
            long time = (long) (Double.parseDouble(timeInSeconds) * 1000);

            return new ServerDoneMessage(Duration.ofMillis(time));
        }
        return null;
    }
}
