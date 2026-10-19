/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  com.mojang.blaze3d.buffers.Std140Builder
 *  com.mojang.blaze3d.buffers.Std140SizeCalculator
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.DepthTestFunction
 *  com.mojang.blaze3d.platform.DestFactor
 *  com.mojang.blaze3d.platform.SourceFactor
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_10789
 *  net.minecraft.class_10799
 *  net.minecraft.class_11280
 *  net.minecraft.class_11280$class_11281
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_4184
 *  net.minecraft.class_9801
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 */
package ru.recoin.common.util.render.pipeline;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.class_10789;
import net.minecraft.class_10799;
import net.minecraft.class_11280;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_9801;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import ru.recoin.common.util.render.LegacyRenderSystem;

public final class StardustSkyPipeline {
    private static final class_2960 SHADER = class_2960.method_60656((String)"core/stardust_sky");
    private static final BlendFunction ADDITIVE = new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE);
    private static final int UBO_SIZE = new Std140SizeCalculator().putVec4().putVec4().putVec4().putVec4().putVec4().putMat4f().putIVec4().get();
    private static final RenderPipeline PIPELINE = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_60125, class_10799.field_60126}).withLocation(class_2960.method_60655((String)"recoin", (String)"pipeline/stardust_sky")).withVertexShader(SHADER).withFragmentShader(SHADER).withUniform("StardustSky", class_10789.field_60031).withVertexFormat(class_290.field_1592, VertexFormat.class_5596.field_27379).withCull(false).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withBlend(ADDITIVE).build());
    private static final long START = System.nanoTime();
    private static final Vector4f MODULATOR = new Vector4f();
    private static final Vector3f OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE = new Matrix4f();
    private static final Matrix4f INVERSE_VIEW_PROJECTION = new Matrix4f();
    private static final Matrix4f COMBINED = new Matrix4f();
    private static final Vector3f CAMERA_DIRECTION = new Vector3f();
    private static final StardustSkyPipeline INSTANCE = new StardustSkyPipeline();
    private class_11280<SkyData> uniforms;
    private GpuBuffer dome;
    private int vertexCount;
    private boolean failed;

    public static StardustSkyPipeline getInstance() {
        return INSTANCE;
    }

    public void updateInverse(Matrix4f view, Matrix4f projection) {
        try {
            COMBINED.set((Matrix4fc)projection).mul((Matrix4fc)view);
            if (Math.abs(COMBINED.determinant()) > 1.0E-8f) {
                INVERSE_VIEW_PROJECTION.set((Matrix4fc)COMBINED).invert();
            } else {
                INVERSE_VIEW_PROJECTION.identity();
            }
        }
        catch (Throwable ignored) {
            INVERSE_VIEW_PROJECTION.identity();
        }
    }

    public void draw(class_4184 camera, float tickDelta, int primary, int secondary, float intensity, float density, float timeOfDay, int mode) {
        if (this.failed || intensity <= 0.001f || camera == null) {
            return;
        }
        try {
            this.ensureDome();
            this.updateInverse(RenderSystem.getModelViewMatrix(), LegacyRenderSystem.getProjectionMatrix());
            class_310 client = class_310.method_1551();
            if (this.dome == null || client.method_1522() == null) {
                return;
            }
            GpuTextureView color = client.method_1522().method_71639();
            GpuTextureView depth = client.method_1522().method_71640();
            CAMERA_DIRECTION.set(0.0f, 0.0f, -1.0f);
            camera.method_23767().transform(CAMERA_DIRECTION);
            CAMERA_DIRECTION.normalize();
            float rain = client.field_1687 == null ? 0.0f : client.field_1687.method_8430(tickDelta);
            float time = (float)((System.nanoTime() - START) % 4096000000000L) / 1.0E9f;
            int width = Math.max(1, client.method_22683().method_4489());
            int height = Math.max(1, client.method_22683().method_4506());
            Vector4f primaryColor = StardustSkyPipeline.color(primary, intensity);
            Vector4f secondaryColor = StardustSkyPipeline.color(secondary, 1.0f);
            SkyData data = new SkyData(primaryColor, secondaryColor, new Vector4f((Vector3fc)CAMERA_DIRECTION, rain), new Vector4f((float)width, (float)height, time, timeOfDay), new Vector4f(intensity, density, tickDelta, 0.0f), new Matrix4f((Matrix4fc)INVERSE_VIEW_PROJECTION), mode);
            GpuBufferSlice skyUbo = this.uniformStorage().method_71102(data);
            MODULATOR.set((Vector4fc)primaryColor);
            OFFSET.set(secondaryColor.x, secondaryColor.y, secondaryColor.z);
            GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().method_71106((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)MODULATOR, (Vector3fc)OFFSET, (Matrix4fc)TEXTURE.identity());
            try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Stardust Sky", color, OptionalInt.empty(), depth, OptionalDouble.empty());){
                pass.setPipeline(PIPELINE);
                RenderSystem.bindDefaultUniforms((RenderPass)pass);
                pass.setUniform("DynamicTransforms", transforms);
                pass.setUniform("StardustSky", skyUbo);
                pass.setVertexBuffer(0, this.dome);
                pass.draw(0, this.vertexCount);
            }
        }
        catch (Throwable throwable) {
            this.failed = true;
            System.err.println("[Stardust] original pipeline disabled: " + throwable.getMessage());
        }
    }

    private class_11280<SkyData> uniformStorage() {
        if (this.uniforms == null) {
            this.uniforms = new class_11280("Stardust Sky UBO", UBO_SIZE, 4);
        }
        return this.uniforms;
    }

    private void ensureDome() {
        if (this.dome != null) {
            return;
        }
        class_287 builder = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27379, class_290.field_1592);
        for (int ring = 0; ring < 14; ++ring) {
            float y0 = -0.24f + 1.24f * (float)ring / 14.0f;
            float y1 = -0.24f + 1.24f * (float)(ring + 1) / 14.0f;
            for (int segment = 0; segment < 48; ++segment) {
                float x0 = (float)segment / 48.0f;
                float x1 = (float)(segment + 1) / 48.0f;
                this.vertex(builder, x0, y0);
                this.vertex(builder, x1, y0);
                this.vertex(builder, x1, y1);
                this.vertex(builder, x1, y1);
                this.vertex(builder, x0, y1);
                this.vertex(builder, x0, y0);
            }
        }
        try (class_9801 built = builder.method_60800();){
            this.vertexCount = built.method_60822().comp_750();
            this.dome = RenderSystem.getDevice().createBuffer(() -> "Stardust Sky Dome", 32, built.method_60818());
        }
    }

    private void vertex(class_287 builder, float u, float y) {
        float angle = u * ((float)Math.PI * 2);
        float radius = (float)Math.sqrt(Math.max(0.0f, 1.0f - y * y));
        builder.method_22912((float)Math.cos(angle) * radius * 128.0f, y * 128.0f, (float)Math.sin(angle) * radius * 128.0f);
    }

    private static Vector4f color(int rgb, float alpha) {
        return new Vector4f((float)(rgb >>> 16 & 0xFF) / 255.0f, (float)(rgb >>> 8 & 0xFF) / 255.0f, (float)(rgb & 0xFF) / 255.0f, alpha);
    }

    private record SkyData(Vector4f primary, Vector4f secondary, Vector4f cameraWeather, Vector4f resolutionTime, Vector4f params, Matrix4f inverse, int mode) implements class_11280.class_11281
    {
        public void method_71104(ByteBuffer buffer) {
            Std140Builder.intoBuffer((ByteBuffer)buffer).putVec4((Vector4fc)this.primary).putVec4((Vector4fc)this.secondary).putVec4((Vector4fc)this.cameraWeather).putVec4((Vector4fc)this.resolutionTime).putVec4((Vector4fc)this.params).putMat4f((Matrix4fc)this.inverse).putIVec4(this.mode, 0, 0, 0);
        }
    }
}

