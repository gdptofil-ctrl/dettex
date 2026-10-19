/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_276
 *  net.minecraft.class_6367
 */
package ru.recoin.implement.features.modules.render;

import net.minecraft.class_276;
import net.minecraft.class_6367;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.ColorSetting;
import ru.recoin.api.feature.module.setting.implement.GroupSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.logger.LoggerUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.render.LegacyRenderSystem;
import ru.recoin.common.util.render.shader.HandEffectRenderer;
import ru.recoin.common.util.render.shader.Shader;
import ru.recoin.common.util.render.shader.ShaderHelper;
import ru.recoin.compat.handglass.shader.GlassHandsRenderer;

public class HandsShader
extends Module {
    private static final long SHADER_TIME_WRAP_NANOS = 180000000000L;
    private final BooleanSetting dualShaderMode = new BooleanSetting("Dual Shader Mode", "Enable dual shader mode to combine two effects").setValue(false);
    private final SelectSetting mainShader = new SelectSetting("Main Shader", "Primary hand shader effect").value("None", "Solid", "Chroma", "Balatro", "Smoke", "Glow", "Aura", "Glass", "Camouflage").selected("Glass");
    private final SelectSetting shader1 = new SelectSetting("Shader 1", "First shader effect").value("None", "Solid", "Chroma", "Balatro", "Smoke", "Glow", "Aura", "Glass", "Camouflage").selected("Glow").visible(this.dualShaderMode::isValue);
    private final SelectSetting shader2 = new SelectSetting("Shader 2", "Second shader effect").value("None", "Solid", "Chroma", "Balatro", "Smoke", "Glow", "Aura", "Glass", "Camouflage").selected("Smoke").visible(this.dualShaderMode::isValue);
    private final SelectSetting blendMode = new SelectSetting("Blend Mode", "How to combine the two shaders").value("Mix", "Add", "Multiply", "Screen", "Overlay", "Difference").selected("Add").visible(this.dualShaderMode::isValue);
    private final ValueSetting blendIntensity = new ValueSetting("Blend Intensity", "Intensity of blending between shaders (0=shader1, 1=shader2)").setValue(1.0f).range(0.0f, 1.0f).visible(this.dualShaderMode::isValue);
    private final ValueSetting shader1Strength = new ValueSetting("Shader 1 Strength", "Strength/opacity of first shader").setValue(1.0f).range(0.0f, 1.0f).visible(this.dualShaderMode::isValue);
    private final ValueSetting shader2Strength = new ValueSetting("Shader 2 Strength", "Strength/opacity of second shader").setValue(1.0f).range(0.0f, 1.0f).visible(this.dualShaderMode::isValue);
    private final ColorSetting solidColor1 = new ColorSetting("Color 1", "First color for gradient").value(-1499549).visible(() -> this.mainShader.isSelected("Solid") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Solid") || this.shader2.isSelected("Solid")));
    private final ColorSetting solidColor2 = new ColorSetting("Color 2", "Second color for gradient").value(-6596170).visible(() -> this.mainShader.isSelected("Solid") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Solid") || this.shader2.isSelected("Solid")));
    private final ValueSetting gradientSpeed = new ValueSetting("Gradient Speed", "Speed of gradient animation").setValue(0.5f).range(0.1f, 1.0f).visible(() -> this.mainShader.isSelected("Solid") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Solid") || this.shader2.isSelected("Solid")));
    private final ValueSetting chromaSpeed = new ValueSetting("Chroma Speed", "Speed of chroma color animation").setValue(0.3f).range(0.05f, 1.0f).visible(() -> this.mainShader.isSelected("Chroma") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Chroma") || this.shader2.isSelected("Chroma")));
    private final ValueSetting chromaSaturation = new ValueSetting("Chroma Saturation", "Saturation of chroma colors").setValue(0.5f).range(0.1f, 1.0f).visible(() -> this.mainShader.isSelected("Chroma") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Chroma") || this.shader2.isSelected("Chroma")));
    private final ValueSetting chromaBrightness = new ValueSetting("Chroma Brightness", "Brightness of chroma effect").setValue(1.0f).range(0.5f, 1.5f).visible(() -> this.mainShader.isSelected("Chroma") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Chroma") || this.shader2.isSelected("Chroma")));
    private final ValueSetting balatrospeed = new ValueSetting("Balatro Speed", "Speed of Balatro effect animation").setValue(0.5f).range(0.1f, 2.0f).visible(() -> this.mainShader.isSelected("Balatro") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Balatro") || this.shader2.isSelected("Balatro")));
    private final ColorSetting balatroMainColor = new ColorSetting("Main Color", "Main color of the effect").value(-2210757).visible(() -> this.mainShader.isSelected("Balatro") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Balatro") || this.shader2.isSelected("Balatro")));
    private final ColorSetting balatroColor2 = new ColorSetting("Color 2", "Secondary color").value(-16749644).visible(() -> this.mainShader.isSelected("Balatro") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Balatro") || this.shader2.isSelected("Balatro")));
    private final ColorSetting balatroColor3 = new ColorSetting("Color 3", "Tertiary color").value(-15326427).visible(() -> this.mainShader.isSelected("Balatro") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Balatro") || this.shader2.isSelected("Balatro")));
    private final ColorSetting balatroColor4 = new ColorSetting("Effect Parameters", "R=Contrast (0-1), G=Lighting (0-1)").value(-10918400).visible(() -> this.mainShader.isSelected("Balatro") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Balatro") || this.shader2.isSelected("Balatro")));
    private final ColorSetting smokeColor = new ColorSetting("Smoke Color", "Color of the smoke effect").value(-6969946).visible(() -> this.mainShader.isSelected("Smoke") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Smoke") || this.shader2.isSelected("Smoke")));
    private final ValueSetting smokeIntensity = new ValueSetting("Smoke Intensity", "Intensity of the smoke pattern").setValue(1.0f).range(0.1f, 3.0f).visible(() -> this.mainShader.isSelected("Smoke") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Smoke") || this.shader2.isSelected("Smoke")));
    private final ValueSetting smokeSpeed = new ValueSetting("Smoke Speed", "Speed of smoke animation").setValue(0.5f).range(0.1f, 2.0f).visible(() -> this.mainShader.isSelected("Smoke") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Smoke") || this.shader2.isSelected("Smoke")));
    private final ColorSetting stripesColor1 = new ColorSetting("Stripe Color 1", "First stripe color").value(-8943479).visible(() -> this.mainShader.isSelected("Stripes") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Stripes") || this.shader2.isSelected("Stripes")));
    private final ColorSetting stripesColor2 = new ColorSetting("Stripe Color 2", "Second stripe color").value(-10853018).visible(() -> this.mainShader.isSelected("Stripes") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Stripes") || this.shader2.isSelected("Stripes")));
    private final ValueSetting stripesWidth = new ValueSetting("Stripe Width", "Width of diagonal stripes").setValue(0.3f).range(0.05f, 0.5f).visible(() -> this.mainShader.isSelected("Stripes") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Stripes") || this.shader2.isSelected("Stripes")));
    private final ValueSetting stripesSpeed = new ValueSetting("Stripe Speed", "Speed of stripe animation").setValue(0.3f).range(0.1f, 2.0f).visible(() -> this.mainShader.isSelected("Stripes") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Stripes") || this.shader2.isSelected("Stripes")));
    private final ColorSetting snowColor = new ColorSetting("Snow Color", "Color of the snow particles").value(-1).visible(() -> this.mainShader.isSelected("Snow") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Snow") || this.shader2.isSelected("Snow")));
    private final ValueSetting snowIntensity = new ValueSetting("Snow Intensity", "Intensity of the snow effect").setValue(1.0f).range(0.1f, 3.0f).visible(() -> this.mainShader.isSelected("Snow") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Snow") || this.shader2.isSelected("Snow")));
    private final ValueSetting snowSpeed = new ValueSetting("Snow Speed", "Speed of falling snow").setValue(1.0f).range(0.1f, 2.0f).visible(() -> this.mainShader.isSelected("Snow") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Snow") || this.shader2.isSelected("Snow")));
    private final BooleanSetting fillGlow = new BooleanSetting("Fill", "Fill hands with glow color or outline only").setValue(true).visible(this::glowParametersVisible);
    private final SelectSetting glowMode = new SelectSetting("Glow Mode", "Color animation used by the hand glow").value("Solid", "Chroma", "Balatro").selected("Solid").visible(this::standaloneGlowSelected);
    private final BooleanSetting effectGlow = new BooleanSetting("Glow", "ShaderESP-style glow around Solid, Chroma or Balatro").setValue(false).visible(this::optionalGlowSelected);
    private final ValueSetting glowRadius = new ValueSetting("Glow Radius", "Glow radius/intensity").setValue(0.04f).range(0.01f, 0.2f).visible(this::glowParametersVisible);
    private final ValueSetting glowPower = new ValueSetting("Glow Power", "Glow power on edges").setValue(0.75f).range(0.1f, 2.0f).visible(this::glowParametersVisible);
    private final ColorSetting glowColor = new ColorSetting("Glow Color", "Glow effect color").value(-13330213).visible(this::glowParametersVisible);
    private final ValueSetting glowDispersion = new ValueSetting("Dispersion", "Light dispersion size from hands").setValue(5.0f).range(1.0f, 10.0f).visible(this::glowParametersVisible);
    private final ValueSetting glassBlurSize = new ValueSetting("Blur Size", "Size of the glass blur effect").setValue(50.0f).range(5.0f, 200.0f).visible(() -> this.mainShader.isSelected("Glass") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Glass") || this.shader2.isSelected("Glass")));
    private final ValueSetting glassQuality = new ValueSetting("Quality", "Quality of the blur (higher = smoother but slower)").setValue(10.0f).range(3.0f, 20.0f).visible(() -> this.mainShader.isSelected("Glass") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Glass") || this.shader2.isSelected("Glass")));
    private final ValueSetting glassDirection = new ValueSetting("Direction", "Number of blur directions").setValue(10.0f).range(4.0f, 16.0f).visible(() -> this.mainShader.isSelected("Glass") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Glass") || this.shader2.isSelected("Glass")));
    private final ValueSetting glassRefraction = new ValueSetting("Refraction", "Strength of glass distortion/refraction").setValue(1.0f).range(0.0f, 3.0f).visible(() -> this.mainShader.isSelected("Glass") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Glass") || this.shader2.isSelected("Glass")));
    private final ValueSetting glassBrightness = new ValueSetting("Brightness", "Brightness multiplier for glass effect").setValue(1.1f).range(0.5f, 2.0f).visible(() -> this.mainShader.isSelected("Glass") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Glass") || this.shader2.isSelected("Glass")));
    private final BooleanSetting glassChromatic = new BooleanSetting("Chromatic Aberration", "Enable chromatic aberration effect").setValue(true).visible(() -> this.mainShader.isSelected("Glass") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Glass") || this.shader2.isSelected("Glass")));
    private final BooleanSetting glassDistortion = new BooleanSetting("Distortion", "Enable wave distortion/wobbling effect").setValue(true).visible(() -> this.mainShader.isSelected("Glass") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Glass") || this.shader2.isSelected("Glass")));
    private final BooleanSetting glassHideHand = new BooleanSetting("Hide Hand Texture", "Hide the hand texture completely, showing only the glass effect").setValue(true).visible(() -> this.mainShader.isSelected("Glass") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Glass") || this.shader2.isSelected("Glass")));
    private final ValueSetting glassBackgroundBlur = new ValueSetting("Background Blur", "Blur strength for the scene behind the hidden hand").setValue(70.0f).range(10, 100).visible(() -> (this.mainShader.isSelected("Glass") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Glass") || this.shader2.isSelected("Glass"))) && this.glassHideHand.isValue());
    private final ColorSetting glassTintColor = new ColorSetting("\u0421\u0442\u0435\u043a\u043b\u044f\u043d\u043d\u044b\u0439 \u043e\u0442\u0442\u0435\u043d\u043e\u043a", "\u0426\u0432\u0435\u0442 \u043f\u0440\u0435\u043b\u043e\u043c\u043b\u0451\u043d\u043d\u043e\u0433\u043e \u0441\u0442\u0435\u043a\u043b\u0430 \u0438 \u0441\u0432\u0435\u0447\u0435\u043d\u0438\u044f \u043f\u043e \u043a\u0440\u0430\u044e").value(-2908929).visible(() -> this.mainShader.isSelected("Glass") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Glass") || this.shader2.isSelected("Glass")));
    private final ValueSetting glassTintIntensity = new ValueSetting("\u0421\u0438\u043b\u0430 \u043e\u0442\u0442\u0435\u043d\u043a\u0430 \u0441\u0442\u0435\u043a\u043b\u0430", "\u0418\u043d\u0442\u0435\u043d\u0441\u0438\u0432\u043d\u043e\u0441\u0442\u044c \u0444\u0438\u043e\u043b\u0435\u0442\u043e\u0432\u043e\u0433\u043e \u043e\u0442\u0442\u0435\u043d\u043a\u0430 \u043d\u0430 \u0440\u0443\u043a\u0435").setValue(0.2f).range(0.0f, 0.8f).visible(() -> this.mainShader.isSelected("Glass") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Glass") || this.shader2.isSelected("Glass")));
    private final ValueSetting glassEdgeGlow = new ValueSetting("\u0421\u0432\u0435\u0447\u0435\u043d\u0438\u0435 \u043a\u0440\u0430\u044f", "\u042f\u0440\u043a\u043e\u0441\u0442\u044c \u0441\u0432\u0435\u0442\u044f\u0449\u0435\u0439\u0441\u044f \u043a\u0430\u0439\u043c\u044b \u0441\u0442\u0435\u043a\u043b\u0430").setValue(1.6f).range(0.0f, 3.0f).visible(() -> this.mainShader.isSelected("Glass") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Glass") || this.shader2.isSelected("Glass")));
    private final ValueSetting glassEdgeSoftness = new ValueSetting("\u041c\u044f\u0433\u043a\u043e\u0441\u0442\u044c \u043a\u0440\u0430\u044f", "\u0421\u0433\u043b\u0430\u0436\u0438\u0432\u0430\u043d\u0438\u0435 \u043a\u043e\u043d\u0442\u0443\u0440\u0430 \u0441\u0442\u0435\u043a\u043b\u0430 \u0432 \u043f\u0438\u043a\u0441\u0435\u043b\u044f\u0445").setValue(6.0f).range(0.0f, 28.0f).visible(() -> this.mainShader.isSelected("Glass") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Glass") || this.shader2.isSelected("Glass")));

    public static HandsShader getInstance() {
        return Instance.get(HandsShader.class);
    }

    private static float shaderTime() {
        return (float)(System.nanoTime() % 180000000000L) / 1.0E9f;
    }

    private static void setUniformColor(Shader shader, String name, int color) {
        shader.setUniform3f(name, (float)(color >>> 16 & 0xFF) / 255.0f, (float)(color >>> 8 & 0xFF) / 255.0f, (float)(color & 0xFF) / 255.0f);
    }

    private void setResolution(Shader shader) {
        shader.setUniform2f("resolution", mc.method_22683().method_4489(), mc.method_22683().method_4506());
    }

    public HandsShader() {
        super("HandsShader", "Hands Shader", ModuleCategory.RENDER);
        GroupSetting dualModeGroup = new GroupSetting("Dual Shader", "Combine two shader effects together", false).settings(this.shader1, this.shader2, this.blendMode, this.blendIntensity, this.shader1Strength, this.shader2Strength).visible(this.dualShaderMode::isValue);
        GroupSetting modeGroup = new GroupSetting("Mode", "Hand shader mode selection", false).settings(this.mainShader, this.dualShaderMode, dualModeGroup);
        GroupSetting solidGroup = new GroupSetting("Solid", "Solid color gradient settings", false).settings(this.solidColor1, this.solidColor2, this.gradientSpeed).visible(() -> this.mainShader.isSelected("Solid") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Solid") || this.shader2.isSelected("Solid")));
        GroupSetting chromaGroup = new GroupSetting("Chroma", "Chroma effect settings", false).settings(this.chromaSpeed, this.chromaSaturation, this.chromaBrightness).visible(() -> this.mainShader.isSelected("Chroma") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Chroma") || this.shader2.isSelected("Chroma")));
        GroupSetting balatroGroup = new GroupSetting("Balatro", "Balatro-style effect settings", false).settings(this.balatrospeed, this.balatroMainColor, this.balatroColor2, this.balatroColor3, this.balatroColor4).visible(() -> this.mainShader.isSelected("Balatro") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Balatro") || this.shader2.isSelected("Balatro")));
        GroupSetting smokeGroup = new GroupSetting("Smoke", "Smoke effect settings", false).settings(this.smokeColor, this.smokeIntensity, this.smokeSpeed).visible(() -> this.mainShader.isSelected("Smoke") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Smoke") || this.shader2.isSelected("Smoke")));
        GroupSetting stripesGroup = new GroupSetting("Stripes", "Diagonal stripes effect settings", false).settings(this.stripesColor1, this.stripesColor2, this.stripesWidth, this.stripesSpeed).visible(() -> this.mainShader.isSelected("Stripes") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Stripes") || this.shader2.isSelected("Stripes")));
        GroupSetting glowGroup = new GroupSetting("Glow", "Hand glow effect settings", false).settings(this.effectGlow, this.glowMode, this.fillGlow, this.glowRadius, this.glowPower, this.glowColor, this.glowDispersion).visible(this::showsGlowControls);
        GroupSetting glassHandEffectGroup = new GroupSetting("Hand Effect", "Glass effect settings for visible hand", false).settings(this.glassBlurSize, this.glassQuality, this.glassDirection, this.glassBrightness).visible(() -> (this.mainShader.isSelected("Glass") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Glass") || this.shader2.isSelected("Glass"))) && !this.glassHideHand.isValue());
        GroupSetting glassBackgroundEffectGroup = new GroupSetting("Background Effect", "Effect settings for background behind hidden hand", false).settings(this.glassBackgroundBlur).visible(() -> (this.mainShader.isSelected("Glass") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Glass") || this.shader2.isSelected("Glass"))) && this.glassHideHand.isValue());
        GroupSetting glassAdvancedGroup = new GroupSetting("Advanced", "Advanced glass effect settings", false).settings(this.glassRefraction, this.glassChromatic, this.glassDistortion, this.glassTintColor, this.glassTintIntensity, this.glassEdgeGlow, this.glassEdgeSoftness).visible(() -> this.mainShader.isSelected("Glass") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Glass") || this.shader2.isSelected("Glass")));
        GroupSetting glassGroup = new GroupSetting("Glass", "Liquid glass effect settings", false).settings(this.glassHideHand, glassHandEffectGroup, glassBackgroundEffectGroup, glassAdvancedGroup).visible(() -> this.mainShader.isSelected("Glass") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Glass") || this.shader2.isSelected("Glass")));
        this.setup(modeGroup, solidGroup, chromaGroup, balatroGroup, smokeGroup, glowGroup, glassGroup);
    }

    @Override
    public void activate() {
        HandEffectRenderer handEffectRenderer;
        GlassHandsRenderer renderer = GlassHandsRenderer.getInstance();
        if (renderer != null) {
            renderer.invalidate();
            renderer.setEnabled(true);
            this.updateGlassHandsRenderer(renderer);
        }
        if ((handEffectRenderer = HandEffectRenderer.getInstance()) != null) {
            handEffectRenderer.invalidate();
            handEffectRenderer.setEnabled(true);
        }
    }

    @Override
    public void deactivate() {
        HandEffectRenderer handEffectRenderer;
        GlassHandsRenderer renderer = GlassHandsRenderer.getInstance();
        if (renderer != null) {
            renderer.setEnabled(false);
        }
        if ((handEffectRenderer = HandEffectRenderer.getInstance()) != null) {
            handEffectRenderer.setEnabled(false);
        }
    }

    public void renderGlassHandsPre() {
        if (this.usesGlassShader()) {
            GlassHandsRenderer renderer = GlassHandsRenderer.getInstance();
            if (renderer == null) {
                return;
            }
            renderer.setEnabled(true);
            this.updateGlassHandsRenderer(renderer);
            renderer.captureSceneBeforeHands();
        } else if (this.usesMaskedShader()) {
            HandEffectRenderer renderer = HandEffectRenderer.getInstance();
            if (renderer == null) {
                return;
            }
            renderer.setEnabled(true);
            this.updateHandEffectRenderer(renderer);
            renderer.captureBeforeHands();
        }
    }

    public void renderGlassHandsPost() {
        if (this.usesGlassShader()) {
            GlassHandsRenderer renderer = GlassHandsRenderer.getInstance();
            if (renderer == null) {
                return;
            }
            this.updateGlassHandsRenderer(renderer);
            renderer.captureSceneAfterHands();
            renderer.renderGlassEffect();
        } else if (this.usesMaskedShader()) {
            HandEffectRenderer renderer = HandEffectRenderer.getInstance();
            if (renderer == null) {
                return;
            }
            this.updateHandEffectRenderer(renderer);
            renderer.captureAfterHands();
            renderer.renderEffect();
        }
    }

    private boolean usesGlassShader() {
        return this.isState() && (this.mainShader.isSelected("Glass") || this.dualShaderMode.isValue() && (this.shader1.isSelected("Glass") || this.shader2.isSelected("Glass")));
    }

    private boolean usesMaskedShader() {
        String shader = this.selectedMaskShader();
        return this.isState() && shader != null && !shader.equals("None") && !shader.equals("Glass");
    }

    private String selectedMaskShader() {
        if (this.dualShaderMode.isValue()) {
            if (!this.shader1.isSelected("None") && !this.shader1.isSelected("Glass")) {
                return this.shader1.getSelected();
            }
            if (!this.shader2.isSelected("None") && !this.shader2.isSelected("Glass")) {
                return this.shader2.getSelected();
            }
            return null;
        }
        return this.mainShader.getSelected();
    }

    private boolean standaloneGlowSelected() {
        return "Glow".equals(this.selectedMaskShader()) || "Aura".equals(this.selectedMaskShader());
    }

    private boolean optionalGlowSelected() {
        String shader = this.selectedMaskShader();
        return "Solid".equals(shader) || "Chroma".equals(shader) || "Balatro".equals(shader);
    }

    private boolean showsGlowControls() {
        return this.standaloneGlowSelected() || this.optionalGlowSelected();
    }

    private boolean glowParametersVisible() {
        return this.standaloneGlowSelected() || this.optionalGlowSelected() && this.effectGlow.isValue();
    }

    private void updateGlassHandsRenderer(GlassHandsRenderer renderer) {
        renderer.setBlurRadius(Math.max(1.0f, this.glassBlurSize.getValue() / 20.0f));
        renderer.setBlurIterations(Math.max(1, Math.min(5, this.glassQuality.getInt() / 4)));
        renderer.setSaturation(1.0f);
        renderer.setReflect(true);
        renderer.setTintColor(this.glassTintColor.getColor());
        renderer.setTintIntensity(this.glassTintIntensity.getValue());
        renderer.setEdgeGlowIntensity(this.glassEdgeGlow.getValue());
        renderer.setEdgeSoftness(this.glassEdgeSoftness.getValue());
        renderer.setRefraction(this.glassRefraction.getValue(), this.glassRefraction.getValue() > 0.001f ? 18.0f : 0.0f);
        renderer.setChromaticStrength(this.glassChromatic.isValue() ? 0.7f : 0.0f);
        renderer.setShimmerStrength(this.glassDistortion.isValue() ? 0.9f : 0.0f);
        renderer.setHaloReach(24.0f);
    }

    private void updateHandEffectRenderer(HandEffectRenderer renderer) {
        String shader = this.selectedMaskShader();
        int mode = this.handEffectMode(shader);
        int first = this.firstEffectColor(shader);
        int second = this.secondEffectColor(shader);
        float intensity = shader != null && (shader.equals("Glow") || this.effectGlow.isValue()) ? Math.max(0.6f, this.glowPower.getValue()) : 1.0f;
        float speed = this.effectSpeed(shader);
        renderer.setEffect(mode, first, second, intensity, speed, this.glowRadius.getValue(), this.glowDispersion.getValue(), this.fillGlow.isValue());
    }

    private int handEffectMode(String shader) {
        if (shader == null) {
            return 0;
        }
        if (this.effectGlow.isValue()) {
            if (shader.equals("Solid")) {
                return 13;
            }
            if (shader.equals("Chroma")) {
                return 14;
            }
            if (shader.equals("Balatro")) {
                return 15;
            }
        }
        return switch (shader) {
            case "Solid" -> 1;
            case "Chroma" -> 2;
            case "Smoke" -> 3;
            case "Stripes" -> 4;
            case "Glow" -> {
                if (this.glowMode.isSelected("Chroma")) {
                    yield 11;
                }
                if (this.glowMode.isSelected("Balatro")) {
                    yield 12;
                }
                yield 6;
            }
            case "Camouflage" -> 7;
            case "Balatro" -> 10;
            case "Aura" -> 16;
            default -> 0;
        };
    }

    private int firstEffectColor(String shader) {
        if (shader == null) {
            return -1;
        }
        return switch (shader) {
            case "Solid" -> this.solidColor1.getColor();
            case "Smoke" -> this.smokeColor.getColor();
            case "Stripes" -> this.stripesColor1.getColor();
            case "Glow", "Aura" -> this.glowColor.getColor();
            case "Balatro" -> this.balatroMainColor.getColor();
            default -> -1;
        };
    }

    private int secondEffectColor(String shader) {
        if (shader == null) {
            return -9459457;
        }
        return switch (shader) {
            case "Solid" -> this.solidColor2.getColor();
            case "Stripes" -> this.stripesColor2.getColor();
            case "Balatro" -> this.balatroColor2.getColor();
            default -> -9459457;
        };
    }

    private float effectSpeed(String shader) {
        if (shader == null) {
            return 1.0f;
        }
        return switch (shader) {
            case "Solid" -> this.gradientSpeed.getValue();
            case "Chroma" -> this.chromaSpeed.getValue();
            case "Smoke" -> this.smokeSpeed.getValue();
            case "Stripes" -> this.stripesSpeed.getValue();
            case "Balatro" -> this.balatrospeed.getValue();
            case "Camouflage" -> 2.2f;
            default -> 1.0f;
        };
    }

    public void render() {
        if (!ShaderHelper.isInitialized()) {
            return;
        }
        try {
            if (this.dualShaderMode.isValue()) {
                this.renderShaderByName(this.shader1.getSelected());
                this.renderShaderByName(this.shader2.getSelected());
            } else if (!this.mainShader.isSelected("None")) {
                this.renderShaderByName(this.mainShader.getSelected());
            }
        }
        catch (Exception e) {
            LoggerUtil.error("Error rendering hand shader: " + e.getMessage());
        }
    }

    private void renderShaderByName(String shaderName) {
        switch (shaderName) {
            case "Solid": {
                this.renderSolid();
                break;
            }
            case "Chroma": {
                this.renderChroma();
                break;
            }
            case "Balatro": {
                this.renderBalatro();
                break;
            }
            case "Smoke": {
                this.renderSmoke();
                break;
            }
            case "Stripes": {
                this.renderStripes();
                break;
            }
            case "Snow": {
                this.renderSnow();
                break;
            }
            case "Glow": {
                this.renderGlow();
                break;
            }
            case "Glass": {
                this.renderGlass();
                break;
            }
            case "Camouflage": {
                this.renderSimpleShader(shaderName);
                break;
            }
        }
    }

    private void renderSimpleShader(String shaderName) {
        class_276 mainFbo = mc.method_1522();
        Shader shader = this.getShaderByName(shaderName);
        class_6367 copyFbo = ShaderHelper.getCopyFbo();
        if (mainFbo == null || shader == null || copyFbo == null) {
            return;
        }
        LegacyRenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
        LegacyRenderSystem.clear(16640);
        this.copySource(mainFbo, copyFbo);
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.defaultBlendFunc();
        LegacyRenderSystem.disableDepthTest();
        LegacyRenderSystem.activeTexture(33984);
        LegacyRenderSystem.bindTexture(copyFbo.method_30277());
        LegacyRenderSystem.activeTexture(33985);
        LegacyRenderSystem.bindTexture(copyFbo.method_30278());
        shader.bind();
        shader.setUniform1i("ColorTexture", 0);
        shader.setUniform1i("DepthTexture", 1);
        float gameTime = HandsShader.shaderTime();
        shader.setUniform1f("time", gameTime);
        this.setShaderUniforms(shader, shaderName, gameTime);
        ShaderHelper.drawFullScreenQuad();
        shader.unbind();
        LegacyRenderSystem.activeTexture(33984);
        LegacyRenderSystem.disableBlend();
        LegacyRenderSystem.enableDepthTest();
    }

    private void copySource(class_276 mainFbo, class_6367 copyFbo) {
        mainFbo.method_68445(copyFbo.method_71639());
        copyFbo.method_29329(mainFbo);
    }

    private void renderChroma() {
        class_276 mainFbo = mc.method_1522();
        Shader chromaShader = ShaderHelper.getChromaShader();
        class_6367 copyFbo = ShaderHelper.getCopyFbo();
        if (mainFbo == null || chromaShader == null || copyFbo == null) {
            return;
        }
        LegacyRenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
        LegacyRenderSystem.clear(16640);
        this.copySource(mainFbo, copyFbo);
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.defaultBlendFunc();
        LegacyRenderSystem.disableDepthTest();
        LegacyRenderSystem.activeTexture(33984);
        LegacyRenderSystem.bindTexture(copyFbo.method_30277());
        LegacyRenderSystem.activeTexture(33985);
        LegacyRenderSystem.bindTexture(copyFbo.method_30278());
        chromaShader.bind();
        chromaShader.setUniform1i("ColorTexture", 0);
        chromaShader.setUniform1i("DepthTexture", 1);
        float gameTime = HandsShader.shaderTime();
        chromaShader.setUniform1f("time", gameTime);
        chromaShader.setUniform1f("chromaSpeed", this.chromaSpeed.getValue());
        chromaShader.setUniform1f("chromaSaturation", this.chromaSaturation.getValue());
        chromaShader.setUniform1f("chromaBrightness", this.chromaBrightness.getValue());
        ShaderHelper.drawFullScreenQuad();
        chromaShader.unbind();
        LegacyRenderSystem.activeTexture(33984);
        LegacyRenderSystem.disableBlend();
        LegacyRenderSystem.enableDepthTest();
        if (mc.method_1522() != null) {
            // empty if block
        }
    }

    private void renderSolid() {
        class_276 mainFbo = mc.method_1522();
        Shader solidShader = ShaderHelper.getSolidShader();
        class_6367 copyFbo = ShaderHelper.getCopyFbo();
        if (mainFbo == null || solidShader == null || copyFbo == null) {
            return;
        }
        LegacyRenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
        LegacyRenderSystem.clear(16640);
        this.copySource(mainFbo, copyFbo);
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.defaultBlendFunc();
        LegacyRenderSystem.disableDepthTest();
        LegacyRenderSystem.activeTexture(33984);
        LegacyRenderSystem.bindTexture(copyFbo.method_30277());
        LegacyRenderSystem.activeTexture(33985);
        LegacyRenderSystem.bindTexture(copyFbo.method_30278());
        solidShader.bind();
        solidShader.setUniform1i("ColorTexture", 0);
        solidShader.setUniform1i("DepthTexture", 1);
        float gameTime = HandsShader.shaderTime() * this.gradientSpeed.getValue();
        solidShader.setUniform1f("time", gameTime);
        HandsShader.setUniformColor(solidShader, "customColor1", this.solidColor1.getColor());
        HandsShader.setUniformColor(solidShader, "customColor2", this.solidColor2.getColor());
        float maxAlpha = Math.max(this.solidColor1.getAlpha(), this.solidColor2.getAlpha());
        solidShader.setUniform1f("effectAlpha", maxAlpha);
        ShaderHelper.drawFullScreenQuad();
        solidShader.unbind();
        LegacyRenderSystem.activeTexture(33984);
        LegacyRenderSystem.disableBlend();
        LegacyRenderSystem.enableDepthTest();
        if (mc.method_1522() != null) {
            // empty if block
        }
    }

    private void renderBalatro() {
        class_276 mainFbo = mc.method_1522();
        Shader balatroShader = ShaderHelper.getBalatroShader();
        class_6367 copyFbo = ShaderHelper.getCopyFbo();
        if (mainFbo == null || balatroShader == null || copyFbo == null) {
            return;
        }
        LegacyRenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
        LegacyRenderSystem.clear(16640);
        this.copySource(mainFbo, copyFbo);
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.defaultBlendFunc();
        LegacyRenderSystem.disableDepthTest();
        LegacyRenderSystem.activeTexture(33984);
        LegacyRenderSystem.bindTexture(copyFbo.method_30277());
        LegacyRenderSystem.activeTexture(33985);
        LegacyRenderSystem.bindTexture(copyFbo.method_30278());
        balatroShader.bind();
        balatroShader.setUniform1i("ColorTexture", 0);
        balatroShader.setUniform1i("DepthTexture", 1);
        float gameTime = HandsShader.shaderTime() * this.balatrospeed.getValue();
        balatroShader.setUniform1f("time", gameTime);
        this.setResolution(balatroShader);
        HandsShader.setUniformColor(balatroShader, "color1", this.balatroMainColor.getColor());
        HandsShader.setUniformColor(balatroShader, "color2", this.balatroColor2.getColor());
        HandsShader.setUniformColor(balatroShader, "color3", this.balatroColor3.getColor());
        HandsShader.setUniformColor(balatroShader, "color4", this.balatroColor4.getColor());
        ShaderHelper.drawFullScreenQuad();
        balatroShader.unbind();
        LegacyRenderSystem.activeTexture(33984);
        LegacyRenderSystem.disableBlend();
        LegacyRenderSystem.enableDepthTest();
        if (mc.method_1522() != null) {
            // empty if block
        }
    }

    private void renderSmoke() {
        class_276 mainFbo = mc.method_1522();
        Shader smokeShader = ShaderHelper.getSmokeShader();
        class_6367 copyFbo = ShaderHelper.getCopyFbo();
        if (mainFbo == null || smokeShader == null || copyFbo == null) {
            return;
        }
        LegacyRenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
        LegacyRenderSystem.clear(16640);
        this.copySource(mainFbo, copyFbo);
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.defaultBlendFunc();
        LegacyRenderSystem.disableDepthTest();
        LegacyRenderSystem.activeTexture(33984);
        LegacyRenderSystem.bindTexture(copyFbo.method_30277());
        LegacyRenderSystem.activeTexture(33985);
        LegacyRenderSystem.bindTexture(copyFbo.method_30278());
        smokeShader.bind();
        smokeShader.setUniform1i("ColorTexture", 0);
        smokeShader.setUniform1i("DepthTexture", 1);
        float gameTime = HandsShader.shaderTime() * this.smokeSpeed.getValue();
        smokeShader.setUniform1f("time", gameTime);
        this.setResolution(smokeShader);
        HandsShader.setUniformColor(smokeShader, "smokeColor", this.smokeColor.getColor());
        smokeShader.setUniform1f("smokeIntensity", this.smokeIntensity.getValue());
        ShaderHelper.drawFullScreenQuad();
        smokeShader.unbind();
        LegacyRenderSystem.activeTexture(33984);
        LegacyRenderSystem.disableBlend();
        LegacyRenderSystem.enableDepthTest();
        if (mc.method_1522() != null) {
            // empty if block
        }
    }

    private void renderStripes() {
        class_276 mainFbo = mc.method_1522();
        Shader stripesShader = ShaderHelper.getStripesShader();
        class_6367 copyFbo = ShaderHelper.getCopyFbo();
        if (mainFbo == null || stripesShader == null || copyFbo == null) {
            return;
        }
        LegacyRenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
        LegacyRenderSystem.clear(16640);
        this.copySource(mainFbo, copyFbo);
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.defaultBlendFunc();
        LegacyRenderSystem.disableDepthTest();
        LegacyRenderSystem.activeTexture(33984);
        LegacyRenderSystem.bindTexture(copyFbo.method_30277());
        LegacyRenderSystem.activeTexture(33985);
        LegacyRenderSystem.bindTexture(copyFbo.method_30278());
        stripesShader.bind();
        stripesShader.setUniform1i("ColorTexture", 0);
        stripesShader.setUniform1i("DepthTexture", 1);
        float gameTime = HandsShader.shaderTime();
        stripesShader.setUniform1f("time", gameTime);
        this.setResolution(stripesShader);
        HandsShader.setUniformColor(stripesShader, "stripesColor1", this.stripesColor1.getColor());
        HandsShader.setUniformColor(stripesShader, "stripesColor2", this.stripesColor2.getColor());
        stripesShader.setUniform1f("stripesWidth", this.stripesWidth.getValue());
        stripesShader.setUniform1f("stripesSpeed", this.stripesSpeed.getValue());
        ShaderHelper.drawFullScreenQuad();
        stripesShader.unbind();
        LegacyRenderSystem.activeTexture(33984);
        LegacyRenderSystem.disableBlend();
        LegacyRenderSystem.enableDepthTest();
        if (mc.method_1522() != null) {
            // empty if block
        }
    }

    private void renderSnow() {
        class_276 mainFbo = mc.method_1522();
        Shader snowShader = ShaderHelper.getSnowShader();
        class_6367 copyFbo = ShaderHelper.getCopyFbo();
        if (mainFbo == null || snowShader == null || copyFbo == null) {
            return;
        }
        LegacyRenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
        LegacyRenderSystem.clear(16640);
        this.copySource(mainFbo, copyFbo);
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.defaultBlendFunc();
        LegacyRenderSystem.disableDepthTest();
        LegacyRenderSystem.activeTexture(33984);
        LegacyRenderSystem.bindTexture(copyFbo.method_30277());
        LegacyRenderSystem.activeTexture(33985);
        LegacyRenderSystem.bindTexture(copyFbo.method_30278());
        snowShader.bind();
        snowShader.setUniform1i("ColorTexture", 0);
        snowShader.setUniform1i("DepthTexture", 1);
        float gameTime = HandsShader.shaderTime();
        snowShader.setUniform1f("time", gameTime);
        this.setResolution(snowShader);
        HandsShader.setUniformColor(snowShader, "snowColor", this.snowColor.getColor());
        snowShader.setUniform1f("snowIntensity", this.snowIntensity.getValue());
        snowShader.setUniform1f("snowSpeed", this.snowSpeed.getValue());
        ShaderHelper.drawFullScreenQuad();
        snowShader.unbind();
        LegacyRenderSystem.activeTexture(33984);
        LegacyRenderSystem.disableBlend();
        LegacyRenderSystem.enableDepthTest();
        if (mc.method_1522() != null) {
            // empty if block
        }
    }

    private void renderGlow() {
        class_276 mainFbo = mc.method_1522();
        Shader glowShader = ShaderHelper.getGlowShader();
        class_6367 copyFbo = ShaderHelper.getCopyFbo();
        if (mainFbo == null || glowShader == null || copyFbo == null) {
            return;
        }
        LegacyRenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
        LegacyRenderSystem.clear(16640);
        this.copySource(mainFbo, copyFbo);
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.defaultBlendFunc();
        LegacyRenderSystem.disableDepthTest();
        LegacyRenderSystem.activeTexture(33984);
        LegacyRenderSystem.bindTexture(copyFbo.method_30277());
        LegacyRenderSystem.activeTexture(33985);
        LegacyRenderSystem.bindTexture(copyFbo.method_30278());
        glowShader.bind();
        glowShader.setUniform1i("ColorTexture", 0);
        glowShader.setUniform1i("DepthTexture", 1);
        float gameTime = HandsShader.shaderTime();
        glowShader.setUniform1f("time", gameTime);
        this.setResolution(glowShader);
        glowShader.setUniformBool("fillGlow", this.fillGlow.isValue());
        glowShader.setUniform1f("glowRadius", this.glowRadius.getValue());
        glowShader.setUniform1f("glowPower", this.glowPower.getValue());
        glowShader.setUniform1f("glowDispersion", this.glowDispersion.getValue());
        HandsShader.setUniformColor(glowShader, "glowColor", this.glowColor.getColor());
        ShaderHelper.drawFullScreenQuad();
        glowShader.unbind();
        LegacyRenderSystem.activeTexture(33984);
        LegacyRenderSystem.disableBlend();
        LegacyRenderSystem.enableDepthTest();
        if (mc.method_1522() != null) {
            // empty if block
        }
    }

    private void renderGlass() {
        class_276 mainFbo = mc.method_1522();
        Shader glassShader = ShaderHelper.getGlassShader();
        class_6367 copyFbo = ShaderHelper.getCopyFbo();
        if (mainFbo == null || glassShader == null || copyFbo == null) {
            return;
        }
        LegacyRenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
        LegacyRenderSystem.clear(16640);
        this.copySource(mainFbo, copyFbo);
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.blendFuncSeparate(1, 0, 1, 0);
        LegacyRenderSystem.disableDepthTest();
        LegacyRenderSystem.activeTexture(33984);
        LegacyRenderSystem.bindTexture(copyFbo.method_30277());
        LegacyRenderSystem.activeTexture(33985);
        LegacyRenderSystem.bindTexture(copyFbo.method_30278());
        glassShader.bind();
        glassShader.setUniform1i("ColorTexture", 0);
        glassShader.setUniform1i("DepthTexture", 1);
        float gameTime = HandsShader.shaderTime();
        glassShader.setUniform1f("time", gameTime);
        this.setResolution(glassShader);
        glassShader.setUniform1f("blurSize", this.glassBlurSize.getValue());
        glassShader.setUniform1f("quality", this.glassQuality.getValue());
        glassShader.setUniform1f("direction", this.glassDirection.getValue());
        glassShader.setUniform1f("refraction", this.glassRefraction.getValue());
        glassShader.setUniform1f("brightness", this.glassBrightness.getValue());
        glassShader.setUniformBool("enableChromatic", this.glassChromatic.isValue());
        glassShader.setUniformBool("enableDistortion", this.glassDistortion.isValue());
        glassShader.setUniformBool("hideHand", this.glassHideHand.isValue());
        glassShader.setUniform1f("backgroundBlur", this.glassBackgroundBlur.getValue());
        ShaderHelper.drawFullScreenQuad();
        glassShader.unbind();
        LegacyRenderSystem.activeTexture(33984);
        LegacyRenderSystem.disableBlend();
        LegacyRenderSystem.enableDepthTest();
        if (mc.method_1522() != null) {
            // empty if block
        }
    }

    private void renderDualShader() {
        class_276 mainFbo = mc.method_1522();
        class_6367 copyFbo = ShaderHelper.getCopyFbo();
        class_6367 shader1Fbo = ShaderHelper.getShader1Fbo();
        class_6367 shader2Fbo = ShaderHelper.getShader2Fbo();
        Shader blendShader = ShaderHelper.getBlendShader();
        if (mainFbo == null || copyFbo == null || shader1Fbo == null || shader2Fbo == null || blendShader == null) {
            return;
        }
        LegacyRenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
        LegacyRenderSystem.clear(16640);
        this.copySource(mainFbo, copyFbo);
        this.renderShaderToFramebuffer(this.shader1.getSelected(), shader1Fbo, copyFbo);
        this.renderShaderToFramebuffer(this.shader2.getSelected(), shader2Fbo, copyFbo);
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.defaultBlendFunc();
        LegacyRenderSystem.disableDepthTest();
        LegacyRenderSystem.activeTexture(33984);
        LegacyRenderSystem.bindTexture(shader1Fbo.method_30277());
        LegacyRenderSystem.activeTexture(33985);
        LegacyRenderSystem.bindTexture(shader2Fbo.method_30277());
        LegacyRenderSystem.activeTexture(33986);
        LegacyRenderSystem.bindTexture(copyFbo.method_30278());
        blendShader.bind();
        blendShader.setUniform1i("Shader1Texture", 0);
        blendShader.setUniform1i("Shader2Texture", 1);
        blendShader.setUniform1i("DepthTexture", 2);
        int blendModeInt = 0;
        if (this.blendMode.isSelected("Add")) {
            blendModeInt = 1;
        } else if (this.blendMode.isSelected("Multiply")) {
            blendModeInt = 2;
        } else if (this.blendMode.isSelected("Screen")) {
            blendModeInt = 3;
        } else if (this.blendMode.isSelected("Overlay")) {
            blendModeInt = 4;
        } else if (this.blendMode.isSelected("Difference")) {
            blendModeInt = 5;
        }
        blendShader.setUniform1i("blendMode", blendModeInt);
        blendShader.setUniform1f("blendIntensity", this.blendIntensity.getValue());
        blendShader.setUniform1f("shader1Strength", this.shader1Strength.getValue());
        blendShader.setUniform1f("shader2Strength", this.shader2Strength.getValue());
        ShaderHelper.drawFullScreenQuad();
        blendShader.unbind();
        LegacyRenderSystem.activeTexture(33984);
        LegacyRenderSystem.disableBlend();
        LegacyRenderSystem.enableDepthTest();
        if (mc.method_1522() != null) {
            // empty if block
        }
    }

    private void renderShaderToFramebuffer(String shaderName, class_6367 targetFbo, class_6367 sourceFbo) {
        if (shaderName.equals("None")) {
            LegacyRenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
            LegacyRenderSystem.clear(16640);
            targetFbo.method_29329((class_276)sourceFbo);
            return;
        }
        Shader shader = this.getShaderByName(shaderName);
        if (shader == null) {
            return;
        }
        LegacyRenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
        LegacyRenderSystem.clear(16640);
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.defaultBlendFunc();
        LegacyRenderSystem.disableDepthTest();
        LegacyRenderSystem.activeTexture(33984);
        LegacyRenderSystem.bindTexture(sourceFbo.method_30277());
        LegacyRenderSystem.activeTexture(33985);
        LegacyRenderSystem.bindTexture(sourceFbo.method_30278());
        shader.bind();
        shader.setUniform1i("ColorTexture", 0);
        shader.setUniform1i("DepthTexture", 1);
        float gameTime = HandsShader.shaderTime();
        shader.setUniform1f("time", gameTime);
        this.setShaderUniforms(shader, shaderName, gameTime);
        ShaderHelper.drawFullScreenQuad();
        shader.unbind();
        LegacyRenderSystem.disableBlend();
        LegacyRenderSystem.enableDepthTest();
    }

    private Shader getShaderByName(String name) {
        return switch (name) {
            case "Chroma" -> ShaderHelper.getChromaShader();
            case "Solid" -> ShaderHelper.getSolidShader();
            case "Balatro" -> ShaderHelper.getBalatroShader();
            case "Smoke" -> ShaderHelper.getSmokeShader();
            case "Stripes" -> ShaderHelper.getStripesShader();
            case "Snow" -> ShaderHelper.getSnowShader();
            case "Glow" -> ShaderHelper.getGlowShader();
            case "Glass" -> ShaderHelper.getGlassShader();
            case "Camouflage" -> ShaderHelper.getCamouflageShader();
            default -> null;
        };
    }

    private void setShaderUniforms(Shader shader, String shaderName, float gameTime) {
        switch (shaderName) {
            case "Chroma": {
                shader.setUniform1f("chromaSpeed", this.chromaSpeed.getValue());
                shader.setUniform1f("chromaSaturation", this.chromaSaturation.getValue());
                shader.setUniform1f("chromaBrightness", this.chromaBrightness.getValue());
                break;
            }
            case "Solid": {
                shader.setUniform1f("time", gameTime * this.gradientSpeed.getValue());
                HandsShader.setUniformColor(shader, "customColor1", this.solidColor1.getColor());
                HandsShader.setUniformColor(shader, "customColor2", this.solidColor2.getColor());
                float maxAlpha = Math.max(this.solidColor1.getAlpha(), this.solidColor2.getAlpha());
                shader.setUniform1f("effectAlpha", maxAlpha);
                break;
            }
            case "Balatro": {
                shader.setUniform1f("time", gameTime * this.balatrospeed.getValue());
                this.setResolution(shader);
                HandsShader.setUniformColor(shader, "color1", this.balatroMainColor.getColor());
                HandsShader.setUniformColor(shader, "color2", this.balatroColor2.getColor());
                HandsShader.setUniformColor(shader, "color3", this.balatroColor3.getColor());
                HandsShader.setUniformColor(shader, "color4", this.balatroColor4.getColor());
                break;
            }
            case "Smoke": {
                shader.setUniform1f("time", gameTime * this.smokeSpeed.getValue());
                this.setResolution(shader);
                HandsShader.setUniformColor(shader, "smokeColor", this.smokeColor.getColor());
                shader.setUniform1f("smokeIntensity", this.smokeIntensity.getValue());
                break;
            }
            case "Stripes": {
                this.setResolution(shader);
                HandsShader.setUniformColor(shader, "stripesColor1", this.stripesColor1.getColor());
                HandsShader.setUniformColor(shader, "stripesColor2", this.stripesColor2.getColor());
                shader.setUniform1f("stripesWidth", this.stripesWidth.getValue());
                shader.setUniform1f("stripesSpeed", this.stripesSpeed.getValue());
                break;
            }
            case "Snow": {
                this.setResolution(shader);
                HandsShader.setUniformColor(shader, "snowColor", this.snowColor.getColor());
                shader.setUniform1f("snowIntensity", this.snowIntensity.getValue());
                shader.setUniform1f("snowSpeed", this.snowSpeed.getValue());
                break;
            }
            case "Glow": {
                this.setResolution(shader);
                shader.setUniformBool("fillGlow", this.fillGlow.isValue());
                shader.setUniform1f("glowRadius", this.glowRadius.getValue());
                shader.setUniform1f("glowPower", this.glowPower.getValue());
                shader.setUniform1f("glowDispersion", this.glowDispersion.getValue());
                HandsShader.setUniformColor(shader, "glowColor", this.glowColor.getColor());
                break;
            }
            case "Glass": {
                this.setResolution(shader);
                shader.setUniform1f("blurSize", this.glassBlurSize.getValue());
                shader.setUniform1f("quality", this.glassQuality.getValue());
                shader.setUniform1f("direction", this.glassDirection.getValue());
                shader.setUniform1f("refraction", this.glassRefraction.getValue());
                shader.setUniform1f("brightness", this.glassBrightness.getValue());
                shader.setUniformBool("enableChromatic", this.glassChromatic.isValue());
                shader.setUniformBool("enableDistortion", this.glassDistortion.isValue());
                break;
            }
            case "Camouflage": 
            case "Mirror": 
            case "Lines": {
                this.setResolution(shader);
            }
        }
    }

    public BooleanSetting getDualShaderMode() {
        return this.dualShaderMode;
    }

    public SelectSetting getMainShader() {
        return this.mainShader;
    }

    public SelectSetting getShader1() {
        return this.shader1;
    }

    public SelectSetting getShader2() {
        return this.shader2;
    }

    public SelectSetting getBlendMode() {
        return this.blendMode;
    }

    public ValueSetting getBlendIntensity() {
        return this.blendIntensity;
    }

    public ValueSetting getShader1Strength() {
        return this.shader1Strength;
    }

    public ValueSetting getShader2Strength() {
        return this.shader2Strength;
    }

    public ColorSetting getSolidColor1() {
        return this.solidColor1;
    }

    public ColorSetting getSolidColor2() {
        return this.solidColor2;
    }

    public ValueSetting getGradientSpeed() {
        return this.gradientSpeed;
    }

    public ValueSetting getChromaSpeed() {
        return this.chromaSpeed;
    }

    public ValueSetting getChromaSaturation() {
        return this.chromaSaturation;
    }

    public ValueSetting getChromaBrightness() {
        return this.chromaBrightness;
    }

    public ValueSetting getBalatrospeed() {
        return this.balatrospeed;
    }

    public ColorSetting getBalatroMainColor() {
        return this.balatroMainColor;
    }

    public ColorSetting getBalatroColor2() {
        return this.balatroColor2;
    }

    public ColorSetting getBalatroColor3() {
        return this.balatroColor3;
    }

    public ColorSetting getBalatroColor4() {
        return this.balatroColor4;
    }

    public ColorSetting getSmokeColor() {
        return this.smokeColor;
    }

    public ValueSetting getSmokeIntensity() {
        return this.smokeIntensity;
    }

    public ValueSetting getSmokeSpeed() {
        return this.smokeSpeed;
    }

    public ColorSetting getStripesColor1() {
        return this.stripesColor1;
    }

    public ColorSetting getStripesColor2() {
        return this.stripesColor2;
    }

    public ValueSetting getStripesWidth() {
        return this.stripesWidth;
    }

    public ValueSetting getStripesSpeed() {
        return this.stripesSpeed;
    }

    public ColorSetting getSnowColor() {
        return this.snowColor;
    }

    public ValueSetting getSnowIntensity() {
        return this.snowIntensity;
    }

    public ValueSetting getSnowSpeed() {
        return this.snowSpeed;
    }

    public BooleanSetting getFillGlow() {
        return this.fillGlow;
    }

    public SelectSetting getGlowMode() {
        return this.glowMode;
    }

    public BooleanSetting getEffectGlow() {
        return this.effectGlow;
    }

    public ValueSetting getGlowRadius() {
        return this.glowRadius;
    }

    public ValueSetting getGlowPower() {
        return this.glowPower;
    }

    public ColorSetting getGlowColor() {
        return this.glowColor;
    }

    public ValueSetting getGlowDispersion() {
        return this.glowDispersion;
    }

    public ValueSetting getGlassBlurSize() {
        return this.glassBlurSize;
    }

    public ValueSetting getGlassQuality() {
        return this.glassQuality;
    }

    public ValueSetting getGlassDirection() {
        return this.glassDirection;
    }

    public ValueSetting getGlassRefraction() {
        return this.glassRefraction;
    }

    public ValueSetting getGlassBrightness() {
        return this.glassBrightness;
    }

    public BooleanSetting getGlassChromatic() {
        return this.glassChromatic;
    }

    public BooleanSetting getGlassDistortion() {
        return this.glassDistortion;
    }

    public BooleanSetting getGlassHideHand() {
        return this.glassHideHand;
    }

    public ValueSetting getGlassBackgroundBlur() {
        return this.glassBackgroundBlur;
    }

    public ColorSetting getGlassTintColor() {
        return this.glassTintColor;
    }

    public ValueSetting getGlassTintIntensity() {
        return this.glassTintIntensity;
    }

    public ValueSetting getGlassEdgeGlow() {
        return this.glassEdgeGlow;
    }

    public ValueSetting getGlassEdgeSoftness() {
        return this.glassEdgeSoftness;
    }
}

