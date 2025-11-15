package org.kirisame.mc.reflect;

import java.lang.reflect.Field;
import java.util.Arrays;

public class ThreadReflect {
    public static Runnable getRunnable(Thread thread){
        try {
            Field field = Arrays.stream(thread.getClass().getDeclaredFields())
                    .filter(f -> f.getName().equals("holder"))
                    .findFirst().get();
            field.setAccessible(true);
            Object holder = field.get(thread);
            Field taskF = Arrays.stream(holder.getClass().getDeclaredFields())
                    .filter(f -> f.getName().equals("task"))
                    .findFirst().get();
            taskF.setAccessible(true);
            return  (Runnable) taskF.get(holder);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
