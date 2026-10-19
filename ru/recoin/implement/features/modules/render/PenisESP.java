/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.render;

import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.ColorSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.render.PenisESPFeatureRenderer;
import ru.recoin.implement.events.render.WorldRenderEvent;

public final class PenisESP
extends Module {
    private final ValueSetting length = new ValueSetting("Length", "Length of the cosmetic model").setValue(0.4f).range(0.15f, 0.8f).step(0.01f);
    private final ValueSetting radius = new ValueSetting("Radius", "Thickness of the cosmetic model").setValue(0.075f).range(0.04f, 0.14f).step(0.005f);
    private final ColorSetting color = new ColorSetting("Color", "Color of the cosmetic model").value(-2052716);

    public PenisESP() {
        super("PenisESP", "Penis ESP", ModuleCategory.RENDER);
        this.setup(this.length, this.radius, this.color);
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (PenisESP.mc.field_1724 == null || PenisESP.mc.field_1687 == null || PenisESP.mc.field_1773 == null || PenisESP.mc.field_1724.method_7325() || mc.method_1560() != PenisESP.mc.field_1724 || !PenisESP.mc.field_1690.method_31044().method_31034()) {
            return;
        }
        PenisESPFeatureRenderer.renderFirstPerson(event, this);
    }

    public ValueSetting getLength() {
        return this.length;
    }

    public ValueSetting getRadius() {
        return this.radius;
    }

    public ColorSetting getColor() {
        return this.color;
    }
}

