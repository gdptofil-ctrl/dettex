/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_238
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_3545
 *  net.minecraft.class_3959$class_3960
 */
package ru.recoin.implement.features.modules.combat.killaura.rotation;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_3545;
import net.minecraft.class_3959;
import ru.recoin.common.QuickImports;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RaytracingUtil;
import ru.recoin.implement.features.modules.combat.killaura.target.BacktrackPositionResolver;

public class PointFinder
implements QuickImports {
    private final Random random = new SecureRandom();
    private class_243 offset = class_243.field_1353;

    public class_3545<class_243, class_238> computeVector(class_1309 entity, float maxDistance, Angle initialAngle, class_243 velocity, boolean ignoreWalls) {
        return this.computeVector(entity, maxDistance, initialAngle, velocity, ignoreWalls, false);
    }

    public class_3545<class_243, class_238> computeVector(class_1309 entity, float maxDistance, Angle initialAngle, class_243 velocity, boolean ignoreWalls, boolean onlyThroughPartialBlocks) {
        class_3545<List<class_243>, class_238> candidatePoints = this.generateCandidatePoints(entity, maxDistance, ignoreWalls, onlyThroughPartialBlocks);
        class_243 bestVector = this.findBestVector((List)candidatePoints.method_15442(), initialAngle);
        this.updateOffset(velocity);
        return new class_3545((Object)(bestVector == null ? BacktrackPositionResolver.eyePosition((class_1297)entity) : bestVector).method_1019(this.offset), (Object)((class_238)candidatePoints.method_15441()));
    }

    public class_3545<class_243, class_238> computeMultiPointVector(class_1309 entity, float maxDistance, Angle initialAngle, class_243 velocity, boolean ignoreWalls) {
        return this.computeMultiPointVector(entity, maxDistance, initialAngle, velocity, ignoreWalls, false);
    }

    public class_3545<class_243, class_238> computeMultiPointVector(class_1309 entity, float maxDistance, Angle initialAngle, class_243 velocity, boolean ignoreWalls, boolean onlyThroughPartialBlocks) {
        class_3545<List<class_243>, class_238> candidatePoints = this.generateMultiPointCandidatePoints(entity, maxDistance, ignoreWalls, onlyThroughPartialBlocks);
        class_243 bestVector = this.findBestVector((List)candidatePoints.method_15442(), initialAngle);
        this.updateOffset(velocity);
        return new class_3545((Object)(bestVector == null ? BacktrackPositionResolver.eyePosition((class_1297)entity) : bestVector).method_1019(this.offset), (Object)((class_238)candidatePoints.method_15441()));
    }

    public class_3545<class_243, class_238> computeRandomMultiPointVector(class_1309 entity, float maxDistance, boolean ignoreWalls) {
        class_3545<List<class_243>, class_238> candidates = this.generateMultiPointCandidatePoints(entity, maxDistance, ignoreWalls);
        List points = (List)candidates.method_15442();
        class_243 point = points.isEmpty() ? BacktrackPositionResolver.eyePosition((class_1297)entity) : (class_243)points.get(this.random.nextInt(points.size()));
        return new class_3545((Object)point, (Object)((class_238)candidates.method_15441()));
    }

    public class_3545<class_243, class_238> computeBodyPartVector(class_1309 entity, float maxDistance, Angle initialAngle, boolean ignoreWalls, float yFractionMin, float yFractionMax, double lateralBias) {
        class_238 entityBox = BacktrackPositionResolver.boundingBox((class_1297)entity);
        class_243 eye = PointFinder.mc.field_1724.method_33571();
        class_243 best = null;
        double bestDifference = Double.POSITIVE_INFINITY;
        for (int i = 0; i < 22; ++i) {
            double y = entityBox.field_1322 + entityBox.method_17940() * ((double)yFractionMin + this.random.nextDouble() * (double)(yFractionMax - yFractionMin));
            double x;
            double z;
            if (lateralBias != 0.0) {
                double side = lateralBias > 0.0 ? entityBox.field_1320 : entityBox.field_1323;
                x = side + lateralBias * entityBox.method_17939() * 0.18;
                z = entityBox.field_1321 + this.random.nextDouble() * entityBox.method_17941();
            } else {
                x = entityBox.field_1323 + this.random.nextDouble() * entityBox.method_17939();
                z = entityBox.field_1321 + this.random.nextDouble() * entityBox.method_17941();
            }
            class_243 point = new class_243(this.clamp(x, entityBox.field_1323, entityBox.field_1320), this.clamp(y, entityBox.field_1322, entityBox.field_1325), this.clamp(z, entityBox.field_1321, entityBox.field_1324));
            if (!this.isValidPoint(eye, point, maxDistance, ignoreWalls, false)) continue;
            double difference = this.calculateRotationDifference(eye, point, initialAngle);
            if (!(difference < bestDifference)) continue;
            bestDifference = difference;
            best = point;
        }
        return new class_3545((Object)(best == null ? BacktrackPositionResolver.eyePosition((class_1297)entity) : best), (Object)entityBox);
    }

    public class_3545<class_243, class_238> computeSnapHitVector(class_1309 entity, float maxDistance, Angle cameraAngle, boolean ignoreWalls) {
        return this.computeSnapHitVector(entity, maxDistance, cameraAngle, ignoreWalls, false);
    }

    public class_3545<class_243, class_238> computeSnapHitVector(class_1309 entity, float maxDistance, Angle cameraAngle, boolean ignoreWalls, boolean onlyThroughPartialBlocks) {
        class_238 entityBox = BacktrackPositionResolver.boundingBox((class_1297)entity);
        class_243 eyePos = PointFinder.mc.field_1724.method_33571();
        class_243 endPos = eyePos.method_1019(cameraAngle.toVector().method_1021((double)maxDistance));
        return entityBox.method_992(eyePos, endPos).filter(point -> this.isValidPoint(eyePos, (class_243)point, maxDistance, ignoreWalls, onlyThroughPartialBlocks)).map(point -> new class_3545(point, (Object)entityBox)).orElseGet(() -> this.computeVector(entity, maxDistance, cameraAngle, class_243.field_1353, ignoreWalls, onlyThroughPartialBlocks));
    }

    public class_3545<List<class_243>, class_238> generateCandidatePoints(class_1309 entity, float maxDistance, boolean ignoreWalls) {
        return this.generateCandidatePoints(entity, maxDistance, ignoreWalls, false);
    }

    public class_3545<List<class_243>, class_238> generateCandidatePoints(class_1309 entity, float maxDistance, boolean ignoreWalls, boolean onlyThroughPartialBlocks) {
        class_238 entityBox = BacktrackPositionResolver.boundingBox((class_1297)entity);
        double stepY = entityBox.method_17940() / 10.0;
        ArrayList<class_243> list = new ArrayList<class_243>(11);
        class_243 center = entityBox.method_1005();
        class_243 eye = PointFinder.mc.field_1724.method_33571();
        if (stepY > 0.0) {
            for (double y = entityBox.field_1322; y <= entityBox.field_1325; y += stepY) {
                class_243 point = new class_243(center.field_1352, y, center.field_1350);
                if (!this.isValidPoint(eye, point, maxDistance, ignoreWalls, onlyThroughPartialBlocks)) continue;
                list.add(point);
            }
        }
        return new class_3545(list, (Object)entityBox);
    }

    public class_3545<List<class_243>, class_238> generateMultiPointCandidatePoints(class_1309 entity, float maxDistance, boolean ignoreWalls) {
        return this.generateMultiPointCandidatePoints(entity, maxDistance, ignoreWalls, false);
    }

    public class_3545<List<class_243>, class_238> generateMultiPointCandidatePoints(class_1309 entity, float maxDistance, boolean ignoreWalls, boolean onlyThroughPartialBlocks) {
        class_238 entityBox = BacktrackPositionResolver.boundingBox((class_1297)entity);
        double stepX = Math.max(entityBox.method_17939() / 5.0, 0.03);
        double stepY = Math.max(entityBox.method_17940() / 14.0, 0.03);
        double stepZ = Math.max(entityBox.method_17941() / 5.0, 0.03);
        double minAimY = entityBox.field_1322 + entityBox.method_17940() * 0.22;
        ArrayList<class_243> list = new ArrayList<class_243>();
        class_243 eye = PointFinder.mc.field_1724.method_33571();
        for (double x = entityBox.field_1323; x <= entityBox.field_1320 + 1.0E-4; x += stepX) {
            for (double y = minAimY; y <= entityBox.field_1325 + 1.0E-4; y += stepY) {
                for (double z = entityBox.field_1321; z <= entityBox.field_1324 + 1.0E-4; z += stepZ) {
                    class_243 point = new class_243(this.clamp(x, entityBox.field_1323, entityBox.field_1320), this.clamp(y, minAimY, entityBox.field_1325), this.clamp(z, entityBox.field_1321, entityBox.field_1324));
                    if (!this.isValidPoint(eye, point, maxDistance, ignoreWalls, onlyThroughPartialBlocks)) continue;
                    list.add(point);
                }
            }
        }
        return new class_3545(list, (Object)entityBox);
    }

    public boolean hasValidPoint(class_1309 entity, float maxDistance, boolean ignoreWalls) {
        return this.hasValidPoint(entity, maxDistance, ignoreWalls, false);
    }

    public boolean hasValidPoint(class_1309 entity, float maxDistance, boolean ignoreWalls, boolean onlyThroughPartialBlocks) {
        class_238 entityBox = BacktrackPositionResolver.boundingBox((class_1297)entity);
        double stepY = entityBox.method_17940() / 10.0;
        if (stepY <= 0.0) {
            return false;
        }
        class_243 center = entityBox.method_1005();
        class_243 eye = PointFinder.mc.field_1724.method_33571();
        for (double y = entityBox.field_1322; y < entityBox.field_1325; y += stepY) {
            if (!this.isValidPoint(eye, new class_243(center.field_1352, y, center.field_1350), maxDistance, ignoreWalls, onlyThroughPartialBlocks)) continue;
            return true;
        }
        return false;
    }

    private boolean isValidPoint(class_243 startPoint, class_243 endPoint, float maxDistance, boolean ignoreWalls, boolean onlyThroughPartialBlocks) {
        if (maxDistance < 0.0f || startPoint.method_1025(endPoint) > (double)maxDistance * (double)maxDistance) {
            return false;
        }
        if (!ignoreWalls) {
            return RaytracingUtil.raycast(startPoint, endPoint, class_3959.class_3960.field_17558).method_17783() != class_239.class_240.field_1332;
        }
        return !onlyThroughPartialBlocks || RaytracingUtil.canSeeThroughPartialBlocks(startPoint, endPoint);
    }

    private class_243 findBestVector(List<class_243> candidatePoints, Angle initialAngle) {
        class_243 eye = PointFinder.mc.field_1724.method_33571();
        class_243 best = null;
        double bestDifference = Double.POSITIVE_INFINITY;
        for (class_243 point : candidatePoints) {
            double difference = this.calculateRotationDifference(eye, point, initialAngle);
            if (!(difference < bestDifference)) continue;
            bestDifference = difference;
            best = point;
        }
        return best;
    }

    private double calculateRotationDifference(class_243 startPoint, class_243 endPoint, Angle initialAngle) {
        Angle targetAngle = AngleUtil.fromVec3d(endPoint.method_1020(startPoint));
        Angle delta = AngleUtil.calculateDelta(initialAngle, targetAngle);
        return Math.hypot(delta.getYaw(), delta.getPitch());
    }

    private void updateOffset(class_243 velocity) {
        if (velocity.method_1027() == 0.0) {
            this.offset = class_243.field_1353;
            return;
        }
        this.offset = this.offset.method_1031(this.random.nextGaussian(), this.random.nextGaussian(), this.random.nextGaussian()).method_18806(velocity);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public Random getRandom() {
        return this.random;
    }

    public class_243 getOffset() {
        return this.offset;
    }
}

