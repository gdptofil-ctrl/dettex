/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.render;

import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.render.pipeline.ContrastPipeline;

public final class Contrast
extends Module {
    private final ValueSetting strength = new ValueSetting("Strength", "Contrast intensity").setValue(50.0f).range(0.0f, 100.0f).step(1.0f);

    public Contrast() {
        super("Contrast", "Contrast", ModuleCategory.RENDER);
        this.setup(this.strength);
    }

    public static Contrast getInstance() {
        return Instance.get(Contrast.class);
    }

    public static void render() {
        Contrast module = Contrast.getInstance();
        if (module != null && module.isState() && module.strength.getValue() > 0.0f) {
            ContrastPipeline.render(module.strength.getValue() / 100.0f);
        }
    }
}

