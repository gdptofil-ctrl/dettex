/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1297
 *  net.minecraft.class_1542
 *  net.minecraft.class_1657
 *  net.minecraft.class_1661
 *  net.minecraft.class_1703
 *  net.minecraft.class_1707
 *  net.minecraft.class_1708
 *  net.minecraft.class_1713
 *  net.minecraft.class_1735
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1842
 *  net.minecraft.class_1844
 *  net.minecraft.class_1847
 *  net.minecraft.class_2246
 *  net.minecraft.class_2248
 *  net.minecraft.class_2302
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_2374
 *  net.minecraft.class_238
 *  net.minecraft.class_2382
 *  net.minecraft.class_2421
 *  net.minecraft.class_243
 *  net.minecraft.class_2586
 *  net.minecraft.class_2589
 *  net.minecraft.class_2595
 *  net.minecraft.class_2680
 *  net.minecraft.class_2769
 *  net.minecraft.class_3489
 *  net.minecraft.class_3965
 *  net.minecraft.class_476
 *  net.minecraft.class_6880
 *  net.minecraft.class_9334
 */
package ru.recoin.implement.features.modules.player;

import java.util.Comparator;
import java.util.function.Predicate;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_1707;
import net.minecraft.class_1708;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1842;
import net.minecraft.class_1844;
import net.minecraft.class_1847;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2302;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2374;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_2421;
import net.minecraft.class_243;
import net.minecraft.class_2586;
import net.minecraft.class_2589;
import net.minecraft.class_2595;
import net.minecraft.class_2680;
import net.minecraft.class_2769;
import net.minecraft.class_3489;
import net.minecraft.class_3965;
import net.minecraft.class_476;
import net.minecraft.class_6880;
import net.minecraft.class_9334;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.TextSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.other.BaritoneCompat;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;

