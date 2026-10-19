/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1922
 *  net.minecraft.class_2338
 *  net.minecraft.class_238
 *  net.minecraft.class_265
 *  net.minecraft.class_638
 */
package ru.recoin.common.util.warden;

import java.util.HashMap;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1922;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_265;
import net.minecraft.class_638;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.other.BaritoneCompat;
import ru.recoin.common.util.warden.WardenPathfinder;

public final class WardenSafeNavigator
implements QuickImports {
    private class_2338 goal;
    private int radius;
    private class_638 world;
    private List<WardenPathfinder.Point> path = List.of();
    private int index;
    private class_2338 issuedGoal;
    private long replanAt;
    private long commandAt;

    public static boolean canStand(class_2338 pos) {
        if (WardenSafeNavigator.mc.field_1687 == null || !WardenSafeNavigator.mc.field_1687.method_22340(pos)) {
            return false;
        }
        return !Double.isNaN(WardenSafeNavigator.standOffset(pos)) && WardenSafeNavigator.clear(pos, WardenSafeNavigator.standOffset(pos));
    }

    private static double standOffset(class_2338 pos) {
        if (WardenSafeNavigator.mc.field_1687 == null) {
            return Double.NaN;
        }
        class_265 current = WardenSafeNavigator.mc.field_1687.method_8320(pos).method_26220((class_1922)WardenSafeNavigator.mc.field_1687, pos);
        double partialTop = Double.NaN;
        for (class_238 box : current.method_1090()) {
            if (!(box.field_1323 <= 0.45) || !(box.field_1320 >= 0.55) || !(box.field_1321 <= 0.45) || !(box.field_1324 >= 0.55) || !(box.field_1322 <= 0.01) || !(box.field_1325 > 0.01) || !(box.field_1325 < 0.99)) continue;
            partialTop = Math.max(Double.isNaN(partialTop) ? 0.0 : partialTop, box.field_1325);
        }
        if (!Double.isNaN(partialTop)) {
            return partialTop;
        }
        class_2338 below = pos.method_10074();
        class_265 support = WardenSafeNavigator.mc.field_1687.method_8320(below).method_26220((class_1922)WardenSafeNavigator.mc.field_1687, below);
        for (class_238 box : support.method_1090()) {
            if (!(box.field_1323 <= 0.45) || !(box.field_1320 >= 0.55) || !(box.field_1321 <= 0.45) || !(box.field_1324 >= 0.55) || !(box.field_1325 >= 0.99)) continue;
            return 0.0;
        }
        return Double.NaN;
    }

    private static boolean clear(class_2338 pos, double offset) {
        if (WardenSafeNavigator.mc.field_1687 == null || !WardenSafeNavigator.mc.field_1687.method_22340(pos)) {
            return false;
        }
        if (!WardenSafeNavigator.mc.field_1687.method_8316(pos).method_15769() || !WardenSafeNavigator.mc.field_1687.method_8316(pos.method_10084()).method_15769()) {
            return false;
        }
        class_238 body = new class_238((double)pos.method_10263() + 0.19, (double)pos.method_10264() + offset + 0.001, (double)pos.method_10260() + 0.19, (double)pos.method_10263() + 0.81, (double)pos.method_10264() + offset + 1.801, (double)pos.method_10260() + 0.81);
        return WardenSafeNavigator.mc.field_1687.method_8587((class_1297)WardenSafeNavigator.mc.field_1724, body);
    }

    private static boolean canTraverse(class_2338 from, class_2338 to) {
        double toY;
        if (Math.abs(from.method_10263() - to.method_10263()) + Math.abs(from.method_10260() - to.method_10260()) != 1 || Math.abs(from.method_10264() - to.method_10264()) > 1) {
            return false;
        }
        double fromOffset = WardenSafeNavigator.standOffset(from);
        double toOffset = WardenSafeNavigator.standOffset(to);
        if (Double.isNaN(fromOffset) || Double.isNaN(toOffset)) {
            return false;
        }
        double fromY = (double)from.method_10264() + fromOffset;
        if (Math.abs(fromY - (toY = (double)to.method_10264() + toOffset)) > 1.01 || !WardenSafeNavigator.clear(from, fromOffset) || !WardenSafeNavigator.clear(to, toOffset)) {
            return false;
        }
        double bodyY = Math.max(fromY, toY);
        class_238 sweep = new class_238((double)Math.min(from.method_10263(), to.method_10263()) + 0.19, bodyY + 0.001, (double)Math.min(from.method_10260(), to.method_10260()) + 0.19, (double)Math.max(from.method_10263(), to.method_10263()) + 0.81, bodyY + 1.801, (double)Math.max(from.method_10260(), to.method_10260()) + 0.81);
        return WardenSafeNavigator.mc.field_1687.method_8587((class_1297)WardenSafeNavigator.mc.field_1724, sweep);
    }

    public void moveTo(Object baritone, class_2338 goal, int radius, long now) {
        if (goal == null) {
            return;
        }
        if (!goal.equals((Object)this.goal) || radius != this.radius || this.world != WardenSafeNavigator.mc.field_1687) {
            this.reset();
            this.goal = goal.method_10062();
            this.radius = radius;
            this.world = WardenSafeNavigator.mc.field_1687;
            BaritoneCompat.cancelPathing(baritone);
        }
        this.tick(baritone, now);
    }

    public void tick(Object baritone, long now) {
        class_2338 next;
        int actualIndex;
        if (this.goal == null || baritone == null || WardenSafeNavigator.mc.field_1724 == null || WardenSafeNavigator.mc.field_1687 != this.world) {
            return;
        }
        if (WardenSafeNavigator.mc.field_1755 != null || WardenSafeNavigator.mc.field_1724.method_6115()) {
            return;
        }
        List<class_2338> actual = BaritoneCompat.getPathPositions(baritone);
        for (int i = actualIndex = Math.max(0, BaritoneCompat.getPathPositionIndex(baritone)); i < Math.min(actual.size(), actualIndex + 5); ++i) {
            next = actual.get(i);
            if ((next.equals((Object)WardenSafeNavigator.mc.field_1724.method_24515()) || WardenSafeNavigator.canStand(next)) && (i <= actualIndex || WardenSafeNavigator.canTraverse(actual.get(i - 1), next))) continue;
            BaritoneCompat.cancelPathing(baritone);
            this.path = List.of();
            this.issuedGoal = null;
            this.replanAt = now + 300L;
            return;
        }
        while (this.index < this.path.size() && this.reached(this.path.get(this.index))) {
            ++this.index;
        }
        if (this.index >= this.path.size()) {
            if (now < this.replanAt) {
                return;
            }
            this.plan();
            this.replanAt = now + 1000L;
        }
        if (this.index >= this.path.size()) {
            return;
        }
        int waypointIndex = Math.min((this.index / 4 + 1) * 4, this.path.size() - 1);
        next = WardenSafeNavigator.blockPos(this.path.get(waypointIndex));
        if (!WardenSafeNavigator.canStand(next)) {
            BaritoneCompat.cancelPathing(baritone);
            this.path = List.of();
            this.issuedGoal = null;
            return;
        }
        if (!next.equals((Object)this.issuedGoal) || now >= this.commandAt && !BaritoneCompat.isPathing(baritone)) {
            BaritoneCompat.goBlock(baritone, next);
            this.issuedGoal = next;
            this.commandAt = now + 750L;
        }
    }

    private void plan() {
        final HashMap<WardenPathfinder.Point, Boolean> standable = new HashMap<WardenPathfinder.Point, Boolean>();
        WardenPathfinder.Terrain terrain = new WardenPathfinder.Terrain(){

            @Override
            public boolean canStand(WardenPathfinder.Point point) {
                return standable.computeIfAbsent(point, p -> WardenSafeNavigator.canStand(WardenSafeNavigator.blockPos(p)));
            }

            @Override
            public boolean canTraverse(WardenPathfinder.Point from, WardenPathfinder.Point to) {
                return WardenSafeNavigator.canTraverse(WardenSafeNavigator.blockPos(from), WardenSafeNavigator.blockPos(to));
            }
        };
        class_2338 start = WardenSafeNavigator.mc.field_1724.method_24515();
        this.path = WardenPathfinder.find(WardenSafeNavigator.point(start), WardenSafeNavigator.point(this.goal), this.radius, terrain, 6000);
        this.index = this.path.size() > 1 ? 1 : this.path.size();
        this.issuedGoal = null;
    }

    private boolean reached(WardenPathfinder.Point point) {
        double dx = WardenSafeNavigator.mc.field_1724.method_23317() - (double)point.x() - 0.5;
        double dz = WardenSafeNavigator.mc.field_1724.method_23321() - (double)point.z() - 0.5;
        double offset = WardenSafeNavigator.standOffset(WardenSafeNavigator.blockPos(point));
        return dx * dx + dz * dz < 0.25 && !Double.isNaN(offset) && Math.abs(WardenSafeNavigator.mc.field_1724.method_23318() - (double)point.y() - offset) < 0.2;
    }

    private static WardenPathfinder.Point point(class_2338 pos) {
        return new WardenPathfinder.Point(pos.method_10263(), pos.method_10264(), pos.method_10260());
    }

    private static class_2338 blockPos(WardenPathfinder.Point point) {
        return new class_2338(point.x(), point.y(), point.z());
    }

    public void reset() {
        this.issuedGoal = null;
        this.goal = null;
        this.world = null;
        this.path = List.of();
        this.index = 0;
        this.commandAt = 0L;
        this.replanAt = 0L;
    }
}

