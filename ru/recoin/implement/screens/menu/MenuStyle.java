/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 *  org.joml.Matrix3x2fStack
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 */
package ru.recoin.implement.screens.menu;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import ru.recoin.api.system.animation.Animation;
import ru.recoin.api.system.animation.Direction;
import ru.recoin.api.system.animation.implement.DecelerateAnimation;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.common.util.render.ShapeShaderRenderer;
import ru.recoin.common.util.render.pipeline.DropShadowPipeline;
import ru.recoin.implement.features.modules.render.Hud;
import ru.recoin.implement.screens.menu.MenuControls;

public final class MenuStyle {
    public static final long OPEN_ANIMATION_MS = 190L;
    public static final long CLOSE_ANIMATION_MS = 165L;
    public static final double BUTTON_HOVER_SMOOTH = 2.2;
    public static final double CHIP_SELECTION_SMOOTH = 1.75;
    public static final int ACCENT = -8145409;
    public static final int TEXT = -986118;
    public static final int MUTED = -7301470;
    public static final int BORDER = 640695635;
    public static final int SURFACE = 604376344;
    public static final int CONTENT = 302650404;
    public static final int CARD = 403313700;
    public static final int CARD_HOVER = 706159414;
    private static final Map<Float, FontRenderer> REGULAR_FONTS = new HashMap<Float, FontRenderer>();
    private static final Map<Float, FontRenderer> MEDIUM_FONTS = new HashMap<Float, FontRenderer>();
    private static final DropShadowPipeline SHADOW = new DropShadowPipeline();

    public static int accent() {
        Hud hud = Hud.getInstance();
        if (hud != null) {
            hud.syncThemeAccent();
        }
        return hud != null ? hud.guiAccentColorSetting.getColor() : -8145409;
    }

    public static int surface() {
        return MenuStyle.background(604376344);
    }

    public static int content() {
        return MenuStyle.background(302650404);
    }

    public static int card() {
        return MenuStyle.background(403313700);
    }

    public static int moduleBackground() {
        return MenuStyle.background(1694696714);
    }

    public static int cardHover() {
        return MenuStyle.background(706159414);
    }

    public static float backgroundOpacity() {
        Hud hud = Hud.getInstance();
        return hud == null ? 1.0f : Math.max(0.1f, Math.min(1.0f, hud.guiOpacitySetting.getValue()));
    }

    private static int background(int color) {
        return ColorUtil.multAlpha(color, MenuStyle.backgroundOpacity());
    }

    public static int border() {
        return ColorUtil.overCol(640695635, ColorUtil.multAlpha(MenuStyle.accent(), 0.22f), 0.18f);
    }

    private MenuStyle() {
    }

    public static FontRenderer font(float size) {
        return REGULAR_FONTS.computeIfAbsent(Float.valueOf(size), value -> Fonts.create(value.floatValue() * 2.0f, "menu-inter-regular"));
    }

    public static FontRenderer medium(float size) {
        return MEDIUM_FONTS.computeIfAbsent(Float.valueOf(size), value -> Fonts.create(value.floatValue() * 2.0f, "menu-inter-medium"));
    }

    public static String fitMedium(String text, float width, float size) {
        return MenuStyle.fit(text, width, MenuStyle.medium(size));
    }

    public static String fit(String text, float width, float size) {
        return MenuStyle.fit(text, width, MenuStyle.font(size));
    }

    private static String fit(String text, float width, FontRenderer font) {
        int end;
        if (font.getStringWidth(text) <= width) {
            return text;
        }
        String suffix = "\u2026";
        if (font.getStringWidth(suffix) > width) {
            return "";
        }
        for (end = text.length(); end > 0 && font.getStringWidth(text.substring(0, end) + suffix) > width; --end) {
        }
        return text.substring(0, end) + suffix;
    }

    public static void fill(class_4587 matrix, float x, float y, float w, float h, float radius, int color) {
        ShapeShaderRenderer.renderRound(ShapeProperties.create(matrix, x, y, w, h).round(radius).color(color).build());
    }

