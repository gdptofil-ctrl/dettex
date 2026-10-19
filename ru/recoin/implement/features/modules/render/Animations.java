/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_3532
 */
package ru.recoin.implement.features.modules.render;

import net.minecraft.class_3532;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.modules.render.UiAnimationManager;

public final class Animations
extends Module {
    public final MultiSelectSetting animate = new MultiSelectSetting("Animate", "Interface parts to animate").value("Tab", "Inventory", "Chests", "Buttons");
    public final ValueSetting tabSpeed = this.speed("Tab Speed", "Tab");
    public final ValueSetting inventorySpeed = this.speed("Inventory Speed", "Inventory");
    public final ValueSetting chestSpeed = this.speed("Chest Speed", "Chests");
    public final ValueSetting buttonSpeed = this.speed("Button Speed", "Buttons");

    public Animations() {
        super("Animations", ModuleCategory.RENDER);
        this.setup(this.animate, this.tabSpeed, this.inventorySpeed, this.chestSpeed, this.buttonSpeed);
    }

    public static Animations getInstance() {
        return Instance.get(Animations.class);
    }

    private ValueSetting speed(String name, String feature) {
        return new ValueSetting(name, feature + " animation multiplier").setValue(1.0f).range(0.1f, 3.0f).step(0.1f).visible(() -> this.animate.isSelected(feature));
    }

    public boolean enabled(String feature) {
        return this.isState() && this.animate.isSelected(feature);
    }

    public long duration(long baseMs, ValueSetting speed) {
        float value = speed == null || !Float.isFinite(speed.getValue()) || speed.getValue() <= 0.0f ? 1.0f : speed.getValue();
        return Math.max(1L, (long)Math.round((float)baseMs / value));
    }

    public float ease(float raw) {
        float t = class_3532.method_15363((float)raw, (float)0.0f, (float)1.0f);
        return this.easeOutBack(t);
    }

    public float easeClosing(float raw) {
        float t = class_3532.method_15363((float)raw, (float)0.0f, (float)1.0f);
        return 1.0f - (1.0f - t) * (1.0f - t);
    }

    private float easeOutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1.0f;
        float x = t - 1.0f;
        return 1.0f + c3 * x * x * x + c1 * x * x;
    }

    @Override
    public void activate() {
        UiAnimationManager.initialize();
    }

    @Override
    public void deactivate() {
        UiAnimationManager.reset();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        UiAnimationManager.tick();
    }
}

