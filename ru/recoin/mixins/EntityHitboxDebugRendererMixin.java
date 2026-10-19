/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.class_12155
 *  net.minecraft.class_1297
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.recoin.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.class_12155;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;

@Mixin(value={class_12155.class})
public abstract class EntityHitboxDebugRendererMixin {
    @WrapOperation(method={"method_75432"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_1297;method_5828(F)Lnet/minecraft/class_243;")})
    private class_243 recoin$showSilentRotation(class_1297 entity, float tickDelta, Operation<class_243> original) {
        Angle silent;
        class_310 client = class_310.method_1551();
        if (entity == client.field_1724 && (silent = RotationController.INSTANCE.getCurrentAngle()) != null) {
            return silent.toVector();
        }
        return (class_243)original.call(new Object[]{entity, Float.valueOf(tickDelta)});
    }
}

