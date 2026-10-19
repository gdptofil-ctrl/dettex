/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1309
 *  net.minecraft.class_310
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package ru.recoin.mixins;

import net.minecraft.class_1309;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.recoin.implement.features.modules.movement.Speed;

@Mixin(value={class_1309.class}, priority=950)
public class LivingEntityElytraTravelMixin {
    @Redirect(method={"method_6091"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_1309;method_6128()Z"), require=0)
    private boolean recoin$redirectIsGliding(class_1309 self) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null || mc.field_1724 != self) {
            return self.method_6128();
        }
        Speed speed = Speed.getInstance();
        return speed != null && speed.isFunTimeElytraMode() || self.method_6128();
    }
}

