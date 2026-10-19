/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1799
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_1799;
import net.minecraft.class_327;
import net.minecraft.class_332;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.render.ItemGuiAlpha;

@Mixin(value={class_332.class})
public class DrawContextMixin {
    @ModifyArg(method={"method_64860", "method_64861"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_332;method_48196(Lcom/mojang/blaze3d/pipeline/RenderPipeline;IIIII)V"), index=5, require=0)
    private int recoin$fadeItemBars(int color) {
        return ColorUtil.multAlpha(color, ItemGuiAlpha.current());
    }

    @Inject(method={"method_64859"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void recoin$fadeItemCount(class_327 renderer, class_1799 stack, int x, int y, String countOverride, CallbackInfo ci) {
        float alpha = ItemGuiAlpha.current();
        if (alpha >= 0.999f) {
            return;
        }
        if (stack.method_7947() != 1 || countOverride != null) {
            String count = countOverride == null ? String.valueOf(stack.method_7947()) : countOverride;
            class_332 context = (class_332)(Object)this;
            context.method_51433(renderer, count, x + 19 - 2 - renderer.method_1727(count), y + 9, ColorUtil.multAlpha(-1, alpha), true);
        }
        ci.cancel();
    }
}

