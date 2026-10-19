/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1306
 *  net.minecraft.class_4587
 *  net.minecraft.class_759
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package ru.recoin.mixins;

import net.minecraft.class_1306;
import net.minecraft.class_4587;
import net.minecraft.class_759;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={class_759.class})
public interface HeldItemRendererAccessor {
    @Invoker(value="method_3217")
    public void recoin$applySwingOffset(class_4587 var1, class_1306 var2, float var3);

    @Invoker(value="method_65816")
    public void recoin$swingArm(float var1, class_4587 var2, int var3, class_1306 var4);
}

