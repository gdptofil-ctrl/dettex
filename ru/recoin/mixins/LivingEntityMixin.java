/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.class_1291
 *  net.minecraft.class_1292
 *  net.minecraft.class_1293
 *  net.minecraft.class_1294
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  net.minecraft.class_6880
 *  net.minecraft.class_746
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.recoin.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.class_1291;
import net.minecraft.class_1292;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_6880;
import net.minecraft.class_746;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.recoin.api.event.EventManager;
import ru.recoin.implement.events.block.PushEvent;
import ru.recoin.implement.events.item.SwingDurationEvent;
import ru.recoin.implement.events.player.GlidingVelocityEvent;
import ru.recoin.implement.events.player.JumpEvent;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.render.NoRender;
import ru.recoin.implement.features.modules.render.SwingAnimations;

@Mixin(value={class_1309.class})
public abstract class LivingEntityMixin {
    @Shadow
    public float field_6283;
    @Unique
    private final class_310 client = class_310.method_1551();

    @Shadow
    public abstract boolean method_6059(class_6880<class_1291> var1);

    @Shadow
    @Nullable
    public abstract class_1293 method_6112(class_6880<class_1291> var1);

    @Inject(method={"method_5697"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$cancelFakePlayerPush(class_1297 entity, CallbackInfo ci) {
        if (entity != null && entity.method_5628() == 1337228) {
            ci.cancel();
        } else if (((class_1297)(Object)this).method_5628() == 1337228) {
            ci.cancel();
        }
    }

    @Inject(method={"method_5810"}, at={@At(value="HEAD")}, cancellable=true)
    public void isPushable(CallbackInfoReturnable<Boolean> infoReturnable) {
        if (((class_1297)(Object)this).method_5628() == 1337228) {
            infoReturnable.setReturnValue(false);
            return;
        }
        if (!EventManager.hasSubscribers(PushEvent.class)) {
            return;
        }
        PushEvent event = new PushEvent(PushEvent.Type.COLLISION);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            infoReturnable.setReturnValue(false);
        }
    }

    @Inject(method={"method_6059"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$disableDarknessEffect(class_6880<class_1291> effect, CallbackInfoReturnable<Boolean> cir) {
        NoRender noRender = NoRender.getInstance();
        if (noRender.isState() && noRender.modeSetting.isSelected("Warden Darkness") && effect.method_55838(class_1294.field_38092) && (Object)this == this.client.field_1724) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method={"method_6112"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$hideDarknessStatus(class_6880<class_1291> effect, CallbackInfoReturnable<class_1293> cir) {
        NoRender noRender = NoRender.getInstance();
        if (noRender.isState() && noRender.modeSetting.isSelected("Warden Darkness") && effect.method_55838(class_1294.field_38092) && (Object)this == this.client.field_1724) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method={"method_6043"}, at={@At(value="HEAD")}, cancellable=true)
    private void jump(CallbackInfo info) {
        LivingEntityMixin livingEntityMixin = this;
        if ((Object)livingEntityMixin instanceof class_746) {
            class_746 player = (class_746)(Object)livingEntityMixin;
            JumpEvent event = new JumpEvent((class_1657)player);
            EventManager.callEvent(event);
            if (event.isCancelled()) {
                info.cancel();
            }
        }
    }

    @ModifyExpressionValue(method={"method_6043"}, at={@At(value="NEW", target="(DDD)Lnet/minecraft/class_243;")})
    private class_243 hookFixRotation(class_243 original) {
        if ((Object)this != this.client.field_1724) {
            return original;
        }
        float yaw = RotationController.INSTANCE.getMoveRotation().getYaw() * ((float)Math.PI / 180);
        return new class_243((double)(-class_3532.method_15374((double)yaw) * 0.2f), 0.0, (double)(class_3532.method_15362((double)yaw) * 0.2f));
    }

    @ModifyExpressionValue(method={"method_61430"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_1309;method_36455()F")})
    private float hookModifyFallFlyingPitch(float original) {
        if ((Object)this != this.client.field_1724) {
            return original;
        }
        return RotationController.INSTANCE.getMoveRotation().getPitch();
    }

    @ModifyExpressionValue(method={"method_61430"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_1309;method_5720()Lnet/minecraft/class_243;")})
    private class_243 hookModifyFallFlyingRotationVector(class_243 original) {
        if ((Object)this != this.client.field_1724) {
            return original;
        }
        return RotationController.INSTANCE.getMoveRotation().toVector();
    }

    @Inject(method={"method_61430"}, at={@At(value="RETURN")}, cancellable=true)
    private void recoin$grimGlideVelocity(CallbackInfoReturnable<class_243> cir) {
        if ((Object)this != this.client.field_1724) {
            return;
        }
        GlidingVelocityEvent event = new GlidingVelocityEvent((class_243)cir.getReturnValue());
        EventManager.callEvent(event);
        cir.setReturnValue(event.getVelocity());
    }

    @Inject(method={"method_6028"}, at={@At(value="HEAD")}, cancellable=true)
    private void swingProgressHook(CallbackInfoReturnable<Integer> cir) {
        if ((Object)this != this.client.field_1724) {
            return;
        }
        SwingAnimations animations = SwingAnimations.getInstance();
        if (animations == null || !animations.isState()) {
            return;
        }
        SwingDurationEvent event = new SwingDurationEvent();
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            float animation = event.getAnimation();
            animation = class_1292.method_5576((class_1309)this.client.field_1724) ? (animation *= (float)(6 - (1 + class_1292.method_5575((class_1309)this.client.field_1724)))) : (animation *= (float)(this.method_6059((class_6880<class_1291>)class_1294.field_5901) ? 6 + (1 + this.method_6112((class_6880<class_1291>)class_1294.field_5901).method_5578()) * 2 : 6));
            cir.setReturnValue(((int)animation));
        }
    }

    @ModifyExpressionValue(method={"method_6031"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_3532;method_15393(F)F", ordinal=1)})
    private float wrapDegreesHook(float original) {
        if ((Object)this == this.client.field_1724) {
            return class_3532.method_15393((float)(RotationController.INSTANCE.getRotation().getYaw() - this.field_6283));
        }
        return original;
    }
}

