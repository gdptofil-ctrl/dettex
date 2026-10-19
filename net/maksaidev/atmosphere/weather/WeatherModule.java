package net.maksaidev.atmosphere.weather;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WeatherModule {
    public static final Logger LOGGER = LoggerFactory.getLogger("maksatmosphere");
    private static final WeatherRenderer RENDERER = new WeatherRenderer();
    private static WeatherConfig config;
    private static RainSound sound;
    private static boolean wasWinter;

    private WeatherModule() {
    }

    public static void init() {
        config = WeatherConfig.get();
        WorldRenderEvents.BEFORE_ENTITIES.register(RENDERER::captureTerrain);
        WorldRenderEvents.END_MAIN.register(RENDERER::render);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> RENDERER.close());
    }

    public static WeatherConfig config() {
        return WeatherConfig.get();
    }

    public static void tick(class_310 client) {
        WeatherConfig c = WeatherModule.config();
        boolean winter = c.enabled && c.mode == WeatherConfig.Mode.WINTER;
        if (winter && !wasWinter) {
            for (class_2960 id : client.method_1483().method_4864()) {
                if (!WeatherModule.muteWinterAmbience(id)) continue;
                client.method_1483().method_4875(id, null);
            }
        }
        wasWinter = winter;
        boolean rain = client.field_1687 != null && client.field_1724 != null && c.enabled && c.mode == WeatherConfig.Mode.RAIN && c.rainIntensity > 0 && c.rainSound > 0;
        if (rain) {
            if (sound == null || sound.method_4793() || !client.method_1483().method_4877(sound)) {
                sound = new RainSound();
                client.method_1483().method_4873(sound);
            }
        } else if (sound != null) {
            sound.stopSound();
            sound = null;
        }
    }

    public static boolean muteWinterAmbience(class_2960 id) {
        WeatherConfig c = config;
        if (c == null || !c.enabled || c.mode != WeatherConfig.Mode.WINTER) {
            return false;
        }
        String path = id.method_12832();
        return id.method_12836().equals("minecraft") && (path.equals("ambient.cave") || path.startsWith("ambient.nether_wastes.") || path.startsWith("ambient.soul_sand_valley.") || path.startsWith("ambient.crimson_forest.") || path.startsWith("ambient.warped_forest.") || path.startsWith("ambient.basalt_deltas.") || path.startsWith("music.nether."));
    }
}