public final class AutoFarm
extends Module {
    private static final double ACTION_RANGE_SQUARED = 6.25;
    private static final double PICKUP_RANGE_SQUARED = 2.25;
    private static final int REPLANT_RESERVE = 16;
    private final SelectSetting mode = new SelectSetting("Mode", "AutoFarm operation mode").value("Swords", "Potions", "Potion Combiner", "Auto Sell", "Crops").selected("Swords");
    private final TextSetting swordPrice = new TextSetting("Sword Price", "Auction price for swords").setText("15000").visible(this::isSword);
    private final ValueSetting swordRelistCooldown = new ValueSetting("Sword Relist Cooldown", "Seconds between sword listings").setValue(60.0f).range(5.0f, 300.0f).step(5.0f).visible(this::isSword);
    private final BooleanSetting swordCraftAll = new BooleanSetting("Craft All", "Use every available sword before relisting").setValue(false).visible(this::isSword);
    private final SelectSetting potionType = new SelectSetting("Potion To Brew", "Potion recipe for brewing mode").value("Strength", "Speed", "Fire Resistance", "Invisibility", "Regeneration", "Healing", "Strong Healing").selected("Strength").visible(this::isPotion);
    private final SelectSetting potionStackMode = new SelectSetting("Stack Mode", "Brew one or three potions at once").value("Multiple", "Single").selected("Multiple").visible(this::isPotion);
    private final BooleanSetting potionEnhance = new BooleanSetting("Enhance", "Create upgraded or extended potions when possible").setValue(false).visible(this::isPotion);
    private final BooleanSetting potionUseChests = new BooleanSetting("Use Chests", "Store completed potions in the nearest chest").setValue(true).visible(this::isPotion);
    private final BooleanSetting potionTargetEsp = new BooleanSetting("Target ESP", "Highlight the active brewing stand").setValue(true).visible(this::isPotion);
    private final ValueSetting potionDelay = new ValueSetting("Potion Delay", "Delay between brewing actions").setValue(250.0f).range(0.0f, 1500.0f).step(10.0f).visible(this::isPotion);
    private final SelectSetting combinerPotions = new SelectSetting("Combiner Potions", "Potion type to take from the combiner").value("Strength", "Speed", "Strength + Speed").selected("Strength").visible(this::isCombiner);
    private final BooleanSetting combinerAutoOpen = new BooleanSetting("Auto Open", "Open the nearest chest automatically").setValue(true).visible(this::isCombiner);
    private final BooleanSetting combinerAutoExp = new BooleanSetting("Auto Experience", "Take experience bottles from the combiner").setValue(false).visible(this::isCombiner);
    private final ValueSetting combinerRefillTo = new ValueSetting("Refill To", "Minimum amount of experience bottles").setValue(40.0f).range(5.0f, 100.0f).step(1.0f).visible(() -> this.isCombiner() && this.combinerAutoExp.isValue());
    private final TextSetting autoSellQuantity = new TextSetting("Sell Quantity", "Reference stack quantity for auto sale").setText("64").visible(this::isAutoSell);
    private final TextSetting autoSellPrice = new TextSetting("Sell Price", "Auction price for the item in hand").setText("10000").visible(this::isAutoSell);
    private final ValueSetting autoSellRelistCooldown = new ValueSetting("Relist Cooldown", "Seconds between auction listings").setValue(60.0f).range(5.0f, 300.0f).step(5.0f).visible(this::isAutoSell);
    private final BooleanSetting autoSellConfirm = new BooleanSetting("Confirmation", "Click auction confirmation buttons").setValue(false).visible(this::isAutoSell);
    private final SelectSetting cropType = new SelectSetting("Crop Type", "Crop to harvest").value("Nether Wart", "Wheat", "Carrots", "Potatoes", "Beetroots", "Sugar Cane").selected("Nether Wart").visible(this::isCrop);
    private final ValueSetting cropScanRadius = new ValueSetting("Scan Radius", "Crop scan radius").setValue(24.0f).range(8.0f, 64.0f).step(2.0f).visible(this::isCrop);
    private final ValueSetting cropVerticalRange = new ValueSetting("Vertical Range", "Crop scan height").setValue(3.0f).range(1.0f, 8.0f).step(1.0f).visible(this::isCrop);
    private final BooleanSetting cropReplant = new BooleanSetting("Replant", "Plant crops again after harvesting").setValue(true).visible(this::isCrop);
    private final BooleanSetting cropUseBonemeal = new BooleanSetting("Use Bonemeal", "Grow immature crops with bonemeal").setValue(true).visible(this::isCrop);
    private final BooleanSetting cropPickup = new BooleanSetting("Pick Up", "Walk to crop drops").setValue(true).visible(this::isCrop);
    private final BooleanSetting cropAutoDeposit = new BooleanSetting("Auto Deposit", "Store harvested crops in a chest").setValue(true).visible(this::isCrop);
    private final BooleanSetting cropTargetEsp = new BooleanSetting("Target ESP", "Highlight the current crop target").setValue(true).visible(this::isCrop);
    private final ValueSetting cropActionDelay = new ValueSetting("Action Delay", "Delay between crop actions").setValue(80.0f).range(0.0f, 500.0f).step(10.0f).visible(this::isCrop);
    private Object baritone;
    private class_2338 currentTarget;
    private class_2338 pendingReplant;
    private long nextActionAt;
    private long lastSellAt;
    private boolean walking;
    private boolean potionDepositPending;

    public AutoFarm() {
        super("AutoFarm", "Auto Farm", ModuleCategory.PLAYER);
        this.setup(this.mode, this.swordPrice, this.swordRelistCooldown, this.swordCraftAll, this.potionType, this.potionStackMode, this.potionEnhance, this.potionUseChests, this.potionTargetEsp, this.potionDelay, this.combinerPotions, this.combinerAutoOpen, this.combinerAutoExp, this.combinerRefillTo, this.autoSellQuantity, this.autoSellPrice, this.autoSellRelistCooldown, this.autoSellConfirm, this.cropType, this.cropScanRadius, this.cropVerticalRange, this.cropReplant, this.cropUseBonemeal, this.cropPickup, this.cropAutoDeposit, this.cropTargetEsp, this.cropActionDelay);
    }

    @Override
    public void activate() {
        this.baritone = BaritoneCompat.getPrimaryBaritone();
        this.currentTarget = null;
        this.pendingReplant = null;
        this.nextActionAt = 0L;
        this.lastSellAt = 0L;
        this.potionDepositPending = false;
    }

    @Override
    public void deactivate() {
        this.currentTarget = null;
        this.pendingReplant = null;
        this.potionDepositPending = false;
        this.stopWalking();
        BaritoneCompat.cancelPathing(this.baritone);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (AutoFarm.mc.field_1724 == null || AutoFarm.mc.field_1687 == null || AutoFarm.mc.field_1761 == null || AutoFarm.mc.field_1724.field_3944 == null) {
            return;
        }
        if (System.currentTimeMillis() < this.nextActionAt) {
            return;
        }
        switch (this.mode.getSelected()) {
            case "Swords": {
                this.handleSwordFarm();
                break;
            }
            case "Potions": {
                this.handlePotionFarm();
                break;
            }
            case "Potion Combiner": {
                this.handlePotionCombiner();
                break;
            }
            case "Auto Sell": {
                this.handleAutoSell();
                break;
            }
            case "Crops": {
                this.handleCropFarm();
                break;
            }
        }
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (this.currentTarget == null || this.isCrop() && !this.cropTargetEsp.isValue() || this.isPotion() && !this.potionTargetEsp.isValue()) {
            return;
        }
        int color = this.isPotion() ? -1426078656 : -1434919840;
        Render3DUtil.drawBox(new class_238(this.currentTarget), color, 1.5f, true, true, false);
    }

    private void handleSwordFarm() {
        if (!this.selectHotbar(stack -> stack.method_31573(class_3489.field_42611))) {
            return;
        }
        this.sellHeldItem(this.swordPrice.getText(), this.swordRelistCooldown.getValue());
    }

    private void handleAutoSell() {
        if (AutoFarm.mc.field_1755 instanceof class_476 && this.autoSellConfirm.isValue()) {
            this.clickFirstContainerItem(stack -> stack.method_31574(class_1802.field_8131));
            return;
        }
        if (!AutoFarm.mc.field_1724.method_6047().method_7960()) {
            this.sellHeldItem(this.autoSellPrice.getText(), this.autoSellRelistCooldown.getValue());
        }
    }

    private void sellHeldItem(String priceText, float cooldownSeconds) {
        long now = System.currentTimeMillis();
        if ((float)(now - this.lastSellAt) < cooldownSeconds * 1000.0f) {
            return;
        }
        String price = this.digits(priceText);
        if (price.isEmpty()) {
            return;
        }
        AutoFarm.mc.field_1724.field_3944.method_45730("ah sell " + price);
        this.lastSellAt = now;
    }

    private void handleCropFarm() {
        class_2338 target;
        if (this.cropAutoDeposit.isValue() && AutoFarm.mc.field_1755 instanceof class_476) {
            this.depositOneCropStack();
            return;
        }
        if (this.cropPickup.isValue() && this.walkToNearestCropDrop()) {
            return;
        }
        if (this.tryReplant()) {
            return;
        }
        if (this.cropUseBonemeal.isValue() && this.tryBonemeal()) {
            return;
        }
        this.currentTarget = target = this.findCropTarget(true);
        if (target == null) {
            if (this.cropAutoDeposit.isValue() && this.hasDepositableCropItems()) {
                this.openDepositChest();
            } else {
                this.stopWalking();
            }
            return;
        }
        if (!this.inActionRange(target)) {
            this.walkTo(target.method_46558(), 1.0);
            return;
        }
        this.stopWalking();
        this.pendingReplant = target.method_10062();
        this.breakBlock(target);
        this.schedule(this.cropActionDelay.getInt());
    }

    private boolean tryBonemeal() {
        class_2338 target = this.findCropTarget(false);
        if (target == null || !this.selectHotbar(stack -> stack.method_31574(class_1802.field_8324))) {
            return false;
        }
        this.currentTarget = target;
        if (!this.inActionRange(target)) {
            this.walkTo(target.method_46558(), 1.0);
            return true;
        }
        this.useBlock(target);
        this.schedule(Math.min(this.cropActionDelay.getInt(), 35));
        return true;
    }

    private boolean tryReplant() {
        if (!this.cropReplant.isValue() || this.pendingReplant == null) {
            return false;
        }
        class_1792 seed = this.seedForCrop();
        if (seed == null || !AutoFarm.mc.field_1687.method_8320(this.pendingReplant).method_26215()) {
            this.pendingReplant = null;
            return false;
        }
        this.currentTarget = this.pendingReplant;
        if (!this.inActionRange(this.pendingReplant)) {
            this.walkTo(this.pendingReplant.method_46558(), 1.0);
            return true;
        }
        if (!this.selectHotbar(stack -> stack.method_31574(seed))) {
            return false;
        }
        this.useBlock(this.pendingReplant.method_10074());
        this.pendingReplant = null;
        this.schedule(this.cropActionDelay.getInt());
        return true;
    }

    private class_2338 findCropTarget(boolean ready) {
        int radius = this.cropScanRadius.getInt();
        int vertical = this.cropVerticalRange.getInt();
        class_2338 origin = AutoFarm.mc.field_1724.method_24515();
        class_2338 best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int x = -radius; x <= radius; ++x) {
            for (int y = -vertical; y <= vertical; ++y) {
                for (int z = -radius; z <= radius; ++z) {
                    double distance;
                    class_2338 pos = origin.method_10069(x, y, z);
                    if ((!ready ? !this.isBonemealableCrop(pos) : !this.isReadyCrop(pos)) || !((distance = AutoFarm.mc.field_1724.method_5707(pos.method_46558())) < bestDistance)) continue;
                    best = pos;
                    bestDistance = distance;
                }
            }
        }
        return best;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private boolean isReadyCrop(class_2338 pos) {
        class_2680 state = AutoFarm.mc.field_1687.method_8320(pos);
        class_2248 block = state.method_26204();
        if (this.cropType.isSelected("Nether Wart")) {
            if (block != class_2246.field_9974) return false;
            if ((Integer)state.method_11654((class_2769)class_2421.field_11306) < 3) return false;
            return true;
        }
        if (this.cropType.isSelected("Sugar Cane")) {
            if (block != class_2246.field_10424) return false;
            if (!AutoFarm.mc.field_1687.method_8320(pos.method_10074()).method_27852(class_2246.field_10424)) return false;
            return true;
        }
        if (!(block instanceof class_2302)) return false;
        class_2302 crop = (class_2302)block;
        if (!this.isSelectedCropBlock(block)) return false;
        if (crop.method_9829(state) < crop.method_9827()) return false;
        return true;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private boolean isBonemealableCrop(class_2338 pos) {
        class_2680 state = AutoFarm.mc.field_1687.method_8320(pos);
        class_2248 block = state.method_26204();
        if (!(block instanceof class_2302)) return false;
        class_2302 crop = (class_2302)block;
        if (!this.isSelectedCropBlock(block)) return false;
        if (crop.method_9829(state) >= crop.method_9827()) return false;
        return true;
    }

    private boolean isSelectedCropBlock(class_2248 block) {
        return this.cropType.isSelected("Wheat") && block == class_2246.field_10293 || this.cropType.isSelected("Carrots") && block == class_2246.field_10609 || this.cropType.isSelected("Potatoes") && block == class_2246.field_10247 || this.cropType.isSelected("Beetroots") && block == class_2246.field_10341;
    }

    private class_1792 seedForCrop() {
        return switch (this.cropType.getSelected()) {
            case "Nether Wart" -> class_1802.field_8790;
            case "Wheat" -> class_1802.field_8317;
            case "Carrots" -> class_1802.field_8179;
            case "Potatoes" -> class_1802.field_8567;
            case "Beetroots" -> class_1802.field_8309;
            case "Sugar Cane" -> class_1802.field_17531;
            default -> null;
        };
    }

    private class_1792 cropItem() {
        return switch (this.cropType.getSelected()) {
            case "Nether Wart" -> class_1802.field_8790;
            case "Wheat" -> class_1802.field_8861;
            case "Carrots" -> class_1802.field_8179;
            case "Potatoes" -> class_1802.field_8567;
            case "Beetroots" -> class_1802.field_8186;
            case "Sugar Cane" -> class_1802.field_17531;
            default -> null;
        };
    }

    private boolean walkToNearestCropDrop() {
        class_1792 item = this.cropItem();
        if (item == null) {
            return false;
        }
        class_1542 drop = AutoFarm.mc.field_1687.method_8390(class_1542.class, AutoFarm.mc.field_1724.method_5829().method_1014(Math.max(6.0, (double)this.cropScanRadius.getValue())), entity -> entity.method_6983().method_31574(item)).stream().min(Comparator.comparingDouble(entity -> entity.method_5858((class_1297)AutoFarm.mc.field_1724))).orElse(null);
        if (drop == null) {
            return false;
        }
        this.currentTarget = drop.method_24515();
        if (drop.method_5858((class_1297)AutoFarm.mc.field_1724) > 2.25) {
            this.walkTo(drop.method_73189(), 2.25);
        } else {
            this.stopWalking();
        }
        return true;
    }

    private void openDepositChest() {
        if (!this.hasDepositableCropItems()) {
            return;
        }
        class_2595 chest = this.findBlockEntity(class_2595.class, this.cropScanRadius.getValue());
        if (chest == null) {
            return;
        }
        this.currentTarget = chest.method_11016();
        if (!this.inActionRange(chest.method_11016())) {
            this.walkTo(chest.method_11016().method_46558(), 1.0);
            return;
        }
        this.useBlock(chest.method_11016());
        this.schedule(250L);
    }

    private void depositOneCropStack() {
        Object object;
        class_1792 item = this.cropItem();
        if (item == null || !((object = AutoFarm.mc.field_1724.field_7512) instanceof class_1707)) {
            return;
        }
        class_1707 handler = (class_1707)object;
        for (class_1735 slot : handler.field_7761) {
            if (!slot.method_7677().method_31574(item) || slot.field_7871 == handler.method_7629() || this.reserveCropItem(item) && this.countItem(item) - slot.method_7677().method_7947() < 16) continue;
            AutoFarm.mc.field_1761.method_2906(handler.field_7763, slot.field_7874, 0, class_1713.field_7794, (class_1657)AutoFarm.mc.field_1724);
            this.schedule(this.cropActionDelay.getInt());
            return;
        }
        AutoFarm.mc.field_1724.method_7346();
    }

    private boolean reserveCropItem(class_1792 item) {
        return item == class_1802.field_8790 || item == class_1802.field_8179 || item == class_1802.field_8567 || item == class_1802.field_17531;
    }

    private boolean hasDepositableCropItems() {
        class_1792 item = this.cropItem();
        return item != null && this.countItem(item) > (this.reserveCropItem(item) ? 16 : 0);
    }

    private void handlePotionFarm() {
        if (this.potionDepositPending) {
            this.handlePotionDeposit();
            return;
        }
        class_1703 class_17032 = AutoFarm.mc.field_1724.field_7512;
        if (!(class_17032 instanceof class_1708)) {
            class_2589 stand = this.findBlockEntity(class_2589.class, 5.0);
            if (stand != null) {
                this.currentTarget = stand.method_11016();
                this.useBlock(stand.method_11016());
                this.schedule(300L);
            }
            return;
        }
        class_1708 handler = (class_1708)class_17032;
        if (handler.method_17377() <= 0 && this.quickMoveFirst((class_1703)handler, stack -> stack.method_31574(class_1802.field_8183))) {
            this.schedule(this.potionDelay.getInt());
            return;
        }
        if (handler.method_17378() > 0) {
            return;
        }
        int bottles = this.potionStackMode.isSelected("Single") ? 1 : 3;
        for (int slot = 0; slot < bottles; ++slot) {
            if (!handler.method_7611(slot).method_7677().method_7960() || !this.quickMoveFirst((class_1703)handler, stack -> this.isPotion((class_1799)stack, (class_6880<class_1842>)class_1847.field_8991))) continue;
            this.schedule(this.potionDelay.getInt());
            return;
        }
        class_1792 ingredient = this.nextPotionIngredient(handler, bottles);
        if (ingredient != null && this.quickMoveFirst((class_1703)handler, stack -> stack.method_31574(ingredient))) {
            this.schedule(this.potionDelay.getInt());
            return;
        }
        if (this.isPotionComplete(handler, bottles)) {
            for (int slot = 0; slot < bottles; ++slot) {
                if (handler.method_7611(slot).method_7677().method_7960()) continue;
                AutoFarm.mc.field_1761.method_2906(handler.field_7763, slot, 0, class_1713.field_7794, (class_1657)AutoFarm.mc.field_1724);
                this.schedule(this.potionDelay.getInt());
                return;
            }
            if (this.potionUseChests.isValue()) {
                this.potionDepositPending = true;
                AutoFarm.mc.field_1724.method_7346();
                this.schedule(200L);
            }
        }
    }

    private void handlePotionCombiner() {
        class_1703 class_17032 = AutoFarm.mc.field_1724.field_7512;
        if (!(class_17032 instanceof class_1707)) {
            class_2595 chest;
            if (this.combinerAutoOpen.isValue() && (chest = this.findBlockEntity(class_2595.class, 5.0)) != null) {
                this.currentTarget = chest.method_11016();
                this.useBlock(chest.method_11016());
                this.schedule(300L);
            }
            return;
        }
        class_1707 handler = (class_1707)class_17032;
        class_6880 wanted = this.combinerPotions.isSelected("Speed") ? class_1847.field_9005 : class_1847.field_8978;
        int wantedCount = this.combinerPotions.isSelected("Strength + Speed") ? 2 : 1;
        for (int moved = 0; moved < wantedCount && this.quickMoveFirstContainer(handler, stack -> this.isPotion((class_1799)stack, (class_6880<class_1842>)wanted)); ++moved) {
        }
        if (this.combinerAutoExp.isValue() && this.countItem(class_1802.field_8287) < this.combinerRefillTo.getInt()) {
            this.quickMoveFirstContainer(handler, stack -> stack.method_31574(class_1802.field_8287));
        }
        this.schedule(150L);
    }

    private class_1792 nextPotionIngredient(class_1708 handler, int bottles) {
        if (this.allPotionSlotsAre(handler, bottles, (class_6880<class_1842>)class_1847.field_8991)) {
            return class_1802.field_8790;
        }
        if (this.allPotionSlotsAre(handler, bottles, (class_6880<class_1842>)class_1847.field_8999)) {
            return switch (this.potionType.getSelected()) {
                case "Strength" -> class_1802.field_8183;
                case "Speed" -> class_1802.field_8479;
                case "Fire Resistance" -> class_1802.field_8135;
                case "Invisibility" -> class_1802.field_8071;
                case "Regeneration" -> class_1802.field_8070;
                case "Healing", "Strong Healing" -> class_1802.field_8597;
                default -> null;
            };
        }
        if (this.potionType.isSelected("Invisibility") && this.allPotionSlotsAre(handler, bottles, (class_6880<class_1842>)class_1847.field_8968)) {
            return class_1802.field_8711;
        }
        if (this.potionEnhance.isValue()) {
            if (this.allPotionSlotsAre(handler, bottles, (class_6880<class_1842>)class_1847.field_8978) || this.allPotionSlotsAre(handler, bottles, (class_6880<class_1842>)class_1847.field_9005) || this.allPotionSlotsAre(handler, bottles, (class_6880<class_1842>)class_1847.field_8963)) {
                return class_1802.field_8601;
            }
            if (this.allPotionSlotsAre(handler, bottles, (class_6880<class_1842>)class_1847.field_8987) || this.allPotionSlotsAre(handler, bottles, (class_6880<class_1842>)class_1847.field_8986)) {
                return class_1802.field_8725;
            }
        }
        return this.potionType.isSelected("Strong Healing") && this.allPotionSlotsAre(handler, bottles, (class_6880<class_1842>)class_1847.field_8963) ? class_1802.field_8601 : null;
    }

    private boolean isPotionComplete(class_1708 handler, int bottles) {
        class_6880 target = switch (this.potionType.getSelected()) {
            case "Strength" -> {
                if (this.potionEnhance.isValue()) {
                    yield class_1847.field_8993;
                }
                yield class_1847.field_8978;
            }
            case "Speed" -> {
                if (this.potionEnhance.isValue()) {
                    yield class_1847.field_8966;
                }
                yield class_1847.field_9005;
            }
            case "Fire Resistance" -> {
                if (this.potionEnhance.isValue()) {
                    yield class_1847.field_8969;
                }
                yield class_1847.field_8987;
            }
            case "Invisibility" -> class_1847.field_8997;
            case "Regeneration" -> {
                if (this.potionEnhance.isValue()) {
                    yield class_1847.field_9003;
                }
                yield class_1847.field_8986;
            }
            case "Healing", "Strong Healing" -> {
                if (this.potionEnhance.isValue() || this.potionType.isSelected("Strong Healing")) {
                    yield class_1847.field_8980;
                }
                yield class_1847.field_8963;
            }
            default -> class_1847.field_8991;
        };
        return this.allPotionSlotsAre(handler, bottles, (class_6880<class_1842>)target);
    }

    private boolean allPotionSlotsAre(class_1708 handler, int bottles, class_6880<class_1842> potion) {
        for (int slot = 0; slot < bottles; ++slot) {
            if (this.isPotion(handler.method_7611(slot).method_7677(), potion)) continue;
            return false;
        }
        return true;
    }

    private boolean isPotion(class_1799 stack, class_6880<class_1842> potion) {
        class_1844 contents = (class_1844)stack.method_58694(class_9334.field_49651);
        return contents != null && contents.comp_2378().isPresent() && ((class_6880)contents.comp_2378().get()).equals(potion);
    }

    private void handlePotionDeposit() {
        class_1703 class_17032 = AutoFarm.mc.field_1724.field_7512;
        if (class_17032 instanceof class_1707) {
            int containerSize;
            class_1707 handler = (class_1707)class_17032;
            for (int slot = containerSize = handler.method_7629().method_5439(); slot < handler.field_7761.size(); ++slot) {
                class_1799 stack = handler.method_7611(slot).method_7677();
                if (!stack.method_31574(class_1802.field_8574) && !stack.method_31574(class_1802.field_8436) && !stack.method_31574(class_1802.field_8150)) continue;
                AutoFarm.mc.field_1761.method_2906(handler.field_7763, slot, 0, class_1713.field_7794, (class_1657)AutoFarm.mc.field_1724);
                this.schedule(this.potionDelay.getInt());
                return;
            }
            AutoFarm.mc.field_1724.method_7346();
            this.potionDepositPending = false;
            return;
        }
        if (AutoFarm.mc.field_1755 != null) {
            AutoFarm.mc.field_1724.method_7346();
            return;
        }
        class_2595 chest = this.findBlockEntity(class_2595.class, 5.0);
        if (chest == null) {
            this.potionDepositPending = false;
            return;
        }
        this.currentTarget = chest.method_11016();
        this.useBlock(chest.method_11016());
        this.schedule(250L);
    }

    private boolean quickMoveFirst(class_1703 handler, Predicate<class_1799> predicate) {
        for (class_1735 slot : handler.field_7761) {
            if (slot.field_7874 < 5 || slot.method_7677().method_7960() || !predicate.test(slot.method_7677())) continue;
            AutoFarm.mc.field_1761.method_2906(handler.field_7763, slot.field_7874, 0, class_1713.field_7794, (class_1657)AutoFarm.mc.field_1724);
            return true;
        }
        return false;
    }

    private boolean quickMoveFirstContainer(class_1707 handler, Predicate<class_1799> predicate) {
        int containerSize = handler.method_7629().method_5439();
        for (int slot = 0; slot < containerSize; ++slot) {
            if (handler.method_7611(slot).method_7677().method_7960() || !predicate.test(handler.method_7611(slot).method_7677())) continue;
            AutoFarm.mc.field_1761.method_2906(handler.field_7763, slot, 0, class_1713.field_7794, (class_1657)AutoFarm.mc.field_1724);
            return true;
        }
        return false;
    }

    private boolean clickFirstContainerItem(Predicate<class_1799> predicate) {
        class_1703 class_17032 = AutoFarm.mc.field_1724.field_7512;
        if (!(class_17032 instanceof class_1707)) {
            return false;
        }
        class_1707 handler = (class_1707)class_17032;
        int containerSize = handler.method_7629().method_5439();
        for (int slot = 0; slot < containerSize; ++slot) {
            if (!predicate.test(handler.method_7611(slot).method_7677())) continue;
            AutoFarm.mc.field_1761.method_2906(handler.field_7763, slot, 0, class_1713.field_7790, (class_1657)AutoFarm.mc.field_1724);
            return true;
        }
        return false;
    }

    private <T extends class_2586> T findBlockEntity(Class<T> type, double range) {
        class_2338 origin = AutoFarm.mc.field_1724.method_24515();
        int radius = (int)Math.ceil(range);
        class_2586 result = null;
        double bestDistance = Double.MAX_VALUE;
        for (int x = -radius; x <= radius; ++x) {
            for (int y = -radius; y <= radius; ++y) {
                for (int z = -radius; z <= radius; ++z) {
                    double distance;
                    class_2338 pos = origin.method_10069(x, y, z);
                    class_2586 entity = AutoFarm.mc.field_1687.method_8321(pos);
                    if (!type.isInstance(entity) || !((distance = AutoFarm.mc.field_1724.method_5707(pos.method_46558())) < bestDistance)) continue;
                    result = (class_2586)type.cast(entity);
                    bestDistance = distance;
                }
            }
        }
        return (T)result;
    }

    private class_2338 findNearest(Predicate<class_2338> predicate, int radius, int vertical) {
        class_2338 origin = AutoFarm.mc.field_1724.method_24515();
        class_2338 result = null;
        double bestDistance = Double.MAX_VALUE;
        for (int x = -radius; x <= radius; ++x) {
            for (int y = -vertical; y <= vertical; ++y) {
                for (int z = -radius; z <= radius; ++z) {
                    double distance;
                    class_2338 pos = origin.method_10069(x, y, z);
                    if (!predicate.test(pos) || !((distance = AutoFarm.mc.field_1724.method_5707(pos.method_46558())) < bestDistance)) continue;
                    result = pos;
                    bestDistance = distance;
                }
            }
        }
        return result;
    }

    private boolean selectHotbar(Predicate<class_1799> predicate) {
        for (int slot2 = 0; slot2 < 9; ++slot2) {
            if (!predicate.test(AutoFarm.mc.field_1724.method_31548().method_5438(slot2))) continue;
            AutoFarm.mc.field_1724.method_31548().method_61496(slot2);
            return true;
        }
        class_1735 inventorySlot = PlayerInventoryUtil.getSlot(slot -> slot.field_7871 instanceof class_1661 && predicate.test(slot.method_7677()));
        if (inventorySlot != null) {
            PlayerInventoryUtil.swapHand(inventorySlot, class_1268.field_5808, false);
            return true;
        }
        return false;
    }

    private int countItem(class_1792 item) {
        int count = 0;
        for (int slot = 0; slot < AutoFarm.mc.field_1724.method_31548().method_5439(); ++slot) {
            class_1799 stack = AutoFarm.mc.field_1724.method_31548().method_5438(slot);
            if (!stack.method_31574(item)) continue;
            count += stack.method_7947();
        }
        return count;
    }

    private boolean inActionRange(class_2338 pos) {
        return pos != null && AutoFarm.mc.field_1724.method_5707(pos.method_46558()) <= 6.25;
    }

    private void useBlock(class_2338 pos) {
        AutoFarm.mc.field_1761.method_2896(AutoFarm.mc.field_1724, class_1268.field_5808, new class_3965(class_243.method_24953((class_2382)pos), class_2350.field_11036, pos, false));
    }

    private void breakBlock(class_2338 pos) {
        AutoFarm.mc.field_1761.method_2910(pos, class_2350.field_11036);
        AutoFarm.mc.field_1724.method_6104(class_1268.field_5808);
    }

    private void walkTo(class_243 target, double stopRangeSquared) {
        if (AutoFarm.mc.field_1724.method_5707(target) <= stopRangeSquared) {
            this.stopWalking();
            return;
        }
        BaritoneCompat.goNear(this.baritone, class_2338.method_49638((class_2374)target), Math.max(1, (int)Math.sqrt(stopRangeSquared)));
        class_243 delta = target.method_1020(AutoFarm.mc.field_1724.method_73189());
        AutoFarm.mc.field_1724.method_36456((float)Math.toDegrees(Math.atan2(-delta.field_1352, delta.field_1350)));
        AutoFarm.mc.field_1690.field_1894.method_23481(true);
        AutoFarm.mc.field_1690.field_1867.method_23481(true);
        AutoFarm.mc.field_1690.field_1903.method_23481(AutoFarm.mc.field_1724.field_5976 && AutoFarm.mc.field_1724.method_24828());
        this.walking = true;
    }

    private void stopWalking() {
        if (!this.walking) {
            return;
        }
        AutoFarm.mc.field_1690.field_1894.method_23481(false);
        AutoFarm.mc.field_1690.field_1867.method_23481(false);
        AutoFarm.mc.field_1690.field_1903.method_23481(false);
        this.walking = false;
    }

    private void schedule(long delay) {
        this.nextActionAt = System.currentTimeMillis() + Math.max(0L, delay);
    }

    private String digits(String text) {
        return text == null ? "" : text.replaceAll("\\D", "");
    }

    private boolean isSword() {
        return this.mode.isSelected("Swords");
    }

    private boolean isPotion() {
        return this.mode.isSelected("Potions");
    }

    private boolean isCombiner() {
        return this.mode.isSelected("Potion Combiner");
    }

    private boolean isAutoSell() {
        return this.mode.isSelected("Auto Sell");
    }

    private boolean isCrop() {
        return this.mode.isSelected("Crops");
    }
}

