package org.kirisame.mc.server.wrapper;

import lombok.Getter;
import org.kirisame.mc.server.wrapper.impl.CommandWrapper;

public class MinecraftWrapper {
    public Object minecraftServer;
    public ClassLoader classLoader;

    public MinecraftWrapper(Object minecraftServer,ClassLoader classLoader) {
        this.minecraftServer = minecraftServer;
        this.classLoader = classLoader;

        commandWrapper = new CommandWrapper(this);
    }

    @Getter
    private final CommandWrapper commandWrapper;

}
