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
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_10789
 *  net.minecraft.class_10799
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
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.class_10789;
import net.minecraft.class_10799;
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

public final class ThemeWavePipeline {
    private static final class_2960 SHADER = class_2960.method_60655((String)"minecraft", (String)"core/recoin_theme_wave");
    private static final RenderPipeline PIPELINE = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_60125}).withLocation(class_2960.method_60655((String)"recoin", (String)"pipeline/theme_wave")).withVertexShader(SHADER).withFragmentShader(SHADER).withVertexFormat(class_290.field_60033, VertexFormat.class_5596.field_27379).withUniform("WaveData", class_10789.field_60031).withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());
    private static final Vector4f MODULATOR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private static final Vector3f OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE = new Matrix4f();
    private GpuBuffer uniform;
    private GpuBuffer vertex;
    private ByteBuffer data;

    public void draw(float x, float y, float width, float height, float progress, int color) {
        class_310 client = class_310.method_1551();
        if (client == null || client.method_1522() == null || progress < 0.0f || progress > 1.0f) {
            return;
        }
        this.ensure();
        this.data.clear();
        this.data.putFloat(x).putFloat(y).putFloat(width).putFloat(height);
        this.data.putFloat(client.method_22683().method_4486()).putFloat(client.method_22683().method_4502()).putFloat(progress).putFloat((float)(System.nanoTime() % 10000000000L) / 1.0E9f);
        this.data.putFloat((float)(color >> 16 & 0xFF) / 255.0f).putFloat((float)(color >> 8 & 0xFF) / 255.0f).putFloat((float)(color & 0xFF) / 255.0f).putFloat((float)(color >>> 24 & 0xFF) / 255.0f);
        this.data.flip();
        if (this.uniform == null || this.uniform.size() < (long)this.data.remaining()) {
            if (this.uniform != null) {
                this.uniform.close();
            }
            this.uniform = RenderSystem.getDevice().createBuffer(() -> "recoin:theme_wave_uniform", 136, (long)this.data.remaining());
        }
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.writeToBuffer(this.uniform.slice(), this.data);
        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().method_71106((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)MODULATOR, (Vector3fc)OFFSET, (Matrix4fc)TEXTURE);
        try (RenderPass pass = encoder.createRenderPass(() -> "recoin:theme_wave_pass", client.method_1522().method_71639(), OptionalInt.empty(), client.method_1522().method_71640(), OptionalDouble.empty());){
            pass.setPipeline(PIPELINE);
            pass.setVertexBuffer(0, this.vertex);
            RenderSystem.bindDefaultUniforms((RenderPass)pass);
            pass.setUniform("DynamicTransforms", transforms);
            pass.setUniform("WaveData", this.uniform);
            pass.draw(0, 6);
        }
    }

    private void ensure() {
        if (this.data != null) {
            return;
        }
        this.data = MemoryUtil.memAlloc((int)64);
        ByteBuffer dummy = MemoryUtil.memAlloc((int)4).putInt(0).flip();
        this.vertex = RenderSystem.getDevice().createBuffer(() -> "recoin:theme_wave_vertex", 32, dummy);
        MemoryUtil.memFree((Buffer)dummy);
    }
}

