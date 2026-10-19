/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.class_2561
 *  net.minecraft.class_268
 *  net.minecraft.class_270
 *  net.minecraft.class_5250
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.recoin.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.class_2561;
import net.minecraft.class_268;
import net.minecraft.class_270;
import net.minecraft.class_5250;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.recoin.implement.features.modules.player.NameProtect;

@Mixin(value={class_268.class})
public class TeamMixin {
    @ModifyReturnValue(method={"method_1142(Lnet/minecraft/class_270;Lnet/minecraft/class_2561;)Lnet/minecraft/class_5250;"}, at={@At(value="RETURN")})
    private static class_5250 recoin$protectAnarchy(class_5250 original, class_270 team, class_2561 name) {
        return NameProtect.protectAnarchy((class_2561)original);
    }
}

