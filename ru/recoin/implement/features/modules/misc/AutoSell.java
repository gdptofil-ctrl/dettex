/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1657
 *  net.minecraft.class_1713
 *  net.minecraft.class_1735
 *  net.minecraft.class_1792$class_9635
 *  net.minecraft.class_1799
 *  net.minecraft.class_1836
 *  net.minecraft.class_437
 *  net.minecraft.class_465
 */
package ru.recoin.implement.features.modules.misc;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1836;
import net.minecraft.class_437;
import net.minecraft.class_465;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.TextSetting;
import ru.recoin.common.util.auction.AuctionListingUtil;
import ru.recoin.common.util.auction.AuctionPriceParser;
import ru.recoin.common.util.other.BaritoneCompat;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.modules.misc.AuctionRelist;
import ru.recoin.implement.features.modules.render.AuctionHelper;

public final class AutoSell
extends Module {
    private final SelectSetting source = new SelectSetting("\u0418\u0441\u0442\u043e\u0447\u043d\u0438\u043a", "\u041e\u0442\u043a\u0443\u0434\u0430 \u0431\u0440\u0430\u0442\u044c \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b \u0434\u043b\u044f \u043f\u0440\u043e\u0434\u0430\u0436\u0438").value("\u0421\u043e \u0432\u0441\u0435\u0445 \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440\u043e\u0432", "\u0421 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044f").selected("\u0421\u043e \u0432\u0441\u0435\u0445 \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440\u043e\u0432");
    private final TextSetting resourceAnarchy = new TextSetting("\u0410\u043d\u0430\u0440\u0445\u0438\u044f \u0440\u0435\u0441\u0443\u0440\u0441\u043e\u0432", "\u0410\u043d\u0430\u0440\u0445\u0438\u044f, \u043d\u0430 \u043a\u043e\u0442\u043e\u0440\u043e\u0439 AutoSell \u0437\u0430\u0431\u0438\u0440\u0430\u0435\u0442 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b \u0438\u0437 \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440\u043e\u0432").setText("314").setMax(6).visible(() -> this.source.isSelected("\u0421\u043e \u0432\u0441\u0435\u0445 \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440\u043e\u0432"));
    private final TextSetting sellAnarchy = new TextSetting("\u0410\u043d\u0430\u0440\u0445\u0438\u044f \u043f\u0440\u043e\u0434\u0430\u0436\u0438", "\u0410\u043d\u0430\u0440\u0445\u0438\u044f, \u043d\u0430 \u043a\u043e\u0442\u043e\u0440\u043e\u0439 AutoSell \u0432\u044b\u0441\u0442\u0430\u0432\u043b\u044f\u0435\u0442 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b \u043d\u0430 \u0430\u0443\u043a\u0446\u0438\u043e\u043d").setText("314").setMax(6);
    private final TextSetting trashAnarchy = new TextSetting("\u0410\u043d\u0430\u0440\u0445\u0438\u044f \u043c\u0443\u0441\u043e\u0440\u0430", "\u0410\u043d\u0430\u0440\u0445\u0438\u044f \u0441 \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440\u043e\u043c \u0432\u043e\u0437\u043b\u0435 \u0442\u0430\u0431\u043b\u0438\u0447\u043a\u0438 \u00ab\u041c\u0443\u0441\u043e\u0440\u00bb").setText("314").setMax(6);
    private final BooleanSetting relistUnsold = new BooleanSetting("\u041f\u0435\u0440\u0435\u0432\u044b\u0441\u0442\u0430\u0432\u043b\u044f\u0442\u044c", "\u041f\u043e\u0432\u0442\u043e\u0440\u043d\u0430\u044f \u043f\u0440\u043e\u0434\u0430\u0436\u0430 \u043d\u0435\u043f\u0440\u043e\u0434\u0430\u043d\u043d\u044b\u0445 \u043b\u043e\u0442\u043e\u0432").setValue(true);
    private boolean enabledAuctionHelper;
    private boolean started;
    private boolean startPending;
    private long nextRoamTime;
    private RelistPhase relistPhase = RelistPhase.IDLE;
    private long nextRelistAt;
    private long relistActionAt;
    private long relistStartedAt;
    private int relistBaselineCount;
    private class_1799 relistTarget = class_1799.field_8037;
    private final Set<Integer> attemptedSlots = new HashSet<Integer>();

    public AutoSell() {
        super("AutoSell", "Auto Sell", ModuleCategory.MISC);
        this.setup(this.source, this.resourceAnarchy, this.sellAnarchy, this.trashAnarchy, this.relistUnsold);
    }

    public static AutoSell getInstance() {
        return Instance.get(AutoSell.class);
    }

    @Override
    public void activate() {
        AuctionRelist relist;
        AuctionHelper helper = AuctionHelper.getInstance();
        if (helper == null) {
            return;
        }
        boolean bl = this.enabledAuctionHelper = !helper.isState();
        if (this.enabledAuctionHelper) {
            helper.setState(true);
        }
        boolean bl2 = this.startPending = (relist = Instance.get(AuctionRelist.class)) != null && relist.isState() && relist.isBusy();
        if (!this.startPending) {
            this.startSelling(helper);
        }
        this.relistPhase = RelistPhase.IDLE;
        this.nextRelistAt = 0L;
        this.nextRoamTime = System.currentTimeMillis() + 20000L + (long)(Math.random() * 15000.0);
    }

    @Override
    public void deactivate() {
        AuctionHelper helper = AuctionHelper.getInstance();
        if (helper != null && helper.isAutoSellActive()) {
            helper.stopAutoSell("\u00a7c[AutoSell] \u041e\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u043e");
        }
        if (helper != null && this.enabledAuctionHelper && helper.isState()) {
            helper.setState(false);
        }
        this.enabledAuctionHelper = false;
        this.started = false;
        this.startPending = false;
        this.relistPhase = RelistPhase.IDLE;
        this.nextRelistAt = 0L;
        this.attemptedSlots.clear();
        Object baritone = BaritoneCompat.getPrimaryBaritone();
        if (baritone != null) {
            BaritoneCompat.cancelPathing(baritone);
        }
        this.nextRoamTime = 0L;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        AuctionHelper helper = AuctionHelper.getInstance();
        long now = System.currentTimeMillis();
        if (helper == null) {
            this.setState(false);
            return;
        }
        if (this.startPending) {
            AuctionRelist relist = Instance.get(AuctionRelist.class);
            if (relist != null && relist.isState() && relist.isBusy()) {
                return;
            }
            this.startPending = false;
            this.startSelling(helper);
        }
        if (!this.started && !helper.isAutoSellActive() && this.nextRelistAt == 0L && this.relistPhase == RelistPhase.IDLE) {
            this.setState(false);
            return;
        }
        if (this.started && !helper.isAutoSellActive()) {
            this.started = false;
            if (this.relistUnsold.isValue()) {
                this.scheduleRelist(now);
            } else {
                this.setState(false);
                return;
            }
        }
        if (!this.started && !helper.isAutoSellActive() && this.relistUnsold.isValue()) {
            this.processRelist(helper, now);
        }
        if (AutoSell.mc.field_1724 != null && AutoSell.mc.field_1755 == null && !helper.isAutoSellActive() && this.relistPhase == RelistPhase.IDLE) {
            if (this.nextRoamTime == 0L) {
                this.nextRoamTime = now + 25000L + (long)(Math.random() * 20000.0);
            } else if (now >= this.nextRoamTime) {
                Object baritone = BaritoneCompat.getPrimaryBaritone();
                if (baritone != null) {
                    int dx = (int)(Math.random() * 5.0) - 2;
                    int dz = (int)(Math.random() * 5.0) - 2;
                    if (dx != 0 || dz != 0) {
                        BaritoneCompat.goNear(baritone, AutoSell.mc.field_1724.method_24515().method_10069(dx, 0, dz), 1);
                    }
                }
                this.nextRoamTime = now + 30000L + (long)(Math.random() * 25000.0);
            }
        }
    }

    public boolean isRelistingAuction() {
        return this.relistPhase != RelistPhase.IDLE;
    }

    private void startSelling(AuctionHelper helper) {
        helper.startAutoSell(this.source.isSelected("\u0421\u043e \u0432\u0441\u0435\u0445 \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440\u043e\u0432"), this.parseAnarchy(this.resourceAnarchy), this.parseAnarchy(this.sellAnarchy), this.parseAnarchy(this.trashAnarchy));
        this.started = helper.isAutoSellActive();
    }

    private void scheduleRelist(long now) {
        this.relistPhase = RelistPhase.IDLE;
        this.relistTarget = class_1799.field_8037;
        this.attemptedSlots.clear();
        this.nextRelistAt = now + 300000L + ThreadLocalRandom.current().nextLong(60001L);
    }

    private void processRelist(AuctionHelper helper, long now) {
        class_437 sale2;
        if (AutoSell.mc.field_1724 == null || AutoSell.mc.field_1724.field_3944 == null || AutoSell.mc.field_1761 == null) {
            return;
        }
        if (this.relistPhase == RelistPhase.IDLE) {
            if (this.nextRelistAt == 0L || now < this.nextRelistAt || AutoSell.mc.field_1755 != null) {
                return;
            }
            AuctionRelist relist = Instance.get(AuctionRelist.class);
            if (relist != null && relist.isState() && relist.isBusy()) {
                return;
            }
            int anarchy = this.parseAnarchy(this.sellAnarchy);
            if (anarchy <= 0) {
                return;
            }
            this.attemptedSlots.clear();
            this.relistStartedAt = now;
            if (!helper.isCurrentAnarchy(anarchy)) {
                AutoSell.mc.field_1724.field_3944.method_45730("an" + anarchy);
                this.relistPhase = RelistPhase.SWITCH_ANARCHY;
                this.relistActionAt = now + 10000L;
            } else {
                AutoSell.mc.field_1724.field_3944.method_45730("ah");
                this.relistPhase = RelistPhase.OPEN_AUCTION;
                this.relistActionAt = now + 500L;
            }
            return;
        }
        if (now - this.relistStartedAt > 30000L) {
            this.closeRelistScreen();
            this.scheduleRelist(now);
            return;
        }
        if (this.relistPhase == RelistPhase.SWITCH_ANARCHY) {
            if (now < this.relistActionAt || !helper.isCurrentAnarchy(this.parseAnarchy(this.sellAnarchy))) {
                return;
            }
            AutoSell.mc.field_1724.field_3944.method_45730("ah");
            this.relistPhase = RelistPhase.OPEN_AUCTION;
            this.relistActionAt = now + 500L;
            return;
        }
        if (this.relistPhase == RelistPhase.WAIT_ITEM) {
            class_465 screen;
            sale2 = AutoSell.mc.field_1755;
            if (sale2 instanceof class_465 && !(screen = (class_465)sale2).method_17577().method_34255().method_7960()) {
                for (class_1735 slot : screen.method_17577().field_7761) {
                    if (slot.field_7871 != AutoSell.mc.field_1724.method_31548() || slot.method_7681()) continue;
                    AutoSell.mc.field_1761.method_2906(screen.method_17577().field_7763, slot.field_7874, 0, class_1713.field_7790, (class_1657)AutoSell.mc.field_1724);
                    break;
                }
                this.relistActionAt = now + 1000L;
                return;
            }
            if (this.countInInventory(this.relistTarget) > this.relistBaselineCount) {
                this.closeRelistScreen();
                helper.startAutoSellRelist(this.relistTarget, this.parseAnarchy(this.sellAnarchy), this.parseAnarchy(this.trashAnarchy));
                this.started = helper.isAutoSellActive();
                this.relistPhase = RelistPhase.IDLE;
                this.relistTarget = class_1799.field_8037;
                this.nextRelistAt = 0L;
                if (!this.started) {
                    this.scheduleRelist(now);
                }
            } else if (now >= this.relistActionAt) {
                this.relistPhase = RelistPhase.OPEN_STORAGE;
                this.relistActionAt = now;
            }
            return;
        }
        if (now < this.relistActionAt || !((sale2 = AutoSell.mc.field_1755) instanceof class_465)) {
            return;
        }
        class_465 screen = (class_465)sale2;
        String title = screen.method_25440().getString().toLowerCase(Locale.ROOT);
        if (this.relistPhase == RelistPhase.OPEN_AUCTION) {
            if (!title.contains("\u0430\u0443\u043a\u0446\u0438\u043e\u043d") && !title.contains("auction")) {
                return;
            }
            class_1735 storage = this.findNamedSlot(screen, "\u0445\u0440\u0430\u043d\u0438\u043b\u0438\u0449", "storage");
            if (storage == null) {
                return;
            }
            AutoSell.mc.field_1761.method_2906(screen.method_17577().field_7763, storage.field_7874, 0, class_1713.field_7790, (class_1657)AutoSell.mc.field_1724);
            this.relistPhase = RelistPhase.OPEN_STORAGE;
            this.relistActionAt = now + 500L;
            return;
        }
        if (!title.contains("\u0445\u0440\u0430\u043d\u0438\u043b\u0438\u0449") && !title.contains("storage")) {
            return;
        }
        class_1735 listing = this.findRelistListing(screen);
        if (listing == null) {
            this.closeRelistScreen();
            this.scheduleRelist(now);
            return;
        }
        this.relistTarget = listing.method_7677().method_7972();
        this.relistBaselineCount = this.countInInventory(this.relistTarget);
        this.attemptedSlots.add(listing.field_7874);
        AutoSell.mc.field_1761.method_2906(screen.method_17577().field_7763, listing.field_7874, 0, class_1713.field_7790, (class_1657)AutoSell.mc.field_1724);
        this.relistPhase = RelistPhase.WAIT_ITEM;
        this.relistActionAt = now + 3000L;
    }

    private class_1735 findNamedSlot(class_465<?> screen, String ... keywords) {
        int count = Math.max(0, screen.method_17577().field_7761.size() - 36);
        for (int i = 0; i < count; ++i) {
            class_1735 slot = screen.method_17577().method_7611(i);
            if (slot == null || !slot.method_7681()) continue;
            String name = slot.method_7677().method_7964().getString().toLowerCase(Locale.ROOT);
            for (String keyword : keywords) {
                if (!name.contains(keyword)) continue;
                return slot;
            }
        }
        return null;
    }

    private class_1735 findRelistListing(class_465<?> screen) {
        int count = Math.max(0, screen.method_17577().field_7761.size() - 36);
        for (int i = 0; i < count; ++i) {
            List tooltip;
            class_1799 stack;
            class_1735 slot = screen.method_17577().method_7611(i);
            if (slot == null || !slot.method_7681() || this.attemptedSlots.contains(slot.field_7874) || AuctionListingUtil.isUnavailable(stack = slot.method_7677(), tooltip = stack.method_7950(class_1792.class_9635.field_51353, (class_1657)AutoSell.mc.field_1724, (class_1836)class_1836.field_41070)) || AuctionPriceParser.getTooltipTotal(tooltip) <= 0L) continue;
            return slot;
        }
        return null;
    }

    private int countInInventory(class_1799 target) {
        if (target.method_7960() || AutoSell.mc.field_1724 == null) {
            return 0;
        }
        int total = 0;
        for (int i = 0; i < 36; ++i) {
            class_1799 stack = AutoSell.mc.field_1724.method_31548().method_5438(i);
            if (stack.method_7909() != target.method_7909() || !stack.method_7964().getString().equals(target.method_7964().getString())) continue;
            total += stack.method_7947();
        }
        return total;
    }

    private void closeRelistScreen() {
        if (AutoSell.mc.field_1724 != null && AutoSell.mc.field_1755 instanceof class_465) {
            AutoSell.mc.field_1724.method_7346();
        }
    }

    private int parseAnarchy(TextSetting setting) {
        String digits;
        String string = digits = setting.getText() == null ? "" : setting.getText().replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(digits);
        }
        catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private static enum RelistPhase {
        IDLE,
        SWITCH_ANARCHY,
        OPEN_AUCTION,
        OPEN_STORAGE,
        WAIT_ITEM;

    }
}

