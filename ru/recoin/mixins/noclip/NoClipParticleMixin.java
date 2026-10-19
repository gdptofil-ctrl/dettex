/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2338
 *  net.minecraft.class_2388
 *  net.minecraft.class_2394
 *  net.minecraft.class_702
 *  net.minecraft.class_703
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.recoin.mixins.noclip;

import net.minecraft.class_2338;
import net.minecraft.class_2388;
import net.minecraft.class_2394;
import net.minecraft.class_702;
import net.minecraft.class_703;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.recoin.implement.features.modules.movement.noclip.NoClipHiddenBlocks;

@Mixin(value={class_702.class})
public abstract class NoClipParticleMixin {
    @Inject(method={"method_3056(Lnet/minecraft/class_2394;DDDDDD)Lnet/minecraft/class_703;"}, at={@At(value="HEAD")}, cancellable=true)
    private void hideBreakingParticles(class_2394 effect, double x, double y, double z, double vx, double vy, double vz, CallbackInfoReturnable<class_703> cir) {
        if (effect instanceof class_2388 && NoClipHiddenBlocks.contains(class_2338.method_49637((double)x, (double)y, (double)z))) {
            cir.setReturnValue(null);
        }
    }
}

