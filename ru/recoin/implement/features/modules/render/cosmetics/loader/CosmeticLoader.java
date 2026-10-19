/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.class_1011
 *  net.minecraft.class_1043
 *  net.minecraft.class_1044
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 */
package ru.recoin.implement.features.modules.render.cosmetics.loader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import ru.recoin.implement.features.modules.render.cosmetics.model.CosmeticModel;
import ru.recoin.implement.features.modules.render.cosmetics.model.ModelPosition;

public class CosmeticLoader {
    private static CosmeticLoader instance;
    private final Map<Integer, CosmeticModel> loadedCosmetics = new ConcurrentHashMap<Integer, CosmeticModel>();
    private final Map<String, class_2960> textureCache = new ConcurrentHashMap<String, class_2960>();

    public static CosmeticLoader getInstance() {
        if (instance == null) {
            instance = new CosmeticLoader();
        }
        return instance;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public CosmeticModel loadFromResource(String path) {
        try (InputStream in = CosmeticLoader.class.getResourceAsStream(path);){
            if (in == null) {
                CosmeticModel cosmeticModel2 = null;
                return cosmeticModel2;
            }
            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            CosmeticModel cosmeticModel = this.loadFromJson(json);
            return cosmeticModel;
        }
        catch (Exception e) {
            return null;
        }
    }

    public CosmeticModel loadFromJson(String json) {
        try {
            JsonObject obj = JsonParser.parseString((String)json).getAsJsonObject();
            return this.loadFromJson(obj);
        }
        catch (Exception e) {
            return null;
        }
    }

    public CosmeticModel loadFromJson(JsonObject obj) {
        try {
            if (obj.has("name") && obj.has("model")) {
                String name = obj.get("name").getAsString();
                int id = obj.has("id") ? obj.get("id").getAsInt() : name.hashCode();
                int category = obj.has("category") ? obj.get("category").getAsInt() : 1;
                CosmeticModel model = new CosmeticModel(name, id, category);
                JsonObject modelObj = obj.getAsJsonObject("model");
                model.setRawModelJson(modelObj.toString());
                if (obj.has("texture")) {
                    String b64 = obj.get("texture").getAsString();
                    class_2960 texId = this.loadTextureFromBase64(name, id, b64);
                    model.setTextureId(texId);
                }
                this.parseModelPosition(obj, model);
                if (obj.has("height")) {
                    model.setHeight(obj.get("height").getAsFloat());
                }
                if (obj.has("scale")) {
                    model.setScale(obj.get("scale").getAsFloat());
                }
                if (obj.has("previewScale")) {
                    model.setPreviewScale(obj.get("previewScale").getAsFloat());
                }
                if (obj.has("previewY")) {
                    model.setPreviewY(obj.get("previewY").getAsFloat());
                }
                if (obj.has("animation")) {
                    model.setAnimationJson(obj.getAsJsonObject("animation"));
                }
                this.loadedCosmetics.put(id, model);
                return model;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    private void parseModelPosition(JsonObject obj, CosmeticModel model) {
        if (obj.has("pos")) {
            model.setPosition(ModelPosition.getById(obj.get("pos").getAsInt()));
        }
        if (obj.has("scale")) {
            model.setScale(obj.get("scale").getAsFloat());
        }
        if (obj.has("x")) {
            model.setX(obj.get("x").getAsFloat());
        }
        if (obj.has("y")) {
            model.setY(obj.get("y").getAsFloat());
        }
        if (obj.has("z")) {
            model.setZ(obj.get("z").getAsFloat());
        }
        if (obj.has("yaw")) {
            model.setYaw(obj.get("yaw").getAsFloat());
        }
        if (obj.has("pitch")) {
            model.setPitch(obj.get("pitch").getAsFloat());
        }
        if (obj.has("roll")) {
            model.setRoll(obj.get("roll").getAsFloat());
        }
    }

    private class_2960 loadTextureFromBase64(String name, int id, String base64) {
        try {
            String key = name.replace(" ", "").toLowerCase() + "_" + id;
            if (this.textureCache.containsKey(key)) {
                return this.textureCache.get(key);
            }
            byte[] bytes = Base64.getDecoder().decode(base64);
            class_1011 image = class_1011.method_4309((InputStream)new ByteArrayInputStream(bytes));
            class_2960 texId = class_2960.method_60655((String)"recoin", (String)("cosmetic/" + key));
            Runnable registerTask = () -> {
                class_1043 texture = new class_1043(() -> "recoin cosmetic", image);
                class_310.method_1551().method_1531().method_4616(texId, (class_1044)texture);
                this.textureCache.put(key, texId);
            };
            if (class_310.method_1551().method_18854()) {
                registerTask.run();
            } else {
                class_310.method_1551().execute(registerTask);
            }
            this.textureCache.put(key, texId);
            return texId;
        }
        catch (Exception e) {
            return null;
        }
    }

    public CosmeticModel getCosmetic(int id) {
        return this.loadedCosmetics.get(id);
    }

    public Map<Integer, CosmeticModel> getAllCosmetics() {
        return this.loadedCosmetics;
    }
}

