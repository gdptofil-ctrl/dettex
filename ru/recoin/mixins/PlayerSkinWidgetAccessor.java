/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_591
 *  net.minecraft.class_8765
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package ru.recoin.mixins;

import net.minecraft.class_591;
import net.minecraft.class_8765;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_8765.class})
public interface PlayerSkinWidgetAccessor {
    @Accessor(value="field_59834")
    public class_591 recoin$getWideModel();

    @Accessor(value="field_59835")
    public class_591 recoin$getSlimModel();

    @Accessor(value="field_46006")
    public float recoin$getYRotation();

    @Accessor(value="field_46006")
    public void recoin$setYRotation(float var1);
}

