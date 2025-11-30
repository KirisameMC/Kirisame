package org.kirisame.mc.api.plugin;

import lombok.Getter;
import lombok.Setter;
import org.kirisame.mc.KirisameMC;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

public abstract class KirisamePlugin {
    public void onLoad(KirisameMC kirisameMC){}
    public void onUnload(KirisameMC kirisameMC){}

    boolean hasSet = false;
    public void $set(PluginDetails details){
        if (hasSet) return;
        hasSet = true;
        this.details = details;
        this.name = details.name();
    }
    @Getter
    protected String name;
    @Getter
    protected PluginDetails details;

    TaggedLogger log(){
        return Logger.tag(name);
    }

    public void log$info(String message,Object... args){
        log().info(message,args);
    }

    public void log$warn(String message,Object... args){
        log().warn(message,args);
    }

    public void log$error(String message,Object... args){
        log().error(message,args);
    }

    public void log$error(Throwable throwable,String message,Object... args){
        log().error(throwable,message,args);
    }
}
