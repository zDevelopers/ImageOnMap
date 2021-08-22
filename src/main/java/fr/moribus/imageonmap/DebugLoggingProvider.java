/*
 *All rights reserved to @UniverseCraft https://gist.github.com/UniverseCraft/cfd2b30c915c7ad7828009559ff036ff#file-debugloggingprovider-java
 * Thanks for the share :)
 **/

package fr.moribus.imageonmap;

import fr.zcraft.quartzlib.tools.reflection.Reflection;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

/**
 * Enables debug logging for Bukkit/Spigot plugins.
 * Note: Only works on java.util.logging.Logger!
 * Will not affect behavior of log4j.
 *
 * @author UniverseCraft
 */
public class DebugLoggingProvider {

    public static final String PACKAGE_NAME = Bukkit.getServer().getClass().getPackage().getName();
    public static final String API_VERSION =
            PACKAGE_NAME.substring(PACKAGE_NAME.lastIndexOf(".") + 1, PACKAGE_NAME.length()).substring(1);
    public static final String CB_PACKAGE = "org.bukkit.craftbukkit.";
    public static final String OBC_PACKAGE = CB_PACKAGE + "v" + API_VERSION + ".";
    public static final String NMS_PACKAGE = "net.minecraft.server.v" + API_VERSION + ".";
    private Writer writer;
    private ConsoleReaderWrapper consoleReader;
    private boolean useJline;

    // Enable debug logging for plugins.
    public void enableDebugLogging() {
        enableDebugLogging(Level.INFO, false);
    }

