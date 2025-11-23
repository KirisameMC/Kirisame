package org.kirisame.mc;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.kirisame.mc.console.ConsoleParser;
import org.kirisame.mc.event.EventBus;
import org.kirisame.mc.event.impl.KirisameLoopEvent;
import org.kirisame.mc.minecraft.MinecraftInstance;
import org.kirisame.mc.reflect.ThreadReflect;
import org.kirisame.mc.server.WrapperFactory;
import org.kirisame.mc.server.wrapper.MinecraftWrapper;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class KirisameMC {

    @Getter
    static KirisameMC instance;
    @Getter
    Config configRoot;
    @Getter
    MinecraftInstance minecraftInstance;
    @Getter
    volatile
    ClassLoader minecraftClassLoader;
    @Getter
    Object server;
    ConsoleParser consoleParser = new ConsoleParser();
    @Getter
    MinecraftWrapper minecraftWrapper;

    {
        instance = this;
    }

    static {
        new KirisameMC();
    }

    public void _workdir_init(){
        if (!new File("kirisame_plugins").isDirectory()){
            new File("kirisame_plugins").mkdirs();
        }
    }

    public void _config_init() throws IOException {
        Config resource = ConfigFactory.parseResources("config.json");
        Config file = null;
        try {
            file = ConfigFactory.parseFile(new File("kirisame.config.json"));
        }catch (Exception ignored){

        }
        if (file != null && !file.isEmpty()) {
            file.withFallback(resource).resolve();
            configRoot = file;
        }else {
            configRoot = resource;
        }
        _config_save();
    }

    public void _config_save() throws IOException {
        String rendered = configRoot.root().render(
                ConfigRenderOptions.defaults()
                        .setJson(true)
                        .setFormatted(true)
                        .setOriginComments(false)
                        .setComments(false)
        );

        FileUtils.write(new File("kirisame.config.json"), rendered, StandardCharsets.UTF_8);
    }

    public void _startupMinecraft() throws Exception {
        minecraftInstance = new MinecraftInstance();
        minecraftInstance.load();
        minecraftInstance.start();
        new Thread(this::KirisameLoop,"KirisameMC").start();
    }

    public void consoleProcesser(String line){
        consoleParser.parse(line);
    }

    public void _init_plugins(){
        while (minecraftClassLoader == null){
            Thread.onSpinWait();
        }
        PluginManager.loadPlugins();
    }

    private void KirisameLoop() {
        Optional<Thread> serverThread = Optional.empty();
        Optional<Thread> serverWatchdogThread = Optional.empty();
        while (minecraftInstance.isRunning()){
            if (serverThread.isPresent() && serverWatchdogThread.isPresent()){
                minecraftClassLoader = serverWatchdogThread.get().getContextClassLoader();
                Logger.info("Successfully Startup KirisameMC!");
                break;
            }
            Set<Map.Entry<Thread, StackTraceElement[]>> entrySet = Thread.getAllStackTraces().entrySet();
            for (Map.Entry<Thread, StackTraceElement[]> threadEntry : entrySet) {
                Thread thread = threadEntry.getKey();
                if (serverThread.isEmpty() && thread.getName().equals("Server thread")){
                    serverThread = Optional.of(thread);
                }
                if (serverWatchdogThread.isEmpty() && thread.getName().equals("Server Watchdog")){
                    serverWatchdogThread = Optional.of(thread);
                }
            }
        }
        getServer:while (checkServerRunning(serverThread.get())){
            if (server == null){
                if (!serverThread.get().isAlive()) {
                    minecraftInstance.setRunning(false);
                    return;
                }
                try {
                    Class<?> loaded = minecraftClassLoader.loadClass("net.minecraft.server.dedicated.ServerWatchdog");
                    for (Field field : loaded.getDeclaredFields()) {
                        if (field.getName().equals("server")){
                            field.setAccessible(true);
                            Runnable serverwatchdog = ThreadReflect.getRunnable(serverWatchdogThread.get());
                            server = field.get(serverwatchdog);
                            minecraftWrapper = WrapperFactory.getWrapper(server,minecraftClassLoader);
                            break getServer;
                        }
                    }
                }catch (Exception e){
                    Logger.error(e, "Error when get server instance");
                }
            }
        }
        while ((!PluginManager.loaded) && checkServerRunning(serverThread.get())){
            Thread.onSpinWait();
        }
        PluginManager.onLoad();
        while (checkServerRunning(serverThread.get())){
            EventBus.post(KirisameLoopEvent.getInstance());
        }
        PluginManager.onUnload();
    }

    private boolean checkServerRunning(Thread server){
        if (!server.isAlive()){
            minecraftInstance.setRunning(false);
        }
        return minecraftInstance.isRunning();
    }

    public void init() {
        try {
            _workdir_init();
            _config_init();
            _startupMinecraft();
            _init_plugins();
        }catch (Exception e){
            Logger.error(e,"Start Kirisame Cause a problem");
        }
    }
}
