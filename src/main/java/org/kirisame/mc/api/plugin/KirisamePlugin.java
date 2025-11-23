package org.kirisame.mc.api.plugin;

import org.kirisame.mc.KirisameMC;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

public interface KirisamePlugin {
    default void onLoad(KirisameMC kirisameMC){}
    default void onUnload(KirisameMC kirisameMC){}

    default TaggedLogger log(){
//        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
//        StackTraceElement caller = null;
//        if (trace.length > 4)
//            caller = trace[3];
//        try {
//            if (trace.length > 4){
//                Class<?> callerClass = Class.forName(caller.getClassName(),true,Thread.currentThread().getContextClassLoader());
//
//                KirisamePluginInfo info = callerClass.getAnnotation(KirisamePluginInfo.class);
//
//                return Logger.tag(info.name());
//            }else {
//                throw new RuntimeException();
//            }
//        }catch (Exception ignored){
//            return caller != null ? Logger.tag(caller.getClassName()) : Logger.tag("Unknown");
//        }
        return Logger.tag(null);
    }

    default void log$info(String message,Object... args){
        log().info(message,args);
    }

    default void log$warn(String message,Object... args){
        log().warn(message,args);
    }

    default void log$error(String message,Object... args){
        log().error(message,args);
    }

    default void log$error(Throwable throwable,String message,Object... args){
        log().error(throwable,message,args);
    }
}
