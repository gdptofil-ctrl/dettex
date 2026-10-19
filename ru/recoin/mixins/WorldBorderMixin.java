/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_238
 *  net.minecraft.class_2784
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.recoin.mixins;

import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_2784;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.recoin.implement.features.modules.player.NoPush;

@Mixin(value={class_2784.class})
public class WorldBorderMixin {
    @Inject(method={"method_39459"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$ignoreWorldBorderCollision(class_1297 entity, class_238 box, CallbackInfoReturnable<Boolean> cir) {
        NoPush noPush = NoPush.getInstance();
        if (noPush != null && noPush.shouldIgnoreBorder()) {
            cir.setReturnValue(false);
        }
    }
}

