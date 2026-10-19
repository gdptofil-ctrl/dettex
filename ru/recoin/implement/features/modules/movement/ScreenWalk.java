/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1041
 *  net.minecraft.class_1713
 *  net.minecraft.class_1747
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_2480
 *  net.minecraft.class_2596
 *  net.minecraft.class_2645
 *  net.minecraft.class_2813
 *  net.minecraft.class_2815
 *  net.minecraft.class_304
 *  net.minecraft.class_3675
 *  net.minecraft.class_437
 *  net.minecraft.class_476
 *  net.minecraft.class_495
 *  net.minecraft.class_5537
 */
package ru.recoin.implement.features.modules.movement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.class_1041;
import net.minecraft.class_1713;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2480;
import net.minecraft.class_2596;
import net.minecraft.class_2645;
import net.minecraft.class_2813;
import net.minecraft.class_2815;
import net.minecraft.class_304;
import net.minecraft.class_3675;
import net.minecraft.class_437;
import net.minecraft.class_476;
import net.minecraft.class_495;
import net.minecraft.class_5537;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.common.util.entity.MovingUtil;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.entity.PlayerInventoryComponent;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.container.CloseScreenEvent;
import ru.recoin.implement.events.item.ClickSlotEvent;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.InputEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.screens.menu.MenuScreen;
import ru.recoin.implement.screens.menu.components.implement.other.SearchComponent;

