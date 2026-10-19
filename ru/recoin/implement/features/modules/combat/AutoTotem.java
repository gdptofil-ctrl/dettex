/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1297
 *  net.minecraft.class_1304
 *  net.minecraft.class_1511
 *  net.minecraft.class_1541
 *  net.minecraft.class_1548
 *  net.minecraft.class_1657
 *  net.minecraft.class_1735
 *  net.minecraft.class_1796
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1839
 *  net.minecraft.class_1937
 *  net.minecraft.class_2246
 *  net.minecraft.class_2338
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_2596
 *  net.minecraft.class_2663
 *  net.minecraft.class_638
 *  net.minecraft.class_746
 *  net.minecraft.class_9279
 *  net.minecraft.class_9290
 *  net.minecraft.class_9304
 *  net.minecraft.class_9334
 *  net.minecraft.class_9362
 */
package ru.recoin.implement.features.modules.combat;

import com.adl.nativeprotect.Native;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1511;
import net.minecraft.class_1541;
import net.minecraft.class_1548;
import net.minecraft.class_1657;
import net.minecraft.class_1735;
import net.minecraft.class_1796;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1839;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2596;
import net.minecraft.class_2663;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_9279;
import net.minecraft.class_9290;
import net.minecraft.class_9304;
import net.minecraft.class_9334;
import net.minecraft.class_9362;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.entity.ScreenWalkInventorySync;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.TickEvent;

