/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.file;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import ru.recoin.api.file.ClientFile;
import ru.recoin.api.file.exception.FileLoadException;
import ru.recoin.api.file.exception.FileSaveException;
import ru.recoin.api.file.impl.ModuleFile;
import ru.recoin.common.util.logger.LoggerUtil;

public class FileController {
    private final List<ClientFile> clientFiles;
    private final File directory;
    private final File moduleConfigDirectory;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, r -> {
        Thread thread2 = new Thread(r, "Recoin-FileAutoSave");
        thread2.setDaemon(true);
        return thread2;
    });

    public FileController(List<ClientFile> clientFiles, File directory, File moduleConfigDirectory) {
        this.clientFiles = clientFiles;
        this.directory = directory;
        this.moduleConfigDirectory = moduleConfigDirectory;
        this.startAutoSave();
    }

    public void startAutoSave() {
        this.scheduler.scheduleAtFixedRate(() -> {
            try {
                this.saveFiles(true);
            }
            catch (FileSaveException e) {
                LoggerUtil.error("Failed to auto-save files: " + e.getMessage());
            }
        }, 1L, 1L, TimeUnit.MINUTES);
    }

    public void stopAutoSave() {
        this.scheduler.shutdown();
        try {
            if (!this.scheduler.awaitTermination(1L, TimeUnit.MINUTES)) {
                this.scheduler.shutdownNow();
            }
        }
        catch (InterruptedException e) {
            this.scheduler.shutdownNow();
        }
    }

    public void saveFiles() throws FileSaveException {
        this.saveFiles(false);
    }

    public void saveFiles(boolean silent) throws FileSaveException {
        if (this.clientFiles.isEmpty()) {
            if (!silent) {
                LoggerUtil.warn("No files to save from directory: " + this.directory.getPath());
            }
            return;
        }
        for (ClientFile clientFile : this.clientFiles) {
            try {
                clientFile.saveToFile(this.directory);
                if (silent) continue;
                LoggerUtil.info("Successfully saved file: " + clientFile.getName() + " to " + this.directory.getPath());
            }
            catch (FileSaveException e) {
                throw new FileSaveException("Failed to save file: " + clientFile.getName(), e);
            }
        }
    }

    public void loadFiles() throws FileLoadException {
        if (this.clientFiles.isEmpty()) {
            LoggerUtil.warn("No files to load from directory: " + this.directory.getPath());
            return;
        }
        FileLoadException failure = null;
        for (ClientFile clientFile : this.clientFiles) {
            try {
                clientFile.loadFromFile(this.directory);
                LoggerUtil.info("Successfully loaded file: " + clientFile.getName() + " from " + this.directory.getPath());
            }
            catch (Exception e) {
                FileLoadException current = new FileLoadException("Failed to load file: " + clientFile.getName(), e);
                if (failure == null) {
                    failure = current;
                    continue;
                }
                failure.addSuppressed(current);
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    public void saveFile(String fileName) throws FileSaveException {
        for (ClientFile clientFile : this.clientFiles) {
            if (!(clientFile instanceof ModuleFile)) continue;
            try {
                clientFile.saveToFile(this.moduleConfigDirectory, fileName);
                LoggerUtil.info("Successfully saved file: " + fileName + " to " + this.moduleConfigDirectory.getPath());
            }
            catch (FileSaveException e) {
                throw new FileSaveException("Failed to save file: " + fileName, e);
            }
        }
    }

    public void saveClientFile(String name) throws FileSaveException {
        for (ClientFile clientFile : this.clientFiles) {
            if (!clientFile.getName().equals(name)) continue;
            clientFile.saveToFile(this.directory);
            return;
        }
        throw new FileSaveException("Unknown client file: " + name);
    }

    public void resetModuleConfig() throws FileSaveException {
        for (ClientFile clientFile : this.clientFiles) {
            if (!(clientFile instanceof ModuleFile)) continue;
            ModuleFile moduleFile = (ModuleFile)clientFile;
            moduleFile.resetToFactoryDefaults();
            moduleFile.saveToFile(this.directory);
            return;
        }
        throw new FileSaveException("Module configuration is unavailable");
    }

    public void loadFile(String fileName) throws FileLoadException {
        for (ClientFile clientFile : this.clientFiles) {
            if (!(clientFile instanceof ModuleFile)) continue;
            try {
                clientFile.loadFromFile(this.moduleConfigDirectory, fileName);
                LoggerUtil.info("Successfully loaded file: " + fileName + " from " + this.moduleConfigDirectory.getPath());
            }
            catch (FileLoadException e) {
                throw new FileLoadException("Failed to load file: " + fileName, e);
            }
        }
    }
}

