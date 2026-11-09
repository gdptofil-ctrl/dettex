package ru.recoin.mixins;

import net.minecraft.class_2596;
import net.minecraft.class_8673;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.common.util.network.PacketTiming;
import ru.recoin.implement.features.modules.movement.FlyModule;

@Mixin(value={class_8673.class})
public class PacketTimingMixin {

    @Inject(method={"method_52787"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$packetTiming(class_2596<?> packet, CallbackInfo callbackInfo) {
        FlyModule module = FlyModule.getInstance();
        if (module == null || !module.isState()) {
            return;
        }
        if (PacketTiming.enqueue(packet)) {
            callbackInfo.cancel();
        }
    }
}
