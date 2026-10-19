/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Builder
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.DepthTestFunction
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.FilterMode
 *  com.mojang.blaze3d.textures.GpuTexture
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.textures.TextureFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_10799
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  org.lwjgl.system.MemoryUtil
 */
package ru.recoin.common.util.render.pipeline;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
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
import net.minecraft.class_10799;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import org.lwjgl.system.MemoryUtil;

public final class MaskCoveragePipeline
implements AutoCloseable {
    private static final RenderPipeline ALPHA_PIPELINE = MaskCoveragePipeline.pipeline(false);
    private static final RenderPipeline RED_PIPELINE = MaskCoveragePipeline.pipeline(true);
    private GpuTexture texture;
    private GpuTextureView view;
    private GpuBuffer vertex;

    private static RenderPipeline pipeline(boolean red) {
        RenderPipeline.Builder builder = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"recoin", (String)("pipeline/mask_coverage_" + (red ? "red" : "alpha")))).withVertexShader(class_2960.method_60655((String)"minecraft", (String)"core/recoin_shader_esp_glow")).withFragmentShader(class_2960.method_60655((String)"minecraft", (String)"core/recoin_shader_esp_coverage")).withVertexFormat(class_290.field_60033, VertexFormat.class_5596.field_27379).withSampler("MaskSampler").withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false);
        if (red) {
            builder.withShaderDefine("MASK_RED");
        }
        return class_10799.method_67887((RenderPipeline)builder.build());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public GpuTextureView draw(GpuTextureView source, boolean red) {
        int width = (source.getWidth(0) + 15) / 16;
        int height = (source.getHeight(0) + 15) / 16;
        if (this.view == null || this.view.getWidth(0) != width || this.view.getHeight(0) != height) {
            if (this.view != null) {
                this.view.close();
            }
            if (this.texture != null) {
                this.texture.close();
            }
            this.texture = RenderSystem.getDevice().createTexture(() -> "recoin:mask_coverage", 12, TextureFormat.RED8, width, height, 1, 1);
            this.view = RenderSystem.getDevice().createTextureView(this.texture);
        }
        if (this.vertex == null) {
            ByteBuffer dummy = MemoryUtil.memAlloc((int)4).putInt(0).flip();
            try {
                this.vertex = RenderSystem.getDevice().createBuffer(() -> "recoin:mask_coverage_vertex", 32, dummy);
            }
            finally {
                MemoryUtil.memFree((Buffer)dummy);
            }
        }
        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "recoin:mask_coverage_pass", this.view, OptionalInt.empty());){
            pass.setPipeline(red ? RED_PIPELINE : ALPHA_PIPELINE);
            pass.setVertexBuffer(0, this.vertex);
            pass.bindTexture("MaskSampler", source, RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST));
            pass.draw(0, 6);
        }
        return this.view;
    }

    @Override
    public void close() {
        if (this.view != null) {
            this.view.close();
        }
        if (this.texture != null) {
            this.texture.close();
        }
        if (this.vertex != null) {
            this.vertex.close();
        }
        this.view = null;
        this.texture = null;
        this.vertex = null;
    }
}

