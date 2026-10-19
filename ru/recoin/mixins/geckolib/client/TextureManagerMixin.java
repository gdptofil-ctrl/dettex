/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.v2.WrapWithCondition
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.class_1044
 *  net.minecraft.class_1049
 *  net.minecraft.class_10537
 *  net.minecraft.class_10539
 *  net.minecraft.class_1060
 *  net.minecraft.class_2960
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.recoin.mixins.geckolib.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.class_1044;
import net.minecraft.class_1049;
import net.minecraft.class_10537;
import net.minecraft.class_10539;
import net.minecraft.class_1060;
import net.minecraft.class_2960;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import ru.recoin.api.mods.geckolib.renderer.texture.GeckoLibAnimatedTexture;

@Mixin(value={class_1060.class})
public abstract class TextureManagerMixin {
    @Shadow
    protected abstract class_10539 method_65925(class_2960 var1, class_10537 var2);

    @Shadow
    public abstract void method_4616(class_2960 var1, class_1044 var2);

    @WrapOperation(method={"method_4619(Lnet/minecraft/class_2960;)Lnet/minecraft/class_1044;"}, at={@At(value="NEW", target="(Lnet/minecraft/class_2960;)Lnet/minecraft/class_1049;")}, require=0)
    private class_1049 geckolib_replaceAnimatableTexture(class_2960 location, Operation<class_1049> original) {
        GeckoLibAnimatedTexture animatableTexture = new GeckoLibAnimatedTexture(location);
        class_10539 contents = this.method_65925(location, (class_10537)animatableTexture);
        if (animatableTexture.isAnimated()) {
            animatableTexture.method_65857(contents);
            this.method_4616(location, (class_1044)animatableTexture);
            return animatableTexture;
        }
        animatableTexture.close();
        return (class_1049)original.call(new Object[]{location});
    }

    @WrapWithCondition(method={"method_4619(Lnet/minecraft/class_2960;)Lnet/minecraft/class_1044;"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_1060;method_65876(Lnet/minecraft/class_2960;Lnet/minecraft/class_10537;)V")}, require=0)
    private boolean geckolib_skipAnimatableTextureRegistration(class_1060 textureManager, class_2960 id, class_10537 texture) {
        return !(texture instanceof GeckoLibAnimatedTexture);
    }
}

