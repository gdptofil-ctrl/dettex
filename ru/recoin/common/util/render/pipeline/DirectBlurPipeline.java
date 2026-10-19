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
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.class_10789;
import net.minecraft.class_10799;
import net.minecraft.class_12137;
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
import ru.recoin.core.Main;

public class DirectBlurPipeline {
    private static final class_2960 PIPELINE_ID = class_2960.method_60655((String)"recoin", (String)"pipeline/direct_blur");
    private static final class_2960 SHADER = class_2960.method_60655((String)"minecraft", (String)"core/recoin_direct_blur");
    private static final int BUFFER_SIZE = 128;
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private static final RenderPipeline PIPELINE = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_60125}).withLocation(PIPELINE_ID).withVertexShader(SHADER).withFragmentShader(SHADER).withVertexFormat(class_290.field_60033, VertexFormat.class_5596.field_27379).withUniform("BlurData", class_10789.field_60031).withSampler("Sampler0").withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());
    private GpuBuffer uniformBuffer;
    private GpuBuffer dummyVertexBuffer;
    private ByteBuffer dataBuffer;
    private GpuTexture copyTexture;
    private GpuTextureView copyTextureView;
    private int lastWidth;
    private int lastHeight;
    private final int[] scissorBounds = new int[4];
    private boolean scissorActive;
    private static final long MAX_SNAPSHOT_AGE_NS = 5000000L;
    private boolean frameSnapshotValid;
    private long lastSnapshotNanos;
    private float lastScreenWidth = -1.0f;
    private float lastScreenHeight = -1.0f;
    private float lastGuiScale = -1.0f;
    private boolean liveMode;

    public void beginFrame() {
        this.frameSnapshotValid = false;
    }

    public void setLiveMode(boolean liveMode) {
        this.liveMode = liveMode;
    }

    public void drawBlur(float x, float y, float width, float height, float radius, float[] radii, int color) {
        class_310 client = class_310.method_1551();
        if (client == null || client.method_1522() == null || client.method_1522().method_30277() == null) {
            return;
        }
        if (!this.updateVisibleScissor(x, y, width, height)) {
            return;
        }
        this.ensureInitialized();
        int fbWidth = client.method_1522().field_1482;
        int fbHeight = client.method_1522().field_1481;
        boolean recreated = this.ensureTextures(fbWidth, fbHeight);
        float screenWidth = client.method_22683().method_4486();
        float screenHeight = client.method_22683().method_4502();
        float guiScale = this.getGuiScale(client);
        if (screenWidth != this.lastScreenWidth || screenHeight != this.lastScreenHeight || guiScale != this.lastGuiScale) {
            this.frameSnapshotValid = false;
            this.lastScreenWidth = screenWidth;
            this.lastScreenHeight = screenHeight;
            this.lastGuiScale = guiScale;
        }
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        long now = System.nanoTime();
        if (this.liveMode || recreated || !this.frameSnapshotValid || now - this.lastSnapshotNanos >= 5000000L) {
            encoder.copyTextureToTexture(client.method_1522().method_30277(), this.copyTexture, 0, 0, 0, 0, 0, fbWidth, fbHeight);
            this.frameSnapshotValid = true;
            this.lastSnapshotNanos = now;
        }
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().method_71106((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)COLOR_MODULATOR, (Vector3fc)MODEL_OFFSET, (Matrix4fc)TEXTURE_MATRIX);
        class_12137 sampler = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
        this.prepareUniformData(x, y, width, height, screenWidth, screenHeight, fbWidth, fbHeight, guiScale, radius, radii, color);
        encoder.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
        try (RenderPass renderPass = encoder.createRenderPass(() -> "recoin:direct_blur_pass", client.method_1522().method_71639(), OptionalInt.empty(), client.method_1522().method_71640(), OptionalDouble.empty());){
            renderPass.setPipeline(PIPELINE);
            renderPass.setVertexBuffer(0, this.dummyVertexBuffer);
            renderPass.bindTexture("Sampler0", this.copyTextureView, sampler);
            RenderSystem.bindDefaultUniforms((RenderPass)renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setUniform("BlurData", this.uniformBuffer);
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
        this.scissorBounds[0] = clippedLeft;
        this.scissorBounds[1] = clippedTop;
        this.scissorBounds[2] = clippedRight - clippedLeft;
        this.scissorBounds[3] = clippedBottom - clippedTop;
        this.scissorActive = true;
        return true;
    }

    private void ensureInitialized() {
        if (this.dataBuffer != null) {
            return;
        }
        this.dataBuffer = MemoryUtil.memAlloc((int)128);
        ByteBuffer dummyData = MemoryUtil.memAlloc((int)4);
        dummyData.putInt(0);
        dummyData.flip();
        this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "recoin:direct_blur_dummy_vertex", 32, dummyData);
        MemoryUtil.memFree((Buffer)dummyData);
    }

    private boolean ensureTextures(int width, int height) {
        if (this.copyTexture != null && this.lastWidth == width && this.lastHeight == height) {
            return false;
        }
        if (this.copyTextureView != null) {
            this.copyTextureView.close();
            this.copyTextureView = null;
        }
        if (this.copyTexture != null) {
            this.copyTexture.close();
            this.copyTexture = null;
        }
        this.copyTexture = RenderSystem.getDevice().createTexture(() -> "recoin:direct_blur_copy", 5, TextureFormat.RGBA8, width, height, 1, 1);
        this.copyTextureView = RenderSystem.getDevice().createTextureView(this.copyTexture);
        this.lastWidth = width;
        this.lastHeight = height;
        return true;
    }

    private void prepareUniformData(float x, float y, float width, float height, float screenWidth, float screenHeight, int fbWidth, int fbHeight, float guiScale, float blurRadius, float[] radii, int color) {
        this.dataBuffer.clear();
        this.dataBuffer.putFloat(x);
        this.dataBuffer.putFloat(y);
        this.dataBuffer.putFloat(width);
        this.dataBuffer.putFloat(height);
        this.dataBuffer.putFloat(screenWidth);
        this.dataBuffer.putFloat(screenHeight);
        this.dataBuffer.putFloat(guiScale);
        this.dataBuffer.putFloat(blurRadius);
        this.dataBuffer.putFloat(fbWidth);
        this.dataBuffer.putFloat(fbHeight);
        this.dataBuffer.putFloat(0.0f);
        this.dataBuffer.putFloat(0.0f);
        this.dataBuffer.putFloat(radii[0]);
        this.dataBuffer.putFloat(radii[1]);
        this.dataBuffer.putFloat(radii[2]);
        this.dataBuffer.putFloat(radii[3]);
        this.dataBuffer.putFloat((float)(color >> 16 & 0xFF) / 255.0f);
        this.dataBuffer.putFloat((float)(color >> 8 & 0xFF) / 255.0f);
        this.dataBuffer.putFloat((float)(color & 0xFF) / 255.0f);
        this.dataBuffer.putFloat((float)(color >> 24 & 0xFF) / 255.0f);
        this.dataBuffer.flip();
        int size = this.dataBuffer.remaining();
        if (this.uniformBuffer == null || this.uniformBuffer.size() < (long)size) {
            if (this.uniformBuffer != null) {
                this.uniformBuffer.close();
            }
            this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "recoin:direct_blur_uniform", 136, (long)size);
        }
    }

    private float getGuiScale(class_310 client) {
        int scaledWidth = Math.max(1, client.method_22683().method_4486());
        return (float)client.method_22683().method_4489() / (float)scaledWidth;
    }
}

