/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1293
 *  net.minecraft.class_1294
 *  net.minecraft.class_1944
 *  net.minecraft.class_2338
 *  net.minecraft.class_2874
 *  net.minecraft.class_765
 */
package ru.recoin.implement.features.modules.render;

import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1944;
import net.minecraft.class_2338;
import net.minecraft.class_2874;
import net.minecraft.class_765;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.player.TickEvent;

public class FullBright
extends Module {
    public final SelectSetting mode = new SelectSetting("Mode", "Fullbright mode").value("Gamma", "Night Vision").selected("Gamma");
    public final BooleanSetting dynamic = new BooleanSetting("Dynamic", "Adjust gamma to the surrounding light").visible(() -> this.mode.isSelected("Gamma"));
    public final ValueSetting brightness = new ValueSetting("Brightness", "Gamma brightness").setValue(0.5f).range(0.0f, 10.0f).step(0.1f).visible(() -> this.mode.isSelected("Gamma") && !this.dynamic.isValue());
    public final ValueSetting minBrightness = new ValueSetting("Min Brightness", "Minimum dynamic gamma").setValue(0.0f).range(0.0f, 5.0f).step(0.1f).visible(() -> this.mode.isSelected("Gamma") && this.dynamic.isValue());
    public final ValueSetting maxBrightness = new ValueSetting("Max Brightness", "Maximum dynamic gamma").setValue(10.0f).range(0.0f, 10.0f).step(0.1f).visible(() -> this.mode.isSelected("Gamma") && this.dynamic.isValue());
    private double previousGamma = Double.NaN;
    private float smoothedLight = 1.0f;
    private float animatedGamma;
    private long lastAnimationNanos;
    private boolean gammaStored;

    public FullBright() {
        super("FullBright", "Full Bright", ModuleCategory.RENDER);
        this.setup(this.mode, this.dynamic, this.brightness, this.minBrightness, this.maxBrightness);
    }

    public static FullBright getInstance() {
        return Instance.get(FullBright.class);
    }

    public boolean isGammaMode() {
        return this.mode.isSelected("Gamma");
    }

    public boolean requiresLightmapUpdate() {
        return this.isState() && this.isGammaMode() || this.gammaStored;
    }

    @Override
    public void activate() {
        this.previousGamma = (Double)FullBright.mc.field_1690.method_42473().method_41753();
        this.animatedGamma = (float)this.previousGamma;
        this.smoothedLight = 1.0f;
        this.lastAnimationNanos = System.nanoTime();
        this.gammaStored = true;
        super.activate();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (FullBright.mc.field_1724 == null) {
            return;
        }
        if (this.mode.isSelected("Night Vision")) {
            this.applyNightVision();
            return;
        }
        this.removeNightVision();
        if (FullBright.mc.field_1687 == null) {
            return;
        }
        class_2338 position = class_2338.method_49637((double)FullBright.mc.field_1724.method_23317(), (double)FullBright.mc.field_1724.method_23320(), (double)FullBright.mc.field_1724.method_23321());
        class_2874 dimension = FullBright.mc.field_1687.method_8597();
        float block = class_765.method_23284((class_2874)dimension, (int)FullBright.mc.field_1687.method_8314(class_1944.field_9282, position));
        float sky = class_765.method_23284((class_2874)dimension, (int)FullBright.mc.field_1687.method_8314(class_1944.field_9284, position));
        this.smoothedLight = this.smoothedLight * 0.88f + Math.max(block, sky) * 0.12f;
    }

    public float resolveGamma(float vanillaGamma) {
        boolean enabled;
        boolean bl = enabled = this.isState() && this.isGammaMode();
        if (!this.gammaStored && !enabled) {
            return vanillaGamma;
        }
        if (enabled && !this.gammaStored) {
            this.previousGamma = (Double)FullBright.mc.field_1690.method_42473().method_41753();
            this.animatedGamma = vanillaGamma;
            this.lastAnimationNanos = System.nanoTime();
            this.gammaStored = true;
        }
        float target = enabled ? (this.dynamic.isValue() ? this.minBrightness.getValue() + (this.maxBrightness.getValue() - this.minBrightness.getValue()) * (1.0f - this.smoothedLight) : this.brightness.getValue()) : (float)this.previousGamma;
        long now = System.nanoTime();
        float progress = Math.min(1.0f, (float)(now - this.lastAnimationNanos) / 2.5E8f);
        this.lastAnimationNanos = now;
        this.animatedGamma += (target - this.animatedGamma) * progress;
        if (!enabled && Math.abs(this.animatedGamma - target) < 0.002f) {
            this.animatedGamma = target;
            this.gammaStored = false;
        }
        return this.animatedGamma;
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.removeNightVision();
    }

    private void applyNightVision() {
        if (FullBright.mc.field_1724.method_6112(class_1294.field_5925) == null) {
            FullBright.mc.field_1724.method_6092(new class_1293(class_1294.field_5925, -1, 0, false, false, false));
        }
    }

    private void removeNightVision() {
        if (FullBright.mc.field_1724 == null) {
            return;
        }
        class_1293 effect = FullBright.mc.field_1724.method_6112(class_1294.field_5925);
        if (effect != null && effect.method_48559()) {
            FullBright.mc.field_1724.method_6016(class_1294.field_5925);
        }
    }
}

