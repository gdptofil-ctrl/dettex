package ru.recoin.mixins;

import net.minecraft.class_2394;
import net.minecraft.class_702;
import net.minecraft.class_703;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.recoin.implement.features.modules.render.perf.RenderCulling;

/** Optimization: particles far from the camera are not spawned at all. */
@Mixin(value={class_702.class})
public class OptimizationParticleMixin {
    @Inject(method={"method_3056(Lnet/minecraft/class_2394;DDDDDD)Lnet/minecraft/class_703;"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void recoin$cullParticle(class_2394 effect, double x, double y, double z, double vx, double vy, double vz, CallbackInfoReturnable<class_703> cir) {
        if (RenderCulling.skipParticle(x, y, z)) {
            cir.setReturnValue(null);
        }
    }
}
