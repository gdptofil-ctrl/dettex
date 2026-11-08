package ru.recoin.mixins;

import net.minecraft.class_10197;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import ru.recoin.implement.features.modules.render.hitcolor.HitTracker;

/** Multiplies the colour of every armor layer drawn while HitColorArmorMixin set a tint. */
@Mixin(value={class_10197.class})
public class HitColorEquipment {
    @ModifyArg(method={"method_64078"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_11785;method_73490(Lnet/minecraft/class_3879;Ljava/lang/Object;Lnet/minecraft/class_4587;Lnet/minecraft/class_1921;IIILnet/minecraft/class_1058;ILnet/minecraft/class_11683$class_11792;)V"), index=6, require=0)
    private int recoin$armorLayerTint(int color) {
        int tint = HitTracker.armorTint;
        return tint == 0 ? color : HitTracker.multiply(color, tint);
    }
}
