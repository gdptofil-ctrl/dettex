package ru.recoin.implement.features.modules.render;

import net.maksaidev.atmosphere.weather.WeatherConfig;
import net.maksaidev.atmosphere.weather.WeatherModule;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;

public final class Weather
extends Module {
    public Weather() {
        super("Weather", "Weather", ModuleCategory.RENDER);
    }

    @Override
    public void activate() {
        WeatherConfig config = WeatherModule.config();
        if (config != null) {
            config.enabled = true;
            if (config.mode == WeatherConfig.Mode.OFF) {
                config.mode = WeatherConfig.Mode.RAIN;
            }
        }
    }

    @Override
    public void deactivate() {
        WeatherConfig config = WeatherModule.config();
        if (config != null) {
            config.enabled = false;
        }
    }
}
