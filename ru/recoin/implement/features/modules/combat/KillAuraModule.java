package ru.recoin.implement.features.modules.combat;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1743;
import net.minecraft.class_1799;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3489;
import net.minecraft.class_3532;
import net.minecraft.class_746;
import net.minecraft.class_9362;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.task.TaskPriority;
import ru.recoin.implement.events.player.RotationUpdateEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationConfig;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.AngleSmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.LinearSmoothMode;

/**
 * Adaptive reach kill aura.
 *
 * <p>All hits are sent through the normal {@code ClientPlayerInteractionManager.attackEntity} path,
 * so the held item, swing animation and attack packet are produced by vanilla. Rotation is silent:
 * the camera never moves, only the movement packets are rotated.
 */
public class KillAuraModule extends Module {

    private static final class MutableVec {
        double x;
        double y;
        double z;
    }

    private final MutableVec[] pool = new MutableVec[8];
    private int poolIndex;

    private final AngleSmoothMode smoothMode = new LinearSmoothMode();
    private final RotationConfig rotationConfig = new RotationConfig(this.smoothMode, true, true);
    private final Angle[] aimAngles = {new Angle(0.0f, 0.0f), new Angle(0.0f, 0.0f)};
    private int aimAngleIndex;

    private final ValueSetting reach = new ValueSetting("Reach", "Maximum eye to hitbox distance")
            .setValue(4.0f).range(3.0f, 4.0f).step(0.05f);
    private final ValueSetting maxRotation = new ValueSetting("Max Rotation", "Maximum yaw/pitch change per movement packet")
            .setValue(30.0f).range(10.0f, 30.0f).step(1.0f);
    private final BooleanSetting onlyWeapon = new BooleanSetting("Only Weapon", "Attack only while holding a sword, axe or mace")
            .setValue(true);
    private final BooleanSetting ignoreFriends = new BooleanSetting("Ignore Friends", "Never target friends")
            .setValue(true);
    private final BooleanSetting humanMiss = new BooleanSetting("Human Miss", "Skip every 8th hit and aim 0.35 blocks away")
            .setValue(true);
    private final BooleanSetting watchdogGuard = new BooleanSetting("Watchdog Guard", "Skip nofall, regen and speed ticks")
            .setValue(true);

    private class_1309 target;
    private int tick;
    private int nextAttackTick;
    private int attacksScheduled;
    private int anomalyScore;
    private int cleanTicks;
    private int lastAnomalyTick = -1;
    private int disableTicks;
    private int stopTicks;
    private int lastAttackTick = -100;
    private class_1309 lastHitTarget;
    private int lastHitTick = -100;
    private int regenGuardUntil;
    private float lastHealth = -1.0f;
    private double lastAttackReach = 3.95;

    public static KillAuraModule getInstance() {
        return Instance.get(KillAuraModule.class);
    }

    public KillAuraModule() {
        super("KillAura", "Kill Aura", ModuleCategory.COMBAT);
        this.setup(this.reach, this.maxRotation, this.onlyWeapon, this.ignoreFriends, this.humanMiss, this.watchdogGuard);
        for (int i = 0; i < this.pool.length; ++i) {
            this.pool[i] = new MutableVec();
        }
    }

    @Override
    public void activate() {
        this.reset();
    }

    @Override
    public void deactivate() {
        this.releaseTarget();
        this.reset();
        super.deactivate();
    }

    private void reset() {
        this.target = null;
        this.tick = 0;
        this.nextAttackTick = 11;
        this.attacksScheduled = 0;
        this.anomalyScore = 0;
        this.cleanTicks = 0;
        this.lastAnomalyTick = -1;
        this.disableTicks = 0;
        this.stopTicks = 0;
        this.lastAttackTick = -100;
        this.lastHitTarget = null;
        this.lastHitTick = -100;
        this.regenGuardUntil = 0;
        this.lastHealth = -1.0f;
        this.lastAttackReach = 3.95;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        class_746 player = mc.field_1724;
        if (player == null || mc.field_1687 == null || !this.isState()) {
            return;
        }
        ++this.tick;

        if (this.stopTicks > 0) {
            --this.stopTicks;
            return;
        }
        if (this.disableTicks > 0) {
            --this.disableTicks;
            this.releaseTarget();
            return;
        }

        float health = player.method_6032();
        if (this.lastHealth >= 0.0f && health < this.lastHealth - 0.5f) {
            this.regenGuardUntil = this.tick + 80;
        }
        this.lastHealth = health;

        if (this.target != null && !this.isValidTarget(this.target)) {
            this.releaseTarget();
        }
        class_1309 found = this.findTarget();
        if (found == null) {
            this.target = null;
        } else if (found != this.target) {
            this.target = found;
        }

        if (this.tick != this.lastAnomalyTick && this.anomalyScore > 0) {
            --this.anomalyScore;
        }
        if (++this.cleanTicks >= 30) {
            this.anomalyScore = 0;
            this.cleanTicks = 0;
        }
        if (this.anomalyScore > 90) {
            this.stopTicks = 120;
            this.anomalyScore = 50;
            this.releaseTarget();
        } else if (this.anomalyScore > 70) {
            this.disableTicks = 60;
            this.anomalyScore = 65;
            this.releaseTarget();
        }
    }

