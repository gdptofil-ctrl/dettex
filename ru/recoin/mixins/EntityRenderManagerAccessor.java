/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1007
 *  net.minecraft.class_742
 *  net.minecraft.class_7920
 *  net.minecraft.class_898
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package ru.recoin.mixins;

import java.util.Map;
import net.minecraft.class_1007;
import net.minecraft.class_742;
import net.minecraft.class_7920;
import net.minecraft.class_898;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_898.class})
public interface EntityRenderManagerAccessor {
    @Accessor(value="field_4687")
    public Map<class_7920, class_1007<class_742>> zenith_getModelRenderers();
}

