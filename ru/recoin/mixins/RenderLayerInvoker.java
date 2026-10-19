/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_12247
 *  net.minecraft.class_1921
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package ru.recoin.mixins;

import net.minecraft.class_12247;
import net.minecraft.class_1921;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={class_1921.class})
public interface RenderLayerInvoker {
    @Invoker(value="method_75940")
    public static class_1921 recoin$of(String name, class_12247 setup) {
        throw new AssertionError();
    }
}

