/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.CommandEncoder
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.GpuTexture
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.textures.TextureFormat
 *  net.minecraft.class_276
 *  net.minecraft.class_310
 */
package ru.recoin.common.util.render.shader;

import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.class_276;
import net.minecraft.class_310;
import ru.recoin.common.util.render.pipeline.HandEffectPipeline;
import ru.recoin.common.util.render.pipeline.HandMaskSmoothingPipeline;
import ru.recoin.compat.handglass.pipeline.KawaseBlurPipeline;
import ru.recoin.compat.handglass.pipeline.MaskDiffPipeline;

public class HandEffectRenderer {
    private static HandEffectRenderer instance;
    private final class_310 client = class_310.method_1551();
    private MaskDiffPipeline maskDiff;
    private KawaseBlurPipeline auraBlur;
    private HandEffectPipeline handEffect;
    private HandMaskSmoothingPipeline maskSmoothing;
    private GpuTexture sceneBeforeTexture;
    private GpuTextureView sceneBeforeTextureView;
    private GpuTexture sceneAfterTexture;
    private GpuTextureView sceneAfterTextureView;
    private GpuTexture depthBeforeTexture;
    private GpuTextureView depthBeforeTextureView;
    private GpuTexture maskTexture;
    private GpuTextureView maskTextureView;
    private int lastWidth;
    private int lastHeight;
    private boolean capturing;
    private boolean enabled;
    private boolean initialized;
    private int mode;
    private int color1 = -1;
    private int color2 = -9459457;
    private float intensity = 1.0f;
    private float speed = 1.0f;
    private float glowRadius = 0.04f;
    private float glowDispersion = 5.0f;
    private boolean glowFill = true;

    public HandEffectRenderer() {
        instance = this;
    }

