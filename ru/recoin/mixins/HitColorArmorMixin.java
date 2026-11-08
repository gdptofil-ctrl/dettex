package ru.recoin.mixins;

import net.minecraft.class_10034;
import net.minecraft.class_11659;
import net.minecraft.class_4587;
import net.minecraft.class_970;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.implement.features.modules.render.hitcolor.HitColor;
import ru.recoin.implement.features.modules.render.hitcolor.HitTracker;
import ru.recoin.implement.features.modules.render.hitcolor.IHitColorState;

/** Hit Color, mode Body + Armor: the armor of the hit entity is tinted too. */
@Mixin(value={class_970.class})
public class HitColorArmorMixin {
    @Inject(method={"method_17157(Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;ILnet/minecraft/class_10034;FF)V"}, at={@At(value="HEAD")}, require=0)
    private void recoin$armorTintBegin(class_4587 matrices, class_11659 queue, int light, class_10034 state, float limbAngle, float limbDistance, CallbackInfo ci) {
        int tint = state instanceof IHitColorState s ? s.recoin$getHitTint() : 0;
        HitTracker.armorTint = tint != 0 && HitColor.paintArmor() ? tint : 0;
    }

    @Inject(method={"method_17157(Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;ILnet/minecraft/class_10034;FF)V"}, at={@At(value="RETURN")}, require=0)
    private void recoin$armorTintEnd(class_4587 matrices, class_11659 queue, int light, class_10034 state, float limbAngle, float limbDistance, CallbackInfo ci) {
        HitTracker.armorTint = 0;
    }
}
