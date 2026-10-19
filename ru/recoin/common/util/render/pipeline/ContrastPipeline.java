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
 *  net.minecraft.class_276
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
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
import net.minecraft.class_276;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryUtil;

public final class ContrastPipeline {
    private static final class_2960 SHADER = class_2960.method_60655((String)"minecraft", (String)"core/recoin_contrast");
    private static final BlendFunction REPLACE_BLEND = new BlendFunction(SourceFactor.ONE, DestFactor.ZERO, SourceFactor.ONE, DestFactor.ZERO);
    private static final RenderPipeline PIPELINE = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_60125}).withLocation(class_2960.method_60655((String)"recoin", (String)"pipeline/contrast")).withVertexShader(SHADER).withFragmentShader(SHADER).withVertexFormat(class_290.field_60033, VertexFormat.class_5596.field_27379).withUniform("ContrastData", class_10789.field_60031).withSampler("MainSampler").withBlend(REPLACE_BLEND).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private static final ContrastPipeline INSTANCE = new ContrastPipeline();
    private GpuBuffer uniformBuffer;
    private GpuBuffer dummyVertexBuffer;
    private ByteBuffer dataBuffer;
    private GpuTexture copyTexture;
    private GpuTextureView copyView;
    private int lastWidth;
    private int lastHeight;

    private ContrastPipeline() {
    }

    public static void render(float strength) {
        INSTANCE.draw(strength);
    }

    public static void close() {
        INSTANCE.release();
    }

    private void draw(float strength) {
        class_310 client = class_310.method_1551();
        class_276 framebuffer = client.method_1522();
        if (framebuffer == null || framebuffer.method_30277() == null || framebuffer.method_71639() == null) {
            return;
        }
        this.ensureInitialized();
        this.ensureTextures(framebuffer.field_1482, framebuffer.field_1481);
        this.dataBuffer.clear();
        this.dataBuffer.putFloat(Math.max(0.0f, Math.min(1.0f, strength)));
        this.dataBuffer.putFloat(0.0f).putFloat(0.0f).putFloat(0.0f).flip();
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.copyTextureToTexture(framebuffer.method_30277(), this.copyTexture, 0, 0, 0, 0, 0, framebuffer.field_1482, framebuffer.field_1481);
        encoder.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().method_71106((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)COLOR_MODULATOR, (Vector3fc)MODEL_OFFSET, (Matrix4fc)TEXTURE_MATRIX);
        class_12137 sampler = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
        try (RenderPass pass = encoder.createRenderPass(() -> "recoin:contrast_pass", framebuffer.method_71639(), OptionalInt.empty());){
            pass.setPipeline(PIPELINE);
            pass.setVertexBuffer(0, this.dummyVertexBuffer);
            pass.bindTexture("MainSampler", this.copyView, sampler);
            RenderSystem.bindDefaultUniforms((RenderPass)pass);
            pass.setUniform("DynamicTransforms", transforms);
            pass.setUniform("ContrastData", this.uniformBuffer);
            pass.draw(0, 6);
        }
    }

    private void ensureInitialized() {
        if (this.dataBuffer != null) {
            return;
        }
        this.dataBuffer = MemoryUtil.memAlloc((int)16);
        this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "recoin:contrast_uniform", 136, 16L);
        ByteBuffer dummy = MemoryUtil.memAlloc((int)4).putInt(0).flip();
        this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "recoin:contrast_vertex", 32, dummy);
        MemoryUtil.memFree((Buffer)dummy);
    }

    private void ensureTextures(int width, int height) {
        if (this.copyTexture != null && width == this.lastWidth && height == this.lastHeight) {
            return;
        }
        if (this.copyView != null) {
            this.copyView.close();
        }
        if (this.copyTexture != null) {
            this.copyTexture.close();
        }
        this.copyTexture = RenderSystem.getDevice().createTexture(() -> "recoin:contrast_copy", 5, TextureFormat.RGBA8, width, height, 1, 1);
        this.copyView = RenderSystem.getDevice().createTextureView(this.copyTexture);
        this.lastWidth = width;
        this.lastHeight = height;
    }

    private void release() {
        if (this.copyView != null) {
            this.copyView.close();
        }
        if (this.copyTexture != null) {
            this.copyTexture.close();
        }
        if (this.uniformBuffer != null) {
            this.uniformBuffer.close();
        }
        if (this.dummyVertexBuffer != null) {
            this.dummyVertexBuffer.close();
        }
        if (this.dataBuffer != null) {
            MemoryUtil.memFree((Buffer)this.dataBuffer);
        }
        this.copyView = null;
        this.copyTexture = null;
        this.uniformBuffer = null;
        this.dummyVertexBuffer = null;
        this.dataBuffer = null;
        this.lastWidth = 0;
        this.lastHeight = 0;
    }
}

