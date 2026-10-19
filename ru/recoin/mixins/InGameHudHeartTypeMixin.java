/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1291
 *  net.minecraft.class_1294
 *  net.minecraft.class_1657
 *  net.minecraft.class_6880
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package ru.recoin.mixins;

import net.minecraft.class_1291;
import net.minecraft.class_1294;
import net.minecraft.class_1657;
import net.minecraft.class_6880;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.recoin.implement.features.modules.render.NoRender;

@Mixin(targets={"net/minecraft/class_329$class_6411"})
public class InGameHudHeartTypeMixin {
    @Redirect(method={"method_37301"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_1657;method_6059(Lnet/minecraft/class_6880;)Z", ordinal=1))
    private static boolean disableWitherHeartType(class_1657 player, class_6880<class_1291> effect) {
        NoRender noRender = NoRender.getInstance();
        if (noRender.isState() && noRender.modeSetting.isSelected("Wither Hearts") && effect == class_1294.field_5920) {
            return false;
        }
        return player.method_6059(effect);
    }
}

