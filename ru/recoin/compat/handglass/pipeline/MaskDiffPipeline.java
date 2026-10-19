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
 *  com.mojang.blaze3d.textures.GpuTextureView
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
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
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

public class MaskDiffPipeline {
    private static final class_2960 PIPELINE_ID = class_2960.method_60655((String)"minecraft", (String)"pipeline/mask_diff");
    private static final class_2960 VERTEX_SHADER = class_2960.method_60655((String)"minecraft", (String)"core/mask_diff");
    private static final class_2960 FRAGMENT_SHADER = class_2960.method_60655((String)"minecraft", (String)"core/mask_diff");
    private static final BlendFunction REPLACE_BLEND = new BlendFunction(SourceFactor.ONE, DestFactor.ZERO, SourceFactor.ONE, DestFactor.ZERO);
    private static final RenderPipeline PIPELINE = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_60125}).withLocation(PIPELINE_ID).withVertexShader(VERTEX_SHADER).withFragmentShader(FRAGMENT_SHADER).withVertexFormat(class_290.field_60033, VertexFormat.class_5596.field_27379).withUniform("MaskData", class_10789.field_60031).withSampler("BeforeSampler").withSampler("AfterSampler").withSampler("DepthBeforeSampler").withSampler("DepthAfterSampler").withBlend(REPLACE_BLEND).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private static final Vector3f MODEL_OFFSET = new Vector3f(0.0f, 0.0f, 0.0f);
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private static final int BUFFER_SIZE = 16;
    private GpuBuffer uniformBuffer;
    private GpuBuffer dummyVertexBuffer;
    private ByteBuffer dataBuffer;
    private boolean initialized = false;

    private void ensureInitialized() {
        if (this.initialized) {
            return;
        }
        this.dataBuffer = MemoryUtil.memAlloc((int)16);
        ByteBuffer dummyData = MemoryUtil.memAlloc((int)4);
        dummyData.putInt(0);
        dummyData.flip();
        this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:mask_diff_dummy_vertex", 32, dummyData);
        MemoryUtil.memFree((Buffer)dummyData);
        this.initialized = true;
    }

    public void createMask(GpuTextureView targetView, GpuTextureView beforeView, GpuTextureView afterView, GpuTextureView depthBeforeView, GpuTextureView depthAfterView, int width, int height) {
        this.ensureInitialized();
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        class_12137 sampler = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
        class_12137 nearestSampler = RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST);
        this.prepareUniformData(width, height);
        int size = this.dataBuffer.remaining();
        if (this.uniformBuffer == null || this.uniformBuffer.size() < (long)size) {
            if (this.uniformBuffer != null) {
                this.uniformBuffer.close();
            }
            this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:mask_diff_uniform", 136, (long)size);
        }
        encoder.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().method_71106((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)COLOR_MODULATOR, (Vector3fc)MODEL_OFFSET, (Matrix4fc)TEXTURE_MATRIX);
        try (RenderPass renderPass = encoder.createRenderPass(() -> "minecraft:mask_diff_pass", targetView, OptionalInt.of(0));){
            renderPass.setPipeline(PIPELINE);
            renderPass.setVertexBuffer(0, this.dummyVertexBuffer);
            renderPass.bindTexture("BeforeSampler", beforeView, sampler);
            renderPass.bindTexture("AfterSampler", afterView, sampler);
            renderPass.bindTexture("DepthBeforeSampler", depthBeforeView, nearestSampler);
            renderPass.bindTexture("DepthAfterSampler", depthAfterView, nearestSampler);
            RenderSystem.bindDefaultUniforms((RenderPass)renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setUniform("MaskData", this.uniformBuffer);
            renderPass.draw(0, 6);
        }
    }

    private void prepareUniformData(int width, int height) {
        this.dataBuffer.clear();
        this.dataBuffer.putFloat(width);
        this.dataBuffer.putFloat(height);
        this.dataBuffer.putFloat(0.0f);
        this.dataBuffer.putFloat(0.0f);
        this.dataBuffer.flip();
    }

    public void close() {
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

