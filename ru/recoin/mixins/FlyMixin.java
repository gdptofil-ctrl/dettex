package ru.recoin.mixins;

import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.implement.features.modules.movement.FlyModule;

@Mixin(value={class_746.class})
public class FlyMixin {

    @Inject(method={"method_5773"}, at={@At(value="HEAD")})
    private void recoin$flyTick(CallbackInfo callbackInfo) {
        FlyModule module = FlyModule.getInstance();
        if (module != null && module.isState()) {
            module.onClientTick();
        }
    }
}
