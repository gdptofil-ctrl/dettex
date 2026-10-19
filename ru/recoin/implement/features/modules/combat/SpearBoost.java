/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_12125
 *  net.minecraft.class_1268
 *  net.minecraft.class_1304
 *  net.minecraft.class_1735
 *  net.minecraft.class_1799
 *  net.minecraft.class_1890
 *  net.minecraft.class_1893
 *  net.minecraft.class_2596
 *  net.minecraft.class_2868
 *  net.minecraft.class_3675
 *  net.minecraft.class_3675$class_306
 *  net.minecraft.class_5134
 *  net.minecraft.class_6880
 *  net.minecraft.class_6880$class_6883
 *  net.minecraft.class_746
 *  net.minecraft.class_7924
 *  net.minecraft.class_9285
 *  net.minecraft.class_9334
 */
package ru.recoin.implement.features.modules.combat;

import net.minecraft.class_12125;
import net.minecraft.class_1268;
import net.minecraft.class_1304;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_1890;
import net.minecraft.class_1893;
import net.minecraft.class_2596;
import net.minecraft.class_2868;
import net.minecraft.class_3675;
import net.minecraft.class_5134;
import net.minecraft.class_6880;
import net.minecraft.class_746;
import net.minecraft.class_7924;
import net.minecraft.class_9285;
import net.minecraft.class_9334;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.common.util.entity.PlayerInventoryComponent;
import ru.recoin.common.util.entity.ScreenWalkInventorySync;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.keyboard.KeyEvent;
import ru.recoin.implement.events.player.TickEvent;