public class ScreenWalk
extends Module {
    private final SelectSetting bypass = new SelectSetting("Bypass", "ScreenWalk movement mode").value("Default", "Vanilla").selected("Default");
    private final List<class_2596<?>> packets = new ArrayList();
    private boolean externalInventorySwap;
    private boolean externalInventorySwapBlocked;
    private boolean externalInventorySwapSyncing;
    private boolean externalBundleSwap;
    private int funTimeClosePhase;
    private int funTimeCloseInputTicks;
    private boolean funTimeClosePacketPending;
    private final List<class_2596<?>> funTimeClosingPackets = new ArrayList();

    public static ScreenWalk getInstance() {
        return Instance.get(ScreenWalk.class);
    }

    public ScreenWalk() {
        super("ScreenWalk", "Screen Walk", ModuleCategory.MOVEMENT);
        this.setup(this.bypass);
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        if (this.bypass.isSelected("Vanilla") || this.externalInventorySwap && !this.externalInventorySwapSyncing) {
            return;
        }
        if (this.externalBundleSwap && e.isSend() && (e.getPacket() instanceof class_2813 || e.getPacket() instanceof class_2815)) {
            return;
        }
        if (!this.bypass.isSelected("Default") && !this.externalInventorySwapSyncing) {
            return;
        }
        class_2596<?> packet = e.getPacket();
        if (packet instanceof class_2813) {
            class_2813 slot = (class_2813)packet;
            if (!this.externalInventorySwapSyncing && (this.packets.isEmpty() && !MovingUtil.hasPlayerMovement() || !PlayerInventoryComponent.shouldSkipExecution())) {
                this.packets.add((class_2596<?>)slot);
                e.cancel();
            }
        } else if (packet instanceof class_2815) {
            class_2815 close = (class_2815)packet;
            if (this.externalInventorySwapSyncing || !this.isMovingNow() && this.packets.isEmpty() && this.funTimeClosePhase <= 0) {
                return;
            }
            if (!this.packets.isEmpty() || this.funTimeClosePhase > 0) {
                this.funTimeClosePacketPending = true;
            }
            e.cancel();
        } else if (packet instanceof class_2645) {
            class_2645 screen = (class_2645)packet;
            if (screen.method_36148() == 0) {
                e.cancel();
            }
        }
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (ScreenWalk.mc.field_1755 instanceof MenuScreen) {
            this.updateScreenMoveKeys();
        }
        if (this.bypass.isSelected("Vanilla")) {
            if (PlayerInventoryComponent.shouldSkipExecution() || ScreenWalk.mc.field_1755 instanceof MenuScreen) {
                this.updateScreenMoveKeys();
            }
            return;
        }
        if (this.bypass.isSelected("Default") && this.funTimeClosePhase > 0) {
            if (this.funTimeCloseInputTicks > 0) {
                if (!this.funTimeClosingPackets.isEmpty()) {
                    PlayerIntersectionUtil.sendPacketWithOutEvent(this.funTimeClosingPackets.remove(0));
                } else {
                    if (this.funTimeClosePacketPending && ScreenWalk.mc.field_1724 != null && ScreenWalk.mc.field_1755 == null) {
                        PlayerIntersectionUtil.sendPacketWithOutEvent(new class_2815(ScreenWalk.mc.field_1724.field_7512.field_7763));
                        this.funTimeClosePacketPending = false;
                    }
                    PlayerInventoryUtil.updateSlots();
                    this.funTimeClosePhase = 0;
                    this.funTimeCloseInputTicks = 0;
                }
            }
            return;
        }
        if (this.bypass.isSelected("Default") && !PlayerInventoryUtil.isServerScreen() && PlayerInventoryComponent.shouldSkipExecution() && (!this.packets.isEmpty() || ScreenWalk.mc.field_1724.field_7512.method_34255().method_7960()) && !this.externalInventorySwapBlocked) {
            PlayerInventoryComponent.updateMoveKeys();
        }
        if (!MovingUtil.hasPlayerMovement() && !this.packets.isEmpty()) {
            PlayerIntersectionUtil.sendPacketWithOutEvent(this.packets.remove(0));
        }
    }

    @EventHandler
    public void onInput(InputEvent e) {
        if (ScreenWalk.mc.field_1755 instanceof MenuScreen && SearchComponent.typing) {
            e.inputNone();
            return;
        }
        if (this.bypass.isSelected("Default") && this.funTimeClosePhase > 0) {
            e.inputNonePreservingSneak();
            ++this.funTimeCloseInputTicks;
            return;
        }
        if (this.externalInventorySwapBlocked) {
            e.inputNonePreservingSneak();
        }
    }

    @EventHandler
    public void onClickSlot(ClickSlotEvent e) {
        if (this.bypass.isSelected("Vanilla") || this.externalInventorySwap || this.isAllowedInventoryClick(e)) {
            return;
        }
        class_1713 actionType = e.getActionType();
        if (!(this.isShulkerClick(e) || this.packets.isEmpty() && !MovingUtil.hasPlayerMovement() || (e.getButton() != 1 || actionType.equals((Object)class_1713.field_7791) || actionType.equals((Object)class_1713.field_7795)) && !actionType.equals((Object)class_1713.field_7793))) {
            e.cancel();
        }
    }

    private boolean isShulkerClick(ClickSlotEvent event) {
        class_1747 item;
        if (event.getButton() != 1 || ScreenWalk.mc.field_1724 == null) {
            return false;
        }
        int slotId = event.getSlotId();
        class_1799 cursor = ScreenWalk.mc.field_1724.field_7512.method_34255();
        class_1799 clicked = slotId >= 0 && slotId < ScreenWalk.mc.field_1724.field_7512.field_7761.size() ? ScreenWalk.mc.field_1724.field_7512.method_7611(slotId).method_7677() : class_1799.field_8037;
        class_1799 target = cursor.method_7960() ? clicked : cursor;
        class_1792 class_17922 = target.method_7909();
        return class_17922 instanceof class_1747 && (item = (class_1747)class_17922).method_7711() instanceof class_2480;
    }

    private boolean isAllowedInventoryClick(ClickSlotEvent event) {
        if (ScreenWalk.mc.field_1724 == null || event.getWindowId() != ScreenWalk.mc.field_1724.field_7512.field_7763) {
            return false;
        }
        if (!ScreenWalk.mc.field_1724.field_7512.method_34255().method_7960()) {
            return true;
        }
        int slotId = event.getSlotId();
        return slotId >= 0 && slotId < ScreenWalk.mc.field_1724.field_7512.field_7761.size() && ScreenWalk.mc.field_1724.field_7512.method_7611(slotId).method_7677().method_7909() instanceof class_5537;
    }

    public boolean isShulkerScreen(class_437 screen) {
        if (screen instanceof class_495) {
            return true;
        }
        if (screen instanceof class_476) {
            class_476 containerScreen = (class_476)screen;
            String title = containerScreen.method_25440().getString().toLowerCase();
            return title.contains("\u0448\u0430\u043b\u043a\u0435\u0440") || title.contains("shulker") || title.contains("\u044f\u0449\u0438\u043a");
        }
        return false;
    }

    @EventHandler
    public void onCloseScreen(CloseScreenEvent e) {
        if (this.bypass.isSelected("Vanilla")) {
            return;
        }
        if (this.bypass.isSelected("Default") && !this.packets.isEmpty()) {
            this.funTimeClosingPackets.clear();
            this.funTimeClosingPackets.addAll(this.packets);
            this.packets.clear();
            this.funTimeClosePhase = 1;
            this.funTimeCloseInputTicks = 0;
            return;
        }
        if (this.bypass.isSelected("Default")) {
            this.funTimeClosePacketPending = false;
        }
        this.restoreMoveKeys();
    }

    private void restoreMoveKeys() {
        if (ScreenWalk.mc.field_1755 == null || PlayerInventoryComponent.shouldSkipExecution() || ScreenWalk.mc.field_1755 instanceof MenuScreen) {
            PlayerInventoryComponent.canMove = true;
            PlayerInventoryComponent.updateMoveKeys();
        } else {
            PlayerInventoryComponent.unPressMoveKeys();
        }
    }

    private void updateScreenMoveKeys() {
        if (ScreenWalk.mc.field_1755 instanceof MenuScreen && SearchComponent.typing) {
            PlayerInventoryComponent.unPressMoveKeys();
        } else {
            PlayerInventoryComponent.updateMoveKeys();
        }
    }

    private void flushPackets(List<class_2596<?>> queued) {
        queued.forEach(PlayerIntersectionUtil::sendPacketWithOutEvent);
        queued.clear();
    }

    private boolean isMovingNow() {
        return MovingUtil.hasPlayerMovement() || this.isPhysicalMovePressed();
    }

    public boolean isMovingForInventorySwap() {
        return this.isMovingNow();
    }

    public boolean isExternalInventorySwapSyncing() {
        return this.externalInventorySwapSyncing;
    }

    public void runExternalInventorySwap(Runnable action) {
        if (action == null) {
            return;
        }
        this.externalInventorySwap = true;
        this.externalInventorySwapSyncing = true;
        try {
            action.run();
        }
        finally {
            this.externalInventorySwap = false;
        }
    }

    public void runExternalBundleSwap(Runnable action) {
        this.externalBundleSwap = true;
        this.runExternalInventorySwap(action);
    }

    public void setExternalInventorySwap(boolean val) {
        this.externalInventorySwap = val;
    }

    public void setExternalInventorySwapBlocked(boolean blocked) {
        this.externalInventorySwapBlocked = blocked;
        if (blocked) {
            PlayerInventoryComponent.canMove = false;
            PlayerInventoryComponent.unPressMoveKeys();
        }
    }

    private boolean isPhysicalMovePressed() {
        return this.isKeyPressed(ScreenWalk.mc.field_1690.field_1894) || this.isKeyPressed(ScreenWalk.mc.field_1690.field_1881) || this.isKeyPressed(ScreenWalk.mc.field_1690.field_1913) || this.isKeyPressed(ScreenWalk.mc.field_1690.field_1849) || this.isKeyPressed(ScreenWalk.mc.field_1690.field_1903) || this.isKeyPressed(ScreenWalk.mc.field_1690.field_1867);
    }

    private boolean isKeyPressed(class_304 keyBinding) {
        return keyBinding != null && class_3675.method_15987((class_1041)mc.method_22683(), (int)keyBinding.method_1429().method_1444());
    }

    @Override
    public void deactivate() {
        this.externalBundleSwap = false;
        this.externalInventorySwapSyncing = false;
        this.externalInventorySwapBlocked = false;
        this.externalInventorySwap = false;
        this.funTimeCloseInputTicks = 0;
        this.funTimeClosePhase = 0;
        this.funTimeClosePacketPending = false;
        this.funTimeClosingPackets.clear();
        this.packets.clear();
        this.restoreMoveKeys();
        super.deactivate();
    }

    public void finishExternalInventorySwap() {
        try {
            if (!this.packets.isEmpty()) {
                PlayerIntersectionUtil.sendPacketWithOutEvent(this.packets.remove(0));
                PlayerInventoryUtil.updateSlots();
            }
        }
        finally {
            this.externalInventorySwap = false;
            this.externalInventorySwapSyncing = false;
            this.externalBundleSwap = false;
        }
    }

    public boolean isInventorySyncBusy() {
        return this.isState() && (!this.packets.isEmpty() || this.funTimeClosePhase > 0 || this.externalInventorySwapBlocked);
    }

    public SelectSetting getBypass() {
        return this.bypass;
    }

    public List<class_2596<?>> getPackets() {
        return this.packets;
    }

    public boolean isExternalInventorySwap() {
        return this.externalInventorySwap;
    }

    public boolean isExternalInventorySwapBlocked() {
        return this.externalInventorySwapBlocked;
    }

    public boolean isExternalBundleSwap() {
        return this.externalBundleSwap;
    }

    public int getFunTimeClosePhase() {
        return this.funTimeClosePhase;
    }

    public int getFunTimeCloseInputTicks() {
        return this.funTimeCloseInputTicks;
    }

    public boolean isFunTimeClosePacketPending() {
        return this.funTimeClosePacketPending;
    }

    public List<class_2596<?>> getFunTimeClosingPackets() {
        return this.funTimeClosingPackets;
    }
}

