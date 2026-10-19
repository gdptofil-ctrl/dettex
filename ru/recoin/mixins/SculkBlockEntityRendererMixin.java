/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2586
 *  net.minecraft.class_824
 *  net.minecraft.class_827
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.recoin.mixins;

import net.minecraft.class_2586;
import net.minecraft.class_824;
import net.minecraft.class_827;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.recoin.implement.features.modules.render.NoRender;

@Mixin(value={class_824.class})
public class SculkBlockEntityRendererMixin {
    @Inject(method={"method_3550"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$hideSculkBlockEntity(class_2586 entity, CallbackInfoReturnable<class_827<?, ?>> cir) {
        NoRender module = NoRender.getInstance();
        if (module != null && module.hidesSculk(entity.method_11010().method_26204())) {
            cir.setReturnValue(null);
        }
    }
}