public class SpearBoost
extends Module {
    private transient class_746 owner;
    private transient class_1799 spear;
    private transient class_1799 bufferStack;
    private transient int originalSlot = -1;
    private transient int fastSlot = -1;
    private transient int spearIndex = -1;
    private transient int startedAt;
    private transient boolean repeatRequested;
    private transient boolean swapping;
    private transient boolean inventorySwapped;
    private transient int generation;

    public SpearBoost() {
        super("SpearBoost", "Spear Boost", ModuleCategory.COMBAT);
    }

    public static SpearBoost getInstance() {
        return Instance.get(SpearBoost.class);
    }

    public boolean handleAttackClick() {
        if (!this.isState() || SpearBoost.mc.field_1724 == null || SpearBoost.mc.field_1687 == null || SpearBoost.mc.field_1761 == null || SpearBoost.mc.field_1755 != null || SpearBoost.mc.field_1724.method_7325() || SpearBoost.mc.field_1724.method_3144() || SpearBoost.mc.field_1724.method_6115()) {
            return false;
        }
        if (this.owner != null) {
            if (SpearBoost.mc.field_1724 == this.owner && SpearBoost.mc.field_1724.method_31548().method_67532() == this.fastSlot) {
                this.repeatRequested = true;
                return true;
            }
            this.restore();
        }
        if (PlayerInventoryComponent.isInventoryActionBusy() || SpearBoost.mc.field_1724.field_7512 != SpearBoost.mc.field_1724.field_7498) {
            return false;
        }
        int source = this.findSpear();
        if (source < 0) {
            return false;
        }
        class_1799 held = SpearBoost.mc.field_1724.method_31548().method_5438(source);
        int candidate = this.fastestSlot(held, source);
        if (candidate < 0) {
            return false;
        }
        this.owner = SpearBoost.mc.field_1724;
        this.spear = held.method_7972();
        this.originalSlot = SpearBoost.mc.field_1724.method_31548().method_67532();
        this.fastSlot = candidate;
        this.spearIndex = source;
        this.bufferStack = SpearBoost.mc.field_1724.method_31548().method_5438(this.fastSlot).method_7972();
        this.startedAt = SpearBoost.mc.field_1724.field_6012;
        this.repeatRequested = false;
        this.select(this.fastSlot);
        return true;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (this.owner == null) {
            return;
        }
        if (SpearBoost.mc.field_1724 != this.owner || SpearBoost.mc.field_1687 == null || SpearBoost.mc.field_1761 == null) {
            this.clear();
            return;
        }
        if (!SpearBoost.mc.field_1690.field_1886.method_1434()) {
            this.repeatRequested = false;
        }
        if (SpearBoost.mc.field_1755 != null || SpearBoost.mc.field_1724.method_6115() || SpearBoost.mc.field_1724.method_31548().method_67532() != this.fastSlot) {
            this.restore();
            return;
        }
        if (this.swapping || PlayerInventoryComponent.isInventoryActionBusy()) {
            return;
        }
        if (!SpearBoost.mc.field_1724.method_31548().method_5438(this.spearIndex).method_31574(this.spear.method_7909()) || !class_1799.method_31577((class_1799)this.bufferStack, (class_1799)SpearBoost.mc.field_1724.method_31548().method_5438(this.fastSlot))) {
            this.restore();
            return;
        }
        if (SpearBoost.mc.field_1724.field_6012 - this.startedAt < 1) {
            return;
        }
        if (SpearBoost.mc.field_1724.field_6012 - this.startedAt > 40) {
            this.restore();
            return;
        }
        if (SpearBoost.mc.field_1724.method_75202(this.spear, 0)) {
            return;
        }
        if (this.spearIndex < 9) {
            this.select(this.spearIndex);
            this.jab();
            this.finishClick();
            return;
        }
        class_1735 source = this.inventorySlot(this.owner, this.spearIndex);
        if (source == null || !this.owner.field_7512.method_34255().method_7960()) {
            this.restore();
            return;
        }
        this.swapping = true;
        int token = this.generation;
        class_746 player = this.owner;
        int sourceIndex = this.spearIndex;
        int buffer = this.fastSlot;
        int selected = this.originalSlot;
        class_1799 expectedSpear = this.spear;
        class_1799 expectedBuffer = this.bufferStack;
        ScreenWalkInventorySync.swapSlot(source, this.fastSlot, false, () -> {
            if (token != this.generation || SpearBoost.mc.field_1724 != this.owner) {
                this.restoreInventory(player, sourceIndex, buffer, selected, expectedSpear, expectedBuffer, null);
                return;
            }
            this.inventorySwapped = this.owner.method_31548().method_5438(this.fastSlot).method_31574(this.spear.method_7909());
            if (this.inventorySwapped && this.isState() && SpearBoost.mc.field_1755 == null && this.owner.method_31548().method_67532() == this.fastSlot) {
                this.jab();
            }
            this.restoreInventory(player, sourceIndex, buffer, selected, expectedSpear, expectedBuffer, () -> {
                if (token != this.generation || SpearBoost.mc.field_1724 != this.owner) {
                    return;
                }
                this.inventorySwapped = false;
                this.swapping = false;
                this.finishClick();
            });
        });
    }

    private void jab() {
        try {
            class_12125 piercing = (class_12125)SpearBoost.mc.field_1724.method_6047().method_58694(class_9334.field_63631);
            if (piercing != null) {
                SpearBoost.mc.field_1761.method_75407(piercing);
                SpearBoost.mc.field_1724.method_6104(class_1268.field_5808);
            }
        }
        finally {
            if (this.spearIndex < 9) {
                this.select(this.fastSlot);
            }
        }
    }

    private void finishClick() {
        if (this.repeatRequested && this.isState() && SpearBoost.mc.field_1690.field_1886.method_1434()) {
            this.repeatRequested = false;
            this.select(this.fastSlot);
            this.startedAt = SpearBoost.mc.field_1724.field_6012;
        } else {
            this.select(this.originalSlot);
            this.clear();
        }
    }

    @EventHandler
    public void onKey(KeyEvent event) {
        if (event.action() != 0 || this.owner == null) {
            return;
        }
        class_3675.class_306 key = class_3675.method_15981((String)SpearBoost.mc.field_1690.field_1886.method_1428());
        if (event.type() == key.method_1442() && event.key() == key.method_1444()) {
            this.repeatRequested = false;
        }
    }

    private int findSpear() {
        int selected = SpearBoost.mc.field_1724.method_31548().method_67532();
        if (SpearBoost.mc.field_1724.method_6047().method_57826(class_9334.field_63631)) {
            return selected;
        }
        int fallback = -1;
        for (int index = 0; index < 36; ++index) {
            class_1799 stack = SpearBoost.mc.field_1724.method_31548().method_5438(index);
            if (!stack.method_57826(class_9334.field_63631)) continue;
            if (this.hasLunge(stack)) {
                return index;
            }
            if (fallback >= 0) continue;
            fallback = index;
        }
        return fallback;
    }

    private int fastestSlot(class_1799 held, int source) {
        double base = SpearBoost.mc.field_1724.method_45326(class_5134.field_23723);
        double best = this.attackSpeed(held, base);
        int result = -1;
        for (int slot = 0; slot < 9; ++slot) {
            class_1799 stack;
            double speed;
            if (slot == source || !((speed = this.attackSpeed(stack = SpearBoost.mc.field_1724.method_31548().method_5438(slot), base)) > best)) continue;
            best = speed;
            result = slot;
        }
        return result;
    }

    private double attackSpeed(class_1799 stack, double base) {
        return ((class_9285)stack.method_58695(class_9334.field_49636, (Object)class_9285.field_49326)).method_57481(class_5134.field_23723, base, class_1304.field_6173);
    }

    private boolean hasLunge(class_1799 stack) {
        if (!stack.method_57826(class_9334.field_63631)) {
            return false;
        }
        class_6880.class_6883 lunge = SpearBoost.mc.field_1687.method_30349().method_30530(class_7924.field_41265).method_10223(class_1893.field_63420.method_29177()).orElse(null);
        return lunge != null && class_1890.method_8225((class_6880)lunge, (class_1799)stack) > 0;
    }

    private void select(int slot) {
        SpearBoost.mc.field_1724.method_31548().method_61496(slot);
        SpearBoost.mc.field_1724.field_3944.method_52787((class_2596)new class_2868(slot));
    }

    private void restore() {
        class_746 player = this.owner;
        int buffer = this.fastSlot;
        int source = this.spearIndex;
        int selected = this.originalSlot;
        class_1799 expectedSpear = this.spear;
        class_1799 expectedBuffer = this.bufferStack;
        if (SpearBoost.mc.field_1724 == player && this.inventorySwapped) {
            this.restoreInventory(player, source, buffer, selected, expectedSpear, expectedBuffer, null);
        } else if (SpearBoost.mc.field_1724 == this.owner && this.originalSlot >= 0 && SpearBoost.mc.field_1724.method_31548().method_67532() == this.fastSlot) {
            this.select(this.originalSlot);
        }
        this.clear();
    }

    private void restoreInventory(class_746 player, int source, int buffer, int selected, class_1799 expectedSpear, class_1799 expectedBuffer, Runnable completion) {
        ScreenWalkInventorySync.whenIdle(() -> {
            if (SpearBoost.mc.field_1724 != player || source < 9 || !player.field_7512.method_34255().method_7960()) {
                return;
            }
            class_1799 current = player.method_31548().method_5438(buffer);
            if (!current.method_7960() && !current.method_31574(expectedSpear.method_7909()) || !class_1799.method_31577((class_1799)player.method_31548().method_5438(source), (class_1799)expectedBuffer)) {
                if (completion != null) {
                    completion.run();
                }
                return;
            }
            ScreenWalkInventorySync.swapSlot(this.inventorySlot(player, source), buffer, false, () -> {
                if (SpearBoost.mc.field_1724 != player) {
                    return;
                }
                if (completion != null) {
                    completion.run();
                } else if (player.method_31548().method_67532() == buffer) {
                    this.select(selected);
                }
            });
        });
    }

    private class_1735 inventorySlot(class_746 player, int index) {
        return player.field_7512.field_7761.stream().filter(slot -> slot.field_7871 == player.method_31548() && slot.method_34266() == index).findFirst().orElse(null);
    }

    private void clear() {
        this.owner = null;
        this.spear = null;
        this.bufferStack = null;
        this.spearIndex = -1;
        this.fastSlot = -1;
        this.originalSlot = -1;
        this.repeatRequested = false;
        this.inventorySwapped = false;
        this.swapping = false;
        ++this.generation;
    }

    @Override
    public void deactivate() {
        this.restore();
        super.deactivate();
    }
}

