/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1735
 *  net.minecraft.class_465
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package ru.recoin.mixins;

import net.minecraft.class_1735;
import net.minecraft.class_465;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={class_465.class})
public interface HandledScreenAccessor {
    @Accessor(value="field_2776")
    public int recoin$getX();

    @Accessor(value="field_2800")
    public int recoin$getY();

    @Accessor(value="field_2792")
    public int recoin$getBackgroundWidth();

    @Accessor(value="field_2779")
    public int recoin$getBackgroundHeight();

    @Invoker(value="method_64240")
    @Nullable
    public class_1735 recoin$getSlotAt(double var1, double var3);
}

