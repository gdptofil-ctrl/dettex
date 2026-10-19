/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.combat.killaura.target;

import java.util.ArrayDeque;
import java.util.Iterator;

public final class BacktrackTargetSelector {
    static final double BASE_REACH = 3.0;
    static final double MAX_REACH = 4.2;
    private static final double FULL_REACH_SPEED = 5.0;
    private static final double MIN_SPEED = 0.15;
    static final long MAX_WINDOW_MILLIS = 200L;
    private final ArrayDeque<HistorySample> history = new ArrayDeque();
    private HistorySample heldSample;

    public void record(long nowMillis, HistoricalBox historicalBox) {
        HistorySample historySample = this.history.peekLast();
        if (historySample != null && (nowMillis < historySample.time || nowMillis - historySample.time > 250L || historicalBox.movementSquared(historySample.box) > 9.0)) {
            this.clear();
            historySample = null;
        }
        if (historySample != null && nowMillis == historySample.time) {
            this.history.removeLast();
        }
        this.history.addLast(new HistorySample(nowMillis, historicalBox));
        while (this.history.size() > 64 || !this.history.isEmpty() && nowMillis - this.history.peekFirst().time > 400L) {
            this.history.removeFirst();
        }
    }

    TargetSelection select(long nowMillis, HistoricalBox historicalBox, double eyeX, double eyeY, double eyeZ, int latencyMillis, double tickRate) {
        return this.select(nowMillis, historicalBox, eyeX, eyeY, eyeZ, latencyMillis, tickRate, null);
    }

    TargetSelection select(long nowMillis, HistoricalBox historicalBox, double eyeX, double eyeY, double eyeZ, int latencyMillis, double tickRate, RayDirection rayDirection) {
        return this.select(nowMillis, historicalBox, eyeX, eyeY, eyeZ, latencyMillis, tickRate, rayDirection, 4.2);
    }

    public TargetSelection select(long nowMillis, HistoricalBox var3, double eyeX, double eyeY, double eyeZ, int latencyMillis, double tickRate, RayDirection rayDirection, double reachLimit) {
        long windowMillis = BacktrackTargetSelector.historyWindowMillis(latencyMillis, tickRate);
        HistorySample latestSample = this.history.peekLast();
        if (latestSample != null && nowMillis >= latestSample.time && nowMillis - latestSample.time <= 100L) {
            boolean var30;
            long var37;
            HistoricalBox box = latestSample.box;
            HistorySample var20 = latestSample;
            Iterator<HistorySample> var21 = this.history.descendingIterator();
            while (var21.hasNext()) {
                HistorySample var22;
                var20 = var22 = var21.next();
                if (latestSample.time - var22.time < 100L) continue;
                break;
            }
            double movementSpeed = (var37 = latestSample.time - var20.time) < 50L ? 0.0 : box.horizontalMovement(var20.box) * 1000.0 / (double)var37;
            double effectiveReach = Math.min(BacktrackTargetSelector.reachForSpeed(movementSpeed), Math.min(reachLimit, 4.2));
            double var27 = box.distance(eyeX, eyeY, eyeZ);
            if (var27 <= 3.0) {
                HistoricalBox var38 = var3.distance(eyeX, eyeY, eyeZ) <= 3.0 ? var3 : box;
                return !var38.intersectsRay(eyeX, eyeY, eyeZ, rayDirection, effectiveReach) && box.intersectsRay(eyeX, eyeY, eyeZ, rayDirection, effectiveReach) ? this.liveSelection(box, windowMillis, effectiveReach, movementSpeed, "remote_in_reach") : this.liveSelection(var38, windowMillis, effectiveReach, movementSpeed, "live_in_reach");
            }
            if (var27 > 4.2) {
                return this.liveSelection(box, windowMillis, effectiveReach, movementSpeed, "outside_max_reach");
            }
            HistoricalBox var29 = var27 <= effectiveReach && var3.distance(eyeX, eyeY, eyeZ) <= effectiveReach ? var3 : box;
            boolean bl = var30 = var27 <= effectiveReach && var29.distance(eyeX, eyeY, eyeZ) <= effectiveReach;
            if (var30 && var29.intersectsRay(eyeX, eyeY, eyeZ, rayDirection, effectiveReach)) {
                return this.liveSelection(var29, windowMillis, effectiveReach, movementSpeed, "moving_in_reach");
            }
            if (var30 && box.intersectsRay(eyeX, eyeY, eyeZ, rayDirection, effectiveReach)) {
                return this.liveSelection(box, windowMillis, effectiveReach, movementSpeed, "remote_in_reach");
            }
            double var31 = box.distance(eyeX, eyeY, eyeZ) - var20.box.distance(eyeX, eyeY, eyeZ);
            if (!(movementSpeed <= 0.15 || !var30 && var31 <= 0.03)) {
                if (BacktrackTargetSelector.isUsableHistory(this.heldSample, latestSample, nowMillis, windowMillis, eyeX, eyeY, eyeZ, rayDirection, effectiveReach)) {
                    return new TargetSelection(this.heldSample.box, true, nowMillis - this.heldSample.time, windowMillis, effectiveReach, movementSpeed, "held_history");
                }
                Iterator<HistorySample> var33 = this.history.descendingIterator();
                while (var33.hasNext()) {
                    HistorySample heldSample = var33.next();
                    long var35 = nowMillis - heldSample.time;
                    if (var35 > windowMillis) break;
                    if (!BacktrackTargetSelector.isUsableHistory(heldSample, latestSample, nowMillis, windowMillis, eyeX, eyeY, eyeZ, rayDirection, effectiveReach)) continue;
                    this.heldSample = heldSample;
                    return new TargetSelection(heldSample.box, true, var35, windowMillis, effectiveReach, movementSpeed, var30 ? "ray_history" : "recent_reachable_history");
                }
                return this.liveSelection(var29, windowMillis, effectiveReach, movementSpeed, "no_reachable_history");
            }
            return this.liveSelection(var29, windowMillis, effectiveReach, movementSpeed, "no_history_benefit");
        }
        return this.liveSelection(var3, windowMillis, 3.0, 0.0, "no_fresh_history");
    }

