/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_2561
 *  net.minecraft.class_2583
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 *  org.apache.commons.lang3.mutable.MutableFloat
 *  org.apache.commons.lang3.mutable.MutableInt
 */
package ru.recoin.api.system.font;

import java.awt.Font;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import ru.recoin.api.event.EventManager;
import ru.recoin.api.system.font.FontAtlas;
import ru.recoin.api.system.font.FontPipeline;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.implement.events.render.TextFactoryEvent;
import ru.recoin.implement.screens.menu.MenuScreen;

public class FontRenderer {
    private static final FontPipeline PIPELINE = new FontPipeline();
    private static final Charset WINDOWS_1251 = Charset.forName("windows-1251");
    private static final Map<String, String> TEXT_CACHE = new ConcurrentHashMap<String, String>();
    private static final Set<Integer> LOGGED_MISSING_GLYPHS = ConcurrentHashMap.newKeySet();
    private static final String[] GLYPH_STRING_CACHE = new String[2048];
    private final FontAtlas atlas;
    private final FontAtlas fallbackAtlas;
    private final float size;
    private final boolean disabled;
    private final Map<String, Float> widthCache = new ConcurrentHashMap<String, Float>();
    private final Map<String, TextBounds> boundsCache = new ConcurrentHashMap<String, TextBounds>();
    private final Font font;

    public static String getGlyphString(int codePoint) {
        if (codePoint >= 0 && codePoint < GLYPH_STRING_CACHE.length && GLYPH_STRING_CACHE[codePoint] != null) {
            return GLYPH_STRING_CACHE[codePoint];
        }
        return new String(Character.toChars(codePoint));
    }

    public FontRenderer(FontAtlas atlas, float size) {
        this(atlas, size, size);
    }

    public FontRenderer(FontAtlas atlas, float size, float exposedSize) {
        this(atlas, null, size, exposedSize);
    }

    public FontRenderer(FontAtlas atlas, FontAtlas fallbackAtlas, float size, float exposedSize) {
        this.atlas = atlas;
        this.fallbackAtlas = fallbackAtlas;
        this.size = size;
        this.disabled = false;
        this.font = new Font("SansSerif", 0, Math.max(1, Math.round(exposedSize)));
        this.atlas.forceLoad();
        if (fallbackAtlas != null) {
            fallbackAtlas.forceLoad();
        }
    }

    private FontRenderer(float exposedSize) {
        this.atlas = null;
        this.fallbackAtlas = null;
        this.size = 0.0f;
        this.disabled = true;
        this.font = new Font("SansSerif", 0, Math.max(1, Math.round(exposedSize)));
    }

    public static FontRenderer disabled(float exposedSize) {
        return new FontRenderer(exposedSize);
    }

    public static void flushQueuedText(class_332 context) {
        PIPELINE.flushQueuedText(context);
    }

    public static void flushQueuedText(class_332 context, List<TextOcclusion> occlusions) {
        PIPELINE.flushQueuedText(context, occlusions);
    }

    public static void setMenuTextBatchActive(boolean active) {
        FontPipeline.setMenuTextBatchActive(active);
    }

    public void drawText(class_4587 matrix, class_2561 text, double x, double y) {
        StringBuilder builder = new StringBuilder();
        this.findStyle(builder, text);
        this.drawString(matrix, builder.toString(), x, y, ColorUtil.getText());
    }

    public void findStyle(StringBuilder builder, class_2561 component) {
        component.method_27658((style, string) -> {
            if (style.method_10973() != null) {
                builder.append(ColorUtil.formatting(0xFF000000 | style.method_10973().method_27716()));
            }
            builder.append(string);
            if (style.method_10973() != null) {
                builder.append(class_124.field_1070);
            }
            return Optional.empty();
        }, class_2583.field_24360);
    }

    public void drawStringWithScroll(class_4587 matrix, String text, double x, double y, float width, int color) {
        String separation = "  |  ";
        float textWidth = this.getStringWidth(text + separation);
        if (textWidth - width < 10.0f) {
            this.drawString(matrix, text, x, y, color);
            return;
        }
        this.drawString(matrix, text + separation + text, x - (double)MathUtil.textScrolling(textWidth), y, color);
    }

