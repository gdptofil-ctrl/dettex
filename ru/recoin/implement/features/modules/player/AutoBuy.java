/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1657
 *  net.minecraft.class_1713
 *  net.minecraft.class_1735
 *  net.minecraft.class_1792
 *  net.minecraft.class_1792$class_9635
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1836
 *  net.minecraft.class_1937
 *  net.minecraft.class_2561
 *  net.minecraft.class_2596
 *  net.minecraft.class_437
 *  net.minecraft.class_465
 *  net.minecraft.class_7439
 */
package ru.recoin.implement.features.modules.player;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1836;
import net.minecraft.class_1937;
import net.minecraft.class_2561;
import net.minecraft.class_2596;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_7439;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.QuickLogger;
import ru.recoin.common.util.auction.AuctionListingUtil;
import ru.recoin.common.util.auction.AuctionPriceParser;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.world.ServerUtil;
import ru.recoin.implement.events.keyboard.KeyEvent;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.DrawEvent;
import ru.recoin.implement.features.modules.player.FunTimeCatalog;
import ru.recoin.implement.features.modules.player.FunTimeItemMatcher;
import ru.recoin.implement.features.modules.render.AuctionHelper;

public final class AutoBuy
extends Module {
    private static final Pattern PRICE_LINE = Pattern.compile("(?:\u0446\u0435\u043d\u0430|\u0441\u0442\u043e\u0438\u043c\u043e\u0441\u0442\u044c|price|cost|\u0437\u0430|[$\u20bd])", 66);
    private static final Pattern COMPACT_PRICE = Pattern.compile("([0-9]+(?:[.,][0-9]+)?)\\s*([k\u043am\u043c])", 66);
    private static final Pattern PLAIN_PRICE = Pattern.compile("([0-9][0-9\\s.,]{0,18})");
    private static final Pattern PURCHASE_PRICE = Pattern.compile("(?:\u0437\u0430|for)\\s*[$\u20bd]?\\s*([0-9][0-9\\s.,]*)", 66);
    private static final long PURCHASE_TIMEOUT = 10000L;
    private static final long PARSE_RESULT_TIMEOUT = 2000L;
    private static final long PARSE_SCREEN_TIMEOUT = 4000L;
    private static final List<CatalogItem> FUNTIME = FunTimeCatalog.ORDERED_ITEMS;
    private static final List<CatalogItem> SPOOKY = FUNTIME.stream().map(item -> switch (item.name()) {
        case "\u042f\u0432\u043d\u0430\u044f \u043f\u044b\u043b\u044c" -> AutoBuy.ci(item.name(), class_1802.field_8601);
        case "\u0414\u0435\u0437\u043e\u0440\u0438\u0435\u043d\u0442\u0430\u0446\u0438\u044f" -> AutoBuy.ci(item.name(), class_1802.field_8574);
        case "\u0422\u0440\u0430\u043f\u043a\u0430" -> AutoBuy.ci(item.name(), class_1802.field_8882);
        default -> item;
    }).toList();
    private static final Set<String> SPOOKY_NAMES = FunTimeCatalog.BY_NAME.keySet();
    public final SelectSetting serverMode = new SelectSetting("\u0420\u0435\u0436\u0438\u043c \u0441\u0435\u0440\u0432\u0435\u0440\u0430", "Auction parser").value("FunTime", "SpookyTime").selected("FunTime");
    public final BooleanSetting autoReparse = new BooleanSetting("Auto ReParse", "\u041f\u043e\u0432\u0442\u043e\u0440\u043d\u044b\u0439 \u043f\u0430\u0440\u0441\u0438\u043d\u0433 \u0446\u0435\u043d").setValue(false);
    public final ValueSetting reparseEvery = new ValueSetting("ReParse Every (min)", "\u0418\u043d\u0442\u0435\u0440\u0432\u0430\u043b \u043f\u0430\u0440\u0441\u0438\u043d\u0433\u0430").setValue(30.0f).range(5, 240).step(5.0f).visible(this.autoReparse::isValue);
    public final ValueSetting parserDiscount = new ValueSetting("Parser Discount (%)", "\u041f\u0440\u043e\u0446\u0435\u043d\u0442 \u043d\u0438\u0436\u0435 \u043c\u0438\u043d\u0438\u043c\u0430\u043b\u044c\u043d\u043e\u0439 \u0446\u0435\u043d\u044b").setValue(20.0f).range(0, 100).step(1.0f).visible(() -> false);
    public final ValueSetting buyCooldown = new ValueSetting("Buy Cooldown (ms)", "\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430 \u043f\u043e\u043a\u0443\u043f\u043a\u0438").setValue(100.0f).range(100, 5000).step(50.0f);
    public final ValueSetting confirmCooldown = new ValueSetting("Confirm Cooldown (ms)", "\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430 \u043f\u043e\u0434\u0442\u0432\u0435\u0440\u0436\u0434\u0435\u043d\u0438\u044f").setValue(50.0f).range(0, 1000).step(10.0f);
    public final BooleanSetting detectAuctionSlowdown = new BooleanSetting("Detect Auction Slowdown", "\u0417\u0430\u043c\u0435\u0440 \u043b\u0430\u0433\u043e\u0432 \u0430\u0443\u043a\u0446\u0438\u043e\u043d\u0430").setValue(true);
    public final BooleanSetting autoFixSlowdown = new BooleanSetting("Auto Fix Slowdown", "\u041f\u0435\u0440\u0435\u043e\u0442\u043a\u0440\u044b\u0442\u044c \u0437\u0430\u0432\u0438\u0441\u0448\u0438\u0439 \u0430\u0443\u043a\u0446\u0438\u043e\u043d").setValue(true).visible(this.detectAuctionSlowdown::isValue);
    public final BooleanSetting lagStatsInChat = new BooleanSetting("Lag Statistics In Chat", "\u0421\u0442\u0430\u0442\u0438\u0441\u0442\u0438\u043a\u0430 \u0437\u0430\u0434\u0435\u0440\u0436\u0435\u043a").setValue(true).visible(this.detectAuctionSlowdown::isValue);
    private final LinkedHashMap<String, Long> prices = new LinkedHashMap();
    private final Deque<Purchase> purchaseHistory = new ArrayDeque<Purchase>();
    private final Deque<PurchaseAttempt> purchaseAttempts = new ArrayDeque<PurchaseAttempt>();
    private Candidate pendingPurchase;
    private long pendingPurchaseAt;
    private long lastRejectedConfirmNotice;
    private long nextAction;
    private long lastParseReset;
    private long nextAnarchySwap;
    private long auctionReopenAt;
    private boolean auctionEnabled;
    private final Deque<String> parseQueue = new ArrayDeque<String>();
    private String parseItemName;
    private long parseSearchSentAt;
    private long nextParseCommandAt;
    private int parsePreviousSyncId;
    private int parseUpdatedCount;
    private int parseSkippedCount;
    private boolean parseActive;
    private long shakeUpdatedAt;
    private long nextShakeTargetAt;
    private float shakeYaw;
    private float shakePitch;
    private float shakeTargetYaw;
    private float shakeTargetPitch;
    private float shakeSpeed;
    private float appliedShakeYaw;
    private float appliedShakePitch;
    private long refreshSentAt;
    private long refreshSignature;
    private long lagTotal;
    private long lagMaximum;
    private int lagSamples;
    private long nextLagReportAt;

    public AutoBuy() {
        super("AutoBuy", "Auto Buy", ModuleCategory.MISC);
        this.setup(this.serverMode, this.autoReparse, this.reparseEvery, this.parserDiscount, this.buyCooldown, this.confirmCooldown, this.detectAuctionSlowdown, this.autoFixSlowdown, this.lagStatsInChat);
    }

    public static AutoBuy getInstance() {
        return Instance.get(AutoBuy.class);
    }

    @Override
    public void activate() {
        long now;
        this.auctionEnabled = false;
        this.pendingPurchase = null;
        this.purchaseAttempts.clear();
        this.cancelPriceParse();
        this.lastParseReset = now = System.currentTimeMillis();
        this.scheduleNextAnarchySwap(now);
        this.resetSlowdownTracking(now);
        super.activate();
    }

    @Override
    public void deactivate() {
        this.auctionEnabled = false;
        this.pendingPurchase = null;
        this.purchaseAttempts.clear();
        this.cancelPriceParse();
        this.resetCameraShake();
        super.deactivate();
    }

    @EventHandler
    public void onParseCancelKey(KeyEvent event) {
        if (this.parseActive && event.isKeyDown(256, true)) {
            this.cancelPriceParse();
            this.sendParseMessage("\u041f\u0430\u0440\u0441\u0438\u043d\u0433 \u043e\u0442\u043c\u0435\u043d\u0451\u043d");
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        class_465 screen;
        long now;
        block24: {
            block23: {
                class_465 current;
                class_437 class_4372;
                if (AutoBuy.mc.field_1724 == null || AutoBuy.mc.field_1761 == null) {
                    return;
                }
                now = System.currentTimeMillis();
                this.purchaseAttempts.removeIf(attempt -> now - attempt.clickedAt > 10000L);
                if (this.autoReparse.isValue() && !this.parseActive && now - this.lastParseReset >= (long)(this.reparseEvery.getValue() * 60000.0f) && (class_4372 = AutoBuy.mc.field_1755) instanceof class_465 && this.isAuctionScreen(current = (class_465)class_4372)) {
                    this.startPriceParse(now);
                }
                if (this.parseActive) {
                    this.tickPriceParse(now);
                    return;
                }
                if (this.auctionEnabled && this.serverMode.isSelected("FunTime") && now >= this.nextAnarchySwap) {
                    this.swapToNextAnarchy();
                    this.scheduleNextAnarchySwap(now);
                    this.auctionReopenAt = now + 11000L;
                }
                if (this.auctionEnabled && this.auctionReopenAt != 0L && now >= this.auctionReopenAt) {
                    this.auctionReopenAt = 0L;
                    if (AutoBuy.mc.field_1755 != null) {
                        AutoBuy.mc.field_1724.method_7346();
                    }
                    AutoBuy.mc.field_1724.field_3944.method_45730("ah");
                    this.nextAction = now + 1000L;
                    return;
                }
                class_4372 = AutoBuy.mc.field_1755;
                if (!(class_4372 instanceof class_465)) break block23;
                screen = (class_465)class_4372;
                if (now >= this.nextAction) break block24;
            }
            return;
        }
        String title = AutoBuy.normalize(screen.method_25440().getString());
        if (this.isConfirm(title)) {
            boolean allowed;
            if (!this.auctionEnabled) {
                return;
            }
            class_1735 confirm = this.findConfirm(screen);
            boolean bl = allowed = confirm != null && this.pendingPurchase != null && now - this.pendingPurchaseAt < 5000L && this.confirmPriceAllowed(screen, this.pendingPurchase);
            if (allowed) {
                this.click(screen, confirm);
            } else {
                this.purchaseAttempts.removeIf(attempt -> attempt.candidate == this.pendingPurchase);
                AutoBuy.mc.field_1724.method_7346();
                this.auctionReopenAt = now + 300L;
                if (now - this.lastRejectedConfirmNotice > 5000L) {
                    this.sendParseMessage("\u041f\u043e\u043a\u0443\u043f\u043a\u0430 \u043e\u0442\u043c\u0435\u043d\u0435\u043d\u0430: \u0446\u0435\u043d\u0443 \u043f\u043e\u0434\u0442\u0432\u0435\u0440\u0436\u0434\u0435\u043d\u0438\u044f \u043d\u0435\u043b\u044c\u0437\u044f \u0431\u0435\u0437\u043e\u043f\u0430\u0441\u043d\u043e \u043f\u0440\u043e\u0432\u0435\u0440\u0438\u0442\u044c");
                    this.lastRejectedConfirmNotice = now;
                }
            }
            this.pendingPurchase = null;
            this.nextAction = now + (long)this.confirmCooldown.getInt();
            return;
        }
        if (!this.isAuction(title)) {
            return;
        }
        if (this.trackAuctionSlowdown(screen, now)) {
            return;
        }
        Candidate best = null;
        long balance = this.auctionEnabled ? AuctionHelper.readScoreboardBalance() : Long.MAX_VALUE;
        int containerSlots = Math.max(0, screen.method_17577().field_7761.size() - 36);
        for (int i = 0; i < containerSlots; ++i) {
            long totalPrice;
            List tooltip;
            class_1799 stack;
            class_1735 slot = screen.method_17577().method_7611(i);
            if (!slot.method_7681() || AuctionListingUtil.isUnavailable(stack = slot.method_7677(), tooltip = stack.method_7950(class_1792.class_9635.field_51353, (class_1657)AutoBuy.mc.field_1724, (class_1836)class_1836.field_41070))) continue;
            long l = totalPrice = this.serverMode.isSelected("FunTime") ? AuctionPriceParser.getFunTimeTooltipTotal(tooltip) : AuctionPriceParser.getTooltipTotal(tooltip);
            if (totalPrice <= 0L) continue;
            for (Map.Entry<String, Long> wanted : this.prices.entrySet()) {
                CatalogItem item = this.catalogItem(wanted.getKey());
                if (item == null || !this.matchesListing(item, stack, tooltip) || totalPrice > wanted.getValue() * (long)Math.max(1, stack.method_7947()) || balance != Long.MAX_VALUE && totalPrice > balance || best != null && totalPrice >= best.price) continue;
                best = new Candidate(slot, totalPrice, wanted.getKey(), stack.method_7972());
            }
        }
        if (!this.auctionEnabled) {
            return;
        }
        if (best != null) {
            this.pendingPurchase = best;
            this.pendingPurchaseAt = now;
            this.purchaseAttempts.addLast(new PurchaseAttempt(best, now));
            while (this.purchaseAttempts.size() > 16) {
                this.purchaseAttempts.removeFirst();
            }
            AutoBuy.mc.field_1761.method_2906(screen.method_17577().field_7763, best.slot.field_7874, 0, class_1713.field_7794, (class_1657)AutoBuy.mc.field_1724);
            this.nextAction = now + (long)this.buyCooldown.getInt();
            return;
        }
        class_1735 refresh = this.findRefreshButton(screen);
        if (refresh != null) {
            this.click(screen, refresh);
            if (this.detectAuctionSlowdown.isValue()) {
                this.refreshSentAt = now;
                this.refreshSignature = this.auctionSignature(screen);
            }
        }
        this.nextAction = now + (long)this.refreshDelay();
    }

    public List<CatalogItem> catalog() {
        return this.serverMode.isSelected("SpookyTime") ? SPOOKY : FUNTIME;
    }

    public Map<String, Long> selectedPrices() {
        LinkedHashMap<String, Long> visible = new LinkedHashMap<String, Long>();
        for (Map.Entry<String, Long> entry : this.prices.entrySet()) {
            if (!(this.serverMode.isSelected("FunTime") ? FunTimeCatalog.BY_NAME.containsKey(entry.getKey()) : SPOOKY_NAMES.contains(entry.getKey()))) continue;
            visible.put(entry.getKey(), entry.getValue());
        }
        return Collections.unmodifiableMap(visible);
    }

    public boolean selected(String name) {
        return this.prices.containsKey(name);
    }

    public void toggle(CatalogItem item) {
        if (this.prices.containsKey(item.name)) {
            this.prices.remove(item.name);
        } else {
            this.prices.put(item.name, 500000L);
        }
    }

    public void changePrice(String name, long delta) {
        this.prices.computeIfPresent(name, (key, value) -> Math.max(1L, Math.min(1000000000L, value + delta)));
    }

    public void setPrice(CatalogItem item, long value) {
        this.prices.put(item.name(), Math.max(1L, Math.min(1000000000L, value)));
    }

    public void searchOnAuction(CatalogItem item) {
        if (AutoBuy.mc.field_1724 == null || AutoBuy.mc.field_1724.field_3944 == null) {
            return;
        }
        mc.method_1507(null);
        AutoBuy.mc.field_1724.field_3944.method_45730("ah search " + item.name());
    }

    public boolean isAuctionEnabled() {
        return this.auctionEnabled;
    }

    public List<Purchase> purchaseHistory() {
        return List.copyOf(this.purchaseHistory);
    }

    public void toggleAuctionEnabled() {
        long now;
        this.auctionEnabled = !this.auctionEnabled;
        this.nextAction = now = System.currentTimeMillis();
        if (this.auctionEnabled) {
            this.scheduleNextAnarchySwap(now);
        } else {
            this.auctionReopenAt = 0L;
            this.resetCameraShake();
        }
    }

    public void requestPriceParse() {
        class_465 screen;
        class_437 class_4372 = AutoBuy.mc.field_1755;
        if (!(class_4372 instanceof class_465) || !this.isAuctionScreen(screen = (class_465)class_4372)) {
            this.sendParseMessage("\u041e\u0442\u043a\u0440\u043e\u0439 \u0430\u0443\u043a\u0446\u0438\u043e\u043d \u043f\u0435\u0440\u0435\u0434 \u0437\u0430\u043f\u0443\u0441\u043a\u043e\u043c \u043f\u0430\u0440\u0441\u0435\u0440\u0430");
            return;
        }
        if (this.parseActive) {
            this.sendParseMessage("\u041f\u0430\u0440\u0441\u0435\u0440 \u0443\u0436\u0435 \u0438\u0449\u0435\u0442 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b");
            return;
        }
        this.startPriceParse(System.currentTimeMillis());
    }

    private void startPriceParse(long now) {
        this.parseQueue.clear();
        this.selectedPrices().keySet().forEach(this.parseQueue::addLast);
        if (this.parseQueue.isEmpty()) {
            this.sendParseMessage("\u0421\u043d\u0430\u0447\u0430\u043b\u0430 \u0432\u044b\u0431\u0435\u0440\u0438 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b \u0432 AutoBuy");
            this.lastParseReset = now;
            return;
        }
        this.parseActive = true;
        this.parseItemName = null;
        this.parseSearchSentAt = 0L;
        this.nextParseCommandAt = now;
        this.parsePreviousSyncId = -1;
        this.parseSkippedCount = 0;
        this.parseUpdatedCount = 0;
        this.lastParseReset = now;
        this.sendParseMessage("\u041f\u043e\u0438\u0441\u043a \u0446\u0435\u043d \u0434\u043b\u044f " + this.parseQueue.size() + " \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432 \u0437\u0430\u043f\u0443\u0449\u0435\u043d");
    }

    private void tickPriceParse(long now) {
        if (AutoBuy.mc.field_1724 == null || AutoBuy.mc.field_1724.field_3944 == null) {
            this.cancelPriceParse();
            return;
        }
        if (AutoBuy.mc.field_1755 != null && !(AutoBuy.mc.field_1755 instanceof class_465)) {
            this.cancelPriceParse();
            this.sendParseMessage("\u041f\u0430\u0440\u0441\u0438\u043d\u0433 \u043e\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d: \u043e\u0442\u043a\u0440\u044b\u0442\u043e \u0434\u0440\u0443\u0433\u043e\u0435 \u043c\u0435\u043d\u044e");
            return;
        }
        if (this.parseItemName == null) {
            if (now < this.nextParseCommandAt) {
                return;
            }
            if (this.parseQueue.isEmpty()) {
                this.finishPriceParse(now);
                return;
            }
            this.parseItemName = this.parseQueue.removeFirst();
            class_437 class_4372 = AutoBuy.mc.field_1755;
            int v0;
            if (class_4372 instanceof class_465) {
                class_465 screen = (class_465)class_4372;
                v0 = screen.method_17577().field_7763;
            } else {
                v0 = this.parsePreviousSyncId = -1;
            }
            if (AutoBuy.mc.field_1755 instanceof class_465) {
                AutoBuy.mc.field_1724.method_7346();
            }
            AutoBuy.mc.field_1724.field_3944.method_45730("ah search " + this.parseItemName);
            this.parseSearchSentAt = now;
            this.nextParseCommandAt = now + 1500L;
            return;
        }
        class_437 class_4373 = AutoBuy.mc.field_1755;
        if (class_4373 instanceof class_465) {
            class_465 screen = (class_465)class_4373;
            if (screen.method_17577().field_7763 != this.parsePreviousSyncId && now - this.parseSearchSentAt >= 800L) {
                long marketPrice = this.parseAuctionPrice(screen, this.parseItemName);
                if (marketPrice > 0L) {
                    double multiplier = 1.0 - (double)this.getParserDiscount() / 100.0;
                    this.prices.put(this.parseItemName, Math.max(1L, Math.min(1000000000L, Math.round((double)marketPrice * multiplier))));
                    ++this.parseUpdatedCount;
                    this.parseItemName = null;
                    this.nextParseCommandAt = Math.max(this.nextParseCommandAt, now + 500L);
                    return;
                }
                if (now - this.parseSearchSentAt >= 2000L) {
                    this.skipParseItem(now);
                    return;
                }
            }
        }
        if (now - this.parseSearchSentAt >= 4000L) {
            this.skipParseItem(now);
        }
    }

    private void skipParseItem(long now) {
        ++this.parseSkippedCount;
        this.parseItemName = null;
        this.nextParseCommandAt = Math.max(this.nextParseCommandAt, now + 500L);
    }

    private long parseAuctionPrice(class_465<?> screen, String name) {
        CatalogItem wanted;
        boolean funTime = this.serverMode.isSelected("FunTime");
        CatalogItem catalogItem = wanted = funTime ? FunTimeCatalog.BY_NAME.get(name) : (CatalogItem)SPOOKY.stream().filter(item -> item.name().equals(name)).findFirst().orElse(null);
        if (wanted == null) {
            return -1L;
        }
        String expectedName = AutoBuy.normalize(name);
        ArrayList<Long> unitPrices = new ArrayList<Long>();
        int containerSlots = Math.max(0, screen.method_17577().field_7761.size() - 36);
        for (int i = 0; i < containerSlots; ++i) {
            List tooltip;
            class_1799 stack;
            class_1735 slot = screen.method_17577().method_7611(i);
            if (!slot.method_7681() || AuctionListingUtil.isUnavailable(stack = slot.method_7677(), tooltip = stack.method_7950(class_1792.class_9635.method_59528((class_1937)AutoBuy.mc.field_1687), (class_1657)AutoBuy.mc.field_1724, (class_1836)(AutoBuy.mc.field_1690.field_1827 ? class_1836.field_41071 : class_1836.field_41070)))) continue;
            boolean sameItem = stack.method_7909() == wanted.item();
            boolean sameName = AutoBuy.normalize(stack.method_7964().getString()).contains(expectedName);
            if (!sameItem && !sameName || (!funTime ? !sameName && !this.stackText(stack).contains(expectedName) : !sameName && !FunTimeItemMatcher.matches(wanted, stack, tooltip))) continue;
            long unitPrice = AuctionPriceParser.getMarketUnitPrice(tooltip, stack.method_7947());
            if (unitPrice <= 0L) continue;
            unitPrices.add(unitPrice);
        }
        if (unitPrices.isEmpty()) {
            return -1L;
        }
        unitPrices.sort(Long::compareTo);
        int sampleSize = Math.min(5, unitPrices.size());
        double sum = 0.0;
        for (int i = 0; i < sampleSize; ++i) {
            sum += (double)((Long)unitPrices.get(i)).longValue();
        }
        return Math.max(1L, Math.round(sum / (double)sampleSize));
    }

    private void finishPriceParse(long now) {
        int updated = this.parseUpdatedCount;
        int skipped = this.parseSkippedCount;
        this.cancelPriceParse();
        this.sendParseMessage("\u0426\u0435\u043d\u044b \u043e\u0431\u043d\u043e\u0432\u043b\u0435\u043d\u044b: " + updated + ", \u0431\u0435\u0437 \u043f\u043e\u0434\u0445\u043e\u0434\u044f\u0449\u0438\u0445 \u043b\u043e\u0442\u043e\u0432: " + skipped);
        if (AutoBuy.mc.field_1724 != null && AutoBuy.mc.field_1724.field_3944 != null) {
            if (AutoBuy.mc.field_1755 instanceof class_465) {
                AutoBuy.mc.field_1724.method_7346();
            }
            AutoBuy.mc.field_1724.field_3944.method_45730("ah");
            this.nextAction = now + 1000L;
        }
    }

    private void cancelPriceParse() {
        this.parseActive = false;
        this.parseQueue.clear();
        this.parseItemName = null;
        this.nextParseCommandAt = 0L;
        this.parseSearchSentAt = 0L;
        this.parsePreviousSyncId = -1;
    }

    @EventHandler
    public void onPurchaseMessage(PacketEvent event) {
        boolean successful;
        class_2596<?> class_25962;
        if (!event.isReceive() || !((class_25962 = event.getPacket()) instanceof class_7439)) {
            return;
        }
        class_7439 packet = (class_7439)class_25962;
        String message = packet.comp_763().getString().toLowerCase(Locale.ROOT);
        if (message.contains("\u0443 \u0432\u0430\u0441 \u043a\u0443\u043f\u0438\u043b\u0438") || message.contains("\u0443 \u0432\u0430\u0441 \u043f\u0440\u0438\u043e\u0431\u0440\u0435\u043b\u0438")) {
            return;
        }
        boolean bl = successful = message.contains("\u0432\u044b \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u043a\u0443\u043f\u0438\u043b\u0438") || message.contains("\u0432\u044b \u043f\u0440\u0438\u043e\u0431\u0440\u0435\u043b\u0438") || message.contains("\u0443\u0441\u043f\u0435\u0448\u043d\u0430\u044f \u043f\u043e\u043a\u0443\u043f\u043a\u0430") || message.contains("\u0443\u0441\u043f\u0435\u0448\u043d\u043e \u043a\u0443\u043f\u043b\u0435\u043d") || message.contains("\u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u043a\u0443\u043f\u043b\u0435\u043d") || message.contains("\u0442\u043e\u0432\u0430\u0440 \u043a\u0443\u043f\u043b\u0435\u043d") || message.contains("\u0443\u0441\u043f\u0435\u0448\u043d\u043e \u043f\u0440\u0438\u043e\u0431\u0440") || message.contains("\u043f\u043e\u043a\u0443\u043f\u043a\u0430 \u0441\u043e\u0432\u0435\u0440\u0448\u0435\u043d\u0430") || message.contains("you bought") || message.contains("purchase successful");
        if (!successful) {
            return;
        }
        mc.execute(() -> this.recordPurchase(message));
    }

    private void recordPurchase(String message) {
        long now = System.currentTimeMillis();
        this.purchaseAttempts.removeIf(attempt -> now - attempt.clickedAt > 10000L);
        Matcher priceMatcher = PURCHASE_PRICE.matcher(message);
        long paidPrice = -1L;
        if (priceMatcher.find()) {
            try {
                paidPrice = Long.parseLong(priceMatcher.group(1).replaceAll("[^0-9]", ""));
            }
            catch (NumberFormatException ignored) {
                paidPrice = -1L;
            }
        }
        PurchaseAttempt matched = null;
        Iterator<PurchaseAttempt> iterator2 = this.purchaseAttempts.descendingIterator();
        while (iterator2.hasNext()) {
            PurchaseAttempt attempt2 = iterator2.next();
            Candidate candidate = attempt2.candidate;
            if (!message.contains(AutoBuy.normalize(candidate.name)) && !message.contains(AutoBuy.normalize(candidate.stack.method_7964().getString()))) continue;
            if (matched == null) {
                matched = attempt2;
            }
            if (paidPrice <= 0L || candidate.price != paidPrice) continue;
            matched = attempt2;
            break;
        }
        if (matched == null) {
            return;
        }
        this.purchaseAttempts.remove(matched);
        Candidate bought = matched.candidate;
        if (this.pendingPurchase == bought) {
            this.pendingPurchase = null;
        }
        this.purchaseHistory.addFirst(new Purchase(bought.stack.method_7972(), bought.name, paidPrice > 0L ? paidPrice : bought.price, now));
        while (this.purchaseHistory.size() > 81) {
            this.purchaseHistory.removeLast();
        }
    }

    private CatalogItem catalogItem(String name) {
        if (this.serverMode.isSelected("FunTime")) {
            return FunTimeCatalog.BY_NAME.get(name);
        }
        for (CatalogItem item : SPOOKY) {
            if (!item.name().equals(name)) continue;
            return item;
        }
        return null;
    }

    private boolean matchesListing(CatalogItem item, class_1799 stack, List<class_2561> tooltip) {
        if (this.serverMode.isSelected("FunTime")) {
            return FunTimeItemMatcher.matches(item, stack, tooltip);
        }
        if (stack.method_7909() != item.item()) {
            return false;
        }
        String expected = AutoBuy.normalize(item.name());
        String actual = AutoBuy.normalize(stack.method_7964().getString());
        return actual.equals(expected) || actual.matches(".*(?<![\\p{L}\\p{N}])" + Pattern.quote(expected) + "(?![\\p{L}\\p{N}]).*");
    }

    private boolean confirmPriceAllowed(class_465<?> screen, Candidate purchase) {
        Long limit = this.prices.get(purchase.name);
        if (limit == null || purchase.price > limit * (long)Math.max(1, purchase.stack.method_7947())) {
            return false;
        }
        long confirmedPrice = -1L;
        int containerSlots = Math.max(0, screen.method_17577().field_7761.size() - 36);
        for (int i = 0; i < containerSlots; ++i) {
            long price;
            List tooltip;
            class_1799 stack = screen.method_17577().method_7611(i).method_7677();
            if (stack.method_7960() || !this.isGreenConfirmItem(stack) && (stack.method_7909() != purchase.stack.method_7909() || !AutoBuy.normalize(stack.method_7964().getString()).equals(AutoBuy.normalize(purchase.stack.method_7964().getString()))) || AuctionListingUtil.isUnavailable(stack, tooltip = stack.method_7950(class_1792.class_9635.field_51353, (class_1657)AutoBuy.mc.field_1724, (class_1836)class_1836.field_41070)) || (price = AuctionPriceParser.getTooltipTotal(tooltip)) <= 0L) continue;
            confirmedPrice = Math.max(confirmedPrice, price);
        }
        return confirmedPrice > 0L && confirmedPrice <= purchase.price && confirmedPrice <= limit * (long)Math.max(1, purchase.stack.method_7947());
    }

    private void sendParseMessage(String message) {
        if (AutoBuy.mc.field_1724 == null) {
            return;
        }
        AutoBuy.mc.field_1724.method_7353(QuickLogger.clientMessage((class_2561)class_2561.method_43470((String)("\u00a77[AutoBuy] \u00a7f" + message))), false);
    }

    public int getParserDiscount() {
        return this.parserDiscount.getInt();
    }

    public boolean isPriceParseActive() {
        return this.parseActive;
    }

    public void setParserDiscount(int discount) {
        this.parserDiscount.setValue(Math.max(0, Math.min(100, discount)));
    }

    public boolean isAuctionScreen(class_465<?> screen) {
        return screen != null && this.isAuction(AutoBuy.normalize(screen.method_25440().getString()));
    }

    private void swapToNextAnarchy() {
        if (AutoBuy.mc.field_1724 == null || AutoBuy.mc.field_1724.field_3944 == null) {
            return;
        }
        int current = ServerUtil.getAnarchy();
        int next = current >= 1 && current < 114 ? current + 1 : (current == 114 ? 201 : (current >= 201 && current < 236 ? current + 1 : (current == 236 ? 301 : (current >= 301 && current < 325 ? current + 1 : (current == 325 ? 501 : (current >= 501 && current < 516 ? current + 1 : (current == 516 ? 901 : (current >= 901 && current < 904 ? current + 1 : 1))))))));
        AutoBuy.mc.field_1724.field_3944.method_45730("an" + next);
    }

    private void click(class_465<?> screen, class_1735 slot) {
        AutoBuy.mc.field_1761.method_2906(screen.method_17577().field_7763, slot.field_7874, 0, class_1713.field_7790, (class_1657)AutoBuy.mc.field_1724);
    }

    private class_1735 findConfirm(class_465<?> screen) {
        int containerSlots = Math.max(0, screen.method_17577().field_7761.size() - 36);
        class_1735 green = null;
        for (int i = 0; i < containerSlots; ++i) {
            class_1735 slot = screen.method_17577().method_7611(i);
            if (!slot.method_7681() || !this.isGreenConfirmItem(slot.method_7677())) continue;
            if (i == 10) {
                return slot;
            }
            if (green != null) continue;
            green = slot;
        }
        return green;
    }

    private boolean isGreenConfirmItem(class_1799 stack) {
        return stack.method_31574(class_1802.field_8581) || stack.method_31574(class_1802.field_8656) || stack.method_31574(class_1802.field_8340) || stack.method_31574(class_1802.field_8734);
    }

    private String stackText(class_1799 stack) {
        StringBuilder text = new StringBuilder(AutoBuy.normalize(stack.method_7964().getString()));
        for (class_2561 line : stack.method_7950(class_1792.class_9635.field_51353, (class_1657)AutoBuy.mc.field_1724, (class_1836)class_1836.field_41070)) {
            text.append(' ').append(AutoBuy.normalize(line.getString()));
        }
        return text.toString();
    }

    @EventHandler
    public void onRender(DrawEvent event) {
        if (AutoBuy.mc.field_1724 != null) {
            this.applyServerCameraShake();
        }
    }

    private class_1735 findRefreshButton(class_465<?> screen) {
        class_1735 result = null;
        int containerSlots = Math.max(0, screen.method_17577().field_7761.size() - 36);
        for (int i = 0; i < containerSlots; ++i) {
            String text;
            class_1735 slot = screen.method_17577().method_7611(i);
            if (!slot.method_7681() || !(text = this.stackText(slot.method_7677())).contains("\u043e\u0431\u043d\u043e\u0432") && !text.contains("refresh") && !text.contains("\u043f\u0435\u0440\u0435\u0437\u0430\u0433\u0440\u0443\u0437") || result != null && slot.field_7874 <= result.field_7874) continue;
            result = slot;
        }
        return result;
    }

    private int refreshDelay() {
        if (this.serverMode.isSelected("SpookyTime")) {
            return 300;
        }
        long remaining = this.nextAnarchySwap - System.currentTimeMillis();
        if (remaining > 20000L) {
            return 100 + ThreadLocalRandom.current().nextInt(51);
        }
        float progress = Math.max(0.0f, Math.min(2.0f, (float)(20000L - remaining) / 10000.0f));
        int minimum = 150 + Math.round(progress * 50.0f);
        return minimum + ThreadLocalRandom.current().nextInt(51);
    }

    private void scheduleNextAnarchySwap(long now) {
        this.nextAnarchySwap = now + 55000L + ThreadLocalRandom.current().nextLong(5001L);
    }

    public boolean isCameraShakeActive() {
        class_465 screen;
        class_437 class_4372;
        return this.isState() && this.serverMode.isSelected("FunTime") && this.auctionEnabled && AutoBuy.mc.field_1724 != null && (class_4372 = AutoBuy.mc.field_1755) instanceof class_465 && this.isAuctionScreen(screen = (class_465)class_4372);
    }

    public float[] cameraShake(float yaw, float pitch) {
        if (!this.isCameraShakeActive()) {
            this.resetCameraShake();
            return new float[]{yaw, pitch};
        }
        long now = System.nanoTime();
        if (this.shakeUpdatedAt == 0L) {
            this.shakeUpdatedAt = now;
        }
        float delta = Math.min(0.05f, (float)(now - this.shakeUpdatedAt) / 1.0E9f);
        this.shakeUpdatedAt = now;
        if (now >= this.nextShakeTargetAt) {
            this.shakeTargetYaw = (float)ThreadLocalRandom.current().nextDouble(-1.35, 1.35);
            this.shakeTargetPitch = (float)ThreadLocalRandom.current().nextDouble(-0.85, 0.85);
            this.shakeSpeed = (float)ThreadLocalRandom.current().nextDouble(10.0, 22.0);
            this.nextShakeTargetAt = now + ThreadLocalRandom.current().nextLong(35000000L, 90000001L);
        }
        float factor = Math.min(1.0f, delta * this.shakeSpeed);
        this.shakeYaw += (this.shakeTargetYaw - this.shakeYaw) * factor;
        this.shakePitch += (this.shakeTargetPitch - this.shakePitch) * factor;
        return new float[]{yaw + this.shakeYaw, Math.max(-90.0f, Math.min(90.0f, pitch + this.shakePitch))};
    }

    private void resetCameraShake() {
        this.nextShakeTargetAt = 0L;
        this.shakeUpdatedAt = 0L;
        this.shakeTargetPitch = 0.0f;
        this.shakeTargetYaw = 0.0f;
        this.shakePitch = 0.0f;
        this.shakeYaw = 0.0f;
    }

    private void applyServerCameraShake() {
        if (!this.isCameraShakeActive()) {
            if (this.appliedShakeYaw != 0.0f || this.appliedShakePitch != 0.0f) {
                AutoBuy.mc.field_1724.method_36456(AutoBuy.mc.field_1724.method_36454() - this.appliedShakeYaw);
                AutoBuy.mc.field_1724.method_36457(Math.max(-90.0f, Math.min(90.0f, AutoBuy.mc.field_1724.method_36455() - this.appliedShakePitch)));
            }
            this.appliedShakePitch = 0.0f;
            this.appliedShakeYaw = 0.0f;
            this.resetCameraShake();
            return;
        }
        this.cameraShake(0.0f, 0.0f);
        AutoBuy.mc.field_1724.method_36456(AutoBuy.mc.field_1724.method_36454() + this.shakeYaw - this.appliedShakeYaw);
        AutoBuy.mc.field_1724.method_36457(Math.max(-90.0f, Math.min(90.0f, AutoBuy.mc.field_1724.method_36455() + this.shakePitch - this.appliedShakePitch)));
        this.appliedShakeYaw = this.shakeYaw;
        this.appliedShakePitch = this.shakePitch;
    }

    private boolean trackAuctionSlowdown(class_465<?> screen, long now) {
        if (!this.detectAuctionSlowdown.isValue()) {
            this.refreshSentAt = 0L;
            return false;
        }
        if (this.refreshSentAt != 0L && this.auctionSignature(screen) != this.refreshSignature) {
            long latency = now - this.refreshSentAt;
            this.lagTotal += latency;
            this.lagMaximum = Math.max(this.lagMaximum, latency);
            ++this.lagSamples;
            this.refreshSentAt = 0L;
        }
        if (this.lagStatsInChat.isValue() && this.lagSamples > 0 && now >= this.nextLagReportAt) {
            AutoBuy.mc.field_1724.method_7353(QuickLogger.clientMessage((class_2561)class_2561.method_43470((String)String.format(Locale.ROOT, "\u00a77[AutoBuy] \u041e\u0442\u043a\u043b\u0438\u043a \u0430\u0443\u043a\u0446\u0438\u043e\u043d\u0430: \u00a7f%d ms \u00a77(\u043c\u0430\u043a\u0441. \u00a7f%d ms\u00a77)", this.lagTotal / (long)this.lagSamples, this.lagMaximum))), false);
            this.lagMaximum = 0L;
            this.lagTotal = 0L;
            this.lagSamples = 0;
            this.nextLagReportAt = now + 5000L;
        }
        if (this.refreshSentAt != 0L && now - this.refreshSentAt >= 3500L && this.autoFixSlowdown.isValue()) {
            this.refreshSentAt = 0L;
            AutoBuy.mc.field_1724.method_7346();
            AutoBuy.mc.field_1724.field_3944.method_45730("ah");
            this.nextAction = now + 1000L;
            return true;
        }
        return false;
    }

    private long auctionSignature(class_465<?> screen) {
        long signature = 1L;
        int slots = Math.max(0, screen.method_17577().field_7761.size() - 36);
        for (int i = 0; i < slots; ++i) {
            class_1799 stack = screen.method_17577().method_7611(i).method_7677();
            signature = signature * 31L + (long)stack.method_7909().hashCode();
            signature = signature * 31L + (long)stack.method_7947();
            signature = signature * 31L + (long)stack.method_57353().hashCode();
        }
        return signature;
    }

    private void resetSlowdownTracking(long now) {
        this.lagMaximum = 0L;
        this.lagTotal = 0L;
        this.refreshSignature = 0L;
        this.refreshSentAt = 0L;
        this.lagSamples = 0;
        this.nextLagReportAt = now + 5000L;
    }

    private boolean isAuction(String title) {
        return title.contains("\u0430\u0443\u043a\u0446\u0438\u043e\u043d") || title.contains("auction") || title.contains("\u043f\u043e\u0438\u0441\u043a") || title.contains("\u0442\u043e\u0432\u0430\u0440\u044b");
    }

    private boolean isConfirm(String title) {
        return title.contains("\u043f\u043e\u0434\u0442\u0432\u0435\u0440\u0436") || title.contains("\u043f\u043e\u043a\u0443\u043f") || title.contains("confirm");
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replace('\u0451', '\u0435').replaceAll("\u00a7.", "").trim();
    }

    private static CatalogItem ci(String name, class_1792 item) {
        return new CatalogItem(name, item);
    }

    private record Candidate(class_1735 slot, long price, String name, class_1799 stack) {
    }

    public record CatalogItem(String name, class_1792 item) {
    }

    private record PurchaseAttempt(Candidate candidate, long clickedAt) {
    }

    public record Purchase(class_1799 stack, String name, long price, long timestamp) {
    }
}

