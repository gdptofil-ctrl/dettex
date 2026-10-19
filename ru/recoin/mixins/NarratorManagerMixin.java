/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.class_333
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.recoin.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.class_333;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={class_333.class})
public class NarratorManagerMixin {
    @ModifyReturnValue(method={"method_1791"}, at={@At(value="RETURN")})
    private boolean isActive(boolean original) {
        return false;
    }
}

