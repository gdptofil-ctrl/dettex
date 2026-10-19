/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.DepthTestFunction
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.FilterMode
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_1044
 *  net.minecraft.class_10789
 *  net.minecraft.class_10799
 *  net.minecraft.class_11231
 *  net.minecraft.class_11244
 *  net.minecraft.class_276
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_4588
 *  net.minecraft.class_6367
 *  net.minecraft.class_8030
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 */
package ru.recoin.common.util.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.class_1044;
import net.minecraft.class_10789;
import net.minecraft.class_10799;
import net.minecraft.class_11231;
import net.minecraft.class_11244;
import net.minecraft.class_276;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4588;
import net.minecraft.class_6367;
import net.minecraft.class_8030;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.render.GuiTextureCache;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.core.Main;
import ru.recoin.mixins.DrawContextAccessor;

public final class ShapeShaderRenderer {
    private static class_276 liquidGlassSource;
    private static final Vector3f BOUNDS_P1;
    private static final Vector3f BOUNDS_P2;
    private static final Vector3f BOUNDS_P3;
    private static final Vector3f BOUNDS_P4;
    private static final Vector3f ICON_P0;
    private static final Vector3f ICON_P1;
    private static final Vector3f ICON_P2;
    private static final Vector3f ICON_P3;
    private static final Matrix4f ICON_MATRIX;
    private static final RenderPipeline ROUND_PIPELINE;
    private static final RenderPipeline ROUND_TEXTURE_PIPELINE;
    private static final RenderPipeline BLUR_PIPELINE;
    private static final RenderPipeline LIQUID_GLASS_PIPELINE;

    private ShapeShaderRenderer() {
    }

    public static boolean renderRound(ShapeProperties shape) {
        class_332 context = Render2DUtil.getActiveContext();
        if (context == null) {
            return false;
        }
        if (!ShapeShaderRenderer.hasVisibleRoundColor(shape)) {
            return true;
        }
        Bounds bounds = ShapeShaderRenderer.bounds(shape);
        if (bounds.width() <= 0.0f || bounds.height() <= 0.0f) {
            return true;
        }
        float shapeScale = ShapeShaderRenderer.shapeScale(shape, bounds);
        float maxCornerRadius = Math.min(bounds.width(), bounds.height()) / 2.0f;
        float rTR = Math.min(Math.max(0.0f, shape.getRound().x * shapeScale), maxCornerRadius);
        float rBR = Math.min(Math.max(0.0f, shape.getRound().y * shapeScale), maxCornerRadius);
        float rTL = Math.min(Math.max(0.0f, shape.getRound().z * shapeScale), maxCornerRadius);
        float rBL = Math.min(Math.max(0.0f, shape.getRound().w * shapeScale), maxCornerRadius);
        ((DrawContextAccessor)context).recoin$getState().method_70919((class_11244)new RoundGuiElement(bounds, rTL, rTR, rBL, rBR, shape.getThickness(), shape.getSoftness(), Render2DUtil.applyRenderAlpha(shape.getOutlineColor()), Render2DUtil.applyRenderAlpha(shape.getColor().x), Render2DUtil.applyRenderAlpha(shape.getColor().y), Render2DUtil.applyRenderAlpha(shape.getColor().z), Render2DUtil.applyRenderAlpha(shape.getColor().w), ShapeShaderRenderer.currentScissor()));
        return true;
    }

    public static boolean renderArc(ShapeProperties shape) {
        class_332 context = Render2DUtil.getActiveContext();
        if (context == null) {
            return false;
        }
        Bounds bounds = ShapeShaderRenderer.bounds(shape);
        if (bounds.width() <= 0.0f || bounds.height() <= 0.0f) {
            return true;
        }
        float start = shape.getStart();
        float end = shape.getEnd();
        if (!Float.isFinite(start) || !Float.isFinite(end) || start == end) {
            return true;
        }
        float cx = bounds.x() + bounds.width() / 2.0f;
        float cy = bounds.y() + bounds.height() / 2.0f;
        float outer = Math.min(bounds.width(), bounds.height()) / 2.0f;
        float thickness = shape.getThickness();
        float band = thickness <= 0.0f ? outer : Math.min(thickness, outer);
        float inner = Math.max(0.0f, outer - band);
        float sweep = Math.max(-360.0f, Math.min(360.0f, end - start));
        int segments = Math.max(4, Math.min(360, Math.round(Math.abs(sweep) / 2.0f)));
        float alpha = Render2DUtil.getRenderAlpha();
        int colorStart = ColorUtil.multAlpha(shape.getColor().x, alpha);
        int colorEnd = ColorUtil.multAlpha(shape.getColor().y, alpha);
        if (ColorUtil.alpha(colorStart) <= 0 && ColorUtil.alpha(colorEnd) <= 0) {
            return true;
        }
        ((DrawContextAccessor)context).recoin$getState().method_70919((class_11244)new ArcGuiElement(cx, cy, inner, outer, start, start + sweep, segments, colorStart, colorEnd, bounds.screenRect(), ShapeShaderRenderer.currentScissor()));
        return true;
    }

