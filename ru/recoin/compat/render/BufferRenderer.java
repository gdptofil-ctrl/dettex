/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.DepthTestFunction
 *  com.mojang.blaze3d.platform.DestFactor
 *  com.mojang.blaze3d.platform.SourceFactor
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_10799
 *  net.minecraft.class_12247
 *  net.minecraft.class_12247$class_12248
 *  net.minecraft.class_1921
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_9801
 *  net.minecraft.class_9801$class_4574
 */
package ru.recoin.compat.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_10799;
import net.minecraft.class_12247;
import net.minecraft.class_1921;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_9801;
import ru.recoin.common.util.render.LegacyRenderSystem;

public final class BufferRenderer {
    private static final class_2960 WHITE_TEXTURE = class_2960.method_60655((String)"minecraft", (String)"textures/block/white_concrete.png");
    private static final BlendFunction SRC_ALPHA_ADDITIVE = new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE);
    private static final Map<PipelineKey, RenderPipeline> PIPELINES = new HashMap<PipelineKey, RenderPipeline>();
    private static final Map<LayerKey, class_1921> LAYERS = new LinkedHashMap<LayerKey, class_1921>(32, 0.75f, true){

        @Override
        protected boolean removeEldestEntry(Map.Entry<LayerKey, class_1921> eldest) {
            return this.size() > 256;
        }
    };

    private BufferRenderer() {
    }

    public static void drawWithGlobalProgram(class_9801 buffer) {
        LayerKey key;
        class_1921 layer;
        class_9801.class_4574 parameters = buffer.method_60822();
        VertexFormat format = parameters.comp_749();
        VertexFormat.class_5596 mode = parameters.comp_752();
        RenderPipeline pipeline = BufferRenderer.createPipeline(format, mode, LegacyRenderSystem.isAdditiveBlend(), LegacyRenderSystem.isDepthTestEnabled(), LegacyRenderSystem.isDepthWriteEnabled());
        class_2960 texture = null;
        if (format.equals((Object)class_290.field_1575)) {
            class_2960 class_29602 = texture = LegacyRenderSystem.getShaderTexture() != null ? LegacyRenderSystem.getShaderTexture() : WHITE_TEXTURE;
        }
        if ((layer = LAYERS.get(key = new LayerKey(pipeline, texture))) == null) {
            class_12247.class_12248 setup = class_12247.method_75927((RenderPipeline)pipeline).method_75937().method_75929(256);
            if (texture != null) {
                setup.method_75934("Sampler0", texture);
            }
            layer = class_1921.method_75940((String)("recoin_compat_" + mode.name().toLowerCase(Locale.ROOT)), (class_12247)setup.method_75938());
            LAYERS.put(key, layer);
        }
        layer.method_60895(buffer);
    }

    public static void draw(class_9801 buffer) {
        BufferRenderer.drawWithGlobalProgram(buffer);
    }

    public static class_1921 translucentPositionColorLayer(String name, VertexFormat.class_5596 mode, int expectedBufferSize) {
        RenderPipeline pipeline = BufferRenderer.createPipeline(class_290.field_1576, mode, false);
        return class_1921.method_75940((String)name, (class_12247)class_12247.method_75927((RenderPipeline)pipeline).method_75937().method_75929(expectedBufferSize).method_75938());
    }

    public static class_1921 translucentPositionColorDepthLayer(String name, VertexFormat.class_5596 mode, int expectedBufferSize) {
        return BufferRenderer.translucentPositionColorDepthLayer(name, mode, expectedBufferSize, true);
    }

    public static class_1921 translucentPositionColorDepthReadLayer(String name, VertexFormat.class_5596 mode, int expectedBufferSize) {
        return BufferRenderer.translucentPositionColorDepthLayer(name, mode, expectedBufferSize, false);
    }

    private static class_1921 translucentPositionColorDepthLayer(String name, VertexFormat.class_5596 mode, int expectedBufferSize, boolean depthWrite) {
        RenderPipeline pipeline = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_56860}).withLocation(class_2960.method_60655((String)"recoin", (String)("compat_depth/" + name + "/" + mode.name().toLowerCase() + (depthWrite ? "_write" : "_read")))).withVertexFormat(class_290.field_1576, mode).withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST).withDepthWrite(depthWrite).withCull(false).build();
        return class_1921.method_75940((String)name, (class_12247)class_12247.method_75927((RenderPipeline)pipeline).method_75937().method_75929(expectedBufferSize).method_75938());
    }

    public static RenderPipeline positionColorPipeline(VertexFormat.class_5596 mode) {
        return BufferRenderer.createPipeline(class_290.field_1576, mode, false, false, false);
    }

    private static RenderPipeline createPipeline(VertexFormat format, VertexFormat.class_5596 mode, boolean additiveBlend) {
        return BufferRenderer.createPipeline(format, mode, additiveBlend, false, false);
    }

    private static RenderPipeline createPipeline(VertexFormat format, VertexFormat.class_5596 mode, boolean additiveBlend, boolean depthTest, boolean depthWrite) {
        PipelineKey key = new PipelineKey(format, mode, additiveBlend, depthTest, depthWrite);
        RenderPipeline pipeline = PIPELINES.get(key);
        if (pipeline != null) {
            return pipeline;
        }
        RenderPipeline.Snippet snippet = format.equals((Object)class_290.field_1575) ? class_10799.field_56864 : class_10799.field_56860;
        String location = "compat/" + mode.name().toLowerCase(Locale.ROOT) + "/" + Integer.toHexString(format.hashCode()) + (additiveBlend ? "_add" : "_alpha") + (depthTest ? "_dt" : "") + (depthWrite ? "_dw" : "");
        pipeline = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{snippet}).withLocation(class_2960.method_60655((String)"recoin", (String)location)).withVertexFormat(format, mode).withBlend(additiveBlend ? SRC_ALPHA_ADDITIVE : BlendFunction.TRANSLUCENT).withDepthTestFunction(depthTest ? DepthTestFunction.LEQUAL_DEPTH_TEST : DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(depthWrite).withCull(false).build();
        PIPELINES.put(key, pipeline);
        return pipeline;
    }

    private record LayerKey(RenderPipeline pipeline, class_2960 texture) {
    }

    private record PipelineKey(VertexFormat format, VertexFormat.class_5596 mode, boolean additive, boolean depthTest, boolean depthWrite) {
    }
}

