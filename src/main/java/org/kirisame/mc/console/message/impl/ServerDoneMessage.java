package org.kirisame.mc.console.message.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.kirisame.mc.console.message.Message;

import java.time.Duration;

@Getter @AllArgsConstructor
public class ServerDoneMessage extends Message {
    Duration duration;
}
