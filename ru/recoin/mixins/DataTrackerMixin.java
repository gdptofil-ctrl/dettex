/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2379
 *  net.minecraft.class_2940
 *  net.minecraft.class_2941
 *  net.minecraft.class_2943
 *  net.minecraft.class_2945
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_2379;
import net.minecraft.class_2940;
import net.minecraft.class_2941;
import net.minecraft.class_2943;
import net.minecraft.class_2945;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_2945.class})
public abstract class DataTrackerMixin {
    @Inject(method={"method_12778(Lnet/minecraft/class_2940;Ljava/lang/Object;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private <T> void onSet(class_2940<T> data, T value, CallbackInfo ci) {
        class_2941 serializer2 = data.comp_2328();
        if (serializer2 == class_2943.field_13319 && !(value instanceof Byte)) {
            ci.cancel();
        }
        if (serializer2 == class_2943.field_13316 && !(value instanceof class_2379)) {
            ci.cancel();
        }
    }
}

