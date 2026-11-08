package ru.recoin.implement.features.modules.render;

import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.GroupSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.modules.render.perf.PerformancePreset;
import ru.recoin.implement.features.modules.render.perf.RenderCulling;
import ru.recoin.implement.features.modules.render.perf.FrameProfiler;
import ru.recoin.implement.events.render.DrawEvent;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.common.util.render.MatrixStackCompat;
import java.util.List;
import net.minecraft.class_332;
import net.minecraft.class_4587;

/**
 * Real FPS optimization:
 * - Culling: entities and block entities hidden behind walls are not drawn, small entities, chests/signs
 *   and particles far away are skipped (the biggest win on servers with lots of mobs, items and bases);
 * - Game Settings: switches off the vanilla options that cost the most (see-through leaves, improved
 *   transparency, chunk fade, texture filtering, clouds, shadows...) and removes the FPS cap;
 * - Client: HUD blur/shadows and the heaviest visual modules; "Show Lag Sources" measures every module.
 * Hiding entities behind walls is skipped when the EntityCulling mod is installed (it already does that).
 * Everything changed is remembered and put back when the module is turned off.
 */
public final class Optimization
extends Module {
    // culling
    private final BooleanSetting entityCulling = new BooleanSetting("Entity Culling", "Don't draw mobs, items and other entities hidden behind blocks").setValue(true);
    private final BooleanSetting cullPlayers = new BooleanSetting("Cull Players", "Also hide players behind walls (off: players are always drawn)").setValue(false).visible(this.entityCulling::isValue);
    private final BooleanSetting blockEntityCulling = new BooleanSetting("Block Entity Culling", "Don't draw chests, signs, banners, heads hidden behind blocks").setValue(true);
    private final ValueSetting blockEntityDistance = new ValueSetting("Block Entity Distance", "Chests, signs, banners further than this are not drawn (0 = no limit)").setValue(48.0f).range(0, 128);
    private final ValueSetting smallEntityDistance = new ValueSetting("Small Entity Distance", "Dropped items, XP, armor stands, item frames further than this are not drawn (0 = no limit)").setValue(32.0f).range(0, 128);
    private final ValueSetting particleDistance = new ValueSetting("Particle Distance", "Particles further than this are not spawned (0 = no limit)").setValue(24.0f).range(0, 64);
    private final GroupSetting cullingGroup = new GroupSetting("Culling", "Skip what you can't see", false).settings(this.entityCulling, this.cullPlayers, this.blockEntityCulling, this.blockEntityDistance, this.smallEntityDistance, this.particleDistance);
    // vanilla options
    private final BooleanSetting unlimitedFps = new BooleanSetting("Unlimited FPS", "Unlimited max FPS and no VSync").setValue(true);
    private final BooleanSetting fastLeaves = new BooleanSetting("Fast Leaves", "Opaque leaves: a big win in forests").setValue(true);
    private final BooleanSetting noImprovedTransparency = new BooleanSetting("No Improved Transparency", "Turn off the expensive transparency pass").setValue(true);
    private final BooleanSetting threadedChunks = new BooleanSetting("Threaded Chunk Updates", "Chunk rebuilds never stall the frame").setValue(true);
    private final BooleanSetting noChunkFade = new BooleanSetting("No Chunk Fade", "New chunks appear at once, without fading in").setValue(true);
    private final BooleanSetting noTextureFiltering = new BooleanSetting("No Texture Filtering", "Turn off RGSS / anisotropic filtering").setValue(true);
    private final BooleanSetting minimalParticles = new BooleanSetting("Minimal Particles", "Particles: minimal").setValue(true);
    private final BooleanSetting noClouds = new BooleanSetting("No Clouds", "Turn clouds off").setValue(true);
    private final BooleanSetting noEntityShadows = new BooleanSetting("No Entity Shadows", "Turn off round shadows under entities").setValue(true);
    private final BooleanSetting noBiomeBlend = new BooleanSetting("No Biome Blend", "Faster chunk rebuilds").setValue(true);
    private final BooleanSetting lowWeather = new BooleanSetting("Low Weather Radius", "Rain and snow only close to you").setValue(true);
    private final BooleanSetting noVignette = new BooleanSetting("No Vignette", "Turn off the dark screen edges").setValue(true);
    private final BooleanSetting noMenuBlur = new BooleanSetting("No Menu Blur", "No blur behind menus and inventories").setValue(true);
    private final BooleanSetting noSmoothLighting = new BooleanSetting("No Smooth Lighting", "Turn off smooth lighting (changes the look)").setValue(false);
    private final ValueSetting entityDistance = new ValueSetting("Entity Distance %", "How far mobs and players are drawn").setValue(75.0f).range(50, 100);
    private final ValueSetting renderDistance = new ValueSetting("Render Distance", "Chunks drawn around you (0 = don't change)").setValue(8.0f).range(0, 32);
    private final ValueSetting simulationDistance = new ValueSetting("Simulation Distance", "Chunks simulated around you (0 = don't change)").setValue(5.0f).range(0, 32);
    private final GroupSetting gameGroup = new GroupSetting("Game Settings", "Vanilla options that cost the most FPS", false).settings(this.unlimitedFps, this.fastLeaves, this.noImprovedTransparency, this.threadedChunks, this.noChunkFade, this.noTextureFiltering, this.minimalParticles, this.noClouds, this.noEntityShadows, this.noBiomeBlend, this.lowWeather, this.noVignette, this.noMenuBlur, this.noSmoothLighting, this.entityDistance, this.renderDistance, this.simulationDistance);
    // client
    private final BooleanSetting noHudBlur = new BooleanSetting("No HUD Blur", "Blur and shadows of the client HUD cost a lot of FPS").setValue(true);
    private final BooleanSetting disableHeavyEffects = new BooleanSetting("Disable Heavy Effects", "MotionBlur, hand shaders, rain, winter, trails and other client effects").setValue(true);
    private final BooleanSetting showLagSources = new BooleanSetting("Show Lag Sources", "Shows on screen which client modules eat the most frame time (turn them off to gain FPS)").setValue(false);
    private final GroupSetting clientGroup = new GroupSetting("Client", "Heavy client visuals", false).settings(this.noHudBlur, this.disableHeavyEffects, this.showLagSources);
    private final PerformancePreset preset = new PerformancePreset();

    public Optimization() {
        super("Optimization", "Optimization", ModuleCategory.RENDER);
        this.setup(this.cullingGroup, this.gameGroup, this.clientGroup);
    }

    private PerformancePreset.Settings presetSettings() {
        PerformancePreset.Settings s = new PerformancePreset.Settings();
        s.owner = this;
        s.minimalParticles = this.minimalParticles.isValue();
        s.noClouds = this.noClouds.isValue();
        s.noEntityShadows = this.noEntityShadows.isValue();
        s.noBiomeBlend = this.noBiomeBlend.isValue();
        s.noSmoothLighting = this.noSmoothLighting.isValue();
        s.fastLeaves = this.fastLeaves.isValue();
        s.noImprovedTransparency = this.noImprovedTransparency.isValue();
        s.noChunkFade = this.noChunkFade.isValue();
        s.noVignette = this.noVignette.isValue();
        s.noTextureFiltering = this.noTextureFiltering.isValue();
        s.lowWeather = this.lowWeather.isValue();
        s.threadedChunks = this.threadedChunks.isValue();
        s.noMenuBlur = this.noMenuBlur.isValue();
        s.unlimitedFps = this.unlimitedFps.isValue();
        s.noHudBlur = this.noHudBlur.isValue();
        s.entityDistancePercent = this.entityDistance.getInt();
        s.renderDistance = this.renderDistance.getInt() >= 2 ? this.renderDistance.getInt() : -1;
        s.simulationDistance = this.simulationDistance.getInt() >= 5 ? this.simulationDistance.getInt() : -1;
        s.disableHeavyModules = this.disableHeavyEffects.isValue();
        return s;
    }

    private void applyCulling() {
        RenderCulling.entityOcclusion = this.entityCulling.isValue();
        RenderCulling.cullPlayers = this.cullPlayers.isValue();
        RenderCulling.blockEntityOcclusion = this.blockEntityCulling.isValue();
        RenderCulling.blockEntityDistance = this.blockEntityDistance.getInt();
        RenderCulling.smallEntityDistance = this.smallEntityDistance.getInt();
        RenderCulling.particleDistance = this.particleDistance.getInt();
        RenderCulling.enabled = true;
        boolean profile = this.showLagSources.isValue();
        if (FrameProfiler.enabled != profile) {
            FrameProfiler.reset();
            FrameProfiler.enabled = profile;
        }
    }

    @Override
    public void activate() {
        this.applyCulling();
        this.preset.apply(this.presetSettings());
    }

    @Override
    public void deactivate() {
        RenderCulling.enabled = false;
        RenderCulling.reset();
        FrameProfiler.enabled = false;
        FrameProfiler.reset();
        this.preset.restore();
    }

    @EventHandler
    public void onDraw(DrawEvent event) {
        if (!FrameProfiler.enabled) {
            return;
        }
        FrameProfiler.frame();
        List<String> lines = FrameProfiler.report();
        if (lines.isEmpty()) {
            lines = List.of("Measuring client modules...");
        }
        class_332 context = event.getDrawContext();
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        FontRenderer font = Fonts.getSize(13, Fonts.Type.INTER_BOLD);
        float y = 40.0f;
        float w = 0.0f;
        for (String line : lines) {
            w = Math.max(w, font.getStringWidth(line));
        }
        float h = font.getStringHeight("Ag") + 2.0f;
        context.method_25294(4, (int)y - 3, (int)(10 + w), (int)(y + h * lines.size() + 1), 0x90000000);
        for (int i = 0; i < lines.size(); ++i) {
            font.drawString(matrix, lines.get(i), 7.0f, y + i * h, i == 0 ? 0xFF7FD8FF : i == 1 ? 0xFFFF7A7A : 0xFFFFFFFF);
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (!this.isState()) {
            return;
        }
        this.applyCulling();
        // also covers a module enabled from the config before a world was joined, and settings edited while active
        PerformancePreset.Settings s = this.presetSettings();
        if (!this.preset.isApplied() || !s.signature().equals(this.preset.signature())) {
            this.preset.restore();
            this.preset.apply(s);
        }
    }
}
