/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
 *  org.joml.Matrix4f
 */
package ru.recoin.implement.features.modules.render;

import java.awt.Color;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.joml.Matrix4f;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.ColorSetting;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.render.pipeline.FogBlurPipeline;
import ru.recoin.implement.events.player.GlobalTickEvent;
import ru.recoin.implement.events.render.FogEvent;

public class WorldTweaks
extends Module {
    private float fixedSkyFov = -1.0f;
    private int cachedSecondarySource = Integer.MIN_VALUE;
    private int cachedSecondaryPreset = -1;
    private int cachedSecondaryColor;
    public final MultiSelectSetting modeSetting = new MultiSelectSetting("World Setting", "Allows you to customize world").value("Time", "Fog", "Fog Blur", "Sky Shader");
    private final transient FogBlurPipeline fogBlur = new FogBlurPipeline();
    public final ValueSetting fogBlurStrength = new ValueSetting("Fog Blur Strength", "Distance blur radius").setValue(6.0f).range(1.0f, 16.0f).step(0.5f).visible(() -> this.modeSetting.isSelected("Fog Blur"));
    public final SelectSetting skyShader = new SelectSetting("Sky Shader", "Animated custom sky palette").value("Black Hole", "Aurora", "Stardust", "Deep Space", "Nebula").visible(() -> this.modeSetting.isSelected("Sky Shader"));
    public final SelectSetting skyColorMode = new SelectSetting("Sky Color", "Use theme or a custom shader tint").value("Theme", "Custom").selected("Theme").visible(() -> this.modeSetting.isSelected("Sky Shader"));
    public final ColorSetting skyColor = new ColorSetting("Custom Sky Color", "Custom shader tint").setColor(-8621313).visible(() -> this.modeSetting.isSelected("Sky Shader") && this.skyColorMode.isSelected("Custom"));
    public final ValueSetting starDensity = new ValueSetting("Star Density", "Number of rendered stars").setValue(1880.0f).range(220.0f, 3600.0f).step(20.0f).visible(this::usesStardustSky);
    public final ValueSetting skyBrightness = new ValueSetting("Sky Brightness", "Sky brightness").setValue(1.55f).range(0.2f, 2.75f).step(0.05f).visible(this::usesStardustSky);
    public final SelectSetting skyTime = new SelectSetting("Sky Time", "Forced sky time").value("Day", "Sunset", "Dawn", "Night", "Midnight", "Noon").selected("Night").visible(this::usesStardustSky);
    public final ValueSetting timeSetting = new ValueSetting("Time", "Sets the value of the time").setValue(12.0f).range(0, 24).visible(() -> this.modeSetting.isSelected("Time"));
    public final ValueSetting distanceSetting = new ValueSetting("Fog Distance", "Distance at which fog reaches full strength").setValue(100.0f).range(20, 200).visible(() -> this.modeSetting.isSelected("Fog") || this.modeSetting.isSelected("Fog Blur"));

    public static WorldTweaks getInstance() {
        return Instance.get(WorldTweaks.class);
    }

    public WorldTweaks() {
        super("WorldTweaks", "World Tweaks", ModuleCategory.RENDER);
        this.setup(this.modeSetting, this.timeSetting, this.distanceSetting, this.fogBlurStrength, this.skyShader, this.skyColorMode, this.skyColor, this.starDensity, this.skyBrightness, this.skyTime);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> this.fogBlur.close());
    }

    @EventHandler
    public void onFog(FogEvent e) {
        if (this.customFogEnabled()) {
            e.setDistance(this.distanceSetting.getValue());
            e.setColor(ColorUtil.getClientColor());
            e.cancel();
        }
    }

    public boolean customSkyEnabled() {
        return this.isState() && this.modeSetting.isSelected("Sky Shader");
    }

    public int skyShaderMode() {
        if (this.skyShader.isSelected("Aurora")) {
            return 1;
        }
        if (this.skyShader.isSelected("Deep Space")) {
            return 3;
        }
        return 0;
    }

    public boolean customFogEnabled() {
        return this.isState() && this.modeSetting.isSelected("Fog") && !this.modeSetting.isSelected("Fog Blur");
    }

    public boolean fogBlurEnabled() {
        return this.isState() && this.modeSetting.isSelected("Fog Blur");
    }

    public void renderFogBlur(Matrix4f projection) {
        if (this.fogBlurEnabled() && WorldTweaks.mc.field_1687 != null && WorldTweaks.mc.field_1724 != null && !WorldTweaks.mc.field_1773.method_35765()) {
            this.fogBlur.draw(projection, this.distanceSetting.getValue(), this.fogBlurStrength.getValue());
        } else {
            this.fogBlur.close();
        }
    }

    @EventHandler
    public void onGlobalTick(GlobalTickEvent event) {
        if (WorldTweaks.mc.field_1687 == null || !this.fogBlurEnabled()) {
            this.fogBlur.close();
        }
    }

    public int fogColor() {
        return ColorUtil.getClientColor();
    }

    public int shaderColor() {
        return this.skyColorMode.isSelected("Custom") ? this.skyColor.getColor() : ColorUtil.getClientColor();
    }

    public float skyProjectionFov() {
        return WorldTweaks.mc.field_1690 != null ? (float)((Integer)WorldTweaks.mc.field_1690.method_41808().method_41753()).intValue() : 70.0f;
    }

    public boolean usesStardustSky() {
        return this.customSkyEnabled() && (this.skyShader.isSelected("Aurora") || this.skyShader.isSelected("Stardust"));
    }

    public boolean usesNebulaSky() {
        return this.customSkyEnabled() && this.skyShader.isSelected("Nebula");
    }

    public int stardustPreset() {
        return this.skyShader.isSelected("Stardust") ? 1 : 0;
    }

    public float stardustIntensity() {
        return Math.min(this.skyBrightness.getValue() * 0.74f, 1.65f);
    }

    public float stardustTime() {
        float ticks = switch (this.skyTime.getSelected()) {
            case "Day" -> 1000.0f;
            case "Sunset" -> 12000.0f;
            case "Dawn" -> 23000.0f;
            case "Midnight" -> 18000.0f;
            case "Noon" -> 6000.0f;
            default -> 14000.0f;
        };
        return ticks / 24000.0f;
    }

    public int stardustPrimaryColor() {
        return this.shaderColor() & 0xFFFFFF;
    }

    public int stardustSecondaryColor() {
        int rgb = this.stardustPrimaryColor();
        int preset = this.stardustPreset();
        if (rgb == this.cachedSecondarySource && preset == this.cachedSecondaryPreset) {
            return this.cachedSecondaryColor;
        }
        float[] hsb = Color.RGBtoHSB(rgb >>> 16 & 0xFF, rgb >>> 8 & 0xFF, rgb & 0xFF, null);
        float shift = preset == 1 ? 0.34f : 0.32f;
        float saturation = preset == 1 ? 0.68f : 0.74f;
        float value = preset == 1 ? 1.08f : 0.92f;
        this.cachedSecondarySource = rgb;
        this.cachedSecondaryPreset = preset;
        this.cachedSecondaryColor = Color.HSBtoRGB(hsb[0] + shift - (float)Math.floor(hsb[0] + shift), Math.min(1.0f, saturation + hsb[1] * 0.2f), Math.min(1.0f, value + hsb[2] * 0.1f)) & 0xFFFFFF;
        return this.cachedSecondaryColor;
    }

    @Override
    public void activate() {
        this.fixedSkyFov = WorldTweaks.mc.field_1690 != null ? (float)((Integer)WorldTweaks.mc.field_1690.method_41808().method_41753()).intValue() : 70.0f;
    }

    @Override
    public void deactivate() {
        this.fogBlur.close();
        this.fixedSkyFov = -1.0f;
    }
}

