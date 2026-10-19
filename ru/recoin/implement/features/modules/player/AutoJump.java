/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.player;

import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.modules.combat.Aura;

public class AutoJump
extends Module {
    private boolean autoPressed;
    private final BooleanSetting auraOnly = new BooleanSetting("Only Attack Aura", "Jump only while Attack Aura has a target").setValue(false);

    public AutoJump() {
        super("AutoJump", "Auto Jump", ModuleCategory.MOVEMENT);
        this.setup(this.auraOnly);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        boolean press;
        if (AutoJump.mc.field_1724 == null || AutoJump.mc.field_1755 != null) {
            this.release();
            return;
        }
        Aura aura = Aura.getInstance();
        if (this.auraOnly.isValue() && (aura == null || !aura.isState() || aura.getTarget() == null)) {
            this.release();
            return;
        }
        boolean bl = press = AutoJump.mc.field_1724.method_24828() && !AutoJump.mc.field_1724.method_5799() && !AutoJump.mc.field_1724.method_6101() && !AutoJump.mc.field_1724.method_6128();
        if (press && !this.autoPressed) {
            AutoJump.mc.field_1690.field_1903.method_23481(true);
            this.autoPressed = true;
        } else if (!press) {
            this.release();
        }
    }

    private void release() {
        if (this.autoPressed) {
            AutoJump.mc.field_1690.field_1903.method_23481(false);
        }
        this.autoPressed = false;
    }

    @Override
    public void deactivate() {
        this.release();
    }
}

