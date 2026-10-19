/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1309
 *  net.minecraft.class_239
 *  net.minecraft.class_3966
 */
package ru.recoin.implement.features.modules.combat;

import net.minecraft.class_1309;
import net.minecraft.class_239;
import net.minecraft.class_3966;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.player.AttackEvent;
import ru.recoin.implement.events.player.TickEvent;

public final class ShiftTap
extends Module {
    private boolean tapping;
    private int tapTicks;
    private boolean attackKeyDown;

    public ShiftTap() {
        super("ShiftTap", "Shift Tap", ModuleCategory.COMBAT);
    }

    public static ShiftTap getInstance() {
        return Instance.get(ShiftTap.class);
    }

    public boolean isTapping() {
        return this.isState() && this.tapping;
    }

    public void triggerTap() {
        if (!this.isState() || ShiftTap.mc.field_1724 == null) {
            return;
        }
        this.tapping = true;
        this.tapTicks = 2;
    }

    @EventHandler
    public void onAttack(AttackEvent event) {
        if (!event.isPre() || ShiftTap.mc.field_1724 == null) {
            return;
        }
        this.triggerTap();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        class_3966 hit;
        class_239 class_2392;
        if (ShiftTap.mc.field_1724 == null) {
            this.tapping = false;
            this.attackKeyDown = false;
            return;
        }
        boolean attackPressed = ShiftTap.mc.field_1690.field_1886.method_1434();
        if (attackPressed && !this.attackKeyDown && (class_2392 = ShiftTap.mc.field_1765) instanceof class_3966 && (hit = (class_3966)class_2392).method_17782() instanceof class_1309) {
            this.triggerTap();
        }
        this.attackKeyDown = attackPressed;
        if (this.tapping && --this.tapTicks <= 0) {
            this.tapping = false;
        }
    }

    @Override
    public void deactivate() {
        this.tapping = false;
        this.tapTicks = 0;
        this.attackKeyDown = false;
        super.deactivate();
    }
}

