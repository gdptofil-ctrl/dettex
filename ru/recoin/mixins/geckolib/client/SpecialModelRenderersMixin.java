/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  net.minecraft.class_10515$class_10516
 *  net.minecraft.class_10517
 *  net.minecraft.class_2960
 *  net.minecraft.class_5699$class_10388
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins.geckolib.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.class_10515;
import net.minecraft.class_10517;
import net.minecraft.class_2960;
import net.minecraft.class_5699;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.api.mods.geckolib.GeckoLibConstants;
import ru.recoin.api.mods.geckolib.renderer.internal.GeckolibItemSpecialRenderer;

@Mixin(value={class_10517.class})
public class SpecialModelRenderersMixin {
    @Shadow
    @Final
    private static class_5699.class_10388<class_2960, MapCodec<? extends class_10515.class_10516>> field_55453;

    @Inject(method={"<clinit>"}, at={@At(value="TAIL")}, require=0)
    private static void geckolib_addSpecialRenderer(CallbackInfo ci) {
        if (field_55453 != null) {
            field_55453.method_65325(GeckoLibConstants.id("geckolib"), GeckolibItemSpecialRenderer.Unbaked.MAP_CODEC);
        }
    }
}

