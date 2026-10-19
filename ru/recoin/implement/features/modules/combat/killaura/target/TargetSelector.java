/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1308
 *  net.minecraft.class_1309
 *  net.minecraft.class_1429
 *  net.minecraft.class_1657
 *  net.minecraft.class_1799
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 */
package ru.recoin.implement.features.modules.combat.killaura.target;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.class_1297;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_1429;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_238;
import net.minecraft.class_243;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.entity.EntityCompat;
import ru.recoin.implement.features.modules.combat.AntiBot;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.PointFinder;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RaytracingUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.combat.killaura.target.BacktrackPositionResolver;

public class TargetSelector
implements QuickImports {
    private final PointFinder pointFinder = new PointFinder();
    private class_1309 currentTarget = null;
    private Stream<class_1309> potentialTargets;
    private class_1309 lockedFovTarget;

    public void lockTarget(class_1309 target) {
        if (this.currentTarget == null) {
            this.currentTarget = target;
        }
    }

    public void releaseTarget() {
        this.currentTarget = null;
    }

    public void validateTarget(Predicate<class_1309> predicate) {
        this.findFirstMatch(predicate).ifPresent(this::lockTarget);
        if (this.currentTarget != null && !predicate.test(this.currentTarget)) {
            this.releaseTarget();
        }
    }

    public void resetFovLock() {
        this.lockedFovTarget = null;
    }

    public void setLockedFovTarget(class_1309 target) {
        this.lockedFovTarget = target;
    }

    public void searchTargets(Iterable<class_1297> entities, float maxDistance, float maxFov, boolean ignoreWalls) {
        this.searchTargets(entities, maxDistance, maxFov, ignoreWalls, "Distance");
    }

    public void searchTargets(Iterable<class_1297> entities, float maxDistance, float maxFov, boolean ignoreWalls, String priority) {
        this.searchTargets(entities, maxDistance, maxFov, ignoreWalls, false, priority);
    }

    public void searchTargets(Iterable<class_1297> entities, float maxDistance, float maxFov, boolean ignoreWalls, boolean onlyThroughPartialBlocks, String priority) {
        this.searchTargets(entities, maxDistance, maxFov, ignoreWalls, onlyThroughPartialBlocks, priority, entity -> true);
    }

    public void searchTargets(Iterable<class_1297> entities, float maxDistance, float maxFov, boolean ignoreWalls, boolean onlyThroughPartialBlocks, String priority, Predicate<class_1309> candidateFilter) {
        if (this.currentTarget != null && (!this.pointFinder.hasValidPoint(this.currentTarget, maxDistance, ignoreWalls, onlyThroughPartialBlocks) || this.getFov(this.currentTarget, maxDistance, ignoreWalls, onlyThroughPartialBlocks) > (double)maxFov)) {
            this.releaseTarget();
        }
        this.potentialTargets = this.createStreamFromEntities(entities, maxDistance, maxFov, ignoreWalls, onlyThroughPartialBlocks, priority, candidateFilter);
    }

    private double getFov(class_1309 entity, float maxDistance, boolean ignoreWalls) {
        return this.getFov(entity, maxDistance, ignoreWalls, false);
    }

    private double getFov(class_1309 entity, float maxDistance, boolean ignoreWalls, boolean onlyThroughPartialBlocks) {
        class_243 attackVector = (class_243)this.pointFinder.computeVector(entity, maxDistance, RotationController.INSTANCE.getRotation(), class_243.field_1353, ignoreWalls, onlyThroughPartialBlocks).method_15442();
        return RaytracingUtil.rayTrace(maxDistance, BacktrackPositionResolver.boundingBox((class_1297)entity), onlyThroughPartialBlocks) ? 0.0 : RotationController.computeRotationDifference(AngleUtil.cameraAngle(), AngleUtil.calculateAngle(attackVector));
    }

    private Stream<class_1309> createStreamFromEntities(Iterable<class_1297> entities, float maxDistance, float maxFov, boolean ignoreWalls, boolean onlyThroughPartialBlocks, String priority, Predicate<class_1309> candidateFilter) {
        String sortPriority = priority == null ? "" : priority.toLowerCase();
        ArrayList<TargetCandidate> candidates = new ArrayList<TargetCandidate>();
        class_243 eye = TargetSelector.mc.field_1724.method_33571();
        double maxDistanceSquared = (double)maxDistance * (double)maxDistance;
        for (class_1297 entity : entities) {
            double fov;
            class_1309 living;
            if (!(entity instanceof class_1309) || !candidateFilter.test(living = (class_1309)entity)) continue;
            class_238 box = BacktrackPositionResolver.boundingBox((class_1297)living);
            double dx = eye.field_1352 < box.field_1323 ? box.field_1323 - eye.field_1352 : Math.max(0.0, eye.field_1352 - box.field_1320);
            double dy = eye.field_1351 < box.field_1322 ? box.field_1322 - eye.field_1351 : Math.max(0.0, eye.field_1351 - box.field_1325);
            double dz = eye.field_1350 < box.field_1321 ? box.field_1321 - eye.field_1350 : Math.max(0.0, eye.field_1350 - box.field_1324);
            if (dx * dx + dy * dy + dz * dz > maxDistanceSquared || !this.pointFinder.hasValidPoint(living, maxDistance, ignoreWalls, onlyThroughPartialBlocks) || !((fov = this.getFov(living, maxDistance, ignoreWalls, onlyThroughPartialBlocks)) < (double)maxFov)) continue;
            double score = switch (sortPriority) {
                case "fov" -> this.getFov(living, maxDistance, ignoreWalls, onlyThroughPartialBlocks);
                case "health" -> living.method_6032() + living.method_6067();
                case "armor" -> living.method_6096();
                default -> living.method_5858((class_1297)TargetSelector.mc.field_1724);
            };
            candidates.add(new TargetCandidate(living, score));
        }
        candidates.sort(Comparator.comparingDouble(TargetCandidate::score));
        return candidates.stream().map(TargetCandidate::entity);
    }

    private Optional<class_1309> findFirstMatch(Predicate<class_1309> predicate) {
        return this.potentialTargets.filter(predicate).findFirst();
    }

    public PointFinder getPointFinder() {
        return this.pointFinder;
    }

    public class_1309 getCurrentTarget() {
        return this.currentTarget;
    }

    public Stream<class_1309> getPotentialTargets() {
        return this.potentialTargets;
    }

    public class_1309 getLockedFovTarget() {
        return this.lockedFovTarget;
    }

    private record TargetCandidate(class_1309 entity, double score) {
    }

    public static class EntityFilter {
        private final List<String> targetSettings;

        public boolean isValid(class_1309 entity) {
            if (this.isLocalPlayer(entity)) {
                return false;
            }
            if (this.isInvalidHealth(entity)) {
                return false;
            }
            if (this.isBotPlayer(entity)) {
                return false;
            }
            return this.isValidEntityType(entity);
        }

        private boolean isLocalPlayer(class_1309 entity) {
            return entity == QuickImports.mc.field_1724;
        }

        private boolean isInvalidHealth(class_1309 entity) {
            return !entity.method_5805() || entity.method_6032() <= 0.0f;
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        private boolean isBotPlayer(class_1309 entity) {
            if (!(entity instanceof class_1657)) return false;
            class_1657 player = (class_1657)entity;
            AntiBot.getInstance();
            if (!AntiBot.isBot(player)) return false;
            return true;
        }

        private boolean isNakedPlayer(class_1657 player) {
            for (class_1799 stack : EntityCompat.getArmorItems((class_1309)player)) {
                if (stack.method_7960()) continue;
                return false;
            }
            return true;
        }

        private boolean isValidEntityType(class_1309 entity) {
            boolean bl;
            class_1309 class_13092 = entity;
            Objects.requireNonNull(class_13092);
            class_1309 class_13093 = class_13092;
            if (class_13093 instanceof class_1657 player) {
                if (FriendUtils.isFriend((class_1297)player)) {
                    bl = this.targetSettings.contains("Friends");
                } else if (this.targetSettings.contains("Players") || this.targetSettings.contains("Naked Player") && this.isNakedPlayer(player)) {
                    bl = true;
                } else {
                    bl = false;
                }
            } else if (class_13093 instanceof class_1429 animal) {
                bl = this.targetSettings.contains("Animals");
            } else if (class_13093 instanceof class_1308 mob) {
                bl = this.targetSettings.contains("Mobs");
            } else {
                bl = false;
            }
            return bl;
        }

        public EntityFilter(List<String> targetSettings) {
            this.targetSettings = targetSettings;
        }
    }
}

