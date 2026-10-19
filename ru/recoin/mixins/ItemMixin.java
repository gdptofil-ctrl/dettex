/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1657
 *  net.minecraft.class_1792
 *  net.minecraft.class_243
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package ru.recoin.mixins;

import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_243;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;

@Mixin(value={class_1792.class})
public class ItemMixin {
    @Redirect(method={"method_7872"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_1657;method_5631(FF)Lnet/minecraft/class_243;"))
    private static class_243 raycastHook(class_1657 player, float pitch, float yaw) {
        return RotationController.INSTANCE.getRotation().toVector();
    }
}

