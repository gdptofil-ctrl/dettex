/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10584
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_2761
 *  net.minecraft.class_310
 *  net.minecraft.class_634
 *  net.minecraft.class_638
 *  net.minecraft.class_640
 */
package ru.recoin.implement.features.modules.combat.killaura.target;

import net.minecraft.class_10584;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2761;
import net.minecraft.class_310;
import net.minecraft.class_634;
import net.minecraft.class_638;
import net.minecraft.class_640;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.target.BacktrackTargetSelector;

public final class TargetBacktrackController {
    public static final float MAX_REACH = 4.2f;
    private final class_310 client = class_310.method_1551();
    private final BacktrackTargetSelector selector = new BacktrackTargetSelector();
    private class_1309 target;
    private class_638 world;
    private BacktrackTargetSelector.TargetSelection selection;
    private long lastUpdatedAt;
    private class_638 timingWorld;
    private volatile float ticksPerSecond = 20.0f;
    private long lastTimeUpdateNanos;

    public synchronized void onPacket(PacketEvent event) {
        if (!event.isReceive() || !(event.getPacket() instanceof class_2761)) {
            return;
        }
        long now = System.nanoTime();
        if (this.lastTimeUpdateNanos != 0L && now > this.lastTimeUpdateNanos) {
            float rate = Math.max(0.0f, Math.min(20.0f, 20.0f * (1.0E9f / (float)(now - this.lastTimeUpdateNanos))));
            this.ticksPerSecond = (float)Math.round(rate * 100.0f) / 100.0f;
        }
        this.lastTimeUpdateNanos = now;
    }

    public void updateTarget(class_1309 entity, Angle rotation) {
        this.updateWorld();
        if (this.client.field_1724 == null || this.client.field_1687 == null || entity == null || !entity.method_5805() || entity.method_31481()) {
            this.reset();
            return;
        }
        if (this.target != entity || this.world != this.client.field_1687) {
            this.reset();
            this.target = entity;
            this.world = this.client.field_1687;
        }
        this.lastUpdatedAt = TargetBacktrackController.monotonicMillis();
        class_238 liveBox = entity.method_5829();
        class_10584 interpolator = entity.method_66233();
        class_243 remotePosition = interpolator == null ? entity.method_73189() : interpolator.method_66265();
        class_238 remoteBox = liveBox.method_997(remotePosition.method_1020(entity.method_73189()));
        this.selector.record(this.lastUpdatedAt, TargetBacktrackController.historicalBox(remoteBox));
        class_634 connection = this.client.method_1562();
        class_640 entry = connection == null ? null : connection.method_2871(this.client.field_1724.method_5667());
        int latency = entry == null ? 0 : entry.method_2959();
        class_243 eye = this.client.field_1724.method_33571();
        class_243 ray = rotation == null ? this.client.field_1724.method_5828(1.0f) : rotation.toVector();
        this.selection = this.selector.select(this.lastUpdatedAt, TargetBacktrackController.historicalBox(liveBox), eye.field_1352, eye.field_1351, eye.field_1350, latency, this.ticksPerSecond, new BacktrackTargetSelector.RayDirection(ray.field_1352, ray.field_1351, ray.field_1350, entity.method_5871()), 4.2f);
    }

    private void updateWorld() {
        if (this.timingWorld != this.client.field_1687) {
            this.resetSession();
            this.timingWorld = this.client.field_1687;
        }
    }

    public boolean isSelectionValid(class_1297 entity) {
        return this.selection != null && this.client.field_1724 != null && entity != null && entity == this.target && entity.method_5805() && !entity.method_31481() && this.world == this.client.field_1687 && TargetBacktrackController.monotonicMillis() - this.lastUpdatedAt <= 100L;
    }

    public class_238 targetBox(class_1297 entity) {
        if (!this.isSelectionValid(entity)) {
            return entity.method_5829();
        }
        BacktrackTargetSelector.HistoricalBox box = this.selection.box();
        return new class_238(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ());
    }

    public class_243 targetOffset(class_1297 entity) {
        if (!this.isSelectionValid(entity)) {
            return class_243.field_1353;
        }
        class_238 box = this.targetBox(entity);
        class_238 liveBox = entity.method_5829();
        class_243 offset = new class_243((box.field_1323 + box.field_1320 - liveBox.field_1323 - liveBox.field_1320) * 0.5, box.field_1322 - liveBox.field_1322, (box.field_1321 + box.field_1324 - liveBox.field_1321 - liveBox.field_1324) * 0.5);
        return offset.method_1027() < 1.0E-12 ? class_243.field_1353 : offset;
    }

    public float attackReach(class_1297 entity) {
        return this.isSelectionValid(entity) ? (float)this.selection.reach() : 3.0f;
    }

    public boolean isHistorical(class_1297 entity) {
        return this.isSelectionValid(entity) && this.selection.historical();
    }

    public void reset() {
        this.selector.clear();
        this.target = null;
        this.world = null;
        this.selection = null;
        this.lastUpdatedAt = 0L;
    }

    public synchronized void resetSession() {
        this.reset();
        this.timingWorld = null;
        this.ticksPerSecond = 20.0f;
        this.lastTimeUpdateNanos = 0L;
    }

    private static long monotonicMillis() {
        return System.nanoTime() / 1000000L;
    }

    private static BacktrackTargetSelector.HistoricalBox historicalBox(class_238 box) {
        return new BacktrackTargetSelector.HistoricalBox(box.field_1323, box.field_1322, box.field_1321, box.field_1320, box.field_1325, box.field_1324);
    }
}

