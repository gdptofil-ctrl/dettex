/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11228
 *  net.minecraft.class_11245
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_11228;
import net.minecraft.class_11245;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.common.util.render.ItemGuiAlpha;

@Mixin(value={class_11228.class})
public class GuiRendererMixin {
    @Unique
    private class_11245 recoin$currentItem;

    @Inject(method={"method_70887"}, at={@At(value="HEAD")})
    private void recoin$beginItemAlpha(class_11245 state, float u, float v, int pixelsPerItem, int itemAtlasSideLength, CallbackInfo ci) {
        this.recoin$currentItem = state;
    }

    @ModifyArg(method={"method_70887"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_11241;<init>(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/class_11231;Lorg/joml/Matrix3x2f;IIIIFFFFILnet/minecraft/class_8030;Lnet/minecraft/class_8030;)V"), index=11)
    private int recoin$applyItemAlpha(int color) {
        int channel = Math.round(255.0f * ItemGuiAlpha.get(this.recoin$currentItem));
        return channel << 24 | channel << 16 | channel << 8 | channel;
    }

    @Inject(method={"method_70887"}, at={@At(value="RETURN")})
    private void recoin$endItemAlpha(class_11245 state, float u, float v, int pixelsPerItem, int itemAtlasSideLength, CallbackInfo ci) {
        this.recoin$currentItem = null;
    }
}