    public void drawString(class_4587 matrix, String text, double x, double y, int color) {
        if (this.disabled) {
            return;
        }
        if ((text = this.processText(text)).isEmpty()) {
            return;
        }
        y += (double)Render2DUtil.getTextYOffset();
        if (ColorUtil.alpha(color = Render2DUtil.applyRenderAlpha(color)) <= 0) {
            return;
        }
        FontAtlas drawAtlas = this.selectAtlas(text);
        if (Render2DUtil.getActiveContext() != null) {
            PIPELINE.drawTextQueued(Render2DUtil.getActiveContext(), drawAtlas, text, (float)x, (float)y, this.size, color);
        } else {
            PIPELINE.drawText(drawAtlas, text, (float)x, (float)y, this.size, color);
        }
    }

    public void drawGradientString(class_4587 matrix, String text, double x, double y, int colorStart, int colorEnd) {
        if (this.disabled) {
            return;
        }
        if ((text = this.processText(text)).isEmpty()) {
            return;
        }
        y += (double)Render2DUtil.getTextYOffset();
        colorStart = Render2DUtil.applyRenderAlpha(colorStart);
        colorEnd = Render2DUtil.applyRenderAlpha(colorEnd);
        if (ColorUtil.alpha(colorStart) <= 0 && ColorUtil.alpha(colorEnd) <= 0) {
            return;
        }
        FontAtlas drawAtlas = this.selectAtlas(text);
        float cursorX = (float)x;
        int visibleLength = Math.max(1, this.stripFormatting(text).length() - 1);
        int index = 0;
        int offset = 0;
        while (offset < text.length()) {
            int codePoint = text.codePointAt(offset);
            int charCount = Character.charCount(codePoint);
            int consumed = this.getFormattingLength(text, offset, charCount);
            if (consumed > 0) {
                offset += consumed;
                continue;
            }
            String glyphText = FontRenderer.getGlyphString(codePoint);
            int color = this.interpolateColor(colorStart, colorEnd, (float)index / (float)visibleLength);
            if (Render2DUtil.getActiveContext() != null) {
                PIPELINE.drawTextQueued(Render2DUtil.getActiveContext(), drawAtlas, glyphText, cursorX, (float)y, this.size, color);
            } else {
                PIPELINE.drawText(drawAtlas, glyphText, cursorX, (float)y, this.size, color);
            }
            cursorX += PIPELINE.getTextWidth(drawAtlas, glyphText, this.size);
            ++index;
            offset += charCount;
        }
    }

    public void drawCenteredString(class_4587 stack, String text, double x, double y, int color) {
        this.drawString(stack, text, x - (double)(this.getStringWidth(text) / 2.0f), y, color);
    }

    public void drawGlowCenteredString(class_4587 stack, String text, double x, double y, int color) {
        if (this.disabled) {
            return;
        }
        text = this.processText(text);
        class_332 context = Render2DUtil.getActiveContext();
        color = Render2DUtil.applyRenderAlpha(color);
        if (context == null || text.isEmpty() || ColorUtil.alpha(color) <= 0) {
            return;
        }
        PIPELINE.drawTextGlowQueued(context, this.selectAtlas(text), text, (float)x - this.getStringWidth(text) / 2.0f, (float)y + Render2DUtil.getTextYOffset(), this.size, color);
    }

    public float getStringWidth(class_2561 text) {
        return text != null ? this.getStringWidth(text.getString()) : 0.0f;
    }

    public float getStringWidth(String text) {
        if (this.disabled || text == null || text.isEmpty()) {
            return 0.0f;
        }
        String processed = this.processText(text);
        Float cached = this.widthCache.get(processed);
        if (cached != null) {
            return cached.floatValue();
        }
        float width = PIPELINE.getTextWidth(this.selectAtlas(processed), processed, this.size);
        if (this.widthCache.size() > 2048) {
            this.widthCache.clear();
        }
        this.widthCache.put(processed, Float.valueOf(width));
        return width;
    }

    public float getStringHeight(class_2561 text) {
        return text != null ? this.getStringHeight(text.getString()) : 0.0f;
    }

    public TextBounds getStringBounds(String text) {
        if (this.disabled || text == null || text.isEmpty()) {
            return new TextBounds(0.0f, 0.0f, 0.0f, 0.0f);
        }
        String processed = this.processText(text);
        TextBounds cached = this.boundsCache.get(processed);
        if (cached != null) {
            return cached;
        }
        TextBounds bounds = PIPELINE.getTextBounds(this.selectAtlas(processed), processed, this.size);
        if (this.boundsCache.size() > 2048) {
            this.boundsCache.clear();
        }
        this.boundsCache.put(processed, bounds);
        return bounds;
    }

