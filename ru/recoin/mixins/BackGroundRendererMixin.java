/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.class_11400
 *  net.minecraft.class_4184
 *  net.minecraft.class_638
 *  net.minecraft.class_7285
 *  net.minecraft.class_7286
 *  net.minecraft.class_758
 *  net.minecraft.class_9779
 *  org.joml.Vector4f
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package ru.recoin.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.class_11400;
import net.minecraft.class_4184;
import net.minecraft.class_638;
import net.minecraft.class_7285;
import net.minecraft.class_7286;
import net.minecraft.class_758;
import net.minecraft.class_9779;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import ru.recoin.api.event.EventManager;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.implement.events.render.FogEvent;
import ru.recoin.implement.features.modules.render.NoRender;

@Mixin(value={class_758.class})
public class BackGroundRendererMixin {
    @ModifyExpressionValue(method={"method_62185", "method_3211"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_11400;method_42593(Lnet/minecraft/class_5636;Lnet/minecraft/class_1297;)Z")})
    private boolean recoin$skipBadEffectFog(boolean original, @Local class_11400 modifier) {
        NoRender noRender = NoRender.getInstance();
        if (noRender.isState() && noRender.modeSetting.isSelected("Bad Effects") && modifier instanceof class_7286) {
            return false;
        }
        return original;
    }

    @Inject(method={"method_62185"}, at={@At(value="HEAD")}, cancellable=true)
    private void getFogColorHook(class_4184 camera, float tickDelta, class_638 world, int clampedViewDistance, float skyDarkness, CallbackInfoReturnable<Vector4f> cir) {
        if (!EventManager.hasSubscribers(FogEvent.class)) {
            return;
        }
        FogEvent event = new FogEvent();
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            int color = event.getColor();
            cir.setReturnValue(new Vector4f(ColorUtil.redf(color), ColorUtil.greenf(color), ColorUtil.bluef(color), ColorUtil.alphaf(color)));
        }
    }

    @ModifyArgs(method={"method_3211"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_758;method_71110(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"))
    private void modifyFog(Args args2, class_4184 camera, int viewDistance, class_9779 tickCounter, float skyDarkness, class_638 world) {
        if (!EventManager.hasSubscribers(FogEvent.class)) {
            return;
        }
        FogEvent event = new FogEvent();
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            int color = event.getColor();
            args2.set(2, (Object)new Vector4f(ColorUtil.redf(color), ColorUtil.greenf(color), ColorUtil.bluef(color), ColorUtil.alphaf(color)));
            args2.set(3, (Object)Float.valueOf(2.0f));
            args2.set(4, (Object)Float.valueOf(event.getDistance()));
            args2.set(5, (Object)Float.valueOf(2.0f));
            args2.set(6, (Object)Float.valueOf(event.getDistance()));
            args2.set(7, (Object)Float.valueOf(event.getDistance()));
            args2.set(8, (Object)Float.valueOf(event.getDistance()));
        }
    }

    @Inject(method={"method_3211"}, at={@At(value="INVOKE", target="Lcom/mojang/blaze3d/systems/RenderSystem;getDevice()Lcom/mojang/blaze3d/systems/GpuDevice;")}, require=0)
    private void recoin$modifyFogSources(class_4184 camera, int viewDistance, class_9779 tickCounter, float skyDarkness, class_638 world, CallbackInfoReturnable<Vector4f> cir, @Local Vector4f fogColor, @Local class_7285 fogData) {
        if (!EventManager.hasSubscribers(FogEvent.class)) {
            return;
        }
        FogEvent event = new FogEvent();
        EventManager.callEvent(event);
        if (!event.isCancelled()) {
            return;
        }
        int color = event.getColor();
        fogColor.set(ColorUtil.redf(color), ColorUtil.greenf(color), ColorUtil.bluef(color), ColorUtil.alphaf(color));
        float distance = event.getDistance();
        fogData.field_60582 = 2.0f;
        fogData.field_60584 = distance;
        fogData.field_60583 = 2.0f;
        fogData.field_60585 = distance;
        fogData.field_60099 = distance;
        fogData.field_60100 = distance;
    }
}