public class AutoTotem
extends Module {
    private final ValueSetting healthSetting = new ValueSetting("Max Health", "Maximum health for triggering Auto Totem").setValue(4.0f).range(0.5f, 20.0f).step(0.5f);
    private final MultiSelectSetting triggerSetting = new MultiSelectSetting("Triggers", "Select in which case the Totem will be taken").value("Crystal", "Anchor", "TNT", "Fall", "Elytra", "Mace", "Creeper").selected("Crystal", "Anchor");
    private final ValueSetting elytraHealthSetting = new ValueSetting("Elytra Health", "Health threshold while gliding on elytra").setValue(10.0f).range(2.0f, 20.0f).visible(() -> this.triggerSetting.isSelected("Elytra"));
    private final ValueSetting TNTRangeSetting = new ValueSetting("TNT Distance", "Distance to TNT for triggering Auto Totem").setValue(8.0f).range(0.0f, 50.0f).visible(() -> this.triggerSetting.isSelected("TNT"));
    private final ValueSetting fallHeightSetting = new ValueSetting("Fall Height", "Minimum fall height to trigger Auto Totem").setValue(15.0f).range(3.0f, 50.0f).visible(() -> this.triggerSetting.isSelected("Fall"));
    private final ValueSetting maceDistanceSetting = new ValueSetting("Mace Height", "Maximum height of a falling mace player").setValue(8.0f).range(0.5f, 20.0f).step(0.5f).visible(() -> this.triggerSetting.isSelected("Mace"));
    private final ValueSetting creeperDistanceSetting = new ValueSetting("Creeper Distance", "Distance to a creeper to trigger Auto Totem").setValue(4.0f).range(1.0f, 10.0f).visible(() -> this.triggerSetting.isSelected("Creeper"));
    private final BooleanSetting keepEnchanted = new BooleanSetting("Keep Enchanted Totems", "Prefer regular totems and keep enchanted ones in inventory").setValue(true);
    private final BooleanSetting dontSwapWhileEating = new BooleanSetting("Don't Swap While Eating", "Don't swap the totem while eating or drinking, swap instantly once finished").setValue(true);
    private final Map<Integer, Double> playerLastY = new HashMap<Integer, Double>();
    private transient class_1799 previousOffhand = class_1799.field_8037;
    private transient class_746 offhandPlayer;
    private transient class_638 offhandWorld;
    private transient boolean restorePending;
    private float fallStartY;
    private boolean wasFalling;
    private boolean swapPending;
    private transient class_638 refillWorld;
    private transient boolean refillRequested;
    private transient int refillDeadline;
    private int threatCheckAge = Integer.MIN_VALUE;
    private boolean immediateThreat;
    private class_638 threatWorld;

    public static AutoTotem getInstance() {
        return Instance.get(AutoTotem.class);
    }

    public boolean isHoldingTotem() {
        return this.isState() && AutoTotem.mc.field_1724 != null && AutoTotem.mc.field_1687 != null && (this.swapPending || this.trigger());
    }

    public AutoTotem() {
        super("AutoTotem", "Auto Totem", ModuleCategory.COMBAT);
        this.setup(this.healthSetting, this.elytraHealthSetting, this.triggerSetting, this.TNTRangeSetting, this.fallHeightSetting, this.maceDistanceSetting, this.creeperDistanceSetting, this.keepEnchanted, this.dontSwapWhileEating);
    }

    @Override
    public void deactivate() {
        this.previousOffhand = class_1799.field_8037;
        this.offhandPlayer = null;
        this.offhandWorld = null;
        this.restorePending = false;
        this.playerLastY.clear();
        this.swapPending = false;
        this.refillWorld = null;
        this.refillRequested = false;
        this.refillDeadline = 0;
        this.threatCheckAge = Integer.MIN_VALUE;
        this.threatWorld = null;
        super.deactivate();
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (AutoTotem.mc.field_1724 == null || AutoTotem.mc.field_1687 == null) {
            return;
        }
        if (this.offhandPlayer != AutoTotem.mc.field_1724 || this.offhandWorld != AutoTotem.mc.field_1687) {
            this.previousOffhand = class_1799.field_8037;
            this.offhandPlayer = AutoTotem.mc.field_1724;
            this.offhandWorld = AutoTotem.mc.field_1687;
            this.swapPending = false;
            this.restorePending = false;
            this.playerLastY.clear();
        }
        this.updateFallTracking();
        this.handleTotem();
        this.updatePlayerTracking();
    }

    @Native
    private void handleTotem() {
        if (this.trigger()) {
            boolean needSwap;
            if (this.dontSwapWhileEating.isValue() && this.isEating() && !this.isImmediateThreat()) {
                return;
            }
            class_1799 offHandStack = AutoTotem.mc.field_1724.method_6079();
            Comparator<class_1735> totemComparator = this.keepEnchanted.isValue() ? Comparator.comparing((class_1735 s) -> AutoTotem.isEnchantedTotem(s.method_7677())).reversed() : Comparator.comparingInt(s -> 0);
            class_1735 slot = PlayerInventoryUtil.getSlot(s -> s.field_7874 != 46 && s.field_7874 != 45 && AutoTotem.isTotem(s.method_7677()) && !AutoTotem.isCooldown(s.method_7677()), totemComparator);
            if (slot == null) {
                return;
            }
            boolean bl = needSwap = !AutoTotem.isTotem(offHandStack) || AutoTotem.isCooldown(offHandStack) || this.keepEnchanted.isValue() && AutoTotem.isEnchantedTotem(offHandStack) && !AutoTotem.isEnchantedTotem(slot.method_7677());
            if (needSwap && !this.swapPending) {
                if (!AutoTotem.isTotem(offHandStack) && !offHandStack.method_7960() && this.previousOffhand.method_7960()) {
                    this.previousOffhand = offHandStack.method_7972();
                }
                this.swapPending = true;
                ScreenWalkInventorySync.swapHand(slot, class_1268.field_5810, true, () -> {
                    this.swapPending = false;
                });
            }
        } else {
            this.restorePreviousOffhand();
        }
    }

    private void restorePreviousOffhand() {
        if (this.previousOffhand.method_7960() || this.swapPending || this.restorePending) {
            return;
        }
        class_1799 offhand = AutoTotem.mc.field_1724.method_6079();
        if (!offhand.method_7960() && !AutoTotem.isTotem(offhand)) {
            this.previousOffhand = class_1799.field_8037;
            return;
        }
        class_746 player = AutoTotem.mc.field_1724;
        class_638 world = AutoTotem.mc.field_1687;
        this.restorePending = true;
        ScreenWalkInventorySync.run(() -> {
            try {
                if (!this.isState() || AutoTotem.mc.field_1724 != player || AutoTotem.mc.field_1687 != world || this.trigger()) {
                    return;
                }
                class_1799 current = player.method_6079();
                if (!current.method_7960() && !AutoTotem.isTotem(current)) {
                    this.previousOffhand = class_1799.field_8037;
                    return;
                }
                class_1735 original = PlayerInventoryUtil.getSlot(s -> s.field_7874 != 45 && s.field_7874 != 46 && class_1799.method_31577((class_1799)s.method_7677(), (class_1799)this.previousOffhand));
                if (original == null || !player.field_7512.method_34255().method_7960()) {
                    return;
                }
                PlayerInventoryUtil.swap(original, 40, true);
                if (class_1799.method_31577((class_1799)player.method_6079(), (class_1799)this.previousOffhand)) {
                    this.previousOffhand = class_1799.field_8037;
                }
            }
            finally {
                this.restorePending = false;
            }
        });
    }

    public static boolean isCooldown(class_1799 stack) {
        if (stack == null || stack.method_7960() || AutoTotem.mc.field_1724 == null) {
            return false;
        }
        class_1796 manager = AutoTotem.mc.field_1724.method_7357();
        return manager.method_7904(stack) || manager.method_7904(class_1802.field_8288.method_7854());
    }

    private boolean hasReadyTotem() {
        if (AutoTotem.mc.field_1724 == null) {
            return false;
        }
        return PlayerInventoryUtil.getSlot(s -> s.field_7874 != 46 && s.field_7874 != 45 && AutoTotem.isTotem(s.method_7677()) && !AutoTotem.isCooldown(s.method_7677())) != null;
    }

    public static boolean isTotem(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        if (stack.method_31574(class_1802.field_8288)) {
            return true;
        }
        String name = stack.method_7964().getString().toLowerCase();
        return name.contains("\u0442\u043e\u0442\u0435\u043c") || name.contains("totem");
    }

    public static boolean isEnchantedTotem(class_1799 stack) {
        if (!AutoTotem.isTotem(stack)) {
            return false;
        }
        if (stack.method_7942()) {
            return true;
        }
        class_9304 enchantments = (class_9304)stack.method_58694(class_9334.field_49633);
        if (enchantments != null && !enchantments.method_57543()) {
            return true;
        }
        class_9304 stored = (class_9304)stack.method_58694(class_9334.field_49643);
        if (stored != null && !stored.method_57543()) {
            return true;
        }
        class_9279 customData = (class_9279)stack.method_58694(class_9334.field_49628);
        if (customData != null && (customData.method_57461().method_10545("Enchantments") || customData.method_57461().method_10545("ench"))) {
            return true;
        }
        class_9290 lore = (class_9290)stack.method_58694(class_9334.field_49632);
        if (lore != null && !lore.comp_2400().isEmpty()) {
            for (class_2561 line : lore.comp_2400()) {
                String str = line.getString().toLowerCase();
                if (!str.contains("\u0447\u0430\u0440") && !str.contains("\u0441\u0444\u0435\u0440") && !str.contains("\u0437\u0430\u0447\u0430\u0440") && !str.contains("enchant") && !str.contains("\u0443\u0440\u043e\u0432\u0435\u043d\u044c") && !str.contains("lvl")) continue;
                return true;
            }
        }
        return false;
    }

    @Native
    public boolean trigger() {
        float fallDistance;
        boolean offHandReadyTotem;
        if (AutoTotem.mc.field_1724 == null || AutoTotem.mc.field_1687 == null) {
            return false;
        }
        this.updateTotemPopTracking();
        float elytra = AutoTotem.mc.field_1724.method_6118(class_1304.field_6174).method_7909().equals(class_1802.field_8833) ? 4.0f : 0.0f;
        float health = AutoTotem.mc.field_1724.method_6032() + AutoTotem.mc.field_1724.method_6067();
        class_1799 offHand = AutoTotem.mc.field_1724.method_6079();
        boolean bl = offHandReadyTotem = AutoTotem.isTotem(offHand) && !AutoTotem.isCooldown(offHand);
        if (!offHandReadyTotem && !this.hasReadyTotem()) {
            this.refillRequested = false;
            return false;
        }
        if (this.refillRequested) {
            return true;
        }
        if (health < this.healthSetting.getValue() + elytra) {
            return true;
        }
        if (this.isImmediateThreat()) {
            return true;
        }
        if (this.triggerSetting.isSelected("Fall") && (fallDistance = this.fallStartY - (float)AutoTotem.mc.field_1724.method_23318()) >= this.fallHeightSetting.getValue() && AutoTotem.mc.field_1724.method_18798().field_1351 < -0.1) {
            return true;
        }
        if (this.triggerSetting.isSelected("Elytra") && AutoTotem.mc.field_1724.method_6128() && health <= this.elytraHealthSetting.getValue()) {
            return true;
        }
        if (this.triggerSetting.isSelected("Mace") && this.isMaceThreat()) {
            return true;
        }
        if (this.triggerSetting.isSelected("Creeper") && this.isCreeperThreat()) {
            return true;
        }
        if (!this.triggerSetting.isSelected("TNT")) {
            return false;
        }
        double range = this.TNTRangeSetting.getValue();
        double rangeSquared = range * range;
        class_238 area = AutoTotem.mc.field_1724.method_5829().method_1014(range);
        return !AutoTotem.mc.field_1687.method_8390(class_1541.class, area, entity -> AutoTotem.mc.field_1724.method_5858((class_1297)entity) < rangeSquared).isEmpty();
    }

    private boolean isImmediateThreat() {
        if (this.threatWorld == AutoTotem.mc.field_1687 && this.threatCheckAge == AutoTotem.mc.field_1724.field_6012) {
            return this.immediateThreat;
        }
        this.threatWorld = AutoTotem.mc.field_1687;
        this.threatCheckAge = AutoTotem.mc.field_1724.field_6012;
        this.immediateThreat = this.checkImmediateThreat();
        return this.immediateThreat;
    }

    private boolean checkImmediateThreat() {
        if (this.triggerSetting.isSelected("Crystal") && !AutoTotem.mc.field_1687.method_8390(class_1511.class, AutoTotem.mc.field_1724.method_5829().method_1014(5.0), entity -> AutoTotem.mc.field_1724.method_5858((class_1297)entity) <= 25.0).isEmpty()) {
            return true;
        }
        if (!this.triggerSetting.isSelected("Anchor")) {
            return false;
        }
        class_2338 center = AutoTotem.mc.field_1724.method_24515();
        class_243 playerPos = AutoTotem.mc.field_1724.method_73189();
        class_2338.class_2339 pos = new class_2338.class_2339();
        for (int x = -5; x <= 5; ++x) {
            int blockX = center.method_10263() + x;
            double dx = (double)blockX + 0.5 - playerPos.field_1352;
            double dxSquared = dx * dx;
            if (dxSquared > 25.0) continue;
            for (int y = -3; y <= 3; ++y) {
                int blockY = center.method_10264() + y;
                double dy = (double)blockY + 0.5 - playerPos.field_1351;
                double xySquared = dxSquared + dy * dy;
                if (xySquared > 25.0) continue;
                for (int z = -5; z <= 5; ++z) {
                    int blockZ = center.method_10260() + z;
                    double dz = (double)blockZ + 0.5 - playerPos.field_1350;
                    if (xySquared + dz * dz > 25.0) continue;
                    pos.method_10103(blockX, blockY, blockZ);
                    if (!AutoTotem.mc.field_1687.method_8320((class_2338)pos).method_27852(class_2246.field_23152)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        class_2663 status;
        class_2596<?> class_25962;
        if (!event.isReceive() || !((class_25962 = event.getPacket()) instanceof class_2663) || (status = (class_2663)class_25962).method_11470() != 35 || AutoTotem.mc.field_1724 == null || AutoTotem.mc.field_1687 == null) {
            return;
        }
        class_638 packetWorld = AutoTotem.mc.field_1687;
        class_746 packetPlayer = AutoTotem.mc.field_1724;
        mc.execute(() -> this.lambda$onPacket$14(packetWorld, (class_1657)packetPlayer, status));
    }

    private void updateTotemPopTracking() {
        if (!this.refillRequested) {
            return;
        }
        class_1799 offhand = AutoTotem.mc.field_1724.method_6079();
        if (this.refillWorld != AutoTotem.mc.field_1687 || AutoTotem.mc.field_1724.field_6012 >= this.refillDeadline || AutoTotem.isTotem(offhand) && !AutoTotem.isCooldown(offhand)) {
            this.refillRequested = false;
            this.refillWorld = null;
        }
    }

    private boolean isMaceThreat() {
        double maxHeight = this.maceDistanceSetting.getValue();
        for (class_1657 player : AutoTotem.mc.field_1687.method_18456()) {
            double dz;
            double dx;
            double height;
            if (player == AutoTotem.mc.field_1724 || FriendUtils.isFriend((class_1297)player) || !(player.method_6047().method_7909() instanceof class_9362) || (height = player.method_23318() - AutoTotem.mc.field_1724.method_23318()) <= 1.0 || height > maxHeight || (dx = player.method_23317() - AutoTotem.mc.field_1724.method_23317()) * dx + (dz = player.method_23321() - AutoTotem.mc.field_1724.method_23321()) * dz > 16.0) continue;
            double lastY = this.playerLastY.getOrDefault(player.method_5628(), player.method_23318());
            boolean fallingTowardMe = player.method_23318() < lastY - 0.01;
            if (!fallingTowardMe) continue;
            return true;
        }
        return false;
    }

    private boolean isCreeperThreat() {
        double distance = this.creeperDistanceSetting.getValue();
        double distanceSquared = distance * distance;
        return !AutoTotem.mc.field_1687.method_8390(class_1548.class, AutoTotem.mc.field_1724.method_5829().method_1014(distance), creeper -> AutoTotem.mc.field_1724.method_5858((class_1297)creeper) <= distanceSquared && (creeper.method_7003(1.0f) > 0.0f || creeper.method_7000())).isEmpty();
    }

    private void updateFallTracking() {
        boolean isFalling;
        boolean bl = isFalling = AutoTotem.mc.field_1724.method_18798().field_1351 < -0.1 && !AutoTotem.mc.field_1724.method_24828() && !AutoTotem.mc.field_1724.method_6101() && !AutoTotem.mc.field_1724.method_5799() && !AutoTotem.mc.field_1724.method_6128();
        if (isFalling && !this.wasFalling) {
            this.fallStartY = (float)AutoTotem.mc.field_1724.method_23318();
        }
        if (AutoTotem.mc.field_1724.method_24828() || AutoTotem.mc.field_1724.method_5799() || AutoTotem.mc.field_1724.method_6101()) {
            this.fallStartY = (float)AutoTotem.mc.field_1724.method_23318();
        }
        this.wasFalling = isFalling;
    }

    private void updatePlayerTracking() {
        for (class_1657 player : AutoTotem.mc.field_1687.method_18456()) {
            if (player == AutoTotem.mc.field_1724) continue;
            this.playerLastY.put(player.method_5628(), player.method_23318());
        }
        this.playerLastY.keySet().removeIf(id -> AutoTotem.mc.field_1687.method_8469(id.intValue()) == null);
    }

    private boolean isEating() {
        if (AutoTotem.mc.field_1724 == null || !AutoTotem.mc.field_1724.method_6115()) {
            return false;
        }
        class_1839 action = AutoTotem.mc.field_1724.method_6030().method_7976();
        return action == class_1839.field_8950 || action == class_1839.field_8946;
    }

    public ValueSetting getHealthSetting() {
        return this.healthSetting;
    }

    public MultiSelectSetting getTriggerSetting() {
        return this.triggerSetting;
    }

    public ValueSetting getElytraHealthSetting() {
        return this.elytraHealthSetting;
    }

    public ValueSetting getTNTRangeSetting() {
        return this.TNTRangeSetting;
    }

    public ValueSetting getFallHeightSetting() {
        return this.fallHeightSetting;
    }

    public ValueSetting getMaceDistanceSetting() {
        return this.maceDistanceSetting;
    }

    public ValueSetting getCreeperDistanceSetting() {
        return this.creeperDistanceSetting;
    }

    public BooleanSetting getKeepEnchanted() {
        return this.keepEnchanted;
    }

    public BooleanSetting getDontSwapWhileEating() {
        return this.dontSwapWhileEating;
    }

    public Map<Integer, Double> getPlayerLastY() {
        return this.playerLastY;
    }

    public class_1799 getPreviousOffhand() {
        return this.previousOffhand;
    }

    public class_746 getOffhandPlayer() {
        return this.offhandPlayer;
    }

    public class_638 getOffhandWorld() {
        return this.offhandWorld;
    }

    public boolean isRestorePending() {
        return this.restorePending;
    }

    public float getFallStartY() {
        return this.fallStartY;
    }

    public boolean isWasFalling() {
        return this.wasFalling;
    }

    public boolean isSwapPending() {
        return this.swapPending;
    }

    public class_638 getRefillWorld() {
        return this.refillWorld;
    }

    public boolean isRefillRequested() {
        return this.refillRequested;
    }

    public int getRefillDeadline() {
        return this.refillDeadline;
    }

    public int getThreatCheckAge() {
        return this.threatCheckAge;
    }

    public class_638 getThreatWorld() {
        return this.threatWorld;
    }

    private /* synthetic */ void lambda$onPacket$14(class_638 packetWorld, class_1657 packetPlayer, class_2663 status) {
        if (!this.isState() || AutoTotem.mc.field_1687 != packetWorld || AutoTotem.mc.field_1724 != packetPlayer || status.method_11469((class_1937)packetWorld) != packetPlayer) {
            return;
        }
        this.refillWorld = packetWorld;
        this.refillDeadline = packetPlayer.field_6012 + 20;
        this.refillRequested = true;
    }
}

