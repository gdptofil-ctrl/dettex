/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1058
 *  net.minecraft.class_4587
 *  net.minecraft.class_4597
 *  net.minecraft.class_4603
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_1058;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_4603;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.implement.features.modules.render.NoRender;

@Mixin(value={class_4603.class})
public class InGameOverlayRendererMixin {
    @Inject(method={"method_23070"}, at={@At(value="HEAD")}, cancellable=true)
    private static void renderFireOverlayHook(class_4587 matrices, class_4597 vertexConsumers, class_1058 sprite, CallbackInfo ci) {
        NoRender noRender = NoRender.getInstance();
        if (noRender.isState() && noRender.modeSetting.isSelected("Fire")) {
            ci.cancel();
        }
    }

    @Inject(method={"method_23068"}, at={@At(value="HEAD")}, cancellable=true)
    private static void renderInWallOverlayHook(class_1058 sprite, class_4587 matrices, class_4597 vertexConsumers, CallbackInfo ci) {
        NoRender noRender = NoRender.getInstance();
        if (noRender.isState() && (noRender.modeSetting.isSelected("Block Overlay") || noRender.modeSetting.isSelected("Walls"))) {
            ci.cancel();
        }
    }
}

