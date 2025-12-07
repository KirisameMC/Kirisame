package org.kirisame.mc;

import com.google.gson.Gson;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.kirisame.mc.console.ConsoleParser;
import org.kirisame.mc.event.EventBus;
import org.kirisame.mc.event.EventHandler;
import org.kirisame.mc.event.impl.KirisameLoopEvent;
import org.kirisame.mc.event.impl.reflect.AgentMessageEvent;
import org.kirisame.mc.minecraft.MinecraftInstance;
import org.kirisame.mc.server.WrapperFactory;
import org.kirisame.mc.server.wrapper.MinecraftWrapper;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class KirisameMC {
    public static final String REBOOT_FLAG = "REBOOT_FLAG.flag";

    @Getter
    static KirisameMC instance;
    @Getter
    static Gson gson = new Gson();
    @Getter
    Config configRoot;
    @Getter
    MinecraftInstance minecraftInstance;
    @Getter
    volatile
    ClassLoader minecraftClassLoader;
    @Getter
    volatile Object server;
    ConsoleParser consoleParser = new ConsoleParser();
    @Getter
    MinecraftWrapper minecraftWrapper;

    @Getter
    boolean rebootFlag = false;

    @SneakyThrows
    public void setRebootFlag(boolean flag){
        rebootFlag = flag;

        if (flag){
            if (!new File(REBOOT_FLAG).isFile()){
                new File(REBOOT_FLAG).createNewFile();
            }
        }else {
            if (new File(REBOOT_FLAG).isFile()){
                new File(REBOOT_FLAG).delete();
            }
        }
    }

    {
        instance = this;
        EventBus.register(this);
    }

    static {
        new KirisameMC();
    }

    protected void _workdir_init(){
        if (!new File("kirisame_plugins").isDirectory()){
            new File("kirisame_plugins").mkdirs();
        }
    }

    protected void _config_init() throws IOException {
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

    protected void _config_save() throws IOException {
        String rendered = configRoot.root().render(
                ConfigRenderOptions.defaults()
                        .setJson(true)
                        .setFormatted(true)
                        .setOriginComments(false)
                        .setComments(false)
        );

        FileUtils.write(new File("kirisame.config.json"), rendered, StandardCharsets.UTF_8);
    }

    protected void _loadMinecraft() throws Exception {
        minecraftInstance = new MinecraftInstance();
        minecraftInstance.load();
    }

    protected void _startupMinecraft(String[] args) throws Exception {
        minecraftInstance.start(args);
        new Thread(this::KirisameLoop,"KirisameMC").start();
    }

    public void consoleProcesser(String line){
        consoleParser.parse(line);
    }

    protected void _init_plugins(){
//        while (minecraftClassLoader == null){
//            Thread.onSpinWait();
//        }
        PluginManager.loadPlugins();
        PluginManager.applyTransforms();
    }

    boolean serverInitSuccessfully = true;

    @EventHandler
    private void agentMessageListener(AgentMessageEvent event){
        if (".getServerEvent".equals(event.getLabel())) server = event.getMessage();
        if (".serverInitEvent".equals(event.getLabel())) serverInitSuccessfully = (boolean) event.getMessage();
    }

    @SneakyThrows
    protected boolean getMinecraftServerRunningStatus(Object server){
        if (server == null) {
            return false;
        }

        final Field f1 = minecraftClassLoader.loadClass("net.minecraft.server.MinecraftServer")
                .getDeclaredField("running");
        f1.setAccessible(true);

        final Field f2 = minecraftClassLoader.loadClass("net.minecraft.server.MinecraftServer")
                .getDeclaredField("stopped");
        f2.setAccessible(true);

        return f1.getBoolean(server) && !f2.getBoolean(server);
    }

    enum KLoopStatus{
        LOOKUP_CLASSLOADER,
        LOOKUP_SERVER,
        WAIT_PLUGIN_MANAGER,
        LOAD_PLUGINS_MAIN,
        TICK,
        EXIT
    }

    KLoopStatus loopStatus = KLoopStatus.LOOKUP_CLASSLOADER;

    @SneakyThrows
    protected void KirisameLoop() {
        AtomicReference<Optional<Thread>> serverThread = new AtomicReference<>(Optional.empty());
        AtomicReference<Optional<Thread>> serverMain = new AtomicReference<>(Optional.empty());

        Thread serverThreadGetter = new Thread(()->{
            while (serverThread.get().isEmpty()){
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
                Thread.getAllStackTraces().keySet().stream().filter(t -> t.getName().equals("Server thread"))
                        .limit(1)
                        .findAny()
                        .ifPresent(t-> serverThread.set(Optional.of(t)));
                Thread.onSpinWait();
            }
        },"ServerThread-Getter");

        serverThreadGetter.start();

         new Thread(() -> {
            while (serverMain.get().isEmpty()) {
                Thread.getAllStackTraces().keySet().stream().filter(t -> t.getName().equals("ServerMain"))
                        .limit(1)
                        .findAny()
                        .ifPresent(t -> serverMain.set(Optional.of(t)));
                Thread.onSpinWait();
            }
        }, "ServerMain-Getter").start();


        while (serverMain.get().isEmpty()) Thread.onSpinWait();

        while (serverMain.get().get().isAlive()) Thread.onSpinWait();

        Thread.sleep(Duration.ofSeconds(2));

        if (serverThread.get().isEmpty()){
            serverThreadGetter.interrupt();
            minecraftInstance.setRunning(false);
            return;
        }

        minecraftClassLoader = serverThread.get().get().getContextClassLoader();
        loopStatus = KLoopStatus.LOOKUP_SERVER;

        while (server == null && serverInitSuccessfully) Thread.onSpinWait();

        if (!serverInitSuccessfully){
            minecraftInstance.setRunning(false);
            return;
        }

        minecraftWrapper = WrapperFactory.getWrapper(server,minecraftClassLoader);

        Logger.info("Successfully startup KirisameMC");

        loopStatus = KLoopStatus.WAIT_PLUGIN_MANAGER;

        ml:while (true){
            if (!getMinecraftServerRunningStatus(server)){
                loopStatus = KLoopStatus.EXIT;
            }
            switch (loopStatus){
                case WAIT_PLUGIN_MANAGER -> {
                    while (!PluginManager.isLoaded()) Thread.onSpinWait();
                    loopStatus = KLoopStatus.LOAD_PLUGINS_MAIN;
                }
                case LOAD_PLUGINS_MAIN -> {
                    PluginManager.onLoad();
                    loopStatus = KLoopStatus.TICK;
                }
                case TICK -> {
                    EventBus.post(KirisameLoopEvent.getInstance());
                    if (!getMinecraftServerRunningStatus(server)){
                        minecraftInstance.setRunning(false);
                        loopStatus = KLoopStatus.EXIT;
                    }
                }
                case EXIT -> {
                    if (PluginManager.isLoadedMain()){
                        PluginManager.onUnload();
                    }
                    break ml;
                }
            }
        }
    }

    public void init(String[] args) {
        try {
            _workdir_init();
            _config_init();
            _loadMinecraft();
            _init_plugins();
            _startupMinecraft(args);
        }catch (Exception e){
            Logger.error(e,"Start Kirisame Cause a problem");
        }
    }
}
