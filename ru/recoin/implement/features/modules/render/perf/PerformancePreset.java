package ru.recoin.implement.features.modules.render.perf;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_12393;
import net.minecraft.class_4063;
import net.minecraft.class_4066;
import net.minecraft.class_6597;
import net.minecraft.class_7172;
import ru.recoin.api.feature.module.Module;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.features.modules.render.Hud;

/**
 * Applies a set of FPS-friendly game options and client switches, and remembers the previous values so
 * that everything can be put back exactly as it was when Optimization is turned off.
 */
public final class PerformancePreset {
    /** Client modules that cost the most frame time. */
    public static final String[] HEAVY_MODULES = new String[]{"MotionBlur", "HandsShader", "Rain", "Winter", "Atmosphere", "Trails", "JumpCircle", "WorldLightning", "KillEffect", "HitBubble", "AttackEffect", "Particles", "AmbientParticles", "Weather", "WetWorld", "ShaderESP", "Contrast", "Arrows", "Holiday"};

    public static final class Settings {
        public boolean minimalParticles = true;
        public boolean noClouds = true;
        public boolean noEntityShadows = true;
        public boolean noBiomeBlend = true;
        public boolean noSmoothLighting;
        public boolean fastLeaves = true;
        public boolean noImprovedTransparency = true;
        public boolean noChunkFade = true;
        public boolean noVignette = true;
        public boolean noTextureFiltering = true;
        public boolean lowWeather = true;
        public boolean threadedChunks = true;
        public boolean noMenuBlur = true;
        public boolean unlimitedFps = true;
        public int entityDistancePercent = 75;
        public int renderDistance = -1;       // -1 = keep
        public int simulationDistance = -1;   // -1 = keep
        public boolean noHudBlur = true;
        public boolean disableHeavyModules = true;
        public Module owner;

        public String signature() {
            return "" + this.minimalParticles + this.noClouds + this.noEntityShadows + this.noBiomeBlend + this.noSmoothLighting + this.fastLeaves
                    + this.noImprovedTransparency + this.noChunkFade + this.noVignette + this.noTextureFiltering + this.lowWeather + this.threadedChunks
                    + this.noMenuBlur + this.unlimitedFps + this.entityDistancePercent + "/" + this.renderDistance + "/" + this.simulationDistance
                    + this.noHudBlur + this.disableHeavyModules;
        }
    }

    /** option -> value it had before the preset changed it */
    private final Map<class_7172<?>, Object> saved = new LinkedHashMap<>();
    private final List<Module> switchedOff = new ArrayList<Module>();
    private boolean applied;
    private boolean hudTouched;
    private boolean hudBlur;
    private boolean hudShadows;
    private boolean hudOutline;
    private String signature = "";

    public boolean isApplied() {
        return this.applied;
    }

    public String signature() {
        return this.signature;
    }

    public void apply(Settings s) {
        class_310 client = class_310.method_1551();
        if (client == null || client.field_1690 == null || this.applied) {
            return;
        }
        class_315 o = client.field_1690;
        this.applied = true;
        if (s.minimalParticles) this.put(() -> o.method_42475(), class_4066.values()[class_4066.values().length - 1]);
        if (s.noClouds) this.put(() -> o.method_42528(), class_4063.values()[0]);
        if (s.noEntityShadows) this.put(() -> o.method_42435(), Boolean.FALSE);
        if (s.noBiomeBlend) this.put(() -> o.method_41805(), 0);
        if (s.noSmoothLighting) this.put(() -> o.method_41792(), Boolean.FALSE);
        if (s.fastLeaves) this.put(() -> o.method_75334(), Boolean.FALSE);            // opaque leaves: far fewer faces and no alpha test
        if (s.noImprovedTransparency) this.put(() -> o.method_75337(), Boolean.FALSE);   // the old "Fabulous" transparency pass
        if (s.noChunkFade) this.put(() -> o.method_76253(), 0.0);
        if (s.noVignette) this.put(() -> o.method_75335(), Boolean.FALSE);
        if (s.noTextureFiltering) this.put(() -> o.method_76747(), class_12393.field_64663);
        if (s.lowWeather) this.put(() -> o.method_75333(), 3);
        if (s.threadedChunks) this.put(() -> o.method_41798(), class_6597.field_34788);  // chunk rebuilds never block the frame
        if (s.noMenuBlur) this.put(() -> o.method_57702(), 0);
        if (s.unlimitedFps) {
            this.put(() -> o.method_42433(), Boolean.FALSE);
            this.put(() -> o.method_42524(), 260);   // 260 = "Unlimited"
        }
        this.put(() -> o.method_42517(), s.entityDistancePercent / 100.0);
        if (s.renderDistance > 0) this.put(() -> o.method_42503(), s.renderDistance);
        if (s.simulationDistance > 0) this.put(() -> o.method_42510(), s.simulationDistance);
        Hud hud = Hud.getInstance();
        if (s.noHudBlur && hud != null) {
            this.hudBlur = hud.disableBlurSetting.isValue();
            this.hudShadows = hud.disableShadowsSetting.isValue();
            this.hudOutline = hud.disableOutlineSetting.isValue();
            hud.disableBlurSetting.setValue(true);
            hud.disableShadowsSetting.setValue(true);
            hud.disableOutlineSetting.setValue(true);   // blur + shadows + outline off = the client's cheap "simple panels" path
            this.hudTouched = true;
        }
        if (s.disableHeavyModules) {
            for (String name : HEAVY_MODULES) {
                Module module;
                try {
                    module = Instance.get(name);
                } catch (Throwable ignored) {
                    continue;
                }
                if (module == null || module == s.owner || !module.isState()) continue;
                module.setState(false);
                this.switchedOff.add(module);
            }
        }
        this.signature = s.signature();
    }

    public void restore() {
        if (!this.applied) {
            return;
        }
        for (Map.Entry<class_7172<?>, Object> e : this.saved.entrySet()) {
            PerformancePreset.setRaw(e.getKey(), e.getValue());
        }
        this.saved.clear();
        Hud hud = Hud.getInstance();
        if (this.hudTouched && hud != null) {
            hud.disableBlurSetting.setValue(this.hudBlur);
            hud.disableShadowsSetting.setValue(this.hudShadows);
            hud.disableOutlineSetting.setValue(this.hudOutline);
        }
        this.hudTouched = false;
        for (Module module : this.switchedOff) {
            if (!module.isState()) {
                module.setState(true);
            }
        }
        this.switchedOff.clear();
        this.applied = false;
        this.signature = "";
    }

    private interface OptionGetter {
        class_7172<?> get();
    }

    /** Saves the option's current value (once) and sets the new one; a missing or rejecting option is skipped. */
    private void put(OptionGetter getter, Object value) {
        try {
            class_7172<?> option = getter.get();
            if (option == null) {
                return;
            }
            Object old = option.method_41753();
            if (old != null && old.equals(value)) {
                return;   // already like that: nothing to change or to restore
            }
            this.saved.putIfAbsent(option, old);
            PerformancePreset.setRaw(option, value);
        } catch (Throwable ignored) {
            // one rejected option must not block the rest of the preset
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void setRaw(class_7172 option, Object value) {
        try {
            option.method_41748(value);
        } catch (Throwable ignored) {
        }
    }
}
