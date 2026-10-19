/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  net.minecraft.class_1011
 *  net.minecraft.class_1043
 *  net.minecraft.class_1044
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 */
package ru.recoin.implement.screens.mainmenu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_2960;
import net.minecraft.class_310;

final class WallpaperStore {
    private static final class_310 mc = class_310.method_1551();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static WallpaperStore instance;
    private final File directory;
    private final File configFile;
    private final List<Entry> entries = new ArrayList<Entry>();
    private String selectedId = "default";
    private class_2960 activeId;
    private int activeWidth;
    private int activeHeight;
    private final Map<String, class_2960> thumbIds = new LinkedHashMap<String, class_2960>();
    private final Map<String, int[]> thumbDimensions = new LinkedHashMap<String, int[]>();

    private WallpaperStore(File directory, File configFile) {
        this.directory = directory;
        this.configFile = configFile;
    }

    File directory() {
        return this.directory;
    }

    void refreshFromDirectory() {
        File[] files;
        if (!this.directory.exists()) {
            this.directory.mkdirs();
        }
        if ((files = this.directory.listFiles((dir, name) -> {
            String lower = name.toLowerCase(Locale.ROOT);
            return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".bmp");
        })) != null) {
            boolean changed = false;
            for (File file : files) {
                boolean exists = this.entries.stream().anyMatch(e -> e.file.equalsIgnoreCase(file.getName()));
                if (exists) continue;
                String id = UUID.randomUUID().toString().substring(0, 8);
                this.entries.add(new Entry(id, file.getName(), file.getName()));
                changed = true;
            }
            if (changed) {
                this.writeConfig();
            }
        }
    }

    static WallpaperStore getInstance() {
        if (instance == null) {
            File dir = new File(WallpaperStore.mc.field_1697, "recoin/wallpapers");
            File cfg = new File(WallpaperStore.mc.field_1697, "recoin/wallpapers.json");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            instance = new WallpaperStore(dir, cfg);
            instance.readConfig();
            if (!instance.isDefault() && !instance.isLoading()) {
                instance.loadActive();
            }
        }
        return instance;
    }

    boolean isDefault() {
        return "default".equals(this.selectedId);
    }

    boolean isLoading() {
        return "loading".equals(this.selectedId);
    }

    class_2960 activeTexture() {
        return this.activeId;
    }

    int activeWidth() {
        return this.activeWidth;
    }

    int activeHeight() {
        return this.activeHeight;
    }

    List<Entry> entries() {
        return Collections.unmodifiableList(this.entries);
    }

    String selectedId() {
        return this.selectedId;
    }

    void select(String id) {
        if (id.equals(this.selectedId)) {
            return;
        }
        this.selectedId = id;
        this.writeConfig();
        if ("default".equals(id) || "loading".equals(id)) {
            this.unloadActive();
        } else {
            this.loadActive();
        }
    }

    String add(File source) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        String ext = WallpaperStore.extensionOf(source.getName());
        String fileName = id + "." + ext;
        File target = new File(this.directory, fileName);
        try {
            Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e) {
            return null;
        }
        this.entries.add(new Entry(id, source.getName(), fileName));
        this.writeConfig();
        return id;
    }

    void remove(String id) {
        class_2960 tid;
        if ("default".equals(id) || "loading".equals(id)) {
            return;
        }
        Entry found = this.findEntry(id);
        this.entries.removeIf(e -> e.id.equals(id));
        if (this.selectedId.equals(id)) {
            this.selectedId = "default";
            this.unloadActive();
        }
        if (found != null) {
            new File(this.directory, found.file).delete();
        }
        if ((tid = this.thumbIds.remove(id)) != null) {
            mc.method_1531().method_4615(tid);
        }
        this.thumbDimensions.remove(id);
        this.writeConfig();
    }

    class_2960 loadThumb(String id) {
        if (this.thumbIds.containsKey(id)) {
            return this.thumbIds.get(id);
        }
        Entry entry = this.findEntry(id);
        if (entry == null) {
            return null;
        }
        File file = new File(this.directory, entry.file);
        if (!file.exists()) {
            return null;
        }
        class_1011 image = this.readImage(file);
        if (image == null) {
            return null;
        }
        class_1043 texture = new class_1043(() -> "recoin_wp_thumb", image);
        class_2960 identifier = class_2960.method_60655((String)"recoin", (String)("wallpaper/thumb_" + id));
        mc.method_1531().method_4616(identifier, (class_1044)texture);
        this.thumbIds.put(id, identifier);
        this.thumbDimensions.put(id, new int[]{image.method_4307(), image.method_4323()});
        return identifier;
    }

    int[] thumbSize(String id) {
        return this.thumbDimensions.getOrDefault(id, new int[]{1, 1});
    }

    void unloadThumbs() {
        this.thumbIds.forEach((id, identifier) -> mc.method_1531().method_4615(identifier));
        this.thumbIds.clear();
        this.thumbDimensions.clear();
    }

    private void loadActive() {
        this.unloadActive();
        Entry entry = this.findEntry(this.selectedId);
        if (entry == null) {
            return;
        }
        File file = new File(this.directory, entry.file);
        if (!file.exists()) {
            return;
        }
        class_1011 image = this.readImage(file);
        if (image == null) {
            this.activeId = null;
            return;
        }
        this.activeWidth = image.method_4307();
        this.activeHeight = image.method_4323();
        class_1043 texture = new class_1043(() -> "recoin_wallpaper", image);
        this.activeId = class_2960.method_60655((String)"recoin", (String)"wallpaper/active");
        mc.method_1531().method_4616(this.activeId, (class_1044)texture);
    }

    private class_1011 readImage(File file) {
        try (FileInputStream in = new FileInputStream(file)) {
            return class_1011.method_4309((InputStream)in);
        }
        catch (Throwable t) {
            try {
                BufferedImage bimg = ImageIO.read(file);
                if (bimg != null) {
                    int w = bimg.getWidth();
                    int h = bimg.getHeight();
                    class_1011 image = new class_1011(w, h, false);
                    int[] rgb = bimg.getRGB(0, 0, w, h, null, 0, w);
                    for (int y = 0; y < h; ++y) {
                        int row = y * w;
                        for (int x = 0; x < w; ++x) {
                            image.method_61941(x, y, rgb[row + x]);
                        }
                    }
                    return image;
                }
            }
            catch (Throwable throwable3) {
                // empty catch block
            }
            return null;
        }
    }

    private void unloadActive() {
        if (this.activeId != null) {
            mc.method_1531().method_4615(this.activeId);
            this.activeId = null;
        }
        this.activeWidth = 0;
        this.activeHeight = 0;
    }

    private Entry findEntry(String id) {
        return this.entries.stream().filter(e -> e.id.equals(id)).findFirst().orElse(null);
    }

    private void readConfig() {
        this.entries.clear();
        if (!this.configFile.exists()) {
            return;
        }
        try (FileReader reader = new FileReader(this.configFile);){
            Config config = (Config)GSON.fromJson((Reader)reader, Config.class);
            if (config != null) {
                if (config.wallpapers != null) {
                    this.entries.addAll(config.wallpapers);
                }
                if (config.selected != null) {
                    this.selectedId = config.selected;
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void writeConfig() {
        Config config = new Config();
        config.selected = this.selectedId;
        config.wallpapers = new ArrayList<Entry>(this.entries);
        try (FileWriter writer = new FileWriter(this.configFile);){
            GSON.toJson((Object)config, (Appendable)writer);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private static String extensionOf(String name) {
        int dot = name.lastIndexOf(46);
        return dot >= 0 ? name.substring(dot + 1).toLowerCase(Locale.ROOT) : "png";
    }

    static final class Entry {
        String id;
        String name;
        String file;

        Entry() {
        }

        Entry(String id, String name, String file) {
            this.id = id;
            this.name = name;
            this.file = file;
        }
    }

    private static final class Config {
        String selected;
        List<Entry> wallpapers;

        private Config() {
        }
    }
}

