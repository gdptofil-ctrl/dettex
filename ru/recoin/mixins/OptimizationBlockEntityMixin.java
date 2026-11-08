package ru.recoin.mixins;

import net.minecraft.class_11683;
import net.minecraft.class_11954;
import net.minecraft.class_2586;
import net.minecraft.class_824;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.recoin.implement.features.modules.render.perf.RenderCulling;

/** Optimization: chests, signs, banners, heads... hidden behind walls or too far away are not drawn. */
@Mixin(value={class_824.class})
public class OptimizationBlockEntityMixin {
    @Inject(method={"method_74348(Lnet/minecraft/class_2586;FLnet/minecraft/class_11683$class_11792;)Lnet/minecraft/class_11954;"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void recoin$cullBlockEntity(class_2586 blockEntity, float tickDelta, class_11683.class_11792 crumbling, CallbackInfoReturnable<class_11954> cir) {
        if (RenderCulling.hideBlockEntity(blockEntity)) {
            cir.setReturnValue(null);
        }
    }
}
