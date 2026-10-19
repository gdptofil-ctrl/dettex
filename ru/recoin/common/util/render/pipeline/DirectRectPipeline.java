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
import ru.recoin.common.util.render.pipeline.EffectUniforms;
import ru.recoin.core.Main;

public class DirectRectPipeline {
    private static final class_2960 PIPELINE_ID = class_2960.method_60655((String)"recoin", (String)"pipeline/direct_rect");
    private static final class_2960 SHADER = class_2960.method_60655((String)"minecraft", (String)"core/recoin_direct_rect");
    private static final int BUFFER_SIZE = 256;
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private static final RenderPipeline PIPELINE = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_60125}).withLocation(PIPELINE_ID).withVertexShader(SHADER).withFragmentShader(SHADER).withVertexFormat(class_290.field_60033, VertexFormat.class_5596.field_27379).withUniform("RectData", class_10789.field_60031).withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());
    private GpuBuffer dummyVertexBuffer;
    private ByteBuffer dataBuffer;
    private final int[] nineColors = new int[9];
    private final int[] scissorBounds = new int[4];
    private boolean scissorActive;

    public void drawRect(float x, float y, float width, float height, int[] colors, float[] radii, float innerBlur) {
        float aaPadding;
        class_310 client = class_310.method_1551();
        if (client == null || client.method_1522() == null) {
            return;
        }
        float guiScale = this.getGuiScale(client);
        float f = aaPadding = innerBlur < 0.0f ? 2.0f / Math.max(guiScale, 0.001f) : 0.0f;
        if (!this.updateVisibleScissor(x - aaPadding, y - aaPadding, width + aaPadding * 2.0f, height + aaPadding * 2.0f)) {
            return;
        }
        if (this.allTransparent(colors)) {
            return;
        }
        this.ensureInitialized();
        this.prepareUniformData(x, y, width, height, client.method_22683().method_4486(), client.method_22683().method_4502(), guiScale, innerBlur, this.to9Colors(colors), radii);
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        GpuBufferSlice effectUniform1 = EffectUniforms.write(this.dataBuffer);
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().method_71106((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)COLOR_MODULATOR, (Vector3fc)MODEL_OFFSET, (Matrix4fc)TEXTURE_MATRIX);
        try (RenderPass renderPass = encoder.createRenderPass(() -> "recoin:direct_rect_pass", client.method_1522().method_71639(), OptionalInt.empty(), client.method_1522().method_71640(), OptionalDouble.empty());){
            renderPass.setPipeline(PIPELINE);
            renderPass.setVertexBuffer(0, this.dummyVertexBuffer);
            RenderSystem.bindDefaultUniforms((RenderPass)renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setUniform("RectData", effectUniform1);
            if (this.scissorActive) {
                int scale = client.method_22683().method_4495();
                renderPass.enableScissor(this.scissorBounds[0] * scale, client.method_22683().method_4507() - (this.scissorBounds[1] + this.scissorBounds[3]) * scale, this.scissorBounds[2] * scale, this.scissorBounds[3] * scale);
            }
            renderPass.draw(0, 6);
        }
    }

    private boolean updateVisibleScissor(float x, float y, float width, float height) {
        this.scissorActive = false;
        Main main = Main.getInstance();
        if (main == null || main.getScissorManager() == null) {
            return true;
        }
        if (!main.getScissorManager().copyCurrentBounds(this.scissorBounds)) {
            return true;
        }
        int left = (int)Math.floor(x);
        int top = (int)Math.floor(y);
        int right = (int)Math.ceil(x + width);
        int bottom = (int)Math.ceil(y + height);
        int clippedLeft = Math.max(left, this.scissorBounds[0]);
        int clippedTop = Math.max(top, this.scissorBounds[1]);
        int clippedRight = Math.min(right, this.scissorBounds[0] + this.scissorBounds[2]);
        int clippedBottom = Math.min(bottom, this.scissorBounds[1] + this.scissorBounds[3]);
        if (clippedRight <= clippedLeft || clippedBottom <= clippedTop) {
            return false;
        }
        this.scissorActive = true;
        return true;
    }

    private boolean allTransparent(int[] colors) {
        for (int color : colors) {
            if ((color >>> 24 & 0xFF) == 0) continue;
            return false;
        }
        return true;
    }

    private void ensureInitialized() {
        if (this.dataBuffer != null) {
            return;
        }
        this.dataBuffer = MemoryUtil.memAlloc((int)256);
        ByteBuffer dummyData = MemoryUtil.memAlloc((int)4);
        dummyData.putInt(0);
        dummyData.flip();
        this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "recoin:direct_rect_dummy_vertex", 32, dummyData);
        MemoryUtil.memFree((Buffer)dummyData);
    }

    private void prepareUniformData(float x, float y, float width, float height, float screenWidth, float screenHeight, float guiScale, float innerBlur, int[] colors, float[] radii) {
        this.dataBuffer.clear();
        this.dataBuffer.putFloat(x);
        this.dataBuffer.putFloat(y);
        this.dataBuffer.putFloat(width);
        this.dataBuffer.putFloat(height);
        this.dataBuffer.putFloat(screenWidth);
        this.dataBuffer.putFloat(screenHeight);
        this.dataBuffer.putFloat(guiScale);
        this.dataBuffer.putFloat(innerBlur);
        this.dataBuffer.putFloat(radii[0]);
        this.dataBuffer.putFloat(radii[1]);
        this.dataBuffer.putFloat(radii[2]);
        this.dataBuffer.putFloat(radii[3]);
        for (int color : colors) {
            this.dataBuffer.putFloat((float)(color >> 16 & 0xFF) / 255.0f);
            this.dataBuffer.putFloat((float)(color >> 8 & 0xFF) / 255.0f);
            this.dataBuffer.putFloat((float)(color & 0xFF) / 255.0f);
            this.dataBuffer.putFloat((float)(color >> 24 & 0xFF) / 255.0f);
        }
        this.dataBuffer.flip();
    }

    private int[] to9Colors(int[] colors) {
        if (colors.length == 1) {
            for (int i = 0; i < 9; ++i) {
                this.nineColors[i] = colors[0];
            }
        } else if (colors.length == 4) {
            this.nineColors[0] = colors[0];
            this.nineColors[1] = this.blend(colors[0], colors[1]);
            this.nineColors[2] = colors[1];
            this.nineColors[3] = this.blend(colors[0], colors[3]);
            this.nineColors[4] = this.blend(colors[0], colors[1], colors[2], colors[3]);
            this.nineColors[5] = this.blend(colors[1], colors[2]);
            this.nineColors[6] = colors[3];
            this.nineColors[7] = this.blend(colors[3], colors[2]);
            this.nineColors[8] = colors[2];
        } else {
            for (int i = 0; i < 9; ++i) {
                this.nineColors[i] = colors[i % colors.length];
            }
        }
        return this.nineColors;
    }

    private int blend(int first, int second) {
        int a = (first >>> 24 & 0xFF) + (second >>> 24 & 0xFF);
        int r = (first >>> 16 & 0xFF) + (second >>> 16 & 0xFF);
        int g = (first >>> 8 & 0xFF) + (second >>> 8 & 0xFF);
        int b = (first & 0xFF) + (second & 0xFF);
        return a / 2 << 24 | r / 2 << 16 | g / 2 << 8 | b / 2;
    }

    private int blend(int first, int second, int third, int fourth) {
        int a = (first >>> 24 & 0xFF) + (second >>> 24 & 0xFF) + (third >>> 24 & 0xFF) + (fourth >>> 24 & 0xFF);
        int r = (first >>> 16 & 0xFF) + (second >>> 16 & 0xFF) + (third >>> 16 & 0xFF) + (fourth >>> 16 & 0xFF);
        int g = (first >>> 8 & 0xFF) + (second >>> 8 & 0xFF) + (third >>> 8 & 0xFF) + (fourth >>> 8 & 0xFF);
        int b = (first & 0xFF) + (second & 0xFF) + (third & 0xFF) + (fourth & 0xFF);
        return a / 4 << 24 | r / 4 << 16 | g / 4 << 8 | b / 4;
    }

    private float getGuiScale(class_310 client) {
        int scaledWidth = Math.max(1, client.method_22683().method_4486());
        return (float)client.method_22683().method_4489() / (float)scaledWidth;
    }
}

