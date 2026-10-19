package net.maksaidev.atmosphere.mixin;

import net.maksaidev.atmosphere.weather.WeatherConfig;
import net.maksaidev.atmosphere.weather.WeatherModule;
import net.minecraft.class_9976;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = class_9976.class)
public abstract class WeatherRenderingMixin {
    @Inject(method = {"method_62320", "method_62319"}, at = @At("HEAD"), cancellable = true)
    private void maksatmosphere$replacePrecipitation(CallbackInfo ci) {
        WeatherConfig c = WeatherModule.config();
        if (c != null && c.enabled && c.mode != WeatherConfig.Mode.OFF) {
            ci.cancel();
        }
    }
}
