/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.class_1313
 *  net.minecraft.class_1702
 *  net.minecraft.class_241
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  net.minecraft.class_638
 *  net.minecraft.class_742
 *  net.minecraft.class_744
 *  net.minecraft.class_746
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.recoin.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.minecraft.class_1313;
import net.minecraft.class_1702;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_638;
import net.minecraft.class_742;
import net.minecraft.class_744;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.recoin.api.event.EventManager;
import ru.recoin.common.util.entity.PlayerInventoryComponent;
import ru.recoin.core.Main;
import ru.recoin.implement.events.block.PushEvent;
import ru.recoin.implement.events.container.CloseScreenEvent;
import ru.recoin.implement.events.item.ItemSlowdownEvent;
import ru.recoin.implement.events.item.UsingItemEvent;
import ru.recoin.implement.events.player.MotionEvent;
import ru.recoin.implement.events.player.MoveEvent;
import ru.recoin.implement.events.player.PlayerTickEndEvent;
import ru.recoin.implement.events.player.PostMotionEvent;
import ru.recoin.implement.events.player.PostTickEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.combat.AutoCrystal;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.misc.ClickAction;
import ru.recoin.implement.features.modules.movement.AutoSprint;
import ru.recoin.implement.features.modules.movement.NoSlow;

@Mixin(value={class_746.class})
public abstract class ClientPlayerEntityMixin
extends class_742 {
    @Unique
    private boolean recoin$motionGround;
    @Unique
    private boolean recoin$overrideMotionGround;
    @Unique
    private float recoin$motionPitch;
    @Unique
    private boolean recoin$overrideMotionPitch;
    @Final
    @Shadow
    protected class_310 field_3937;
    @Shadow
    public class_744 field_3913;

    @Shadow
    public abstract float method_5695(float var1);

    @Shadow
    public abstract float method_5705(float var1);

    @Shadow
    public abstract boolean method_6115();

    @Shadow
    protected abstract void method_3148(float var1, float var2);

    public ClientPlayerEntityMixin(class_638 world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method={"method_5773"}, at={@At(value="HEAD")})
    public void tick(CallbackInfo info) {
        if (this.field_3937.field_1724 != null && this.field_3937.field_1687 != null) {
            EventManager.callEvent(TickEvent.INSTANCE);
        }
    }

    @Inject(method={"method_5773"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_742;method_5773()V", shift=At.Shift.AFTER)}, require=0)
    public void postTick(CallbackInfo callbackInfo) {
        if (this.field_3937.field_1724 != null && this.field_3937.field_1687 != null) {
            EventManager.callEvent(PostTickEvent.INSTANCE);
        }
    }

    @ModifyExpressionValue(method={"method_3136", "method_5773"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_746;method_36454()F")}, require=0)
    private float hookSilentRotationYaw(float original) {
        if (Aura.getInstance().isLegitRotationActive() && !(RotationController.INSTANCE.getLastRotationProvider() instanceof ClickAction) && RotationController.INSTANCE.getLastRotationProvider() != AutoCrystal.getInstance()) {
            return original;
        }
        return RotationController.INSTANCE.getRotation().getYaw();
    }

    @ModifyExpressionValue(method={"method_3136", "method_5773"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_746;method_36455()F")}, require=0)
    private float hookSilentRotationPitch(float original) {
        if (this.recoin$overrideMotionPitch) {
            return this.recoin$motionPitch;
        }
        if (Aura.getInstance().isLegitRotationActive() && !(RotationController.INSTANCE.getLastRotationProvider() instanceof ClickAction) && RotationController.INSTANCE.getLastRotationProvider() != AutoCrystal.getInstance()) {
            return original;
        }
        return RotationController.INSTANCE.getRotation().getPitch();
    }

    @Inject(method={"method_7346"}, at={@At(value="HEAD")}, cancellable=true)
    private void closeHandledScreenHook(CallbackInfo info) {
        CloseScreenEvent event = new CloseScreenEvent(this.field_3937.field_1755);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }

    @ModifyExpressionValue(method={"method_6007"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_746;method_6115()Z")}, require=0)
    private boolean usingItemHook(boolean original) {
        if (original) {
            UsingItemEvent event = new UsingItemEvent((byte)1);
            EventManager.callEvent(event);
            if (event.isCancelled()) {
                return false;
            }
            AutoSprint.getInstance().tickStop = 1;
        }
        return original;
    }

    @Redirect(method={"method_67270"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_241;method_35582(F)Lnet/minecraft/class_241;", ordinal=1), require=0)
    private class_241 noSlowItemSlowdownHook(class_241 movement, float multiplier) {
        ItemSlowdownEvent slowdownEvent = new ItemSlowdownEvent();
        EventManager.callEvent(slowdownEvent);
        if (multiplier == 0.2f && slowdownEvent.isCancelled() && this.method_6115() && !this.method_5765()) {
            return movement;
        }
        UsingItemEvent event = new UsingItemEvent((byte)1);
        EventManager.callEvent(event);
        if (event.isCancelled() && this.method_6115() && !this.method_5765()) {
            return movement;
        }
        return movement.method_35582(multiplier);
    }

    @ModifyReturnValue(method={"method_20303"}, at={@At(value="RETURN")}, require=0)
    private boolean shouldSlowDownHook(boolean original) {
        NoSlow noSlow = NoSlow.getInstance();
        return noSlow.isState() && !noSlow.isSneakInputActive() ? false : original;
    }

    @WrapOperation(method={"method_74047"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_1702;method_7586()I")}, require=0)
    private int canSprintHook(class_1702 instance, Operation<Integer> original) {
        AutoSprint autoSprint = AutoSprint.getInstance();
        return autoSprint.isState() && autoSprint.ignoreHungerSetting.isValue() ? 8 : (Integer)original.call(new Object[]{instance});
    }

    @ModifyExpressionValue(method={"method_3136"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_746;method_24828()Z")})
    private boolean recoin$motionGround(boolean original) {
        return this.recoin$overrideMotionGround ? this.recoin$motionGround : original;
    }

    @Inject(method={"method_3136"}, at={@At(value="HEAD")}, cancellable=true)
    private void preMotion(CallbackInfo ci) {
        MotionEvent event = new MotionEvent(this.method_23317(), this.method_23318(), this.method_23321(), this.method_5705(1.0f), this.method_5695(1.0f), this.method_24828());
        EventManager.callEvent(event);
        this.recoin$motionGround = event.isOnGround();
        this.recoin$overrideMotionGround = !event.isCancelled() && this.recoin$motionGround != this.method_24828();
        this.recoin$motionPitch = event.getPitch();
        boolean bl = this.recoin$overrideMotionPitch = !event.isCancelled() && this.recoin$motionPitch != this.method_5695(1.0f);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method={"method_5784"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_742;method_5784(Lnet/minecraft/class_1313;Lnet/minecraft/class_243;)V")}, cancellable=true, require=0)
    public void onMoveHook(class_1313 movementType, class_243 movement, CallbackInfo ci) {
        MoveEvent event = new MoveEvent(movement);
        EventManager.callEvent(event);
        double oldX = this.method_23317();
        double oldZ = this.method_23321();
        super.method_5784(movementType, event.getMovement());
        this.method_3148((float)(this.method_23317() - oldX), (float)(this.method_23321() - oldZ));
        ci.cancel();
    }

    @Inject(method={"method_3136"}, at={@At(value="RETURN")}, cancellable=true)
    private void postMotion(CallbackInfo ci) {
        PostMotionEvent postMotionEvent = new PostMotionEvent();
        this.recoin$overrideMotionGround = false;
        this.recoin$overrideMotionPitch = false;
        EventManager.callEvent(postMotionEvent);
        PlayerInventoryComponent.postMotion();
        if (postMotionEvent.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method={"method_30673"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    public void pushOutOfBlocks(double x, double z, CallbackInfo ci) {
        PushEvent event = new PushEvent(PushEvent.Type.BLOCK);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method={"method_65949"}, at={@At(value="HEAD")}, cancellable=true)
    private void stopTestSprint(CallbackInfoReturnable<Boolean> cir) {
        if (Main.getInstance().getAttackPerpetrator().getAttackHandler().isFastSprintResetActive()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method={"method_5773"}, at={@At(value="TAIL")})
    private void recoin$playerTickEnd(CallbackInfo ci) {
        if (this.field_3937.field_1724 != null && this.field_3937.field_1687 != null) {
            EventManager.callEvent(new PlayerTickEndEvent());
        }
    }

    @Inject(method={"method_48300"}, at={@At(value="HEAD")}, cancellable=true)
    private void blockTestSprint(CallbackInfoReturnable<Boolean> cir) {
        if (Main.getInstance().getAttackPerpetrator().getAttackHandler().isFastSprintResetActive()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method={"method_65949"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_746;method_6115()Z")}, cancellable=true, require=0)
    public void shouldStopSprintingHook(CallbackInfoReturnable<Boolean> cir) {
        NoSlow noSlow = NoSlow.getInstance();
        if (noSlow.isState() && !noSlow.isSneakInputActive()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method={"method_48300"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_746;method_6115()Z")}, cancellable=true, require=0)
    public void canStartSprintingHook(CallbackInfoReturnable<Boolean> cir) {
        NoSlow noSlow = NoSlow.getInstance();
        if (noSlow.isState() && !noSlow.isSneakInputActive()) {
            cir.setReturnValue(true);
        }
    }
}

