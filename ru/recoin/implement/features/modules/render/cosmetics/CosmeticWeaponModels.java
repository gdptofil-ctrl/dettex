/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParser
 *  net.minecraft.class_1799
 *  net.minecraft.class_2960
 *  net.minecraft.class_9280
 *  net.minecraft.class_9334
 */
package ru.recoin.implement.features.modules.render.cosmetics;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_9280;
import net.minecraft.class_9334;

public final class CosmeticWeaponModels {
    private static final class_2960 SWORD = class_2960.method_60655((String)"recoin", (String)"cosmetics/sword");
    private static final class_2960 MACE = class_2960.method_60655((String)"recoin", (String)"cosmetics/mace");
    private static final Map<String, class_9280> SWORDS = CosmeticWeaponModels.load(SWORD);
    private static final Map<String, class_9280> MACES = CosmeticWeaponModels.load(MACE);

    private CosmeticWeaponModels() {
    }

    public static boolean isCosmeticModel(class_1799 stack) {
        class_2960 model = (class_2960)stack.method_58694(class_9334.field_54199);
        return SWORD.equals((Object)model) || MACE.equals((Object)model);
    }

    public static class_1799 apply(class_1799 stack, String selected, boolean mace) {
        return CosmeticWeaponModels.apply(stack, selected, mace ? MACE : SWORD, mace ? MACES : SWORDS);
    }

    private static class_1799 apply(class_1799 stack, String selected, class_2960 model, Map<String, class_9280> models) {
        if (selected == null || selected.isEmpty()) {
            return stack;
        }
        class_9280 data = models.get(selected);
        if (data == null) {
            return stack;
        }
        if (data.equals(stack.method_58694(class_9334.field_49637)) && model.equals(stack.method_58694(class_9334.field_54199))) {
            return stack;
        }
        class_1799 renderedStack = stack.method_7972();
        renderedStack.method_57379(class_9334.field_54199, model);
        renderedStack.method_57379(class_9334.field_49637, data);
        return renderedStack;
    }

    /*
     * Enabled aggressive exception aggregation
     */
    private static Map<String, class_9280> load(class_2960 model) {
        String path = "/assets/" + model.method_12836() + "/items/" + model.method_12832() + ".json";
        try (InputStream stream = CosmeticWeaponModels.class.getResourceAsStream(path);){
            Map map;
            if (stream == null) {
                throw new IllegalStateException("Missing cosmetic item definition: " + path);
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);){
                HashMap<String, class_9280> models = new HashMap<String, class_9280>();
                for (JsonElement entry : JsonParser.parseReader((Reader)reader).getAsJsonObject().getAsJsonObject("model").getAsJsonArray("cases")) {
                    String name = entry.getAsJsonObject().get("when").getAsString();
                    class_9280 data = new class_9280(List.of(), List.of(), List.of(name), List.of());
                    models.put(name, data);
                    models.put(name.replace("'", ""), data);
                }
                map = Map.copyOf(models);
            }
            return map;
        }
        catch (IOException e) {
            throw new IllegalStateException("Unable to load cosmetic item definition: " + path, e);
        }
    }
}

