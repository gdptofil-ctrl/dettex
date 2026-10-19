/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 */
package ru.recoin.api.file.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import ru.recoin.api.file.ClientFile;
import ru.recoin.api.file.exception.FileLoadException;
import ru.recoin.api.file.exception.FileSaveException;
import ru.recoin.api.repository.staff.StaffRegistry;

public final class StaffFile
extends ClientFile {
    public StaffFile() {
        super("staff");
    }

    @Override
    public void saveToFile(File path) throws FileSaveException {
        File file = new File(path, this.getName() + ".json");
        try (FileWriter writer = new FileWriter(file);){
            new GsonBuilder().setPrettyPrinting().create().toJson(StaffRegistry.names(), (Appendable)writer);
        }
        catch (IOException e) {
            throw new FileSaveException("Failed to save staff", e);
        }
    }

    @Override
    public void loadFromFile(File path) throws FileLoadException {
        File file = new File(path, this.getName() + ".json");
        if (!file.isFile()) {
            return;
        }
        try (FileReader reader = new FileReader(file);){
            String[] names = (String[])new Gson().fromJson((Reader)reader, String[].class);
            StaffRegistry.clear();
            if (names != null) {
                for (String name : names) {
                    if (name == null || name.isBlank()) continue;
                    StaffRegistry.add(name);
                }
            }
        }
        catch (Exception e) {
            throw new FileLoadException("Failed to load staff", e);
        }
    }
}

