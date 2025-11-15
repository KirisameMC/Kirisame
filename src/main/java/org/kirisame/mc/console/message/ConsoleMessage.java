package org.kirisame.mc.console.message;

import lombok.Getter;
import lombok.Setter;

@Getter
public class ConsoleMessage {
    long time;
    String thread;
    String level;
    String message;

    @Setter
    Message content;

    public ConsoleMessage(long time, String thread, String level, String message) {
        this.time = time;
        this.thread = thread;
        this.level = level;
        this.message = message;
    }

    @Override
    public String toString() {
        return "ConsoleMessage{" +
                "time=" + time +
                ", thread='" + thread + '\'' +
                ", level='" + level + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