    public float getStringHeight(String text) {
        if (this.disabled) {
            return 0.0f;
        }
        String processed = this.processText(text == null || text.isEmpty() ? " " : text);
        return PIPELINE.getTextHeight(this.selectAtlas(processed), processed, this.size);
    }

    public boolean hasGlyph(int codePoint) {
        if (this.disabled) {
            return false;
        }
        return this.atlas.hasGlyph(codePoint) || this.fallbackAtlas != null && this.fallbackAtlas.hasGlyph(codePoint);
    }

    public void logMissingGlyphs(String text) {
        if (this.disabled) {
            return;
        }
        if (text == null || text.isEmpty()) {
            return;
        }
        int offset = 0;
        while (offset < text.length()) {
            int codePoint = text.codePointAt(offset);
            int charCount = Character.charCount(codePoint);
            int consumed = this.getFormattingLength(text, offset, charCount);
            if (consumed > 0) {
                offset += consumed;
                continue;
            }
            if (codePoint != 10 && !this.hasGlyph(codePoint) && LOGGED_MISSING_GLYPHS.add(codePoint)) {
                System.out.printf("[Recoin/Font] Missing Inter Bold glyph: U+%04X (%s)%n", codePoint, new String(Character.toChars(codePoint)));
            }
            offset += charCount;
        }
    }

    private FontAtlas selectAtlas(String text) {
        if (this.fallbackAtlas == null || text == null || text.isEmpty()) {
            return this.atlas;
        }
        int i = 0;
        while (i < text.length()) {
            int codePoint = text.codePointAt(i);
            int charCount = Character.charCount(codePoint);
            int consumed = this.getFormattingLength(text, i, charCount);
            if (consumed > 0) {
                i += consumed;
                continue;
            }
            if (codePoint != 10 && !this.atlas.hasGlyph(codePoint)) {
                return this.fallbackAtlas;
            }
            i += charCount;
        }
        return this.atlas;
    }

    public String trimToWidth(String text, int maxWidth, boolean backwards) {
        return backwards ? this.trimToWidthBackwards(text, maxWidth) : this.trimToWidth(text, maxWidth);
    }

    public String trimToWidth(String text, int maxWidth) {
        return text.substring(0, this.getTrimmedLength(text, maxWidth));
    }

    private int getTrimmedLength(String text, int maxWidth) {
        WidthLimitingVisitor visitor = new WidthLimitingVisitor(maxWidth);
        this.visitForwards(text, visitor);
        return visitor.getLength();
    }

    public String trimToWidthBackwards(String text, int maxWidth) {
        int charCount;
        MutableFloat widthLeft = new MutableFloat((float)maxWidth);
        MutableInt trimmedLength = new MutableInt(text.length());
        for (int i = text.length(); i > 0; i -= charCount) {
            int codePoint = text.codePointBefore(i);
            charCount = Character.charCount(codePoint);
            widthLeft.subtract(this.getStringWidth(FontRenderer.getGlyphString(codePoint)));
            if (!(widthLeft.floatValue() < 0.0f)) continue;
            trimmedLength.setValue(i + charCount);
            break;
        }
        return text.substring(trimmedLength.intValue());
    }

    private boolean visitForwards(String text, WidthLimitingVisitor visitor) {
        int charCount;
        for (int i = 0; i < text.length(); i += charCount) {
            int codePoint = text.codePointAt(i);
            charCount = Character.charCount(codePoint);
            if (visitor.accept(i, codePoint)) continue;
            return false;
        }
        return true;
    }

    private String processText(String text) {
        if (text == null) {
            return "";
        }
        if (TEXT_CACHE.size() > 2048) {
            TEXT_CACHE.clear();
        }
        if (class_310.method_1551().field_1755 instanceof MenuScreen || !EventManager.hasSubscribers(TextFactoryEvent.class)) {
            return TEXT_CACHE.computeIfAbsent(text, this::fixMojibake);
        }
        TextFactoryEvent event = new TextFactoryEvent(text);
        EventManager.callEvent(event);
        return TEXT_CACHE.computeIfAbsent(event.getText(), this::fixMojibake);
    }

