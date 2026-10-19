/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_634
 *  net.minecraft.class_638
 *  net.minecraft.class_638$class_5271
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package ru.recoin.mixins;

import net.minecraft.class_634;
import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_634.class})
public interface ClientPlayNetworkHandlerAccessor {
    @Accessor(value="field_3699")
    public void recoin$setWorld(class_638 var1);

    @Accessor(value="field_24321")
    public void recoin$setWorldProperties(class_638.class_5271 var1);
}

