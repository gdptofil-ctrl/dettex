/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.core.Appender
 *  org.apache.logging.log4j.core.Layout
 *  org.apache.logging.log4j.core.LoggerContext
 *  org.apache.logging.log4j.core.appender.FileAppender
 *  org.apache.logging.log4j.core.appender.FileAppender$Builder
 *  org.apache.logging.log4j.core.config.Configuration
 *  org.apache.logging.log4j.core.layout.PatternLayout
 */
package ru.recoin.common.util.logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;

public final class RecoinFileLogger {
    private static final String APPENDER_NAME = "RecoinFileAppender";

    private RecoinFileLogger() {
    }

    public static void install() {
        try {
            LoggerContext context = (LoggerContext)LogManager.getContext((boolean)false);
            Configuration configuration = context.getConfiguration();
            if (configuration.getAppender(APPENDER_NAME) != null) {
                return;
            }
            Path logPath = FabricLoader.getInstance().getGameDir().resolve("logs").resolve("dettexclient.log");
            Files.createDirectories(logPath.getParent(), new FileAttribute[0]);
            try { Files.deleteIfExists(logPath); } catch (Throwable ignored) {}
            try { Files.createFile(logPath, new FileAttribute[0]); } catch (Throwable ignored) {}
            PatternLayout layout = PatternLayout.newBuilder().withConfiguration(configuration).withPattern("%d{yyyy-MM-dd HH:mm:ss} [%t/%level]: %msg%n%throwable").build();
            FileAppender appender = ((FileAppender.Builder)((FileAppender.Builder)((FileAppender.Builder)((FileAppender.Builder)FileAppender.newBuilder().setName(APPENDER_NAME)).withFileName(logPath.toString()).withAppend(false).withLocking(false).withBufferedIo(false)).setConfiguration(configuration)).setLayout((Layout)layout)).build();
            if (appender == null) {
                throw new IllegalStateException("Could not create Dettex log appender");
            }
            appender.start();
            configuration.addAppender((Appender)appender);
            configuration.getRootLogger().addAppender((Appender)appender, null, null);
            context.updateLoggers();
            try { LogManager.getLogger((String)"DETTEX").info("File logging enabled: {}", (Object)logPath.toAbsolutePath()); } catch (Throwable ignored) {}
        }
        catch (Exception exception) {
            System.err.println("[RECOIN] Failed to initialize file logging: " + String.valueOf(exception));
            exception.printStackTrace(System.err);
        }
    }
}

