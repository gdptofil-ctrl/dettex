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

public final class MotionBlurPipeline {
    private static final class_2960 SHADER = class_2960.method_60655((String)"minecraft", (String)"core/recoin_motion_blur");
    private static final RenderPipeline PIPELINE = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_60125}).withLocation(class_2960.method_60655((String)"recoin", (String)"pipeline/motion_blur")).withVertexShader(SHADER).withFragmentShader(SHADER).withVertexFormat(class_290.field_60033, VertexFormat.class_5596.field_27379).withUniform("MotionBlurData", class_10789.field_60031).withSampler("MainSampler").withSampler("DepthSampler").withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());
    private static final int BUFFER_SIZE = 320;
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private GpuBuffer uniformBuffer;
    private GpuBuffer dummyVertex;
    private ByteBuffer data;
    private GpuTexture copyTexture;
    private GpuTextureView copyView;
    private int lastWidth;
    private int lastHeight;

    public void draw(Matrix4f mvInverse, Matrix4f projInverse, Matrix4f prevModelView, Matrix4f prevProjection, Vector3f cameraPos, Vector3f prevCameraPos, float strength, int samples, boolean centered, boolean useDepth) {
        class_310 client = class_310.method_1551();
        class_276 fb = client.method_1522();
        if (fb == null || fb.method_30277() == null) {
            return;
        }
        this.ensure();
        int w = fb.field_1482;
        int h = fb.field_1481;
        this.ensureTextures(w, h);
        this.data.clear();
        mvInverse.get(this.data);
        this.data.position(this.data.position() + 64);
        projInverse.get(this.data);
        this.data.position(this.data.position() + 64);
        prevModelView.get(this.data);
        this.data.position(this.data.position() + 64);
        prevProjection.get(this.data);
        this.data.position(this.data.position() + 64);
        this.data.putFloat(cameraPos.x).putFloat(cameraPos.y).putFloat(cameraPos.z).putFloat(0.0f);
        this.data.putFloat(prevCameraPos.x).putFloat(prevCameraPos.y).putFloat(prevCameraPos.z).putFloat(0.0f);
        this.data.putFloat(strength).putFloat(samples).putFloat(centered ? 1.0f : 0.0f).putFloat(useDepth ? 1.0f : 0.0f);
        this.data.flip();
        int sz = this.data.remaining();
        if (this.uniformBuffer == null || this.uniformBuffer.size() < (long)sz) {
            if (this.uniformBuffer != null) {
                this.uniformBuffer.close();
            }
            this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "recoin:motion_blur_uniform", 136, (long)sz);
        }
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.copyTextureToTexture(fb.method_30277(), this.copyTexture, 0, 0, 0, 0, 0, w, h);
        encoder.writeToBuffer(this.uniformBuffer.slice(), this.data);
        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().method_71106((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)COLOR_MODULATOR, (Vector3fc)MODEL_OFFSET, (Matrix4fc)TEXTURE_MATRIX);
        class_12137 linear = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
        class_12137 nearest = RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST);
        try (RenderPass pass = encoder.createRenderPass(() -> "recoin:motion_blur_pass", fb.method_71639(), OptionalInt.empty());){
            pass.setPipeline(PIPELINE);
            pass.setVertexBuffer(0, this.dummyVertex);
            pass.bindTexture("MainSampler", this.copyView, linear);
            try {
                GpuTextureView depthView = fb.method_71640();
                if (depthView != null) {
                    pass.bindTexture("DepthSampler", depthView, nearest);
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            RenderSystem.bindDefaultUniforms((RenderPass)pass);
            pass.setUniform("DynamicTransforms", transforms);
            pass.setUniform("MotionBlurData", this.uniformBuffer);
            pass.draw(0, 6);
        }
    }

    private void ensure() {
        if (this.data != null) {
            return;
        }
        this.data = MemoryUtil.memAlloc((int)320);
        ByteBuffer d = MemoryUtil.memAlloc((int)4).putInt(0).flip();
        this.dummyVertex = RenderSystem.getDevice().createBuffer(() -> "recoin:motion_blur_vertex", 32, d);
        MemoryUtil.memFree((Buffer)d);
    }

    private void ensureTextures(int w, int h) {
        if (this.copyTexture != null && this.lastWidth == w && this.lastHeight == h) {
            return;
        }
        if (this.copyView != null) {
            this.copyView.close();
            this.copyView = null;
        }
        if (this.copyTexture != null) {
            this.copyTexture.close();
            this.copyTexture = null;
        }
        this.copyTexture = RenderSystem.getDevice().createTexture(() -> "recoin:motion_blur_copy", 5, TextureFormat.RGBA8, w, h, 1, 1);
        this.copyView = RenderSystem.getDevice().createTextureView(this.copyTexture);
        this.lastWidth = w;
        this.lastHeight = h;
    }
}

