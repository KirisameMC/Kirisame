package org.kirisame.mc.api.plugin;

import org.kirisame.mc.KirisameMC;
import org.tinylog.Logger;

public interface KirisamePlugin {
    default void onLoad(KirisameMC kirisameMC){}
    default void onUnload(KirisameMC kirisameMC){}

    default void log$info(String message,Object... args){
        Logger.info(message,args);
    }

    default void log$warn(String message,Object... args){
        Logger.warn(message,args);
    }

    default void log$error(String message,Object... args){
        Logger.error(message,args);
    }

    default void log$error(Throwable throwable,String message,Object... args){
        Logger.error(throwable,message,args);
    }
}
