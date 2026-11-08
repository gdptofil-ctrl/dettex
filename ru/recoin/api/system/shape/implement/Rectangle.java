/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 */
package ru.recoin.api.system.shape.implement;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import ru.recoin.api.system.shape.Shape;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.common.util.render.ShapeShaderRenderer;
import ru.recoin.common.util.render.pipeline.DirectRectPipeline;
import ru.recoin.compat.render.BufferRenderer;

public class Rectangle
implements Shape,
QuickImports {
    private static final DirectRectPipeline DIRECT_PIPELINE = new DirectRectPipeline();
    private final int[] directColors = new int[4];
    private final int[] directSingleColor = new int[1];
    private final float[] directRadii = new float[4];
    private final Vector3f p1 = new Vector3f();
    private final Vector3f p2 = new Vector3f();
    private final Vector3f p3 = new Vector3f();
    private final Vector3f p4 = new Vector3f();
    private final Bounds directBounds = new Bounds();

    @Override
    public void render(ShapeProperties shape) {
        if (Render2DUtil.getRenderAlpha() <= 0.0f) {
            return;
        }
        if (Render2DUtil.isQueuedShapeActive() && ShapeShaderRenderer.renderRound(shape)) {
            return;
        }
        if (this.renderDirect(shape)) {
            return;
        }
        if (ShapeShaderRenderer.renderRound(shape)) {
            return;
        }
        Matrix4f matrix4f = shape.getMatrix().method_23760().method_23761();
        class_287 buffer = ((class_289)tessellator.get()).method_60827(VertexFormat.class_5596.field_27382, class_290.field_1576);
        drawEngine.quad(matrix4f, buffer, shape.getX(), shape.getY(), shape.getWidth(), shape.getHeight(), shape.getColor().x);
        BufferRenderer.drawWithGlobalProgram(buffer.method_60800());
    }

    private boolean renderDirect(ShapeProperties shape) {
        try {
            Bounds bounds = this.bounds(shape);
            if (bounds.width <= 0.0f || bounds.height <= 0.0f) {
                return true;
            }
            if (this.shouldDecorate(shape, bounds)) {
                this.drawThemeShadow(bounds, shape);
                this.drawThemeOutline(bounds, shape);
            }
            this.directColors[0] = Render2DUtil.applyRenderAlpha(shape.getColor().x);
            this.directColors[1] = Render2DUtil.applyRenderAlpha(shape.getColor().z);
            this.directColors[2] = Render2DUtil.applyRenderAlpha(shape.getColor().w);
            this.directColors[3] = Render2DUtil.applyRenderAlpha(shape.getColor().y);
            if (ColorUtil.alpha(this.directColors[0]) <= 0 && ColorUtil.alpha(this.directColors[1]) <= 0 && ColorUtil.alpha(this.directColors[2]) <= 0 && ColorUtil.alpha(this.directColors[3]) <= 0) {
                return true;
            }
            DIRECT_PIPELINE.drawRect(bounds.x, bounds.y, bounds.width, bounds.height, this.directColors, this.remapRadii(shape), shape.getSoftness());
            return true;
        }
        catch (RuntimeException ignored) {
            return false;
        }
    }

    /** Hud 3 neon frame: soft animated theme glow plus a thin gradient outline around the shape. */
    public void renderNeonFrame(ShapeProperties shape) {
        if (Render2DUtil.getRenderAlpha() <= 0.0f) {
            return;
        }
        try {
            Bounds bounds = this.bounds(shape);
            if (bounds.width <= 0.0f || bounds.height <= 0.0f) {
                return;
            }
            this.drawThemeShadow(bounds, shape);
            this.drawGradientLayer(bounds, shape, 0.6f, 0.85f);
        }
        catch (RuntimeException ignored) {
            // decoration only
        }
    }

    private boolean shouldDecorate(ShapeProperties shape, Bounds bounds) {
        return false;
    }

    private boolean isSplitHudPanel(ShapeProperties shape) {
        boolean topHalf = shape.getRound().x > 0.0f && shape.getRound().z > 0.0f && shape.getRound().y <= 0.0f && shape.getRound().w <= 0.0f;
        boolean bottomHalf = shape.getRound().y > 0.0f && shape.getRound().w > 0.0f && shape.getRound().x <= 0.0f && shape.getRound().z <= 0.0f;
        return topHalf || bottomHalf;
    }

    private void drawThemeShadow(Bounds bounds, ShapeProperties shape) {
        this.drawGradientLayer(bounds, shape, 7.0f, 0.025f);
        this.drawGradientLayer(bounds, shape, 5.4f, 0.035f);
        this.drawGradientLayer(bounds, shape, 4.0f, 0.045f);
        this.drawGradientLayer(bounds, shape, 2.8f, 0.06f);
        this.drawGradientLayer(bounds, shape, 1.8f, 0.08f);
    }

    private void drawThemeOutline(Bounds bounds, ShapeProperties shape) {
        float thickness = Math.max(0.35f, Math.min(shape.getThickness(), 0.55f));
        this.drawGradientLayer(bounds, shape, thickness, 0.58f);
    }

    private void drawLayer(Bounds bounds, ShapeProperties shape, float expand, int color) {
        this.directSingleColor[0] = Render2DUtil.applyRenderAlpha(color);
        DIRECT_PIPELINE.drawRect(bounds.x - expand, bounds.y - expand, bounds.width + expand * 2.0f, bounds.height + expand * 2.0f, this.directSingleColor, this.remapRadii(shape, expand), shape.getSoftness());
    }

    private void drawGradientLayer(Bounds bounds, ShapeProperties shape, float expand, float alpha) {
        float ringThickness = Math.max(0.65f, Math.min(expand <= 1.0f ? expand : 1.15f, 1.35f));
        this.directColors[0] = Render2DUtil.applyRenderAlpha(ColorUtil.multAlpha(ColorUtil.fade(0), alpha));
        this.directColors[1] = Render2DUtil.applyRenderAlpha(ColorUtil.multAlpha(ColorUtil.fade(90), alpha));
        this.directColors[2] = Render2DUtil.applyRenderAlpha(ColorUtil.multAlpha(ColorUtil.fade(270), alpha));
        this.directColors[3] = Render2DUtil.applyRenderAlpha(ColorUtil.multAlpha(ColorUtil.fade(180), alpha));
        DIRECT_PIPELINE.drawRect(bounds.x - expand, bounds.y - expand, bounds.width + expand * 2.0f, bounds.height + expand * 2.0f, this.directColors, this.remapRadii(shape, expand), -ringThickness);
    }

    private float[] remapRadii(ShapeProperties shape) {
        return this.remapRadii(shape, 0.0f);
    }

    private float[] remapRadii(ShapeProperties shape, float add) {
        this.directRadii[0] = shape.getRound().x + add;
        this.directRadii[1] = shape.getRound().z + add;
        this.directRadii[2] = shape.getRound().w + add;
        this.directRadii[3] = shape.getRound().y + add;
        return this.directRadii;
    }

    private Bounds bounds(ShapeProperties shape) {
        Matrix4f matrix = shape.getMatrix().method_23760().method_23761();
        matrix.transformPosition(shape.getX(), shape.getY(), 0.0f, this.p1);
        matrix.transformPosition(shape.getX() + shape.getWidth(), shape.getY(), 0.0f, this.p2);
        matrix.transformPosition(shape.getX() + shape.getWidth(), shape.getY() + shape.getHeight(), 0.0f, this.p3);
        matrix.transformPosition(shape.getX(), shape.getY() + shape.getHeight(), 0.0f, this.p4);
        float minX = Math.min(Math.min(this.p1.x, this.p2.x), Math.min(this.p3.x, this.p4.x));
        float maxX = Math.max(Math.max(this.p1.x, this.p2.x), Math.max(this.p3.x, this.p4.x));
        float minY = Math.min(Math.min(this.p1.y, this.p2.y), Math.min(this.p3.y, this.p4.y));
        float maxY = Math.max(Math.max(this.p1.y, this.p2.y), Math.max(this.p3.y, this.p4.y));
        this.directBounds.set(minX, minY, maxX - minX, maxY - minY);
        return this.directBounds;
    }

    private static final class Bounds {
        private float x;
        private float y;
        private float width;
        private float height;

        private Bounds() {
        }

        private void set(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}

