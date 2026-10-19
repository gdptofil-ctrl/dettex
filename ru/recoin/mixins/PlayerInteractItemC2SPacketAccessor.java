/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2886
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Mutable
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package ru.recoin.mixins;

import net.minecraft.class_2886;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_2886.class})
public interface PlayerInteractItemC2SPacketAccessor {
    @Mutable
    @Accessor(value="field_51930")
    public void recoin$setYaw(float var1);

    @Mutable
    @Accessor(value="field_51931")
    public void recoin$setPitch(float var1);
}

