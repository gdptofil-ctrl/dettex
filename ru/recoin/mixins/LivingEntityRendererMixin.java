/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.class_10042
 *  net.minecraft.class_10055
 *  net.minecraft.class_1058
 *  net.minecraft.class_11659
 *  net.minecraft.class_11683$class_11792
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1921
 *  net.minecraft.class_2960
 *  net.minecraft.class_3532
 *  net.minecraft.class_3879
 *  net.minecraft.class_4587
 *  net.minecraft.class_583
 *  net.minecraft.class_591
 *  net.minecraft.class_922
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.recoin.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.class_10042;
import net.minecraft.class_10055;
import net.minecraft.class_1058;
import net.minecraft.class_11659;
import net.minecraft.class_11683;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_3879;
import net.minecraft.class_4587;
import net.minecraft.class_583;
import net.minecraft.class_591;
import net.minecraft.class_922;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.recoin.api.accessor.ICustomPlayerModelState;
import ru.recoin.api.event.EventManager;
import ru.recoin.common.QuickImports;
import ru.recoin.implement.events.render.EntityColorEvent;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.render.EntityESP;
import ru.recoin.implement.features.modules.render.ShaderESP;
import ru.recoin.implement.features.modules.render.custommodels.CustomPlayerModelRenderer;

@Mixin(value={class_922.class})
public abstract class LivingEntityRendererMixin
implements QuickImports {
    @Unique
    private class_1309 recoin$rotationEntity;
    @Unique
    private int recoin$bodyYawTick = Integer.MIN_VALUE;
    @Unique
    private float recoin$bodyYaw;
    @Unique
    private float recoin$previousBodyYaw;
    @Unique
    private long recoin$rotationReleaseStarted = -1L;
    @Unique
    private float recoin$lastVisualHeadYaw;
    @Unique
    private float recoin$lastVisualBodyYaw;
    @Unique
    private float recoin$lastVisualPitch;
    @Unique
    private long recoin$lastVisualRotationNanos = -1L;
    @Shadow
    protected class_583<? super class_10042> field_4737;

    @Shadow
    @Nullable
    protected abstract class_1921 method_24302(class_10042 var1, boolean var2, boolean var3, boolean var4);

    @Shadow
    public abstract class_2960 method_3885(class_10042 var1);

    @Inject(method={"method_4055(Lnet/minecraft/class_1309;D)Z"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void recoin$hideVanillaPlayerLabels(class_1309 entity, double squaredDistanceToCamera, CallbackInfoReturnable<Boolean> cir) {
        EntityESP entityESP = EntityESP.getInstance();
        if (entityESP != null && entityESP.rendersNametag(entity)) {
            cir.setReturnValue(false);
        }
    }

    @Redirect(method={"method_4054(Lnet/minecraft/class_10042;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_12075;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_922;method_24302(Lnet/minecraft/class_10042;ZZZ)Lnet/minecraft/class_1921;"), require=0)
    private class_1921 renderHook(class_922 instance, class_10042 state, boolean showBody, boolean translucent, boolean showOutline) {
        if (!translucent && state.field_53333) {
            EntityColorEvent event = new EntityColorEvent(-1, state);
            EventManager.callEvent(event);
            if (event.isCancelled()) {
                translucent = true;
            }
        }
        return this.method_24302(state, showBody, translucent, showOutline);
    }

    @ModifyArg(method={"method_4054(Lnet/minecraft/class_10042;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_12075;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_11659;method_73490(Lnet/minecraft/class_3879;Ljava/lang/Object;Lnet/minecraft/class_4587;Lnet/minecraft/class_1921;IIILnet/minecraft/class_1058;ILnet/minecraft/class_11683$class_11792;)V"), index=6, require=0)
    private int recoin$modifyTintedColor(int color, @Local(argsOnly=true) class_10042 renderState) {
        EntityColorEvent event = new EntityColorEvent(color, renderState);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            return event.getColor();
        }
        return color;
    }

    @WrapOperation(method={"method_4054(Lnet/minecraft/class_10042;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_12075;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_11659;method_73490(Lnet/minecraft/class_3879;Ljava/lang/Object;Lnet/minecraft/class_4587;Lnet/minecraft/class_1921;IIILnet/minecraft/class_1058;ILnet/minecraft/class_11683$class_11792;)V")}, require=0)
    private void recoin$renderCustomPlayerModel(class_11659 queue, class_3879 model, Object submittedState, class_4587 matrices, class_1921 renderLayer, int light, int overlay, int color, class_1058 sprite, int outlineColor, class_11683.class_11792 crumblingOverlay, Operation<Void> original) {
        if (submittedState instanceof class_10055) {
            ICustomPlayerModelState customState;
            class_10055 playerState = (class_10055)submittedState;
            if (submittedState instanceof ICustomPlayerModelState && (customState = (ICustomPlayerModelState)submittedState).recoin$hasCustomModel() && model instanceof class_591) {
                class_591 playerModel = (class_591)model;
                playerModel.method_62110(playerState);
                if (CustomPlayerModelRenderer.render(customState.recoin$getCustomModel(), playerModel, playerState, matrices, queue, light, overlay, color, outlineColor)) {
                    return;
                }
            }
        }
        original.call(new Object[]{queue, model, submittedState, matrices, renderLayer, light, overlay, color, sprite, outlineColor, crumblingOverlay});
    }

    @Inject(method={"method_62355(Lnet/minecraft/class_1309;Lnet/minecraft/class_10042;F)V"}, at={@At(value="TAIL")}, require=0)
    private void recoin$trackEntity(class_1309 entity, class_10042 state, float delta, CallbackInfo ci) {
        this.recoin$applyVisualRotation(entity, state, delta);
        ShaderESP shader = ShaderESP.getInstance();
        if (shader != null && shader.shouldHighlight((class_1297)entity)) {
            state.field_61821 = shader.getOutlineColor();
        }
    }

    @Unique
    private void recoin$applyVisualRotation(class_1309 entity, class_10042 state, float tickDelta) {
        RotationController controller = RotationController.INSTANCE;
        if (entity != LivingEntityRendererMixin.mc.field_1724) {
            return;
        }
        if (entity.method_49694() != null) {
            this.recoin$resetVisualRotation();
            return;
        }
        if (controller.getCurrentAngle() == null) {
            this.recoin$releaseVisualRotation(entity, state);
            return;
        }
        this.recoin$rotationReleaseStarted = -1L;
        float rotationYaw = controller.getRotation().getYaw();
        if (this.recoin$rotationEntity != entity) {
            this.recoin$rotationEntity = entity;
            this.recoin$bodyYaw = entity.field_6283;
            this.recoin$previousBodyYaw = entity.field_6220;
            this.recoin$bodyYawTick = Integer.MIN_VALUE;
            this.recoin$lastVisualHeadYaw = entity.field_6241;
            this.recoin$lastVisualBodyYaw = entity.field_6283;
            this.recoin$lastVisualPitch = entity.method_36455();
            this.recoin$lastVisualRotationNanos = System.nanoTime();
        }
        if (this.recoin$bodyYawTick != entity.field_6012) {
            this.recoin$previousBodyYaw = this.recoin$bodyYaw;
            this.recoin$bodyYaw = this.recoin$computeBodyYaw(entity, rotationYaw);
            this.recoin$bodyYawTick = entity.field_6012;
        }
        float targetHeadYaw = class_3532.method_17821((float)tickDelta, (float)controller.getPreviousRotation().getYaw(), (float)rotationYaw);
        float bodyYaw = class_3532.method_17821((float)tickDelta, (float)this.recoin$previousBodyYaw, (float)this.recoin$bodyYaw);
        float targetPitch = class_3532.method_16439((float)tickDelta, (float)controller.getPreviousRotation().getPitch(), (float)controller.getRotation().getPitch());
        float headYaw = targetHeadYaw;
        float visualPitch = targetPitch;
        state.field_53446 = bodyYaw;
        state.field_53447 = class_3532.method_15393((float)(headYaw - bodyYaw));
        state.field_53448 = visualPitch;
        this.recoin$lastVisualHeadYaw = headYaw;
        this.recoin$lastVisualBodyYaw = bodyYaw;
        this.recoin$lastVisualPitch = state.field_53448;
    }

    @Unique
    private void recoin$releaseVisualRotation(class_1309 entity, class_10042 state) {
        if (this.recoin$rotationEntity != entity) {
            this.recoin$resetVisualRotation();
            return;
        }
        long now = System.nanoTime();
        if (this.recoin$rotationReleaseStarted < 0L) {
            this.recoin$rotationReleaseStarted = now;
        }
        float progress = class_3532.method_15363((float)((float)(now - this.recoin$rotationReleaseStarted) / 1.8E8f), (float)0.0f, (float)1.0f);
        float eased = progress * progress * (3.0f - 2.0f * progress);
        if (progress >= 1.0f) {
            this.recoin$resetVisualRotation();
            return;
        }
        float vanillaBodyYaw = state.field_53446;
        float vanillaHeadYaw = vanillaBodyYaw + state.field_53447;
        float bodyYaw = class_3532.method_17821((float)eased, (float)this.recoin$lastVisualBodyYaw, (float)vanillaBodyYaw);
        float headYaw = class_3532.method_17821((float)eased, (float)this.recoin$lastVisualHeadYaw, (float)vanillaHeadYaw);
        state.field_53446 = bodyYaw;
        state.field_53447 = class_3532.method_15393((float)(headYaw - bodyYaw));
        state.field_53448 = class_3532.method_16439((float)eased, (float)this.recoin$lastVisualPitch, (float)state.field_53448);
    }

    @Unique
    private void recoin$resetVisualRotation() {
        this.recoin$rotationEntity = null;
        this.recoin$bodyYawTick = Integer.MIN_VALUE;
        this.recoin$rotationReleaseStarted = -1L;
        this.recoin$lastVisualRotationNanos = -1L;
    }

    @Unique
    private float recoin$computeBodyYaw(class_1309 entity, float rotationYaw) {
        float deltaBodyYaw;
        double x = entity.method_23317() - entity.field_6014;
        double z = entity.method_23321() - entity.field_5969;
        float offset = this.recoin$bodyYaw;
        if (x * x + z * z > 0.002500000176951289) {
            offset = (float)(class_3532.method_15349((double)z, (double)x) * 57.2957763671875 - 90.0);
        }
        if (entity.field_6251 > 0.0f) {
            offset = rotationYaw;
        }
        return ((deltaBodyYaw = class_3532.method_15363((float)class_3532.method_15393((float)(rotationYaw - (this.recoin$bodyYaw + class_3532.method_15393((float)(offset - this.recoin$bodyYaw)) * 0.3f))), (float)-45.0f, (float)75.0f)) > 50.0f ? deltaBodyYaw * 0.2f : 0.0f) + rotationYaw - deltaBodyYaw;
    }
}

