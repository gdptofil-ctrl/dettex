/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1297
 *  net.minecraft.class_1304
 *  net.minecraft.class_1542
 *  net.minecraft.class_156
 *  net.minecraft.class_1657
 *  net.minecraft.class_1735
 *  net.minecraft.class_1802
 *  net.minecraft.class_1923
 *  net.minecraft.class_2246
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_238
 *  net.minecraft.class_2382
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_2586
 *  net.minecraft.class_2591
 *  net.minecraft.class_2596
 *  net.minecraft.class_2818
 *  net.minecraft.class_2848
 *  net.minecraft.class_2848$class_2849
 *  net.minecraft.class_3532
 *  net.minecraft.class_3965
 *  net.minecraft.class_4587
 *  net.minecraft.class_7439
 */
package ru.recoin.implement.features.modules.misc;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1542;
import net.minecraft.class_156;
import net.minecraft.class_1657;
import net.minecraft.class_1735;
import net.minecraft.class_1802;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2586;
import net.minecraft.class_2591;
import net.minecraft.class_2596;
import net.minecraft.class_2818;
import net.minecraft.class_2848;
import net.minecraft.class_3532;
import net.minecraft.class_3965;
import net.minecraft.class_4587;
import net.minecraft.class_7439;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.ButtonSetting;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.common.QuickLogger;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.entity.ScreenWalkInventorySync;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.common.util.task.TaskPriority;
import ru.recoin.common.util.world.ServerUtil;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.RotationUpdateEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.DrawEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationConfig;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.misc.TelegramAPI;