    public static boolean renderBlur(ShapeProperties shape) {
        class_310 client = class_310.method_1551();
        class_276 framebuffer = client.method_1522();
        return ShapeShaderRenderer.renderBlur(shape, framebuffer == null ? null : framebuffer.method_71639());
    }

    public static boolean renderBlur(ShapeProperties shape, GpuTextureView textureView) {
        class_332 context = Render2DUtil.getActiveContext();
        if (context == null || textureView == null) {
            return false;
        }
        Bounds bounds = ShapeShaderRenderer.bounds(shape);
        if (bounds.width() <= 0.0f || bounds.height() <= 0.0f) {
            return true;
        }
        ((DrawContextAccessor)context).recoin$getState().method_70919((class_11244)new BlurGuiElement(bounds, Math.min(ShapeShaderRenderer.maxRadius(shape) * ShapeShaderRenderer.shapeScale(shape, bounds), Math.min(bounds.width(), bounds.height()) / 2.0f), Render2DUtil.applyRenderAlpha(shape.getColor().x), textureView, ShapeShaderRenderer.currentScissor()));
        return true;
    }

    public static boolean renderLiquidGlass(ShapeProperties shape) {
        class_310 client = class_310.method_1551();
        class_276 framebuffer = client.method_1522();
        GpuTextureView source = liquidGlassSource != null ? liquidGlassSource.method_71639() : (framebuffer == null ? null : framebuffer.method_71639());
        return ShapeShaderRenderer.renderLiquidGlass(shape, source);
    }

    public static void captureLiquidGlassSource() {
        class_310 client = class_310.method_1551();
        class_276 framebuffer = client.method_1522();
        if (framebuffer == null || framebuffer.method_71639() == null) {
            return;
        }
        int width = client.method_22683().method_4489();
        int height = client.method_22683().method_4506();
        if (liquidGlassSource == null) {
            liquidGlassSource = new class_6367("recoin_liquid_glass_source", width, height, false);
        }
        if (ShapeShaderRenderer.liquidGlassSource.field_1482 != width || ShapeShaderRenderer.liquidGlassSource.field_1481 != height) {
            liquidGlassSource.method_1234(width, height);
        }
        framebuffer.method_68445(liquidGlassSource.method_71639());
    }

    public static boolean renderLiquidGlass(ShapeProperties shape, GpuTextureView textureView) {
        class_332 context = Render2DUtil.getActiveContext();
        if (context == null || textureView == null) {
            return false;
        }
        Bounds bounds = ShapeShaderRenderer.bounds(shape);
        if (bounds.width() <= 0.0f || bounds.height() <= 0.0f) {
            return true;
        }
        ((DrawContextAccessor)context).recoin$getState().method_70919((class_11244)new LiquidGlassGuiElement(bounds, Math.min(ShapeShaderRenderer.maxRadius(shape) * ShapeShaderRenderer.shapeScale(shape, bounds), Math.min(bounds.width(), bounds.height()) / 2.0f), shape.getColor().x, textureView, ShapeShaderRenderer.currentScissor()));
        return true;
    }

    public static boolean renderRoundTexture(ShapeProperties shape, GpuTextureView textureView, int color) {
        return ShapeShaderRenderer.renderRoundTexture(shape, textureView, color, 0.0f, 1.0f, 1.0f, 0.0f);
    }

