/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1713
 *  net.minecraft.class_1735
 */
package ru.recoin.implement.features.modules.misc;

import net.minecraft.class_1713;
import net.minecraft.class_1735;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.world.ServerUtil;
import ru.recoin.implement.events.item.ClickSlotEvent;

public final class LockSlot
extends Module {
    private final MultiSelectSetting slots = new MultiSelectSetting("Slots", "Locked hotbar slots").value("1", "2", "3", "4", "5", "6", "7", "8", "9");
    private final BooleanSetting onlyPvp = new BooleanSetting("Only PvP", "Lock selected slots only during PvP");

    public static LockSlot getInstance() {
        return Instance.get(LockSlot.class);
    }

    public LockSlot() {
        super("LockSlot", "Lock Slot", ModuleCategory.MISC);
        this.setup(this.slots, this.onlyPvp);
    }

    @EventHandler
    public void onClickSlot(ClickSlotEvent event) {
        if (event.getActionType() == class_1713.field_7795 && this.isLockedSlotId(event.getSlotId())) {
            event.cancel();
        }
    }

    public boolean shouldCancelSelectedSlotDrop(int selectedSlot) {
        return this.isActiveNow() && selectedSlot >= 0 && selectedSlot <= 8 && this.slots.isSelected(String.valueOf(selectedSlot + 1));
    }

    private boolean isLockedSlotId(int slotId) {
        if (!this.isActiveNow() || LockSlot.mc.field_1724 == null || slotId < 0 || slotId >= LockSlot.mc.field_1724.field_7512.field_7761.size()) {
            return false;
        }
        class_1735 slot = LockSlot.mc.field_1724.field_7512.method_7611(slotId);
        return slot.field_7871 == LockSlot.mc.field_1724.method_31548() && slot.method_34266() >= 0 && slot.method_34266() <= 8 && this.slots.isSelected(String.valueOf(slot.method_34266() + 1));
    }

    private boolean isActiveNow() {
        return this.isState() && (!this.onlyPvp.isValue() || ServerUtil.isPvp());
    }
}

