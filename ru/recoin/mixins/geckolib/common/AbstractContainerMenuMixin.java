/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.class_1703
 *  net.minecraft.class_1799
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.recoin.mixins.geckolib.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.class_1703;
import net.minecraft.class_1799;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.recoin.api.mods.geckolib.GeckoLibConstants;

@Mixin(value={class_1703.class})
public class AbstractContainerMenuMixin {
    @WrapOperation(method={"method_30010"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_1799;method_46651(I)Lnet/minecraft/class_1799;", ordinal=1)}, require=0)
    public class_1799 geckolib_removeGeckolibIdOnCopy(class_1799 instance, int count, Operation<class_1799> original) {
        class_1799 copy = (class_1799)original.call(new Object[]{instance, count});
        if (copy.method_57826(GeckoLibConstants.STACK_ANIMATABLE_ID_COMPONENT.get())) {
            copy.method_57381(GeckoLibConstants.STACK_ANIMATABLE_ID_COMPONENT.get());
        }
        return copy;
    }

    @WrapOperation(method={"method_34246"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_1799;method_7973(Lnet/minecraft/class_1799;Lnet/minecraft/class_1799;)Z")}, require=0)
    public boolean geckolib_allowLazyStackIdParity(class_1799 stack, class_1799 other, Operation<Boolean> original) {
        return (Boolean)original.call(new Object[]{stack, other}) != false && ((Number)stack.method_58695(GeckoLibConstants.STACK_ANIMATABLE_ID_COMPONENT.get(), (Object)Integer.MIN_VALUE)).equals(other.method_58695(GeckoLibConstants.STACK_ANIMATABLE_ID_COMPONENT.get(), (Object)Integer.MIN_VALUE));
    }
}

