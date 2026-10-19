/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
 *  net.minecraft.class_124
 *  net.minecraft.class_1263
 *  net.minecraft.class_1268
 *  net.minecraft.class_1269
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_1713
 *  net.minecraft.class_1735
 *  net.minecraft.class_1792
 *  net.minecraft.class_1792$class_9635
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1812
 *  net.minecraft.class_1836
 *  net.minecraft.class_1887
 *  net.minecraft.class_1890
 *  net.minecraft.class_1893
 *  net.minecraft.class_1937
 *  net.minecraft.class_2248
 *  net.minecraft.class_2281
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_2378
 *  net.minecraft.class_2382
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_2480
 *  net.minecraft.class_2487
 *  net.minecraft.class_2531
 *  net.minecraft.class_2561
 *  net.minecraft.class_2586
 *  net.minecraft.class_2596
 *  net.minecraft.class_2625
 *  net.minecraft.class_266
 *  net.minecraft.class_268
 *  net.minecraft.class_269
 *  net.minecraft.class_270
 *  net.minecraft.class_2868
 *  net.minecraft.class_2879
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_3489
 *  net.minecraft.class_3708
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_3965
 *  net.minecraft.class_437
 *  net.minecraft.class_465
 *  net.minecraft.class_5250
 *  net.minecraft.class_5321
 *  net.minecraft.class_6880
 *  net.minecraft.class_7439
 *  net.minecraft.class_7924
 *  net.minecraft.class_8646
 *  net.minecraft.class_9011
 *  net.minecraft.class_9279
 *  net.minecraft.class_9290
 *  net.minecraft.class_9304
 *  net.minecraft.class_9334
 *  org.lwjgl.glfw.GLFW
 */
package ru.recoin.implement.features.modules.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.class_124;
import net.minecraft.class_1263;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1812;
import net.minecraft.class_1836;
import net.minecraft.class_1887;
import net.minecraft.class_1890;
import net.minecraft.class_1893;
import net.minecraft.class_1937;
import net.minecraft.class_2248;
import net.minecraft.class_2281;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2378;
import net.minecraft.class_2382;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2480;
import net.minecraft.class_2487;
import net.minecraft.class_2531;
import net.minecraft.class_2561;
import net.minecraft.class_2586;
import net.minecraft.class_2596;
import net.minecraft.class_2625;
import net.minecraft.class_266;
import net.minecraft.class_268;
import net.minecraft.class_269;
import net.minecraft.class_270;
import net.minecraft.class_2868;
import net.minecraft.class_2879;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3489;
import net.minecraft.class_3708;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_5250;
import net.minecraft.class_5321;
import net.minecraft.class_6880;
import net.minecraft.class_7439;
import net.minecraft.class_7924;
import net.minecraft.class_8646;
import net.minecraft.class_9011;
import net.minecraft.class_9279;
import net.minecraft.class_9290;
import net.minecraft.class_9304;
import net.minecraft.class_9334;
import org.lwjgl.glfw.GLFW;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BindSetting;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.GroupSetting;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.api.system.sound.SoundManager;
import ru.recoin.common.QuickLogger;
import ru.recoin.common.util.auction.AuctionListingUtil;
import ru.recoin.common.util.auction.AuctionPriceParser;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.task.TaskPriority;
import ru.recoin.common.util.world.ServerUtil;
import ru.recoin.implement.events.chat.ChatCommandEvent;
import ru.recoin.implement.events.container.DrawSlotsEvent;
import ru.recoin.implement.events.container.HandledScreenEvent;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.RotationUpdateEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationConfig;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.SmoothMode;

