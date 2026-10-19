/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1269
 *  net.minecraft.class_1291
 *  net.minecraft.class_1294
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_1735
 *  net.minecraft.class_1792
 *  net.minecraft.class_6880
 */
package ru.recoin.implement.features.modules.player;

import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1291;
import net.minecraft.class_1294;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_6880;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.entity.PlayerInventoryComponent;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.entity.ScreenWalkInventorySync;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.player.TickEvent;

public class AutoUse
extends Module {
    private static final int USE_TIMEOUT_TICKS = 45;
    private final MultiSelectSetting modes = new MultiSelectSetting("Mode", "Choose what will be used").value("Eat", "Invisibility");
    private class_1735 sourceSlot;
    private class_1792 expectedItem;
    private int initialCount;
    private int initialHunger;
    private int previousHotbar = -1;
    private int activeTicks;
    private int retryDelay;
    private boolean inventorySwap;
    private boolean directHotbar;
    private boolean observedUsing;
    private int postUseTicks;
    private boolean restoring;
    private boolean blockedUntilNeedClears;

    public AutoUse() {
        super("AutoUse", "Auto Use", ModuleCategory.PLAYER);
        this.setup(this.modes);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (AutoUse.mc.field_1724 == null || AutoUse.mc.field_1761 == null) {
            return;
        }
        if (this.retryDelay > 0) {
            --this.retryDelay;
        }
        if (this.sourceSlot != null) {
            this.tickActiveUse();
            return;
        }
        if (this.blockedUntilNeedClears) {
            if (this.wantedSlot() == null) {
                this.blockedUntilNeedClears = false;
            } else {
                return;
            }
        }
        if (this.retryDelay > 0 || PlayerInventoryComponent.isInventoryActionBusy()) {
            return;
        }
        class_1735 slot = this.wantedSlot();
        if (slot == null || AutoUse.mc.field_1724.method_7357().method_7904(slot.method_7677())) {
            return;
        }
        this.begin(slot);
    }

    private void begin(class_1735 slot) {
        this.sourceSlot = slot;
        this.expectedItem = slot.method_7677().method_7909();
        this.initialCount = slot.method_7677().method_7947();
        this.initialHunger = AutoUse.mc.field_1724.method_7344().method_7586();
        this.previousHotbar = AutoUse.mc.field_1724.method_31548().method_67532();
        this.activeTicks = 0;
        this.observedUsing = false;
        this.restoring = false;
        boolean hotbar = slot.field_7871 == AutoUse.mc.field_1724.method_31548() && slot.method_34266() >= 0 && slot.method_34266() < 9;
        this.directHotbar = hotbar && slot.method_34266() != this.previousHotbar;
        boolean bl = this.inventorySwap = !hotbar;
        if (this.directHotbar) {
            AutoUse.mc.field_1724.method_31548().method_61496(slot.method_34266());
        } else if (this.inventorySwap) {
            ScreenWalkInventorySync.swapHand(slot, class_1268.field_5808, true);
        }
    }

    public static AutoUse getInstance() {
        return Instance.get(AutoUse.class);
    }

    public boolean isActivelyUsing() {
        return this.isState() && this.sourceSlot != null && !this.restoring;
    }

    private void tickActiveUse() {
        if (this.restoring) {
            if (!PlayerInventoryComponent.isInventoryActionBusy()) {
                this.clearState();
            }
            return;
        }
        if (!AutoUse.mc.field_1724.method_6047().method_31574(this.expectedItem)) {
            if (!PlayerInventoryComponent.isInventoryActionBusy() && ++this.activeTicks > 25) {
                this.finish();
            }
            return;
        }
        ++this.activeTicks;
        if (AutoUse.mc.field_1724.method_6115()) {
            this.observedUsing = true;
        }
        if (this.observedUsing) {
            AutoUse.mc.field_1690.field_1904.method_23481(true);
            if (!AutoUse.mc.field_1724.method_6115()) {
                this.finish();
                return;
            }
            if (this.activeTicks >= 45) {
                this.finish();
                return;
            }
            return;
        }
        AutoUse.mc.field_1690.field_1904.method_23481(true);
        class_1269 result = AutoUse.mc.field_1761.method_2919((class_1657)AutoUse.mc.field_1724, class_1268.field_5808);
        if (result.method_23665() && !AutoUse.mc.field_1724.method_6115() && AutoUse.mc.field_1724.method_6047().method_7935((class_1309)AutoUse.mc.field_1724) > 0) {
            AutoUse.mc.field_1724.method_6019(class_1268.field_5808);
        }
        if (AutoUse.mc.field_1724.method_6115()) {
            this.observedUsing = true;
        } else if (this.activeTicks >= 20) {
            this.finish();
        }
    }

    private void finish() {
        boolean timedOut = this.activeTicks >= 45;
        AutoUse.mc.field_1690.field_1904.method_23481(false);
        if (timedOut && AutoUse.mc.field_1724.method_6115()) {
            AutoUse.mc.field_1761.method_2897((class_1657)AutoUse.mc.field_1724);
        }
        if (this.inventorySwap && this.sourceSlot != null && !PlayerInventoryComponent.isInventoryActionBusy()) {
            ScreenWalkInventorySync.swapHand(this.sourceSlot, class_1268.field_5808, true);
            this.restoring = true;
        } else {
            if (this.directHotbar && this.previousHotbar >= 0) {
                AutoUse.mc.field_1724.method_31548().method_61496(this.previousHotbar);
            }
            this.clearState();
        }
        this.retryDelay = 12;
        if (timedOut) {
            this.blockedUntilNeedClears = true;
        }
    }

    private class_1735 wantedSlot() {
        class_1735 food;
        if (this.modes.isSelected("Eat") && AutoUse.mc.field_1724.method_7344().method_7587() && (food = PlayerInventoryUtil.getFoodMaxSaturationSlot()) != null) {
            return food;
        }
        if (this.modes.isSelected("Invisibility") && !PlayerIntersectionUtil.isPotionActive((class_6880<class_1291>)class_1294.field_5905)) {
            return PlayerInventoryUtil.getPotion((class_6880<class_1291>)class_1294.field_5905);
        }
        return null;
    }

    private void clearState() {
        this.sourceSlot = null;
        this.expectedItem = null;
        this.initialCount = 0;
        this.initialHunger = 0;
        this.previousHotbar = -1;
        this.activeTicks = 0;
        this.postUseTicks = 0;
        this.inventorySwap = false;
        this.directHotbar = false;
        this.observedUsing = false;
        this.restoring = false;
    }

    @Override
    public void deactivate() {
        if (AutoUse.mc.field_1690 != null) {
            AutoUse.mc.field_1690.field_1904.method_23481(false);
        }
        if (AutoUse.mc.field_1724 != null && AutoUse.mc.field_1724.method_6115() && AutoUse.mc.field_1761 != null) {
            AutoUse.mc.field_1761.method_2897((class_1657)AutoUse.mc.field_1724);
        }
        if (this.directHotbar && AutoUse.mc.field_1724 != null && this.previousHotbar >= 0) {
            AutoUse.mc.field_1724.method_31548().method_61496(this.previousHotbar);
        } else if (this.inventorySwap && this.sourceSlot != null && AutoUse.mc.field_1724 != null && !PlayerInventoryComponent.isInventoryActionBusy()) {
            ScreenWalkInventorySync.swapHand(this.sourceSlot, class_1268.field_5808, true);
        }
        this.clearState();
        this.blockedUntilNeedClears = false;
    }
}

