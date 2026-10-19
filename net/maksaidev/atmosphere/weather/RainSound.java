package net.maksaidev.atmosphere.weather;

import net.minecraft.class_1101;
import net.minecraft.class_1113;
import net.minecraft.class_2960;
import net.minecraft.class_3414;
import net.minecraft.class_3419;

public final class RainSound
extends class_1101 {
    RainSound() {
        super(class_3414.method_47908(class_2960.method_60655("maksatmosphere", "rain_continuous")), class_3419.field_15252, class_1113.method_43221());
        this.field_5446 = true;
        this.field_5451 = 0;
        this.field_5442 = 0.01f;
        this.field_18936 = true;
    }

    public void stopSound() {
        this.method_24876();
    }

    public void method_16896() {
        WeatherConfig config = WeatherModule.config();
        if (config != null && config.enabled && config.mode == WeatherConfig.Mode.RAIN) {
            float target = (float)config.rainSound / 100.0f * ((float)config.rainIntensity / 100.0f);
            this.field_5442 = Math.max(0.001f, target);
        } else {
            this.method_24876();
        }
    }
}
