package org.kirisame.mc.api.command;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

import java.util.HashMap;
import java.util.Map;

public class CommandManager {
    static HashMap<KirisameCommandLabel,KirisameCommand> commands = new HashMap<>();

    static {
        try (ScanResult scanResult = new ClassGraph().acceptPackages("org.kirisame.mc.command").enableClassInfo().scan()){

            scanResult.getClassesImplementing(KirisameCommand.class.getName()).loadClasses().forEach(commandClass -> {
                try {
                    KirisameCommand command = (KirisameCommand) commandClass.getConstructor().newInstance();
                    register(command);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public static void register(KirisameCommand command){
        commands.put(command.label(),command);
    }

    public static int execute(String[] args){
        if (args.length == 0)
            return 1;
        for (Map.Entry<KirisameCommandLabel, KirisameCommand> commandEntry : commands.entrySet()) {
            KirisameCommandLabel label = commandEntry.getKey();
            KirisameCommand command = commandEntry.getValue();
            if (label.lookup().contains(args[0])){
                return command.execute(args);
            }
        }
        return 0;
    }
}
