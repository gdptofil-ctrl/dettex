/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1041
 *  net.minecraft.class_1268
 *  net.minecraft.class_1294
 *  net.minecraft.class_1297
 *  net.minecraft.class_1304
 *  net.minecraft.class_1309
 *  net.minecraft.class_1322$class_1323
 *  net.minecraft.class_1657
 *  net.minecraft.class_1735
 *  net.minecraft.class_1743
 *  net.minecraft.class_1792$class_9635
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1836
 *  net.minecraft.class_2246
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_2596
 *  net.minecraft.class_2670
 *  net.minecraft.class_2678
 *  net.minecraft.class_2724
 *  net.minecraft.class_2761
 *  net.minecraft.class_2828
 *  net.minecraft.class_2848
 *  net.minecraft.class_2848$class_2849
 *  net.minecraft.class_2879
 *  net.minecraft.class_3489
 *  net.minecraft.class_3675
 *  net.minecraft.class_408
 *  net.minecraft.class_433
 *  net.minecraft.class_437
 *  net.minecraft.class_465
 *  net.minecraft.class_5134
 *  net.minecraft.class_640
 *  net.minecraft.class_9285
 *  net.minecraft.class_9334
 *  net.minecraft.class_9362
 */
package ru.recoin.implement.features.modules.combat.killaura.attack;

import java.util.Comparator;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.LockSupport;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_1041;
import net.minecraft.class_1268;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1322;
import net.minecraft.class_1657;
import net.minecraft.class_1735;
import net.minecraft.class_1743;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1836;
import net.minecraft.class_2246;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2596;
import net.minecraft.class_2670;
import net.minecraft.class_2678;
import net.minecraft.class_2724;
import net.minecraft.class_2761;
import net.minecraft.class_2828;
import net.minecraft.class_2848;
import net.minecraft.class_2879;
import net.minecraft.class_3489;
import net.minecraft.class_3675;
import net.minecraft.class_408;
import net.minecraft.class_433;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_5134;
import net.minecraft.class_640;
import net.minecraft.class_9285;
import net.minecraft.class_9334;
import net.minecraft.class_9362;
import ru.recoin.api.feature.module.Module;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.world.ServerUtil;
import ru.recoin.mixins.ClientPlayerEntityRotationAccessor;
import ru.recoin.common.util.entity.PlayerInventoryComponent;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.other.StopWatch;
import ru.recoin.core.listener.impl.EventListener;
import ru.recoin.implement.events.item.UsingItemEvent;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.combat.AutoCrystal;
import ru.recoin.implement.features.modules.combat.TriggerBot;
import ru.recoin.implement.features.modules.combat.killaura.attack.AttackPerpetrator;
import ru.recoin.implement.features.modules.combat.killaura.attack.ClickScheduler;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RaytracingUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.misc.ClickAction;
import ru.recoin.implement.features.modules.movement.AirStuck;
import ru.recoin.implement.features.modules.movement.AutoSprint;
import ru.recoin.implement.screens.menu.MenuScreen;

