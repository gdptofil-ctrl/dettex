/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_1743
 *  net.minecraft.class_239
 *  net.minecraft.class_3489
 *  net.minecraft.class_3966
 *  net.minecraft.class_9362
 */
package ru.recoin.implement.features.modules.combat;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1743;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_3489;
import net.minecraft.class_3545;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_9362;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.logger.LoggerUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.task.TaskPriority;
import ru.recoin.core.Main;
import ru.recoin.implement.events.player.RotationUpdateEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.modules.combat.MaceHelper;
import ru.recoin.implement.features.modules.combat.killaura.attack.AttackPerpetrator;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.PointFinder;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RaytracingUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationConfig;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.AngleSmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.LinearSmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.SmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.shard.Shard21SmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.shard.Shard2SmoothMode;

public class TriggerBot
extends Module {
    private class_1309 target = null;
    private long groundedSince = -1L;
    private final MultiSelectSetting attackSetting = new MultiSelectSetting("Attack setting", "Allows you to customize the attack").value("Only Critical", "Crits Only Space", "Smart Crits", "Break Shield", "UnPress Shield", "No Attack When Eat", "No Attack In Containers", "Only Weapon", "Ignore The Walls").selected("Only Critical", "Smart Crits", "Break Shield", "No Attack When Eat", "No Attack In Containers");
    private final BooleanSetting onlyThroughPartialBlocks = new BooleanSetting("Only Through Partial Blocks", "Allows attacks only through non-full blocks while Ignore The Walls is enabled").setValue(false).visible(() -> this.attackSetting.isSelected("Ignore The Walls"));
    private final BooleanSetting randomizeHits = new BooleanSetting("\u0420\u0430\u043d\u0434\u043e\u043c\u0438\u0437\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u0443\u0434\u0430\u0440\u044b", "\u0420\u0430\u043d\u0434\u043e\u043c\u0438\u0437\u0430\u0446\u0438\u044f \u0432\u044b\u0441\u043e\u0442\u044b \u043f\u0430\u0434\u0435\u043d\u0438\u044f \u0434\u043b\u044f \u043a\u0440\u0438\u0442\u043e\u0432").setValue(false);
    private final SelectSetting sprintResetMode = new SelectSetting("Sprint Reset", "Selects sprint reset behavior before attack").value("Off", "Legit", "Packet", "Fast").selected("Legit");
    private final SelectSetting bypass = new SelectSetting("Bypass", "Trigger bypass mode: Shard / Shard 2.0 add silent rotation and a reach guard").value("Default", "Shard", "Shard 2.0", "Shard 2.1").selected("Default");
    private final ValueSetting reach = new ValueSetting("Reach", "How far to look for a target (attacks stay inside the server limit)").setValue(3.0f).range(3.0f, 4.0f).step(0.05f);
    private final ValueSetting silentFov = new ValueSetting("Silent FOV", "How close to the crosshair a target must be for silent trigger").setValue(10.0f).range(2.0f, 40.0f).step(1.0f);
    private final ValueSetting reactionMs = new ValueSetting("Reaction", "Human reaction delay before the first hit on a new target").setValue(120.0f).range(0.0f, 400.0f).step(10.0f);
    private final PointFinder pointFinder = new PointFinder();
    private AngleSmoothMode smoothMode;
    private String smoothModeName = "";
    private long readyAt;
    private long nextHitAt;
    private long lastPointAt;
    private class_243 cachedPoint;
    private class_1297 cachedPointEntity;
    private long lastGuardLog;

    public static TriggerBot getInstance() {
        return Instance.get(TriggerBot.class);
    }

    public TriggerBot() {
        super("TriggerBot", "Trigger Bot", ModuleCategory.COMBAT);
        this.setup(this.attackSetting, this.onlyThroughPartialBlocks, this.randomizeHits, this.sprintResetMode, this.bypass, this.reach, this.silentFov, this.reactionMs);
    }

    private boolean isShardBypass() {
        return !this.bypass.isSelected("Default");
    }

    /** Attacks are never sent beyond this distance: Shard flags anything that looks like reach. */
    private float safeReach() {
        if (this.bypass.isSelected("Shard 2.1")) {
            return 3.15f;
        }
        if (this.bypass.isSelected("Shard 2.0")) {
            return 3.09f;
        }
        if (this.bypass.isSelected("Shard")) {
            return 3.05f;
        }
        return 3.0f;
    }

    private AngleSmoothMode getSmoothMode() {
        if (this.smoothMode == null || !this.smoothModeName.equals(this.bypass.getSelected())) {
            this.smoothModeName = this.bypass.getSelected();
            this.smoothMode = switch (this.bypass.getSelected()) {
                case "Shard 2.1" -> new Shard21SmoothMode();
                case "Shard 2.0" -> new Shard2SmoothMode();
                case "Shard" -> new SmoothMode();
                default -> new LinearSmoothMode();
            };
        }
        return this.smoothMode;
    }

    @Override
    public void deactivate() {
        this.target = null;
        this.groundedSince = -1L;
        this.readyAt = 0L;
        this.nextHitAt = 0L;
        this.cachedPoint = null;
        this.cachedPointEntity = null;
        RotationController.INSTANCE.release(this);
        Main.getInstance().getAttackPerpetrator().getAttackHandler().clearPacketSprintReset();
        Main.getInstance().getAttackPerpetrator().getAttackHandler().clearFastSprintReset(this);
        super.deactivate();
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (this.isShardBypass()) {
            this.shardTick();
            return;
        }
        this.defaultTick();
    }

    private void defaultTick() {
        class_3966 r;
        class_1297 class_12972;
        class_3966 result;
        if (TriggerBot.mc.field_1724 == null || !this.canAttackWithHeldItem()) {
            this.target = null;
            Main.getInstance().getAttackPerpetrator().getAttackHandler().clearPacketSprintReset();
            Main.getInstance().getAttackPerpetrator().getAttackHandler().clearFastSprintReset(this);
            return;
        }
        boolean mace = TriggerBot.mc.field_1724.method_6047().method_7909() instanceof class_9362;
        float range = 3.0f + (mace ? MaceHelper.getInstance().getTargetDistanceBonus() : 0.0f);
        boolean ignoreWalls = this.attackSetting.isSelected("Ignore The Walls");
        if (ignoreWalls) {
            result = RaytracingUtil.raytraceEntity(range, AngleUtil.cameraAngle(), entity -> {
                class_1657 player;
                return !(entity instanceof class_1657) || !FriendUtils.isFriend((player = (class_1657)entity).method_5477().getString());
            });
        } else {
            class_3966 entityHit;
            class_239 hit = RaytracingUtil.raycastCrosshair((class_1297)TriggerBot.mc.field_1724, range, range, 1.0f, AngleUtil.cameraAngle());
            class_3966 class_39662 = result = hit instanceof class_3966 ? (entityHit = (class_3966)hit) : null;
        }
        if (result instanceof class_3966 && (class_12972 = (r = result).method_17782()) instanceof class_1309) {
            AttackPerpetrator.AttackPerpetratorConfigurable config;
            class_1309 entity2 = (class_1309)class_12972;
            if (ignoreWalls && this.onlyThroughPartialBlocks.isValue() && !RaytracingUtil.canSeeThroughPartialBlocks(TriggerBot.mc.field_1724.method_33571(), r.method_17784())) {
                this.target = null;
                Main.getInstance().getAttackPerpetrator().getAttackHandler().clearPacketSprintReset();
                Main.getInstance().getAttackPerpetrator().getAttackHandler().clearFastSprintReset(this);
                return;
            }
            ArrayList<String> options = new ArrayList<String>(this.attackSetting.getSelected());
            if (this.onlyThroughPartialBlocks.isValue() && ignoreWalls) {
                options.add("Only Through Partial Blocks");
            }
            if (options.contains("Only Critical") && this.isInWater()) {
                options.remove("Only Critical");
            }
            if (options.contains("Only Critical") && options.contains("Crits Only Space") && this.hasWaitedOnGroundForSplash()) {
                options.remove("Only Critical");
            }
            if ((config = new AttackPerpetrator.AttackPerpetratorConfigurable(this.target = entity2, AngleUtil.cameraAngle(), mace ? 3.0f : 3.3f, options, new SelectSetting("Mode", "").value("Default"), this.target.method_5829(), this.sprintResetMode, this.randomizeHits.isValue(), false, this)).isFastSprintReset()) {
                Main.getInstance().getAttackPerpetrator().getAttackHandler().updateFastSprintReset(config);
            } else {
                Main.getInstance().getAttackPerpetrator().getAttackHandler().clearFastSprintReset(this);
            }
            Main.getInstance().getAttackPerpetrator().performAttack(config);
        } else {
            this.target = null;
            Main.getInstance().getAttackPerpetrator().getAttackHandler().clearPacketSprintReset();
            Main.getInstance().getAttackPerpetrator().getAttackHandler().clearFastSprintReset(this);
        }
    }

    // ------------------------------------------------------------------ Shard bypass
    //
    // "Shard" / "Shard 2.0" keep the crosshair trigger, but the hit itself is sent with a
    // silently rotated server angle (the camera never moves), only after a human reaction
    // delay and only inside the reach the server accepts. The target may be found up to
    // 4 blocks away, yet the attack packet is never sent from beyond ~3.1 blocks, so a
    // 4-block player cannot trigger a reach flag.

    @EventHandler
    public void onRotationUpdate(RotationUpdateEvent event) {
        if (!this.isShardBypass()) {
            return;
        }
        if (event.getType() == 0) {
            this.shardRotate();
        } else if (event.getType() == 2) {
            this.shardAttack();
        }
    }

    private void shardTick() {
        if (TriggerBot.mc.field_1724 == null || TriggerBot.mc.field_1687 == null || TriggerBot.mc.field_1755 != null
                || !this.canAttackWithHeldItem()
                || (this.attackSetting.isSelected("No Attack When Eat") && TriggerBot.mc.field_1724.method_6115())) {
            this.releaseShard();
            return;
        }
        Aura aura = Aura.getInstance();
        if (aura != null && aura.isState() && aura.getTarget() != null) {
            this.releaseShard();   // Aura owns the rotation while it is active
            return;
        }
        class_1309 found = this.findShardTarget();
        if (found == null) {
            this.releaseShard();
            return;
        }
        if (found != this.target) {
            this.target = found;
            this.cachedPoint = null;
            this.cachedPointEntity = null;
            this.readyAt = System.nanoTime() + (long)(this.reactionMs.getValue() * 1_000_000.0)
                    + ThreadLocalRandom.current().nextLong(0L, 90_000_000L);
        }
    }

    private void releaseShard() {
        if (this.target != null) {
            RotationController.INSTANCE.release(this);
        }
        this.target = null;
        this.readyAt = 0L;
        this.nextHitAt = 0L;
        this.cachedPoint = null;
        this.cachedPointEntity = null;
        Main.getInstance().getAttackPerpetrator().getAttackHandler().clearPacketSprintReset();
        Main.getInstance().getAttackPerpetrator().getAttackHandler().clearFastSprintReset(this);
    }

    private class_1309 findShardTarget() {
        Angle view = AngleUtil.cameraAngle();
        class_243 eye = TriggerBot.mc.field_1724.method_33571();
        class_1309 best = null;
        double bestScore = Double.MAX_VALUE;
        for (class_1297 entity : TriggerBot.mc.field_1687.method_18112()) {
            class_1309 living;
            if (!(entity instanceof class_1309) || (living = (class_1309)entity) == TriggerBot.mc.field_1724
                    || !living.method_5805() || living.method_31481() || living.method_6032() <= 0.0f) {
                continue;
            }
            if (living instanceof class_1657 && FriendUtils.isFriend((class_1297)living)) {
                continue;
            }
            double distance = eye.method_1022(living.method_33571());
            if (distance > (double)this.reach.getValue()) {
                continue;
            }
            if (!this.attackSetting.isSelected("Ignore The Walls") && this.isBehindWall(eye, living)) {
                continue;
            }
            Angle aim = AngleUtil.fromVec3d(living.method_33571().method_1020(eye));
            double angle = Math.hypot(RotationController.computeAngleDifference(aim.getYaw(), view.getYaw()), aim.getPitch() - view.getPitch());
            if (angle > (double)this.silentFov.getValue()) {
                continue;
            }
            double score = angle * 4.0 + distance;   // prefer what is closest to the crosshair, then the nearest
            if (score < bestScore) {
                bestScore = score;
                best = living;
            }
        }
        return best;
    }

    private boolean isBehindWall(class_243 eye, class_1309 entity) {
        class_3965 hit = RaytracingUtil.raycast(eye, entity.method_33571(), class_3959.class_3960.field_17559);
        return hit != null && hit.method_17783() != class_239.class_240.field_1332;
    }

    private class_243 aimPoint(class_1309 entity) {
        long now = System.nanoTime();
        if (this.cachedPoint == null || this.cachedPointEntity != entity || now - this.lastPointAt > 250_000_000L) {
            class_3545<class_243, class_238> result = this.pointFinder.computeMultiPointVector(entity, this.safeReach() + 0.5f,
                    RotationController.INSTANCE.getRotation(), class_243.field_1353,
                    this.attackSetting.isSelected("Ignore The Walls"), this.onlyThroughPartialBlocks.isValue());
            this.cachedPoint = (class_243)result.method_15442();
            this.cachedPointEntity = entity;
            this.lastPointAt = now;
        }
        return this.cachedPoint;
    }

    private void shardRotate() {
        if (this.target == null || TriggerBot.mc.field_1724 == null || System.nanoTime() < this.readyAt) {
            return;
        }
        Angle aim = AngleUtil.fromVec3d(this.aimPoint(this.target).method_1020(TriggerBot.mc.field_1724.method_33571()));
        RotationConfig config = new RotationConfig(this.getSmoothMode(), true, true);
        RotationController.INSTANCE.rotateTo(new Angle.VecRotation(aim, aim.toVector()), this.target, 1, config, TaskPriority.HIGH_IMPORTANCE_1, this);
    }

    private void shardAttack() {
        if (this.target == null || TriggerBot.mc.field_1724 == null || System.nanoTime() < this.readyAt) {
            return;
        }
        long now = System.nanoTime();
        if (now < this.nextHitAt) {
            return;
        }
        float safe = this.safeReach();
        if (!this.isWithinReach(this.target, safe)) {
            if (now - this.lastGuardLog > 2_000_000_000L) {
                this.lastGuardLog = now;
                LoggerUtil.info("[TriggerBot] target is outside the safe reach (" + safe + "), attack skipped");
            }
            return;
        }
        Angle server = RotationController.INSTANCE.getRotation();
        if (!RaytracingUtil.rayTrace(server.toVector(), safe, this.target.method_5829(), this.onlyThroughPartialBlocks.isValue())) {
            return;   // the silent rotation has not reached the hitbox yet
        }
        ArrayList<String> options = new ArrayList<String>(this.attackSetting.getSelected());
        if (this.onlyThroughPartialBlocks.isValue() && this.attackSetting.isSelected("Ignore The Walls")) {
            options.add("Only Through Partial Blocks");
        }
        if (options.contains("Only Critical") && this.isInWater()) {
            options.remove("Only Critical");
        }
        if (options.contains("Only Critical") && options.contains("Crits Only Space") && this.hasWaitedOnGroundForSplash()) {
            options.remove("Only Critical");
        }
        AttackPerpetrator.AttackPerpetratorConfigurable config = new AttackPerpetrator.AttackPerpetratorConfigurable(this.target, server, safe,
                options, this.bypass, this.target.method_5829(), this.sprintResetMode, this.randomizeHits.isValue(), true, this);
        if (config.isFastSprintReset()) {
            Main.getInstance().getAttackPerpetrator().getAttackHandler().updateFastSprintReset(config);
        } else {
            Main.getInstance().getAttackPerpetrator().getAttackHandler().clearFastSprintReset(this);
        }
        Main.getInstance().getAttackPerpetrator().performAttack(config);
        // small human gap between clicks; the click scheduler still enforces the weapon cooldown
        this.nextHitAt = now + ThreadLocalRandom.current().nextLong(15_000_000L, 85_000_000L);
    }

    private boolean isWithinReach(class_1309 entity, float range) {
        class_243 eye = TriggerBot.mc.field_1724.method_33571();
        class_238 box = entity.method_5829();
        double x = eye.field_1352 - Math.clamp(eye.field_1352, box.field_1323, box.field_1320);
        double y = eye.field_1351 - Math.clamp(eye.field_1351, box.field_1322, box.field_1325);
        double z = eye.field_1350 - Math.clamp(eye.field_1350, box.field_1321, box.field_1324);
        return x * x + y * y + z * z <= (double)range * (double)range;
    }

    private boolean hasWaitedOnGroundForSplash() {
        if (TriggerBot.mc.field_1724 == null || !TriggerBot.mc.field_1724.method_24828()) {
            this.groundedSince = -1L;
            return false;
        }
        if (this.groundedSince < 0L) {
            this.groundedSince = System.currentTimeMillis();
        }
        return System.currentTimeMillis() - this.groundedSince >= 200L;
    }

    private boolean canAttackWithHeldItem() {
        if (!this.attackSetting.isSelected("Only Weapon")) {
            return true;
        }
        return TriggerBot.mc.field_1724.method_6047().method_31573(class_3489.field_42611) || TriggerBot.mc.field_1724.method_6047().method_7909() instanceof class_1743 || TriggerBot.mc.field_1724.method_6047().method_7909() instanceof class_9362;
    }

    private boolean isInWater() {
        return TriggerBot.mc.field_1724 != null && (TriggerBot.mc.field_1724.method_5799() || TriggerBot.mc.field_1724.method_5869() || TriggerBot.mc.field_1724.method_5681());
    }

    public class_1309 getTarget() {
        return this.target;
    }

    public long getGroundedSince() {
        return this.groundedSince;
    }

    public MultiSelectSetting getAttackSetting() {
        return this.attackSetting;
    }

    public BooleanSetting getOnlyThroughPartialBlocks() {
        return this.onlyThroughPartialBlocks;
    }

    public BooleanSetting getRandomizeHits() {
        return this.randomizeHits;
    }

    public SelectSetting getSprintResetMode() {
        return this.sprintResetMode;
    }
}

