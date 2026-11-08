/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_1268
 *  net.minecraft.class_1269
 *  net.minecraft.class_1297
 *  net.minecraft.class_1542
 *  net.minecraft.class_1657
 *  net.minecraft.class_1703
 *  net.minecraft.class_1713
 *  net.minecraft.class_1735
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_2246
 *  net.minecraft.class_2248
 *  net.minecraft.class_2338
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_2350
 *  net.minecraft.class_238
 *  net.minecraft.class_2382
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_2596
 *  net.minecraft.class_2626
 *  net.minecraft.class_2637
 *  net.minecraft.class_3489
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_3965
 *  net.minecraft.class_437
 *  net.minecraft.class_465
 *  net.minecraft.class_746
 *  net.minecraft.class_9334
 */
package ru.recoin.implement.features.modules.misc;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.class_124;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1297;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2596;
import net.minecraft.class_2626;
import net.minecraft.class_2637;
import net.minecraft.class_3489;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_746;
import net.minecraft.class_9334;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.TextSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.QuickLogger;
import ru.recoin.common.util.other.BaritoneCompat;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;

public final class AnchorFarming
extends Module {
    private final TextSetting rtpCommand = new TextSetting("\u041a\u043e\u043c\u0430\u043d\u0434\u0430 RTP", "\u041a\u043e\u043c\u0430\u043d\u0434\u0430 \u0434\u043b\u044f \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0430\u0446\u0438\u0438 \u043f\u043e \u043c\u0438\u0440\u0443").setText("rtp");
    private final TextSetting sellCommand = new TextSetting("\u041a\u043e\u043c\u0430\u043d\u0434\u0430 \u043f\u0440\u043e\u0434\u0430\u0436\u0438", "\u041a\u043e\u043c\u0430\u043d\u0434\u0430 \u0434\u043b\u044f \u043e\u0442\u043a\u0440\u044b\u0442\u0438\u044f \u043c\u0435\u043d\u044e \u0441\u043a\u0443\u043f\u043a\u0438").setText("sell");
    private final ValueSetting scanRadius = new ValueSetting("\u0420\u0430\u0434\u0438\u0443\u0441 \u043f\u043e\u0438\u0441\u043a\u0430", "\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f \u043e\u0431\u043d\u0430\u0440\u0443\u0436\u0435\u043d\u0438\u044f \u044f\u043a\u043e\u0440\u0435\u0439").setValue(64.0f).range(16.0f, 128.0f).step(8.0f);
    private final ValueSetting rtpTimeoutMin = new ValueSetting("\u0422\u0430\u0439\u043c\u0430\u0443\u0442 RTP (\u043c\u0438\u043d)", "\u041c\u0438\u043d\u0443\u0442\u044b \u0431\u0435\u0437 \u043d\u0430\u0445\u043e\u0434\u043e\u043a \u0434\u043e \u043d\u043e\u0432\u043e\u0433\u043e /rtp").setValue(5.0f).range(1.0f, 15.0f).step(0.5f);
    private final BooleanSetting autoEat = new BooleanSetting("\u0410\u0432\u0442\u043e \u0435\u0434\u0430", "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u043a\u0443\u0448\u0430\u0442\u044c \u043f\u0440\u0438 \u0433\u043e\u043b\u043e\u0434\u0435").setValue(true);
    private final BooleanSetting esp = new BooleanSetting("\u041f\u043e\u0434\u0441\u0432\u0435\u0442\u043a\u0430", "\u041e\u0442\u043e\u0431\u0440\u0430\u0436\u0430\u0442\u044c 3D \u0431\u043e\u043a\u0441\u044b \u043d\u0430\u0439\u0434\u0435\u043d\u043d\u044b\u0445 \u044f\u043a\u043e\u0440\u0435\u0439").setValue(true);
    private final ExecutorService scanner = Executors.newSingleThreadExecutor(r -> {
        Thread thread2 = new Thread(r, "Recoin-AnchorFarming-Scanner");
        thread2.setDaemon(true);
        return thread2;
    });
    private final AtomicBoolean scanning = new AtomicBoolean(false);
    private final Set<class_2338> foundAnchors = ConcurrentHashMap.newKeySet();
    private final Set<class_2338> minedAnchors = ConcurrentHashMap.newKeySet();
    private Object baritone;
    private Boolean previousAllowBreak;
    private Boolean previousAllowSprint;
    private Boolean previousAllowParkour;
    private Boolean previousAllowParkourAscend;
    private Boolean previousAllowParkourPlace;
    private State state = State.SEARCHING;
    private class_2338 currentTarget;
    private class_2338 pendingPickupPos;
    private long lastFoundAnchorTime;
    private long lastScanTime;
    private long lastRtpTime;
    private long sellStartTime;
    private long lastSellSlotClickTime;
    private long mineStartTime;
    private long pickupStartTime;
    private long targetPathStartTime;
    private long targetProgressTime;
    private class_2338 lastTargetPathPosition;
    private int previousSlotBeforeEat = -1;
    private boolean isEating;

    public AnchorFarming() {
        super("Anchor Farming", "Anchor Farming", ModuleCategory.PVE);
        this.setup(this.rtpCommand, this.sellCommand, this.scanRadius, this.rtpTimeoutMin, this.autoEat, this.esp);
    }

    public static AnchorFarming getInstance() {
        return Instance.get(AnchorFarming.class);
    }

    @Override
    public void activate() {
        long now;
        this.baritone = BaritoneCompat.getPrimaryBaritone();
        this.previousAllowBreak = BaritoneCompat.getBooleanSetting("allowBreak");
        this.previousAllowSprint = BaritoneCompat.getBooleanSetting("allowSprint");
        this.previousAllowParkour = BaritoneCompat.getBooleanSetting("allowParkour");
        this.previousAllowParkourAscend = BaritoneCompat.getBooleanSetting("allowParkourAscend");
        this.previousAllowParkourPlace = BaritoneCompat.getBooleanSetting("allowParkourPlace");
        this.foundAnchors.clear();
        this.minedAnchors.clear();
        this.currentTarget = null;
        this.pendingPickupPos = null;
        this.isEating = false;
        this.previousSlotBeforeEat = -1;
        this.state = State.SEARCHING;
        this.lastFoundAnchorTime = now = System.currentTimeMillis();
        this.lastScanTime = 0L;
        this.lastRtpTime = 0L;
        this.sellStartTime = 0L;
        this.lastSellSlotClickTime = 0L;
        this.mineStartTime = 0L;
        this.pickupStartTime = 0L;
        this.targetPathStartTime = 0L;
        this.targetProgressTime = 0L;
        this.lastTargetPathPosition = null;
        if (AnchorFarming.mc.field_1724 == null) {
            return;
        }
        if (!this.hasPickaxe()) {
            this.notifyMessage("\u00a7c\u0414\u043b\u044f \u0440\u0430\u0431\u043e\u0442\u044b \u043c\u043e\u0434\u0443\u043b\u044f \u043d\u0435\u043e\u0431\u0445\u043e\u0434\u0438\u043c\u0430 \u043a\u0438\u0440\u043a\u0430 \u0441 \u043f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u044c\u044e > 5% \u0432 \u0445\u043e\u0442\u0431\u0430\u0440\u0435 \u0438\u043b\u0438 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435!");
            this.setState(false);
            return;
        }
        if (!this.hasFood()) {
            this.notifyMessage("\u00a7c\u0414\u043b\u044f \u0440\u0430\u0431\u043e\u0442\u044b \u043c\u043e\u0434\u0443\u043b\u044f \u043d\u0435\u043e\u0431\u0445\u043e\u0434\u0438\u043c\u0430 \u0435\u0434\u0430 \u0432 \u0445\u043e\u0442\u0431\u0430\u0440\u0435 \u0438\u043b\u0438 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435!");
            this.setState(false);
            return;
        }
        this.configureBaritone(true);
        this.notifyMessage("\u00a7a\u041c\u043e\u0434\u0443\u043b\u044c \u0437\u0430\u043f\u0443\u0449\u0435\u043d! \u041d\u0430\u0447\u0438\u043d\u0430\u0435\u043c \u043f\u043e\u0438\u0441\u043a \u044f\u043a\u043e\u0440\u0435\u0439 \u0432\u043e\u0437\u0440\u043e\u0436\u0434\u0435\u043d\u0438\u044f...");
    }

    @Override
    public void deactivate() {
        this.stopEating();
        if (this.baritone != null) {
            BaritoneCompat.cancelMining(this.baritone);
            BaritoneCompat.cancelPathing(this.baritone);
        }
        this.configureBaritone(false);
        this.foundAnchors.clear();
        this.minedAnchors.clear();
        this.currentTarget = null;
        this.pendingPickupPos = null;
        this.resetTargetPath();
        this.notifyMessage("\u00a77\u041c\u043e\u0434\u0443\u043b\u044c \u043e\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d.");
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        if (AnchorFarming.mc.field_1687 == null || event.isSend()) {
            return;
        }
        class_2596<?> packet = event.getPacket();
        if (packet instanceof class_2626) {
            class_2626 blockUpdate = (class_2626)packet;
            this.handleBlockUpdate(blockUpdate.method_11309(), blockUpdate.method_11308().method_26204());
        } else if (packet instanceof class_2637) {
            class_2637 chunkDelta = (class_2637)packet;
            chunkDelta.method_30621((pos, blockState) -> this.handleBlockUpdate((class_2338)pos, blockState.method_26204()));
        }
    }

    private void handleBlockUpdate(class_2338 pos, class_2248 block) {
        class_2338 immutable = pos.method_10062();
        if (block == class_2246.field_23152) {
            if (!this.minedAnchors.contains(immutable)) {
                this.foundAnchors.add(immutable);
                this.lastFoundAnchorTime = System.currentTimeMillis();
            }
        } else {
            this.foundAnchors.remove(immutable);
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        long timeoutMs;
        if (AnchorFarming.mc.field_1724 == null || AnchorFarming.mc.field_1687 == null || AnchorFarming.mc.field_1761 == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (!this.hasPickaxe()) {
            this.notifyMessage("\u00a7c\u041a\u0438\u0440\u043a\u0430 \u0441\u043b\u043e\u043c\u0430\u043b\u0430\u0441\u044c \u0438\u043b\u0438 \u043e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u0435\u0442! \u0412\u044b\u043a\u043b\u044e\u0447\u0430\u0435\u043c \u043c\u043e\u0434\u0443\u043b\u044c.");
            this.setState(false);
            return;
        }
        if (!this.hasFood()) {
            this.notifyMessage("\u00a7c\u0417\u0430\u043a\u043e\u043d\u0447\u0438\u043b\u0430\u0441\u044c \u0435\u0434\u0430! \u0412\u044b\u043a\u043b\u044e\u0447\u0430\u0435\u043c \u043c\u043e\u0434\u0443\u043b\u044c.");
            this.setState(false);
            return;
        }
        if (this.autoEat.isValue() && (this.isEating || AnchorFarming.mc.field_1724.method_7344().method_7586() <= 16) && this.handleEating()) {
            return;
        }
        if (this.countAnchorsInInventory() > 0) {
            this.handleSelling(now);
            return;
        }
        if (this.state == State.SELLING) {
            if (AnchorFarming.mc.field_1755 instanceof class_465) {
                AnchorFarming.mc.field_1724.method_7346();
            }
            this.state = State.SEARCHING;
            this.lastFoundAnchorTime = now;
            this.currentTarget = null;
        }
        if (this.state == State.WAITING_RTP) {
            if (now - this.lastRtpTime < 4000L) {
                return;
            }
            this.state = State.SEARCHING;
            this.lastFoundAnchorTime = now;
            this.foundAnchors.clear();
            this.currentTarget = null;
        }
        if (this.state == State.PICKING_UP) {
            this.handlePickup(now);
            return;
        }
        if (now - this.lastScanTime >= 1500L && this.scanning.compareAndSet(false, true)) {
            this.lastScanTime = now;
            int radius = Math.round(this.scanRadius.getValue());
            class_2338 playerCenter = AnchorFarming.mc.field_1724.method_24515();
            this.scanner.execute(() -> {
                try {
                    this.scanAnchorsAsync(playerCenter, radius);
                }
                finally {
                    this.scanning.set(false);
                }
            });
        }
        if (now - this.lastFoundAnchorTime >= (timeoutMs = (long)(this.rtpTimeoutMin.getValue() * 60000.0f))) {
            this.notifyMessage("\u00a7e\u0417\u0430 " + Math.round(this.rtpTimeoutMin.getValue()) + " \u043c\u0438\u043d. \u044f\u043a\u043e\u0440\u0435\u0439 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u043e. \u0422\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0438\u0440\u0443\u0435\u043c\u0441\u044f \u043d\u0430 /rtp...");
            this.performRtp(now);
            return;
        }
        this.purgeInvalidAnchors();
        if (this.currentTarget != null && !this.minedAnchors.contains(this.currentTarget) && AnchorFarming.mc.field_1687.method_2935().method_12123(this.currentTarget.method_10263() >> 4, this.currentTarget.method_10260() >> 4) && !AnchorFarming.mc.field_1687.method_8320(this.currentTarget).method_27852(class_2246.field_23152)) {
            this.beginPickup(this.currentTarget, now);
        } else if (this.currentTarget != null && this.minedAnchors.contains(this.currentTarget)) {
            this.currentTarget = null;
            this.resetTargetPath();
        }
        if (this.state == State.PICKING_UP) {
            this.handlePickup(now);
            return;
        }
        if (this.currentTarget == null && !this.foundAnchors.isEmpty()) {
            this.currentTarget = this.foundAnchors.stream().filter(pos -> !this.minedAnchors.contains(pos)).min(Comparator.comparingDouble(pos -> AnchorFarming.mc.field_1724.method_24515().method_10262((class_2382)pos))).orElse(null);
            if (this.currentTarget != null) {
                this.lastFoundAnchorTime = now;
                this.resetTargetPath();
                BaritoneCompat.cancelPathing(this.baritone);
            }
        }
        if (this.currentTarget != null) {
            double reach = AnchorFarming.mc.field_1724.method_55754();
            class_3965 visibleHit = this.visibleAnchorFace(this.currentTarget, reach);
            if (visibleHit != null) {
                this.state = State.MINING;
                this.handleMining(this.currentTarget, visibleHit, now);
            } else {
                if (this.state == State.MINING) {
                    AnchorFarming.mc.field_1761.method_2925();
                }
                this.state = State.GOING_TO_ANCHOR;
                this.mineStartTime = 0L;
                this.navigateToTarget(now);
            }
        } else {
            this.state = State.SEARCHING;
            if (!BaritoneCompat.isPathing(this.baritone)) {
                this.wanderAround();
            }
        }
    }

    private class_3965 visibleAnchorFace(class_2338 target, double reach) {
        class_243 eye = AnchorFarming.mc.field_1724.method_33571();
        return Arrays.stream(class_2350.values()).map(side -> class_243.method_24953((class_2382)target).method_1031((double)side.method_10148() * 0.45, (double)side.method_10164() * 0.45, (double)side.method_10165() * 0.45)).filter(point -> eye.method_1022(point) <= reach).map(point -> AnchorFarming.mc.field_1687.method_17742(new class_3959(eye, point, class_3959.class_3960.field_17559, class_3959.class_242.field_1348, (class_1297)AnchorFarming.mc.field_1724))).filter(hit -> hit.method_17783() == class_239.class_240.field_1332 && hit.method_17777().equals((Object)target)).min(Comparator.comparingDouble(hit -> eye.method_1025(hit.method_17784()))).orElse(null);
    }

    private void handleMining(class_2338 target, class_3965 hit, long now) {
        this.ensurePickaxeSelected();
        if (this.mineStartTime == 0L) {
            this.mineStartTime = now;
        }
        BaritoneCompat.cancelPathing(this.baritone);
        AnchorFarming.mc.field_1761.method_2902(target, hit.method_17780());
        AnchorFarming.mc.field_1724.method_6104(class_1268.field_5808);
        if (!AnchorFarming.mc.field_1687.method_8320(target).method_27852(class_2246.field_23152)) {
            this.minedAnchors.add(target);
            this.foundAnchors.remove(target);
            this.currentTarget = null;
            this.mineStartTime = 0L;
            this.beginPickup(target, now);
            this.notifyMessage("\u00a7a\u042f\u043a\u043e\u0440\u044c \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0434\u043e\u0431\u044b\u0442! \u041f\u043e\u0434\u0431\u0438\u0440\u0430\u0435\u043c \u043b\u0443\u0442...");
        }
    }

    private void beginPickup(class_2338 target, long now) {
        this.minedAnchors.add(target.method_10062());
        this.foundAnchors.remove(target);
        this.currentTarget = null;
        this.resetTargetPath();
        this.mineStartTime = 0L;
        this.pendingPickupPos = target.method_10062();
        this.pickupStartTime = now;
        this.state = State.PICKING_UP;
        BaritoneCompat.cancelPathing(this.baritone);
    }

    private void navigateToTarget(long now) {
        boolean retry;
        if (this.baritone == null || this.currentTarget == null) {
            return;
        }
        class_2338 playerPos = AnchorFarming.mc.field_1724.method_24515().method_10062();
        if (this.lastTargetPathPosition == null || !this.lastTargetPathPosition.equals((Object)playerPos)) {
            this.lastTargetPathPosition = playerPos;
            this.targetProgressTime = now;
        }
        boolean pathing = BaritoneCompat.isPathing(this.baritone);
        boolean bl = retry = this.targetPathStartTime == 0L || !pathing && now - this.targetPathStartTime >= 1500L || pathing && now - this.targetProgressTime >= 7000L;
        if (!retry) {
            return;
        }
        if (pathing) {
            BaritoneCompat.cancelPathing(this.baritone);
        }
        BaritoneCompat.goNear(this.baritone, this.currentTarget, 1);
        this.targetPathStartTime = now;
        this.targetProgressTime = now;
        this.lastTargetPathPosition = playerPos;
    }

    private void resetTargetPath() {
        this.targetPathStartTime = 0L;
        this.targetProgressTime = 0L;
        this.lastTargetPathPosition = null;
    }

    private void handlePickup(long now) {
        if (this.pendingPickupPos == null || now - this.pickupStartTime >= 6000L) {
            this.pendingPickupPos = null;
            this.state = State.SEARCHING;
            return;
        }
        class_1542 drop = AnchorFarming.mc.field_1687.method_8390(class_1542.class, new class_238(this.pendingPickupPos).method_1014(6.0), item -> item.method_5805() && item.method_6983().method_31574(class_1802.field_23141)).stream().min(Comparator.comparingDouble(arg_0 -> ((class_746)AnchorFarming.mc.field_1724).method_5858(arg_0))).orElse(null);
        if (drop == null) {
            return;
        }
        if (AnchorFarming.mc.field_1724.method_5858((class_1297)drop) <= 2.25) {
            BaritoneCompat.cancelPathing(this.baritone);
            return;
        }
        if (!BaritoneCompat.isPathing(this.baritone)) {
            BaritoneCompat.goNear(this.baritone, drop.method_24515(), 0);
        }
    }

    private void handleSelling(long now) {
        if (this.baritone != null) {
            BaritoneCompat.cancelPathing(this.baritone);
            BaritoneCompat.cancelMining(this.baritone);
        }
        this.state = State.SELLING;
        class_437 class_4372 = AnchorFarming.mc.field_1755;
        if (!(class_4372 instanceof class_465)) {
            if (now - this.sellStartTime > 2000L) {
                this.sellStartTime = now;
                String cmd = this.sellCommand.getText().trim();
                if (cmd.startsWith("/")) {
                    cmd = cmd.substring(1);
                }
                if (!cmd.isEmpty()) {
                    AnchorFarming.mc.field_1724.field_3944.method_45730(cmd);
                    this.notifyMessage("\u00a7e\u041e\u0442\u043a\u0440\u044b\u0432\u0430\u0435\u043c \u043c\u0435\u043d\u044e \u043f\u0440\u043e\u0434\u0430\u0436\u0438 /" + cmd + "...");
                }
            }
            return;
        }
        class_465 handledScreen = (class_465)class_4372;
        class_1703 handler = handledScreen.method_17577();
        if (handler == null) {
            return;
        }
        if (now - this.lastSellSlotClickTime < 120L) {
            return;
        }
        class_1735 anchorSlot = null;
        for (class_1735 slot : handler.field_7761) {
            if (slot.field_7871 != AnchorFarming.mc.field_1724.method_31548() || !slot.method_7681() || !slot.method_7677().method_31574(class_1802.field_23141)) continue;
            anchorSlot = slot;
            break;
        }
        if (anchorSlot != null) {
            this.lastSellSlotClickTime = now;
            AnchorFarming.mc.field_1761.method_2906(handler.field_7763, anchorSlot.field_7874, 0, class_1713.field_7794, (class_1657)AnchorFarming.mc.field_1724);
            return;
        }
        class_1735 confirmSlot = this.findSellConfirmButton(handler);
        if (confirmSlot != null) {
            this.lastSellSlotClickTime = now;
            AnchorFarming.mc.field_1761.method_2906(handler.field_7763, confirmSlot.field_7874, 0, class_1713.field_7790, (class_1657)AnchorFarming.mc.field_1724);
        }
        AnchorFarming.mc.field_1724.method_7346();
        this.notifyMessage("\u00a7a\u042f\u043a\u043e\u0440\u044f \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0441\u0434\u0430\u043d\u044b \u0432 /sell!");
        this.lastFoundAnchorTime = now;
        this.state = State.SEARCHING;
    }

    private class_1735 findSellConfirmButton(class_1703 handler) {
        for (class_1735 slot : handler.field_7761) {
            class_1799 stack;
            String name;
            if (slot.field_7871 == AnchorFarming.mc.field_1724.method_31548() || !slot.method_7681() || !(name = (stack = slot.method_7677()).method_7964().getString().toLowerCase()).contains("\u043f\u0440\u043e\u0434\u0430\u0442\u044c") && !name.contains("sell") && !name.contains("\u043f\u043e\u0434\u0442\u0432\u0435\u0440\u0434\u0438\u0442\u044c") && !stack.method_31574(class_1802.field_8687) && !stack.method_31574(class_1802.field_8581)) continue;
            return slot;
        }
        return null;
    }

    private void performRtp(long now) {
        String cmd;
        if (this.baritone != null) {
            BaritoneCompat.cancelPathing(this.baritone);
            BaritoneCompat.cancelMining(this.baritone);
        }
        if ((cmd = this.rtpCommand.getText().trim()).startsWith("/")) {
            cmd = cmd.substring(1);
        }
        if (!cmd.isEmpty()) {
            AnchorFarming.mc.field_1724.field_3944.method_45730(cmd);
        }
        this.lastRtpTime = now;
        this.lastFoundAnchorTime = now;
        this.state = State.WAITING_RTP;
        this.currentTarget = null;
        this.foundAnchors.clear();
    }

    private void wanderAround() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        int dx = rnd.nextInt(-45, 46);
        int dz = rnd.nextInt(-45, 46);
        if (Math.abs(dx) < 15) {
            int n = dx = dx >= 0 ? 25 : -25;
        }
        if (Math.abs(dz) < 15) {
            dz = dz >= 0 ? 25 : -25;
        }
        int targetX = AnchorFarming.mc.field_1724.method_31477() + dx;
        int targetZ = AnchorFarming.mc.field_1724.method_31479() + dz;
        BaritoneCompat.goXZ(this.baritone, targetX, targetZ);
    }

    private void scanAnchorsAsync(class_2338 center, int radius) {
        if (AnchorFarming.mc.field_1687 == null) {
            return;
        }
        class_2338.class_2339 mutable = new class_2338.class_2339();
        int minX = center.method_10263() - radius;
        int maxX = center.method_10263() + radius;
        int minY = Math.max(AnchorFarming.mc.field_1687.method_31607(), center.method_10264() - 32);
        int maxY = Math.min(AnchorFarming.mc.field_1687.method_31607() + AnchorFarming.mc.field_1687.method_31605() - 1, center.method_10264() + 32);
        int minZ = center.method_10260() - radius;
        int maxZ = center.method_10260() + radius;
        int minChunkX = minX >> 4;
        int maxChunkX = maxX >> 4;
        int minChunkZ = minZ >> 4;
        int maxChunkZ = maxZ >> 4;
        for (int cx = minChunkX; cx <= maxChunkX && this.isState(); ++cx) {
            for (int cz = minChunkZ; cz <= maxChunkZ && this.isState(); ++cz) {
                if (!AnchorFarming.mc.field_1687.method_2935().method_12123(cx, cz)) continue;
                int startX = Math.max(minX, cx << 4);
                int endX = Math.min(maxX, (cx << 4) + 15);
                int startZ = Math.max(minZ, cz << 4);
                int endZ = Math.min(maxZ, (cz << 4) + 15);
                for (int x = startX; x <= endX; ++x) {
                    for (int z = startZ; z <= endZ; ++z) {
                        for (int y = minY; y <= maxY; ++y) {
                            class_2338 immutable;
                            mutable.method_10103(x, y, z);
                            if (!AnchorFarming.mc.field_1687.method_8320((class_2338)mutable).method_27852(class_2246.field_23152) || this.minedAnchors.contains(immutable = mutable.method_10062())) continue;
                            this.foundAnchors.add(immutable);
                        }
                    }
                }
            }
        }
    }

    private void purgeInvalidAnchors() {
        if (AnchorFarming.mc.field_1687 == null) {
            return;
        }
        this.foundAnchors.removeIf(pos -> {
            if (this.minedAnchors.contains(pos)) {
                return true;
            }
            if (AnchorFarming.mc.field_1687.method_2935().method_12123(pos.method_10263() >> 4, pos.method_10260() >> 4)) {
                return !AnchorFarming.mc.field_1687.method_8320(pos).method_27852(class_2246.field_23152);
            }
            return false;
        });
    }

    private boolean handleEating() {
        class_1269 result;
        if (AnchorFarming.mc.field_1724.method_7344().method_7586() >= 20) {
            this.stopEating();
            return false;
        }
        int foodSlot = this.findHotbarFoodSlot();
        if (foodSlot < 0) {
            int invFoodSlot = this.findInventoryFoodSlot();
            if (invFoodSlot >= 0) {
                AnchorFarming.mc.field_1761.method_2906(AnchorFarming.mc.field_1724.field_7498.field_7763, invFoodSlot, 8, class_1713.field_7791, (class_1657)AnchorFarming.mc.field_1724);
                foodSlot = 8;
            } else {
                this.stopEating();
                return false;
            }
        }
        if (!this.isEating) {
            this.isEating = true;
            this.previousSlotBeforeEat = AnchorFarming.mc.field_1724.method_31548().method_67532();
            AnchorFarming.mc.field_1724.method_31548().method_61496(foodSlot);
            if (this.baritone != null) {
                BaritoneCompat.pausePathing(this.baritone);
            }
        }
        AnchorFarming.mc.field_1690.field_1904.method_23481(true);
        if (!AnchorFarming.mc.field_1724.method_6115() && (result = AnchorFarming.mc.field_1761.method_2919((class_1657)AnchorFarming.mc.field_1724, class_1268.field_5808)).method_23665()) {
            AnchorFarming.mc.field_1724.method_6019(class_1268.field_5808);
        }
        return true;
    }

    private void stopEating() {
        if (!this.isEating) {
            return;
        }
        if (AnchorFarming.mc.field_1690 != null) {
            AnchorFarming.mc.field_1690.field_1904.method_23481(false);
        }
        if (AnchorFarming.mc.field_1724 != null && this.previousSlotBeforeEat >= 0) {
            AnchorFarming.mc.field_1724.method_31548().method_61496(this.previousSlotBeforeEat);
        }
        this.isEating = false;
        this.previousSlotBeforeEat = -1;
        if (this.baritone != null) {
            BaritoneCompat.resumePathing(this.baritone);
        }
    }

    private boolean hasPickaxe() {
        if (AnchorFarming.mc.field_1724 == null) {
            return false;
        }
        for (int i = 0; i < AnchorFarming.mc.field_1724.method_31548().method_5439(); ++i) {
            class_1799 stack = AnchorFarming.mc.field_1724.method_31548().method_5438(i);
            if (!this.isGoodPickaxe(stack)) continue;
            return true;
        }
        return false;
    }

    private boolean isGoodPickaxe(class_1799 stack) {
        if (stack.method_7960() || !stack.method_31573(class_3489.field_42614)) {
            return false;
        }
        if (stack.method_7963()) {
            int remaining = stack.method_7936() - stack.method_7919();
            return (double)remaining > (double)stack.method_7936() * 0.05;
        }
        return true;
    }

    private void ensurePickaxeSelected() {
        class_1799 stack;
        int i;
        if (AnchorFarming.mc.field_1724 == null) {
            return;
        }
        int hotbarPick = -1;
        int bestPriority = -1;
        for (i = 0; i < 9; ++i) {
            stack = AnchorFarming.mc.field_1724.method_31548().method_5438(i);
            if (!this.isGoodPickaxe(stack)) continue;
            int priority = stack.method_31574(class_1802.field_22024) ? 3 : (stack.method_31574(class_1802.field_8377) ? 2 : 1);
            if (priority <= bestPriority) continue;
            bestPriority = priority;
            hotbarPick = i;
        }
        if (hotbarPick >= 0) {
            AnchorFarming.mc.field_1724.method_31548().method_61496(hotbarPick);
            return;
        }
        for (i = 9; i < 36; ++i) {
            stack = AnchorFarming.mc.field_1724.method_31548().method_5438(i);
            if (!this.isGoodPickaxe(stack)) continue;
            int selected = AnchorFarming.mc.field_1724.method_31548().method_67532();
            AnchorFarming.mc.field_1761.method_2906(AnchorFarming.mc.field_1724.field_7498.field_7763, i, selected, class_1713.field_7791, (class_1657)AnchorFarming.mc.field_1724);
            break;
        }
    }

    private boolean hasFood() {
        if (AnchorFarming.mc.field_1724 == null) {
            return false;
        }
        for (int i = 0; i < AnchorFarming.mc.field_1724.method_31548().method_5439(); ++i) {
            class_1799 stack = AnchorFarming.mc.field_1724.method_31548().method_5438(i);
            if (stack.method_7960() || stack.method_58694(class_9334.field_50075) == null) continue;
            return true;
        }
        return false;
    }

    private int findHotbarFoodSlot() {
        if (AnchorFarming.mc.field_1724 == null) {
            return -1;
        }
        for (int i = 0; i < 9; ++i) {
            class_1799 stack = AnchorFarming.mc.field_1724.method_31548().method_5438(i);
            if (stack.method_7960() || stack.method_58694(class_9334.field_50075) == null) continue;
            return i;
        }
        return -1;
    }

    private int findInventoryFoodSlot() {
        if (AnchorFarming.mc.field_1724 == null) {
            return -1;
        }
        for (int i = 9; i < 36; ++i) {
            class_1799 stack = AnchorFarming.mc.field_1724.method_31548().method_5438(i);
            if (stack.method_7960() || stack.method_58694(class_9334.field_50075) == null) continue;
            return i;
        }
        return -1;
    }

    private int countAnchorsInInventory() {
        if (AnchorFarming.mc.field_1724 == null) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < AnchorFarming.mc.field_1724.method_31548().method_5439(); ++i) {
            class_1799 stack = AnchorFarming.mc.field_1724.method_31548().method_5438(i);
            if (stack.method_7960() || !stack.method_31574(class_1802.field_23141)) continue;
            count += stack.method_7947();
        }
        return count;
    }

    private void configureBaritone(boolean enable) {
        if (this.baritone == null) {
            return;
        }
        BaritoneCompat.setBooleanSetting("allowBreak", false);
        BaritoneCompat.setSetting("allowSprint", true);
        BaritoneCompat.setSetting("allowParkour", false);
        BaritoneCompat.setSetting("allowParkourAscend", false);
        BaritoneCompat.setSetting("allowParkourPlace", false);
        if (!enable) {
            BaritoneCompat.cancelMining(this.baritone);
            BaritoneCompat.cancelPathing(this.baritone);
            if (this.previousAllowBreak != null) {
                BaritoneCompat.setBooleanSetting("allowBreak", this.previousAllowBreak);
            }
            if (this.previousAllowSprint != null) {
                BaritoneCompat.setBooleanSetting("allowSprint", this.previousAllowSprint);
            }
            if (this.previousAllowParkour != null) {
                BaritoneCompat.setBooleanSetting("allowParkour", this.previousAllowParkour);
            }
            if (this.previousAllowParkourAscend != null) {
                BaritoneCompat.setBooleanSetting("allowParkourAscend", this.previousAllowParkourAscend);
            }
            if (this.previousAllowParkourPlace != null) {
                BaritoneCompat.setBooleanSetting("allowParkourPlace", this.previousAllowParkourPlace);
            }
        }
    }

    private void notifyMessage(String text) {
        if (AnchorFarming.mc.field_1705 != null) {
            AnchorFarming.mc.field_1705.method_1743().method_1812(QuickLogger.clientMessage((class_2561)class_2561.method_43470((String)text).method_27692(class_124.field_1080)));
        }
    }

    @EventHandler
    public void onRender(WorldRenderEvent event) {
        if (!this.esp.isValue() || AnchorFarming.mc.field_1687 == null || AnchorFarming.mc.field_1724 == null) {
            return;
        }
        for (class_2338 pos : this.foundAnchors) {
            if (this.minedAnchors.contains(pos)) continue;
            boolean isTarget = pos.equals((Object)this.currentTarget);
            int color = isTarget ? -872349816 : -861273857;
            float lineWidth = isTarget ? 2.2f : 1.4f;
            Render3DUtil.drawBox(new class_238(pos), color, lineWidth, true, true, false);
        }
    }

    private static enum State {
        SEARCHING,
        GOING_TO_ANCHOR,
        MINING,
        SELLING,
        PICKING_UP,
        EATING,
        WAITING_RTP;

    }
}

