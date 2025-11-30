package org.kirisame.mc;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.bytebuddy.agent.builder.AgentBuilder;
import org.jspecify.annotations.Nullable;
import org.kirisame.mc.api.agent.AgentStatus;
import org.kirisame.mc.api.plugin.KirisamePlugin;
import org.kirisame.mc.api.plugin.KirisameTransform;
import org.kirisame.mc.api.plugin.PluginDetails;
import org.tinylog.Logger;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class PluginManager {
    static final Map<String, PluginLoadInfo> plugins = new HashMap<>();
    static final Map<String, KirisameTransform> transforms = new HashMap<>();

    @Getter
    static volatile boolean loaded = false;

    /**
     * Custom ClassLoader for plugins that allows them to access classes from each other.
     * It follows the parent-first model.
     */
    private static class InterPluginClassLoader extends URLClassLoader {
        public InterPluginClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            // This method is called by loadClass() only after the parent ClassLoader fails to find the class.
            try {
                return super.findClass(name); // 1. Try to find in its own JAR.
            } catch (ClassNotFoundException e) {
                // 2. If not found, iterate over all other loaded plugins.
                for (PluginLoadInfo otherPlugin : plugins.values()) {
                    if (otherPlugin.classLoader() == this) continue;
                    try {
                        if (otherPlugin.classLoader() instanceof InterPluginClassLoader otherLoader) {
                            return otherLoader.findClassInSelf(name);
                        }
                    } catch (ClassNotFoundException ignored) {
                        // Continue to the next plugin.
                    }
                }
                throw e; // If not found in any plugin, re-throw.
            }
        }

        public Class<?> findClassInSelf(String name) throws ClassNotFoundException {
            return super.findClass(name);
        }
    }

    /**
     * Creates a bridge ClassLoader that can access both Kirisame's classes (via parent delegation)
     * and Minecraft's classes (by explicitly trying to load from it).
     */
    private static ClassLoader createPluginParentClassLoader() {
        // The implicit parent of this anonymous class is the ClassLoader that loaded PluginManager,
        // which is the one that knows about all Kirisame classes.
        return new ClassLoader() {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                try {
                    // 1. Delegate to parent first (finds Java & Kirisame classes).
                    return super.loadClass(name, resolve);
                } catch (ClassNotFoundException e) {
                    // 2. If parent fails, try to load from Minecraft's classloader.
                    try {
                        return KirisameMC.getInstance().getMinecraftClassLoader().loadClass(name);
                    } catch (ClassNotFoundException ex) {
                        // If Minecraft classloader also fails, throw the original exception.
                        throw e;
                    }catch (NullPointerException exn){
                        throw e;
                    }
                }
            }
        };
    }

    private static File[] getPluginsFiles() {
        File pluginsDir = new File("kirisame_plugins");
        if (!pluginsDir.exists()) {
            pluginsDir.mkdirs();
        }
        File[] files = pluginsDir.listFiles();
        return files == null ? new File[0] : files;
    }

    private static PluginDetails getPluginDetails(ClassLoader classLoader){
        InputStream stream = classLoader.getResourceAsStream("plugin.json");
        if (stream == null)
            return null;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }
            return KirisameMC.getGson().fromJson(jsonContent.toString(), PluginDetails.class);
        } catch (IOException e) {
            Logger.error(e, "Error reading plugin.json from classloader");
            return null;
        }
    }

    private static Class<?> findPluginMainClass(URLClassLoader classLoader, PluginDetails details) {
        try {
            return classLoader.loadClass(details.main());
        } catch (ClassNotFoundException e) {
            Logger.error(e, "Main class {} not found in plugin {}", details.main(), details.name());
            return null;
        }
    }

    private static Class<?> findPluginTransform(URLClassLoader classLoader, PluginDetails details) {
        try {
            return details.transform() == null ? null : classLoader.loadClass(details.transform());
        } catch (ClassNotFoundException e) {
            Logger.error(e, "Transform class {} not found in plugin {}", details.transform(), details.name());
            return null;
        }
    }

    @SneakyThrows
    public static PluginLoadInfo loadPlugin(File pluginFile) {
        // This parent loader can see both Kirisame and Minecraft classes.
        ClassLoader parentLoader = createPluginParentClassLoader();

        InterPluginClassLoader pluginClassLoader = new InterPluginClassLoader(
                new URL[]{pluginFile.toURI().toURL()},
                parentLoader
        );

        PluginDetails details = getPluginDetails(pluginClassLoader);
        if (details == null){
            pluginClassLoader.close();
            Logger.warn("Cannot find plugin.json in {}, skip it", pluginFile.getName());
            return null;
        }

        Class<?> mainClass = findPluginMainClass(pluginClassLoader, details);
        if (mainClass == null) {
            pluginClassLoader.close();
            return null;
        }

        Class<?> transformClass = findPluginTransform(pluginClassLoader, details);
        PluginTransform pluginTransform = PluginTransform.empty();
        if (transformClass != null){
            pluginTransform = new PluginTransform(transformClass,Folder.empty());
        }

        if (plugins.containsKey(details.name())) {
            Logger.warn("A plugin with the name '{}' is already loaded. Skipping {}.", details.name(), pluginFile.getName());
            pluginClassLoader.close();
            return null;
        }

        if (details.minecraftVersion() == null){
            Logger.warn("Plugin {} does not specify a Minecraft version. It may not be compatible with this server version.", details.name());
        }else if (!KirisameMC.getInstance().getMinecraftInstance().getMinecraftVersion().equals(details.minecraftVersion())) {
            Logger.warn("Plugin {} (for MC {}) may not be compatible with this server version ({}).", details.name(), details.minecraftVersion(), KirisameMC.getInstance().getMinecraftInstance().getMinecraftVersion());
        }

        PluginLoadInfo info = new PluginLoadInfo(pluginFile, pluginClassLoader, new PluginMain(mainClass,Folder.empty()), pluginTransform, details);

        plugins.put(details.name(), info);
        Logger.info("Loaded plugin {} version {} by {}.", info.pluginDetails.name(), info.pluginDetails.version(), info.pluginDetails.author());

        return info;
    }

    public static void loadPlugins() {
        for (File plugin : getPluginsFiles()) {
            if (plugin.isFile() && plugin.getName().endsWith(".jar")) {
                try {
                    PluginLoadInfo info = loadPlugin(plugin);

                    if (info != null) {
                        KirisameTransform transform = info.transform().load();
                        transforms.put(info.pluginDetails.name(), transform);
                    }
                } catch (Exception e) {
                    Logger.error(e, "An unexpected error occurred while loading plugin {}", plugin.getName());
                }
            }
        }
        loaded = true;
    }

    public static void onLoad() {
        for (PluginLoadInfo info : List.copyOf(plugins.values())) {
            try {
                KirisamePlugin plugin = info.plugin.load(info.pluginDetails());
                plugin.onLoad(KirisameMC.getInstance());
            } catch (Exception e) {
                Logger.error(e, "Error occurred while enabling plugin {}", info.pluginDetails().name());
            }
        }
    }

    public static void applyTransforms(){
        for (KirisameTransform transform : transforms.values()) {
            try {
                AgentStatus.setBuilder(transform.apply(AgentStatus.getInst(), AgentStatus.getBuilder()));
            } catch (Exception e) {
                Logger.error(e, "Error occurred while applying transform {}", transform.getClass().getName());
            }
        }
        AgentStatus.getBuilder().installOn(AgentStatus.getInst());
        Logger.info("All transforms have been applied.");
    }

    public static void onUnload() {
        for (PluginLoadInfo info : plugins.values()) {
            try {
                info.plugin().get().onUnload(KirisameMC.getInstance());
            } catch (Exception e) {
                Logger.error(e, "Error occurred while unloading plugin {}", info.pluginDetails().name());
            }
            try {
                info.classLoader().close();
            } catch (IOException e) {
                Logger.error(e, "Error closing classloader for plugin {}", info.pluginDetails().name());
            }
        }
        plugins.clear();
        loaded = false;
        Logger.info("All plugins have been unloaded.");
    }

    public record PluginLoadInfo(File pluginFile, URLClassLoader classLoader, PluginMain plugin, PluginTransform transform,
                                 PluginDetails pluginDetails) {
    }

    public record PluginMain(Class<?> mainClass, Folder<KirisamePlugin> obj){
        @SneakyThrows
        public KirisamePlugin load(PluginDetails details){
            if (obj.get().isPresent()) return obj.get().get();
            if (mainClass != null){
                obj.setObj((KirisamePlugin) mainClass.getConstructor().newInstance());
                obj.get().get().$set(details);
            }
            return obj.get().orElse(null);
        }

        public KirisamePlugin get(){
            return obj.get().orElseThrow(() -> new IllegalStateException("Plugin not loaded"));
        }
    }

    public record PluginTransform(Class<?> transformClass, Folder<KirisameTransform> obj){
        static final KirisameTransform defaultTransform = (inst, builder) -> builder;

        public static PluginTransform empty(){
            return new PluginTransform(null, Folder.empty());
        }
        @SneakyThrows
        public KirisameTransform load(){
            if (obj.get().isPresent()) return obj.get().get();
            if (transformClass != null){
                obj.setObj((KirisameTransform) transformClass.getConstructor().newInstance());
            }
            return obj.get().orElse(defaultTransform);
        }
        public KirisameTransform get(){
            return obj.get().orElse(defaultTransform);
        }
    }

    public static class Folder<T>{
        @Setter
        T obj;
        public Folder(T obj){
            this.obj = obj;
        }
        public static <S> Folder<S> empty(){
            return new Folder<S>(null);
        }
        public Optional<T> get(){
            return Optional.ofNullable(obj);
        }
    }

    @UtilityClass
    public class API{
        public PluginDetails getPluginDetails(String name){
            return plugins.get(name).pluginDetails();
        }

        public boolean isPluginLoaded(String name){
            return plugins.get(name).plugin().obj().get().isPresent();
        }

        @Nullable
        public KirisamePlugin getPlugin(String name){
            return plugins.get(name).plugin().get();
        }
    }
}
