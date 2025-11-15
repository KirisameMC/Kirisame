package org.kirisame.mc.console;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.kirisame.mc.console.message.ConsoleMessage;
import org.kirisame.mc.console.message.Message;
import org.kirisame.mc.console.parser.MessageParser;
import org.kirisame.mc.console.parser.Parser;
import org.kirisame.mc.event.EventBus;
import org.kirisame.mc.event.impl.ConsoleMessageEvent;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

public class ConsoleParser {
    static List<Parser<?>> parsers = new ArrayList<>();
    static {
        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .acceptPackages("org.kirisame.mc.console.parser.impl")
                .scan()){
            scanResult.getSubclasses(Parser.class).loadClasses().forEach(clazz->{
                try {
                    Parser<?> parser = (Parser<?>) clazz.getDeclaredConstructor().newInstance();
                    parsers.add(parser);
                }catch (Exception e){
                    Logger.error("Error to cast parser name {}",clazz.getName());
                }
            });
        }
    }

    MessageParser messageParser = new MessageParser();
    public void parse(String input){
        ConsoleMessage message = messageParser.parse(input);
        if (message == null) return;

        for (Parser<?> p : parsers) {
            if (!p.match(message.getMessage())) return;
            Message m = p.parse(message.getMessage());
            if (m != null){
                message.setContent(m);
                break;
            }
        }

        new Thread(()->EventBus.post(new ConsoleMessageEvent(message)),"Parser-Handle").start();
    }
}
