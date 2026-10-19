/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1308
 *  net.minecraft.class_1309
 *  net.minecraft.class_1429
 *  net.minecraft.class_1657
 *  net.minecraft.class_1753
 *  net.minecraft.class_1764
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1835
 *  net.minecraft.class_1890
 *  net.minecraft.class_2338
 *  net.minecraft.class_2374
 *  net.minecraft.class_238
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_3965
 *  net.minecraft.class_640
 *  net.minecraft.class_9278
 *  net.minecraft.class_9334
 */
package ru.recoin.implement.features.modules.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_1429;
import net.minecraft.class_1657;
import net.minecraft.class_1753;
import net.minecraft.class_1764;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1835;
import net.minecraft.class_1890;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_640;
import net.minecraft.class_9278;
import net.minecraft.class_9334;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.entity.EntityCompat;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.common.util.task.TaskPriority;
import ru.recoin.implement.events.player.RotationUpdateEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.combat.CombatMotionPrediction;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RaytracingUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationConfig;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;

public final class AimBot
extends Module {
    private final MultiSelectSetting targets = new MultiSelectSetting("Targets", "\u0426\u0435\u043b\u0438 \u0434\u043b\u044f \u043b\u0443\u043a\u0430 \u0438 \u0430\u0440\u0431\u0430\u043b\u0435\u0442\u0430").value("Players", "Mobs", "Animals", "Friends", "Naked").selected("Players");
    private final MultiSelectSetting weapons = new MultiSelectSetting("Weapons", "\u041e\u0440\u0443\u0436\u0438\u0435 \u0434\u043b\u044f \u043d\u0430\u0432\u0435\u0434\u0435\u043d\u0438\u044f").value("Bow", "Crossbow", "Trident").selected("Bow", "Crossbow", "Trident");
    private final ValueSetting range = new ValueSetting("Range", "\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f \u043d\u0430\u0432\u0435\u0434\u0435\u043d\u0438\u044f").setValue(40.0f).range(10.0f, 100.0f).step(1.0f);
    private final ValueSetting aimTime = new ValueSetting("Aim Time", "\u0412\u0440\u0435\u043c\u044f \u0434\u043e\u0432\u043e\u0434\u043a\u0438 \u0432 \u0442\u0438\u043a\u0430\u0445").setValue(10.0f).range(0.0f, 40.0f).step(1.0f);
    private final ValueSetting prediction = new ValueSetting("Prediction", "Extra lead ticks on top of projectile flight time").setValue(2.0f).range(0.0f, 10.0f).step(0.25f);
    private final BooleanSetting silent = new BooleanSetting("Silent", "\u0422\u0438\u0445\u0438\u0439 \u043f\u043e\u0432\u043e\u0440\u043e\u0442").setValue(true);
    private final BooleanSetting showTarget = new BooleanSetting("Show Target", "Show a marker at the current aim point").setValue(true);
    private transient class_1309 target;
    private transient float progress;
    private transient class_243 aimPoint;

    public AimBot() {
        super("AimBot", "Aim Bot", ModuleCategory.COMBAT);
        this.setup(this.targets, this.weapons, this.range, this.aimTime, this.prediction, this.silent, this.showTarget);
    }

    @Override
    public void deactivate() {
        this.target = null;
        this.aimPoint = null;
        this.progress = 0.0f;
        RotationController.INSTANCE.release(this);
        super.deactivate();
    }

    @EventHandler
    public void onRotation(RotationUpdateEvent event) {
        TargetShot selected;
        class_1309 found;
        Angle current;
        if (event.getType() != 0) {
            return;
        }
        if (AimBot.mc.field_1724 == null || AimBot.mc.field_1687 == null || this.weapon().method_7960()) {
            this.target = null;
            this.aimPoint = null;
            this.progress = 0.0f;
            RotationController.INSTANCE.release(this);
            return;
        }
        Angle angle = current = this.silent.isValue() ? RotationController.INSTANCE.getRotation() : AngleUtil.cameraAngle();
        if (current == null) {
            current = AngleUtil.cameraAngle();
        }
        class_1309 class_13092 = found = (selected = this.findTarget(current)) == null ? null : selected.target();
        if (this.target != found) {
            this.target = found;
            this.progress = 0.0f;
        }
        if (this.target == null) {
            this.aimPoint = null;
            RotationController.INSTANCE.release(this);
            return;
        }
        this.progress = Math.min(1.0f, this.progress + 1.0f / Math.max(1.0f, this.aimTime.getValue()));
        Shot shot = selected.shot();
        class_243 class_2432 = this.aimPoint = shot == null ? null : shot.point();
        if (shot == null) {
            RotationController.INSTANCE.release(this);
            return;
        }
        Angle desired = shot.angle();
        float desiredYaw = desired.getYaw();
        float desiredPitch = desired.getPitch();
        float yaw = current.getYaw() + class_3532.method_15393((float)(desiredYaw - current.getYaw())) * this.progress;
        float pitch = class_3532.method_15363((float)(current.getPitch() + (desiredPitch - current.getPitch()) * this.progress), (float)-90.0f, (float)90.0f);
        if (this.silent.isValue()) {
            Aura aura = Aura.getInstance();
            TaskPriority priority = aura != null && aura.isState() && aura.getTarget() != null ? TaskPriority.STANDARD : TaskPriority.HIGH_IMPORTANCE_2;
            RotationController.INSTANCE.rotateTo(new Angle(yaw, pitch), 1, new RotationConfig(true, true), priority, this);
        } else {
            RotationController.INSTANCE.release(this);
            AimBot.mc.field_1724.method_36456(yaw);
            AimBot.mc.field_1724.method_36457(pitch);
        }
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (!this.showTarget.isValue() || this.target == null || this.aimPoint == null || AimBot.mc.field_1724 == null) {
            return;
        }
        class_243 center = this.aimPoint;
        class_238 marker = new class_238(center.field_1352 - 0.08, center.field_1351 - 0.08, center.field_1350 - 0.08, center.field_1352 + 0.08, center.field_1351 + 0.08, center.field_1350 + 0.08);
        Render3DUtil.drawBox(marker, ColorUtil.getClientColor(), 1.0f, true, false, false);
    }

    private class_1799 weapon() {
        if (AimBot.mc.field_1724.method_6115()) {
            class_1799 active = AimBot.mc.field_1724.method_6030();
            if (active.method_7909() instanceof class_1753 && this.weapons.isSelected("Bow") || active.method_7909() instanceof class_1764 && this.weapons.isSelected("Crossbow")) {
                return active;
            }
            if (active.method_7909() instanceof class_1835 && this.weapons.isSelected("Trident") && AimBot.mc.field_1724.method_6048() >= 10 && class_1890.method_60123((class_1799)active, (class_1309)AimBot.mc.field_1724) <= 0.0f) {
                return active;
            }
            return class_1799.field_8037;
        }
        for (class_1799 stack : new class_1799[]{AimBot.mc.field_1724.method_6047(), AimBot.mc.field_1724.method_6079()}) {
            if (!this.weapons.isSelected("Crossbow") || !(stack.method_7909() instanceof class_1764) || !class_1764.method_7781((class_1799)stack)) continue;
            return stack;
        }
        return class_1799.field_8037;
    }

    private TargetShot findTarget(Angle current) {
        ArrayList<class_1309> candidates = new ArrayList<class_1309>();
        for (class_1297 entity2 : AimBot.mc.field_1687.method_18112()) {
            class_1309 living;
            if (!(entity2 instanceof class_1309) || !this.valid(living = (class_1309)entity2) || !(AimBot.mc.field_1724.method_5858((class_1297)living) <= (double)(this.range.getValue() * this.range.getValue()))) continue;
            candidates.add(living);
        }
        candidates.sort(Comparator.comparingDouble(entity -> AimBot.mc.field_1724.method_5858((class_1297)entity) * (entity == this.target ? 0.9 : 1.0)));
        for (int i = 0; i < Math.min(4, candidates.size()); ++i) {
            class_1309 candidate = (class_1309)candidates.get(i);
            Shot shot = this.findAimPoint(candidate, current);
            if (shot == null) continue;
            return new TargetShot(candidate, shot);
        }
        return null;
    }

    private boolean valid(class_1309 living) {
        if (living == AimBot.mc.field_1724 || !living.method_5805() || living.method_7325()) {
            return false;
        }
        if (living instanceof class_1657) {
            class_1657 player = (class_1657)living;
            if (FriendUtils.isFriend((class_1297)player)) {
                return this.targets.isSelected("Friends");
            }
            boolean naked = true;
            for (class_1799 stack : EntityCompat.getArmorItems((class_1309)player)) {
                if (stack.method_7960()) continue;
                naked = false;
                break;
            }
            return this.targets.isSelected("Players") || naked && this.targets.isSelected("Naked");
        }
        if (living instanceof class_1429) {
            return this.targets.isSelected("Animals");
        }
        return living instanceof class_1308 && this.targets.isSelected("Mobs");
    }

    private Shot findAimPoint(class_1309 entity, Angle current) {
        class_640 entry;
        double speed;
        boolean rocket;
        class_1799 stack = this.weapon();
        boolean bow = stack.method_7909() instanceof class_1753;
        boolean trident = stack.method_7909() instanceof class_1835;
        class_9278 projectiles = (class_9278)stack.method_58694(class_9334.field_49649);
        boolean bl = rocket = !bow && projectiles != null && projectiles.method_57438(class_1802.field_8639);
        if (bow) {
            speed = (double)class_1753.method_7722((int)AimBot.mc.field_1724.method_6048()) * 3.0;
        } else if (trident) {
            speed = 2.5;
        } else {
            speed = rocket ? 1.6 : 3.15;
        }
        if (speed < 0.3) {
            return null;
        }
        double drag = rocket ? 1.0 : (double)0.99f;
        double gravity = rocket ? 0.0 : 0.05;
        class_243 origin = AimBot.mc.field_1724.method_33571().method_1031(0.0, rocket ? -0.15 : -0.1, 0.0);
        class_243 movement = AimBot.mc.field_1724.method_60478();
        class_243 inherited = bow || trident ? new class_243(movement.field_1352, AimBot.mc.field_1724.method_24828() ? 0.0 : movement.field_1351, movement.field_1350) : class_243.field_1353;
        double lead = this.prediction.getValue();
        if (mc.method_1562() != null && (entry = mc.method_1562().method_2871(AimBot.mc.field_1724.method_5667())) != null) {
            lead += class_3532.method_15350((double)((double)entry.method_2959() / 100.0), (double)0.0, (double)4.0);
        }
        class_243[] path = CombatMotionPrediction.path(entity, 96);
        class_238 box = entity.method_5829();
        double[][] samples = new double[][]{{0.5, 0.6, 0.5}, {0.5, 0.78, 0.5}, {0.5, 0.38, 0.5}, {0.25, 0.6, 0.5}, {0.75, 0.6, 0.5}, {0.5, 0.6, 0.25}, {0.5, 0.6, 0.75}, {0.5, 0.88, 0.5}, {0.5, 0.22, 0.5}};
        Shot best = null;
        double bestScore = Double.POSITIVE_INFINITY;
        block0: for (double[] sample : samples) {
            class_243 offset = new class_243(class_3532.method_16436((double)sample[0], (double)box.field_1323, (double)box.field_1320), class_3532.method_16436((double)sample[1], (double)box.field_1322, (double)box.field_1325), class_3532.method_16436((double)sample[2], (double)box.field_1321, (double)box.field_1324)).method_1020(path[0]);
            double lower = 0.05;
            for (double upper = 0.5; upper <= 80.0; upper += 0.5) {
                class_243 needed = this.launchVelocity(origin, CombatMotionPrediction.at(path, upper + lead).method_1019(offset), inherited, upper, drag, gravity);
                if (!(needed.method_1027() > speed * speed)) {
                    double high = upper;
                    for (int iteration = 0; iteration < 16; ++iteration) {
                        double middle = (lower + high) * 0.5;
                        class_243 test = this.launchVelocity(origin, CombatMotionPrediction.at(path, middle + lead).method_1019(offset), inherited, middle, drag, gravity);
                        if (test.method_1027() > speed * speed) {
                            lower = middle;
                            continue;
                        }
                        high = middle;
                    }
                    double time = (lower + high) * 0.5;
                    class_243 point = CombatMotionPrediction.at(path, time + lead).method_1019(offset);
                    class_243 launch = this.launchVelocity(origin, point, inherited, time, drag, gravity).method_1029().method_1021(speed);
                    if (!this.clearTrajectory(entity, origin, launch.method_1019(inherited), path, lead, time, drag, gravity)) continue block0;
                    Angle angle = AngleUtil.fromVec3d(launch);
                    double turn = Math.abs(class_3532.method_15393((float)(angle.getYaw() - current.getYaw()))) + Math.abs(angle.getPitch() - current.getPitch());
                    double score = Math.abs(sample[1] - 0.6) * 4.0 + Math.abs(sample[0] - 0.5) + Math.abs(sample[2] - 0.5) + time * 0.015 + turn * 0.002;
                    if (!(score < bestScore)) continue block0;
                    bestScore = score;
                    best = new Shot(angle, point);
                    continue block0;
                }
                lower = upper;
            }
        }
        return best;
    }

    private class_243 launchVelocity(class_243 origin, class_243 point, class_243 inherited, double time, double drag, double gravity) {
        int ticks = (int)time;
        double fraction = time - (double)ticks;
        double decay = Math.pow(drag, ticks);
        double sum = drag == 1.0 ? time : (1.0 - decay) / (1.0 - drag) + fraction * decay;
        double drop = drag == 1.0 ? gravity * ((double)(ticks * (ticks - 1)) * 0.5 + fraction * (double)ticks) : gravity * (time - sum) / (1.0 - drag);
        return point.method_1020(origin).method_1031(0.0, drop, 0.0).method_1021(1.0 / sum).method_1020(inherited);
    }

    private boolean clearTrajectory(class_1309 entity, class_243 position, class_243 velocity, class_243[] path, double lead, double flightTime, double drag, double gravity) {
        class_238 interior = entity.method_5829().method_1011(0.035);
        int tick = 0;
        while ((double)tick <= Math.ceil(flightTime)) {
            class_243 next = position.method_1019(velocity);
            class_243 fromOffset = CombatMotionPrediction.at(path, (double)tick + lead).method_1020(path[0]);
            class_243 toOffset = CombatMotionPrediction.at(path, (double)(tick + 1) + lead).method_1020(path[0]);
            class_243 relativeStart = position.method_1020(fromOffset);
            class_243 relativeEnd = next.method_1020(toOffset);
            Optional intersection = interior.method_992(relativeStart, relativeEnd);
            double hitFraction = interior.method_1006(relativeStart) ? 0.0 : (intersection.isPresent() ? relativeStart.method_1022((class_243)intersection.get()) / Math.max(1.0E-9, relativeStart.method_1022(relativeEnd)) : 2.0);
            class_3965 block = RaytracingUtil.raycast(position, next, class_3959.class_3960.field_17558, (class_1297)AimBot.mc.field_1724);
            double blockFraction = block.method_17783() == class_239.class_240.field_1333 ? 2.0 : position.method_1022(block.method_17784()) / Math.max(1.0E-9, position.method_1022(next));
            for (class_1297 obstacle : AimBot.mc.field_1687.method_8333((class_1297)AimBot.mc.field_1724, new class_238(position, next).method_1014(0.3), other -> other != entity && other.method_5805() && other.method_5863())) {
                class_238 obstacleBox = obstacle.method_5829();
                Optional obstacleHit = obstacleBox.method_992(position, next);
                double d = obstacleBox.method_1006(position) ? 0.0 : (obstacleHit.isPresent() ? position.method_1022((class_243)obstacleHit.get()) / Math.max(1.0E-9, position.method_1022(next)) : 2.0);
                double fraction = d;
                if (!(fraction <= 1.0) || !(fraction <= Math.min(hitFraction, blockFraction))) continue;
                return false;
            }
            if (hitFraction <= 1.0 && hitFraction < blockFraction) {
                return true;
            }
            if (blockFraction <= 1.0) {
                return false;
            }
            if (!AimBot.mc.field_1687.method_8316(class_2338.method_49638((class_2374)next)).method_15769()) {
                return false;
            }
            position = next;
            velocity = velocity.method_1021(drag).method_1031(0.0, -gravity, 0.0);
            ++tick;
        }
        return false;
    }

    private record TargetShot(class_1309 target, Shot shot) {
    }

    private record Shot(Angle angle, class_243 point) {
    }
}

