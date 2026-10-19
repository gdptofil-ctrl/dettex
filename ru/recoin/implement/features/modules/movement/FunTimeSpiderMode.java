/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMaps
 *  net.minecraft.class_10192
 *  net.minecraft.class_10936$class_10937
 *  net.minecraft.class_10938
 *  net.minecraft.class_1263
 *  net.minecraft.class_1268
 *  net.minecraft.class_1293
 *  net.minecraft.class_1294
 *  net.minecraft.class_1297
 *  net.minecraft.class_1304
 *  net.minecraft.class_1657
 *  net.minecraft.class_1713
 *  net.minecraft.class_1723
 *  net.minecraft.class_1735
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1922
 *  net.minecraft.class_1937
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_238
 *  net.minecraft.class_2382
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_2596
 *  net.minecraft.class_2653
 *  net.minecraft.class_2680
 *  net.minecraft.class_2813
 *  net.minecraft.class_2846
 *  net.minecraft.class_2846$class_2847
 *  net.minecraft.class_2885
 *  net.minecraft.class_3532
 *  net.minecraft.class_3965
 *  net.minecraft.class_638
 *  net.minecraft.class_7923
 *  net.minecraft.class_9290
 *  net.minecraft.class_9334
 */
package ru.recoin.implement.features.modules.movement;

import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_10192;
import net.minecraft.class_10936;
import net.minecraft.class_10938;
import net.minecraft.class_1263;
import net.minecraft.class_1268;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_1723;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1922;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2596;
import net.minecraft.class_2653;
import net.minecraft.class_2680;
import net.minecraft.class_2813;
import net.minecraft.class_2846;
import net.minecraft.class_2885;
import net.minecraft.class_3532;
import net.minecraft.class_3965;
import net.minecraft.class_638;
import net.minecraft.class_7923;
import net.minecraft.class_9290;
import net.minecraft.class_9334;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.entity.PlayerInventoryComponent;
import ru.recoin.common.util.entity.SimulatedPlayer;
import ru.recoin.common.util.task.TaskPriority;
import ru.recoin.common.util.world.ServerUtil;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.InputEvent;
import ru.recoin.implement.features.draggables.Notifications;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationConfig;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.AngleSmoothMode;
import ru.recoin.implement.features.modules.movement.Spider;

