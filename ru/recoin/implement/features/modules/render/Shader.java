package ru.recoin.implement.features.modules.render;

import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.render.pipeline.ShaderFxPipeline;

/**
 * Shader: realistic post effects without Iris. One full-screen pass:
 * bloom (glowing lava, torches, sun), sun rays, richer sky with sun glow, aerial haze, wet look in
 * rain, filmic ACES tone mapping, saturation / contrast, sharpening, vignette.
 */
public final class Shader
extends Module {
    private static final ShaderFxPipeline PIPELINE = new ShaderFxPipeline();

    private final SelectSetting preset = new SelectSetting("Preset", "Realistic, Cinematic, Vivid, Dreamy or your own sliders").value("Realistic", "Cinematic", "Vivid", "Dreamy", "Custom").selected("Realistic");
    private final ValueSetting bloom = new ValueSetting("Bloom", "Glow of bright things: lava, fire, torches, sun").setValue(0.6f).range(0.0f, 2.0f).visible(() -> this.custom());
    private final ValueSetting rays = new ValueSetting("Sun Rays", "Light rays through gaps in trees and mountains").setValue(1.0f).range(0.0f, 3.0f).visible(() -> this.custom());
    private final ValueSetting haze = new ValueSetting("Haze", "Far terrain fades into the air").setValue(1.0f).range(0.0f, 3.0f).visible(() -> this.custom());
    private final ValueSetting sky = new ValueSetting("Sky", "Deeper sky gradient and sun glow").setValue(1.0f).range(0.0f, 1.0f).visible(() -> this.custom());
    private final ValueSetting exposure = new ValueSetting("Exposure", "Brightness before tone mapping").setValue(1.1f).range(0.5f, 2.0f).visible(() -> this.custom());
    private final ValueSetting saturation = new ValueSetting("Saturation", "Colour intensity").setValue(1.1f).range(0.0f, 2.0f).visible(() -> this.custom());
    private final ValueSetting contrast = new ValueSetting("Contrast", "Difference between dark and light").setValue(1.05f).range(0.5f, 1.5f).visible(() -> this.custom());
    private final ValueSetting sharpen = new ValueSetting("Sharpen", "Crisper textures").setValue(0.3f).range(0.0f, 1.0f).visible(() -> this.custom());
    private final ValueSetting vignette = new ValueSetting("Vignette", "Darker screen edges").setValue(0.35f).range(0.0f, 1.0f).visible(() -> this.custom());
    private final ValueSetting aberration = new ValueSetting("Aberration", "Coloured fringes at the screen edges").setValue(0.0f).range(0.0f, 2.0f).visible(() -> this.custom());
    private final BooleanSetting wetInRain = new BooleanSetting("Wet In Rain", "Darker, wet-looking world while it rains").setValue(true);

    private final Matrix4f projectionMatrix = new Matrix4f();
    private final Matrix4f inverseProjection = new Matrix4f();
    private final Matrix4f cameraRotation = new Matrix4f();
    private final Vector3f cameraPosition = new Vector3f();
    private boolean projectionValid;
    private float elapsed;
    private long lastFrameNanos;

    public Shader() {
        super("Shader", "Shader", ModuleCategory.RENDER);
        this.setup(this.preset, this.bloom, this.rays, this.haze, this.sky, this.exposure, this.saturation, this.contrast, this.sharpen, this.vignette, this.aberration, this.wetInRain);
    }

    private boolean custom() {
        return this.preset.isSelected("Custom");
    }

    /** bloom, rays, haze, sky, exposure, saturation, contrast, sharpen, vignette, aberration */
    private float[] values() {
        return switch (this.preset.getSelected()) {
            case "Cinematic" -> new float[]{0.7f, 1.4f, 1.4f, 0.8f, 1.0f, 0.95f, 1.12f, 0.2f, 0.55f, 0.6f};
            case "Vivid" -> new float[]{0.5f, 0.8f, 0.6f, 1.0f, 1.15f, 1.35f, 1.1f, 0.45f, 0.25f, 0.0f};
            case "Dreamy" -> new float[]{1.4f, 1.8f, 1.6f, 1.0f, 1.2f, 1.05f, 0.92f, 0.0f, 0.4f, 0.3f};
            case "Custom" -> new float[]{this.bloom.getValue(), this.rays.getValue(), this.haze.getValue(), this.sky.getValue(), this.exposure.getValue(), this.saturation.getValue(), this.contrast.getValue(), this.sharpen.getValue(), this.vignette.getValue(), this.aberration.getValue()};
            default -> new float[]{0.6f, 1.0f, 1.0f, 1.0f, 1.1f, 1.1f, 1.05f, 0.3f, 0.35f, 0.0f};   // Realistic
        };
    }

    @Override
    public void activate() {
        this.projectionValid = false;
        this.lastFrameNanos = 0L;
    }

    public static boolean active() {
        try {
            Shader m = Instance.get(Shader.class);
            return m != null && m.isState();
        } catch (Throwable ignored) {
            return false;
        }
    }

    /** Called after the world is drawn (before the hand and the HUD). */
    public static void render(Matrix4f projection) {
        Shader m;
        try {
            m = Instance.get(Shader.class);
        } catch (Throwable ignored) {
            return;
        }
        class_310 mc = class_310.method_1551();
        if (m == null || !m.isState() || projection == null || mc.field_1687 == null || mc.field_1773 == null) {
            return;
        }
        class_4184 camera = mc.field_1773.method_19418();
        if (camera == null) {
            return;
        }
        long now = System.nanoTime();
        if (m.lastFrameNanos != 0L) {
            m.elapsed = (m.elapsed + Math.min(0.1f, (now - m.lastFrameNanos) / 1.0e9f)) % 100000.0f;
        }
        m.lastFrameNanos = now;
        if (!m.projectionValid || !m.projectionMatrix.equals(projection)) {
            m.projectionMatrix.set((Matrix4fc)projection);
            m.inverseProjection.set((Matrix4fc)projection).invert();
            m.projectionValid = true;
        }
        class_243 position = camera.method_71156();
        m.cameraRotation.rotation((Quaternionfc)camera.method_23767());
        m.cameraPosition.set((float)position.field_1352, (float)position.field_1351, (float)position.field_1350);
        // sun direction from the real time of day (same curve as vanilla)
        double fraction = mc.field_1687.method_8532() % 24000L / 24000.0 - 0.25;
        fraction -= Math.floor(fraction);
        double eased = 0.5 - Math.cos(fraction * Math.PI) / 2.0;
        double radians = (fraction * 2.0 + eased) / 3.0 * Math.PI * 2.0;
        float sunX = (float)(-Math.sin(radians));
        float sunY = (float)Math.cos(radians);
        float wet = m.wetInRain.isValue() ? mc.field_1687.method_8430(1.0f) : 0.0f;
        float[] v = m.values();
        int presetIndex = switch (m.preset.getSelected()) {
            case "Cinematic" -> 1;
            case "Vivid" -> 2;
            case "Dreamy" -> 3;
            case "Custom" -> 4;
            default -> 0;
        };
        int tint = switch (presetIndex) {
            case 1 -> 0xFFF2F0FF;   // slightly cool
            case 3 -> 0xFFFFF0F6;   // soft pink
            default -> 0xFFFFFFFF;
        };
        PIPELINE.draw(projection, m.inverseProjection, m.cameraRotation, m.cameraPosition, m.elapsed, presetIndex, v[0], v[4], v[8], v[7], v[5], v[6], v[1], wet, v[2], v[9], tint, v[3], sunX, sunY);
    }
}
