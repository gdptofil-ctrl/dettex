/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1657
 *  net.minecraft.class_1713
 *  net.minecraft.class_1735
 *  net.minecraft.class_1799
 *  net.minecraft.class_465
 *  org.lwjgl.glfw.GLFW
 */
package ru.recoin.implement.features.modules.misc;

import java.util.LinkedList;
import java.util.Queue;
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_465;
import org.lwjgl.glfw.GLFW;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.other.StopWatch;
import ru.recoin.implement.events.container.HandledScreenEvent;

public final class ItemScroller
extends Module {
    private final Queue<SlotReference> slotQueue = new LinkedList<SlotReference>();
    private final StopWatch delayTimer = new StopWatch();
    private final ValueSetting delay = new ValueSetting("Delay", "Item scroller delay").setValue(0.0f).range(0, 1000).step(5.0f);
    private int lastDraggedSlot = -1;
    private boolean throwComboDown;

    public ItemScroller() {
        super("ItemScroller", "Item Scroller", ModuleCategory.MISC);
        this.setup(this.delay);
    }

    @EventHandler
    public void onHandledScreen(HandledScreenEvent event) {
        if (ItemScroller.mc.field_1724 == null || ItemScroller.mc.field_1761 == null || !(ItemScroller.mc.field_1755 instanceof class_465)) {
            return;
        }
        boolean throwCombo = this.isThrowComboHeld();
        if (throwCombo && !this.throwComboDown) {
            this.throwMatching(event.getSlotHover());
        }
        this.throwComboDown = throwCombo;
        if (this.isActivationHeld()) {
            SlotReference reference;
            class_1735 slot = event.getSlotHover();
            if (slot != null && slot.field_7874 != this.lastDraggedSlot && this.canMoveSlot(slot) && !this.slotQueue.contains(reference = new SlotReference(slot, slot.field_7874))) {
                this.slotQueue.add(reference);
            }
            this.lastDraggedSlot = slot == null ? -1 : slot.field_7874;
        } else {
            this.lastDraggedSlot = -1;
        }
        while (!this.slotQueue.isEmpty()) {
            SlotReference reference = this.slotQueue.peek();
            if (reference == null || !this.canMoveSlot(reference.slot())) {
                this.slotQueue.poll();
                continue;
            }
            if (this.delay.getValue() > 0.0f && !this.delayTimer.every(this.delay.getValue())) {
                return;
            }
            this.slotQueue.poll();
            this.moveSlot(reference.slot(), reference.slotId());
            this.delayTimer.reset();
        }
    }

    @Override
    public void deactivate() {
        this.slotQueue.clear();
        this.lastDraggedSlot = -1;
        this.throwComboDown = false;
        super.deactivate();
    }

    private boolean isActivationHeld() {
        long window = mc.method_22683().method_4490();
        return GLFW.glfwGetKey((long)window, (int)340) == 1 && GLFW.glfwGetMouseButton((long)window, (int)0) == 1;
    }

    private boolean isThrowComboHeld() {
        long window = mc.method_22683().method_4490();
        boolean control = GLFW.glfwGetKey((long)window, (int)341) == 1 || GLFW.glfwGetKey((long)window, (int)345) == 1;
        boolean shift = GLFW.glfwGetKey((long)window, (int)340) == 1 || GLFW.glfwGetKey((long)window, (int)344) == 1;
        return control && shift && GLFW.glfwGetKey((long)window, (int)81) == 1;
    }

    private void throwMatching(class_1735 hovered) {
        if (hovered == null || !hovered.method_7681()) {
            return;
        }
        class_1799 sample = hovered.method_7677().method_7972();
        boolean playerInventory = hovered.field_7871 == ItemScroller.mc.field_1724.method_31548();
        for (class_1735 slot : ItemScroller.mc.field_1724.field_7512.field_7761) {
            if (!slot.method_7681() || slot.field_7871 == ItemScroller.mc.field_1724.method_31548() != playerInventory || !class_1799.method_31577((class_1799)sample, (class_1799)slot.method_7677())) continue;
            ItemScroller.mc.field_1761.method_2906(ItemScroller.mc.field_1724.field_7512.field_7763, slot.field_7874, 1, class_1713.field_7795, (class_1657)ItemScroller.mc.field_1724);
        }
    }

    private boolean canMoveSlot(class_1735 slot) {
        if (slot == null || !slot.method_7681() || slot.method_7677().method_7960()) {
            return false;
        }
        int id = slot.field_7874;
        if (id < 0 || id >= 9) {
            return id >= 9 && id < 36 ? this.hasSpaceInRange(0, 9) : this.hasSpaceInRange(0, 36);
        }
        return this.hasSpaceInRange(9, 36);
    }

    private boolean hasSpaceInRange(int from, int to) {
        if (ItemScroller.mc.field_1724 == null) {
            return false;
        }
        for (int index = from; index < to; ++index) {
            class_1799 stack = ItemScroller.mc.field_1724.method_31548().method_5438(index);
            if (!stack.method_7960() && (!stack.method_7946() || stack.method_7947() >= stack.method_7914())) continue;
            return true;
        }
        return false;
    }

    private void moveSlot(class_1735 slot, int fallbackSlotId) {
        int slotId;
        if (ItemScroller.mc.field_1724 == null || ItemScroller.mc.field_1761 == null) {
            return;
        }
        int n = slotId = slot == null ? fallbackSlotId : slot.field_7874;
        if (slot != null && (!slot.method_7681() || slot.method_7677().method_7960())) {
            return;
        }
        ItemScroller.mc.field_1761.method_2906(ItemScroller.mc.field_1724.field_7512.field_7763, slotId, 0, class_1713.field_7794, (class_1657)ItemScroller.mc.field_1724);
    }

    private record SlotReference(class_1735 slot, int slotId) {
    }
}