public final class BaseFinder
extends Module {
    public final MultiSelectSetting blocks = new MultiSelectSetting("Blocks", "Targets considered a base").value("Chests", "Shulkers", "Barrels", "Beds", "Furnaces", "Spawners", "Obsidian", "Items", "Players", "Anvils", "Ender Chests").selected("Chests", "Shulkers", "Barrels", "Beds", "Furnaces", "Spawners", "Obsidian", "Items", "Players");
    public final ValueSetting chunkRadius = new ValueSetting("Chunk Radius", "Search radius").setValue(4.0f).range(1, 8).step(1.0f);
    public final SelectSetting workMode = new SelectSetting("Work Mode", "Finder workflow").value("Private Search", "Tonnel", "Elytra").selected("Elytra");
    public final ValueSetting minHeight = new ValueSetting("Min Height", "Minimum flight altitude").setValue(120.0f).range(60.0f, 250.0f).step(5.0f);
    public final ValueSetting maxHeight = new ValueSetting("Max Height", "Maximum flight altitude").setValue(150.0f).range(80.0f, 300.0f).step(5.0f);
    public final ValueSetting turnDistance = new ValueSetting("Turn Distance", "Distance before changing flight direction").setValue(500.0f).range(100.0f, 2000.0f).step(50.0f);
    private final Map<class_2338, FoundEntry> foundEntries = new ConcurrentHashMap<class_2338, FoundEntry>();
    private final Set<class_2338> foundPositions = ConcurrentHashMap.newKeySet();
    private final Set<Long> scannedObsidianChunks = ConcurrentHashMap.newKeySet();
    private final Map<String, Integer> recentCounts = new ConcurrentHashMap<String, Integer>();
    private final List<SpottedRecord> spottedRecords = new CopyOnWriteArrayList<SpottedRecord>();
    public final ButtonSetting openFile = new ButtonSetting("Bases File", "Opens bases.txt in notepad").setButtonName("Open").setRunnable(this::openBasesFile);
    public final ButtonSetting clearList = new ButtonSetting("Clear Screen", "Clears findings from screen").setButtonName("Clear").setRunnable(() -> {
        this.spottedRecords.clear();
        this.recentCounts.clear();
        this.message("\u00a7a\u0421\u043f\u0438\u0441\u043e\u043a \u043d\u0430\u0445\u043e\u0434\u043e\u043a \u043d\u0430 \u044d\u043a\u0440\u0430\u043d\u0435 \u043e\u0447\u0438\u0449\u0435\u043d.");
    });
    private State state = State.CHECK_SUPPLIES;
    private int scanTicks;
    private int entityScanTicks;
    private int delay;
    private int tunnelDistance;
    private int previousSlot = -1;
    private class_2338 tunnelStart;
    private class_2338 placedOre;
    private float originalYaw;
    private float originalPitch;
    private boolean elytraAscending = true;
    private int rocketCooldown = 0;
    private int supplyWarningCooldown = 0;
    private float flightYaw = 0.0f;
    private float targetYaw = 0.0f;
    private class_2338 lastTurnPos;
    private boolean flightYawInitialized = false;
    private TakeoffPhase takeoffPhase = TakeoffPhase.IDLE;
    private int takeoffTicks = 0;
    private long lastSpottedTime;
    private class_2338 lastSpottedPos;
    private String lastSpottedSummary = "";
    private long lastBatchAlertTime;

    public BaseFinder() {
        super("BaseFinder", "Base Finder", ModuleCategory.MISC);
        this.setup(this.blocks, this.chunkRadius, this.workMode, this.minHeight, this.maxHeight, this.turnDistance, this.openFile, this.clearList);
    }

    @Override
    public void activate() {
        this.foundEntries.clear();
        this.foundPositions.clear();
        this.scannedObsidianChunks.clear();
        this.recentCounts.clear();
        this.delay = 0;
        this.entityScanTicks = 0;
        this.scanTicks = 0;
        this.state = State.CHECK_SUPPLIES;
        this.placedOre = null;
        this.tunnelStart = null;
        this.previousSlot = -1;
        this.supplyWarningCooldown = 0;
        this.rocketCooldown = 0;
        this.flightYawInitialized = false;
        this.lastSpottedTime = 0L;
        this.lastSpottedPos = null;
        this.lastSpottedSummary = "";
        this.lastTurnPos = null;
        this.resetTakeoff();
        if (BaseFinder.mc.field_1724 != null) {
            this.targetYaw = this.flightYaw = BaseFinder.mc.field_1724.method_36454();
            this.lastTurnPos = BaseFinder.mc.field_1724.method_24515();
            this.flightYawInitialized = true;
            if (this.workMode.isSelected("Tonnel")) {
                this.command("#tunnel");
            } else if (this.workMode.isSelected("Elytra")) {
                this.checkAndEquipSupplies();
            }
        }
    }

    @Override
    public void deactivate() {
        this.resetTakeoff();
        RotationController.INSTANCE.release(this);
        if (BaseFinder.mc.field_1724 != null && (this.workMode.isSelected("Tonnel") || this.workMode.isSelected("Private Search"))) {
            this.command("#stop");
        }
    }

    @EventHandler
    public void onRotationUpdate(RotationUpdateEvent event) {
        if (event.getType() != 0) {
            return;
        }
        if (!this.isState() || !this.workMode.isSelected("Elytra") || BaseFinder.mc.field_1724 == null) {
            RotationController.INSTANCE.release(this);
            return;
        }
        if (!this.flightYawInitialized) {
            return;
        }
        boolean isGliding = BaseFinder.mc.field_1724.method_6128();
        if (!isGliding && this.takeoffPhase != TakeoffPhase.WAITING_GLIDE) {
            RotationController.INSTANCE.release(this);
            return;
        }
        float pitch = isGliding ? (this.elytraAscending ? -25.0f : 11.0f) : -20.0f;
        RotationController.INSTANCE.rotateTo(new Angle(this.flightYaw, pitch), 2, new RotationConfig(true, true), TaskPriority.HIGH_IMPORTANCE_3, this);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (BaseFinder.mc.field_1724 == null || BaseFinder.mc.field_1687 == null) {
            return;
        }
        if (!this.workMode.isSelected("Elytra") && this.stopForPlayer()) {
            return;
        }
        if (this.workMode.isSelected("Private Search")) {
            this.privateSearchTick();
        } else if (this.workMode.isSelected("Elytra")) {
            this.elytraFlightTick();
        }
        if (++this.entityScanTicks >= 5) {
            this.entityScanTicks = 0;
            this.scanEntities();
        }
        if (++this.scanTicks >= 10) {
            this.scanTicks = 0;
            this.scanChunks();
        }
        if (!this.recentCounts.isEmpty() && System.currentTimeMillis() - this.lastBatchAlertTime > 1500L) {
            this.lastBatchAlertTime = System.currentTimeMillis();
            this.triggerAlert();
        }
    }

    private boolean checkAndEquipSupplies() {
        class_1735 elytraSlot;
        if (BaseFinder.mc.field_1724 == null) {
            return false;
        }
        boolean hasElytra = BaseFinder.mc.field_1724.method_6118(class_1304.field_6174).method_31574(class_1802.field_8833);
        if (!hasElytra && (elytraSlot = PlayerInventoryUtil.getSlot(class_1802.field_8833)) != null) {
            ScreenWalkInventorySync.run(() -> PlayerInventoryUtil.moveItem(elytraSlot, 6, false, true));
            hasElytra = true;
        }
        boolean hasRockets = this.hasFireworks();
        return hasElytra && hasRockets;
    }

    private boolean hasFireworks() {
        if (BaseFinder.mc.field_1724 == null) {
            return false;
        }
        if (BaseFinder.mc.field_1724.method_6047().method_31574(class_1802.field_8639) || BaseFinder.mc.field_1724.method_6079().method_31574(class_1802.field_8639)) {
            return true;
        }
        return this.findFireworkSlot() != null;
    }

    private class_1735 findFireworkSlot() {
        return PlayerInventoryUtil.getSlot(class_1802.field_8639, Comparator.comparingInt(slot -> slot.field_7874 >= 36 && slot.field_7874 <= 44 ? 1 : 0), s -> true);
    }

    private void useFirework() {
        if (BaseFinder.mc.field_1724 == null) {
            return;
        }
        if (BaseFinder.mc.field_1724.method_6047().method_31574(class_1802.field_8639)) {
            PlayerIntersectionUtil.interactItem(class_1268.field_5808);
            BaseFinder.mc.field_1724.method_6104(class_1268.field_5808);
            return;
        }
        if (BaseFinder.mc.field_1724.method_6079().method_31574(class_1802.field_8639)) {
            PlayerIntersectionUtil.interactItem(class_1268.field_5810);
            BaseFinder.mc.field_1724.method_6104(class_1268.field_5810);
            return;
        }
        class_1735 fireworkSlot = this.findFireworkSlot();
        if (fireworkSlot == null) {
            return;
        }
        if (fireworkSlot.field_7874 >= 36 && fireworkSlot.field_7874 <= 44) {
            int prevSlot = BaseFinder.mc.field_1724.method_31548().method_67532();
            int targetSlot = fireworkSlot.field_7874 - 36;
            BaseFinder.mc.field_1724.method_31548().method_61496(targetSlot);
            PlayerIntersectionUtil.interactItem(class_1268.field_5808);
            BaseFinder.mc.field_1724.method_6104(class_1268.field_5808);
            BaseFinder.mc.field_1724.method_31548().method_61496(prevSlot);
            return;
        }
        ScreenWalkInventorySync.swapUseRestore(fireworkSlot, () -> {
            PlayerIntersectionUtil.interactItem(class_1268.field_5808);
            BaseFinder.mc.field_1724.method_6104(class_1268.field_5808);
        });
    }

    private void elytraFlightTick() {
        float yawDiff;
        double distSinceTurn;
        if (!this.checkAndEquipSupplies()) {
            this.resetTakeoff();
            return;
        }
        if (!this.flightYawInitialized) {
            this.targetYaw = this.flightYaw = BaseFinder.mc.field_1724.method_36454();
            this.lastTurnPos = BaseFinder.mc.field_1724.method_24515();
            this.flightYawInitialized = true;
        }
        if (this.rocketCooldown > 0) {
            --this.rocketCooldown;
        }
        if (!BaseFinder.mc.field_1724.method_6128()) {
            this.handleTakeoff();
            return;
        }
        this.resetTakeoff();
        if (this.lastTurnPos == null) {
            this.lastTurnPos = BaseFinder.mc.field_1724.method_24515();
        }
        if ((distSinceTurn = Math.hypot(BaseFinder.mc.field_1724.method_23317() - (double)this.lastTurnPos.method_10263(), BaseFinder.mc.field_1724.method_23321() - (double)this.lastTurnPos.method_10260())) >= (double)this.turnDistance.getValue()) {
            float turnSign = ThreadLocalRandom.current().nextBoolean() ? 1.0f : -1.0f;
            float turnAngle = (float)ThreadLocalRandom.current().nextDouble(30.0, 75.0);
            this.targetYaw = class_3532.method_15393((float)(this.targetYaw + turnSign * turnAngle));
            this.lastTurnPos = BaseFinder.mc.field_1724.method_24515();
        }
        this.flightYaw = Math.abs(yawDiff = class_3532.method_15393((float)(this.targetYaw - this.flightYaw))) > 3.0f ? class_3532.method_15393((float)(this.flightYaw + Math.signum(yawDiff) * Math.min(Math.abs(yawDiff), 3.5f))) : this.targetYaw;
        double currentY = BaseFinder.mc.field_1724.method_23318();
        float minH = this.minHeight.getValue();
        float maxH = this.maxHeight.getValue();
        if (currentY <= (double)minH) {
            this.elytraAscending = true;
        } else if (currentY >= (double)maxH) {
            this.elytraAscending = false;
        }
        float flightPitch = this.elytraAscending ? -25.0f : 11.0f;
        RotationController.INSTANCE.rotateTo(new Angle(this.flightYaw, flightPitch), 2, new RotationConfig(true, true), TaskPriority.HIGH_IMPORTANCE_3, this);
        if (this.elytraAscending) {
            double vy = BaseFinder.mc.field_1724.method_18798().field_1351;
            double horizSpeed = BaseFinder.mc.field_1724.method_18798().method_37267();
            if (this.rocketCooldown <= 0 && (vy < 0.25 || horizSpeed < 0.75)) {
                this.useFirework();
                this.rocketCooldown = 35;
            }
        }
    }

    private void handleTakeoff() {
        ++this.takeoffTicks;
        switch (this.takeoffPhase.ordinal()) {
            case 0: {
                if (BaseFinder.mc.field_1724.method_24828()) {
                    BaseFinder.mc.field_1690.field_1903.method_23481(true);
                    BaseFinder.mc.field_1724.method_6043();
                    this.takeoffPhase = TakeoffPhase.JUMPING;
                    this.takeoffTicks = 0;
                    break;
                }
                if (!(BaseFinder.mc.field_1724.field_6017 > (double)0.05f) && !(BaseFinder.mc.field_1724.method_18798().field_1351 < 0.0)) break;
                this.takeoffPhase = TakeoffPhase.WAITING_GLIDE;
                this.takeoffTicks = 0;
                break;
            }
            case 1: {
                BaseFinder.mc.field_1690.field_1903.method_23481(false);
                if (!(BaseFinder.mc.field_1724.method_18798().field_1351 <= 0.08) && !(BaseFinder.mc.field_1724.field_6017 > (double)0.04f) && this.takeoffTicks < 8) break;
                BaseFinder.mc.field_1724.method_23669();
                if (BaseFinder.mc.field_1724.field_3944 != null) {
                    BaseFinder.mc.field_1724.field_3944.method_52787((class_2596)new class_2848((class_1297)BaseFinder.mc.field_1724, class_2848.class_2849.field_12982));
                }
                this.takeoffPhase = TakeoffPhase.WAITING_GLIDE;
                this.takeoffTicks = 0;
                break;
            }
            case 2: {
                BaseFinder.mc.field_1690.field_1903.method_23481(false);
                if (BaseFinder.mc.field_1724.method_6128() || this.takeoffTicks >= 2) {
                    BaseFinder.mc.field_1724.method_23669();
                    RotationController.INSTANCE.rotateTo(new Angle(this.flightYaw, -20.0f), 2, new RotationConfig(true, true), TaskPriority.HIGH_IMPORTANCE_3, this);
                    this.useFirework();
                    this.rocketCooldown = 35;
                    this.elytraAscending = true;
                    this.resetTakeoff();
                    break;
                }
                if (!BaseFinder.mc.field_1724.method_24828() || this.takeoffTicks <= 12) break;
                this.resetTakeoff();
            }
        }
    }

    private void resetTakeoff() {
        this.takeoffPhase = TakeoffPhase.IDLE;
        this.takeoffTicks = 0;
        if (BaseFinder.mc.field_1690 != null && BaseFinder.mc.field_1690.field_1903 != null) {
            BaseFinder.mc.field_1690.field_1903.method_23481(false);
        }
    }

    private void scanEntities() {
        if (BaseFinder.mc.field_1687 == null || BaseFinder.mc.field_1724 == null) {
            return;
        }
        if (this.blocks.isSelected("Items")) {
            for (class_1297 entity : BaseFinder.mc.field_1687.method_18112()) {
                class_2338 pos;
                class_1542 item;
                if (!(entity instanceof class_1542) || (item = (class_1542)entity).method_31481() || !this.foundPositions.add(pos = item.method_24515())) continue;
                this.registerFound("\u041f\u0440\u0435\u0434\u043c\u0435\u0442\u044b (" + item.method_6983().method_7964().getString() + ")", pos, 1677786982);
            }
        }
        if (this.blocks.isSelected("Players")) {
            for (class_1657 player : BaseFinder.mc.field_1687.method_18456()) {
                class_2338 pos;
                if (player == BaseFinder.mc.field_1724 || FriendUtils.isFriend((class_1297)player) || player.method_7325() || !this.foundPositions.add(pos = player.method_24515())) continue;
                this.registerFound("\u0418\u0433\u0440\u043e\u043a (" + player.method_5477().getString() + ")", pos, 1694442018);
            }
        }
    }

    private void scanChunks() {
        if (BaseFinder.mc.field_1687 == null || BaseFinder.mc.field_1724 == null) {
            return;
        }
        class_1923 center = BaseFinder.mc.field_1724.method_31476();
        int radius = this.chunkRadius.getInt();
        for (int x = center.field_9181 - radius; x <= center.field_9181 + radius; ++x) {
            for (int z = center.field_9180 - radius; z <= center.field_9180 + radius; ++z) {
                class_1923 chunkPos;
                class_2818 chunk = BaseFinder.mc.field_1687.method_8497(x, z);
                if (chunk == null) continue;
                for (class_2586 entity : chunk.method_12214().values()) {
                    class_2338 pos2;
                    class_2591 type = entity.method_11017();
                    String name = null;
                    int color = 0;
                    if (type == class_2591.field_11914 || type == class_2591.field_11891) {
                        if (this.blocks.isSelected("Chests")) {
                            name = "\u0421\u0443\u043d\u0434\u0443\u043a";
                            color = 1694483028;
                        }
                    } else if (type == class_2591.field_11896) {
                        if (this.blocks.isSelected("Shulkers")) {
                            name = "\u0428\u0430\u043b\u043a\u0435\u0440";
                            color = 1693875067;
                        }
                    } else if (type == class_2591.field_16411) {
                        if (this.blocks.isSelected("Barrels")) {
                            name = "\u0411\u043e\u0447\u043a\u0430";
                            color = 1694163262;
                        }
                    } else if (type == class_2591.field_11910) {
                        if (this.blocks.isSelected("Beds")) {
                            name = "\u041a\u0440\u043e\u0432\u0430\u0442\u044c";
                            color = 1694455125;
                        }
                    } else if (type == class_2591.field_11903 || type == class_2591.field_16415 || type == class_2591.field_16414) {
                        if (this.blocks.isSelected("Furnaces")) {
                            name = "\u041f\u0435\u0447\u043a\u0430";
                            color = 1694472499;
                        }
                    } else if (type == class_2591.field_11889) {
                        if (this.blocks.isSelected("Spawners")) {
                            name = "\u0421\u043f\u0430\u0432\u043d\u0435\u0440";
                            color = 1681129471;
                        }
                    } else if (type == class_2591.field_11901 && this.blocks.isSelected("Ender Chests")) {
                        name = "\u042d\u043d\u0434\u0435\u0440-\u0441\u0443\u043d\u0434\u0443\u043a";
                        color = 1687761390;
                    }
                    if (name == null || !this.foundPositions.add(pos2 = entity.method_11016().method_10062())) continue;
                    this.registerFound(name, pos2, color);
                }
                if (!this.blocks.isSelected("Obsidian") || !this.scannedObsidianChunks.add((chunkPos = chunk.method_12004()).method_8324())) continue;
                chunk.method_51525(state -> state.method_27852(class_2246.field_10540) || state.method_27852(class_2246.field_22423), (pos, state) -> {
                    class_2338 imm = pos.method_10062();
                    if (this.foundPositions.add(imm)) {
                        this.registerFound("\u041e\u0431\u0441\u0438\u0434\u0438\u0430\u043d", imm, 1686647739);
                    }
                });
            }
        }
    }

    private void registerFound(String type, class_2338 pos, int color) {
        this.foundEntries.put(pos, new FoundEntry(type, pos, color, System.currentTimeMillis()));
        this.recentCounts.merge(type, 1, Integer::sum);
        this.lastSpottedPos = pos;
        this.lastSpottedTime = System.currentTimeMillis();
        if (System.currentTimeMillis() - this.lastBatchAlertTime > 1500L) {
            this.lastBatchAlertTime = System.currentTimeMillis();
            this.triggerAlert();
        }
    }

    private void triggerAlert() {
        if (this.recentCounts.isEmpty() || this.lastSpottedPos == null) {
            return;
        }
        StringBuilder summaryBuilder = new StringBuilder();
        this.recentCounts.forEach((name, count) -> {
            if (summaryBuilder.length() > 0) {
                summaryBuilder.append(", ");
            }
            summaryBuilder.append((String)name);
            if (count > 1) {
                summaryBuilder.append(" (x").append(count).append(")");
            }
        });
        String summary = summaryBuilder.toString();
        this.recentCounts.clear();
        String coordsStr = this.lastSpottedPos.method_10263() + " " + this.lastSpottedPos.method_10264() + " " + this.lastSpottedPos.method_10260();
        Object anarchyStr = ServerUtil.anarchy > 0 ? "\u0410\u043d\u0430\u0440\u0445\u0438\u044f #" + ServerUtil.anarchy : ServerUtil.server;
        String nickStr = BaseFinder.mc.field_1724 != null ? BaseFinder.mc.field_1724.method_5477().getString() : "Unknown";
        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        boolean updated = false;
        for (int i = 0; i < this.spottedRecords.size(); ++i) {
            SpottedRecord existing = this.spottedRecords.get(i);
            if (existing.pos().method_19455((class_2382)this.lastSpottedPos) >= 48) continue;
            this.spottedRecords.set(i, new SpottedRecord(summary, existing.pos(), (String)anarchyStr, nickStr, timeStr));
            updated = true;
            break;
        }
        if (!updated) {
            this.spottedRecords.add(new SpottedRecord(summary, this.lastSpottedPos, (String)anarchyStr, nickStr, timeStr));
        }
        this.message("\u00a7a\u0417\u0430\u043c\u0435\u0447\u0435\u043d: \u00a7f" + summary + " \u00a7a\u043d\u0430 XYZ: \u00a7f" + coordsStr + " \u00a77(" + (String)anarchyStr + ")");
        this.notifyTelegram("BaseFinder: " + summary + "\nXYZ: " + coordsStr + "\nServer: " + (String)anarchyStr + "\nNick: " + nickStr);
        this.logToFile(coordsStr, (String)anarchyStr, nickStr, summary);
    }

    public static File getBasesFile() {
        File file;
        File dir = new File(BaseFinder.mc.field_1697, "recoin");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!(file = new File(dir, "bases.txt")).exists()) {
            try {
                file.createNewFile();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return file;
    }

    public void openBasesFile() {
        try {
            File file = BaseFinder.getBasesFile();
            class_156.method_668().method_672(file);
        }
        catch (Exception e) {
            this.message("\u00a7c\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043e\u0442\u043a\u0440\u044b\u0442\u044c bases.txt: " + e.getMessage());
        }
    }

    private void logToFile(String coords, String anarchy, String nick, String summary) {
        try {
            File file = BaseFinder.getBasesFile();
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String line = String.format("[%s] [\u041d\u0438\u043a: %s] [\u0410\u043d\u0430\u0440\u0445\u0438\u044f: %s] [XYZ: %s] \u0417\u0430\u043c\u0435\u0447\u0435\u043d\u043e: %s", time, nick, anarchy, coords, summary);
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8, true));){
                writer.println(line);
                writer.flush();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @EventHandler
    public void onDraw(DrawEvent event) {
        int startIdx;
        if (this.spottedRecords.isEmpty() || BaseFinder.mc.field_1705 == null) {
            return;
        }
        int bossCount = BaseFinder.mc.field_1705.method_1740() != null ? BaseFinder.mc.field_1705.method_1740().field_2060.size() : 0;
        float y = bossCount > 0 ? (float)(bossCount * 22 + 14) : 15.0f;
        float centerX = (float)window.getScaledWidth() / 2.0f;
        FontRenderer font = Fonts.getSize(12, Fonts.Type.INTER_BOLD);
        FontRenderer subFont = Fonts.getSize(10, Fonts.Type.INTER_BOLD);
        class_4587 matrix = MatrixStackCompat.from(event.getDrawContext().method_51448());
        int maxItems = 10;
        for (int i = startIdx = Math.max(0, this.spottedRecords.size() - maxItems); i < this.spottedRecords.size(); ++i) {
            SpottedRecord record = this.spottedRecords.get(i);
            String line1 = "\u0417\u0410\u041c\u0415\u0427\u0415\u041d: " + record.summary() + " | XYZ: " + record.pos().method_10263() + " " + record.pos().method_10264() + " " + record.pos().method_10260();
            String line2 = record.anarchy() + " | \u041d\u0438\u043a: " + record.nick() + " | " + record.time();
            font.drawString(matrix, line1, centerX - font.getStringWidth(line1) / 2.0f, y, -1);
            subFont.drawString(matrix, line2, centerX - subFont.getStringWidth(line2) / 2.0f, y + 9.0f, -1);
            y += 18.0f;
        }
    }

    @EventHandler
    public void onRender(WorldRenderEvent event) {
        for (FoundEntry entry : this.foundEntries.values()) {
            Render3DUtil.drawBox(new class_238(entry.pos()), entry.color(), 1.5f, true, true, false);
        }
    }

    private void privateSearchTick() {
        if (this.delay-- > 0) {
            return;
        }
        switch (this.state.ordinal()) {
            case 0: {
                int ore = this.emeraldOreSlot();
                if (ore < 0) {
                    return;
                }
                this.tunnelStart = BaseFinder.mc.field_1724.method_24515();
                this.tunnelDistance = ThreadLocalRandom.current().nextInt(20, 30);
                this.command("#tunnel");
                this.state = State.TUNNELING;
                break;
            }
            case 1: {
                if (this.tunnelStart == null || BaseFinder.mc.field_1724.method_24515().method_19455((class_2382)this.tunnelStart) < this.tunnelDistance) break;
                this.command("#stop");
                this.state = State.STOPPING;
                this.delay = 10;
                break;
            }
            case 2: {
                this.originalYaw = BaseFinder.mc.field_1724.method_36454();
                this.originalPitch = BaseFinder.mc.field_1724.method_36455();
                this.previousSlot = BaseFinder.mc.field_1724.method_31548().method_67532();
                int ore = this.emeraldOreSlot();
                if (ore < 0) {
                    this.message("\u00a7cEmerald ore is over; waiting for supplies");
                    this.state = State.CHECK_SUPPLIES;
                    break;
                }
                BaseFinder.mc.field_1724.method_31548().method_61496(ore);
                this.state = State.PLACING;
                this.delay = 5;
                break;
            }
            case 3: {
                this.placedOre = this.tryPlaceOre();
                if (this.placedOre == null) {
                    this.restoreSlot();
                    this.state = State.RESUMING;
                    this.delay = 5;
                    break;
                }
                this.state = State.WAITING_CHAT;
                this.delay = 10;
                break;
            }
            case 4: {
                this.state = State.BREAKING;
                break;
            }
            case 5: {
                this.restoreSlot();
                if (this.placedOre != null && !BaseFinder.mc.field_1687.method_8320(this.placedOre).method_26215()) {
                    this.lookAt(class_243.method_24953((class_2382)this.placedOre));
                    BaseFinder.mc.field_1761.method_2902(this.placedOre, class_2350.field_11036);
                    BaseFinder.mc.field_1724.method_6104(class_1268.field_5808);
                    return;
                }
                this.state = State.RESUMING;
                this.delay = 5;
                break;
            }
            case 6: {
                BaseFinder.mc.field_1724.method_36456(this.originalYaw);
                BaseFinder.mc.field_1724.method_36457(this.originalPitch);
                this.tunnelStart = BaseFinder.mc.field_1724.method_24515();
                this.tunnelDistance = ThreadLocalRandom.current().nextInt(20, 30);
                this.command("#tunnel");
                this.state = State.TUNNELING;
            }
        }
    }

    private class_2338 tryPlaceOre() {
        class_2338[] positions;
        class_2350 facing = BaseFinder.mc.field_1724.method_5735();
        class_2338 base = BaseFinder.mc.field_1724.method_24515();
        for (class_2338 pos : positions = new class_2338[]{base.method_10093(facing.method_10160()).method_10084(), base.method_10093(facing.method_10170()).method_10084(), base.method_10093(facing.method_10160()), base.method_10093(facing.method_10170()), base.method_10093(facing).method_10084(), base.method_10093(facing)}) {
            if (!this.placeAt(pos)) continue;
            return pos;
        }
        this.message("\u00a77No valid position for the test block");
        return null;
    }

    private boolean placeAt(class_2338 pos) {
        if (!BaseFinder.mc.field_1687.method_8320(pos).method_26215()) {
            return false;
        }
        for (class_2350 direction : class_2350.values()) {
            class_2338 support = pos.method_10093(direction);
            if (BaseFinder.mc.field_1687.method_8320(support).method_26215()) continue;
            class_2350 side = direction.method_10153();
            class_243 hit = class_243.method_24953((class_2382)support).method_1019(class_243.method_24954((class_2382)side.method_62675()).method_1021(0.5));
            this.lookAt(hit);
            BaseFinder.mc.field_1761.method_2896(BaseFinder.mc.field_1724, class_1268.field_5808, new class_3965(hit, side, support, false));
            BaseFinder.mc.field_1724.method_6104(class_1268.field_5808);
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        class_7439 packet;
        block7: {
            block6: {
                class_2596<?> class_25962;
                if (BaseFinder.mc.field_1724 == null || !this.workMode.isSelected("Private Search") || event.isSend() || !((class_25962 = event.getPacket()) instanceof class_7439)) break block6;
                packet = (class_7439)class_25962;
                if (this.state == State.WAITING_CHAT) break block7;
            }
            return;
        }
        String text = packet.comp_763().getString().toLowerCase(Locale.ROOT);
        if (text.contains("\u043f\u0435\u0440\u0435\u0441\u0435\u043a\u0430\u0435\u0442\u0441\u044f") || text.contains("overlap")) {
            this.message("\u00a7d!!! REGION OVERLAP FOUND !!!");
            this.notifyTelegram("BaseFinder: region overlap at " + this.coords());
            this.command("#stop");
            this.setState(false);
        } else if (text.contains("\u0443\u0441\u043f\u0435\u0448\u043d\u043e") || text.contains("created")) {
            this.state = State.BREAKING;
            this.delay = 2;
        }
    }

    private boolean stopForPlayer() {
        for (class_1657 player : BaseFinder.mc.field_1687.method_18456()) {
            if (player == BaseFinder.mc.field_1724 || FriendUtils.isFriend((class_1297)player)) continue;
            this.message("\u00a7cPlayer detected: " + player.method_5477().getString());
            this.notifyTelegram("BaseFinder: player " + player.method_5477().getString() + " detected at " + this.coords());
            this.command("#stop");
            this.setState(false);
            return true;
        }
        return false;
    }

    private int emeraldOreSlot() {
        for (int slot = 0; slot < 9; ++slot) {
            if (!BaseFinder.mc.field_1724.method_31548().method_5438(slot).method_31574(class_1802.field_8837) && !BaseFinder.mc.field_1724.method_31548().method_5438(slot).method_31574(class_1802.field_29216)) continue;
            return slot;
        }
        return -1;
    }

    private void restoreSlot() {
        if (this.previousSlot >= 0 && this.previousSlot < 9) {
            BaseFinder.mc.field_1724.method_31548().method_61496(this.previousSlot);
        }
    }

    private void lookAt(class_243 pos) {
        class_243 delta = pos.method_1020(BaseFinder.mc.field_1724.method_33571());
        BaseFinder.mc.field_1724.method_36456((float)(Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0));
        BaseFinder.mc.field_1724.method_36457((float)(-Math.toDegrees(Math.atan2(delta.field_1351, Math.hypot(delta.field_1352, delta.field_1350)))));
    }

    private void command(String command) {
        if (BaseFinder.mc.field_1724 != null) {
            BaseFinder.mc.field_1724.field_3944.method_45729(command);
        }
    }

    private void message(String text) {
        if (BaseFinder.mc.field_1724 != null) {
            BaseFinder.mc.field_1724.method_7353(QuickLogger.clientMessage((class_2561)class_2561.method_43470((String)text)), false);
        }
    }

    private String coords() {
        return BaseFinder.mc.field_1724.method_31477() + " " + BaseFinder.mc.field_1724.method_31478() + " " + BaseFinder.mc.field_1724.method_31479();
    }

    private void notifyTelegram(String text) {
        TelegramAPI api = TelegramAPI.getInstance();
        if (api != null) {
            api.sendNotification(text);
        }
    }

    private static enum State {
        CHECK_SUPPLIES,
        TUNNELING,
        STOPPING,
        PLACING,
        WAITING_CHAT,
        BREAKING,
        RESUMING;

    }

    private static enum TakeoffPhase {
        IDLE,
        JUMPING,
        WAITING_GLIDE;

    }

    public record FoundEntry(String type, class_2338 pos, int color, long timestamp) {
    }

    public record SpottedRecord(String summary, class_2338 pos, String anarchy, String nick, String time) {
    }
}

