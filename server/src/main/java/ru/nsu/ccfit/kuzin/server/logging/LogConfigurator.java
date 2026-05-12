package ru.nsu.ccfit.kuzin.server.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.*;


public final class LogConfigurator {
    private static boolean configured = false;

    private LogConfigurator() {
    }

    public static synchronized void configure(Path logPath, boolean enabled) throws IOException {
        Logger rootLogger = Logger.getLogger("");

        if (!enabled) {
            rootLogger.setLevel(Level.OFF);
            return;
        }

        if (configured) {
            return;
        }

        Files.createDirectories(logPath.toAbsolutePath().getParent());

        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }


        FileHandler fileHandler = new FileHandler(logPath.toString(), true) {
            @Override
            public synchronized void publish(LogRecord record) {
                super.publish(record);
                flush();
            }
        };
        fileHandler.setLevel(Level.ALL);
        fileHandler.setFormatter(new SimpleFormatter());

        rootLogger.addHandler(fileHandler);
        rootLogger.setLevel(Level.INFO);

        configured = true;
    }
}
