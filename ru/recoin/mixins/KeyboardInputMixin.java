/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.class_10185
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_3532
 *  net.minecraft.class_743
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.recoin.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.class_10185;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_3532;
import net.minecraft.class_743;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import ru.recoin.api.event.EventManager;
import ru.recoin.common.util.entity.PlayerInventoryComponent;
import ru.recoin.core.Main;
import ru.recoin.implement.events.player.InputEvent;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.combat.ShiftTap;
import ru.recoin.implement.features.modules.combat.killaura.attack.AttackHandler;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationPlan;
import ru.recoin.implement.features.modules.misc.ClickAction;
import ru.recoin.implement.features.modules.movement.AutoSprint;
import ru.recoin.implement.features.modules.movement.ScreenWalk;
import ru.recoin.mixins.InputMixin;

@Mixin(value={class_743.class})
public class KeyboardInputMixin
extends InputMixin {
    @ModifyExpressionValue(method={"method_3129"}, at={@At(value="NEW", target="(ZZZZZZZ)Lnet/minecraft/class_10185;")})
    private class_10185 tickHook(class_10185 original) {
        InputEvent event = new InputEvent(original);
        EventManager.callEvent(event);
        PlayerInventoryComponent.input(event);
        ClickAction clickAction = ClickAction.getInstance();
        if (clickAction != null && clickAction.isSpookyWindChargeMovementSuppressed()) {
            return class_10185.field_54098;
        }
        class_10185 input = this.transformInput(event.getInput());
        if (ShiftTap.getInstance().isTapping()) {
            input = new class_10185(input.comp_3159(), input.comp_3160(), input.comp_3161(), input.comp_3162(), input.comp_3163(), true, input.comp_3165());
        }
        if (Main.getInstance().getAttackPerpetrator().getAttackHandler().isFastSprintResetActive() && (input.comp_3159() != input.comp_3160() || input.comp_3161() != input.comp_3162()) && input.comp_3165()) {
            input = new class_10185(input.comp_3159(), input.comp_3160(), input.comp_3161(), input.comp_3162(), input.comp_3163(), input.comp_3164(), false);
        }
        return input;
    }

    @Unique
    private class_10185 transformInput(class_10185 input) {
        class_1309 auraTarget;
        boolean swapBlocked;
        Aura aura = Aura.getInstance();
        ScreenWalk screenWalk = ScreenWalk.getInstance();
        boolean bl = swapBlocked = screenWalk != null && (screenWalk.isExternalInventorySwapBlocked() || screenWalk.isExternalInventorySwapSyncing() || screenWalk.isInventorySyncBusy());
        if (!PlayerInventoryComponent.canMove || swapBlocked) {
            if (aura != null && aura.isState() && aura.isTargetCorrectionEnabled()) {
                return new class_10185(false, false, false, false, input.comp_3163(), input.comp_3164(), false);
            }
            return input;
        }
        if (aura != null && aura.isLegitRotationActive() && RotationController.INSTANCE.getLastRotationProvider() == aura) {
            return input;
        }
        RotationController rotationController = RotationController.INSTANCE;
        Angle angle = rotationController.getCurrentAngle();
        RotationPlan configurable = rotationController.getCurrentRotationPlan();
        if (KeyboardInputMixin.mc.field_1724 == null) {
            return input;
        }
        class_1309 class_13092 = auraTarget = aura == null ? null : aura.getTarget();
        if (aura != null && aura.isState() && aura.isTargetCorrectionEnabled() && this.isCurrentAuraTarget((class_1297)auraTarget)) {
            return this.transformTargetInput(input, angle != null ? angle : AngleUtil.cameraAngle(), (class_1297)auraTarget);
        }
        if (angle == null || configurable == null || !configurable.isMoveCorrection()) {
            return input;
        }
        float baseYaw = !configurable.isFreeCorrection() && Aura.getInstance().isState() && configurable.getEntity() != null ? this.getFocusedYaw(configurable.getEntity()) : KeyboardInputMixin.mc.field_1724.method_36454();
        return this.transformFocusedInput(input, angle, baseYaw);
    }

    @Unique
    private boolean isCurrentAuraTarget(class_1297 target) {
        Aura aura = Aura.getInstance();
        return target != null && aura.getTarget() == target && target.method_5805() && !target.method_31481();
    }

    @Unique
    private class_10185 transformTargetInput(class_10185 input, Angle angle, class_1297 target) {
        if (target == null) {
            return input;
        }
        boolean sprint = input.comp_3165();
        Aura aura = Aura.getInstance();
        if (AutoSprint.getInstance().isState() && aura.getTarget() != null) {
            AttackHandler attackHandler = Main.getInstance().getAttackPerpetrator().getAttackHandler();
            sprint = AutoSprint.getInstance().tickStop == 0 && !attackHandler.shouldPauseSprint(aura.getConfig()) && !attackHandler.isPacketSprintResetActive() && !attackHandler.isAdaptiveAttackPending();
        }
        return this.transformFocusedInput(new class_10185(true, false, false, false, input.comp_3163(), input.comp_3164(), sprint), angle, this.getFocusedYaw(target));
    }

    @Unique
    private class_10185 transformFocusedInput(class_10185 input, Angle angle, float baseYaw) {
        float forward = class_743.method_40218((boolean)input.comp_3159(), (boolean)input.comp_3160());
        float strafe = class_743.method_40218((boolean)input.comp_3161(), (boolean)input.comp_3162());
        if (forward == 0.0f && strafe == 0.0f) {
            return input;
        }
        double movementAngle = class_3532.method_15338((double)Math.toDegrees(KeyboardInputMixin.a(baseYaw, forward, strafe)));
        float closestForward = 0.0f;
        float closestStrafe = 0.0f;
        float closestDifference = Float.MAX_VALUE;
        float f = -1.0f;
        while (f <= 1.0f) {
            float pf = f;
            float f2 = -1.0f;
            while (f2 <= 1.0f) {
                double predictedAngle;
                float difference;
                float ps = f2;
                if ((pf != 0.0f || ps != 0.0f) && (difference = (float)Math.abs(movementAngle - (predictedAngle = class_3532.method_15338((double)Math.toDegrees(KeyboardInputMixin.a(angle.getYaw(), pf, ps)))))) < closestDifference) {
                    closestDifference = difference;
                    closestForward = pf;
                    closestStrafe = ps;
                }
                f2 = ps + 1.0f;
            }
            f = pf + 1.0f;
        }
        boolean sprint = input.comp_3165();
        Aura aura = Aura.getInstance();
        if (AutoSprint.getInstance().isState() && closestForward > 0.0f) {
            boolean auraPause;
            AttackHandler attackHandler = Main.getInstance().getAttackPerpetrator().getAttackHandler();
            boolean bl = auraPause = aura != null && aura.isState() && aura.getTarget() != null && (attackHandler.shouldPauseSprint(aura.getConfig()) || attackHandler.isPacketSprintResetActive() || attackHandler.isAdaptiveAttackPending());
            if (!auraPause && AutoSprint.getInstance().tickStop == 0) {
                sprint = true;
            }
        }
        return new class_10185(closestForward > 0.0f, closestForward < 0.0f, closestStrafe > 0.0f, closestStrafe < 0.0f, input.comp_3163(), input.comp_3164(), sprint);
    }

    @Unique
    private float getFocusedYaw(class_1297 target) {
        if (target == null) {
            return KeyboardInputMixin.mc.field_1724.method_36454();
        }
        double dx = target.method_23317() - KeyboardInputMixin.mc.field_1724.method_23317();
        double dz = target.method_23321() - KeyboardInputMixin.mc.field_1724.method_23321();
        return class_3532.method_15393((float)((float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0f));
    }

    @Unique
    private static double a(float rotationYaw, double moveForward, double moveStrafing) {
        float f;
        float f4;
        float f2 = f4 = moveForward < 0.0 ? rotationYaw + 180.0f : rotationYaw;
        if (moveStrafing > 0.0) {
            float f3 = moveForward < 0.0 ? -0.5f : (moveForward > 0.0 ? 0.5f : 1.0f);
            f = -90.0f * f3;
        } else if (moveStrafing < 0.0) {
            float f22 = moveForward < 0.0 ? -0.5f : (moveForward > 0.0 ? 0.5f : 1.0f);
            f = 90.0f * f22;
        } else {
            f = 0.0f;
        }
        return Math.toRadians(f4 + f);
    }
}

