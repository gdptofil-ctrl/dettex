/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10017
 *  net.minecraft.class_10201
 *  net.minecraft.class_10442
 *  net.minecraft.class_1060
 *  net.minecraft.class_11697
 *  net.minecraft.class_11786
 *  net.minecraft.class_1297
 *  net.minecraft.class_310
 *  net.minecraft.class_315
 *  net.minecraft.class_327
 *  net.minecraft.class_330
 *  net.minecraft.class_5599
 *  net.minecraft.class_776
 *  net.minecraft.class_897
 *  net.minecraft.class_898
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.recoin.mixins;

import java.util.function.Supplier;
import net.minecraft.class_10017;
import net.minecraft.class_10201;
import net.minecraft.class_10442;
import net.minecraft.class_1060;
import net.minecraft.class_11697;
import net.minecraft.class_11786;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_327;
import net.minecraft.class_330;
import net.minecraft.class_5599;
import net.minecraft.class_776;
import net.minecraft.class_897;
import net.minecraft.class_898;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.recoin.implement.features.modules.render.custompet.render.CustomPetRendererBridge;

@Mixin(value={class_898.class})
public abstract class CustomPetEntityRenderDispatcherMixin {
    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void recoin_bootstrapCustomPetRenderer(class_310 minecraft, class_1060 textureManager, class_10442 itemModelResolver, class_330 mapRenderer, class_776 blockRenderDispatcher, class_11697 atlasManager, class_327 font, class_315 options, Supplier<class_5599> modelSetSupplier, class_10201 equipmentAssetManager, class_11786 playerSkinRenderCache, CallbackInfo ci) {
        CustomPetRendererBridge.bootstrap((class_898)(Object)this, minecraft, blockRenderDispatcher, itemModelResolver, mapRenderer, atlasManager, font, modelSetSupplier, equipmentAssetManager, playerSkinRenderCache);
    }

    @Inject(method={"method_3953(Lnet/minecraft/class_1297;)Lnet/minecraft/class_897;"}, at={@At(value="HEAD")}, cancellable=true)
    private <T extends class_1297> void recoin_useCustomPetRenderer(T entity, CallbackInfoReturnable<class_897<? super T, ?>> cir) {
        class_897<? super T, ?> renderer = CustomPetRendererBridge.getCustomRenderer(entity);
        if (renderer != null) {
            cir.setReturnValue(renderer);
        }
    }

    @Inject(method={"method_68832(Lnet/minecraft/class_10017;)Lnet/minecraft/class_897;"}, at={@At(value="HEAD")}, cancellable=true)
    private <S extends class_10017> void recoin_useCustomPetStateRenderer(S renderState, CallbackInfoReturnable<class_897<?, ? super S>> cir) {
        class_897<?, ? super S> renderer = CustomPetRendererBridge.getCustomRenderer(renderState);
        if (renderer != null) {
            cir.setReturnValue(renderer);
        }
    }
}

