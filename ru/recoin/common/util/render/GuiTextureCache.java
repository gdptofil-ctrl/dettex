/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  net.minecraft.class_11231
 *  net.minecraft.class_12137
 */
package ru.recoin.common.util.render;

import com.mojang.blaze3d.textures.GpuTextureView;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.class_11231;
import net.minecraft.class_12137;

public final class GuiTextureCache {
    private static final Map<GpuTextureView, class_11231> TEXTURES = new IdentityHashMap<GpuTextureView, class_11231>();

    private GuiTextureCache() {
    }

    public static void beginFrame() {
        TEXTURES.clear();
    }

    public static class_11231 get(GpuTextureView texture, class_12137 sampler) {
        class_11231 setup = TEXTURES.get(texture);
        if (setup == null || setup.comp_5053() != sampler) {
            setup = class_11231.method_70900((GpuTextureView)texture, (class_12137)sampler);
            TEXTURES.put(texture, setup);
        }
        return setup;
    }
}

