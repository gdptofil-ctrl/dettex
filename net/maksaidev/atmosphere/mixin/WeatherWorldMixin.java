package net.maksaidev.atmosphere.mixin;

import net.maksaidev.atmosphere.weather.WeatherConfig;
import net.maksaidev.atmosphere.weather.WeatherModule;
import net.minecraft.class_1937;
import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = class_1937.class)
public abstract class WeatherWorldMixin {
    @Inject(method = "method_8430", at = @At("HEAD"), cancellable = true)
    private void maksatmosphere$visualRain(float delta, CallbackInfoReturnable<Float> cir) {
        WeatherConfig c;
        if ((Object)this instanceof class_638 && (c = WeatherModule.config()) != null && c.enabled && c.mode != WeatherConfig.Mode.OFF) {
            cir.setReturnValue(c.mode == WeatherConfig.Mode.RAIN ? (float)c.rainIntensity / 100.0f : 0.0f);
        }
    }
}
