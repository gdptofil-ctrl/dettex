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
 *  com.mojang.blaze3d.shaders.ShaderType
 *  com.mojang.blaze3d.systems.CommandEncoder
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.FilterMode
 *  com.mojang.blaze3d.textures.GpuTexture
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_1044
 *  net.minecraft.class_10789
 *  net.minecraft.class_10799
 *  net.minecraft.class_11231
 *  net.minecraft.class_11244
 *  net.minecraft.class_12137
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_4588
 *  net.minecraft.class_8030
 *  org.joml.Matrix3x2fStack
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector2f
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 *  org.lwjgl.system.MemoryUtil
 */
package ru.recoin.api.system.font;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.class_1044;
import net.minecraft.class_10789;
import net.minecraft.class_10799;
import net.minecraft.class_11231;
import net.minecraft.class_11244;
import net.minecraft.class_12137;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4588;
import net.minecraft.class_8030;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryUtil;
import ru.recoin.api.system.font.FontAtlas;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.MsdfGlyph;
import ru.recoin.common.util.render.GuiTextureCache;
import ru.recoin.core.Main;
import ru.recoin.mixins.DrawContextAccessor;

public class FontPipeline {
    private static final class_2960 PIPELINE_ID = class_2960.method_60655((String)"recoin", (String)"pipeline/msdf");
    private static final class_2960 SHADER_ID = class_2960.method_60655((String)"minecraft", (String)"core/msdf");
    private static final class_2960 GUI_PIPELINE_ID = class_2960.method_60655((String)"recoin", (String)"pipeline/msdf_gui");
    private static final class_2960 GUI_SHADER_ID = class_2960.method_60655((String)"minecraft", (String)"core/msdf_gui");
    private static final float FIXED_GUI_SCALE = 2.0f;
    private static final int MAX_CHARS = 256;
    private static final int BUFFER_SIZE = 16448;
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private static final int[] LEGACY_COLORS = new int[32];
    private static final RenderPipeline PIPELINE = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_60125}).withLocation(PIPELINE_ID).withVertexShader(SHADER_ID).withFragmentShader(SHADER_ID).withVertexFormat(class_290.field_60033, VertexFormat.class_5596.field_27379).withUniform("FontData", class_10789.field_60031).withSampler("Sampler0").withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());
    private static final RenderPipeline GUI_PIPELINE = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(GUI_PIPELINE_ID).withVertexShader(GUI_SHADER_ID).withFragmentShader(GUI_SHADER_ID).withVertexFormat(class_290.field_1584, VertexFormat.class_5596.field_27382).withSampler("Sampler0").withUniform("DynamicTransforms", class_10789.field_60031).withUniform("Projection", class_10789.field_60031).withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());
    private static final RenderPipeline GUI_GLOW_PIPELINE = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_60125}).withLocation(class_2960.method_60655((String)"recoin", (String)"pipeline/msdf_gui_glow")).withVertexShader(GUI_SHADER_ID).withFragmentShader(GUI_SHADER_ID).withShaderDefine("MSDF_GLOW").withVertexFormat(class_290.field_1584, VertexFormat.class_5596.field_27382).withSampler("Sampler0").withBlend(new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE)).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());
    private static boolean menuTextBatchActive;
    private final List<CharData> charBatch = new ArrayList<CharData>();
    private GpuBuffer uniformBuffer;
    private GpuBuffer dummyVertexBuffer;
    private ByteBuffer dataBuffer;
    private boolean initialized;
    private FontAtlas currentAtlas;
    private float currentOutlineWidth;
    private int currentOutlineColor;
    private final List<GuiTextBatch> guiTextBatches = new ArrayList<GuiTextBatch>();
    private final List<GuiCharData> queuedCharScratch = new ArrayList<GuiCharData>();
    private final Vector2f queuedGlyphP0 = new Vector2f();
    private final Vector2f queuedGlyphP1 = new Vector2f();
    private final Vector2f queuedGlyphP2 = new Vector2f();
    private final Vector2f queuedGlyphP3 = new Vector2f();

    public static void prepareLoadingGuiPipeline() throws IOException {
        String transforms = FontPipeline.readLoadingShader("include/dynamictransforms.glsl").replaceAll("(?m)^#version[^\\r\\n]*", "");
        String projection = FontPipeline.readLoadingShader("include/projection.glsl").replaceAll("(?m)^#version[^\\r\\n]*", "");
        String vertex = FontPipeline.readLoadingShader("core/msdf_gui.vsh").replace("#moj_import <minecraft:dynamictransforms.glsl>", transforms).replace("#moj_import <minecraft:projection.glsl>", projection);
        String fragment = FontPipeline.readLoadingShader("core/msdf_gui.fsh").replace("#moj_import <minecraft:dynamictransforms.glsl>", transforms);
        if (!RenderSystem.getDevice().precompilePipeline(GUI_PIPELINE, (id, type) -> type == ShaderType.VERTEX ? vertex : fragment).isValid()) {
            throw new IOException("Loading MSDF shader compilation failed");
        }
    }

    private static String readLoadingShader(String path) throws IOException {
        try (InputStream stream = FontPipeline.class.getResourceAsStream("/assets/minecraft/shaders/" + path);){
            if (stream == null) {
                throw new IOException("Missing loading font shader: " + path);
            }
            String string = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            return string;
        }
    }

    public static void setMenuTextBatchActive(boolean active) {
        menuTextBatchActive = active;
    }

    public static boolean isMenuTextBatchActive() {
        return menuTextBatchActive;
    }

    private int getFixedScaledWidth() {
        class_310 client = class_310.method_1551();
        if (client == null || client.method_22683() == null) {
            return 960;
        }
        return client.method_22683().method_4486();
    }

    private int getFixedScaledHeight() {
        class_310 client = class_310.method_1551();
        if (client == null || client.method_22683() == null) {
            return 540;
        }
        return client.method_22683().method_4502();
    }

    private float getFixedGuiScale() {
        class_310 client = class_310.method_1551();
        if (client == null || client.method_22683() == null) {
            return 2.0f;
        }
        return client.method_22683().method_4495();
    }

    private void ensureInitialized() {
        if (this.initialized) {
            return;
        }
        this.dataBuffer = MemoryUtil.memAlloc((int)16448);
        ByteBuffer dummyData = MemoryUtil.memAlloc((int)4);
        dummyData.putInt(0);
        dummyData.flip();
        this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "recoin:font_dummy_vertex", 32, dummyData);
        MemoryUtil.memFree((Buffer)dummyData);
        this.initialized = true;
    }

    public void drawText(FontAtlas atlas, String text, float x, float y, float size, int color) {
        this.drawText(atlas, text, x, y, size, color, 0.0f, 0, 0.0f);
    }

    public void drawTextQueued(class_332 context, FontAtlas atlas, String text, float x, float y, float size, int color) {
        this.drawTextQueued(context, atlas, text, x, y, size, color, false);
    }

    public void drawTextGlowQueued(class_332 context, FontAtlas atlas, String text, float x, float y, float size, int color) {
        this.drawTextQueued(context, atlas, text, x, y, size, color, true);
    }

    private void drawTextQueued(class_332 context, FontAtlas atlas, String text, float x, float y, float size, int color, boolean glow) {
        if (context == null || text == null || text.isEmpty()) {
            return;
        }
        atlas.ensureLoaded();
        if (atlas.getGlyphCount() == 0) {
            return;
        }
        List<GuiCharData> chars = this.queuedCharScratch;
        chars.clear();
        float scale = size / atlas.getFontSize();
        float pxRange = atlas.getDistanceRange();
        float cursorX = x;
        float cursorY = y;
        int currentColor = color;
        Matrix3x2fStack matrices = context.method_51448();
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -3.4028235E38f;
        float maxY = -3.4028235E38f;
        int i = 0;
        while (i < text.length()) {
            int codePoint = text.codePointAt(i);
            int charCount = Character.charCount(codePoint);
            FormatResult format = this.readFormat(text, i, charCount, color);
            if (format.consumed > 0) {
                if (format.hasColor) {
                    currentColor = format.color;
                }
                i += format.consumed;
                continue;
            }
            if (codePoint == 10) {
                cursorX = x;
                cursorY += atlas.getLineHeight() * scale;
                i += charCount;
                continue;
            }
            boolean syntheticUnderscore = false;
            MsdfGlyph glyph = atlas.getGlyph(codePoint);
            if (glyph == null && codePoint == 95) {
                glyph = atlas.getGlyph(45);
                boolean bl = syntheticUnderscore = glyph != null;
            }
            if (glyph == null) {
                glyph = atlas.getGlyph(63);
            }
            if (glyph == null) {
                cursorX += size * 0.5f;
                i += charCount;
                continue;
            }
            float yOffset = glyph.yOffset * scale + (syntheticUnderscore ? size * 0.35f : 0.0f);
            Vector2f p0 = matrices.transformPosition(cursorX + glyph.xOffset * scale, cursorY + yOffset, this.queuedGlyphP0);
            Vector2f p1 = matrices.transformPosition(cursorX + glyph.xOffset * scale, cursorY + yOffset + glyph.height * scale, this.queuedGlyphP1);
            Vector2f p2 = matrices.transformPosition(cursorX + glyph.xOffset * scale + glyph.width * scale, cursorY + yOffset + glyph.height * scale, this.queuedGlyphP2);
            Vector2f p3 = matrices.transformPosition(cursorX + glyph.xOffset * scale + glyph.width * scale, cursorY + yOffset, this.queuedGlyphP3);
            if (glyph.width > 0.0f && glyph.height > 0.0f) {
                chars.add(new GuiCharData(p0.x, p0.y, p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, glyph.u0, glyph.v0, glyph.u1, glyph.v1, currentColor, pxRange));
                minX = Math.min(minX, Math.min(Math.min(p0.x, p1.x), Math.min(p2.x, p3.x)));
                minY = Math.min(minY, Math.min(Math.min(p0.y, p1.y), Math.min(p2.y, p3.y)));
                maxX = Math.max(maxX, Math.max(Math.max(p0.x, p1.x), Math.max(p2.x, p3.x)));
                maxY = Math.max(maxY, Math.max(Math.max(p0.y, p1.y), Math.max(p2.y, p3.y)));
            }
            cursorX += glyph.xAdvance * scale;
            i += charCount;
        }
        if (!chars.isEmpty()) {
            class_8030 bounds = new class_8030((int)Math.floor(minX), (int)Math.floor(minY), Math.max(1, (int)Math.ceil(maxX) - (int)Math.floor(minX)), Math.max(1, (int)Math.ceil(maxY) - (int)Math.floor(minY)));
            class_8030 scissor = Main.getInstance().getScissorManager().currentScreenRect();
            if (menuTextBatchActive && !glow) {
                this.queueGuiText(atlas.getTextureId(), scissor, chars, minX, minY, maxX, maxY);
            } else {
                ((DrawContextAccessor)context).recoin$getState().method_70919((class_11244)new MsdfGuiElement(new ArrayList<GuiCharData>(chars), atlas.getTextureId(), bounds, scissor, glow));
            }
        }
        chars.clear();
    }

    public void flushQueuedText(class_332 context) {
        this.flushQueuedText(context, List.of());
    }

    public void flushQueuedText(class_332 context, List<FontRenderer.TextOcclusion> occlusions) {
        if (context == null || this.guiTextBatches.isEmpty()) {
            this.guiTextBatches.clear();
            return;
        }
        for (GuiTextBatch batch : this.guiTextBatches) {
            List<GuiCharData> visible = batch.chars;
            for (FontRenderer.TextOcclusion occlusion : occlusions) {
                ArrayList<GuiCharData> clipped = new ArrayList<GuiCharData>(visible.size());
                for (GuiCharData glyph : visible) {
                    FontPipeline.clipGlyph(glyph, occlusion, clipped);
                }
                visible = clipped;
                if (!visible.isEmpty()) continue;
                break;
            }
            if (visible.isEmpty()) continue;
            ((DrawContextAccessor)context).recoin$getState().method_70919((class_11244)new MsdfGuiElement(visible, batch.texture, batch.bounds(), batch.scissor));
        }
        this.guiTextBatches.clear();
    }

    private static void clipGlyph(GuiCharData glyph, FontRenderer.TextOcclusion mask, List<GuiCharData> output) {
        if (Math.abs(glyph.x0 - glyph.x1) > 0.001f || Math.abs(glyph.y0 - glyph.y3) > 0.001f || glyph.x3 <= glyph.x0 || glyph.y1 <= glyph.y0) {
            output.add(glyph);
            return;
        }
        float left = Math.max(glyph.x0, mask.x());
        float top = Math.max(glyph.y0, mask.y());
        float right = Math.min(glyph.x3, mask.x() + mask.width());
        float bottom = Math.min(glyph.y1, mask.y() + mask.height());
        if (left >= right || top >= bottom) {
            output.add(glyph);
            return;
        }
        FontPipeline.addGlyphFragment(glyph, glyph.x0, glyph.y0, glyph.x3, top, output);
        FontPipeline.addGlyphFragment(glyph, glyph.x0, bottom, glyph.x3, glyph.y1, output);
        FontPipeline.addGlyphFragment(glyph, glyph.x0, top, left, bottom, output);
        FontPipeline.addGlyphFragment(glyph, right, top, glyph.x3, bottom, output);
    }

    private static void addGlyphFragment(GuiCharData glyph, float left, float top, float right, float bottom, List<GuiCharData> output) {
        if (left >= right || top >= bottom) {
            return;
        }
        float width = glyph.x3 - glyph.x0;
        float height = glyph.y1 - glyph.y0;
        float u0 = glyph.u0 + (glyph.u1 - glyph.u0) * (left - glyph.x0) / width;
        float u1 = glyph.u0 + (glyph.u1 - glyph.u0) * (right - glyph.x0) / width;
        float v0 = glyph.v0 + (glyph.v1 - glyph.v0) * (top - glyph.y0) / height;
        float v1 = glyph.v0 + (glyph.v1 - glyph.v0) * (bottom - glyph.y0) / height;
        output.add(new GuiCharData(left, top, left, bottom, right, bottom, right, top, u0, v0, u1, v1, glyph.color, glyph.pxRange));
    }

    private void queueGuiText(class_2960 texture, class_8030 scissor, List<GuiCharData> chars, float minX, float minY, float maxX, float maxY) {
        for (GuiTextBatch batch : this.guiTextBatches) {
            if (!batch.texture.equals((Object)texture) || !Objects.equals(batch.scissor, scissor)) continue;
            batch.add(chars, minX, minY, maxX, maxY);
            return;
        }
        GuiTextBatch batch = new GuiTextBatch(texture, scissor);
        batch.add(chars, minX, minY, maxX, maxY);
        this.guiTextBatches.add(batch);
    }

    public void drawText(FontAtlas atlas, String text, float x, float y, float size, int color, float outlineWidth, int outlineColor, float rotation) {
        class_310 client = class_310.method_1551();
        if (client == null || client.method_1522() == null || text == null || text.isEmpty()) {
            return;
        }
        atlas.ensureLoaded();
        if (atlas.getGlyphCount() == 0) {
            return;
        }
        this.ensureInitialized();
        if (this.currentAtlas != null && (this.currentAtlas != atlas || this.currentOutlineWidth != outlineWidth || this.currentOutlineColor != outlineColor)) {
            this.flush();
        }
        this.currentAtlas = atlas;
        this.currentOutlineWidth = outlineWidth;
        this.currentOutlineColor = outlineColor;
        float scale = size / atlas.getFontSize();
        float cursorX = x;
        float cursorY = y;
        float pivotX = x + this.getTextWidth(atlas, text, size) / 2.0f;
        float pivotY = y + this.getTextHeight(atlas, text, size) / 2.0f;
        float rotationRad = (float)Math.toRadians(rotation);
        int currentColor = color;
        int i = 0;
        while (i < text.length()) {
            int codePoint = text.codePointAt(i);
            int charCount = Character.charCount(codePoint);
            FormatResult format = this.readFormat(text, i, charCount, color);
            if (format.consumed > 0) {
                if (format.hasColor) {
                    currentColor = format.color;
                }
                i += format.consumed;
                continue;
            }
            if (codePoint == 10) {
                cursorX = x;
                cursorY += atlas.getLineHeight() * scale;
                i += charCount;
                continue;
            }
            boolean syntheticUnderscore = false;
            MsdfGlyph glyph = atlas.getGlyph(codePoint);
            if (glyph == null && codePoint == 95) {
                glyph = atlas.getGlyph(45);
                boolean bl = syntheticUnderscore = glyph != null;
            }
            if (glyph == null) {
                glyph = atlas.getGlyph(63);
            }
            if (glyph == null) {
                cursorX += size * 0.5f;
                i += charCount;
                continue;
            }
            float glyphX = cursorX + glyph.xOffset * scale;
            float glyphY = cursorY + glyph.yOffset * scale + (syntheticUnderscore ? size * 0.35f : 0.0f);
            float glyphW = glyph.width * scale;
            float glyphH = glyph.height * scale;
            if (glyph.width > 0.0f && glyph.height > 0.0f) {
                this.charBatch.add(new CharData(glyphX, glyphY, glyphW, glyphH, glyph.u0, glyph.v0, glyph.u1, glyph.v1, currentColor, rotationRad, pivotX, pivotY, scale));
            }
            cursorX += glyph.xAdvance * scale;
            if (this.charBatch.size() >= 256) {
                this.flush();
                this.currentAtlas = atlas;
                this.currentOutlineWidth = outlineWidth;
                this.currentOutlineColor = outlineColor;
            }
            i += charCount;
        }
        if (!this.charBatch.isEmpty() && this.currentAtlas != null) {
            this.flush();
        }
    }

    public void flush() {
        GpuTexture gpuTexture;
        if (this.charBatch.isEmpty() || this.currentAtlas == null) {
            this.charBatch.clear();
            this.currentAtlas = null;
            return;
        }
        class_310 client = class_310.method_1551();
        if (client.method_1522() == null) {
            this.charBatch.clear();
            this.currentAtlas = null;
            return;
        }
        class_1044 texture = client.method_1531().method_4619(this.currentAtlas.getTextureId());
        try {
            gpuTexture = texture.method_68004();
        }
        catch (Exception exception) {
            this.charBatch.clear();
            this.currentAtlas = null;
            return;
        }
        this.prepareUniformData(this.currentAtlas, this.currentOutlineWidth, this.currentOutlineColor);
        int size = this.dataBuffer.remaining();
        if (this.uniformBuffer == null || this.uniformBuffer.size() < (long)size) {
            if (this.uniformBuffer != null) {
                this.uniformBuffer.close();
            }
            this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "recoin:font_uniform", 136, (long)size);
        }
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().method_71106((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)COLOR_MODULATOR, (Vector3fc)MODEL_OFFSET, (Matrix4fc)TEXTURE_MATRIX);
        class_12137 sampler = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
        GpuTextureView textureView = RenderSystem.getDevice().createTextureView(gpuTexture);
        try (RenderPass renderPass = encoder.createRenderPass(() -> "recoin:font_pass", client.method_1522().method_71639(), OptionalInt.empty(), client.method_1522().method_71640(), OptionalDouble.empty());){
            renderPass.setPipeline(PIPELINE);
            renderPass.setVertexBuffer(0, this.dummyVertexBuffer);
            renderPass.bindTexture("Sampler0", textureView, sampler);
            RenderSystem.bindDefaultUniforms((RenderPass)renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setUniform("FontData", this.uniformBuffer);
            renderPass.draw(0, this.charBatch.size() * 6);
        }
        textureView.close();
        this.charBatch.clear();
        this.currentAtlas = null;
    }

    private void prepareUniformData(FontAtlas atlas, float outlineWidth, int outlineColor) {
        this.dataBuffer.clear();
        this.dataBuffer.putFloat(this.getFixedScaledWidth());
        this.dataBuffer.putFloat(this.getFixedScaledHeight());
        this.dataBuffer.putFloat(this.getFixedGuiScale());
        this.dataBuffer.putFloat(outlineWidth);
        this.putColor(outlineColor);
        this.dataBuffer.putFloat(atlas.getAtlasWidth());
        this.dataBuffer.putFloat(atlas.getAtlasHeight());
        this.dataBuffer.putFloat(atlas.getDistanceRange());
        this.dataBuffer.putFloat(atlas.getFontSize());
        this.dataBuffer.putInt(this.charBatch.size());
        this.dataBuffer.putInt(0);
        this.dataBuffer.putInt(0);
        this.dataBuffer.putInt(0);
        for (CharData data : this.charBatch) {
            this.dataBuffer.putFloat(data.x);
            this.dataBuffer.putFloat(data.y);
            this.dataBuffer.putFloat(data.width);
            this.dataBuffer.putFloat(data.height);
            this.dataBuffer.putFloat(data.u0);
            this.dataBuffer.putFloat(data.v0);
            this.dataBuffer.putFloat(data.u1);
            this.dataBuffer.putFloat(data.v1);
            this.putColor(data.color);
            this.dataBuffer.putFloat(data.rotation);
            this.dataBuffer.putFloat(data.pivotX);
            this.dataBuffer.putFloat(data.pivotY);
            this.dataBuffer.putFloat(data.glyphScale);
        }
        this.dataBuffer.flip();
    }

    private void putColor(int color) {
        this.dataBuffer.putFloat((float)(color >> 16 & 0xFF) / 255.0f);
        this.dataBuffer.putFloat((float)(color >> 8 & 0xFF) / 255.0f);
        this.dataBuffer.putFloat((float)(color & 0xFF) / 255.0f);
        this.dataBuffer.putFloat((float)(color >> 24 & 0xFF) / 255.0f);
    }

    public float getTextWidth(FontAtlas atlas, String text, float size) {
        atlas.ensureLoaded();
        float scale = size / atlas.getFontSize();
        float width = 0.0f;
        float maxWidth = 0.0f;
        int i = 0;
        while (i < text.length()) {
            int codePoint = text.codePointAt(i);
            int charCount = Character.charCount(codePoint);
            FormatResult format = this.readFormat(text, i, charCount, 0);
            if (format.consumed > 0) {
                i += format.consumed;
                continue;
            }
            if (codePoint == 10) {
                maxWidth = Math.max(maxWidth, width);
                width = 0.0f;
                i += charCount;
                continue;
            }
            MsdfGlyph glyph = atlas.getGlyph(codePoint);
            if (glyph == null && codePoint == 95) {
                glyph = atlas.getGlyph(45);
            }
            if (glyph == null) {
                glyph = atlas.getGlyph(63);
            }
            width += glyph != null ? glyph.xAdvance * scale : size * 0.5f;
            i += charCount;
        }
        return Math.max(maxWidth, width);
    }

    public FontRenderer.TextBounds getTextBounds(FontAtlas atlas, String text, float size) {
        atlas.ensureLoaded();
        float scale = size / atlas.getFontSize();
        float cursorX = 0.0f;
        float cursorY = 0.0f;
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        int i = 0;
        while (i < text.length()) {
            int codePoint = text.codePointAt(i);
            int charCount = Character.charCount(codePoint);
            FormatResult format = this.readFormat(text, i, charCount, 0);
            if (format.consumed > 0) {
                i += format.consumed;
                continue;
            }
            i += charCount;
            if (codePoint == 10) {
                cursorX = 0.0f;
                cursorY += atlas.getLineHeight() * scale;
                continue;
            }
            boolean syntheticUnderscore = false;
            MsdfGlyph glyph = atlas.getGlyph(codePoint);
            if (glyph == null && codePoint == 95) {
                glyph = atlas.getGlyph(45);
                boolean bl = syntheticUnderscore = glyph != null;
            }
            if (glyph == null) {
                glyph = atlas.getGlyph(63);
            }
            if (glyph == null) {
                cursorX += size * 0.5f;
                continue;
            }
            if (glyph.width > 0.0f && glyph.height > 0.0f) {
                float x = cursorX + glyph.xOffset * scale;
                float y = cursorY + glyph.yOffset * scale + (syntheticUnderscore ? size * 0.35f : 0.0f);
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x + glyph.width * scale);
                maxY = Math.max(maxY, y + glyph.height * scale);
            }
            cursorX += glyph.xAdvance * scale;
        }
        return Float.isFinite(minX) ? new FontRenderer.TextBounds(minX, minY, maxX - minX, maxY - minY) : new FontRenderer.TextBounds(0.0f, 0.0f, 0.0f, 0.0f);
    }

    public float getTextHeight(FontAtlas atlas, String text, float size) {
        atlas.ensureLoaded();
        float scale = size / atlas.getFontSize();
        int lines = 1;
        for (int i = 0; i < text.length(); ++i) {
            if (text.charAt(i) != '\n') continue;
            ++lines;
        }
        return (float)lines * atlas.getLineHeight() * scale;
    }

    private FormatResult readFormat(String text, int index, int charCount, int defaultColor) {
        int end;
        int codePoint = text.codePointAt(index);
        if (codePoint != 167 && codePoint != 38 && codePoint != 65533 && codePoint != 9167 && codePoint != 1042) {
            return FormatResult.NONE;
        }
        if (codePoint == 1042 && index + charCount < text.length() && text.codePointAt(index + charCount) == 167) {
            charCount += Character.charCount(167);
            codePoint = 167;
        }
        if ((codePoint == 167 || codePoint == 38 || codePoint == 65533) && index + charCount < text.length()) {
            int code;
            int next = text.codePointAt(index + charCount);
            if (next == 35 && index + charCount + 7 <= text.length()) {
                try {
                    String hex = text.substring(index + charCount + 1, index + charCount + 7);
                    return new FormatResult(charCount + 7, true, this.inheritAlpha(0xFF000000 | Integer.parseInt(hex, 16), defaultColor));
                }
                catch (Exception hex) {
                    // empty catch block
                }
            }
            if ((code = "0123456789abcdefklmnor".indexOf(Character.toLowerCase((char)next))) >= 0) {
                int consumed = charCount + Character.charCount(next);
                if (code < 16) {
                    return new FormatResult(consumed, true, this.inheritAlpha(LEGACY_COLORS[code], defaultColor));
                }
                if (code == 21) {
                    return new FormatResult(consumed, true, defaultColor);
                }
                return new FormatResult(consumed, false, 0);
            }
        }
        if (codePoint == 9167 && (end = text.indexOf(9167, index + charCount)) > index) {
            try {
                return new FormatResult(end - index + 1, true, this.inheritAlpha(Integer.parseInt(text.substring(index + charCount, end)), defaultColor));
            }
            catch (NumberFormatException ignored) {
                return new FormatResult(end - index + 1, false, 0);
            }
        }
        return FormatResult.NONE;
    }

    private int inheritAlpha(int color, int defaultColor) {
        int inheritedAlpha = defaultColor >>> 24 & 0xFF;
        int colorAlpha = color >>> 24 & 0xFF;
        int alpha = colorAlpha * inheritedAlpha / 255;
        return color & 0xFFFFFF | alpha << 24;
    }

    static {
        for (int i = 0; i < 16; ++i) {
            int shadow = (i >> 3 & 1) * 85;
            int r = (i >> 2 & 1) * 170 + shadow;
            int g = (i >> 1 & 1) * 170 + shadow;
            int b = (i & 1) * 170 + shadow;
            if (i == 6) {
                r += 85;
            }
            FontPipeline.LEGACY_COLORS[i] = 0xFF000000 | r << 16 | g << 8 | b;
            FontPipeline.LEGACY_COLORS[i + 16] = (r & 0xFCFCFC) >> 2 << 24 | r << 16 | g << 8 | b;
        }
        menuTextBatchActive = false;
    }

    private record FormatResult(int consumed, boolean hasColor, int color) {
        private static final FormatResult NONE = new FormatResult(0, false, 0);
    }

    private record GuiCharData(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3, float u0, float v0, float u1, float v1, int color, float pxRange) {
    }

    private record MsdfGuiElement(List<GuiCharData> chars, class_2960 texture, class_8030 bounds, class_8030 scissor, boolean glow) implements class_11244 {
        private MsdfGuiElement(List<GuiCharData> chars, class_2960 texture, class_8030 bounds, class_8030 scissor) {
            this(chars, texture, bounds, scissor, false);
        }

        public void method_70917(class_4588 vertexConsumer) {
            for (GuiCharData data : this.chars) {
                int packedRange = Math.round(data.pxRange * 100.0f);
                vertexConsumer.method_22912(data.x0, data.y0, 0.0f).method_22913(data.u0, data.v0).method_39415(data.color).method_22921(packedRange, 240);
                vertexConsumer.method_22912(data.x1, data.y1, 0.0f).method_22913(data.u0, data.v1).method_39415(data.color).method_22921(packedRange, 240);
                vertexConsumer.method_22912(data.x2, data.y2, 0.0f).method_22913(data.u1, data.v1).method_39415(data.color).method_22921(packedRange, 240);
                vertexConsumer.method_22912(data.x3, data.y3, 0.0f).method_22913(data.u1, data.v0).method_39415(data.color).method_22921(packedRange, 240);
            }
        }

        public RenderPipeline comp_4055() {
            return this.glow ? GUI_GLOW_PIPELINE : GUI_PIPELINE;
        }

        public class_11231 comp_4056() {
            class_1044 abstractTexture = class_310.method_1551().method_1531().method_4619(this.texture);
            return GuiTextureCache.get(abstractTexture.method_71659(), RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR));
        }

        public class_8030 comp_4069() {
            return this.scissor;
        }

        public class_8030 comp_4274() {
            return this.bounds;
        }
    }

    private static final class GuiTextBatch {
        private final class_2960 texture;
        private final class_8030 scissor;
        private final List<GuiCharData> chars = new ArrayList<GuiCharData>();
        private float minX = Float.MAX_VALUE;
        private float minY = Float.MAX_VALUE;
        private float maxX = -3.4028235E38f;
        private float maxY = -3.4028235E38f;

        private GuiTextBatch(class_2960 texture, class_8030 scissor) {
            this.texture = texture;
            this.scissor = scissor;
        }

        private void add(List<GuiCharData> newChars, float minX, float minY, float maxX, float maxY) {
            this.chars.addAll(newChars);
            this.minX = Math.min(this.minX, minX);
            this.minY = Math.min(this.minY, minY);
            this.maxX = Math.max(this.maxX, maxX);
            this.maxY = Math.max(this.maxY, maxY);
        }

        private class_8030 bounds() {
            return new class_8030((int)Math.floor(this.minX), (int)Math.floor(this.minY), Math.max(1, (int)Math.ceil(this.maxX) - (int)Math.floor(this.minX)), Math.max(1, (int)Math.ceil(this.maxY) - (int)Math.floor(this.minY)));
        }
    }

    private record CharData(float x, float y, float width, float height, float u0, float v0, float u1, float v1, int color, float rotation, float pivotX, float pivotY, float glyphScale) {
    }
}

