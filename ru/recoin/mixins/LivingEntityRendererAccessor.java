/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_3887
 *  net.minecraft.class_922
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package ru.recoin.mixins;

import net.minecraft.class_3887;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={class_922.class})
public interface LivingEntityRendererAccessor {
    @Invoker(value="method_4046")
    public boolean recoin$addFeature(class_3887<?, ?> var1);
}

