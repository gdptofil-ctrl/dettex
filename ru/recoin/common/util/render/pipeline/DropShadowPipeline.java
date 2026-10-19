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
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
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
import ru.recoin.common.util.render.pipeline.EffectUniforms;

public final class DropShadowPipeline {
    private static final class_2960 ID = class_2960.method_60655((String)"recoin", (String)"pipeline/drop_shadow");
    private static final class_2960 SHADER = class_2960.method_60655((String)"minecraft", (String)"core/recoin_drop_shadow");
    private static final BlendFunction PREMULTIPLIED = new BlendFunction(SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA);
    private static final RenderPipeline PIPELINE = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_60125}).withLocation(ID).withVertexShader(SHADER).withFragmentShader(SHADER).withVertexFormat(class_290.field_60033, VertexFormat.class_5596.field_27379).withUniform("ShadowData", class_10789.field_60031).withBlend(PREMULTIPLIED).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private GpuBuffer vertexBuffer;
    private ByteBuffer data;

    public void draw(float x, float y, float width, float height, float[] radii, float blurStrength, float spread, int color) {
        class_310 client = class_310.method_1551();
        if (client == null || client.method_1522() == null || width <= 0.0f || height <= 0.0f || color >>> 24 == 0) {
            return;
        }
        float blur = Math.max(blurStrength, 0.0f);
        float safeSpread = Math.max(spread, 0.0f);
        float padding = safeSpread + blur * 3.0f;
        this.ensureInitialized();
        this.data.clear();
        this.data.putFloat(x - padding).putFloat(y - padding).putFloat(width + padding * 2.0f).putFloat(height + padding * 2.0f);
        this.data.putFloat(client.method_22683().method_4486()).putFloat(client.method_22683().method_4502()).putFloat((float)client.method_22683().method_4486() / (float)Math.max(1, client.method_1522().field_1482)).putFloat((float)client.method_22683().method_4502() / (float)Math.max(1, client.method_1522().field_1481));
        this.data.putFloat(width).putFloat(height).putFloat(Math.max(blur, 0.001f)).putFloat(safeSpread);
        this.data.putFloat(radii[0]).putFloat(radii[1]).putFloat(radii[2]).putFloat(radii[3]);
        this.data.putFloat((float)(color >> 16 & 0xFF) / 255.0f).putFloat((float)(color >> 8 & 0xFF) / 255.0f).putFloat((float)(color & 0xFF) / 255.0f).putFloat((float)(color >>> 24 & 0xFF) / 255.0f);
        this.data.flip();
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        GpuBufferSlice effectUniform1 = EffectUniforms.write(this.data);
        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().method_71106((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)COLOR_MODULATOR, (Vector3fc)MODEL_OFFSET, (Matrix4fc)TEXTURE_MATRIX);
        try (RenderPass pass = encoder.createRenderPass(() -> "recoin:drop_shadow_pass", client.method_1522().method_71639(), OptionalInt.empty(), client.method_1522().method_71640(), OptionalDouble.empty());){
            pass.setPipeline(PIPELINE);
            pass.setVertexBuffer(0, this.vertexBuffer);
            RenderSystem.bindDefaultUniforms((RenderPass)pass);
            pass.setUniform("DynamicTransforms", transforms);
            pass.setUniform("ShadowData", effectUniform1);
            pass.draw(0, 24);
        }
    }

    private void ensureInitialized() {
        if (this.data != null) {
            return;
        }
        this.data = MemoryUtil.memAlloc((int)96);
        ByteBuffer dummy = MemoryUtil.memAlloc((int)4).putInt(0).flip();
        this.vertexBuffer = RenderSystem.getDevice().createBuffer(() -> "recoin:drop_shadow_vertex", 32, dummy);
        MemoryUtil.memFree((Buffer)dummy);
    }
}

