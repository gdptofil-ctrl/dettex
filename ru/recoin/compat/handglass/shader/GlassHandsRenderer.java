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
package ru.recoin.compat.handglass.shader;

import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.class_276;
import net.minecraft.class_310;
import ru.recoin.compat.handglass.pipeline.GlassCompositePipeline;
import ru.recoin.compat.handglass.pipeline.KawaseBlurPipeline;
import ru.recoin.compat.handglass.pipeline.MaskDiffPipeline;

public class GlassHandsRenderer {
    private static GlassHandsRenderer instance;
    private final class_310 client = class_310.method_1551();
    private KawaseBlurPipeline kawaseBlur;
    private GlassCompositePipeline glassComposite;
    private MaskDiffPipeline maskDiff;
    private GpuTexture sceneBeforeTexture;
    private GpuTextureView sceneBeforeTextureView;
    private GpuTexture sceneAfterTexture;
    private GpuTextureView sceneAfterTextureView;
    private GpuTexture depthBeforeTexture;
    private GpuTextureView depthBeforeTextureView;
    private GpuTexture depthAfterTexture;
    private GpuTextureView depthAfterTextureView;
    private GpuTexture maskTexture;
    private GpuTextureView maskTextureView;
    private int lastWidth = 0;
    private int lastHeight = 0;
    private boolean capturing = false;
    private boolean enabled = false;
    private boolean initialized = false;
    private float blurRadius = 6.0f;
    private int blurIterations = 4;
    private float saturation = 1.0f;
    private boolean reflect = true;
    private int tintColor = -4622081;
    private float tintIntensity = 0.2f;
    private float edgeGlowIntensity = 1.35f;
    private float edgeSoftness = 8.0f;
    private float refractionStrength = 1.25f;
    private float refractionReach = 18.0f;
    private float chromaticStrength = 0.7f;
    private float shimmerStrength = 0.45f;
    private float haloReach = 24.0f;

    public GlassHandsRenderer() {
        instance = this;
    }

    public static GlassHandsRenderer getInstance() {
        if (instance == null) {
            instance = new GlassHandsRenderer();
        }
        return instance;
    }

    public static void resetInstance() {
        if (instance != null) {
            instance.close();
            GlassHandsRenderer.instance.initialized = false;
        }
    }