    public static HandEffectRenderer getInstance() {
        if (instance == null) {
            instance = new HandEffectRenderer();
        }
        return instance;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            this.ensureInitialized();
        }
    }

    public void setEffect(int mode, int color1, int color2, float intensity, float speed, float glowRadius, float glowDispersion, boolean glowFill) {
        this.mode = mode;
        this.color1 = color1;
        this.color2 = color2;
        this.intensity = intensity;
        this.speed = speed;
        this.glowRadius = glowRadius;
        this.glowDispersion = glowDispersion;
        this.glowFill = glowFill;
    }

    private void ensureInitialized() {
        if (this.initialized) {
            return;
        }
        if (this.maskDiff != null) {
            this.maskDiff.close();
        }
        if (this.auraBlur != null) {
            this.auraBlur.close();
        }
        if (this.handEffect != null) {
            this.handEffect.close();
        }
        if (this.maskSmoothing != null) {
            this.maskSmoothing.close();
        }
        this.maskDiff = new MaskDiffPipeline();
        this.auraBlur = new KawaseBlurPipeline();
        this.handEffect = new HandEffectPipeline();
        this.maskSmoothing = new HandMaskSmoothingPipeline();
        this.lastWidth = 0;
        this.lastHeight = 0;
        this.initialized = true;
    }

    private void ensureTextures(int width, int height) {
        if (width == this.lastWidth && height == this.lastHeight && this.sceneBeforeTexture != null) {
            return;
        }
        this.cleanupTextures();
        this.sceneBeforeTexture = RenderSystem.getDevice().createTexture(() -> "recoin:hand_effect_scene_before", 13, TextureFormat.RGBA8, width, height, 1, 1);
        this.sceneBeforeTextureView = RenderSystem.getDevice().createTextureView(this.sceneBeforeTexture);
        this.sceneAfterTexture = RenderSystem.getDevice().createTexture(() -> "recoin:hand_effect_scene_after", 13, TextureFormat.RGBA8, width, height, 1, 1);
        this.sceneAfterTextureView = RenderSystem.getDevice().createTextureView(this.sceneAfterTexture);
        this.depthBeforeTexture = RenderSystem.getDevice().createTexture(() -> "recoin:hand_effect_depth_before", 13, TextureFormat.DEPTH32, width, height, 1, 1);
        this.depthBeforeTextureView = RenderSystem.getDevice().createTextureView(this.depthBeforeTexture);
        this.maskTexture = RenderSystem.getDevice().createTexture(() -> "recoin:hand_effect_mask", 13, TextureFormat.RGBA8, width, height, 1, 1);
        this.maskTextureView = RenderSystem.getDevice().createTextureView(this.maskTexture);
        this.lastWidth = width;
        this.lastHeight = height;
    }

    public void captureBeforeHands() {
        this.capturing = false;
        if (!this.enabled) {
            return;
        }
        this.ensureInitialized();
        class_276 fb = this.client.method_1522();
        if (fb == null || fb.method_30277() == null || fb.method_30278() == null) {
            return;
        }
        int width = fb.field_1482;
        int height = fb.field_1481;
        this.ensureTextures(width, height);
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.copyTextureToTexture(fb.method_30277(), this.sceneBeforeTexture, 0, 0, 0, 0, 0, width, height);
        if (fb.method_30278() != null) {
            encoder.copyTextureToTexture(fb.method_30278(), this.depthBeforeTexture, 0, 0, 0, 0, 0, width, height);
        }
        this.capturing = true;
    }

    public void captureAfterHands() {
        if (!this.enabled || !this.capturing) {
            return;
        }
        class_276 fb = this.client.method_1522();
        if (fb == null || fb.method_30277() == null || fb.method_71640() == null || fb.field_1482 != this.lastWidth || fb.field_1481 != this.lastHeight) {
            this.capturing = false;
            return;
        }
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.copyTextureToTexture(fb.method_30277(), this.sceneAfterTexture, 0, 0, 0, 0, 0, this.lastWidth, this.lastHeight);
        this.maskDiff.createMask(this.maskTextureView, this.sceneBeforeTextureView, this.sceneAfterTextureView, this.depthBeforeTextureView, fb.method_71640(), this.lastWidth, this.lastHeight);
    }

    public void renderEffect() {
        boolean balatroMode;
        float blurOffset;
        if (!this.enabled || !this.capturing) {
            return;
        }
        class_276 fb = this.client.method_1522();
        if (fb == null || fb.method_30277() == null) {
            this.capturing = false;
            return;
        }
        int blurIterations = Math.max(2, Math.min(4, Math.round(this.glowDispersion * 0.45f)));
        GpuTextureView glowView = this.auraBlur.blur(this.maskTexture, this.maskTextureView, this.lastWidth, this.lastHeight, blurIterations, blurOffset = Math.max(1.0f, this.glowRadius * 90.0f));
        if (glowView == null) {
            this.capturing = false;
            return;
        }
        GpuTextureView compositeMaskView = this.maskTextureView;
        GpuTextureView compositeGlowView = glowView;
        boolean bl = balatroMode = this.mode == 10 || this.mode == 12 || this.mode == 15;
        if (this.mode == 16) {
            GpuTextureView temporalGlow = this.maskSmoothing.smoothTemporal(glowView, this.lastWidth, this.lastHeight, 2.65f, 0.94f);
            if (temporalGlow != null) {
                compositeGlowView = temporalGlow;
            }
        } else {
            GpuTextureView spatialMask;
            this.maskSmoothing.stopTemporalHistory();
            if (balatroMode && (spatialMask = this.maskSmoothing.smoothSpatial(this.maskTextureView, this.lastWidth, this.lastHeight, 1.35f)) != null) {
                compositeMaskView = spatialMask;
            }
        }
        this.handEffect.composite(fb.method_71639(), this.sceneBeforeTextureView, this.sceneAfterTextureView, compositeMaskView, compositeGlowView, this.lastWidth, this.lastHeight, this.mode, this.color1, this.color2, this.intensity, this.speed, this.glowRadius, this.glowDispersion, this.glowFill);
        this.capturing = false;
    }

    public void invalidate() {
        this.cleanupTextures();
        if (this.maskDiff != null) {
            this.maskDiff.close();
        }
        if (this.auraBlur != null) {
            this.auraBlur.close();
        }
        if (this.handEffect != null) {
            this.handEffect.close();
        }
        if (this.maskSmoothing != null) {
            this.maskSmoothing.close();
        }
        this.maskDiff = null;
        this.auraBlur = null;
        this.handEffect = null;
        this.maskSmoothing = null;
        this.lastWidth = 0;
        this.lastHeight = 0;
        this.initialized = false;
        this.capturing = false;
    }

    public void close() {
        this.cleanupTextures();
        if (this.maskDiff != null) {
            this.maskDiff.close();
            this.maskDiff = null;
        }
        if (this.auraBlur != null) {
            this.auraBlur.close();
            this.auraBlur = null;
        }
        if (this.handEffect != null) {
            this.handEffect.close();
            this.handEffect = null;
        }
        if (this.maskSmoothing != null) {
            this.maskSmoothing.close();
            this.maskSmoothing = null;
        }
        this.initialized = false;
    }

    private void cleanupTextures() {
        if (this.sceneBeforeTextureView != null) {
            this.sceneBeforeTextureView.close();
            this.sceneBeforeTextureView = null;
        }
        if (this.sceneBeforeTexture != null) {
            this.sceneBeforeTexture.close();
            this.sceneBeforeTexture = null;
        }
        if (this.sceneAfterTextureView != null) {
            this.sceneAfterTextureView.close();
            this.sceneAfterTextureView = null;
        }
        if (this.sceneAfterTexture != null) {
            this.sceneAfterTexture.close();
            this.sceneAfterTexture = null;
        }
        if (this.depthBeforeTextureView != null) {
            this.depthBeforeTextureView.close();
            this.depthBeforeTextureView = null;
        }
        if (this.depthBeforeTexture != null) {
            this.depthBeforeTexture.close();
            this.depthBeforeTexture = null;
        }
        if (this.maskTextureView != null) {
            this.maskTextureView.close();
            this.maskTextureView = null;
        }
        if (this.maskTexture != null) {
            this.maskTexture.close();
            this.maskTexture = null;
        }
    }
}

