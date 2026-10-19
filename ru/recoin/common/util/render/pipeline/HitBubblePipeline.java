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
 *  com.mojang.blaze3d.systems.RenderSystem$class_5590
 *  com.mojang.blaze3d.textures.FilterMode
 *  com.mojang.blaze3d.textures.GpuTexture
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.textures.TextureFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_10789
 *  net.minecraft.class_10799
 *  net.minecraft.class_276
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_9801
 *  net.minecraft.class_9801$class_4574
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
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.class_10789;
import net.minecraft.class_10799;
import net.minecraft.class_276;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_9801;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryUtil;

public final class HitBubblePipeline {
    private static final class_2960 SHADER = class_2960.method_60655((String)"minecraft", (String)"core/recoin_hit_bubble");
    private static final RenderPipeline PIPELINE = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_60125}).withLocation(class_2960.method_60655((String)"recoin", (String)"pipeline/hit_bubble")).withVertexShader(SHADER).withFragmentShader(SHADER).withVertexFormat(class_290.field_1575, VertexFormat.class_5596.field_27382).withUniform("BubbleData", class_10789.field_60031).withSampler("SceneSampler").withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST).withDepthWrite(false).withCull(false).build());
    private static final Vector4f WHITE = new Vector4f(1.0f);
    private static final Vector3f OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private GpuBuffer uniform;
    private ByteBuffer data;
    private GpuTexture scene;
    private GpuTextureView sceneView;
    private int width;
    private int height;

    public void draw(class_9801 built, float strength) {
        try (class_9801 class_98012 = built;){
            class_276 framebuffer = class_310.method_1551().method_1522();
            if (framebuffer == null || framebuffer.method_30277() == null || framebuffer.method_71639() == null || framebuffer.field_1482 <= 0 || framebuffer.field_1481 <= 0) {
                return;
            }
            this.ensure(framebuffer.field_1482, framebuffer.field_1481);
            this.data.clear();
            this.data.putFloat(this.width).putFloat(this.height).putFloat(strength).putFloat(0.0f).flip();
            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            encoder.copyTextureToTexture(framebuffer.method_30277(), this.scene, 0, 0, 0, 0, 0, this.width, this.height);
            encoder.writeToBuffer(this.uniform.slice(), this.data);
            class_9801.class_4574 parameters = built.method_60822();
            GpuBuffer vertices = parameters.comp_749().uploadImmediateVertexBuffer(built.method_60818());
            RenderSystem.class_5590 indices = RenderSystem.getSequentialBuffer((VertexFormat.class_5596)parameters.comp_752());
            GpuBuffer indexBuffer = indices.method_68274(parameters.comp_751());
            GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().method_71106((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)WHITE, (Vector3fc)OFFSET, (Matrix4fc)TEXTURE_MATRIX);
            try (RenderPass pass = encoder.createRenderPass(() -> "recoin:hit_bubble", framebuffer.method_71639(), OptionalInt.empty(), framebuffer.field_1478 ? framebuffer.method_71640() : null, OptionalDouble.empty());){
                pass.setPipeline(PIPELINE);
                pass.disableScissor();
                pass.setVertexBuffer(0, vertices);
                pass.setIndexBuffer(indexBuffer, indices.method_31924());
                pass.bindTexture("SceneSampler", this.sceneView, RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR));
                RenderSystem.bindDefaultUniforms((RenderPass)pass);
                pass.setUniform("DynamicTransforms", transforms);
                pass.setUniform("BubbleData", this.uniform);
                pass.drawIndexed(0, 0, parameters.comp_751(), 1);
            }
        }
    }

    public void close() {
        if (this.sceneView != null) {
            this.sceneView.close();
        }
        if (this.scene != null) {
            this.scene.close();
        }
        if (this.uniform != null) {
            this.uniform.close();
        }
        if (this.data != null) {
            MemoryUtil.memFree((Buffer)this.data);
        }
        this.sceneView = null;
        this.scene = null;
        this.uniform = null;
        this.data = null;
        this.width = 0;
        this.height = 0;
    }

    private void ensure(int targetWidth, int targetHeight) {
        if (this.data == null) {
            this.data = MemoryUtil.memAlloc((int)16);
            this.uniform = RenderSystem.getDevice().createBuffer(() -> "recoin:hit_bubble_uniform", 136, 16L);
        }
        if (this.scene != null && this.width == targetWidth && this.height == targetHeight) {
            return;
        }
        if (this.sceneView != null) {
            this.sceneView.close();
        }
        if (this.scene != null) {
            this.scene.close();
        }
        this.scene = RenderSystem.getDevice().createTexture(() -> "recoin:hit_bubble_scene", 5, TextureFormat.RGBA8, targetWidth, targetHeight, 1, 1);
        this.sceneView = RenderSystem.getDevice().createTextureView(this.scene);
        this.width = targetWidth;
        this.height = targetHeight;
    }
}