    @EventHandler
    public void onRotation(RotationUpdateEvent event) {
        if (!this.isState() || mc.field_1724 == null) {
            return;
        }
        if (event.getType() == 0) {
            this.updateRotation();
        } else if (event.getType() == 2) {
            this.tryAttack();
        }
    }

    private void updateRotation() {
        class_746 player = mc.field_1724;
        class_1309 current = this.target;
        if (player == null || current == null || this.stopTicks > 0 || this.disableTicks > 0) {
            RotationController.INSTANCE.release(this);
            return;
        }
        Aura aura = Aura.getInstance();
        if (aura != null && aura.isState()) {
            return;
        }

        class_243 eye = player.method_33571();
        class_243 point = this.aimPoint(eye, current);
        Angle aim = AngleUtil.fromVec3d(point.method_1020(eye));
        float jitter = (float) (ThreadLocalRandom.current().nextDouble(-0.015, 0.015) * 57.29577951308232);

        Angle out = this.aimAngles[this.aimAngleIndex];
        this.aimAngleIndex ^= 1;
        out.setYaw(aim.getYaw() + jitter);
        out.setPitch(Math.clamp(aim.getPitch() + jitter, -89.5f, 89.5f));

        RotationController.INSTANCE.rotateTo(new Angle.VecRotation(out, out.toVector()), current, 1,
                this.rotationConfig, TaskPriority.HIGH_IMPORTANCE_1, this);
    }

    private void tryAttack() {
        class_746 player = mc.field_1724;
        class_1309 current = this.target;
        if (player == null || current == null || this.stopTicks > 0 || this.disableTicks > 0
                || mc.field_1761 == null) {
            return;
        }
        Aura aura = Aura.getInstance();
        if (aura != null && aura.isState()) {
            return;
        }
        if (this.tick < this.nextAttackTick) {
            return;
        }
        if (player.method_6115()) {
            return;
        }
        if (player.method_7261(0.5f) < 0.92f) {
            return;
        }
        if (this.onlyWeapon.isValue() && !this.isHeldWeapon(player)) {
            return;
        }
        if (this.watchdogGuard.isValue()) {
            if (this.tick < this.regenGuardUntil) {
                this.scheduleNext();
                return;
            }
            class_243 velocity = player.method_18798();
            if (velocity.field_1351 < -0.5) {
                this.scheduleNext();
                return;
            }
            if (velocity.field_1352 * velocity.field_1352 + velocity.field_1350 * velocity.field_1350 > 0.28 * 0.28) {
                this.scheduleNext();
                return;
            }
            if (player.method_5715() && !player.method_24828()) {
                this.scheduleNext();
                return;
            }
        }
        if (current == this.lastHitTarget && this.tick - this.lastHitTick < 3) {
            return;
        }

        double adaptive = this.adaptiveReach(current);
        class_243 eye = player.method_33571();
        double distanceSq = this.eyeToBoxSq(eye, current);
        if (distanceSq > adaptive * adaptive) {
            return;
        }
        double distance = Math.sqrt(distanceSq);
        if (distance > 4.2) {
            this.addAnomaly(5);
            this.scheduleNext();
            return;
        }

        class_243 point = this.aimPoint(eye, current);
        Angle desired = AngleUtil.fromVec3d(point.method_1020(eye));
        Angle server = RotationController.INSTANCE.getServerAngle();
        if (server == null) {
            server = RotationController.INSTANCE.getRotation();
        }
        double yawDiff = Math.abs(class_3532.method_15393(desired.getYaw() - server.getYaw()));
        double pitchDiff = Math.abs(desired.getPitch() - server.getPitch());
        if (yawDiff > 12.0 || pitchDiff > 12.0) {
            return;
        }
        if (server.getPitch() <= -90.0f || server.getPitch() >= 90.0f) {
            return;
        }

        this.lastAttackReach = adaptive;
        if (this.humanMiss.isValue() && (this.attacksScheduled + 1) % 8 == 0) {
            player.method_6104(class_1268.field_5808);
            this.scheduleNext();
            return;
        }

        mc.field_1761.method_2918(player, current);
        double after = Math.sqrt(this.eyeToBoxSq(player.method_33571(), current));
        if (after > 4.2) {
            this.addAnomaly(5);
        }
        this.scheduleNext();
    }

    private void scheduleNext() {
        ++this.attacksScheduled;
        int interval = 11 + ThreadLocalRandom.current().nextInt(4);
        if (this.attacksScheduled % 10 == 0) {
            interval = 16;
        }
        this.nextAttackTick = this.tick + interval;
    }

