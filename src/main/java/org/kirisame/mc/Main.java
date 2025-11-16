package org.kirisame.mc;

import org.kirisame.mc.api.command.CommandManager;
import org.tinylog.Logger;

import java.io.*;

public class Main {
    static KirisameMC kirisameMC;

    public static void main(String[] args) throws IOException {

        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        InputStream originalIn = System.in;

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

        PipedInputStream newSystemIn = new PipedInputStream();
        PipedOutputStream inputOfSystemIn = new PipedOutputStream(newSystemIn);

        Thread inputRedirectThread = new Thread(() -> {
            BufferedReader br = new BufferedReader(new InputStreamReader(originalIn));
            String line;

            try {
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("!!")) {
                        String all = line.replaceAll("!!", "");
                        int i = CommandManager.execute(all.split(" "));
                        if (i != 1) {
                            Logger.error("Command Error: " + i);
                        }
                    } else {
                        inputOfSystemIn.write((line + "\n").getBytes());
                        inputOfSystemIn.flush();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "Kirisame-Console-Handle");

        inputRedirectThread.setDaemon(true);
        inputRedirectThread.start();


        System.setIn(newSystemIn);
        System.setOut(teeOut);


        kirisameMC = KirisameMC.getInstance();
        kirisameMC.init();
    }
}