    public static boolean renderRoundTexture(ShapeProperties shape, GpuTextureView textureView, int color, float u1, float v1, float u2, float v2) {
        class_332 context = Render2DUtil.getActiveContext();
        if (context == null || textureView == null) {
            return false;
        }
        if (Render2DUtil.getRenderAlpha() <= 0.0f || ColorUtil.alpha(color) <= 0) {
            return true;
        }
        Bounds bounds = ShapeShaderRenderer.bounds(shape);
        if (bounds.width() <= 0.0f || bounds.height() <= 0.0f) {
            return true;
        }
        ((DrawContextAccessor)context).recoin$getState().method_70919((class_11244)new RoundGpuTextureGuiElement(bounds, Math.min(ShapeShaderRenderer.maxRadius(shape) * ShapeShaderRenderer.shapeScale(shape, bounds), Math.min(bounds.width(), bounds.height()) / 2.0f), color, textureView, u1, v1, u2, v2, ShapeShaderRenderer.currentScissor()));
        return true;
    }

    public static boolean renderRoundTexture(ShapeProperties shape, class_2960 texture, int color) {
        return ShapeShaderRenderer.renderRoundTexture(shape, texture, color, 0.0f, 0.0f, 1.0f, 1.0f);
    }

    public static boolean renderRoundTexture(ShapeProperties shape, class_2960 texture, int color, float u1, float v1, float u2, float v2) {
        class_332 context = Render2DUtil.getActiveContext();
        if (context == null || texture == null) {
            return false;
        }
        if (Render2DUtil.getRenderAlpha() <= 0.0f || ColorUtil.alpha(color) <= 0) {
            return true;
        }
        Bounds bounds = ShapeShaderRenderer.bounds(shape);
        if (bounds.width() <= 0.0f || bounds.height() <= 0.0f) {
            return true;
        }
        ((DrawContextAccessor)context).recoin$getState().method_70919((class_11244)new RoundTextureGuiElement(bounds, Math.min(ShapeShaderRenderer.maxRadius(shape) * ShapeShaderRenderer.shapeScale(shape, bounds), Math.min(bounds.width(), bounds.height()) / 2.0f), color, texture, u1, v1, u2, v2, ShapeShaderRenderer.currentScissor()));
        return true;
    }

    public static boolean renderIcon(ShapeProperties shape, class_2960 texture, int color) {
        class_332 context = Render2DUtil.getActiveContext();
        if (context == null || texture == null) {
            return false;
        }
        if (Render2DUtil.getRenderAlpha() <= 0.0f || ColorUtil.alpha(color) <= 0) {
            return true;
        }
        float w = shape.getWidth();
        float h = shape.getHeight();
        if (w <= 0.0f || h <= 0.0f) {
            return true;
        }
        float pivotX = shape.getX() + w;
        float pivotY = shape.getY();
        ICON_MATRIX.set((Matrix4fc)shape.getMatrix().method_23760().method_23761());
        ICON_MATRIX.translate(pivotX, pivotY, 0.0f).rotateZ((float)Math.toRadians(90.0)).translate(-pivotX, -pivotY, 0.0f);
        float qw = h;
        float qh = w;
        ICON_MATRIX.transformPosition(pivotX, pivotY + qh, 0.0f, ICON_P0);
        ICON_MATRIX.transformPosition(pivotX + qw, pivotY + qh, 0.0f, ICON_P1);
        ICON_MATRIX.transformPosition(pivotX + qw, pivotY, 0.0f, ICON_P2);
        ICON_MATRIX.transformPosition(pivotX, pivotY, 0.0f, ICON_P3);
        float minX = Math.min(Math.min(ShapeShaderRenderer.ICON_P0.x, ShapeShaderRenderer.ICON_P1.x), Math.min(ShapeShaderRenderer.ICON_P2.x, ShapeShaderRenderer.ICON_P3.x));
        float maxX = Math.max(Math.max(ShapeShaderRenderer.ICON_P0.x, ShapeShaderRenderer.ICON_P1.x), Math.max(ShapeShaderRenderer.ICON_P2.x, ShapeShaderRenderer.ICON_P3.x));
        float minY = Math.min(Math.min(ShapeShaderRenderer.ICON_P0.y, ShapeShaderRenderer.ICON_P1.y), Math.min(ShapeShaderRenderer.ICON_P2.y, ShapeShaderRenderer.ICON_P3.y));
        float maxY = Math.max(Math.max(ShapeShaderRenderer.ICON_P0.y, ShapeShaderRenderer.ICON_P1.y), Math.max(ShapeShaderRenderer.ICON_P2.y, ShapeShaderRenderer.ICON_P3.y));
        class_8030 bounds = new class_8030((int)Math.floor(minX), (int)Math.floor(minY), Math.max(1, (int)Math.ceil(maxX - minX)), Math.max(1, (int)Math.ceil(maxY - minY)));
        ((DrawContextAccessor)context).recoin$getState().method_70919((class_11244)new TexturedQuadGuiElement(ShapeShaderRenderer.ICON_P0.x, ShapeShaderRenderer.ICON_P0.y, 0.0f, 0.0f, ShapeShaderRenderer.ICON_P1.x, ShapeShaderRenderer.ICON_P1.y, 0.0f, 1.0f, ShapeShaderRenderer.ICON_P2.x, ShapeShaderRenderer.ICON_P2.y, 1.0f, 1.0f, ShapeShaderRenderer.ICON_P3.x, ShapeShaderRenderer.ICON_P3.y, 1.0f, 0.0f, color, texture, bounds, ShapeShaderRenderer.currentScissor()));
        return true;
    }

