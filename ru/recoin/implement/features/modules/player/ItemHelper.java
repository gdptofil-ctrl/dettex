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
 *  net.minecraft.class_1802
 *  net.minecraft.class_6880
 */
package ru.recoin.implement.features.modules.player;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1291;
import net.minecraft.class_1294;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_6880;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BindSetting;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.entity.PlayerInventoryComponent;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.entity.ScreenWalkInventorySync;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.player.TickEvent;

public class ItemHelper
extends Module {
    private final BindSetting chorus = ItemHelper.bind("\u041f\u043b\u043e\u0434 \u0445\u043e\u0440\u0443\u0441\u0430");
    private final BindSetting goldenApple = ItemHelper.bind("\u0417\u043e\u043b\u043e\u0442\u043e\u0435 \u044f\u0431\u043b\u043e\u043a\u043e");
    private final BindSetting enchantedApple = ItemHelper.bind("\u0417\u0430\u0447\u0430\u0440\u043e\u0432\u0430\u043d\u043d\u043e\u0435 \u0437\u043e\u043b\u043e\u0442\u043e\u0435 \u044f\u0431\u043b\u043e\u043a\u043e");
    private final BindSetting healingPotion = ItemHelper.bind("\u0417\u0435\u043b\u044c\u0435 \u0438\u0441\u0446\u0435\u043b\u0435\u043d\u0438\u044f");
    private final BindSetting shield = ItemHelper.bind("\u0429\u0438\u0442");
    private final BindSetting crossbow = ItemHelper.bind("\u0410\u0440\u0431\u0430\u043b\u0435\u0442");
    private final List<HeldItem> items = List.of(new HeldItem(this.chorus, () -> PlayerInventoryUtil.getSlot(class_1802.field_8233, s -> true)), new HeldItem(this.goldenApple, () -> PlayerInventoryUtil.getSlot(class_1802.field_8463, s -> true)), new HeldItem(this.enchantedApple, () -> PlayerInventoryUtil.getSlot(class_1802.field_8367, s -> true)), new HeldItem(this.healingPotion, () -> PlayerInventoryUtil.getPotion((class_6880<class_1291>)class_1294.field_5915)), new HeldItem(this.shield, () -> PlayerInventoryUtil.getSlot(class_1802.field_8255, s -> true)), new HeldItem(this.crossbow, () -> PlayerInventoryUtil.getSlot(class_1802.field_8399, s -> true)));
    private HeldItem active;
    private HeldItem waitForRelease;
    private class_1735 sourceSlot;
    private class_1792 expectedItem;
    private int selectedHotbarSlot = -1;
    private int phaseTicks;
    private int useAttempts;
    private int initialStackCount;
    private boolean swapped;
    private boolean selectedDirectly;
    private boolean observedUsing;
    private int activeUseTicks;

    public static ItemHelper getInstance() {
        return Instance.get(ItemHelper.class);
    }

    public ItemHelper() {
        super("ItemHelper", "Item Helper", ModuleCategory.PLAYER);
        this.setup(this.chorus, this.goldenApple, this.enchantedApple, this.healingPotion, this.shield, this.crossbow);
    }

    private static BindSetting bind(String name) {
        return new BindSetting(name, "\u0423\u0434\u0435\u0440\u0436\u0438\u0432\u0430\u0439\u0442\u0435 \u043a\u043b\u0430\u0432\u0438\u0448\u0443 \u0434\u043b\u044f \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u044f").setType(0);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (ItemHelper.mc.field_1724 == null || ItemHelper.mc.field_1761 == null) {
            return;
        }
        HeldItem held = this.findHeldBind();
        if (held == null) {
            this.waitForRelease = null;
        }
        if (this.active == null) {
            if (held != null && held != this.waitForRelease) {
                this.begin(held);
            }
            return;
        }
        if (!this.isPhysicallyHeld(this.active)) {
            this.restore(true);
            return;
        }
        if (!ItemHelper.mc.field_1724.method_6047().method_31574(this.expectedItem)) {
            if (PlayerInventoryComponent.isInventoryActionBusy()) {
                return;
            }
            if (this.observedUsing || ++this.phaseTicks > 20) {
                this.restore(true);
            }
            return;
        }
        if (ItemHelper.mc.field_1724.method_6047().method_7947() < this.initialStackCount) {
            this.restore(true);
            return;
        }
        if (++this.activeUseTicks >= 45) {
            this.restore(true);
            return;
        }
        ItemHelper.mc.field_1690.field_1904.method_23481(true);
        if (!this.observedUsing) {
            if (ItemHelper.mc.field_1724.method_6115()) {
                this.observedUsing = true;
                this.phaseTicks = 0;
                return;
            }
            this.startUsing();
            if (ItemHelper.mc.field_1724.method_6115()) {
                this.observedUsing = true;
                this.phaseTicks = 0;
                return;
            }
            if (ItemHelper.mc.field_1724.method_6047().method_7935((class_1309)ItemHelper.mc.field_1724) <= 0 && this.useAttempts > 0 && this.phaseTicks <= 0) {
                this.restore(true);
            } else if (this.useAttempts > 0 && ++this.phaseTicks >= 20) {
                this.restore(true);
            }
            return;
        }
        if (ItemHelper.mc.field_1724.method_6115()) {
            this.phaseTicks = 0;
            return;
        }
        this.restore(true);
    }

    private HeldItem findHeldBind() {
        return this.items.stream().filter(this::isPhysicallyHeld).findFirst().orElse(null);
    }

    private boolean isPhysicallyHeld(HeldItem item) {
        int key = item.bind().getKey();
        return item.bind().isVisible() && PlayerIntersectionUtil.isKey(PlayerIntersectionUtil.getKeyType(key), key);
    }

    private void begin(HeldItem item) {
        class_1735 slot = item.slot().get();
        if (slot == null || slot.method_7677().method_7960()) {
            return;
        }
        this.active = item;
        this.sourceSlot = slot;
        this.expectedItem = slot.method_7677().method_7909();
        this.initialStackCount = slot.method_7677().method_7947();
        this.selectedHotbarSlot = ItemHelper.mc.field_1724.method_31548().method_67532();
        this.phaseTicks = 0;
        this.useAttempts = 0;
        this.observedUsing = false;
        this.activeUseTicks = 0;
        boolean alreadySelected = slot.field_7871 == ItemHelper.mc.field_1724.method_31548() && slot.method_34266() == this.selectedHotbarSlot;
        boolean hotbar = slot.field_7871 == ItemHelper.mc.field_1724.method_31548() && slot.method_34266() >= 0 && slot.method_34266() < 9;
        this.selectedDirectly = hotbar && !alreadySelected;
        boolean bl = this.swapped = !alreadySelected && !hotbar;
        if (this.selectedDirectly) {
            ItemHelper.mc.field_1724.method_31548().method_61496(slot.method_34266());
        } else if (this.swapped) {
            ScreenWalkInventorySync.swapHand(slot, class_1268.field_5808, true);
        }
    }

    private void startUsing() {
        ++this.useAttempts;
        ItemHelper.mc.field_1690.field_1904.method_23481(true);
        class_1269 result = ItemHelper.mc.field_1761.method_2919((class_1657)ItemHelper.mc.field_1724, class_1268.field_5808);
        int maxUseTime = ItemHelper.mc.field_1724.method_6047().method_7935((class_1309)ItemHelper.mc.field_1724);
        if (maxUseTime > 0 && result.method_23665() && !ItemHelper.mc.field_1724.method_6115()) {
            ItemHelper.mc.field_1724.method_6019(class_1268.field_5808);
        }
        if (ItemHelper.mc.field_1724.method_6115()) {
            this.observedUsing = true;
            this.phaseTicks = 0;
        }
    }

    private void restore(boolean latchUntilRelease) {
        if (ItemHelper.mc.field_1690 != null) {
            ItemHelper.mc.field_1690.field_1904.method_23481(false);
        }
        if (ItemHelper.mc.field_1724 != null && ItemHelper.mc.field_1724.method_6115()) {
            ItemHelper.mc.field_1761.method_2897((class_1657)ItemHelper.mc.field_1724);
        }
        if (this.swapped && this.sourceSlot != null && ItemHelper.mc.field_1724 != null) {
            ScreenWalkInventorySync.swapHand(this.sourceSlot, class_1268.field_5808, true);
        }
        if (this.selectedDirectly && ItemHelper.mc.field_1724 != null && this.selectedHotbarSlot >= 0) {
            ItemHelper.mc.field_1724.method_31548().method_61496(this.selectedHotbarSlot);
        }
        if (latchUntilRelease) {
            this.waitForRelease = this.active;
        }
        this.active = null;
        this.sourceSlot = null;
        this.expectedItem = null;
        this.selectedHotbarSlot = -1;
        this.phaseTicks = 0;
        this.useAttempts = 0;
        this.initialStackCount = 0;
        this.swapped = false;
        this.selectedDirectly = false;
        this.observedUsing = false;
        this.activeUseTicks = 0;
    }

    @Override
    public void deactivate() {
        if (this.active != null) {
            this.restore(false);
        }
        this.waitForRelease = null;
    }

    public boolean isUsingHelperItem() {
        return this.isState() && this.active != null;
    }

    public boolean isActivelyUsing() {
        return this.isState() && this.active != null;
    }

    private record HeldItem(BindSetting bind, Supplier<class_1735> slot) {
    }
}