    private void ensureInitialized() {
        if (this.initialized) {
            return;
        }
        if (this.kawaseBlur != null) {
            this.kawaseBlur.close();
        }
        if (this.glassComposite != null) {
            this.glassComposite.close();
        }
        if (this.maskDiff != null) {
            this.maskDiff.close();
        }
        this.kawaseBlur = new KawaseBlurPipeline();
        this.glassComposite = new GlassCompositePipeline();
        this.maskDiff = new MaskDiffPipeline();
        this.lastWidth = 0;
        this.lastHeight = 0;
        this.initialized = true;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            this.ensureInitialized();
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setBlurRadius(float radius) {
        this.blurRadius = radius;
    }

    public void setBlurIterations(int iterations) {
        this.blurIterations = Math.max(1, Math.min(8, iterations));
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }

    public void setReflect(boolean reflect) {
        this.reflect = reflect;
    }

    public void setTintColor(int color) {
        this.tintColor = color;
    }

    public void setTintIntensity(float intensity) {
        this.tintIntensity = intensity;
    }

    public void setEdgeGlowIntensity(float intensity) {
        this.edgeGlowIntensity = Math.max(0.0f, Math.min(3.0f, intensity));
    }

    public void setEdgeSoftness(float softness) {
        this.edgeSoftness = Math.max(0.0f, Math.min(28.0f, softness));
    }

    public void setRefraction(float strength, float reach) {
        this.refractionStrength = Math.max(0.0f, Math.min(3.0f, strength));
        this.refractionReach = Math.max(0.0f, Math.min(80.0f, reach));
    }

    public void setChromaticStrength(float strength) {
        this.chromaticStrength = Math.max(0.0f, Math.min(1.0f, strength));
    }

    public void setShimmerStrength(float strength) {
        this.shimmerStrength = Math.max(0.0f, Math.min(1.0f, strength));
    }

    public void setHaloReach(float reach) {
        this.haloReach = Math.max(1.0f, Math.min(64.0f, reach));
    }

    private void ensureTextures(int width, int height) {
        if (width == this.lastWidth && height == this.lastHeight && this.sceneBeforeTexture != null) {
            return;
        }
        this.cleanupTextures();
        this.sceneBeforeTexture = RenderSystem.getDevice().createTexture(() -> "minecraft:glass_scene_before", 13, TextureFormat.RGBA8, width, height, 1, 1);
        this.sceneBeforeTextureView = RenderSystem.getDevice().createTextureView(this.sceneBeforeTexture);
        this.sceneAfterTexture = RenderSystem.getDevice().createTexture(() -> "minecraft:glass_scene_after", 13, TextureFormat.RGBA8, width, height, 1, 1);
        this.sceneAfterTextureView = RenderSystem.getDevice().createTextureView(this.sceneAfterTexture);
        this.depthBeforeTexture = RenderSystem.getDevice().createTexture(() -> "minecraft:glass_depth_before", 13, TextureFormat.DEPTH32, width, height, 1, 1);
        this.depthBeforeTextureView = RenderSystem.getDevice().createTextureView(this.depthBeforeTexture);
        this.depthAfterTexture = RenderSystem.getDevice().createTexture(() -> "minecraft:glass_depth_after", 13, TextureFormat.DEPTH32, width, height, 1, 1);
        this.depthAfterTextureView = RenderSystem.getDevice().createTextureView(this.depthAfterTexture);
        this.maskTexture = RenderSystem.getDevice().createTexture(() -> "minecraft:glass_mask", 13, TextureFormat.RGBA8, width, height, 1, 1);
        this.maskTextureView = RenderSystem.getDevice().createTextureView(this.maskTexture);
        this.lastWidth = width;
        this.lastHeight = height;
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
        if (this.depthAfterTextureView != null) {
            this.depthAfterTextureView.close();
            this.depthAfterTextureView = null;
        }
        if (this.depthAfterTexture != null) {
            this.depthAfterTexture.close();
            this.depthAfterTexture = null;
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

    public void captureSceneBeforeHands() {
        if (!this.enabled) {
            return;
        }
        this.ensureInitialized();
        class_276 fb = this.client.method_1522();
        if (fb == null || fb.method_30277() == null) {
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

    public void captureSceneAfterHands() {
        if (!this.enabled || !this.capturing) {
            return;
        }
        class_276 fb = this.client.method_1522();
        if (fb == null || fb.method_30277() == null) {
            return;
        }
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.copyTextureToTexture(fb.method_30277(), this.sceneAfterTexture, 0, 0, 0, 0, 0, this.lastWidth, this.lastHeight);
        if (fb.method_30278() != null) {
            encoder.copyTextureToTexture(fb.method_30278(), this.depthAfterTexture, 0, 0, 0, 0, 0, this.lastWidth, this.lastHeight);
        }
    }

    public void renderGlassEffect() {
        if (!this.enabled || !this.capturing) {
            return;
        }
        class_276 fb = this.client.method_1522();
        if (fb == null || fb.method_30277() == null) {
            this.capturing = false;
            return;
        }
        this.maskDiff.createMask(this.maskTextureView, this.sceneBeforeTextureView, this.sceneAfterTextureView, this.depthBeforeTextureView, this.depthAfterTextureView, this.lastWidth, this.lastHeight);
        GpuTextureView blurredView = this.kawaseBlur.blur(this.sceneBeforeTexture, this.sceneBeforeTextureView, this.lastWidth, this.lastHeight, this.blurIterations, this.blurRadius);
        if (blurredView == null) {
            this.capturing = false;
            return;
        }
        this.glassComposite.composite(fb.method_71639(), this.sceneBeforeTextureView, blurredView, this.maskTextureView, this.lastWidth, this.lastHeight, this.saturation, this.reflect, this.tintColor, this.tintIntensity, this.edgeGlowIntensity, this.edgeSoftness, this.refractionStrength, this.refractionReach, this.chromaticStrength, this.shimmerStrength, this.haloReach);
        this.capturing = false;
    }

    public boolean isCapturing() {
        return this.capturing;
    }

    public void invalidate() {
        this.cleanupTextures();
        if (this.kawaseBlur != null) {
            this.kawaseBlur.close();
        }
        if (this.glassComposite != null) {
            this.glassComposite.close();
        }
        if (this.maskDiff != null) {
            this.maskDiff.close();
        }
        this.kawaseBlur = null;
        this.glassComposite = null;
        this.maskDiff = null;
        this.lastWidth = 0;
        this.lastHeight = 0;
        this.initialized = false;
        this.capturing = false;
    }

    public void close() {
        this.cleanupTextures();
        if (this.kawaseBlur != null) {
            this.kawaseBlur.close();
            this.kawaseBlur = null;
        }
        if (this.glassComposite != null) {
            this.glassComposite.close();
            this.glassComposite = null;
        }
        if (this.maskDiff != null) {
            this.maskDiff.close();
            this.maskDiff = null;
        }
        this.lastWidth = 0;
        this.lastHeight = 0;
        this.initialized = false;
    }
}

