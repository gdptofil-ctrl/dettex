/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.class_10442
 *  net.minecraft.class_10444
 *  net.minecraft.class_10515
 *  net.minecraft.class_11566
 *  net.minecraft.class_1799
 *  net.minecraft.class_638
 *  net.minecraft.class_811
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.recoin.mixins.geckolib.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.class_10442;
import net.minecraft.class_10444;
import net.minecraft.class_10515;
import net.minecraft.class_11566;
import net.minecraft.class_1799;
import net.minecraft.class_638;
import net.minecraft.class_811;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.recoin.api.mods.geckolib.renderer.internal.GeckolibItemSpecialRenderer;

@Mixin(targets={"net/minecraft/class_10455"})
public class SpecialModelWrapperMixin {
    @WrapOperation(method={"method_65584", "method_65584"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_10515;method_65695(Lnet/minecraft/class_1799;)Ljava/lang/Object;")}, require=0)
    @Nullable
    public <T> T geckolib_extractAllArguments(class_10515<T> instance, class_1799 itemStack, Operation<T> original, class_10444 renderState, class_1799 itemStack2, class_10442 modelResolver, class_811 displayContext, @Nullable class_638 level, @Nullable class_11566 itemOwner, int layerIndex) {
        Object object;
        if (instance instanceof GeckolibItemSpecialRenderer) {
            GeckolibItemSpecialRenderer geckolibRenderer = (GeckolibItemSpecialRenderer)instance;
            object = geckolibRenderer.extractArgument(itemStack, renderState, displayContext, level, itemOwner);
        } else {
            object = original.call(new Object[]{instance, itemStack});
        }
        return (T)object;
    }
}

