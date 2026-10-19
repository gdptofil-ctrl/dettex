/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1922
 *  net.minecraft.class_2338
 *  net.minecraft.class_259
 *  net.minecraft.class_265
 *  net.minecraft.class_3726
 *  net.minecraft.class_4970$class_4971
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.recoin.mixins.noclip;

import net.minecraft.class_1922;
import net.minecraft.class_2338;
import net.minecraft.class_259;
import net.minecraft.class_265;
import net.minecraft.class_3726;
import net.minecraft.class_4970;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.recoin.implement.features.modules.movement.noclip.NoClipHiddenBlocks;

@Mixin(value={class_4970.class_4971.class})
public abstract class NoClipBlockStateMixin {
    @Inject(method={"method_26194(Lnet/minecraft/class_1922;Lnet/minecraft/class_2338;Lnet/minecraft/class_3726;)Lnet/minecraft/class_265;"}, at={@At(value="HEAD")}, cancellable=true)
    private void noClipCollision(class_1922 world, class_2338 pos, class_3726 context, CallbackInfoReturnable<class_265> cir) {
        if (NoClipHiddenBlocks.contains(pos)) {
            cir.setReturnValue(class_259.method_1073());
        }
    }

    @Inject(method={"method_26220(Lnet/minecraft/class_1922;Lnet/minecraft/class_2338;)Lnet/minecraft/class_265;"}, at={@At(value="HEAD")}, cancellable=true)
    private void noClipCollisionWithoutContext(class_1922 world, class_2338 pos, CallbackInfoReturnable<class_265> cir) {
        if (NoClipHiddenBlocks.contains(pos)) {
            cir.setReturnValue(class_259.method_1073());
        }
    }
}

