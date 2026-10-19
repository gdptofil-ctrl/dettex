package net.maksaidev.atmosphere.weather;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import net.fabricmc.loader.api.FabricLoader;

public final class WeatherConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("maksatmosphere-weather.json");
    private static WeatherConfig instance;
    public boolean enabled = false;
    public Mode mode = Mode.RAIN;
    public int rainIntensity = 90;
    public int rainSound = 60;
    public int puddles = 75;
    public boolean splashes = true;
    public int splashDensity = 75;
    public int ringVisibility = 100;
    public boolean wetGround = false;
    public int snowDensity = 100;
    public int wind = 30;
    public boolean snowCover = true;
    public int blizzard = 65;
    public Quality quality = Quality.BALANCED;
    public int sunlight = 75;
    public int sunRays = 65;
    public int sunHeight = 35;
    public int dew = 50;
    public int dustDensity = 65;
    public int dustSize = 50;
    public int dustLight = 70;
    public int sakuraDensity = 60;
    public int sakuraSize = 50;
    public int sakuraGroundLife = 50;
    public int sakuraWind = 30;
    public int sakuraHue = 96;
    public int sakuraSaturation = 45;
    public int sakuraFlutter = 55;
    public boolean sakuraTint = true;
    public int sakuraTintStrength = 60;
    public int eclipseDarkness = 70;
    public int eclipseCorona = 75;
    public int eclipseStars = 70;
    public int eclipseDust = 45;
    public int meteorRate = 60;
    public int meteorBrightness = 80;
    public int meteorTrail = 70;
    public int meteorStars = 70;
    public int meteorDebris = 50;
    public int meteorSize = 55;
    public int meteorVariation = 75;
    public int meteorProximity = 65;
    public int meteorFog = 60;
    public int meteorFogDistance = 45;
    public int meteorSkyGlow = 65;
    public int meteorNebula = 55;
    public int bloodMoonSize = 65;
    public int bloodMoonGlow = 70;
    public int bloodSky = 75;
    public int bloodClouds = 65;
    public int bloodMist = 55;
    public int bloodGround = 45;
    public int bloodEmbers = 35;
    public int bloodHeight = 58;

    public static WeatherConfig get() {
        if (instance == null) {
            instance = WeatherConfig.load();
        }
        return instance;
    }

    public static WeatherConfig load() {
        if (!Files.exists(FILE, new LinkOption[0])) {
            return new WeatherConfig();
        }
        try (BufferedReader reader = Files.newBufferedReader(FILE);) {
            WeatherConfig config = (WeatherConfig)GSON.fromJson((Reader)reader, WeatherConfig.class);
            if (config == null) {
                WeatherConfig weatherConfig2 = new WeatherConfig();
                return weatherConfig2;
            }
            config.validate();
            WeatherConfig weatherConfig = config;
            return weatherConfig;
        }
        catch (Exception ex) {
            WeatherModule.LOGGER.warn("[MaksaiAtmosphere] \u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u0440\u043e\u0447\u0438\u0442\u0430\u0442\u044c maksatmosphere-weather.json", (Throwable)ex);
            return new WeatherConfig();
        }
    }

    public static void save() {
        WeatherConfig config = WeatherConfig.get();
        config.validate();
        try {
            Files.createDirectories(FILE.getParent(), new FileAttribute[0]);
            try (BufferedWriter writer = Files.newBufferedWriter(FILE, new OpenOption[0]);) {
                GSON.toJson((Object)config, (Appendable)writer);
            }
        }
        catch (IOException ex) {
            WeatherModule.LOGGER.warn("[MaksaiAtmosphere] \u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c maksatmosphere-weather.json", (Throwable)ex);
        }
    }

    private void validate() {
        if (this.mode == null) {
            this.mode = Mode.OFF;
        }
        if (this.quality == null) {
            this.quality = Quality.BALANCED;
        }
        this.rainIntensity = WeatherConfig.clamp(this.rainIntensity);
        this.rainSound = WeatherConfig.clamp(this.rainSound);
        this.puddles = WeatherConfig.clamp(this.puddles);
        this.splashDensity = WeatherConfig.clamp(this.splashDensity);
        this.ringVisibility = WeatherConfig.clamp(this.ringVisibility);
        this.snowDensity = WeatherConfig.clamp(this.snowDensity);
        this.wind = WeatherConfig.clamp(this.wind);
        this.blizzard = WeatherConfig.clamp(this.blizzard);
        this.sunlight = WeatherConfig.clamp(this.sunlight);
        this.sunRays = WeatherConfig.clamp(this.sunRays);
        this.sunHeight = WeatherConfig.clamp(this.sunHeight);
        this.dew = WeatherConfig.clamp(this.dew);
        this.dustDensity = WeatherConfig.clamp(this.dustDensity);
        this.dustSize = WeatherConfig.clamp(this.dustSize);
        this.dustLight = WeatherConfig.clamp(this.dustLight);
        this.sakuraDensity = WeatherConfig.clamp(this.sakuraDensity);
        this.sakuraSize = WeatherConfig.clamp(this.sakuraSize);
        this.sakuraGroundLife = WeatherConfig.clamp(this.sakuraGroundLife);
        this.sakuraWind = WeatherConfig.clamp(this.sakuraWind);
        this.sakuraHue = WeatherConfig.clamp(this.sakuraHue);
        this.sakuraSaturation = WeatherConfig.clamp(this.sakuraSaturation);
        this.sakuraFlutter = WeatherConfig.clamp(this.sakuraFlutter);
        this.sakuraTintStrength = WeatherConfig.clamp(this.sakuraTintStrength);
        this.eclipseDarkness = WeatherConfig.clamp(this.eclipseDarkness);
        this.eclipseCorona = WeatherConfig.clamp(this.eclipseCorona);
        this.eclipseStars = WeatherConfig.clamp(this.eclipseStars);
        this.eclipseDust = WeatherConfig.clamp(this.eclipseDust);
        this.meteorRate = WeatherConfig.clamp(this.meteorRate);
        this.meteorBrightness = WeatherConfig.clamp(this.meteorBrightness);
        this.meteorTrail = WeatherConfig.clamp(this.meteorTrail);
        this.meteorStars = WeatherConfig.clamp(this.meteorStars);
        this.meteorDebris = WeatherConfig.clamp(this.meteorDebris);
        this.meteorSize = WeatherConfig.clamp(this.meteorSize);
        this.meteorVariation = WeatherConfig.clamp(this.meteorVariation);
        this.meteorProximity = WeatherConfig.clamp(this.meteorProximity);
        this.meteorFog = WeatherConfig.clamp(this.meteorFog);
        this.meteorFogDistance = WeatherConfig.clamp(this.meteorFogDistance);
        this.meteorSkyGlow = WeatherConfig.clamp(this.meteorSkyGlow);
        this.meteorNebula = WeatherConfig.clamp(this.meteorNebula);
        this.bloodMoonSize = WeatherConfig.clamp(this.bloodMoonSize);
        this.bloodMoonGlow = WeatherConfig.clamp(this.bloodMoonGlow);
        this.bloodSky = WeatherConfig.clamp(this.bloodSky);
        this.bloodClouds = WeatherConfig.clamp(this.bloodClouds);
        this.bloodMist = WeatherConfig.clamp(this.bloodMist);
        this.bloodGround = WeatherConfig.clamp(this.bloodGround);
        this.bloodEmbers = WeatherConfig.clamp(this.bloodEmbers);
        this.bloodHeight = WeatherConfig.clamp(this.bloodHeight);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    public static String modeName(Mode mode) {
        return switch (mode) {
            case OFF -> "\u0412\u044b\u043a\u043b";
            case RAIN -> "\u0414\u043e\u0436\u0434\u044c";
            case WINTER -> "\u0417\u0438\u043c\u0430";
            case SUNRISE -> "\u0420\u0430\u0441\u0441\u0432\u0435\u0442";
            case DUST -> "\u041f\u044b\u043b\u044c";
            case SAKURA -> "\u0421\u0430\u043a\u0443\u0440\u0430";
            case ECLIPSE -> "\u0417\u0430\u0442\u043c\u0435\u043d\u0438\u0435";
            case METEOR -> "\u0417\u0432\u0451\u0437\u0434\u043d\u044b\u0439 \u0434\u043e\u0436\u0434\u044c";
            case BLOOD_MOON -> "\u041a\u0440\u043e\u0432\u0430\u0432\u0430\u044f \u043b\u0443\u043d\u0430";
        };
    }

    public static String qualityName(Quality quality) {
        return switch (quality) {
            case FAST -> "\u0411\u044b\u0441\u0442\u0440\u043e";
            case BALANCED -> "\u0411\u0430\u043b\u0430\u043d\u0441";
            case HIGH -> "\u0412\u044b\u0441\u043e\u043a\u043e\u0435";
        };
    }

    public static int modeColor(Mode mode) {
        return switch (mode) {
            case OFF -> 0x808890;
            case RAIN -> 7244960;
            case WINTER -> 14477556;
            case SUNRISE -> 15769680;
            case DUST -> 12623968;
            case SAKURA -> 15769784;
            case ECLIPSE -> 7360688;
            case METEOR -> 15761456;
            case BLOOD_MOON -> 12593208;
        };
    }

    public static enum Mode {
        OFF,
        RAIN,
        WINTER,
        SUNRISE,
        DUST,
        SAKURA,
        ECLIPSE,
        METEOR,
        BLOOD_MOON;

        public Mode next() {
            return Mode.values()[(this.ordinal() + 1) % Mode.values().length];
        }
    }

    public static enum Quality {
        FAST,
        BALANCED,
        HIGH;

        public Quality next() {
            return Quality.values()[(this.ordinal() + 1) % Quality.values().length];
        }
    }
}
