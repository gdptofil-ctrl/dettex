package net.maksaidev.atmosphere.mixin;

import net.maksaidev.atmosphere.weather.WeatherConfig;
import net.maksaidev.atmosphere.weather.WeatherModule;
import net.minecraft.class_9975;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = class_9975.class)
public abstract class WeatherSkyMixin {
    @Inject(method = {"method_62307", "method_62306"}, at = @At("HEAD"), cancellable = true)
    private void maksatmosphere$customSun(CallbackInfo ci) {
        WeatherConfig c = WeatherModule.config();
        if (c != null && c.enabled && c.mode == WeatherConfig.Mode.SUNRISE) {
            ci.cancel();
        }
    }
}