    private class_1309 findTarget() {
        class_746 player = mc.field_1724;
        if (player == null || mc.field_1687 == null) {
            return null;
        }
        class_243 eye = player.method_33571();
        class_238 search = player.method_5829().method_1014(5.0);
        List<class_1297> entities = mc.field_1687.method_66349(player, search);
        class_1309 best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int i = 0; i < entities.size(); ++i) {
            class_1297 entity = entities.get(i);
            if (!(entity instanceof class_1309) || entity == player) {
                continue;
            }
            class_1309 living = (class_1309) entity;
            if (!living.method_5805() || living.method_31481() || living.method_6032() <= 0.0f) {
                continue;
            }
            if (this.ignoreFriends.isValue() && entity instanceof class_1657 && FriendUtils.isFriend(entity)) {
                continue;
            }
            double distanceSq = this.eyeToBoxSq(eye, living);
            if (distanceSq > 25.0) {
                continue;
            }
            if (distanceSq < bestDistance) {
                bestDistance = distanceSq;
                best = living;
            }
        }
        return best;
    }

    private boolean isValidTarget(class_1309 entity) {
        return entity != mc.field_1724 && entity.method_5805() && !entity.method_31481()
                && entity.method_6032() > 0.0f && this.eyeToBoxSq(mc.field_1724.method_33571(), entity) <= 36.0;
    }

    private double adaptiveReach(class_1309 entity) {
        class_746 player = mc.field_1724;
        double value;
        class_243 velocity = entity.method_18798();
        double dx = player.method_23317() - entity.method_23317();
        double dz = player.method_23321() - entity.method_23321();
        boolean approaching = velocity.field_1352 * dx + velocity.field_1350 * dz > 0.0025;
        boolean airborne = !entity.method_24828();
        boolean hurt = entity == this.lastHitTarget && this.tick - this.lastHitTick <= 10;

        if (hurt) {
            value = 3.75;
        } else if (airborne) {
            value = 3.90;
        } else if (approaching) {
            value = 3.85;
        } else {
            value = 3.95;
        }
        value = Math.min(value, this.reach.getValue());

        double lower = Math.max(3.0, this.lastAttackReach - 0.10);
        double upper = this.reach.getValue();
        if (upper < lower) {
            lower = upper;
        }
        return Math.clamp(value, lower, upper);
    }

    private class_243 aimPoint(class_243 eye, class_1309 entity) {
        class_238 box = entity.method_5829();
        double x = Math.clamp(eye.field_1352, box.field_1323, box.field_1320);
        double y = Math.clamp(eye.field_1351, box.field_1322, box.field_1325);
        double z = Math.clamp(eye.field_1350, box.field_1321, box.field_1324);
        if (this.humanMiss.isValue() && (this.attacksScheduled + 1) % 8 == 0
                && this.tick >= this.nextAttackTick - 1) {
            double dx = x - eye.field_1352;
            double dz = z - eye.field_1350;
            double length = Math.sqrt(dx * dx + dz * dz);
            if (length > 0.0001) {
                x += (-dz / length) * 0.35;
                z += (dx / length) * 0.35;
            }
        }
        return new class_243(x, y, z);
    }

    private double eyeToBoxSq(class_243 eye, class_1309 entity) {
        class_238 box = entity.method_5829();
        MutableVec vec = this.pool[this.poolIndex++ & 7];
        vec.x = eye.field_1352 - Math.clamp(eye.field_1352, box.field_1323, box.field_1320);
        vec.y = eye.field_1351 - Math.clamp(eye.field_1351, box.field_1322, box.field_1325);
        vec.z = eye.field_1350 - Math.clamp(eye.field_1350, box.field_1321, box.field_1324);
        return vec.x * vec.x + vec.y * vec.y + vec.z * vec.z;
    }

    private boolean isHeldWeapon(class_746 player) {
        class_1799 stack = player.method_6047();
        return stack != null && !stack.method_7960()
                && (stack.method_31573(class_3489.field_42611)
                || stack.method_7909() instanceof class_1743
                || stack.method_7909() instanceof class_9362);
    }

    private void addAnomaly(int amount) {
        this.anomalyScore += amount;
        if (this.anomalyScore > 100) {
            this.anomalyScore = 100;
        }
        this.cleanTicks = 0;
        this.lastAnomalyTick = this.tick;
    }

    private void releaseTarget() {
        if (this.target != null) {
            RotationController.INSTANCE.release(this);
        }
        this.target = null;
    }

    /**
     * Called from {@code KillAuraMixin} before an attack packet is sent.
     */
    public void onAttackSent(class_1297 entity) {
        this.lastAttackTick = this.tick;
        this.lastHitTick = this.tick;
        this.lastHitTarget = entity instanceof class_1309 ? (class_1309) entity : null;
    }

    /**
     * Called from {@code KillAuraMixin}; blocks stale attacks beyond the post-hit distance.
     */
    public boolean validateAttackTarget(class_1297 entity) {
        if (this.stopTicks > 0 || mc.field_1724 == null) {
            return false;
        }
        if (entity instanceof class_1309) {
            double distance = this.eyeToBoxSq(mc.field_1724.method_33571(), (class_1309) entity);
            return distance <= 4.2 * 4.2;
        }
        return true;
    }

    public class_1309 getTarget() {
        return this.target;
    }

    public float getMaxRotation() {
        return this.maxRotation.getValue();
    }

    public int getAnomalyScore() {
        return this.anomalyScore;
    }
}
