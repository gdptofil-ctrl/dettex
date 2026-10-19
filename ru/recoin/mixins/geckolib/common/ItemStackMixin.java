/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.class_1799
 *  net.minecraft.class_9335
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.recoin.mixins.geckolib.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.class_1799;
import net.minecraft.class_9335;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.recoin.api.mods.geckolib.GeckoLibConstants;
import ru.recoin.api.mods.geckolib.util.GeckoLibUtil;

@Mixin(value={class_1799.class})
public class ItemStackMixin {
    @WrapOperation(method={"method_7971"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_1799;method_46651(I)Lnet/minecraft/class_1799;")}, require=0)
    public class_1799 geckolib_removeGeckolibIdOnCopy(class_1799 instance, int count, Operation<class_1799> original) {
        class_1799 copy = (class_1799)original.call(new Object[]{instance, count});
        if (count < instance.method_7947() && copy.method_57826(GeckoLibConstants.STACK_ANIMATABLE_ID_COMPONENT.get())) {
            copy.method_57381(GeckoLibConstants.STACK_ANIMATABLE_ID_COMPONENT.get());
        }
        return copy;
    }

    @WrapOperation(method={"method_31577"}, at={@At(value="INVOKE", target="Ljava/util/Objects;equals(Ljava/lang/Object;Ljava/lang/Object;)Z")}, require=0)
    private static boolean geckolib_skipGeckolibIdOnCompare(Object a, Object b, Operation<Boolean> original) {
        class_9335 components;
        block5: {
            block4: {
                if (((Boolean)original.call(new Object[]{a, b})).booleanValue()) {
                    return true;
                }
                if (!(a instanceof class_9335)) break block4;
                components = (class_9335)a;
                if (b instanceof class_9335) break block5;
            }
            return false;
        }
        class_9335 components2 = (class_9335)b;
        return GeckoLibUtil.areComponentsMatchingIgnoringGeckoLibId(components, components2);
    }
}

