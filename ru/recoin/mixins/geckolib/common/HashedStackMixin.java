/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.class_10936
 *  net.minecraft.class_10936$class_10937
 *  net.minecraft.class_9326
 *  net.minecraft.class_9331
 *  net.minecraft.class_9336
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.recoin.mixins.geckolib.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.Optional;
import net.minecraft.class_10936;
import net.minecraft.class_9326;
import net.minecraft.class_9331;
import net.minecraft.class_9336;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.recoin.api.mods.geckolib.GeckoLibConstants;

@Mixin(targets={"net/minecraft/class_10938$class_10939"})
public class HashedStackMixin {
    @WrapOperation(method={"matches"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_10936;matches(Lnet/minecraft/class_9326;Lnet/minecraft/class_10936$class_10937;)Z")}, require=0)
    public boolean geckolib_allowLazyStackIdParity(class_10936 patchMap, class_9326 componentPatch, class_10936.class_10937 hasher, Operation<Boolean> original) {
        int localStackHashedId;
        if (!((Boolean)original.call(new Object[]{patchMap, componentPatch, hasher})).booleanValue()) {
            return false;
        }
        class_9331<Long> componentType = GeckoLibConstants.STACK_ANIMATABLE_ID_COMPONENT.get();
        int remoteStackHashedId = patchMap.comp_3874().getOrDefault(componentType, Integer.MIN_VALUE);
        return remoteStackHashedId == (localStackHashedId = Optional.ofNullable(componentPatch.method_57845(componentType)).map(optional -> optional.map(value -> (Integer)hasher.apply(new class_9336(componentType, value))).orElse(Integer.MIN_VALUE)).orElse(Integer.MIN_VALUE).intValue());
    }
}

