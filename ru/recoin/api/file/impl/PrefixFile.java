/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonIOException
 *  com.google.gson.JsonSyntaxException
 */
package ru.recoin.api.file.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import ru.recoin.api.file.ClientFile;
import ru.recoin.api.file.exception.FileLoadException;
import ru.recoin.api.file.exception.FileSaveException;
import ru.recoin.implement.features.commands.CommandDispatcher;

public class PrefixFile
extends ClientFile {
    public PrefixFile() {
        super("prefix");
    }

    @Override
    public void saveToFile(File path) throws FileSaveException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File file = new File(path, this.getName() + ".json");
        try (FileWriter writer = new FileWriter(file);){
            gson.toJson((Object)CommandDispatcher.prefix, (Appendable)writer);
        }
        catch (JsonIOException | IOException e) {
            throw new FileSaveException(String.format("Failed to save %s to file", this.getName()), e);
        }
    }

    @Override
    public void loadFromFile(File path) throws FileLoadException {
        Gson gson = new Gson();
        File file = new File(path, this.getName() + ".json");
        if (!file.exists() || file.length() == 0L) {
            return;
        }
        try (FileReader reader = new FileReader(file);){
            String string = (String)gson.fromJson((Reader)reader, String.class);
            if (string != null && !string.isEmpty()) {
                CommandDispatcher.prefix = string;
            }
        }
        catch (IOException e) {
            throw new FileLoadException(String.format("Failed to load %s from file", this.getName()), e);
        }
        catch (JsonSyntaxException e) {
            throw new FileLoadException(String.format("JSON syntax error, %s config cannot be loaded", this.getName()), e);
        }
        catch (JsonIOException e) {
            throw new FileLoadException(String.format("JSON IO error, %s config cannot be loaded", this.getName()), e);
        }
    }
}

