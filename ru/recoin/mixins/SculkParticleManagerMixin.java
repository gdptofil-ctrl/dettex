/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2394
 *  net.minecraft.class_2396
 *  net.minecraft.class_2398
 *  net.minecraft.class_702
 *  net.minecraft.class_703
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.recoin.mixins;

import net.minecraft.class_2394;
import net.minecraft.class_2396;
import net.minecraft.class_2398;
import net.minecraft.class_702;
import net.minecraft.class_703;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.recoin.implement.features.modules.render.NoRender;

@Mixin(value={class_702.class})
public class SculkParticleManagerMixin {
    @Inject(method={"method_3056(Lnet/minecraft/class_2394;DDDDDD)Lnet/minecraft/class_703;"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$hideSculkParticles(class_2394 effect, double x, double y, double z, double vx, double vy, double vz, CallbackInfoReturnable<class_703> cir) {
        NoRender module = NoRender.getInstance();
        if (module == null || !module.isState() || !module.modeSetting.isSelected("Sculk Particles")) {
            return;
        }
        class_2396 type = effect.method_10295();
        if (type == class_2398.field_38003 || type == class_2398.field_38004 || type == class_2398.field_38002 || type == class_2398.field_28275 || type == class_2398.field_38357) {
            cir.setReturnValue(null);
        }
    }
}

