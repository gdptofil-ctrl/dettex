/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_304
 *  net.minecraft.class_310
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.recoin.mixins;

import net.minecraft.class_304;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.recoin.implement.features.modules.player.ItemHelper;

@Mixin(value={class_304.class})
public class KeyBindingMixin {
    @Inject(method={"method_1434"}, at={@At(value="HEAD")}, cancellable=true)
    private void hookIsPressed(CallbackInfoReturnable<Boolean> cir) {
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1690 == null) {
            return;
        }
        ItemHelper helper = ItemHelper.getInstance();
        if ((Object)this == mc.field_1690.field_1904) {
            boolean helperUse;
            boolean bl = helperUse = helper != null && helper.isUsingHelperItem();
            if (helperUse) {
                cir.setReturnValue(true);
            }
        }
    }
}

