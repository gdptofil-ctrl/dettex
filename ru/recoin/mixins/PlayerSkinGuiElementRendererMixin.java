/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11238
 *  net.minecraft.class_11255
 *  net.minecraft.class_4587
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_11238;
import net.minecraft.class_11255;
import net.minecraft.class_4587;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.implement.screens.mainmenu.PlayerSkinWalkAnimation;

@Mixin(value={class_11238.class})
public abstract class PlayerSkinGuiElementRendererMixin {
    @Inject(method={"method_70912(Lnet/minecraft/class_11255;Lnet/minecraft/class_4587;)V"}, at={@At(value="HEAD")})
    private void recoin$applyAltManagerWalk(class_11255 state, class_4587 matrices, CallbackInfo ci) {
        PlayerSkinWalkAnimation.applyIfRegistered(state.comp_4141());
    }
}

