/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.class_1309
 *  net.minecraft.class_1671
 *  net.minecraft.class_243
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.recoin.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.class_1309;
import net.minecraft.class_1671;
import net.minecraft.class_243;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import ru.recoin.api.event.EventManager;
import ru.recoin.common.QuickImports;
import ru.recoin.implement.events.player.FireworkEvent;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;

@Mixin(value={class_1671.class})
public class FireworkRocketEntityMixin
implements QuickImports {
    @Shadow
    @Nullable
    private class_1309 field_7616;

    @WrapOperation(method={"method_5773"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_1309;method_5720()Lnet/minecraft/class_243;")})
    public class_243 getRotationVectorHook(class_1309 instance, Operation<class_243> original) {
        if (this.field_7616 == FireworkRocketEntityMixin.mc.field_1724) {
            return RotationController.INSTANCE.getMoveRotation().toVector();
        }
        return (class_243)original.call(new Object[]{instance});
    }

    @WrapOperation(method={"method_5773"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_1309;method_18798()Lnet/minecraft/class_243;", ordinal=0)})
    public class_243 getVelocityHook(class_1309 instance, Operation<class_243> original) {
        if (this.field_7616 == FireworkRocketEntityMixin.mc.field_1724) {
            FireworkEvent event = new FireworkEvent((class_243)original.call(new Object[]{instance}));
            EventManager.callEvent(event);
            return event.getVector();
        }
        return (class_243)original.call(new Object[]{instance});
    }
}

