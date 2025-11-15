package org.kirisame.mc;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public class Main {
    static KirisameMC kirisameMC;

    public static void main(String[] args) {
        if (!isAddOpensEnabled()) {
            try {
                restartWithAddOpens(args);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        // 简单 Tee 输出流
        OutputStream teeStream = new OutputStream() {
            private final StringBuilder lineBuffer = new StringBuilder();

            @Override
            public synchronized void write(int b) {
                char c = (char) b;
                lineBuffer.append(c);
                originalOut.write(b);

                if (c == '\n') {
                    String line = lineBuffer.toString();
                    lineBuffer.setLength(0);
                    if (kirisameMC != null)
                        kirisameMC.consoleProcesser(line);
                }
            }
        };

        PrintStream teeOut = new PrintStream(teeStream, true);

        System.setOut(teeOut);


        kirisameMC = KirisameMC.getInstance();
        kirisameMC.init();
    }

    private static boolean isAddOpensEnabled() {
        List<String> jvmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
        return jvmArgs.stream().anyMatch(arg -> arg.equals("--add-opens") || arg.equals("--add-opens=java.base/java.lang=ALL-UNNAMED"));
    }

    private static void restartWithAddOpens(String[] args) throws IOException, InterruptedException {
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        String jarPath = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getAbsolutePath();

        List<String> command = new ArrayList<>();
        command.add(javaBin);
        command.add("--add-opens=java.base/java.lang=ALL-UNNAMED");
        command.add("-jar");
        command.add(jarPath);

        for (String arg : args) {
            command.add(arg);
        }

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.inheritIO();
        builder.start().waitFor();
    }
}
