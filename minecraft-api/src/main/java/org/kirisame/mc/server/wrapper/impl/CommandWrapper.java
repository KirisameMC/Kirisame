package org.kirisame.mc.server.wrapper.impl;

import lombok.SneakyThrows;
import org.kirisame.mc.server.wrapper.MinecraftWrapper;
import org.kirisame.mc.server.wrapper.Wrapper;

import java.lang.reflect.Method;

public class CommandWrapper extends Wrapper {

    private final Method minecraftServer$getCommands;
    private final Method minecraftServer$createCommandSourceStack;
    private final Method commands$performPrefixedCommand;

    public CommandWrapper(MinecraftWrapper minecraftWrapper) {
        super(minecraftWrapper);

        // reflect init
        minecraftServer$getCommands = getReflect().loadClass("net.minecraft.server.MinecraftServer")
                .loadMethod("getCommands")
                .getTargetMethod();
        minecraftServer$createCommandSourceStack = getReflect().loadClass("net.minecraft.server.MinecraftServer")
                .loadMethod("createCommandSourceStack")
                .getTargetMethod();
        commands$performPrefixedCommand = getReflect().loadClass("net.minecraft.commands.Commands")
                .loadMethod("performPrefixedCommand",
                        getReflect().loadClass("net.minecraft.commands.CommandSourceStack").getTargetClazz(),
                        String.class)
                .getTargetMethod();
    }

    @SneakyThrows
    public void execute(String command){
        Object commandSourceStack = minecraftServer$createCommandSourceStack.invoke(getMinecraftWrapper().minecraftServer);
        Object commands = minecraftServer$getCommands.invoke(getMinecraftWrapper().minecraftServer);
        commands$performPrefixedCommand.invoke(commands, commandSourceStack, command);
    }
}
