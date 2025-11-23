package org.kirisame.mc.event;

import lombok.experimental.UtilityClass;
import org.tinylog.Logger;

import java.lang.reflect.Method;
import java.util.*;

@UtilityClass
public class EventBus {
    // 存储：事件类型 -> 监听器列表
    private final Map<Class<?>, List<ListenerMethod>> listeners = new HashMap<>();

    public void register(Class<?> listenerClass){
        _register(null,listenerClass);
    }

    public void register(Object listenerObject) {
        _register(listenerObject, listenerObject.getClass());
    }

    private void _register(Object listenerObject, Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(EventHandler.class)) continue;

            Class<?>[] params = method.getParameterTypes();
            if (params.length != 1 || !Event.class.isAssignableFrom(params[0])) continue;

            method.setAccessible(true);
            Class<?> eventType = params[0];
            int priority = method.getAnnotation(EventHandler.class).priority();

            listeners.computeIfAbsent(eventType, k -> new ArrayList<>())
                    .add(new ListenerMethod(listenerObject, method, priority));

            // 排序：优先级高的先执行
            listeners.get(eventType).sort(Comparator.comparingInt(ListenerMethod::priority).reversed());
        }
    }

    public <T extends Event> T post(T event) {
        List<ListenerMethod> methods = listeners.get(event.getClass());
        if (methods != null) {
            for (ListenerMethod lm : methods) {
                try {
                    if (event instanceof CancelableEvent cancelableEvent){
                        if (cancelableEvent.isCancel() && lm.method().getAnnotation(EventHandler.class).ignoreCancelled()){
                            lm.method().invoke(lm.owner(), event);
                        } else if (!cancelableEvent.isCancel()) {
                            lm.method().invoke(lm.owner(), event);
                        }
                    }else {
                        lm.method().invoke(lm.owner(), event);
                    }
                } catch (Exception e) {
                    Logger.error(e, "An error occurred while processing an event to method {}:{}",lm.method().getDeclaringClass().getName(),lm.method().getName());
                }
            }
        }
        return event;
    }

    public record ListenerMethod(Object owner, Method method, int priority) {
    }
}