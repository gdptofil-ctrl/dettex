/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMultimap
 *  com.google.common.collect.Multimap
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.properties.Property
 *  com.mojang.authlib.properties.PropertyMap
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1935
 *  net.minecraft.class_332
 *  net.minecraft.class_9296
 *  net.minecraft.class_9334
 */
package ru.recoin.implement.screens.autobuy;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;
import net.minecraft.class_332;
import net.minecraft.class_9296;
import net.minecraft.class_9334;
import ru.recoin.implement.features.modules.player.AutoBuy;

public final class SpecialItemIconRenderer {
    private static final Map<String, class_1799> CACHE = new HashMap<String, class_1799>();
    private static final Map<String, String> HEAD_TEXTURES = Map.of("\u0421\u0444\u0435\u0440\u0430 \u0425\u0430\u043e\u0441\u0430", "http://textures.minecraft.net/texture/e7a7ae7cdcf616e8b7a4221a621b2435753c60ed6a258ea060dae3002ffe9e28", "\u0421\u0444\u0435\u0440\u0430 \u0422\u0438\u0442\u0430\u043d\u0430", "http://textures.minecraft.net/texture/81e9698458b7841c96ae4f24ec84ae01724100641c564e2a7b185f406e8ed23", "\u0421\u0444\u0435\u0440\u0430 \u0410\u0440\u0435\u0441\u0430", "http://textures.minecraft.net/texture/c16adc6bafcb57fd707dee7dd6a736fe126711d53a1fd6ce789da41b3be13f2a", "\u0421\u0444\u0435\u0440\u0430 \u0411\u0435\u0441\u0442\u0438\u0438", "http://textures.minecraft.net/texture/5411ac17381b9fce9bab3c72afdb7f198570daf4732bd811d31c227d80fa39b1");

    public static void draw(class_332 context, AutoBuy.CatalogItem item, int x, int y) {
        context.method_51427(SpecialItemIconRenderer.resolve(item), x, y);
    }

    public static class_1799 resolve(AutoBuy.CatalogItem item) {
        return CACHE.computeIfAbsent(item.name(), name -> {
            String texture = HEAD_TEXTURES.get(name);
            if (texture == null) {
                return new class_1799((class_1935)(switch (name) {
                    case "\u0414\u0435\u0437\u043e\u0440\u0438\u0435\u043d\u0442\u0430\u0446\u0438\u044f" -> class_1802.field_8449;
                    case "\u042f\u0432\u043d\u0430\u044f \u043f\u044b\u043b\u044c" -> class_1802.field_8479;
                    case "\u0422\u0440\u0430\u043f\u043a\u0430" -> class_1802.field_22021;
                    default -> item.item();
                }));
            }
            String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + texture + "\"}}}";
            String value = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
            Property textureProperty = new Property("textures", value);
            PropertyMap properties = new PropertyMap((Multimap)ImmutableMultimap.of((Object)"textures", (Object)textureProperty));
            GameProfile profile = new GameProfile(UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)), "SpecialIcon", properties);
            class_1799 stack = new class_1799((class_1935)class_1802.field_8575);
            stack.method_57379(class_9334.field_49617, class_9296.method_73307((GameProfile)profile));
            return stack;
        });
    }

    private SpecialItemIconRenderer() {
    }
}

