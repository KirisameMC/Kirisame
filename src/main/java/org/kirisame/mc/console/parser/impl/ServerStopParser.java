package org.kirisame.mc.console.parser.impl;

import org.kirisame.mc.console.message.impl.ServerStopMessage;
import org.kirisame.mc.console.parser.Parser;

public class ServerStopParser extends Parser<ServerStopMessage> {
    @Override
    public String getRule() {
        return "Stopping the server";
    }

    @Override
    public ServerStopMessage parse(String input) {
        return match(input) ? new ServerStopMessage() : null;
    }
}
