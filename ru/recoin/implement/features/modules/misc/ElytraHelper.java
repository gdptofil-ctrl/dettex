/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1304
 *  net.minecraft.class_1735
 *  net.minecraft.class_1802
 *  net.minecraft.class_746
 */
package ru.recoin.implement.features.modules.misc;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.class_1268;
import net.minecraft.class_1304;
import net.minecraft.class_1735;
import net.minecraft.class_1802;
import net.minecraft.class_746;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BindSetting;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.entity.PlayerInventoryComponent;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.entity.ScreenWalkInventorySync;
import ru.recoin.common.util.task.scripts.Script;
import ru.recoin.implement.events.keyboard.KeyEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;

public class ElytraHelper
extends Module {
    private final BindSetting elytraSetting = new BindSetting("Elytra Swap", "Changes place chest-plate with elytra");
    private final BindSetting fireworkSetting = new BindSetting("Use FireWork", "Swaps and uses fireworks");
    private final Script script = new Script();
    private transient int lastFireworkAge = Integer.MIN_VALUE;
    private transient class_746 fireworkPlayer;

    public ElytraHelper() {
        super("ElytraHelper", "Elytra Helper", ModuleCategory.MISC);
        this.setup(this.elytraSetting, this.fireworkSetting);
    }

    @EventHandler
    public void onKey(KeyEvent e) {
        if (!this.script.isFinished()) {
            return;
        }
        if (e.isKeyDown(this.elytraSetting.getKey())) {
            class_1735 slot = this.chestPlate();
            if (slot != null) {
                ScreenWalkInventorySync.run(() -> PlayerInventoryUtil.moveItem(slot, 6, false, true));
            }
        } else if (e.isKeyDown(this.fireworkSetting.getKey())) {
            this.useFirework();
        }
    }

    private void useFirework() {
        if (ElytraHelper.mc.field_1724 == null || !ElytraHelper.mc.field_1724.method_6128() || !this.canUseFireworkByChestSlot() || PlayerInventoryComponent.isInventoryActionBusy()) {
            return;
        }
        if (this.fireworkPlayer == ElytraHelper.mc.field_1724 && (long)ElytraHelper.mc.field_1724.field_6012 - (long)this.lastFireworkAge < 4L) {
            return;
        }
        class_1735 fireworkSlot = this.getFireworkSlot();
        if (fireworkSlot == null) {
            return;
        }
        this.fireworkPlayer = ElytraHelper.mc.field_1724;
        this.lastFireworkAge = ElytraHelper.mc.field_1724.field_6012;
        ScreenWalkInventorySync.swapUseRestore(fireworkSlot, () -> {
            if (ElytraHelper.mc.field_1724 == this.fireworkPlayer && ElytraHelper.mc.field_1724.method_6128() && this.canUseFireworkByChestSlot()) {
                RotationController controller = RotationController.INSTANCE;
                RotationController.setForcedInteractAngle(controller.getRotation());
                try {
                    PlayerIntersectionUtil.interactItem(class_1268.field_5808);
                }
                finally {
                    RotationController.setForcedInteractAngle(null);
                }
            }
        });
    }

    @EventHandler
    public void onTick(TickEvent e) {
        this.script.update();
    }

    private class_1735 chestPlate() {
        if (Objects.requireNonNull(ElytraHelper.mc.field_1724).method_6118(class_1304.field_6174).method_7909().equals(class_1802.field_8833)) {
            return PlayerInventoryUtil.getSlot(List.of(class_1802.field_22028, class_1802.field_8058, class_1802.field_8873, class_1802.field_8523, class_1802.field_8678, class_1802.field_8577));
        }
        return PlayerInventoryUtil.getSlot(class_1802.field_8833);
    }

    private boolean canUseFireworkByChestSlot() {
        return ElytraHelper.mc.field_1724 != null && ElytraHelper.mc.field_1724.method_6118(class_1304.field_6174).method_31574(class_1802.field_8833);
    }

    private class_1735 getFireworkSlot() {
        return PlayerInventoryUtil.getSlot(class_1802.field_8639, Comparator.comparingInt(slot -> this.isHotbarSlot((class_1735)slot) ? 1 : 0), s -> true);
    }

    private boolean isHotbarSlot(class_1735 slot) {
        return slot.field_7874 >= 36 && slot.field_7874 <= 44;
    }
}

