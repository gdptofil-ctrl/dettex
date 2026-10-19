/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10185
 *  net.minecraft.class_1268
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_1735
 *  net.minecraft.class_1802
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 *  net.minecraft.class_638
 *  net.minecraft.class_746
 */
package ru.recoin.implement.features.modules.misc.clickaction;

import net.minecraft.class_10185;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1735;
import net.minecraft.class_1802;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_638;
import net.minecraft.class_746;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.entity.PlayerInventoryComponent;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.entity.ScreenWalkInventorySync;
import ru.recoin.common.util.task.TaskPriority;
import ru.recoin.implement.events.player.InputEvent;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationConfig;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.AngleSmoothMode;
import ru.recoin.implement.features.modules.misc.ClickAction;

public final class SpookyWindCharge
extends AngleSmoothMode {
    private final ClickAction owner;
    private final RotationConfig rotation = new RotationConfig(this, true, true);
    private Phase phase = Phase.IDLE;
    private class_746 player;
    private class_638 world;
    private class_1735 slot;
    private Angle angle;
    private float baseYaw;
    private int aimTicks;
    private int requiredTicks;
    private int useWaitTicks;
    private int age;
    private int generation;
    private long lastThrow;
    private boolean previousUsing;
    private boolean suppressedUse;

    public SpookyWindCharge(ClickAction owner) {
        super("SpookyWindCharge");
        this.owner = owner;
    }

    public void start(class_1735 source) {
        if (this.active() || SpookyWindCharge.mc.field_1724 == null || SpookyWindCharge.mc.field_1687 == null || SpookyWindCharge.mc.field_1761 == null || PlayerInventoryComponent.isInventoryActionBusy() || System.currentTimeMillis() - this.lastThrow < 250L) {
            return;
        }
        this.player = SpookyWindCharge.mc.field_1724;
        this.world = SpookyWindCharge.mc.field_1687;
        this.slot = source;
        Angle current = RotationController.INSTANCE.getRotation();
        this.angle = new Angle(current.getYaw(), current.getPitch());
        this.baseYaw = current.getYaw();
        this.requiredTicks = !this.player.method_5624() && this.player.field_3913.field_54155.equals((Object)class_10185.field_54098) ? 1 : 2;
        this.age = 0;
        this.useWaitTicks = 0;
        this.aimTicks = 0;
        this.previousUsing = this.player.method_6115();
        this.suppressedUse = SpookyWindCharge.mc.field_1690.field_1904.method_1434();
        this.phase = Phase.AIM;
        ++this.generation;
        this.player.method_5728(false);
        SpookyWindCharge.mc.field_1690.field_1904.method_23481(false);
        if (this.player.method_6115()) {
            SpookyWindCharge.mc.field_1761.method_2897((class_1657)this.player);
        }
    }

    public boolean active() {
        return this.phase != Phase.IDLE;
    }

    public boolean suppressesMovement() {
        return this.phase == Phase.AIM || this.phase == Phase.THROW;
    }

    public void updateRotation() {
        if (!this.active()) {
            return;
        }
        if (!this.valid() || !this.owner.isState() || ++this.age > 80) {
            this.reset();
            return;
        }
        if (this.phase == Phase.RELEASE && this.hasAuraTarget()) {
            this.finish(true);
            return;
        }
        if (this.phase == Phase.AIM) {
            float jitter = this.angle.getPitch() != 90.0f ? (float)((Math.random() * 2.0 - 1.0) * (double)0.35f) : 0.0f;
            this.angle = new Angle(this.baseYaw + jitter, SpookyWindCharge.step(this.angle.getPitch(), 90.0f, SpookyWindCharge.speed()));
            ++this.aimTicks;
        } else if (this.phase == Phase.RELEASE) {
            Angle camera = AngleUtil.cameraAngle();
            float speed = SpookyWindCharge.speed();
            float yawDelta = class_3532.method_15393((float)(camera.getYaw() - this.angle.getYaw()));
            this.angle = new Angle(this.angle.getYaw() + class_3532.method_15363((float)yawDelta, (float)(-speed), (float)speed), SpookyWindCharge.step(this.angle.getPitch(), camera.getPitch(), speed));
            if (class_3532.method_15393((float)(camera.getYaw() - this.angle.getYaw())) == 0.0f && this.angle.getPitch() == camera.getPitch()) {
                this.finish(false);
                return;
            }
        }
        if (this.suppressesMovement() || this.suppressedUse) {
            SpookyWindCharge.mc.field_1690.field_1904.method_23481(false);
        }
        RotationController.INSTANCE.rotateTo(this.angle, 1, this.rotation, TaskPriority.CRITICAL_FOR_USER_PROTECTION, this.owner);
    }

    public void tick() {
        class_1268 held;
        if (this.active() && !this.valid()) {
            this.reset();
            return;
        }
        if (this.phase == Phase.THROW) {
            this.player.method_5728(false);
            return;
        }
        if (this.phase != Phase.AIM) {
            return;
        }
        this.player.method_5728(false);
        boolean using = this.player.method_6115();
        if (using) {
            SpookyWindCharge.mc.field_1690.field_1904.method_23481(false);
            SpookyWindCharge.mc.field_1761.method_2897((class_1657)this.player);
        }
        boolean waitUse = using || this.previousUsing;
        this.previousUsing = using;
        if (waitUse) {
            if (++this.useWaitTicks > 6) {
                this.reset();
            }
            return;
        }
        if (this.angle.getPitch() != 90.0f || this.aimTicks < this.requiredTicks || PlayerInventoryComponent.isInventoryActionBusy()) {
            return;
        }
        this.phase = Phase.THROW;
        int request = this.generation;
        held = this.player.method_6047().method_31574(class_1802.field_49098) ? class_1268.field_5808 : (this.player.method_6079().method_31574(class_1802.field_49098) ? class_1268.field_5810 : null);
        if (held != null) {
            this.throwCharge(request, held);
        } else {
            class_1735 source = this.slot;
            if (source == null || !source.method_7677().method_31574(class_1802.field_49098)) {
                this.reset();
                return;
            }
            if (!PlayerInventoryUtil.isBundleBackedSlot(source) && (source = PlayerInventoryUtil.resolveSlot(source)) == null) {
                this.reset();
                return;
            }
            class_1735 useSource = source;
            ScreenWalkInventorySync.swapUseRestore(useSource, () -> this.throwCharge(request, class_1268.field_5808));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void throwCharge(int request, class_1268 hand) {
        boolean accepted;
        if (request != this.generation || this.phase != Phase.THROW || !this.valid() || !this.owner.isState()) {
            return;
        }
        if (!this.player.method_5998(hand).method_31574(class_1802.field_49098) || System.currentTimeMillis() - this.lastThrow < 250L) {
            this.reset();
            return;
        }
        Angle use = new Angle(this.angle.getYaw(), 90.0f);
        RotationController.setForcedInteractAngle(use);
        try {
            accepted = SpookyWindCharge.mc.field_1761.method_2919((class_1657)this.player, hand).method_23665();
        }
        finally {
            RotationController.setForcedInteractAngle(null);
        }
        if (accepted) {
            this.lastThrow = System.currentTimeMillis();
            this.owner.scheduleSpookyWindChargeJump();
        }
        this.phase = Phase.RELEASE;
    }

    public void input(InputEvent event) {
        if (this.suppressesMovement()) {
            event.inputNone();
        }
    }

    private boolean valid() {
        return this.player != null && SpookyWindCharge.mc.field_1724 == this.player && SpookyWindCharge.mc.field_1687 == this.world && SpookyWindCharge.mc.field_1761 != null;
    }

    private boolean hasAuraTarget() {
        Aura aura = Aura.getInstance();
        return aura != null && aura.isState() && aura.getTarget() != null && aura.getTarget().method_5805();
    }

    public void reset() {
        this.finish(false);
    }

    private void finish(boolean handoff) {
        ++this.generation;
        this.phase = Phase.IDLE;
        this.slot = null;
        if (this.suppressedUse && this.valid() && PlayerIntersectionUtil.isKey(SpookyWindCharge.mc.field_1690.field_1904)) {
            SpookyWindCharge.mc.field_1690.field_1904.method_23481(true);
        }
        this.suppressedUse = false;
        RotationController.INSTANCE.release(this.owner, handoff);
    }

    private static float speed() {
        return 25.0f + (float)Math.random() * 10.0f;
    }

    private static float step(float current, float target, float speed) {
        return Math.abs(target - current) <= speed ? target : current + Math.copySign(speed, target - current);
    }

    @Override
    public Angle limitAngleChange(Angle current, Angle target, class_243 point, class_1297 entity) {
        return target;
    }

    @Override
    public class_243 randomValue() {
        return class_243.field_1353;
    }

    private static enum Phase {
        IDLE,
        AIM,
        THROW,
        RELEASE;

    }
}

