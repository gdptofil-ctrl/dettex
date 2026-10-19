/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1297
 *  net.minecraft.class_1542
 *  net.minecraft.class_1548
 *  net.minecraft.class_1657
 *  net.minecraft.class_1661
 *  net.minecraft.class_1713
 *  net.minecraft.class_1735
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1937
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_238
 *  net.minecraft.class_2382
 *  net.minecraft.class_243
 *  net.minecraft.class_2586
 *  net.minecraft.class_2595
 *  net.minecraft.class_2596
 *  net.minecraft.class_2663
 *  net.minecraft.class_3965
 *  net.minecraft.class_476
 *  net.minecraft.class_746
 */
package ru.recoin.implement.features.modules.misc;

import java.util.Comparator;
import java.util.List;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1542;
import net.minecraft.class_1548;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2586;
import net.minecraft.class_2595;
import net.minecraft.class_2596;
import net.minecraft.class_2663;
import net.minecraft.class_3965;
import net.minecraft.class_476;
import net.minecraft.class_746;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.common.util.other.BaritoneCompat;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.DrawEvent;

public final class CreeperFarm
extends Module {
    private static final class_2338 REGION_MIN = new class_2338(2220, 14, 2000);
    private static final class_2338 REGION_MAX = new class_2338(2320, 15, 2100);
    private static final int UNLOAD_AT = 64;
    private static final long ACTION_DELAY_MS = 160L;
    private static final long COMMAND_TIMEOUT_MS = 3000L;
    private final BooleanSetting unloadGunpowder = new BooleanSetting("Unload Gunpowder", "Store gunpowder after collecting more than one stack").setValue(false);
    private Object baritone;
    private State state = State.FARMING;
    private class_2338 patrolTarget;
    private class_2338 chestTarget;
    private int patrolIndex;
    private long lastJumpTick;
    private long nextActionAt;
    private long unloadStartedAt;
    private int kills;
    private int collectedGunpowder;
    private int lastGunpowderCount;
    private long startedAt;

    public CreeperFarm() {
        super("CreeperFarm", "Creeper Farm", ModuleCategory.MISC);
        this.setup(this.unloadGunpowder);
    }

    @Override
    public void activate() {
        this.baritone = BaritoneCompat.getPrimaryBaritone();
        this.state = State.FARMING;
        this.patrolTarget = null;
        this.chestTarget = null;
        this.patrolIndex = 0;
        this.lastJumpTick = 0L;
        this.nextActionAt = 0L;
        this.unloadStartedAt = 0L;
        this.kills = 0;
        this.collectedGunpowder = 0;
        this.lastGunpowderCount = this.countGunpowder();
        this.startedAt = System.currentTimeMillis();
    }

    @Override
    public void deactivate() {
        BaritoneCompat.cancelPathing(this.baritone);
        if (CreeperFarm.mc.field_1724 != null && CreeperFarm.mc.field_1755 instanceof class_476) {
            CreeperFarm.mc.field_1724.method_7346();
        }
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        class_2663 status;
        if (event.isSend() || CreeperFarm.mc.field_1687 == null) {
            return;
        }
        class_2596<?> class_25962 = event.getPacket();
        if (class_25962 instanceof class_2663 && (status = (class_2663)class_25962).method_11470() == 3 && status.method_11469((class_1937)CreeperFarm.mc.field_1687) instanceof class_1548) {
            ++this.kills;
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        class_1548 nearest;
        if (CreeperFarm.mc.field_1724 == null || CreeperFarm.mc.field_1687 == null || CreeperFarm.mc.field_1761 == null) {
            return;
        }
        this.updateCollectedGunpowder();
        if (!this.isInsideFarm(CreeperFarm.mc.field_1724.method_24515())) {
            BaritoneCompat.cancelPathing(this.baritone);
            return;
        }
        this.autoJump();
        if (System.currentTimeMillis() < this.nextActionAt) {
            return;
        }
        if (this.state == State.UNLOAD_CLAN || this.state == State.UNLOAD_ENDER || this.state == State.UNLOAD_CHEST) {
            this.tickUnload();
            return;
        }
        if (this.unloadGunpowder.isValue() && this.countGunpowder() > 64) {
            this.startUnload();
            return;
        }
        List<class_1548> creepers = this.creepers();
        class_1548 class_15482 = nearest = creepers.isEmpty() ? null : creepers.getFirst();
        if (nearest != null) {
            this.patrolTarget = null;
            this.tickCreeper(nearest);
            return;
        }
        class_1542 loot = this.nearestSafeLoot();
        if (loot != null) {
            BaritoneCompat.goNear(this.baritone, loot.method_24515(), 1);
            this.state = State.COLLECTING_LOOT;
            return;
        }
        this.tickPatrol();
    }

    private void tickCreeper(class_1548 creeper) {
        this.state = State.FARMING;
        double distance = CreeperFarm.mc.field_1724.method_5739((class_1297)creeper);
        if (distance > 4.5) {
            BaritoneCompat.goNear(this.baritone, creeper.method_24515(), 4);
            return;
        }
        BaritoneCompat.cancelPathing(this.baritone);
        if (CreeperFarm.mc.field_1724.method_7261(0.4f) >= 0.9f) {
            CreeperFarm.mc.field_1761.method_2918((class_1657)CreeperFarm.mc.field_1724, (class_1297)creeper);
            CreeperFarm.mc.field_1724.method_6104(class_1268.field_5808);
            this.nextActionAt = System.currentTimeMillis() + 160L;
        }
    }

    private void tickPatrol() {
        this.state = State.PATROLLING;
        if (this.patrolTarget == null) {
            this.patrolTarget = this.patrolPoint(this.patrolIndex++);
        }
        BaritoneCompat.goNear(this.baritone, this.patrolTarget, 4);
        if (CreeperFarm.mc.field_1724.method_24515().method_19771((class_2382)this.patrolTarget, 5.0)) {
            this.patrolTarget = null;
        }
    }

    private void startUnload() {
        this.state = State.UNLOAD_CLAN;
        this.chestTarget = null;
        this.unloadStartedAt = System.currentTimeMillis();
        CreeperFarm.mc.field_1724.field_3944.method_45730("clan storage");
        this.nextActionAt = System.currentTimeMillis() + 500L;
    }

    private void tickUnload() {
        if (this.countGunpowder() == 0) {
            this.finishUnload();
            return;
        }
        if (CreeperFarm.mc.field_1755 instanceof class_476) {
            this.transferGunpowder();
            return;
        }
        long elapsed = System.currentTimeMillis() - this.unloadStartedAt;
        if (this.state == State.UNLOAD_CLAN && elapsed >= 3000L) {
            this.state = State.UNLOAD_ENDER;
            this.unloadStartedAt = System.currentTimeMillis();
            CreeperFarm.mc.field_1724.field_3944.method_45730("ec");
            this.nextActionAt = System.currentTimeMillis() + 500L;
            return;
        }
        if (this.state == State.UNLOAD_ENDER && elapsed >= 3000L) {
            this.state = State.UNLOAD_CHEST;
            this.chestTarget = null;
            this.nextActionAt = System.currentTimeMillis();
        }
        if (this.state != State.UNLOAD_CHEST) {
            return;
        }
        if (this.chestTarget == null || !this.hasFreeSlot(this.chestTarget)) {
            this.chestTarget = this.nearestAvailableChest();
        }
        if (this.chestTarget == null) {
            this.state = State.FARMING;
            return;
        }
        if (CreeperFarm.mc.field_1724.method_24515().method_19771((class_2382)this.chestTarget, 4.5)) {
            CreeperFarm.mc.field_1761.method_2896(CreeperFarm.mc.field_1724, class_1268.field_5808, new class_3965(class_243.method_24953((class_2382)this.chestTarget), class_2350.field_11036, this.chestTarget, false));
            this.nextActionAt = System.currentTimeMillis() + 500L;
        } else {
            BaritoneCompat.goNear(this.baritone, this.chestTarget, 3);
        }
    }

    private void transferGunpowder() {
        for (class_1735 slot : CreeperFarm.mc.field_1724.field_7512.field_7761) {
            if (!(slot.field_7871 instanceof class_1661) || !slot.method_7677().method_31574(class_1802.field_8054)) continue;
            CreeperFarm.mc.field_1761.method_2906(CreeperFarm.mc.field_1724.field_7512.field_7763, slot.field_7874, 1, class_1713.field_7794, (class_1657)CreeperFarm.mc.field_1724);
            this.nextActionAt = System.currentTimeMillis() + 160L;
            return;
        }
        CreeperFarm.mc.field_1724.method_7346();
        this.finishUnload();
    }

    private void finishUnload() {
        BaritoneCompat.cancelPathing(this.baritone);
        this.state = State.FARMING;
        this.chestTarget = null;
        this.unloadStartedAt = 0L;
        this.nextActionAt = System.currentTimeMillis() + 250L;
    }

    private void autoJump() {
        long worldTick = CreeperFarm.mc.field_1687.method_75260();
        if (!CreeperFarm.mc.field_1724.method_24828() || CreeperFarm.mc.field_1724.field_5976 || CreeperFarm.mc.field_1690.field_1903.method_1434() || worldTick - this.lastJumpTick <= 2L) {
            return;
        }
        this.lastJumpTick = worldTick;
        CreeperFarm.mc.field_1724.method_6043();
    }

    private List<class_1548> creepers() {
        return CreeperFarm.mc.field_1687.method_8390(class_1548.class, new class_238(class_243.method_24954((class_2382)REGION_MIN), class_243.method_24954((class_2382)REGION_MAX.method_10069(1, 1, 1))), creeper -> creeper.method_5805() && creeper.method_6032() > 0.0f).stream().sorted(Comparator.comparingDouble(arg_0 -> ((class_746)CreeperFarm.mc.field_1724).method_5739(arg_0))).toList();
    }

    private class_1542 nearestSafeLoot() {
        return CreeperFarm.mc.field_1687.method_8390(class_1542.class, new class_238(class_243.method_24954((class_2382)REGION_MIN), class_243.method_24954((class_2382)REGION_MAX.method_10069(1, 1, 1))), item -> item.method_6983().method_31574(class_1802.field_8054) || item.method_6983().method_31574(class_1802.field_8287)).stream().filter(item -> this.creepers().stream().noneMatch(creeper -> creeper.method_5858((class_1297)item) <= 36.0)).min(Comparator.comparingDouble(item -> CreeperFarm.mc.field_1724.method_5858((class_1297)item))).orElse(null);
    }

    private class_2338 patrolPoint(int index) {
        int chunksX = (REGION_MAX.method_10263() >> 4) - (REGION_MIN.method_10263() >> 4) + 1;
        int chunksZ = (REGION_MAX.method_10260() >> 4) - (REGION_MIN.method_10260() >> 4) + 1;
        int total = chunksX * chunksZ;
        int point = Math.floorMod(index, total);
        int chunkX = (REGION_MIN.method_10263() >> 4) + point / chunksZ;
        int chunkZ = (REGION_MIN.method_10260() >> 4) + point % chunksZ;
        return new class_2338(this.MathHelperClamp(chunkX * 16 + 8, REGION_MIN.method_10263(), REGION_MAX.method_10263()), REGION_MIN.method_10264(), this.MathHelperClamp(chunkZ * 16 + 8, REGION_MIN.method_10260(), REGION_MAX.method_10260()));
    }

    private int MathHelperClamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    private class_2338 nearestAvailableChest() {
        class_2338 closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (class_2338 pos : class_2338.method_10097((class_2338)REGION_MIN, (class_2338)REGION_MAX)) {
            double distance;
            if (!this.hasFreeSlot(pos) || !((distance = CreeperFarm.mc.field_1724.method_5707(class_243.method_24953((class_2382)pos))) < closestDistance)) continue;
            closest = pos.method_10062();
            closestDistance = distance;
        }
        return closest;
    }

    private boolean hasFreeSlot(class_2338 pos) {
        class_2586 class_25862 = CreeperFarm.mc.field_1687.method_8321(pos);
        if (!(class_25862 instanceof class_2595)) {
            return false;
        }
        class_2595 chest = (class_2595)class_25862;
        for (int i = 0; i < chest.method_5439(); ++i) {
            if (!chest.method_5438(i).method_7960()) continue;
            return true;
        }
        return false;
    }

    private int countGunpowder() {
        if (CreeperFarm.mc.field_1724 == null) {
            return 0;
        }
        int total = 0;
        for (int i = 0; i < CreeperFarm.mc.field_1724.method_31548().method_5439(); ++i) {
            class_1799 stack = CreeperFarm.mc.field_1724.method_31548().method_5438(i);
            if (!stack.method_31574(class_1802.field_8054)) continue;
            total += stack.method_7947();
        }
        return total;
    }

    private void updateCollectedGunpowder() {
        int current = this.countGunpowder();
        if (current > this.lastGunpowderCount) {
            this.collectedGunpowder += current - this.lastGunpowderCount;
        }
        this.lastGunpowderCount = current;
    }

    private boolean isInsideFarm(class_2338 pos) {
        return pos.method_10263() >= REGION_MIN.method_10263() && pos.method_10263() <= REGION_MAX.method_10263() && pos.method_10264() >= REGION_MIN.method_10264() && pos.method_10264() <= REGION_MAX.method_10264() && pos.method_10260() >= REGION_MIN.method_10260() && pos.method_10260() <= REGION_MAX.method_10260();
    }

    @EventHandler
    public void onDraw(DrawEvent event) {
        if (CreeperFarm.mc.field_1724 == null || CreeperFarm.mc.field_1687 == null) {
            return;
        }
        long seconds = Math.max(0L, (System.currentTimeMillis() - this.startedAt) / 1000L);
        String stats = "CreeperFarm | " + this.state.title + " | powder: " + this.collectedGunpowder + " | kills: " + this.kills + " | " + String.format("%02d:%02d", seconds / 60L, seconds % 60L);
        event.getDrawContext().method_25303(CreeperFarm.mc.field_1772, stats, 8, 8, -1);
    }

    private static enum State {
        FARMING("farming"),
        COLLECTING_LOOT("collecting loot"),
        PATROLLING("patrolling"),
        UNLOAD_CLAN("unloading: clan"),
        UNLOAD_ENDER("unloading: ender chest"),
        UNLOAD_CHEST("unloading: chest");

        private final String title;

        private State(String title) {
            this.title = title;
        }
    }
}

