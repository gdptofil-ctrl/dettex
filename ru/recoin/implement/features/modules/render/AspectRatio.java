/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.render;

import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.implement.events.render.AspectRatioEvent;

public class AspectRatio
extends Module {
    private final SelectSetting ratio = new SelectSetting("Ratio", "Screen aspect ratio").value("16:9", "4:3", "1:1", "16:10", "21:9", "32:9", "5:4", "2:1", "Custom").selected("16:9");
    private final ValueSetting customRatio = new ValueSetting("Custom Ratio", "Custom screen aspect ratio").setValue(2.0f).range(1.0f, 4.0f).step(0.05f).visible(() -> this.ratio.isSelected("Custom"));

    public AspectRatio() {
        super("AspectRatio", "AspectRatio", ModuleCategory.RENDER);
        this.setup(this.ratio, this.customRatio);
    }

    public float getRatio() {
        return switch (this.ratio.getSelected()) {
            case "4:3" -> 1.3333334f;
            case "1:1" -> 1.0f;
            case "16:10" -> 1.6f;
            case "21:9" -> 2.3333333f;
            case "32:9" -> 3.5555556f;
            case "5:4" -> 1.25f;
            case "2:1" -> 2.0f;
            case "Custom" -> this.customRatio.getValue();
            default -> 1.7777778f;
        };
    }

    @EventHandler
    public void onAspectRatio(AspectRatioEvent event) {
        event.setRatio(this.getRatio());
        event.cancel();
    }
}

