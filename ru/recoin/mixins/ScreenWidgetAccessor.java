/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_364
 *  net.minecraft.class_4068
 *  net.minecraft.class_437
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package ru.recoin.mixins;

import net.minecraft.class_364;
import net.minecraft.class_4068;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={class_437.class})
public interface ScreenWidgetAccessor {
    @Invoker(value="method_37063")
    public <T extends class_364 & class_4068> T recoin$addDrawableChild(T var1);
}

