package org.kirisame.mc;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import lombok.Getter;
import lombok.SneakyThrows;
import org.kirisame.mc.api.KirisamePlugin;
import org.kirisame.mc.api.KirisamePluginInfo;
import org.tinylog.Logger;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

public class PluginManager {
    static HashMap<String,PluginLoadInfo> plugins = new HashMap<>();

    @Getter
    static volatile boolean loaded = false;

    private static ClassLoader getPluginClassLoader(){
        return new ClassLoader() {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                try {
                    return super.loadClass(name,resolve);
                } catch (ClassNotFoundException e) {
                    try {
                        return KirisameMC.getInstance().getMinecraftClassLoader().loadClass(name);
                    }catch (Exception ignored){}
                    throw e;
                }
            }
        };
    }

    private static File[] getPlugins(){
        File pluginsDir = new File("kirisame_plugins");
        File[] files = pluginsDir.listFiles();
        return files == null ? new File[0] : files;
    }

    private static Class<?> findPluginMainClass(URLClassLoader classLoader){
        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .addClassLoader(classLoader)
                .enableAnnotationInfo()
                .scan()){
            ClassInfoList classes = scanResult.getClassesWithAnnotation(KirisamePluginInfo.class.getName());
            if (classes.size() == 1)
                return classes.getFirst().loadClass();
            return null;
        }
    }

    @SneakyThrows
    public static void loadPlugin(File pluginFile){
        URLClassLoader urlClassLoader = new URLClassLoader(
                new URL[]{pluginFile.toURI().toURL()},
                getPluginClassLoader()
        );
        Class<?> mainClass = findPluginMainClass(urlClassLoader);
        if (mainClass == null)
            return;

        KirisamePluginInfo annotation = mainClass.getAnnotation(KirisamePluginInfo.class);

        if (!KirisameMC.getInstance().getMinecraftInstance().getMinecraftVersion().equals(annotation.minecraftVersion())){
            Logger.warn("Plugin {} is not compatible with this version of Minecraft Server", annotation.name());
            return;
        }

        Object object = mainClass.getConstructor().newInstance();
        PluginLoadInfo info = new PluginLoadInfo(pluginFile, urlClassLoader, (KirisamePlugin) object,
                new PluginInfo(annotation.name(), annotation.version(), annotation.author(), annotation.description(), annotation.minecraftVersion()));

        plugins.put(annotation.name(), info);
        Logger.info("Loaded plugin {} named {}", pluginFile.getName(), annotation.name());
    }

    public static void loadPlugins(){
        for (File plugin : getPlugins()) {
            if (plugin.isFile() && plugin.getName().endsWith(".jar")){
                try {
                    loadPlugin(plugin);
                } catch (Exception e) {
                    Logger.error(e, "Error when pre-loading plugin {}", plugin.getName());
                }
            }
        }
        loaded = true;
    }

    public static void onLoad(){
        for (PluginLoadInfo info : plugins.values()) {
            try {
                info.plugin().onLoad(KirisameMC.getInstance());
            } catch (Exception e) {
                Logger.error(e, "Error when loading plugin {}", info.pluginInfo().name());
            }
        }
    }

    public static void onUnload(){
        for (PluginLoadInfo info : plugins.values()) {
            try {
                info.plugin().onUnload(KirisameMC.getInstance());
            }catch (Exception e){
                Logger.error(e, "Error when unloading plugin {}", info.pluginInfo().name());
            }
        }
    }

    public record PluginLoadInfo(File pluginFile,ClassLoader classLoader, KirisamePlugin plugin, PluginInfo pluginInfo){

    }
    public record PluginInfo(String name, String version, String author,String description,String minecraftVersion){

    }
}
