/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1735
 *  net.minecraft.class_1799
 *  net.minecraft.class_2338
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_2596
 *  net.minecraft.class_2680
 *  net.minecraft.class_2868
 *  net.minecraft.class_746
 */
package ru.recoin.implement.features.modules.player;

import java.util.Objects;
import net.minecraft.class_1268;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_239;
import net.minecraft.class_2596;
import net.minecraft.class_2680;
import net.minecraft.class_2868;
import net.minecraft.class_746;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.entity.PlayerInventoryComponent;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.entity.ScreenWalkInventorySync;
import ru.recoin.common.util.other.StopWatch;
import ru.recoin.implement.events.block.BlockBreakingEvent;
import ru.recoin.implement.events.keyboard.HotBarScrollEvent;
import ru.recoin.implement.events.player.HotBarUpdateEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.ItemRendererEvent;
import ru.recoin.implement.features.modules.misc.ClickAction;

public class AutoTool
extends Module {
    private final StopWatch swap = new StopWatch();
    private final StopWatch breaking = new StopWatch();
    private class_1799 renderStack;
    private class_2338 lastBreakPos;
    private int previousHotbarSlot = -1;
    private boolean hotbarSwap;
    private class_1735 previousInventorySlot;
    private int previousInventoryHotbarSlot = -1;
    private boolean inventorySwap;
    private boolean inventoryRestorePending;
    private transient class_746 swapPlayer;
    private transient class_1799 inventoryDisplaced;

    public AutoTool() {
        super("AutoTool", "Auto Tool", ModuleCategory.PLAYER);
    }

    @Override
    public void deactivate() {
        ScreenWalkInventorySync.whenIdle(() -> {
            if (!this.restoreInventorySlot()) {
                this.restoreHotbarSlot();
            }
        });
        this.renderStack = null;
        super.deactivate();
    }

    @EventHandler
    public void onItemRenderer(ItemRendererEvent e) {
        if (this.renderStack != null && e.getHand().equals((Object)class_1268.field_5808) && Objects.equals(AutoTool.mc.field_1724, e.getPlayer())) {
            e.setStack(this.renderStack);
        }
    }

    @EventHandler
    public void onHotBarUpdate(HotBarUpdateEvent e) {
        if (this.hotbarSwap || this.inventorySwap) {
            e.cancel();
        }
    }

    @EventHandler
    public void onHotBarScroll(HotBarScrollEvent e) {
        if (this.hotbarSwap || this.inventorySwap) {
            e.cancel();
        }
    }

    @EventHandler
    public void onBlockBreaking(BlockBreakingEvent e) {
        this.breaking.reset();
        this.lastBreakPos = e.blockPos();
        if (AutoTool.mc.field_1724 == null || AutoTool.mc.field_1724.method_68878() || !this.swap.finished(220.0) || this.inventoryRestorePending || PlayerInventoryComponent.isInventoryActionBusy() || this.isExperienceSwapActive()) {
            return;
        }
        class_1735 currentBestSlot = this.findBestTool(this.lastBreakPos);
        if (currentBestSlot == null || currentBestSlot == PlayerInventoryUtil.mainHandSlot()) {
            return;
        }
        int hotbarSlot = this.getHotbarSlot(currentBestSlot);
        if (hotbarSlot != -1) {
            this.startHotbarSwap(hotbarSlot);
            this.swap.reset();
        } else if (!this.inventorySwap && !PlayerInventoryComponent.isInventoryActionBusy()) {
            this.previousInventorySlot = currentBestSlot;
            this.previousInventoryHotbarSlot = AutoTool.mc.field_1724.method_31548().method_67532();
            this.swapPlayer = AutoTool.mc.field_1724;
            this.inventoryDisplaced = AutoTool.mc.field_1724.method_6047().method_7972();
            this.inventorySwap = true;
            this.renderStack = AutoTool.mc.field_1724.method_6047();
            ScreenWalkInventorySync.swapSlot(currentBestSlot, this.previousInventoryHotbarSlot, false, null);
            this.swap.reset();
            e.cancel();
        }
    }

    @EventHandler
    public void onTick(TickEvent e) {
        boolean isMining;
        if (AutoTool.mc.field_1724 == null) {
            return;
        }
        if (this.swapPlayer != null && AutoTool.mc.field_1724 != this.swapPlayer) {
            this.clearSwapState();
        }
        if (PlayerInventoryComponent.isInventoryActionBusy()) {
            return;
        }
        boolean bl = isMining = AutoTool.mc.field_1690.field_1886.method_1434() && AutoTool.mc.field_1765 != null && AutoTool.mc.field_1765.method_17783() == class_239.class_240.field_1332;
        if (isMining) {
            this.breaking.reset();
            return;
        }
        if (this.breaking.finished(280.0) && this.swap.finished(220.0)) {
            if (this.inventorySwap) {
                if (!this.restoreInventorySlot()) {
                    this.restoreHotbarSlot();
                }
                this.swap.reset();
            } else if (this.hotbarSwap) {
                this.restoreHotbarSlot();
                this.swap.reset();
            }
        }
    }

    private boolean restoreInventorySlot() {
        if (AutoTool.mc.field_1724 == null || this.swapPlayer != null && AutoTool.mc.field_1724 != this.swapPlayer) {
            this.clearSwapState();
            return false;
        }
        if (!this.inventorySwap || this.previousInventorySlot == null || this.previousInventoryHotbarSlot < 0) {
            return false;
        }
        if (this.inventoryRestorePending) {
            return true;
        }
        if (PlayerInventoryComponent.isInventoryActionBusy()) {
            return true;
        }
        class_1735 source = PlayerInventoryUtil.resolveSlot(this.previousInventorySlot);
        if (source == null || this.inventoryDisplaced == null || !PlayerInventoryUtil.matchesSwapStack(source.method_7677(), this.inventoryDisplaced)) {
            this.clearSwapState();
            return false;
        }
        this.inventoryRestorePending = true;
        ScreenWalkInventorySync.swapSlot(source, this.previousInventoryHotbarSlot, false, () -> {
            this.previousInventorySlot = null;
            this.previousInventoryHotbarSlot = -1;
            this.inventorySwap = false;
            this.inventoryRestorePending = false;
            this.inventoryDisplaced = null;
            this.renderStack = null;
            this.restoreHotbarSlot();
        });
        return true;
    }

    private class_1735 findBestTool(class_2338 blockPos) {
        if (blockPos == null || AutoTool.mc.field_1687 == null) {
            return PlayerInventoryUtil.mainHandSlot();
        }
        class_2680 state = AutoTool.mc.field_1687.method_8320(blockPos);
        if (PlayerIntersectionUtil.isAir(state)) {
            return PlayerInventoryUtil.mainHandSlot();
        }
        class_1735 mainHand = PlayerInventoryUtil.mainHandSlot();
        float currentSpeed = mainHand != null ? mainHand.method_7677().method_7924(state) : 1.0f;
        class_1735 best = null;
        float bestSpeed = currentSpeed;
        for (class_1735 slot : PlayerInventoryUtil.slots().toList()) {
            float speed;
            if (slot.field_7871 != AutoTool.mc.field_1724.method_31548() || slot.method_34266() < 0 || slot.method_34266() >= 36 || !((speed = slot.method_7677().method_7924(state)) > bestSpeed)) continue;
            bestSpeed = speed;
            best = slot;
        }
        return best != null ? best : mainHand;
    }

    private void startHotbarSwap(int hotbarSlot) {
        this.swapPlayer = AutoTool.mc.field_1724;
        if (!this.hotbarSwap) {
            this.previousHotbarSlot = AutoTool.mc.field_1724.method_31548().method_67532();
            this.hotbarSwap = true;
        }
        this.renderStack = null;
        AutoTool.mc.field_1724.method_31548().method_61496(hotbarSlot);
        if (AutoTool.mc.field_1724.field_3944 != null) {
            AutoTool.mc.field_1724.field_3944.method_52787((class_2596)new class_2868(hotbarSlot));
        }
    }

    private void restoreHotbarSlot() {
        if (AutoTool.mc.field_1724 == null || this.swapPlayer != null && AutoTool.mc.field_1724 != this.swapPlayer) {
            this.clearSwapState();
            return;
        }
        if (!this.hotbarSwap || this.previousHotbarSlot < 0) {
            return;
        }
        AutoTool.mc.field_1724.method_31548().method_61496(this.previousHotbarSlot);
        if (AutoTool.mc.field_1724.field_3944 != null) {
            AutoTool.mc.field_1724.field_3944.method_52787((class_2596)new class_2868(this.previousHotbarSlot));
        }
        this.previousHotbarSlot = -1;
        this.hotbarSwap = false;
    }

    private int getHotbarSlot(class_1735 slot) {
        return slot.field_7871 == AutoTool.mc.field_1724.method_31548() && slot.method_34266() >= 0 && slot.method_34266() < 9 ? slot.method_34266() : -1;
    }

    private void clearSwapState() {
        this.previousHotbarSlot = -1;
        this.hotbarSwap = false;
        this.previousInventorySlot = null;
        this.previousInventoryHotbarSlot = -1;
        this.inventorySwap = false;
        this.inventoryRestorePending = false;
        this.inventoryDisplaced = null;
        this.renderStack = null;
        this.swapPlayer = null;
    }

    public boolean isHoldingTool() {
        return AutoTool.mc.field_1724 == this.swapPlayer && (this.hotbarSwap || this.inventorySwap);
    }

    private boolean isExperienceSwapActive() {
        ClickAction clickAction = ClickAction.getInstance();
        return clickAction != null && clickAction.isExperienceSwapActive();
    }
}

