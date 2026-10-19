/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_9919
 *  net.minecraft.class_9919$class_10601
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.recoin.mixins;

import net.minecraft.class_310;
import net.minecraft.class_9919;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.recoin.implement.screens.mainmenu.RecoinMainMenuScreen;

@Mixin(value={class_9919.class})
public abstract class InactivityFpsLimiterMixin {
    @Inject(method={"method_61937"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$useConfiguredFpsInMainMenu(CallbackInfoReturnable<Integer> cir) {
        class_310 client = class_310.method_1551();
        class_9919 limiter = (class_9919)(Object)this;
        if (RecoinMainMenuScreen.isRecoinMenu(client.field_1755) && limiter.method_66514() == class_9919.class_10601.field_55847) {
            cir.setReturnValue((Integer)client.field_1690.method_42524().method_41753());
        }
    }
}

