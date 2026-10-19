/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2960
 */
package ru.recoin.api.system.font;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_2960;
import ru.recoin.api.system.font.FontAtlas;
import ru.recoin.api.system.font.FontRenderer;

public class Fonts {
    private static final Map<FontKey, FontRenderer> FONT_CACHE = new HashMap<FontKey, FontRenderer>();
    private static final Map<String, FontAtlas> ATLAS_CACHE = new HashMap<String, FontAtlas>();

    public static FontRenderer create(float size, String name) {
        if (Fonts.isIconFont(name)) {
            return FontRenderer.disabled(size);
        }
        FontAtlas atlas = Fonts.getAtlas(name);
        if (name.equals(Type.PLIANT_BOLD2.getType()) || name.equals(Type.PLIANT_BOLD.getType()) || name.equals(Type.PLIANT_MEDIUM.getType()) || name.equals(Type.PLIANT_REGULAR.getType())) {
            return new FontRenderer(atlas, Fonts.getAtlas(Type.DEFAULT.getType()), size / 2.0f, size);
        }
        return new FontRenderer(atlas, size / 2.0f, size);
    }

    private static boolean isIconFont(String name) {
        return name.equals(Type.ICONS.getType()) || name.equals(Type.GUI_ICONS.getType()) || name.equals(Type.HUD_ICONS.getType()) || name.equals(Type.CATEGORY_ICONS.getType()) || name.equals(Type.ICONSTYPETHO.getType()) || name.equals(Type.MAINMENU_ICONS.getType());
    }

    private static FontAtlas getAtlas(String name) {
        return ATLAS_CACHE.computeIfAbsent(name, path -> new FontAtlas(class_2960.method_60655((String)"minecraft", (String)("fonts/" + path + ".json")), class_2960.method_60655((String)"minecraft", (String)("fonts/" + path + ".png"))));
    }

    public static void init() {
        for (Type type : Type.values()) {
            for (int size = 4; size <= 32; ++size) {
                Fonts.getSize(size, type);
            }
        }
    }

    public static FontRenderer getSize(int size) {
        return Fonts.getSize(size, Type.DEFAULT);
    }

    public static FontRenderer getSize(int size, Type type) {
        Type resolvedType = Fonts.resolveLegacyType(type);
        return FONT_CACHE.computeIfAbsent(new FontKey(size, resolvedType), key -> Fonts.create(size, resolvedType.getType()));
    }

    private static Type resolveLegacyType(Type type) {
        return switch (type.ordinal()) {
            case 1, 2, 3 -> Type.INTER_BOLD;
            default -> type;
        };
    }

    public static FontRenderer getMenuSize(int size) {
        return Fonts.getMenuSize(size, Type.INTER_BOLD);
    }

    public static FontRenderer getMenuSize(int size, Type type) {
        int scaledSize = Math.max(1, size + 1);
        Type menuType = switch (type.ordinal()) {
            case 12, 13, 14, 15, 16, 17, 18, 19 -> type;
            default -> Type.INTER_BOLD;
        };
        return Fonts.getSize(scaledSize, menuType);
    }

    public static enum Type {
        DEFAULT("default"),
        BOLD("bold"),
        REGULAR("regular"),
        REGULARNEW("regularnew"),
        TEST("test"),
        INTER_MEDIUM("inter-medium"),
        INTER_SEMI("inter-semi"),
        INTER_BOLD("inter-bold"),
        PLIANT_BOLD2("pliant-bold2"),
        PLIANT_BOLD("pliant-bold"),
        PLIANT_MEDIUM("pliant-medium"),
        PLIANT_REGULAR("pliant-regular"),
        ICONS("icons"),
        GUI_ICONS("guiicons"),
        HUD_ICONS("hudicons"),
        CATEGORY_ICONS("categoryicons"),
        ICONSTYPETHO("iconstypetho"),
        MAINMENU_ICONS("mainmenuicons"),
        COMBINED_ICONS("combined-icons"),
        SOCIAL_ICONS("social-icons");

        private final String type;

        public String getType() {
            return this.type;
        }

        private Type(String type) {
            this.type = type;
        }
    }

    private record FontKey(int size, Type type) {
    }
}

