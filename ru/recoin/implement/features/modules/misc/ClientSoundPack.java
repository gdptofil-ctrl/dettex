package ru.recoin.implement.features.modules.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3419;
import net.minecraft.class_5819;

/**
 * Sounds shipped in assets/recoin/sounds (CC0, see COSMETICS_CREDITS.txt), listed per use
 * in assets/recoin/sounds/catalog.json: mace, totem, kill, death.
 */
public final class ClientSoundPack {
    private static final String PATH = "/assets/recoin/sounds/catalog.json";
    private static final Map<String, List<String[]>> CATEGORIES = ClientSoundPack.load();
    private static final Map<String, class_3414> EVENTS = new ConcurrentHashMap<String, class_3414>();

    private ClientSoundPack() {
    }

    /** Display names of a category; never empty so SelectSetting.selected(values[0]) is safe. */
    public static String[] names(String category) {
        List<String[]> list = CATEGORIES.get(category);
        if (list == null || list.isEmpty()) {
            return new String[]{"-"};
        }
        String[] out = new String[list.size()];
        for (int i = 0; i < out.length; ++i) {
            out[i] = list.get(i)[0];
        }
        return out;
    }

    /** Plays the sound shown as {@code name} in {@code category} for the local player only. */
    public static void play(String category, String name, float volume) {
        List<String[]> list = CATEGORIES.get(category);
        if (list == null || name == null) {
            return;
        }
        for (String[] entry : list) {
            if (entry[0].equals(name)) {
                ClientSoundPack.playId(entry[1], volume);
                return;
            }
        }
    }

    public static void playId(String soundId, float volume) {
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1724 == null || mc.method_1483() == null || volume <= 0.0f) {
            return;
        }
        class_3414 event = EVENTS.computeIfAbsent(soundId, id -> class_3414.method_47908((class_2960)class_2960.method_60654((String)id)));
        mc.method_1483().method_4873((class_1113)new class_1109(event, class_3419.field_15245, volume, 1.0f, class_5819.method_43047(), mc.field_1724.method_24515()));
    }

    private static Map<String, List<String[]>> load() {
        HashMap<String, List<String[]>> result = new HashMap<String, List<String[]>>();
        try (InputStream stream = ClientSoundPack.class.getResourceAsStream(PATH)) {
            if (stream == null) {
                return result;
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader((Reader)reader).getAsJsonObject();
                for (Map.Entry<String, JsonElement> category : root.entrySet()) {
                    if (!category.getValue().isJsonArray()) continue;
                    ArrayList<String[]> entries = new ArrayList<String[]>();
                    JsonArray array = category.getValue().getAsJsonArray();
                    for (JsonElement element : array) {
                        JsonObject obj = element.getAsJsonObject();
                        entries.add(new String[]{obj.get("name").getAsString(), obj.get("sound").getAsString()});
                    }
                    result.put(category.getKey(), List.copyOf(entries));
                }
            }
        }
        catch (Exception exception) {
            // missing or broken catalog: the modules simply have nothing to play
        }
        return result;
    }
}
