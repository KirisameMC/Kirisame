package org.kirisame.mc.server;

import org.kirisame.mc.server.wrapper.MinecraftWrapper;

public class WrapperFactory {
    public static MinecraftWrapper getWrapper(Object minecraftServer,ClassLoader classLoader){
        return new MinecraftWrapper(minecraftServer,classLoader);
    }
}
