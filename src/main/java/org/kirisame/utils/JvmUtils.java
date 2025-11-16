package org.kirisame.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

public class JvmUtils {
    public static String getCmdline(){
        StringBuilder sb = new StringBuilder();

        String javaHome = System.getProperty("java.home");
        String os = System.getProperty("os.name").toLowerCase();
        String javaExec = os.contains("win") ? javaHome + "\\bin\\java.exe" : javaHome + "/bin/java";
        sb.append(javaExec).append(" ");

        try {
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            List<String> jvmArgs = runtime.getInputArguments();
            for (String arg : jvmArgs) {
                sb.append(arg).append(" ");
            }
        } catch (Throwable ignored) {}

        String command = System.getProperty("sun.java.command");
        if (command != null && !command.isEmpty()) {
            String[] parts = command.split(" ", 2);
            String main = parts[0];
            String mainArgs = parts.length > 1 ? parts[1] : "";

            if (main.endsWith(".jar")) {
                sb.append("-jar ").append(main).append(" ");
                sb.append(mainArgs);
            } else {
                String cp = System.getProperty("java.class.path");
                if (cp != null && !cp.isEmpty()) {
                    sb.append("-cp ").append(cp).append(" ");
                }
                sb.append(main).append(" ");
                sb.append(mainArgs);
            }
        }

        return sb.toString().trim();
    }
}
