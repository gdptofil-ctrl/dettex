/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.class_10209
 *  net.minecraft.class_11228
 *  net.minecraft.class_11246
 *  net.minecraft.class_11684
 *  net.minecraft.class_1297
 *  net.minecraft.class_239
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_3966
 *  net.minecraft.class_4184
 *  net.minecraft.class_437
 *  net.minecraft.class_4599
 *  net.minecraft.class_757
 *  net.minecraft.class_758
 *  net.minecraft.class_758$class_4596
 *  net.minecraft.class_9779
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.gen.Invoker
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.recoin.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.class_10209;
import net.minecraft.class_11228;
import net.minecraft.class_11246;
import net.minecraft.class_11684;
import net.minecraft.class_1297;
import net.minecraft.class_239;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3966;
import net.minecraft.class_4184;
import net.minecraft.class_437;
import net.minecraft.class_4599;
import net.minecraft.class_757;
import net.minecraft.class_758;
import net.minecraft.class_9779;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.recoin.api.event.EventManager;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.render.GuiRenderFlusher;
import ru.recoin.common.util.render.GuiTextureCache;
import ru.recoin.common.util.render.HudFinalPass;
import ru.recoin.common.util.render.LegacyRenderSystem;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.common.util.render.pipeline.ContrastPipeline;
import ru.recoin.common.util.render.pipeline.EffectUniforms;
import ru.recoin.core.Main;
import ru.recoin.implement.events.render.AspectRatioEvent;
import ru.recoin.implement.events.render.FovEvent;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RaytracingUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.misc.FreeCam;
import ru.recoin.implement.features.modules.render.Atmosphere;
import ru.recoin.implement.features.modules.render.Contrast;
import ru.recoin.implement.features.modules.render.HandsShader;
import ru.recoin.implement.features.modules.render.MotionBlur;
import ru.recoin.implement.features.modules.render.ViewModel;
import ru.recoin.implement.features.modules.render.WetWorld;
import ru.recoin.implement.features.modules.render.Winter;
import ru.recoin.implement.screens.autobuy.AutoBuyScreen;
import ru.recoin.implement.screens.mainmenu.RecoinPostGuiScreen;
import ru.recoin.implement.screens.menu.MenuScreen;

@Mixin(value={class_757.class})
public abstract class GameRendererMixin {
    @Unique
    private final Matrix4f recoin$effectViewMatrix = new Matrix4f();
    @Unique
    private final Quaternionf recoin$effectRotation = new Quaternionf();
    @Final
    @Shadow
    private class_310 field_4015;
    @Final
    @Shadow
    private class_11246 field_59966;
    @Final
    @Shadow
    private class_11228 field_59965;
    @Final
    @Shadow
    private class_758 field_60793;
    @Final
    @Shadow
    private class_11684 field_61734;
    @Final
    @Shadow
    private class_4599 field_20948;

    @Inject(method={"method_3192"}, at={@At(value="HEAD")})
    private void recoin$beginEffectUniforms(class_9779 tickCounter, boolean tick, CallbackInfo ci) {
        GuiTextureCache.beginFrame();
        EffectUniforms.beginFrame();
    }

    @Inject(method={"close"}, at={@At(value="TAIL")})
    private void recoin$closeEffectUniforms(CallbackInfo ci) {
        GuiTextureCache.beginFrame();
        EffectUniforms.close();
        ContrastPipeline.close();
    }

    @Shadow
    public abstract float method_32796();

    @Shadow
    public abstract Matrix4f method_22973(float var1);

    @Invoker(value="method_3196")
    protected abstract float recoin$getHandFov(class_4184 var1, float var2, boolean var3);

    @Inject(method={"method_3172(FZLorg/joml/Matrix4f;)V"}, at={@At(value="HEAD")})
    private void recoin$beginHandDragFrame(float tickProgress, boolean sleeping, Matrix4f view, CallbackInfo ci) {
        ViewModel viewModel = ViewModel.getInstance();
        if (viewModel != null && viewModel.isState()) {
            float fov = this.recoin$getHandFov(this.field_4015.field_1773.method_19418(), tickProgress, false);
            float aspect = (float)this.field_4015.method_22683().method_4489() / (float)this.field_4015.method_22683().method_4506();
            viewModel.getHandGeometry().beginFrame(new Matrix4f().perspective((float)Math.toRadians(fov), aspect, 0.05f, this.method_32796()));
        }
    }

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void recoin$init(CallbackInfo ci) {
        GuiRenderFlusher.bind(this.field_59965, this.field_59966, this.field_60793);
    }

