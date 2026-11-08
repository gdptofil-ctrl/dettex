/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.pve;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.class_303;
import net.minecraft.class_1263;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1733;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2586;
import net.minecraft.class_2595;
import net.minecraft.class_2627;
import net.minecraft.class_2680;
import net.minecraft.class_2868;
import net.minecraft.class_3489;
import net.minecraft.class_3719;
import net.minecraft.class_3965;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.TextSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.QuickLogger;
import ru.recoin.common.util.entity.MovingUtil;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.other.BaritoneCompat;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.modules.misc.TelegramAPI;
import ru.recoin.implement.screens.menu.MenuScreen;

/**
 * SwordWarm farm:
 * takes sticks and emeralds from a nearby chest (at least 1 stick + 2 emeralds
 * for one sword, at most 32 sticks + 64 emeralds for 32 swords), crafts as many
 * emerald swords as the materials allow and lists every sword on the auction
 * with the price configured by the player.
 */
public final class SwordWarm
extends Module {
    private static final int MIN_STICKS = 1;
    private static final int MIN_EMERALDS = 2;
    private static final int MAX_STICKS = 32;
    private static final int MAX_EMERALDS = 64;
    private static final int MAX_SWORDS = 32;
    private static final int EMERALDS_PER_SWORD = 2;
    private static final int STICKS_PER_SWORD = 1;
    private static final int RESULT_SLOT = 0;
    private static final int[][] EMERALD_LAYOUTS = {{2, 5}, {1, 4}, {3, 6}};
    private static final int[] STICK_LAYOUTS = {8, 7, 9};
    private static final int FRUITLESS_LIMIT = 30;
    private static final int CRAFTING_PLAYER_SLOTS_START = 10;
    private static final int PLAYER_INVENTORY_SIZE = 36;
    private static final double INTERACT_RANGE_SQUARED = 6.25;
    private static final long OPEN_TIMEOUT_MS = 3000L;
    private static final long CHEST_SKIP_MS = 12000L;
    private static final long SELL_COMMAND_GAP_MS = 1400L;

    private final TextSetting sellPrice = new TextSetting("Sell Price", "\u0426\u0435\u043d\u0430 \u043c\u0435\u0447\u0430 \u043d\u0430 \u0430\u0443\u043a\u0446\u0438\u043e\u043d\u0435").setText("15000");
    private final ValueSetting chestRadius = new ValueSetting("Chest Radius", "\u0420\u0430\u0434\u0438\u0443\u0441 \u043f\u043e\u0438\u0441\u043a\u0430 \u0441\u0443\u043d\u0434\u0443\u043a\u0430 \u0441 \u043c\u0430\u0442\u0435\u0440\u0438\u0430\u043b\u0430\u043c\u0438").setValue(16.0f).range(4.0f, 48.0f).step(1.0f);
    private final ValueSetting tableRadius = new ValueSetting("Table Radius", "\u0420\u0430\u0434\u0438\u0443\u0441 \u043f\u043e\u0438\u0441\u043a\u0430 \u0441\u0442\u0430\u043d\u043a\u0430").setValue(24.0f).range(4.0f, 48.0f).step(1.0f);
    private final ValueSetting actionDelay = new ValueSetting("Action Delay", "\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430 \u043c\u0435\u0436\u0434\u0443 \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u044f\u043c\u0438 \u0441 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0451\u043c").setValue(100.0f).range(40.0f, 400.0f).step(10.0f);
    private final ValueSetting sellDelay = new ValueSetting("Sell Delay", "\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430 \u043c\u0435\u0436\u0434\u0443 \u0432\u044b\u0441\u0442\u0430\u0432\u043b\u0435\u043d\u0438\u044f\u043c\u0438 \u043c\u0435\u0447\u0435\u0439").setValue(2.0f).range(0.5f, 30.0f).step(0.5f);
    private final ValueSetting cycleCooldown = new ValueSetting("Cycle Cooldown", "\u041f\u0430\u0443\u0437\u0430 \u043f\u043e\u0441\u043b\u0435 \u0446\u0438\u043a\u043b\u0430").setValue(5.0f).range(0.0f, 120.0f).step(1.0f);
    private final BooleanSetting bypass = new BooleanSetting("Bypass", "\u041e\u0431\u0445\u043e\u0434 \u043f\u0440\u043e\u0432\u0435\u0440\u043e\u043a FunTime: \u043f\u043b\u0430\u0432\u043d\u044b\u0435 \u0437\u0430\u0434\u0435\u0440\u0436\u043a\u0438 \u0438 \u0431\u0435\u0437\u043e\u043f\u0430\u0441\u043d\u044b\u0435 \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u044f").setValue(true);
    private final BooleanSetting telegram = new BooleanSetting("Telegram", "\u041e\u0442\u043f\u0440\u0430\u0432\u043b\u044f\u0442\u044c \u043e\u0442\u0447\u0451\u0442 \u043e \u0446\u0438\u043a\u043b\u0435 \u0432 Telegram").setValue(true);

    private final Deque<Runnable> pendingClicks = new ArrayDeque<>();
    private final Predicate<class_1799> swordPredicate = stack -> {
        if (stack.method_7960()) {
            return false;
        }
        if (stack.method_31573(class_3489.field_42611)) {
            return true;
        }
        String name = stack.method_7964().getString().toLowerCase(Locale.ROOT);
        return name.contains("\u043c\u0435\u0447") || name.contains("sword") || name.contains("\u043a\u043b\u0438\u043d\u043e\u043a");
    };
    private final Map<class_2338, Long> skippedChests = new HashMap<>();

    private State state = State.COLLECT;
    private Object baritone;
    private class_2338 chestPos;
    private class_2338 tablePos;
    private int layoutIndex;
    private int emeraldSlotA = 2;
    private int emeraldSlotB = 5;
    private int stickSlot = 8;
    private long nextActionAt;
    private long stateSince;
    private long lastNotifyAt;
    private long nextChestScanAt;
    private long nextInteractAt;
    private long aimReadyAt;
    private class_2338 aimPos;
    private long lastSellCommandAt;
    private long nextCraftCheckAt;
    private long nextChatCheckAt;
    private long chatCooldownUntil;
    private String lastChatLine = "";
    private boolean walking;
    private int openAttempts;
    private int crafted;
    private int sold;
    private int craftTarget;
    private int sellTarget;
    private int swordsAtStart;
    private int fillAttempts;
    private int fruitlessTicks;
    private int resultClicks;
    private int sellAttempts;
    private int swordsAtSell;
    private boolean sellArmed;
    private boolean tableChecked;
    private boolean withdrawTable;
    private long tableWithdrawAt;
    private class_2248 craftingTableBlock;
    private class_2338 placeSupport;
    private class_2338 placeTarget;
    private int placeAttempts;
    private int placeTries;
    private long nextPlaceAt;

    public SwordWarm() {
        super("SwordWarm", "Sword Warm", ModuleCategory.PVE);
        this.setup(this.sellPrice, this.chestRadius, this.tableRadius, this.actionDelay, this.sellDelay, this.cycleCooldown, this.bypass, this.telegram);
    }

    @Override
    public void activate() {
        this.baritone = BaritoneCompat.getPrimaryBaritone();
        this.pendingClicks.clear();
        this.skippedChests.clear();
        this.chestPos = null;
        this.tablePos = null;
        this.nextActionAt = 0L;
        this.lastNotifyAt = 0L;
        this.nextChestScanAt = 0L;
        this.nextInteractAt = 0L;
        this.aimReadyAt = 0L;
        this.aimPos = null;
        this.lastSellCommandAt = 0L;
        this.nextCraftCheckAt = 0L;
        this.nextChatCheckAt = 0L;
        this.chatCooldownUntil = 0L;
        this.lastChatLine = this.currentChatLine().toLowerCase(Locale.ROOT);
        this.openAttempts = 0;
        this.crafted = 0;
        this.sold = 0;
        this.craftTarget = 0;
        this.sellTarget = 0;
        this.swordsAtStart = this.countSwords();
        this.fillAttempts = 0;
        this.fruitlessTicks = 0;
        this.resultClicks = 0;
        this.sellAttempts = 0;
        this.swordsAtSell = 0;
        this.sellArmed = false;
        this.tableChecked = false;
        this.withdrawTable = false;
        this.tableWithdrawAt = 0L;
        this.placeSupport = null;
        this.placeTarget = null;
        this.placeAttempts = 0;
        this.placeTries = 0;
        this.nextPlaceAt = 0L;
        this.layoutIndex = 0;
        this.applyLayout();
        this.state = State.COLLECT;
        this.stateSince = System.currentTimeMillis();
    }

    @Override
    public void deactivate() {
        this.pendingClicks.clear();
        this.skippedChests.clear();
        this.chestPos = null;
        this.tablePos = null;
        this.placeSupport = null;
        this.placeTarget = null;
        this.stopWalking();
        this.forceCloseScreen();
        BaritoneCompat.cancelPathing(this.baritone);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.field_1724 == null || mc.field_1687 == null || mc.field_1761 == null || mc.field_1724.field_3944 == null) {
            return;
        }
        if (mc.field_1755 instanceof MenuScreen) {
            this.stopWalking();
            return;
        }
        long now = System.currentTimeMillis();
        if (!this.pendingClicks.isEmpty()) {
            if (now < this.nextActionAt) {
                return;
            }
            Runnable click = this.pendingClicks.poll();
            if (click != null) {
                click.run();
            }
            this.nextActionAt = now + this.clickDelay();
            return;
        }
        if (now < this.nextActionAt) {
            return;
        }
        switch (this.state) {
            case COLLECT: {
                this.tickCollect(now);
                break;
            }
            case OPEN_CHEST: {
                this.tickOpenChest(now);
                break;
            }
            case WAIT_CHEST: {
                this.tickWaitChest(now);
                break;
            }
            case WITHDRAW: {
                this.tickWithdraw(now);
                break;
            }
            case GO_TABLE: {
                this.tickGoTable(now);
                break;
            }
            case OPEN_TABLE: {
                this.tickOpenTable(now);
                break;
            }
            case WAIT_TABLE: {
                this.tickWaitTable(now);
                break;
            }
            case CRAFT: {
                this.tickCraft(now);
                break;
            }
            case SELL: {
                this.tickSell(now);
                break;
            }
            case COOLDOWN: {
                if (now >= this.nextActionAt) {
                    this.crafted = 0;
                    this.sold = 0;
                    this.craftTarget = 0;
                    this.sellTarget = 0;
                    this.fillAttempts = 0;
                    this.fruitlessTicks = 0;
                    this.resultClicks = 0;
                    this.sellAttempts = 0;
                    this.swordsAtSell = 0;
                    this.sellArmed = false;
                    this.tableChecked = false;
                    this.withdrawTable = false;
        this.tableWithdrawAt = 0L;
                    this.placeSupport = null;
                    this.placeTarget = null;
                    this.placeAttempts = 0;
                    this.placeTries = 0;
                    this.layoutIndex = 0;
                    this.applyLayout();
                    this.chestPos = null;
                    this.nextChestScanAt = 0L;
                    this.swordsAtStart = this.countSwords();
                    this.setState(State.COLLECT);
                    this.nextActionAt = now + 100L;
                }
                break;
            }
        }
    }

    private void tickCollect(long now) {
        if (this.hasOpenScreen()) {
            this.closeScreen();
            this.nextActionAt = now + this.clickDelay();
            return;
        }
        if (!this.tableChecked) {
            this.tableChecked = true;
            if (this.tablePos == null || !this.isCraftingTable(this.tablePos)) {
                this.tablePos = this.findCraftingTable(this.tableRadius.getValue());
            }
            this.withdrawTable = this.tablePos == null && !this.hasCraftingTableItem();
        }
        if (this.hasMaterials() && (!this.withdrawTable || this.hasCraftingTableItem())) {
            this.craftTarget = this.availableSwords();
            this.setState(State.GO_TABLE);
            this.nextActionAt = now + 100L;
            return;
        }
        if (this.chestPos == null) {
            if (now < this.nextChestScanAt) {
                return;
            }
            this.nextChestScanAt = now + 400L;
            this.chestPos = this.findSupplyChest(this.chestRadius.getValue());
            if (this.chestPos == null) {
                this.failCycle(this.skippedChests.isEmpty()
                        ? "\u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d \u0441\u0443\u043d\u0434\u0443\u043a \u0441 \u0438\u0437\u0443\u043c\u0440\u0443\u0434\u0430\u043c\u0438 \u0438 \u043f\u0430\u043b\u043a\u0430\u043c\u0438 (\u043d\u0443\u0436\u043d\u043e \u043e\u0442 1 \u043f\u0430\u043b\u043a\u0438 \u0438 2 \u0438\u0437\u0443\u043c\u0440\u0443\u0434\u043e\u0432)"
                        : "\u0432 \u043d\u0430\u0439\u0434\u0435\u043d\u043d\u044b\u0445 \u0441\u0443\u043d\u0434\u0443\u043a\u0430\u0445 \u043d\u0435\u0442 \u043f\u0430\u043b\u043e\u043a \u0438 \u0438\u0437\u0443\u043c\u0440\u0443\u0434\u043e\u0432 \u0434\u043b\u044f \u043c\u0435\u0447\u0430");
                return;
            }
            this.notify("SwordWarm: \u043d\u0430\u0439\u0434\u0435\u043d \u0441\u0443\u043d\u0434\u0443\u043a " + this.chestPos.method_10263() + " " + this.chestPos.method_10264() + " " + this.chestPos.method_10260());
        }
        if (!this.isNear(this.chestPos)) {
            this.walkTo(this.chestPos);
            this.nextActionAt = now + 200L;
            return;
        }
        this.stopWalking();
        this.setState(State.OPEN_CHEST);
        this.nextActionAt = now + 50L;
    }

    private void tickOpenChest(long now) {
        if (this.chestPos == null || !this.isSupplyContainer(mc.field_1687.method_8321(this.chestPos))) {
            this.chestPos = null;
            this.setState(State.COLLECT);
            return;
        }
        if (this.isChestScreen()) {
            this.openAttempts = 0;
            this.setState(State.WITHDRAW);
            this.nextActionAt = now + 150L;
            return;
        }
        if (this.hasOpenScreen()) {
            this.closeScreen();
            this.nextActionAt = now + this.clickDelay();
            return;
        }
        if (!this.isNear(this.chestPos)) {
            this.setState(State.COLLECT);
            return;
        }
        this.stopWalking();
        if (!this.useBlockSafely(this.chestPos, now)) {
            this.nextActionAt = now + 60L;
            return;
        }
        this.stateSince = now;
        this.setState(State.WAIT_CHEST);
    }

    private void tickWaitChest(long now) {
        if (this.isChestScreen()) {
            this.openAttempts = 0;
            this.setState(State.WITHDRAW);
            this.nextActionAt = now + 150L;
            return;
        }
        if (now - this.stateSince < OPEN_TIMEOUT_MS) {
            return;
        }
        if (++this.openAttempts >= 3) {
            this.failCycle("\u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043e\u0442\u043a\u0440\u044b\u0442\u044c \u0441\u0443\u043d\u0434\u0443\u043a");
            return;
        }
        this.setState(State.OPEN_CHEST);
    }

    private void tickWithdraw(long now) {
        if (!this.isChestScreen()) {
            this.setState(State.OPEN_CHEST);
            return;
        }
        class_1707 handler = (class_1707)mc.field_1724.field_7512;
        if (!this.tableChecked) {
            this.tableChecked = true;
            if (this.tablePos == null || !this.isCraftingTable(this.tablePos)) {
                this.tablePos = this.findCraftingTable(this.tableRadius.getValue());
            }
            this.withdrawTable = this.tablePos == null && !this.hasCraftingTableItem();
        }
        int needSticks = Math.max(0, MAX_STICKS - this.countItem(class_1802.field_8556));
        if (needSticks > 0 && this.queueWithdraw(handler, class_1802.field_8556, needSticks)) {
            return;
        }
        if (this.state != State.WITHDRAW) {
            return;
        }
        int totalSticks = this.countItem(class_1802.field_8556);
        int usefulEmeralds = Math.min(MAX_EMERALDS, totalSticks * EMERALDS_PER_SWORD);
        int needEmeralds = Math.max(0, usefulEmeralds - this.countItem(class_1802.field_8687));
        if (needEmeralds > 0 && this.queueWithdraw(handler, class_1802.field_8687, needEmeralds)) {
            return;
        }
        if (this.state != State.WITHDRAW) {
            return;
        }
        if (this.withdrawTable && !this.hasCraftingTableItem()) {
            if (this.tableWithdrawAt == 0L && this.queueWithdrawTable(handler)) {
                this.tableWithdrawAt = now + 1500L;
                return;
            }
            if (this.tableWithdrawAt != 0L && now < this.tableWithdrawAt) {
                return;
            }
            this.tableWithdrawAt = 0L;
        }
        class_2338 exhaustedChest = this.chestPos;
        this.closeScreen();
        this.chestPos = null;
        this.openAttempts = 0;
        if (this.hasMaterials()) {
            this.craftTarget = this.availableSwords();
            this.setState(State.GO_TABLE);
            this.nextActionAt = now + 200L;
            return;
        }
        if (exhaustedChest != null) {
            this.skipChest(exhaustedChest);
        }
        this.nextChestScanAt = 0L;
        this.setState(State.COLLECT);
        this.nextActionAt = now + this.clickDelay();
    }

    private boolean queueWithdraw(class_1707 handler, class_1792 item, int needed) {
        if (needed <= 0) {
            return false;
        }
        int take = needed;
        if (item == class_1802.field_8556) {
            take = Math.min(needed, MAX_STICKS);
        }
        if (item == class_1802.field_8687) {
            take = Math.min(needed, MAX_EMERALDS);
        }
        int containerSlots = this.containerSlots(handler);
        for (int index = 0; index < containerSlots; ++index) {
            class_1735 slot = handler.method_7611(index);
            class_1799 stack = slot.method_7677();
            if (stack.method_7960()) {
                continue;
            }
            boolean match = stack.method_31574(item);
            if (!match && item == class_1802.field_8556) {
                String name = stack.method_7964().getString().toLowerCase(java.util.Locale.ROOT);
                match = name.contains("stick") || name.contains("палк");
            }
            if (!match) {
                continue;
            }
            int count = stack.method_7947();
            if (count <= take) {
                // Whole stack fits: shift-click moves it straight into the inventory.
                this.enqueueClick(slot.field_7874, 0, class_1713.field_7794);
                return true;
            }
            int emptySlot = this.findEmptyInventorySlot(handler, containerSlots);
            if (emptySlot < 0) {
                this.failCycle("\u043d\u0435\u0442 \u0441\u0432\u043e\u0431\u043e\u0434\u043d\u043e\u0433\u043e \u043c\u0435\u0441\u0442\u0430 \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435");
                return false;
            }
            // Take the stack on the cursor, drop one item per click into an empty
            // slot until the needed amount is moved, then put the rest back.
            this.enqueueClick(slot.field_7874, 0, class_1713.field_7790);
            for (int taken = 0; taken < take; ++taken) {
                this.enqueueClick(emptySlot, 1, class_1713.field_7790);
            }
            this.enqueueClick(slot.field_7874, 0, class_1713.field_7790);
            return true;
        }
        return false;
    }

    private boolean queueWithdrawTable(class_1707 handler) {
        int containerSlots = this.containerSlots(handler);
        for (int index = 0; index < containerSlots; ++index) {
            class_1735 slot = handler.method_7611(index);
            if (!this.isCraftingTableStack(slot.method_7677())) {
                continue;
            }
            // Shift-click moves the table straight into the inventory, which keeps
            // the cursor free for the material withdrawal clicks.
            this.enqueueClick(slot.field_7874, 0, class_1713.field_7794);
            return true;
        }
        return false;
    }

    private void tickGoTable(long now) {
        if (this.hasOpenScreen()) {
            this.closeScreen();
            this.nextActionAt = now + 150L;
            return;
        }
        if (this.tablePos == null || !this.isCraftingTable(this.tablePos)) {
            this.tablePos = this.findCraftingTable(this.tableRadius.getValue());
            if (this.tablePos == null) {
                if (!this.tickPlaceTable(now)) {
                    this.failCycle("\u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d \u0441\u0442\u0430\u043d\u043e\u043a \u0438 \u043d\u0435\u0442 \u0432\u0435\u0440\u0441\u0442\u0430\u043a\u0430 \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435");
                    return;
                }
                if (this.tablePos == null) {
                    this.nextActionAt = now + 80L;
                    return;
                }
            }
        }
        if (mc.field_1724.field_7512 instanceof class_1733) {
            this.openAttempts = 0;
            this.setState(State.CRAFT);
            this.nextActionAt = now + 200L;
            return;
        }
        if (!this.isNear(this.tablePos)) {
            if (now - this.stateSince > 15000L) {
                this.tablePos = null;
                if (this.tickPlaceTable(now)) {
                    this.stateSince = now;
                    this.nextActionAt = now + 100L;
                    return;
                }
                this.failCycle("\u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u043e\u0434\u043e\u0439\u0442\u0438 \u043a \u0441\u0442\u0430\u043d\u043a\u0443");
                return;
            }
            this.walkTo(this.tablePos);
            this.nextActionAt = now + 200L;
            return;
        }
        this.stopWalking();
        this.setState(State.OPEN_TABLE);
        this.nextActionAt = now + 50L;
    }

    /**
     * Places a crafting table from the inventory next to the player when no
     * table is nearby. Returns {@code true} while placing is still possible
     * (the table may appear on a later tick), {@code false} when there is no
     * table to place or every position has been tried.
     */
    private boolean tickPlaceTable(long now) {
        if (this.placeTarget != null && this.isCraftingTable(this.placeTarget)) {
            this.tablePos = this.placeTarget.method_10062();
            this.notify("SwordWarm: \u0432\u0435\u0440\u0441\u0442\u0430\u043a \u043f\u043e\u0441\u0442\u0430\u0432\u043b\u0435\u043d");
            this.placeTarget = null;
            this.placeSupport = null;
            this.placeAttempts = 0;
            this.placeTries = 0;
            return true;
        }
        if (this.hasOpenScreen()) {
            this.closeScreen();
            this.nextPlaceAt = now + 150L;
            return true;
        }
        if (this.placeTarget == null) {
            if (this.placeTries >= 12 || this.findCraftingTableItemSlot() < 0) {
                return false;
            }
            this.placeSupport = this.findPlacementSupport();
            if (this.placeSupport == null) {
                return false;
            }
            this.placeTarget = this.placeSupport.method_10084();
            this.placeAttempts = 0;
            this.nextPlaceAt = now;
        }
        if (now >= this.nextPlaceAt && this.placeAttempts >= 3) {
            this.placeTarget = null;
            this.placeSupport = null;
            this.placeAttempts = 0;
            return true;
        }
        if (!this.isCraftingTableStack(mc.field_1724.method_6047())) {
            if (now < this.nextPlaceAt) {
                return true;
            }
            if (!this.equipCraftingTable()) {
                return false;
            }
            this.nextPlaceAt = now + 200L;
            return true;
        }
        if (now < this.nextPlaceAt) {
            return true;
        }
        if (!this.aimAt(this.placeSupport)) {
            this.nextPlaceAt = now + 60L;
            return true;
        }
        if (this.bypass.isValue() && MovingUtil.hasPlayerMovement()) {
            this.nextPlaceAt = now + 80L;
            return true;
        }
        ++this.placeAttempts;
        ++this.placeTries;
        this.useBlockTop(this.placeSupport);
        this.nextPlaceAt = now + 350L;
        return true;
    }

    private class_2338 findPlacementSupport() {
        if (mc.field_1724 == null || mc.field_1687 == null) {
            return null;
        }
        class_2338 origin = mc.field_1724.method_24515();
        class_2338 best = null;
        double bestScore = Double.MAX_VALUE;
        for (int dy = -1; dy <= 1; ++dy) {
            for (int dx = -2; dx <= 2; ++dx) {
                for (int dz = -2; dz <= 2; ++dz) {
                    if (dx == 0 && dz == 0) {
                        continue;
                    }
                    class_2338 support = origin.method_10069(dx, dy, dz);
                    class_2338 target = support.method_10084();
                    if (target.equals(origin) || target.equals(origin.method_10084())) {
                        continue;
                    }
                    class_2680 supportState = mc.field_1687.method_8320(support);
                    class_2680 targetState = mc.field_1687.method_8320(target);
                    if (supportState.method_26215() || !targetState.method_26215()) {
                        continue;
                    }
                    if (!mc.field_1687.method_8316(target).method_15769()) {
                        continue;
                    }
                    if (supportState.method_26220(mc.field_1687, support).method_1110()) {
                        continue;
                    }
                    double distance = mc.field_1724.method_33571().method_1025(target.method_46558());
                    if (distance > 20.25) {
                        continue;
                    }
                    double score = distance + (double)Math.abs(dy) * 0.5;
                    if (score >= bestScore) {
                        continue;
                    }
                    bestScore = score;
                    best = support.method_10062();
                }
            }
        }
        return best;
    }

    private void useBlockTop(class_2338 support) {
        class_243 hit = support.method_46558().method_1019(new class_243(0.0, 0.5, 0.0));
        class_3965 result = new class_3965(hit, class_2350.field_11036, support, false);
        mc.field_1761.method_2896(mc.field_1724, class_1268.field_5808, result);
        mc.field_1724.method_6104(class_1268.field_5808);
    }

    private void tickOpenTable(long now) {
        if (this.tablePos == null || !this.isCraftingTable(this.tablePos)) {
            this.tablePos = null;
            this.setState(State.GO_TABLE);
            return;
        }
        if (mc.field_1724.field_7512 instanceof class_1733) {
            this.openAttempts = 0;
            this.setState(State.CRAFT);
            this.nextActionAt = now + 200L;
            return;
        }
        if (this.hasOpenScreen()) {
            this.closeScreen();
            this.nextActionAt = now + this.clickDelay();
            return;
        }
        if (!this.isNear(this.tablePos)) {
            this.setState(State.GO_TABLE);
            return;
        }
        this.stopWalking();
        if (!this.useBlockSafely(this.tablePos, now)) {
            this.nextActionAt = now + 60L;
            return;
        }
        this.stateSince = now;
        this.setState(State.WAIT_TABLE);
    }

    private void tickWaitTable(long now) {
        if (mc.field_1724.field_7512 instanceof class_1733) {
            this.openAttempts = 0;
            this.setState(State.CRAFT);
            this.nextActionAt = now + 200L;
            return;
        }
        if (now - this.stateSince < OPEN_TIMEOUT_MS) {
            return;
        }
        if (++this.openAttempts >= 3) {
            this.failCycle("\u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043e\u0442\u043a\u0440\u044b\u0442\u044c \u0441\u0442\u0430\u043d\u043e\u043a");
            return;
        }
        this.setState(State.OPEN_TABLE);
    }

    private void tickCraft(long now) {
        if (!(mc.field_1724.field_7512 instanceof class_1733)) {
            this.setState(State.GO_TABLE);
            return;
        }
        if (now < this.nextCraftCheckAt) {
            return;
        }
        class_1733 handler = (class_1733)mc.field_1724.field_7512;
        if (this.craftTarget <= 0) {
            this.craftTarget = this.availableSwords();
        }
        if (this.craftTarget <= 0) {
            this.failCycle("\u043d\u0435 \u043e\u0441\u0442\u0430\u043b\u043e\u0441\u044c \u043c\u0430\u0442\u0435\u0440\u0438\u0430\u043b\u043e\u0432 \u0434\u043b\u044f \u043a\u0440\u0430\u0444\u0442\u0430");
            return;
        }
        int swordsNow = this.countSwords();
        if (swordsNow - this.swordsAtStart > this.crafted) {
            this.crafted = swordsNow - this.swordsAtStart;
            this.fruitlessTicks = 0;
            this.resultClicks = 0;
        }
        if (this.crafted >= this.craftTarget) {
            this.finishCrafting(now);
            return;
        }
        class_1735 result = handler.method_7611(RESULT_SLOT);
        if (!result.method_7677().method_7960()) {
            if (!this.swordPredicate.test(result.method_7677())) {
                if (this.crafted > 0) {
                    this.finishCrafting(now);
                } else if (!this.tryNextLayout(handler)) {
                    this.failCycle("\u0440\u0435\u0446\u0435\u043f\u0442 \u043c\u0435\u0447\u0430 \u043d\u0435 \u0441\u043e\u0432\u043f\u0430\u043b");
                }
                return;
            }
            if (this.resultClicks >= this.craftTarget + 12) {
                this.finishCrafting(now);
                return;
            }
            ++this.resultClicks;
            this.fruitlessTicks = 0;
            this.enqueueClick(RESULT_SLOT, 0, class_1713.field_7794);
            return;
        }
        if (this.gridHasItems(handler)) {
            if (++this.fruitlessTicks >= FRUITLESS_LIMIT) {
                if (this.crafted > 0) {
                    this.finishCrafting(now);
                } else if (!this.tryNextLayout(handler)) {
                    this.failCycle("\u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043a\u0440\u0430\u0444\u0442\u0438\u0442\u044c \u043c\u0435\u0447\u0438");
                }
            }
            return;
        }
        if (this.crafted > 0) {
            this.finishCrafting(now);
            return;
        }
        if (this.availableSwords() < 1) {
            this.failCycle("\u043d\u0435\u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u043c\u0430\u0442\u0435\u0440\u0438\u0430\u043b\u043e\u0432 \u0434\u043b\u044f \u043a\u0440\u0430\u0444\u0442\u0430");
            return;
        }
        if (this.fillAttempts >= 4) {
            this.failCycle("\u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043a\u0440\u0430\u0444\u0442\u0438\u0442\u044c \u043c\u0435\u0447\u0438");
            return;
        }
        ++this.fillAttempts;
        this.fruitlessTicks = 0;
        this.queueFillGrid(handler);
    }

    private void queueFillGrid(class_1733 handler) {
        this.queueClearGrid(handler);
        int target = Math.max(1, Math.min(this.craftTarget, MAX_SWORDS));
        this.queueEmeralds(handler, target);
        this.queueSticks(handler, target);
    }

    private void queueEmeralds(class_1733 handler, int targetPerSlot) {
        int needA = targetPerSlot;
        int needB = targetPerSlot;
        for (class_1735 slot : handler.field_7761) {
            if (needA <= 0 && needB <= 0) {
                break;
            }
            if (slot.field_7874 < CRAFTING_PLAYER_SLOTS_START) {
                continue;
            }
            class_1799 stack = slot.method_7677();
            if (stack.method_7960() || !stack.method_31574(class_1802.field_8687)) {
                continue;
            }
            int count = stack.method_7947();
            int toA = Math.min(count, needA);
            int toB = Math.min(count - toA, needB);
            if (toA <= 0 && toB <= 0) {
                continue;
            }
            this.enqueueClick(slot.field_7874, 0, class_1713.field_7790);
            for (int taken = 0; taken < toA; ++taken) {
                this.enqueueClick(this.emeraldSlotA, 1, class_1713.field_7790);
            }
            for (int taken = 0; taken < toB; ++taken) {
                this.enqueueClick(this.emeraldSlotB, 1, class_1713.field_7790);
            }
            if (count - toA - toB > 0) {
                this.enqueueClick(slot.field_7874, 0, class_1713.field_7790);
            }
            needA -= toA;
            needB -= toB;
        }
    }

    private void queueSticks(class_1733 handler, int target) {
        int remaining = target;
        for (class_1735 slot : handler.field_7761) {
            if (remaining <= 0) {
                break;
            }
            if (slot.field_7874 < CRAFTING_PLAYER_SLOTS_START) {
                continue;
            }
            class_1799 stack = slot.method_7677();
            if (stack.method_7960() || !stack.method_31574(class_1802.field_8556)) {
                continue;
            }
            int count = stack.method_7947();
            int toSlot = Math.min(count, remaining);
            this.enqueueClick(slot.field_7874, 0, class_1713.field_7790);
            if (toSlot >= count) {
                this.enqueueClick(this.stickSlot, 0, class_1713.field_7790);
            } else {
                for (int taken = 0; taken < toSlot; ++taken) {
                    this.enqueueClick(this.stickSlot, 1, class_1713.field_7790);
                }
                this.enqueueClick(slot.field_7874, 0, class_1713.field_7790);
            }
            remaining -= toSlot;
        }
    }

    private boolean tryNextLayout(class_1733 handler) {
        if (this.layoutIndex + 1 >= EMERALD_LAYOUTS.length) {
            return false;
        }
        this.queueClearGrid(handler);
        ++this.layoutIndex;
        this.applyLayout();
        this.fillAttempts = 0;
        this.fruitlessTicks = 0;
        this.resultClicks = 0;
        this.nextCraftCheckAt = System.currentTimeMillis() + 400L;
        return true;
    }

    private void queueClearGrid(class_1733 handler) {
        for (int slotId = 1; slotId <= 9; ++slotId) {
            if (!handler.method_7611(slotId).method_7677().method_7960()) {
                this.enqueueClick(slotId, 0, class_1713.field_7794);
            }
        }
    }

    private void applyLayout() {
        int[] emerald = EMERALD_LAYOUTS[this.layoutIndex];
        this.emeraldSlotA = emerald[0];
        this.emeraldSlotB = emerald[1];
        this.stickSlot = STICK_LAYOUTS[this.layoutIndex];
    }

    private boolean gridHasItems(class_1733 handler) {
        for (int slotId = 1; slotId <= 9; ++slotId) {
            if (!handler.method_7611(slotId).method_7677().method_7960()) {
                return true;
            }
        }
        return false;
    }

    private void finishCrafting(long now) {
        boolean craftedSomething = this.crafted > 0;
        this.closeScreen();
        this.fruitlessTicks = 0;
        this.resultClicks = 0;
        this.fillAttempts = 0;
        this.craftTarget = 0;
        if (craftedSomething) {
            this.sellTarget = Math.min(this.crafted, this.countSwords());
            this.swordsAtSell = this.countSwords();
            this.sold = 0;
            this.sellAttempts = 0;
            this.sellArmed = false;
            this.setState(State.SELL);
        } else {
            this.setState(State.COLLECT);
        }
        this.nextActionAt = now + 200L;
    }

    private void tickSell(long now) {
        int remainingSwords = this.countSwords();
        this.sold = Math.max(0, Math.min(this.sellTarget, this.swordsAtSell - remainingSwords));
        if (this.sellTarget <= 0 || this.sold >= this.sellTarget || remainingSwords <= 0) {
            this.finishCycle(now);
            return;
        }
        if (this.hasOpenScreen()) {
            this.closeScreen();
            this.nextActionAt = now + 150L;
            return;
        }
        String price = this.digits(this.sellPrice.getText());
        if (price.isEmpty()) {
            this.failCycle("\u043d\u0435 \u0443\u043a\u0430\u0437\u0430\u043d\u0430 \u0446\u0435\u043d\u0430 \u043c\u0435\u0447\u0430");
            return;
        }
        if (this.sellAttempts > this.sellTarget + 5) {
            this.failCycle("\u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u0440\u043e\u0434\u0430\u0442\u044c \u043c\u0435\u0447\u0438");
            return;
        }
        if (this.handleChatCooldown(now)) {
            return;
        }
        if (!this.sellArmed) {
            if (!this.selectSword()) {
                this.finishCycle(now);
                return;
            }
            this.sellArmed = true;
            this.nextActionAt = now + 200L;
            return;
        }
        if (this.bypass.isValue() && now - this.lastSellCommandAt < SELL_COMMAND_GAP_MS) {
            this.nextActionAt = this.lastSellCommandAt + SELL_COMMAND_GAP_MS;
            return;
        }
        mc.field_1724.field_3944.method_45730("ah sell " + price);
        this.lastSellCommandAt = now;
        ++this.sellAttempts;
        this.sellArmed = false;
        this.nextActionAt = now + this.sellDelayMs();
    }

    private void finishCycle(long now) {
        String report = "SwordWarm: \u0441\u043e\u0437\u0434\u0430\u043d\u043e " + this.crafted + " \u043c\u0435\u0447\u0435\u0439, \u043f\u0440\u043e\u0434\u0430\u043d\u043e " + this.sold + " \u043f\u043e \u0446\u0435\u043d\u0435 " + this.digits(this.sellPrice.getText()) + ".";
        this.sendTelegram(report);
        this.notify(report);
        this.setState(State.COOLDOWN);
        this.nextActionAt = now + this.cycleDelayMs();
    }

    private void failCycle(String reason) {
        String message = "SwordWarm: " + reason + ".";
        this.notify(message);
        this.sendTelegram(message);
        this.pendingClicks.clear();
        this.forceCloseScreen();
        this.stopWalking();
        this.chestPos = null;
        this.tablePos = null;
        this.crafted = 0;
        this.sold = 0;
        this.craftTarget = 0;
        this.sellTarget = 0;
        this.sellAttempts = 0;
        this.swordsAtSell = 0;
        this.sellArmed = false;
        this.resultClicks = 0;
        this.fillAttempts = 0;
        this.fruitlessTicks = 0;
        this.tableChecked = false;
        this.withdrawTable = false;
        this.tableWithdrawAt = 0L;
        this.placeSupport = null;
        this.placeTarget = null;
        this.placeAttempts = 0;
        this.placeTries = 0;
        this.nextChestScanAt = 0L;
        this.setState(State.COOLDOWN);
        long cooldown = Math.max(5000L, this.cycleDelayMs());
        if (this.bypass.isValue()) {
            cooldown += 1000L + (long)(Math.random() * 2500.0);
        }
        this.nextActionAt = System.currentTimeMillis() + cooldown;
    }

    private void sendTelegram(String message) {
        if (!this.telegram.isValue()) {
            return;
        }
        TelegramAPI api = TelegramAPI.getInstance();
        if (api != null) {
            api.sendSwordWarmReport(message);
        }
    }

    private void notify(String message) {
        long now = System.currentTimeMillis();
        if (now - this.lastNotifyAt < 2500L || mc.field_1724 == null) {
            return;
        }
        this.lastNotifyAt = now;
        mc.field_1724.method_7353(QuickLogger.clientMessage(class_2561.method_43470(message)), false);
    }

    private boolean hasMaterials() {
        return this.availableSwords() >= 1;
    }

    private int availableSwords() {
        int sticks = this.countItem(class_1802.field_8556) / STICKS_PER_SWORD;
        int emeralds = this.countItem(class_1802.field_8687) / EMERALDS_PER_SWORD;
        return Math.min(MAX_SWORDS, Math.min(sticks, emeralds));
    }

    private int countItem(class_1792 item) {
        int count = 0;
        if (mc.field_1724 == null) {
            return 0;
        }
        for (int slot = 0; slot < PLAYER_INVENTORY_SIZE; ++slot) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(slot);
            if (stack.method_7960()) {
                continue;
            }
            if (stack.method_31574(item)) {
                count += stack.method_7947();
            }
        }
        return count;
    }

    private int countSwords() {
        int count = 0;
        if (mc.field_1724 == null) {
            return 0;
        }
        for (int slot = 0; slot < PLAYER_INVENTORY_SIZE; ++slot) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(slot);
            if (this.swordPredicate.test(stack)) {
                ++count;
            }
        }
        return count;
    }

    private boolean selectSword() {
        for (int slot = 0; slot < 9; ++slot) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(slot);
            if (!this.swordPredicate.test(stack)) {
                continue;
            }
            this.selectHotbarSlot(slot);
            return true;
        }
        class_1735 inventorySlot = PlayerInventoryUtil.getSlot(slot -> slot.field_7871 instanceof class_1661 && this.swordPredicate.test(slot.method_7677()));
        if (inventorySlot == null) {
            return false;
        }
        PlayerInventoryUtil.swapHand(inventorySlot, class_1268.field_5808, false);
        return true;
    }

    private class_2338 findSupplyChest(float radius) {
        int needEmeralds = Math.max(0, MAX_EMERALDS - this.countItem(class_1802.field_8687));
        int needSticks = Math.max(0, MAX_STICKS - this.countItem(class_1802.field_8556));
        int needTable = this.withdrawTable && !this.hasCraftingTableItem() ? 1 : 0;
        long now = System.currentTimeMillis();
        this.skippedChests.entrySet().removeIf(entry -> entry.getValue() <= now);
        class_2338 origin = mc.field_1724.method_24515();
        int horizontal = (int)Math.ceil(radius);
        int vertical = 6;
        double maxDistance = (double)radius * (double)radius;
        class_2338 best = null;
        double bestScore = Double.MAX_VALUE;
        for (int x = -horizontal; x <= horizontal; ++x) {
            for (int y = -vertical; y <= vertical; ++y) {
                for (int z = -horizontal; z <= horizontal; ++z) {
                    class_2338 pos = origin.method_10069(x, y, z);
                    class_1263 container = this.supplyContainerAt(pos);
                    if (container == null) {
                        continue;
                    }
                    Long skipUntil = this.skippedChests.get(pos);
                    if (skipUntil != null && skipUntil > now) {
                        continue;
                    }
                    double distance = mc.field_1724.method_5707(pos.method_46558());
                    if (distance > maxDistance) {
                        continue;
                    }
                    double score = distance;
                    if (this.containerContentsKnown(container)) {
                        score = this.containerHasNeeded(container, needEmeralds, needSticks, needTable) ? score - maxDistance : score + maxDistance;
                    }
                    if (score >= bestScore) {
                        continue;
                    }
                    bestScore = score;
                    best = pos.method_10062();
                }
            }
        }
        return best;
    }

    private class_1263 supplyContainerAt(class_2338 pos) {
        class_2586 entity = mc.field_1687.method_8321(pos);
        return this.isSupplyContainer(entity) ? (class_1263)entity : null;
    }

    private boolean isSupplyContainer(Object entity) {
        return entity instanceof class_2595 || entity instanceof class_2627 || entity instanceof class_3719;
    }

    private void skipChest(class_2338 pos) {
        long until = System.currentTimeMillis() + CHEST_SKIP_MS;
        this.skippedChests.put(pos.method_10062(), until);
        for (class_2350 direction : class_2350.values()) {
            class_2338 neighbor = pos.method_10093(direction);
            if (mc.field_1687.method_8321(neighbor) instanceof class_2595) {
                this.skippedChests.put(neighbor.method_10062(), until);
            }
        }
    }

    private boolean containerContentsKnown(class_1263 container) {
        for (int slot = 0; slot < container.method_5439(); ++slot) {
            if (!container.method_5438(slot).method_7960()) {
                return true;
            }
        }
        return false;
    }

    private boolean containerHasNeeded(class_1263 container, int needEmeralds, int needSticks, int needTable) {
        int emeralds = 0;
        int sticks = 0;
        int tables = 0;
        for (int slot = 0; slot < container.method_5439(); ++slot) {
            class_1799 stack = container.method_5438(slot);
            if (stack.method_7960()) {
                continue;
            }
            if (stack.method_31574(class_1802.field_8687)) {
                emeralds += stack.method_7947();
            } else if (stack.method_31574(class_1802.field_8556)) {
                sticks += stack.method_7947();
            } else if (this.isCraftingTableStack(stack)) {
                ++tables;
            }
        }
        return (needEmeralds <= 0 || emeralds > 0) && (needSticks <= 0 || sticks > 0) && (needTable <= 0 || tables > 0);
    }

    private class_2338 findCraftingTable(float radius) {
        class_2338 origin = mc.field_1724.method_24515();
        int horizontal = (int)Math.ceil(radius);
        int vertical = 5;
        class_2338 best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int x = -horizontal; x <= horizontal; ++x) {
            for (int y = -vertical; y <= vertical; ++y) {
                for (int z = -horizontal; z <= horizontal; ++z) {
                    class_2338 pos = origin.method_10069(x, y, z);
                    if (!this.isCraftingTable(pos)) {
                        continue;
                    }
                    double distance = mc.field_1724.method_5707(pos.method_46558());
                    if (distance >= bestDistance) {
                        continue;
                    }
                    bestDistance = distance;
                    best = pos.method_10062();
                }
            }
        }
        return best;
    }

    private boolean isCraftingTable(class_2338 pos) {
        if (mc.field_1687 == null || pos == null) {
            return false;
        }
        class_2680 state = mc.field_1687.method_8320(pos);
        if (state == null || state.method_26215()) {
            return false;
        }
        return this.isCraftingTableBlock(state.method_26204());
    }

    private boolean isCraftingTableBlock(class_2248 block) {
        if (block == null) {
            return false;
        }
        if (this.craftingTableBlock != null) {
            return block == this.craftingTableBlock;
        }
        String id = block.toString().toLowerCase(Locale.ROOT);
        if (id.contains("crafting_table")) {
            this.craftingTableBlock = block;
            return true;
        }
        String key = block.method_9518().getString().toLowerCase(Locale.ROOT);
        if (key.contains("crafting_table")) {
            this.craftingTableBlock = block;
            return true;
        }
        return key.contains("\u0432\u0435\u0440\u0441\u0442\u0430\u043a") || key.contains("crafting table") || key.contains("workbench");
    }

    private boolean isCraftingTableStack(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        String id = stack.method_7909().toString().toLowerCase(Locale.ROOT);
        if (id.contains("crafting_table")) {
            return true;
        }
        String name = stack.method_7964().getString().toLowerCase(Locale.ROOT);
        return name.contains("\u0432\u0435\u0440\u0441\u0442\u0430\u043a") || name.contains("crafting table") || name.contains("workbench");
    }

    private int findCraftingTableItemSlot() {
        if (mc.field_1724 == null) {
            return -1;
        }
        for (int slot = 0; slot < PLAYER_INVENTORY_SIZE; ++slot) {
            if (this.isCraftingTableStack(mc.field_1724.method_31548().method_5438(slot))) {
                return slot;
            }
        }
        return -1;
    }

    private boolean hasCraftingTableItem() {
        return this.findCraftingTableItemSlot() >= 0;
    }

    private boolean equipCraftingTable() {
        if (mc.field_1724 == null) {
            return false;
        }
        if (this.isCraftingTableStack(mc.field_1724.method_6047())) {
            return true;
        }
        for (int slot = 0; slot < 9; ++slot) {
            if (!this.isCraftingTableStack(mc.field_1724.method_31548().method_5438(slot))) {
                continue;
            }
            this.selectHotbarSlot(slot);
            return true;
        }
        class_1735 inventorySlot = PlayerInventoryUtil.getSlot(slot -> this.isCraftingTableStack(slot.method_7677()));
        if (inventorySlot == null) {
            return false;
        }
        PlayerInventoryUtil.swapHand(inventorySlot, class_1268.field_5808, false);
        return true;
    }

    private void selectHotbarSlot(int slot) {
        if (mc.field_1724 == null || slot < 0 || slot > 8) {
            return;
        }
        mc.field_1724.method_31548().method_61496(slot);
        // Mod code changes the selected slot directly, so the server has to be
        // told explicitly; otherwise /ah sell still uses the previous item.
        mc.field_1724.field_3944.method_52787(new class_2868(slot));
    }

    private int containerSlots(class_1707 handler) {
        return Math.max(0, handler.method_17388() * 9);
    }

    private int findEmptyInventorySlot(class_1707 handler, int containerSlots) {
        for (class_1735 slot : handler.field_7761) {
            if (slot.field_7874 < containerSlots) {
                continue;
            }
            if (slot.method_7677().method_7960()) {
                return slot.field_7874;
            }
        }
        return -1;
    }

    private boolean isChestScreen() {
        return mc.field_1724.field_7512 instanceof class_1707;
    }

    private boolean isNear(class_2338 pos) {
        return pos != null && mc.field_1724.method_5707(pos.method_46558()) <= INTERACT_RANGE_SQUARED;
    }

    private boolean useBlockSafely(class_2338 pos, long now) {
        if (!this.bypass.isValue()) {
            this.useBlock(pos);
            return true;
        }
        if (!this.aimAt(pos)) {
            this.aimPos = null;
            return false;
        }
        if (this.aimPos == null || !this.aimPos.equals(pos)) {
            this.aimPos = pos.method_10062();
            this.aimReadyAt = now + 90L + (long)(Math.random() * 90.0);
            return false;
        }
        if (now < this.aimReadyAt) {
            return false;
        }
        if (MovingUtil.hasPlayerMovement() || mc.field_1724.method_6115()) {
            return false;
        }
        if (now < this.nextInteractAt || now < this.chatCooldownUntil) {
            return false;
        }
        this.nextInteractAt = now + 130L + (long)(Math.random() * 90.0);
        this.aimPos = null;
        this.useBlock(pos);
        return true;
    }

    private boolean aimAt(class_2338 pos) {
        class_243 delta = pos.method_46558().method_1020(mc.field_1724.method_33571());
        double horizontal = Math.sqrt(delta.field_1352 * delta.field_1352 + delta.field_1350 * delta.field_1350);
        float targetYaw = (float)Math.toDegrees(Math.atan2(-delta.field_1352, delta.field_1350));
        float targetPitch = (float)(-Math.toDegrees(Math.atan2(delta.field_1351, horizontal)));
        float yaw = mc.field_1724.method_36454();
        float pitch = mc.field_1724.method_36455();
        float yawDiff = this.wrapDegrees(targetYaw - yaw);
        float pitchDiff = targetPitch - pitch;
        if (!this.bypass.isValue()) {
            mc.field_1724.method_36456(targetYaw);
            mc.field_1724.method_36457(targetPitch);
            return true;
        }
        boolean aimed = Math.abs(yawDiff) <= 1.5f && Math.abs(pitchDiff) <= 1.2f;
        if (aimed) {
            mc.field_1724.method_36456(targetYaw);
            mc.field_1724.method_36457(targetPitch);
            return true;
        }
        float yawStep = Math.max(-16.0f, Math.min(16.0f, yawDiff)) * 0.7f;
        float pitchStep = Math.max(-12.0f, Math.min(12.0f, pitchDiff)) * 0.7f;
        mc.field_1724.method_36456(yaw + yawStep);
        mc.field_1724.method_36457(pitch + pitchStep);
        return false;
    }

    private void useBlock(class_2338 pos) {
        mc.field_1761.method_2896(mc.field_1724, class_1268.field_5808, this.blockHit(pos));
        mc.field_1724.method_6104(class_1268.field_5808);
    }

    private class_3965 blockHit(class_2338 pos) {
        class_243 center = pos.method_46558();
        class_2350 side = class_2350.method_58251(mc.field_1724.method_33571().method_1020(center));
        class_243 hitPos = center.method_1019(class_243.method_24954(side.method_62675()).method_1021(0.5));
        return new class_3965(hitPos, side, pos, false);
    }

    private float wrapDegrees(float degrees) {
        float result = degrees % 360.0f;
        if (result >= 180.0f) {
            result -= 360.0f;
        }
        if (result < -180.0f) {
            result += 360.0f;
        }
        return result;
    }

    private void walkTo(class_2338 pos) {
        if (this.baritone == null) {
            this.baritone = BaritoneCompat.getPrimaryBaritone();
        }
        BaritoneCompat.goNear(this.baritone, pos, 2);
        class_243 target = pos.method_46558();
        class_243 delta = target.method_1020(mc.field_1724.method_73189());
        mc.field_1724.method_36456((float)Math.toDegrees(Math.atan2(-delta.field_1352, delta.field_1350)));
        mc.field_1690.field_1894.method_23481(true);
        mc.field_1690.field_1867.method_23481(true);
        mc.field_1690.field_1903.method_23481(mc.field_1724.field_5976 && mc.field_1724.method_24828());
        this.walking = true;
    }

    private void stopWalking() {
        if (this.walking) {
            mc.field_1690.field_1894.method_23481(false);
            mc.field_1690.field_1867.method_23481(false);
            mc.field_1690.field_1903.method_23481(false);
            this.walking = false;
        }
        BaritoneCompat.cancelPathing(this.baritone);
    }

    private boolean hasOpenScreen() {
        return mc.field_1755 != null;
    }

    private void closeScreen() {
        if (mc.field_1724 == null || !this.hasOpenScreen()) {
            return;
        }
        if (mc.field_1724.field_7512 != mc.field_1724.field_7498 && this.bypass.isValue() && !mc.field_1724.field_7512.method_34255().method_7960()) {
            class_1735 target = this.findEmptyPlayerSlot(mc.field_1724.field_7512);
            if (target != null) {
                PlayerInventoryUtil.clickSlot(target.field_7874, 0, class_1713.field_7790, false);
                this.nextActionAt = System.currentTimeMillis() + this.clickDelay();
                return;
            }
        }
        mc.field_1724.method_7346();
    }

    private void forceCloseScreen() {
        if (mc.field_1724 != null && this.hasOpenScreen()) {
            mc.field_1724.method_7346();
        }
    }

    private class_1735 findEmptyPlayerSlot(class_1703 handler) {
        int containerSlots = handler instanceof class_1733 ? CRAFTING_PLAYER_SLOTS_START : (handler instanceof class_1707 ? this.containerSlots((class_1707)handler) : 0);
        class_1799 cursor = handler.method_34255();
        for (class_1735 slot : handler.field_7761) {
            if (slot.field_7874 < containerSlots || !slot.method_7681()) {
                continue;
            }
            class_1799 stack = slot.method_7677();
            if (stack.method_7960()) {
                return slot;
            }
            if (class_1799.method_31577(stack, cursor) && stack.method_7947() + cursor.method_7947() <= stack.method_7914()) {
                return slot;
            }
        }
        return null;
    }

    private void enqueueClick(int slotId, int button, class_1713 clickType) {
        class_1703 current = mc.field_1724.field_7512;
        int windowId = current.field_7763;
        Class<?> handlerType = current.getClass();
        this.pendingClicks.add(() -> {
            if (mc.field_1724 == null) {
                return;
            }
            class_1703 handler = mc.field_1724.field_7512;
            if (handler == mc.field_1724.field_7498 || handler.field_7763 != windowId || !handlerType.isInstance(handler) || slotId < 0 || slotId >= handler.field_7761.size()) {
                this.pendingClicks.clear();
                return;
            }
            if (clickType == class_1713.field_7794 && !handler.method_34255().method_7960()) {
                this.pendingClicks.clear();
                return;
            }
            PlayerInventoryUtil.clickSlot(slotId, button, clickType, false);
        });
    }

    private long clickDelay() {
        long base = Math.max(40L, this.actionDelay.getInt());
        if (!this.bypass.isValue()) {
            return base;
        }
        long delay = Math.max(55L, base + (long)((double)base * (Math.random() * 0.6 - 0.1)));
        if (Math.random() < 0.06) {
            delay += 180L + (long)(Math.random() * 420.0);
        }
        return delay;
    }

    private long sellDelayMs() {
        long base = Math.max(500L, (long)(this.sellDelay.getValue() * 1000.0f));
        if (!this.bypass.isValue()) {
            return base;
        }
        long delay = base + (long)((double)base * (0.25 + Math.random() * 0.5)) + 200L;
        if (Math.random() < 0.08) {
            delay += 1500L + (long)(Math.random() * 2500.0);
        }
        return Math.max(SELL_COMMAND_GAP_MS, delay);
    }

    private long cycleDelayMs() {
        long base = Math.max(0L, (long)(this.cycleCooldown.getValue() * 1000.0f));
        if (!this.bypass.isValue()) {
            return base;
        }
        return base + (long)(Math.random() * Math.max(2000.0, (double)base * 0.3));
    }

    private boolean handleChatCooldown(long now) {
        if (!this.bypass.isValue() || now < this.nextChatCheckAt) {
            return false;
        }
        this.nextChatCheckAt = now + 300L;
        String latest = this.currentChatLine().toLowerCase(Locale.ROOT);
        if (latest.isEmpty() || latest.equals(this.lastChatLine)) {
            return false;
        }
        this.lastChatLine = latest;
        if (!this.isCooldownMessage(latest)) {
            return false;
        }
        long wait = 3500L + (long)(Math.random() * 2500.0);
        this.chatCooldownUntil = now + wait;
        this.nextActionAt = now + wait;
        this.notify("SwordWarm: \u0441\u0435\u0440\u0432\u0435\u0440 \u043f\u043e\u043f\u0440\u043e\u0441\u0438\u043b \u043f\u043e\u0434\u043e\u0436\u0434\u0430\u0442\u044c \u2014 \u043f\u0430\u0443\u0437\u0430");
        return true;
    }

    private boolean isCooldownMessage(String text) {
        return text.contains("\u043f\u043e\u0434\u043e\u0436\u0434") || text.contains("\u043d\u0435 \u0442\u0430\u043a \u0431\u044b\u0441\u0442\u0440\u043e") || text.contains("\u0441\u043b\u0438\u0448\u043a\u043e\u043c \u0447\u0430\u0441\u0442\u043e") || text.contains("\u043a\u0443\u043b\u0434\u0430\u0443\u043d") || text.contains("cooldown") || text.contains("slow down") || text.contains("wait a");
    }

    private String currentChatLine() {
        if (mc.field_1705 == null) {
            return "";
        }
        List<class_303> lines = mc.field_1705.method_1743().field_2061;
        if (lines == null || lines.isEmpty()) {
            return "";
        }
        return lines.get(0).comp_893().getString();
    }

    private void setState(State next) {
        if (this.state != next) {
            this.state = next;
            this.stateSince = System.currentTimeMillis();
            this.fruitlessTicks = 0;
            if (next == State.CRAFT) {
                this.fillAttempts = 0;
                this.resultClicks = 0;
            }
        }
    }

    private String digits(String text) {
        return text == null ? "" : text.replaceAll("[^0-9]", "");
    }

    private static enum State {
        COLLECT,
        OPEN_CHEST,
        WAIT_CHEST,
        WITHDRAW,
        GO_TABLE,
        OPEN_TABLE,
        WAIT_TABLE,
        CRAFT,
        SELL,
        COOLDOWN;
    }
}
