package org.kirisame.mc;

import lombok.Getter;
import lombok.SneakyThrows;
import org.kirisame.mc.api.plugin.KirisamePlugin;
import org.kirisame.mc.api.plugin.PluginDetails;
import org.tinylog.Logger;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginManager {
    static final Map<String, PluginLoadInfo> plugins = new HashMap<>();

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

    @SneakyThrows
    public static void loadPlugin(File pluginFile) {
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
            return;
        }

        Class<?> mainClass = findPluginMainClass(pluginClassLoader, details);
        if (mainClass == null) {
            pluginClassLoader.close();
            return;
        }

        if (plugins.containsKey(details.name())) {
            Logger.warn("A plugin with the name '{}' is already loaded. Skipping {}.", details.name(), pluginFile.getName());
            pluginClassLoader.close();
            return;
        }

        if (!KirisameMC.getInstance().getMinecraftInstance().getMinecraftVersion().equals(details.minecraftVersion())) {
            Logger.warn("Plugin {} (for MC {}) may not be compatible with this server version ({}).", details.name(), details.minecraftVersion(), KirisameMC.getInstance().getMinecraftInstance().getMinecraftVersion());
        }

        Object object = mainClass.getConstructor().newInstance();
        PluginLoadInfo info = new PluginLoadInfo(pluginFile, pluginClassLoader, (KirisamePlugin) object, details);

        plugins.put(details.name(), info);
        Logger.info("Loaded plugin {} version {} by {}.", info.pluginDetails.name(), info.pluginDetails.version(), info.pluginDetails.author());
    }

    public static void loadPlugins() {
        for (File plugin : getPluginsFiles()) {
            if (plugin.isFile() && plugin.getName().endsWith(".jar")) {
                try {
                    loadPlugin(plugin);
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
                info.plugin().onLoad(KirisameMC.getInstance());
            } catch (Exception e) {
                Logger.error(e, "Error occurred while enabling plugin {}", info.pluginDetails().name());
            }
        }
    }

    public static void onUnload() {
        for (PluginLoadInfo info : plugins.values()) {
            try {
                info.plugin().onUnload(KirisameMC.getInstance());
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

    public record PluginLoadInfo(File pluginFile, URLClassLoader classLoader, KirisamePlugin plugin,
                                 PluginDetails pluginDetails) {
    }
}
