package ru.recoin.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.class_12155;
import net.minecraft.class_1297;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import ru.recoin.implement.features.modules.render.hitcolor.HitColorHitbox;

/** Hit Color Hitbox for the vanilla F3+B hitboxes. */
@Mixin(value={class_12155.class})
public class HitColorHitboxMixin {
    @ModifyArg(method={"method_75432"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_12179;method_75535(I)Lnet/minecraft/class_12179;", ordinal=0), index=0, require=0)
    private int recoin$hitboxColor(int color, @Local(argsOnly=true) class_1297 entity) {
        return HitColorHitbox.colorFor(entity, color);
    }
}