    @Inject(method={"method_3188"}, at={@At(value="TAIL")})
    private void recoin$renderShaderCompatibleWorldFeatures(class_9779 tickCounter, CallbackInfo ci) {
        Main main = Main.getInstance();
        if (main != null) {
            main.renderWorldFeaturesAfterShaderPack();
        }
    }

    @Inject(method={"method_3190"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_746;method_76762(FLnet/minecraft/class_1297;)Lnet/minecraft/class_239;")}, cancellable=true, require=0)
    private void onUpdateTargetedEntity(float tickDelta, CallbackInfo ci) {
        class_239 hit;
        if (PlayerIntersectionUtil.nullCheck()) {
            return;
        }
        FreeCam freeCam = FreeCam.getInstance();
        if (freeCam.isState()) {
            class_10209.method_64146().method_15407();
            this.field_4015.field_1765 = RaytracingUtil.raycast(freeCam.pos, 4.5, AngleUtil.cameraAngle(), false);
            ci.cancel();
            return;
        }
        RotationController rotations = RotationController.INSTANCE;
        if (rotations.hasActiveRotation() && (hit = RaytracingUtil.raycastCrosshair((class_1297)this.field_4015.field_1724, this.field_4015.field_1724.method_55754(), this.field_4015.field_1724.method_55755(), tickDelta, rotations.getServerAngle())) != null) {
            class_1297 class_12972;
            class_10209.method_64146().method_15407();
            this.field_4015.field_1765 = hit;
            if (hit instanceof class_3966) {
                class_3966 entityHit = (class_3966)hit;
                class_12972 = entityHit.method_17782();
            } else {
                class_12972 = null;
            }
            this.field_4015.field_1692 = class_12972;
            ci.cancel();
        }
    }

    @Inject(method={"method_22973"}, at={@At(value="TAIL")}, cancellable=true, require=0)
    public void getBasicProjectionMatrixHook(float fovDegrees, CallbackInfoReturnable<Matrix4f> cir) {
        if (!EventManager.hasSubscribers(AspectRatioEvent.class)) {
            return;
        }
        AspectRatioEvent aspectRatioEvent = new AspectRatioEvent();
        EventManager.callEvent(aspectRatioEvent);
        if (aspectRatioEvent.isCancelled()) {
            Matrix4f matrix4f = new Matrix4f();
            matrix4f.perspective(fovDegrees * ((float)Math.PI / 180), aspectRatioEvent.getRatio(), 0.05f, this.method_32796());
            cir.setReturnValue(matrix4f);
        }
    }

    @Inject(method={"method_3188"}, at={@At(value="INVOKE", target="Lcom/mojang/blaze3d/systems/CommandEncoder;clearDepthTexture(Lcom/mojang/blaze3d/textures/GpuTexture;D)V")}, require=0)
    private void recoin$renderWetWorldBeforeDepthClear(class_9779 tickCounter, CallbackInfo ci) {
        class_4184 camera = this.field_4015.field_1773.method_19418();
        if (camera != null) {
            WetWorld wetWorld = WetWorld.getInstance();
            Atmosphere atmosphere = Atmosphere.getInstance();
            Winter winter = Winter.getInstance();
            MotionBlur motionBlur = MotionBlur.getInstance();
            boolean needsMatrices = wetWorld != null && wetWorld.isState() || atmosphere != null && atmosphere.isState() || winter != null && winter.isState() || motionBlur != null && motionBlur.isState() || ru.recoin.implement.features.modules.render.Shader.active();
            float fov = LegacyRenderSystem.getLastFov();
            if (fov <= 0.0f && this.field_4015.field_1690 != null) {
                fov = ((Integer)this.field_4015.field_1690.method_41808().method_41753()).floatValue();
            }
            Matrix4f proj = needsMatrices ? this.method_22973(fov) : null;
            Matrix4f viewMatrix = needsMatrices ? this.recoin$effectViewMatrix.rotation((Quaternionfc)camera.method_23767().conjugate(this.recoin$effectRotation)) : null;
            WetWorld.updateMatrices(viewMatrix, proj, camera, fov);
            WetWorld.render();
            Winter.updateMatrices(viewMatrix, proj, camera, fov);
            if (atmosphere == null || !atmosphere.isState()) {
                Winter.render();
            }
            Atmosphere.render(proj);
            ru.recoin.implement.features.modules.render.Shader.render(proj);
            MotionBlur.updateMatrices(viewMatrix, proj, camera);
            MotionBlur.render();
        }
    }

    @Inject(method={"method_3172"}, at={@At(value="TAIL")}, require=0)
    private void recoin$renderGlassHandsBeforeScreenOverlays(float tickProgress, boolean sleeping, Matrix4f positionMatrix, CallbackInfo ci) {
        HandsShader handsShader = HandsShader.getInstance();
        if (handsShader != null && handsShader.isState()) {
            this.field_61734.method_73002();
            this.field_20948.method_23000().method_22993();
            handsShader.renderGlassHandsPost();
        }
    }

    @ModifyExpressionValue(method={"method_3196"}, at={@At(value="INVOKE", target="Ljava/lang/Integer;intValue()I", remap=false)}, require=0)
    private int hookGetFov(int original) {
        FovEvent event = new FovEvent();
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            LegacyRenderSystem.setLastFov(event.getFov());
            return event.getFov();
        }
        LegacyRenderSystem.setLastFov(original);
        return original;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Inject(method={"method_3192"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_11228;method_70890(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V", shift=At.Shift.AFTER)}, require=0)
    private void recoin$renderHudAfterGui(class_9779 tickCounter, boolean tick, CallbackInfo ci) {
        Object overlayContext;
        class_437 class_4372;
        if (this.field_4015.method_18506() == null && (class_4372 = this.field_4015.field_1755) instanceof RecoinPostGuiScreen) {
            RecoinPostGuiScreen overlay = (RecoinPostGuiScreen)class_4372;
            this.field_59966.method_70926();
            int mouseX = (int)this.field_4015.field_1729.method_68879(this.field_4015.method_22683());
            int mouseY = (int)this.field_4015.field_1729.method_68883(this.field_4015.method_22683());
            class_332 context = new class_332(this.field_4015, this.field_59966, mouseX, mouseY);
            Render2DUtil.setActiveContext(context);
            try {
                overlay.renderRecoinOverlay(context, mouseX, mouseY, tickCounter.method_60637(false));
                Render2DUtil.onRender(context);
                context.method_73199();
            }
            finally {
                Render2DUtil.setActiveContext(null);
            }
            this.field_59965.method_70890(this.field_60793.method_71109(class_758.class_4596.field_60101));
            return;
        }
        class_437 mouseX = this.field_4015.field_1755;
        if (mouseX instanceof MenuScreen) {
            MenuScreen menu = (MenuScreen)mouseX;
            overlayContext = new class_332(this.field_4015, this.field_59966, (int)this.field_4015.field_1729.method_68879(this.field_4015.method_22683()), (int)this.field_4015.field_1729.method_68883(this.field_4015.method_22683()));
            menu.getDropdownMenuComponent().renderThemeWave((class_332)overlayContext);
            return;
        }
        overlayContext = this.field_4015.field_1755;
        if (overlayContext instanceof AutoBuyScreen) {
            AutoBuyScreen autoBuyScreen = (AutoBuyScreen)overlayContext;
            this.field_59966.method_70926();
            int mouseX2 = (int)this.field_4015.field_1729.method_68879(this.field_4015.method_22683());
            int mouseY = (int)this.field_4015.field_1729.method_68883(this.field_4015.method_22683());
            class_332 context = new class_332(this.field_4015, this.field_59966, mouseX2, mouseY);
            Render2DUtil.setActiveContext(context);
            try {
                autoBuyScreen.renderOverlay(context, mouseX2, mouseY, tickCounter.method_60637(false));
                Render2DUtil.onRender(context);
                context.method_73199();
            }
            finally {
                Render2DUtil.setActiveContext(null);
            }
            this.field_59965.method_70890(this.field_60793.method_71109(class_758.class_4596.field_60101));
            return;
        }
        if (this.field_4015.field_1687 == null || this.field_4015.field_1724 == null || this.field_4015.method_18506() != null || this.field_4015.field_1755 != null) {
            return;
        }
        this.field_59966.method_70926();
        int mouseX3 = (int)this.field_4015.field_1729.method_68879(this.field_4015.method_22683());
        int mouseY = (int)this.field_4015.field_1729.method_68883(this.field_4015.method_22683());
        class_332 context = new class_332(this.field_4015, this.field_59966, mouseX3, mouseY);
        HudFinalPass.render(context);
        this.field_59965.method_70890(this.field_60793.method_71109(class_758.class_4596.field_60101));
    }

    @Inject(method={"method_3192"}, at={@At(value="TAIL")})
    private void recoin$renderContrast(class_9779 tickCounter, boolean tick, CallbackInfo ci) {
        Contrast.render();
    }
}