    private String fixMojibake(String text) {
        if (text == null || text.isEmpty() || !this.looksLikeMojibake(text)) {
            return text;
        }
        try {
            String decoded = new String(text.getBytes(WINDOWS_1251), StandardCharsets.UTF_8);
            return this.mojibakeMarkers(decoded) < this.mojibakeMarkers(text) && this.formattingCount(decoded) >= this.formattingCount(text) ? decoded : text;
        }
        catch (Exception ignored) {
            return text;
        }
    }

    private boolean looksLikeMojibake(String text) {
        return text.indexOf(1056) >= 0 || text.indexOf(1057) >= 0 || text.indexOf(194) >= 0 || text.indexOf(65533) >= 0;
    }

    private int mojibakeMarkers(String text) {
        int markers = 0;
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (c != '\u0420' && c != '\u0421' && c != '\u00c2' && c != '\ufffd') continue;
            ++markers;
        }
        return markers;
    }

    private int formattingCount(String text) {
        int count = 0;
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (c != '\u00a7' && c != '\u23cf') continue;
            ++count;
        }
        return count;
    }

    private int getFormattingLength(String text, int index, int charCount) {
        int end;
        int codePoint = text.codePointAt(index);
        if (codePoint == 1042 && index + charCount < text.length() && text.codePointAt(index + charCount) == 167) {
            charCount += Character.charCount(167);
            codePoint = 167;
        }
        if ((codePoint == 167 || codePoint == 38 || codePoint == 65533) && index + charCount < text.length()) {
            int next = text.codePointAt(index + charCount);
            if (next == 35 && index + charCount + 7 <= text.length()) {
                return charCount + 7;
            }
            if ("0123456789abcdefklmnor".indexOf(Character.toLowerCase((char)next)) >= 0) {
                return charCount + Character.charCount(next);
            }
        }
        if (codePoint == 9167 && (end = text.indexOf(9167, index + charCount)) > index) {
            return end - index + 1;
        }
        return 0;
    }

    private String stripFormatting(String text) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            int codePoint = text.codePointAt(i);
            int charCount = Character.charCount(codePoint);
            int consumed = this.getFormattingLength(text, i, charCount);
            if (consumed > 0) {
                i += consumed;
                continue;
            }
            builder.appendCodePoint(codePoint);
            i += charCount;
        }
        return builder.toString();
    }

    private int interpolateColor(int colorStart, int colorEnd, float t) {
        float startAlpha = (float)(colorStart >> 24 & 0xFF) / 255.0f;
        float startRed = (float)(colorStart >> 16 & 0xFF) / 255.0f;
        float startGreen = (float)(colorStart >> 8 & 0xFF) / 255.0f;
        float startBlue = (float)(colorStart & 0xFF) / 255.0f;
        float endAlpha = (float)(colorEnd >> 24 & 0xFF) / 255.0f;
        float endRed = (float)(colorEnd >> 16 & 0xFF) / 255.0f;
        float endGreen = (float)(colorEnd >> 8 & 0xFF) / 255.0f;
        float endBlue = (float)(colorEnd & 0xFF) / 255.0f;
        int alpha = (int)((startAlpha + t * (endAlpha - startAlpha)) * 255.0f);
        int red = (int)((startRed + t * (endRed - startRed)) * 255.0f);
        int green = (int)((startGreen + t * (endGreen - startGreen)) * 255.0f);
        int blue = (int)((startBlue + t * (endBlue - startBlue)) * 255.0f);
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    public Font getFont() {
        return this.font;
    }

    static {
        for (int i = 0; i < GLYPH_STRING_CACHE.length; ++i) {
            if (!Character.isValidCodePoint(i)) continue;
            FontRenderer.GLYPH_STRING_CACHE[i] = new String(Character.toChars(i));
        }
    }

    public record TextBounds(float x, float y, float width, float height) {
    }

    private class WidthLimitingVisitor {
        private float widthLeft;
        private int length;

        private WidthLimitingVisitor(float maxWidth) {
            this.widthLeft = maxWidth;
        }

        public boolean accept(int index, int codePoint) {
            this.widthLeft -= FontRenderer.this.getStringWidth(new String(Character.toChars(codePoint)));
            if (this.widthLeft >= 0.0f) {
                this.length = index + Character.charCount(codePoint);
                return true;
            }
            return false;
        }

        public int getLength() {
            return this.length;
        }
    }

    public record TextOcclusion(float x, float y, float width, float height) {
    }
}

