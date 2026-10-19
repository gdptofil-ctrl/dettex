/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1308
 *  net.minecraft.class_1309
 *  net.minecraft.class_1429
 *  net.minecraft.class_1657
 *  net.minecraft.class_1743
 *  net.minecraft.class_1792
 *  net.minecraft.class_238
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_3489
 *  net.minecraft.class_3532
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_9362
 */
package ru.recoin.implement.features.modules.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import net.minecraft.class_1297;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_1429;
import net.minecraft.class_1657;
import net.minecraft.class_1743;
import net.minecraft.class_1792;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_3489;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_9362;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.keyboard.MouseRotationEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.modules.combat.TriggerBot;

public class AimAssist
extends Module {
    private static final double[] STEPS = new double[]{0.0, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875, 1.0};
    private final SelectSetting mode = new SelectSetting("Mode", "Aim assist mode").value("Default", "FunTime").selected("Default");
    private final MultiSelectSetting targets = new MultiSelectSetting("Targets", "Entities that may be selected").value("Players", "Animals", "Mobs", "Friends").selected("Players", "Friends");
    private final BooleanSetting throughWalls = new BooleanSetting("Through Walls", "Aim through blocks").setValue(false);
    private final ValueSetting defaultSpeed = new ValueSetting("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c", "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0434\u043e\u0432\u043e\u0434\u043a\u0438 \u043a\u0430\u043c\u0435\u0440\u044b").setValue(0.08f).range(0.02f, 0.4f).step(0.01f).visible(() -> this.mode.isSelected("Default"));
    private final ValueSetting funtimeSpeed = new ValueSetting("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c", "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0434\u043e\u0432\u043e\u0434\u043a\u0438 \u043a\u0430\u043c\u0435\u0440\u044b").setValue(4.0f).range(1.0f, 10.0f).step(0.25f).visible(() -> this.mode.isSelected("FunTime"));
    private final BooleanSetting onlyWeapon = new BooleanSetting("Only Weapon", "Work only while holding a weapon").setValue(true);
    private class_1309 target;
    private class_243 smoothedDirection;
    private long lastAimNano;

    public AimAssist() {
        super("AimAssist", "Aim Assist", ModuleCategory.COMBAT);
        this.setup(this.mode, this.targets, this.throughWalls, this.defaultSpeed, this.funtimeSpeed, this.onlyWeapon);
    }

    public static AimAssist getInstance() {
        return Instance.get(AimAssist.class);
    }

    public class_1309 getTarget() {
        return this.target;
    }

    @Override
    public void deactivate() {
        this.target = null;
        this.lastAimNano = 0L;
        super.deactivate();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        class_1309 found;
        if (AimAssist.mc.field_1724 == null || AimAssist.mc.field_1687 == null) {
            this.target = null;
            this.lastAimNano = 0L;
            return;
        }
        class_1309 triggerTarget = TriggerBot.getInstance().isState() ? TriggerBot.getInstance().getTarget() : null;
        class_1309 class_13092 = found = triggerTarget != null ? triggerTarget : this.findTarget();
        if (found != this.target) {
            this.smoothedDirection = null;
        }
        this.target = found;
    }

    @EventHandler
    public void onLook(MouseRotationEvent event) {
        if (AimAssist.mc.field_1724 == null || AimAssist.mc.field_1687 == null || !this.isValid(this.target) || AimAssist.mc.field_1724.method_6115()) {
            return;
        }
        if (this.onlyWeapon.isValue() && !this.isHoldingWeapon()) {
            return;
        }
        float frame = mc.method_61966().method_60636();
        if (this.mode.isSelected("Default")) {
            class_243 vec3d = this.target.method_73189().method_1031(0.0, class_3532.method_15350((double)(AimAssist.mc.field_1724.method_33571().field_1351 - this.target.method_23318()), (double)0.0, (double)1.0), 0.0).method_1020(AimAssist.mc.field_1724.method_33571()).method_1029();
            float targetYaw = (float)Math.toDegrees(Math.atan2(-vec3d.field_1352, vec3d.field_1350));
            float currentYaw = AimAssist.mc.field_1724.method_36454();
            float deltaYaw = class_3532.method_15393((float)(targetYaw - currentYaw));
            float speedVal = class_3532.method_15363((float)this.defaultSpeed.getValue(), (float)0.02f, (float)0.4f);
            float delta = this.measureDelta();
            float factor = 1.0f - (float)Math.pow(1.0f - speedVal, delta);
            float newYaw = currentYaw + deltaYaw * factor;
            AimAssist.mc.field_1724.method_36456(newYaw);
            AimAssist.mc.field_1724.field_6241 = newYaw;
            return;
        }
        class_243 direction = this.computeAimDirection(AimAssist.mc.field_1724.method_33571(), this.target, 3.0, this.throughWalls.isValue());
        if (direction == class_243.field_1353) {
            return;
        }
        this.smoothedDirection = this.smoothedDirection == null ? direction : this.smoothedDirection.method_35590(direction, 0.2000000448441151);
        float yaw = (float)class_3532.method_15338((double)(Math.toDegrees(Math.atan2(this.smoothedDirection.field_1350, this.smoothedDirection.field_1352)) - 90.0));
        float pitch = (float)(-Math.toDegrees(Math.atan2(this.smoothedDirection.field_1351, Math.hypot(this.smoothedDirection.field_1352, this.smoothedDirection.field_1350))));
        float deltaYaw = class_3532.method_15393((float)(yaw - AimAssist.mc.field_1724.method_36454()));
        float deltaPitch = pitch - AimAssist.mc.field_1724.method_36455();
        if (Math.abs(deltaPitch) <= 13.0f && Math.abs(deltaYaw) < 8.0f && this.rayHits(AimAssist.mc.field_1724.method_33571(), AimAssist.mc.field_1724.method_36454(), AimAssist.mc.field_1724.method_36455(), 3.0, (class_1297)this.target, this.throughWalls.isValue())) {
            deltaPitch = 0.0f;
        }
        float ease = class_3532.method_15363((float)((float)Math.hypot(deltaYaw, deltaPitch) / 4.0f), (float)0.0f, (float)1.0f);
        float spd = this.funtimeSpeed.getValue() * frame * ease;
        if (spd <= 0.0f) {
            return;
        }
        float step = Math.min(1.0f, spd / Math.max(Math.abs(deltaYaw), Math.abs(deltaPitch) * 2.0f));
        AimAssist.mc.field_1724.method_36456(AimAssist.mc.field_1724.method_36454() + deltaYaw * step);
        if (deltaPitch != 0.0f) {
            AimAssist.mc.field_1724.method_36457(class_3532.method_15363((float)(AimAssist.mc.field_1724.method_36455() + deltaPitch * step), (float)-90.0f, (float)90.0f));
        }
    }

    private class_1309 findTarget() {
        class_243 eye = AimAssist.mc.field_1724.method_33571();
        class_243 look = class_243.method_1030((float)AimAssist.mc.field_1724.method_36455(), (float)AimAssist.mc.field_1724.method_36454());
        return StreamSupport.stream(AimAssist.mc.field_1687.method_18112().spliterator(), false).filter(class_1309.class::isInstance).map(class_1309.class::cast).filter(entity -> this.isValid((class_1309)entity) && (this.throughWalls.isValue() || this.hasVisibleSample(eye, (class_1309)entity, 4.0))).min(Comparator.comparingDouble(entity -> Math.acos(class_3532.method_15350((double)look.method_1026(entity.method_5829().method_1005().method_1020(eye).method_1029()), (double)-1.0, (double)1.0)))).orElse(null);
    }

    private boolean isValid(class_1309 entity) {
        return entity != null && entity.method_5805() && !entity.method_7325() && entity != AimAssist.mc.field_1724 && this.withinBoxReach((class_1297)entity, 4.0 + AimAssist.mc.field_1724.method_18798().method_1033() * 3.0) && this.isSelectedType(entity);
    }

    private boolean isSelectedType(class_1309 entity) {
        if (entity instanceof class_1657) {
            class_1657 player = (class_1657)entity;
            return this.targets.isSelected("Players") && (this.targets.isSelected("Friends") || !FriendUtils.isFriend((class_1297)player));
        }
        if (entity instanceof class_1308) {
            return this.targets.isSelected("Mobs");
        }
        return entity instanceof class_1429 && this.targets.isSelected("Animals");
    }

    private boolean isHoldingWeapon() {
        class_1792 item = AimAssist.mc.field_1724.method_6047().method_7909();
        return AimAssist.mc.field_1724.method_6047().method_31573(class_3489.field_42611) || item instanceof class_1743 || item instanceof class_9362;
    }

    private boolean withinBoxReach(class_1297 entity, double reach) {
        double z;
        double y;
        class_243 eye = AimAssist.mc.field_1724.method_33571();
        class_238 box = entity.method_5829();
        double x = class_3532.method_15350((double)eye.field_1352, (double)box.field_1323, (double)box.field_1320) - eye.field_1352;
        return x * x + (y = class_3532.method_15350((double)eye.field_1351, (double)box.field_1322, (double)box.field_1325) - eye.field_1351) * y + (z = class_3532.method_15350((double)eye.field_1350, (double)box.field_1321, (double)box.field_1324) - eye.field_1350) * z <= reach * reach;
    }

    private boolean rayHits(class_243 origin, float yaw, float pitch, double distance, class_1297 entity, boolean walls) {
        Optional hit;
        class_243 direction = class_243.method_1030((float)pitch, (float)yaw).method_1021(distance);
        class_238 box = entity.method_5829();
        Optional optional = hit = box.method_1006(origin) ? Optional.of(origin) : box.method_992(origin, origin.method_1019(direction));
        if (hit.isEmpty()) {
            return false;
        }
        return walls || AimAssist.mc.field_1687.method_17742(new class_3959(origin, (class_243)hit.get(), class_3959.class_3960.field_17558, class_3959.class_242.field_1348, (class_1297)AimAssist.mc.field_1724)).method_17783() == class_239.class_240.field_1333;
    }

    private boolean hasVisibleSample(class_243 from, class_1309 entity, double reach) {
        class_238 box = entity.method_5829();
        int last = STEPS.length - 1;
        double reachSq = reach * reach;
        for (int x = 0; x <= last; ++x) {
            for (int y = 0; y <= last; ++y) {
                for (int z = 0; z <= last; ++z) {
                    class_243 end;
                    class_243 point;
                    double distanceSq;
                    if (x > 0 && x < last && y > 0 && y < last && z > 0 && z < last || (distanceSq = from.method_1025(point = this.sample(box, x, y, z))) > reachSq || AimAssist.mc.field_1687.method_17742(new class_3959(from, end = point.method_1019(from.method_1020(point).method_1021(0.05000000993895991 / Math.sqrt(distanceSq))), class_3959.class_3960.field_17558, class_3959.class_242.field_1348, (class_1297)AimAssist.mc.field_1724)).method_17783() != class_239.class_240.field_1333) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private class_243 computeAimDirection(class_243 eye, class_1309 entity, double reach, boolean walls) {
        class_238 box = entity.method_5829();
        boolean mace = AimAssist.mc.field_1724 != null && AimAssist.mc.field_1724.method_6047().method_7909() instanceof class_9362;
        class_243 aimEye = !mace || AimAssist.mc.field_1724 == null ? eye : eye.method_1019(AimAssist.mc.field_1724.method_18798());
        class_243 targetEye = entity.method_73189().method_1031(0.0, (double)entity.method_5751(), 0.0);
        double eyeDistance = aimEye.method_1022(targetEye);
        class_243 origin = mace && eyeDistance > 3.0 ? new class_243(aimEye.field_1352, targetEye.field_1351, aimEye.field_1350) : aimEye;
        double blendDistance = mace ? Math.min(eyeDistance, 3.0) : eyeDistance;
        double aimHeight = mace && eyeDistance > 3.0 ? targetEye.field_1351 : aimEye.field_1351;
        double y = class_3532.method_16436((double)class_3532.method_15350((double)(blendDistance / 3.0), (double)0.0, (double)1.0), (double)box.field_1322, (double)class_3532.method_15350((double)aimHeight, (double)box.field_1322, (double)box.field_1325));
        ArrayList<class_243> points = new ArrayList<class_243>();
        points.add(new class_243((box.field_1323 + box.field_1320) * 0.5, y, (box.field_1321 + box.field_1324) * 0.5));
        int last = STEPS.length - 1;
        for (int x = 0; x <= last; ++x) {
            for (int sy = 0; sy <= last; ++sy) {
                for (int z = 0; z <= last; ++z) {
                    if (x != 0 && x != last && sy != 0 && sy != last && z != 0 && z != last) continue;
                    points.add(this.sample(box, x, sy, z));
                }
            }
        }
        for (double padding : new double[]{0.0, 0.20000001551382535}) {
            List<class_243> through;
            List<class_243> visible = this.collect(points, origin, entity, reach, padding, mace, false);
            if (!visible.isEmpty()) {
                return this.closestToCentroid(visible).method_1020(origin);
            }
            if (!walls || (through = this.collect(points, origin, entity, reach, padding, mace, true)).isEmpty()) continue;
            return this.closestToCentroid(through).method_1020(origin);
        }
        return class_243.field_1353;
    }

    private List<class_243> collect(List<class_243> points, class_243 origin, class_1309 entity, double reach, double padding, boolean mace, boolean walls) {
        ArrayList<class_243> result = new ArrayList<class_243>();
        for (class_243 point : points) {
            float pitch;
            class_243 direction = point.method_1020(origin);
            double length = direction.method_1033();
            double limit = reach + padding;
            if (!mace && !(length <= limit)) continue;
            float traceDistance = (float)(mace ? length + padding + 0.010000001417203743 : limit);
            float yaw = (float)class_3532.method_15338((double)(Math.toDegrees(Math.atan2(direction.field_1350, direction.field_1352)) - 90.0));
            if (!this.rayHits(origin, yaw, pitch = (float)(-Math.toDegrees(Math.atan2(direction.field_1351, Math.hypot(direction.field_1352, direction.field_1350)))), traceDistance, (class_1297)entity, walls)) continue;
            result.add(point);
        }
        return result;
    }

    private class_243 closestToCentroid(List<class_243> points) {
        class_243 centroid = points.stream().reduce(class_243.field_1353, class_243::method_1019).method_1021(1.0 / (double)points.size());
        return points.stream().min(Comparator.comparingDouble(point -> point.method_1025(centroid))).orElseThrow();
    }

    private class_243 sample(class_238 box, int x, int y, int z) {
        return new class_243(class_3532.method_16436((double)STEPS[x], (double)box.field_1323, (double)box.field_1320), class_3532.method_16436((double)STEPS[y], (double)box.field_1322, (double)box.field_1325), class_3532.method_16436((double)STEPS[z], (double)box.field_1321, (double)box.field_1324));
    }

    private float measureDelta() {
        long now = System.nanoTime();
        if (this.lastAimNano == 0L) {
            this.lastAimNano = now;
            return 1.0f;
        }
        float delta = (float)(now - this.lastAimNano) / 1.6666667E7f;
        this.lastAimNano = now;
        return class_3532.method_15363((float)delta, (float)0.25f, (float)4.0f);
    }
}

