package ru.recoin.implement.features.modules.render;

import java.util.Random;
import net.minecraft.class_2394;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_4184;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_7923;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_3414;
import net.minecraft.class_3419;
import net.minecraft.class_5819;
import ru.recoin.implement.events.render.WorldRenderEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.ColorSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.render.pipeline.AtmospherePipeline;
import ru.recoin.implement.events.player.TickEvent;

/**
 * Seasonal atmosphere: a single full-screen pass (haze, color grade, god rays from the real sun,
 * vignette, snow cover) plus ambient particles spawned through the vanilla particle engine.
 * Rewritten: the decompiled particle renderer threw every frame and the shader time never advanced.
 */
public final class Atmosphere
extends Module {
    public final SelectSetting mode = new SelectSetting("Mode", "Lighting and particle preset").value("Dawn", "Day", "Dusk", "Night", "Rain", "Storm", "Autumn", "Dust", "Snow", "Ash").selected("Snow");
    public final ValueSetting density = Atmosphere.number("Density", "Haze density", 1.0f, 3.0f);
    public final ValueSetting scatterHeight = Atmosphere.number("Scatter Height", "Light scattering height", 64.0f, 320.0f);
    public final ValueSetting godRays = Atmosphere.number("God Rays", "Sun ray strength (0 = off)", 1.0f, 3.0f);
    public final ValueSetting softness = Atmosphere.number("Softness", "Haze and light softness", 1.0f, 3.0f);
    public final ColorSetting lightColor = new ColorSetting("Light Color", "Lighting tint").setColor(-1);
    public final ValueSetting amount = Atmosphere.number("Strength", "Ground snow strength and particle amount", 1.0f, 3.0f);
    public final ValueSetting speed = Atmosphere.number("Speed", "Haze movement speed", 1.0f, 5.0f);
    public final ValueSetting haze = Atmosphere.number("Haze", "Distant haze strength", 1.0f, 3.0f);
    public final ValueSetting grade = Atmosphere.number("Color Grade", "Seasonal color grading strength", 1.0f, 3.0f);
    public final ValueSetting vignette = Atmosphere.number("Vignette", "Darkened screen edges", 1.0f, 3.0f);
    public final BooleanSetting particles = new BooleanSetting("Particles", "Snow, ash, dust or fireflies around you").setValue(true);
    public final BooleanSetting rainSound = new BooleanSetting("Rain Sound", "Sound of rain and thunder in Rain/Storm modes").setValue(true);
    private static final AtmospherePipeline PIPELINE = new AtmospherePipeline();
    private static final int MAX_PARTICLES_PER_TICK = 24;
    private final Matrix4f projectionMatrix = new Matrix4f();
    private final Matrix4f inverseProjection = new Matrix4f();
    private final Matrix4f cameraRotation = new Matrix4f();
    private final Vector3f cameraPosition = new Vector3f();
    private final Random random = new Random();
    private boolean projectionValid;
    private long lastFrameNanos;
    private float elapsed;
    private String particleMode;
    private class_2394 particleA;
    private class_2394 particleB;

    public Atmosphere() {
        super("Atmosphere", "Atmosphere", ModuleCategory.RENDER);
        this.setup(this.mode, this.particles, this.rainSound, this.density, this.scatterHeight, this.godRays, this.softness, this.lightColor, this.amount, this.speed, this.haze, this.grade, this.vignette);
    }

    private static ValueSetting number(String name, String description, float value, float maximum) {
        return new ValueSetting(name, description).setValue(value).range(0.0f, maximum).step(0.05f);
    }

    public static Atmosphere getInstance() {
        return Instance.get(Atmosphere.class);
    }

    public int preset() {
        return switch (this.mode.getSelected()) {
            case "Dawn" -> 0;
            case "Day" -> 1;
            case "Dusk" -> 2;
            case "Night" -> 3;
            case "Rain" -> 9;
            case "Storm" -> 10;
            case "Autumn" -> 7;
            case "Dust" -> 6;
            case "Ash" -> 8;
            default -> 5;
        };
    }

    public long timeOfDay() {
        return switch (this.mode.getSelected()) {
            case "Dawn" -> 23000L;
            case "Dusk" -> 12000L;
            case "Night" -> 18000L;
            default -> 6000L;
        };
    }

    private int atmosphereColor() {
        int color = switch (this.preset()) {
            case 0 -> -12315;
            case 2 -> -1531300;
            case 3 -> -6508837;
            case 6 -> -2702684;
            case 8 -> -4604218;
            case 7 -> 0xFFFFBE82;
            case 9 -> 0xFF9AA8B8;
            case 10 -> 0xFF6A7488;
            default -> -2759952;
        };
        int tint = this.lightColor.getColor();
        int red = (color >>> 16 & 0xFF) * (tint >>> 16 & 0xFF) / 255;
        int green = (color >>> 8 & 0xFF) * (tint >>> 8 & 0xFF) / 255;
        int blue = (color & 0xFF) * (tint & 0xFF) / 255;
        return 0xFF000000 | red << 16 | green << 8 | blue;
    }

    /** Vanilla sky angle for the forced time of day, as a world-space sun direction (x, y). */
    private float[] sunDirection() {
        double fraction = (double)this.timeOfDay() / 24000.0 - 0.25;
        fraction -= Math.floor(fraction);
        double eased = 0.5 - Math.cos(fraction * Math.PI) / 2.0;
        double skyAngle = (fraction * 2.0 + eased) / 3.0;
        double radians = skyAngle * Math.PI * 2.0;
        return new float[]{(float)(-Math.sin(radians)), (float)Math.cos(radians)};
    }

    @Override
    public void activate() {
        this.projectionValid = false;
        this.lastFrameNanos = 0L;
        this.particleMode = null;
    }

    @Override
    public void deactivate() {
        this.projectionValid = false;
        this.lastFrameNanos = 0L;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        class_638 world = Atmosphere.mc.field_1687;
        class_746 player = Atmosphere.mc.field_1724;
        if (world == null || player == null || !this.particles.isValue() || Atmosphere.mc.method_1493()) {
            return;
        }
        if (!this.mode.getSelected().equals(this.particleMode)) {
            this.particleMode = this.mode.getSelected();
            this.resolveParticles();
        }
        this.tickRainSound(player);
        if (this.particleA == null) {
            return;
        }
        int count = Math.min(MAX_PARTICLES_PER_TICK, Math.round(this.baseRate() * this.amount.getValue()));
        class_243 pos = player.method_73189();
        for (int i = 0; i < count; ++i) {
            class_2394 effect = this.particleB != null && this.random.nextBoolean() ? this.particleB : this.particleA;
            double radius = this.falling() ? 16.0 : 8.0;
            double x = pos.field_1352 + (this.random.nextDouble() * 2.0 - 1.0) * radius;
            double z = pos.field_1350 + (this.random.nextDouble() * 2.0 - 1.0) * radius;
            double y = this.falling() ? pos.field_1351 + 6.0 + this.random.nextDouble() * 10.0 : pos.field_1351 + this.random.nextDouble() * 4.0 - 0.5;
            double drift = (this.random.nextDouble() - 0.5) * 0.02;
            world.method_8406(effect, x, y, z, drift, this.falling() ? -0.02 : 0.0, drift);
        }
    }

    private boolean raining() {
        String m = this.mode.getSelected();
        return "Rain".equals(m) || "Storm".equals(m);
    }

    private int soundTimer;
    private int thunderIn = -1;
    private float lastFlash;

    /** Vanilla rain and thunder sounds around the player while a Rain/Storm mode is on. */
    private void tickRainSound(class_746 player) {
        if (!this.raining() || !this.rainSound.isValue() || Atmosphere.mc.field_1687.method_8419()) {
            return;   // the world's own rain already makes the sound
        }
        boolean storm = "Storm".equals(this.mode.getSelected());
        if (--this.soundTimer <= 0) {
            this.soundTimer = 2 + this.random.nextInt(4);
            class_243 pos = player.method_73189();
            Atmosphere.playSound("weather.rain", pos.field_1352 + this.random.nextGaussian() * 4.0, pos.field_1351 + 1.0, pos.field_1350 + this.random.nextGaussian() * 4.0, (storm ? 0.28f : 0.18f) * Math.min(1.5f, this.amount.getValue()), 0.9f + this.random.nextFloat() * 0.2f);
        }
        Rain rain = Rain.getInstance();
        float flash = rain == null ? 0.0f : rain.getFlash();
        if (storm && flash > 0.5f && this.lastFlash <= 0.5f) {
            this.thunderIn = 8 + this.random.nextInt(30);   // thunder comes a moment after the flash
        }
        this.lastFlash = flash;
        if (this.thunderIn >= 0 && this.thunderIn-- == 0) {
            class_243 pos = player.method_73189();
            Atmosphere.playSound("entity.lightning_bolt.thunder", pos.field_1352, pos.field_1351 + 10.0, pos.field_1350, 0.7f, 0.75f + this.random.nextFloat() * 0.25f);
        }
    }

    private static class_3419 weatherCategory;

    private static void playSound(String id, double x, double y, double z, float volume, float pitch) {
        if (weatherCategory == null) {
            for (class_3419 category : class_3419.values()) {
                if ("weather".equals(category.method_14840())) {
                    weatherCategory = category;
                }
            }
            if (weatherCategory == null) {
                weatherCategory = class_3419.values()[0];
            }
        }
        class_3414 event = class_3414.method_47908((class_2960)class_2960.method_60655((String)"minecraft", (String)id));
        Atmosphere.mc.method_1483().method_4873((class_1113)new class_1109(event, weatherCategory, volume, pitch, class_5819.method_43047(), x, y, z));
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        Rain rain = Rain.getInstance();
        if (rain == null) {
            return;
        }
        if (this.raining() && this.particles.isValue()) {
            boolean storm = "Storm".equals(this.mode.getSelected());
            rain.renderForAtmosphere(event, storm, this.amount.getValue() * (storm ? 1.3f : 1.0f));
            this.rainWasOn = true;
        } else if (this.rainWasOn) {
            this.rainWasOn = false;
            rain.clearForAtmosphere();
        }
    }

    private boolean rainWasOn;

    private boolean falling() {
        String m = this.mode.getSelected();
        return "Snow".equals(m) || "Ash".equals(m) || "Dust".equals(m);
    }

    private float baseRate() {
        return switch (this.mode.getSelected()) {
            case "Snow" -> 10.0f;
            case "Ash", "Dust" -> 7.0f;
            case "Night" -> 1.5f;
            default -> 2.0f;
        };
    }

    private void resolveParticles() {
        String[] ids = switch (this.mode.getSelected()) {
            case "Snow" -> new String[]{"minecraft:snowflake", null};
            case "Ash" -> new String[]{"minecraft:ash", "minecraft:white_ash"};
            case "Dust" -> new String[]{"minecraft:white_ash", "minecraft:mycelium"};
            case "Night" -> new String[]{"minecraft:firefly", "minecraft:end_rod"};
            case "Autumn" -> new String[]{"minecraft:pale_oak_leaves", "minecraft:cherry_leaves"};
            case "Rain", "Storm" -> new String[]{null, null};   // drawn by the rain renderer
            case "Dawn" -> new String[]{"minecraft:cherry_leaves", null};
            case "Dusk" -> new String[]{"minecraft:spore_blossom_air", null};
            default -> new String[]{"minecraft:spore_blossom_air", null};
        };
        this.particleA = Atmosphere.particle(ids[0]);
        this.particleB = Atmosphere.particle(ids[1]);
    }

    private static class_2394 particle(String id) {
        if (id == null) {
            return null;
        }
        try {
            Object type = class_7923.field_41180.method_63535(class_2960.method_60654((String)id));
            return type instanceof class_2394 ? (class_2394)type : null;
        }
        catch (Throwable throwable) {
            return null;
        }
    }

    private float wetness() {
        return switch (this.mode.getSelected()) {
            case "Rain" -> Math.min(1.0f, 0.55f + this.amount.getValue() * 0.3f);
            case "Storm" -> 1.0f;
            default -> 0.0f;
        };
    }

    private float flash() {
        Rain rain = Rain.getInstance();
        return "Storm".equals(this.mode.getSelected()) && rain != null ? rain.getFlash() : 0.0f;
    }

    public static void render(Matrix4f projection) {
        Atmosphere module = Atmosphere.getInstance();
        if (module == null || !module.isState() || projection == null || Atmosphere.mc.field_1687 == null || Atmosphere.mc.field_1773.method_35765()) {
            return;
        }
        long now = System.nanoTime();
        if (module.lastFrameNanos != 0L) {
            float dt = Math.min(0.1f, (float)(now - module.lastFrameNanos) / 1.0E9f);
            module.elapsed = (module.elapsed + dt * Math.max(0.05f, module.speed.getValue())) % 100000.0f;
        }
        module.lastFrameNanos = now;
        boolean snowCover = module.preset() == 5 && module.amount.getValue() > 0.0f;
        boolean anything = snowCover || module.wetness() > 0.0f || module.density.getValue() > 0.0f && module.haze.getValue() > 0.0f || module.godRays.getValue() > 0.0f || module.grade.getValue() > 0.0f || module.vignette.getValue() > 0.0f;
        if (!anything) {
            return;   // every effect is at 0: skip the full-screen pass entirely
        }
        class_4184 camera = Atmosphere.mc.field_1773.method_19418();
        if (camera == null) {
            return;
        }
        if (!module.projectionValid || !module.projectionMatrix.equals((Object)projection)) {
            module.projectionMatrix.set((Matrix4fc)projection);
            module.inverseProjection.set((Matrix4fc)projection).invert();
            module.projectionValid = true;
        }
        class_243 position = camera.method_71156();
        module.cameraRotation.rotation((Quaternionfc)camera.method_23767());
        module.cameraPosition.set((float)position.field_1352, (float)position.field_1351, (float)position.field_1350);
        float[] sun = module.sunDirection();
        PIPELINE.draw(projection, module.inverseProjection, module.cameraRotation, module.cameraPosition, module.elapsed, module.preset(), module.density.getValue(), module.scatterHeight.getValue(), module.godRays.getValue(), module.softness.getValue(), module.amount.getValue(), module.speed.getValue(), module.haze.getValue(), module.grade.getValue(), module.vignette.getValue(), module.atmosphereColor(), sun[0], sun[1], module.wetness(), module.flash());
    }
}
