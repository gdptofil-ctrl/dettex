/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.class_1309
 *  net.minecraft.class_1799
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.recoin.mixins.geckolib.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.recoin.api.mods.geckolib.GeckoLibConstants;

@Mixin(value={class_1309.class})
public class LivingEntityMixin {
    @WrapOperation(method={"method_45324"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_1799;method_7973(Lnet/minecraft/class_1799;Lnet/minecraft/class_1799;)Z")}, require=0)
    public boolean geckolib_allowLazyStackIdParity(class_1799 remoteStack, class_1799 localStack, Operation<Boolean> original) {
        return (Boolean)original.call(new Object[]{remoteStack, localStack}) != false && ((Number)remoteStack.method_58695(GeckoLibConstants.STACK_ANIMATABLE_ID_COMPONENT.get(), (Object)Integer.MIN_VALUE)).equals(localStack.method_58695(GeckoLibConstants.STACK_ANIMATABLE_ID_COMPONENT.get(), (Object)Integer.MIN_VALUE));
    }
}

