/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_243
 *  net.minecraft.class_3414
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.recoin.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_3414;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.recoin.api.event.EventManager;
import ru.recoin.api.system.sound.SoundManager;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.entity.PlayerInventoryComponent;
import ru.recoin.implement.events.block.PushEvent;
import ru.recoin.implement.events.player.KeepSprintEvent;
import ru.recoin.implement.events.player.SwimmingEvent;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationPlan;

@Mixin(value={class_1657.class})
public abstract class PlayerEntityMixin
implements QuickImports {
    @Inject(method={"method_76457"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$replaceAttackSound(class_3414 sound, CallbackInfo ci) {
        if ((Object)this == PlayerEntityMixin.mc.field_1724 && SoundManager.shouldReplaceCustomHitSound()) {
            ci.cancel();
        }
    }

    @Inject(method={"method_5675"}, at={@At(value="HEAD")}, cancellable=true)
    public void isPushedByFluids(CallbackInfoReturnable<Boolean> cir) {
        PushEvent event = new PushEvent(PushEvent.Type.WATER);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method={"method_7324(Lnet/minecraft/class_1297;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_1297;method_5728(Z)V", shift=At.Shift.AFTER)}, require=0)
    public void attackHook(class_1297 target, CallbackInfo callbackInfo) {
        EventManager.callEvent(new KeepSprintEvent());
    }

    @ModifyExpressionValue(method={"method_7324(Lnet/minecraft/class_1297;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_1297;method_36454()F")}, require=0)
    private float hookFixRotation(float original) {
        return RotationController.INSTANCE.getMoveRotation().getYaw();
    }

    @ModifyExpressionValue(method={"method_6091"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_1657;method_5720()Lnet/minecraft/class_243;")}, require=0)
    public class_243 travelHook(class_243 vec3d) {
        if ((Object)this == PlayerEntityMixin.mc.field_1724) {
            RotationPlan plan = RotationController.INSTANCE.getCurrentRotationPlan();
            if (PlayerInventoryComponent.canMove && plan != null && plan.isMoveCorrection()) {
                vec3d = RotationController.INSTANCE.getMoveRotation().toVector();
            }
        }
        SwimmingEvent event = new SwimmingEvent(vec3d);
        EventManager.callEvent(event);
        return event.getVector();
    }
}