    public static void separator(class_4587 matrix, float x, float y, float length, boolean vertical) {
        Matrix3x2fStack transform;
        float thickness = 0.65f;
        float endX = x + (vertical ? 0.0f : length);
        float endY = y + (vertical ? length : 0.0f);
        class_332 context = Render2DUtil.getActiveContext();
        if (context != null && Math.abs((transform = context.method_51448()).m01()) < 1.0E-4f && Math.abs(transform.m10()) < 1.0E-4f && transform.m00() > 1.0E-4f && transform.m11() > 1.0E-4f) {
            float pixels = class_310.method_1551().method_22683().method_4495();
            float scaleX = transform.m00() * pixels;
            float scaleY = transform.m11() * pixels;
            float offsetX = transform.m20() * pixels;
            float offsetY = transform.m21() * pixels;
            x = ((float)Math.round(x * scaleX + offsetX) - offsetX) / scaleX;
            y = ((float)Math.round(y * scaleY + offsetY) - offsetY) / scaleY;
            endX = ((float)Math.round(endX * scaleX + offsetX) - offsetX) / scaleX;
            endY = ((float)Math.round(endY * scaleY + offsetY) - offsetY) / scaleY;
            float scale = vertical ? scaleX : scaleY;
            thickness = (float)Math.max(1, Math.round(thickness * scale)) / scale;
        }
        MenuStyle.fill(matrix, x, y, vertical ? thickness : endX - x, vertical ? endY - y : thickness, 0.0f, MenuStyle.border());
    }

    public static void shadow(class_4587 matrix, float x, float y, float w, float h, float radius) {
        Hud hud = Hud.getInstance();
        if (hud != null && hud.shouldDisableShadows(false, true)) {
            return;
        }
        float strength = hud != null ? hud.getShadowStrength() : 1.0f;
        Matrix4f transform = matrix.method_23760().method_23761();
        Vector3f position = transform.transformPosition(x, y, 0.0f, new Vector3f());
        float scale = Math.abs(transform.m00());
        float r = radius * scale;
        SHADOW.draw(position.x, position.y, w * scale, h * scale, new float[]{r, r, r, r}, 16.0f * scale, 1.5f * scale, Render2DUtil.applyRenderAlpha(ColorUtil.multAlpha(-1207959552, strength)));
    }

    public static void gradient(class_4587 matrix, float x, float y, float w, float h, float radius, int top, int bottom) {
        ShapeShaderRenderer.renderRound(ShapeProperties.create(matrix, x, y, w, h).round(radius).color(top, top, bottom, bottom).build());
    }

    public static void gradientLeft(class_4587 matrix, float x, float y, float w, float h, float radius, int top, int bottom) {
        ShapeShaderRenderer.renderRound(ShapeProperties.create(matrix, x, y, w, h).round(0.0f, 0.0f, radius, radius).color(top, top, bottom, bottom).build());
    }

    public static void gradientRight(class_4587 matrix, float x, float y, float w, float h, float radius, int top, int bottom) {
        ShapeShaderRenderer.renderRound(ShapeProperties.create(matrix, x, y, w, h).round(radius, radius, 0.0f, 0.0f).color(top, top, bottom, bottom).build());
    }

    public static void bottomRight(class_4587 matrix, float x, float y, float w, float h, float radius, int color) {
        ShapeShaderRenderer.renderRound(ShapeProperties.create(matrix, x, y, w, h).round(0.0f, radius, 0.0f, 0.0f).color(color).build());
    }

    public static void outline(class_4587 matrix, float x, float y, float w, float h, float radius, int color) {
        MenuStyle.outline(matrix, x, y, w, h, radius, 1.0f, color);
    }

    public static void outline(class_4587 matrix, float x, float y, float w, float h, float radius, float thickness, int color) {
        ShapeShaderRenderer.renderRound(ShapeProperties.create(matrix, x, y, w, h).round(radius).thickness(thickness).color(color).build());
    }

    public static void toggle(class_4587 matrix, float x, float y, float progress) {
        MenuControls.toggle(matrix, x, y, progress);
    }

    public static <K> float toggleProgress(Map<K, Animation> animations, K key, boolean enabled) {
        Direction direction = enabled ? Direction.FORWARDS : Direction.BACKWARDS;
        Animation animation = animations.computeIfAbsent(key, ignored -> {
            Animation initial = new DecelerateAnimation().setMs(150).setValue(1.0);
            initial.setDirection(direction);
            return initial.complete();
        });
        animation.setDirection(direction);
        return animation.getOutput().floatValue();
    }

    public static void star(class_4587 matrix, float x, float y, float size, int color) {
        Fonts.getSize(Math.round(size * 2.0f), Fonts.Type.COMBINED_ICONS).drawString(matrix, "\ue0ab", x, y, color);
    }
}

