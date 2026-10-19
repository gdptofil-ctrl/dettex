/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10774
 *  net.minecraft.class_1297
 *  net.minecraft.class_1937
 *  net.minecraft.class_2338
 *  net.minecraft.class_2560
 *  net.minecraft.class_2680
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_10774;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2560;
import net.minecraft.class_2680;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.common.QuickImports;
import ru.recoin.implement.features.modules.movement.NoWeb;

@Mixin(value={class_2560.class})
public abstract class CobwebBlockMixin
implements QuickImports {
    @Inject(method={"method_9548"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$reallyworldGrimBypass(class_2680 state, class_1937 world, class_2338 pos, class_1297 entity, class_10774 collisionHandler, boolean moved, CallbackInfo ci) {
        if (entity != CobwebBlockMixin.mc.field_1724) {
            return;
        }
        NoWeb noWeb = NoWeb.getInstance();
        if (noWeb != null && noWeb.shouldBypassCobwebCollision()) {
            ci.cancel();
            noWeb.onCobwebCollision(pos);
        }
    }
}

