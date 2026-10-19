/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10444
 *  net.minecraft.class_1058
 *  net.minecraft.class_1799
 *  net.minecraft.class_1937
 *  net.minecraft.class_4587
 *  net.minecraft.class_5819
 *  net.minecraft.class_811
 *  net.minecraft.class_9848
 */
package ru.recoin.common.util.render;

import java.util.HashMap;
import net.minecraft.class_10444;
import net.minecraft.class_1058;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_4587;
import net.minecraft.class_5819;
import net.minecraft.class_811;
import net.minecraft.class_9848;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.render.Render2DUtil;

public final class ItemRender
implements QuickImports {
    public static final HashMap<class_1799, CachedSprite> SPRITE_CACHE = new HashMap();

    public static void drawItem(class_4587 matrix, class_1799 stack, float x, float y, boolean count, boolean bar) {
        CachedSprite cachedSprite = ItemRender.getSpriteTexture(stack);
        if (cachedSprite != null) {
            Render2DUtil.drawSprite(matrix, cachedSprite.sprite, x, y, 16.0f, 16);
        }
        if (count) {
            ItemRender.drawCount(matrix, stack, x, y);
        }
        if (bar) {
            ItemRender.drawBar(matrix, stack, x, y);
        }
    }

    private static void drawCount(class_4587 matrix, class_1799 stack, float x, float y) {
        Object text;
        int count = stack.method_7947();
        Object object = text = count > 1 ? "" + count : "";
        if (!((String)text).isEmpty()) {
            FontRenderer font = Fonts.getSize(16);
            font.drawString(matrix, (String)text, x + 16.0f - font.getStringWidth((String)text), y + 11.0f, ColorUtil.getText());
        }
    }

    private static void drawBar(class_4587 matrix, class_1799 stack, float x, float y) {
        if (stack.method_31578()) {
            rectangle.render(ShapeProperties.create(matrix, x + 1.5f, y + 13.0f, 13.0, 2.0).color(-16777216).build());
            rectangle.render(ShapeProperties.create(matrix, x + 1.5f, y + 13.0f, stack.method_31579(), 1.0).color(class_9848.method_61334((int)stack.method_31580())).build());
        }
    }

    private static CachedSprite getSpriteTexture(class_1799 stack) {
        return SPRITE_CACHE.computeIfAbsent(stack, key -> {
            class_10444 state = new class_10444();
            mc.method_65386().method_65596(state, stack, class_811.field_4317, (class_1937)ItemRender.mc.field_1687, null, 0);
            class_1058 sprite = ItemRender.getFirstParticleSprite(state);
            if (sprite != null) {
                return new CachedSprite(sprite, 0, 0xFFFFFF);
            }
            return null;
        });
    }

    private static class_1058 getFirstParticleSprite(class_10444 state) {
        if (state.method_65606()) {
            return null;
        }
        return state.method_65603(class_5819.method_43049((long)0L));
    }

    private ItemRender() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public record CachedSprite(class_1058 sprite, int glId, int color) {
    }
}

