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
import java.util.BitSet;
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
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.common.util.render.pipeline.EffectUniforms;
import ru.recoin.core.Main;

public class StyledBlurPipeline {
    private static final class_2960 PIPELINE_ID = class_2960.method_60655((String)"recoin", (String)"pipeline/styled_blur");
    private static final class_2960 SHADER = class_2960.method_60655((String)"minecraft", (String)"core/recoin_styled_blur");
    private static final int BUFFER_SIZE = 128;
    private static final float BLUR_BUFFER_SCALE = 0.35f;
    private static final int BLUR_TILE_SIZE = 32;
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private static final RenderPipeline PIPELINE = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_60125}).withLocation(PIPELINE_ID).withVertexShader(SHADER).withFragmentShader(SHADER).withVertexFormat(class_290.field_60033, VertexFormat.class_5596.field_27379).withUniform("BlurData", class_10789.field_60031).withSampler("Sampler0").withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());
    private GpuBuffer dummyVertexBuffer;
    private ByteBuffer dataBuffer;
    private GpuTexture copyTexture;
    private GpuTextureView copyTextureView;
    private GpuTexture tempTexture;
    private GpuTextureView tempTextureView;
    private GpuTexture preparedTexture;
    private GpuTextureView preparedTextureView;
    private int lastWidth;
    private int lastHeight;
    private int lastBlurWidth;
    private int lastBlurHeight;
    private final int[] scissorBounds = new int[4];
    private final float[] fullFrameRadii = new float[4];
    private boolean scissorActive;
    private boolean frameSnapshotValid;
    private float lastScreenWidth = -1.0f;
    private float lastScreenHeight = -1.0f;
    private float lastGuiScale = -1.0f;
    private boolean preparedBlurValid;
    private final BitSet preparedTiles = new BitSet();
    private float preparedBlurRadius = -1.0f;
    private boolean liveMode;

    public void beginFrame() {
        this.frameSnapshotValid = false;
        this.preparedBlurValid = false;
        this.preparedTiles.clear();
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
            this.preparedBlurValid = false;
            this.lastScreenWidth = screenWidth;
            this.lastScreenHeight = screenHeight;
            this.lastGuiScale = guiScale;
        }
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        if (recreated || !this.frameSnapshotValid) {
            encoder.copyTextureToTexture(client.method_1522().method_30277(), this.copyTexture, 0, 0, 0, 0, 0, fbWidth, fbHeight);
            this.frameSnapshotValid = true;
            this.preparedBlurValid = false;
        }
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().method_71106((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)COLOR_MODULATOR, (Vector3fc)MODEL_OFFSET, (Matrix4fc)TEXTURE_MATRIX);
        class_12137 sampler = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
        if (!this.preparedBlurValid || Math.abs(this.preparedBlurRadius - radius) > 0.05f) {
            this.preparedTiles.clear();
            this.preparedBlurRadius = radius;
        }
        int columns = (this.lastBlurWidth + 32 - 1) / 32;
        int rows = (this.lastBlurHeight + 32 - 1) / 32;
        int firstColumn = 0;
        int endColumn = columns;
        int firstRow = 0;
        int endRow = rows;
        if (Render2DUtil.isHudDecorationActive() && !Render2DUtil.isGuiDecorationActive()) {
            double lower = (1.0 - (double)(y + height) * (double)guiScale / (double)fbHeight) * (double)this.lastBlurHeight;
            double upper = (1.0 - (double)y * (double)guiScale / (double)fbHeight) * (double)this.lastBlurHeight;
            firstRow = Math.max(0, Math.min(rows, (int)Math.floor((lower - 2.0) / 32.0)));
            endRow = Math.max(0, Math.min(rows, (int)Math.ceil((upper + 2.0) / 32.0)));
            if (this.lastBlurWidth >= 256) {
                double left = (double)x * (double)guiScale / (double)fbWidth * (double)this.lastBlurWidth;
                double right = (double)(x + width) * (double)guiScale / (double)fbWidth * (double)this.lastBlurWidth;
                firstColumn = Math.max(0, Math.min(columns, (int)Math.floor((left - 2.0) / 32.0)));
                endColumn = Math.max(0, Math.min(columns, (int)Math.ceil((right + 2.0) / 32.0)));
            }
        }
        for (int row = firstRow; row < endRow; ++row) {
            int rowOffset = row * columns;
            int missing = this.preparedTiles.nextClearBit(rowOffset + firstColumn);
            while (missing < rowOffset + endColumn) {
                int nextOffset;
                int nextSet;
                int rectangleEndRow;
                int nextPrepared = this.preparedTiles.nextSetBit(missing);
                int gapEnd = nextPrepared < 0 ? rowOffset + endColumn : Math.min(rowOffset + endColumn, nextPrepared);
                int firstMissingColumn = missing - rowOffset;
                int missingEndColumn = gapEnd - rowOffset;
                for (rectangleEndRow = row + 1; rectangleEndRow < endRow && ((nextSet = this.preparedTiles.nextSetBit((nextOffset = rectangleEndRow * columns) + firstMissingColumn)) < 0 || nextSet >= nextOffset + missingEndColumn); ++rectangleEndRow) {
                }
                this.prepareBlurredFrame(encoder, dynamicTransforms, sampler, screenWidth, screenHeight, fbWidth, fbHeight, guiScale, this.preparedBlurRadius, firstMissingColumn * 32, row * 32, Math.min(this.lastBlurWidth, missingEndColumn * 32), Math.min(this.lastBlurHeight, rectangleEndRow * 32));
                for (int preparedRow = row; preparedRow < rectangleEndRow; ++preparedRow) {
                    this.preparedTiles.set(preparedRow * columns + firstMissingColumn, preparedRow * columns + missingEndColumn);
                }
                missing = this.preparedTiles.nextClearBit(gapEnd);
            }
        }
        this.prepareUniformData(x, y, width, height, screenWidth, screenHeight, fbWidth, fbHeight, guiScale, radius, radii, color, 0.0f, 0.0f);
        GpuBufferSlice effectUniform1 = EffectUniforms.write(this.dataBuffer);
        try (RenderPass renderPass = encoder.createRenderPass(() -> "recoin:styled_blur_mask_pass", client.method_1522().method_71639(), OptionalInt.empty(), client.method_1522().method_71640(), OptionalDouble.empty());){
            renderPass.setPipeline(PIPELINE);
            renderPass.setVertexBuffer(0, this.dummyVertexBuffer);
            renderPass.bindTexture("Sampler0", this.preparedTextureView, sampler);
            RenderSystem.bindDefaultUniforms((RenderPass)renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setUniform("BlurData", effectUniform1);
            if (this.scissorActive) {
                int scale = client.method_22683().method_4495();
                renderPass.enableScissor(this.scissorBounds[0] * scale, client.method_22683().method_4507() - (this.scissorBounds[1] + this.scissorBounds[3]) * scale, this.scissorBounds[2] * scale, this.scissorBounds[3] * scale);
            }
            renderPass.draw(0, 6);
        }
    }

    private void prepareBlurredFrame(CommandEncoder encoder, GpuBufferSlice dynamicTransforms, class_12137 sampler, float screenWidth, float screenHeight, int fbWidth, int fbHeight, float guiScale, float radius, int firstColumn, int firstRow, int endColumn, int endRow) {
        double ratio = (double)screenHeight * (double)guiScale / (double)fbHeight;
        double reach = 12.0 * (double)Math.max(1.0f, radius / 24.0f) / (double)this.lastBlurHeight;
        double lower = Math.max(0.001, Math.min(0.999, 1.0 - ratio + ratio * ((double)firstRow + 0.5) / (double)this.lastBlurHeight - reach));
        double upper = Math.max(0.001, Math.min(0.999, 1.0 - ratio + ratio * ((double)endRow - 0.5) / (double)this.lastBlurHeight + reach));
        int tempFirstRow = Math.max(0, (int)Math.floor(lower * (double)this.lastBlurHeight - 0.5) - 1);
        int tempEndRow = Math.min(this.lastBlurHeight, (int)Math.ceil(upper * (double)this.lastBlurHeight + 0.5) + 1);
        double horizontalRatio = (double)screenWidth * (double)guiScale / (double)fbWidth;
        double left = Math.max(0.001, Math.min(0.999, horizontalRatio * ((double)firstColumn + 0.5) / (double)this.lastBlurWidth));
        double right = Math.max(0.001, Math.min(0.999, horizontalRatio * ((double)endColumn - 0.5) / (double)this.lastBlurWidth));
        int tempFirstColumn = Math.max(0, (int)Math.floor(left * (double)this.lastBlurWidth - 0.5) - 1);
        int tempEndColumn = Math.min(this.lastBlurWidth, (int)Math.ceil(right * (double)this.lastBlurWidth + 0.5) + 1);
        this.prepareUniformData(0.0f, 0.0f, screenWidth, screenHeight, screenWidth, screenHeight, fbWidth, fbHeight, guiScale, radius, this.fullFrameRadii, -1, 1.0f, 0.0f);
        GpuBufferSlice effectUniform2 = EffectUniforms.write(this.dataBuffer);
        try (RenderPass renderPass = encoder.createRenderPass(() -> "recoin:styled_blur_horizontal_pass", this.tempTextureView, OptionalInt.empty());){
            renderPass.setPipeline(PIPELINE);
            renderPass.setVertexBuffer(0, this.dummyVertexBuffer);
            renderPass.bindTexture("Sampler0", this.copyTextureView, sampler);
            RenderSystem.bindDefaultUniforms((RenderPass)renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setUniform("BlurData", effectUniform2);
            renderPass.enableScissor(tempFirstColumn, tempFirstRow, tempEndColumn - tempFirstColumn, tempEndRow - tempFirstRow);
            renderPass.draw(0, 6);
        }
        this.prepareUniformData(0.0f, 0.0f, screenWidth, screenHeight, screenWidth, screenHeight, fbWidth, fbHeight, guiScale, radius, this.fullFrameRadii, -1, 0.0f, 1.0f);
        GpuBufferSlice effectUniform3 = EffectUniforms.write(this.dataBuffer);
        try (RenderPass renderPass = encoder.createRenderPass(() -> "recoin:styled_blur_vertical_pass", this.preparedTextureView, OptionalInt.empty());){
            renderPass.setPipeline(PIPELINE);
            renderPass.setVertexBuffer(0, this.dummyVertexBuffer);
            renderPass.bindTexture("Sampler0", this.tempTextureView, sampler);
            RenderSystem.bindDefaultUniforms((RenderPass)renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setUniform("BlurData", effectUniform3);
            renderPass.enableScissor(firstColumn, firstRow, endColumn - firstColumn, endRow - firstRow);
            renderPass.draw(0, 6);
        }
        this.preparedBlurValid = true;
        this.preparedBlurRadius = radius;
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
        this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "recoin:styled_blur_dummy_vertex", 32, dummyData);
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
        if (this.tempTextureView != null) {
            this.tempTextureView.close();
            this.tempTextureView = null;
        }
        if (this.tempTexture != null) {
            this.tempTexture.close();
            this.tempTexture = null;
        }
        if (this.preparedTextureView != null) {
            this.preparedTextureView.close();
            this.preparedTextureView = null;
        }
        if (this.preparedTexture != null) {
            this.preparedTexture.close();
            this.preparedTexture = null;
        }
        this.copyTexture = RenderSystem.getDevice().createTexture(() -> "recoin:styled_blur_copy", 5, TextureFormat.RGBA8, width, height, 1, 1);
        this.copyTextureView = RenderSystem.getDevice().createTextureView(this.copyTexture);
        int blurWidth = Math.max(1, Math.round((float)width * 0.35f));
        int blurHeight = Math.max(1, Math.round((float)height * 0.35f));
        this.tempTexture = RenderSystem.getDevice().createTexture(() -> "recoin:styled_blur_temp", 12, TextureFormat.RGBA8, blurWidth, blurHeight, 1, 1);
        this.tempTextureView = RenderSystem.getDevice().createTextureView(this.tempTexture);
        this.preparedTexture = RenderSystem.getDevice().createTexture(() -> "recoin:styled_blur_prepared", 12, TextureFormat.RGBA8, blurWidth, blurHeight, 1, 1);
        this.preparedTextureView = RenderSystem.getDevice().createTextureView(this.preparedTexture);
        this.lastWidth = width;
        this.lastHeight = height;
        this.lastBlurWidth = blurWidth;
        this.lastBlurHeight = blurHeight;
        this.preparedBlurValid = false;
        return true;
    }

    private void prepareUniformData(float x, float y, float width, float height, float screenWidth, float screenHeight, int fbWidth, int fbHeight, float guiScale, float blurRadius, float[] radii, int color, float directionX, float directionY) {
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
        this.dataBuffer.putFloat(directionX);
        this.dataBuffer.putFloat(directionY);
        this.dataBuffer.putFloat(radii[0]);
        this.dataBuffer.putFloat(radii[1]);
        this.dataBuffer.putFloat(radii[2]);
        this.dataBuffer.putFloat(radii[3]);
        this.dataBuffer.putFloat((float)(color >> 16 & 0xFF) / 255.0f);
        this.dataBuffer.putFloat((float)(color >> 8 & 0xFF) / 255.0f);
        this.dataBuffer.putFloat((float)(color & 0xFF) / 255.0f);
        this.dataBuffer.putFloat((float)(color >> 24 & 0xFF) / 255.0f);
        this.dataBuffer.flip();
    }

    private float getGuiScale(class_310 client) {
        int scaledWidth = Math.max(1, client.method_22683().method_4486());
        return (float)client.method_22683().method_4489() / (float)scaledWidth;
    }
}

