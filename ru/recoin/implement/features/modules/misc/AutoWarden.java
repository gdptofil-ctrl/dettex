/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  baritone.api.BaritoneAPI
 *  baritone.api.IBaritone
 *  baritone.api.pathing.goals.Goal
 *  baritone.api.pathing.goals.GoalBlock
 *  baritone.api.pathing.goals.GoalNear
 *  net.minecraft.class_1268
 *  net.minecraft.class_1269
 *  net.minecraft.class_1291
 *  net.minecraft.class_1293
 *  net.minecraft.class_1294
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_1661
 *  net.minecraft.class_1703
 *  net.minecraft.class_1707
 *  net.minecraft.class_1713
 *  net.minecraft.class_1735
 *  net.minecraft.class_1744
 *  net.minecraft.class_1746
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1844
 *  net.minecraft.class_1923
 *  net.minecraft.class_2246
 *  net.minecraft.class_2281
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_2350$class_2353
 *  net.minecraft.class_238
 *  net.minecraft.class_2382
 *  net.minecraft.class_239
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_2586
 *  net.minecraft.class_2595
 *  net.minecraft.class_2596
 *  net.minecraft.class_2614
 *  net.minecraft.class_2627
 *  net.minecraft.class_266
 *  net.minecraft.class_2680
 *  net.minecraft.class_269
 *  net.minecraft.class_2745
 *  net.minecraft.class_2769
 *  net.minecraft.class_2818
 *  net.minecraft.class_2868
 *  net.minecraft.class_3489
 *  net.minecraft.class_3532
 *  net.minecraft.class_3719
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_3965
 *  net.minecraft.class_418
 *  net.minecraft.class_476
 *  net.minecraft.class_638
 *  net.minecraft.class_6880
 *  net.minecraft.class_7260
 *  net.minecraft.class_8052
 *  net.minecraft.class_8646
 *  net.minecraft.class_9279
 *  net.minecraft.class_9334
 *  org.lwjgl.glfw.GLFW
 */
package ru.recoin.implement.features.modules.misc;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalNear;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1744;
import net.minecraft.class_1746;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2281;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2586;
import net.minecraft.class_2595;
import net.minecraft.class_2596;
import net.minecraft.class_2614;
import net.minecraft.class_2627;
import net.minecraft.class_266;
import net.minecraft.class_2680;
import net.minecraft.class_269;
import net.minecraft.class_2745;
import net.minecraft.class_2769;
import net.minecraft.class_2818;
import net.minecraft.class_2868;
import net.minecraft.class_3489;
import net.minecraft.class_3532;
import net.minecraft.class_3719;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_418;
import net.minecraft.class_476;
import net.minecraft.class_638;
import net.minecraft.class_6880;
import net.minecraft.class_7260;
import net.minecraft.class_8052;
import net.minecraft.class_8646;
import net.minecraft.class_9279;
import net.minecraft.class_9334;
import org.lwjgl.glfw.GLFW;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.TextSetting;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.common.util.task.TaskPriority;
import ru.recoin.common.util.warden.DungeonPatrol;
import ru.recoin.common.util.warden.WardenLootPolicy;
import ru.recoin.common.util.warden.WardenSafeNavigator;
import ru.recoin.common.util.warden.WardenStorageTrip;
import ru.recoin.common.util.warden.WardenTimerCache;
import ru.recoin.common.util.world.ServerUtil;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationConfig;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;