    private static float maxRadius(ShapeProperties shape) {
        return Math.max(Math.max(shape.getRound().x, shape.getRound().y), Math.max(shape.getRound().z, shape.getRound().w));
    }

    private static float shapeScale(ShapeProperties shape, Bounds bounds) {
        float scaleX = Math.abs(shape.getWidth()) < 0.001f ? 1.0f : bounds.width() / Math.abs(shape.getWidth());
        float scaleY = Math.abs(shape.getHeight()) < 0.001f ? 1.0f : bounds.height() / Math.abs(shape.getHeight());
        return Math.max(0.01f, Math.min(scaleX, scaleY));
    }

    private static boolean hasVisibleRoundColor(ShapeProperties shape) {
        if (Render2DUtil.getRenderAlpha() <= 0.0f) {
            return false;
        }
        if (ColorUtil.alpha(shape.getColor().x) > 0 || ColorUtil.alpha(shape.getColor().y) > 0 || ColorUtil.alpha(shape.getColor().z) > 0 || ColorUtil.alpha(shape.getColor().w) > 0) {
            return true;
        }
        return shape.getThickness() > 0.0f && ColorUtil.alpha(shape.getOutlineColor()) > 0;
    }

    private static class_8030 currentScissor() {
        Main main = Main.getInstance();
        return main == null ? null : main.getScissorManager().currentScreenRect();
    }

    private static Bounds bounds(ShapeProperties shape) {
        Matrix4f matrix = shape.getMatrix().method_23760().method_23761();
        matrix.transformPosition(shape.getX(), shape.getY(), 0.0f, BOUNDS_P1);
        matrix.transformPosition(shape.getX() + shape.getWidth(), shape.getY(), 0.0f, BOUNDS_P2);
        matrix.transformPosition(shape.getX() + shape.getWidth(), shape.getY() + shape.getHeight(), 0.0f, BOUNDS_P3);
        matrix.transformPosition(shape.getX(), shape.getY() + shape.getHeight(), 0.0f, BOUNDS_P4);
        Vector3f p1 = BOUNDS_P1;
        Vector3f p2 = BOUNDS_P2;
        Vector3f p3 = BOUNDS_P3;
        Vector3f p4 = BOUNDS_P4;
        float minX = Math.min(Math.min(p1.x, p2.x), Math.min(p3.x, p4.x));
        float maxX = Math.max(Math.max(p1.x, p2.x), Math.max(p3.x, p4.x));
        float minY = Math.min(Math.min(p1.y, p2.y), Math.min(p3.y, p4.y));
        float maxY = Math.max(Math.max(p1.y, p2.y), Math.max(p3.y, p4.y));
        return new Bounds(minX, minY, maxX - minX, maxY - minY);
    }

