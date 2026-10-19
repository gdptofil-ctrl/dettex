/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.class_10055
 *  net.minecraft.class_11659
 *  net.minecraft.class_2960
 *  net.minecraft.class_4587
 *  net.minecraft.class_591
 *  net.minecraft.class_972
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_10055;
import net.minecraft.class_11659;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_591;
import net.minecraft.class_972;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.common.util.render.WaveyCapeRenderer;

@Mixin(value={class_972.class})
public class CapeFeatureRendererMixin {
    private static final class_2960 RECOIN_CAPE = class_2960.method_60655((String)"minecraft", (String)"capes/recoin");

    @Inject(method={"method_4177"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$renderWaveyCape(class_4587 matrices, class_11659 queue, int light, class_10055 state, float limbAngle, float limbDistance, CallbackInfo ci) {
        if (state.field_53333 || !state.field_53532 || state.field_53520 == null || state.field_53520.comp_1627() == null || !RECOIN_CAPE.equals((Object)state.field_53520.comp_1627().comp_3627())) {
            return;
        }
        if (FabricLoader.getInstance().isModLoaded("waveycapes")) {
            return;
        }
        matrices.method_22903();
        class_972 renderer = (class_972)(Object)this;
        ((class_591)renderer.method_17165()).method_63512().method_22703(matrices);
        ((class_591)renderer.method_17165()).field_3391.method_22703(matrices);
        WaveyCapeRenderer.render(matrices, queue, light, state, RECOIN_CAPE);
        matrices.method_22909();
        ci.cancel();
    }
}

