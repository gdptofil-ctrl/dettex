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
package ru.recoin.compat.handglass.pipeline;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
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

public class KawaseBlurPipeline {
    private static final class_2960 DOWN_PIPELINE_ID = class_2960.method_60655((String)"minecraft", (String)"pipeline/kawase_down");
    private static final class_2960 DOWN_VERTEX_SHADER = class_2960.method_60655((String)"minecraft", (String)"core/kawase_down");
    private static final class_2960 DOWN_FRAGMENT_SHADER = class_2960.method_60655((String)"minecraft", (String)"core/kawase_down");
    private static final class_2960 UP_PIPELINE_ID = class_2960.method_60655((String)"minecraft", (String)"pipeline/kawase_up");
    private static final class_2960 UP_VERTEX_SHADER = class_2960.method_60655((String)"minecraft", (String)"core/kawase_up");
    private static final class_2960 UP_FRAGMENT_SHADER = class_2960.method_60655((String)"minecraft", (String)"core/kawase_up");
    private static final RenderPipeline DOWN_PIPELINE = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_60125}).withLocation(DOWN_PIPELINE_ID).withVertexShader(DOWN_VERTEX_SHADER).withFragmentShader(DOWN_FRAGMENT_SHADER).withVertexFormat(class_290.field_60033, VertexFormat.class_5596.field_27379).withUniform("KawaseData", class_10789.field_60031).withSampler("Sampler0").withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());
    private static final RenderPipeline UP_PIPELINE = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_60125}).withLocation(UP_PIPELINE_ID).withVertexShader(UP_VERTEX_SHADER).withFragmentShader(UP_FRAGMENT_SHADER).withVertexFormat(class_290.field_60033, VertexFormat.class_5596.field_27379).withUniform("KawaseData", class_10789.field_60031).withSampler("Sampler0").withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private static final Vector3f MODEL_OFFSET = new Vector3f(0.0f, 0.0f, 0.0f);
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private static final int MAX_ITERATIONS = 8;
    private static final int BUFFER_SIZE = 32;
    private final GpuBuffer[] passUniforms = new GpuBuffer[17];
    private final int[] uniformWidths = new int[17];
    private final int[] uniformHeights = new int[17];
    private final float[] uniformOffsets = new float[17];
    private GpuBuffer dummyVertexBuffer;
    private ByteBuffer dataBuffer;
    private GpuTexture[] downTextures;
    private GpuTextureView[] downTextureViews;
    private GpuTexture[] upTextures;
    private GpuTextureView[] upTextureViews;
    private int[] downWidths;
    private int[] downHeights;
    private int[] upWidths;
    private int[] upHeights;
    private GpuTexture finalTexture;
    private GpuTextureView finalTextureView;
    private int lastWidth = 0;
    private int lastHeight = 0;
    private boolean initialized = false;

    private void ensureInitialized() {
        if (this.initialized) {
            return;
        }
        this.dataBuffer = MemoryUtil.memAlloc((int)32);
        ByteBuffer dummyData = MemoryUtil.memAlloc((int)4);
        dummyData.putInt(0);
        dummyData.flip();
        this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:kawase_dummy_vertex", 32, dummyData);
        MemoryUtil.memFree((Buffer)dummyData);
        this.downTextures = new GpuTexture[8];
        this.downTextureViews = new GpuTextureView[8];
        this.upTextures = new GpuTexture[8];
        this.upTextureViews = new GpuTextureView[8];
        this.downWidths = new int[8];
        this.downHeights = new int[8];
        this.upWidths = new int[8];
        this.upHeights = new int[8];
        this.initialized = true;
    }

    private void ensureFramebuffers(int width, int height) {
        if (width == this.lastWidth && height == this.lastHeight) {
            return;
        }
        this.cleanupFramebuffers();
        this.finalTexture = RenderSystem.getDevice().createTexture(() -> "minecraft:kawase_final", 13, TextureFormat.RGBA8, width, height, 1, 1);
        this.finalTextureView = RenderSystem.getDevice().createTextureView(this.finalTexture);
        int w = width;
        int h = height;
        for (int i = 0; i < 8; ++i) {
            w = Math.max(1, w / 2);
            h = Math.max(1, h / 2);
            int index = i;
            int fw = w;
            int fh = h;
            this.downWidths[i] = fw;
            this.downHeights[i] = fh;
            this.upWidths[i] = fw;
            this.upHeights[i] = fh;
            this.downTextures[i] = RenderSystem.getDevice().createTexture(() -> "minecraft:kawase_down_" + index, 13, TextureFormat.RGBA8, fw, fh, 1, 1);
            this.downTextureViews[i] = RenderSystem.getDevice().createTextureView(this.downTextures[i]);
            this.upTextures[i] = RenderSystem.getDevice().createTexture(() -> "minecraft:kawase_up_" + index, 13, TextureFormat.RGBA8, fw, fh, 1, 1);
            this.upTextureViews[i] = RenderSystem.getDevice().createTextureView(this.upTextures[i]);
        }
        this.lastWidth = width;
        this.lastHeight = height;
    }

    private void cleanupFramebuffers() {
        if (this.finalTextureView != null) {
            this.finalTextureView.close();
            this.finalTextureView = null;
        }
        if (this.finalTexture != null) {
            this.finalTexture.close();
            this.finalTexture = null;
        }
        if (this.downTextureViews != null) {
            for (int i = 0; i < 8; ++i) {
                if (this.downTextureViews[i] != null) {
                    this.downTextureViews[i].close();
                    this.downTextureViews[i] = null;
                }
                if (this.downTextures[i] != null) {
                    this.downTextures[i].close();
                    this.downTextures[i] = null;
                }
                if (this.upTextureViews[i] != null) {
                    this.upTextureViews[i].close();
                    this.upTextureViews[i] = null;
                }
                if (this.upTextures[i] == null) continue;
                this.upTextures[i].close();
                this.upTextures[i] = null;
            }
        }
    }

    public GpuTextureView blur(GpuTexture sourceTexture, GpuTextureView sourceView, int width, int height, int iterations, float offset) {
        int i;
        if (sourceTexture == null || sourceView == null) {
            return null;
        }
        this.ensureInitialized();
        this.ensureFramebuffers(width, height);
        iterations = Math.min(iterations, 8);
        if (iterations < 1) {
            iterations = 1;
        }
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        class_12137 sampler = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().method_71106((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)COLOR_MODULATOR, (Vector3fc)MODEL_OFFSET, (Matrix4fc)TEXTURE_MATRIX);
        GpuTextureView currentSource = sourceView;
        int currentWidth = width;
        int currentHeight = height;
        for (i = 0; i < iterations; ++i) {
            int targetWidth = this.downWidths[i];
            int targetHeight = this.downHeights[i];
            GpuBuffer uniformBuffer = this.uniformForPass(encoder, i, currentWidth, currentHeight, offset);
            int finalI = i;
            try (RenderPass renderPass = encoder.createRenderPass(() -> "minecraft:kawase_down_pass_" + finalI, this.downTextureViews[i], OptionalInt.empty());){
                renderPass.setPipeline(DOWN_PIPELINE);
                renderPass.setVertexBuffer(0, this.dummyVertexBuffer);
                renderPass.bindTexture("Sampler0", currentSource, sampler);
                RenderSystem.bindDefaultUniforms((RenderPass)renderPass);
                renderPass.setUniform("DynamicTransforms", dynamicTransforms);
                renderPass.setUniform("KawaseData", uniformBuffer);
                renderPass.draw(0, 6);
            }
            currentSource = this.downTextureViews[i];
            currentWidth = targetWidth;
            currentHeight = targetHeight;
        }
        for (i = iterations - 1; i >= 0; --i) {
            GpuBuffer uniformBuffer = this.uniformForPass(encoder, 8 + i, currentWidth, currentHeight, offset);
            int finalI = i;
            try (RenderPass renderPass = encoder.createRenderPass(() -> "minecraft:kawase_up_pass_" + finalI, this.upTextureViews[i], OptionalInt.empty());){
                renderPass.setPipeline(UP_PIPELINE);
                renderPass.setVertexBuffer(0, this.dummyVertexBuffer);
                renderPass.bindTexture("Sampler0", currentSource, sampler);
                RenderSystem.bindDefaultUniforms((RenderPass)renderPass);
                renderPass.setUniform("DynamicTransforms", dynamicTransforms);
                renderPass.setUniform("KawaseData", uniformBuffer);
                renderPass.draw(0, 6);
            }
            currentSource = this.upTextureViews[i];
            currentWidth = this.upWidths[i];
            currentHeight = this.upHeights[i];
        }
        GpuBuffer uniformBuffer = this.uniformForPass(encoder, 16, currentWidth, currentHeight, offset);
        try (RenderPass renderPass = encoder.createRenderPass(() -> "minecraft:kawase_final_pass", this.finalTextureView, OptionalInt.empty());){
            renderPass.setPipeline(UP_PIPELINE);
            renderPass.setVertexBuffer(0, this.dummyVertexBuffer);
            renderPass.bindTexture("Sampler0", currentSource, sampler);
            RenderSystem.bindDefaultUniforms((RenderPass)renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setUniform("KawaseData", uniformBuffer);
            renderPass.draw(0, 6);
        }
        return this.finalTextureView;
    }

    private GpuBuffer uniformForPass(CommandEncoder encoder, int index, int width, int height, float offset) {
        GpuBuffer buffer = this.passUniforms[index];
        if (buffer != null && this.uniformWidths[index] == width && this.uniformHeights[index] == height && this.uniformOffsets[index] == offset) {
            return buffer;
        }
        if (buffer == null) {
            this.passUniforms[index] = buffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:kawase_uniform_" + index, 136, 16L);
        }
        this.prepareUniformData(width, height, offset);
        encoder.writeToBuffer(buffer.slice(), this.dataBuffer);
        this.uniformWidths[index] = width;
        this.uniformHeights[index] = height;
        this.uniformOffsets[index] = offset;
        return buffer;
    }

    private void prepareUniformData(int width, int height, float offset) {
        this.dataBuffer.clear();
        this.dataBuffer.putFloat(width);
        this.dataBuffer.putFloat(height);
        this.dataBuffer.putFloat(offset);
        this.dataBuffer.putFloat(0.0f);
        this.dataBuffer.flip();
    }

    public void close() {
        this.cleanupFramebuffers();
        for (int i = 0; i < this.passUniforms.length; ++i) {
            if (this.passUniforms[i] == null) continue;
            this.passUniforms[i].close();
            this.passUniforms[i] = null;
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