    // Enable debug logging for plugins, specifying a default log level for when none can be found.
    public void enableDebugLogging(Level defaultLevel, boolean showThread) {

        if (System.getProperty("enableDebugLogging") != null) {
            Bukkit.getLogger().warning(
                    "***Debug logging provider was already initialized somewhere"
                            + " else and should not be initialized again.");
            return;
        }

        System.setProperty("enableDebugLogging", "true");

        Bukkit.getLogger().info("Enabling Bukkit debug logging by UniverseCraft on API v" + API_VERSION + "...");

        // Ew, reflection
        try {
            Server server = Bukkit.getServer();
            Class craftServer =
                    Reflection.getBukkitClassByName("CraftServer");// Class.forName(OBC_PACKAGE + "CraftServer");

            Object cserver = craftServer.cast(server);

            Field craftServerConsole = craftServer.getDeclaredField("console");
            craftServerConsole.setAccessible(true);
            Object minecraftServer = craftServerConsole.get(cserver);

            Class<?> minekraftServer = Class.forName(NMS_PACKAGE + "MinecraftServer");
            Field minecraftServerReader = minekraftServer.getDeclaredField("reader");
            minecraftServerReader.setAccessible(true);
            Object consoleReader = minecraftServerReader.get(minecraftServer);
            this.consoleReader = new ConsoleReaderWrapper(consoleReader);

            Class<?> konsoleReader = Class.forName(CB_PACKAGE + "libs.jline.console.ConsoleReader");
            Field consoleReaderOut = konsoleReader.getDeclaredField("out");
            consoleReaderOut.setAccessible(true);
            Writer out = (Writer) consoleReaderOut.get(consoleReader);
            this.writer = out;

            Class<?> main = Class.forName(CB_PACKAGE + "Main");
            Field mainUseJline = main.getField("useJline");
            useJline = mainUseJline.getBoolean(null);
        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to initialize debug logging: reflection error");
            e.printStackTrace();
        }

        //
        // Repeal and replace ForwardLogHandler
        //

        Logger global = Logger.getLogger("");

        global.addHandler(new Handler() {
            private boolean useJline = DebugLoggingProvider.this.useJline;
            private Map<String, Plugin> pluginLoggers = new HashMap<>();
            private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            private Map<Long, Thread> threadIds = new HashMap<>();

            private Thread getThreadById(long id) {
                if (threadIds.containsKey(id)) {
                    return threadIds.get(id);
                }
                for (Thread thread : Thread.getAllStackTraces().keySet()) {
                    if (thread.getId() == id) {
                        threadIds.put(id, thread);
                        return thread;
                    }
                }
                return null;
            }

            // Hack to get around the fact that PluginLoggers are somehow not available via Logger#getLogger
            // and hence appear to have a log level of null
            private Plugin getPluginByLoggerName(String loggerName) {
                if (pluginLoggers.containsKey(loggerName)) {
                    return pluginLoggers.get(loggerName);
                }
                for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                    if (plugin.getClass().getCanonicalName().equals(loggerName)) {
                        pluginLoggers.put(loggerName, plugin);
                        return plugin;
                    }
                }
                return null;
            }

            private Level getLogLevel(String loggerName) {
                Logger logger = Logger.getLogger(loggerName);
                if (logger.getLevel() != null) {
                    return logger.getLevel();
                }
                Plugin plugin = getPluginByLoggerName(loggerName);
                if (plugin != null) {
                    return plugin.getLogger().getLevel();
                }
                return defaultLevel;
            }

            @Override
            public void publish(LogRecord record) {
                Level recordLevel = record.getLevel();
                if (recordLevel.intValue() > Level.CONFIG.intValue()) {
                    return; // It'll be handled by the default handler
                }

                Level logLevel = getLogLevel(record.getLoggerName());
                if (recordLevel.intValue() < logLevel.intValue()) {
                    return; // Insufficient loglevel
                }

                String rawMessage = record.getMessage();
                String date = dateFormat.format(record.getMillis());
                String messageWithThread = new StringBuilder("[").append(date).append("] [")
                        .append(getThreadById(record.getThreadID()).getName()).append("/").append(recordLevel)
                        .append("]: ").append(rawMessage).append("\n").toString();
                String messageWithoutThread =
                        new StringBuilder("[").append(date).append(" ").append(recordLevel).append("]: ")
                                .append(rawMessage).append("\n").toString();
                String message = showThread ? messageWithThread : messageWithoutThread;

                // From org.bukkit.craftbukkit.v1_12_R1.util.TerminalConsoleWriterThread, l.33-50
                try {
                    if (useJline) {
                        consoleReader.flush();
                        writer.write("\b\b" + message);
                        writer.flush();
                        try {
                            consoleReader.drawLine();
                        } catch (Throwable throwable) {
                            consoleReader.getCursorBuffer().clear();
                        }
                    } else {
                        writer.write(message);
                        writer.flush();
                    }
                } catch (IOException e) {
                    Bukkit.getLogger().warning("Exception in custom logging handler: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            @Override
            public void flush() {
                try {
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void close() throws SecurityException {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    // java.util.logging.Level uses different enum values than log4j's, which are what ultimately get outputted.
    // This converts from client-facing levels to log4j levels. Note that this mapping is not bijective.
    public String fromJulLevel(Level level) {
        if (level == Level.ALL) {
            return "ALL";
        }
        if (level == Level.SEVERE) {
            return "ERROR";
        }
        if (level == Level.WARNING) {
            return "WARN";
        }
        if (level == Level.INFO) {
            return "INFO";
        }
        if (level == Level.CONFIG) {
            return "DEBUG";
        }
        if (level == Level.FINE || level == Level.FINER || level == Level.FINEST) {
            return "TRACE";
        }
        return "OFF";
    }

    // Wrap necessary methods from some OBC classes.
    // ConsoleReaderWrapper wraps org.bukkit.craftbukkit.libs.jline.console.ConsoleReader.
    public static class ConsoleReaderWrapper {

        static Class<?> ConsoleReader;
        static Method print;
        static Method flush;
        static Method drawLine;
        static Method getCursorBuffer;
        static Map<ConsoleReaderWrapper, CursorBufferWrapper> cursorBuffers;

        static {
            cursorBuffers = new HashMap<>();

            try {
                ConsoleReader = Class.forName(CB_PACKAGE + "libs.jline.console.ConsoleReader");
                print = ConsoleReader.getMethod("print", CharSequence.class);
                flush = ConsoleReader.getMethod("flush");
                drawLine = ConsoleReader.getMethod("drawLine");
                getCursorBuffer = ConsoleReader.getMethod("getCursorBuffer");
            } catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        Object reader;

        public ConsoleReaderWrapper(Object obcReader) {
            this.reader = obcReader;
        }

        public void print(String str) {
            try {
                print.invoke(reader, (CharSequence) str);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        public void flush() {
            try {
                flush.invoke(reader);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        public void drawLine() {
            try {
                drawLine.invoke(reader);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        public CursorBufferWrapper getCursorBuffer() {
            if (cursorBuffers.containsKey(this)) {
                return cursorBuffers.get(this);
            }
            Object cursorBuffer = null;
            try {
                cursorBuffer = getCursorBuffer.invoke(reader);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
            CursorBufferWrapper bufferWrapper = new CursorBufferWrapper(cursorBuffer);
            cursorBuffers.put(this, bufferWrapper);
            return bufferWrapper;
        }

        // CursorBufferWrapper wraps org.bukkit.craftbukkit.libs.jline.console.CursorBuffer.
        public static class CursorBufferWrapper {
            static Class<?> CursorBuffer;
            static Method CursorBuffer_clear;

            static {
                try {
                    CursorBuffer = Class.forName(CB_PACKAGE + "libs.jline.console.CursorBuffer");
                    CursorBuffer_clear = CursorBufferWrapper.CursorBuffer.getMethod("clear");
                } catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            Object buffer;

            public CursorBufferWrapper(Object obcBuffer) {
                this.buffer = obcBuffer;
            }

            public void clear() {
                try {
                    CursorBuffer_clear.invoke(buffer);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}