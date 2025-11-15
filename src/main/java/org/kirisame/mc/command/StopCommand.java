package org.kirisame.mc.command;

import org.kirisame.mc.api.command.KirisameCommand;
import org.kirisame.mc.api.command.KirisameCommandLabel;
import org.tinylog.Logger;

public class StopCommand implements KirisameCommand {
    @Override
    public KirisameCommandLabel label() {
        return new KirisameCommandLabel("stop");
    }

    @Override
    public int execute(String[] args) {
        Logger.info("Trigger the command stop. jvm machine will shutdown!");
        System.exit(0);
        return 0;
    }
}
