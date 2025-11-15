package org.kirisame.mc.server.wrapper;

import lombok.Getter;
import org.kirisame.mc.server.Reflect;

public class Wrapper {
    @Getter
    MinecraftWrapper minecraftWrapper;

    public Reflect getReflect() {
        return Reflect.create(minecraftWrapper.classLoader);
    }

    public Wrapper(MinecraftWrapper minecraftWrapper) {
        this.minecraftWrapper = minecraftWrapper;
    }
}
