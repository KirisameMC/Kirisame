package org.kirisame.mc.api.agent;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.kirisame.mc.event.EventBus;
import org.kirisame.mc.event.impl.reflect.AgentMessageEvent;
import org.tinylog.Logger;

import java.util.HashMap;

public class AgentMessageBus {
    static HashMap<String, AgentMessageHandle> messageHandles = new HashMap<>();

    static {
        try (ScanResult scanResult = new ClassGraph().enableAnnotationInfo().scan()){
            ClassInfoList classes = scanResult.getClassesWithAnnotation(AgentMessageLabel.class);
            for (Class<?> loadClass : classes.loadClasses()) {
                try {
                    AgentMessageLabel annotation = loadClass.getAnnotation(AgentMessageLabel.class);
                    messageHandles.put(annotation.name(), (AgentMessageHandle) loadClass.getConstructor().newInstance());
                } catch (Exception e) {
                    Logger.error(e, "Error when loading message handle {}",loadClass.getName());
                }
            }
        }
    }

    public static Object post(String label,Object message){
        EventBus.post(new AgentMessageEvent(message,label));
        return messageHandles.getOrDefault(label, (m)->{
            Logger.warn("No message handle for label {}",label);
            return null;
        }).handle(message);
    }
}
