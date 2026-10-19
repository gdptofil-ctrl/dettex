/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1301
 *  net.minecraft.class_1675
 *  net.minecraft.class_1922
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_2374
 *  net.minecraft.class_238
 *  net.minecraft.class_239
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_3965
 *  net.minecraft.class_3966
 *  net.minecraft.class_638
 *  net.minecraft.class_746
 */
package ru.recoin.implement.features.modules.combat.killaura.rotation;

import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.class_1297;
import net.minecraft.class_1301;
import net.minecraft.class_1675;
import net.minecraft.class_1922;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2374;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_638;
import net.minecraft.class_746;
import ru.recoin.common.QuickImports;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.combat.killaura.attack.AttackPerpetrator;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;

public final class RaytracingUtil
implements QuickImports {
    public static class_3965 raycast(double range, Angle angle, boolean includeFluids) {
        return RaytracingUtil.raycast(Objects.requireNonNull(RaytracingUtil.mc.field_1724).method_5836(1.0f), range, angle, includeFluids);
    }

    public static class_3965 raycast(class_243 vec, double range, Angle angle, boolean includeFluids) {
        class_1297 entity = mc.method_1560();
        if (entity == null) {
            return null;
        }
        class_243 rotationVec = angle.toVector();
        class_243 end = vec.method_1031(rotationVec.field_1352 * range, rotationVec.field_1351 * range, rotationVec.field_1350 * range);
        class_638 world = RaytracingUtil.mc.field_1687;
        if (world == null) {
            return null;
        }
        class_3959.class_242 fluidHandling = includeFluids ? class_3959.class_242.field_1347 : class_3959.class_242.field_1348;
        class_3959 context = new class_3959(vec, end, class_3959.class_3960.field_17559, fluidHandling, entity);
        return world.method_17742(context);
    }

    public static class_3965 raycast(class_243 start, class_243 end, class_3959.class_3960 shapeType) {
        return RaytracingUtil.raycast(start, end, shapeType, (class_1297)RaytracingUtil.mc.field_1724);
    }

    public static class_3965 raycast(class_243 start, class_243 end, class_3959.class_3960 shapeType, class_1297 entity) {
        return RaytracingUtil.raycast(start, end, shapeType, class_3959.class_242.field_1348, entity);
    }

    public static class_3965 raycast(class_243 start, class_243 end, class_3959.class_3960 shapeType, class_3959.class_242 fluidHandling, class_1297 entity) {
        return RaytracingUtil.mc.field_1687.method_17742(new class_3959(start, end, shapeType, fluidHandling, entity));
    }

    public static class_3966 raytraceEntity(double range, Angle angle, Predicate<class_1297> filter) {
        class_746 entity = RaytracingUtil.mc.field_1724;
        if (entity == null) {
            return null;
        }
        class_243 cameraVec = entity.method_5836(1.0f);
        class_243 rotationVec = angle.toVector();
        class_243 vec3d3 = cameraVec.method_1031(rotationVec.field_1352 * range, rotationVec.field_1351 * range, rotationVec.field_1350 * range);
        class_238 box = entity.method_5829().method_18804(rotationVec.method_1021(range)).method_1009(1.0, 1.0, 1.0);
        return class_1675.method_18075((class_1297)entity, (class_243)cameraVec, (class_243)vec3d3, (class_238)box, e -> !e.method_7325() && filter.test((class_1297)e), (double)(range * range));
    }

    public static class_239 raycastCrosshair(class_1297 entity, double blockRange, double entityRange, float tickDelta, Angle angle) {
        class_243 direction;
        class_243 end;
        if (entity == null || RaytracingUtil.mc.field_1687 == null) {
            return null;
        }
        double range = Math.max(blockRange, entityRange);
        class_243 start = entity.method_5836(tickDelta);
        class_3965 blockHit = RaytracingUtil.mc.field_1687.method_17742(new class_3959(start, end = start.method_1019((direction = angle.toVector()).method_1021(range)), class_3959.class_3960.field_17559, class_3959.class_242.field_1348, entity));
        double blockDistanceSq = blockHit.method_17783() == class_239.class_240.field_1333 ? range * range : start.method_1025(blockHit.method_17784());
        class_238 searchBox = entity.method_5829().method_18804(direction.method_1021(range)).method_1014(1.0);
        class_3966 entityHit = class_1675.method_18075((class_1297)entity, (class_243)start, (class_243)end, (class_238)searchBox, (Predicate)class_1301.field_52443, (double)Math.min(blockDistanceSq, entityRange * entityRange));
        if (entityHit != null && start.method_1025(entityHit.method_17784()) < blockDistanceSq) {
            return entityHit;
        }
        if (blockHit.method_17783() != class_239.class_240.field_1333 && blockDistanceSq <= blockRange * blockRange) {
            return blockHit;
        }
        class_243 missPos = start.method_1019(direction.method_1021(blockRange));
        return class_3965.method_17778((class_243)missPos, (class_2350)class_2350.method_58251((class_243)direction).method_10153(), (class_2338)class_2338.method_49638((class_2374)missPos));
    }

    public static boolean rayTrace(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        Angle angle = Aura.getInstance().isLegitRotationActive() ? AngleUtil.cameraAngle() : RotationController.INSTANCE.getRotation();
        return RaytracingUtil.rayTrace(angle.toVector(), config.getMaximumRange(), config.getBox(), config.isPartialBlocksOnly());
    }

    public static boolean rayTrace(double range, class_238 box) {
        return RaytracingUtil.rayTrace(RotationController.INSTANCE.getRotation().toVector(), range, box);
    }

    public static boolean rayTrace(double range, class_238 box, boolean onlyThroughPartialBlocks) {
        return RaytracingUtil.rayTrace(RotationController.INSTANCE.getRotation().toVector(), range, box, onlyThroughPartialBlocks);
    }

    public static boolean rayTrace(class_243 clientVec, double range, class_238 box) {
        class_243 cameraVec = Objects.requireNonNull(RaytracingUtil.mc.field_1724).method_33571();
        return box.method_1006(cameraVec) || box.method_992(cameraVec, cameraVec.method_1019(clientVec.method_1021(range))).isPresent();
    }

    public static boolean rayTrace(class_243 clientVec, double range, class_238 box, boolean onlyThroughPartialBlocks) {
        class_243 cameraVec = Objects.requireNonNull(RaytracingUtil.mc.field_1724).method_33571();
        class_243 end = cameraVec.method_1019(clientVec.method_1021(range));
        if (box.method_1006(cameraVec)) {
            return true;
        }
        class_243 hitPoint = box.method_992(cameraVec, end).orElse(null);
        return hitPoint != null && (!onlyThroughPartialBlocks || RaytracingUtil.canSeeThroughPartialBlocks(cameraVec, hitPoint));
    }

    public static boolean canSeeThroughPartialBlocks(class_243 start, class_243 end) {
        class_243 direction = end.method_1020(start);
        double length = direction.method_1033();
        if (length < 1.0E-6 || RaytracingUtil.mc.field_1687 == null) {
            return true;
        }
        direction = direction.method_1021(1.0 / length);
        class_243 cursor = start;
        class_2338 previous = null;
        for (int i = 0; i < 16; ++i) {
            class_3965 hit = RaytracingUtil.mc.field_1687.method_17742(new class_3959(cursor, end, class_3959.class_3960.field_17558, class_3959.class_242.field_1348, (class_1297)RaytracingUtil.mc.field_1724));
            if (hit.method_17783() == class_239.class_240.field_1333) {
                return true;
            }
            class_2338 position = hit.method_17777();
            if (RaytracingUtil.mc.field_1687.method_8320(position).method_26234((class_1922)RaytracingUtil.mc.field_1687, position)) {
                return false;
            }
            class_243 next = hit.method_17784().method_1019(direction.method_1021(0.01));
            if (position.equals(previous)) {
                next = cursor.method_1019(direction.method_1021(0.0625));
            }
            if (next.method_1025(end) >= start.method_1025(end)) {
                return true;
            }
            previous = position;
            cursor = next;
        }
        return false;
    }

    private RaytracingUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

