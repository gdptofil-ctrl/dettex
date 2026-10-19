/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1686
 *  net.minecraft.class_1799
 *  net.minecraft.class_239
 *  net.minecraft.class_243
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_1686;
import net.minecraft.class_1799;
import net.minecraft.class_239;
import net.minecraft.class_243;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.api.event.EventManager;
import ru.recoin.common.QuickImports;
import ru.recoin.implement.events.player.PotionSplashEvent;

@Mixin(value={class_1686.class})
public abstract class PotionEntityMixin
implements QuickImports {
    @Inject(method={"method_7488"}, at={@At(value="HEAD")})
    private void recoin$onPotionCollision(class_239 hitResult, CallbackInfo ci) {
        class_1686 potion = (class_1686)(Object)this;
        if (PotionEntityMixin.mc.field_1724 == null) {
            return;
        }
        class_1799 stack = potion.method_7495().method_7972();
        class_243 impactPos = potion.method_73189();
        EventManager.callEvent(new PotionSplashEvent(potion, stack, impactPos, hitResult));
    }
}

