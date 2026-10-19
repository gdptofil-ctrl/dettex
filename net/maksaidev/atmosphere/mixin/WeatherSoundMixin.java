package net.maksaidev.atmosphere.mixin;

import net.maksaidev.atmosphere.weather.WeatherModule;
import net.minecraft.class_1113;
import net.minecraft.class_1140;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = class_1140.class)
public abstract class WeatherSoundMixin {
    @Inject(method = "method_4854(Lnet/minecraft/class_1113;)Lnet/minecraft/class_1140$class_11518;", at = @At("HEAD"), cancellable = true)
    private void maksatmosphere$muteWinterAmbience(class_1113 sound, CallbackInfoReturnable<class_1140.class_11518> cir) {
        if (WeatherModule.muteWinterAmbience(sound.method_4775())) {
            cir.setReturnValue(class_1140.class_11518.field_60956);
        }
    }
}
