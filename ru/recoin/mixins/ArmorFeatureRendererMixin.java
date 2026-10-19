/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10034
 *  net.minecraft.class_11659
 *  net.minecraft.class_4587
 *  net.minecraft.class_970
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_10034;
import net.minecraft.class_11659;
import net.minecraft.class_4587;
import net.minecraft.class_970;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.api.accessor.ICustomPlayerModelState;

@Mixin(value={class_970.class})
public class ArmorFeatureRendererMixin {
    @Inject(method={"method_17157(Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;ILnet/minecraft/class_10034;FF)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$hideArmorForCustomModels(class_4587 matrices, class_11659 queue, int light, class_10034 state, float limbAngle, float limbDistance, CallbackInfo ci) {
        ICustomPlayerModelState customState;
        if (state instanceof ICustomPlayerModelState && (customState = (ICustomPlayerModelState)state).recoin$hasCustomModel()) {
            ci.cancel();
        }
    }
}

