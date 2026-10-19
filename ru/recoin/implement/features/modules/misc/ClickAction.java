/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_1735
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_239
 *  net.minecraft.class_2596
 *  net.minecraft.class_2886
 *  net.minecraft.class_3532
 *  net.minecraft.class_3966
 *  net.minecraft.class_746
 */
package ru.recoin.implement.features.modules.misc;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_239;
import net.minecraft.class_2596;
import net.minecraft.class_2886;
import net.minecraft.class_3532;
import net.minecraft.class_3966;
import net.minecraft.class_746;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BindSetting;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.entity.PlayerInventoryComponent;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.entity.ScreenWalkInventorySync;
import ru.recoin.common.util.other.BooleanSettable;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.other.StopWatch;
import ru.recoin.common.util.task.TaskPriority;
import ru.recoin.common.util.task.scripts.Script;
import ru.recoin.common.util.world.ServerUtil;
import ru.recoin.implement.events.keyboard.HotBarScrollEvent;
import ru.recoin.implement.events.keyboard.KeyEvent;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.HotBarUpdateEvent;
import ru.recoin.implement.events.player.InputEvent;
import ru.recoin.implement.events.player.PostMotionEvent;
import ru.recoin.implement.events.player.RotationUpdateEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationConfig;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.WindChargeSmoothMode;
import ru.recoin.implement.features.modules.misc.clickaction.SpookyWindCharge;
import ru.recoin.implement.features.modules.player.AutoTool;
import ru.recoin.implement.features.modules.render.ProjectilePrediction;

