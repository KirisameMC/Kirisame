package org.kirisame.mc.minecraft;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.Setter;
import org.kirisame.mc.KirisameMC;
import org.tinylog.Logger;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;

public class MinecraftInstance {

    String jarFileLoc;
    ClassLoader jarClassLoader;
    @Getter
    String minecraftVersion;
    @Getter
    @Setter
    volatile boolean running = false;

    public MinecraftInstance(){
        jarFileLoc = KirisameMC.getInstance().getConfigRoot().getString("main");
    }

    public void load() throws Exception {
        if (!new File(jarFileLoc).isFile()){
            throw new FileNotFoundException("Minecraft Server Not Found ("+jarFileLoc+") !");
        }

        jarClassLoader = new URLClassLoader(new URL[]{new File(jarFileLoc).toURI().toURL()},ClassLoader.getSystemClassLoader());

        InputStream versionJsonIs = jarClassLoader.getResourceAsStream("version.json");
        if (versionJsonIs == null){
            throw new IOException("Unable load Minecraft Server!");
        }
        { // get version.json
            JsonObject versionJson = JsonParser.parseReader(new InputStreamReader(versionJsonIs)).getAsJsonObject();
            minecraftVersion = versionJson.get("id").getAsString();
        }

        Logger.info("Found Minecraft Server, version {}",minecraftVersion);
    }

    public void start() throws Exception {
        Class<?> clazz = jarClassLoader.loadClass("net.minecraft.bundler.Main");
        Logger.info("Reflect Minecraft's main function");
        running = true;
        clazz.getMethod("main", String[].class).invoke(null, new Object[]{new String[]{"-nogui"}});
    }

    public Object getMinecraftServer() throws Exception {
        return KirisameMC.getInstance().getServer();
    }
}
