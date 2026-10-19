/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.class_1309
 *  net.minecraft.class_276
 *  net.minecraft.class_310
 *  net.minecraft.class_4184
 *  net.minecraft.class_761
 *  net.minecraft.class_9909
 *  net.minecraft.class_9916
 *  net.minecraft.class_9925
 *  net.minecraft.class_9960
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.class_1309;
import net.minecraft.class_276;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_761;
import net.minecraft.class_9909;
import net.minecraft.class_9916;
import net.minecraft.class_9925;
import net.minecraft.class_9960;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.common.util.render.LegacyRenderSystem;
import ru.recoin.common.util.render.pipeline.NebulaSkyPipeline;
import ru.recoin.common.util.render.pipeline.ShaderEspGlowPipeline;
import ru.recoin.common.util.render.pipeline.SkyShaderPipeline;
import ru.recoin.common.util.render.pipeline.StardustSkyPipeline;
import ru.recoin.implement.features.modules.misc.FreeCam;
import ru.recoin.implement.features.modules.render.BlockOverlay;
import ru.recoin.implement.features.modules.render.NoRender;
import ru.recoin.implement.features.modules.render.ShaderESP;
import ru.recoin.implement.features.modules.render.WorldTweaks;

@Mixin(value={class_761.class})
public abstract class WorldRendererMixin {
    @Shadow
    @Final
    private class_9960 field_53081;
    @Shadow
    private class_276 field_53080;
    private final ShaderEspGlowPipeline recoin$shaderEspGlow = new ShaderEspGlowPipeline();

    @Inject(method={"close"}, at={@At(value="TAIL")})
    private void recoin$closeShaderEspGlow(CallbackInfo ci) {
        this.recoin$shaderEspGlow.close();
    }

    @Inject(method={"method_22712"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void recoin$cancelVanillaBlockOutline(CallbackInfo ci) {
        BlockOverlay overlay = BlockOverlay.getInstance();
        if (overlay != null && overlay.isState()) {
            ci.cancel();
        }
    }

    @ModifyVariable(method={"method_22712"}, at=@At(value="HEAD"), argsOnly=true, ordinal=0, require=0)
    private int recoin$blockOverlayColor(int original) {
        BlockOverlay overlay = BlockOverlay.getInstance();
        return overlay != null && overlay.isState() ? overlay.vanillaOutlineColor() : original;
    }

    @ModifyVariable(method={"method_22712"}, at=@At(value="HEAD"), argsOnly=true, ordinal=0, require=0)
    private float recoin$blockOverlayWidth(float original) {
        BlockOverlay overlay = BlockOverlay.getInstance();
        return overlay != null && overlay.isState() ? overlay.vanillaLineWidth() : original;
    }

    @ModifyArg(method={"method_22710"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_761;method_74752(Lnet/minecraft/class_4184;Lnet/minecraft/class_4604;Z)V"), index=2, require=0)
    private boolean recoin$forceSpectatorChunkRendering(boolean spectator) {
        return spectator || FreeCam.getInstance().isState();
    }

    @Redirect(method={"method_72917"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_1309;method_6113()Z"), require=0)
    private boolean recoin$renderRealPlayerInFreeCam(class_1309 entity) {
        FreeCam freeCam = FreeCam.getInstance();
        if (freeCam != null && freeCam.isState() && entity == class_310.method_1551().field_1724) {
            return true;
        }
        return entity.method_6113();
    }

    @Inject(method={"method_62203"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void recoin$cancelWeather(class_9909 frameGraphBuilder, GpuBufferSlice gpuBufferSlice, CallbackInfo ci) {
        NoRender noRender = NoRender.getInstance();
        if (noRender != null && noRender.isState() && noRender.modeSetting.isSelected("Weather")) {
            ci.cancel();
        }
    }

    @Inject(method={"method_62200"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void recoin$renderCustomSky(class_9909 graph, class_4184 camera, GpuBufferSlice transforms, CallbackInfo ci) {
        WorldTweaks tweaks = WorldTweaks.getInstance();
        if (tweaks != null && tweaks.usesStardustSky()) {
            class_9916 pass = graph.method_61911("recoin_stardust_sky");
            this.field_53081.field_53091 = pass.method_61933(this.field_53081.field_53091);
            pass.method_61929(() -> {
                RenderSystem.setShaderFog((GpuBufferSlice)transforms);
                float tickDelta = class_310.method_1551().method_61966().method_60637(false);
                StardustSkyPipeline.getInstance().draw(camera, tickDelta, tweaks.stardustPrimaryColor(), tweaks.stardustSecondaryColor(), tweaks.stardustIntensity(), tweaks.starDensity.getValue() / 3600.0f, tweaks.stardustTime(), tweaks.stardustPreset());
            });
            ci.cancel();
            return;
        }
        if (tweaks == null || !tweaks.customSkyEnabled() || tweaks.usesNebulaSky()) {
            return;
        }
        class_9916 pass = graph.method_61911("recoin_custom_sky");
        class_9925 target = this.field_53081.field_53091 = pass.method_61933(this.field_53081.field_53091);
        int mode = tweaks.skyShaderMode();
        float yaw = camera.method_19330();
        float pitch = camera.method_19329();
        float fov = tweaks.skyProjectionFov();
        int fogColor = tweaks.fogColor();
        int shaderColor = tweaks.shaderColor();
        boolean fogEnabled = tweaks.customFogEnabled();
        pass.method_61929(() -> SkyShaderPipeline.getInstance().draw((class_276)target.get(), mode, yaw, pitch, fov, fogColor, fogEnabled, shaderColor));
        ci.cancel();
    }

    @Inject(method={"method_62200"}, at={@At(value="RETURN")})
    private void recoin$renderNebulaSky(class_9909 graph, class_4184 camera, GpuBufferSlice transforms, CallbackInfo ci) {
        WorldTweaks tweaks = WorldTweaks.getInstance();
        if (tweaks == null || !tweaks.usesNebulaSky()) {
            return;
        }
        class_9916 pass = graph.method_61911("recoin_nebula_sky");
        class_9925 target = this.field_53081.field_53091 = pass.method_61933(this.field_53081.field_53091);
        float fov = LegacyRenderSystem.getLastFov();
        float skyFov = fov > 0.0f ? fov : tweaks.skyProjectionFov();
        pass.method_61929(() -> NebulaSkyPipeline.getInstance().draw((class_276)target.get(), camera, skyFov, tweaks.shaderColor(), tweaks.stardustSecondaryColor()));
    }

    @Inject(method={"method_3254"}, at={@At(value="TAIL")}, require=0)
    private void recoin$drawShaderEspGlow(CallbackInfo ci) {
        ShaderESP shader = ShaderESP.getInstance();
        class_276 main = class_310.method_1551().method_1522();
        if (shader == null || !shader.isState() || main == null || this.field_53080 == null) {
            return;
        }
        this.recoin$shaderEspGlow.draw(main.method_71639(), this.field_53080.method_71639(), main.field_1482, main.field_1481, shader.getGlowRadius(), shader.getGlowIntensity());
    }
}

