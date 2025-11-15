package org.kirisame.mc.server;

import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Reflect {
    @Getter
    ClassLoader classLoader = Reflect.class.getClassLoader();
    @Getter
    Class<?> targetClazz;
    @Getter
    Method targetMethod;
    @Getter
    Field targetField;
    @Getter
    String packagePath = "";

    public static Reflect create(){
        return new Reflect();
    }

    public static Reflect create(ClassLoader classLoader){
        return new Reflect().setClassLoader(classLoader);
    }

    public Reflect setClassLoader(ClassLoader classLoader){
        this.classLoader = classLoader;
        return this;
    }

    public Reflect setPackagePath(String packagePath){
        this.packagePath = packagePath;
        return this;
    }

    @SneakyThrows
    public Reflect loadClass(String className) {
        if (!packagePath.isEmpty()){
            targetClazz = Class.forName(packagePath + "." + className,true,classLoader);
        }else {
            targetClazz = Class.forName(className,true,classLoader);
        }

        return this;
    }

    @SneakyThrows
    public Reflect loadMethod(String methodName) {
        targetMethod = targetClazz.getMethod(methodName);
        return this;
    }

    @SneakyThrows
    public Reflect loadMethod(String methodName, Class<?>... parameterTypes){
        targetMethod = targetClazz.getMethod(methodName,parameterTypes);
        return this;
    }

    @SneakyThrows
    public Reflect loadField(String fieldName) {
        targetField = targetClazz.getField(fieldName);
        return this;
    }
}