public class AuctionHelper
extends Module {
    static final long AUTO_SELL_AUCTION_RETRY_MS = 2500L;
    static final RotationConfig AUTO_SELL_CONTAINER_ROTATION = new RotationConfig(new SmoothMode(), false, true);
    static final Pattern AUCTION_WAIT_PATTERN = Pattern.compile("\u043f\u043e\u0434\u043e\u0436\u0434\u0438\u0442\u0435\\D{0,16}(\\d+)\\s*\u0441\u0435\u043a", 66);
    static final Pattern SCOREBOARD_BALANCE_PATTERN = Pattern.compile("(?i)(?:\u043c\u043e\u043d\u0435\u0442|\u0431\u0430\u043b\u0430\u043d\u0441|coins|money|\\$)[^0-9]*([0-9][0-9 ,._]*)");
    static final Pattern SCOREBOARD_DIGITS_PATTERN = Pattern.compile("[0-9][0-9 ,._]*");
    private String title = "";
    private List<AuctionItem> pageItems = new ArrayList<AuctionItem>();
    private double averageEffectivePrice = 0.0;
    private double minEffectivePrice = Double.MAX_VALUE;
    private long lastAnalyzedSignature = Long.MIN_VALUE;
    private long lastScanTime = 0L;
    private long cachedScoreboardBalance = Long.MAX_VALUE;
    private long lastBalanceCheckTime = 0L;
    private boolean isScanningPage = false;
    private boolean wasPressed = false;
    private boolean averageSellWasPressed = false;
    private BindSetting searchKey = new BindSetting("Search Key", "Key to search item in hand on AH");
    private BindSetting averageSellKey = new BindSetting("\u041f\u0440\u043e\u0434\u0430\u0436\u0430 \u043f\u043e \u0441\u0440.\u0446\u0435\u043d\u0435", "\u041f\u0440\u043e\u0434\u0430\u0451\u0442 \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u0432 \u0440\u0443\u043a\u0435 \u043f\u043e \u0441\u0440\u0435\u0434\u043d\u0435\u0439 \u0446\u0435\u043d\u0435 \u043f\u0435\u0440\u0432\u044b\u0445 \u043f\u044f\u0442\u0438 \u0441\u0442\u0440\u0430\u043d\u0438\u0446");
    private BooleanSetting bypassFunTime = new BooleanSetting("\u041e\u0431\u0445\u043e\u0434 Funtime", "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c \u0443\u0432\u0435\u043b\u0438\u0447\u0435\u043d\u043d\u044b\u0435 \u0437\u0430\u0434\u0435\u0440\u0436\u043a\u0438 \u0434\u043b\u044f \u043e\u0431\u0445\u043e\u0434\u0430 \u043f\u0440\u043e\u0432\u0435\u0440\u043e\u043a FunTime").setValue(false);
    private BooleanSetting purchaseSound = new BooleanSetting("\u0417\u0432\u0443\u043a \u043f\u0440\u0438 \u043f\u043e\u043a\u0443\u043f\u043a\u0435", "Apple Pay \u043f\u043e\u0441\u043b\u0435 \u0443\u0441\u043f\u0435\u0448\u043d\u043e\u0439 \u043f\u043e\u043a\u0443\u043f\u043a\u0438 \u043d\u0430 \u0430\u0443\u043a\u0446\u0438\u043e\u043d\u0435").setValue(true);
    private long lastPurchaseSound;
    private AverageSellState averageSellState = AverageSellState.IDLE;
    private class_1799 averageSellStack = class_1799.field_8037;
    private Set<Long> averagePageSignatures = new HashSet<Long>();
    private double averagePriceSum;
    private int averagePriceCount;
    private int averagePages;
    private long averageNextActionAt;
    private long averagePageDeadline;
    private long averageStartedAt;
    private long averageLastSeenAuction;
    private boolean averageAuctionOpened;
    private long averageScanDeadline;
    private String averagePendingCommand;
    private final Map<String, PriceCacheEntry> averagePriceCache = new HashMap<String, PriceCacheEntry>();
    private boolean autoSellActive;
    private AutoSellState autoSellState = AutoSellState.IDLE;
    private long autoSellNextActionAt;
    private class_2338 autoSellContainerPos;
    private boolean autoSellAllContainers;
    private int autoSellHotbarSlot;
    private class_1799 autoSellOnlyItem = class_1799.field_8037;
    private long autoSellRevenue;
    private long autoSellLastSellPrice;
    private int autoSellSoldLots;
    private final List<class_2338> autoSellContainers = new ArrayList<class_2338>();
    private final List<class_1799> autoSellRejectedItems = new ArrayList<class_1799>();
    private int autoSellContainerIndex;
    private int autoSellResourceAnarchy;
    private int autoSellSaleAnarchy;
    private int autoSellTrashAnarchy;
    private boolean autoSellNeedNextContainer;
    private boolean autoSellReopenCurrentContainer;
    private boolean autoSellSourcesExhausted;
    private boolean autoSellFinishAfterTrash;
    private boolean autoSellSaleSwitchIssued;
    private boolean autoSellContainerInteractSent;
    private long autoSellAuctionReadyAt;
    private long autoSellContainerOpenStartedAt;
    private class_3965 autoSellContainerHit;
    private Angle autoSellContainerAngle;
    private BooleanSetting calculator = new BooleanSetting("\u041a\u0430\u043b\u044c\u043a\u0443\u043b\u044f\u0442\u043e\u0440", "\u0412\u044b\u0447\u0438\u0441\u043b\u044f\u0442\u044c \u0432\u044b\u0440\u0430\u0436\u0435\u043d\u0438\u044f \u0432 /ah sell").setValue(true);
    private BooleanSetting considerBalance = new BooleanSetting("\u0423\u0447\u0438\u0442\u044b\u0432\u0430\u0442\u044c \u0431\u0430\u043b\u0430\u043d\u0441", "\u041f\u043e\u0434\u0441\u0432\u0435\u0447\u0438\u0432\u0430\u0442\u044c \u0442\u043e\u043b\u044c\u043a\u043e \u0434\u043e\u0441\u0442\u0443\u043f\u043d\u044b\u0435 \u043f\u043e \u0431\u0430\u043b\u0430\u043d\u0441\u0443 \u043b\u043e\u0442\u044b").setValue(false);
    private ValueSetting minimumDurability = new ValueSetting("Minimum Durability", "Hide damageable items below this durability percentage").setValue(0.0f).range(0.0f, 100.0f).step(1.0f);
    private BooleanSetting enchantedOnly = new BooleanSetting("Enchanted Only", "Hide items without enchantments").setValue(false);
    private SelectSetting countMode = new SelectSetting("Count Mode", "Exact or minimum stack size").value("Exact", "Minimum").selected("Exact");
    private ValueSetting countValue = new ValueSetting("Count", "Required item count").setValue(1.0f).range(1.0f, 64.0f).step(1.0f);
    private GroupSetting countGroup = new GroupSetting("Count Filter", "Filter auction lots by item count").settings(this.countMode, this.countValue).setValue(false);
    private MultiSelectSetting armorEnchants = new MultiSelectSetting("\u0427\u0430\u0440\u044b", "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u0442\u043e\u043b\u044c\u043a\u043e \u0431\u0440\u043e\u043d\u044e \u0441 \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u044b\u043c\u0438 \u0447\u0430\u0440\u0430\u043c\u0438").value("\u0417\u0430\u0449\u0438\u0442\u0430 5", "\u041f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u044c 5", "\u041f\u043e\u0447\u0438\u043d\u043a\u0430", "\u0428\u0438\u043f\u044b");
    private BooleanSetting armorNoDurability = new BooleanSetting("\u0411\u0435\u0437 \u043f\u043e\u0432\u0440\u0435\u0436\u0434\u0435\u043d\u0438\u0439", "\u0421\u043a\u0440\u044b\u0432\u0430\u0442\u044c \u043f\u043e\u0432\u0440\u0435\u0436\u0434\u0435\u043d\u043d\u0443\u044e \u0431\u0440\u043e\u043d\u044e").setValue(false);
    private GroupSetting armorGroup = new GroupSetting("\u0411\u0440\u043e\u043d\u044f", "\u0424\u0438\u043b\u044c\u0442\u0440 \u0447\u0430\u0440 \u043d\u0430 \u0431\u0440\u043e\u043d\u0435", false).settings(this.armorEnchants, this.armorNoDurability);
    private MultiSelectSetting maceEnchants = new MultiSelectSetting("\u0427\u0430\u0440\u044b", "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u0442\u043e\u043b\u044c\u043a\u043e \u0431\u0443\u043b\u0430\u0432\u044b \u0441 \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u044b\u043c\u0438 \u0447\u0430\u0440\u0430\u043c\u0438").value("\u041f\u0440\u043e\u0431\u0438\u0442\u0438\u0435 3", "\u041f\u043b\u043e\u0442\u043d\u043e\u0441\u0442\u044c 5");
    private GroupSetting maceGroup = new GroupSetting("\u0411\u0443\u043b\u0430\u0432\u0430", "\u0424\u0438\u043b\u044c\u0442\u0440 \u0447\u0430\u0440 \u043d\u0430 \u0431\u0443\u043b\u0430\u0432\u0435", false).settings(this.maceEnchants);
    private BooleanSetting noSilkTouch = new BooleanSetting("No Silk Touch", "Hide pickaxes without silk touch").setValue(false).visible(this::isPickaxeContext);
    private BooleanSetting noLevel3 = new BooleanSetting("No Level 3", "Hide potions without level 3 effects").setValue(false).visible(this::isPotionContext);
    private BooleanSetting noCombined = new BooleanSetting("No Combined", "Hide potions without strength + speed").setValue(false).visible(this::isPotionContext);

    public static AuctionHelper getInstance() {
        return Instance.get(AuctionHelper.class);
    }

    private String getAveragePriceCacheKey(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return "";
        }
        class_1799 comparable = stack.method_7972();
        comparable.method_57381(class_9334.field_49607);
        return this.resolveSearchName(stack) + "|" + String.valueOf(comparable.method_57353());
    }

    public AuctionHelper() {
        super("AuctionHelper", "Auction Helper", ModuleCategory.RENDER);
        this.searchKey.setKey(-1);
        this.averageSellKey.setKey(-1);
        this.setup(this.searchKey, this.averageSellKey, this.bypassFunTime, this.purchaseSound, this.calculator, this.considerBalance, this.minimumDurability, this.enchantedOnly, this.countGroup, this.armorGroup, this.maceGroup, this.noSilkTouch, this.noLevel3, this.noCombined);
        ItemTooltipCallback.EVENT.register((stack, ctx, type, lines) -> this.appendPerUnitTooltip(stack, lines));
    }

    @EventHandler
    public void onChatCommand(ChatCommandEvent event) {
        String normalized;
        if (!this.calculator.isValue() || event.getCommand() == null) {
            return;
        }
        String command = event.getCommand().trim();
        String prefix = "ah sell ";
        String string = normalized = command.startsWith("/") ? command.substring(1) : command;
        if (!normalized.regionMatches(true, 0, prefix, 0, prefix.length())) {
            return;
        }
        String expression = normalized.substring(prefix.length()).trim();
        if (expression.isEmpty()) {
            return;
        }
        try {
            double value = new ExpressionParser(expression).parse();
            if (!Double.isFinite(value) || value > 9.223372036854776E18 || value < -9.223372036854776E18) {
                return;
            }
            event.setCommand("ah sell " + Math.round(value));
        }
        catch (IllegalArgumentException illegalArgumentException) {
            // empty catch block
        }
    }

    public String getCalculatorPreview(String input) {
        String prefix;
        if (!this.isState() || !this.calculator.isValue() || input == null) {
            return null;
        }
        String command = input.trim();
        String normalized = command.startsWith("/") ? command.substring(1) : command;
        if (!normalized.regionMatches(true, 0, prefix = "ah sell ", 0, prefix.length())) {
            return null;
        }
        String expression = normalized.substring(prefix.length()).trim();
        if (expression.isEmpty() || expression.chars().noneMatch(ch -> ch == 43 || ch == 45 || ch == 42 || ch == 47)) {
            return null;
        }
        try {
            double value = new ExpressionParser(expression).parse();
            if (!Double.isFinite(value) || value > 9.223372036854776E18 || value < -9.223372036854776E18) {
                return null;
            }
            return "= " + Math.round(value);
        }
        catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        class_2596<?> class_25962 = event.getPacket();
        if (!(class_25962 instanceof class_7439)) {
            return;
        }
        class_7439 packet = (class_7439)class_25962;
        String message = packet.comp_763().getString().toLowerCase();
        if (this.purchaseSound.isValue() && this.isPurchaseConfirmation(message) && System.currentTimeMillis() - this.lastPurchaseSound > 750L) {
            this.lastPurchaseSound = System.currentTimeMillis();
            SoundManager.playSound(SoundManager.APPLE_PAY, 1.0f, 1.0f);
        }
        this.handleAutoSellMessage(message);
        if (this.averageSellState == AverageSellState.IDLE) {
            return;
        }
        if (message.contains("\u0442\u0430\u043a\u043e\u0433\u043e \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430") && (message.contains("\u043d\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442") || message.contains("\u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d"))) {
            if (this.autoSellActive) {
                this.rejectCurrentAutoSellItem("\u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u043d\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442 \u043d\u0430 \u0430\u0443\u043a\u0446\u0438\u043e\u043d\u0435");
            } else {
                this.cancelAverageSell("\u00a7c[AH] \u041f\u0440\u0435\u0434\u043c\u0435\u0442 \u043d\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442, \u043f\u043e\u0438\u0441\u043a \u043e\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d", false);
            }
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        Object cleanedName;
        if (AuctionHelper.mc.field_1724 != null && AuctionHelper.mc.field_1755 == null) {
            this.handleAverageSellKey();
            this.processAutoSell();
        }
        if (AuctionHelper.mc.field_1724 != null && AuctionHelper.mc.field_1755 == null && this.searchKey.getKey() != -1 && this.averageSellState == AverageSellState.IDLE) {
            boolean isKeyDown;
            boolean bl = isKeyDown = GLFW.glfwGetKey((long)mc.method_22683().method_4490(), (int)this.searchKey.getKey()) == 1;
            if (isKeyDown) {
                if (!this.wasPressed) {
                    class_1799 handStack = AuctionHelper.mc.field_1724.method_6047();
                    if (!handStack.method_7960()) {
                        String rawName = handStack.method_7964().getString();
                        cleanedName = this.resolveSearchName(handStack);
                        if (!((String)cleanedName).isEmpty()) {
                            this.clientMessage((class_2561)class_2561.method_43470((String)("\u00a77\u041e\u0442\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u043e \u043d\u0430 \u043f\u043e\u0438\u0441\u043a: \u00a7f" + (String)cleanedName)));
                            AuctionHelper.mc.field_1724.field_3944.method_45730(this.getSearchCommand() + " " + (String)cleanedName);
                        } else {
                            this.clientMessage((class_2561)class_2561.method_43470((String)("\u00a7c\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043e\u0447\u0438\u0441\u0442\u0438\u0442\u044c \u0438\u043c\u044f: \u00a7f" + rawName)));
                        }
                    }
                    this.wasPressed = true;
                }
            } else {
                this.wasPressed = false;
            }
        }
        long now = System.currentTimeMillis();
        cleanedName = AuctionHelper.mc.field_1755;
        if (cleanedName instanceof class_465) {
            class_465 screen = (class_465)cleanedName;
            this.title = screen.method_25440().getString();
            this.processAutoSellContainer(screen);
            if (this.isAuctionScreen(screen)) {
                this.averageLastSeenAuction = now;
                long sig = this.computeContainerSignature(screen);
                if (sig != this.lastAnalyzedSignature) {
                    this.lastAnalyzedSignature = sig;
                    this.lastScanTime = now;
                    this.scanAndAnalyzePage(screen);
                }
                if (this.averageSellState != AverageSellState.IDLE) {
                    this.averageAuctionOpened = true;
                }
                this.processAverageSell(screen);
                return;
            }
        }
        if (this.averageAuctionOpened && this.averageSellState != AverageSellState.IDLE && this.averageSellState != AverageSellState.FINALIZING && now - this.averageLastSeenAuction > 1500L) {
            if (this.autoSellActive) {
                this.scheduleAutoSellAuctionRetry("\u0430\u0443\u043a\u0446\u0438\u043e\u043d \u0437\u0430\u043a\u0440\u044b\u043b\u0441\u044f", 2500L);
            } else {
                this.cancelAverageSell("\u00a7c[AH] \u041f\u043e\u0438\u0441\u043a \u0441\u0440\u0435\u0434\u043d\u0435\u0439 \u0446\u0435\u043d\u044b \u043e\u0442\u043c\u0435\u043d\u0451\u043d: \u0430\u0443\u043a\u0446\u0438\u043e\u043d \u0437\u0430\u043a\u0440\u044b\u0442", false);
            }
            this.reset();
            this.title = "";
            return;
        }
        if (this.averageSellState == AverageSellState.WAITING_AUCTION) {
            long timeout;
            if (this.averagePendingCommand != null && now >= this.averageNextActionAt) {
                AuctionHelper.mc.field_1724.field_3944.method_45730(this.averagePendingCommand);
                this.averagePendingCommand = null;
                this.averageStartedAt = now;
            }
            long l = timeout = this.bypassFunTime.isValue() ? 5000L : 3000L;
            if (!this.averageAuctionOpened && this.averagePendingCommand == null && now - this.averageStartedAt > timeout) {
                if (this.autoSellActive) {
                    this.scheduleAutoSellAuctionRetry("\u0430\u0443\u043a\u0446\u0438\u043e\u043d \u043d\u0435 \u043e\u0442\u043a\u0440\u044b\u043b\u0441\u044f", 2500L);
                } else {
                    this.cancelAverageSell("\u00a7c[AH] \u0410\u0443\u043a\u0446\u0438\u043e\u043d \u043d\u0435 \u043e\u0442\u043a\u0440\u044b\u043b\u0441\u044f, \u043f\u043e\u0438\u0441\u043a \u043e\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d", false);
                }
            }
        } else if (this.averageSellState == AverageSellState.FINALIZING && now >= this.averageNextActionAt) {
            this.finishAverageSell();
        } else if (this.averageSellState != AverageSellState.IDLE && now - this.averageStartedAt > (this.bypassFunTime.isValue() ? 25000L : 20000L)) {
            if (this.autoSellActive) {
                this.scheduleAutoSellAuctionRetry("\u0430\u0443\u043a\u0446\u0438\u043e\u043d \u043d\u0435 \u043e\u0442\u0432\u0435\u0442\u0438\u043b", 2500L);
            } else {
                this.cancelAverageSell("\u00a7c[AH] \u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043e\u0431\u0440\u0430\u0431\u043e\u0442\u0430\u0442\u044c \u0430\u0443\u043a\u0446\u0438\u043e\u043d");
            }
        }
        this.reset();
        this.title = "";
    }

    @EventHandler
    public void onRotationUpdate(RotationUpdateEvent event) {
        if (event.getType() != 0 || !this.autoSellActive || this.autoSellContainerAngle == null) {
            return;
        }
        if (this.autoSellState != AutoSellState.OPENING_CONTAINER && this.autoSellState != AutoSellState.OPENING_TRASH) {
            return;
        }
        RotationController.INSTANCE.rotateTo(this.autoSellContainerAngle, 2, AUTO_SELL_CONTAINER_ROTATION, TaskPriority.HIGH_IMPORTANCE_2, this);
    }

    private void handleAverageSellKey() {
        boolean down;
        int key = this.averageSellKey.getKey();
        if (key == -1) {
            return;
        }
        boolean bl = down = GLFW.glfwGetKey((long)mc.method_22683().method_4490(), (int)key) == 1;
        if (down && !this.averageSellWasPressed && this.averageSellState == AverageSellState.IDLE) {
            this.startAverageSell();
        }
        this.averageSellWasPressed = down;
    }

    public void startAutoSell(boolean allContainers, int resourceAnarchy, int saleAnarchy, int trashAnarchy) {
        if (AuctionHelper.mc.field_1724 == null || AuctionHelper.mc.field_1687 == null || AuctionHelper.mc.field_1761 == null) {
            return;
        }
        if (saleAnarchy <= 0 || trashAnarchy <= 0 || allContainers && resourceAnarchy <= 0) {
            this.log("\u00a7cAutoSell: \u043f\u0440\u043e\u0432\u0435\u0440\u044c \u043d\u043e\u043c\u0435\u0440\u0430 \u0430\u043d\u0430\u0440\u0445\u0438\u0439 \u0432 \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430\u0445");
            return;
        }
        this.autoSellAllContainers = allContainers;
        this.autoSellResourceAnarchy = resourceAnarchy;
        this.autoSellSaleAnarchy = saleAnarchy;
        this.autoSellTrashAnarchy = trashAnarchy;
        this.autoSellHotbarSlot = 0;
        this.autoSellOnlyItem = class_1799.field_8037;
        this.autoSellRevenue = 0L;
        this.autoSellLastSellPrice = 0L;
        this.autoSellSoldLots = 0;
        this.autoSellContainers.clear();
        this.autoSellRejectedItems.clear();
        this.autoSellContainerIndex = 0;
        this.autoSellContainerPos = null;
        this.autoSellNeedNextContainer = false;
        this.autoSellReopenCurrentContainer = false;
        this.autoSellSourcesExhausted = !allContainers;
        this.autoSellFinishAfterTrash = false;
        this.autoSellSaleSwitchIssued = false;
        this.autoSellContainerInteractSent = false;
        this.autoSellAuctionReadyAt = 0L;
        this.autoSellContainerOpenStartedAt = 0L;
        this.autoSellContainerHit = null;
        this.autoSellContainerAngle = null;
        this.autoSellActive = true;
        this.autoSellNextActionAt = System.currentTimeMillis();
        this.averageSellWasPressed = false;
        if (allContainers) {
            this.requestResourceAnarchy();
            this.log("\u00a7aAutoSell: \u043f\u0435\u0440\u0435\u0445\u043e\u0436\u0443 \u043d\u0430 \u0430\u043d\u0430\u0440\u0445\u0438\u044e \u0440\u0435\u0441\u0443\u0440\u0441\u043e\u0432 \u00a7f" + resourceAnarchy);
        } else {
            this.requestSaleAnarchy();
            this.log("\u00a7aAutoSell: \u043f\u0435\u0440\u0435\u0445\u043e\u0436\u0443 \u043d\u0430 \u0430\u043d\u0430\u0440\u0445\u0438\u044e \u043f\u0440\u043e\u0434\u0430\u0436\u0438 \u00a7f" + saleAnarchy);
        }
    }

    private void processAutoSell() {
        if (!this.autoSellActive || AuctionHelper.mc.field_1724 == null || AuctionHelper.mc.field_1755 != null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now < this.autoSellNextActionAt) {
            return;
        }
        if (this.autoSellState == AutoSellState.GOING_RESOURCE) {
            if (!this.isOnAnarchy(this.autoSellResourceAnarchy)) {
                this.sendAnarchy(this.autoSellResourceAnarchy);
                this.autoSellNextActionAt = now + 1500L;
                return;
            }
            this.autoSellState = AutoSellState.RESOURCE_READY;
            this.autoSellNextActionAt = now + 1500L;
            return;
        }
        if (this.autoSellState == AutoSellState.RESOURCE_READY) {
            if (!this.isOnAnarchy(this.autoSellResourceAnarchy)) {
                this.requestResourceAnarchy();
                return;
            }
            this.prepareResourceContainer();
            return;
        }
        if (this.autoSellState == AutoSellState.GOING_SALE) {
            if (!this.isOnAnarchy(this.autoSellSaleAnarchy)) {
                this.autoSellSaleSwitchIssued = true;
                this.sendAnarchy(this.autoSellSaleAnarchy);
                this.autoSellNextActionAt = now + 1500L;
                return;
            }
            this.autoSellState = AutoSellState.SALE_READY;
            this.autoSellNextActionAt = this.autoSellAuctionReadyAt = this.autoSellSaleSwitchIssued ? now + 10000L : now;
            if (this.autoSellSaleSwitchIssued) {
                this.log("\u00a7eAutoSell: \u0436\u0434\u0443 10 \u0441\u0435\u043a\u0443\u043d\u0434 \u0434\u043e \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u044f /ah");
            }
            return;
        }
        if (this.autoSellState == AutoSellState.SALE_READY) {
            this.autoSellState = AutoSellState.SELL_NEXT;
            this.autoSellNextActionAt = now;
            return;
        }
        if (this.autoSellState == AutoSellState.GOING_TRASH) {
            if (!this.isOnAnarchy(this.autoSellTrashAnarchy)) {
                this.sendAnarchy(this.autoSellTrashAnarchy);
                this.autoSellNextActionAt = now + 1500L;
                return;
            }
            this.autoSellState = AutoSellState.TRASH_READY;
            this.autoSellNextActionAt = now + 1500L;
            return;
        }
        if (this.autoSellState == AutoSellState.TRASH_READY) {
            class_2338 trash = this.findTrashContainer(12);
            if (trash == null) {
                this.log("\u00a7eAutoSell: \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440 \u0432\u043e\u0437\u043b\u0435 \u0442\u0430\u0431\u043b\u0438\u0447\u043a\u0438 \u00ab\u041c\u0443\u0441\u043e\u0440\u00bb, \u043f\u043e\u0432\u0442\u043e\u0440\u044f\u044e \u043f\u043e\u0438\u0441\u043a");
                this.autoSellNextActionAt = now + 2000L;
                return;
            }
            this.autoSellContainerPos = trash;
            this.autoSellState = AutoSellState.OPENING_TRASH;
            this.openAutoSellContainer();
            return;
        }
        if (this.autoSellState == AutoSellState.OPENING_TRASH) {
            this.tryOpenAutoSellContainer();
            if (now - this.autoSellContainerOpenStartedAt > 2500L) {
                this.autoSellState = AutoSellState.TRASH_READY;
                this.autoSellNextActionAt = now + 1000L;
                this.clearAutoSellContainerRotation();
                this.log("\u00a7eAutoSell: \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440 \u00ab\u041c\u0443\u0441\u043e\u0440\u00bb \u043d\u0435 \u043e\u0442\u043a\u0440\u044b\u043b\u0441\u044f, \u043f\u043e\u0432\u0442\u043e\u0440\u044f\u044e");
            }
            return;
        }
        if (this.autoSellState == AutoSellState.OPENING_CONTAINER) {
            this.tryOpenAutoSellContainer();
            if (now - this.autoSellContainerOpenStartedAt > 2500L) {
                this.clearAutoSellContainerRotation();
                if (this.autoSellAllContainers && this.openNextAutoSellContainer()) {
                    this.log("\u00a7eAutoSell: \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440 \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d, \u043f\u0440\u043e\u043f\u0443\u0441\u043a\u0430\u044e");
                } else if (this.autoSellAllContainers) {
                    this.autoSellSourcesExhausted = true;
                    this.autoSellReopenCurrentContainer = false;
                    this.sellCollectedItemsOrFinish();
                } else {
                    this.stopAutoSell("\u00a7c[AutoSell] \u041a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440 \u043d\u0435 \u043e\u0442\u043a\u0440\u044b\u043b\u0441\u044f");
                }
            }
            return;
        }
        if (this.autoSellState == AutoSellState.RETRY_AUCTION) {
            if (!this.isOnAnarchy(this.autoSellSaleAnarchy)) {
                this.sendAnarchy(this.autoSellSaleAnarchy);
                this.autoSellAuctionReadyAt = Math.max(this.autoSellAuctionReadyAt, now + 10000L);
                this.autoSellNextActionAt = now + 1500L;
                return;
            }
            if (now < this.autoSellAuctionReadyAt) {
                this.autoSellNextActionAt = this.autoSellAuctionReadyAt;
                return;
            }
            if (AuctionHelper.mc.field_1724.method_6047().method_7960()) {
                this.autoSellState = AutoSellState.SELL_NEXT;
                this.autoSellNextActionAt = now;
                return;
            }
            this.autoSellState = AutoSellState.SELLING;
            this.startAverageSell();
            return;
        }
        if (this.autoSellState == AutoSellState.RETRY_SAME_PRICE) {
            if (this.autoSellLastSellPrice <= 0L || AuctionHelper.mc.field_1724.method_6047().method_7960()) {
                this.autoSellState = AutoSellState.SELL_NEXT;
                this.autoSellNextActionAt = now;
                return;
            }
            AuctionHelper.mc.field_1724.field_3944.method_45730("ah sell " + this.autoSellLastSellPrice);
            this.autoSellState = AutoSellState.WAITING_SELL_RESULT;
            this.autoSellNextActionAt = now + 1200L;
            this.log("\u00a7eAutoSell: \u043f\u043e\u0432\u0442\u043e\u0440\u043d\u043e \u0432\u044b\u0441\u0442\u0430\u0432\u043b\u044f\u044e \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u043f\u043e \u0442\u043e\u0439 \u0436\u0435 \u0446\u0435\u043d\u0435 \u00a7f" + this.autoSellLastSellPrice);
            return;
        }
        if (this.autoSellState == AutoSellState.SELL_NEXT) {
            if (this.averageSellState != AverageSellState.IDLE) {
                return;
            }
            class_1735 slot = this.findNextAutoSellSlot();
            if (slot == null) {
                this.finishAutoSellBatch();
                return;
            }
            this.moveSlotToSelectedHotbar(slot);
            this.autoSellState = AutoSellState.SELLING;
            this.autoSellNextActionAt = now + 250L;
            this.startAverageSell();
            return;
        }
        if (this.autoSellState == AutoSellState.WAITING_SELL_RESULT && this.averageSellState == AverageSellState.IDLE) {
            this.autoSellState = AutoSellState.SELL_NEXT;
            this.autoSellNextActionAt = now + 450L;
        }
    }

    private void processAutoSellContainer(class_465<?> screen) {
        if (!this.autoSellActive || AuctionHelper.mc.field_1724 == null || this.isAuctionScreen(screen)) {
            return;
        }
        if (this.autoSellState == AutoSellState.OPENING_TRASH || this.autoSellState == AutoSellState.DUMPING_TRASH) {
            this.processTrashContainer(screen);
            return;
        }
        if (this.autoSellState != AutoSellState.OPENING_CONTAINER && this.autoSellState != AutoSellState.LOOTING_CONTAINER) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now < this.autoSellNextActionAt) {
            return;
        }
        this.autoSellState = AutoSellState.LOOTING_CONTAINER;
        this.clearAutoSellContainerRotation();
        int containerSize = Math.max(0, screen.method_17577().field_7761.size() - 36);
        class_1735 source = null;
        for (int i = 0; i < containerSize; ++i) {
            class_1735 slot = screen.method_17577().method_7611(i);
            if (slot == null || !slot.method_7681()) continue;
            source = slot;
            break;
        }
        if (source == null) {
            AuctionHelper.mc.field_1724.method_7346();
            this.autoSellNeedNextContainer = true;
            this.autoSellReopenCurrentContainer = false;
            this.autoSellState = AutoSellState.RESOURCE_READY;
            this.autoSellNextActionAt = now + 350L;
            this.log("\u00a77AutoSell: \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440 \u043f\u0443\u0441\u0442, \u043f\u0440\u043e\u0432\u0435\u0440\u044f\u044e \u0441\u043b\u0435\u0434\u0443\u044e\u0449\u0438\u0439");
            return;
        }
        if (AuctionHelper.mc.field_1724.method_31548().method_7376() == -1) {
            AuctionHelper.mc.field_1724.method_7346();
            this.autoSellReopenCurrentContainer = true;
            this.autoSellNeedNextContainer = false;
            this.autoSellSourcesExhausted = false;
            this.requestSaleAnarchy();
            this.log("\u00a77AutoSell: \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044c \u0437\u0430\u043f\u043e\u043b\u043d\u0435\u043d, \u043d\u0430\u0447\u0438\u043d\u0430\u044e \u043f\u0440\u043e\u0434\u0430\u0436\u0443");
            return;
        }
        AuctionHelper.mc.field_1761.method_2906(screen.method_17577().field_7763, source.field_7874, 0, class_1713.field_7794, (class_1657)AuctionHelper.mc.field_1724);
        this.autoSellNextActionAt = now + 80L;
    }

    private void processTrashContainer(class_465<?> screen) {
        long now = System.currentTimeMillis();
        if (now < this.autoSellNextActionAt) {
            return;
        }
        this.autoSellState = AutoSellState.DUMPING_TRASH;
        this.clearAutoSellContainerRotation();
        for (class_1735 slot : screen.method_17577().field_7761) {
            if (slot == null || !slot.method_7681() || slot.field_7871 != AuctionHelper.mc.field_1724.method_31548() || !this.isRejectedAutoSellItem(slot.method_7677())) continue;
            AuctionHelper.mc.field_1761.method_2906(screen.method_17577().field_7763, slot.field_7874, 0, class_1713.field_7794, (class_1657)AuctionHelper.mc.field_1724);
            this.autoSellNextActionAt = now + 100L;
            return;
        }
        AuctionHelper.mc.field_1724.method_7346();
        this.autoSellRejectedItems.clear();
        this.autoSellContainerPos = null;
        if (this.autoSellFinishAfterTrash || this.autoSellSourcesExhausted || !this.autoSellAllContainers) {
            this.stopAutoSell("\u00a7a[AutoSell] \u041f\u0440\u043e\u0434\u0430\u0436\u0430 \u0437\u0430\u0432\u0435\u0440\u0448\u0435\u043d\u0430, \u043d\u0435\u043f\u0440\u043e\u0434\u0430\u0432\u0430\u0435\u043c\u044b\u0435 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b \u0441\u043b\u043e\u0436\u0435\u043d\u044b \u0432 \u00ab\u041c\u0443\u0441\u043e\u0440\u00bb");
            return;
        }
        if (!this.autoSellReopenCurrentContainer && !this.autoSellNeedNextContainer) {
            this.autoSellNeedNextContainer = true;
        }
        this.requestResourceAnarchy();
    }

    private boolean isPurchaseConfirmation(String message) {
        if (message == null) {
            return false;
        }
        if (message.contains("\u0443 \u0432\u0430\u0441 \u043a\u0443\u043f\u0438\u043b\u0438") || message.contains("\u0443 \u0432\u0430\u0441 \u043f\u0440\u0438\u043e\u0431\u0440\u0435\u043b\u0438")) {
            return false;
        }
        return message.contains("\u0432\u044b \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u043a\u0443\u043f\u0438\u043b\u0438") || message.contains("\u0432\u044b \u043f\u0440\u0438\u043e\u0431\u0440\u0435\u043b\u0438") || message.contains("\u0443\u0441\u043f\u0435\u0448\u043d\u0430\u044f \u043f\u043e\u043a\u0443\u043f\u043a\u0430") || message.contains("\u0443\u0441\u043f\u0435\u0448\u043d\u043e \u043a\u0443\u043f\u043b\u0435\u043d") || message.contains("\u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u043a\u0443\u043f\u043b\u0435\u043d") || message.contains("\u0442\u043e\u0432\u0430\u0440 \u043a\u0443\u043f\u043b\u0435\u043d") || message.contains("\u0443\u0441\u043f\u0435\u0448\u043d\u043e \u043f\u0440\u0438\u043e\u0431\u0440") || message.contains("\u043f\u043e\u043a\u0443\u043f\u043a\u0430 \u0441\u043e\u0432\u0435\u0440\u0448\u0435\u043d\u0430") || message.contains("you bought") || message.contains("purchase successful");
    }

    private void handleAutoSellMessage(String message) {
        boolean purchase;
        if (!this.autoSellActive) {
            return;
        }
        long auctionWait = this.extractAuctionWaitMillis(message);
        if (auctionWait > 0L && (message.contains("\u0430\u0443\u043a\u0446\u0438\u043e\u043d") || message.contains("/ah"))) {
            this.scheduleAutoSellAuctionRetry("\u0441\u0435\u0440\u0432\u0435\u0440 \u043f\u0440\u043e\u0441\u0438\u0442 \u043f\u043e\u0434\u043e\u0436\u0434\u0430\u0442\u044c", auctionWait);
            return;
        }
        if ((message.contains("\u0430\u0443\u043a\u0446\u0438\u043e\u043d") || message.contains("/ah")) && (message.contains("\u0437\u0430\u043a\u0440\u044b\u0442") || message.contains("\u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f") || message.contains("\u0432\u0440\u0435\u043c\u0435\u043d\u043d\u043e \u043d\u0435 \u0440\u0430\u0431\u043e\u0442\u0430\u0435\u0442"))) {
            this.scheduleAutoSellAuctionRetry("\u0430\u0443\u043a\u0446\u0438\u043e\u043d \u0432\u0440\u0435\u043c\u0435\u043d\u043d\u043e \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d", 2500L);
            return;
        }
        if (this.autoSellLastSellPrice > 0L && this.autoSellState == AutoSellState.WAITING_SELL_RESULT && message.contains("\u0441\u043b\u0438\u0448\u043a\u043e\u043c") && (message.contains("\u0434\u043e\u0440\u043e\u0433") || message.contains("\u0432\u044b\u0441\u043e\u043a") || message.contains("\u0431\u043e\u043b\u044c\u0448"))) {
            this.autoSellState = AutoSellState.RETRY_SAME_PRICE;
            this.autoSellNextActionAt = System.currentTimeMillis() + 500L;
            this.log("\u00a7eAutoSell: \u0446\u0435\u043d\u0430 \u043e\u0442\u043c\u0435\u0447\u0435\u043d\u0430 \u043a\u0430\u043a \u0441\u043b\u0438\u0448\u043a\u043e\u043c \u0432\u044b\u0441\u043e\u043a\u0430\u044f, \u043f\u043e\u0432\u0442\u043e\u0440\u044f\u044e \u0435\u0451 \u0431\u0435\u0437 \u0438\u0437\u043c\u0435\u043d\u0435\u043d\u0438\u044f");
            return;
        }
        boolean bl = purchase = (message.contains("\u0443 \u0432\u0430\u0441 \u043a\u0443\u043f\u0438\u043b\u0438") || message.contains("\u0443 \u0432\u0430\u0441 \u043f\u0440\u0438\u043e\u0431\u0440\u0435\u043b\u0438")) && message.contains("/ah");
        if (purchase) {
            this.autoSellRevenue += this.extractPurchasedPrice(message);
            ++this.autoSellSoldLots;
        }
        if (message.contains("\u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0432\u044b\u0441\u0442\u0430\u0432\u0438\u0442\u044c") || message.contains("\u043e\u0441\u0432\u043e\u0431\u043e\u0434\u0438\u0442\u0435") && (message.contains("\u0445\u0440\u0430\u043d\u0438\u043b\u0438\u0449\u0435") || message.contains("\u0441\u043b\u043e\u0442")) || message.contains("ah rent")) {
            this.autoSellState = AutoSellState.WAITING_SLOT;
            this.autoSellNextActionAt = Long.MAX_VALUE;
            this.log("\u00a7eAutoSell: \u0441\u043b\u043e\u0442\u044b \u0430\u0443\u043a\u0446\u0438\u043e\u043d\u0430 \u0437\u0430\u043d\u044f\u0442\u044b, \u0436\u0434\u0443 \u043f\u043e\u043a\u0443\u043f\u043a\u0443");
            return;
        }
        if (this.autoSellState == AutoSellState.WAITING_SLOT && (message.contains("\u0443 \u0432\u0430\u0441 \u043a\u0443\u043f\u0438\u043b\u0438") || message.contains("\u0443 \u0432\u0430\u0441 \u043f\u0440\u0438\u043e\u0431\u0440\u0435\u043b\u0438") || message.contains("\u043a\u0443\u043f\u0438\u043b\u0438")) && message.contains("/ah")) {
            this.autoSellState = AutoSellState.SELL_NEXT;
            this.autoSellNextActionAt = System.currentTimeMillis() + 500L;
            this.log("\u00a7aAutoSell: \u043e\u0441\u0432\u043e\u0431\u043e\u0434\u0438\u043b\u0441\u044f \u0441\u043b\u043e\u0442, \u0432\u044b\u0441\u0442\u0430\u0432\u043b\u044f\u044e \u0441\u043b\u0435\u0434\u0443\u044e\u0449\u0438\u0439 \u043b\u043e\u0442");
        }
    }

    private long extractPurchasedPrice(String message) {
        if (message == null) {
            return 0L;
        }
        int dollar = message.lastIndexOf(36);
        String tail = dollar >= 0 ? message.substring(dollar + 1) : message;
        String digits = tail.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(digits);
        }
        catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    private void requestResourceAnarchy() {
        this.autoSellState = AutoSellState.GOING_RESOURCE;
        this.autoSellNextActionAt = System.currentTimeMillis();
    }

    private void requestSaleAnarchy() {
        this.autoSellSaleSwitchIssued = !this.isOnAnarchy(this.autoSellSaleAnarchy);
        this.autoSellHotbarSlot = 0;
        this.autoSellState = AutoSellState.GOING_SALE;
        this.autoSellNextActionAt = System.currentTimeMillis();
    }

    private void requestTrashAnarchy(boolean finishAfterTrash) {
        this.autoSellFinishAfterTrash = finishAfterTrash;
        this.autoSellState = AutoSellState.GOING_TRASH;
        this.autoSellNextActionAt = System.currentTimeMillis();
    }

    private boolean isOnAnarchy(int anarchy) {
        if (anarchy <= 0) {
            return false;
        }
        if (ServerUtil.getAnarchy() == anarchy) {
            return true;
        }
        if (AuctionHelper.mc.field_1687 == null) {
            return false;
        }
        class_266 sidebar = AuctionHelper.mc.field_1687.method_8428().method_1189(class_8646.field_45157);
        if (sidebar == null) {
            return false;
        }
        String title = class_124.method_539((String)sidebar.method_1114().getString());
        if (title == null) {
            return false;
        }
        Matcher matcher = Pattern.compile("(?iu)(?:\u0430\u043d\u0430\u0440\u0445\u0438\u044f|anarchy)\\s*[-#\u2116:]?\\s*(\\d{1,3})").matcher(title);
        return matcher.find() && Integer.parseInt(matcher.group(1)) == anarchy;
    }

    public boolean isCurrentAnarchy(int anarchy) {
        return this.isOnAnarchy(anarchy);
    }

    public void startAutoSellRelist(class_1799 item, int saleAnarchy, int trashAnarchy) {
        if (item == null || item.method_7960()) {
            return;
        }
        this.startAutoSell(false, saleAnarchy, saleAnarchy, trashAnarchy);
        if (this.autoSellActive) {
            this.autoSellOnlyItem = item.method_46651(1);
        }
    }

    private void sendAnarchy(int anarchy) {
        if (AuctionHelper.mc.field_1724 != null && AuctionHelper.mc.field_1724.field_3944 != null && anarchy > 0 && !this.isOnAnarchy(anarchy)) {
            AuctionHelper.mc.field_1724.field_3944.method_45730("an" + anarchy);
        }
    }

    private void prepareResourceContainer() {
        if (this.autoSellContainers.isEmpty()) {
            this.autoSellContainers.addAll(this.findContainers(8));
            this.autoSellContainerIndex = 0;
            this.autoSellContainerPos = this.autoSellContainers.isEmpty() ? null : this.autoSellContainers.getFirst();
            this.autoSellNeedNextContainer = false;
            if (this.autoSellContainerPos == null) {
                this.stopAutoSell("\u00a7c[AutoSell] \u041d\u0430 \u0430\u043d\u0430\u0440\u0445\u0438\u0438 \u0440\u0435\u0441\u0443\u0440\u0441\u043e\u0432 \u0440\u044f\u0434\u043e\u043c \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440");
                return;
            }
        } else if (this.autoSellReopenCurrentContainer) {
            this.autoSellReopenCurrentContainer = false;
            if (this.autoSellContainerIndex < 0 || this.autoSellContainerIndex >= this.autoSellContainers.size()) {
                this.autoSellSourcesExhausted = true;
                this.sellCollectedItemsOrFinish();
                return;
            }
            this.autoSellContainerPos = this.autoSellContainers.get(this.autoSellContainerIndex);
        } else if (this.autoSellNeedNextContainer) {
            this.autoSellNeedNextContainer = false;
            if (!this.openNextAutoSellContainer()) {
                this.autoSellSourcesExhausted = true;
                this.autoSellContainerPos = null;
                this.sellCollectedItemsOrFinish();
                return;
            }
            return;
        }
        this.autoSellState = AutoSellState.OPENING_CONTAINER;
        this.openAutoSellContainer();
        this.log("\u00a7aAutoSell: \u043e\u0442\u043a\u0440\u044b\u0432\u0430\u044e \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440 " + (this.autoSellContainerIndex + 1) + "/" + this.autoSellContainers.size());
    }

    private void finishAutoSellBatch() {
        if (!this.autoSellRejectedItems.isEmpty()) {
            this.requestTrashAnarchy(this.autoSellSourcesExhausted || !this.autoSellAllContainers);
            this.log("\u00a7eAutoSell: \u043f\u0435\u0440\u0435\u043d\u043e\u0448\u0443 \u043d\u0435\u043f\u0440\u043e\u0434\u0430\u0432\u0430\u0435\u043c\u044b\u0435 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b \u0432 \u00ab\u041c\u0443\u0441\u043e\u0440\u00bb");
            return;
        }
        if (this.autoSellAllContainers && !this.autoSellSourcesExhausted) {
            this.requestResourceAnarchy();
            return;
        }
        this.stopAutoSell("\u00a7a[AutoSell] \u041f\u0440\u043e\u0434\u0430\u0436\u0430 \u0437\u0430\u0432\u0435\u0440\u0448\u0435\u043d\u0430: \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432 \u0431\u043e\u043b\u044c\u0448\u0435 \u043d\u0435\u0442");
    }

    private void sellCollectedItemsOrFinish() {
        this.autoSellHotbarSlot = 0;
        if (this.hasSellableAutoSellItems()) {
            this.requestSaleAnarchy();
            this.log("\u00a77AutoSell: \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440\u044b \u0441\u043e\u0431\u0440\u0430\u043d\u044b, \u043d\u0430\u0447\u0438\u043d\u0430\u044e \u0432\u044b\u0441\u0442\u0430\u0432\u043b\u044f\u0442\u044c \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b");
            return;
        }
        if (!this.autoSellRejectedItems.isEmpty()) {
            this.requestTrashAnarchy(true);
            return;
        }
        this.stopAutoSell("\u00a7a[AutoSell] \u041f\u0440\u043e\u0434\u0430\u0436\u0430 \u0437\u0430\u0432\u0435\u0440\u0448\u0435\u043d\u0430: \u0432\u0441\u0435 \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440\u044b \u043e\u0431\u0440\u0430\u0431\u043e\u0442\u0430\u043d\u044b");
    }

    private boolean hasSellableAutoSellItems() {
        if (AuctionHelper.mc.field_1724 == null) {
            return false;
        }
        for (int i = 0; i < 36; ++i) {
            class_1799 stack = AuctionHelper.mc.field_1724.method_31548().method_5438(i);
            if (stack.method_7960() || !this.matchesAutoSellTarget(stack) || this.isRejectedAutoSellItem(stack)) continue;
            return true;
        }
        return false;
    }

    private long extractAuctionWaitMillis(String message) {
        if (message == null || message.isBlank()) {
            return -1L;
        }
        Matcher matcher = AUCTION_WAIT_PATTERN.matcher(message);
        if (!matcher.find()) {
            return -1L;
        }
        try {
            return Math.max(2500L, Long.parseLong(matcher.group(1)) * 1000L + 250L);
        }
        catch (NumberFormatException ignored) {
            return -1L;
        }
    }

    private void scheduleAutoSellAuctionRetry(String reason, long delayMs) {
        if (!this.autoSellActive) {
            return;
        }
        if (AuctionHelper.mc.field_1724 != null && AuctionHelper.mc.field_1755 instanceof class_465) {
            AuctionHelper.mc.field_1724.method_7346();
        }
        this.clearAverageSell();
        long wait = Math.max(2500L, delayMs);
        this.autoSellState = AutoSellState.RETRY_AUCTION;
        this.autoSellAuctionReadyAt = this.autoSellNextActionAt = System.currentTimeMillis() + wait;
        this.log("\u00a7eAutoSell: " + reason + ", \u043f\u043e\u0432\u0442\u043e\u0440\u044e \u0447\u0435\u0440\u0435\u0437 " + String.format(Locale.ROOT, "%.1f", (double)wait / 1000.0) + " \u0441\u0435\u043a.");
    }

    private void rejectCurrentAutoSellItem(String reason) {
        class_1799 rejected = this.averageSellStack.method_7960() ? (AuctionHelper.mc.field_1724 == null ? class_1799.field_8037 : AuctionHelper.mc.field_1724.method_6047().method_7972()) : this.averageSellStack.method_7972();
        if (!rejected.method_7960() && this.autoSellRejectedItems.stream().noneMatch(stack -> class_1799.method_31577((class_1799)stack, (class_1799)rejected))) {
            this.autoSellRejectedItems.add(rejected);
        }
        if (AuctionHelper.mc.field_1724 != null && AuctionHelper.mc.field_1755 instanceof class_465) {
            AuctionHelper.mc.field_1724.method_7346();
        }
        this.clearAverageSell();
        this.autoSellState = AutoSellState.SELL_NEXT;
        this.autoSellNextActionAt = System.currentTimeMillis() + 250L;
        this.log("\u00a7eAutoSell: " + reason + ", \u043e\u0442\u043f\u0440\u0430\u0432\u043b\u044e \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u0432 \u00ab\u041c\u0443\u0441\u043e\u0440\u00bb");
    }

    private boolean isRejectedAutoSellItem(class_1799 stack) {
        return stack != null && !stack.method_7960() && this.autoSellRejectedItems.stream().anyMatch(rejected -> class_1799.method_31577((class_1799)rejected, (class_1799)stack));
    }

    private boolean matchesAutoSellTarget(class_1799 stack) {
        return this.autoSellOnlyItem.method_7960() || stack.method_7909() == this.autoSellOnlyItem.method_7909() && stack.method_7964().getString().equals(this.autoSellOnlyItem.method_7964().getString());
    }

    private class_2338 findNearestContainer(int radius) {
        List<class_2338> containers = this.findContainers(radius);
        return containers.isEmpty() ? null : containers.getFirst();
    }

    private List<class_2338> findContainers(int radius) {
        if (AuctionHelper.mc.field_1724 == null || AuctionHelper.mc.field_1687 == null) {
            return List.of();
        }
        class_2338 base = AuctionHelper.mc.field_1724.method_24515();
        ArrayList<class_2338> result = new ArrayList<class_2338>();
        class_2338 trashContainer = this.findTrashContainer(radius + 2);
        for (class_2338 pos2 : class_2338.method_10097((class_2338)base.method_10069(-radius, -radius, -radius), (class_2338)base.method_10069(radius, radius, radius))) {
            if (!this.isAutoSellContainer(pos2) || this.isTrashContainerPart(pos2, trashContainer)) continue;
            result.add(pos2.method_10062());
        }
        class_243 eye = AuctionHelper.mc.field_1724.method_33571();
        result.sort(Comparator.comparingDouble(pos -> pos.method_46558().method_1025(eye)));
        return result;
    }

    private class_2338 findTrashContainer(int radius) {
        if (AuctionHelper.mc.field_1724 == null || AuctionHelper.mc.field_1687 == null) {
            return null;
        }
        class_2338 base = AuctionHelper.mc.field_1724.method_24515();
        class_2338 best = null;
        double bestDistance = Double.MAX_VALUE;
        for (class_2338 signPos : class_2338.method_10097((class_2338)base.method_10069(-radius, -radius, -radius), (class_2338)base.method_10069(radius, radius, radius))) {
            class_2625 sign;
            class_2586 class_25862 = AuctionHelper.mc.field_1687.method_8321(signPos);
            if (!(class_25862 instanceof class_2625) || !this.isTrashSign(sign = (class_2625)class_25862)) continue;
            for (class_2338 candidate : class_2338.method_10097((class_2338)signPos.method_10069(-2, -2, -2), (class_2338)signPos.method_10069(2, 2, 2))) {
                double distance;
                if (!this.isAutoSellContainer(candidate) || !((distance = candidate.method_10262((class_2382)signPos)) < bestDistance)) continue;
                bestDistance = distance;
                best = candidate.method_10062();
            }
        }
        return best;
    }

    private boolean isTrashContainerPart(class_2338 pos, class_2338 trashContainer) {
        if (trashContainer == null) {
            return false;
        }
        if (pos.equals((Object)trashContainer)) {
            return true;
        }
        class_2248 first = AuctionHelper.mc.field_1687.method_8320(pos).method_26204();
        class_2248 second = AuctionHelper.mc.field_1687.method_8320(trashContainer).method_26204();
        return first instanceof class_2281 && second instanceof class_2281 && pos.method_10262((class_2382)trashContainer) <= 1.0;
    }

    private boolean isTrashSign(class_2625 sign) {
        return this.signTextContainsTrash(sign.method_49853().method_49877(false)) || this.signTextContainsTrash(sign.method_49854().method_49877(false));
    }

    private boolean signTextContainsTrash(class_2561[] lines) {
        for (class_2561 line : lines) {
            if (line == null || !line.getString().toLowerCase(Locale.ROOT).contains("\u043c\u0443\u0441\u043e\u0440")) continue;
            return true;
        }
        return false;
    }

    private boolean openNextAutoSellContainer() {
        if (++this.autoSellContainerIndex >= this.autoSellContainers.size()) {
            return false;
        }
        this.autoSellContainerPos = this.autoSellContainers.get(this.autoSellContainerIndex);
        this.autoSellState = AutoSellState.OPENING_CONTAINER;
        this.autoSellNextActionAt = System.currentTimeMillis();
        this.openAutoSellContainer();
        this.log("\u00a77AutoSell: \u0441\u043b\u0435\u0434\u0443\u044e\u0449\u0438\u0439 \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440 " + (this.autoSellContainerIndex + 1) + "/" + this.autoSellContainers.size());
        return true;
    }

    private boolean isAutoSellContainer(class_2338 pos) {
        if (AuctionHelper.mc.field_1687 == null) {
            return false;
        }
        class_2586 blockEntity = AuctionHelper.mc.field_1687.method_8321(pos);
        if (blockEntity instanceof class_1263) {
            return true;
        }
        class_2248 block = AuctionHelper.mc.field_1687.method_8320(pos).method_26204();
        return block instanceof class_2281 || block instanceof class_2531 || block instanceof class_3708 || block instanceof class_2480;
    }

    private void openAutoSellContainer() {
        if (AuctionHelper.mc.field_1724 == null || AuctionHelper.mc.field_1761 == null || this.autoSellContainerPos == null) {
            return;
        }
        this.autoSellContainerHit = this.findAutoSellContainerHit(this.autoSellContainerPos);
        this.autoSellContainerAngle = AngleUtil.calculateAngle(this.autoSellContainerHit.method_17784());
        this.autoSellContainerInteractSent = false;
        this.autoSellNextActionAt = this.autoSellContainerOpenStartedAt = System.currentTimeMillis();
        RotationController.INSTANCE.rotateTo(this.autoSellContainerAngle, 2, AUTO_SELL_CONTAINER_ROTATION, TaskPriority.HIGH_IMPORTANCE_2, this);
    }

    private void tryOpenAutoSellContainer() {
        if (this.autoSellContainerInteractSent || this.autoSellContainerHit == null || this.autoSellContainerAngle == null || AuctionHelper.mc.field_1724 == null || AuctionHelper.mc.field_1761 == null) {
            return;
        }
        Angle delta = AngleUtil.calculateDelta(RotationController.INSTANCE.getServerAngle(), this.autoSellContainerAngle);
        if (Math.abs(delta.getYaw()) > 3.0f || Math.abs(delta.getPitch()) > 3.0f) {
            return;
        }
        class_1269 result = AuctionHelper.mc.field_1761.method_2896(AuctionHelper.mc.field_1724, class_1268.field_5808, this.autoSellContainerHit);
        if (result.method_23665()) {
            AuctionHelper.mc.field_1724.field_3944.method_52787((class_2596)new class_2879(class_1268.field_5808));
        }
        this.autoSellContainerInteractSent = true;
    }

    private class_3965 findAutoSellContainerHit(class_2338 pos) {
        class_243 eye = AuctionHelper.mc.field_1724.method_33571();
        class_243 center = pos.method_46558();
        class_3965 nearestVisible = null;
        double nearestVisibleDistance = Double.MAX_VALUE;
        class_2350 nearestSide = class_2350.field_11036;
        class_243 nearestPoint = center.method_1031(0.0, 0.501, 0.0);
        double nearestDistance = nearestPoint.method_1025(eye);
        for (class_2350 side : class_2350.values()) {
            class_3965 ray;
            class_243 point = center.method_1031((double)side.method_10148() * 0.501, (double)side.method_10164() * 0.501, (double)side.method_10165() * 0.501);
            double distance = point.method_1025(eye);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestSide = side;
                nearestPoint = point;
            }
            if ((ray = AuctionHelper.mc.field_1687.method_17742(new class_3959(eye, point, class_3959.class_3960.field_17559, class_3959.class_242.field_1348, (class_1297)AuctionHelper.mc.field_1724))).method_17783() != class_239.class_240.field_1332 || !ray.method_17777().equals((Object)pos) || !(distance < nearestVisibleDistance)) continue;
            nearestVisible = ray;
            nearestVisibleDistance = distance;
        }
        return nearestVisible != null ? nearestVisible : new class_3965(nearestPoint, nearestSide, pos, false);
    }

    private void clearAutoSellContainerRotation() {
        this.autoSellContainerHit = null;
        this.autoSellContainerAngle = null;
        this.autoSellContainerInteractSent = false;
        this.autoSellContainerOpenStartedAt = 0L;
    }

    private class_1735 findNextAutoSellSlot() {
        if (AuctionHelper.mc.field_1724 == null) {
            return null;
        }
        while (this.autoSellHotbarSlot < 9) {
            class_1735 hotbar = this.findPlayerInventorySlot(this.autoSellHotbarSlot++);
            if (hotbar == null || !hotbar.method_7681() || !this.matchesAutoSellTarget(hotbar.method_7677()) || this.isRejectedAutoSellItem(hotbar.method_7677())) continue;
            return hotbar;
        }
        for (int inventoryIndex = 9; inventoryIndex <= 35; ++inventoryIndex) {
            class_1735 source = this.findPlayerInventorySlot(inventoryIndex);
            if (source == null || !source.method_7681() || !this.matchesAutoSellTarget(source.method_7677()) || this.isRejectedAutoSellItem(source.method_7677())) continue;
            for (int hotbarIndex = 0; hotbarIndex < 9; ++hotbarIndex) {
                class_1735 target = this.findPlayerInventorySlot(hotbarIndex);
                if (target == null || target.method_7681()) continue;
                PlayerInventoryUtil.clickSlot(source.field_7874, hotbarIndex, class_1713.field_7791, false);
                this.autoSellHotbarSlot = 0;
                return this.findPlayerInventorySlot(hotbarIndex);
            }
        }
        return null;
    }

    private class_1735 findPlayerInventorySlot(int index) {
        if (AuctionHelper.mc.field_1724 == null) {
            return null;
        }
        for (class_1735 slot : AuctionHelper.mc.field_1724.field_7512.field_7761) {
            if (slot == null || slot.field_7871 != AuctionHelper.mc.field_1724.method_31548() || slot.method_34266() != index) continue;
            return slot;
        }
        return null;
    }

    private void moveSlotToSelectedHotbar(class_1735 slot) {
        if (AuctionHelper.mc.field_1724 == null || slot == null) {
            return;
        }
        int index = slot.method_34266();
        if (index < 0 || index > 8) {
            return;
        }
        AuctionHelper.mc.field_1724.method_31548().method_61496(index);
        AuctionHelper.mc.field_1724.field_3944.method_52787((class_2596)new class_2868(index));
    }

    public void stopAutoSell(String message) {
        if (AuctionHelper.mc.field_1724 != null) {
            this.clientMessage((class_2561)class_2561.method_43470((String)message));
        }
        this.autoSellActive = false;
        this.autoSellOnlyItem = class_1799.field_8037;
        this.autoSellState = AutoSellState.IDLE;
        this.autoSellContainerPos = null;
        this.autoSellContainers.clear();
        this.autoSellRejectedItems.clear();
        this.autoSellContainerIndex = 0;
        this.autoSellNeedNextContainer = false;
        this.autoSellReopenCurrentContainer = false;
        this.autoSellSourcesExhausted = false;
        this.autoSellFinishAfterTrash = false;
        this.autoSellSaleSwitchIssued = false;
        this.autoSellLastSellPrice = 0L;
        this.clearAutoSellContainerRotation();
        this.autoSellAuctionReadyAt = 0L;
        this.autoSellNextActionAt = 0L;
    }

    public boolean isAutoSellActive() {
        return this.autoSellActive;
    }

    public long getAutoSellRevenue() {
        return this.autoSellRevenue;
    }

    public int getAutoSellSoldLots() {
        return this.autoSellSoldLots;
    }

    private void startAverageSell() {
        class_1799 handStack = AuctionHelper.mc.field_1724.method_6047();
        if (handStack.method_7960()) {
            this.clientMessage((class_2561)class_2561.method_43470((String)"\u00a7c[AH] \u0412\u043e\u0437\u044c\u043c\u0438 \u043f\u0440\u043e\u0434\u0430\u0432\u0430\u0435\u043c\u044b\u0439 \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u0432 \u0440\u0443\u043a\u0443"));
            return;
        }
        String name = this.resolveSearchName(handStack);
        if (name.isEmpty()) {
            this.clientMessage((class_2561)class_2561.method_43470((String)"\u00a7c[AH] \u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043e\u043f\u0440\u0435\u0434\u0435\u043b\u0438\u0442\u044c \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430"));
            return;
        }
        String cacheKey = this.getAveragePriceCacheKey(handStack);
        PriceCacheEntry cached = this.averagePriceCache.get(cacheKey);
        long now = System.currentTimeMillis();
        if (cached != null && now - cached.timestamp <= 30000L) {
            this.averageSellStack = handStack.method_7972();
            long sellPrice = Math.max(1L, Math.round(cached.unitPrice * (double)Math.max(1, this.averageSellStack.method_7947())));
            if (this.autoSellActive) {
                this.autoSellLastSellPrice = sellPrice;
            }
            AuctionHelper.mc.field_1724.field_3944.method_45730("ah sell " + sellPrice);
            if (this.autoSellActive) {
                this.autoSellState = AutoSellState.WAITING_SELL_RESULT;
                this.autoSellNextActionAt = now + 1200L;
            }
            long secLeft = Math.max(1L, (30000L - (now - cached.timestamp)) / 1000L);
            this.clientMessage((class_2561)class_2561.method_43470((String)("\u00a7a[AH] \u041f\u0440\u043e\u0434\u0430\u0436\u0430 \u043f\u043e \u0441\u043e\u0445\u0440\u0430\u043d\u0451\u043d\u043d\u043e\u0439 \u0446\u0435\u043d\u0435 (" + secLeft + " \u0441\u0435\u043a): \u00a7f" + sellPrice)));
            return;
        }
        this.averageSellStack = handStack.method_7972();
        this.averagePageSignatures.clear();
        this.averagePriceSum = 0.0;
        this.averagePriceCount = 0;
        this.averagePages = 0;
        this.averageAuctionOpened = false;
        this.averageLastSeenAuction = this.averageStartedAt = System.currentTimeMillis();
        if (this.bypassFunTime.isValue()) {
            long enterDelay = 1000L + (long)(Math.random() * 2000.0);
            this.averageNextActionAt = this.averageStartedAt + enterDelay;
            this.averagePageDeadline = this.averageStartedAt + 8000L;
            this.averageScanDeadline = 0L;
            this.averageSellState = AverageSellState.WAITING_AUCTION;
            this.averagePendingCommand = this.getSearchCommand() + " " + name;
            this.log("\u00a7a\u041f\u043e\u0438\u0441\u043a (" + String.format(Locale.ROOT, "%.1f", Float.valueOf((float)enterDelay / 1000.0f)) + " \u0441\u0435\u043a): \u00a7f" + name);
        } else {
            this.averageNextActionAt = this.averageStartedAt + 300L;
            this.averagePageDeadline = this.averageStartedAt + 3000L;
            this.averageScanDeadline = 0L;
            this.averageSellState = AverageSellState.WAITING_AUCTION;
            this.averagePendingCommand = null;
            AuctionHelper.mc.field_1724.field_3944.method_45730(this.getSearchCommand() + " " + name);
            this.log("\u00a7a\u0417\u0430\u043f\u0443\u0449\u0435\u043d \u043f\u043e\u0438\u0441\u043a: \u00a7f" + name);
        }
        this.log("\u00a77\u0421\u0447\u0438\u0442\u0430\u044e \u0441\u0440\u0435\u0434\u043d\u044e\u044e \u0446\u0435\u043d\u0443 \u043f\u043e 5 \u0441\u0430\u043c\u044b\u043c \u0434\u0435\u0448\u0451\u0432\u044b\u043c \u043b\u043e\u0442\u0430\u043c \u0441 \u043a\u0430\u0436\u0434\u043e\u0439 \u0441\u0442\u0440\u0430\u043d\u0438\u0446\u044b, \u043c\u0430\u043a\u0441\u0438\u043c\u0443\u043c 5 \u0441\u0442\u0440\u0430\u043d\u0438\u0446");
    }

    private void processAverageSell(class_465<?> screen) {
        int maxPages;
        long signature;
        if (this.averageSellState == AverageSellState.IDLE || this.averageSellState == AverageSellState.FINALIZING) {
            return;
        }
        long now = System.currentTimeMillis();
        this.averageSellState = AverageSellState.SCANNING;
        if (now < this.averageNextActionAt) {
            return;
        }
        if (this.bypassFunTime.isValue()) {
            if (this.averageScanDeadline == 0L) {
                this.averageScanDeadline = now + 3000L + (long)(Math.random() * 2000.0);
                this.log("\u00a77\u0410\u043d\u0430\u043b\u0438\u0437\u0438\u0440\u0443\u044e \u0446\u0435\u043d\u044b (" + String.format(Locale.ROOT, "%.1f", Float.valueOf((float)(this.averageScanDeadline - now) / 1000.0f)) + " \u0441\u0435\u043a)...");
            }
            if (now < this.averageScanDeadline) {
                return;
            }
        }
        if (this.averagePageSignatures.contains(signature = this.getPageSignature(screen))) {
            if (now > this.averagePageDeadline) {
                this.prepareAverageSell();
            }
            return;
        }
        this.averagePageSignatures.add(signature);
        int previousCount = this.averagePriceCount;
        this.collectAveragePrices(screen);
        ++this.averagePages;
        this.log("\u00a77\u0421\u0442\u0440\u0430\u043d\u0438\u0446\u0430 " + this.averagePages + ": \u043d\u0430\u0439\u0434\u0435\u043d\u043e \u00a7f" + (this.averagePriceCount - previousCount) + " \u00a77\u0446\u0435\u043d");
        int n = maxPages = this.bypassFunTime.isValue() ? 1 : 5;
        if (this.averagePages >= maxPages) {
            this.prepareAverageSell();
            return;
        }
        class_1735 nextPage = this.findNextPageSlot(screen);
        if (nextPage == null || AuctionHelper.mc.field_1761 == null) {
            this.log("\u00a7e\u0421\u043b\u0435\u0434\u0443\u044e\u0449\u0435\u0439 \u0441\u0442\u0440\u0430\u043d\u0438\u0446\u044b \u043d\u0435\u0442, \u0441\u0447\u0438\u0442\u0430\u044e \u043f\u043e " + this.averagePages + " \u0441\u0442\u0440.");
            this.prepareAverageSell();
            return;
        }
        this.log("\u00a77\u041e\u0442\u043a\u0440\u044b\u0432\u0430\u044e \u0441\u0442\u0440\u0430\u043d\u0438\u0446\u0443 " + (this.averagePages + 1));
        AuctionHelper.mc.field_1761.method_2906(screen.method_17577().field_7763, nextPage.field_7874, 0, class_1713.field_7790, (class_1657)AuctionHelper.mc.field_1724);
        long pageTurnDelay = this.bypassFunTime.isValue() ? 220L : 400L;
        this.averageNextActionAt = now + pageTurnDelay;
        this.averagePageDeadline = now + 1200L;
    }

    private void collectAveragePrices(class_465<?> screen) {
        ArrayList<AverageCandidate> candidates = new ArrayList<AverageCandidate>();
        int containerSize = Math.max(0, screen.method_17577().field_7761.size() - 36);
        for (int i = 0; i < containerSize; ++i) {
            long totalPrice;
            class_1799 stack;
            class_1735 slot = screen.method_17577().method_7611(i);
            if (slot == null || !slot.method_7681() || !(stack = slot.method_7677()).method_31574(this.averageSellStack.method_7909()) || !this.sameAuctionVariant(stack, this.averageSellStack) || !this.similarEnchants(stack, this.averageSellStack)) continue;
            List tooltip = stack.method_7950(class_1792.class_9635.method_59528((class_1937)AuctionHelper.mc.field_1687), (class_1657)AuctionHelper.mc.field_1724, (class_1836)(AuctionHelper.mc.field_1690.field_1827 ? class_1836.field_41071 : class_1836.field_41070));
            if (AuctionListingUtil.isUnavailable(stack, tooltip) || (totalPrice = this.extractPrice(tooltip)) <= 0L) continue;
            int quantity = Math.max(1, this.extractQuantity(tooltip, stack));
            double pricePerUnit = (double)totalPrice / (double)quantity;
            candidates.add(new AverageCandidate(pricePerUnit, totalPrice, quantity));
        }
        candidates.sort((a, b) -> Double.compare(a.pricePerUnit(), b.pricePerUnit()));
        int limit = Math.min(5, candidates.size());
        for (int i = 0; i < limit; ++i) {
            AverageCandidate candidate = (AverageCandidate)candidates.get(i);
            this.averagePriceSum += candidate.pricePerUnit();
            ++this.averagePriceCount;
        }
    }

    private long getPageSignature(class_465<?> screen) {
        long hash = 1125899906842597L;
        int containerSize = Math.max(0, screen.method_17577().field_7761.size() - 36);
        for (int i = 0; i < containerSize; ++i) {
            class_1735 slot = screen.method_17577().method_7611(i);
            if (slot == null || !slot.method_7681()) continue;
            class_1799 stack = slot.method_7677();
            hash = 31L * hash + (long)i;
            hash = 31L * hash + (long)stack.method_7909().hashCode();
            hash = 31L * hash + (long)stack.method_7947();
            hash = 31L * hash + (long)stack.method_7964().getString().hashCode();
            hash = 31L * hash + (long)stack.method_57353().hashCode();
            List<class_2561> tooltip = stack.method_7950(class_1792.class_9635.method_59528((class_1937)AuctionHelper.mc.field_1687), (class_1657)AuctionHelper.mc.field_1724, (class_1836)(AuctionHelper.mc.field_1690.field_1827 ? class_1836.field_41071 : class_1836.field_41070));
            for (class_2561 line : tooltip) {
                hash = 31L * hash + (long)line.getString().hashCode();
            }
        }
        return hash;
    }

    private class_1735 findNextPageSlot(class_465<?> screen) {
        int containerSize = Math.max(0, screen.method_17577().field_7761.size() - 36);
        for (int i = 0; i < containerSize; ++i) {
            class_1735 slot = screen.method_17577().method_7611(i);
            if (slot == null || !slot.method_7681()) continue;
            class_1799 stack = slot.method_7677();
            StringBuilder description = new StringBuilder(stack.method_7964().getString().toLowerCase());
            stack.method_7950(class_1792.class_9635.method_59528((class_1937)AuctionHelper.mc.field_1687), (class_1657)AuctionHelper.mc.field_1724, (class_1836)class_1836.field_41070).forEach(line -> description.append(' ').append(line.getString().toLowerCase()));
            String value = description.toString();
            if (!value.contains("\u0441\u043b\u0435\u0434\u0443\u044e\u0449\u0430\u044f") && !value.contains("\u0432\u043f\u0435\u0440\u0451\u0434") && !value.contains("\u0432\u043f\u0435\u0440\u0435\u0434") && !value.contains("next page") && !value.contains("next")) continue;
            return slot;
        }
        return null;
    }

    private void prepareAverageSell() {
        if (this.averagePriceCount == 0) {
            if (this.autoSellActive) {
                this.rejectCurrentAutoSellItem("\u0434\u043b\u044f \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u043e \u0446\u0435\u043d");
                return;
            }
            this.cancelAverageSell("\u00a7c[AH] \u041d\u0430 \u0441\u0442\u0440\u0430\u043d\u0438\u0446\u0430\u0445 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u043e \u0446\u0435\u043d", true);
            return;
        }
        if (this.bypassFunTime.isValue()) {
            long sellPrice = Math.max(1L, Math.round(this.averagePriceSum / (double)this.averagePriceCount * (double)Math.max(1, this.averageSellStack.method_7947())));
            int digits = String.valueOf(sellPrice).length();
            long typingDelay = 2000L + Math.min(2000L, (long)digits * 250L) + (long)(Math.random() * 1000.0);
            typingDelay = Math.max(2000L, Math.min(5000L, typingDelay));
            this.averageSellState = AverageSellState.FINALIZING;
            this.averageNextActionAt = System.currentTimeMillis() + typingDelay;
            if (AuctionHelper.mc.field_1724 != null) {
                AuctionHelper.mc.field_1724.method_7346();
            }
            this.log("\u00a77\u0412\u044b\u0441\u0442\u0430\u0432\u043b\u0435\u043d\u0438\u0435 \u0447\u0435\u0440\u0435\u0437 " + String.format(Locale.ROOT, "%.1f", Float.valueOf((float)typingDelay / 1000.0f)) + " \u0441\u0435\u043a...");
        } else {
            this.averageSellState = AverageSellState.FINALIZING;
            this.averageNextActionAt = System.currentTimeMillis() + 250L;
            if (AuctionHelper.mc.field_1724 != null) {
                AuctionHelper.mc.field_1724.method_7346();
            }
        }
    }

    private void finishAverageSell() {
        if (AuctionHelper.mc.field_1724 == null || this.averagePriceCount == 0 || this.averageSellStack.method_7960()) {
            this.clearAverageSell();
            return;
        }
        long sellPrice = Math.max(1L, Math.round(this.averagePriceSum / (double)this.averagePriceCount * (double)Math.max(1, this.averageSellStack.method_7947())));
        if (this.autoSellActive) {
            this.autoSellLastSellPrice = sellPrice;
        }
        double unitPrice = this.averagePriceSum / (double)this.averagePriceCount;
        String cacheKey = this.getAveragePriceCacheKey(this.averageSellStack);
        if (!cacheKey.isEmpty()) {
            this.averagePriceCache.put(cacheKey, new PriceCacheEntry(unitPrice, System.currentTimeMillis()));
        }
        AuctionHelper.mc.field_1724.field_3944.method_45730("ah sell " + sellPrice);
        if (this.autoSellActive) {
            this.autoSellState = AutoSellState.WAITING_SELL_RESULT;
            this.autoSellNextActionAt = System.currentTimeMillis() + 1200L;
        }
        this.clientMessage((class_2561)class_2561.method_43470((String)("\u00a7a[AH] \u041f\u0440\u043e\u0434\u0430\u0436\u0430 \u043f\u043e \u0441\u0440\u0435\u0434\u043d\u0435\u0439 \u0446\u0435\u043d\u0435: \u00a7f" + sellPrice + " \u00a77(" + this.averagePriceCount + " \u043b\u043e\u0442\u043e\u0432, " + this.averagePages + " \u0441\u0442\u0440.)")));
        this.clearAverageSell();
    }

    private void cancelAverageSell(String message) {
        this.cancelAverageSell(message, false);
    }

    private void cancelAverageSell(String message, boolean closeAuction) {
        if (closeAuction && AuctionHelper.mc.field_1724 != null && AuctionHelper.mc.field_1755 instanceof class_465) {
            AuctionHelper.mc.field_1724.method_7346();
        }
        this.clientMessage((class_2561)class_2561.method_43470((String)message));
        this.clearAverageSell();
        if (this.autoSellActive) {
            this.stopAutoSell("\u00a7c[AutoSell] \u041e\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u043e \u0438\u0437-\u0437\u0430 \u043e\u0448\u0438\u0431\u043a\u0438 \u043f\u0440\u043e\u0434\u0430\u0436\u0438");
        }
    }

    private void log(String message) {
        if (AuctionHelper.mc.field_1724 != null) {
            this.clientMessage((class_2561)class_2561.method_43470((String)("\u00a78[\u00a7aAH\u00a78] " + message)));
        }
    }

    private void clientMessage(class_2561 message) {
        if (AuctionHelper.mc.field_1724 != null) {
            AuctionHelper.mc.field_1724.method_7353(QuickLogger.clientMessage(message), false);
        }
    }

    private void clearAverageSell() {
        this.averageSellState = AverageSellState.IDLE;
        this.averageSellStack = class_1799.field_8037;
        this.averagePageSignatures.clear();
        this.averagePriceSum = 0.0;
        this.averagePriceCount = 0;
        this.averagePages = 0;
        this.averageAuctionOpened = false;
        this.averageLastSeenAuction = 0L;
        this.averageScanDeadline = 0L;
        this.averagePendingCommand = null;
    }

    private int extractQuantity(List<class_2561> tooltip, class_1799 stack) {
        for (class_2561 lineText : tooltip) {
            String digits;
            String s = lineText.getString().toLowerCase();
            if (!s.contains("\u043a\u043e\u043b\u0438\u0447") && !s.contains("\u043a\u043e\u043b-\u0432\u043e") && !s.contains("amount") && !s.contains("count") && !s.contains("quantity") || (digits = s.replaceAll("[^0-9]", "")).isEmpty()) continue;
            try {
                int q = Integer.parseInt(digits);
                if (q <= 0) continue;
                return q;
            }
            catch (NumberFormatException numberFormatException) {
            }
        }
        return Math.max(1, stack.method_7947());
    }

    private boolean similarEnchants(class_1799 a, class_1799 b) {
        class_9304 ea = class_1890.method_57532((class_1799)a);
        class_9304 eb = class_1890.method_57532((class_1799)b);
        if (ea.method_57541() != eb.method_57541()) {
            return false;
        }
        for (class_6880 entry : ea.method_57534()) {
            if (ea.method_57536(entry) == eb.method_57536(entry)) continue;
            return false;
        }
        return true;
    }

    private boolean sameAuctionVariant(class_1799 auctionStack, class_1799 sellingStack) {
        if (auctionStack.method_31574(class_1802.field_8575) && sellingStack.method_31574(class_1802.field_8575)) {
            String auctionSphere = this.sphereIdentity(auctionStack);
            String sellingSphere = this.sphereIdentity(sellingStack);
            if (!auctionSphere.isEmpty() || !sellingSphere.isEmpty()) {
                return !auctionSphere.isEmpty() && auctionSphere.equals(sellingSphere);
            }
            return Objects.equals(auctionStack.method_58694(class_9334.field_49617), sellingStack.method_58694(class_9334.field_49617)) && this.sameCustomName(auctionStack, sellingStack);
        }
        if (auctionStack.method_7909() instanceof class_1812 && sellingStack.method_7909() instanceof class_1812) {
            return Objects.equals(auctionStack.method_58694(class_9334.field_49651), sellingStack.method_58694(class_9334.field_49651)) && this.sameCustomName(auctionStack, sellingStack);
        }
        if (!auctionStack.method_31574(class_1802.field_22024) || !sellingStack.method_31574(class_1802.field_22024)) {
            return true;
        }
        return this.isTrapPickaxe(auctionStack) == this.isTrapPickaxe(sellingStack);
    }

    private boolean sameCustomName(class_1799 first, class_1799 second) {
        return Objects.equals(first.method_58694(class_9334.field_49631), second.method_58694(class_9334.field_49631));
    }

    private String sphereIdentity(class_1799 stack) {
        String normalized;
        if (stack == null || stack.method_7960() || !stack.method_31574(class_1802.field_8575)) {
            return "";
        }
        String donationId = this.sphereDonationId(stack);
        if (!donationId.isEmpty()) {
            return donationId;
        }
        String visibleName = this.cleanSphereName(stack.method_7964().getString()).toLowerCase(Locale.ROOT);
        if (visibleName.contains("\u0441\u0444\u0435\u0440\u0430") || visibleName.contains("sphere")) {
            return visibleName;
        }
        class_2561 customName = (class_2561)stack.method_58694(class_9334.field_49631);
        if (customName != null && ((normalized = this.cleanSphereName(customName.getString()).toLowerCase(Locale.ROOT)).contains("\u0441\u0444\u0435\u0440\u0430") || normalized.contains("sphere"))) {
            return normalized;
        }
        return "";
    }

    private String sphereDonationId(class_1799 stack) {
        class_9279 component = (class_9279)stack.method_58694(class_9334.field_49628);
        if (component == null) {
            return "";
        }
        class_2487 customData = component.method_57461();
        String donationId = customData.method_10558("don-item").orElse("").trim().toLowerCase(Locale.ROOT);
        return donationId.contains("sphere") ? donationId : "";
    }

    private String cleanSphereName(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            return "";
        }
        return rawName.replaceAll("\u00a7[0-9a-fk-orxX]", "").replaceAll("[^\\p{L}\\p{N} ]", " ").replaceAll("\\s+", " ").trim();
    }

    private boolean isTrapPickaxe(class_1799 stack) {
        if (stack == null || stack.method_7960() || !stack.method_31574(class_1802.field_22024)) {
            return false;
        }
        String name = stack.method_7964().getString().replaceAll("\u00a7[0-9a-fk-orxX]", "").toLowerCase(Locale.ROOT);
        return name.contains("\u0442\u0440\u0430\u043f\u043a");
    }

    private void appendPerUnitTooltip(class_1799 stack, List<class_2561> lines) {
        class_465 screen;
        if (this.isScanningPage || !this.isState() || stack == null || stack.method_7960() || lines == null || lines.isEmpty()) {
            return;
        }
        class_437 class_4372 = AuctionHelper.mc.field_1755;
        if (!(class_4372 instanceof class_465) || !this.isAuctionScreen(screen = (class_465)class_4372)) {
            return;
        }
        int priceLineIndex = -1;
        long totalPrice = -1L;
        for (int i = 0; i < lines.size(); ++i) {
            String digits;
            String line = lines.get(i).getString();
            if (!this.isPriceLine(line) || (digits = line.replaceAll("[^\\d]", "")).isEmpty()) continue;
            try {
                totalPrice = Long.parseLong(digits);
                priceLineIndex = i;
                break;
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        if (priceLineIndex < 0 || totalPrice <= 0L) {
            return;
        }
        int quantity = this.extractQuantity(lines, stack);
        if (quantity <= 1) {
            return;
        }
        long perUnit = Math.max(1L, totalPrice / (long)quantity);
        lines.add(priceLineIndex + 1, (class_2561)class_2561.method_43470((String)("\u00a7a$ \u00a7f\u0426\u0435\u043d\u0430 \u0437\u0430 \u0448\u0442\u0443\u043a\u0443: \u00a7a" + this.formatPrice(perUnit))));
    }

    private String formatPrice(long price) {
        return String.format(Locale.US, "%,d", price);
    }

    private String resolveSearchName(class_1799 stack) {
        String special = this.specialSearchName(stack);
        if (special != null) {
            return special;
        }
        String tier = this.tierSearchName(stack.method_7964().getString());
        return tier != null ? tier : this.cleanItemName(stack.method_7964().getString());
    }

    private String specialSearchName(class_1799 stack) {
        int level;
        if (stack == null || stack.method_7960()) {
            return null;
        }
        String rawLower = stack.method_7964().getString().toLowerCase(Locale.ROOT).replace('\u0451', '\u0435');
        if (rawLower.contains("\u043a\u0440\u0443\u0448\u0438\u0442\u0435\u043b")) {
            if (stack.method_31574(class_1802.field_22027)) {
                return "\u0428\u043b\u0435\u043c \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f";
            }
            if (stack.method_31574(class_1802.field_22028)) {
                return "\u041d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f";
            }
            if (stack.method_31574(class_1802.field_22029)) {
                return "\u041f\u043e\u043d\u043e\u0436\u0438 \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f";
            }
            if (stack.method_31574(class_1802.field_22030)) {
                return "\u0411\u043e\u0442\u0438\u043d\u043a\u0438 \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f";
            }
            if (stack.method_31574(class_1802.field_22022)) {
                return "\u041c\u0435\u0447 \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f";
            }
            if (stack.method_31574(class_1802.field_22024)) {
                return "\u041a\u0438\u0440\u043a\u0430 \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f";
            }
            if (stack.method_31574(class_1802.field_8102)) {
                return "\u041b\u0443\u043a \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f";
            }
            if (stack.method_31574(class_1802.field_8399)) {
                return "\u0410\u0440\u0431\u0430\u043b\u0435\u0442 \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f";
            }
            if (stack.method_31574(class_1802.field_8547)) {
                return "\u0422\u0440\u0435\u0437\u0443\u0431\u0435\u0446 \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f";
            }
            if (stack.method_31574(class_1802.field_49814)) {
                return "\u0411\u0443\u043b\u0430\u0432\u0430 \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f";
            }
            if (stack.method_31574(class_1802.field_8833)) {
                return "\u042d\u043b\u0438\u0442\u0440\u044b \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f";
            }
            if (stack.method_31574(class_1802.field_8378)) {
                return "\u0423\u0434\u043e\u0447\u043a\u0430 \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f";
            }
            if (stack.method_31574(class_1802.field_8288)) {
                return "\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f";
            }
        }
        if (stack.method_31574(class_1802.field_8288)) {
            if (rawLower.contains("\u0434\u0435\u043c\u043e\u043d")) {
                return "\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u0414\u0435\u043c\u043e\u043d\u0430";
            }
            if (rawLower.contains("\u0440\u0430\u0437\u0434\u043e\u0440")) {
                return "\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u0420\u0430\u0437\u0434\u043e\u0440\u0430";
            }
            if (rawLower.contains("\u044f\u0440\u043e\u0441\u0442")) {
                return "\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u042f\u0440\u043e\u0441\u0442\u0438";
            }
            if (rawLower.contains("\u043a\u0430\u0440\u0430\u0442\u0435\u043b")) {
                return "\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u041a\u0430\u0440\u0430\u0442\u0435\u043b\u044f";
            }
            if (rawLower.contains("\u0432\u0438\u0445\u0440")) {
                return "\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u0412\u0438\u0445\u0440\u044f";
            }
        }
        if (stack.method_31574(class_1802.field_8575)) {
            String visibleCustomName;
            String lowerCustomName;
            String visibleName = this.cleanSphereName(stack.method_7964().getString());
            String lowerVisibleName = visibleName.toLowerCase(Locale.ROOT);
            if (lowerVisibleName.contains("\u0441\u0444\u0435\u0440\u0430") || lowerVisibleName.contains("sphere")) {
                return visibleName;
            }
            class_2561 customName = (class_2561)stack.method_58694(class_9334.field_49631);
            if (customName != null && ((lowerCustomName = (visibleCustomName = this.cleanSphereName(customName.getString())).toLowerCase(Locale.ROOT)).contains("\u0441\u0444\u0435\u0440\u0430") || lowerCustomName.contains("sphere"))) {
                return visibleCustomName;
            }
            String donationId = this.sphereDonationId(stack);
            if (!donationId.isEmpty()) {
                String type;
                int marker = donationId.lastIndexOf("sphere-");
                return switch (type = donationId.substring(marker + "sphere-".length()).replace('_', '-')) {
                    case "hydra" -> "\u0421\u0444\u0435\u0440\u0430 \u0413\u0438\u0434\u0440\u044b";
                    case "ares" -> "\u0421\u0444\u0435\u0440\u0430 \u0410\u0440\u0435\u0441\u0430";
                    case "beast", "bestia" -> "\u0421\u0444\u0435\u0440\u0430 \u0411\u0435\u0441\u0442\u0438\u0438";
                    case "icar", "icarus" -> "\u0421\u0444\u0435\u0440\u0430 \u0418\u043a\u0430\u0440\u0430";
                    case "satyr" -> "\u0421\u0444\u0435\u0440\u0430 \u0421\u0430\u0442\u0438\u0440\u0430";
                    case "eris", "erida" -> "\u0421\u0444\u0435\u0440\u0430 \u042d\u0440\u0438\u0434\u0430";
                    case "chaos" -> "\u0421\u0444\u0435\u0440\u0430 \u0425\u0430\u043e\u0441\u0430";
                    case "titan" -> "\u0421\u0444\u0435\u0440\u0430 \u0422\u0438\u0442\u0430\u043d\u0430";
                    case "chimera" -> "\u0421\u0444\u0435\u0440\u0430 \u0425\u0438\u043c\u0435\u0440\u044b";
                    case "andromeda" -> "\u0421\u0444\u0435\u0440\u0430 \u0410\u043d\u0434\u0440\u043e\u043c\u0435\u0434\u044b";
                    case "pandora" -> "\u0421\u0444\u0435\u0440\u0430 \u041f\u0430\u043d\u0434\u043e\u0440\u044b";
                    default -> "\u0421\u0444\u0435\u0440\u0430 " + type.replace('-', ' ');
                };
            }
        }
        if (stack.method_31574(class_1802.field_8634)) {
            return "\u042d\u043d\u0434\u0435\u0440 \u0436\u0435\u043c\u0447\u0443\u0433";
        }
        if (stack.method_31574(class_1802.field_8287) && ((level = this.extractExperienceBottleLevel(stack)) == 15 || level == 30 || level == 45 || level == 50)) {
            return "\u041f\u0443\u0437\u044b\u0440\u0435\u043a \u0441 \u0443\u0440\u043e\u0432\u043d\u0435\u043c " + level;
        }
        return null;
    }

    private int extractExperienceBottleLevel(class_1799 stack) {
        Matcher matcher;
        StringBuilder text = new StringBuilder(stack.method_7964().getString());
        if (AuctionHelper.mc.field_1724 != null && AuctionHelper.mc.field_1687 != null) {
            stack.method_7950(class_1792.class_9635.method_59528((class_1937)AuctionHelper.mc.field_1687), (class_1657)AuctionHelper.mc.field_1724, (class_1836)(AuctionHelper.mc.field_1690.field_1827 ? class_1836.field_41071 : class_1836.field_41070)).forEach(line -> text.append(' ').append(line.getString()));
        }
        return (matcher = Pattern.compile("(15|30|45|50)").matcher(text.toString())).find() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    private String tierSearchName(String rawName) {
        String[][] colors;
        if (rawName == null) {
            return null;
        }
        String lower = rawName.toLowerCase().replaceAll("\u00a7[0-9a-fk-orxX]", "");
        if (!(lower.contains("tier") || lower.contains("\u0442\u0430\u0435\u0440") || lower.contains("\u0442\u0438\u0440"))) {
            return null;
        }
        for (String[] color : colors = new String[][]{{"white", "\u0432\u0430\u0439\u0442"}, {"\u0432\u0430\u0439\u0442", "\u0432\u0430\u0439\u0442"}, {"black", "\u0431\u043b\u044d\u043a"}, {"\u0431\u043b\u044d\u043a", "\u0431\u043b\u044d\u043a"}, {"\u0431\u043b\u0435\u043a", "\u0431\u043b\u044d\u043a"}, {"red", "\u0440\u0435\u0434"}, {"blue", "\u0431\u043b\u044e"}, {"green", "\u0433\u0440\u0438\u043d"}, {"yellow", "\u0435\u043b\u043b\u043e\u0443"}, {"purple", "\u043f\u0435\u0440\u043f\u043b"}, {"orange", "\u043e\u0440\u0438\u043d\u0434\u0436"}, {"gray", "\u0433\u0440\u0435\u0439"}, {"grey", "\u0433\u0440\u0435\u0439"}, {"pink", "\u043f\u0438\u043d\u043a"}, {"cyan", "\u0446\u0438\u0430\u043d"}, {"lime", "\u043b\u0430\u0439\u043c"}, {"brown", "\u0431\u0440\u0430\u0443\u043d"}}) {
            if (!lower.contains(color[0])) continue;
            return "\u0442\u0430\u0435\u0440 " + color[1];
        }
        return null;
    }

    private String cleanItemName(String rawName) {
        String word;
        int endIndex;
        String word2;
        int startIndex;
        if (rawName == null || rawName.isEmpty()) {
            return "";
        }
        String clearColor = rawName.replaceAll("\u00a7[0-9a-fk-orxX]", "");
        StringBuilder normalized = new StringBuilder(clearColor.length());
        for (int i = 0; i < clearColor.length(); ++i) {
            char c = clearColor.charAt(i);
            normalized.append(this.isItemNameCharacter(c) ? c : (char)' ');
        }
        String text = normalized.toString();
        String[] rawWords = text.trim().split("\\s+");
        ArrayList<String> words = new ArrayList<String>();
        for (String w : rawWords) {
            if (w.isEmpty()) continue;
            words.add(w);
        }
        if (words.isEmpty()) {
            return "";
        }
        for (startIndex = 0; startIndex < words.size() && (this.isJunkWord(word2 = (String)words.get(startIndex)) || !this.hasLetters(word2)); ++startIndex) {
        }
        if (startIndex >= words.size()) {
            startIndex = 0;
        }
        for (endIndex = words.size() - 1; endIndex > startIndex && (this.isJunkWord(word = (String)words.get(endIndex)) || !this.hasLetters(word)); --endIndex) {
        }
        ArrayList<String> cleanedWords = new ArrayList<String>();
        for (int i = startIndex; i <= endIndex; ++i) {
            String word3 = (String)words.get(i);
            if (i == startIndex) {
                word3 = word3.replaceFirst("^[^a-zA-Z\u0430-\u044f\u0410-\u042f\u0451\u0401]+", "");
            }
            if (i == endIndex) {
                word3 = word3.replaceFirst("[^a-zA-Z\u0430-\u044f\u0410-\u042f\u0451\u0401]+$", "");
            }
            if (word3.isEmpty()) continue;
            cleanedWords.add(word3);
        }
        return String.join((CharSequence)" ", cleanedWords).trim();
    }

    private boolean isItemNameCharacter(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c >= '\u0410' && c <= '\u042f' || c >= '\u0430' && c <= '\u044f' || c == '\u0401' || c == '\u0451' || c == '-' || c == ' ';
    }

    private boolean hasLetters(String word) {
        for (int i = 0; i < word.length(); ++i) {
            char c = word.charAt(i);
            if (!(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '\u0430' && c <= '\u044f' || c >= '\u0410' && c <= '\u042f' || c == '\u0451') && c != '\u0401') continue;
            return true;
        }
        return false;
    }

    private boolean isJunkWord(String word) {
        if (word == null || word.isEmpty()) {
            return true;
        }
        String lower = word.toLowerCase(Locale.ROOT);
        if (lower.length() >= 2) {
            boolean allCharactersSame = true;
            char firstChar = lower.charAt(0);
            for (int i = 1; i < lower.length(); ++i) {
                if (lower.charAt(i) == firstChar) continue;
                allCharactersSame = false;
                break;
            }
            if (allCharactersSame) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onDrawSlots(DrawSlotsEvent event) {
        class_465<?> screen = event.getScreen();
        if (!this.isAuctionScreen(screen)) {
            return;
        }
        long now = System.currentTimeMillis();
        long sig = this.computeContainerSignature(screen);
        if (sig != this.lastAnalyzedSignature) {
            this.lastAnalyzedSignature = sig;
            this.lastScanTime = now;
            this.scanAndAnalyzePage(screen);
        }
        if (this.pageItems.isEmpty()) {
            return;
        }
        long balance = this.considerBalance.isValue() ? this.getCachedScoreboardBalance() : Long.MAX_VALUE;
        class_332 context = event.getDrawContext();
        int drawn = 0;
        for (AuctionItem item : this.pageItems) {
            class_1735 slot;
            if (item.totalPrice() > balance || (slot = screen.method_17577().method_7611(item.slotId())) == null || !slot.method_7681() || AuctionListingUtil.isUnavailable(slot.method_7677(), this.getItemTooltipLines(slot.method_7677()))) continue;
            int color = drawn == 0 ? ColorUtil.getColor(0, 255, 0, 120) : ColorUtil.getColor(255, 255, 0, 120);
            context.method_25294(slot.field_7873, slot.field_7872, slot.field_7873 + 16, slot.field_7872 + 16, color);
            if (++drawn < 3) continue;
            break;
        }
    }

    @EventHandler
    public void onHandledScreen(HandledScreenEvent event) {
        class_437 class_4372 = AuctionHelper.mc.field_1755;
        if (!(class_4372 instanceof class_465)) {
            return;
        }
        class_465 screen = (class_465)class_4372;
        if (this.isAuctionStorageScreen(screen)) {
            int offsetX = (screen.field_22789 - event.getBackgroundWidth()) / 2;
            int offsetY = (screen.field_22790 - event.getBackgroundHeight()) / 2;
            this.drawStorageTotal(event.getDrawContext(), screen, event.getBackgroundWidth(), offsetX, offsetY);
        }
    }

    private long getCachedScoreboardBalance() {
        long now = System.currentTimeMillis();
        if (now - this.lastBalanceCheckTime < 500L) {
            return this.cachedScoreboardBalance;
        }
        this.lastBalanceCheckTime = now;
        this.cachedScoreboardBalance = AuctionHelper.readScoreboardBalance();
        return this.cachedScoreboardBalance;
    }

    public static long readScoreboardBalance() {
        class_310 client = class_310.method_1551();
        if (client.field_1687 == null) {
            return Long.MAX_VALUE;
        }
        class_269 scoreboard = client.field_1687.method_8428();
        class_266 objective = scoreboard.method_1189(class_8646.field_45157);
        if (objective == null) {
            return Long.MAX_VALUE;
        }
        for (class_9011 entry : scoreboard.method_1184(objective)) {
            String digits;
            String lower;
            String rawLine = class_268.method_1142((class_270)scoreboard.method_1164(entry.comp_2127()), (class_2561)entry.method_55387()).getString();
            String line = class_124.method_539((String)rawLine);
            if (line == null || line.isBlank() || !(lower = line.toLowerCase(Locale.ROOT)).contains("\u043c\u043e\u043d\u0435\u0442") && !lower.contains("\u0431\u0430\u043b\u0430\u043d\u0441") && !lower.contains("coins") && !lower.contains("money") && !lower.contains("$")) continue;
            Matcher matcher = SCOREBOARD_BALANCE_PATTERN.matcher(line);
            if (matcher.find() && !(digits = matcher.group(1).replaceAll("[^0-9]", "")).isEmpty()) {
                try {
                    return Long.parseLong(digits);
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
            }
            Matcher fallback = SCOREBOARD_DIGITS_PATTERN.matcher(line);
            while (fallback.find()) {
                String digits2 = fallback.group().replaceAll("[^0-9]", "");
                if (digits2.isEmpty()) continue;
                try {
                    return Long.parseLong(digits2);
                }
                catch (NumberFormatException numberFormatException) {
                }
            }
        }
        return Long.MAX_VALUE;
    }

    private boolean isAuctionStorageScreen(class_465<?> screen) {
        String lower = screen.method_25440().getString().toLowerCase(Locale.ROOT);
        return lower.contains("\u0445\u0440\u0430\u043d\u0438\u043b\u0438\u0449\u0435") || lower.contains("storage") || lower.contains("stash");
    }

    private void drawStorageTotal(class_332 context, class_465<?> screen, int backgroundWidth, int offsetX, int offsetY) {
        long total = this.calculateStorageTotal(screen);
        class_5250 text = class_2561.method_43470((String)"$ ").method_27692(class_124.field_1060).method_10852((class_2561)class_2561.method_43470((String)"\u0412\u0441\u0435\u0433\u043e: ").method_27692(class_124.field_1068)).method_10852((class_2561)class_2561.method_43470((String)this.formatPrice(total)).method_27692(class_124.field_1060));
        int titleCenterX = offsetX + backgroundWidth / 2;
        int x = titleCenterX + -85;
        int y = offsetY + 7;
        context.method_51439(AuctionHelper.mc.field_1772, (class_2561)text, x, y, -1, true);
    }

    private long calculateStorageTotal(class_465<?> screen) {
        long total = 0L;
        int containerSize = Math.max(0, screen.method_17577().field_7761.size() - 36);
        for (int i = 0; i < containerSize; ++i) {
            class_1799 stack;
            class_1735 slot = screen.method_17577().method_7611(i);
            if (slot == null || !slot.method_7681() || (stack = slot.method_7677()).method_7960() || this.isStorageUtilityItem(stack)) continue;
            List tooltip = stack.method_7950(class_1792.class_9635.method_59528((class_1937)AuctionHelper.mc.field_1687), (class_1657)AuctionHelper.mc.field_1724, (class_1836)(AuctionHelper.mc.field_1690.field_1827 ? class_1836.field_41071 : class_1836.field_41070));
            long price = this.extractPrice(tooltip);
            if (price <= 0L) continue;
            if (Long.MAX_VALUE - total < price) {
                return Long.MAX_VALUE;
            }
            total += price;
        }
        return total;
    }

    private boolean isStorageUtilityItem(class_1799 stack) {
        String name = stack.method_7964().getString().toLowerCase(Locale.ROOT);
        return name.contains("\u0441\u043b\u043e\u0436\u0438\u0442\u044c") || name.contains("\u0437\u0430\u0431\u0440\u0430\u0442\u044c") || name.contains("\u0432\u044b\u0431\u0440\u043e\u0441\u0438\u0442\u044c") || name.contains("\u043d\u0430\u0437\u0430\u0434") || name.contains("\u0432\u043f\u0435\u0440\u0451\u0434") || name.contains("\u0432\u043f\u0435\u0440\u0435\u0434") || name.contains("\u0441\u0442\u0440\u0430\u043d\u0438\u0446") || name.contains("next") || name.contains("back") || name.contains("take") || name.contains("drop") || name.contains("put");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void scanAndAnalyzePage(class_465<?> screen) {
        this.isScanningPage = true;
        try {
            this.pageItems.clear();
            this.minEffectivePrice = Double.MAX_VALUE;
            this.averageEffectivePrice = 0.0;
            double totalEffectivePrice = 0.0;
            int pricedItemCount = 0;
            int containerSize = Math.max(0, screen.method_17577().field_7761.size() - 36);
            for (int i = 0; i < containerSize; ++i) {
                long totalPrice;
                List<class_2561> tooltip;
                class_1799 stack;
                class_1735 slot = screen.method_17577().method_7611(i);
                if (slot == null || !slot.method_7681() || (stack = slot.method_7677()).method_7960() || i >= 45 && this.isAuctionUtilityName(class_124.method_539((String)stack.method_7964().getString())) || AuctionListingUtil.isUnavailable(stack, tooltip = this.getItemTooltipLines(stack)) || this.shouldHideItem(stack, tooltip) || (totalPrice = this.extractPrice(tooltip)) < 0L) continue;
                int count = Math.max(1, stack.method_7947());
                int maxDurability = stack.method_7936();
                int currentDurability = maxDurability > 0 ? maxDurability - stack.method_7919() : 0;
                double pricePerUnit = (double)totalPrice / (double)count;
                double durabilityFactor = 1.0;
                if (maxDurability > 0) {
                    durabilityFactor = (double)currentDurability / (double)maxDurability;
                    durabilityFactor = Math.max(0.1, durabilityFactor);
                }
                double effectivePrice = pricePerUnit / durabilityFactor;
                this.pageItems.add(new AuctionItem(slot.field_7874, totalPrice, count, maxDurability, currentDurability, effectivePrice));
                totalEffectivePrice += effectivePrice;
                ++pricedItemCount;
                if (!(effectivePrice < this.minEffectivePrice)) continue;
                this.minEffectivePrice = effectivePrice;
            }
            if (pricedItemCount > 0) {
                this.averageEffectivePrice = totalEffectivePrice / (double)pricedItemCount;
                this.pageItems.sort(Comparator.comparingDouble(AuctionItem::effectivePrice));
            }
        }
        finally {
            this.isScanningPage = false;
        }
    }

    private long computeContainerSignature(class_465<?> screen) {
        if (screen == null || screen.method_17577() == null) {
            return 0L;
        }
        long hash = 0x4000000000000L + (long)screen.method_17577().field_7763;
        int containerSlots = Math.max(0, screen.method_17577().field_7761.size() - 36);
        for (int i = 0; i < containerSlots; ++i) {
            class_1735 slot = screen.method_17577().method_7611(i);
            if (slot == null || !slot.method_7681()) {
                hash = 31L * hash;
                continue;
            }
            class_1799 stack = slot.method_7677();
            int itemId = class_1792.method_7880((class_1792)stack.method_7909());
            int count = stack.method_7947();
            int damage = stack.method_7919();
            int compHash = Objects.hashCode(stack.method_58694(class_9334.field_49632));
            hash = 31L * hash + ((long)itemId << 20) + ((long)count << 12) + (long)(damage ^ compHash);
        }
        return hash;
    }

    private List<class_2561> getItemTooltipLines(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return Collections.emptyList();
        }
        if (stack.method_7909() instanceof class_1812) {
            return stack.method_7950(class_1792.class_9635.field_51353, (class_1657)AuctionHelper.mc.field_1724, (class_1836)class_1836.field_41070);
        }
        class_9290 lore = (class_9290)stack.method_58694(class_9334.field_49632);
        if (lore != null && !lore.comp_2400().isEmpty()) {
            return lore.comp_2400();
        }
        return stack.method_7950(class_1792.class_9635.field_51353, (class_1657)AuctionHelper.mc.field_1724, (class_1836)class_1836.field_41070);
    }

    private boolean shouldHideItem(class_1799 stack, List<class_2561> tooltip) {
        class_1792 item = stack.method_7909();
        if (stack.method_7963() && this.minimumDurability.getValue() > 0.0f && (float)(stack.method_7936() - stack.method_7919()) / (float)stack.method_7936() < this.minimumDurability.getValue() / 100.0f) {
            return true;
        }
        if (this.enchantedOnly.isValue() && !stack.method_7942()) {
            return true;
        }
        if (this.countGroup.isValue()) {
            int requiredCount = this.countValue.getInt();
            if (this.countMode.isSelected("Exact") && stack.method_7947() != requiredCount) {
                return true;
            }
            if (this.countMode.isSelected("Minimum") && stack.method_7947() < requiredCount) {
                return true;
            }
        }
        if (this.armorGroup.isValue() && stack.method_58694(class_9334.field_54196) != null) {
            if (this.armorNoDurability.isValue() && stack.method_7936() > 0 && stack.method_7986()) {
                return true;
            }
            for (String required : this.armorEnchants.getSelected()) {
                if (this.armorHasEnchant(stack, required)) continue;
                return true;
            }
        }
        if (this.maceGroup.isValue() && stack.method_31574(class_1802.field_49814)) {
            for (String required : this.maceEnchants.getSelected()) {
                if (this.maceHasEnchant(stack, required)) continue;
                return true;
            }
        }
        if (stack.method_31573(class_3489.field_42614) && this.noSilkTouch.isValue() && !this.hasEnchantment(stack, (class_5321<class_1887>)class_1893.field_9099)) {
            return true;
        }
        if (item instanceof class_1812) {
            List<String> tooltipStrings = tooltip.stream().map(text -> class_124.method_539((String)text.getString()).toLowerCase(Locale.ROOT).replace('\u0451', '\u0435')).toList();
            if (this.noLevel3.isValue() && !this.hasLevel3Potion(tooltipStrings)) {
                return true;
            }
            if (this.noCombined.isValue() && !this.isCombinedPotion(tooltipStrings)) {
                return true;
            }
        }
        return false;
    }

    private String getSearchCommand() {
        return "ah search";
    }

    private boolean armorHasEnchant(class_1799 stack, String required) {
        return switch (required) {
            case "\u0417\u0430\u0449\u0438\u0442\u0430 5" -> {
                if (this.getEnchantmentLevel(stack, (class_5321<class_1887>)class_1893.field_9111) >= 5) {
                    yield true;
                }
                yield false;
            }
            case "\u041f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u044c 5" -> {
                if (this.getEnchantmentLevel(stack, (class_5321<class_1887>)class_1893.field_9119) >= 5) {
                    yield true;
                }
                yield false;
            }
            case "\u041f\u043e\u0447\u0438\u043d\u043a\u0430" -> this.hasEnchantment(stack, (class_5321<class_1887>)class_1893.field_9101);
            case "\u0428\u0438\u043f\u044b" -> this.hasEnchantment(stack, (class_5321<class_1887>)class_1893.field_9097);
            default -> true;
        };
    }

    private boolean maceHasEnchant(class_1799 stack, String required) {
        return switch (required) {
            case "\u041f\u0440\u043e\u0431\u0438\u0442\u0438\u0435 3" -> {
                if (this.getEnchantmentLevel(stack, (class_5321<class_1887>)class_1893.field_50158) >= 3) {
                    yield true;
                }
                yield false;
            }
            case "\u041f\u043b\u043e\u0442\u043d\u043e\u0441\u0442\u044c 5" -> {
                if (this.getEnchantmentLevel(stack, (class_5321<class_1887>)class_1893.field_50157) >= 5) {
                    yield true;
                }
                yield false;
            }
            default -> true;
        };
    }

    private long extractPrice(List<class_2561> tooltip) {
        if (tooltip == null || tooltip.isEmpty()) {
            return -1L;
        }
        long ftPrice = AuctionPriceParser.getFunTimeTooltipTotal(tooltip);
        if (ftPrice >= 0L) {
            return ftPrice;
        }
        long totalPrice = AuctionPriceParser.getTooltipTotal(tooltip);
        if (totalPrice >= 0L) {
            return totalPrice;
        }
        for (class_2561 lineText : tooltip) {
            String priceString;
            String raw = lineText.getString();
            String line = class_124.method_539((String)raw);
            if (line == null || line.isBlank() || AuctionPriceParser.isPerItemLine(line = line.toLowerCase(Locale.ROOT).replace('\u0451', '\u0435').trim()) || !this.isPriceLine(line) || (priceString = line.replaceAll("[^\\d]", "")).isEmpty()) continue;
            try {
                return Long.parseLong(priceString);
            }
            catch (NumberFormatException numberFormatException) {
            }
        }
        return -1L;
    }

    private boolean isPriceLine(String line) {
        String lower = line.toLowerCase(Locale.ROOT).replace('\u0451', '\u0435');
        return lower.contains("\u0446\u0435\u043d\u0430") || lower.contains("\u0441\u0442\u043e\u0438\u043c\u043e\u0441\u0442\u044c") || lower.contains("\u043a\u0443\u0440\u0441") || lower.contains("price") || lower.contains("cost") || lower.contains("buy") || lower.contains("$") || lower.contains("\u20bd") || lower.contains("\u043c\u043e\u043d\u0435\u0442");
    }

    private boolean hasLevel3Potion(List<String> tooltip) {
        for (String line : tooltip) {
            if (!line.contains("\u0441\u0438\u043b\u0430") && !line.contains("\u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c") || !line.contains("iii") && !line.contains("3") && !line.contains("\u0443\u0441\u0438\u043b\u0435\u043d\u043d")) continue;
            return true;
        }
        return false;
    }

    private boolean isCombinedPotion(List<String> tooltip) {
        boolean hasStrength = tooltip.stream().anyMatch(line -> line.contains("\u0441\u0438\u043b\u0430"));
        boolean hasSpeed = tooltip.stream().anyMatch(line -> line.contains("\u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c"));
        return hasStrength && hasSpeed;
    }

    private boolean hasEnchantment(class_1799 stack, class_5321<class_1887> key) {
        return this.getEnchantmentLevel(stack, key) > 0;
    }

    private int getEnchantmentLevel(class_1799 stack, class_5321<class_1887> key) {
        class_6880 entry = AuctionHelper.mc.field_1687.method_30349().method_30530(class_7924.field_41265).method_10223(key.method_29177()).orElse(null);
        return entry == null ? 0 : class_1890.method_8225((class_6880)entry, (class_1799)stack);
    }

    private boolean isAuctionByTitle(String screenTitle) {
        if (screenTitle == null) {
            return false;
        }
        String lower = class_124.method_539((String)screenTitle).toLowerCase(Locale.ROOT).replace('\u0451', '\u0435');
        return lower.contains("\u0430\u0443\u043a\u0446\u0438\u043e\u043d") || lower.contains("\u043f\u043e\u0438\u0441\u043a") || lower.contains("\u0431\u0438\u0440\u0436\u0430") || lower.contains("\u043a\u0430\u0442\u0435\u0433\u043e\u0440\u0438\u044f") || lower.contains("\u0440\u044b\u043d\u043e\u043a") || lower.contains("\u0442\u043e\u0432\u0430\u0440\u044b") || lower.contains("auction") || lower.contains("market") || lower.contains("category") || lower.contains("search");
    }

    private boolean isAuctionScreen(class_465<?> screen) {
        if (screen == null) {
            return false;
        }
        if (this.isAuctionByTitle(screen.method_25440().getString())) {
            return true;
        }
        int containerSlots = Math.max(0, screen.method_17577().field_7761.size() - 36);
        if (containerSlots < 45) {
            return false;
        }
        int utilitySlots = 0;
        for (int i = 45; i < Math.min(containerSlots, 54); ++i) {
            String name;
            class_1735 slot = screen.method_17577().method_7611(i);
            if (slot == null || !slot.method_7681() || !this.isAuctionUtilityName(name = class_124.method_539((String)slot.method_7677().method_7964().getString()))) continue;
            ++utilitySlots;
        }
        return utilitySlots >= 2;
    }

    private boolean isAuctionUtilityName(String name) {
        if (name == null) {
            return false;
        }
        String lower = name.toLowerCase(Locale.ROOT).replace('\u0451', '\u0435');
        return lower.contains("\u043e\u0431\u043d\u043e\u0432") || lower.contains("\u043d\u0430\u0437\u0430\u0434") || lower.contains("\u0432\u043f\u0435\u0440\u0435\u0434") || lower.contains("\u0441\u0442\u0440\u0430\u043d\u0438\u0446") || lower.contains("\u043f\u043e\u0438\u0441\u043a") || lower.contains("\u0430\u0443\u043a\u0446\u0438\u043e\u043d") || lower.contains("\u0431\u0438\u0440\u0436\u0430") || lower.contains("\u0442\u0430\u043b\u0438\u0441\u043c\u0430\u043d") || lower.contains("\u043f\u0440\u0435\u0434\u043c\u0435\u0442") || lower.contains("\u0448\u043c\u043e\u0442") || lower.contains("\u0444\u0438\u043b\u044c\u0442\u0440") || lower.contains("\u0441\u043e\u0440\u0442\u0438\u0440\u043e\u0432\u043a") || lower.contains("\u0431\u0430\u043b\u0430\u043d\u0441") || lower.contains("\u043c\u043e\u0438 \u043b\u043e\u0442\u044b") || lower.contains("refresh") || lower.contains("back") || lower.contains("next") || lower.contains("page") || lower.contains("search") || lower.contains("auction") || lower.contains("market") || lower.contains("filter");
    }

    private boolean isArmorContext() {
        String lower = this.title.toLowerCase(Locale.ROOT).replace('\u0451', '\u0435');
        return !lower.contains("\u043a\u0438\u0440\u043a\u0430") && !lower.contains("\u0441\u0438\u043b\u044b") && !lower.contains("\u0441\u043a\u043e\u0440\u043e\u0441\u0442\u0438");
    }

    private boolean isPickaxeContext() {
        String lower = this.title.toLowerCase(Locale.ROOT).replace('\u0451', '\u0435');
        return !lower.contains("\u0448\u043b\u0435\u043c") && !lower.contains("\u043d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a") && !lower.contains("\u043f\u043e\u043d\u043e\u0436\u0438") && !lower.contains("\u0431\u043e\u0442\u0438\u043d\u043a\u0438") && !lower.contains("\u0431\u0440\u043e\u043d\u044f") && !lower.contains("\u0441\u0438\u043b\u044b") && !lower.contains("\u0441\u043a\u043e\u0440\u043e\u0441\u0442\u0438");
    }

    private boolean isPotionContext() {
        String lower = this.title.toLowerCase(Locale.ROOT).replace('\u0451', '\u0435');
        return !lower.contains("\u0448\u043b\u0435\u043c") && !lower.contains("\u043d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a") && !lower.contains("\u043f\u043e\u043d\u043e\u0436\u0438") && !lower.contains("\u0431\u043e\u0442\u0438\u043d\u043a\u0438") && !lower.contains("\u0431\u0440\u043e\u043d\u044f") && !lower.contains("\u043a\u0438\u0440\u043a\u0430");
    }

    private void reset() {
        this.pageItems.clear();
        this.averageEffectivePrice = 0.0;
        this.minEffectivePrice = Double.MAX_VALUE;
        this.lastAnalyzedSignature = Long.MIN_VALUE;
    }

    private static enum AverageSellState {
        IDLE,
        WAITING_AUCTION,
        SCANNING,
        FINALIZING;

    }

    private static enum AutoSellState {
        IDLE,
        GOING_RESOURCE,
        RESOURCE_READY,
        GOING_SALE,
        SALE_READY,
        GOING_TRASH,
        TRASH_READY,
        OPENING_CONTAINER,
        LOOTING_CONTAINER,
        OPENING_TRASH,
        DUMPING_TRASH,
        SELL_NEXT,
        SELLING,
        RETRY_AUCTION,
        RETRY_SAME_PRICE,
        WAITING_SELL_RESULT,
        WAITING_SLOT;

    }

    private static final class ExpressionParser {
        private final String expression;
        private int position;

        private ExpressionParser(String expression) {
            this.expression = expression;
        }

        private double parse() {
            double value = this.parseExpression();
            this.skipSpaces();
            if (this.position != this.expression.length()) {
                throw new IllegalArgumentException("Unexpected character");
            }
            return value;
        }

        private double parseExpression() {
            double value = this.parseTerm();
            while (true) {
                this.skipSpaces();
                if (this.consume('+')) {
                    value += this.parseTerm();
                    continue;
                }
                if (!this.consume('-')) break;
                value -= this.parseTerm();
            }
            return value;
        }

        private double parseTerm() {
            double value = this.parseFactor();
            while (true) {
                this.skipSpaces();
                if (this.consume('*')) {
                    value *= this.parseFactor();
                    continue;
                }
                if (!this.consume('/')) break;
                double divisor = this.parseFactor();
                if (divisor == 0.0) {
                    throw new IllegalArgumentException("Division by zero");
                }
                value /= divisor;
            }
            return value;
        }

        private double parseFactor() {
            this.skipSpaces();
            if (this.consume('+')) {
                return this.parseFactor();
            }
            if (this.consume('-')) {
                return -this.parseFactor();
            }
            if (this.consume('(')) {
                double value = this.parseExpression();
                this.skipSpaces();
                if (!this.consume(')')) {
                    throw new IllegalArgumentException("Missing closing bracket");
                }
                return value;
            }
            int start = this.position;
            boolean decimalPoint = false;
            while (this.position < this.expression.length()) {
                char current = this.expression.charAt(this.position);
                if (Character.isDigit(current)) {
                    ++this.position;
                    continue;
                }
                if (current != '.' && current != ',' || decimalPoint) break;
                decimalPoint = true;
                ++this.position;
            }
            if (start == this.position) {
                throw new IllegalArgumentException("Expected number");
            }
            return Double.parseDouble(this.expression.substring(start, this.position).replace(',', '.'));
        }

        private boolean consume(char expected) {
            if (this.position < this.expression.length() && this.expression.charAt(this.position) == expected) {
                ++this.position;
                return true;
            }
            return false;
        }

        private void skipSpaces() {
            while (this.position < this.expression.length() && Character.isWhitespace(this.expression.charAt(this.position))) {
                ++this.position;
            }
        }
    }

    private record PriceCacheEntry(double unitPrice, long timestamp) {
    }

    private record AverageCandidate(double pricePerUnit, long totalPrice, int quantity) {
    }

    private record AuctionItem(int slotId, long totalPrice, int count, int maxDurability, int currentDurability, double effectivePrice) {
    }
}

