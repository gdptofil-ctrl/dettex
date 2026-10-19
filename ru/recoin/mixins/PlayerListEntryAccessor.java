/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1934
 *  net.minecraft.class_640
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package ru.recoin.mixins;

import net.minecraft.class_1934;
import net.minecraft.class_640;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_640.class})
public interface PlayerListEntryAccessor {
    @Accessor(value="field_3744")
    public void recoin$setGameMode(class_1934 var1);
}

