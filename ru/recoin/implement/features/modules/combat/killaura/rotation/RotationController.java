/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_243
 *  net.minecraft.class_2596
 *  net.minecraft.class_2708
 *  net.minecraft.class_2828
 *  net.minecraft.class_2886
 *  net.minecraft.class_3532
 */
package ru.recoin.implement.features.modules.combat.killaura.rotation;

import java.util.Objects;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2708;
import net.minecraft.class_2828;
import net.minecraft.class_2886;
import net.minecraft.class_3532;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.event.EventManager;
import ru.recoin.api.feature.module.Module;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.task.TaskPriority;
import ru.recoin.common.util.task.TaskProcessor;
import ru.recoin.core.Main;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.GlobalTickEvent;
import ru.recoin.implement.events.player.PlayerVelocityStrafeEvent;
import ru.recoin.implement.events.player.RotationUpdateEvent;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationConfig;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationPlan;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.ApexTimeSmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.NewSlothSmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.ReallyWorldSmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.SpookyTimeOldSmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.SpookyTimeSmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.shard.Shard21SmoothMode;
import ru.recoin.implement.features.modules.misc.ClickAction;
import ru.recoin.implement.features.modules.misc.clickaction.SpookyWindCharge;
import ru.recoin.implement.features.modules.movement.ScreenWalk;
import ru.recoin.mixins.PlayerInteractItemC2SPacketAccessor;