public final class AutoWarden
extends Module {
    public final BooleanSetting useSpeed = new BooleanSetting("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c", "\u0411\u0440\u0430\u0442\u044c \u0438 \u043f\u043e\u0434\u0434\u0435\u0440\u0436\u0438\u0432\u0430\u0442\u044c \u044d\u0444\u0444\u0435\u043a\u0442 \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u0438").setValue(false);
    public final BooleanSetting reportOffenders = new BooleanSetting("\u0420\u0435\u043f\u043e\u0440\u0442\u0438\u0442\u044c \u043e\u0431\u0438\u0434\u0447\u0438\u043a\u043e\u0432", "\u041e\u0442\u043f\u0440\u0430\u0432\u043b\u044f\u0442\u044c \u0436\u0430\u043b\u043e\u0431\u0443 \u043d\u0430 \u0438\u0433\u0440\u043e\u043a\u0430 \u043f\u043e\u0441\u043b\u0435 \u0441\u043c\u0435\u0440\u0442\u0438").setValue(false);
    public final SelectSetting lootPriority = new SelectSetting("\u041f\u0440\u0438\u043e\u0440\u0438\u0442\u0435\u0442\u044b \u043b\u0443\u0442\u0430", "\u0424\u0438\u043b\u044c\u0442\u0440 \u0446\u0435\u043d\u043d\u043e\u0441\u0442\u0438 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432").value("\u041d\u0438\u0437\u043a\u0438\u0439", "\u0421\u0440\u0435\u0434\u043d\u0438\u0439", "\u0412\u044b\u0441\u043e\u043a\u0438\u0439").selected("\u0421\u0440\u0435\u0434\u043d\u0438\u0439");
    public final BooleanSetting debug = new BooleanSetting("\u041e\u0442\u043b\u0430\u0434\u043a\u0430", "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u0441\u043e\u0441\u0442\u043e\u044f\u043d\u0438\u0435 AutoWarden \u0432 \u0447\u0430\u0442\u0435").setValue(false);
    private final TextSetting learnedSupplyChests = new TextSetting("AutoWarden Supply Memory", "").setText("").setMax(2048).visible(() -> false);
    private final TextSetting learnedLootChests = new TextSetting("AutoWarden Loot Memory", "").setText("").setMax(2048).visible(() -> false);
    private static final Pattern CLOCK = Pattern.compile("(\\d{1,2}:\\d{2}(?::\\d{2})?)");
    private static final Pattern SECONDS = Pattern.compile("(\\d+)\\s*(?:\u0441|s|\u0441\u0435\u043a|sec)", 66);
    private static final Pattern MINUTES = Pattern.compile("(\\d+)\\s*(?:\u043c|m|\u043c\u0438\u043d|min(?:ute)?)\\s*(?:(\\d+)\\s*(?:\u0441|s|\u0441\u0435\u043a|sec(?:ond)?))?", 66);
    private static final Pattern SPOOKY_ANARCHY = Pattern.compile("(?iu)\u0430\u043d\u0430\u0440\u0445\u0438\u044f\\s*[-#\u2116]?\\s*(\\d{1,3})");
    private static final long TARGET_TIMEOUT = 15000L;
    private static final long OPEN_TIMEOUT = 1500L;
    private static final long LOOT_CLICK_DELAY = 100L;
    private static final long RESTOCK_LOOT_GUARD = 15000L;
    private static final long HOME_RETRY_INTERVAL = 2500L;
    private static final int FOOD_SUPPLY_TARGET = 3;
    private static final int DUNGEON_MIN_X = -2070;
    private static final int DUNGEON_MAX_X = -1921;
    private static final int DUNGEON_MIN_Y = -60;
    private static final int DUNGEON_MAX_Y = -35;
    private static final int DUNGEON_MIN_Z = -2076;
    private static final int DUNGEON_MAX_Z = -1929;
    private static final long PATROL_LOOT_WINDOW = 15000L;
    private static final long PATROL_RETRY_DELAY = 5000L;
    private static final long PATROL_LEG_TIMEOUT = 120000L;
    private static final long FORCED_DEPOSIT_INTERVAL = 180000L;
    private final TextSetting farmAnarchies = new TextSetting("\u0410\u043d\u0430\u0440\u0445\u0438\u0438 \u0434\u043b\u044f \u0444\u0430\u0440\u043c\u0430", "").setText("").setMax(64).visible(() -> false);
    private final TextSetting baseAnarchy = new TextSetting("\u0411\u0430\u0437\u043e\u0432\u0430\u044f \u0430\u043d\u0430\u0440\u0445\u0438\u044f", "").setText("").setMax(8).visible(() -> false);
    private final List<class_2338> containers = new ArrayList<class_2338>();
    private final Map<class_2338, Long> respawnAt = new HashMap<class_2338, Long>();
    private final Map<class_2338, Long> ignoredUntil = new HashMap<class_2338, Long>();
    private final Map<class_2338, Long> recentWardenPositions = new HashMap<class_2338, Long>();
    private final Set<class_2338> visited = new HashSet<class_2338>();
    private final Set<class_2338> readyChests = new HashSet<class_2338>();
    private final Set<class_2338> rejectedStorageChests = new HashSet<class_2338>();
    private final Set<class_2338> knownDungeonChests = new HashSet<class_2338>();
    private final Map<Integer, DungeonPatrol> dungeonPatrols = new HashMap<Integer, DungeonPatrol>();
    private final WardenStorageTrip storageTrip = new WardenStorageTrip();
    private int activeFarmAnarchy;
    private long depositBlockedSince;
    private IBaritone baritone;
    private class_638 lastWorld;
    private int lastAnarchy = -1;
    private Phase phase = Phase.SEARCHING;
    private ContainerPurpose containerPurpose = ContainerPurpose.LOOT;
    private ConsumeAction consumeAction = ConsumeAction.NONE;
    private class_2338 target;
    private class_2338 supplyChest;
    private class_2338 lootChest;
    private long phaseSince;
    private long nextActionAt;
    private long nextScanAt;
    private long nextPathAt;
    private long nextTimerScanAt;
    private long nextServerActionAt;
    private long nextRestockAttemptAt;
    private long lastProgressAt;
    private double lastTargetDistance = Double.MAX_VALUE;
    private int openAttempts;
    private int originalHotbar = -1;
    private int swappedConsumeSlot = -1;
    private int consumeInitialCount;
    private int consumeInitialHunger;
    private int consumeInitialEffectDuration;
    private long consumeStartedAt;
    private int farmIndex;
    private boolean walking;
    private int waitPatrolIndex;
    private PendingTransfer pendingTransfer;
    private String supplySearchKey;
    private int pendingFarmAnarchy;
    private boolean farmHomeSent;
    private boolean farmTravelActive;
    private boolean farmCenterPending;
    private class_2338 farmCenterTarget;
    private boolean pvpLastTick;
    private long farmSwitchIssuedAt;
    private class_2338 patrolTarget;
    private DungeonPatrol dungeonPatrol;
    private DungeonPatrol.Sector patrolSector;
    private long lastPatrolTickAt;
    private long patrolStuckMillis;
    private long patrolLegMillis;
    private double patrolDistance = Double.MAX_VALUE;
    private class_2338 movementGoal;
    private class_2338 preferredChest;
    private long nextChestSelectionAt;
    private long lootBlockedSince;
    private final List<Integer> anarchies = new ArrayList<Integer>();
    private boolean deathReported;
    private boolean deathHomePending;
    private long deathHomeNotBefore;
    private long forcedDepositAt;
    private long activatedAt;
    private boolean initialInvisDone;
    private boolean storageSelectionPending;

    public AutoWarden() {
        super("AutoWarden", "Auto Warden", ModuleCategory.MISC);
        this.setup(this.useSpeed, this.reportOffenders, this.lootPriority, this.debug, this.baseAnarchy, this.farmAnarchies, this.learnedSupplyChests, this.learnedLootChests);
    }

    @Override
    public void activate() {
        long now;
        this.loadAnarchySettings();
        this.selectStorageAnarchy();
        this.storageSelectionPending = true;
        this.baritone = this.getPrimaryBaritone();
        if (AutoWarden.mc.field_1705 != null) {
            AutoWarden.mc.field_1705.method_1743().method_1812((class_2561)class_2561.method_43470((String)"Shift + \u041f\u0440\u043e\u0431\u0435\u043b \u2014 \u0431\u044b\u0441\u0442\u0440\u043e\u0435 \u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u0435 \u0444\u0443\u043d\u043a\u0446\u0438\u0438"));
        }
        this.resetRuntime();
        this.activatedAt = now = System.currentTimeMillis();
        this.initialInvisDone = AutoWarden.mc.field_1724 != null && AutoWarden.mc.field_1724.method_6059(class_1294.field_5905);
        this.forcedDepositAt = now + 180000L;
    }

    @Override
    public void deactivate() {
        this.stopWalking();
        this.stopConsuming();
        this.recoverPendingTransfer();
        this.cancelBaritonePathing();
        RotationController.INSTANCE.release(this);
        if (AutoWarden.mc.field_1724 != null) {
            AutoWarden.mc.field_1724.method_5660(false);
        }
        this.resetRuntime();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        boolean forceDeposit;
        class_1703 entity2;
        Object openHandler;
        class_1703 class_17032;
        boolean lootingOpenChest;
        long window;
        if (AutoWarden.mc.field_1724 == null || AutoWarden.mc.field_1687 == null || AutoWarden.mc.field_1761 == null || AutoWarden.mc.field_1724.field_3944 == null) {
            return;
        }
        if (this.storageSelectionPending && this.selectStorageAnarchy()) {
            this.storageSelectionPending = false;
        }
        if (this.baritone == null) {
            this.baritone = this.getPrimaryBaritone();
        }
        if (this.debug.isValue() && AutoWarden.mc.field_1724.field_6012 % 20 == 0) {
            AutoWarden.mc.field_1705.method_1743().method_1812((class_2561)class_2561.method_43470((String)("[AW] " + this.getPatrolStatus() + " \u0421\u043e\u0441\u0442\u043e\u044f\u043d\u0438\u0435: " + String.valueOf((Object)this.phase) + ", Baritone: " + (this.isBaritonePathing() ? "\u0438\u0434\u0451\u0442" : "\u0441\u0442\u043e\u0438\u0442"))));
        }
        if ((GLFW.glfwGetKey((long)(window = mc.method_22683().method_4490()), (int)340) == 1 || GLFW.glfwGetKey((long)window, (int)344) == 1) && GLFW.glfwGetKey((long)window, (int)32) == 1) {
            this.setState(false);
            return;
        }
        int activeAnarchy = this.currentAnarchy();
        if (this.lastWorld != AutoWarden.mc.field_1687 || activeAnarchy > 0 && activeAnarchy != this.lastAnarchy) {
            this.resetWorldNavigation();
            this.lastWorld = AutoWarden.mc.field_1687;
        }
        if (activeAnarchy > 0) {
            this.lastAnarchy = activeAnarchy;
        }
        long now = System.currentTimeMillis();
        if (this.isBaritonePathing() && AutoWarden.mc.field_1755 == null) {
            RotationController.INSTANCE.release(this);
        }
        boolean inPvp = ServerUtil.isPvp();
        if (this.pvpLastTick && !inPvp) {
            if (this.shouldDeposit()) {
                this.nextServerActionAt = 0L;
            }
            if (this.phase == Phase.RETREATING) {
                this.target = null;
                this.patrolTarget = null;
                this.nextActionAt = 0L;
                this.nextPathAt = 0L;
                this.stopWalking();
                this.cancelBaritonePathing();
                this.changePhase(Phase.SEARCHING, now);
            }
        }
        this.pvpLastTick = inPvp;
        if (this.handleDeath(now)) {
            return;
        }
        if (this.insideDungeon(AutoWarden.mc.field_1724.method_73189())) {
            if (now >= this.nextScanAt) {
                this.scanContainers();
                this.nextScanAt = now + 750L;
            }
            if (AutoWarden.mc.field_1724.method_23318() >= -60.0 && AutoWarden.mc.field_1724.method_23318() <= -33.0 && AutoWarden.mc.field_1687.method_22340(AutoWarden.mc.field_1724.method_24515())) {
                this.dungeonPatrol.recordVisit(AutoWarden.mc.field_1724.method_31477(), AutoWarden.mc.field_1724.method_31479(), now);
            }
        }
        if (this.handleConsumption(now)) {
            return;
        }
        if (this.storageTrip.active()) {
            WardenStorageTrip.Action action = this.storageTrip.advance(this.currentAnarchy(), inPvp, now);
            if (action == WardenStorageTrip.Action.ARRIVED) {
                if (AutoWarden.mc.field_1724.field_6012 < 10) {
                    return;
                }
                ContainerPurpose purpose = this.storageTrip.purpose() == WardenStorageTrip.Purpose.DEPOSIT ? ContainerPurpose.DEPOSIT : ContainerPurpose.RESTOCK;
                this.storageTrip.clear();
                this.target = null;
                this.containerPurpose = purpose;
                this.nextActionAt = 0L;
                this.nextRestockAttemptAt = 0L;
                this.nextServerActionAt = 0L;
                if (this.hasDepositExtras()) {
                    this.handleDeposit(now);
                } else {
                    this.handleRestock(now);
                }
            } else if (inPvp) {
                this.waitOutPvp(now);
            } else {
                this.stopWalking();
                if (action == WardenStorageTrip.Action.SWITCH) {
                    this.sendCommand("an" + this.storageTrip.destination());
                }
            }
            return;
        }
        boolean bl = lootingOpenChest = this.containerPurpose == ContainerPurpose.LOOT && AutoWarden.mc.field_1755 instanceof class_476 && (class_17032 = AutoWarden.mc.field_1724.field_7512) instanceof class_1707 && this.hasCollectableLoot((class_1707)(openHandler = (class_1707)class_17032));
        if (inPvp && this.shouldDeposit() && this.currentAnarchy() != this.baseAnarchyNumber() && !lootingOpenChest) {
            if (AutoWarden.mc.field_1755 instanceof class_476) {
                AutoWarden.mc.field_1724.method_7346();
            }
            this.waitOutPvp(now);
            return;
        }
        if (now >= this.nextTimerScanAt) {
            this.scanRespawnTimers(now);
            this.syncSharedTimers(now);
            for (class_1297 entity3 : AutoWarden.mc.field_1687.method_18112()) {
                if (!(entity3 instanceof class_7260) || !entity3.method_5805()) continue;
                this.recentWardenPositions.put(entity3.method_24515().method_10062(), now + 5000L);
            }
            this.recentWardenPositions.values().removeIf(expires -> expires <= now);
            this.nextTimerScanAt = now + 500L;
        }
        if (this.containerPurpose == ContainerPurpose.LOOT && this.phase != Phase.RETREATING && this.insideDungeon(AutoWarden.mc.field_1724.method_73189()) && this.dangerNearby()) {
            if (AutoWarden.mc.field_1755 instanceof class_476) {
                AutoWarden.mc.field_1724.method_7346();
            }
            this.stopWalking();
            this.target = null;
            this.changePhase(Phase.RETREATING, now);
            return;
        }
        if (this.canUseConsumablesHere()) {
            if (!this.initialInvisDone) {
                if (AutoWarden.mc.field_1724.method_6059(class_1294.field_5905) || this.countInventory(this::isInvisibilityPotion) < 1) {
                    this.initialInvisDone = true;
                } else {
                    this.stopWalking();
                    this.cancelBaritonePathing();
                    if (this.tryStartConsumable()) {
                        return;
                    }
                    return;
                }
            }
            if (this.tryStartConsumable()) {
                return;
            }
        }
        if (AutoWarden.mc.field_1755 instanceof class_476 && (entity2 = AutoWarden.mc.field_1724.field_7512) instanceof class_1707) {
            class_1707 handler = (class_1707)entity2;
            this.handleContainer(handler, now);
            return;
        }
        if (AutoWarden.mc.field_1755 != null) {
            return;
        }
        if (now < this.nextActionAt) {
            return;
        }
        if (this.containerPurpose != ContainerPurpose.LOOT && this.phase != Phase.RETREATING) {
            if (this.phase == Phase.OPENING) {
                this.openTarget(now);
            } else if (this.phase == Phase.WAITING_GUI) {
                this.waitForGui(now);
            } else if (this.target != null) {
                this.moveToTarget(now);
            } else if (this.containerPurpose == ContainerPurpose.DEPOSIT) {
                this.handleDeposit(now);
            } else {
                this.handleRestock(now);
            }
            return;
        }
        if (this.moveHeldItemToInventory(now)) {
            return;
        }
        if (this.freeOneHotbarSlot(now)) {
            return;
        }
        boolean lootChestSoon = this.lootChestReadyWithin(15000L, now);
        if (lootChestSoon && this.containerPurpose == ContainerPurpose.RESTOCK) {
            this.stopWalking();
            this.cancelBaritonePathing();
            this.target = null;
            this.containerPurpose = ContainerPurpose.LOOT;
            this.changePhase(Phase.SEARCHING, now);
        }
        if (!(this.farmTravelActive || !this.needsOperationalRestock() || lootChestSoon || !ServerUtil.isSpookyTime() && now < this.nextRestockAttemptAt || this.containerPurpose == ContainerPurpose.RESTOCK && this.target != null)) {
            if (now >= this.nextRestockAttemptAt) {
                if (this.hasDepositExtras()) {
                    this.handleDeposit(now);
                } else {
                    this.handleRestock(now);
                }
            }
            return;
        }
        boolean bl2 = forceDeposit = now >= this.forcedDepositAt && this.hasDepositExtras();
        if (!(!this.shouldDeposit() && !forceDeposit || inPvp || this.containerPurpose == ContainerPurpose.DEPOSIT && this.target != null)) {
            this.handleDeposit(now);
            return;
        }
        switch (this.phase.ordinal()) {
            case 0: {
                this.search(now);
                break;
            }
            case 1: 
            case 5: 
            case 6: {
                this.moveToTarget(now);
                break;
            }
            case 2: {
                this.openTarget(now);
                break;
            }
            case 3: {
                this.waitForGui(now);
                break;
            }
            case 4: {
                this.retreat(now);
            }
        }
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (AutoWarden.mc.field_1724 == null || AutoWarden.mc.field_1687 == null) {
            return;
        }
        if (this.target != null) {
            Render3DUtil.drawBox(new class_238(this.target), -1442801921, 1.5f, true, true, false);
        }
        this.knownDungeonChests.forEach(pos -> {
            if (pos.equals((Object)this.target)) {
                return;
            }
            int color = this.readyChests.contains(pos) ? -2013200490 : (!this.respawnAt.containsKey(pos) ? -2005365592 : -1996536320);
            Render3DUtil.drawBox(new class_238(pos), color, 1.0f, true, false, false);
        });
    }

    private void search(long now) {
        boolean urgentChest;
        if (this.currentAnarchy() == this.baseAnarchyNumber()) {
            if (this.hasDepositExtras() || this.shouldDeposit()) {
                this.handleDeposit(now);
                return;
            }
            if (this.needsRestock() || this.needsOperationalRestock()) {
                this.handleRestock(now);
                return;
            }
            this.beginFarmTravel(now);
            return;
        }
        if (this.farmTravelActive) {
            if (this.pendingFarmAnarchy > 0 && this.currentAnarchy() != this.pendingFarmAnarchy) {
                this.handleFarmTravel(now);
                return;
            }
            if (this.farmHomeSent && this.insideDungeon(AutoWarden.mc.field_1724.method_73189())) {
                if (this.farmCenterPending && this.moveToDungeonCenter(now)) {
                    return;
                }
                this.farmTravelActive = false;
                this.pendingFarmAnarchy = 0;
            } else if (this.nearDungeonArea()) {
                this.farmTravelActive = false;
                this.farmHomeSent = true;
                this.pendingFarmAnarchy = 0;
                this.requireFarmCenter();
                if (this.moveToDungeonCenter(now)) {
                    return;
                }
            } else {
                this.handleFarmTravel(now);
                return;
            }
        }
        if (this.farmCenterPending && this.insideDungeon(AutoWarden.mc.field_1724.method_73189()) && this.moveToDungeonCenter(now)) {
            return;
        }
        if (!this.insideDungeon(AutoWarden.mc.field_1724.method_73189())) {
            if (this.nearDungeonArea()) {
                this.requireFarmCenter();
                this.moveToDungeonCenter(now);
                return;
            }
            this.stopWalking();
            this.beginFarmTravel(now);
            return;
        }
        this.pendingFarmAnarchy = 0;
        this.farmHomeSent = true;
        class_2338 pick = this.findPreferredChest(now);
        boolean bl = urgentChest = pick != null && WardenLootPolicy.chestTier(this.remainingRespawn(pick), AutoWarden.mc.field_1724.method_33571().method_1025(pick.method_46558())) <= 1;
        if (!urgentChest && now - this.dungeonPatrol.lastVisitAt() >= 15000L && this.dungeonPatrol.next(now) != null) {
            this.patrolDungeonChests(now);
            return;
        }
        this.target = pick;
        if (this.target != null) {
            this.patrolTarget = null;
            this.nextPathAt = 0L;
            this.cancelBaritonePathing();
            this.containerPurpose = ContainerPurpose.LOOT;
            this.changePhase(Phase.MOVING, now);
            return;
        }
        this.patrolDungeonChests(now);
    }

    private void patrolDungeonChests(long now) {
        if (!this.insideDungeon(AutoWarden.mc.field_1724.method_73189())) {
            this.stopWalking();
            this.patrolTarget = null;
            this.handleFarmTravel(now);
            return;
        }
        DungeonPatrol.Sector nextSector = this.dungeonPatrol.next(now);
        if (nextSector == null) {
            if (this.patrolTarget != null) {
                this.cancelBaritonePathing();
            }
            this.patrolTarget = null;
            return;
        }
        class_2338 center = new class_2338(nextSector.centerX(), class_3532.method_15340((int)AutoWarden.mc.field_1724.method_31478(), (int)-60, (int)-33), nextSector.centerZ());
        if (!nextSector.equals(this.patrolSector) || this.patrolTarget == null) {
            this.patrolSector = nextSector;
            this.patrolTarget = center;
            this.cancelBaritonePathing();
            this.nextPathAt = 0L;
            this.lastPatrolTickAt = now;
            this.patrolLegMillis = 0L;
            this.patrolStuckMillis = 0L;
            this.patrolDistance = Double.MAX_VALUE;
        }
        long elapsed = class_3532.method_53062((long)(now - this.lastPatrolTickAt), (long)0L, (long)250L);
        this.lastPatrolTickAt = now;
        this.patrolStuckMillis += elapsed;
        this.patrolLegMillis += elapsed;
        double distance = AutoWarden.mc.field_1724.method_73189().method_1022(this.patrolTarget.method_61082());
        if (distance < this.patrolDistance - 0.75) {
            this.patrolDistance = distance;
            this.patrolStuckMillis = 0L;
        }
        if (this.patrolStuckMillis >= 15000L || this.patrolLegMillis >= 120000L) {
            this.dungeonPatrol.defer(now, 5000L);
            this.patrolTarget = null;
            this.cancelBaritonePathing();
            this.nextPathAt = 0L;
            return;
        }
        if (now >= this.nextPathAt && !this.isBaritonePathing()) {
            this.navigateTo(this.patrolTarget, 2, now);
            this.nextPathAt = now + 2500L;
        }
    }

    private double weightedChestDistance(class_2338 pos) {
        double dx = (double)pos.method_10263() + 0.5 - AutoWarden.mc.field_1724.method_23317();
        double dy = (double)pos.method_10264() + 0.5 - AutoWarden.mc.field_1724.method_23320();
        double dz = (double)pos.method_10260() + 0.5 - AutoWarden.mc.field_1724.method_23321();
        return dx * dx + dz * dz + (dy > 0.0 ? 2.0 : 1.0) * dy * dy;
    }

    private class_2338 findPreferredChest(long now) {
        if (now < this.nextChestSelectionAt && (this.preferredChest == null || !this.visited.contains(this.preferredChest) && this.ignoredUntil.getOrDefault(this.preferredChest, 0L) <= now && this.isDungeonContainer(this.preferredChest))) {
            return this.preferredChest;
        }
        this.nextChestSelectionAt = now + 500L;
        this.preferredChest = null;
        int bestTier = Integer.MAX_VALUE;
        double bestScore = Double.MAX_VALUE;
        for (class_2338 chest : this.containers) {
            double score;
            double distance;
            long remaining;
            int tier;
            if (this.visited.contains(chest) || this.ignoredUntil.getOrDefault(chest, 0L) > now || !this.isDungeonContainer(chest) || (tier = WardenLootPolicy.chestTier(remaining = this.remainingRespawn(chest), distance = AutoWarden.mc.field_1724.method_33571().method_1025(chest.method_46558()))) < 0) continue;
            double d = score = tier == 5 ? (double)remaining : this.weightedChestDistance(chest);
            if (tier > bestTier || tier == bestTier && score >= bestScore || this.isArmoredPlayerNear(chest) || this.isWardenNear(chest) && (remaining > 0L || !(distance <= 16.0)) || this.findStandSpot(chest) == null) continue;
            bestTier = tier;
            bestScore = score;
            this.preferredChest = chest;
        }
        return this.preferredChest;
    }

    private boolean isArmoredPlayerNear(class_2338 chest) {
        return AutoWarden.mc.field_1687.method_18456().stream().anyMatch(player -> player != AutoWarden.mc.field_1724 && player.method_5805() && player.method_6096() > 0 && player.method_5707(chest.method_46558()) < 20.0);
    }

    private boolean isWardenNear(class_2338 chest) {
        return this.recentWardenPositions.keySet().stream().anyMatch(pos -> pos.method_10262((class_2382)chest) < 25.0);
    }

    private void pathTo(class_2338 pos, long now) {
        boolean changed;
        if (pos == null) {
            return;
        }
        boolean bl = changed = !pos.equals((Object)this.movementGoal);
        if (changed || now >= this.nextPathAt && !this.isBaritonePathing()) {
            RotationController.INSTANCE.release(this);
            this.navigateTo(pos, 0, now);
            this.movementGoal = pos;
            this.nextPathAt = now + 500L;
        }
    }

    private void navigateTo(class_2338 pos, int radius, long now) {
        if (this.baritone == null) {
            return;
        }
        if (radius > 0) {
            this.baritone.getCustomGoalProcess().setGoalAndPath((Goal)new GoalNear(pos, radius));
        } else {
            this.baritone.getCustomGoalProcess().setGoalAndPath((Goal)new GoalBlock(pos));
        }
    }

    private IBaritone getPrimaryBaritone() {
        try {
            return BaritoneAPI.getProvider().getPrimaryBaritone();
        }
        catch (LinkageError | RuntimeException ignored) {
            return null;
        }
    }

    private boolean isBaritonePathing() {
        return this.baritone != null && this.baritone.getPathingBehavior().isPathing();
    }

    private void cancelBaritonePathing() {
        if (this.baritone != null) {
            this.baritone.getPathingBehavior().cancelEverything();
        }
    }

    private boolean moveToDungeonCenter(long now) {
        double dz;
        if (AutoWarden.mc.field_1724 == null) {
            return true;
        }
        if (this.farmCenterTarget == null) {
            this.farmCenterTarget = new class_2338(-1995, class_3532.method_15340((int)AutoWarden.mc.field_1724.method_31478(), (int)-60, (int)-33), -2002);
        }
        class_2338 center = this.farmCenterTarget;
        double dx = AutoWarden.mc.field_1724.method_23317() - (double)center.method_10263() - 0.5;
        if (dx * dx + (dz = AutoWarden.mc.field_1724.method_23321() - (double)center.method_10260() - 0.5) * dz <= 4.0) {
            if (center.equals((Object)this.movementGoal)) {
                this.cancelBaritonePathing();
            }
            this.movementGoal = null;
            this.farmCenterPending = false;
            this.farmCenterTarget = null;
            this.nextPathAt = 0L;
            return false;
        }
        if (!center.equals((Object)this.movementGoal) || now >= this.nextPathAt && !this.isBaritonePathing()) {
            this.cancelBaritonePathing();
            this.navigateTo(center, 2, now);
            this.movementGoal = center;
            this.nextPathAt = now + 2500L;
        }
        return true;
    }

    private void requireFarmCenter() {
        if (this.farmCenterPending) {
            return;
        }
        this.farmCenterPending = true;
        this.farmCenterTarget = null;
    }

    private void moveToTarget(long now) {
        class_2338 standTarget;
        long remaining;
        boolean atChest;
        if ((this.phase == Phase.RESTOCK || this.phase == Phase.DEPOSIT) && this.target == null) {
            if (this.containerPurpose == ContainerPurpose.DEPOSIT) {
                this.handleDeposit(now);
            } else {
                this.handleRestock(now);
            }
            return;
        }
        if (this.target == null || !this.validTargetForPhase()) {
            this.failTarget(now, 5000L);
            return;
        }
        class_243 targetVisible = this.containerPurpose == ContainerPurpose.LOOT ? this.visiblePoint(this.target) : null;
        boolean bl = atChest = targetVisible != null && AutoWarden.mc.field_1724.method_33571().method_1025(targetVisible) <= 20.25;
        if (this.containerPurpose == ContainerPurpose.LOOT && !atChest && now >= this.nextChestSelectionAt) {
            class_2338 pick = this.findPreferredChest(now);
            if (pick == null) {
                this.failTarget(now, 2000L);
                return;
            }
            if (!(pick.equals((Object)this.target) || this.remainingRespawn(this.target) > 25000L && now - this.phaseSince < 1000L)) {
                this.target = pick;
                this.changePhase(Phase.MOVING, now);
                this.nextPathAt = 0L;
            }
        }
        long l = remaining = this.containerPurpose == ContainerPurpose.LOOT ? this.remainingRespawn(this.target) : 0L;
        if (remaining > 0L) {
            this.readyChests.remove(this.target);
            if (now - this.phaseSince > 60000L) {
                this.failTarget(now, 30000L);
                return;
            }
        }
        if ((standTarget = this.findStandSpot(this.target)) == null) {
            this.failTarget(now, 30000L);
            return;
        }
        double distance = AutoWarden.mc.field_1724.method_73189().method_1022(standTarget.method_61082());
        if (distance < this.lastTargetDistance - 0.8) {
            this.lastTargetDistance = distance;
            this.lastProgressAt = now;
        }
        if (now - this.lastProgressAt > 15000L) {
            this.failTarget(now, 30000L);
            return;
        }
        class_243 visible = this.visiblePoint(this.target);
        if (visible != null && AutoWarden.mc.field_1724.method_33571().method_1025(visible) <= 20.25) {
            this.lookSilently(visible);
            this.stopWalking();
            if (remaining > 0L) {
                this.lastProgressAt = now;
                return;
            }
            this.changePhase(Phase.OPENING, now);
            return;
        }
        this.pathTo(standTarget, now);
    }

    private class_2338 findStandSpot(class_2338 chest) {
        int range = ServerUtil.isSpookyTime() ? 3 : 1;
        class_2338 best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int dx = -range; dx <= range; ++dx) {
            for (int dz = -range; dz <= range; ++dz) {
                int dy;
                if (dx == 0 && dz == 0) continue;
                int n = dy = ServerUtil.isSpookyTime() ? -3 : -1;
                while (dy <= 1) {
                    class_2338 side = chest.method_10069(dx, dy, dz);
                    boolean standable = ServerUtil.isSpookyTime() ? WardenSafeNavigator.canStand(side) : AutoWarden.mc.field_1687.method_8320(side).method_26215() && AutoWarden.mc.field_1687.method_8320(side.method_10084()).method_26215() && !AutoWarden.mc.field_1687.method_8320(side.method_10074()).method_26215();
                    double distance = AutoWarden.mc.field_1724.method_5707(side.method_61082());
                    if (standable && distance < bestDistance && this.canReachChest(side, chest)) {
                        best = side;
                        bestDistance = distance;
                    }
                    ++dy;
                }
            }
        }
        if (best != null) {
            return best;
        }
        if (ServerUtil.isSpookyTime()) {
            return null;
        }
        class_2338 above = chest.method_10084();
        if (AutoWarden.mc.field_1687.method_8320(above).method_26215() && AutoWarden.mc.field_1687.method_8320(above.method_10084()).method_26215() && this.canReachChest(above, chest)) {
            return above;
        }
        return null;
    }

    private boolean canReachChest(class_2338 from, class_2338 chest) {
        class_243 eye = from.method_46558().method_1031(0.0, (double)AutoWarden.mc.field_1724.method_18381(AutoWarden.mc.field_1724.method_18376()) - 0.5, 0.0);
        return this.visiblePoint(eye, chest) != null;
    }

    private void openTarget(long now) {
        class_3965 crosshairHit;
        class_3965 hit;
        if (this.target == null || !this.validTargetForPhase()) {
            this.failTarget(now, 5000L);
            return;
        }
        if (this.containerPurpose == ContainerPurpose.LOOT && this.remainingRespawn(this.target) > 0L) {
            this.stopWalking();
            this.changePhase(Phase.MOVING, now);
            return;
        }
        if (now < this.nextActionAt) {
            return;
        }
        class_239 class_2392 = AutoWarden.mc.field_1765;
        class_3965 class_39652 = hit = class_2392 instanceof class_3965 && (crosshairHit = (class_3965)class_2392).method_17777().equals((Object)this.target) && AutoWarden.mc.field_1724.method_33571().method_1025(crosshairHit.method_17784()) <= 20.25 ? crosshairHit : null;
        if (hit == null) {
            class_243 point = this.visiblePoint(this.target);
            if (point == null || AutoWarden.mc.field_1724.method_33571().method_1025(point) > 20.25) {
                this.changePhase(this.phaseForPurpose(), now);
                return;
            }
            hit = this.raycastTarget(this.target, point);
        }
        if (hit == null) {
            if (now - this.phaseSince > 1200L) {
                this.changePhase(this.phaseForPurpose(), now);
            }
            return;
        }
        this.selectSafeHotbarSlot();
        AutoWarden.mc.field_1724.method_6104(class_1268.field_5808);
        AutoWarden.mc.field_1761.method_2896(AutoWarden.mc.field_1724, class_1268.field_5808, hit);
        ++this.openAttempts;
        this.nextActionAt = now + 50L;
        this.changePhase(Phase.WAITING_GUI, now);
    }

    private void waitForGui(long now) {
        this.stopWalking();
        this.cancelBaritonePathing();
        if (now - this.phaseSince < 1500L) {
            return;
        }
        if (this.openAttempts < 4) {
            this.changePhase(Phase.OPENING, now);
        } else {
            this.failTarget(now, 30000L);
        }
    }

    private void handleContainer(class_1707 handler, long now) {
        int missingSpeed;
        class_3965 hit;
        class_239 class_2392;
        this.stopWalking();
        this.cancelBaritonePathing();
        if (this.target == null && (class_2392 = AutoWarden.mc.field_1765) instanceof class_3965 && this.isDungeonContainer((hit = (class_3965)class_2392).method_17777())) {
            this.target = hit.method_17777().method_10062();
        }
        if (this.target == null && !this.containers.isEmpty()) {
            this.target = this.containers.stream().filter(pos -> AutoWarden.mc.field_1724.method_5707(pos.method_46558()) <= 25.0).min(Comparator.comparingDouble(pos -> AutoWarden.mc.field_1724.method_5707(pos.method_46558()))).orElse(null);
        }
        if (!this.isExpectedContainer()) {
            return;
        }
        if (this.containerPurpose == ContainerPurpose.LOOT) {
            if (now < this.nextActionAt) {
                return;
            }
            int containerSlots = handler.method_7629().method_5439();
            boolean remainingLoot = false;
            for (int slot = 0; slot < containerSlots; ++slot) {
                class_1799 stack = handler.method_7611(slot).method_7677();
                if (!this.isLoot(stack)) continue;
                remainingLoot = true;
                if (!this.canReceiveLoot(handler, stack)) continue;
                class_1799 before = stack.method_7972();
                this.clickQuickMove(handler, slot);
                class_1799 after = handler.method_7611(slot).method_7677();
                if (!class_1799.method_31577((class_1799)before, (class_1799)after) || after.method_7947() < before.method_7947()) {
                    this.lootBlockedSince = 0L;
                } else if (this.lootBlockedSince == 0L) {
                    this.lootBlockedSince = now;
                } else if (now - this.lootBlockedSince >= 1500L) {
                    AutoWarden.mc.field_1724.method_7346();
                    this.lootBlockedSince = 0L;
                    if (this.hasDepositExtras()) {
                        this.handleDeposit(now);
                    } else {
                        this.failTarget(now, 5000L);
                    }
                    return;
                }
                this.nextActionAt = now + 100L;
                return;
            }
            if (remainingLoot) {
                AutoWarden.mc.field_1724.method_7346();
                if (this.hasDepositExtras()) {
                    this.handleDeposit(now);
                } else {
                    this.failTarget(now, 5000L);
                }
                return;
            }
            if (now - this.phaseSince < 250L) {
                return;
            }
            this.finishLoot(now);
            return;
        }
        if (now < this.nextActionAt) {
            return;
        }
        if (this.pendingTransfer != null) {
            this.tickPendingTransfer(now);
            return;
        }
        if (!AutoWarden.mc.field_1724.field_7512.method_34255().method_7960()) {
            this.settleUnexpectedCursor(handler, now);
            return;
        }
        int containerSlots = handler.method_7629().method_5439();
        if (!this.learnOpenedStorage(handler, containerSlots, now)) {
            return;
        }
        if (this.containerPurpose == ContainerPurpose.DEPOSIT) {
            boolean keptInvisibility = false;
            boolean keptFood = false;
            int moved = 0;
            for (int slot = containerSlots; slot < handler.field_7761.size(); ++slot) {
                class_1799 stack = handler.method_7611(slot).method_7677();
                if (stack.method_7960() || this.useSpeed.isValue() && this.isSpeedPotion(stack)) continue;
                if (this.isInvisibilityPotion(stack) && !keptInvisibility) {
                    keptInvisibility = true;
                    continue;
                }
                if (this.isFoodSupply(stack) && !keptFood) {
                    keptFood = true;
                    continue;
                }
                class_1799 before = stack.method_7972();
                this.clickQuickMove(handler, slot);
                class_1799 after = handler.method_7611(slot).method_7677();
                if (class_1799.method_31577((class_1799)before, (class_1799)after) && after.method_7947() >= before.method_7947()) {
                    if (this.depositBlockedSince == 0L) {
                        this.depositBlockedSince = now;
                    }
                    if (now - this.depositBlockedSince > 1500L) {
                        this.storageMessage("\u0421\u0443\u043d\u0434\u0443\u043a \u0434\u043b\u044f \u0432\u044b\u0433\u0440\u0443\u0437\u043a\u0438 \u0437\u0430\u043f\u043e\u043b\u043d\u0435\u043d, \u043e\u0436\u0438\u0434\u0430\u044e \u0441\u0432\u043e\u0431\u043e\u0434\u043d\u043e\u0435 \u043c\u0435\u0441\u0442\u043e.", now);
                    }
                    this.nextActionAt = now + 300L;
                    return;
                }
                this.depositBlockedSince = 0L;
                if (++moved < 4) continue;
                this.nextActionAt = now + 100L;
                return;
            }
            if (moved > 0) {
                this.nextActionAt = now + 100L;
                return;
            }
            this.finishDeposit(now);
            return;
        }
        int missingInvisibility = Math.max(0, 1 - this.countInventory(this::isInvisibilityPotion));
        if (missingInvisibility > 0) {
            this.prepareSupplySearch("invisibility");
            if (!this.takeFromStorage(handler, this::isInvisibilityPotion, missingInvisibility)) {
                this.rejectCurrentSupplyChest(now);
                return;
            }
            this.nextActionAt = now + 70L;
            return;
        }
        if (this.useSpeed.isValue() && (missingSpeed = Math.max(0, 1 - this.countInventory(this::isSpeedPotion))) > 0) {
            this.prepareSupplySearch("speed");
            if (this.takeFromStorage(handler, this::isSpeedPotion, missingSpeed)) {
                this.nextActionAt = now + 70L;
                return;
            }
            this.rejectCurrentSupplyChest(now);
            return;
        }
        int missingFood = Math.max(0, 3 - this.countInventory(this::isFoodSupply));
        if (missingFood > 0) {
            this.prepareSupplySearch("food");
            if (!this.takeFromStorage(handler, this::isFoodSupply, missingFood)) {
                this.rejectCurrentSupplyChest(now);
                return;
            }
            this.nextActionAt = now + 70L;
            return;
        }
        this.supplyChest = this.target;
        this.supplySearchKey = null;
        AutoWarden.mc.field_1724.method_7346();
        this.target = null;
        this.rejectedStorageChests.clear();
        this.containerPurpose = ContainerPurpose.LOOT;
        this.beginFarmTravel(now);
        this.changePhase(Phase.SEARCHING, now);
    }

    private boolean hasCollectableLoot(class_1707 handler) {
        for (int slot = 0; slot < handler.method_7629().method_5439(); ++slot) {
            class_1799 stack = handler.method_7611(slot).method_7677();
            if (!this.isLoot(stack) || !this.canReceiveLoot(handler, stack)) continue;
            return true;
        }
        return false;
    }

    private boolean canReceiveLoot(class_1707 handler, class_1799 stack) {
        for (class_1735 slot : handler.field_7761) {
            if (!(slot.field_7871 instanceof class_1661) || slot.method_34266() < 0 || slot.method_34266() >= 36 || !slot.method_7680(stack) || slot.method_7681() && (!class_1799.method_31577((class_1799)slot.method_7677(), (class_1799)stack) || slot.method_7677().method_7947() >= slot.method_7676(stack))) continue;
            return true;
        }
        return false;
    }

    private void finishLoot(long now) {
        this.lootBlockedSince = 0L;
        this.nextChestSelectionAt = 0L;
        class_2338 looted = this.target;
        AutoWarden.mc.field_1724.method_7346();
        if (looted != null) {
            this.readyChests.remove(looted);
            this.visited.add(looted);
            this.ignoredUntil.put(looted, now + 120000L);
            if (!this.respawnAt.containsKey(looted)) {
                long defaultRespawn = now + 180000L;
                this.respawnAt.put(looted, defaultRespawn);
                WardenTimerCache.update(this.currentAnarchy(), looted, defaultRespawn);
            }
        }
        this.target = null;
        this.openAttempts = 0;
        if (this.shouldDeposit()) {
            if (this.dangerNearby()) {
                this.changePhase(Phase.RETREATING, now);
            } else {
                this.handleDeposit(now);
            }
        } else if (this.dangerNearby()) {
            this.changePhase(Phase.RETREATING, now);
        } else {
            this.changePhase(Phase.SEARCHING, now);
        }
    }

    private void handleDeposit(long now) {
        class_2338 storage;
        if (!this.hasDepositExtras()) {
            this.target = null;
            this.containerPurpose = ContainerPurpose.LOOT;
            if (this.currentAnarchy() == this.baseAnarchyNumber() || this.needsRestock()) {
                this.handleRestock(now);
                return;
            }
            this.changePhase(Phase.SEARCHING, now);
            return;
        }
        if (!this.ensureStorageAnarchy(ContainerPurpose.DEPOSIT, now)) {
            return;
        }
        this.scanContainersAndStorage();
        class_2338 remembered = this.rememberedChest(this.learnedLootChests);
        if (remembered != null) {
            this.lootChest = remembered;
        }
        if ((storage = this.nearestStorageChest(this::hasHopperBelow)) == null && this.lootChest != null && this.isStorageContainer(this.lootChest) && !this.sameStorageContainer(this.lootChest, this.supplyChest) && !this.rejectedStorageChests.contains(this.lootChest)) {
            storage = this.lootChest;
        }
        if (storage == null) {
            storage = this.nearestStorageChest(pos -> !this.sameStorageContainer((class_2338)pos, this.supplyChest));
        }
        this.target = storage;
        if (this.target != null) {
            this.lootChest = this.target;
            this.changePhase(Phase.DEPOSIT, now);
        } else {
            this.stopWalking();
            this.storageMessage("\u0418\u0449\u0443 \u0441\u0443\u043d\u0434\u0443\u043a \u0434\u043b\u044f \u0432\u044b\u0433\u0440\u0443\u0437\u043a\u0438 \u0440\u044f\u0434\u043e\u043c \u0441\u043e \u0441\u043a\u043b\u0430\u0434\u043e\u043c.", now);
            this.nextActionAt = now + 500L;
            this.changePhase(Phase.DEPOSIT, now);
        }
    }

    private void handleRestock(long now) {
        if (this.hasDepositExtras()) {
            this.handleDeposit(now);
            return;
        }
        if (!this.needsRestock()) {
            this.target = null;
            this.containerPurpose = ContainerPurpose.LOOT;
            if (this.currentAnarchy() == this.baseAnarchyNumber()) {
                this.beginFarmTravel(now);
            }
            this.changePhase(Phase.SEARCHING, now);
            return;
        }
        this.prepareSupplySearch(this.nextMissingSupplyKey());
        if (!this.ensureStorageAnarchy(ContainerPurpose.RESTOCK, now)) {
            return;
        }
        this.scanContainersAndStorage();
        class_2338 rememberedLootChest = this.rememberedChest(this.learnedLootChests);
        if (rememberedLootChest != null) {
            this.lootChest = rememberedLootChest;
        }
        if (this.supplyChest == null) {
            this.supplyChest = this.rememberedChest(this.learnedSupplyChests);
        }
        if (this.target == null || !this.isStorageContainer(this.target) || this.rejectedStorageChests.contains(this.target)) {
            class_2338 class_23382 = this.target = this.supplyChest != null && this.isStorageContainer(this.supplyChest) && !this.sameStorageContainer(this.supplyChest, this.lootChest) && !this.rejectedStorageChests.contains(this.supplyChest) ? this.supplyChest : this.nearestStorageChest(pos -> !this.hasHopperBelow((class_2338)pos) && !this.sameStorageContainer((class_2338)pos, this.lootChest));
            if (this.target == null) {
                this.target = this.nearestStorageChest(pos -> !this.sameStorageContainer((class_2338)pos, this.lootChest));
            }
        }
        if (this.target != null) {
            this.changePhase(Phase.RESTOCK, now);
        } else {
            this.stopWalking();
            this.storageMessage("\u0416\u0434\u0443 \u043d\u0435\u0434\u043e\u0441\u0442\u0430\u044e\u0449\u0438\u0435 \u0440\u0430\u0441\u0445\u043e\u0434\u043d\u0438\u043a\u0438 \u0432\u043e \u0432\u0442\u043e\u0440\u043e\u043c \u0441\u0443\u043d\u0434\u0443\u043a\u0435.", now);
            this.rejectedStorageChests.clear();
            this.changePhase(Phase.RESTOCK, now);
            this.nextRestockAttemptAt = now + 4000L;
        }
    }

    private void storageMessage(String message, long now) {
        if (now < this.nextServerActionAt || AutoWarden.mc.field_1705 == null) {
            return;
        }
        AutoWarden.mc.field_1705.method_1743().method_1812((class_2561)class_2561.method_43470((String)("[AutoWarden] " + message)));
        this.nextServerActionAt = now + 5000L;
    }

    private boolean ensureStorageAnarchy(ContainerPurpose purpose, long now) {
        if (this.containerPurpose != purpose) {
            this.target = null;
            this.stopWalking();
        }
        this.containerPurpose = purpose;
        int destination = this.baseAnarchyNumber();
        if (destination <= 0) {
            this.stopWalking();
            if (now >= this.nextServerActionAt) {
                AutoWarden.mc.field_1705.method_1743().method_1812((class_2561)class_2561.method_43470((String)"[AutoWarden] \u0412\u043a\u043b\u044e\u0447\u0438 \u043c\u043e\u0434\u0443\u043b\u044c \u043d\u0430 \u0430\u043d\u0430\u0440\u0445\u0438\u0438 \u0441\u043e \u0441\u043a\u043b\u0430\u0434\u043e\u043c."));
                this.nextServerActionAt = now + 5000L;
            }
            return false;
        }
        if (this.currentAnarchy() == destination) {
            return true;
        }
        int currentFarm = this.currentAnarchy();
        if (currentFarm > 0 && currentFarm != destination) {
            this.activeFarmAnarchy = currentFarm;
        }
        this.storageTrip.begin(destination, purpose == ContainerPurpose.DEPOSIT ? WardenStorageTrip.Purpose.DEPOSIT : WardenStorageTrip.Purpose.RESTOCK);
        this.farmHomeSent = false;
        this.farmTravelActive = false;
        this.farmCenterPending = false;
        this.farmCenterTarget = null;
        this.pendingFarmAnarchy = 0;
        this.target = null;
        this.stopWalking();
        this.changePhase(Phase.SEARCHING, now);
        return false;
    }

    private void rejectCurrentSupplyChest(long now) {
        if (this.target != null && this.target.equals((Object)this.supplyChest)) {
            this.supplyChest = null;
        }
        this.rejectStorageChest(this.target);
        this.recoverPendingTransfer();
        AutoWarden.mc.field_1724.method_7346();
        this.target = null;
        this.changePhase(Phase.RESTOCK, now);
        this.nextActionAt = now + 300L;
    }

    private boolean learnOpenedStorage(class_1707 handler, int containerSlots, long now) {
        class_3965 hit;
        class_239 class_2392;
        if (this.target == null && (class_2392 = AutoWarden.mc.field_1765) instanceof class_3965 && this.isStorageContainer((hit = (class_3965)class_2392).method_17777())) {
            this.target = hit.method_17777().method_10062();
        }
        if (this.target == null) {
            this.target = this.nearestStorageChest(pos -> true);
        }
        if (this.target == null) {
            return false;
        }
        boolean containsSupplies = false;
        for (int slot = 0; slot < containerSlots; ++slot) {
            class_1799 stack = handler.method_7611(slot).method_7677();
            if (!this.isInvisibilityPotion(stack) && !this.isSpeedPotion(stack) && !this.isFoodSupply(stack)) continue;
            containsSupplies = true;
            break;
        }
        if (this.containerPurpose == ContainerPurpose.RESTOCK) {
            if (!this.sameStorageContainer(this.target, this.lootChest) && !this.hasHopperBelow(this.target)) {
                this.supplyChest = this.target == null ? null : this.target.method_10062();
                this.rememberChest(this.learnedSupplyChests, this.supplyChest);
                return true;
            }
            this.lootChest = this.target == null ? null : this.target.method_10062();
            this.rememberChest(this.learnedLootChests, this.lootChest);
            if (this.lootChest != null) {
                this.rejectedStorageChests.add(this.lootChest);
            }
            AutoWarden.mc.field_1724.method_7346();
            this.target = null;
            this.changePhase(Phase.RESTOCK, now);
            this.nextActionAt = now + 150L;
            return false;
        }
        if (containsSupplies && !this.hasHopperBelow(this.target) && !this.sameStorageContainer(this.target, this.rememberedChest(this.learnedLootChests))) {
            this.supplyChest = this.target == null ? null : this.target.method_10062();
            this.rememberChest(this.learnedSupplyChests, this.supplyChest);
            if (this.supplyChest != null) {
                this.rejectedStorageChests.add(this.supplyChest);
            }
            if (this.sameStorageContainer(this.target, this.lootChest)) {
                this.lootChest = null;
            }
            AutoWarden.mc.field_1724.method_7346();
            this.target = null;
            this.changePhase(Phase.DEPOSIT, now);
            this.nextActionAt = now + 150L;
            return false;
        }
        this.lootChest = this.target == null ? null : this.target.method_10062();
        this.rememberChest(this.learnedLootChests, this.lootChest);
        return true;
    }

    private class_2338 rememberedChest(TextSetting memory) {
        int anarchy = this.baseAnarchyNumber();
        if (anarchy <= 0 || memory.getText() == null) {
            return null;
        }
        for (String entry : memory.getText().split(";")) {
            String[] xyz;
            String[] pair = entry.split("=");
            if (pair.length != 2 || !pair[0].equals(String.valueOf(anarchy)) || (xyz = pair[1].split(",")).length != 3) continue;
            try {
                class_2338 pos = new class_2338(Integer.parseInt(xyz[0]), Integer.parseInt(xyz[1]), Integer.parseInt(xyz[2]));
                return this.isStorageContainer(pos) ? pos : null;
            }
            catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private void rememberChest(TextSetting memory, class_2338 pos) {
        int anarchy = this.baseAnarchyNumber();
        if (anarchy <= 0 || pos == null) {
            return;
        }
        StringBuilder value = new StringBuilder();
        String current = memory.getText() == null ? "" : memory.getText();
        for (String entry : current.split(";")) {
            if (entry.isBlank() || entry.startsWith(anarchy + "=")) continue;
            if (!value.isEmpty()) {
                value.append(';');
            }
            value.append(entry);
        }
        if (!value.isEmpty()) {
            value.append(';');
        }
        value.append(anarchy).append('=').append(pos.method_10263()).append(',').append(pos.method_10264()).append(',').append(pos.method_10260());
        memory.setText(value.toString());
    }

    private void finishDeposit(long now) {
        this.pvpLastTick = false;
        this.forcedDepositAt = now + 180000L;
        AutoWarden.mc.field_1724.method_7346();
        this.target = null;
        this.depositBlockedSince = 0L;
        this.containerPurpose = ContainerPurpose.RESTOCK;
        this.handleRestock(now);
    }

    private void retreat(long now) {
        if (!this.dangerNearby() || now - this.phaseSince > 8000L) {
            this.stopWalking();
            if (this.baritone != null) {
                this.cancelBaritonePathing();
            }
            if (this.shouldDeposit()) {
                this.handleDeposit(now);
            } else {
                this.changePhase(Phase.SEARCHING, now);
            }
            return;
        }
        if (this.target == null || now >= this.nextPathAt) {
            this.target = this.safeRetreatPoint();
            this.pathTo(this.target, now);
            this.nextPathAt = now + 1500L;
        }
    }

    private void waitOutPvp(long now) {
        this.containerPurpose = ContainerPurpose.DEPOSIT;
        if (this.phase != Phase.RETREATING) {
            this.target = null;
            this.stopWalking();
            if (this.baritone != null) {
                this.cancelBaritonePathing();
            }
            this.changePhase(Phase.RETREATING, now);
        }
        if (this.target == null || now >= this.nextPathAt) {
            this.target = this.safeRetreatPoint();
            this.pathTo(this.target, now);
            this.nextPathAt = now + 1000L;
        }
    }

    private boolean handleDeath(long now) {
        class_1309 class_13092;
        if (!AutoWarden.mc.field_1724.method_29504() && !(AutoWarden.mc.field_1755 instanceof class_418)) {
            this.deathReported = false;
            if (this.deathHomePending) {
                this.stopWalking();
                this.stopConsuming();
                this.cancelBaritonePathing();
                if (now < this.deathHomeNotBefore) {
                    return true;
                }
                this.deathHomePending = false;
                this.initialInvisDone = false;
                this.activatedAt = now;
                this.farmTravelActive = false;
                this.farmHomeSent = false;
                this.pendingFarmAnarchy = 0;
                this.activeFarmAnarchy = 0;
                this.resetWorldNavigation();
                this.forcedDepositAt = now + 180000L;
                this.containerPurpose = ContainerPurpose.RESTOCK;
                this.handleRestock(now);
                this.nextActionAt = now + 1500L;
                return true;
            }
            return false;
        }
        if (!this.deathHomePending) {
            this.deathHomePending = true;
            this.deathHomeNotBefore = now + 1500L;
        }
        this.stopWalking();
        this.stopConsuming();
        this.cancelBaritonePathing();
        if (this.reportOffenders.isValue() && !this.deathReported && (class_13092 = AutoWarden.mc.field_1724.method_6065()) instanceof class_1657) {
            class_1657 attacker = (class_1657)class_13092;
            this.sendCommand("report " + attacker.method_5477().getString() + " \u0447\u0438\u0442");
            this.deathReported = true;
        }
        if (now >= this.nextActionAt) {
            AutoWarden.mc.field_1724.method_7331();
            this.nextActionAt = now + 1000L;
        }
        return true;
    }

    private boolean tryStartConsumable() {
        if (AutoWarden.mc.field_1755 != null || this.consumeAction != ConsumeAction.NONE) {
            return false;
        }
        class_1293 invisibility = AutoWarden.mc.field_1724.method_6112(class_1294.field_5905);
        if (!(AutoWarden.mc.field_1724.method_5851() || invisibility != null && invisibility.method_5584() >= 400 || this.countInventory(this::isInvisibilityPotion) <= 0)) {
            return this.beginConsuming(ConsumeAction.INVISIBILITY, this::isInvisibilityPotion);
        }
        if (AutoWarden.mc.field_1724.method_7344().method_7586() <= 18 && this.countInventory(this::isFoodSupply) > 0) {
            return this.beginConsuming(ConsumeAction.FOOD, this::isFoodSupply);
        }
        if (this.useSpeed.isValue() && !AutoWarden.mc.field_1724.method_6059(class_1294.field_5904)) {
            if (AutoWarden.mc.field_1724.method_7357().method_7904(class_1802.field_8574.method_7854()) || AutoWarden.mc.field_1724.method_7357().method_7904(class_1802.field_8436.method_7854())) {
                return false;
            }
            if (this.countInventory(this::isSpeedPotion) > 0) {
                return this.beginConsuming(ConsumeAction.SPEED, this::isSpeedPotion);
            }
        }
        return false;
    }

    private boolean beginConsuming(ConsumeAction action, Predicate<class_1799> predicate) {
        class_1735 source = AutoWarden.mc.field_1724.field_7512.field_7761.stream().filter(slot -> slot.field_7871 instanceof class_1661).filter(slot -> slot.method_34266() >= 0 && slot.method_34266() < 36).filter(class_1735::method_7681).filter(slot -> predicate.test(slot.method_7677())).findFirst().orElse(null);
        if (source == null) {
            return false;
        }
        this.stopWalking();
        this.cancelBaritonePathing();
        this.originalHotbar = AutoWarden.mc.field_1724.method_31548().method_67532();
        this.swappedConsumeSlot = -1;
        if (source.method_34266() < 9) {
            AutoWarden.mc.field_1724.method_31548().method_61496(source.method_34266());
            AutoWarden.mc.field_1724.field_3944.method_52787((class_2596)new class_2868(source.method_34266()));
        } else {
            this.swappedConsumeSlot = source.field_7874;
            AutoWarden.mc.field_1761.method_2906(AutoWarden.mc.field_1724.field_7512.field_7763, source.field_7874, this.originalHotbar, class_1713.field_7791, (class_1657)AutoWarden.mc.field_1724);
        }
        if (!predicate.test(AutoWarden.mc.field_1724.method_6047())) {
            this.stopConsuming();
            return false;
        }
        this.consumeAction = action;
        this.consumeInitialCount = AutoWarden.mc.field_1724.method_6047().method_7947();
        this.consumeInitialHunger = AutoWarden.mc.field_1724.method_7344().method_7586();
        class_1293 effect = action == ConsumeAction.INVISIBILITY ? AutoWarden.mc.field_1724.method_6112(class_1294.field_5905) : (action == ConsumeAction.SPEED ? AutoWarden.mc.field_1724.method_6112(class_1294.field_5904) : null);
        this.consumeInitialEffectDuration = effect == null ? 0 : effect.method_5584();
        this.consumeStartedAt = System.currentTimeMillis();
        AutoWarden.mc.field_1690.field_1904.method_23481(true);
        class_1269 result = AutoWarden.mc.field_1761.method_2919((class_1657)AutoWarden.mc.field_1724, class_1268.field_5808);
        if (result.method_23665() && !AutoWarden.mc.field_1724.method_6115()) {
            AutoWarden.mc.field_1724.method_6019(class_1268.field_5808);
        }
        return true;
    }

    private boolean handleConsumption(long now) {
        boolean consumed;
        if (this.consumeAction == ConsumeAction.NONE) {
            return false;
        }
        this.stopWalking();
        this.cancelBaritonePathing();
        class_1799 held = AutoWarden.mc.field_1724.method_6047();
        boolean valid = switch (this.consumeAction.ordinal()) {
            case 1 -> this.isInvisibilityPotion(held);
            case 3 -> this.isSpeedPotion(held);
            case 2 -> this.isFoodSupply(held);
            default -> false;
        };
        boolean completed = switch (this.consumeAction.ordinal()) {
            case 1 -> this.effectRenewed((class_6880<class_1291>)class_1294.field_5905);
            case 3 -> this.effectRenewed((class_6880<class_1291>)class_1294.field_5904);
            case 2 -> {
                if (AutoWarden.mc.field_1724.method_7344().method_7586() > this.consumeInitialHunger) {
                    yield true;
                }
                yield false;
            }
            default -> true;
        };
        boolean bl = consumed = held.method_7947() < this.consumeInitialCount || !valid;
        if (completed || consumed && now - this.consumeStartedAt > 350L || now - this.consumeStartedAt > 4500L) {
            this.stopConsuming();
            this.nextActionAt = now + 500L;
        } else {
            class_1269 result;
            AutoWarden.mc.field_1690.field_1904.method_23481(true);
            if (!AutoWarden.mc.field_1724.method_6115() && now - this.consumeStartedAt > 250L && (result = AutoWarden.mc.field_1761.method_2919((class_1657)AutoWarden.mc.field_1724, class_1268.field_5808)).method_23665() && !AutoWarden.mc.field_1724.method_6115()) {
                AutoWarden.mc.field_1724.method_6019(class_1268.field_5808);
            }
        }
        return this.consumeAction != ConsumeAction.NONE;
    }

    private boolean effectRenewed(class_6880<class_1291> type) {
        class_1293 effect = AutoWarden.mc.field_1724.method_6112(type);
        return effect != null && effect.method_5584() > this.consumeInitialEffectDuration + 20;
    }

    private void stopConsuming() {
        AutoWarden.mc.field_1690.field_1904.method_23481(false);
        if (AutoWarden.mc.field_1724 == null) {
            this.consumeAction = ConsumeAction.NONE;
            return;
        }
        if (AutoWarden.mc.field_1724.method_6115() && AutoWarden.mc.field_1761 != null) {
            AutoWarden.mc.field_1761.method_2897((class_1657)AutoWarden.mc.field_1724);
        }
        if (this.swappedConsumeSlot >= 0 && this.originalHotbar >= 0 && AutoWarden.mc.field_1761 != null) {
            AutoWarden.mc.field_1761.method_2906(AutoWarden.mc.field_1724.field_7512.field_7763, this.swappedConsumeSlot, this.originalHotbar, class_1713.field_7791, (class_1657)AutoWarden.mc.field_1724);
        } else if (this.originalHotbar >= 0 && this.originalHotbar != AutoWarden.mc.field_1724.method_31548().method_67532()) {
            AutoWarden.mc.field_1724.method_31548().method_61496(this.originalHotbar);
            AutoWarden.mc.field_1724.field_3944.method_52787((class_2596)new class_2868(this.originalHotbar));
        }
        this.originalHotbar = -1;
        this.swappedConsumeSlot = -1;
        this.consumeAction = ConsumeAction.NONE;
    }

    private boolean freeOneHotbarSlot(long now) {
        if (AutoWarden.mc.field_1724 == null) {
            return false;
        }
        for (int hotbar = 0; hotbar < 9; ++hotbar) {
            if (!AutoWarden.mc.field_1724.method_31548().method_5438(hotbar).method_7960()) continue;
            return false;
        }
        int emptyInventory = -1;
        for (int slot = 9; slot < 36; ++slot) {
            if (!AutoWarden.mc.field_1724.method_31548().method_5438(slot).method_7960()) continue;
            emptyInventory = slot;
            break;
        }
        if (emptyInventory < 0) {
            return false;
        }
        for (int hotbar = 0; hotbar < 9; ++hotbar) {
            if (hotbar == AutoWarden.mc.field_1724.method_31548().method_67532()) continue;
            AutoWarden.mc.field_1761.method_2906(AutoWarden.mc.field_1724.field_7498.field_7763, emptyInventory, hotbar, class_1713.field_7791, (class_1657)AutoWarden.mc.field_1724);
            this.nextActionAt = now + 150L;
            return true;
        }
        return false;
    }

    private boolean moveHeldItemToInventory(long now) {
        if (AutoWarden.mc.field_1724 == null || AutoWarden.mc.field_1761 == null || this.consumeAction != ConsumeAction.NONE || AutoWarden.mc.field_1724.method_6115()) {
            return false;
        }
        int hotbar = AutoWarden.mc.field_1724.method_31548().method_67532();
        class_1799 held = AutoWarden.mc.field_1724.method_31548().method_5438(hotbar);
        if (held.method_7960()) {
            return false;
        }
        for (int inventory = 9; inventory < 36; ++inventory) {
            if (!AutoWarden.mc.field_1724.method_31548().method_5438(inventory).method_7960()) continue;
            AutoWarden.mc.field_1761.method_2906(AutoWarden.mc.field_1724.field_7498.field_7763, inventory, hotbar, class_1713.field_7791, (class_1657)AutoWarden.mc.field_1724);
            this.nextActionAt = now + 150L;
            return true;
        }
        return false;
    }

    private void scanContainers() {
        this.containers.clear();
        if (this.insideDungeon(AutoWarden.mc.field_1724.method_73189())) {
            for (int chunkX = -130; chunkX <= -121; ++chunkX) {
                for (int chunkZ = -130; chunkZ <= -121; ++chunkZ) {
                    try {
                        if (!AutoWarden.mc.field_1687.method_22340(new class_2338(chunkX << 4, -40, chunkZ << 4))) continue;
                        class_2818 chunk = AutoWarden.mc.field_1687.method_8497(chunkX, chunkZ);
                        for (Map.Entry entry : chunk.method_12214().entrySet()) {
                            if (!this.isDungeonChestPosition((class_2338)entry.getKey()) || !this.isChestState(chunk.method_8320((class_2338)entry.getKey()))) continue;
                            class_2338 chest = ((class_2338)entry.getKey()).method_10062();
                            this.containers.add(chest);
                            this.knownDungeonChests.add(chest);
                        }
                        this.dungeonPatrol.recordScan(chunkX, chunkZ);
                        continue;
                    }
                    catch (RuntimeException chunk) {
                        // empty catch block
                    }
                }
            }
        } else {
            class_1923 origin = new class_1923(AutoWarden.mc.field_1724.method_24515());
            for (int x = -4; x <= 4; ++x) {
                for (int z = -4; z <= 4; ++z) {
                    try {
                        class_2818 chunk = AutoWarden.mc.field_1687.method_8497(origin.field_9181 + x, origin.field_9180 + z);
                        for (Map.Entry entry : chunk.method_12214().entrySet()) {
                            if (!this.isChestState(chunk.method_8320((class_2338)entry.getKey()))) continue;
                            class_2338 chest = ((class_2338)entry.getKey()).method_10062();
                            this.containers.add(chest);
                            this.knownDungeonChests.add(chest);
                        }
                        continue;
                    }
                    catch (RuntimeException runtimeException) {
                        // empty catch block
                    }
                }
            }
        }
    }

    private void scanRespawnTimers(long now) {
        for (class_1297 entity : AutoWarden.mc.field_1687.method_18112()) {
            class_2338 chest;
            long duration;
            String text = this.getEntityTimerText(entity);
            if (text.isEmpty() || (duration = this.parseTimer(text)) < 0L || (chest = this.findChestBelow(entity.method_24515())) == null) continue;
            if (duration == 0L) {
                this.respawnAt.remove(chest);
                this.visited.remove(chest);
                this.ignoredUntil.remove(chest);
                this.readyChests.add(chest);
                continue;
            }
            long expiresAt = now + duration;
            this.respawnAt.put(chest, expiresAt);
            this.readyChests.remove(chest);
            this.knownDungeonChests.add(chest);
            WardenTimerCache.update(this.currentAnarchy(), chest, expiresAt);
        }
        class_243 playerPos = AutoWarden.mc.field_1724.method_73189();
        for (class_2338 chest : this.containers) {
            if (!this.isDungeonChestPosition(chest) || !AutoWarden.mc.field_1687.method_22340(chest) || !(playerPos.method_1025(chest.method_46558()) <= 1024.0) || this.visited.contains(chest) || this.respawnAt.containsKey(chest) || this.hasCooldownEntity(chest)) continue;
            this.readyChests.add(chest);
        }
    }

    private String getEntityTimerText(class_1297 entity) {
        String str;
        if (entity == null) {
            return "";
        }
        class_2561 customName = entity.method_5797();
        if (customName != null && !customName.getString().isBlank()) {
            return customName.getString();
        }
        class_2561 name = entity.method_5477();
        if (!(name == null || name.getString().isBlank() || entity instanceof class_1657 || (str = name.getString()).equalsIgnoreCase("Armor Stand") || str.equalsIgnoreCase("\u0421\u0442\u043e\u0439\u043a\u0430 \u0434\u043b\u044f \u0431\u0440\u043e\u043d\u0438"))) {
            return str;
        }
        return "";
    }

    private void syncSharedTimers(long now) {
        Iterator<Map.Entry<class_2338, Long>> it = this.respawnAt.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<class_2338, Long> entry = it.next();
            if (entry.getValue() > now) continue;
            if (this.isDungeonChestPosition(entry.getKey())) {
                this.visited.remove(entry.getKey());
                this.ignoredUntil.remove(entry.getKey());
                this.readyChests.add(entry.getKey());
            }
            it.remove();
        }
        Map<class_2338, Long> shared = WardenTimerCache.snapshot(this.currentAnarchy(), now);
        this.respawnAt.keySet().removeIf(pos -> !shared.containsKey(pos) && !this.readyChests.contains(pos));
        this.respawnAt.putAll(shared);
        shared.keySet().stream().filter(this::isDungeonChestPosition).forEach(this.knownDungeonChests::add);
    }

    private class_2338 findChestBelow(class_2338 origin) {
        for (int dy = 0; dy <= 5; ++dy) {
            class_2338 check = origin.method_10087(dy);
            if (!this.isDungeonContainer(check)) continue;
            return check.method_10062();
        }
        return null;
    }

    private boolean hasCooldownEntity(class_2338 pos) {
        if (AutoWarden.mc.field_1687 == null) {
            return false;
        }
        class_238 box = new class_238(pos).method_1009(2.5, 5.0, 2.5);
        for (class_1297 entity : AutoWarden.mc.field_1687.method_8335((class_1297)AutoWarden.mc.field_1724, box)) {
            String text;
            class_2338 timerPos = entity.method_24515();
            if (timerPos.method_10263() != pos.method_10263() || timerPos.method_10260() != pos.method_10260() || timerPos.method_10264() < pos.method_10264() || timerPos.method_10264() > pos.method_10264() + 5 || (text = this.getEntityTimerText(entity)).isEmpty() || this.parseTimer(text) < 0L) continue;
            return true;
        }
        return false;
    }

    private long parseTimer(String text) {
        if (text == null || text.isBlank()) {
            return -1L;
        }
        String clean = text.replaceAll("\u00a7.", "").toLowerCase(Locale.ROOT);
        Matcher matcher = CLOCK.matcher(clean);
        if (matcher.find()) {
            String[] parts = matcher.group(1).split(":");
            try {
                if (parts.length == 3) {
                    return ((long)Integer.parseInt(parts[0]) * 3600L + (long)Integer.parseInt(parts[1]) * 60L + (long)Integer.parseInt(parts[2])) * 1000L;
                }
                if (parts.length == 2) {
                    return ((long)Integer.parseInt(parts[0]) * 60L + (long)Integer.parseInt(parts[1])) * 1000L;
                }
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        if ((matcher = MINUTES.matcher(clean)).find()) {
            try {
                long mins = Long.parseLong(matcher.group(1));
                long secs = matcher.group(2) != null ? Long.parseLong(matcher.group(2)) : 0L;
                return (mins * 60L + secs) * 1000L;
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        if ((matcher = SECONDS.matcher(clean)).find()) {
            try {
                return Long.parseLong(matcher.group(1)) * 1000L;
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return -1L;
    }

    private boolean isLoot(class_1799 stack) {
        return stack != null && !stack.method_7960() && !this.isJunk(stack);
    }

    private boolean isJunk(class_1799 stack) {
        if (this.lootPriority.isSelected("\u041d\u0438\u0437\u043a\u0438\u0439")) {
            return false;
        }
        return this.isHighPriorityJunk(stack) || this.lootPriority.isSelected("\u0412\u044b\u0441\u043e\u043a\u0438\u0439") && this.isLowPriorityJunk(stack);
    }

    private boolean isHighPriorityJunk(class_1799 stack) {
        class_1792 item = stack.method_7909();
        return stack.method_31573(class_3489.field_42615) || stack.method_31573(class_3489.field_42612) || item instanceof class_1746 || item instanceof class_8052 && item != class_1802.field_41946 || item == class_1802.field_8894 || item == class_1802.field_8598 || item == class_1802.field_8547 || item == class_1802.field_8448 || item == class_1802.field_37523 || item == class_1802.field_28101 || item == class_1802.field_8466 || item == class_1802.field_38418 || item == class_1802.field_8323 || item == class_1802.field_20417 || item == class_1802.field_8711 || item == class_1802.field_8782 || item == class_1802.field_8261;
    }

    private boolean isLowPriorityJunk(class_1799 stack) {
        class_1792 item = stack.method_7909();
        class_9279 data = (class_9279)stack.method_58694(class_9334.field_49628);
        boolean potionPopper = data != null && data.method_57461().method_10562("PublicBukkitValues").flatMap(values2 -> values2.method_10558("minecraft:ftid")).filter("potion-popper"::equals).isPresent();
        return item instanceof class_1744 || stack.method_31573(class_3489.field_42614) || stack.method_31573(class_3489.field_42612) || item == class_1802.field_8233 || item == class_1802.field_38974 || item == class_1802.field_8864 || item == class_1802.field_8536 || item == class_1802.field_8347 || item == class_1802.field_8300 || item == class_1802.field_8503 || item == class_1802.field_8441 || item == class_1802.field_8149 || item == class_1802.field_22401 || item == class_1802.field_8814 || item == class_1802.field_8745 || item == class_1802.field_8815 || item == class_1802.field_8287 || item == class_1802.field_17515 || item == class_1802.field_8687 || item == class_1802.field_8479 || potionPopper || stack.method_57826(class_9334.field_52175) || item == class_1802.field_8070 || item == class_1802.field_8613 || item == class_1802.field_8235 || item == class_1802.field_8510 || item == class_1802.field_16314 || item == class_1802.field_8657 || item == class_1802.field_8805 || item == class_1802.field_8058 || item == class_1802.field_8348 || item == class_1802.field_8285;
    }

    private boolean takeFromStorage(class_1707 handler, Predicate<class_1799> predicate, int amount) {
        class_1735 source = handler.field_7761.stream().filter(slot -> !(slot.field_7871 instanceof class_1661)).filter(class_1735::method_7681).filter(slot -> predicate.test(slot.method_7677())).findFirst().orElse(null);
        class_1735 destination = handler.field_7761.stream().filter(slot -> slot.field_7871 instanceof class_1661).filter(slot -> slot.method_34266() >= 0 && slot.method_34266() < 36).filter(slot -> !slot.method_7681() || class_1799.method_31577((class_1799)slot.method_7677(), (class_1799)(source == null ? class_1799.field_8037 : source.method_7677())) && slot.method_7677().method_7947() < slot.method_7677().method_7914()).findFirst().orElse(null);
        if (source == null || destination == null) {
            return false;
        }
        if (this.containerPurpose == ContainerPurpose.RESTOCK && this.target != null) {
            this.supplyChest = this.target.method_10062();
            this.rememberChest(this.learnedSupplyChests, this.supplyChest);
        }
        if (amount >= source.method_7677().method_7947() || !source.method_7677().method_7946()) {
            this.clickQuickMove(handler, source.field_7874);
            return true;
        }
        this.startTransfer(source, destination, Math.min(amount, source.method_7677().method_7947()));
        return true;
    }

    private void prepareSupplySearch(String key) {
        if (key.equals(this.supplySearchKey)) {
            return;
        }
        this.supplySearchKey = key;
        this.rejectedStorageChests.clear();
    }

    private String nextMissingSupplyKey() {
        if (this.countInventory(this::isInvisibilityPotion) < 1) {
            return "invisibility";
        }
        if (this.useSpeed.isValue() && this.countInventory(this::isSpeedPotion) < 1) {
            return "speed";
        }
        if (this.countInventory(this::isFoodSupply) < 3) {
            return "food";
        }
        return "";
    }

    private void startTransfer(class_1735 source, class_1735 destination, int amount) {
        if (amount <= 0 || source == null || destination == null || !AutoWarden.mc.field_1724.field_7512.method_34255().method_7960()) {
            return;
        }
        this.pendingTransfer = new PendingTransfer(AutoWarden.mc.field_1724.field_7512.field_7763, source.field_7874, destination.field_7874, amount);
    }

    private void tickPendingTransfer(long now) {
        PendingTransfer transfer = this.pendingTransfer;
        if (transfer == null || AutoWarden.mc.field_1724.field_7512.field_7763 != transfer.syncId) {
            this.recoverPendingTransfer();
            return;
        }
        switch (transfer.stage.ordinal()) {
            case 0: {
                if (!AutoWarden.mc.field_1724.field_7512.method_34255().method_7960()) {
                    transfer.stage = TransferStage.PLACE;
                    transfer.lastClickAt = 0L;
                    return;
                }
                if (now - transfer.lastClickAt < 120L) break;
                AutoWarden.mc.field_1761.method_2906(transfer.syncId, transfer.sourceSlot, 0, class_1713.field_7790, (class_1657)AutoWarden.mc.field_1724);
                transfer.lastClickAt = now;
                break;
            }
            case 1: {
                if (AutoWarden.mc.field_1724.field_7512.method_34255().method_7960()) {
                    if (transfer.remaining <= 0) {
                        transfer.stage = TransferStage.RETURN;
                    }
                    return;
                }
                if (transfer.remaining > 0 && now - transfer.lastClickAt >= 120L) {
                    AutoWarden.mc.field_1761.method_2906(transfer.syncId, transfer.destinationSlot, 1, class_1713.field_7790, (class_1657)AutoWarden.mc.field_1724);
                    --transfer.remaining;
                    transfer.lastClickAt = now;
                }
                if (transfer.remaining > 0) break;
                transfer.stage = TransferStage.RETURN;
                break;
            }
            case 2: {
                if (AutoWarden.mc.field_1724.field_7512.method_34255().method_7960()) {
                    this.pendingTransfer = null;
                    this.nextActionAt = now + 300L;
                    return;
                }
                if (now - transfer.lastClickAt < 120L) break;
                AutoWarden.mc.field_1761.method_2906(transfer.syncId, transfer.sourceSlot, 0, class_1713.field_7790, (class_1657)AutoWarden.mc.field_1724);
                transfer.lastClickAt = now;
            }
        }
        this.nextActionAt = now + 120L;
    }

    private void recoverPendingTransfer() {
        if (this.pendingTransfer == null || AutoWarden.mc.field_1724 == null || AutoWarden.mc.field_1761 == null) {
            return;
        }
        if (AutoWarden.mc.field_1724.field_7512.field_7763 == this.pendingTransfer.syncId && !AutoWarden.mc.field_1724.field_7512.method_34255().method_7960()) {
            AutoWarden.mc.field_1761.method_2906(this.pendingTransfer.syncId, this.pendingTransfer.sourceSlot, 0, class_1713.field_7790, (class_1657)AutoWarden.mc.field_1724);
        }
        this.pendingTransfer = null;
    }

    private void settleUnexpectedCursor(class_1707 handler, long now) {
        class_1799 cursor = AutoWarden.mc.field_1724.field_7512.method_34255();
        class_1735 destination = handler.field_7761.stream().filter(slot -> !(slot.field_7871 instanceof class_1661)).filter(slot -> !slot.method_7681() || class_1799.method_31577((class_1799)slot.method_7677(), (class_1799)cursor) && slot.method_7677().method_7947() < slot.method_7677().method_7914()).findFirst().orElseGet(() -> handler.field_7761.stream().filter(slot -> slot.field_7871 instanceof class_1661).filter(slot -> slot.method_34266() >= 0 && slot.method_34266() < 36).filter(slot -> !slot.method_7681() || class_1799.method_31577((class_1799)slot.method_7677(), (class_1799)cursor) && slot.method_7677().method_7947() < slot.method_7677().method_7914()).findFirst().orElse(null));
        if (destination != null) {
            AutoWarden.mc.field_1761.method_2906(handler.field_7763, destination.field_7874, 0, class_1713.field_7790, (class_1657)AutoWarden.mc.field_1724);
        }
        this.nextActionAt = now + 70L;
    }

    private boolean canUseConsumablesHere() {
        if (AutoWarden.mc.field_1724 == null) {
            return false;
        }
        if (this.phase == Phase.DEPOSIT || this.phase == Phase.RESTOCK) {
            return false;
        }
        if (this.containerPurpose == ContainerPurpose.RESTOCK || this.containerPurpose == ContainerPurpose.DEPOSIT) {
            return false;
        }
        int currentAnarchy = this.currentAnarchy();
        return currentAnarchy > 0 && currentAnarchy != this.baseAnarchyNumber();
    }

    private boolean needsOperationalRestock() {
        class_1293 invisibility = AutoWarden.mc.field_1724.method_6112(class_1294.field_5905);
        boolean missingInvis = !AutoWarden.mc.field_1724.method_5851() && (invisibility == null || invisibility.method_5584() < 400) && this.countInventory(this::isInvisibilityPotion) < 1;
        boolean missingFood = AutoWarden.mc.field_1724.method_7344().method_7586() <= 18 && this.countInventory(this::isFoodSupply) < 1;
        boolean missingSpeed = this.useSpeed.isValue() && !AutoWarden.mc.field_1724.method_6059(class_1294.field_5904) && this.countInventory(this::isSpeedPotion) < 1;
        return missingInvis || missingFood || missingSpeed;
    }

    private boolean needsRestock() {
        boolean missingInvis = this.countInventory(this::isInvisibilityPotion) < 1;
        boolean missingFood = this.countInventory(this::isFoodSupply) < 3;
        boolean missingSpeed = this.useSpeed.isValue() && this.countInventory(this::isSpeedPotion) < 1;
        return missingInvis || missingFood || missingSpeed;
    }

    private boolean lootChestReadyWithin(long delay, long now) {
        if (!this.insideDungeon(AutoWarden.mc.field_1724.method_73189())) {
            return false;
        }
        return this.respawnAt.entrySet().stream().filter(entry -> this.isDungeonChestPosition((class_2338)entry.getKey())).filter(entry -> !this.visited.contains(entry.getKey())).filter(entry -> this.ignoredUntil.getOrDefault(entry.getKey(), 0L) <= now).anyMatch(entry -> (Long)entry.getValue() - now <= delay);
    }

    private int countInventory(Predicate<class_1799> predicate) {
        int count = 0;
        for (int slot = 0; slot < 36; ++slot) {
            class_1799 stack = AutoWarden.mc.field_1724.method_31548().method_5438(slot);
            if (stack.method_7960() || !predicate.test(stack)) continue;
            count += stack.method_7947();
        }
        return count;
    }

    private boolean isInvisibilityPotion(class_1799 stack) {
        return this.potionHas(stack, (class_6880<class_1291>)class_1294.field_5905, "\u0438\u043d\u0432\u0438\u0437", "invis");
    }

    private boolean isSpeedPotion(class_1799 stack) {
        return this.potionHas(stack, (class_6880<class_1291>)class_1294.field_5904, "\u0441\u043a\u043e\u0440\u043e\u0441\u0442", "speed", "swift");
    }

    private boolean potionHas(class_1799 stack, class_6880<class_1291> effect, String ... names) {
        if (stack.method_7960() || !stack.method_31574(class_1802.field_8574) && !stack.method_31574(class_1802.field_8436) && !stack.method_31574(class_1802.field_8150)) {
            return false;
        }
        if (this.containsAny(this.normalize(stack.method_7964().getString()), names)) {
            return true;
        }
        class_1844 contents = (class_1844)stack.method_58694(class_9334.field_49651);
        if (contents == null) {
            return false;
        }
        for (class_1293 instance : contents.method_57397()) {
            if (!instance.method_5579().equals(effect)) continue;
            return true;
        }
        return false;
    }

    private boolean isFoodSupply(class_1799 stack) {
        return stack != null && !stack.method_7960() && stack.method_31574(class_1802.field_8071);
    }

    private boolean dangerNearby() {
        for (class_1657 player : AutoWarden.mc.field_1687.method_18456()) {
            if (player == AutoWarden.mc.field_1724 || player.method_29504() || !AutoWarden.mc.field_1724.method_5851() || !(player.method_5739((class_1297)AutoWarden.mc.field_1724) <= 32.0f)) continue;
            return true;
        }
        for (class_1297 entity : AutoWarden.mc.field_1687.method_18112()) {
            class_7260 warden;
            if (!(entity instanceof class_7260) || (warden = (class_7260)entity).method_5739((class_1297)AutoWarden.mc.field_1724) > 30.0f) continue;
            class_243 toPlayer = AutoWarden.mc.field_1724.method_73189().method_1020(warden.method_73189());
            double targetYaw = Math.toDegrees(Math.atan2(toPlayer.field_1350, toPlayer.field_1352)) - 90.0;
            if (Math.abs(class_3532.method_15393((float)((float)((double)warden.method_73188() - targetYaw)))) > 10.0f || !(warden.method_5739((class_1297)AutoWarden.mc.field_1724) < 4.0f) && !(warden.method_18798().method_1026(toPlayer.method_1029()) > 0.01)) continue;
            return true;
        }
        return false;
    }

    private class_2338 safeRetreatPoint() {
        class_2338 best = AutoWarden.mc.field_1724.method_24515();
        double bestScore = -1.0;
        for (int angle = 0; angle < 360; angle += 30) {
            int x = (int)Math.floor(AutoWarden.mc.field_1724.method_23317() + Math.cos(Math.toRadians(angle)) * 25.0);
            int z = (int)Math.floor(AutoWarden.mc.field_1724.method_23321() + Math.sin(Math.toRadians(angle)) * 25.0);
            if (this.insideDungeon(AutoWarden.mc.field_1724.method_73189())) {
                x = class_3532.method_15340((int)x, (int)-2060, (int)-1931);
                z = class_3532.method_15340((int)z, (int)-2066, (int)-1939);
            }
            double score = Double.MAX_VALUE;
            for (class_1297 entity : AutoWarden.mc.field_1687.method_18112()) {
                if (entity == AutoWarden.mc.field_1724 || !(entity instanceof class_1657) && !(entity instanceof class_7260)) continue;
                score = Math.min(score, Math.hypot(entity.method_23317() - (double)x, entity.method_23321() - (double)z));
            }
            if (!(score > bestScore)) continue;
            class_2338 candidate = new class_2338(x, AutoWarden.mc.field_1724.method_31478(), z);
            if (ServerUtil.isSpookyTime() && !WardenSafeNavigator.canStand(candidate)) continue;
            bestScore = score;
            best = candidate;
        }
        return best;
    }

    private class_2338 nearestStorageChest(Predicate<class_2338> predicate) {
        return this.containers.stream().filter(this::isStorageContainer).filter(pos -> !this.rejectedStorageChests.contains(pos)).filter(predicate).min(Comparator.comparingDouble(pos -> AutoWarden.mc.field_1724.method_5707(pos.method_46558()))).orElse(null);
    }

    private void rejectStorageChest(class_2338 pos) {
        if (pos == null) {
            return;
        }
        this.rejectedStorageChests.add(pos.method_10062());
        for (class_2350 direction : class_2350.class_2353.field_11062) {
            class_2338 neighbor = pos.method_10093(direction);
            if (!this.sameStorageContainer(pos, neighbor)) continue;
            this.rejectedStorageChests.add(neighbor.method_10062());
        }
    }

    private boolean sameStorageContainer(class_2338 first, class_2338 second) {
        if (first == null || second == null || AutoWarden.mc.field_1687 == null) {
            return false;
        }
        if (first.equals((Object)second)) {
            return true;
        }
        class_2680 state = AutoWarden.mc.field_1687.method_8320(first);
        if (!(state.method_26204() instanceof class_2281) || state.method_11654((class_2769)class_2281.field_10770) == class_2745.field_12569) {
            return false;
        }
        return first.method_10093(class_2281.method_9758((class_2680)state)).equals((Object)second);
    }

    private void scanContainersAndStorage() {
        if (AutoWarden.mc.field_1687 == null || AutoWarden.mc.field_1724 == null) {
            return;
        }
        class_2338 playerPos = AutoWarden.mc.field_1724.method_24515();
        class_1923 origin = new class_1923(playerPos);
        for (int cx = -3; cx <= 3; ++cx) {
            for (int cz = -3; cz <= 3; ++cz) {
                try {
                    class_2818 chunk = AutoWarden.mc.field_1687.method_8497(origin.field_9181 + cx, origin.field_9180 + cz);
                    if (chunk == null) continue;
                    for (Map.Entry entry : chunk.method_12214().entrySet()) {
                        class_2338 pos = (class_2338)entry.getKey();
                        if (Math.abs(pos.method_10263() - playerPos.method_10263()) > 32 || Math.abs(pos.method_10264() - playerPos.method_10264()) > 16 || Math.abs(pos.method_10260() - playerPos.method_10260()) > 32 || !this.isStorageContainer(pos) || this.containers.contains(pos)) continue;
                        this.containers.add(pos.method_10062());
                    }
                    continue;
                }
                catch (RuntimeException runtimeException) {
                    // empty catch block
                }
            }
        }
    }

    private boolean validTargetForPhase() {
        if (this.target == null) {
            return false;
        }
        if (this.phase == Phase.RETREATING) {
            return true;
        }
        if (this.containerPurpose == ContainerPurpose.LOOT) {
            return this.isDungeonContainer(this.target);
        }
        return this.isStorageContainer(this.target);
    }

    private boolean isDungeonContainer(class_2338 pos) {
        return this.isDungeonChestPosition(pos) && AutoWarden.mc.field_1687 != null && (AutoWarden.mc.field_1687.method_8320(pos).method_27852(class_2246.field_10034) || AutoWarden.mc.field_1687.method_8320(pos).method_27852(class_2246.field_10380));
    }

    private boolean isDungeonChestPosition(class_2338 pos) {
        return pos != null && AutoWarden.mc.field_1687 != null && "minecraft:overworld".equals(AutoWarden.mc.field_1687.method_27983().method_29177().toString()) && pos.method_10263() >= -2070 && pos.method_10263() <= -1921 && pos.method_10264() >= -60 && pos.method_10264() <= -35 && pos.method_10260() >= -2076 && pos.method_10260() <= -1929;
    }

    private boolean isChestState(class_2680 state) {
        return state.method_27852(class_2246.field_10034) || state.method_27852(class_2246.field_10380);
    }

    private boolean isStorageContainer(class_2338 pos) {
        if (pos == null || AutoWarden.mc.field_1687 == null) {
            return false;
        }
        class_2586 entity = AutoWarden.mc.field_1687.method_8321(pos);
        return entity instanceof class_2595 || entity instanceof class_3719 || entity instanceof class_2627;
    }

    private boolean hasHopperBelow(class_2338 pos) {
        if (pos == null) {
            return false;
        }
        if (AutoWarden.mc.field_1687.method_8321(pos.method_10074()) instanceof class_2614) {
            return true;
        }
        for (class_2350 direction : class_2350.class_2353.field_11062) {
            class_2338 neighbor = pos.method_10093(direction);
            if (!(AutoWarden.mc.field_1687.method_8321(neighbor) instanceof class_2595) || !(AutoWarden.mc.field_1687.method_8321(neighbor.method_10074()) instanceof class_2614)) continue;
            return true;
        }
        return false;
    }

    private boolean insideDungeon(class_243 pos) {
        return AutoWarden.mc.field_1687 != null && "minecraft:overworld".equals(AutoWarden.mc.field_1687.method_27983().method_29177().toString()) && pos.field_1352 >= -2070.0 && pos.field_1352 <= -1921.0 && pos.field_1350 >= -2076.0 && pos.field_1350 <= -1929.0;
    }

    private boolean nearDungeonArea() {
        double dz;
        if (AutoWarden.mc.field_1724 == null || AutoWarden.mc.field_1687 == null) {
            return false;
        }
        if (!"minecraft:overworld".equals(AutoWarden.mc.field_1687.method_27983().method_29177().toString())) {
            return false;
        }
        double dx = AutoWarden.mc.field_1724.method_23317() - -2000.0;
        return dx * dx + (dz = AutoWarden.mc.field_1724.method_23321() - -2000.0) * dz <= 10000.0;
    }

    private class_243 visiblePoint(class_2338 pos) {
        return this.visiblePoint(AutoWarden.mc.field_1724.method_33571(), pos);
    }

    private class_243 visiblePoint(class_243 eye, class_2338 pos) {
        class_243 center = pos.method_46558();
        class_243 best = null;
        double bestSquared = Double.MAX_VALUE;
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                for (int z = -1; z <= 1; ++z) {
                    class_3965 hit;
                    class_243 point = center.method_1031((double)x * 0.4, (double)y * 0.4, (double)z * 0.4);
                    double squared = point.method_1025(center);
                    if (squared >= bestSquared || (hit = AutoWarden.mc.field_1687.method_17742(new class_3959(eye, point, class_3959.class_3960.field_17558, class_3959.class_242.field_1348, (class_1297)AutoWarden.mc.field_1724))).method_17783() != class_239.class_240.field_1332 || !hit.method_17777().equals((Object)pos)) continue;
                    best = point;
                    bestSquared = squared;
                }
            }
        }
        return best;
    }

    private class_3965 raycastTarget(class_2338 pos, class_243 point) {
        class_3965 hit = AutoWarden.mc.field_1687.method_17742(new class_3959(AutoWarden.mc.field_1724.method_33571(), point, class_3959.class_3960.field_17558, class_3959.class_242.field_1348, (class_1297)AutoWarden.mc.field_1724));
        return hit.method_17783() == class_239.class_240.field_1332 && hit.method_17777().equals((Object)pos) ? hit : null;
    }

    private void lookSilently(class_243 point) {
        if (point == null || AutoWarden.mc.field_1724 == null) {
            return;
        }
        Angle targetAngle = AngleUtil.calculateAngle(point).adjustSensitivity();
        RotationController.INSTANCE.rotateTo(targetAngle, 1, RotationConfig.DEFAULT, TaskPriority.HIGH_IMPORTANCE_2, this);
    }

    private void stopWalking() {
        this.movementGoal = null;
        this.nextPathAt = 0L;
        if (this.baritone != null) {
            this.cancelBaritonePathing();
        }
        if (AutoWarden.mc.field_1690 != null) {
            AutoWarden.mc.field_1690.field_1894.method_23481(false);
            AutoWarden.mc.field_1690.field_1881.method_23481(false);
            AutoWarden.mc.field_1690.field_1913.method_23481(false);
            AutoWarden.mc.field_1690.field_1849.method_23481(false);
            AutoWarden.mc.field_1690.field_1867.method_23481(false);
            AutoWarden.mc.field_1690.field_1903.method_23481(false);
        }
        if (AutoWarden.mc.field_1724 != null) {
            AutoWarden.mc.field_1724.method_5728(false);
        }
        this.walking = false;
    }

    private void selectSafeHotbarSlot() {
        class_1799 held = AutoWarden.mc.field_1724.method_6047();
        if (!held.method_31574(class_1802.field_8366) && !held.method_7964().getString().contains("\u2605")) {
            return;
        }
        for (int slot = 0; slot < 9; ++slot) {
            class_1799 stack = AutoWarden.mc.field_1724.method_31548().method_5438(slot);
            if (!stack.method_7960() && (stack.method_31574(class_1802.field_8366) || stack.method_7964().getString().contains("\u2605"))) continue;
            AutoWarden.mc.field_1724.method_31548().method_61496(slot);
            return;
        }
    }

    private boolean shouldDeposit() {
        if (!this.hasDepositExtras()) {
            return false;
        }
        int occupiedSlots = 0;
        for (int i = 0; i < 36; ++i) {
            if (AutoWarden.mc.field_1724.method_31548().method_5438(i).method_7960()) continue;
            ++occupiedSlots;
        }
        return occupiedSlots > WardenLootPolicy.depositThreshold(this.lootPriority.getSelected(), ServerUtil.isPvp());
    }

    private boolean hasDepositExtras() {
        if (AutoWarden.mc.field_1724 == null) {
            return false;
        }
        boolean keptInvisibility = false;
        boolean keptFood = false;
        for (int i = 0; i < 36; ++i) {
            class_1799 stack = AutoWarden.mc.field_1724.method_31548().method_5438(i);
            if (stack.method_7960() || this.useSpeed.isValue() && this.isSpeedPotion(stack)) continue;
            if (!keptInvisibility && this.isInvisibilityPotion(stack)) {
                keptInvisibility = true;
                continue;
            }
            if (!keptFood && this.isFoodSupply(stack)) {
                keptFood = true;
                continue;
            }
            return true;
        }
        return false;
    }

    private boolean isExpectedContainer() {
        if (this.containerPurpose != ContainerPurpose.LOOT) {
            return true;
        }
        return this.target != null && this.isDungeonContainer(this.target) || this.insideDungeon(AutoWarden.mc.field_1724.method_73189());
    }

    private void clickQuickMove(class_1707 handler, int slot) {
        AutoWarden.mc.field_1761.method_2906(handler.field_7763, slot, 0, class_1713.field_7794, (class_1657)AutoWarden.mc.field_1724);
    }

    private long remainingRespawn(class_2338 pos) {
        return this.respawnAt.getOrDefault(pos, 0L) - System.currentTimeMillis();
    }

    private String normalize(String text) {
        return text == null ? "" : text.replaceAll("\u00a7.", "").toLowerCase(Locale.ROOT).trim();
    }

    private boolean containsAny(String text, String ... parts) {
        for (String part : parts) {
            if (!text.contains(part)) continue;
            return true;
        }
        return false;
    }

    private String digits(String text) {
        return text == null ? "" : text.replaceAll("\\D", "");
    }

    private String nextFarmAnarchy() {
        String raw = this.farmAnarchies.getText();
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String[] values2 = raw.split(",");
        ArrayList<String> valid = new ArrayList<String>();
        for (String v : values2) {
            int anarchy = this.parseAnarchy(v);
            if (anarchy <= 0 || anarchy == this.baseAnarchyNumber()) continue;
            valid.add(String.valueOf(anarchy));
        }
        if (valid.isEmpty()) {
            return "";
        }
        return (String)valid.get(Math.floorMod(this.farmIndex++, valid.size()));
    }

    private void beginFarmTravel(long now) {
        String value;
        if (this.farmTravelActive) {
            return;
        }
        String string = value = this.activeFarmAnarchy > 0 && this.getAnarchies().contains(this.activeFarmAnarchy) && this.activeFarmAnarchy != this.baseAnarchyNumber() ? String.valueOf(this.activeFarmAnarchy) : this.nextFarmAnarchy();
        if (value.isEmpty()) {
            if (now >= this.nextServerActionAt) {
                AutoWarden.mc.field_1705.method_1743().method_1812((class_2561)class_2561.method_43470((String)"[AutoWarden] \u0414\u043e\u0431\u0430\u0432\u044c \u0430\u043d\u0430\u0440\u0445\u0438\u044e \u0434\u043b\u044f \u0444\u0430\u0440\u043c\u0430: .warden add <\u0430\u043d\u0430\u0440\u0445\u0438\u044f>"));
                this.nextServerActionAt = now + 5000L;
            }
            return;
        }
        try {
            this.activeFarmAnarchy = this.pendingFarmAnarchy = Integer.parseInt(value);
        }
        catch (NumberFormatException ignored) {
            this.pendingFarmAnarchy = 0;
            return;
        }
        this.farmHomeSent = false;
        this.farmTravelActive = true;
        this.farmSwitchIssuedAt = now;
        this.initialInvisDone = AutoWarden.mc.field_1724 != null && AutoWarden.mc.field_1724.method_6059(class_1294.field_5905);
        int currentAnarchy = this.currentAnarchy();
        if (currentAnarchy <= 0) {
            if (ServerUtil.isSpookyTime()) {
                this.sendCommand("an" + this.pendingFarmAnarchy);
                this.nextServerActionAt = now + 5000L;
            } else {
                this.nextServerActionAt = now + 1000L;
            }
            return;
        }
        if (currentAnarchy != this.pendingFarmAnarchy) {
            this.sendCommand("an" + this.pendingFarmAnarchy);
            this.nextServerActionAt = now + 8000L;
        } else if (this.nearDungeonArea()) {
            this.farmHomeSent = true;
            this.requireFarmCenter();
        } else {
            if (!(this.countInventory(this::isInvisibilityPotion) <= 0 || AutoWarden.mc.field_1724 != null && AutoWarden.mc.field_1724.method_6059(class_1294.field_5905))) {
                return;
            }
            this.sendCommand("home");
            this.farmHomeSent = true;
            this.requireFarmCenter();
            this.nextServerActionAt = now + 2500L;
        }
    }

    private void handleFarmTravel(long now) {
        if (this.farmHomeSent) {
            if (!this.insideDungeon(AutoWarden.mc.field_1724.method_73189()) && !this.nearDungeonArea() && now >= this.nextServerActionAt) {
                if (!(this.countInventory(this::isInvisibilityPotion) <= 0 || AutoWarden.mc.field_1724 != null && AutoWarden.mc.field_1724.method_6059(class_1294.field_5905))) {
                    return;
                }
                this.sendCommand("home");
                this.requireFarmCenter();
                this.nextServerActionAt = now + 2500L;
            }
            return;
        }
        if (this.pendingFarmAnarchy <= 0) {
            this.beginFarmTravel(now);
            return;
        }
        int currentAnarchy = this.currentAnarchy();
        if (currentAnarchy <= 0) {
            if (now >= this.nextServerActionAt) {
                if (ServerUtil.isSpookyTime()) {
                    this.sendCommand("an" + this.pendingFarmAnarchy);
                    this.farmSwitchIssuedAt = now;
                    this.nextServerActionAt = now + 5000L;
                } else {
                    this.nextServerActionAt = now + 1000L;
                }
            }
            return;
        }
        if (currentAnarchy != this.pendingFarmAnarchy) {
            if (now >= this.nextServerActionAt) {
                this.sendCommand("an" + this.pendingFarmAnarchy);
                this.farmSwitchIssuedAt = now;
                this.nextServerActionAt = now + 8000L;
            }
            return;
        }
        if (!(this.farmHomeSent || this.farmSwitchIssuedAt != 0L && now - this.farmSwitchIssuedAt < 1700L)) {
            if (!(this.countInventory(this::isInvisibilityPotion) <= 0 || AutoWarden.mc.field_1724 != null && AutoWarden.mc.field_1724.method_6059(class_1294.field_5905))) {
                return;
            }
            this.farmHomeSent = true;
            if (!this.nearDungeonArea()) {
                this.sendCommand("home");
                this.requireFarmCenter();
                this.nextServerActionAt = now + 2500L;
            } else {
                this.requireFarmCenter();
            }
        }
    }

    private void sendCommand(String command) {
        String clean;
        String string = clean = command == null ? "" : command.trim();
        if (clean.equalsIgnoreCase("home") && (this.currentAnarchy() == this.baseAnarchyNumber() || this.containerPurpose != ContainerPurpose.LOOT || this.currentAnarchy() != this.pendingFarmAnarchy)) {
            return;
        }
        if (!clean.isEmpty()) {
            AutoWarden.mc.field_1724.field_3944.method_45730(clean);
        }
    }

    private int spookyAnarchy() {
        int current = ServerUtil.getAnarchy();
        if (AutoWarden.mc.field_1687 == null) {
            return current;
        }
        class_269 scoreboard = AutoWarden.mc.field_1687.method_8428();
        class_266 objective = scoreboard.method_1189(class_8646.field_45157);
        if (objective == null) {
            return current;
        }
        String title = objective.method_1114().getString().replaceAll("\u00a7.", "");
        Matcher matcher = SPOOKY_ANARCHY.matcher(title);
        if (!matcher.find()) {
            return current;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        }
        catch (NumberFormatException ignored) {
            return current;
        }
    }

    private int currentAnarchy() {
        return ServerUtil.isSpookyTime() ? this.spookyAnarchy() : ServerUtil.getAnarchy();
    }

    private void failTarget(long now, long ignoreFor) {
        this.nextChestSelectionAt = 0L;
        this.lootBlockedSince = 0L;
        if (this.target != null) {
            this.ignoredUntil.put(this.target, now + ignoreFor);
        }
        this.target = null;
        this.openAttempts = 0;
        this.stopWalking();
        this.cancelBaritonePathing();
        this.changePhase(Phase.SEARCHING, now);
    }

    private Phase phaseForPurpose() {
        return switch (this.containerPurpose.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> Phase.MOVING;
            case 1 -> Phase.DEPOSIT;
            case 2 -> Phase.RESTOCK;
        };
    }

    private void changePhase(Phase next, long now) {
        this.phase = next;
        this.phaseSince = now;
        this.lastProgressAt = now;
        this.lastTargetDistance = this.target == null ? Double.MAX_VALUE : AutoWarden.mc.field_1724.method_73189().method_1022(this.target.method_46558());
    }

    private void resetRuntime() {
        this.storageTrip.clear();
        this.activeFarmAnarchy = 0;
        this.depositBlockedSince = 0L;
        this.dungeonPatrols.clear();
        this.resetWorldNavigation();
        this.deathHomePending = false;
        this.deathHomeNotBefore = 0L;
        this.rejectedStorageChests.clear();
        this.supplyChest = null;
        this.lootChest = null;
        this.farmIndex = 0;
        this.nextServerActionAt = 0L;
        this.pendingTransfer = null;
        this.supplySearchKey = null;
        this.pendingFarmAnarchy = 0;
        this.farmHomeSent = false;
        this.farmTravelActive = false;
        this.farmCenterPending = false;
        this.farmCenterTarget = null;
        this.farmSwitchIssuedAt = 0L;
        this.forcedDepositAt = System.currentTimeMillis() + 180000L;
        this.initialInvisDone = false;
        RotationController.INSTANCE.release(this);
    }

    private int baseAnarchyNumber() {
        return this.parseAnarchy(this.baseAnarchy.getText());
    }

    private int parseAnarchy(String text) {
        if (text == null) {
            return 0;
        }
        try {
            int value = Integer.parseInt(text.trim());
            return value >= 1 && value <= 999 ? value : 0;
        }
        catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private void loadAnarchySettings() {
        this.anarchies.clear();
        int base = this.baseAnarchyNumber();
        if (base > 0) {
            this.anarchies.add(base);
        }
        for (String value : this.farmAnarchies.getText().split(",")) {
            int anarchy = this.parseAnarchy(value);
            if (anarchy <= 0 || this.anarchies.contains(anarchy)) continue;
            this.anarchies.add(anarchy);
        }
    }

    private boolean selectStorageAnarchy() {
        int current = this.currentAnarchy();
        if (current <= 0) {
            return false;
        }
        this.loadAnarchySettings();
        if (this.baseAnarchyNumber() <= 0) {
            this.anarchies.remove((Object)current);
            this.anarchies.addFirst(current);
            this.syncAnarchySettings();
        }
        return true;
    }

    public static AutoWarden getInstance() {
        return Instance.get(AutoWarden.class);
    }

    public List<Integer> getAnarchies() {
        this.loadAnarchySettings();
        return List.copyOf(this.anarchies);
    }

    public boolean addAnarchy(int anarchy) {
        this.loadAnarchySettings();
        if (anarchy < 1 || anarchy > 999 || this.anarchies.contains(anarchy) || this.anarchies.size() >= 10) {
            return false;
        }
        this.anarchies.add(anarchy);
        this.syncAnarchySettings();
        return true;
    }

    public boolean setBaseAnarchy(int anarchy) {
        this.loadAnarchySettings();
        if (anarchy < 1 || anarchy > 999 || !this.anarchies.contains(anarchy) && this.anarchies.size() >= 10) {
            return false;
        }
        this.anarchies.remove((Object)anarchy);
        this.anarchies.addFirst(anarchy);
        this.syncAnarchySettings();
        return true;
    }

    public boolean removeAnarchy(int anarchy) {
        this.loadAnarchySettings();
        boolean removed = this.anarchies.remove((Object)anarchy);
        if (removed) {
            this.syncAnarchySettings();
        }
        return removed;
    }

    public void clearAnarchies() {
        this.anarchies.clear();
        this.syncAnarchySettings();
    }

    private void syncAnarchySettings() {
        this.baseAnarchy.setText(this.anarchies.isEmpty() ? "" : String.valueOf(this.anarchies.getFirst()));
        this.farmAnarchies.setText(this.anarchies.stream().skip(1L).map(String::valueOf).collect(Collectors.joining(",")));
    }

    private void resetWorldNavigation() {
        this.dungeonPatrol = this.dungeonPatrols.computeIfAbsent(this.currentAnarchy(), ignored -> new DungeonPatrol(-2070, -1921, -2076, -1929));
        this.containers.clear();
        this.recentWardenPositions.clear();
        this.movementGoal = null;
        this.preferredChest = null;
        this.lootBlockedSince = 0L;
        this.nextChestSelectionAt = 0L;
        this.knownDungeonChests.clear();
        this.readyChests.clear();
        this.respawnAt.clear();
        this.syncSharedTimers(System.currentTimeMillis());
        this.ignoredUntil.clear();
        this.visited.clear();
        this.target = null;
        this.phase = Phase.SEARCHING;
        this.containerPurpose = ContainerPurpose.LOOT;
        this.consumeAction = ConsumeAction.NONE;
        this.phaseSince = System.currentTimeMillis();
        this.nextTimerScanAt = 0L;
        this.nextPathAt = 0L;
        this.nextScanAt = 0L;
        this.nextActionAt = 0L;
        this.openAttempts = 0;
        this.originalHotbar = -1;
        this.walking = false;
        this.waitPatrolIndex = 0;
        this.patrolTarget = null;
        this.patrolSector = null;
        this.lastPatrolTickAt = 0L;
        this.patrolLegMillis = 0L;
        this.patrolStuckMillis = 0L;
        this.patrolDistance = Double.MAX_VALUE;
    }

    public String getPatrolStatus() {
        if (this.dungeonPatrol == null) {
            return "\u041f\u0430\u0442\u0440\u0443\u043b\u044c \u0435\u0449\u0451 \u043d\u0435 \u0437\u0430\u043f\u0443\u0441\u043a\u0430\u043b\u0441\u044f.";
        }
        return "\u0410\u043d\u0430\u0440\u0445\u0438\u044f " + this.currentAnarchy() + ", \u0441\u043a\u043b\u0430\u0434 " + this.baseAnarchyNumber() + (String)(this.storageTrip.active() ? ", \u043f\u0435\u0440\u0435\u0445\u043e\u0434 \u043d\u0430 " + this.storageTrip.destination() : "") + ": \u043f\u0440\u043e\u0439\u0434\u0435\u043d\u043e \u0443\u0447\u0430\u0441\u0442\u043a\u043e\u0432 " + this.dungeonPatrol.visitedCount() + "/" + this.dungeonPatrol.total() + ", \u043f\u0440\u043e\u0441\u043a\u0430\u043d\u0438\u0440\u043e\u0432\u0430\u043d\u043e \u0447\u0430\u043d\u043a\u043e\u0432 \u0437\u0430 \u043a\u0440\u0443\u0433 " + this.dungeonPatrol.scannedCount() + "/" + this.dungeonPatrol.total() + ", \u043e\u0442\u043b\u043e\u0436\u0435\u043d\u043e \u0434\u043e \u043f\u043e\u0432\u0442\u043e\u0440\u043d\u043e\u0439 \u043f\u043e\u043f\u044b\u0442\u043a\u0438 " + this.dungeonPatrol.deferredCount(System.currentTimeMillis()) + ", \u043f\u043e\u043b\u043d\u044b\u0445 \u043a\u0440\u0443\u0433\u043e\u0432 " + this.dungeonPatrol.completedPasses() + ".";
    }

    public Phase getCurrentPhase() {
        return this.phase;
    }

    public class_2338 getTargetChestPos() {
        return this.target;
    }

    public static enum Phase {
        SEARCHING,
        MOVING,
        OPENING,
        WAITING_GUI,
        RETREATING,
        DEPOSIT,
        RESTOCK;

    }

    private static enum ContainerPurpose {
        LOOT,
        DEPOSIT,
        RESTOCK;

    }

    private static enum ConsumeAction {
        NONE,
        INVISIBILITY,
        FOOD,
        SPEED;

    }

    private static final class PendingTransfer {
        private final int syncId;
        private final int sourceSlot;
        private final int destinationSlot;
        private int remaining;
        private TransferStage stage = TransferStage.PICKUP;
        private long lastClickAt;

        private PendingTransfer(int syncId, int sourceSlot, int destinationSlot, int remaining) {
            this.syncId = syncId;
            this.sourceSlot = sourceSlot;
            this.destinationSlot = destinationSlot;
            this.remaining = remaining;
        }
    }

    private static enum TransferStage {
        PICKUP,
        PLACE,
        RETURN;

    }
}