public class AttackHandler
implements QuickImports {
    private static final Pattern OFFHAND_DAMAGE_LINE = Pattern.compile("\\+\\s*([0-9]+(?:[.,][0-9]+)?)\\s*(?:\u0443\u0440\u043e\u043d \u043f\u0440\u0438 \u0430\u0442\u0430\u043a\u0435|attack damage)");
    private final StopWatch attackTimer = new StopWatch();
    private final StopWatch shieldWatch = new StopWatch();
    private final StopWatch cpsTimer = new StopWatch();
    private long nextCpsDelay = 100L;
    private static final long MAX_DAMAGE_SWAP_COOLDOWN_MS = 250L;
    private static final long MAX_DAMAGE_RESTORE_RETRY_MS = 200L;
    private static final int MAX_DAMAGE_RESTORE_CONFIRM_TICKS = 20;
    private long lastMaxDamageSwap;
    private long lastMaxDamageRestore;
    private class_1735 maxDamageReturnSlot;
    private class_1799 maxDamageOriginal;
    private int maxDamageRestoreConfirm;
    private final ClickScheduler clickScheduler = new ClickScheduler();
    private int count;
    private transient int ticksSinceAttack = 20;
    private boolean packetPositionKnown;
    private boolean packetOnGround = true;
    private double packetX;
    private double packetY;
    private double packetZ;
    private double packetVelocityY;
    private float packetFallDistance;
    private boolean packetSprintResetActive;
    private int smartCritLastAge = -1;
    private boolean packetSprintWasActive;
    private transient int fastSprintTicks;
    private transient Module fastSprintOwner;
    private final Object adaptiveLock = new Object();
    private final Thread adaptiveThread = this.createAdaptiveThread();
    private volatile boolean adaptiveAttackPending;
    private volatile boolean adaptiveAttackReady;
    private volatile long adaptiveDeadline;
    private volatile AttackPerpetrator.AttackPerpetratorConfigurable adaptiveConfig;
    private volatile long adaptiveTickAnchor;
    private volatile long adaptiveLastPulse;
    private volatile double adaptivePing;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void tick() {
        if (AttackHandler.mc.field_1724 != null && this.ticksSinceAttack < 20) {
            ++this.ticksSinceAttack;
        }
        this.restoreMaxDamage();
        AttackPerpetrator.AttackPerpetratorConfigurable config = null;
        Object object = this.adaptiveLock;
        synchronized (object) {
            if (this.adaptiveAttackPending && this.adaptiveAttackReady) {
                config = this.adaptiveConfig;
                this.adaptiveAttackPending = false;
                this.adaptiveAttackReady = false;
                this.adaptiveConfig = null;
                this.adaptiveDeadline = 0L;
            }
        }
        if (config != null) {
            this.executeAdaptiveAttack(config);
        }
    }

    void onPacket(PacketEvent e) {
        class_2596<?> packet = e.getPacket();
        if (!e.isSend() && (packet instanceof class_2678 || packet instanceof class_2724)) {
            this.ticksSinceAttack = 20;
            this.clearFastSprintReset();
            this.resetPacketState();
            this.resetAdaptiveTiming();
        } else if (e.isSend() && packet instanceof class_2828) {
            class_2828 move = (class_2828)packet;
            this.trackMovementPacket(move);
        }
        if (!e.isSend() && packet instanceof class_2761) {
            this.updateAdaptivePulse(true);
        } else if (!e.isSend() && packet instanceof class_2670) {
            this.updateAdaptivePulse(false);
        }
        if (packet instanceof class_2879) {
            this.clickScheduler.recalculate();
        }
    }

    void onUsingItem(UsingItemEvent e) {
        if (e.getType() == -1 && !this.shieldWatch.finished(50.0)) {
            e.cancel();
        }
    }

    public void handleAttack(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        boolean aimedAtTarget;
        if (this.isCrystalActionReserved(config)) {
            this.restorePacketSprintReset();
            this.restoreManualSprintKey(config);
            return;
        }
        if (!this.isWithinAttackRange(config)) {
            this.restorePacketSprintReset();
            this.restoreManualSprintKey(config);
            return;
        }
        Aura aura = Aura.getInstance();
        if (aura != null && aura.getPvpMode().isSelected("1.8")) {
            if (!(!RaytracingUtil.rayTrace(config) && !this.isAirStuckActive() || !this.canCrit(config, 0) || config.isFastSprintReset() && EventListener.serverSprint)) {
                this.attackEntity(config);
            }
            return;
        }
        boolean resetEnabled = config.isSprintReset();
        if (config.isAdaptiveTiming() && this.adaptiveAttackPending) {
            return;
        }
        boolean bl = aimedAtTarget = RaytracingUtil.rayTrace(config) || this.isAirStuckActive();
        if (!aimedAtTarget) {
            this.restorePacketSprintReset();
            this.restoreManualSprintKey(config);
            return;
        }
        if (this.canAttack(config, 0)) {
            this.preAttackEntity(config);
        }
        if (this.canAttack(config, 0) && (config.isFastSprintReset() ? !EventListener.serverSprint : !resetEnabled || !this.isSprinting())) {
            if (config.isAdaptiveTiming()) {
                this.runOrScheduleAdaptiveAttack(config);
            } else {
                this.attackEntity(config);
                this.restorePacketSprintReset();
                this.restoreManualSprintKey(config);
            }
        }
    }

    public boolean handleRecordedAttack(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        if (this.isCrystalActionReserved(config)) {
            return false;
        }
        if (config == null || config.getTarget() == null || !config.getTarget().method_5805() || AttackHandler.mc.field_1724 == null || AttackHandler.mc.field_1687 == null || AttackHandler.mc.field_1761 == null) {
            return false;
        }
        if (AttackHandler.mc.field_1724.method_5858((class_1297)config.getTarget()) > (double)(config.getMaximumRange() * config.getMaximumRange())) {
            return false;
        }
        if (!this.canCrit(config, 0) || !RaytracingUtil.rayTrace(config) && !this.isAirStuckActive()) {
            return false;
        }
        if (config.isFastSprintReset() && EventListener.serverSprint) {
            return false;
        }
        return this.attackEntity(config);
    }

    public void preAttackEntity(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        if (this.isCrystalActionReserved(config)) {
            return;
        }
        if (!this.isWithinAttackRange(config)) {
            return;
        }
        if (config.isShouldUnPressShield() && AttackHandler.mc.field_1724.method_6115() && AttackHandler.mc.field_1724.method_6030().method_7909().equals(class_1802.field_8255)) {
            AttackHandler.mc.field_1761.method_2897((class_1657)AttackHandler.mc.field_1724);
            this.shieldWatch.reset();
        }
        if (!config.isSprintReset() || this.isInWater()) {
            return;
        }
        if (config.isFastSprintReset()) {
            return;
        }
        if (config.isPacketSprintReset()) {
            this.beginPacketSprintReset();
            return;
        }
        if (config.isLegitSprintReset()) {
            if (AutoSprint.getInstance().isState()) {
                AutoSprint.getInstance().tickStop = MathUtil.getRandom(1, 1);
            }
            AttackHandler.mc.field_1690.field_1867.method_23481(false);
            AttackHandler.mc.field_1724.method_5728(false);
        }
    }

    private void restoreManualSprintKey(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        if (config == null || !config.isSprintReset() || config.isFastSprintReset() || AutoSprint.getInstance().isState() || mc.method_22683() == null) {
            return;
        }
        AttackHandler.mc.field_1690.field_1867.method_23481(class_3675.method_15987((class_1041)mc.method_22683(), (int)AttackHandler.mc.field_1690.field_1867.method_1429().method_1444()));
    }

    private void beginPacketSprintReset() {
        if (this.packetSprintResetActive || AttackHandler.mc.field_1724 == null || AttackHandler.mc.field_1724.field_3944 == null) {
            return;
        }
        boolean bl = this.packetSprintWasActive = AttackHandler.mc.field_1724.method_5624() || EventListener.serverSprint;
        if (!this.packetSprintWasActive) {
            return;
        }
        this.packetSprintResetActive = true;
        AttackHandler.mc.field_1690.field_1867.method_23481(false);
        AttackHandler.mc.field_1724.method_5728(false);
        AttackHandler.mc.field_1724.field_3944.method_52787((class_2596)new class_2848((class_1297)AttackHandler.mc.field_1724, class_2848.class_2849.field_12985));
    }

    private void restorePacketSprintReset() {
        if (!this.packetSprintResetActive) {
            return;
        }
        boolean restore = this.packetSprintWasActive;
        this.packetSprintResetActive = false;
        this.packetSprintWasActive = false;
        if (restore && AttackHandler.mc.field_1724 != null && AttackHandler.mc.field_1724.field_3944 != null) {
            AttackHandler.mc.field_1724.method_5728(true);
            AttackHandler.mc.field_1724.field_3944.method_52787((class_2596)new class_2848((class_1297)AttackHandler.mc.field_1724, class_2848.class_2849.field_12981));
        }
    }

    public void clearPacketSprintReset() {
        this.restorePacketSprintReset();
    }

    public void updateFastSprintReset(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        if (this.fastSprintTicks > 0) {
            --this.fastSprintTicks;
        }
        if (config == null || !config.isFastSprintReset() || AttackHandler.mc.field_1724 == null || AttackHandler.mc.field_1687 == null) {
            return;
        }
        if (this.isWithinAttackRange(config) && this.canAttack(config, 1)) {
            this.fastSprintTicks = 1;
            this.fastSprintOwner = config.getAttackOwner();
            AttackHandler.mc.field_1724.method_5728(false);
        }
    }

    public boolean isFastSprintResetActive() {
        if (this.fastSprintTicks <= 0 || this.fastSprintOwner == null || !this.fastSprintOwner.isState()) {
            return false;
        }
        Module module = this.fastSprintOwner;
        if (module instanceof Aura) {
            Aura aura = (Aura)module;
            return aura.getSprintResetMode().isSelected("Fast");
        }
        module = this.fastSprintOwner;
        if (module instanceof TriggerBot) {
            TriggerBot triggerBot = (TriggerBot)module;
            return triggerBot.getSprintResetMode().isSelected("Fast");
        }
        return false;
    }

    public void clearFastSprintReset() {
        this.fastSprintTicks = 0;
        this.fastSprintOwner = null;
    }

    public void clearFastSprintReset(Module owner) {
        if (this.fastSprintOwner == owner) {
            this.clearFastSprintReset();
        }
    }

    private Thread createAdaptiveThread() {
        Thread thread2 = new Thread(this::adaptiveLoop, "Recoin Adaptive Tick Edge");
        thread2.setDaemon(true);
        thread2.start();
        return thread2;
    }

    private void updateAdaptivePulse(boolean serverTick) {
        class_640 entry;
        long now;
        this.adaptiveLastPulse = now = System.nanoTime();
        if (serverTick || this.adaptiveTickAnchor == 0L) {
            this.adaptiveTickAnchor = now;
        }
        if (AttackHandler.mc.field_1724 != null && mc.method_1562() != null && (entry = mc.method_1562().method_2871(AttackHandler.mc.field_1724.method_5667())) != null && entry.method_2959() > 0 && entry.method_2959() <= 2000) {
            this.adaptivePing = this.adaptivePing <= 0.0 ? (double)entry.method_2959() : this.adaptivePing + ((double)entry.method_2959() - this.adaptivePing) * 0.15;
        }
    }

    private boolean hasAdaptivePulse(long now) {
        return this.adaptiveTickAnchor > 0L && this.adaptiveLastPulse > 0L && now >= this.adaptiveTickAnchor && now - this.adaptiveLastPulse <= 5000000000L;
    }

    private double adaptivePhase(long now) {
        double phase = (double)(now - this.adaptiveTickAnchor) * 1.0E-6 + this.adaptivePing * 0.3;
        return (phase %= 50.0) < 0.0 ? phase + 50.0 : phase;
    }

    private void runOrScheduleAdaptiveAttack(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        long now = System.nanoTime();
        if (!this.hasAdaptivePulse(now)) {
            this.queueAdaptiveAttack(config, now);
            return;
        }
        double phase = this.adaptivePhase(now);
        double untilTickEnd = 50.0 - phase;
        if (untilTickEnd <= 4.5 || phase >= 45.0 && phase <= 49.2) {
            this.queueAdaptiveAttack(config, now);
            return;
        }
        long delay = (long)((45.0 - phase + (phase > 45.0 ? 50.0 : 0.0)) * 1000000.0);
        if (delay <= 250000L) {
            this.queueAdaptiveAttack(config, now);
            return;
        }
        this.queueAdaptiveAttack(config, now + delay);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void queueAdaptiveAttack(AttackPerpetrator.AttackPerpetratorConfigurable config, long deadline) {
        Object object = this.adaptiveLock;
        synchronized (object) {
            if (this.adaptiveAttackPending) {
                return;
            }
            this.adaptiveConfig = config;
            this.adaptiveDeadline = deadline;
            this.adaptiveAttackPending = true;
            this.adaptiveAttackReady = deadline <= System.nanoTime();
        }
        LockSupport.unpark(this.adaptiveThread);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void adaptiveLoop() {
        while (true) {
            if (!this.adaptiveAttackPending) {
                LockSupport.park();
                continue;
            }
            long deadline = this.adaptiveDeadline;
            long remaining = deadline - System.nanoTime();
            if (remaining > 120000L) {
                LockSupport.parkNanos(remaining - 120000L);
                continue;
            }
            while (this.adaptiveAttackPending && this.adaptiveDeadline == deadline && System.nanoTime() < deadline) {
                Thread.onSpinWait();
            }
            Object object = this.adaptiveLock;
            synchronized (object) {
                if (!this.adaptiveAttackPending || this.adaptiveDeadline != deadline) {
                    continue;
                }
                this.adaptiveAttackReady = true;
            }
            LockSupport.park();
        }
    }

    private void executeAdaptiveAttack(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        TriggerBot triggerBot;
        Aura aura;
        Module module;
        boolean validTarget;
        class_1309 target = config == null ? null : config.getTarget();
        boolean validOwner = config != null && config.getAttackOwner() != null && config.getAttackOwner().isState();
        boolean bl = validTarget = config != null && ((module = config.getAttackOwner()) instanceof Aura && (aura = (Aura)module).getTarget() == target || (module = config.getAttackOwner()) instanceof TriggerBot && (triggerBot = (TriggerBot)module).getTarget() == target);
        if (!(target != null && target.method_5805() && !target.method_31481() && AttackHandler.mc.field_1724 != null && AttackHandler.mc.field_1724.method_5805() && AttackHandler.mc.field_1687 != null && AttackHandler.mc.field_1761 != null && config.isAdaptiveTiming() && validOwner && validTarget && this.isWithinAttackRange(config))) {
            this.restorePacketSprintReset();
            this.restoreManualSprintKey(config);
            return;
        }
        if (config.isSprintReset() && (config.isFastSprintReset() ? EventListener.serverSprint : this.isSprinting())) {
            this.restorePacketSprintReset();
            this.restoreManualSprintKey(config);
            return;
        }
        this.attackEntity(config);
        this.restorePacketSprintReset();
        this.restoreManualSprintKey(config);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void resetAdaptiveTiming() {
        Object object = this.adaptiveLock;
        synchronized (object) {
            this.adaptiveAttackPending = false;
            this.adaptiveAttackReady = false;
            this.adaptiveConfig = null;
            this.adaptiveDeadline = 0L;
        }
        this.adaptiveTickAnchor = 0L;
        this.adaptiveLastPulse = 0L;
        this.adaptivePing = 0.0;
        LockSupport.unpark(this.adaptiveThread);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean attackEntity(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        double currentDamage = this.offhandDamageBonus(AttackHandler.mc.field_1724.method_6079());
        if (this.isCrystalActionReserved(config)) {
            return false;
        }
        this.restoreMaxDamage();
        class_1735 maxDamageSlot = null;
        boolean swappedMaxDamage = false;
        if (Aura.getInstance().getMaxDamage().isValue() && this.maxDamageReturnSlot == null && this.canSilentSwapOffhand() && System.currentTimeMillis() - this.lastMaxDamageSwap >= MAX_DAMAGE_SWAP_COOLDOWN_MS && (maxDamageSlot = PlayerInventoryUtil.getSlot(arg_0 -> this.attackEntity$0(currentDamage, arg_0), Comparator.comparingDouble(slot -> this.offhandDamageBonus(slot.method_7677())))) != null) {
            class_1799 original = AttackHandler.mc.field_1724.method_6079().method_7972();
            PlayerInventoryUtil.swapHand(maxDamageSlot, class_1268.field_5810, false);
            this.maxDamageReturnSlot = maxDamageSlot;
            this.maxDamageOriginal = original;
            this.maxDamageRestoreConfirm = 0;
            swappedMaxDamage = true;
            this.lastMaxDamageSwap = System.currentTimeMillis();
        }
        try {
            this.attack(config);
        }
        finally {
            if (swappedMaxDamage) {
                this.lastMaxDamageRestore = 0L;
                this.restoreMaxDamage();
            }
        }
        this.breakShield(config);
        this.attackTimer.reset();
        Aura aura = Aura.getInstance();
        if (aura != null && config.getAttackOwner() == aura) {
            aura.onRotationAttack(config.getTarget());
            AutoCrystal crystal = AutoCrystal.getInstance();
            if (crystal != null) {
                crystal.onAuraHit(config.getTarget());
            }
        }
        this.clickScheduler.recalculate();
        this.ticksSinceAttack = 0;
        if (Aura.getInstance() != null && Aura.getInstance().getPvpMode().isSelected("1.8")) {
            this.cpsTimer.reset();
            float min = Math.min(Aura.getInstance().getMinCps().getValue(), Aura.getInstance().getMaxCps().getValue());
            float max = Math.max(Aura.getInstance().getMinCps().getValue(), Aura.getInstance().getMaxCps().getValue());
            float cps = min + ThreadLocalRandom.current().nextFloat() * (max - min);
            this.nextCpsDelay = (long)(1000.0f / Math.max(1.0f, cps));
        }
        ++this.count;
        return true;
    }

    private boolean canSilentSwapOffhand() {
        return AttackHandler.mc.field_1724 != null && AttackHandler.mc.field_1755 == null && AttackHandler.mc.field_1724.field_7512 == AttackHandler.mc.field_1724.field_7498 && AttackHandler.mc.field_1724.field_7512.method_34255().method_7960();
    }

    private void restoreMaxDamage() {
        if (this.maxDamageReturnSlot == null || AttackHandler.mc.field_1724 == null) {
            return;
        }
        if (this.maxDamageOriginal != null && PlayerInventoryUtil.matchesSwapStack(AttackHandler.mc.field_1724.method_6079(), this.maxDamageOriginal)) {
            if (++this.maxDamageRestoreConfirm >= MAX_DAMAGE_RESTORE_CONFIRM_TICKS) {
                this.maxDamageReturnSlot = null;
                this.maxDamageOriginal = null;
                this.maxDamageRestoreConfirm = 0;
            }
            return;
        }
        this.maxDamageRestoreConfirm = 0;
        long now = System.currentTimeMillis();
        if (now - this.lastMaxDamageRestore < MAX_DAMAGE_RESTORE_RETRY_MS || !this.canSilentSwapOffhand()) {
            return;
        }
        this.lastMaxDamageRestore = now;
        PlayerInventoryUtil.swapHand(this.maxDamageReturnSlot, class_1268.field_5810, false);
    }

    private boolean isCrystalActionReserved(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        if (PlayerInventoryComponent.isInventoryActionBusy()) {
            return true;
        }
        ClickAction clickAction = ClickAction.getInstance();
        if (clickAction != null && clickAction.isSilentWindChargeActive()) {
            return true;
        }
        Aura aura = Aura.getInstance();
        AutoCrystal crystal = AutoCrystal.getInstance();
        return config != null && aura != null && config.getAttackOwner() == aura && crystal != null && crystal.isAuraAttackBlocked();
    }

    private double offhandDamageBonus(class_1799 stack) {
        double bonus;
        if (stack == null || stack.method_7960()) {
            return 0.0;
        }
        class_9285 modifiers = (class_9285)stack.method_58694(class_9334.field_49636);
        double d = bonus = modifiers == null ? 0.0 : modifiers.comp_2393().stream().filter(entry -> entry.comp_2395().equals((Object)class_5134.field_23721) && entry.comp_2396().comp_2450() == class_1322.class_1323.field_6328 && entry.comp_2397().method_57286(class_1304.field_6171)).mapToDouble(entry -> entry.comp_2396().comp_2449()).sum();
        if (bonus > 0.0 || !stack.method_31574(class_1802.field_8288) && !stack.method_31574(class_1802.field_8575)) {
            return Math.max(0.0, bonus);
        }
        boolean offhandSection = false;
        for (class_2561 lineText : stack.method_7950(class_1792.class_9635.field_51353, (class_1657)AttackHandler.mc.field_1724, (class_1836)class_1836.field_41070)) {
            Matcher match;
            String line = lineText.getString().toLowerCase(Locale.ROOT).replace('\u0451', '\u0435').trim();
            if (line.contains("\u043a\u043e\u0433\u0434\u0430 \u0432\u043e \u0432\u0442\u043e\u0440\u043e\u0441\u0442\u0435\u043f\u0435\u043d\u043d\u043e\u0439 \u0440\u0443\u043a\u0435") || line.contains("when in off hand")) {
                offhandSection = true;
                continue;
            }
            if (line.startsWith("\u043a\u043e\u0433\u0434\u0430 ") || line.startsWith("when in ")) {
                offhandSection = false;
            }
            if (!offhandSection || !(match = OFFHAND_DAMAGE_LINE.matcher(line)).find()) continue;
            try {
                return Double.parseDouble(match.group(1).replace(',', '.'));
            }
            catch (NumberFormatException ignored) {
                return 0.0;
            }
        }
        return 0.0;
    }

    private boolean isWeaponLikeHeld() {
        class_1799 stack = AttackHandler.mc.field_1724.method_6047();
        return stack.method_31573(class_3489.field_42611) || stack.method_7909() instanceof class_1743 || stack.method_7909() instanceof class_9362;
    }

    private void breakShield(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        class_1309 target = config.getTarget();
        Angle angleToPlayer = AngleUtil.fromVec3d(AttackHandler.mc.field_1724.method_5829().method_1005().method_1020(target.method_33571()));
        boolean targetOnShield = target.method_6115() && target.method_6030().method_7909().equals(class_1802.field_8255);
        boolean angle = Math.abs(RotationController.computeAngleDifference(target.method_36454(), angleToPlayer.getYaw())) < 90.0f;
        class_1735 axe = PlayerInventoryUtil.getSlot(s -> s.method_7677().method_7909() instanceof class_1743);
        if (config.isShouldBreakShield() && targetOnShield && axe != null && angle && PlayerInventoryComponent.script.isFinished()) {
            PlayerInventoryUtil.swapHand(axe, class_1268.field_5808, false);
            PlayerInventoryUtil.closeScreen(true);
            this.attack(config);
            PlayerInventoryUtil.swapHand(axe, class_1268.field_5808, false, true);
            PlayerInventoryUtil.closeScreen(true);
        }
    }

    private void performSmartCrit(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        class_1657 player = AttackHandler.mc.field_1724;
        if (player == null || !player.method_5805() || AttackHandler.mc.field_1687 == null || AttackHandler.mc.field_1761 == null) {
            return;
        }
        class_1309 target = config == null ? null : config.getTarget();
        if (target == null || !target.method_5805()) {
            return;
        }
        // Only crit from ground (otherwise already airborne → let normal crit happen)
        if (!player.method_24828()) {
            return;
        }
        CriticalState cs = this.resolveCriticalState(0);
        if (this.hasMovementRestrictions(cs)) {
            return;
        }
        if (!player.method_56094((class_1297)target, 0.0)) {
            return;
        }
        if (player.method_5624()) {
            return; // sprinting - sprint-reset path handles attacks separately
        }
        if (this.smartCritLastAge == player.field_6012) {
            return; // once per attack age
        }
        boolean holyFamily = ServerUtil.isHolyWorld() || ServerUtil.isHolyFake();
        if (holyFamily) {
            // HolyWorld-copy friendly: down-only packet (no headroom required)
            player.field_6017 = 0.01f;
            ClientPlayerEntityRotationAccessor accessor = (ClientPlayerEntityRotationAccessor)player;
            double x = player.method_23317();
            double y = player.method_23318();
            double z = player.method_23321();
            PlayerIntersectionUtil.sendPacketWithOutEvent(new class_2828.class_2830(x, y - 1.0E-6, z, accessor.recoin$getLastYaw(), accessor.recoin$getLastPitch(), false, false));
        } else {
            // Standard packet crit: mini-hop up then down (requires headroom)
            class_238 bb = player.method_5829();
            if (!AttackHandler.mc.field_1687.method_8587((class_1297)player, bb.method_989(0.0, 0.001, 0.0))) {
                return;
            }
            class_243 pos = player.method_73189();
            PlayerIntersectionUtil.sendPacketWithOutEvent(new class_2828.class_2829(pos.field_1352, pos.field_1351 + 0.001, pos.field_1350, false, player.field_5976));
            PlayerIntersectionUtil.sendPacketWithOutEvent(new class_2828.class_2829(pos.field_1352, pos.field_1351, pos.field_1350, false, player.field_5976));
        }
        this.smartCritLastAge = player.field_6012;
    }

    private void attack(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        Angle restoreAngle = this.applyApexTimeServerRotation(config);
        if (config.isSmartCrits()) {
            this.performSmartCrit(config);
        }
        AttackHandler.mc.field_1761.method_2918((class_1657)AttackHandler.mc.field_1724, (class_1297)config.getTarget());
        AttackHandler.mc.field_1724.method_6104(class_1268.field_5808);
        Aura.getInstance().notifyRotationAttack();
        if (restoreAngle != null) {
            this.restoreApexTimeServerRotation(restoreAngle);
        }
    }

    private Angle applyApexTimeServerRotation(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        if (config == null || AttackHandler.mc.field_1724 == null || config.getAimMode() == null || !config.getAimMode().isSelected("ApexTime")) {
            return null;
        }
        Aura aura = Aura.getInstance();
        Angle aim = aura.getApexTimeServerAngle();
        if (aim == null) {
            return null;
        }
        Angle camera = AngleUtil.cameraAngle();
        PlayerIntersectionUtil.sendPacketWithOutEvent(new class_2828.class_2831(aim.getYaw(), aim.getPitch(), AttackHandler.mc.field_1724.method_24828(), AttackHandler.mc.field_1724.field_5976));
        return camera;
    }

    private void restoreApexTimeServerRotation(Angle camera) {
        if (AttackHandler.mc.field_1724 == null || camera == null) {
            return;
        }
        PlayerIntersectionUtil.sendPacketWithOutEvent(new class_2828.class_2831(camera.getYaw(), camera.getPitch(), AttackHandler.mc.field_1724.method_24828(), AttackHandler.mc.field_1724.field_5976));
    }

    private boolean isInWater() {
        return AttackHandler.mc.field_1724.method_5799() || AttackHandler.mc.field_1724.method_5869() || AttackHandler.mc.field_1724.method_5681();
    }

    public boolean canAttack(AttackPerpetrator.AttackPerpetratorConfigurable config, int ticks) {
        Aura aura = Aura.getInstance();
        if (aura != null && aura.getPvpMode().isSelected("1.8")) {
            return this.canCrit(config, 0);
        }
        for (int i = 0; i <= ticks; ++i) {
            if (!this.canCrit(config, i)) continue;
            return true;
        }
        return false;
    }

    public boolean shouldPauseSprint(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        if (this.isCrystalActionReserved(config)) {
            return false;
        }
        if (config != null && config.isFastSprintReset()) {
            return this.isFastSprintResetActive();
        }
        if (!this.isWithinAttackRange(config) || !this.canAttack(config, 0) || !RaytracingUtil.rayTrace(config) && !this.isAirStuckActive()) {
            return false;
        }
        return (config.isMaceAttack() ? this.isMaceCooldownReady(1) : this.clickScheduler.isCooldownCompleteWithin(60L)) && !AttackHandler.mc.field_1724.method_5869() && (AttackHandler.mc.field_1724.method_24828() || AttackHandler.mc.field_1724.method_18798().field_1351 <= 0.2);
    }

    public boolean isWithinAttackRange(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        double range;
        double z;
        double y;
        if (AttackHandler.mc.field_1724 == null || config == null || config.getTarget() == null || !config.getTarget().method_5805() || config.getTarget().method_31481() || config.getBox() == null) {
            return false;
        }
        class_243 eye = AttackHandler.mc.field_1724.method_33571();
        class_238 box = config.getBox();
        double x = eye.field_1352 - Math.clamp(eye.field_1352, box.field_1323, box.field_1320);
        return x * x + (y = eye.field_1351 - Math.clamp(eye.field_1351, box.field_1322, box.field_1325)) * y + (z = eye.field_1350 - Math.clamp(eye.field_1350, box.field_1321, box.field_1324)) * z <= (range = (double)config.getMaximumRange()) * range;
    }

    private boolean isAirStuckActive() {
        AirStuck airStuck = AirStuck.getInstance();
        return airStuck != null && airStuck.isFrozen();
    }

    private boolean isSprinting() {
        return EventListener.serverSprint && !AttackHandler.mc.field_1724.method_6128() && !AttackHandler.mc.field_1724.method_5799();
    }

    public boolean canCrit(AttackPerpetrator.AttackPerpetratorConfigurable config, int ticks) {
        if (config == null || config.getTarget() == null || !config.getTarget().method_5805()) {
            return false;
        }
        if (config.isNoAttackInContainers() && this.isContainerScreen(AttackHandler.mc.field_1755)) {
            return false;
        }
        if (AttackHandler.mc.field_1724.method_6115() && !AttackHandler.mc.field_1724.method_6030().method_7909().equals(class_1802.field_8255) && config.isEatAndAttack()) {
            return false;
        }
        Aura aura = Aura.getInstance();
        if (aura != null && aura.getPvpMode().isSelected("1.8")) {
            return this.cpsTimer.finished(this.nextCpsDelay);
        }
        if (config.isMaceAttack()) {
            if (!this.isMaceCooldownReady(ticks)) {
                return false;
            }
            if (this.isMaceSmashReady() && AttackHandler.mc.field_1724.method_18798().field_1351 < 0.0) {
                return true;
            }
        } else if (!this.clickScheduler.isCooldownComplete(ticks)) {
            return false;
        }
        if (this.isAirStuckActive()) {
            return true;
        }
        CriticalState criticalState = this.resolveCriticalState(ticks);
        if (config.isOnlyCritical() && !this.hasMovementRestrictions(criticalState)) {
            if (config.isCritsOnlySpace() && criticalState.onGround && !AttackHandler.mc.field_1690.field_1903.method_1434()) {
                return true;
            }
            if (config.isSmartCrits() && criticalState.onGround) {
                return true;
            }
            return this.isPlayerInCriticalState(criticalState, ticks, config.isRandomizeHits());
        }
        return true;
    }

    private boolean hasMovementRestrictions(CriticalState state) {
        return AttackHandler.mc.field_1724.method_6059(class_1294.field_5919) || AttackHandler.mc.field_1724.method_6059(class_1294.field_5902) || PlayerIntersectionUtil.isBoxInBlock(state.box.method_1014(-0.001), class_2246.field_10343) || AttackHandler.mc.field_1724.method_5869() || AttackHandler.mc.field_1724.method_5771() || AttackHandler.mc.field_1724.method_6101() || AttackHandler.mc.field_1724.method_6128() || AttackHandler.mc.field_1724.method_31549().field_7479;
    }

    private boolean isPlayerInCriticalState(CriticalState state, int ticks, boolean randomizeHits) {
        boolean hurtOrSlowed;
        if (state == null) {
            return false;
        }
        boolean constrained = state.horizontalCollision || !AttackHandler.mc.field_1687.method_8587((class_1297)AttackHandler.mc.field_1724, state.box.method_1009(0.22, 0.0, 0.22).method_1011(1.0E-7));
        boolean bl = hurtOrSlowed = AttackHandler.mc.field_1724.field_6235 > 0 || AttackHandler.mc.field_1724.method_6059(class_1294.field_5909);
        float fallThreshold = constrained ? 0.0f : (randomizeHits ? ThreadLocalRandom.current().nextFloat(0.006f, 0.05f) : 0.006f);
        double velocityThreshold = constrained ? -0.008 : -0.012;
        if (hurtOrSlowed) {
            fallThreshold = Math.min(fallThreshold, constrained ? 0.006f : 0.01f);
            velocityThreshold = Math.max(velocityThreshold, constrained ? -0.008 : -0.014);
        }
        if (ticks > 0) {
            fallThreshold = Math.max(0.0f, fallThreshold - 0.004f);
            velocityThreshold = Math.max(velocityThreshold, -0.004);
        }
        if (state.onGround || state.fallDistance <= fallThreshold) {
            return false;
        }
        if (state.velocityY < velocityThreshold) {
            return true;
        }
        return state.fallDistance > fallThreshold + 0.02f;
    }

    private CriticalState resolveCriticalState(int ticks) {
        boolean onGround = this.packetPositionKnown ? this.packetOnGround : AttackHandler.mc.field_1724.method_24828();
        float fallDistance = this.packetPositionKnown ? this.packetFallDistance : (float)AttackHandler.mc.field_1724.field_6017;
        double velocityY = this.packetPositionKnown ? this.packetVelocityY : AttackHandler.mc.field_1724.method_18798().field_1351;
        class_238 box = AttackHandler.mc.field_1724.method_5829();
        double gravity = Math.max(0.0, AttackHandler.mc.field_1724.method_45325(class_5134.field_49078));
        for (int i = 0; i < Math.max(0, ticks); ++i) {
            if (onGround && AttackHandler.mc.field_1690.field_1903.method_1434()) {
                onGround = false;
                velocityY = 0.42;
            } else if (!onGround && (velocityY = (velocityY - gravity) * 0.98) < 0.0) {
                fallDistance += (float)(-velocityY);
            }
            box = box.method_989(0.0, velocityY, 0.0);
        }
        return new CriticalState(onGround, fallDistance, velocityY, box, AttackHandler.mc.field_1724.field_5976);
    }

    private void trackMovementPacket(class_2828 move) {
        boolean onGround = move.method_12273();
        if (move.method_36171()) {
            double x = move.method_12269(this.packetX);
            double y = move.method_12268(this.packetY);
            double z = move.method_12274(this.packetZ);
            if (this.packetPositionKnown) {
                this.packetVelocityY = y - this.packetY;
                if (onGround) {
                    this.packetFallDistance = 0.0f;
                } else if (this.packetVelocityY < -1.0E-7) {
                    this.packetFallDistance += (float)(-this.packetVelocityY);
                }
            } else {
                this.packetPositionKnown = true;
                this.packetFallDistance = 0.0f;
                this.packetVelocityY = 0.0;
            }
            this.packetX = x;
            this.packetY = y;
            this.packetZ = z;
        } else if (onGround) {
            this.packetFallDistance = 0.0f;
        }
        this.packetOnGround = onGround;
    }

    private void resetPacketState() {
        this.packetPositionKnown = false;
        this.packetOnGround = true;
        this.packetVelocityY = 0.0;
        this.packetZ = 0.0;
        this.packetY = 0.0;
        this.packetX = 0.0;
        this.packetFallDistance = 0.0f;
    }

    private boolean isMaceSmashReady() {
        return !AttackHandler.mc.field_1724.method_24828() && class_9362.method_58659((class_1309)AttackHandler.mc.field_1724);
    }

    private boolean isMaceCooldownReady(int ticks) {
        if (AttackHandler.mc.field_1724.method_7357().method_7904(AttackHandler.mc.field_1724.method_6047())) {
            return false;
        }
        if (this.isMaceSmashReady()) {
            return true;
        }
        int leadTicks = Math.max(0, ticks);
        if (AttackHandler.mc.field_1724.field_6017 > 1.5) {
            return this.ticksSinceAttack > 3 - leadTicks;
        }
        return AttackHandler.mc.field_1724.method_7261(0.5f + (float)leadTicks) >= 0.9f;
    }

    private boolean isContainerScreen(class_437 screen) {
        if (screen == null) {
            return false;
        }
        if (screen instanceof MenuScreen || screen instanceof class_433 || screen instanceof class_408) {
            return false;
        }
        return screen instanceof class_465;
    }

    public void setNextCpsDelay(long nextCpsDelay) {
        this.nextCpsDelay = nextCpsDelay;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setTicksSinceAttack(int ticksSinceAttack) {
        this.ticksSinceAttack = ticksSinceAttack;
    }

    public void setPacketPositionKnown(boolean packetPositionKnown) {
        this.packetPositionKnown = packetPositionKnown;
    }

    public void setPacketOnGround(boolean packetOnGround) {
        this.packetOnGround = packetOnGround;
    }

    public void setPacketX(double packetX) {
        this.packetX = packetX;
    }

    public void setPacketY(double packetY) {
        this.packetY = packetY;
    }

    public void setPacketZ(double packetZ) {
        this.packetZ = packetZ;
    }

    public void setPacketVelocityY(double packetVelocityY) {
        this.packetVelocityY = packetVelocityY;
    }

    public void setPacketFallDistance(float packetFallDistance) {
        this.packetFallDistance = packetFallDistance;
    }

    public void setPacketSprintResetActive(boolean packetSprintResetActive) {
        this.packetSprintResetActive = packetSprintResetActive;
    }

    public void setPacketSprintWasActive(boolean packetSprintWasActive) {
        this.packetSprintWasActive = packetSprintWasActive;
    }

    public void setFastSprintTicks(int fastSprintTicks) {
        this.fastSprintTicks = fastSprintTicks;
    }

    public void setFastSprintOwner(Module fastSprintOwner) {
        this.fastSprintOwner = fastSprintOwner;
    }

    public void setAdaptiveAttackPending(boolean adaptiveAttackPending) {
        this.adaptiveAttackPending = adaptiveAttackPending;
    }

    public void setAdaptiveAttackReady(boolean adaptiveAttackReady) {
        this.adaptiveAttackReady = adaptiveAttackReady;
    }

    public void setAdaptiveDeadline(long adaptiveDeadline) {
        this.adaptiveDeadline = adaptiveDeadline;
    }

    public void setAdaptiveConfig(AttackPerpetrator.AttackPerpetratorConfigurable adaptiveConfig) {
        this.adaptiveConfig = adaptiveConfig;
    }

    public void setAdaptiveTickAnchor(long adaptiveTickAnchor) {
        this.adaptiveTickAnchor = adaptiveTickAnchor;
    }

    public void setAdaptiveLastPulse(long adaptiveLastPulse) {
        this.adaptiveLastPulse = adaptiveLastPulse;
    }

    public void setAdaptivePing(double adaptivePing) {
        this.adaptivePing = adaptivePing;
    }

    public StopWatch getAttackTimer() {
        return this.attackTimer;
    }

    public StopWatch getShieldWatch() {
        return this.shieldWatch;
    }

    public StopWatch getCpsTimer() {
        return this.cpsTimer;
    }

    public long getNextCpsDelay() {
        return this.nextCpsDelay;
    }

    public ClickScheduler getClickScheduler() {
        return this.clickScheduler;
    }

    public int getCount() {
        return this.count;
    }

    public int getTicksSinceAttack() {
        return this.ticksSinceAttack;
    }

    public boolean isPacketPositionKnown() {
        return this.packetPositionKnown;
    }

    public boolean isPacketOnGround() {
        return this.packetOnGround;
    }

    public double getPacketX() {
        return this.packetX;
    }

    public double getPacketY() {
        return this.packetY;
    }

    public double getPacketZ() {
        return this.packetZ;
    }

    public double getPacketVelocityY() {
        return this.packetVelocityY;
    }

    public float getPacketFallDistance() {
        return this.packetFallDistance;
    }

    public boolean isPacketSprintResetActive() {
        return this.packetSprintResetActive;
    }

    public boolean isPacketSprintWasActive() {
        return this.packetSprintWasActive;
    }

    public int getFastSprintTicks() {
        return this.fastSprintTicks;
    }

    public Module getFastSprintOwner() {
        return this.fastSprintOwner;
    }

    public Object getAdaptiveLock() {
        return this.adaptiveLock;
    }

    public Thread getAdaptiveThread() {
        return this.adaptiveThread;
    }

    public boolean isAdaptiveAttackPending() {
        return this.adaptiveAttackPending;
    }

    public boolean isAdaptiveAttackReady() {
        return this.adaptiveAttackReady;
    }

    public long getAdaptiveDeadline() {
        return this.adaptiveDeadline;
    }

    public AttackPerpetrator.AttackPerpetratorConfigurable getAdaptiveConfig() {
        return this.adaptiveConfig;
    }

    public long getAdaptiveTickAnchor() {
        return this.adaptiveTickAnchor;
    }

    public long getAdaptiveLastPulse() {
        return this.adaptiveLastPulse;
    }

    public double getAdaptivePing() {
        return this.adaptivePing;
    }

    private /* synthetic */ boolean attackEntity$0(double currentDamage, class_1735 slot) {
        return this.offhandDamageBonus(slot.method_7677()) > currentDamage;
    }

    private record CriticalState(boolean onGround, float fallDistance, double velocityY, class_238 box, boolean horizontalCollision) {
    }
}

