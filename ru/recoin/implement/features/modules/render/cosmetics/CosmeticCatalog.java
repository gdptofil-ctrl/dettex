package ru.recoin.implement.features.modules.render.cosmetics;

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
import java.util.Locale;
import java.util.Map;

/**
 * Lists the cosmetics shipped in assets/recoin/cosmetics/catalog.json.
 * To add a cosmetic: drop its JSON (or cape texture) into the assets and add an entry to the catalog.
 */
public final class CosmeticCatalog {
    public static final String WINGS = "wings";
    public static final String HATS = "hats";
    public static final String BACKPACKS = "backpacks";
    public static final String HALOS = "halos";
    public static final String PLAYER_MODELS = "player_models";
    public static final String CAPES = "capes";
    private static final String PATH = "/assets/recoin/cosmetics/catalog.json";
    private static final Map<String, List<Entry>> CATEGORIES = CosmeticCatalog.load();

    private CosmeticCatalog() {
    }

    public static List<Entry> entries(String category) {
        List<Entry> list = CATEGORIES.get(category);
        return list == null ? List.of() : list;
    }

    /** Display names for a select setting; never empty so SelectSetting.selected(values[0]) is safe. */
    public static String[] names(String category) {
        List<Entry> list = CosmeticCatalog.entries(category);
        if (list.isEmpty()) {
            return new String[]{"-"};
        }
        String[] out = new String[list.size()];
        for (int i = 0; i < out.length; ++i) {
            out[i] = list.get(i).name();
        }
        return out;
    }

    public static Entry find(String category, String name) {
        if (name == null) {
            return null;
        }
        for (Entry entry : CosmeticCatalog.entries(category)) {
            if (entry.name().equalsIgnoreCase(name)) {
                return entry;
            }
        }
        return null;
    }

    /** Menu icon (path inside the recoin namespace) for any category, or null. */
    public static String iconFor(String category, String name) {
        Entry entry = CosmeticCatalog.find(category, name);
        return entry == null ? null : entry.icon();
    }

    private static Map<String, List<Entry>> load() {
        HashMap<String, List<Entry>> result = new HashMap<String, List<Entry>>();
        try (InputStream stream = CosmeticCatalog.class.getResourceAsStream(PATH)) {
            if (stream == null) {
                return result;
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader((Reader)reader).getAsJsonObject();
                for (Map.Entry<String, JsonElement> category : root.entrySet()) {
                    if (!category.getValue().isJsonArray()) continue;
                    ArrayList<Entry> entries = new ArrayList<Entry>();
                    JsonArray array = category.getValue().getAsJsonArray();
                    for (JsonElement element : array) {
                        JsonObject obj = element.getAsJsonObject();
                        String name = obj.get("name").getAsString();
                        String file = obj.has("file") ? obj.get("file").getAsString() : null;
                        String texture = obj.has("texture") ? obj.get("texture").getAsString() : null;
                        String icon = obj.has("icon") ? obj.get("icon").getAsString() : null;
                        entries.add(new Entry(name, file, texture, icon));
                    }
                    result.put(category.getKey().toLowerCase(Locale.ROOT), List.copyOf(entries));
                }
            }
        }
        catch (Exception exception) {
            // a broken catalog only hides the cosmetics, it must never crash the client
        }
        return result;
    }

    public record Entry(String name, String file, String texture, String icon) {
        public String resourcePath() {
            return this.file == null ? null : "/assets/recoin/cosmetics/" + this.file;
        }
    }
}
