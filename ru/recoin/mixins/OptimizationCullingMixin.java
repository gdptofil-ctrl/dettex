package ru.recoin.mixins;

import net.minecraft.class_1297;
import net.minecraft.class_4604;
import net.minecraft.class_898;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.recoin.implement.features.modules.render.perf.RenderCulling;

/** Optimization: entities hidden behind walls or too far away are not drawn. */
@Mixin(value={class_898.class})
public class OptimizationCullingMixin {
    @Inject(method={"method_3950(Lnet/minecraft/class_1297;Lnet/minecraft/class_4604;DDD)Z"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void recoin$cullEntity(class_1297 entity, class_4604 frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (RenderCulling.hideEntity(entity)) {
            cir.setReturnValue(false);
        }
    }
}
