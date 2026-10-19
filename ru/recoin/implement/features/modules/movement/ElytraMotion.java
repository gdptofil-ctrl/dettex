/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_243
 */
package ru.recoin.implement.features.modules.movement;

import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.Setting;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.entity.MovingUtil;
import ru.recoin.implement.events.player.GlidingVelocityEvent;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.movement.ElytraTarget;

public class ElytraMotion
extends Module {
    private final ValueSetting distance = new ValueSetting("\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f", "\u041e\u0441\u0442\u0430\u043d\u0430\u0432\u043b\u0438\u0432\u0430\u0435\u0442 \u043f\u043e\u043b\u0451\u0442 \u0440\u044f\u0434\u043e\u043c \u0441 \u0446\u0435\u043b\u044c\u044e").setValue(3.0f).range(1.0f, 5.0f).step(0.1f);
    private final BooleanSetting restoreSpeed = new BooleanSetting("\u0412\u043e\u0437\u0432\u0440\u0430\u0449\u0430\u0442\u044c \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c", "").setValue(true);
    private transient class_243 savedVelocity;

    public ElytraMotion() {
        super("ElytraMotion", ModuleCategory.MOVEMENT);
        this.setup(this.distance, this.restoreSpeed);
    }

    @EventHandler
    public void onGlidingVelocity(GlidingVelocityEvent event) {
        float threshold;
        Setting setting;
        BooleanSetting setting2;
        Setting setting3;
        boolean overtake;
        if (ElytraMotion.mc.field_1724 == null || ElytraMotion.mc.field_1687 == null) {
            this.savedVelocity = null;
            return;
        }
        Aura aura = Aura.getInstance();
        class_1309 target = aura == null ? null : aura.getTarget();
        ElytraTarget elytraTarget = ElytraTarget.getInstance();
        boolean bl = overtake = elytraTarget != null && (setting3 = elytraTarget.get("\u042d\u043b\u0438\u0442\u0440\u0430 \u043f\u0435\u0440\u0435\u0433\u043e\u043d")) instanceof BooleanSetting && (setting2 = (BooleanSetting)setting3).isValue();
        if (elytraTarget != null && (setting = elytraTarget.get("\u041f\u0440\u0435\u0434\u0438\u043a\u0442\u0438\u0442\u044c \u043e\u0442 \u0431\u043f\u0441")) instanceof ValueSetting) {
            ValueSetting setting4 = (ValueSetting)setting;
            threshold = setting4.getValue();
        } else {
            threshold = 15.0f;
        }
        if (target != null && ElytraMotion.mc.field_1724.method_6128() && ElytraMotion.mc.field_1724.method_33571().method_1022(target.method_5829().method_1005().method_1031(0.0, (target.method_23318() - target.field_6036) * 2.0, 0.0)) < (double)this.distance.getValue() && (MovingUtil.getSpeedSqrt((class_1297)target) * 20.0 < (double)threshold || !overtake)) {
            if (this.restoreSpeed.isValue() && this.savedVelocity == null) {
                this.savedVelocity = event.getVelocity();
            }
            event.setVelocity(class_243.field_1353);
        } else if (this.restoreSpeed.isValue() && this.savedVelocity != null) {
            event.setVelocity(this.savedVelocity);
            this.savedVelocity = null;
        }
    }

    @Override
    public void deactivate() {
        this.savedVelocity = null;
        super.deactivate();
    }
}

