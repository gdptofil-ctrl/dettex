/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.DepthTestFunction
 *  com.mojang.blaze3d.systems.CommandEncoder
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_10789
 *  net.minecraft.class_10799
 *  net.minecraft.class_276
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_9801
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
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.class_10789;
import net.minecraft.class_10799;
import net.minecraft.class_276;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_9801;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryUtil;

public final class SkyShaderPipeline {
    private static final class_2960 SHADER = class_2960.method_60655((String)"minecraft", (String)"core/recoin_custom_sky");
    private static final RenderPipeline PIPELINE = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_60125}).withLocation(class_2960.method_60655((String)"recoin", (String)"pipeline/custom_sky")).withVertexShader(SHADER).withFragmentShader(SHADER).withVertexFormat(class_290.field_1592, VertexFormat.class_5596.field_27379).withUniform("SkyData", class_10789.field_60031).withoutBlend().withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());
    private static final Vector4f MODULATOR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private static final Vector3f OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE = new Matrix4f();
    private static final SkyShaderPipeline INSTANCE = new SkyShaderPipeline();
    private GpuBuffer uniform;
    private GpuBuffer vertex;
    private int vertexCount;
    private ByteBuffer data;

    public static SkyShaderPipeline getInstance() {
        return INSTANCE;
    }

    public void draw(class_276 target, int mode, float yaw, float pitch, float fov, int fogColor, boolean fogEnabled, int shaderColor) {
        this.draw(target, mode, yaw, pitch, fov, fogColor, fogEnabled, shaderColor, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    }

    public void draw(class_276 target, int mode, float yaw, float pitch, float fov, int fogColor, boolean fogEnabled, int shaderColor, float windSpeed, float windDirection, float fogDensity, float horizonDissolve, float skyLift, float edgeSoftness) {
        if (target == null || target.method_71639() == null) {
            return;
        }
        this.ensure();
        this.data.clear();
        this.data.putFloat(target.field_1482).putFloat(target.field_1481).putFloat((float)(System.nanoTime() % 100000000000L) / 1.0E9f).putFloat(mode);
        this.data.putFloat(yaw).putFloat(pitch).putFloat(fov).putFloat(0.0f);
        this.data.putFloat((float)(fogColor >> 16 & 0xFF) / 255.0f).putFloat((float)(fogColor >> 8 & 0xFF) / 255.0f).putFloat((float)(fogColor & 0xFF) / 255.0f).putFloat(fogEnabled ? 1.0f : 0.0f);
        this.data.putFloat((float)(shaderColor >> 16 & 0xFF) / 255.0f).putFloat((float)(shaderColor >> 8 & 0xFF) / 255.0f).putFloat((float)(shaderColor & 0xFF) / 255.0f).putFloat(1.0f);
        this.data.putFloat(windSpeed).putFloat(windDirection).putFloat(fogDensity).putFloat(horizonDissolve);
        this.data.putFloat(skyLift).putFloat(edgeSoftness).putFloat(0.0f).putFloat(0.0f);
        this.data.flip();
        if (this.uniform == null || this.uniform.size() < (long)this.data.remaining()) {
            if (this.uniform != null) {
                this.uniform.close();
            }
            this.uniform = RenderSystem.getDevice().createBuffer(() -> "recoin:custom_sky_uniform", 136, (long)this.data.remaining());
        }
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.writeToBuffer(this.uniform.slice(), this.data);
        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().method_71106((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)MODULATOR, (Vector3fc)OFFSET, (Matrix4fc)TEXTURE);
        try (RenderPass pass = encoder.createRenderPass(() -> "recoin:custom_sky_pass", target.method_71639(), OptionalInt.empty(), target.method_71640(), OptionalDouble.empty());){
            pass.setPipeline(PIPELINE);
            pass.setVertexBuffer(0, this.vertex);
            RenderSystem.bindDefaultUniforms((RenderPass)pass);
            pass.setUniform("DynamicTransforms", transforms);
            pass.setUniform("SkyData", this.uniform);
            pass.draw(0, this.vertexCount);
        }
    }

    private void ensure() {
        if (this.data != null) {
            return;
        }
        this.data = MemoryUtil.memAlloc((int)96);
        class_287 builder = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27379, class_290.field_1592);
        for (int ring = 0; ring < 20; ++ring) {
            float y0 = -1.0f + 2.0f * (float)ring / 20.0f;
            float y1 = -1.0f + 2.0f * (float)(ring + 1) / 20.0f;
            for (int segment = 0; segment < 64; ++segment) {
                float u0 = (float)segment / 64.0f;
                float u1 = (float)(segment + 1) / 64.0f;
                this.vertex(builder, u0, y0);
                this.vertex(builder, u1, y0);
                this.vertex(builder, u1, y1);
                this.vertex(builder, u1, y1);
                this.vertex(builder, u0, y1);
                this.vertex(builder, u0, y0);
            }
        }
        try (class_9801 built = builder.method_60800();){
            this.vertexCount = built.method_60822().comp_750();
            this.vertex = RenderSystem.getDevice().createBuffer(() -> "recoin:custom_sky_dome", 32, built.method_60818());
        }
    }

    private void vertex(class_287 builder, float u, float y) {
        float angle = u * ((float)Math.PI * 2);
        float radius = (float)Math.sqrt(Math.max(0.0f, 1.0f - y * y));
        builder.method_22912((float)Math.cos(angle) * radius * 128.0f, y * 128.0f, (float)Math.sin(angle) * radius * 128.0f);
    }
}

