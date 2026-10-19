/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1657
 *  net.minecraft.class_1713
 *  net.minecraft.class_1735
 *  net.minecraft.class_2596
 *  net.minecraft.class_437
 *  net.minecraft.class_465
 *  net.minecraft.class_7439
 */
package ru.recoin.implement.features.modules.misc;

import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_2596;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_7439;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.modules.misc.AutoSell;
import ru.recoin.implement.features.modules.render.AuctionHelper;

public class AuctionRelist
extends Module {
    private static final long DEFAULT_COOLDOWN_MS = 60000L;
    private static final long CYCLE_TIMEOUT_MS = 15000L;
    private Phase phase = Phase.COOLDOWN;
    private long cooldownUntil;
    private long nextActionAt;
    private long startedAt;
    private long resultDeadline;
    private long pendingCooldownMs = -1L;

    public AuctionRelist() {
        super("AuctionRelist", "Auction Relist", ModuleCategory.MISC);
    }

    @Override
    public void activate() {
        this.phase = Phase.COOLDOWN;
        this.cooldownUntil = System.currentTimeMillis();
        this.pendingCooldownMs = -1L;
    }

    @Override
    public void deactivate() {
        this.phase = Phase.COOLDOWN;
        this.pendingCooldownMs = -1L;
    }

    public boolean isBusy() {
        return this.phase != Phase.COOLDOWN;
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (AuctionRelist.mc.field_1724 == null || AuctionRelist.mc.field_1724.field_3944 == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (this.phase != Phase.COOLDOWN && this.pendingCooldownMs >= 0L) {
            this.closeScreen();
            this.scheduleCooldown(now, this.pendingCooldownMs);
            return;
        }
        if (this.phase != Phase.COOLDOWN && now - this.startedAt > 15000L) {
            this.closeScreen();
            this.scheduleCooldown(now, 5000L);
            return;
        }
        switch (this.phase.ordinal()) {
            case 0: {
                if (now < this.cooldownUntil) break;
                AutoSell autoSell = AutoSell.getInstance();
                AuctionHelper helper = AuctionHelper.getInstance();
                if (autoSell != null && autoSell.isRelistingAuction() || helper != null && helper.isAutoSellActive()) {
                    return;
                }
                AuctionRelist.mc.field_1724.field_3944.method_45730("ah");
                this.phase = Phase.OPEN_AUCTION;
                this.startedAt = now;
                this.nextActionAt = now + 500L;
                break;
            }
            case 1: {
                class_437 helper;
                if (now < this.nextActionAt || !((helper = AuctionRelist.mc.field_1755) instanceof class_465)) {
                    return;
                }
                class_465 screen = (class_465)helper;
                class_1735 storage = this.findSlotByName(screen, "\u0445\u0440\u0430\u043d\u0438\u043b\u0438\u0449", "storage");
                if (storage == null) break;
                this.clickSlot(screen, storage.field_7874);
                this.phase = Phase.OPEN_STORAGE;
                this.nextActionAt = now + 500L;
                break;
            }
            case 2: {
                class_437 storage;
                if (now < this.nextActionAt || !((storage = AuctionRelist.mc.field_1755) instanceof class_465)) {
                    return;
                }
                class_465 screen = (class_465)storage;
                class_1735 relist = this.findSlotByName(screen, "\u043f\u0435\u0440\u0435\u0432\u044b\u0441\u0442\u0430\u0432", "relist");
                if (relist == null) break;
                this.clickSlot(screen, relist.field_7874);
                this.phase = Phase.WAIT_RESULT;
                this.pendingCooldownMs = -1L;
                this.resultDeadline = now + 3000L;
                break;
            }
            case 3: {
                if (this.pendingCooldownMs >= 0L) {
                    this.closeScreen();
                    this.scheduleCooldown(now, this.pendingCooldownMs);
                    break;
                }
                if (now <= this.resultDeadline) break;
                this.closeScreen();
                this.scheduleCooldown(now, 60000L);
                break;
            }
        }
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        class_2596<?> class_25962;
        if (this.phase == Phase.COOLDOWN || !((class_25962 = e.getPacket()) instanceof class_7439)) {
            return;
        }
        class_7439 packet = (class_7439)class_25962;
        String message = packet.comp_763().getString().toLowerCase();
        if (message.contains("\u043f\u043e\u0434\u043e\u0436\u0434\u0438\u0442\u0435") && (message.contains("\u0430\u0443\u043a\u0446\u0438\u043e\u043d") || message.contains("/ah"))) {
            int seconds = this.parseSeconds(message);
            if (seconds > 0) {
                this.pendingCooldownMs = (long)seconds * 1000L + 250L;
            }
            return;
        }
        if (this.phase != Phase.WAIT_RESULT) {
            return;
        }
        if (message.contains("\u043f\u0435\u0440\u0435\u0432\u044b\u0441\u0442\u0430\u0432\u043b\u0435\u043d")) {
            this.pendingCooldownMs = 60000L;
            return;
        }
    }

    private int parseSeconds(String message) {
        int idx = message.indexOf("\u043f\u043e\u0434\u043e\u0436\u0434\u0438\u0442\u0435");
        if (idx < 0) {
            return -1;
        }
        StringBuilder digits = new StringBuilder();
        for (int i = idx; i < message.length(); ++i) {
            char c = message.charAt(i);
            if (Character.isDigit(c)) {
                digits.append(c);
                continue;
            }
            if (digits.length() > 0) break;
        }
        if (digits.length() == 0) {
            return -1;
        }
        try {
            return Integer.parseInt(digits.toString());
        }
        catch (NumberFormatException ex) {
            return -1;
        }
    }

    private void scheduleCooldown(long now, long ms) {
        this.phase = Phase.COOLDOWN;
        this.cooldownUntil = now + Math.max(1000L, ms);
        this.pendingCooldownMs = -1L;
    }

    private void closeScreen() {
        if (AuctionRelist.mc.field_1724 != null) {
            AuctionRelist.mc.field_1724.method_7346();
        }
    }

    private void clickSlot(class_465<?> screen, int slotId) {
        if (AuctionRelist.mc.field_1761 == null) {
            return;
        }
        AuctionRelist.mc.field_1761.method_2906(screen.method_17577().field_7763, slotId, 0, class_1713.field_7790, (class_1657)AuctionRelist.mc.field_1724);
    }

    private class_1735 findSlotByName(class_465<?> screen, String ... keywords) {
        int containerSize = Math.max(0, screen.method_17577().field_7761.size() - 36);
        for (int i = 0; i < containerSize; ++i) {
            class_1735 slot = screen.method_17577().method_7611(i);
            if (slot == null || !slot.method_7681()) continue;
            String name = slot.method_7677().method_7964().getString().toLowerCase();
            for (String keyword : keywords) {
                if (!name.contains(keyword)) continue;
                return slot;
            }
        }
        return null;
    }

    private static enum Phase {
        COOLDOWN,
        OPEN_AUCTION,
        OPEN_STORAGE,
        WAIT_RESULT;

    }
}

