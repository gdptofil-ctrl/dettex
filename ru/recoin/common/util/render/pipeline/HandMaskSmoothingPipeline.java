/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.DepthTestFunction
 *  com.mojang.blaze3d.platform.DestFactor
 *  com.mojang.blaze3d.platform.SourceFactor
 *  com.mojang.blaze3d.systems.CommandEncoder
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.FilterMode
 *  com.mojang.blaze3d.textures.GpuTexture
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.textures.TextureFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_10789
 *  net.minecraft.class_10799
 *  net.minecraft.class_12137
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 *  org.lwjgl.system.MemoryUtil
 */
package ru.recoin.common.util.render.pipeline;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import net.minecraft.class_10789;
import net.minecraft.class_10799;
import net.minecraft.class_12137;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryUtil;

public final class HandMaskSmoothingPipeline {
    private static final class_2960 PIPELINE_ID = class_2960.method_60655((String)"recoin", (String)"pipeline/hand_mask_smoothing");
    private static final class_2960 SHADER = class_2960.method_60655((String)"minecraft", (String)"core/recoin_hand_mask_smoothing");
    private static final int BUFFER_SIZE = 16;
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private static final Vector3f MODEL_OFFSET = new Vector3f(0.0f, 0.0f, 0.0f);
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private static final BlendFunction REPLACE_BLEND = new BlendFunction(SourceFactor.ONE, DestFactor.ZERO, SourceFactor.ONE, DestFactor.ZERO);
    private static final RenderPipeline PIPELINE = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_60125}).withLocation(PIPELINE_ID).withVertexShader(SHADER).withFragmentShader(SHADER).withVertexFormat(class_290.field_60033, VertexFormat.class_5596.field_27379).withUniform("HandMaskSmoothData", class_10789.field_60031).withSampler("CurrentSampler").withSampler("HistorySampler").withBlend(REPLACE_BLEND).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());
    private GpuBuffer uniformBuffer;
    private GpuBuffer dummyVertexBuffer;
    private ByteBuffer dataBuffer;
    private GpuTexture historyTextureA;
    private GpuTexture historyTextureB;
    private GpuTexture spatialTexture;
    private GpuTextureView historyViewA;
    private GpuTextureView historyViewB;
    private GpuTextureView spatialView;
    private int width;
    private int height;
    private boolean historyAIsCurrent = true;
    private boolean historyValid;
    private boolean initialized;

    private void ensureInitialized() {
        if (this.initialized) {
            return;
        }
        this.dataBuffer = MemoryUtil.memAlloc((int)16);
        ByteBuffer dummyData = MemoryUtil.memAlloc((int)4);
        dummyData.putInt(0);
        dummyData.flip();
        this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "recoin:hand_mask_smoothing_dummy_vertex", 32, dummyData);
        MemoryUtil.memFree((Buffer)dummyData);
        this.initialized = true;
    }

    private void ensureTextures(int targetWidth, int targetHeight) {
        if (this.historyTextureA != null && this.width == targetWidth && this.height == targetHeight) {
            return;
        }
        this.cleanupTextures();
        int usage = 13;
        this.historyTextureA = this.createTexture("recoin:hand_mask_history_a", targetWidth, targetHeight, usage);
        this.historyTextureB = this.createTexture("recoin:hand_mask_history_b", targetWidth, targetHeight, usage);
        this.spatialTexture = this.createTexture("recoin:hand_mask_spatial", targetWidth, targetHeight, usage);
        this.historyViewA = RenderSystem.getDevice().createTextureView(this.historyTextureA);
        this.historyViewB = RenderSystem.getDevice().createTextureView(this.historyTextureB);
        this.spatialView = RenderSystem.getDevice().createTextureView(this.spatialTexture);
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.clearColorTexture(this.historyTextureA, 0);
        encoder.clearColorTexture(this.historyTextureB, 0);
        encoder.clearColorTexture(this.spatialTexture, 0);
        this.width = targetWidth;
        this.height = targetHeight;
        this.historyAIsCurrent = true;
        this.historyValid = false;
    }

    private GpuTexture createTexture(String label, int targetWidth, int targetHeight, int usage) {
        return RenderSystem.getDevice().createTexture(() -> label, usage, TextureFormat.RGBA8, targetWidth, targetHeight, 1, 1);
    }

    public GpuTextureView smoothSpatial(GpuTextureView source, int targetWidth, int targetHeight, float radiusPx) {
        if (source == null) {
            return null;
        }
        this.ensureInitialized();
        this.ensureTextures(targetWidth, targetHeight);
        this.render(source, source, this.spatialView, targetWidth, targetHeight, radiusPx, 0.0f);
        return this.spatialView;
    }

    public GpuTextureView smoothTemporal(GpuTextureView source, int targetWidth, int targetHeight, float radiusPx, float historyFade) {
        if (source == null) {
            return null;
        }
        this.ensureInitialized();
        this.ensureTextures(targetWidth, targetHeight);
        if (!this.historyValid) {
            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            encoder.clearColorTexture(this.historyTextureA, 0);
            encoder.clearColorTexture(this.historyTextureB, 0);
        }
        GpuTextureView history = this.historyAIsCurrent ? this.historyViewA : this.historyViewB;
        GpuTextureView target = this.historyAIsCurrent ? this.historyViewB : this.historyViewA;
        this.render(source, history, target, targetWidth, targetHeight, radiusPx, this.historyValid ? historyFade : 0.0f);
        this.historyAIsCurrent = !this.historyAIsCurrent;
        this.historyValid = true;
        return target;
    }

    public void stopTemporalHistory() {
        this.historyValid = false;
    }

    private void render(GpuTextureView current, GpuTextureView history, GpuTextureView target, int targetWidth, int targetHeight, float radiusPx, float historyFade) {
        this.prepareUniformData(targetWidth, targetHeight, radiusPx, historyFade);
        int size = this.dataBuffer.remaining();
        if (this.uniformBuffer == null || this.uniformBuffer.size() < (long)size) {
            if (this.uniformBuffer != null) {
                this.uniformBuffer.close();
            }
            this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "recoin:hand_mask_smoothing_uniform", 136, (long)size);
        }
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().method_71106((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)COLOR_MODULATOR, (Vector3fc)MODEL_OFFSET, (Matrix4fc)TEXTURE_MATRIX);
        class_12137 linearSampler = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
        try (RenderPass renderPass = encoder.createRenderPass(() -> "recoin:hand_mask_smoothing_pass", target, OptionalInt.empty());){
            renderPass.setPipeline(PIPELINE);
            renderPass.setVertexBuffer(0, this.dummyVertexBuffer);
            renderPass.bindTexture("CurrentSampler", current, linearSampler);
            renderPass.bindTexture("HistorySampler", history, linearSampler);
            RenderSystem.bindDefaultUniforms((RenderPass)renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setUniform("HandMaskSmoothData", this.uniformBuffer);
            renderPass.draw(0, 6);
        }
    }

    private void prepareUniformData(int targetWidth, int targetHeight, float radiusPx, float historyFade) {
        this.dataBuffer.clear();
        this.dataBuffer.putFloat(targetWidth);
        this.dataBuffer.putFloat(targetHeight);
        this.dataBuffer.putFloat(Math.max(0.5f, radiusPx));
        this.dataBuffer.putFloat(Math.max(0.0f, Math.min(0.98f, historyFade)));
        this.dataBuffer.flip();
    }

    private void cleanupTextures() {
        if (this.historyViewA != null) {
            this.historyViewA.close();
        }
        if (this.historyViewB != null) {
            this.historyViewB.close();
        }
        if (this.spatialView != null) {
            this.spatialView.close();
        }
        if (this.historyTextureA != null) {
            this.historyTextureA.close();
        }
        if (this.historyTextureB != null) {
            this.historyTextureB.close();
        }
        if (this.spatialTexture != null) {
            this.spatialTexture.close();
        }
        this.historyViewA = null;
        this.historyViewB = null;
        this.spatialView = null;
        this.historyTextureA = null;
        this.historyTextureB = null;
        this.spatialTexture = null;
        this.width = 0;
        this.height = 0;
        this.historyValid = false;
    }

    public void close() {
        this.cleanupTextures();
        if (this.uniformBuffer != null) {
            this.uniformBuffer.close();
            this.uniformBuffer = null;
        }
        if (this.dummyVertexBuffer != null) {
            this.dummyVertexBuffer.close();
            this.dummyVertexBuffer = null;
        }
        if (this.dataBuffer != null) {
            MemoryUtil.memFree((Buffer)this.dataBuffer);
            this.dataBuffer = null;
        }
        this.initialized = false;
    }
}

