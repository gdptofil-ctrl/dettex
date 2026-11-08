/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_1268
 *  net.minecraft.class_1269
 *  net.minecraft.class_1293
 *  net.minecraft.class_1294
 *  net.minecraft.class_1297
 *  net.minecraft.class_1541
 *  net.minecraft.class_1657
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1844
 *  net.minecraft.class_1922
 *  net.minecraft.class_1937
 *  net.minecraft.class_1972
 *  net.minecraft.class_2246
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_2374
 *  net.minecraft.class_238
 *  net.minecraft.class_2382
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_3489
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_3965
 *  net.minecraft.class_6880
 *  net.minecraft.class_9334
 */
package ru.recoin.implement.features.modules.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import net.minecraft.class_124;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1541;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_1922;
import net.minecraft.class_1937;
import net.minecraft.class_1972;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2374;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_3489;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_6880;
import net.minecraft.class_9334;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.common.QuickLogger;
import ru.recoin.common.util.other.BaritoneCompat;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.common.util.task.TaskPriority;
import ru.recoin.implement.events.player.RotationUpdateEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationConfig;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.SmoothMode;
import ru.recoin.implement.features.modules.misc.TelegramAPI;
import ru.recoin.implement.features.modules.player.AncientXRay;

public final class AncientFarmer
extends Module {
    public final SelectSetting searchMode = new SelectSetting("\u0420\u0435\u0436\u0438\u043c \u043f\u043e\u0438\u0441\u043a\u0430 \u0442\u0435\u0440\u0440\u0438\u0442\u043e\u0440\u0438\u0438", "\u0412\u044b\u0441\u043e\u0442\u0430 \u043f\u043e\u0438\u0441\u043a\u0430 \u043f\u043b\u043e\u0442\u043d\u043e\u0439 \u0442\u0435\u0440\u0440\u0438\u0442\u043e\u0440\u0438\u0438").value("\u041f\u043e\u0438\u0441\u043a \u0441\u0432\u0435\u0440\u0445\u0443", "\u041f\u043e\u0438\u0441\u043a \u0441\u043d\u0438\u0437\u0443").selected("\u041f\u043e\u0438\u0441\u043a \u0441\u0432\u0435\u0440\u0445\u0443");
    private final ExecutorService scanner = Executors.newSingleThreadExecutor(r -> {
        Thread thread2 = new Thread(r, "Recoin-AncientFarmer");
        thread2.setDaemon(true);
        return thread2;
    });
    private final AtomicBoolean scanning = new AtomicBoolean();
    private final RotationConfig rotation = new RotationConfig(new SmoothMode(), false, true);
    private State state = State.SEARCH;
    private Object baritone;
    private AncientXRay xray;
    private volatile class_238 territory;
    private class_2338 actionPos;
    private Angle requestedAngle;
    private long mineDelay;
    private int previousSlot = -1;
    private boolean consuming;

    public AncientFarmer() {
        super("AncientFarmer", "Ancient Farmer", ModuleCategory.PVE);
        this.setup(this.searchMode);
    }

    public static AncientFarmer getInstance() {
        return Instance.get(AncientFarmer.class);
    }

    @Override
    public void activate() {
        this.baritone = BaritoneCompat.getPrimaryBaritone();
        this.xray = AncientXRay.getInstance();
        this.configure(true);
        this.territory = null;
        this.actionPos = null;
        this.requestedAngle = null;
        this.state = State.SEARCH;
        this.mineDelay = System.currentTimeMillis();
    }

    @Override
    public void deactivate() {
        this.stopConsuming();
        RotationController.INSTANCE.release(this);
        this.requestedAngle = null;
        this.actionPos = null;
        this.territory = null;
        this.configure(false);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        boolean tntMayBeSpent;
        if (AncientFarmer.mc.field_1724 == null || AncientFarmer.mc.field_1687 == null || AncientFarmer.mc.field_1761 == null) {
            return;
        }
        Requirement missing = this.firstMissing();
        boolean bl = tntMayBeSpent = missing == Requirement.TNT && this.state != State.SEARCH;
        if (missing != null && !tntMayBeSpent) {
            this.message("\u0414\u043b\u044f \u0440\u0430\u0431\u043e\u0442\u044b \u043c\u043e\u0434\u0443\u043b\u044f " + missing.description + "!");
            this.setState(false);
            return;
        }
        if (this.consumeIfNeeded()) {
            BaritoneCompat.pausePathing(this.baritone);
            return;
        }
        BaritoneCompat.resumePathing(this.baritone);
        if (this.xray == null) {
            this.xray = AncientXRay.getInstance();
        }
        if (this.xray == null) {
            return;
        }
        if (!this.xray.isState()) {
            this.xray.setState(true);
            return;
        }
        List<class_2338> debris = this.debris();
        boolean near = this.territory != null && AncientFarmer.mc.field_1724.method_24515().method_19771((class_2382)class_2338.method_49638((class_2374)this.territory.method_1005()), 2.5);
        boolean primed = !AncientFarmer.mc.field_1687.method_8390(class_1541.class, AncientFarmer.mc.field_1724.method_5829().method_1014(8.0), entity -> true).isEmpty();
        this.actionPos = null;
        switch (this.state.ordinal()) {
            case 0: {
                this.search(debris, near);
                break;
            }
            case 1: {
                this.handleTnt(primed);
                break;
            }
            case 2: {
                this.retreat();
                break;
            }
            case 3: {
                this.mine(debris);
            }
        }
    }

    @EventHandler
    public void onRotation(RotationUpdateEvent event) {
        if (event.getType() == 0 && this.requestedAngle != null) {
            RotationController.INSTANCE.rotateTo(this.requestedAngle, 2, this.rotation, TaskPriority.HIGH_IMPORTANCE_1, this);
        }
    }

    @EventHandler
    public void onRender(WorldRenderEvent event) {
        if (this.actionPos != null) {
            Render3DUtil.drawBox(new class_238(this.actionPos), -857318842, 1.0f, true, true, false);
        }
    }

    private void search(List<class_2338> debris, boolean near) {
        if (!debris.isEmpty()) {
            this.message("\u0412\u0441\u043a\u0430\u043f\u044b\u0432\u0430\u0435\u043c \u043e\u0431\u043b\u043e\u043c\u043a\u0438 \u043d\u0430\u0439\u0434\u0435\u043d\u043d\u044b\u0435 \u043f\u043e \u043f\u0443\u0442\u0438");
            this.state = State.MINE;
            this.mineDelay = System.currentTimeMillis();
            return;
        }
        if (this.territory == null) {
            if (AncientFarmer.mc.field_1724.field_6012 > 20 && this.scanning.compareAndSet(false, true)) {
                this.message("\u041f\u0435\u0440\u0435\u0445\u043e\u0434\u0438\u043c \u043a \u043f\u043e\u0438\u0441\u043a\u0443 \u043d\u043e\u0432\u043e\u0439 \u0442\u0435\u0440\u0440\u0438\u0442\u043e\u0440\u0438\u0438.");
                this.scanner.execute(() -> {
                    try {
                        this.territory = this.findTerritory();
                    }
                    finally {
                        this.scanning.set(false);
                    }
                });
            }
            return;
        }
        if (near) {
            BaritoneCompat.cancelPathing(this.baritone);
            this.state = State.TNT;
        } else if (!BaritoneCompat.isPathing(this.baritone)) {
            BaritoneCompat.goBlock(this.baritone, class_2338.method_49638((class_2374)this.territory.method_1005()));
        }
    }

    private void handleTnt(boolean primed) {
        class_2338 target;
        if (primed) {
            this.state = State.RETREAT;
            return;
        }
        double reach = AncientFarmer.mc.field_1724.method_55754();
        class_2338 placed = this.positions(AncientFarmer.mc.field_1724.method_24515(), (int)reach, (int)reach, (int)reach).stream().filter(pos -> AncientFarmer.mc.field_1687.method_8320(pos).method_27852(class_2246.field_10375)).min(Comparator.comparingDouble(this::distance)).orElse(null);
        if (placed != null) {
            this.actionPos = placed;
            class_3965 hit = this.visibleFace(placed, reach);
            if (hit != null && this.selectHotbar(stack -> stack.method_31574(class_1802.field_8884))) {
                this.interact(hit);
            }
            return;
        }
        class_243 eye = AncientFarmer.mc.field_1724.method_33571();
        this.actionPos = target = (class_2338)this.positions(AncientFarmer.mc.field_1724.method_24515(), (int)reach, (int)reach, (int)reach).stream().filter(pos -> AncientFarmer.mc.field_1687.method_8320(pos).method_26215()).filter(pos -> AncientFarmer.mc.field_1687.method_8320(pos.method_10074()).method_26212((class_1922)AncientFarmer.mc.field_1687, pos.method_10074())).filter(pos -> !AncientFarmer.mc.field_1724.method_5829().method_18804(AncientFarmer.mc.field_1724.method_18798()).method_1014(0.1).method_994(new class_238(pos))).filter(pos -> eye.method_1022(class_243.method_24955((class_2382)pos)) <= reach).filter(pos -> class_243.method_24955((class_2382)pos).method_1020((class_243)eye).method_1029().field_1351 <= 0.0).min(Comparator.comparingDouble(this::distance)).orElse(null);
        if (target != null) {
            class_3965 hit = this.raycastTop(target.method_10074());
            if (hit != null && this.selectHotbar(stack -> stack.method_31574(class_1802.field_8626))) {
                this.interact(hit);
            }
            return;
        }
        if (!BaritoneCompat.isPathing(this.baritone)) {
            class_2338 stand = this.positions(AncientFarmer.mc.field_1724.method_24515(), 16, 8, 16).stream().filter(pos -> !pos.equals((Object)AncientFarmer.mc.field_1724.method_24515())).filter(this::canStand).min(Comparator.comparingDouble(this::distance)).orElse(null);
            if (stand == null) {
                this.state = State.SEARCH;
            } else {
                BaritoneCompat.goBlock(this.baritone, stand);
            }
        }
    }

    private void retreat() {
        List<class_1541> burning = AncientFarmer.mc.field_1687.method_8390(class_1541.class, AncientFarmer.mc.field_1724.method_5829().method_1014(32.0), entity -> true);
        if (burning.isEmpty()) {
            this.message("\u041e\u0436\u0438\u0434\u0430\u0435\u043c \u043e\u0431\u043b\u043e\u043c\u043a\u0438, \u0438 \u043d\u0430\u0447\u0438\u043d\u0430\u0435\u043c \u0432\u0441\u043a\u0430\u043f\u044b\u0432\u0430\u0442\u044c");
            BaritoneCompat.cancelPathing(this.baritone);
            this.territory = null;
            this.state = State.MINE;
            this.mineDelay = System.currentTimeMillis();
        } else if (!BaritoneCompat.isPathing(this.baritone)) {
            BaritoneCompat.runAway(this.baritone, 20.0, (class_2338[])burning.stream().map(class_1297::method_24515).toArray(class_2338[]::new));
        }
    }

    private void mine(List<class_2338> debris) {
        if (System.currentTimeMillis() - this.mineDelay < 1000L) {
            return;
        }
        if (!debris.isEmpty()) {
            if (!BaritoneCompat.isMining(this.baritone)) {
                BaritoneCompat.minePositions(this.baritone, class_2246.field_22109, debris);
                TelegramAPI telegram = TelegramAPI.getInstance();
                if (telegram != null) {
                    telegram.sendNotification("\u26cf\ufe0f AncientFarmer \u2014 \u041d\u0430\u0439\u0434\u0435\u043d\u044b \u0434\u0440\u0435\u0432\u043d\u0438\u0435 \u043e\u0431\u043b\u043e\u043c\u043a\u0438!\n\n\ud83d\udccd \u041f\u043e\u0437\u0438\u0446\u0438\u0439 \u0434\u043b\u044f \u0434\u043e\u0431\u044b\u0447\u0438: " + debris.size());
                }
            }
        } else if (!BaritoneCompat.isMining(this.baritone)) {
            this.state = State.SEARCH;
        }
    }

    private class_238 findTerritory() {
        if (AncientFarmer.mc.field_1724 == null || AncientFarmer.mc.field_1687 == null) {
            return null;
        }
        int reach = Math.max(16, ((Integer)AncientFarmer.mc.field_1690.method_42503().method_41753() - 1) * 16);
        class_2338 feet = AncientFarmer.mc.field_1724.method_24515();
        int bottom = this.searchMode.isSelected("\u041f\u043e\u0438\u0441\u043a \u0441\u0432\u0435\u0440\u0445\u0443") ? 90 : 20;
        int top = this.searchMode.isSelected("\u041f\u043e\u0438\u0441\u043a \u0441\u0432\u0435\u0440\u0445\u0443") ? 178 : 60;
        class_238 best = null;
        double bestScore = -1.0;
        double bestFill = 0.0;
        for (int x = feet.method_10263() - reach; x <= feet.method_10263() + reach && this.isState(); x += 8) {
            for (int z = feet.method_10260() - reach; z <= feet.method_10260() + reach; z += 8) {
                if (!AncientFarmer.mc.field_1687.method_2935().method_12123(x >> 4, z >> 4) || !AncientFarmer.mc.field_1687.method_8621().method_11952(new class_2338(x - 20, bottom, z - 20)) || !AncientFarmer.mc.field_1687.method_8621().method_11952(new class_2338(x + 20, top, z + 20))) continue;
                for (int y = bottom; y <= top; y += 8) {
                    int solid = 0;
                    int total = 0;
                    boolean badBiome = false;
                    for (int dx = -20; dx <= 20 && !badBiome; dx += 4) {
                        block4: for (int dy = -20; dy <= 20 && !badBiome; dy += 4) {
                            for (int dz = -20; dz <= 20; dz += 4) {
                                class_2338 sample = new class_2338(x + dx, y + dy, z + dz);
                                class_6880 biome = AncientFarmer.mc.field_1687.method_23753(sample);
                                if (biome.method_40225(class_1972.field_23859) || biome.method_40225(class_1972.field_22076)) {
                                    badBiome = true;
                                    continue block4;
                                }
                                ++total;
                                if (AncientFarmer.mc.field_1687.method_8320(sample).method_26215() || !AncientFarmer.mc.field_1687.method_8320(sample).method_26212((class_1922)AncientFarmer.mc.field_1687, sample)) continue;
                                ++solid;
                            }
                        }
                    }
                    if (badBiome || total == 0) continue;
                    double fill = (double)solid / (double)total;
                    double score = fill - feet.method_40081((double)x, (double)y, (double)z) * 1.0E-9;
                    class_2338 anchor = this.firstSolid(new class_2338(x, y, z), 20);
                    if (!(score > bestScore) || anchor == null) continue;
                    bestScore = score;
                    bestFill = fill;
                    best = new class_238(class_243.method_24954((class_2382)anchor.method_10069(-20, -20, -20)), class_243.method_24954((class_2382)anchor.method_10069(20, 20, 20)));
                }
            }
        }
        if (best != null) {
            long success = Math.round(bestFill * 100.0);
            long distance = Math.round(Math.sqrt(feet.method_19770((class_2374)best.method_1005())));
            this.message("\u0423\u0441\u043f\u0435\u0448\u043d\u043e\u0441\u0442\u044c: \u00a7c" + success + "%\u00a77, \u0434\u043e \u043d\u0435\u0451 \u00a7c" + distance + "\u00a77 \u0431\u043b\u043e\u043a\u043e\u0432");
        }
        return best;
    }

    private class_2338 firstSolid(class_2338 center, int radius) {
        for (class_2338 pos : class_2338.method_10097((class_2338)center.method_10069(-radius, -radius, -radius), (class_2338)center.method_10069(radius, radius, radius))) {
            if (AncientFarmer.mc.field_1687.method_8320(pos).method_26215() || !AncientFarmer.mc.field_1687.method_8320(pos).method_26212((class_1922)AncientFarmer.mc.field_1687, pos)) continue;
            return pos.method_10062();
        }
        return null;
    }

    private List<class_2338> debris() {
        if (this.xray == null) {
            return List.of();
        }
        Set<class_2338> found = this.xray.getFoundDebris();
        ArrayList<class_2338> result = new ArrayList<class_2338>();
        for (class_2338 pos : found) {
            if (BaritoneCompat.isMineBlacklisted(this.baritone, pos)) continue;
            result.add(pos);
        }
        return result;
    }

    private boolean consumeIfNeeded() {
        class_1269 result;
        class_1293 fire = AncientFarmer.mc.field_1724.method_6112(class_1294.field_5918);
        int slot = -1;
        if (fire == null || fire.method_5584() <= 100) {
            slot = this.hotbar(this::isFirePotion);
        }
        if (slot == -1 && AncientFarmer.mc.field_1724.method_7344().method_7586() <= 17) {
            slot = this.hotbar(stack -> stack.method_58694(class_9334.field_50075) != null);
        }
        if (slot == -1) {
            this.stopConsuming();
            return false;
        }
        if (!this.consuming) {
            this.consuming = true;
            this.previousSlot = AncientFarmer.mc.field_1724.method_31548().method_67532();
            AncientFarmer.mc.field_1724.method_31548().method_61496(slot);
        }
        AncientFarmer.mc.field_1690.field_1904.method_23481(true);
        if (!AncientFarmer.mc.field_1724.method_6115() && (result = AncientFarmer.mc.field_1761.method_2919((class_1657)AncientFarmer.mc.field_1724, class_1268.field_5808)).method_23665()) {
            AncientFarmer.mc.field_1724.method_6019(class_1268.field_5808);
        }
        return true;
    }

    private void stopConsuming() {
        if (!this.consuming) {
            return;
        }
        if (AncientFarmer.mc.field_1690 != null) {
            AncientFarmer.mc.field_1690.field_1904.method_23481(false);
        }
        if (AncientFarmer.mc.field_1724 != null && this.previousSlot >= 0) {
            AncientFarmer.mc.field_1724.method_31548().method_61496(this.previousSlot);
        }
        this.consuming = false;
        this.previousSlot = -1;
    }

    private void interact(class_3965 hit) {
        this.requestedAngle = AngleUtil.calculateAngle(hit.method_17784());
        Angle delta = AngleUtil.calculateDelta(RotationController.INSTANCE.getServerAngle(), this.requestedAngle);
        if (Math.abs(delta.getYaw()) > 1.5f || Math.abs(delta.getPitch()) > 1.5f || AncientFarmer.mc.field_1724.field_6012 % 5 != 0) {
            return;
        }
        class_1269 result = AncientFarmer.mc.field_1761.method_2896(AncientFarmer.mc.field_1724, class_1268.field_5808, hit);
        if (result.method_23665()) {
            AncientFarmer.mc.field_1724.method_6104(class_1268.field_5808);
        }
    }

    private class_3965 visibleFace(class_2338 pos, double reach) {
        class_243 eye = AncientFarmer.mc.field_1724.method_33571();
        return Arrays.stream(class_2350.values()).map(side -> class_243.method_24953((class_2382)pos).method_1031((double)side.method_10148() * 0.45, (double)side.method_10164() * 0.45, (double)side.method_10165() * 0.45)).filter(point -> eye.method_1022(point) <= reach).map(point -> AncientFarmer.mc.field_1687.method_17742(new class_3959(eye, point, class_3959.class_3960.field_17559, class_3959.class_242.field_1348, (class_1297)AncientFarmer.mc.field_1724))).filter(hit -> hit.method_17783() == class_239.class_240.field_1332 && hit.method_17777().equals((Object)pos)).min(Comparator.comparingDouble(hit -> eye.method_1025(hit.method_17784()))).orElse(null);
    }

    private class_3965 raycastTop(class_2338 support) {
        class_243 point = class_243.method_24955((class_2382)support.method_10084());
        class_3965 hit = AncientFarmer.mc.field_1687.method_17742(new class_3959(AncientFarmer.mc.field_1724.method_33571(), point, class_3959.class_3960.field_17559, class_3959.class_242.field_1348, (class_1297)AncientFarmer.mc.field_1724));
        return hit.method_17783() == class_239.class_240.field_1332 && hit.method_17777().equals((Object)support) ? new class_3965(point, class_2350.field_11036, support, false) : null;
    }

    private boolean selectHotbar(Predicate<class_1799> predicate) {
        int slot = this.hotbar(predicate);
        if (slot < 0) {
            return false;
        }
        AncientFarmer.mc.field_1724.method_31548().method_61496(slot);
        return true;
    }

    private int hotbar(Predicate<class_1799> predicate) {
        for (int slot = 0; slot < 9; ++slot) {
            if (!predicate.test(AncientFarmer.mc.field_1724.method_31548().method_5438(slot))) continue;
            return slot;
        }
        return -1;
    }

    private boolean isFirePotion(class_1799 stack) {
        class_1844 contents = (class_1844)stack.method_58694(class_9334.field_49651);
        if (contents == null) {
            return false;
        }
        for (class_1293 effect : contents.method_57397()) {
            if (!effect.method_5579().equals((Object)class_1294.field_5918)) continue;
            return true;
        }
        return false;
    }

    private Requirement firstMissing() {
        for (Requirement requirement : Requirement.values()) {
            if (requirement.available(this)) continue;
            return requirement;
        }
        return null;
    }

    private List<class_2338> positions(class_2338 center, int xz, int y, int zRadius) {
        ArrayList<class_2338> result = new ArrayList<class_2338>();
        for (class_2338 pos : class_2338.method_10097((class_2338)center.method_10069(-xz, -y, -zRadius), (class_2338)center.method_10069(xz, y, zRadius))) {
            result.add(pos.method_10062());
        }
        return result;
    }

    private boolean canStand(class_2338 pos) {
        return AncientFarmer.mc.field_1687.method_8320(pos.method_10074()).method_26212((class_1922)AncientFarmer.mc.field_1687, pos.method_10074()) && AncientFarmer.mc.field_1687.method_8320(pos).method_26215() && AncientFarmer.mc.field_1687.method_8320(pos.method_10084()).method_26215();
    }

    private double distance(class_2338 pos) {
        return AncientFarmer.mc.field_1724.method_24515().method_10262((class_2382)pos);
    }

    private void configure(boolean enabled) {
        if (this.baritone == null) {
            return;
        }
        BaritoneCompat.cancelMining(this.baritone);
        BaritoneCompat.cancelPathing(this.baritone);
        BaritoneCompat.setSetting("blockFreeLook", true);
        BaritoneCompat.setSetting("allowBreak", true);
        BaritoneCompat.setSetting("creative", enabled);
        BaritoneCompat.setSetting("turnSpeed", Float.valueOf(60.0f));
        BaritoneCompat.setSetting("randomLooking", 1.0);
        BaritoneCompat.setSetting("randomLooking113", 1.0);
        this.territory = null;
        this.state = State.SEARCH;
    }

    private void message(String text) {
        if (AncientFarmer.mc.field_1705 != null) {
            AncientFarmer.mc.field_1705.method_1743().method_1812(QuickLogger.clientMessage((class_2561)class_2561.method_43470((String)text).method_27692(class_124.field_1080)));
        }
    }

    private static enum State {
        SEARCH,
        TNT,
        RETREAT,
        MINE;

    }

    private static enum Requirement {
        FLY("\u043d\u0435\u043e\u0431\u0445\u043e\u0434\u0438\u043c\u043e \u0432\u043a\u043b\u044e\u0447\u0438\u0442\u044c \u0440\u0435\u0436\u0438\u043c \u043f\u043e\u043b\u0451\u0442\u0430 (/fly)"),
        NETHER("\u043d\u0435\u043e\u0431\u0445\u043e\u0434\u0438\u043c\u043e \u043d\u0430\u0445\u043e\u0434\u0438\u0442\u044c\u0441\u044f \u0432 \u041d\u0435\u0437\u0435\u0440\u0435"),
        FOOD("\u0432 \u0445\u043e\u0442\u0431\u0430\u0440\u0435 \u0434\u043e\u043b\u0436\u043d\u0430 \u0431\u044b\u0442\u044c \u0435\u0434\u0430"),
        TNT("\u0432 \u0445\u043e\u0442\u0431\u0430\u0440\u0435 \u0434\u043e\u043b\u0436\u0435\u043d \u0431\u044b\u0442\u044c \u0434\u0438\u043d\u0430\u043c\u0438\u0442"),
        FLINT("\u0432 \u0445\u043e\u0442\u0431\u0430\u0440\u0435 \u0434\u043e\u043b\u0436\u043d\u043e \u0431\u044b\u0442\u044c \u043e\u0433\u043d\u0438\u0432\u043e"),
        PICKAXE("\u0432 \u0445\u043e\u0442\u0431\u0430\u0440\u0435 \u0434\u043e\u043b\u0436\u043d\u0430 \u0431\u044b\u0442\u044c \u043a\u0438\u0440\u043a\u0430 \u0441 \u043f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u044c\u044e \u0431\u043e\u043b\u044c\u0448\u0435 5%"),
        FIRE_RESISTANCE("\u0432 \u0445\u043e\u0442\u0431\u0430\u0440\u0435 \u0434\u043e\u043b\u0436\u043d\u0430 \u0431\u044b\u0442\u044c \u043e\u0433\u043d\u0435\u0441\u0442\u043e\u0439\u043a\u043e\u0441\u0442\u044c");

        private final String description;

        private Requirement(String description) {
            this.description = description;
        }

        private boolean available(AncientFarmer farmer) {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> AncientFarmer.mc.field_1724.method_31549().field_7479;
                case 1 -> {
                    if (AncientFarmer.mc.field_1687.method_27983() == class_1937.field_25180) {
                        yield true;
                    }
                    yield false;
                }
                case 2 -> {
                    if (farmer.hotbar(stack -> stack.method_58694(class_9334.field_50075) != null) >= 0) {
                        yield true;
                    }
                    yield false;
                }
                case 3 -> {
                    if (farmer.hotbar(stack -> stack.method_31574(class_1802.field_8626)) >= 0) {
                        yield true;
                    }
                    yield false;
                }
                case 4 -> {
                    if (farmer.hotbar(stack -> stack.method_31574(class_1802.field_8884)) >= 0) {
                        yield true;
                    }
                    yield false;
                }
                case 5 -> {
                    if (farmer.hotbar(stack -> stack.method_31573(class_3489.field_42614) && (double)(stack.method_7936() - stack.method_7919()) > (double)stack.method_7936() * 0.05) >= 0) {
                        yield true;
                    }
                    yield false;
                }
                case 6 -> farmer.hotbar(farmer::isFirePotion) >= 0;
            };
        }
    }
}

