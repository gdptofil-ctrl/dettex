package ru.recoin.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.class_10042;
import net.minecraft.class_1309;
import net.minecraft.class_4608;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.implement.features.modules.render.hitcolor.HitColor;
import ru.recoin.implement.features.modules.render.hitcolor.HitTracker;
import ru.recoin.implement.features.modules.render.hitcolor.IHitColorState;

/** Hit Color: tints the body of a hit entity and optionally removes the vanilla red flash. */
@Mixin(value={class_922.class})
public class HitColorRendererMixin {
    @Inject(method={"method_62355(Lnet/minecraft/class_1309;Lnet/minecraft/class_10042;F)V"}, at={@At(value="TAIL")}, require=0)
    private void recoin$storeHitTint(class_1309 entity, class_10042 state, float delta, CallbackInfo ci) {
        ((IHitColorState)state).recoin$setHitTint(HitColor.tintFor(entity));
    }

    @ModifyArg(method={"method_4054(Lnet/minecraft/class_10042;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_12075;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_11659;method_73490(Lnet/minecraft/class_3879;Ljava/lang/Object;Lnet/minecraft/class_4587;Lnet/minecraft/class_1921;IIILnet/minecraft/class_1058;ILnet/minecraft/class_11683$class_11792;)V"), index=6, require=0)
    private int recoin$hitTintBody(int color, @Local(argsOnly=true) class_10042 state) {
        int tint = ((IHitColorState)state).recoin$getHitTint();
        return tint == 0 ? color : HitTracker.multiply(color, tint);
    }

    @ModifyArg(method={"method_4054(Lnet/minecraft/class_10042;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_12075;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_11659;method_73490(Lnet/minecraft/class_3879;Ljava/lang/Object;Lnet/minecraft/class_4587;Lnet/minecraft/class_1921;IIILnet/minecraft/class_1058;ILnet/minecraft/class_11683$class_11792;)V"), index=5, require=0)
    private int recoin$hideVanillaRed(int overlay, @Local(argsOnly=true) class_10042 state) {
        if (((IHitColorState)state).recoin$getHitTint() != 0 && HitColor.hideVanillaRed()) {
            return class_4608.field_21444;
        }
        return overlay;
    }
}
