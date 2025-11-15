package org.kirisame.mc.event;

import java.lang.reflect.Method;

public record ListenerMethod(Object owner, Method method, int priority) {
}
