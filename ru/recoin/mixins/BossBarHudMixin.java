/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_337
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_337;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.implement.features.modules.render.Hud;
import ru.recoin.implement.features.modules.render.NoRender;

@Mixin(value={class_337.class})
public class BossBarHudMixin {
    @Inject(method={"method_1796"}, at={@At(value="HEAD")}, cancellable=true)
    private void render(CallbackInfo ci) {
        NoRender noRender = NoRender.getInstance();
        if (Hud.getInstance().isState() && Hud.getInstance().interfaceSettings.isSelected("Boss Bars") || noRender.isState() && noRender.modeSetting.isSelected("Boss Bar")) {
            ci.cancel();
        }
    }
}

