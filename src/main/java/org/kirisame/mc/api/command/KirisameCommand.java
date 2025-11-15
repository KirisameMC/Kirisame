package org.kirisame.mc.api.command;

public interface KirisameCommand {
    KirisameCommandLabel label();
    int execute(String[] args);
}
