package org.kirisame.mc.launcher;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Launcher {
    Config config;
    Config kirisame_need_config;
    Process process;
    private void init_config(){
        Config resource = ConfigFactory.parseResources("config.json");
        kirisame_need_config = ConfigFactory.parseResources("kirisame_need.json");
        Config file = null;
        if (new File("launcher.config.json").exists()){
            try {
                file = ConfigFactory.parseFile(new File("launcher.config.json"));
            } catch (Exception e) {
                Logger.error(e, "Error when loading launcher.config.json");
            }
        }
        if (file != null)
            config = file.withFallback(resource);
        else{
            config = resource;
            try {
                FileUtils.write(new File("launcher.config.json"), config.root().render(
                        ConfigRenderOptions.defaults()
                                .setComments(false)
                                .setOriginComments(false)
                                .setJson(true)
                                .setFormatted(true)
                ), "UTF-8");
            } catch (IOException e) {
                Logger.error(e, "Error when writing launcher.config.json");
            }
        }
        config = config.resolve();
    }

    @SneakyThrows
    private void startup_kirisame(){
        String kirisame_jar = config.getString("kirisame_jar");
        String java_executable = config.getString("java_executable");
        String agent_jar = config.getString("agent_jar");
        List<String> game_options = config.getStringList("game_options");
        List<String> jvm_options = config.getStringList("jvm_options");
        List<String> kirisame_need_jvm_options = kirisame_need_config.getStringList("jvm_options");

        List<String> commands_line = new ArrayList<>();

        commands_line.add(java_executable);
        commands_line.add("-javaagent:"+agent_jar);
        commands_line.addAll(jvm_options);
        commands_line.addAll(kirisame_need_jvm_options);
        commands_line.add("-jar");
        commands_line.add(kirisame_jar);
        commands_line.addAll(game_options);

        Logger.info("Starting KirisameMC with command: " + String.join(" ", commands_line));

        ProcessBuilder processBuilder = new ProcessBuilder(commands_line);
        processBuilder.inheritIO();
        process = processBuilder.start();
        process.waitFor();
    }

    public void enter(String[] args){
        init_config();
        startup_kirisame();
    }
}
