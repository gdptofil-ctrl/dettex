/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1764
 *  net.minecraft.class_1839
 *  net.minecraft.class_243
 *  net.minecraft.class_2596
 *  net.minecraft.class_2886
 */
package ru.recoin.implement.features.modules.movement;

import net.minecraft.class_1268;
import net.minecraft.class_1764;
import net.minecraft.class_1839;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2886;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.entity.MovingUtil;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.entity.PlayerInventoryComponent;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.other.StopWatch;
import ru.recoin.common.util.task.scripts.Script;
import ru.recoin.implement.events.item.ItemSlowdownEvent;
import ru.recoin.implement.events.item.UsingItemEvent;
import ru.recoin.implement.events.player.TickEvent;

public class NoSlow
extends Module {
    private final StopWatch notifWatch = new StopWatch();
    private final Script script = new Script();
    private boolean finish;
    private int ticks = 0;
    private int cycleCounter = 0;
    private final transient StopWatch grimTicksWatch = new StopWatch();
    private transient boolean grimTicksCancel;
    public final SelectSetting itemMode = new SelectSetting("Item Mode", "\u0420\u0435\u0436\u0438\u043c \u043e\u0431\u0445\u043e\u0434\u0430 \u0434\u043b\u044f \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432").value("Vanilla", "Grim New", "Grim Old", "FunTime", "ReallyWorld", "BravoHvH", "Grim Ticks");
    private final ValueSetting bravoSpeed = new ValueSetting("Bravo Speed", "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0434\u0432\u0438\u0436\u0435\u043d\u0438\u044f \u043f\u0440\u0438 \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0438").setValue(0.6f).range(0.1f, 1.0f).step(0.05f).visible(() -> this.itemMode.isSelected("BravoHvH"));

    public static NoSlow getInstance() {
        return Instance.get(NoSlow.class);
    }

    public NoSlow() {
        super("NoSlow", "No Slow", ModuleCategory.MOVEMENT);
        this.setup(this.itemMode, this.bravoSpeed);
    }

    @EventHandler
    public void onTick(TickEvent e) {
        this.grimTicksCancel = false;
        if (this.itemMode.isSelected("Grim Ticks") && NoSlow.mc.field_1724 != null && NoSlow.mc.field_1687 != null && !NoSlow.mc.field_1724.method_6128() && NoSlow.mc.field_1724.method_6115() && this.grimTicksWatch.finished(125.0)) {
            this.grimTicksCancel = true;
            PlayerIntersectionUtil.interactItemVanilla(class_1268.field_5808);
            this.grimTicksWatch.reset();
        }
        if (NoSlow.mc.field_1724 != null && NoSlow.mc.field_1724.method_6115() && !NoSlow.mc.field_1724.method_6128()) {
            ++this.ticks;
        } else {
            this.ticks = 0;
            this.cycleCounter = 0;
        }
        if (PlayerInventoryComponent.script.isFinished() && MovingUtil.hasPlayerMovement()) {
            this.script.update();
        }
    }

    @EventHandler
    public void onUsingItem(UsingItemEvent e) {
        class_1268 second;
        if (NoSlow.mc.field_1724 == null) {
            return;
        }
        if (this.isSneakInputActive()) {
            return;
        }
        class_1268 first = NoSlow.mc.field_1724.method_6058();
        class_1268 class_12682 = second = first.equals((Object)class_1268.field_5808) ? class_1268.field_5810 : class_1268.field_5808;
        if (e.getType() == 1) {
            String mode;
            switch (mode = this.itemMode.getSelected()) {
                case "Vanilla": {
                    e.cancel();
                    break;
                }
                case "Grim Old": {
                    if (!NoSlow.mc.field_1724.method_6079().method_7976().equals((Object)class_1839.field_8952) && !NoSlow.mc.field_1724.method_6047().method_7976().equals((Object)class_1839.field_8952)) break;
                    PlayerIntersectionUtil.interactItem(first);
                    PlayerIntersectionUtil.interactItem(second);
                    e.cancel();
                    break;
                }
                case "Grim New": {
                    if (NoSlow.mc.field_1724.method_6048() < 7) {
                        PlayerInventoryUtil.updateSlots();
                        PlayerInventoryUtil.closeScreen(true);
                        break;
                    }
                    e.cancel();
                    break;
                }
            }
        } else if (e.getType() == 2) {
            while (!this.script.isFinished()) {
                this.script.update();
            }
        }
    }

    @EventHandler
    public void onItemSlowdown(ItemSlowdownEvent e) {
        if (NoSlow.mc.field_1724 == null || !NoSlow.mc.field_1724.method_6115() || NoSlow.mc.field_1724.method_6128()) {
            return;
        }
        if (this.itemMode.isSelected("Grim Ticks")) {
            if (NoSlow.mc.field_1687 != null && this.grimTicksCancel) {
                e.cancel();
            }
            return;
        }
        if (this.itemMode.isSelected("ReallyWorld")) {
            int[] cycle = new int[]{2, 3, 3};
            if (this.ticks >= cycle[this.cycleCounter % cycle.length]) {
                e.cancel();
                this.ticks = 0;
                ++this.cycleCounter;
            }
            return;
        }
        if (this.itemMode.isSelected("BravoHvH")) {
            e.cancel();
            class_243 velocity = NoSlow.mc.field_1724.method_18798();
            NoSlow.mc.field_1724.method_18800(velocity.field_1352 * (double)this.bravoSpeed.getValue(), velocity.field_1351, velocity.field_1350 * (double)this.bravoSpeed.getValue());
            return;
        }
        if (!this.itemMode.isSelected("FunTime")) {
            return;
        }
        if (NoSlow.mc.field_1724.method_6030().method_7909() instanceof class_1764) {
            e.cancel();
        } else if (NoSlow.mc.field_1724.method_6058() == class_1268.field_5808 && NoSlow.mc.field_1724.method_6030().method_7976() == class_1839.field_8950 && NoSlow.mc.field_1724.method_6079().method_7909() instanceof class_1764) {
            e.cancel();
        }
    }

    public boolean isSneakInputActive() {
        return NoSlow.mc.field_1724 != null && (NoSlow.mc.field_1690.field_1832.method_1434() || NoSlow.mc.field_1724.field_3913 != null && NoSlow.mc.field_1724.field_3913.field_54155.comp_3164() || NoSlow.mc.field_1724.method_5715() && !NoSlow.mc.field_1724.method_5681());
    }

    @Override
    public void deactivate() {
        this.grimTicksCancel = false;
        super.deactivate();
    }
}

