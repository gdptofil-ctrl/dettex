/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonIOException
 *  com.google.gson.JsonSyntaxException
 *  net.minecraft.class_2248
 *  net.minecraft.class_7923
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
import java.util.ArrayList;
import java.util.Arrays;
import net.minecraft.class_2248;
import net.minecraft.class_7923;
import ru.recoin.api.file.ClientFile;
import ru.recoin.api.file.exception.FileLoadException;
import ru.recoin.api.file.exception.FileSaveException;
import ru.recoin.api.repository.box.BoxESPRepository;

public class BlockESPFile
extends ClientFile {
    private final BoxESPRepository repository;

    public BlockESPFile(BoxESPRepository repository) {
        super("blockESP");
        this.repository = repository;
    }

    @Override
    public void saveToFile(File path) throws FileSaveException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File file = new File(path, this.getName() + ".json");
        try (FileWriter writer = new FileWriter(file);){
            ArrayList blocksList = new ArrayList();
            this.repository.blocks.forEach((block, color) -> blocksList.add(new BlockESP(block.method_63499(), (int)color)));
            gson.toJson(blocksList, (Appendable)writer);
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
            BlockESP[] blockESP = (BlockESP[])gson.fromJson((Reader)reader, BlockESP[].class);
            this.repository.clearBlocks();
            if (blockESP != null) {
                Arrays.asList(blockESP).forEach(esp -> class_7923.field_41175.method_10220().filter(type -> type.method_63499().equals(esp.block)).findFirst().ifPresent(type -> this.repository.addBlock((class_2248)type, esp.color)));
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

    private record BlockESP(String block, int color) {
    }
}

