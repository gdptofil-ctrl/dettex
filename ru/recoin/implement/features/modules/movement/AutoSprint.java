/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1294
 */
package ru.recoin.implement.features.modules.movement;

import net.minecraft.class_1294;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.other.Instance;
import ru.recoin.core.Main;
import ru.recoin.implement.events.player.KeepSprintEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.combat.killaura.attack.AttackHandler;
import ru.recoin.implement.features.modules.misc.ClickAction;

public class AutoSprint
extends Module {
    public int tickStop = 0;
    private final BooleanSetting keepSprintSetting = new BooleanSetting("Keep Sprint", "Keep sprint before impact, thus not slowing you down").setValue(false);
    private final ValueSetting keepSprintStrength = new ValueSetting("Keep Sprint Strength", "Strength of velocity preservation after attack").setValue(0.6f).range(0.2f, 1.0f).step(0.1f).visible(this.keepSprintSetting::isValue);
    public final BooleanSetting ignoreHungerSetting = new BooleanSetting("Ignore Hunger", "Keep sprint before impact, thus not slowing you down").setValue(false);
    private final BooleanSetting ignoreBlindness = new BooleanSetting("Ignore Blindness", "Keep sprinting while affected by blindness").setValue(false);

    public static AutoSprint getInstance() {
        return Instance.get(AutoSprint.class);
    }

    public AutoSprint() {
        super("AutoSprint", "Auto Sprint", ModuleCategory.MOVEMENT);
        this.setup(this.keepSprintSetting, this.keepSprintStrength, this.ignoreHungerSetting, this.ignoreBlindness);
    }

    @EventHandler
    public void onTick(TickEvent e) {
        AttackHandler attackHandler;
        boolean horizontal;
        if (AutoSprint.mc.field_1724 == null) {
            return;
        }
        ClickAction clickAction = ClickAction.getInstance();
        if (clickAction != null && clickAction.isSpookyWindChargeMovementSuppressed()) {
            AutoSprint.mc.field_1724.method_5728(false);
            return;
        }
        if (AutoSprint.mc.field_1724.method_6059(class_1294.field_5919) && !this.ignoreBlindness.isValue()) {
            AutoSprint.mc.field_1724.method_5728(false);
            AutoSprint.mc.field_1690.field_1867.method_23481(false);
            return;
        }
        boolean bl = horizontal = AutoSprint.mc.field_1724.field_5976 && !AutoSprint.mc.field_1724.field_34927;
        if (Main.getInstance().getAttackPerpetrator().getAttackHandler().isFastSprintResetActive()) {
            AutoSprint.mc.field_1724.method_5728(false);
            return;
        }
        if (this.tickStop != 0) {
            AutoSprint.mc.field_1724.method_5728(false);
            AutoSprint.mc.field_1690.field_1867.method_23481(false);
            --this.tickStop;
            return;
        }
        Aura aura = Aura.getInstance();
        if (aura.isState() && aura.getTarget() != null && aura.getConfig().isSprintReset() && (attackHandler = Main.getInstance().getAttackPerpetrator().getAttackHandler()).isWithinAttackRange(aura.getConfig()) && (attackHandler.shouldPauseSprint(aura.getConfig()) || attackHandler.isAdaptiveAttackPending() || attackHandler.isPacketSprintResetActive())) {
            AutoSprint.mc.field_1724.method_5728(false);
            AutoSprint.mc.field_1690.field_1867.method_23481(false);
            return;
        }
        if (!AutoSprint.mc.field_1724.method_5715() && !horizontal && AutoSprint.mc.field_1690.field_1894.method_1434()) {
            AutoSprint.mc.field_1724.method_5728(true);
            AutoSprint.mc.field_1690.field_1867.method_23481(true);
        }
    }

    @EventHandler
    public void onKeepSprint(KeepSprintEvent e) {
        if (Main.getInstance().getAttackPerpetrator().getAttackHandler().isFastSprintResetActive()) {
            return;
        }
        if (this.keepSprintSetting.isValue() && (!AutoSprint.mc.field_1724.method_6059(class_1294.field_5919) || this.ignoreBlindness.isValue())) {
            float strength = this.keepSprintStrength.getValue();
            AutoSprint.mc.field_1724.method_18800(AutoSprint.mc.field_1724.method_18798().field_1352 / (double)strength, AutoSprint.mc.field_1724.method_18798().field_1351, AutoSprint.mc.field_1724.method_18798().field_1350 / (double)strength);
            AutoSprint.mc.field_1724.method_5728(true);
        }
    }

    @Override
    public void deactivate() {
        if (AutoSprint.mc.field_1724 != null) {
            AutoSprint.mc.field_1724.method_5728(false);
        }
        super.deactivate();
    }
}