    static {
        BOUNDS_P1 = new Vector3f();
        BOUNDS_P2 = new Vector3f();
        BOUNDS_P3 = new Vector3f();
        BOUNDS_P4 = new Vector3f();
        ICON_P0 = new Vector3f();
        ICON_P1 = new Vector3f();
        ICON_P2 = new Vector3f();
        ICON_P3 = new Vector3f();
        ICON_MATRIX = new Matrix4f();
        ROUND_PIPELINE = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"recoin", (String)"pipeline/round_gui")).withVertexShader(class_2960.method_60655((String)"minecraft", (String)"core/recoin_round")).withFragmentShader(class_2960.method_60655((String)"minecraft", (String)"core/recoin_round")).withUniform("DynamicTransforms", class_10789.field_60031).withUniform("Projection", class_10789.field_60031).withVertexFormat(class_290.field_1584, VertexFormat.class_5596.field_27382).withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build();
        ROUND_TEXTURE_PIPELINE = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"recoin", (String)"pipeline/round_gui_texture")).withVertexShader(class_2960.method_60655((String)"minecraft", (String)"core/recoin_round_tex")).withFragmentShader(class_2960.method_60655((String)"minecraft", (String)"core/recoin_round_tex")).withSampler("Sampler0").withUniform("DynamicTransforms", class_10789.field_60031).withUniform("Projection", class_10789.field_60031).withVertexFormat(class_290.field_1590, VertexFormat.class_5596.field_27382).withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build();
        BLUR_PIPELINE = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"recoin", (String)"pipeline/blur_gui")).withVertexShader(class_2960.method_60655((String)"minecraft", (String)"core/recoin_blur")).withFragmentShader(class_2960.method_60655((String)"minecraft", (String)"core/recoin_blur")).withSampler("Sampler0").withUniform("DynamicTransforms", class_10789.field_60031).withUniform("Projection", class_10789.field_60031).withVertexFormat(class_290.field_1584, VertexFormat.class_5596.field_27382).withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build();
        LIQUID_GLASS_PIPELINE = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"recoin", (String)"pipeline/liquid_glass_gui")).withVertexShader(class_2960.method_60655((String)"minecraft", (String)"core/recoin_liquid_glass")).withFragmentShader(class_2960.method_60655((String)"minecraft", (String)"core/recoin_liquid_glass")).withSampler("Sampler0").withUniform("DynamicTransforms", class_10789.field_60031).withUniform("Projection", class_10789.field_60031).withVertexFormat(class_290.field_1584, VertexFormat.class_5596.field_27382).withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build();
    }

    private record Bounds(float x, float y, float width, float height) {
        class_8030 screenRect() {
            return new class_8030((int)Math.floor(this.x), (int)Math.floor(this.y), (int)Math.ceil(this.width), (int)Math.ceil(this.height));
        }
    }

    private record RoundGuiElement(Bounds rect, float radiusTL, float radiusTR, float radiusBL, float radiusBR, float thickness, float softness, int outlineColor, int colorTopLeft, int colorBottomLeft, int colorTopRight, int colorBottomRight, class_8030 scissor) implements class_11244
    {
        public void method_70917(class_4588 vertexConsumer) {
            float x0 = this.rect.x();
            float y0 = this.rect.y();
            float x1 = this.rect.x() + this.rect.width();
            float y1 = this.rect.y() + this.rect.height();
            float radiusLimit = Math.min(this.rect.width(), this.rect.height()) * 0.5f;
            int packedTopLeftTopRight = RoundGuiElement.packRadiiByte(this.radiusTL, this.radiusTR, radiusLimit);
            int packedBottomLeftBottomRight = RoundGuiElement.packRadiiByte(this.radiusBL, this.radiusBR, radiusLimit);
            float halfW = this.rect.width() * 0.5f;
            float halfH = this.rect.height() * 0.5f;
            vertexConsumer.method_22912(x0, y0, this.thickness).method_22913(-halfW, -halfH).method_39415(this.colorTopLeft).method_22921(packedTopLeftTopRight, packedBottomLeftBottomRight);
            vertexConsumer.method_22912(x0, y1, this.thickness).method_22913(-halfW, halfH).method_39415(this.colorBottomLeft).method_22921(packedTopLeftTopRight, packedBottomLeftBottomRight);
            vertexConsumer.method_22912(x1, y1, this.thickness).method_22913(halfW, halfH).method_39415(this.colorBottomRight).method_22921(packedTopLeftTopRight, packedBottomLeftBottomRight);
            vertexConsumer.method_22912(x1, y0, this.thickness).method_22913(halfW, -halfH).method_39415(this.colorTopRight).method_22921(packedTopLeftTopRight, packedBottomLeftBottomRight);
        }

        private static int packRadiiByte(float firstRadius, float secondRadius, float radiusLimit) {
            float precision = 255.0f / Math.max(radiusLimit, 0.001f);
            int first = Math.max(0, Math.min(255, Math.round(firstRadius * precision)));
            int second = Math.max(0, Math.min(255, Math.round(secondRadius * precision)));
            return (first & 0xFF) << 8 | second & 0xFF;
        }

        public RenderPipeline comp_4055() {
            return ROUND_PIPELINE;
        }

        public class_11231 comp_4056() {
            return class_11231.method_70899();
        }

        public class_8030 comp_4069() {
            return this.scissor;
        }

        public class_8030 comp_4274() {
            int left = (int)Math.floor(this.rect.x() - 0.75f);
            int top = (int)Math.floor(this.rect.y() - 0.75f);
            return new class_8030(left, top, (int)Math.ceil(this.rect.x() + this.rect.width() + 0.75f) - left, (int)Math.ceil(this.rect.y() + this.rect.height() + 0.75f) - top);
        }
    }

    private record ArcGuiElement(float cx, float cy, float innerRadius, float outerRadius, float startDeg, float endDeg, int segments, int colorStart, int colorEnd, class_8030 bounds, class_8030 scissor) implements class_11244
    {

        public void method_70917(class_4588 vertexConsumer) {
            float aa = 0.8f;
            for (int i = 0; i < this.segments; ++i) {
                float t0 = (float)i / (float)this.segments;
                float t1 = (float)(i + 1) / (float)this.segments;
                float a0 = (float)Math.toRadians(this.startDeg + (this.endDeg - this.startDeg) * t0);
                float a1 = (float)Math.toRadians(this.startDeg + (this.endDeg - this.startDeg) * t1);
                float sin0 = (float)Math.sin(a0);
                float cos0 = (float)Math.cos(a0);
                float sin1 = (float)Math.sin(a1);
                float cos1 = (float)Math.cos(a1);
                int c0 = ArcGuiElement.lerpColor(this.colorStart, this.colorEnd, t0);
                int c1 = ArcGuiElement.lerpColor(this.colorStart, this.colorEnd, t1);
                this.band(vertexConsumer, sin0, cos0, sin1, cos1, this.innerRadius, this.outerRadius, c0, c1, 1.0f, 1.0f);
                this.band(vertexConsumer, sin0, cos0, sin1, cos1, this.outerRadius, this.outerRadius + 0.8f, c0, c1, 1.0f, 0.0f);
                if (!(this.innerRadius > 0.0f)) continue;
                float in = Math.max(0.0f, this.innerRadius - 0.8f);
                this.band(vertexConsumer, sin0, cos0, sin1, cos1, in, this.innerRadius, c0, c1, 0.0f, 1.0f);
            }
        }

        private void band(class_4588 vc, float sin0, float cos0, float sin1, float cos1, float rIn, float rOut, int c0, int c1, float alphaIn, float alphaOut) {
            float ox0 = this.cx + rOut * sin0;
            float oy0 = this.cy - rOut * cos0;
            float ix0 = this.cx + rIn * sin0;
            float iy0 = this.cy - rIn * cos0;
            float ox1 = this.cx + rOut * sin1;
            float oy1 = this.cy - rOut * cos1;
            float ix1 = this.cx + rIn * sin1;
            float iy1 = this.cy - rIn * cos1;
            int cOut0 = ArcGuiElement.scaleAlpha(c0, alphaOut);
            int cIn0 = ArcGuiElement.scaleAlpha(c0, alphaIn);
            int cOut1 = ArcGuiElement.scaleAlpha(c1, alphaOut);
            int cIn1 = ArcGuiElement.scaleAlpha(c1, alphaIn);
            vc.method_22912(ox0, oy0, 0.0f).method_39415(cOut0);
            vc.method_22912(ix0, iy0, 0.0f).method_39415(cIn0);
            vc.method_22912(ix1, iy1, 0.0f).method_39415(cIn1);
            vc.method_22912(ox1, oy1, 0.0f).method_39415(cOut1);
        }

        private static int scaleAlpha(int color, float mult) {
            int alpha = Math.round((float)(color >>> 24 & 0xFF) * Math.max(0.0f, Math.min(1.0f, mult)));
            return alpha << 24 | color & 0xFFFFFF;
        }

        private static int lerpColor(int a, int b, float t) {
            int aa = a >>> 24 & 0xFF;
            int ar = a >>> 16 & 0xFF;
            int ag = a >>> 8 & 0xFF;
            int ab = a & 0xFF;
            int ba = b >>> 24 & 0xFF;
            int br = b >>> 16 & 0xFF;
            int bg = b >>> 8 & 0xFF;
            int bb = b & 0xFF;
            int oa = Math.round((float)aa + (float)(ba - aa) * t);
            int or = Math.round((float)ar + (float)(br - ar) * t);
            int og = Math.round((float)ag + (float)(bg - ag) * t);
            int ob = Math.round((float)ab + (float)(bb - ab) * t);
            return oa << 24 | or << 16 | og << 8 | ob;
        }

        public RenderPipeline comp_4055() {
            return class_10799.field_56879;
        }

        public class_11231 comp_4056() {
            return class_11231.method_70899();
        }

        public class_8030 comp_4069() {
            return this.scissor;
        }

        public class_8030 comp_4274() {
            return this.bounds;
        }

        }

    private record BlurGuiElement(Bounds rect, float radius, int tintColor, GpuTextureView source, class_8030 scissor) implements class_11244
    {
        public void method_70917(class_4588 vertexConsumer) {
            class_310 client = class_310.method_1551();
            float x0 = this.rect.x();
            float y0 = this.rect.y();
            float x1 = this.rect.x() + this.rect.width();
            float y1 = this.rect.y() + this.rect.height();
            int halfWidth = Math.max(1, Math.round(this.rect.width() * 8.0f));
            int halfHeight = Math.max(1, Math.round(this.rect.height() * 8.0f));
            vertexConsumer.method_22912(x0, y0, this.radius).method_22913(0.0f, 0.0f).method_39415(this.tintColor).method_22921(halfWidth, halfHeight);
            vertexConsumer.method_22912(x0, y1, this.radius).method_22913(0.0f, 1.0f).method_39415(this.tintColor).method_22921(halfWidth, halfHeight);
            vertexConsumer.method_22912(x1, y1, this.radius).method_22913(1.0f, 1.0f).method_39415(this.tintColor).method_22921(halfWidth, halfHeight);
            vertexConsumer.method_22912(x1, y0, this.radius).method_22913(1.0f, 0.0f).method_39415(this.tintColor).method_22921(halfWidth, halfHeight);
        }

        public RenderPipeline comp_4055() {
            return BLUR_PIPELINE;
        }

        public class_11231 comp_4056() {
            class_310 client = class_310.method_1551();
            return GuiTextureCache.get(this.source, RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR));
        }

        public class_8030 comp_4069() {
            return this.scissor;
        }

        public class_8030 comp_4274() {
            return this.rect.screenRect();
        }
    }

    private record LiquidGlassGuiElement(Bounds rect, float radius, int tintColor, GpuTextureView source, class_8030 scissor) implements class_11244
    {
        public void method_70917(class_4588 vertexConsumer) {
            float x0 = this.rect.x();
            float y0 = this.rect.y();
            float x1 = this.rect.x() + this.rect.width();
            float y1 = this.rect.y() + this.rect.height();
            int halfWidth = Math.max(1, Math.round(this.rect.width() * 8.0f));
            int halfHeight = Math.max(1, Math.round(this.rect.height() * 8.0f));
            vertexConsumer.method_22912(x0, y0, this.radius).method_22913(0.0f, 0.0f).method_39415(this.tintColor).method_22921(halfWidth, halfHeight);
            vertexConsumer.method_22912(x0, y1, this.radius).method_22913(0.0f, 1.0f).method_39415(this.tintColor).method_22921(halfWidth, halfHeight);
            vertexConsumer.method_22912(x1, y1, this.radius).method_22913(1.0f, 1.0f).method_39415(this.tintColor).method_22921(halfWidth, halfHeight);
            vertexConsumer.method_22912(x1, y0, this.radius).method_22913(1.0f, 0.0f).method_39415(this.tintColor).method_22921(halfWidth, halfHeight);
        }

        public RenderPipeline comp_4055() {
            return LIQUID_GLASS_PIPELINE;
        }

        public class_11231 comp_4056() {
            return GuiTextureCache.get(this.source, RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR));
        }

        public class_8030 comp_4069() {
            return this.scissor;
        }

        public class_8030 comp_4274() {
            return this.rect.screenRect();
        }
    }

    private record RoundGpuTextureGuiElement(Bounds rect, float radius, int color, GpuTextureView textureView, float u1, float v1, float u2, float v2, class_8030 scissor) implements class_11244
    {
        public void method_70917(class_4588 vertexConsumer) {
            float x0 = this.rect.x();
            float y0 = this.rect.y();
            float x1 = this.rect.x() + this.rect.width();
            float y1 = this.rect.y() + this.rect.height();
            int halfWidth = Math.max(1, Math.round(this.rect.width() * 8.0f));
            int halfHeight = Math.max(1, Math.round(this.rect.height() * 8.0f));
            vertexConsumer.method_22912(x0, y0, this.radius).method_39415(this.color).method_22913(this.u1, this.v1).method_22921(halfWidth, halfHeight).method_22914(0.0f, 0.0f, 0.0f);
            vertexConsumer.method_22912(x0, y1, this.radius).method_39415(this.color).method_22913(this.u1, this.v2).method_22921(halfWidth, halfHeight).method_22914(0.0f, 1.0f, 0.0f);
            vertexConsumer.method_22912(x1, y1, this.radius).method_39415(this.color).method_22913(this.u2, this.v2).method_22921(halfWidth, halfHeight).method_22914(1.0f, 1.0f, 0.0f);
            vertexConsumer.method_22912(x1, y0, this.radius).method_39415(this.color).method_22913(this.u2, this.v1).method_22921(halfWidth, halfHeight).method_22914(1.0f, 0.0f, 0.0f);
        }

        public RenderPipeline comp_4055() {
            return ROUND_TEXTURE_PIPELINE;
        }

        public class_11231 comp_4056() {
            return GuiTextureCache.get(this.textureView, RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR));
        }

        public class_8030 comp_4069() {
            return this.scissor;
        }

        public class_8030 comp_4274() {
            return this.rect.screenRect();
        }
    }

    private record RoundTextureGuiElement(Bounds rect, float radius, int color, class_2960 texture, float u1, float v1, float u2, float v2, class_8030 scissor) implements class_11244
    {
        public void method_70917(class_4588 vertexConsumer) {
            float x0 = this.rect.x();
            float y0 = this.rect.y();
            float x1 = this.rect.x() + this.rect.width();
            float y1 = this.rect.y() + this.rect.height();
            int halfWidth = Math.max(1, Math.round(this.rect.width() * 8.0f));
            int halfHeight = Math.max(1, Math.round(this.rect.height() * 8.0f));
            vertexConsumer.method_22912(x0, y0, this.radius).method_39415(this.color).method_22913(this.u1, this.v1).method_22921(halfWidth, halfHeight).method_22914(0.0f, 0.0f, 0.0f);
            vertexConsumer.method_22912(x0, y1, this.radius).method_39415(this.color).method_22913(this.u1, this.v2).method_22921(halfWidth, halfHeight).method_22914(0.0f, 1.0f, 0.0f);
            vertexConsumer.method_22912(x1, y1, this.radius).method_39415(this.color).method_22913(this.u2, this.v2).method_22921(halfWidth, halfHeight).method_22914(1.0f, 1.0f, 0.0f);
            vertexConsumer.method_22912(x1, y0, this.radius).method_39415(this.color).method_22913(this.u2, this.v1).method_22921(halfWidth, halfHeight).method_22914(1.0f, 0.0f, 0.0f);
        }

        public RenderPipeline comp_4055() {
            return ROUND_TEXTURE_PIPELINE;
        }

        public class_11231 comp_4056() {
            class_1044 abstractTexture = class_310.method_1551().method_1531().method_4619(this.texture);
            return GuiTextureCache.get(abstractTexture.method_71659(), abstractTexture.method_75484());
        }

        public class_8030 comp_4069() {
            return this.scissor;
        }

        public class_8030 comp_4274() {
            return this.rect.screenRect();
        }
    }

    private record TexturedQuadGuiElement(float x0, float y0, float u0, float v0, float x1, float y1, float u1, float v1, float x2, float y2, float u2, float v2, float x3, float y3, float u3, float v3, int color, class_2960 texture, class_8030 bounds, class_8030 scissor) implements class_11244
    {

        public void method_70917(class_4588 vertexConsumer) {
            vertexConsumer.method_22912(this.x0, this.y0, 0.0f).method_22913(this.u0, this.v0).method_39415(this.color);
            vertexConsumer.method_22912(this.x1, this.y1, 0.0f).method_22913(this.u1, this.v1).method_39415(this.color);
            vertexConsumer.method_22912(this.x2, this.y2, 0.0f).method_22913(this.u2, this.v2).method_39415(this.color);
            vertexConsumer.method_22912(this.x3, this.y3, 0.0f).method_22913(this.u3, this.v3).method_39415(this.color);
        }

        public RenderPipeline comp_4055() {
            return class_10799.field_56883;
        }

        public class_11231 comp_4056() {
            class_1044 abstractTexture = class_310.method_1551().method_1531().method_4619(this.texture);
            return GuiTextureCache.get(abstractTexture.method_71659(), abstractTexture.method_75484());
        }

        public class_8030 comp_4069() {
            return this.scissor;
        }

        public class_8030 comp_4274() {
            return this.bounds;
        }

        }
}

