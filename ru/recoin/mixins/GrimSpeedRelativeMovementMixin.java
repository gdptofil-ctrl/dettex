/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.implement.features.modules.movement.Speed;

@Mixin(value={class_1297.class})
public abstract class GrimSpeedRelativeMovementMixin {
    @Inject(method={"method_5724(FLnet/minecraft/class_243;)V"}, at={@At(value="TAIL")})
    private void recoin$grim72RelativeMovement(float movementSpeed, class_243 movementInput, CallbackInfo callbackInfo) {
        Speed module;
        if ((Object)this == class_310.method_1551().field_1724 && (module = Speed.getInstance()) != null) {
            module.afterGrim72RelativeMovement();
        }
    }
}

