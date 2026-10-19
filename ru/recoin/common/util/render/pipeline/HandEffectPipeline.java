/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Builder
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.DepthTestFunction
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
package ru.recoin.common.util.render.pipeline;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
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
import ru.recoin.common.util.render.pipeline.EffectUniforms;
import ru.recoin.common.util.render.pipeline.MaskCoveragePipeline;

public class HandEffectPipeline {
    private static final long TIME_WRAP_NANOS = 180000000000L;
    private static final class_2960 PIPELINE_ID = class_2960.method_60655((String)"recoin", (String)"pipeline/hand_effect");
    private static final class_2960 SHADER = class_2960.method_60655((String)"minecraft", (String)"core/recoin_hand_effect");
    private static final int BUFFER_SIZE = 64;
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private static final Vector3f MODEL_OFFSET = new Vector3f(0.0f, 0.0f, 0.0f);
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private static final RenderPipeline PIPELINE = HandEffectPipeline.createPipeline(false);
    private static final RenderPipeline AURA_PIPELINE = HandEffectPipeline.createPipeline(true);
    private GpuBuffer dummyVertexBuffer;
    private ByteBuffer dataBuffer;
    private boolean initialized;
    private final MaskCoveragePipeline auraCoverage = new MaskCoveragePipeline();

    private static RenderPipeline createPipeline(boolean aura) {
        RenderPipeline.Builder builder = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_60125}).withLocation(aura ? class_2960.method_60655((String)"recoin", (String)"pipeline/hand_aura") : PIPELINE_ID).withVertexShader(SHADER).withFragmentShader(SHADER).withVertexFormat(class_290.field_60033, VertexFormat.class_5596.field_27379).withUniform("HandEffectData", class_10789.field_60031).withSampler("SceneSampler").withSampler("SceneBeforeSampler").withSampler("MaskSampler").withSampler("GlowSampler").withSampler("CoverageSampler").withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false);
        if (aura) {
            builder.withShaderDefine("AURA_ONLY");
        }
        return class_10799.method_67887((RenderPipeline)builder.build());
    }

    private void ensureInitialized() {
        if (this.initialized) {
            return;
        }
        this.dataBuffer = MemoryUtil.memAlloc((int)64);
        ByteBuffer dummyData = MemoryUtil.memAlloc((int)4);
        dummyData.putInt(0);
        dummyData.flip();
        this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "recoin:hand_effect_dummy_vertex", 32, dummyData);
        MemoryUtil.memFree((Buffer)dummyData);
        this.initialized = true;
    }

    public void composite(GpuTextureView targetView, GpuTextureView sceneBeforeView, GpuTextureView sceneView, GpuTextureView maskView, GpuTextureView glowView, int width, int height, int mode, int color1, int color2, float intensity, float speed, float glowRadius, float glowDispersion, boolean glowFill) {
        this.ensureInitialized();
        GpuTextureView coverageView = mode == 16 ? this.auraCoverage.draw(glowView, true) : glowView;
        this.prepareUniformData(width, height, mode, color1, color2, intensity, speed, glowRadius, glowDispersion, glowFill);
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        GpuBufferSlice effectUniform1 = EffectUniforms.write(this.dataBuffer);
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().method_71106((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)COLOR_MODULATOR, (Vector3fc)MODEL_OFFSET, (Matrix4fc)TEXTURE_MATRIX);
        class_12137 linearSampler = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
        try (RenderPass renderPass = encoder.createRenderPass(() -> "recoin:hand_effect_pass", targetView, OptionalInt.empty());){
            renderPass.setPipeline(mode == 16 ? AURA_PIPELINE : PIPELINE);
            renderPass.setVertexBuffer(0, this.dummyVertexBuffer);
            renderPass.bindTexture("SceneSampler", sceneView, linearSampler);
            renderPass.bindTexture("SceneBeforeSampler", sceneBeforeView, linearSampler);
            renderPass.bindTexture("MaskSampler", maskView, linearSampler);
            renderPass.bindTexture("GlowSampler", glowView, linearSampler);
            renderPass.bindTexture("CoverageSampler", coverageView, RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST));
            RenderSystem.bindDefaultUniforms((RenderPass)renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setUniform("HandEffectData", effectUniform1);
            renderPass.draw(0, 6);
        }
    }

    private void prepareUniformData(int width, int height, int mode, int color1, int color2, float intensity, float speed, float glowRadius, float glowDispersion, boolean glowFill) {
        this.dataBuffer.clear();
        this.dataBuffer.putFloat(width);
        this.dataBuffer.putFloat(height);
        float wrappedTime = (float)(System.nanoTime() % 180000000000L) / 1.0E9f;
        this.dataBuffer.putFloat(wrappedTime * Math.max(0.01f, speed));
        this.dataBuffer.putFloat(mode);
        this.putColor(color1);
        this.putColor(color2);
        this.dataBuffer.putFloat(intensity);
        this.dataBuffer.putFloat(glowRadius);
        this.dataBuffer.putFloat(glowDispersion);
        this.dataBuffer.putFloat(glowFill ? 1.0f : 0.0f);
        this.dataBuffer.flip();
    }

    private void putColor(int color) {
        this.dataBuffer.putFloat((float)(color >> 16 & 0xFF) / 255.0f);
        this.dataBuffer.putFloat((float)(color >> 8 & 0xFF) / 255.0f);
        this.dataBuffer.putFloat((float)(color & 0xFF) / 255.0f);
        this.dataBuffer.putFloat((float)(color >> 24 & 0xFF) / 255.0f);
    }

    public void close() {
        this.auraCoverage.close();
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

