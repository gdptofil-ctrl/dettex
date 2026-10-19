/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.class_1297
 *  net.minecraft.class_1937
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  net.minecraft.class_4184
 *  net.minecraft.class_746
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package ru.recoin.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import ru.recoin.api.event.EventManager;
import ru.recoin.implement.events.render.CameraEvent;
import ru.recoin.implement.events.render.CameraPositionEvent;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.misc.ClickAction;
import ru.recoin.implement.features.modules.movement.PitBike;
import ru.recoin.implement.features.modules.movement.Speed;
import ru.recoin.implement.features.modules.movement.pitbike.PitBikeEntity;

@Mixin(value={class_4184.class})
public abstract class CameraMixin {
    @Shadow
    private class_243 field_18712;
    @Shadow
    @Final
    private class_2338.class_2339 field_18713;
    @Shadow
    private float field_18718;
    @Shadow
    private float field_18717;

    @Shadow
    public abstract void method_19325(float var1, float var2);

    @Shadow
    protected abstract void method_19324(float var1, float var2, float var3);

    @Shadow
    protected abstract float method_19318(float var1);

    @Shadow
    protected abstract void method_19322(class_243 var1);

    @Inject(method={"method_19321"}, at={@At(value="TAIL")})
    private void recoin$bikeHandlebarCamera(class_1937 world, class_1297 focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        class_1297 class_12972;
        PitBike module = PitBike.getInstance();
        class_310 client = class_310.method_1551();
        if (thirdPerson || focusedEntity != client.field_1724 || module == null || !module.isHandlebarViewActive() || !((class_12972 = focusedEntity.method_5854()) instanceof PitBikeEntity)) {
            return;
        }
        PitBikeEntity bike = (PitBikeEntity)class_12972;
        float tilt = bike.getWheelieAngle(tickDelta) + bike.getTerrainPitch(tickDelta);
        class_243 eye = new class_243(0.0, 1.64, -0.3).method_31033((float)Math.toRadians(-bike.getLeanAngle(tickDelta))).method_1023(0.0, 0.34, -0.7).method_1037((float)Math.toRadians(tilt)).method_1031(0.0, 0.34, -0.7).method_1031(0.0, (double)(-bike.getSuspension(tickDelta)), 0.0).method_1024((float)Math.toRadians(-bike.method_61415(tickDelta)));
        double speed = bike.method_18798().method_37267();
        double amount = (double)module.getCameraShake() * Math.min(1.0, speed / 0.5) * (bike.method_24828() ? 1.0 : 0.12);
        double phase = (double)((float)bike.field_6012 + tickDelta) * 1.9;
        this.method_19322(bike.method_30950(tickDelta).method_1019(eye).method_1031(Math.sin(phase * 0.83) * 0.009 * amount, Math.sin(phase) * 0.012 * amount, 0.0));
        this.method_19325(this.field_18718 + (float)(Math.sin(phase * 0.7) * 0.22 * amount), class_3532.method_15363((float)(this.field_18717 - tilt + (float)(Math.sin(phase) * 0.3 * amount)), (float)-90.0f, (float)90.0f));
    }

    @Inject(method={"method_19321"}, at={@At(value="HEAD")}, require=0)
    private void recoin$updateLegitRotation(class_1937 world, class_1297 focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        Aura aura = Aura.getInstance();
        if (aura != null) {
            if (RotationController.INSTANCE.getLastRotationProvider() instanceof ClickAction) {
                return;
            }
            aura.updateLegitRotationFrame();
            aura.updateSpookyRotationFrame();
        }
    }

    @ModifyArgs(method={"method_19321"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_4184;method_19325(FF)V"), require=0)
    private void recoin$applyAuraCameraRotation(Args args2) {
        Aura aura = Aura.getInstance();
        class_310 client = class_310.method_1551();
        if (aura == null || client.field_1724 == null) {
            return;
        }
        if (aura.isLegitRotationActive()) {
            args2.set(0, (Object)Float.valueOf(client.field_1724.method_36454()));
            args2.set(1, (Object)Float.valueOf(client.field_1724.method_36455()));
        } else if (aura.isRenderCameraRotationActive()) {
            Angle angle = aura.getRenderCameraRotation(client.method_61966().method_60637(false));
            args2.set(0, (Object)Float.valueOf(angle.getYaw()));
            args2.set(1, (Object)Float.valueOf(angle.getPitch()));
        }
    }

    @Inject(method={"method_19321"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_4184;method_19327(DDD)V", shift=At.Shift.AFTER)}, cancellable=true)
    private void updateHook(class_1937 world, class_1297 focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        class_746 player;
        if (!EventManager.hasSubscribers(CameraEvent.class)) {
            return;
        }
        CameraEvent event = new CameraEvent(false, 4.0f, new Angle(this.field_18718, this.field_18717));
        EventManager.callEvent(event);
        Angle angle = event.getAngle();
        if (event.isCancelled() && angle != null && focusedEntity instanceof class_746 && !(player = (class_746)focusedEntity).method_6113() && thirdPerson) {
            float fixedPitch = inverseView ? -angle.getPitch() : angle.getPitch();
            float fixedYaw = angle.getYaw() - (float)(inverseView ? 180 : 0);
            float distance = event.getDistance();
            this.method_19325(fixedYaw, fixedPitch);
            this.method_19324(event.isCameraClip() ? -distance : -this.method_19318(distance), 0.0f, 0.0f);
            ci.cancel();
        }
    }

    @Inject(method={"method_19322(Lnet/minecraft/class_243;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void posHook(class_243 pos, CallbackInfo ci) {
        if (!EventManager.hasSubscribers(CameraPositionEvent.class)) {
            this.field_18712 = pos;
            this.field_18713.method_10102(pos.field_1352, pos.field_1351, pos.field_1350);
            ci.cancel();
            return;
        }
        CameraPositionEvent event = new CameraPositionEvent(pos);
        EventManager.callEvent(event);
        this.field_18712 = pos = event.getPos();
        this.field_18713.method_10102(pos.field_1352, pos.field_1351, pos.field_1350);
        ci.cancel();
    }

    @ModifyExpressionValue(method={"method_19317"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_1297;method_5751()F")}, require=0)
    private float recoin$useStandingCameraHeightForFunTimeElytra(float original) {
        class_310 client = class_310.method_1551();
        Speed speed = Speed.getInstance();
        return speed != null && speed.isFunTimeElytraMode() && client.field_1724 != null && client.field_1724 == client.method_1560() ? 1.62f : original;
    }
}

