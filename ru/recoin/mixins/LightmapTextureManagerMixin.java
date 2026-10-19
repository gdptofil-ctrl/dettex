/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1291
 *  net.minecraft.class_6880
 *  net.minecraft.class_746
 *  net.minecraft.class_765
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package ru.recoin.mixins;

import net.minecraft.class_1291;
import net.minecraft.class_6880;
import net.minecraft.class_746;
import net.minecraft.class_765;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.recoin.implement.features.modules.render.FullBright;

@Mixin(value={class_765.class})
public class LightmapTextureManagerMixin {
    @ModifyArg(method={"method_3313(F)V"}, at=@At(value="INVOKE", target="Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;", ordinal=6), index=0)
    private float recoin$applyFullBrightGamma(float original) {
        FullBright fullBright = FullBright.getInstance();
        return fullBright == null ? original : fullBright.resolveGamma(original);
    }

    @Redirect(method={"method_3313(F)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_746;method_66279(Lnet/minecraft/class_6880;F)F"))
    private float recoin$removeDarkness(class_746 player, class_6880<class_1291> effect, float tickProgress) {
        FullBright fullBright = FullBright.getInstance();
        if (fullBright != null && fullBright.isState() && !fullBright.isGammaMode()) {
            return 0.0f;
        }
        return player.method_66279(effect, tickProgress);
    }
}

