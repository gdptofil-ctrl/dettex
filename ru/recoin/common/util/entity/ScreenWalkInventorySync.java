/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1661
 *  net.minecraft.class_1713
 *  net.minecraft.class_1735
 *  net.minecraft.class_1799
 *  net.minecraft.class_746
 */
package ru.recoin.common.util.entity;

import java.util.ArrayDeque;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.function.BooleanSupplier;
import net.minecraft.class_1268;
import net.minecraft.class_1661;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_746;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.entity.PlayerInventoryComponent;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.world.ServerUtil;
import ru.recoin.implement.features.modules.misc.ServerHelper;
import ru.recoin.implement.features.modules.movement.ScreenWalk;

public final class ScreenWalkInventorySync
implements QuickImports {
    private static final Queue<Runnable> pendingOperations = new ArrayDeque<Runnable>();
    private static final Map<class_1735, PlayerInventoryUtil.BundleResolution> bundleHandSessions = new IdentityHashMap<class_1735, PlayerInventoryUtil.BundleResolution>();

    public static void run(Runnable action) {
        ScreenWalkInventorySync.run(action, null);
    }

    public static void whenIdle(Runnable action) {
        class_746 player = ScreenWalkInventorySync.mc.field_1724;
        if (PlayerInventoryComponent.isInventoryActionBusy()) {
            pendingOperations.offer(() -> {
                if (ScreenWalkInventorySync.mc.field_1724 == player) {
                    ScreenWalkInventorySync.whenIdle(action);
                }
            });
        } else if (player != null) {
            action.run();
        }
    }

    private static void run(Runnable action, Runnable completion) {
        if (action == null) {
            return;
        }
        ScreenWalk screenWalk = ScreenWalk.getInstance();
        if (!ScreenWalkInventorySync.shouldUseScreenWalkSwap(screenWalk)) {
            PlayerInventoryComponent.runInventoryAction(() -> {
                action.run();
                if (completion != null) {
                    completion.run();
                }
            });
            return;
        }
        if (PlayerInventoryComponent.isInventoryActionBusy()) {
            pendingOperations.offer(() -> ScreenWalkInventorySync.run(action, completion));
            return;
        }
        PlayerInventoryComponent.script.cleanup();
        ScreenWalkInventorySync.scheduleFunTime(screenWalk, action, completion);
    }

    public static void swapHand(class_1735 slot, class_1268 hand, boolean updateInventory) {
        ScreenWalkInventorySync.swapHand(slot, hand, updateInventory, null);
    }

    public static void swapHand(class_1735 slot, class_1268 hand, boolean updateInventory, Runnable completion) {
        if (slot == null || ScreenWalkInventorySync.mc.field_1724 == null) {
            if (completion != null) {
                completion.run();
            }
            return;
        }
        int button = hand == class_1268.field_5808 ? ScreenWalkInventorySync.mc.field_1724.method_31548().method_67532() : 40;
        ScreenWalkInventorySync.swapSlot(slot, button, updateInventory, completion);
    }

    public static void swapSlot(class_1735 slot, int button, boolean updateInventory, Runnable completion) {
        if (slot == null || ScreenWalkInventorySync.mc.field_1724 == null) {
            if (completion != null) {
                completion.run();
            }
            return;
        }
        if (PlayerInventoryUtil.isBundleBackedSlot(slot)) {
            PlayerInventoryUtil.BundleResolution active = bundleHandSessions.remove(slot);
            if (active != null) {
                ScreenWalkInventorySync.run(() -> {
                    PlayerInventoryUtil.swap(active.destination(), button, updateInventory);
                    PlayerInventoryUtil.returnBundleItem(active);
                }, completion);
                return;
            }
            PlayerInventoryUtil.BundleResolution resolution = PlayerInventoryUtil.prepareBundleResolution(slot);
            if (resolution == null) {
                return;
            }
            bundleHandSessions.put(slot, resolution);
            ScreenWalkInventorySync.run(() -> {
                class_1735 extracted = PlayerInventoryUtil.extractBundleItem(resolution);
                if (extracted == null) {
                    bundleHandSessions.remove(slot);
                    return;
                }
                PlayerInventoryUtil.swap(extracted, button, updateInventory);
            }, completion);
            return;
        }
        class_746 player = ScreenWalkInventorySync.mc.field_1724;
        ScreenWalkInventorySync.runSwapWhenCursorEmpty(player, slot, button, updateInventory, completion);
    }

    private static void runSwapWhenCursorEmpty(class_746 player, class_1735 source, int button, boolean updateInventory, Runnable completion) {
        ScreenWalkInventorySync.run(() -> {
            if (player == null || ScreenWalkInventorySync.mc.field_1724 != player) {
                return;
            }
            if (!player.field_7512.method_34255().method_7960()) {
                pendingOperations.offer(() -> ScreenWalkInventorySync.runSwapWhenCursorEmpty(player, source, button, updateInventory, completion));
                return;
            }
            class_1735 current = PlayerInventoryUtil.resolveSlot(source);
            if (current == null) {
                return;
            }
            PlayerInventoryUtil.swap(current, button, updateInventory);
            if (completion != null) {
                completion.run();
            }
        }, null);
    }

    public static void swapUseRestore(class_1735 slot, Runnable useAction) {
        if (slot == null || useAction == null || ScreenWalkInventorySync.mc.field_1724 == null) {
            return;
        }
        if (PlayerInventoryUtil.isBundleBackedSlot(slot)) {
            ScreenWalkInventorySync.bundleUseFromHandRestore(slot, useAction);
            return;
        }
        ScreenWalkInventorySync.swapUseRestore(new UseRequest(ScreenWalkInventorySync.mc.field_1724, slot, slot.method_7677().method_7972()), useAction);
    }

    private static void swapUseRestore(UseRequest request, Runnable useAction) {
        if (ScreenWalkInventorySync.mc.field_1724 != request.player()) {
            return;
        }
        if (PlayerInventoryComponent.isInventoryActionBusy()) {
            pendingOperations.offer(() -> ScreenWalkInventorySync.swapUseRestore(request, useAction));
            return;
        }
        class_1735 resolvedSlot = request.resolve();
        if (resolvedSlot == null) {
            return;
        }
        InventoryUse operation = new InventoryUse(resolvedSlot, request.stack());
        ScreenWalk screenWalk = ScreenWalk.getInstance();
        if (ScreenWalkInventorySync.isHotbarSlot(resolvedSlot)) {
            ScreenWalkInventorySync.scheduleHotbarUse(operation, useAction);
            return;
        }
        if (!ScreenWalkInventorySync.shouldUseScreenWalkSwap(screenWalk)) {
            ScreenWalkInventorySync.scheduleDirectUse(operation, useAction);
            return;
        }
        PlayerInventoryComponent.script.cleanup();
        ScreenWalkInventorySync.scheduleUseFunTime(screenWalk, operation, useAction);
    }

    public static void bundleUseFromHandRestore(class_1735 slot, Runnable useAction) {
        if (slot == null || useAction == null || ScreenWalkInventorySync.mc.field_1724 == null) {
            return;
        }
        if (!PlayerInventoryUtil.isBundleBackedSlot(slot)) {
            ScreenWalkInventorySync.swapUseRestore(slot, useAction);
            return;
        }
        if (PlayerInventoryComponent.isInventoryActionBusy()) {
            pendingOperations.offer(() -> ScreenWalkInventorySync.bundleUseFromHandRestore(slot, useAction));
            return;
        }
        PlayerInventoryUtil.BundleResolution resolution = PlayerInventoryUtil.prepareBundleHandResolution(slot);
        if (resolution == null) {
            return;
        }
        boolean[] extracted = new boolean[]{false};
        ScreenWalk screenWalk = ScreenWalk.getInstance();
        boolean screenWalkSwap = ScreenWalkInventorySync.shouldUseScreenWalkSwap(screenWalk);
        PlayerInventoryComponent.lockInventoryActions(9);
        PlayerInventoryComponent.script.cleanup().addTickStep(1, () -> ScreenWalkInventorySync.runBundleAction(screenWalk, screenWalkSwap, () -> {
            extracted[0] = PlayerInventoryUtil.extractBundleItemToHand(resolution) != null;
        })).addTickStep(2, () -> {
            if (extracted[0]) {
                ScreenWalkInventorySync.useBundleTarget(resolution, useAction);
            }
        }).addTickStep(7, () -> {
            if (!extracted[0] && ScreenWalkInventorySync.mc.field_1724.field_7498.method_34255().method_7960()) {
                return;
            }
            ScreenWalkInventorySync.runBundleAction(screenWalk, screenWalkSwap, () -> PlayerInventoryUtil.returnBundleItemFromHand(resolution));
        }).addTickStep(8, () -> {
            PlayerInventoryUtil.updateSlots();
            ScreenWalkInventorySync.finishInventoryAction();
        });
    }

    private static void useBundleTarget(PlayerInventoryUtil.BundleResolution resolution, Runnable useAction) {
        class_1799 held = ScreenWalkInventorySync.mc.field_1724.method_6047();
        if (held.method_7960() || !class_1799.method_31577((class_1799)held, (class_1799)resolution.targetStack())) {
            return;
        }
        useAction.run();
    }

    private static void runBundleAction(ScreenWalk screenWalk, boolean syncing, Runnable action) {
        if (!syncing) {
            action.run();
            return;
        }
        screenWalk.runExternalBundleSwap(action);
        screenWalk.finishExternalInventorySwap();
    }

    private static void scheduleDirectUse(InventoryUse operation, Runnable useAction) {
        PlayerInventoryComponent.lockInventoryActions(11);
        PlayerInventoryComponent.script.cleanup().addTickStep(0, operation::swapIn).addTickStep(4, () -> operation.use(useAction)).addTickStep(8, operation::beginRestore).addTickStep(0, () -> {}, operation::restore).addTickStep(9, PlayerInventoryUtil::updateSlots).addTickStep(10, ScreenWalkInventorySync::finishInventoryAction);
    }

    private static boolean isHotbarSlot(class_1735 slot) {
        return slot.field_7871 instanceof class_1661 && slot.method_34266() >= 0 && slot.method_34266() < 9;
    }

    private static void scheduleHotbarUse(InventoryUse operation, Runnable useAction) {
        if (operation.source.method_34266() == operation.selectedSlot) {
            PlayerInventoryComponent.runInventoryAction(() -> operation.use(useAction));
            return;
        }
        PlayerInventoryComponent.lockInventoryActions(8);
        PlayerInventoryComponent.script.cleanup().addTickStep(0, operation::select).addTickStep(3, () -> operation.use(useAction)).addTickStep(6, operation::beginRestore).addTickStep(0, () -> {}, operation::restore).addTickStep(7, ScreenWalkInventorySync::finishInventoryAction);
    }

    private static void selectHotbarSlot(int slot) {
        ScreenWalkInventorySync.mc.field_1724.method_31548().method_61496(slot);
    }

    private static void scheduleUseFunTime(ScreenWalk screenWalk, InventoryUse operation, Runnable useAction) {
        ScreenWalkInventorySync.stop(screenWalk);
        PlayerInventoryComponent.lockInventoryActions(3);
        PlayerInventoryComponent.script.addTickStep(1, PlayerInventoryComponent::rotateToCamera).addTickStep(2, () -> ScreenWalkInventorySync.runExternalUse(screenWalk, operation::swapIn)).addTickStep(2, () -> operation.use(useAction)).addTickStep(2, operation::beginRestore).addTickStep(0, () -> ScreenWalkInventorySync.restoreMoveKeysIfIdle(screenWalk), () -> ScreenWalkInventorySync.runExternalRestore(screenWalk, operation::restore));
    }

    private static void runExternalUse(ScreenWalk screenWalk, Runnable action) {
        screenWalk.runExternalInventorySwap(action);
        screenWalk.finishExternalInventorySwap();
    }

    private static boolean runExternalRestore(ScreenWalk screenWalk, BooleanSupplier condition) {
        boolean[] result = new boolean[]{false};
        screenWalk.runExternalInventorySwap(() -> {
            result[0] = condition.getAsBoolean();
        });
        screenWalk.finishExternalInventorySwap();
        return result[0];
    }

    private static boolean shouldUseScreenWalkSwap(ScreenWalk screenWalk) {
        return screenWalk != null && screenWalk.getBypass().isSelected("Default") && (screenWalk.isState() || ServerUtil.isFunTime() || ScreenWalkInventorySync.isSpaceMode()) && ScreenWalkInventorySync.mc.field_1724 != null;
    }

    private static boolean isSpaceMode() {
        ServerHelper serverHelper = ServerHelper.getInstance();
        return serverHelper != null && serverHelper.isSpaceMode();
    }

    private static void scheduleFunTime(ScreenWalk screenWalk, Runnable action, Runnable completion) {
        PlayerInventoryComponent.lockInventoryActions(2);
        PlayerInventoryComponent.script.addTickStep(0, () -> {
            ScreenWalkInventorySync.stop(screenWalk);
            PlayerInventoryComponent.rotateToCamera();
        }).addTickStep(1, () -> {
            try {
                screenWalk.runExternalInventorySwap(action);
                screenWalk.finishExternalInventorySwap();
            }
            finally {
                ScreenWalkInventorySync.restoreMoveKeysIfIdle(screenWalk);
            }
            if (completion != null) {
                completion.run();
            }
        });
    }

    private static void stop(ScreenWalk screenWalk) {
        screenWalk.setExternalInventorySwapBlocked(true);
        PlayerInventoryComponent.disableMoveKeys();
    }

    private static void restoreMoveKeysIfIdle(ScreenWalk screenWalk) {
        screenWalk.setExternalInventorySwapBlocked(false);
        ScreenWalkInventorySync.restoreMoveKeys();
        ScreenWalkInventorySync.finishInventoryAction();
    }

    private static void finishInventoryAction() {
        PlayerInventoryComponent.unlockInventoryActions();
        ScreenWalkInventorySync.runNextPendingAction();
    }

    private static void restoreMoveKeys() {
        PlayerInventoryComponent.canMove = true;
        PlayerInventoryComponent.updateMoveKeys();
    }

    public static void runNextPendingAction() {
        if (!pendingOperations.isEmpty() && !PlayerInventoryComponent.isInventoryActionBusy()) {
            pendingOperations.poll().run();
        }
    }

    public static void clearPendingOperations() {
        pendingOperations.clear();
        bundleHandSessions.clear();
    }

    private ScreenWalkInventorySync() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private record UseRequest(class_746 player, class_1735 source, class_1799 stack) {
        private class_1735 resolve() {
            if (QuickImports.mc.field_1724 != this.player || this.stack.method_7960()) {
                return null;
            }
            class_1735 current = PlayerInventoryUtil.resolveSlot(this.source);
            if (current != null && class_1799.method_31577((class_1799)current.method_7677(), (class_1799)this.stack)) {
                return current;
            }
            return PlayerInventoryUtil.slots().filter(slot -> slot.field_7871 == this.source.field_7871 && class_1799.method_31577((class_1799)slot.method_7677(), (class_1799)this.stack)).findFirst().orElse(null);
        }
    }

    private static final class InventoryUse {
        private final class_746 player;
        private final class_1735 source;
        private final class_1799 requested;
        private final int selectedSlot;
        private final boolean hotbar;
        private boolean swapRequested;
        private boolean selected;
        private boolean restoreRequested;
        private int lastRestoreTick = Integer.MIN_VALUE;
        private int restoreClicks;
        private boolean restoreSent;

        private InventoryUse(class_1735 source, class_1799 requested) {
            this.player = QuickImports.mc.field_1724;
            this.source = source;
            this.requested = requested;
            this.selectedSlot = this.player.method_31548().method_67532();
            this.hotbar = ScreenWalkInventorySync.isHotbarSlot(source);
        }

        private boolean valid() {
            return QuickImports.mc.field_1724 == this.player && QuickImports.mc.field_1761 != null;
        }

        private void select() {
            if (!this.valid()) {
                return;
            }
            class_1735 current = PlayerInventoryUtil.resolveSlot(this.source);
            if (current == null || !class_1799.method_31577((class_1799)current.method_7677(), (class_1799)this.requested)) {
                return;
            }
            ScreenWalkInventorySync.selectHotbarSlot(this.source.method_34266());
            this.selected = true;
        }

        private void swapIn() {
            if (!this.valid() || this.swapRequested || !this.player.field_7512.method_34255().method_7960()) {
                return;
            }
            class_1735 current = PlayerInventoryUtil.resolveSlot(this.source);
            if (current == null || !class_1799.method_31577((class_1799)current.method_7677(), (class_1799)this.requested)) {
                return;
            }
            this.swapRequested = true;
            PlayerInventoryUtil.clickSlot(current, this.selectedSlot, class_1713.field_7791, false);
        }

        private void use(Runnable action) {
            int heldSlot;
            if (!this.valid()) {
                return;
            }
            class_1799 heldStack = this.player.method_31548().method_5438(heldSlot = this.hotbar ? this.source.method_34266() : this.selectedSlot);
            if (heldStack.method_7960() || !heldStack.method_31574(this.requested.method_7909())) {
                return;
            }
            ScreenWalkInventorySync.selectHotbarSlot(heldSlot);
            this.selected = true;
            action.run();
        }

        private void beginRestore() {
            this.restoreRequested = true;
        }

        private boolean restore() {
            if (!this.restoreRequested) {
                return false;
            }
            if (!this.valid()) {
                this.reset();
                return true;
            }
            if (!this.swapRequested) {
                this.restoreHotbar();
                this.reset();
                return true;
            }
            int age = this.player.field_6012;
            class_1799 hand = this.player.method_31548().method_5438(this.selectedSlot);
            boolean stillHolding = !hand.method_7960() && hand.method_31574(this.requested.method_7909());
            if (!stillHolding) {
                this.restoreHotbar();
                this.reset();
                return true;
            }
            if (!this.player.field_7512.method_34255().method_7960()) {
                return false;
            }
            if (this.restoreSent && age - this.lastRestoreTick < 2) {
                return false;
            }
            if (++this.restoreClicks > 40) {
                this.restoreHotbar();
                this.reset();
                return true;
            }
            this.restoreSent = true;
            this.lastRestoreTick = age;
            class_1735 current = PlayerInventoryUtil.resolveSlot(this.source);
            if (current != null) {
                PlayerInventoryUtil.clickSlot(current, this.selectedSlot, class_1713.field_7791, false);
            }
            return false;
        }

        private void restoreHotbar() {
            if (this.hotbar && this.selected && this.player.method_31548().method_67532() == this.source.method_34266()) {
                ScreenWalkInventorySync.selectHotbarSlot(this.selectedSlot);
            }
        }

        private void reset() {
            this.swapRequested = false;
            this.selected = false;
        }
    }
}

