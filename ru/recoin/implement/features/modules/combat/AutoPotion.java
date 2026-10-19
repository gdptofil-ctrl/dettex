/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1291
 *  net.minecraft.class_1293
 *  net.minecraft.class_1294
 *  net.minecraft.class_1735
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1844
 *  net.minecraft.class_6880
 *  net.minecraft.class_9334
 */
package ru.recoin.implement.features.modules.combat;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1268;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_6880;
import net.minecraft.class_9334;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.entity.PlayerInventoryComponent;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.entity.ScreenWalkInventorySync;
import ru.recoin.common.util.other.StopWatch;
import ru.recoin.common.util.task.TaskPriority;
import ru.recoin.implement.events.player.RotationUpdateEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationConfig;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.SlothSmoothMode;

public class AutoPotion
extends Module {
    private final MultiSelectSetting potions = new MultiSelectSetting("Potions", "\u0417\u0435\u043b\u044c\u044f \u0434\u043b\u044f \u0430\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u043e\u0433\u043e \u0431\u0430\u0444\u0430").value("\u0421\u0438\u043b\u0430", "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c", "\u041e\u0433\u043d\u0435\u0441\u0442\u043e\u0439\u043a\u043e\u0441\u0442\u044c").selected("\u0421\u0438\u043b\u0430", "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c", "\u041e\u0433\u043d\u0435\u0441\u0442\u043e\u0439\u043a\u043e\u0441\u0442\u044c");
    private final ValueSetting delay = new ValueSetting("Delay", "\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430 \u043f\u0435\u0440\u0435\u0434 \u0431\u0440\u043e\u0441\u043a\u043e\u043c").setValue(500.0f).range(100.0f, 2000.0f).step(50.0f);
    private final StopWatch throwTimer = new StopWatch();
    private boolean shouldLookDown;

    public AutoPotion() {
        super("AutoPotion", "Auto Potion", ModuleCategory.COMBAT);
        this.setup(this.potions, this.delay);
    }

    @Override
    public void deactivate() {
        this.shouldLookDown = false;
        super.deactivate();
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (AutoPotion.mc.field_1724 == null || AutoPotion.mc.field_1687 == null || AutoPotion.mc.field_1761 == null) {
            this.shouldLookDown = false;
            return;
        }
        if (AutoPotion.mc.field_1755 != null || AutoPotion.mc.field_1724.method_6115()) {
            this.shouldLookDown = false;
            return;
        }
        class_1735 potionSlot = this.findEligiblePotionSlot();
        if (potionSlot == null) {
            this.shouldLookDown = false;
            return;
        }
        this.shouldLookDown = true;
        if (RotationController.INSTANCE.getServerAngle().getPitch() < 88.0f) {
            return;
        }
        if (!this.throwTimer.finished(this.delay.getValue()) || PlayerInventoryComponent.isInventoryActionBusy()) {
            return;
        }
        ScreenWalkInventorySync.swapUseRestore(potionSlot, () -> {
            PlayerIntersectionUtil.interactItem(class_1268.field_5808);
            if (AutoPotion.mc.field_1724 != null) {
                AutoPotion.mc.field_1724.method_6104(class_1268.field_5808);
            }
        });
        this.throwTimer.reset();
    }

    @EventHandler
    public void onRotationUpdate(RotationUpdateEvent e) {
        if (e.getType() != 0 || !this.shouldLookDown) {
            return;
        }
        RotationController.INSTANCE.rotateTo(AngleUtil.pitch(90.0f), new RotationConfig(new SlothSmoothMode(), true, true), TaskPriority.HIGH_IMPORTANCE_1, this);
    }

    private class_1735 findEligiblePotionSlot() {
        if (AutoPotion.mc.field_1724 == null) {
            return null;
        }
        return PlayerInventoryUtil.slots().filter(slot -> {
            class_1799 stack = slot.method_7677();
            if (!stack.method_31574(class_1802.field_8436)) {
                return false;
            }
            List<String> eligible = this.getEligibleEffects(stack);
            return !eligible.isEmpty();
        }).findFirst().orElse(null);
    }

    private List<String> getEligibleEffects(class_1799 stack) {
        ArrayList<String> keys2 = new ArrayList<String>();
        if (AutoPotion.mc.field_1724 == null) {
            return keys2;
        }
        class_1844 contents = (class_1844)stack.method_58694(class_9334.field_49651);
        if (contents == null) {
            return keys2;
        }
        for (class_1293 instance : contents.method_57397()) {
            class_6880 type = instance.method_5579();
            String key = this.effectKey((class_6880<class_1291>)type);
            if (key == null || !this.potions.isSelected(key) || AutoPotion.mc.field_1724.method_6059(type)) continue;
            keys2.add(key);
        }
        return keys2;
    }

    private String effectKey(class_6880<class_1291> type) {
        if (type.comp_349() == class_1294.field_5910.comp_349()) {
            return "\u0421\u0438\u043b\u0430";
        }
        if (type.comp_349() == class_1294.field_5904.comp_349()) {
            return "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c";
        }
        if (type.comp_349() == class_1294.field_5918.comp_349()) {
            return "\u041e\u0433\u043d\u0435\u0441\u0442\u043e\u0439\u043a\u043e\u0441\u0442\u044c";
        }
        return null;
    }
}

