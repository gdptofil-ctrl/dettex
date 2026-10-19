/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_1268
 *  net.minecraft.class_1269
 *  net.minecraft.class_1291
 *  net.minecraft.class_1293
 *  net.minecraft.class_1294
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1542
 *  net.minecraft.class_1657
 *  net.minecraft.class_1661
 *  net.minecraft.class_1703
 *  net.minecraft.class_1707
 *  net.minecraft.class_1713
 *  net.minecraft.class_1735
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1844
 *  net.minecraft.class_2246
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_2350$class_2353
 *  net.minecraft.class_2382
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_2595
 *  net.minecraft.class_2596
 *  net.minecraft.class_2680
 *  net.minecraft.class_2708
 *  net.minecraft.class_2868
 *  net.minecraft.class_3532
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_3965
 *  net.minecraft.class_418
 *  net.minecraft.class_476
 *  net.minecraft.class_5892
 *  net.minecraft.class_6880
 *  net.minecraft.class_746
 *  net.minecraft.class_8168
 *  net.minecraft.class_9334
 */
package ru.recoin.implement.features.modules.misc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.class_124;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2382;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2595;
import net.minecraft.class_2596;
import net.minecraft.class_2680;
import net.minecraft.class_2708;
import net.minecraft.class_2868;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_418;
import net.minecraft.class_476;
import net.minecraft.class_5892;
import net.minecraft.class_6880;
import net.minecraft.class_746;
import net.minecraft.class_8168;
import net.minecraft.class_9334;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.TextSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.QuickLogger;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.other.BaritoneCompat;
import ru.recoin.common.util.world.ServerUtil;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.DeathScreenEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.draggables.Notifications;

