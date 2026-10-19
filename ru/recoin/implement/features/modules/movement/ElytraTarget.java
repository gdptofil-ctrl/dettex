/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.movement;

import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BindSetting;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.keyboard.KeyEvent;

public class ElytraTarget
extends Module {
    public final ValueSetting findRange = new ValueSetting("Find Range", "Target search range while gliding").setValue(32.0f).range(6.0f, 64.0f);
    public final ValueSetting forwardLead = new ValueSetting("Forward Lead", "Target prediction lead").setValue(3.0f).range(0.0f, 6.0f);
    private final BindSetting toggleLead = new BindSetting("Toggle Lead", "Toggles target prediction");
    public final BooleanSetting overtake = new BooleanSetting("\u042d\u043b\u0438\u0442\u0440\u0430 \u043f\u0435\u0440\u0435\u0433\u043e\u043d", "\u041e\u043f\u0435\u0440\u0435\u0436\u0430\u0442\u044c \u0431\u044b\u0441\u0442\u0440\u0443\u044e \u0446\u0435\u043b\u044c").setValue(false);
    public final ValueSetting predictionSpeedThreshold = new ValueSetting("\u041f\u0440\u0435\u0434\u0438\u043a\u0442\u0438\u0442\u044c \u043e\u0442 \u0431\u043f\u0441", "\u041c\u0438\u043d\u0438\u043c\u0430\u043b\u044c\u043d\u0430\u044f \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0446\u0435\u043b\u0438 \u0434\u043b\u044f \u043e\u043f\u0435\u0440\u0435\u0436\u0435\u043d\u0438\u044f").setValue(15.0f).range(10.0f, 30.0f).step(1.0f);
    private boolean predictionEnabled = true;

    public ElytraTarget() {
        super("Elytra Target", "Elytra Target", ModuleCategory.COMBAT);
        this.setup(this.findRange, this.forwardLead, this.toggleLead, this.overtake, this.predictionSpeedThreshold);
    }

    public static ElytraTarget getInstance() {
        return Instance.get(ElytraTarget.class);
    }

    public float getPredictionTicks() {
        return this.isState() && this.predictionEnabled ? this.forwardLead.getValue() : 0.0f;
    }

    @EventHandler
    public void onKey(KeyEvent event) {
        if (this.isState() && event.isKeyDown(this.toggleLead.getKey())) {
            this.predictionEnabled = !this.predictionEnabled;
        }
    }
}