public class RotationController
implements QuickImports {
    public static RotationController INSTANCE = new RotationController();
    private static Angle forcedInteractAngle;
    private RotationPlan lastRotationPlan;
    private final TaskProcessor<RotationPlan> rotationPlanTaskProcessor = new TaskProcessor();
    private Angle currentAngle;
    private Angle previousAngle;
    private Angle serverAngle = Angle.DEFAULT;
    private Module lastRotationProvider;
    private Angle auraCameraAngle;
    private Angle previousAuraCameraAngle;
    private int resetTicks;

    public static void setForcedInteractAngle(Angle angle) {
        forcedInteractAngle = angle;
    }

    public static Angle getForcedInteractAngle() {
        return forcedInteractAngle;
    }

    public RotationController() {
        Main.getInstance().getEventManager().register(this);
    }

    public void setRotation(Angle value) {
        this.previousAngle = value == null ? (this.currentAngle != null ? this.currentAngle : AngleUtil.cameraAngle()) : this.currentAngle;
        this.currentAngle = value;
        if (value != null && this.lastRotationProvider instanceof Aura) {
            this.previousAuraCameraAngle = this.auraCameraAngle != null ? this.auraCameraAngle : AngleUtil.cameraAngle();
            this.auraCameraAngle = value;
        }
    }

    public Angle getRotation() {
        return this.currentAngle != null ? this.currentAngle : AngleUtil.cameraAngle();
    }

    public boolean hasActiveRotation() {
        return this.currentAngle != null;
    }

    public Angle getPreviousRotation() {
        if (this.currentAngle != null && this.previousAngle != null) {
            return this.previousAngle;
        }
        if (RotationController.mc.field_1724 == null) {
            return Angle.DEFAULT;
        }
        return new Angle(RotationController.mc.field_1724.field_5982, RotationController.mc.field_1724.field_6004);
    }

    public Angle getMoveRotation() {
        RotationPlan rotationPlan = this.getCurrentRotationPlan();
        return this.currentAngle != null && rotationPlan != null && rotationPlan.isMoveCorrection() ? this.currentAngle : AngleUtil.cameraAngle();
    }

    public RotationPlan getCurrentRotationPlan() {
        return this.rotationPlanTaskProcessor.fetchActiveTaskValue() != null ? this.rotationPlanTaskProcessor.fetchActiveTaskValue() : this.lastRotationPlan;
    }

    public void rotateTo(Angle.VecRotation vecRotation, class_1309 entity, int reset, RotationConfig configurable, TaskPriority taskPriority, Module provider) {
        this.rotateTo(configurable.createRotationPlan(vecRotation.getAngle(), vecRotation.getVec(), (class_1297)entity, reset), taskPriority, provider);
    }

    public void rotateTo(Angle angle, int reset, RotationConfig configurable, TaskPriority taskPriority, Module provider) {
        this.rotateTo(configurable.createRotationPlan(angle, angle.toVector(), null, reset), taskPriority, provider);
    }

    public void rotateTo(Angle angle, RotationConfig configurable, TaskPriority taskPriority, Module provider) {
        this.rotateTo(configurable.createRotationPlan(angle, angle.toVector(), null, 1), taskPriority, provider);
    }

    public void rotateTo(RotationPlan plan, TaskPriority taskPriority, Module provider) {
        this.rotationPlanTaskProcessor.addTask(new TaskProcessor.Task<RotationPlan>(1, taskPriority.getPriority(), provider, plan));
        Module activeProvider = this.rotationPlanTaskProcessor.fetchActiveTaskProvider();
        if (activeProvider != null) {
            this.lastRotationProvider = activeProvider;
        }
    }

    public void update() {
        boolean resetting;
        RotationPlan activePlan;
        RotationPlan activeTaskPlan = this.rotationPlanTaskProcessor.fetchActiveTaskValue();
        Module activeTaskProvider = this.rotationPlanTaskProcessor.fetchActiveTaskProvider();
        RotationPlan rotationPlan = activePlan = activeTaskPlan != null ? activeTaskPlan : this.lastRotationPlan;
        if (activePlan == null) {
            return;
        }
        Angle clientAngle = AngleUtil.cameraAngle();
        boolean bl = resetting = activeTaskPlan == null;
        if (!resetting) {
            this.resetTicks = 0;
            this.lastRotationProvider = activeTaskProvider;
        } else {
            ++this.resetTicks;
        }
        if (resetting && this.lastRotationPlan != null) {
            double differenceFromCurrentToPlayer = RotationController.computeRotationDifference(this.serverAngle, clientAngle);
            if (activePlan.getTicksUntilReset() <= this.resetTicks && differenceFromCurrentToPlayer < (double)activePlan.getResetThreshold()) {
                this.setRotation(null);
                this.lastRotationPlan = null;
                this.lastRotationProvider = null;
                this.resetTicks = 0;
                return;
            }
        }
        Angle nextAngle = activePlan.nextRotation(this.currentAngle != null ? this.currentAngle : clientAngle, resetting);
        Angle newAngle = activePlan.getAngleSmooth() instanceof SpookyWindCharge || activePlan.getAngleSmooth() instanceof SpookyTimeSmoothMode || activePlan.getAngleSmooth() instanceof ApexTimeSmoothMode || activePlan.getAngleSmooth() instanceof SpookyTimeOldSmoothMode || activePlan.getAngleSmooth() instanceof ReallyWorldSmoothMode || activePlan.getAngleSmooth() instanceof Shard21SmoothMode || activePlan.getAngleSmooth() instanceof NewSlothSmoothMode ? nextAngle : nextAngle.adjustSensitivity();
        this.setRotation(newAngle);
        this.applyCameraRotation(newAngle);
        this.lastRotationPlan = activePlan;
    }

    public static double computeRotationDifference(Angle a, Angle b) {
        return Math.hypot(Math.abs(RotationController.computeAngleDifference(a.getYaw(), b.getYaw())), Math.abs(a.getPitch() - b.getPitch()));
    }

    public static float computeAngleDifference(float a, float b) {
        return class_3532.method_15393((float)(a - b));
    }

    private class_243 fixVelocity(class_243 currVelocity, class_243 movementInput, float speed) {
        if (this.currentAngle != null) {
            float yaw = this.currentAngle.getYaw();
            double d = movementInput.method_1027();
            if (d < 1.0E-7) {
                return class_243.field_1353;
            }
            class_243 vec3d = (d > 1.0 ? movementInput.method_1029() : movementInput).method_1021((double)speed);
            float f = class_3532.method_15374((double)(yaw * ((float)Math.PI / 180)));
            float g = class_3532.method_15362((double)(yaw * ((float)Math.PI / 180)));
            return new class_243(vec3d.method_10216() * (double)g - vec3d.method_10215() * (double)f, vec3d.method_10214(), vec3d.method_10215() * (double)g + vec3d.method_10216() * (double)f);
        }
        return currVelocity;
    }

    public void clear() {
        this.rotationPlanTaskProcessor.activeTasks.clear();
        this.resetTicks = 0;
        this.lastRotationProvider = null;
    }

    public void release(Module provider) {
        this.release(provider, false);
    }

    public void release(Module provider, boolean preserveRotation) {
        RotationPlan nextPlan;
        this.rotationPlanTaskProcessor.removeProvider(provider);
        if (this.lastRotationProvider != provider) {
            return;
        }
        if (!preserveRotation && provider instanceof ClickAction) {
            this.previousAngle = this.currentAngle = this.auraCameraAngle != null && Aura.getInstance().isRenderCameraRotationActive() ? this.auraCameraAngle : AngleUtil.cameraAngle();
        }
        if ((nextPlan = this.rotationPlanTaskProcessor.fetchActiveTaskValue()) != null) {
            this.lastRotationPlan = nextPlan;
            this.lastRotationProvider = this.rotationPlanTaskProcessor.fetchActiveTaskProvider();
        } else {
            this.lastRotationPlan = null;
            this.lastRotationProvider = null;
            if (!preserveRotation) {
                this.setRotation(null);
            }
        }
        this.resetTicks = 0;
    }

    public void resetImmediately() {
        this.rotationPlanTaskProcessor.activeTasks.clear();
        this.lastRotationPlan = null;
        this.lastRotationProvider = null;
        this.resetTicks = 0;
        this.setRotation(null);
        this.previousAuraCameraAngle = null;
        this.auraCameraAngle = null;
    }

    public Angle getAuraCameraAngle() {
        return this.auraCameraAngle != null ? this.auraCameraAngle : AngleUtil.cameraAngle();
    }

    public Angle getPreviousAuraCameraAngle() {
        return this.previousAuraCameraAngle != null ? this.previousAuraCameraAngle : this.getAuraCameraAngle();
    }

    public void tickTasks() {
        this.rotationPlanTaskProcessor.tick(1);
    }

    private void applyCameraRotation(Angle angle) {
        if (this.lastRotationProvider instanceof ClickAction) {
            return;
        }
        Aura aura = Aura.getInstance();
        if (RotationController.mc.field_1724 != null && aura != null && aura.isState() && aura.isCameraRotationEnabled() && aura.getTarget() != null) {
            if (aura.isRenderCameraRotationActive()) {
                return;
            }
            RotationController.mc.field_1724.method_36456(angle.getYaw());
            RotationController.mc.field_1724.method_36457(angle.getPitch());
        }
    }

    @EventHandler
    public void onPlayerVelocityStrafe(PlayerVelocityStrafeEvent e) {
        boolean targetCorrection;
        if (Aura.getInstance().isLegitRotationActive() && this.lastRotationProvider == Aura.getInstance()) {
            return;
        }
        RotationPlan currentRotationPlan = this.getCurrentRotationPlan();
        Aura aura = Aura.getInstance();
        boolean bl = targetCorrection = aura != null && aura.isState() && aura.isTargetCorrectionEnabled() && aura.getTarget() != null && aura.getTarget().method_5805() && (ScreenWalk.getInstance() == null || !ScreenWalk.getInstance().isExternalInventorySwapBlocked());
        if (currentRotationPlan != null && (currentRotationPlan.isMoveCorrection() || targetCorrection)) {
            e.setVelocity(this.fixVelocity(e.getVelocity(), e.getMovementInput(), e.getSpeed()));
        }
    }

    @EventHandler
    public void onTick(GlobalTickEvent e) {
        this.tickTasks();
        EventManager.callEvent(new RotationUpdateEvent((byte)0));
        this.update();
        EventManager.callEvent(new RotationUpdateEvent((byte)2));
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        class_2596<?> class_25962;
        if (event.isSend() && (class_25962 = event.getPacket()) instanceof class_2886) {
            class_2886 useItem = (class_2886)class_25962;
            if (forcedInteractAngle != null || this.currentAngle != null) {
                Angle packetAngle = forcedInteractAngle != null ? forcedInteractAngle : this.currentAngle;
                PlayerInteractItemC2SPacketAccessor accessor = (PlayerInteractItemC2SPacketAccessor)useItem;
                accessor.recoin$setYaw(packetAngle.getYaw());
                accessor.recoin$setPitch(packetAngle.getPitch());
            }
        }
        if (!event.isCancelled()) {
            class_2596<?> class_25963 = event.getPacket();
            Objects.requireNonNull(class_25963);
            class_2596<?> class_25964 = class_25963;
            if (class_25964 instanceof class_2828 player && player.method_36172()) {
                this.serverAngle = new Angle(player.method_12271(1.0f), player.method_12270(1.0f));
            } else if (class_25964 instanceof class_2708 player) {
                this.serverAngle = new Angle(player.comp_3228().comp_3150(), player.comp_3228().comp_3151());
            }
        }
    }

    public RotationPlan getLastRotationPlan() {
        return this.lastRotationPlan;
    }

    public TaskProcessor<RotationPlan> getRotationPlanTaskProcessor() {
        return this.rotationPlanTaskProcessor;
    }

    public Angle getCurrentAngle() {
        return this.currentAngle;
    }

    public Angle getPreviousAngle() {
        return this.previousAngle;
    }

    public Angle getServerAngle() {
        return this.serverAngle;
    }

    public Module getLastRotationProvider() {
        return this.lastRotationProvider;
    }

    public int getResetTicks() {
        return this.resetTicks;
    }
}

