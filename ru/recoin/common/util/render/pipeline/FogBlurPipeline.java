/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Builder
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
 *  org.lwjgl.system.MemoryUtil
 */
package ru.recoin.common.util.render.pipeline;

import com.mojang.blaze3d.buffers.GpuBuffer;
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
import org.lwjgl.system.MemoryUtil;

public final class FogBlurPipeline
implements AutoCloseable {
    private static final float BUFFER_SCALE = 0.35f;
    private static final class_2960 VERTEX = class_2960.method_60655((String)"minecraft", (String)"core/recoin_fog_blur");
    private static final RenderPipeline PREPARE = FogBlurPipeline.pipeline("fog_blur_prepare", "FogBlurData", "MainSampler", "DepthSampler");
    private static final RenderPipeline GAUSSIAN = FogBlurPipeline.pipeline("fog_gaussian", "FogBlurPass", "MainSampler");
    private static final RenderPipeline COMPOSITE = FogBlurPipeline.pipeline("fog_blur", "FogBlurData", "MainSampler", "BlurSampler", "DepthSampler");
    private final Matrix4f inverseProjection = new Matrix4f();
    private final GpuBuffer[] blurUniforms = new GpuBuffer[2];
    private final GpuTexture[] blurTextures = new GpuTexture[3];
    private final GpuTextureView[] blurViews = new GpuTextureView[3];
    private GpuBuffer uniforms;
    private GpuBuffer vertex;
    private ByteBuffer data;
    private GpuTexture copy;
    private GpuTextureView copyView;
    private int width;
    private int height;

    private static RenderPipeline pipeline(String name, String uniform, String ... samplers) {
        RenderPipeline.Builder builder = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"recoin", (String)("pipeline/" + name))).withVertexShader(VERTEX).withFragmentShader(class_2960.method_60655((String)"minecraft", (String)("core/recoin_" + name))).withVertexFormat(class_290.field_60033, VertexFormat.class_5596.field_27379).withUniform(uniform, class_10789.field_60031).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false);
        for (String sampler : samplers) {
            builder.withSampler(sampler);
        }
        return class_10799.method_67887((RenderPipeline)builder.build());
    }

    public void draw(Matrix4f projection, float distance, float strength) {
        class_276 framebuffer = class_310.method_1551().method_1522();
        if (projection == null || framebuffer == null || framebuffer.method_30277() == null || framebuffer.method_71640() == null || strength <= 0.0f) {
            return;
        }
        int w = framebuffer.field_1482;
        int h = framebuffer.field_1481;
        if (w <= 0 || h <= 0) {
            return;
        }
        this.ensureBuffers();
        this.ensureTextures(w, h, framebuffer.method_30277().getFormat());
        this.data.clear();
        this.inverseProjection.set((Matrix4fc)projection).invert().get(this.data);
        this.data.position(64);
        this.data.putFloat(distance).putFloat(strength).putFloat(w).putFloat(h).flip();
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.copyTextureToTexture(framebuffer.method_30277(), this.copy, 0, 0, 0, 0, 0, w, h);
        encoder.writeToBuffer(this.uniforms.slice(), this.data);
        class_12137 linear = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
        class_12137 nearest = RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST);
        try (RenderPass pass = encoder.createRenderPass(() -> "recoin:fog_prepare", this.blurViews[0], OptionalInt.empty());){
            pass.setPipeline(PREPARE);
            pass.setVertexBuffer(0, this.vertex);
            pass.setUniform("FogBlurData", this.uniforms);
            pass.bindTexture("MainSampler", this.copyView, linear);
            pass.bindTexture("DepthSampler", framebuffer.method_71640(), nearest);
            pass.draw(0, 6);
        }
        int bw = this.blurTextures[0].getWidth(0);
        int bh = this.blurTextures[0].getHeight(0);
        for (int axis = 0; axis < 2; ++axis) {
            float sigma = Math.max(0.5f, strength * (axis == 0 ? (float)bw / (float)w : (float)bh / (float)h));
            this.data.clear().putFloat(axis == 0 ? 1.0f / (float)bw : 0.0f).putFloat(axis == 1 ? 1.0f / (float)bh : 0.0f).putFloat(sigma).putFloat(Math.min(18.0f, (float)Math.ceil(sigma * 3.0f))).flip();
            encoder.writeToBuffer(this.blurUniforms[axis].slice(), this.data);
            this.gaussianPass(encoder, axis);
        }
        try (RenderPass pass = encoder.createRenderPass(() -> "recoin:fog_composite", framebuffer.method_71639(), OptionalInt.empty());){
            pass.setPipeline(COMPOSITE);
            pass.setVertexBuffer(0, this.vertex);
            pass.setUniform("FogBlurData", this.uniforms);
            pass.bindTexture("MainSampler", this.copyView, linear);
            pass.bindTexture("BlurSampler", this.blurViews[2], linear);
            pass.bindTexture("DepthSampler", framebuffer.method_71640(), nearest);
            pass.draw(0, 6);
        }
    }

    private void gaussianPass(CommandEncoder encoder, int axis) {
        try (RenderPass pass = encoder.createRenderPass(() -> "recoin:fog_gaussian", this.blurViews[axis + 1], OptionalInt.empty());){
            pass.setPipeline(GAUSSIAN);
            pass.setVertexBuffer(0, this.vertex);
            pass.setUniform("FogBlurPass", this.blurUniforms[axis]);
            pass.bindTexture("MainSampler", this.blurViews[axis], RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR));
            pass.draw(0, 6);
        }
    }

    private void ensureBuffers() {
        if (this.data != null) {
            return;
        }
        try {
            this.data = MemoryUtil.memAlloc((int)80);
            this.uniforms = RenderSystem.getDevice().createBuffer(() -> "recoin:fog_uniforms", 136, 80L);
            for (int axis = 0; axis < 2; ++axis) {
                this.blurUniforms[axis] = RenderSystem.getDevice().createBuffer(() -> "recoin:fog_gaussian_uniforms", 136, 16L);
            }
            ByteBuffer dummy = MemoryUtil.memAlloc((int)4).putInt(0).flip();
            try {
                this.vertex = RenderSystem.getDevice().createBuffer(() -> "recoin:fog_vertex", 32, dummy);
            }
            finally {
                MemoryUtil.memFree((Buffer)dummy);
            }
        }
        catch (RuntimeException exception) {
            this.close();
            throw exception;
        }
    }

    private void ensureTextures(int w, int h, TextureFormat format) {
        if (this.copy != null && w == this.width && h == this.height && this.copy.getFormat() == format) {
            return;
        }
        this.closeTextures();
        try {
            this.copy = RenderSystem.getDevice().createTexture(() -> "recoin:fog_copy", 5, format, w, h, 1, 1);
            this.copyView = RenderSystem.getDevice().createTextureView(this.copy);
            int bw = Math.max(1, Math.round((float)w * 0.35f));
            int bh = Math.max(1, Math.round((float)h * 0.35f));
            for (int index = 0; index < this.blurTextures.length; ++index) {
                this.blurTextures[index] = RenderSystem.getDevice().createTexture(() -> "recoin:fog_intermediate", 12, TextureFormat.RGBA8, bw, bh, 1, 1);
                this.blurViews[index] = RenderSystem.getDevice().createTextureView(this.blurTextures[index]);
            }
            this.width = w;
            this.height = h;
        }
        catch (RuntimeException exception) {
            this.closeTextures();
            throw exception;
        }
    }

    private void closeTextures() {
        for (int index = 0; index < this.blurTextures.length; ++index) {
            if (this.blurViews[index] != null) {
                this.blurViews[index].close();
            }
            if (this.blurTextures[index] != null) {
                this.blurTextures[index].close();
            }
            this.blurViews[index] = null;
            this.blurTextures[index] = null;
        }
        if (this.copyView != null) {
            this.copyView.close();
        }
        if (this.copy != null) {
            this.copy.close();
        }
        this.copyView = null;
        this.copy = null;
    }

    @Override
    public void close() {
        this.closeTextures();
        for (int axis = 0; axis < this.blurUniforms.length; ++axis) {
            if (this.blurUniforms[axis] != null) {
                this.blurUniforms[axis].close();
            }
            this.blurUniforms[axis] = null;
        }
        if (this.uniforms != null) {
            this.uniforms.close();
        }
        if (this.vertex != null) {
            this.vertex.close();
        }
        if (this.data != null) {
            MemoryUtil.memFree((Buffer)this.data);
        }
        this.uniforms = null;
        this.vertex = null;
        this.data = null;
    }
}

