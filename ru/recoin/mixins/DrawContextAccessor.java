/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11246
 *  net.minecraft.class_332
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package ru.recoin.mixins;

import net.minecraft.class_11246;
import net.minecraft.class_332;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_332.class})
public interface DrawContextAccessor {
    @Accessor(value="field_59826")
    public class_11246 recoin$getState();
}