public final class AutoEnd
extends Module {
    private static final int MIN_ANARCHY = 101;
    private static final int MAX_ANARCHY = 905;
    private static final int SEARCH_RADIUS = 40;
    private static final int DUNGEON_FLOOR_TOLERANCE = 3;
    private static final int SAND_SEARCH_DEPTH = 18;
    private static final int ABOVE_POT_SEARCH_HEIGHT = 18;
    private static final int EMPTY_SCANS_BEFORE_RETURN = 8;
    private static final int MIN_LOOT_BEFORE_RETURN = 5;
    private static final int MIN_FOOD_SUPPLY = 2;
    private static final long ACTION_DELAY = 120L;
    private static final long SWITCH_DELAY = 2000L;
    private static final long COMBAT_TIME = 30000L;
    private static final long LOOT_WAIT = 2500L;
    private static final int BUFF_REFRESH_TICKS = 200;
    private static final long CONSUME_TIMEOUT = 5000L;
    private static final long HOME_RETRY_DELAY = 12000L;
    private static final long POT_REACH_TIMEOUT = 8000L;
    private static final long PATH_REISSUE_DELAY = 1500L;
    private static final long DESCENT_TIMEOUT = 10000L;
    private static AutoEnd instance;
    public final BooleanSetting takeSpeed = new BooleanSetting("\u0411\u0440\u0430\u0442\u044c \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c", "\u0411\u0440\u0430\u0442\u044c \u043e\u0434\u043d\u043e \u0437\u0435\u043b\u044c\u0435 \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u0438 \u043f\u0435\u0440\u0435\u0434 \u043f\u043e\u0445\u043e\u0434\u043e\u043c \u0432 \u042d\u043d\u0434").setValue(false);
    public final BooleanSetting reportKillers = new BooleanSetting("\u0420\u0435\u043f\u043e\u0440\u0442\u0438\u0442\u044c \u043e\u0431\u0438\u0434\u0447\u0438\u043a\u043e\u0432", "\u041f\u043e\u0441\u043b\u0435 \u0441\u043c\u0435\u0440\u0442\u0438 \u043e\u0442\u043f\u0440\u0430\u0432\u043b\u044f\u0442\u044c /report <\u043d\u0438\u043a> \u0447\u0438\u0442").setValue(false);
    private final BooleanSetting chatActions = new BooleanSetting("\u0414\u0435\u0439\u0441\u0442\u0432\u0438\u044f \u0432 \u0447\u0430\u0442", "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u044d\u0442\u0430\u043f\u044b AutoEnd \u043e\u0442 \u0438\u043c\u0435\u043d\u0438 DettexClient").setValue(true);
    private final ValueSetting dungeonRadius = new ValueSetting("\u0420\u0430\u0434\u0438\u0443\u0441 \u0434\u0430\u043d\u0436\u0430", "\u0417\u0430 \u044d\u0442\u043e\u0439 \u0434\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u0435\u0439 AutoEnd \u0432\u043e\u0437\u0432\u0440\u0430\u0449\u0430\u0435\u0442\u0441\u044f \u043a X=0, Z=0").setValue(96.0f).range(32, 256);
    private final TextSetting baseAnarchy = new TextSetting("\u041e\u0441\u043d\u043e\u0432\u043d\u0430\u044f \u0430\u043d\u0430\u0440\u0445\u0438\u044f", "\u0410\u043d\u0430\u0440\u0445\u0438\u044f \u0441 \u0441\u0443\u043d\u0434\u0443\u043a\u043e\u043c \u0434\u043b\u044f \u0440\u0430\u0441\u0445\u043e\u0434\u043d\u0438\u043a\u043e\u0432 \u0438 \u0434\u043e\u0431\u044b\u0447\u0438").setText("").setMax(3);
    private final TextSetting endAnarchy = new TextSetting("\u042d\u043d\u0434-\u0430\u043d\u0430\u0440\u0445\u0438\u044f", "\u0410\u043d\u0430\u0440\u0445\u0438\u044f \u0441 \u043a\u0430\u0441\u0442\u043e\u043c\u043d\u044b\u043c \u043c\u0438\u0440\u043e\u043c \u042d\u043d\u0434\u0430").setText("").setMax(3);
    private final TextSetting endWarp = new TextSetting("\u0412\u0430\u0440\u043f \u0432 \u042d\u043d\u0434\u0435", "\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u043b\u0438\u0447\u043d\u043e\u0433\u043e \u0432\u0430\u0440\u043f\u0430 \u0432 \u0434\u0430\u043d\u0436\u0435; AutoEnd \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0435\u0442 /warp <\u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435>").setText("").setMax(32);
    private final Set<class_2338> exhaustedPots = new HashSet<class_2338>();
    private final Set<class_2338> exhaustedSand = new HashSet<class_2338>();
    private final Set<class_2338> rejectedStorageChests = new HashSet<class_2338>();
    private final List<class_1799> departureInventory = new ArrayList<class_1799>();
    private Object baritone;
    private Phase phase = Phase.SWITCH_BASE;
    private class_2338 storageChest;
    private class_2338 supplyChest;
    private class_2338 lootChest;
    private class_2338 target;
    private class_2338 patrolTarget;
    private long nextActionAt;
    private long phaseStartedAt;
    private long combatUntil;
    private long nextScanAt;
    private long patrolExpiresAt;
    private int emptyScans;
    private int dungeonFloorY = Integer.MIN_VALUE;
    private int lootActionsThisRun;
    private int previousSelectedSlot = -1;
    private int swappedBrushSlot = -1;
    private boolean deathReported;
    private String supplySearchKey;
    private PendingTransfer pendingTransfer;
    private Phase resumeAfterBuffs = Phase.SEARCHING;
    private Phase resumeAfterOrigin = Phase.SEARCHING;
    private BuffConsumeAction consumeAction = BuffConsumeAction.NONE;
    private int originalBuffHotbar = -1;
    private int swappedBuffSlot = -1;
    private int consumeInitialDuration;
    private int consumeInitialStackCount;
    private int consumeInitialHunger;
    private long consumeStartedAt;
    private boolean homeRequired;
    private boolean homeCommandSent;
    private class_2338 homeCommandOrigin;
    private long homeCommandAt;
    private boolean homeTeleportConfirmed;
    private int descentStartY = Integer.MIN_VALUE;
    private long descentStartedAt;
    private class_2338 lastPathTarget;
    private int lastPathRadius = -1;
    private long lastPathIssuedAt;
    private class_243 lastPathPlayerPos;
    private long lastPathProgressAt;
    private Boolean previousAllowBreak;
    private Boolean previousAllowPlace;
    private Boolean previousAllowInventory;
    private Boolean previousAllowSprint;

    public AutoEnd() {
        super("AutoEnd", "Auto End", ModuleCategory.MISC);
        instance = this;
        this.setup(this.takeSpeed, this.reportKillers, this.chatActions, this.dungeonRadius, this.baseAnarchy, this.endAnarchy, this.endWarp);
    }

    public static AutoEnd getInstance() {
        return instance;
    }

    public boolean configure(int base, int end) {
        if (!AutoEnd.validAnarchy(base) || !AutoEnd.validAnarchy(end) || base == end) {
            return false;
        }
        this.baseAnarchy.setText(Integer.toString(base));
        this.endAnarchy.setText(Integer.toString(end));
        return true;
    }

    public int addAnarchy(int value) {
        if (!AutoEnd.validAnarchy(value)) {
            return 0;
        }
        if (!AutoEnd.validAnarchy(this.getBaseAnarchy())) {
            this.baseAnarchy.setText(Integer.toString(value));
            return 1;
        }
        if (!AutoEnd.validAnarchy(this.getEndAnarchy()) && value != this.getBaseAnarchy()) {
            this.endAnarchy.setText(Integer.toString(value));
            return 2;
        }
        return 0;
    }

    public int getBaseAnarchy() {
        return AutoEnd.parseAnarchy(this.baseAnarchy.getText());
    }

    public int getEndAnarchy() {
        return AutoEnd.parseAnarchy(this.endAnarchy.getText());
    }

    public String status() {
        return "\u0431\u0430\u0437\u0430=" + AutoEnd.displayAnarchy(this.getBaseAnarchy()) + ", \u044d\u043d\u0434=" + AutoEnd.displayAnarchy(this.getEndAnarchy()) + ", \u0432\u0430\u0440\u043f=" + (this.getEndWarp().isEmpty() ? "\u043d\u0435 \u0437\u0430\u0434\u0430\u043d" : this.getEndWarp()) + ", \u044d\u0442\u0430\u043f=" + this.phase.title;
    }

    @Override
    public void activate() {
        if (!AutoEnd.validAnarchy(this.getBaseAnarchy()) || !AutoEnd.validAnarchy(this.getEndAnarchy())) {
            this.notifyUser("\u00a7c\u0421\u043d\u0430\u0447\u0430\u043b\u0430 \u0443\u043a\u0430\u0436\u0438 \u0430\u043d\u0430\u0440\u0445\u0438\u0438: .autoend add <\u0431\u0430\u0437\u0430> <\u044d\u043d\u0434>");
            this.setState(false);
            return;
        }
        if (!AutoEnd.validWarpName(this.getEndWarp())) {
            this.notifyUser("\u00a7c\u0423\u043a\u0430\u0436\u0438 \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u0432\u0430\u0440\u043f\u0430 \u0432 \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0435 AutoEnd \u00ab\u0412\u0430\u0440\u043f \u0432 \u042d\u043d\u0434\u0435\u00bb");
            this.setState(false);
            return;
        }
        this.baritone = BaritoneCompat.getPrimaryBaritone();
        if (this.baritone == null) {
            this.notifyUser("\u00a7cAutoEnd \u0442\u0440\u0435\u0431\u0443\u0435\u0442 \u0443\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u043d\u044b\u0439 Baritone");
            this.setState(false);
            return;
        }
        this.previousAllowBreak = BaritoneCompat.getBooleanSetting("allowBreak");
        this.previousAllowPlace = BaritoneCompat.getBooleanSetting("allowPlace");
        this.previousAllowInventory = BaritoneCompat.getBooleanSetting("allowInventory");
        this.previousAllowSprint = BaritoneCompat.getBooleanSetting("allowSprint");
        BaritoneCompat.setBooleanSetting("allowBreak", false);
        BaritoneCompat.setBooleanSetting("allowPlace", false);
        BaritoneCompat.setBooleanSetting("allowInventory", false);
        BaritoneCompat.setBooleanSetting("allowSprint", true);
        this.exhaustedPots.clear();
        this.exhaustedSand.clear();
        this.departureInventory.clear();
        this.rejectedStorageChests.clear();
        this.storageChest = null;
        this.supplyChest = null;
        this.lootChest = null;
        this.supplySearchKey = null;
        this.pendingTransfer = null;
        this.consumeAction = BuffConsumeAction.NONE;
        this.originalBuffHotbar = -1;
        this.swappedBuffSlot = -1;
        this.homeRequired = false;
        this.homeCommandSent = false;
        this.homeCommandOrigin = null;
        this.homeCommandAt = 0L;
        this.homeTeleportConfirmed = false;
        this.descentStartY = Integer.MIN_VALUE;
        this.descentStartedAt = 0L;
        this.lastPathTarget = null;
        this.lastPathRadius = -1;
        this.lastPathIssuedAt = 0L;
        this.lastPathPlayerPos = null;
        this.lastPathProgressAt = 0L;
        this.resumeAfterBuffs = Phase.SEARCHING;
        this.resumeAfterOrigin = Phase.SEARCHING;
        this.target = null;
        this.patrolTarget = null;
        this.combatUntil = 0L;
        this.nextActionAt = 0L;
        this.nextScanAt = 0L;
        this.emptyScans = 0;
        this.dungeonFloorY = Integer.MIN_VALUE;
        this.lootActionsThisRun = 0;
        this.deathReported = false;
        this.changePhase(Phase.SWITCH_BASE);
        this.notifyUser("\u00a7a\u0426\u0438\u043a\u043b AutoEnd \u0437\u0430\u043f\u0443\u0449\u0435\u043d");
        this.action("AutoEnd \u0437\u0430\u043f\u0443\u0449\u0435\u043d. \u0413\u043e\u0442\u043e\u0432\u043b\u044e \u043f\u043e\u0445\u043e\u0434 \u0432 \u042d\u043d\u0434.");
    }

    @Override
    public void deactivate() {
        this.action("AutoEnd \u043e\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d.");
        this.stopBuffConsumption();
        this.releaseBrush();
        this.stopDescentMovement();
        this.cancelPathing();
        this.restoreBaritoneSettings();
        this.recoverPendingTransfer();
        if (AutoEnd.mc.field_1724 != null && AutoEnd.mc.field_1755 instanceof class_476) {
            AutoEnd.mc.field_1724.method_7346();
        }
    }

    @EventHandler
    public void onDeath(DeathScreenEvent event) {
        if (event.getTicksSinceDeath() > 2) {
            return;
        }
        this.handleDeath();
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        if (event.isSend()) {
            return;
        }
        class_2596<?> class_25962 = event.getPacket();
        if (class_25962 instanceof class_5892) {
            class_5892 packet = (class_5892)class_25962;
            if (AutoEnd.mc.field_1724 != null && packet.comp_2275() == AutoEnd.mc.field_1724.method_5628()) {
                this.handleDeath();
                return;
            }
        }
        if (event.getPacket() instanceof class_2708 && this.phase == Phase.END_HOME && this.homeCommandSent) {
            this.homeTeleportConfirmed = true;
        }
    }

    private void handleDeath() {
        class_1657 player;
        if (this.deathReported || AutoEnd.mc.field_1724 == null) {
            return;
        }
        this.deathReported = true;
        this.cancelPathing();
        this.stopBuffConsumption();
        this.releaseBrush();
        this.stopDescentMovement();
        this.homeRequired = true;
        this.homeCommandSent = false;
        this.homeCommandOrigin = null;
        this.homeCommandAt = 0L;
        this.homeTeleportConfirmed = false;
        this.dungeonFloorY = Integer.MIN_VALUE;
        this.combatUntil = 0L;
        this.resumeAfterBuffs = Phase.SEARCHING;
        this.changePhase(Phase.SWITCH_BASE);
        if (!this.reportKillers.isValue()) {
            return;
        }
        class_1309 attacker = AutoEnd.mc.field_1724.method_6065();
        if (attacker instanceof class_1657 && !(player = (class_1657)attacker).method_5667().equals(AutoEnd.mc.field_1724.method_5667())) {
            AutoEnd.mc.field_1724.field_3944.method_45730("report " + player.method_5477().getString() + " \u0447\u0438\u0442");
            this.notifyUser("\u00a7e\u041e\u0442\u043f\u0440\u0430\u0432\u043b\u0435\u043d \u0440\u0435\u043f\u043e\u0440\u0442 \u043d\u0430 " + player.method_5477().getString());
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        class_1703 class_17032;
        boolean keepBrushInHand;
        if (AutoEnd.mc.field_1724 == null || AutoEnd.mc.field_1687 == null || AutoEnd.mc.field_1761 == null) {
            return;
        }
        if (AutoEnd.mc.field_1724.method_29504() || AutoEnd.mc.field_1755 instanceof class_418) {
            this.handleDeath();
            return;
        }
        this.deathReported = false;
        long now = System.currentTimeMillis();
        if (this.consumeAction != BuffConsumeAction.NONE) {
            this.handleBuffConsumption(now);
            return;
        }
        boolean bl = keepBrushInHand = this.phase == Phase.BRUSHING && AutoEnd.mc.field_1724.method_6047().method_31574(class_1802.field_42716);
        if (AutoEnd.mc.field_1755 == null && this.stowHeldItems(!keepBrushInHand, now)) {
            return;
        }
        if (AutoEnd.mc.field_1755 instanceof class_476 && (class_17032 = AutoEnd.mc.field_1724.field_7512) instanceof class_1707) {
            class_1707 handler = (class_1707)class_17032;
            if (this.phase == Phase.PREPARE || this.phase == Phase.DEPOSIT) {
                this.handleStorage(handler, now);
                return;
            }
        }
        if (this.homeRequired && ServerUtil.getAnarchy() == this.getEndAnarchy() && this.isEndWorkPhase(this.phase)) {
            this.target = null;
            this.patrolTarget = null;
            this.cancelPathing();
            this.resumeAfterBuffs = Phase.SEARCHING;
            this.changePhase(Phase.END_BUFFS);
            return;
        }
        if (ServerUtil.getAnarchy() == this.getEndAnarchy() && this.isEndWorkPhase(this.phase) && this.needsBuffRefresh()) {
            this.resumeAfterBuffs = this.phase;
            this.changePhase(Phase.END_BUFFS);
        }
        if (ServerUtil.getAnarchy() == this.getEndAnarchy() && this.isEndWorkPhase(this.phase) && AutoEnd.mc.field_1724.method_7344().method_7586() <= 17) {
            if (!this.beginBuffConsumption(BuffConsumeAction.FOOD, stack -> stack.method_31574(class_1802.field_8071))) {
                this.returnToBaseForBuff(now, "\u0437\u043e\u043b\u043e\u0442\u044b\u0435 \u043c\u043e\u0440\u043a\u043e\u0432\u043a\u0438");
            }
            return;
        }
        if (ServerUtil.getAnarchy() == this.getEndAnarchy() && this.isEndWorkPhase(this.phase) && this.outsideDungeon()) {
            this.resumeAfterOrigin = Phase.SEARCHING;
            this.target = null;
            this.patrolTarget = null;
            this.emptyScans = 0;
            this.changePhase(Phase.END_ORIGIN);
        }
        if (now < this.nextActionAt) {
            return;
        }
        switch (this.phase.ordinal()) {
            case 0: {
                this.switchToAnarchy(this.getBaseAnarchy(), Phase.OPEN_BASE, now);
                break;
            }
            case 1: {
                this.openStorage(now);
                break;
            }
            case 2: 
            case 3: {
                if (AutoEnd.mc.field_1755 instanceof class_476) break;
                this.openStorage(now);
                break;
            }
            case 4: {
                this.switchToAnarchy(this.getEndAnarchy(), Phase.END_BUFFS, now);
                break;
            }
            case 5: {
                this.reachEndOrigin(now);
                break;
            }
            case 6: {
                this.prepareEndBuffs(now);
                break;
            }
            case 7: {
                this.enterEndHome(now);
                break;
            }
            case 8: {
                this.descendAtOrigin(now);
                break;
            }
            case 9: {
                this.searchEnd(now);
                break;
            }
            case 10: {
                this.breakPot(now);
                break;
            }
            case 11: {
                this.collectDrops(now);
                break;
            }
            case 12: {
                this.brushSand(now);
                break;
            }
            case 13: {
                this.waitCombat(now);
            }
        }
    }

    private void switchToAnarchy(int anarchy, Phase after, long now) {
        if (anarchy == this.getBaseAnarchy() && ServerUtil.getAnarchy() == this.getEndAnarchy() && this.combatActive(now) && !this.homeRequired) {
            this.changePhase(Phase.COMBAT_WAIT);
            return;
        }
        if (ServerUtil.getAnarchy() == anarchy) {
            if (after == Phase.OPEN_BASE) {
                this.rejectedStorageChests.clear();
                this.supplySearchKey = null;
            }
            this.storageChest = after == Phase.OPEN_BASE ? this.storageChest : null;
            this.target = null;
            this.patrolTarget = null;
            this.emptyScans = 0;
            if (after == Phase.END_BUFFS) {
                this.homeCommandSent = false;
                this.resumeAfterBuffs = Phase.SEARCHING;
            }
            this.changePhase(after);
            return;
        }
        if (now - this.phaseStartedAt < 350L) {
            return;
        }
        this.sendCommand("an" + anarchy);
        this.nextActionAt = now + 2000L;
    }

    private void openStorage(long now) {
        class_2338 preferred;
        if (ServerUtil.getAnarchy() != this.getBaseAnarchy()) {
            this.changePhase(Phase.SWITCH_BASE);
            return;
        }
        boolean preparing = this.departureInventory.isEmpty();
        class_2338 class_23382 = preferred = preparing ? this.supplyChest : this.lootChest;
        if (this.storageChest == null && preferred != null && !this.rejectedStorageChests.contains(preferred) && AutoEnd.mc.field_1687.method_8321(preferred) instanceof class_2595) {
            this.storageChest = preferred;
        }
        if (this.storageChest == null || !(AutoEnd.mc.field_1687.method_8321(this.storageChest) instanceof class_2595)) {
            this.storageChest = this.nearestChest(28);
        }
        if (this.storageChest == null) {
            if (preparing && !this.rejectedStorageChests.isEmpty()) {
                this.stopForMissing("\u043d\u0443\u0436\u043d\u044b\u0435 \u0440\u0430\u0441\u0445\u043e\u0434\u043d\u0438\u043a\u0438");
            } else {
                this.notifyUser("\u00a7cAutoEnd: \u0440\u044f\u0434\u043e\u043c \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d \u043f\u043e\u0434\u0445\u043e\u0434\u044f\u0449\u0438\u0439 \u0441\u0443\u043d\u0434\u0443\u043a");
                this.nextActionAt = now + 2000L;
            }
            return;
        }
        if (!AutoEnd.mc.field_1724.method_24515().method_19771((class_2382)this.storageChest, 4.5)) {
            this.pathNear(this.storageChest, 3);
            this.nextActionAt = now + 350L;
            return;
        }
        this.cancelPathing();
        AutoEnd.mc.field_1761.method_2896(AutoEnd.mc.field_1724, class_1268.field_5808, new class_3965(class_243.method_24953((class_2382)this.storageChest), class_2350.field_11036, this.storageChest, false));
        this.nextActionAt = now + 500L;
        if (this.phase == Phase.OPEN_BASE) {
            this.changePhase(this.departureInventory.isEmpty() ? Phase.PREPARE : Phase.DEPOSIT);
        }
    }

    private void handleStorage(class_1707 handler, long now) {
        int missingSpeed;
        if (now < this.nextActionAt) {
            return;
        }
        if (this.pendingTransfer != null) {
            this.tickPendingTransfer(now);
            return;
        }
        if (!AutoEnd.mc.field_1724.field_7512.method_34255().method_7960()) {
            this.settleUnexpectedCursor(handler, now);
            return;
        }
        if (this.phase == Phase.DEPOSIT) {
            if (this.lootChest == null && this.storageChest != null && this.storageChest.equals((Object)this.supplyChest) && this.tryNextStorageChest("\u0441\u0432\u043e\u0431\u043e\u0434\u043d\u044b\u0439 \u0441\u0443\u043d\u0434\u0443\u043a \u0434\u043b\u044f \u0434\u043e\u0431\u044b\u0447\u0438", false)) {
                return;
            }
            Transfer excess = this.findExcessLoot(handler);
            if (excess != null) {
                this.lootChest = this.storageChest;
                this.startTransfer(excess.from, excess.to, excess.amount);
                this.nextActionAt = now + 120L;
                return;
            }
            if (this.hasExcessLoot()) {
                if (this.tryNextStorageChest("\u043c\u0435\u0441\u0442\u043e \u0434\u043b\u044f \u0434\u043e\u0431\u044b\u0447\u0438", true)) {
                    return;
                }
                return;
            }
            this.departureInventory.clear();
            this.rejectedStorageChests.clear();
            this.rejectChest(this.storageChest);
            this.supplySearchKey = null;
            AutoEnd.mc.field_1724.method_7346();
            this.storageChest = null;
            this.changePhase(Phase.OPEN_BASE);
            this.nextActionAt = now + 300L;
            return;
        }
        int missingInvisibility = Math.max(0, 1 - this.countInventory(this::isInvisibilityPotion));
        if (missingInvisibility > 0) {
            this.prepareSupplySearch("invisibility");
            if (!this.takeFromStorage(handler, this::isInvisibilityPotion, missingInvisibility)) {
                this.tryNextStorageChest("\u0437\u0435\u043b\u044c\u0435 \u043d\u0435\u0432\u0438\u0434\u0438\u043c\u043e\u0441\u0442\u0438", true);
                return;
            }
            this.nextActionAt = now + 120L;
            return;
        }
        if (this.takeSpeed.isValue() && (missingSpeed = Math.max(0, 1 - this.countInventory(this::isSpeedPotion))) > 0) {
            this.prepareSupplySearch("speed");
            if (!this.takeFromStorage(handler, this::isSpeedPotion, missingSpeed)) {
                this.tryNextStorageChest("\u0437\u0435\u043b\u044c\u0435 \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u0438", true);
                return;
            }
            this.nextActionAt = now + 120L;
            return;
        }
        int missingFood = Math.max(0, 2 - this.countInventory(stack -> stack.method_31574(class_1802.field_8071)));
        if (missingFood > 0) {
            this.prepareSupplySearch("food");
            if (!this.takeFromStorage(handler, stack -> stack.method_31574(class_1802.field_8071), missingFood)) {
                this.tryNextStorageChest("\u0437\u043e\u043b\u043e\u0442\u044b\u0435 \u043c\u043e\u0440\u043a\u043e\u0432\u043a\u0438", true);
                return;
            }
            this.nextActionAt = now + 120L;
            return;
        }
        this.snapshotDepartureInventory();
        this.dungeonFloorY = Integer.MIN_VALUE;
        this.lootActionsThisRun = 0;
        this.homeRequired = true;
        this.homeCommandSent = false;
        this.homeCommandOrigin = null;
        this.homeCommandAt = 0L;
        this.homeTeleportConfirmed = false;
        this.supplyChest = this.storageChest;
        this.rejectedStorageChests.clear();
        this.supplySearchKey = null;
        AutoEnd.mc.field_1724.method_7346();
        this.storageChest = null;
        this.changePhase(Phase.SWITCH_END);
        this.nextActionAt = now + 500L;
    }

    private void reachEndOrigin(long now) {
        if (ServerUtil.getAnarchy() != this.getEndAnarchy()) {
            this.changePhase(Phase.SWITCH_END);
            return;
        }
        class_2338 current = AutoEnd.mc.field_1724.method_24515();
        this.ensureDungeonFloor();
        long horizontalDistanceSquared = (long)current.method_10263() * (long)current.method_10263() + (long)current.method_10260() * (long)current.method_10260();
        if (horizontalDistanceSquared > 9L) {
            this.pathNear(new class_2338(0, this.dungeonFloorY, 0), 2);
            this.nextActionAt = now + 300L;
            return;
        }
        this.cancelPathing();
        class_2338 potAbove = this.nearestBlock(state -> state.method_26204() instanceof class_8168, this.exhaustedPots, 1, 18);
        if (potAbove != null) {
            this.target = potAbove;
            this.emptyScans = 0;
            this.changePhase(Phase.BREAKING_POT);
            return;
        }
        this.descentStartY = current.method_10264();
        this.descentStartedAt = now;
        this.changePhase(Phase.DESCENDING);
    }

    private void descendAtOrigin(long now) {
        int dropped;
        if (ServerUtil.getAnarchy() != this.getEndAnarchy()) {
            this.stopDescentMovement();
            this.changePhase(Phase.SWITCH_END);
            return;
        }
        int n = dropped = this.descentStartY == Integer.MIN_VALUE ? 0 : this.descentStartY - AutoEnd.mc.field_1724.method_24515().method_10264();
        if (dropped >= 3 && AutoEnd.mc.field_1724.method_24828()) {
            this.stopDescentMovement();
            this.dungeonFloorY = AutoEnd.mc.field_1724.method_24515().method_10264();
            this.target = null;
            this.patrolTarget = null;
            this.emptyScans = 0;
            this.changePhase(Phase.SEARCHING);
            return;
        }
        if (now - this.descentStartedAt >= 10000L) {
            this.stopDescentMovement();
            this.descentStartY = AutoEnd.mc.field_1724.method_24515().method_10264();
            this.descentStartedAt = now;
            this.action("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043f\u0443\u0441\u0442\u0438\u0442\u044c\u0441\u044f \u0443 X=0, Z=0. \u041f\u043e\u0432\u0442\u043e\u0440\u044f\u044e \u043f\u0440\u044b\u0436\u043e\u043a.");
        }
        this.cancelPathing();
        class_2338 drop = this.nearestDropAtOrigin();
        class_243 destination = drop == null ? new class_243(0.5, AutoEnd.mc.field_1724.method_23318(), 0.5) : class_243.method_24953((class_2382)drop);
        class_243 delta = destination.method_1020(AutoEnd.mc.field_1724.method_73189());
        double horizontalDistanceSquared = delta.field_1352 * delta.field_1352 + delta.field_1350 * delta.field_1350;
        if (horizontalDistanceSquared > 0.04) {
            AutoEnd.mc.field_1724.method_36456((float)Math.toDegrees(Math.atan2(-delta.field_1352, delta.field_1350)));
            AutoEnd.mc.field_1690.field_1894.method_23481(true);
        } else {
            AutoEnd.mc.field_1690.field_1894.method_23481(false);
        }
        AutoEnd.mc.field_1690.field_1903.method_23481(AutoEnd.mc.field_1724.method_24828() && (drop == null || horizontalDistanceSquared > 4.0));
        this.nextActionAt = now + 50L;
    }

    private class_2338 nearestDropAtOrigin() {
        int y = AutoEnd.mc.field_1724.method_24515().method_10264();
        class_2338 result = null;
        double best = Double.MAX_VALUE;
        for (int x = -2; x <= 2; ++x) {
            for (int z = -2; z <= 2; ++z) {
                double distance;
                class_2338 candidate = new class_2338(x, y, z);
                if (!AutoEnd.mc.field_1687.method_8320(candidate).method_26215() || !AutoEnd.mc.field_1687.method_8320(candidate.method_10074()).method_26215() || !AutoEnd.mc.field_1687.method_8320(candidate.method_10087(2)).method_26215() || !((distance = AutoEnd.mc.field_1724.method_5707(class_243.method_24953((class_2382)candidate))) < best)) continue;
                best = distance;
                result = candidate;
            }
        }
        return result;
    }

    private void stopDescentMovement() {
        if (AutoEnd.mc.field_1690 == null) {
            return;
        }
        AutoEnd.mc.field_1690.field_1894.method_23481(false);
        AutoEnd.mc.field_1690.field_1903.method_23481(false);
    }

    private void prepareEndBuffs(long now) {
        if (ServerUtil.getAnarchy() != this.getEndAnarchy()) {
            this.changePhase(Phase.SWITCH_END);
            return;
        }
        this.cancelPathing();
        if (this.buffNeedsRefresh((class_6880<class_1291>)class_1294.field_5905)) {
            if (!this.beginBuffConsumption(BuffConsumeAction.INVISIBILITY, this::isInvisibilityPotion)) {
                this.returnToBaseForBuff(now, "\u0437\u0435\u043b\u044c\u0435 \u043d\u0435\u0432\u0438\u0434\u0438\u043c\u043e\u0441\u0442\u0438");
            }
            return;
        }
        if (this.takeSpeed.isValue() && this.buffNeedsRefresh((class_6880<class_1291>)class_1294.field_5904)) {
            if (!this.beginBuffConsumption(BuffConsumeAction.SPEED, this::isSpeedPotion)) {
                this.returnToBaseForBuff(now, "\u0437\u0435\u043b\u044c\u0435 \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u0438");
            }
            return;
        }
        if (AutoEnd.mc.field_1724.method_7344().method_7586() <= 17) {
            if (!this.beginBuffConsumption(BuffConsumeAction.FOOD, stack -> stack.method_31574(class_1802.field_8071))) {
                this.returnToBaseForBuff(now, "\u0437\u043e\u043b\u043e\u0442\u044b\u0435 \u043c\u043e\u0440\u043a\u043e\u0432\u043a\u0438");
            }
            return;
        }
        if (this.homeRequired) {
            this.homeCommandSent = false;
            this.changePhase(Phase.END_HOME);
        } else {
            Phase resume = this.isEndWorkPhase(this.resumeAfterBuffs) ? this.resumeAfterBuffs : Phase.SEARCHING;
            this.changePhase(resume);
        }
    }

    private void enterEndHome(long now) {
        if (ServerUtil.getAnarchy() != this.getEndAnarchy()) {
            this.changePhase(Phase.SWITCH_END);
            return;
        }
        if (this.needsBuffRefresh()) {
            this.changePhase(Phase.END_BUFFS);
            return;
        }
        if (!this.homeCommandSent) {
            this.action("\u0411\u0430\u0444\u044b \u0433\u043e\u0442\u043e\u0432\u044b. \u0422\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0438\u0440\u0443\u044e\u0441\u044c \u043d\u0430 \u0432\u0430\u0440\u043f " + this.getEndWarp() + ".");
            this.homeCommandOrigin = AutoEnd.mc.field_1724.method_24515().method_10062();
            this.homeCommandAt = now;
            this.homeTeleportConfirmed = false;
            this.sendCommand("warp " + this.getEndWarp());
            this.homeCommandSent = true;
            this.nextActionAt = now + 500L;
            return;
        }
        class_2338 current = AutoEnd.mc.field_1724.method_24515();
        if (!this.homeTeleportConfirmed) {
            if (now - this.homeCommandAt >= 12000L) {
                this.action("\u0422\u0435\u043b\u0435\u043f\u043e\u0440\u0442 \u043d\u0430 \u0432\u0430\u0440\u043f \u043d\u0435 \u043f\u0440\u043e\u0438\u0437\u043e\u0448\u0451\u043b. \u041f\u043e\u0432\u0442\u043e\u0440\u044f\u044e \u043a\u043e\u043c\u0430\u043d\u0434\u0443.");
                this.homeCommandOrigin = current.method_10062();
                this.homeCommandAt = now;
                this.homeTeleportConfirmed = false;
                this.sendCommand("warp " + this.getEndWarp());
            }
            this.nextActionAt = now + 500L;
            return;
        }
        this.homeRequired = false;
        this.homeCommandSent = false;
        this.homeCommandOrigin = null;
        this.homeTeleportConfirmed = false;
        this.dungeonFloorY = current.method_10264();
        this.target = null;
        this.patrolTarget = null;
        this.emptyScans = 0;
        this.resumeAfterOrigin = Phase.SEARCHING;
        this.changePhase(Phase.END_ORIGIN);
    }

    private boolean beginBuffConsumption(BuffConsumeAction action, Predicate<class_1799> predicate) {
        if (AutoEnd.mc.field_1755 != null || this.consumeAction != BuffConsumeAction.NONE) {
            return false;
        }
        class_1735 source = AutoEnd.mc.field_1724.field_7512.field_7761.stream().filter(slot -> slot.field_7871 instanceof class_1661).filter(slot -> slot.method_34266() >= 0 && slot.method_34266() < 36).filter(class_1735::method_7681).filter(slot -> predicate.test(slot.method_7677())).findFirst().orElse(null);
        if (source == null) {
            return false;
        }
        if (action == BuffConsumeAction.FOOD) {
            this.cancelPathing();
            this.stopMovementKeys();
        }
        this.originalBuffHotbar = AutoEnd.mc.field_1724.method_31548().method_67532();
        this.swappedBuffSlot = -1;
        if (source.method_34266() >= 0 && source.method_34266() < 9) {
            AutoEnd.mc.field_1724.method_31548().method_61496(source.method_34266());
            AutoEnd.mc.field_1724.field_3944.method_52787((class_2596)new class_2868(source.method_34266()));
        } else {
            this.swappedBuffSlot = source.field_7874;
            AutoEnd.mc.field_1761.method_2906(AutoEnd.mc.field_1724.field_7512.field_7763, source.field_7874, this.originalBuffHotbar, class_1713.field_7791, (class_1657)AutoEnd.mc.field_1724);
        }
        class_1799 held = AutoEnd.mc.field_1724.method_6047();
        if (!predicate.test(held)) {
            this.stopBuffConsumption();
            return false;
        }
        this.consumeAction = action;
        this.consumeInitialDuration = action.effect == null ? 0 : this.buffDuration(action.effect);
        this.consumeInitialStackCount = held.method_7947();
        this.consumeInitialHunger = AutoEnd.mc.field_1724.method_7344().method_7586();
        this.consumeStartedAt = System.currentTimeMillis();
        this.action(switch (action.ordinal()) {
            default -> throw new MatchException(null, null);
            case 1 -> "\u041f\u044c\u044e \u0437\u0435\u043b\u044c\u0435 \u043d\u0435\u0432\u0438\u0434\u0438\u043c\u043e\u0441\u0442\u0438.";
            case 2 -> "\u041f\u044c\u044e \u0437\u0435\u043b\u044c\u0435 \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u0438.";
            case 3 -> "\u041f\u0440\u043e\u0433\u043e\u043b\u043e\u0434\u0430\u043b\u0430\u0441\u044c. \u0415\u043c \u0437\u043e\u043b\u043e\u0442\u0443\u044e \u043c\u043e\u0440\u043a\u043e\u0432\u043a\u0443.";
            case 0 -> "";
        });
        AutoEnd.mc.field_1690.field_1904.method_23481(true);
        class_1269 result = AutoEnd.mc.field_1761.method_2919((class_1657)AutoEnd.mc.field_1724, class_1268.field_5808);
        if (result.method_23665() && !AutoEnd.mc.field_1724.method_6115()) {
            AutoEnd.mc.field_1724.method_6019(class_1268.field_5808);
        }
        return true;
    }

    private void handleBuffConsumption(long now) {
        class_1269 result;
        boolean itemConsumed;
        if (this.consumeAction == BuffConsumeAction.NONE || AutoEnd.mc.field_1724 == null) {
            return;
        }
        if (this.consumeAction == BuffConsumeAction.FOOD) {
            this.stopMovementKeys();
        }
        int duration = this.consumeAction.effect == null ? 0 : this.buffDuration(this.consumeAction.effect);
        class_1799 held = AutoEnd.mc.field_1724.method_6047();
        boolean durationRenewed = this.consumeAction != BuffConsumeAction.FOOD && duration > Math.max(200, this.consumeInitialDuration + 20);
        boolean hungerRestored = this.consumeAction == BuffConsumeAction.FOOD && AutoEnd.mc.field_1724.method_7344().method_7586() > this.consumeInitialHunger;
        boolean bl = itemConsumed = held.method_7947() < this.consumeInitialStackCount || !this.consumeAction.matches(this, held);
        if (durationRenewed || hungerRestored || itemConsumed && now - this.consumeStartedAt > 350L || now - this.consumeStartedAt >= 5000L) {
            this.stopBuffConsumption();
            this.nextActionAt = now + 500L;
            return;
        }
        AutoEnd.mc.field_1690.field_1904.method_23481(true);
        if (!AutoEnd.mc.field_1724.method_6115() && now - this.consumeStartedAt > 250L && (result = AutoEnd.mc.field_1761.method_2919((class_1657)AutoEnd.mc.field_1724, class_1268.field_5808)).method_23665() && !AutoEnd.mc.field_1724.method_6115()) {
            AutoEnd.mc.field_1724.method_6019(class_1268.field_5808);
        }
    }

    private void stopBuffConsumption() {
        if (AutoEnd.mc.field_1690 != null) {
            AutoEnd.mc.field_1690.field_1904.method_23481(false);
        }
        if (AutoEnd.mc.field_1724 == null) {
            this.consumeAction = BuffConsumeAction.NONE;
            return;
        }
        if (AutoEnd.mc.field_1724.method_6115() && AutoEnd.mc.field_1761 != null) {
            AutoEnd.mc.field_1761.method_2897((class_1657)AutoEnd.mc.field_1724);
        }
        if (this.swappedBuffSlot >= 0 && this.originalBuffHotbar >= 0 && AutoEnd.mc.field_1761 != null) {
            AutoEnd.mc.field_1761.method_2906(AutoEnd.mc.field_1724.field_7512.field_7763, this.swappedBuffSlot, this.originalBuffHotbar, class_1713.field_7791, (class_1657)AutoEnd.mc.field_1724);
        } else if (this.originalBuffHotbar >= 0 && this.originalBuffHotbar != AutoEnd.mc.field_1724.method_31548().method_67532()) {
            AutoEnd.mc.field_1724.method_31548().method_61496(this.originalBuffHotbar);
            AutoEnd.mc.field_1724.field_3944.method_52787((class_2596)new class_2868(this.originalBuffHotbar));
        }
        this.originalBuffHotbar = -1;
        this.swappedBuffSlot = -1;
        this.consumeAction = BuffConsumeAction.NONE;
    }

    private void stopMovementKeys() {
        if (AutoEnd.mc.field_1690 == null) {
            return;
        }
        AutoEnd.mc.field_1690.field_1894.method_23481(false);
        AutoEnd.mc.field_1690.field_1881.method_23481(false);
        AutoEnd.mc.field_1690.field_1913.method_23481(false);
        AutoEnd.mc.field_1690.field_1849.method_23481(false);
        AutoEnd.mc.field_1690.field_1903.method_23481(false);
        AutoEnd.mc.field_1690.field_1867.method_23481(false);
    }

    private void returnToBaseForBuff(long now, String missing) {
        this.notifyUser("\u00a7eAutoEnd: \u0437\u0430\u043a\u043e\u043d\u0447\u0438\u043b\u0441\u044f \u0440\u0430\u0441\u0445\u043e\u0434\u043d\u0438\u043a \u2014 " + missing + ", \u0432\u043e\u0437\u0432\u0440\u0430\u0449\u0430\u044e\u0441\u044c \u043d\u0430 \u0431\u0430\u0437\u0443");
        this.resumeAfterBuffs = Phase.SEARCHING;
        this.requestReturn(now);
    }

    private boolean needsBuffRefresh() {
        return this.buffNeedsRefresh((class_6880<class_1291>)class_1294.field_5905) || this.takeSpeed.isValue() && this.buffNeedsRefresh((class_6880<class_1291>)class_1294.field_5904);
    }

    private boolean buffNeedsRefresh(class_6880<class_1291> effect) {
        return this.buffDuration(effect) <= 200;
    }

    private int buffDuration(class_6880<class_1291> effect) {
        class_1293 instance = AutoEnd.mc.field_1724.method_6112(effect);
        return instance == null ? 0 : instance.method_5584();
    }

    private boolean isEndWorkPhase(Phase value) {
        return value == Phase.SEARCHING || value == Phase.BREAKING_POT || value == Phase.COLLECTING || value == Phase.BRUSHING || value == Phase.DESCENDING;
    }

    private boolean outsideDungeon() {
        double z;
        double radius = this.dungeonRadius.getInt();
        double x = AutoEnd.mc.field_1724.method_23317();
        return x * x + (z = AutoEnd.mc.field_1724.method_23321()) * z > radius * radius;
    }

    private void searchEnd(long now) {
        class_2338 sand;
        if (ServerUtil.getAnarchy() != this.getEndAnarchy()) {
            this.changePhase(Phase.SWITCH_END);
            return;
        }
        this.ensureDungeonFloor();
        if (this.combatActive(now) && this.evadeNearbyPlayer(now)) {
            return;
        }
        if (now < this.nextScanAt) {
            return;
        }
        this.nextScanAt = now + 650L;
        boolean full = this.inventoryFull();
        if (!full && this.acquiredLootCount() >= 5) {
            this.requestReturn(now);
            return;
        }
        if (!full) {
            this.target = this.nearestBlock(state -> state.method_26204() instanceof class_8168, this.exhaustedPots);
        }
        if (this.target != null) {
            this.emptyScans = 0;
            this.changePhase(Phase.BREAKING_POT);
            return;
        }
        if (this.hasBrush() && (sand = this.nearestBlock(state -> state.method_27852(class_2246.field_42728), this.exhaustedSand, -18, 3)) != null) {
            this.target = sand;
            this.changePhase(Phase.BRUSHING);
            return;
        }
        if (full) {
            if (this.hasRunLoot()) {
                this.requestReturn(now);
            } else {
                this.notifyUser("\u00a7cAutoEnd: \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044c \u0431\u044b\u043b \u0437\u0430\u043f\u043e\u043b\u043d\u0435\u043d \u0435\u0449\u0451 \u0434\u043e \u0441\u0431\u043e\u0440\u0430 \u0434\u043e\u0431\u044b\u0447\u0438");
                this.setState(false);
            }
            return;
        }
        ++this.emptyScans;
        if (this.emptyScans >= 8 && this.acquiredLootCount() >= 5) {
            this.requestReturn(now);
        } else {
            this.patrol(now, false);
            this.nextScanAt = now + 900L;
        }
    }

    private void breakPot(long now) {
        if (this.target == null || !(AutoEnd.mc.field_1687.method_8320(this.target).method_26204() instanceof class_8168)) {
            if (this.target != null) {
                this.exhaustedPots.add(this.target.method_10062());
            }
            this.target = null;
            ++this.lootActionsThisRun;
            this.combatUntil = Math.max(this.combatUntil, now + 30000L);
            this.action("\u0412\u0430\u0437\u0430 \u0440\u0430\u0437\u0431\u0438\u0442\u0430. \u0417\u0430\u043f\u0443\u0449\u0435\u043d \u041a\u0422 \u043d\u0430 30 \u0441\u0435\u043a\u0443\u043d\u0434.");
            this.changePhase(Phase.COLLECTING);
            return;
        }
        if (this.combatActive(now) && this.evadeNearbyPlayer(now)) {
            return;
        }
        if (!AutoEnd.mc.field_1724.method_24515().method_19771((class_2382)this.target, 4.4)) {
            this.pathNear(this.target, 3);
            this.nextActionAt = now + 220L;
            return;
        }
        class_3965 potHit = this.raycastPot(this.target);
        if (potHit == null) {
            if (now - this.phaseStartedAt >= 8000L) {
                this.exhaustedPots.add(this.target.method_10062());
                this.target = null;
                this.changePhase(Phase.SEARCHING);
            } else {
                this.pathNear(this.target, 1);
                this.nextActionAt = now + 220L;
            }
            return;
        }
        this.cancelPathing();
        this.aimAt(potHit.method_17784());
        AutoEnd.mc.field_1761.method_2902(this.target, potHit.method_17780());
        AutoEnd.mc.field_1724.method_6104(class_1268.field_5808);
        this.nextActionAt = now + 70L;
    }

    private class_3965 raycastPot(class_2338 pot) {
        class_3965 hit = AutoEnd.mc.field_1687.method_17742(new class_3959(AutoEnd.mc.field_1724.method_33571(), class_243.method_24953((class_2382)pot), class_3959.class_3960.field_17558, class_3959.class_242.field_1348, (class_1297)AutoEnd.mc.field_1724));
        return hit.method_17783() == class_239.class_240.field_1332 && hit.method_17777().equals((Object)pot) ? hit : null;
    }

    private void collectDrops(long now) {
        class_1542 drop = AutoEnd.mc.field_1687.method_8390(class_1542.class, AutoEnd.mc.field_1724.method_5829().method_1014(14.0), entity -> !entity.method_6983().method_7960() && Math.abs(entity.method_31478() - this.dungeonFloor()) <= 3).stream().min(Comparator.comparingDouble(arg_0 -> ((class_746)AutoEnd.mc.field_1724).method_5858(arg_0))).orElse(null);
        if (drop != null && now - this.phaseStartedAt < 8000L) {
            this.pathNear(drop.method_24515(), 1);
            this.nextActionAt = now + 180L;
            return;
        }
        if (now - this.phaseStartedAt < 2500L) {
            return;
        }
        this.changePhase(Phase.SEARCHING);
    }

    private void brushSand(long now) {
        if (this.target == null || !AutoEnd.mc.field_1687.method_8320(this.target).method_27852(class_2246.field_42728)) {
            if (this.target != null) {
                this.exhaustedSand.add(this.target.method_10062());
            }
            this.target = null;
            ++this.lootActionsThisRun;
            this.releaseBrush();
            this.changePhase(Phase.COLLECTING);
            return;
        }
        if (!this.hasBrush()) {
            this.releaseBrush();
            this.changePhase(Phase.SEARCHING);
            return;
        }
        if (this.combatActive(now) && this.evadeNearbyPlayer(now)) {
            this.releaseBrush();
            return;
        }
        if (!AutoEnd.mc.field_1724.method_24515().method_19771((class_2382)this.target, 4.0)) {
            this.releaseBrush();
            this.pathNear(this.target, 2);
            this.nextActionAt = now + 180L;
            return;
        }
        if (!this.equipBrush()) {
            this.changePhase(Phase.SEARCHING);
            return;
        }
        this.cancelPathing();
        this.aimAt(class_243.method_24953((class_2382)this.target));
        AutoEnd.mc.field_1690.field_1904.method_23481(true);
        if (!AutoEnd.mc.field_1724.method_6115()) {
            class_243 center = class_243.method_24953((class_2382)this.target);
            class_243 towardPlayer = AutoEnd.mc.field_1724.method_33571().method_1020(center);
            class_2350 face = class_2350.method_10142((double)towardPlayer.field_1352, (double)towardPlayer.field_1351, (double)towardPlayer.field_1350);
            class_243 hitPos = center.method_1019(class_243.method_24954((class_2382)face.method_62675()).method_1021(0.5));
            class_1269 result = AutoEnd.mc.field_1761.method_2896(AutoEnd.mc.field_1724, class_1268.field_5808, new class_3965(hitPos, face, this.target, false));
            if (result.method_23665() && !AutoEnd.mc.field_1724.method_6115()) {
                AutoEnd.mc.field_1724.method_6019(class_1268.field_5808);
            }
        }
        this.nextActionAt = now + 50L;
    }

    private void requestReturn(long now) {
        this.releaseBrush();
        this.cancelPathing();
        if (this.combatActive(now)) {
            this.changePhase(Phase.COMBAT_WAIT);
            this.patrol(now, true);
            return;
        }
        this.changePhase(Phase.SWITCH_BASE);
        this.nextActionAt = now + 150L;
    }

    private void waitCombat(long now) {
        if (!this.combatActive(now)) {
            this.cancelPathing();
            this.changePhase(Phase.SWITCH_BASE);
            return;
        }
        if (!this.evadeNearbyPlayer(now)) {
            this.patrol(now, true);
        }
    }

    private boolean evadeNearbyPlayer(long now) {
        class_1657 nearest = AutoEnd.mc.field_1687.method_18456().stream().filter(player -> player != AutoEnd.mc.field_1724 && player.method_5805() && !player.method_7325()).min(Comparator.comparingDouble(arg_0 -> ((class_746)AutoEnd.mc.field_1724).method_5858(arg_0))).orElse(null);
        if (nearest == null) {
            return false;
        }
        class_243 away = AutoEnd.mc.field_1724.method_73189().method_1020(nearest.method_73189());
        if (away.method_37268() < 0.01) {
            away = new class_243(1.0, 0.0, 0.0);
        }
        away = away.method_1029().method_1021(42.0);
        this.patrolTarget = new class_2338(class_3532.method_15357((double)(AutoEnd.mc.field_1724.method_23317() + away.field_1352)), this.dungeonFloor(), class_3532.method_15357((double)(AutoEnd.mc.field_1724.method_23321() + away.field_1350)));
        this.patrolExpiresAt = now + 4000L;
        this.pathNear(this.patrolTarget, 2);
        this.nextActionAt = now + 300L;
        return true;
    }

    private void patrol(long now, boolean combat) {
        if (this.patrolTarget == null || now >= this.patrolExpiresAt || AutoEnd.mc.field_1724.method_24515().method_19771((class_2382)this.patrolTarget, 5.0)) {
            double angle = ((double)AutoEnd.mc.field_1687.method_75260() * 0.73 + (double)this.emptyScans * 1.91) % (Math.PI * 2);
            double distance = combat ? 34.0 : 24.0;
            this.patrolTarget = new class_2338(class_3532.method_15357((double)(AutoEnd.mc.field_1724.method_23317() + Math.cos(angle) * distance)), this.dungeonFloor(), class_3532.method_15357((double)(AutoEnd.mc.field_1724.method_23321() + Math.sin(angle) * distance)));
            this.patrolExpiresAt = now + 7000L;
        }
        this.pathNear(this.patrolTarget, 2);
        this.nextActionAt = now + 350L;
    }

    private void pathNear(class_2338 pos, int radius) {
        if (pos == null) {
            return;
        }
        long now = System.currentTimeMillis();
        boolean sameGoal = pos.equals((Object)this.lastPathTarget) && radius == this.lastPathRadius;
        class_243 playerPos = AutoEnd.mc.field_1724.method_73189();
        if (sameGoal) {
            if (AutoEnd.mc.field_1724.method_24515().method_19771((class_2382)pos, (double)radius + 0.75)) {
                return;
            }
            if (this.lastPathPlayerPos == null || playerPos.method_1025(this.lastPathPlayerPos) >= 0.0625) {
                this.lastPathPlayerPos = playerPos;
                this.lastPathProgressAt = now;
                return;
            }
            if (now - Math.max(this.lastPathIssuedAt, this.lastPathProgressAt) < 1500L) {
                return;
            }
        }
        if (BaritoneCompat.goNear(this.baritone, pos, radius)) {
            this.lastPathTarget = pos.method_10062();
            this.lastPathRadius = radius;
            this.lastPathIssuedAt = now;
            this.lastPathPlayerPos = playerPos;
            this.lastPathProgressAt = now;
        }
    }

    private void cancelPathing() {
        BaritoneCompat.cancelPathing(this.baritone);
        this.lastPathTarget = null;
        this.lastPathRadius = -1;
        this.lastPathIssuedAt = 0L;
        this.lastPathPlayerPos = null;
        this.lastPathProgressAt = 0L;
    }

    private class_2338 nearestBlock(Predicate<class_2680> predicate, Set<class_2338> exhausted) {
        return this.nearestBlock(predicate, exhausted, -3, 3);
    }

    private class_2338 nearestBlock(Predicate<class_2680> predicate, Set<class_2338> exhausted, int minYOffset, int maxYOffset) {
        class_2338 origin = AutoEnd.mc.field_1724.method_24515();
        int floorY = this.dungeonFloor();
        class_2338 min = new class_2338(origin.method_10263() - 40, floorY + minYOffset, origin.method_10260() - 40);
        class_2338 max = new class_2338(origin.method_10263() + 40, floorY + maxYOffset, origin.method_10260() + 40);
        class_2338 result = null;
        double best = Double.MAX_VALUE;
        for (class_2338 candidate : class_2338.method_10097((class_2338)min, (class_2338)max)) {
            double distance;
            if (exhausted.contains(candidate) || !predicate.test(AutoEnd.mc.field_1687.method_8320(candidate)) || !((distance = AutoEnd.mc.field_1724.method_5707(class_243.method_24953((class_2382)candidate))) < best)) continue;
            best = distance;
            result = candidate.method_10062();
        }
        return result;
    }

    private void ensureDungeonFloor() {
        if (this.dungeonFloorY == Integer.MIN_VALUE) {
            this.dungeonFloorY = AutoEnd.mc.field_1724.method_24515().method_10264();
        }
    }

    private int dungeonFloor() {
        this.ensureDungeonFloor();
        return this.dungeonFloorY;
    }

    private class_2338 nearestChest(int radius) {
        class_2338 origin = AutoEnd.mc.field_1724.method_24515();
        class_2338 result = null;
        double best = Double.MAX_VALUE;
        for (class_2338 pos : class_2338.method_10097((class_2338)origin.method_10069(-radius, -8, -radius), (class_2338)origin.method_10069(radius, 8, radius))) {
            double distance;
            if (this.rejectedStorageChests.contains(pos) || !(AutoEnd.mc.field_1687.method_8321(pos) instanceof class_2595) || !((distance = AutoEnd.mc.field_1724.method_5707(class_243.method_24953((class_2382)pos))) < best)) continue;
            best = distance;
            result = pos.method_10062();
        }
        return result;
    }

    private boolean takeFromStorage(class_1707 handler, Predicate<class_1799> predicate, int amount) {
        class_1735 source = handler.field_7761.stream().filter(slot -> !(slot.field_7871 instanceof class_1661)).filter(class_1735::method_7681).filter(slot -> predicate.test(slot.method_7677())).findFirst().orElse(null);
        class_1735 destination = handler.field_7761.stream().filter(slot -> slot.field_7871 instanceof class_1661).filter(slot -> slot.method_34266() >= 0 && slot.method_34266() < 36).filter(slot -> !slot.method_7681() || class_1799.method_31577((class_1799)slot.method_7677(), (class_1799)(source == null ? class_1799.field_8037 : source.method_7677())) && slot.method_7677().method_7947() < slot.method_7677().method_7914()).findFirst().orElse(null);
        if (source == null || destination == null) {
            return false;
        }
        this.supplyChest = this.storageChest;
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

    private boolean tryNextStorageChest(String missing, boolean required) {
        class_2338 current = this.storageChest == null ? null : this.storageChest.method_10062();
        this.rejectChest(current);
        class_2338 next = this.nearestChest(28);
        if (next == null) {
            if (current != null && !required) {
                this.rejectedStorageChests.clear();
            }
            if (required) {
                this.stopForMissing(missing);
            }
            return false;
        }
        this.recoverPendingTransfer();
        AutoEnd.mc.field_1724.method_7346();
        this.storageChest = next;
        this.changePhase(Phase.OPEN_BASE);
        this.nextActionAt = System.currentTimeMillis() + 300L;
        return true;
    }

    private void rejectChest(class_2338 pos) {
        if (pos == null) {
            return;
        }
        this.rejectedStorageChests.add(pos.method_10062());
        for (class_2350 direction : class_2350.class_2353.field_11062) {
            class_2338 neighbor = pos.method_10093(direction);
            if (!(AutoEnd.mc.field_1687.method_8321(neighbor) instanceof class_2595)) continue;
            this.rejectedStorageChests.add(neighbor.method_10062());
        }
    }

    private Transfer findExcessLoot(class_1707 handler) {
        for (class_1735 source : handler.field_7761) {
            class_1735 destination;
            int baseline;
            int current;
            int excess;
            if (!(source.field_7871 instanceof class_1661) || !source.method_7681() || source.method_7677().method_31574(class_1802.field_42716) || (excess = (current = this.countMainInventory(stack -> class_1799.method_31577((class_1799)stack, (class_1799)source.method_7677()))) - (baseline = this.departureInventory.stream().filter(stack -> class_1799.method_31577((class_1799)stack, (class_1799)source.method_7677())).mapToInt(class_1799::method_7947).sum())) <= 0 || (destination = (class_1735)handler.field_7761.stream().filter(slot -> !(slot.field_7871 instanceof class_1661)).filter(slot -> !slot.method_7681() || class_1799.method_31577((class_1799)slot.method_7677(), (class_1799)source.method_7677()) && slot.method_7677().method_7947() < slot.method_7677().method_7914()).findFirst().orElse(null)) == null) continue;
            int room = destination.method_7681() ? destination.method_7677().method_7914() - destination.method_7677().method_7947() : source.method_7677().method_7914();
            return new Transfer(source, destination, Math.min(Math.min(excess, source.method_7677().method_7947()), room));
        }
        return null;
    }

    private boolean hasExcessLoot() {
        for (int index = 0; index < Math.min(36, AutoEnd.mc.field_1724.method_31548().method_5439()); ++index) {
            int baseline;
            int current;
            class_1799 currentStack = AutoEnd.mc.field_1724.method_31548().method_5438(index);
            if (currentStack.method_7960() || currentStack.method_31574(class_1802.field_42716) || (current = this.countMainInventory(stack -> class_1799.method_31577((class_1799)stack, (class_1799)currentStack))) <= (baseline = this.departureInventory.stream().filter(stack -> class_1799.method_31577((class_1799)stack, (class_1799)currentStack)).mapToInt(class_1799::method_7947).sum())) continue;
            return true;
        }
        return false;
    }

    private boolean hasRunLoot() {
        return this.lootActionsThisRun > 0 || this.hasExcessLoot();
    }

    private int acquiredLootCount() {
        int total = 0;
        int size = Math.min(36, AutoEnd.mc.field_1724.method_31548().method_5439());
        for (int index = 0; index < size; ++index) {
            class_1799 type = AutoEnd.mc.field_1724.method_31548().method_5438(index);
            if (type.method_7960() || type.method_31574(class_1802.field_42716)) continue;
            boolean alreadyCounted = false;
            for (int previous = 0; previous < index; ++previous) {
                class_1799 previousStack = AutoEnd.mc.field_1724.method_31548().method_5438(previous);
                if (previousStack.method_7960() || !class_1799.method_31577((class_1799)previousStack, (class_1799)type)) continue;
                alreadyCounted = true;
                break;
            }
            if (alreadyCounted) continue;
            int current = this.countMainInventory(stack -> class_1799.method_31577((class_1799)stack, (class_1799)type));
            int baseline = this.departureInventory.stream().filter(stack -> class_1799.method_31577((class_1799)stack, (class_1799)type)).mapToInt(class_1799::method_7947).sum();
            total += Math.max(0, current - baseline);
        }
        return total;
    }

    private void startTransfer(class_1735 source, class_1735 destination, int amount) {
        if (amount <= 0 || source == null || destination == null || !AutoEnd.mc.field_1724.field_7512.method_34255().method_7960()) {
            return;
        }
        int syncId = AutoEnd.mc.field_1724.field_7512.field_7763;
        if (amount >= source.method_7677().method_7947()) {
            AutoEnd.mc.field_1761.method_2906(syncId, source.field_7874, 0, class_1713.field_7794, (class_1657)AutoEnd.mc.field_1724);
            return;
        }
        this.pendingTransfer = new PendingTransfer(syncId, source.field_7874, destination.field_7874, amount);
    }

    private void tickPendingTransfer(long now) {
        PendingTransfer transfer = this.pendingTransfer;
        if (transfer == null || AutoEnd.mc.field_1724.field_7512.field_7763 != transfer.syncId) {
            this.pendingTransfer = null;
            return;
        }
        switch (transfer.stage.ordinal()) {
            case 0: {
                if (AutoEnd.mc.field_1724.field_7512.method_34255().method_7960()) {
                    AutoEnd.mc.field_1761.method_2906(transfer.syncId, transfer.sourceSlot, 0, class_1713.field_7790, (class_1657)AutoEnd.mc.field_1724);
                }
                transfer.stage = TransferStage.PLACE;
                break;
            }
            case 1: {
                if (AutoEnd.mc.field_1724.field_7512.method_34255().method_7960()) {
                    this.pendingTransfer = null;
                    return;
                }
                if (transfer.remaining > 0) {
                    AutoEnd.mc.field_1761.method_2906(transfer.syncId, transfer.destinationSlot, 1, class_1713.field_7790, (class_1657)AutoEnd.mc.field_1724);
                    --transfer.remaining;
                }
                if (transfer.remaining > 0) break;
                transfer.stage = TransferStage.RETURN;
                break;
            }
            case 2: {
                if (!AutoEnd.mc.field_1724.field_7512.method_34255().method_7960()) {
                    AutoEnd.mc.field_1761.method_2906(transfer.syncId, transfer.sourceSlot, 0, class_1713.field_7790, (class_1657)AutoEnd.mc.field_1724);
                }
                if (!AutoEnd.mc.field_1724.field_7512.method_34255().method_7960()) break;
                this.pendingTransfer = null;
            }
        }
        this.nextActionAt = now + 120L;
    }

    private void recoverPendingTransfer() {
        if (this.pendingTransfer == null || AutoEnd.mc.field_1724 == null || AutoEnd.mc.field_1761 == null) {
            return;
        }
        if (AutoEnd.mc.field_1724.field_7512.field_7763 == this.pendingTransfer.syncId && !AutoEnd.mc.field_1724.field_7512.method_34255().method_7960()) {
            AutoEnd.mc.field_1761.method_2906(this.pendingTransfer.syncId, this.pendingTransfer.sourceSlot, 0, class_1713.field_7790, (class_1657)AutoEnd.mc.field_1724);
        }
        this.pendingTransfer = null;
    }

    private void settleUnexpectedCursor(class_1707 handler, long now) {
        class_1799 cursor = AutoEnd.mc.field_1724.field_7512.method_34255();
        class_1735 destination = handler.field_7761.stream().filter(slot -> !(slot.field_7871 instanceof class_1661)).filter(slot -> !slot.method_7681() || class_1799.method_31577((class_1799)slot.method_7677(), (class_1799)cursor) && slot.method_7677().method_7947() < slot.method_7677().method_7914()).findFirst().orElseGet(() -> handler.field_7761.stream().filter(slot -> slot.field_7871 instanceof class_1661).filter(slot -> slot.method_34266() >= 0 && slot.method_34266() < 36).filter(slot -> !slot.method_7681() || class_1799.method_31577((class_1799)slot.method_7677(), (class_1799)cursor) && slot.method_7677().method_7947() < slot.method_7677().method_7914()).findFirst().orElse(null));
        if (destination != null) {
            AutoEnd.mc.field_1761.method_2906(handler.field_7763, destination.field_7874, 0, class_1713.field_7790, (class_1657)AutoEnd.mc.field_1724);
        }
        this.nextActionAt = now + 120L;
    }

    private boolean equipBrush() {
        if (AutoEnd.mc.field_1724.method_6047().method_31574(class_1802.field_42716)) {
            return true;
        }
        class_1735 brush = PlayerInventoryUtil.getSlot(class_1802.field_42716, slot -> slot.field_7871 instanceof class_1661);
        if (brush == null) {
            return false;
        }
        this.previousSelectedSlot = AutoEnd.mc.field_1724.method_31548().method_67532();
        if (brush.method_34266() >= 0 && brush.method_34266() < 9) {
            AutoEnd.mc.field_1724.method_31548().method_61496(brush.method_34266());
            AutoEnd.mc.field_1724.field_3944.method_52787((class_2596)new class_2868(brush.method_34266()));
        } else {
            this.swappedBrushSlot = brush.field_7874;
            PlayerInventoryUtil.clickSlot(brush.field_7874, this.previousSelectedSlot, class_1713.field_7791, false);
        }
        return AutoEnd.mc.field_1724.method_6047().method_31574(class_1802.field_42716);
    }

    private boolean stowHeldItems(boolean stowMainHand, long now) {
        class_1735 destination;
        if (AutoEnd.mc.field_1724 == null || AutoEnd.mc.field_1761 == null || !AutoEnd.mc.field_1724.field_7512.method_34255().method_7960()) {
            return false;
        }
        if (!AutoEnd.mc.field_1724.method_6079().method_7960() && (destination = this.emptyPlayerSlot(-1)) != null) {
            AutoEnd.mc.field_1761.method_2906(AutoEnd.mc.field_1724.field_7512.field_7763, destination.field_7874, 40, class_1713.field_7791, (class_1657)AutoEnd.mc.field_1724);
            this.nextActionAt = now + 120L;
            return true;
        }
        if (!stowMainHand || AutoEnd.mc.field_1724.method_6047().method_7960()) {
            return false;
        }
        int selectedSlot = AutoEnd.mc.field_1724.method_31548().method_67532();
        class_1735 destination2 = this.emptyPlayerSlot(selectedSlot);
        if (destination2 == null) {
            return false;
        }
        AutoEnd.mc.field_1761.method_2906(AutoEnd.mc.field_1724.field_7512.field_7763, destination2.field_7874, selectedSlot, class_1713.field_7791, (class_1657)AutoEnd.mc.field_1724);
        this.nextActionAt = now + 120L;
        return true;
    }

    private class_1735 emptyPlayerSlot(int excludedInventoryIndex) {
        return AutoEnd.mc.field_1724.field_7512.field_7761.stream().filter(slot -> slot.field_7871 instanceof class_1661).filter(slot -> slot.method_34266() >= 9 && slot.method_34266() < 36).filter(slot -> slot.method_34266() != excludedInventoryIndex && !slot.method_7681()).findFirst().orElseGet(() -> AutoEnd.mc.field_1724.field_7512.field_7761.stream().filter(slot -> slot.field_7871 instanceof class_1661).filter(slot -> slot.method_34266() >= 0 && slot.method_34266() < 9).filter(slot -> slot.method_34266() != excludedInventoryIndex && !slot.method_7681()).findFirst().orElse(null));
    }

    private void releaseBrush() {
        if (AutoEnd.mc.field_1724 == null || AutoEnd.mc.field_1761 == null) {
            return;
        }
        AutoEnd.mc.field_1690.field_1904.method_23481(false);
        if (AutoEnd.mc.field_1724.method_6115()) {
            AutoEnd.mc.field_1761.method_2897((class_1657)AutoEnd.mc.field_1724);
        }
        if (this.swappedBrushSlot >= 0 && this.previousSelectedSlot >= 0) {
            PlayerInventoryUtil.clickSlot(this.swappedBrushSlot, this.previousSelectedSlot, class_1713.field_7791, false);
        } else if (this.previousSelectedSlot >= 0 && this.previousSelectedSlot != AutoEnd.mc.field_1724.method_31548().method_67532()) {
            AutoEnd.mc.field_1724.method_31548().method_61496(this.previousSelectedSlot);
            AutoEnd.mc.field_1724.field_3944.method_52787((class_2596)new class_2868(this.previousSelectedSlot));
        }
        this.swappedBrushSlot = -1;
        this.previousSelectedSlot = -1;
    }

    private void aimAt(class_243 point) {
        class_243 delta = point.method_1020(AutoEnd.mc.field_1724.method_33571());
        double horizontal = Math.sqrt(delta.field_1352 * delta.field_1352 + delta.field_1350 * delta.field_1350);
        AutoEnd.mc.field_1724.method_36456((float)(Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0));
        AutoEnd.mc.field_1724.method_36457(class_3532.method_15363((float)((float)(-Math.toDegrees(Math.atan2(delta.field_1351, horizontal)))), (float)-90.0f, (float)90.0f));
    }

    private boolean combatActive(long now) {
        return now < this.combatUntil || ServerUtil.isPvp();
    }

    private boolean inventoryFull() {
        return AutoEnd.mc.field_1724.method_31548().method_7376() == -1;
    }

    private boolean hasBrush() {
        return this.countInventory(stack -> stack.method_31574(class_1802.field_42716)) > 0;
    }

    private boolean isInvisibilityPotion(class_1799 stack) {
        return this.potionHas(stack, (class_6880<class_1291>)class_1294.field_5905);
    }

    private boolean isSpeedPotion(class_1799 stack) {
        return this.potionHas(stack, (class_6880<class_1291>)class_1294.field_5904);
    }

    private boolean potionHas(class_1799 stack, class_6880<class_1291> effect) {
        if (stack == null || stack.method_7960() || !stack.method_31574(class_1802.field_8574)) {
            return false;
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

    private int countInventory(Predicate<class_1799> predicate) {
        int count = 0;
        for (int index = 0; index < AutoEnd.mc.field_1724.method_31548().method_5439(); ++index) {
            class_1799 stack = AutoEnd.mc.field_1724.method_31548().method_5438(index);
            if (stack.method_7960() || !predicate.test(stack)) continue;
            count += stack.method_7947();
        }
        return count;
    }

    private int countMainInventory(Predicate<class_1799> predicate) {
        int count = 0;
        for (int index = 0; index < Math.min(36, AutoEnd.mc.field_1724.method_31548().method_5439()); ++index) {
            class_1799 stack = AutoEnd.mc.field_1724.method_31548().method_5438(index);
            if (stack.method_7960() || !predicate.test(stack)) continue;
            count += stack.method_7947();
        }
        return count;
    }

    private void snapshotDepartureInventory() {
        this.departureInventory.clear();
        for (int index = 0; index < Math.min(36, AutoEnd.mc.field_1724.method_31548().method_5439()); ++index) {
            class_1799 stack = AutoEnd.mc.field_1724.method_31548().method_5438(index);
            if (stack.method_7960()) continue;
            this.departureInventory.add(stack.method_7972());
        }
    }

    private void stopForMissing(String name) {
        this.notifyUser("\u00a7cAutoEnd: \u0432 \u0441\u0443\u043d\u0434\u0443\u043a\u0435 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u043e \u2014 " + name);
        AutoEnd.mc.field_1724.method_7346();
        this.setState(false);
    }

    private void sendCommand(String command) {
        if (AutoEnd.mc.field_1724 != null && AutoEnd.mc.field_1724.field_3944 != null) {
            AutoEnd.mc.field_1724.field_3944.method_45730(command);
        }
    }

    private void changePhase(Phase next) {
        boolean changed;
        boolean bl = changed = this.phase != next;
        if (this.phase == Phase.DESCENDING && next != Phase.DESCENDING) {
            this.stopDescentMovement();
        }
        this.phase = next;
        this.phaseStartedAt = System.currentTimeMillis();
        if (changed) {
            this.announcePhase(next);
        }
    }

    private void notifyUser(String message) {
        Notifications.getInstance().addList((class_2561)class_2561.method_43470((String)message), 3000L);
    }

    private void announcePhase(Phase value) {
        switch (value.ordinal()) {
            case 0: {
                this.action("\u041f\u0435\u0440\u0435\u0445\u043e\u0436\u0443 \u043d\u0430 \u043e\u0441\u043d\u043e\u0432\u043d\u0443\u044e \u0430\u043d\u0430\u0440\u0445\u0438\u044e.");
                break;
            }
            case 1: {
                this.action("\u0418\u0449\u0443 \u043f\u043e\u0434\u0445\u043e\u0434\u044f\u0449\u0438\u0439 \u0441\u0443\u043d\u0434\u0443\u043a \u043d\u0430 \u0431\u0430\u0437\u0435.");
                break;
            }
            case 2: {
                this.action("\u041f\u043e\u043f\u043e\u043b\u043d\u044f\u044e \u0440\u0430\u0441\u0445\u043e\u0434\u043d\u0438\u043a\u0438.");
                break;
            }
            case 3: {
                this.action("\u0421\u043a\u043b\u0430\u0434\u044b\u0432\u0430\u044e \u0434\u043e\u0431\u044b\u0447\u0443 \u0432 \u0441\u0443\u043d\u0434\u0443\u043a.");
                break;
            }
            case 4: {
                this.action("\u0412\u043e\u0437\u0432\u0440\u0430\u0449\u0430\u044e\u0441\u044c \u043d\u0430 \u042d\u043d\u0434-\u0430\u043d\u0430\u0440\u0445\u0438\u044e.");
                break;
            }
            case 5: {
                this.action("\u0418\u0434\u0443 \u043a X=0, Z=0 \u0438 \u043f\u0440\u043e\u0432\u0435\u0440\u044f\u044e \u0432\u0430\u0437\u044b \u0441\u0432\u0435\u0440\u0445\u0443.");
                break;
            }
            case 6: {
                this.action("\u041f\u0440\u043e\u0432\u0435\u0440\u044f\u044e \u0434\u043b\u0438\u0442\u0435\u043b\u044c\u043d\u043e\u0441\u0442\u044c \u0431\u0430\u0444\u043e\u0432.");
                break;
            }
            case 7: {
                this.action("\u041f\u0435\u0440\u0435\u0445\u043e\u0436\u0443 \u043d\u0430 \u043b\u0438\u0447\u043d\u044b\u0439 \u0432\u0430\u0440\u043f \u0432 \u042d\u043d\u0434\u0435.");
                break;
            }
            case 8: {
                this.action("\u0421\u0432\u0435\u0440\u0445\u0443 \u0432\u0430\u0437 \u043d\u0435\u0442. \u041f\u0440\u044b\u0433\u0430\u044e \u0432\u043d\u0438\u0437 \u0443 X=0, Z=0.");
                break;
            }
            case 9: {
                this.action("\u0418\u0449\u0443 \u0432\u0430\u0437\u044b \u0438 \u043f\u043e\u0434\u043e\u0437\u0440\u0438\u0442\u0435\u043b\u044c\u043d\u044b\u0439 \u043f\u0435\u0441\u043e\u043a.");
                break;
            }
            case 10: {
                this.action("\u041d\u0430\u0439\u0434\u0435\u043d\u0430 \u0432\u0430\u0437\u0430. \u0418\u0434\u0443 \u0437\u0430 \u0434\u043e\u0431\u044b\u0447\u0435\u0439.");
                break;
            }
            case 11: {
                this.action("\u041f\u043e\u0434\u0431\u0438\u0440\u0430\u044e \u0432\u044b\u043f\u0430\u0432\u0448\u0438\u0435 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b.");
                break;
            }
            case 12: {
                this.action("\u041d\u0430\u0439\u0434\u0435\u043d \u043f\u043e\u0434\u043e\u0437\u0440\u0438\u0442\u0435\u043b\u044c\u043d\u044b\u0439 \u043f\u0435\u0441\u043e\u043a. \u0418\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u044e \u043a\u0438\u0441\u0442\u043e\u0447\u043a\u0443.");
                break;
            }
            case 13: {
                this.action("\u0414\u0435\u0439\u0441\u0442\u0432\u0443\u0435\u0442 \u041a\u0422. \u0423\u0445\u043e\u0436\u0443 \u043f\u043e\u0434\u0430\u043b\u044c\u0448\u0435 \u043e\u0442 \u0438\u0433\u0440\u043e\u043a\u043e\u0432.");
            }
        }
    }

    private void action(String message) {
        if (this.chatActions.isValue() && AutoEnd.mc.field_1705 != null) {
            AutoEnd.mc.field_1705.method_1743().method_1812(QuickLogger.clientMessage((class_2561)class_2561.method_43470((String)message).method_27692(class_124.field_1080)));
        }
    }

    private void restoreBaritoneSettings() {
        if (this.previousAllowBreak != null) {
            BaritoneCompat.setBooleanSetting("allowBreak", this.previousAllowBreak);
        }
        if (this.previousAllowPlace != null) {
            BaritoneCompat.setBooleanSetting("allowPlace", this.previousAllowPlace);
        }
        if (this.previousAllowInventory != null) {
            BaritoneCompat.setBooleanSetting("allowInventory", this.previousAllowInventory);
        }
        if (this.previousAllowSprint != null) {
            BaritoneCompat.setBooleanSetting("allowSprint", this.previousAllowSprint);
        }
        this.previousAllowBreak = null;
        this.previousAllowPlace = null;
        this.previousAllowInventory = null;
        this.previousAllowSprint = null;
    }

    private static boolean validAnarchy(int value) {
        return value >= 101 && value <= 905;
    }

    private String getEndWarp() {
        String value = this.endWarp.getText();
        return value == null ? "" : value.trim();
    }

    private static boolean validWarpName(String value) {
        return value != null && value.matches("[\\p{L}\\p{N}_.-]{1,32}");
    }

    private static int parseAnarchy(String value) {
        try {
            return Integer.parseInt(value == null ? "" : value.replaceAll("[^0-9]", ""));
        }
        catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private static String displayAnarchy(int value) {
        return AutoEnd.validAnarchy(value) ? Integer.toString(value) : "\u043d\u0435 \u0437\u0430\u0434\u0430\u043d\u0430";
    }

    private static enum Phase {
        SWITCH_BASE("\u043f\u0435\u0440\u0435\u0445\u043e\u0434 \u043d\u0430 \u0431\u0430\u0437\u0443"),
        OPEN_BASE("\u043f\u043e\u0438\u0441\u043a \u0441\u0443\u043d\u0434\u0443\u043a\u0430"),
        PREPARE("\u043f\u043e\u043f\u043e\u043b\u043d\u0435\u043d\u0438\u0435"),
        DEPOSIT("\u0441\u043a\u043b\u0430\u0434\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u0435"),
        SWITCH_END("\u043f\u0435\u0440\u0435\u0445\u043e\u0434 \u0432 \u042d\u043d\u0434"),
        END_ORIGIN("\u043f\u0443\u0442\u044c \u043a 0,0"),
        END_BUFFS("\u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435 \u0431\u0430\u0444\u043e\u0432"),
        END_HOME("\u043f\u0435\u0440\u0435\u0445\u043e\u0434 \u043d\u0430 \u0432\u0430\u0440\u043f"),
        DESCENDING("\u043f\u0440\u044b\u0436\u043e\u043a \u0432\u043d\u0438\u0437"),
        SEARCHING("\u043f\u043e\u0438\u0441\u043a \u0432\u0430\u0437"),
        BREAKING_POT("\u0440\u0430\u0437\u0431\u0438\u0432\u0430\u043d\u0438\u0435 \u0432\u0430\u0437\u044b"),
        COLLECTING("\u0441\u0431\u043e\u0440 \u0434\u043e\u0431\u044b\u0447\u0438"),
        BRUSHING("\u043e\u0447\u0438\u0441\u0442\u043a\u0430 \u043f\u0435\u0441\u043a\u0430"),
        COMBAT_WAIT("\u043e\u0436\u0438\u0434\u0430\u043d\u0438\u0435 \u041a\u0422");

        private final String title;

        private Phase(String title) {
            this.title = title;
        }
    }

    private static enum BuffConsumeAction {
        NONE(null),
        INVISIBILITY((class_6880<class_1291>)class_1294.field_5905),
        SPEED((class_6880<class_1291>)class_1294.field_5904),
        FOOD(null);

        private final class_6880<class_1291> effect;

        private BuffConsumeAction(class_6880<class_1291> effect) {
            this.effect = effect;
        }

        private boolean matches(AutoEnd owner, class_1799 stack) {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 1 -> owner.isInvisibilityPotion(stack);
                case 2 -> owner.isSpeedPotion(stack);
                case 3 -> {
                    if (stack != null && stack.method_31574(class_1802.field_8071)) {
                        yield true;
                    }
                    yield false;
                }
                case 0 -> false;
            };
        }
    }

    private static final class PendingTransfer {
        private final int syncId;
        private final int sourceSlot;
        private final int destinationSlot;
        private int remaining;
        private TransferStage stage = TransferStage.PICKUP;

        private PendingTransfer(int syncId, int sourceSlot, int destinationSlot, int amount) {
            this.syncId = syncId;
            this.sourceSlot = sourceSlot;
            this.destinationSlot = destinationSlot;
            this.remaining = amount;
        }
    }

    private record Transfer(class_1735 from, class_1735 to, int amount) {
    }

    private static enum TransferStage {
        PICKUP,
        PLACE,
        RETURN;

    }
}