public class ClickAction
extends Module {
    private final BindSetting expBind = new BindSetting("EXP Bottle", "Throw Experience Bottle");
    private final BindSetting friendBind = new BindSetting("Friend Add", "add/remove Friend");
    private final BooleanSetting windChargeUnderSelf = new BooleanSetting("Wind Charge Under Self", "Rotate down before using wind charge").setValue(true);
    private final BooleanSetting windChargeJump = new BooleanSetting("Wind Charge Jump", "Jump after using wind charge").setValue(true);
    private final transient SpookyWindCharge spookyWindCharge = new SpookyWindCharge(this);
    private final List<KeyBind> keyBindings = new ArrayList<KeyBind>();
    private final StopWatch stopWatch = new StopWatch();
    private final Script script = new Script();
    private final RotationConfig windChargeRotation = new RotationConfig(new WindChargeSmoothMode(), true, true);
    private transient class_1735 pendingUseSlot;
    private transient Angle pendingUseAngle;
    private transient boolean pendingUseScheduled;
    private transient boolean pendingUseClearAfterMotion;
    private transient boolean pendingUseMoveCorrection;
    private transient int pendingUseWaitTicks;
    private transient int jumpCountdown = -1;
    private transient boolean windChargeJumpInput;

    public static ClickAction getInstance() {
        return Instance.get(ClickAction.class);
    }

    public ClickAction() {
        super("ClickAction", "Click Action", ModuleCategory.MISC);
        this.keyBindings.add(new KeyBind(class_1802.field_8634, new BindSetting("Ender Pearl", "Throw Ender Pearl"), new BooleanSettable()));
        this.keyBindings.add(new KeyBind(class_1802.field_49098, new BindSetting("Wind Charge", "Throw Wind Charge"), new BooleanSettable()));
        this.keyBindings.forEach(bind -> this.setup(bind.setting));
        this.setup(this.expBind, this.friendBind, this.windChargeUnderSelf, this.windChargeJump);
    }

    @EventHandler
    public void onHotBarUpdate(HotBarUpdateEvent e) {
        if (!this.script.isFinished() || this.spookyWindCharge.suppressesMovement()) {
            e.cancel();
        }
    }

    @EventHandler
    public void onHotBarScroll(HotBarScrollEvent e) {
        if (!this.script.isFinished() || this.spookyWindCharge.suppressesMovement()) {
            e.cancel();
        }
    }

    @EventHandler
    public void onKey(KeyEvent e) {
        class_3966 result;
        Object class_2392;
        if (e.isKeyDown(this.friendBind.getKey()) && (class_2392 = ClickAction.mc.field_1765) instanceof class_3966 && (class_2392 = (result = (class_3966)class_2392).method_17782()) instanceof class_1657) {
            class_1657 player = (class_1657)class_2392;
            if (FriendUtils.isFriend((class_1297)player)) {
                FriendUtils.removeFriend(player);
            } else {
                FriendUtils.addFriend(player);
            }
        }
        this.keyBindings.stream().filter(bind -> e.isKeyDown(bind.setting.getKey())).forEach(this::swapAndUse);
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        for (KeyBind bind : this.keyBindings) {
            if (!PlayerIntersectionUtil.isKey(bind.setting) || PlayerInventoryUtil.getSlot(bind.item) == null) continue;
            ProjectilePrediction.getInstance().drawPredictionInHand(e.getStack(), List.of(bind.item.method_7854()), this.getUseAngle(bind));
        }
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (ClickAction.mc.field_1724 == null || ClickAction.mc.field_1687 == null) {
            this.spookyWindCharge.reset();
            this.jumpCountdown = -1;
            return;
        }
        if (this.jumpCountdown > 0) {
            --this.jumpCountdown;
        }
        this.spookyWindCharge.tick();
        if (this.spookyWindCharge.active()) {
            return;
        }
        if (PlayerIntersectionUtil.isKey(this.expBind)) {
            if (PlayerInventoryComponent.isInventoryActionBusy()) {
                return;
            }
            AutoTool autoTool = Instance.get(AutoTool.class);
            if (autoTool != null && autoTool.isHoldingTool()) {
                return;
            }
            if (PlayerInventoryUtil.notifyCooldown(class_1802.field_8287, this.expBind.getName())) {
                return;
            }
            class_1735 slot = PlayerInventoryUtil.getSlot(class_1802.field_8287);
            if (slot == null) {
                return;
            }
            RotationController.INSTANCE.rotateTo(AngleUtil.pitch(75.0f), new RotationConfig(true, false), TaskPriority.HIGH_IMPORTANCE_2, this);
            if (ClickAction.mc.field_1724.method_6047().method_7909() != class_1802.field_8287) {
                if (this.script.isFinished() && this.stopWatch.every(250.0)) {
                    int hotbarSlot = ClickAction.mc.field_1724.method_31548().method_67532();
                    class_746 player = ClickAction.mc.field_1724;
                    class_1799 original = player.method_6047().method_7972();
                    boolean bundled = PlayerInventoryUtil.isBundleBackedSlot(slot);
                    ScreenWalkInventorySync.swapSlot(slot, hotbarSlot, true, null);
                    this.script.cleanup().addTickStep(0, () -> {
                        if (ClickAction.mc.field_1724 != player) {
                            return;
                        }
                        if (bundled) {
                            ScreenWalkInventorySync.swapSlot(slot, hotbarSlot, true, null);
                            return;
                        }
                        class_1735 source = PlayerInventoryUtil.resolveSlot(slot);
                        if (source != null && PlayerInventoryUtil.matchesSwapStack(source.method_7677(), original)) {
                            ScreenWalkInventorySync.swapSlot(source, hotbarSlot, true, null);
                        }
                    }, () -> !PlayerInventoryComponent.isInventoryActionBusy());
                }
            } else {
                PlayerIntersectionUtil.interactItem(class_1268.field_5808, AngleUtil.pitch(75.0f));
                this.stopWatch.reset();
            }
        } else if (!this.script.isFinished()) {
            this.script.update();
        }
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        class_2596<?> class_25962;
        if (this.windChargeJump.isValue() && !this.spookyWindCharge.active() && event.isSend() && (class_25962 = event.getPacket()) instanceof class_2886) {
            class_2886 packet = (class_2886)class_25962;
            if (ClickAction.mc.field_1724 != null && ClickAction.mc.field_1724.method_5998(packet.method_12551()).method_31574(class_1802.field_49098)) {
                this.jumpCountdown = 1;
            }
        }
    }

    @EventHandler(value=4)
    public void onInput(InputEvent event) {
        if (this.windChargeJumpInput) {
            PlayerInventoryComponent.setAutomatedJump(false);
            this.windChargeJumpInput = false;
        }
        if (this.spookyWindCharge.suppressesMovement()) {
            this.spookyWindCharge.input(event);
            return;
        }
        if (this.jumpCountdown == 0) {
            event.setJumping(true);
            this.windChargeJumpInput = true;
            PlayerInventoryComponent.setAutomatedJump(true);
            this.jumpCountdown = -1;
        }
    }

    @EventHandler
    public void onRotationUpdate(RotationUpdateEvent event) {
        class_1792 item;
        if (this.spookyWindCharge.active()) {
            return;
        }
        if (this.pendingUseSlot == null || this.pendingUseAngle == null) {
            return;
        }
        if (this.pendingUseMoveCorrection) {
            this.updateWindChargeRotation(event);
            return;
        }
        if (event.getType() == 0 && ++this.pendingUseWaitTicks > 40) {
            this.clearPendingUse();
            return;
        }
        if (!this.pendingUseScheduled && this.pendingUseSlot.method_7677() != null && PlayerInventoryUtil.notifyCooldown(item = this.pendingUseSlot.method_7677().method_7909(), item == class_1802.field_49098 ? "Wind Charge" : "Ender Pearl")) {
            this.clearPendingUse();
            return;
        }
        RotationController.INSTANCE.rotateTo(this.pendingUseAngle, 1, this.pendingUseMoveCorrection ? this.windChargeRotation : new RotationConfig(false, true), TaskPriority.CRITICAL_FOR_USER_PROTECTION, this);
        if (event.getType() == 2 && !this.pendingUseScheduled && !PlayerInventoryComponent.isInventoryActionBusy()) {
            float pitchError = Math.abs(RotationController.INSTANCE.getRotation().getPitch() - this.pendingUseAngle.getPitch());
            float sentPitchError = Math.abs(RotationController.INSTANCE.getServerAngle().getPitch() - this.pendingUseAngle.getPitch());
            if (pitchError <= 5.0f && sentPitchError <= 5.0f) {
                this.pendingUseScheduled = true;
                class_1735 slot = this.pendingUseSlot;
                Angle angle = this.pendingUseAngle;
                ScreenWalkInventorySync.swapUseRestore(slot, () -> {
                    try {
                        if (!this.isState() || this.pendingUseSlot == null || ClickAction.mc.field_1724 == null) {
                            return;
                        }
                        this.interactItem(angle);
                    }
                    finally {
                        this.pendingUseClearAfterMotion = true;
                    }
                });
            }
        }
    }

    @EventHandler(value=0)
    public void onSpookyRotationUpdate(RotationUpdateEvent event) {
        if (event.getType() == 0) {
            this.spookyWindCharge.updateRotation();
        }
    }

    private void updateWindChargeRotation(RotationUpdateEvent event) {
        class_1792 item;
        if (this.pendingUseSlot == null || this.pendingUseAngle == null) {
            return;
        }
        if (this.pendingUseScheduled && PlayerInventoryComponent.script.isFinished()) {
            this.clearPendingUse(!this.pendingUseMoveCorrection);
            return;
        }
        if (this.pendingUseSlot.method_7677() != null && PlayerInventoryUtil.notifyCooldown(item = this.pendingUseSlot.method_7677().method_7909(), item == class_1802.field_49098 ? "Wind Charge" : "Ender Pearl")) {
            this.clearPendingUse(!this.pendingUseMoveCorrection);
            return;
        }
        RotationController.INSTANCE.rotateTo(this.pendingUseAngle, 1, this.pendingUseMoveCorrection ? this.windChargeRotation : new RotationConfig(false, true), TaskPriority.CRITICAL_FOR_USER_PROTECTION, this);
        if (event.getType() == 2 && !this.pendingUseScheduled) {
            float pitchError = Math.abs(RotationController.INSTANCE.getRotation().getPitch() - this.pendingUseAngle.getPitch());
            float sentPitchError = Math.abs(RotationController.INSTANCE.getServerAngle().getPitch() - this.pendingUseAngle.getPitch());
            if (pitchError <= 5.0f && sentPitchError <= 5.0f) {
                this.pendingUseScheduled = true;
                class_1735 slot = this.pendingUseSlot;
                ScreenWalkInventorySync.swapUseRestore(slot, () -> {
                    try {
                        this.interactItem(RotationController.INSTANCE.getServerAngle());
                    }
                    finally {
                        this.pendingUseClearAfterMotion = true;
                    }
                });
            }
        }
    }

    @EventHandler
    public void onPostMotion(PostMotionEvent event) {
        if (this.pendingUseClearAfterMotion) {
            this.clearPendingUse(!this.pendingUseMoveCorrection);
        }
    }

    public void swapAndUse(KeyBind bind) {
        if (this.spookyWindCharge.active()) {
            return;
        }
        if (PlayerInventoryUtil.notifyCooldown(bind.item, bind.setting.getName())) {
            return;
        }
        class_1735 targetSlot = PlayerInventoryUtil.getSlot(bind.item);
        if (targetSlot == null) {
            return;
        }
        if (bind.item == class_1802.field_49098) {
            this.useWindCharge(targetSlot);
            return;
        }
        Angle useAngle = this.getUseAngle(bind);
        if (bind.item == class_1802.field_8634) {
            this.usePearlSlot(targetSlot, useAngle);
        } else {
            this.useSlot(targetSlot, useAngle);
        }
    }

    private Angle getWindChargeAngle() {
        if (!this.windChargeUnderSelf.isValue()) {
            return AngleUtil.cameraAngle();
        }
        float t = ClickAction.mc.field_1724 != null ? (float)ClickAction.mc.field_1724.field_6012 + mc.method_61966().method_60637(false) : 0.0f;
        float silent = (float)(Math.sin(t * 0.31f) * 6.600001001477404 + Math.sin(t * 0.73f + 1.1f) * 0.3000001491338646);
        float yaw = AngleUtil.cameraAngle().getYaw() + silent;
        float pitch = class_3532.method_15363((float)(90.0f + silent / 2.0f), (float)-90.0f, (float)90.0f);
        return new Angle(yaw, pitch);
    }

    private Angle getUseAngle(KeyBind bind) {
        if (bind.item == class_1802.field_49098) {
            return this.getWindChargeAngle();
        }
        return AngleUtil.cameraAngle();
    }

    private void useWindCharge(class_1735 targetSlot) {
        if (PlayerInventoryUtil.notifyCooldown(targetSlot != null ? targetSlot.method_7677() : class_1802.field_49098.method_7854(), "Wind Charge")) {
            return;
        }
        if (!this.windChargeUnderSelf.isValue()) {
            ScreenWalkInventorySync.swapUseRestore(targetSlot, () -> this.interactItem(AngleUtil.cameraAngle()));
            return;
        }
        if (ServerUtil.isSpookyTime()) {
            this.useSpookyTimeWindCharge(targetSlot);
            return;
        }
        if (this.pendingUseSlot != null) {
            return;
        }
        this.pendingUseSlot = targetSlot;
        this.pendingUseAngle = this.getWindChargeAngle();
        this.pendingUseScheduled = false;
        this.pendingUseMoveCorrection = true;
        RotationController.INSTANCE.rotateTo(this.pendingUseAngle, 2, this.windChargeRotation, TaskPriority.CRITICAL_FOR_USER_PROTECTION, this);
    }

    private void useSpookyTimeWindCharge(class_1735 targetSlot) {
        if (ClickAction.mc.field_1724 == null || this.pendingUseSlot != null || this.spookyWindCharge.active()) {
            return;
        }
        this.jumpCountdown = -1;
        this.spookyWindCharge.start(targetSlot);
    }

    private void useSlot(class_1735 slot, Angle angle) {
        if (slot.method_7677() != null && PlayerInventoryUtil.notifyCooldown(slot.method_7677(), slot.method_7677().method_7909().method_63680().getString())) {
            return;
        }
        ScreenWalkInventorySync.swapUseRestore(slot, () -> this.interactItem(angle));
    }

    private void usePearlSlot(class_1735 slot, Angle angle) {
        if (PlayerInventoryUtil.notifyCooldown(slot != null ? slot.method_7677() : class_1802.field_8634.method_7854(), "Ender Pearl")) {
            return;
        }
        if (!this.needsAuraUseRotation()) {
            ScreenWalkInventorySync.swapUseRestore(slot, () -> this.interactItem(angle));
            return;
        }
        if (this.pendingUseSlot != null) {
            return;
        }
        this.pendingUseSlot = slot;
        this.pendingUseAngle = angle;
        this.pendingUseScheduled = false;
        this.pendingUseMoveCorrection = false;
        this.pendingUseWaitTicks = 0;
        RotationController.INSTANCE.rotateTo(this.pendingUseAngle, 1, new RotationConfig(false, true), TaskPriority.CRITICAL_FOR_USER_PROTECTION, this);
    }

    private void interactItem(Angle angle) {
        RotationController.setForcedInteractAngle(angle);
        try {
            PlayerIntersectionUtil.interactItem(class_1268.field_5808, angle);
        }
        finally {
            RotationController.setForcedInteractAngle(null);
        }
    }

    private void clearPendingUse() {
        this.clearPendingUse(true);
    }

    private void clearPendingUse(boolean releaseRotation) {
        this.pendingUseSlot = null;
        this.pendingUseAngle = null;
        this.pendingUseScheduled = false;
        this.pendingUseClearAfterMotion = false;
        this.pendingUseMoveCorrection = false;
        this.pendingUseWaitTicks = 0;
        if (releaseRotation) {
            RotationController.INSTANCE.release(this);
        }
    }

    @Override
    public void deactivate() {
        this.spookyWindCharge.reset();
        ScreenWalkInventorySync.whenIdle(this.script::update);
        this.clearPendingUse();
        this.jumpCountdown = -1;
        this.windChargeJumpInput = false;
        PlayerInventoryComponent.setAutomatedJump(false);
    }

    private boolean needsAuraUseRotation() {
        Aura aura = Aura.getInstance();
        return aura != null && aura.isState() && aura.getTarget() != null;
    }

    public boolean isExperienceSwapActive() {
        return !this.script.isFinished();
    }

    public boolean isSilentWindChargeActive() {
        return this.isState() && (this.spookyWindCharge.active() || this.pendingUseSlot != null && this.pendingUseMoveCorrection);
    }

    public boolean isSpookyWindChargeActive() {
        return this.isState() && this.spookyWindCharge.active();
    }

    public boolean isSpookyWindChargeMovementSuppressed() {
        return this.isState() && this.spookyWindCharge.suppressesMovement();
    }

    public void scheduleSpookyWindChargeJump() {
        if (this.windChargeJump.isValue()) {
            this.jumpCountdown = 2;
        }
    }

    public record KeyBind(class_1792 item, BindSetting setting, BooleanSettable draw) {
    }
}