final class FunTimeSpiderMode
implements QuickImports {
    private static final RotationConfig ROTATION = new RotationConfig(new AngleSmoothMode("Spider"){

        @Override
        public Angle limitAngleChange(Angle current, Angle target, class_243 vector, class_1297 entity) {
            return target;
        }

        @Override
        public class_243 randomValue() {
            return class_243.field_1353;
        }
    }, false, false);
    private final Spider owner;
    private class_638 world;
    private boolean running;
    private boolean disabling;
    private boolean restoringMode;
    private boolean inventoryBorrowed;
    private boolean hotbarBorrowed;
    private boolean headSwap;
    private boolean offhandPrepared;
    private boolean bootsBorrowed;
    private int sourceSlot = -1;
    private int originalSelected = -1;
    private int offhandSlot = -1;
    private int bootsSlot = -1;
    private int bootsHotbar = -1;
    private int rotationIdleTicks = -1;
    private long lastAction;
    private long inventoryDeadline;
    private Runnable inventoryAction;
    private Runnable beforePhysics;
    private Runnable afterPhysics;

    FunTimeSpiderMode(Spider owner) {
        this.owner = owner;
    }

    boolean isRunning() {
        return this.running;
    }

    void reset() {
        RotationController.INSTANCE.release(this.owner);
        this.world = null;
        this.restoringMode = false;
        this.disabling = false;
        this.running = false;
        this.clearItem();
        this.bootsBorrowed = false;
        this.offhandPrepared = false;
        this.bootsHotbar = -1;
        this.bootsSlot = -1;
        this.offhandSlot = -1;
        this.rotationIdleTicks = -1;
        this.inventoryDeadline = 0L;
        this.lastAction = 0L;
        this.afterPhysics = null;
        this.beforePhysics = null;
        this.inventoryAction = null;
    }

    boolean requestDisable() {
        if (!this.running || FunTimeSpiderMode.mc.field_1724 == null || FunTimeSpiderMode.mc.field_1687 != this.world || !this.busy()) {
            return false;
        }
        this.disabling = true;
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void prepare(boolean selected) {
        if (FunTimeSpiderMode.mc.field_1724 == null || FunTimeSpiderMode.mc.field_1687 == null || FunTimeSpiderMode.mc.field_1761 == null || FunTimeSpiderMode.mc.field_1687 != this.world) {
            boolean finish = this.disabling;
            this.reset();
            if (finish) {
                this.owner.finishFunTimeDisable();
                return;
            }
            if (FunTimeSpiderMode.mc.field_1724 == null || FunTimeSpiderMode.mc.field_1687 == null || FunTimeSpiderMode.mc.field_1761 == null) {
                return;
            }
        }
        if (!this.running) {
            if (!selected) {
                return;
            }
            this.running = true;
            this.world = FunTimeSpiderMode.mc.field_1687;
        }
        this.restoringMode |= !selected;
        boolean rotated = false;
        try {
            if (this.inventoryAction != null) {
                if (System.currentTimeMillis() < this.inventoryDeadline || FunTimeSpiderMode.mc.field_1724.field_7512 != FunTimeSpiderMode.mc.field_1724.field_7498 || PlayerInventoryComponent.isInventoryActionBusy()) {
                    this.stopHorizontalMovement();
                    return;
                }
                Runnable action = this.inventoryAction;
                this.inventoryAction = null;
                action.run();
            }
            this.clampCeiling();
            if (this.disabling || this.restoringMode) {
                if (this.elapsed() >= 100L && this.beforePhysics == null && this.afterPhysics == null) {
                    if (this.hasBorrowedItems()) {
                        this.scheduleInventory(this::restoreInventory);
                    } else {
                        boolean finish = this.disabling;
                        this.reset();
                        if (finish) {
                            this.owner.finishFunTimeDisable();
                        }
                    }
                }
                return;
            }
            if (this.warnMissingItem()) {
                this.owner.setState(false);
                return;
            }
            this.equipJumpBoots();
            if (this.inventoryAction != null || !ServerUtil.isFunTime() || this.elapsed() < 100L) {
                return;
            }
            class_1792 item = this.owner.getCustomBlockItem();
            int index = this.findItem(item);
            class_238 predicted = SimulatedPlayer.simulateLocalPlayer((int)1).boundingBox;
            if (!this.offhandPrepared && this.offhandSlot < 0 && !FunTimeSpiderMode.mc.field_1724.method_6079().method_7960() && this.findWall(predicted) != null) {
                this.prepareOffhand();
                return;
            }
            if (!FunTimeSpiderMode.mc.field_1724.method_6047().method_31574(item) && this.findWall(predicted) != null) {
                this.selectItem(index);
                return;
            }
            class_3965 hit = this.findWall(FunTimeSpiderMode.mc.field_1724.method_5829());
            double jump = FunTimeSpiderMode.mc.field_1724.method_6106();
            if (hit == null || this.verticalMovement((double)jump).field_1351 < jump - 1.0E-5) {
                return;
            }
            class_243 delta = hit.method_17784().method_1020(FunTimeSpiderMode.mc.field_1724.method_33571());
            double horizontal = Math.hypot(delta.field_1352, delta.field_1350);
            float yaw = horizontal < 1.0E-4 ? FunTimeSpiderMode.mc.field_1724.method_36454() : (float)Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0f;
            float pitch = (float)(-Math.toDegrees(Math.atan2(delta.field_1351, horizontal)));
            this.rotate(new Angle(class_3532.method_15393((float)yaw), class_3532.method_15363((float)pitch, (float)-90.0f, (float)90.0f)));
            this.rotationIdleTicks = 0;
            rotated = true;
            this.beforePhysics = () -> {
                boolean swapHands;
                boolean bl = swapHands = ServerUtil.getAnarchy() > 100;
                if (swapHands) {
                    this.swapHands();
                }
                FunTimeSpiderMode.mc.field_1761.method_41931(FunTimeSpiderMode.mc.field_1687, sequence -> new class_2885(class_1268.field_5808, hit, sequence));
                this.afterPhysics = () -> {
                    if (swapHands) {
                        this.swapHands();
                    }
                    class_243 velocity = FunTimeSpiderMode.mc.field_1724.method_18798();
                    FunTimeSpiderMode.mc.field_1724.method_18800(velocity.field_1352, (double)FunTimeSpiderMode.mc.field_1724.method_6106(), velocity.field_1350);
                    FunTimeSpiderMode.mc.field_1724.method_24830(true);
                };
            };
            this.lastAction = System.currentTimeMillis();
        }
        finally {
            if (!rotated && this.running && this.rotationIdleTicks >= 0) {
                this.rotate(new Angle(FunTimeSpiderMode.mc.field_1724.method_36454(), FunTimeSpiderMode.mc.field_1724.method_36455()));
                if (this.rotationIdleTicks++ > 20) {
                    this.rotationIdleTicks = -1;
                    RotationController.INSTANCE.release(this.owner);
                }
            }
        }
    }

    void tick() {
        if (!this.validWorld()) {
            return;
        }
        Runnable action = this.beforePhysics;
        this.beforePhysics = null;
        if (action != null) {
            action.run();
        }
    }

    void postTick() {
        if (!this.validWorld()) {
            return;
        }
        Runnable action = this.afterPhysics;
        this.afterPhysics = null;
        if (action != null) {
            action.run();
        }
    }

    void input(InputEvent event) {
        if (!this.validWorld()) {
            return;
        }
        if (this.inventoryAction != null) {
            event.inputNonePreservingSneak();
        }
        if (this.isJumpSuppressed()) {
            event.setJumping(true);
        }
    }

    boolean isJumpSuppressed() {
        return this.running && FunTimeSpiderMode.mc.field_1687 == this.world && this.lastAction != 0L && this.elapsed() < 100L;
    }

    void packet(PacketEvent event) {
        class_2653 update;
        if (!this.validWorld() || !event.isReceive() || this.elapsed() >= 300L) {
            return;
        }
        class_2596<?> class_25962 = event.getPacket();
        if (class_25962 instanceof class_2653 && (update = (class_2653)class_25962).method_11450() == FunTimeSpiderMode.mc.field_1724.method_31548().method_67532() + 36 && update.method_11449().method_7909() == FunTimeSpiderMode.mc.field_1724.method_6079().method_7909()) {
            event.cancel();
        }
    }

    boolean blocksHotbar() {
        return this.running && (this.inventoryBorrowed || this.hotbarBorrowed || this.sourceSlot >= 0 || this.offhandPrepared || this.offhandSlot >= 0);
    }

    private boolean validWorld() {
        return this.running && FunTimeSpiderMode.mc.field_1724 != null && FunTimeSpiderMode.mc.field_1687 == this.world && FunTimeSpiderMode.mc.field_1761 != null;
    }

    private long elapsed() {
        return System.currentTimeMillis() - this.lastAction;
    }

    private boolean hasBorrowedItems() {
        return this.blocksHotbar() || this.bootsBorrowed;
    }

    private boolean busy() {
        return this.elapsed() < 100L || this.beforePhysics != null || this.afterPhysics != null || this.inventoryAction != null || this.hasBorrowedItems();
    }

    private void rotate(Angle angle) {
        RotationController.INSTANCE.rotateTo(angle, 1, ROTATION, TaskPriority.HIGH_IMPORTANCE_3, this.owner);
    }

    private void swapHands() {
        FunTimeSpiderMode.mc.field_1724.field_3944.method_52787((class_2596)new class_2846(class_2846.class_2847.field_12969, class_2338.field_10980, class_2350.field_11033));
    }

    private class_3965 findWall(class_238 bounds) {
        class_238 box = bounds.method_1011(1.0E-5);
        class_238 slice = new class_238(box.field_1323, box.field_1322 + 0.5, box.field_1321, box.field_1320, Math.min(box.field_1322 + 1.0, box.field_1325), box.field_1324);
        for (class_2338 cell : class_2338.method_62671((class_238)slice)) {
            for (class_2350 direction : class_2350.values()) {
                class_3965 hit;
                class_2680 state;
                if (direction.method_10164() != 0 || (state = FunTimeSpiderMode.mc.field_1687.method_8320(cell.method_10093(direction))).method_45474() || state.method_55781((class_1937)FunTimeSpiderMode.mc.field_1687, (class_1657)FunTimeSpiderMode.mc.field_1724, new class_3965(class_2338.field_10980.method_46558(), class_2350.field_11035, class_2338.field_10980, false)).method_23665() || state.method_26218((class_1922)FunTimeSpiderMode.mc.field_1687, class_2338.field_10980).method_1110() || (hit = this.wallHit(cell, direction)) == null) continue;
                return hit;
            }
        }
        return null;
    }

    private class_3965 wallHit(class_2338 cell, class_2350 direction) {
        class_2338 support = cell.method_10093(direction).method_10062();
        class_243 eye = FunTimeSpiderMode.mc.field_1724.method_33571();
        class_243 offset = class_243.method_24954((class_2382)direction.method_62675()).method_1021(1.0E-12);
        class_238 plane = new class_238(cell).method_991(new class_238(support)).method_1009(-Math.max(0.05, (double)Math.abs(direction.method_10148())), -Math.max(0.05, (double)Math.abs(direction.method_10164())), -Math.max(0.05, (double)Math.abs(direction.method_10165()))).method_997(offset);
        class_243 point = new class_243(class_3532.method_15350((double)eye.field_1352, (double)plane.field_1323, (double)plane.field_1320), class_3532.method_15350((double)eye.field_1351, (double)plane.field_1322, (double)plane.field_1325), class_3532.method_15350((double)eye.field_1350, (double)plane.field_1321, (double)plane.field_1324));
        class_3965 hit = class_238.method_1010(List.of(class_238.method_29968((class_243)class_243.field_1353)), (class_243)eye, (class_243)point, (class_2338)support);
        if (hit == null || hit.method_17780().method_10153() != direction || eye.method_1022(point) >= 4.45) {
            return null;
        }
        return new class_3965(hit.method_17784().method_1019(offset), hit.method_17780(), support, hit.method_17781());
    }

    private class_243 verticalMovement(double velocity) {
        return class_1297.method_20736((class_1297)FunTimeSpiderMode.mc.field_1724, (class_243)new class_243(0.0, velocity, 0.0), (class_238)FunTimeSpiderMode.mc.field_1724.method_5829().method_35580(1.0E-5, 0.0, 1.0E-5), (class_1937)FunTimeSpiderMode.mc.field_1687, List.of());
    }

    private void clampCeiling() {
        if (this.elapsed() >= 250L) {
            return;
        }
        class_243 velocity = FunTimeSpiderMode.mc.field_1724.method_18798();
        if (velocity.field_1351 <= 0.0) {
            return;
        }
        double allowed = this.verticalMovement((double)velocity.field_1351).field_1351;
        if (allowed + 1.0E-5 < velocity.field_1351) {
            FunTimeSpiderMode.mc.field_1724.method_18800(velocity.field_1352, Math.max(0.0, allowed - 1.0E-4), velocity.field_1350);
        }
    }

    private int findItem(class_1792 item) {
        int selected = FunTimeSpiderMode.mc.field_1724.method_31548().method_67532();
        if (FunTimeSpiderMode.mc.field_1724.method_31548().method_5438(selected).method_31574(item)) {
            return selected;
        }
        for (int i = 0; i < 36; ++i) {
            if (!FunTimeSpiderMode.mc.field_1724.method_31548().method_5438(i).method_31574(item)) continue;
            return i;
        }
        return FunTimeSpiderMode.mc.field_1724.method_6079().method_31574(item) ? 40 : -1;
    }

    private int screenSlot(int index) {
        return FunTimeSpiderMode.mc.field_1724.field_7498.method_37418((class_1263)FunTimeSpiderMode.mc.field_1724.method_31548(), index).orElse(-1);
    }

    private boolean scheduleInventory(Runnable action) {
        if (this.inventoryAction != null || PlayerInventoryComponent.isInventoryActionBusy() || !PlayerInventoryComponent.canMove || FunTimeSpiderMode.mc.field_1724.field_7512 != FunTimeSpiderMode.mc.field_1724.field_7498) {
            return false;
        }
        this.inventoryAction = action;
        this.inventoryDeadline = System.currentTimeMillis() + 60L;
        this.stopHorizontalMovement();
        return true;
    }

    private void stopHorizontalMovement() {
        FunTimeSpiderMode.mc.field_1724.method_5728(false);
        FunTimeSpiderMode.mc.field_1724.method_18800(0.0, FunTimeSpiderMode.mc.field_1724.method_18798().field_1351, 0.0);
    }

    private void selectItem(int index) {
        if (this.inventoryBorrowed || this.hotbarBorrowed || this.sourceSlot >= 0 || this.inventoryAction != null) {
            return;
        }
        int selected = FunTimeSpiderMode.mc.field_1724.method_31548().method_67532();
        if (index < 9) {
            if (index != selected) {
                this.originalSelected = selected;
                FunTimeSpiderMode.mc.field_1724.method_31548().method_61496(index);
                FunTimeSpiderMode.mc.field_1761.method_2911();
                this.hotbarBorrowed = true;
            }
            return;
        }
        int slot = this.screenSlot(index);
        if (slot < 0) {
            return;
        }
        this.sourceSlot = slot;
        this.originalSelected = selected;
        boolean bl = this.headSwap = !this.offhandPrepared && slot != ((class_1735)FunTimeSpiderMode.mc.field_1724.field_7498.field_7761.getLast()).field_7874 && FunTimeSpiderMode.mc.field_1724.method_6047().method_31574(class_1802.field_8575);
        if (!this.scheduleInventory(() -> {
            if (this.disabling || this.restoringMode || !FunTimeSpiderMode.mc.field_1724.field_7498.method_7611(slot).method_7677().method_31574(this.owner.getCustomBlockItem())) {
                this.clearItem();
                return;
            }
            this.swap(slot, selected);
            if (this.headSwap) {
                this.swap(slot, 40);
            }
            this.inventoryBorrowed = true;
        })) {
            this.clearItem();
        }
    }

    private void prepareOffhand() {
        int slot = this.screenSlot(FunTimeSpiderMode.mc.field_1724.method_31548().method_67532());
        if (slot < 0) {
            return;
        }
        this.offhandSlot = slot;
        if (!this.scheduleInventory(() -> {
            if (this.disabling || this.restoringMode) {
                this.offhandSlot = -1;
                return;
            }
            this.swap(slot, 40);
            this.offhandPrepared = true;
        })) {
            this.offhandSlot = -1;
        }
    }

    private void clearItem() {
        this.originalSelected = -1;
        this.sourceSlot = -1;
        this.headSwap = false;
        this.hotbarBorrowed = false;
        this.inventoryBorrowed = false;
    }

    private void restoreInventory() {
        boolean changed = false;
        if (this.inventoryBorrowed) {
            if (this.headSwap) {
                this.swap(this.sourceSlot, 40);
            }
            this.swap(this.sourceSlot, this.originalSelected);
            changed = true;
        } else if (this.hotbarBorrowed) {
            FunTimeSpiderMode.mc.field_1724.method_31548().method_61496(this.originalSelected);
            FunTimeSpiderMode.mc.field_1761.method_2911();
        }
        this.clearItem();
        if (this.offhandPrepared) {
            this.swap(this.offhandSlot, 40);
            changed = true;
        }
        this.offhandSlot = -1;
        this.offhandPrepared = false;
        if (this.bootsBorrowed) {
            this.swapBoots();
            changed = true;
        }
        this.clearBoots();
        if (changed) {
            this.resyncInventory();
        }
    }

    private void swap(int slot, int button) {
        class_1723 handler = FunTimeSpiderMode.mc.field_1724.field_7498;
        handler.method_7593(slot, button, class_1713.field_7791, (class_1657)FunTimeSpiderMode.mc.field_1724);
        FunTimeSpiderMode.mc.field_1724.field_3944.method_52787((class_2596)new class_2813(handler.field_7763, 0, (short)slot, (byte)button, class_1713.field_7791, Int2ObjectMaps.emptyMap(), class_10938.method_68853((class_1799)handler.method_34255().method_7972(), (class_10936.class_10937)FunTimeSpiderMode.mc.field_1724.field_3944.method_68823())));
    }

    private void resyncInventory() {
        class_1723 handler = FunTimeSpiderMode.mc.field_1724.field_7498;
        class_1799 stack = ((class_1792)class_7923.field_41178.method_10200(ThreadLocalRandom.current().nextInt(0, 101))).method_7854();
        int slot = handler.field_7761.stream().filter(value -> !value.method_7677().method_7960()).mapToInt(value -> value.field_7874).findFirst().orElse(0);
        FunTimeSpiderMode.mc.field_1724.field_3944.method_52787((class_2596)new class_2813(handler.field_7763, 0, (short)slot, (byte)0, class_1713.field_7793, Int2ObjectMaps.emptyMap(), class_10938.method_68853((class_1799)stack, (class_10936.class_10937)FunTimeSpiderMode.mc.field_1724.field_3944.method_68823())));
    }

    private void equipJumpBoots() {
        if (this.bootsBorrowed || this.inventoryAction != null) {
            return;
        }
        class_1293 effect = FunTimeSpiderMode.mc.field_1724.method_6112(class_1294.field_5913);
        class_1799 feet = FunTimeSpiderMode.mc.field_1724.method_6118(class_1304.field_6166);
        if (this.isJumpBoots(feet) || effect != null && (!feet.method_7960() || effect.method_5584() > 200)) {
            return;
        }
        for (int index = 0; index < 36; ++index) {
            if (!this.isJumpBoots(FunTimeSpiderMode.mc.field_1724.method_31548().method_5438(index))) continue;
            int slot = this.screenSlot(index);
            if (slot < 0) {
                return;
            }
            this.bootsSlot = slot;
            int n = this.bootsHotbar = index < 9 ? index : -1;
            if (!this.scheduleInventory(() -> {
                if (this.disabling || this.restoringMode || !this.isJumpBoots(FunTimeSpiderMode.mc.field_1724.field_7498.method_7611(slot).method_7677())) {
                    this.clearBoots();
                    return;
                }
                this.swapBoots();
                this.resyncInventory();
                this.bootsBorrowed = true;
            })) {
                this.clearBoots();
            }
            return;
        }
    }

    private void swapBoots() {
        if (this.bootsHotbar >= 0) {
            this.swap(8, this.bootsHotbar);
        } else {
            this.swap(this.bootsSlot, 8);
            this.swap(8, 8);
            this.swap(this.bootsSlot, 8);
        }
    }

    private void clearBoots() {
        this.bootsBorrowed = false;
        this.bootsHotbar = -1;
        this.bootsSlot = -1;
    }

    private boolean isJumpBoots(class_1799 stack) {
        if (stack.method_7960()) {
            return false;
        }
        class_10192 equipment = (class_10192)stack.method_58694(class_9334.field_54196);
        if (equipment == null || equipment.comp_3174() != class_1304.field_6166) {
            return false;
        }
        if (this.jumpBootName(stack.method_7964().getString())) {
            return true;
        }
        class_9290 lore = (class_9290)stack.method_58695(class_9334.field_49632, (Object)class_9290.field_49340);
        return this.jumpBootName(lore.toString()) || lore.comp_2400().stream().anyMatch(line -> this.jumpBootName(line.getString()));
    }

    private boolean jumpBootName(String value) {
        String name = value.toLowerCase(Locale.ROOT);
        return name.contains("\u043f\u043e\u043f\u0440\u044b\u0433\u0443\u043d") || name.contains("\u043f\u0440\u044b\u0433\u0443\u0447") || name.contains("poprygun");
    }

    private void warn(String message) {
        Notifications.getInstance().addIconList((class_2561)class_2561.method_43470((String)message), 3000L, "\ue07e", -45737);
    }

    boolean warnMissingItem() {
        if (FunTimeSpiderMode.mc.field_1724 == null) {
            return false;
        }
        class_1792 item = this.owner.getCustomBlockItem();
        if (item == null || item == class_1802.field_8162) {
            this.warn("\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0431\u043b\u043e\u043a \u0432 .spider");
            return true;
        }
        if (this.findItem(item) < 0) {
            this.warn("\u0412\u044b\u0431\u0440\u0430\u043d\u043d\u043e\u0433\u043e \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430 \u043d\u0435\u0442 \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435");
            return true;
        }
        return false;
    }
}