    private TargetSelection liveSelection(HistoricalBox historicalBox, long var2, double var4, double var6, String var8) {
        this.heldSample = null;
        return new TargetSelection(historicalBox, false, 0L, var2, var4, var6, var8);
    }

    private static boolean isUsableHistory(HistorySample historySample, HistorySample var1, long nowMillis, long windowMillis, double eyeX, double eyeY, double eyeZ, RayDirection rayDirection, double effectiveReach) {
        return historySample != null && historySample.time < var1.time && nowMillis - historySample.time >= 0L && nowMillis - historySample.time <= windowMillis && historySample.box.movementSquared(var1.box) > 1.0E-8 && historySample.box.distance(eyeX, eyeY, eyeZ) <= effectiveReach && historySample.box.intersectsRay(eyeX, eyeY, eyeZ, rayDirection, effectiveReach);
    }

    static double reachForSpeed(double movementSpeed) {
        if (Double.isFinite(movementSpeed) && !(movementSpeed <= 0.15)) {
            double var2 = Math.min(1.0, (movementSpeed - 0.15) / 4.85);
            return Math.min(4.2, 3.0 + 4.0 * var2);
        }
        return 3.0;
    }

    static long historyWindowMillis(int latencyMillis, double tickRate) {
        double var3 = 1000.0 / Math.max(10.0, Math.min(20.0, Double.isFinite(tickRate) ? tickRate : 20.0));
        return Math.max(100L, Math.min(200L, Math.round((double)Math.max(0, latencyMillis) * 0.5 + 2.0 * var3)));
    }

    public int size() {
        return this.history.size();
    }

    public void clear() {
        this.history.clear();
        this.heldSample = null;
    }

    record HistorySample(long time, HistoricalBox box) {
    }

    public record HistoricalBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        double distance(double var1, double var3, double var5) {
            double var7 = Math.max(Math.max(this.minX - var1, var1 - this.maxX), 0.0);
            double var9 = Math.max(Math.max(this.minY - var3, var3 - this.maxY), 0.0);
            double var11 = Math.max(Math.max(this.minZ - var5, var5 - this.maxZ), 0.0);
            return Math.sqrt(var7 * var7 + var9 * var9 + var11 * var11);
        }

        double movementSquared(HistoricalBox historicalBox) {
            double var2 = (this.minX + this.maxX - historicalBox.minX - historicalBox.maxX) * 0.5;
            double var4 = this.minY - historicalBox.minY;
            double var6 = (this.minZ + this.maxZ - historicalBox.minZ - historicalBox.maxZ) * 0.5;
            return var2 * var2 + var4 * var4 + var6 * var6;
        }

        double horizontalMovement(HistoricalBox historicalBox) {
            double var2 = (this.minX + this.maxX - historicalBox.minX - historicalBox.maxX) * 0.5;
            double var4 = (this.minZ + this.maxZ - historicalBox.minZ - historicalBox.maxZ) * 0.5;
            return Math.hypot(var2, var4);
        }

        boolean intersectsRay(double var1, double var3, double var5, RayDirection rayDirection, double var8) {
            if (rayDirection == null) {
                return true;
            }
            double var10 = 0.0;
            double var12 = var8;
            double var14 = Math.max(0.0, rayDirection.margin);
            for (int var16 = 0; var16 < 3; ++var16) {
                double var17;
                double var19;
                if (var16 == 0) {
                    var17 = var1;
                    var19 = rayDirection.x;
                } else if (var16 == 1) {
                    var17 = var3;
                    var19 = rayDirection.y;
                } else {
                    var17 = var5;
                    var19 = rayDirection.z;
                }
                double var21 = (var16 == 0 ? this.minX : (var16 == 1 ? this.minY : this.minZ)) - var14;
                double var23 = (var16 == 0 ? this.maxX : (var16 == 1 ? this.maxY : this.maxZ)) + var14;
                if (Math.abs(var19) < 1.0E-9) {
                    if (var17 < var21 || var17 > var23) {
                        return false;
                    }
                    continue;
                }
                double var25 = (var21 - var17) / var19;
                double var27 = (var23 - var17) / var19;
                var10 = Math.max(var10, Math.min(var25, var27));
                var12 = Math.min(var12, Math.max(var25, var27));
                if (var10 > var12) {
                    return false;
                }
            }
            return true;
        }
    }

    public record RayDirection(double x, double y, double z, double margin) {
    }

    public record TargetSelection(HistoricalBox box, boolean historical, long ageMs, long windowMs, double reach, double speed, String reason) {
    }
}

