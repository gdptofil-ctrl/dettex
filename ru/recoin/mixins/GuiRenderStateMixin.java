/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11245
 *  net.minecraft.class_11246
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_11245;
import net.minecraft.class_11246;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.common.util.render.ItemGuiAlpha;

@Mixin(value={class_11246.class})
public class GuiRenderStateMixin {
    @Inject(method={"method_70920"}, at={@At(value="HEAD")})
    private void recoin$captureItemAlpha(class_11245 state, CallbackInfo ci) {
        ItemGuiAlpha.capture(state);
    }
}

