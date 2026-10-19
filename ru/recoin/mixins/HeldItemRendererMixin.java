/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.class_10444
 *  net.minecraft.class_11659
 *  net.minecraft.class_1268
 *  net.minecraft.class_1306
 *  net.minecraft.class_1799
 *  net.minecraft.class_310
 *  net.minecraft.class_4587
 *  net.minecraft.class_742
 *  net.minecraft.class_746
 *  net.minecraft.class_759
 *  net.minecraft.class_811
 *  org.joml.Quaternionfc
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.class_10444;
import net.minecraft.class_11659;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_742;
import net.minecraft.class_746;
import net.minecraft.class_759;
import net.minecraft.class_811;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.api.event.EventManager;
import ru.recoin.implement.events.item.HandAnimationEvent;
import ru.recoin.implement.events.item.HandOffsetEvent;
import ru.recoin.implement.features.modules.movement.PitBike;
import ru.recoin.implement.features.modules.render.HandsShader;
import ru.recoin.implement.features.modules.render.SwingAnimations;
import ru.recoin.implement.features.modules.render.ViewModel;

@Mixin(value={class_759.class})
public abstract class HeldItemRendererMixin {
    @WrapOperation(method={"method_3233(Lnet/minecraft/class_1309;Lnet/minecraft/class_1799;Lnet/minecraft/class_811;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;I)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_10444;method_65604(Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;III)V")})
    private void recoin$captureItemBounds(class_10444 state, class_4587 matrices, class_11659 queue, int light, int overlay, int outline, Operation<Void> original, @Local(argsOnly=true) class_811 displayContext) {
        ViewModel viewModel = ViewModel.getInstance();
        class_310 client = class_310.method_1551();
        if (viewModel != null && viewModel.isState() && client.field_1724 != null && (displayContext == class_811.field_4322 || displayContext == class_811.field_4321)) {
            boolean right = displayContext == class_811.field_4322;
            class_1268 hand = right == (client.field_1724.method_6068() == class_1306.field_6183) ? class_1268.field_5808 : class_1268.field_5810;
            viewModel.getHandGeometry().capture(hand, matrices, state.method_72173());
        }
        original.call(new Object[]{state, matrices, queue, light, overlay, outline});
    }

    @Inject(method={"method_22976(FLnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_746;I)V"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void recoin$renderGlassHandsPre(float tickProgress, class_4587 matrices, class_11659 queue, class_746 player, int light, CallbackInfo ci) {
        PitBike bike = PitBike.getInstance();
        if (bike != null && bike.isHandlebarViewActive()) {
            ci.cancel();
            return;
        }
        HandsShader handsShader = HandsShader.getInstance();
        if (handsShader != null && handsShader.isState()) {
            handsShader.renderGlassHandsPre();
        }
    }

    @WrapOperation(method={"method_22976(FLnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_746;I)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_4587;method_22907(Lorg/joml/Quaternionfc;)V", ordinal=0)}, require=0)
    private void recoin$cancelCameraPitchSway(class_4587 matrices, Quaternionfc quaternion, Operation<Void> original) {
    }

    @WrapOperation(method={"method_22976(FLnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_746;I)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_4587;method_22907(Lorg/joml/Quaternionfc;)V", ordinal=1)}, require=0)
    private void recoin$cancelCameraYawSway(class_4587 matrices, Quaternionfc quaternion, Operation<Void> original) {
    }

    @Inject(method={"method_3228"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_4587;method_22903()V", shift=At.Shift.AFTER)}, require=0)
    private void renderFirstPersonItemHook(class_742 player, float tickProgress, float pitch, class_1268 hand, float swingProgress, class_1799 item, float equipProgress, class_4587 matrices, class_11659 queue, int light, CallbackInfo ci) {
        EventManager.callEvent(new HandOffsetEvent(matrices, item, hand));
    }

    @ModifyExpressionValue(method={"method_3220"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_746;method_75194(F)F")}, require=0)
    private float recoin$removeCooldownBob(float original) {
        SwingAnimations animations = SwingAnimations.getInstance();
        return animations != null && animations.isState() ? 1.0f : original;
    }

    @ModifyReturnValue(method={"method_65910"}, at={@At(value="RETURN")}, require=0)
    private boolean recoin$skipSpuriousEquipAnim(boolean original, @Local(ordinal=0, argsOnly=true) class_1799 from, @Local(ordinal=1, argsOnly=true) class_1799 to) {
        SwingAnimations animations = SwingAnimations.getInstance();
        if (!original && animations != null && animations.isState() && from != null && to != null && from.method_7909() == to.method_7909()) {
            return true;
        }
        return original;
    }

    @Unique
    private boolean recoin$applyCustomSwing(class_4587 matrices, class_1268 hand, float swingProgress, int armSign, float equipProgress) {
        SwingAnimations animations = SwingAnimations.getInstance();
        if (animations == null || !animations.isState()) {
            return false;
        }
        HandAnimationEvent event = new HandAnimationEvent(matrices, hand, swingProgress, armSign, equipProgress);
        EventManager.callEvent(event);
        return event.isCancelled();
    }

    @WrapOperation(method={"method_3228"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_759;method_65816(FLnet/minecraft/class_4587;ILnet/minecraft/class_1306;)V")})
    private void wrapSwingArm(class_759 renderer, float swingProgress, class_4587 matrices, int armSign, class_1306 arm, Operation<Void> original, @Local(ordinal=0, argsOnly=true) class_1268 hand, @Local(ordinal=3, argsOnly=true) float equipProgress) {
        if (!this.recoin$applyCustomSwing(matrices, hand, swingProgress, armSign, equipProgress)) {
            original.call(new Object[]{renderer, Float.valueOf(swingProgress), matrices, armSign, arm});
        }
    }

    @WrapOperation(method={"method_3228"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_12152;method_75391(FLnet/minecraft/class_4587;ILnet/minecraft/class_1306;)V")}, require=0)
    private void wrapLancingSwing(float swingProgress, class_4587 matrices, int armSign, class_1306 arm, Operation<Void> original, @Local(ordinal=0, argsOnly=true) class_1268 hand, @Local(ordinal=3, argsOnly=true) float equipProgress) {
        if (!this.recoin$applyCustomSwing(matrices, hand, swingProgress, armSign, equipProgress)) {
            original.call(new Object[]{Float.valueOf(swingProgress), matrices, armSign, arm});
        }
    }
}

