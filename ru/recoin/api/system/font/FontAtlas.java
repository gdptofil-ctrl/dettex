/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3298
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package ru.recoin.api.system.font;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.recoin.api.system.font.MsdfGlyph;

public class FontAtlas {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"recoin/FontAtlas");
    private final class_2960 jsonId;
    private final class_2960 textureId;
    private final Map<Integer, MsdfGlyph> glyphs = new HashMap<Integer, MsdfGlyph>();
    private final MsdfGlyph[] asciiGlyphs = new MsdfGlyph[128];
    private final MsdfGlyph[] cyrillicGlyphs = new MsdfGlyph[256];
    private final AtomicBoolean loaded = new AtomicBoolean(false);
    private float atlasWidth = 512.0f;
    private float atlasHeight = 512.0f;
    private float fontSize = 32.0f;
    private float lineHeight = 40.0f;
    private float distanceRange = 4.0f;
    private boolean yOriginBottom = false;

    public FontAtlas(class_2960 jsonId, class_2960 textureId) {
        this.jsonId = jsonId;
        this.textureId = textureId;
    }

    public void forceLoad() {
        this.ensureLoaded();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void ensureLoaded() {
        if (this.loaded.get()) {
            return;
        }
        FontAtlas fontAtlas = this;
        synchronized (fontAtlas) {
            if (this.loaded.get()) {
                return;
            }
            this.doLoad();
        }
    }

    private void doLoad() {
        try {
            InputStream inputStream = null;
            Optional resource = class_310.method_1551().method_1478().method_14486(this.jsonId);
            if (resource.isPresent()) {
                inputStream = ((class_3298)resource.get()).method_14482();
            } else {
                String path = "/assets/" + this.jsonId.method_12836() + "/" + this.jsonId.method_12832();
                inputStream = FontAtlas.class.getResourceAsStream(path);
            }
            if (inputStream == null) {
                LOGGER.warn("Font JSON not found: {}", (Object)this.jsonId);
                return;
            }
            try (InputStream is = inputStream;
                 InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);){
                this.parseJson(JsonParser.parseReader((Reader)reader).getAsJsonObject());
                this.loaded.set(true);
            }
        }
        catch (Exception exception) {
            LOGGER.error("Failed to load font: {}", (Object)this.jsonId, (Object)exception);
        }
    }

    private void parseJson(JsonObject root) {
        float emSize = 1.0f;
        if (root.has("atlas")) {
            JsonObject atlas = root.getAsJsonObject("atlas");
            this.atlasWidth = this.getFloat(atlas, "width", 512.0f);
            this.atlasHeight = this.getFloat(atlas, "height", 512.0f);
            this.fontSize = this.getFloat(atlas, "size", 32.0f);
            this.distanceRange = this.getFloat(atlas, "distanceRange", 4.0f);
            if (atlas.has("yOrigin")) {
                this.yOriginBottom = atlas.get("yOrigin").getAsString().equalsIgnoreCase("bottom");
            }
        }
        if (root.has("metrics")) {
            JsonObject metrics = root.getAsJsonObject("metrics");
            emSize = this.getFloat(metrics, "emSize", 1.0f);
            this.lineHeight = this.getFloat(metrics, "lineHeight", 1.2f) * this.fontSize;
        }
        if (root.has("glyphs")) {
            JsonArray glyphsArray = root.getAsJsonArray("glyphs");
            for (JsonElement element : glyphsArray) {
                this.parseGlyph(element.getAsJsonObject(), emSize);
            }
        }
    }

    private void parseGlyph(JsonObject glyphJson, float emSize) {
        float left;
        int unicode = -1;
        if (glyphJson.has("unicode")) {
            unicode = glyphJson.get("unicode").getAsInt();
        } else if (glyphJson.has("char")) {
            String value = glyphJson.get("char").getAsString();
            if (!value.isEmpty()) {
                unicode = value.codePointAt(0);
            }
        } else if (glyphJson.has("id")) {
            unicode = glyphJson.get("id").getAsInt();
        }
        if (unicode < 0) {
            return;
        }
        float advance = this.getFloat(glyphJson, "advance", 0.0f) * this.fontSize;
        if (advance == 0.0f) {
            advance = this.getFloat(glyphJson, "xadvance", 0.0f);
        }
        float x = 0.0f;
        float y = 0.0f;
        float width = 0.0f;
        float height = 0.0f;
        float xOffset = 0.0f;
        float yOffset = 0.0f;
        if (glyphJson.has("atlasBounds")) {
            JsonObject bounds = glyphJson.getAsJsonObject("atlasBounds");
            left = this.getFloat(bounds, "left", 0.0f);
            float bottom = this.getFloat(bounds, "bottom", 0.0f);
            float right = this.getFloat(bounds, "right", 0.0f);
            float top = this.getFloat(bounds, "top", 0.0f);
            x = left;
            width = right - left;
            height = top - bottom;
            y = this.yOriginBottom ? this.atlasHeight - top : bottom;
        } else if (glyphJson.has("x") && glyphJson.has("y")) {
            x = this.getFloat(glyphJson, "x", 0.0f);
            y = this.getFloat(glyphJson, "y", 0.0f);
            width = this.getFloat(glyphJson, "width", 0.0f);
            height = this.getFloat(glyphJson, "height", 0.0f);
        }
        if (glyphJson.has("planeBounds")) {
            JsonObject plane = glyphJson.getAsJsonObject("planeBounds");
            left = this.getFloat(plane, "left", 0.0f);
            float top = this.getFloat(plane, "top", 0.0f);
            xOffset = left * this.fontSize / emSize;
            yOffset = (0.95f - top / emSize) * this.fontSize;
        } else if (glyphJson.has("xoffset") && glyphJson.has("yoffset")) {
            xOffset = this.getFloat(glyphJson, "xoffset", 0.0f);
            yOffset = this.getFloat(glyphJson, "yoffset", 0.0f);
        }
        MsdfGlyph msdfGlyph = new MsdfGlyph(unicode, x, y, width, height, xOffset, yOffset, advance, this.atlasWidth, this.atlasHeight);
        this.glyphs.put(unicode, msdfGlyph);
        if (unicode >= 0 && unicode < 128) {
            this.asciiGlyphs[unicode] = msdfGlyph;
        } else if (unicode >= 1024 && unicode < 1280) {
            this.cyrillicGlyphs[unicode - 1024] = msdfGlyph;
        }
    }

    private float getFloat(JsonObject object, String key, float fallback) {
        return object.has(key) ? object.get(key).getAsFloat() : fallback;
    }

    MsdfGlyph getGlyph(int codePoint) {
        if (codePoint >= 0 && codePoint < 128) {
            return this.asciiGlyphs[codePoint];
        }
        if (codePoint >= 1024 && codePoint < 1280) {
            return this.cyrillicGlyphs[codePoint - 1024];
        }
        return this.glyphs.get(codePoint);
    }

    boolean hasGlyph(int codePoint) {
        this.ensureLoaded();
        if (codePoint >= 0 && codePoint < 128) {
            return this.asciiGlyphs[codePoint] != null;
        }
        if (codePoint >= 1024 && codePoint < 1280) {
            return this.cyrillicGlyphs[codePoint - 1024] != null;
        }
        return this.glyphs.containsKey(codePoint);
    }

    public class_2960 getTextureId() {
        return this.textureId;
    }

    public float getFontSize() {
        return this.fontSize;
    }

    public float getLineHeight() {
        return this.lineHeight;
    }

    public float getAtlasWidth() {
        return this.atlasWidth;
    }

    public float getAtlasHeight() {
        return this.atlasHeight;
    }

    public float getDistanceRange() {
        return this.distanceRange;
    }

    public int getGlyphCount() {
        return this.glyphs.size();
    }
}

