/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_276
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  net.minecraft.class_6367
 *  org.joml.Matrix4f
 *  org.joml.Vector2f
 *  org.joml.Vector3f
 */
package ru.recoin.api.system.shape.implement;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.class_276;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_6367;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import ru.recoin.api.system.shape.Shape;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.common.util.render.ShapeShaderRenderer;
import ru.recoin.common.util.render.pipeline.DirectBlurPipeline;
import ru.recoin.common.util.render.pipeline.DropShadowPipeline;
import ru.recoin.common.util.render.pipeline.StyledBlurPipeline;
import ru.recoin.compat.render.BufferRenderer;
import ru.recoin.compat.render.GlStateManager;
import ru.recoin.implement.features.modules.render.Hud;
import ru.recoin.implement.screens.mainmenu.RecoinMainMenuScreen;
import ru.recoin.implement.screens.menu.MenuScreen;

public class Blur
implements Shape,
QuickImports {
    private static final DirectBlurPipeline DIRECT_PIPELINE = new DirectBlurPipeline();
    private static final StyledBlurPipeline STYLED_PIPELINE = new StyledBlurPipeline();
    private static final DropShadowPipeline SHADOW_PIPELINE = new DropShadowPipeline();
    private static final long FRAMEBUFFER_COPY_COOLDOWN_NS = 1000000L;
    public class_276 input;
    public Vector2f resolution = new Vector2f();
    private final float[] directRadii = new float[4];
    private final Vector3f p1 = new Vector3f();
    private final Vector3f p2 = new Vector3f();
    private final Vector3f p3 = new Vector3f();
    private final Vector3f p4 = new Vector3f();
    private final Bounds directBounds = new Bounds();
    private long lastFramebufferCopyNanos;
    private boolean colorlessMode;
    private boolean shadowEnabled = true;
    private float styledRadius = 63.0f;

    @Override
    public void render(ShapeProperties shape) {
        if (Render2DUtil.getRenderAlpha() <= 0.0f) {
            return;
        }
        if (this.isHudContext() && !this.isGuiContext() && Hud.getInstance().shouldUseSimpleHudPanels()) {
            this.renderSolid(shape);
            return;
        }
        this.renderShadow(shape);
        if (this.renderDirect(shape)) {
            this.renderOutline(shape);
            return;
        }
        this.setup();
        if (this.input != null) {
            this.ensureInputSnapshot();
            if (ShapeShaderRenderer.renderBlur(shape, this.input.method_71639())) {
                return;
            }
        }
        if (ShapeShaderRenderer.renderRound(shape)) {
            return;
        }
        if (this.renderSolid(shape)) {
            return;
        }
        Matrix4f matrix4f = shape.getMatrix().method_23760().method_23761();
        class_287 buffer = ((class_289)tessellator.get()).method_60827(VertexFormat.class_5596.field_27382, class_290.field_1576);
        drawEngine.quad(matrix4f, buffer, shape.getX(), shape.getY(), shape.getWidth(), shape.getHeight(), shape.getColor().x);
        GlStateManager._activeTexture(33984);
        BufferRenderer.drawWithGlobalProgram(buffer.method_60800());
    }

    private boolean renderSolid(ShapeProperties shape) {
        try {
            rectangle.render(shape.toBuilder().color(this.solidFallbackColor(shape)).build());
            return true;
        }
        catch (RuntimeException ignored) {
            return false;
        }
    }

    private int solidFallbackColor(ShapeProperties shape) {
        if (this.isGuiContext() && !this.isHudContext() && ColorUtil.alpha(shape.getColor().x) > 0) {
            return shape.getColor().x;
        }
        if (this.isHudContext() && !this.isGuiContext()) {
            return ColorUtil.multAlpha(1543701770, Hud.getInstance().hudOpacitySetting.getValue());
        }
        return 1543701770;
    }

    private void renderShadow(ShapeProperties shape) {
        boolean hudOrGui;
        if (!this.shadowEnabled) {
            return;
        }
        boolean hud = Render2DUtil.isHudDecorationActive();
        boolean gui = Render2DUtil.isGuiDecorationActive();
        boolean bl = hudOrGui = hud || gui;
        if (!hudOrGui || this.isSplitHudPanel(shape)) {
            return;
        }
        if (Hud.getInstance().shouldDisableShadows(hud, gui)) {
            return;
        }
        if (shape.getWidth() <= 10.0f || shape.getHeight() <= 8.0f) {
            return;
        }
        Bounds bounds = this.bounds(shape);
        SHADOW_PIPELINE.draw(bounds.x, bounds.y, bounds.width, bounds.height, this.remapRadii(shape), 8.6f, 0.5f, Render2DUtil.applyRenderAlpha(ColorUtil.getColor(0, 0, 0, hud && !gui ? 0.32f : 0.25f)));
    }

    private int themeShadowColor() {
        int theme = ColorUtil.getClientColor();
        int red = Math.round((float)ColorUtil.red(theme) * 0.42f);
        int green = Math.round((float)ColorUtil.green(theme) * 0.42f);
        int blue = Math.round((float)ColorUtil.blue(theme) * 0.42f);
        return Render2DUtil.applyRenderAlpha(ColorUtil.getColor(red, green, blue, 0.42f));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void renderOutline(ShapeProperties shape) {
        boolean hud = Render2DUtil.isHudDecorationActive();
        boolean gui = Render2DUtil.isGuiDecorationActive();
        if (!hud || this.isSplitHudPanel(shape)) {
            return;
        }
        if (Hud.getInstance().shouldDisableOutline(hud, gui)) {
            return;
        }
        if (shape.getWidth() <= 10.0f || shape.getHeight() <= 8.0f) {
            return;
        }
        boolean queued = Render2DUtil.isQueuedShapeActive();
        Render2DUtil.setQueuedShapeActive(false);
        try {
            rectangle.render(shape.toBuilder().softness(-0.35f).thickness(0.0f).outlineColor(0).color(1440211698).build());
        }
        finally {
            Render2DUtil.setQueuedShapeActive(queued);
        }
    }

    private boolean isSplitHudPanel(ShapeProperties shape) {
        boolean topHalf = shape.getRound().x > 0.0f && shape.getRound().z > 0.0f && shape.getRound().y <= 0.0f && shape.getRound().w <= 0.0f;
        boolean bottomHalf = shape.getRound().y > 0.0f && shape.getRound().w > 0.0f && shape.getRound().x <= 0.0f && shape.getRound().z <= 0.0f;
        return topHalf || bottomHalf;
    }

    private boolean renderDirect(ShapeProperties shape) {
        try {
            Bounds bounds = this.bounds(shape);
            if (bounds.width <= 0.0f || bounds.height <= 0.0f) {
                return true;
            }
            float blurRadius = Math.max(45.0f, shape.getSoftness() * 20.0f);
            if (this.shouldDisableBlur()) {
                this.renderSolid(shape);
                return true;
            }
            int color = Render2DUtil.applyRenderAlpha(this.getBlurTint(shape.getColor().x));
            if (ColorUtil.alpha(color) <= 0) {
                return true;
            }
            if (this.useStyledBlur()) {
                STYLED_PIPELINE.drawBlur(bounds.x, bounds.y, bounds.width, bounds.height, this.styledBlurRadius(), this.remapRadii(shape), color);
            } else {
                DIRECT_PIPELINE.drawBlur(bounds.x, bounds.y, bounds.width, bounds.height, blurRadius, this.remapRadii(shape), color);
            }
            return true;
        }
        catch (RuntimeException ignored) {
            return false;
        }
    }

    public void beginFrame() {
        DIRECT_PIPELINE.beginFrame();
        STYLED_PIPELINE.beginFrame();
    }

    public void setLiveMode(boolean liveMode) {
        DIRECT_PIPELINE.setLiveMode(liveMode);
        STYLED_PIPELINE.setLiveMode(liveMode);
    }

    public void setColorlessMode(boolean colorlessMode) {
        this.colorlessMode = colorlessMode;
    }

    public void setShadowEnabled(boolean shadowEnabled) {
        this.shadowEnabled = shadowEnabled;
    }

    public void setStyledBlurRadius(float radius) {
        this.styledRadius = Math.max(1.0f, radius);
    }

    public void resetStyledBlurRadius() {
        this.styledRadius = 63.0f;
    }

    private boolean useStyledBlur() {
        return this.isHudContext() || this.isGuiContext();
    }

    private boolean shouldDisableBlur() {
        return Hud.getInstance().shouldDisableBlur(this.isHudContext(), this.isGuiContext());
    }

    private boolean isHudContext() {
        return Render2DUtil.isHudDecorationActive();
    }

    private boolean isGuiContext() {
        return Render2DUtil.isGuiDecorationActive() || !this.isHudContext() && Blur.mc.field_1755 instanceof MenuScreen;
    }

    private float styledBlurRadius() {
        if (this.isHudContext() && !this.isGuiContext()) {
            return Math.max(1.0f, this.styledRadius * Hud.getInstance().hudBlurStrengthSetting.getValue());
        }
        return this.styledRadius;
    }

    private int getBlurTint(int color) {
        if (RecoinMainMenuScreen.isRecoinMenu(Blur.mc.field_1755)) {
            return this.colorlessMode ? ColorUtil.getColor(250, 255, 255, 0.58f) : ColorUtil.getColor(255, 255, 255, 0.58f);
        }
        Hud hud = Hud.getInstance();
        if (this.isHudContext() && !this.isGuiContext()) {
            return ColorUtil.multAlpha(hud.hudBlurColorSetting.getColor(), 0.58f * hud.hudOpacitySetting.getValue());
        }
        if (this.isGuiContext()) {
            return ColorUtil.multAlpha(hud.guiBackgroundColorSetting.getColor(), 0.58f);
        }
        if (this.colorlessMode) {
            return ColorUtil.getColor(250, 255, 255, 0.58f);
        }
        return ColorUtil.getColor(255, 255, 255, 0.58f);
    }

    private float[] remapRadii(ShapeProperties shape) {
        this.directRadii[0] = shape.getRound().x;
        this.directRadii[1] = shape.getRound().z;
        this.directRadii[2] = shape.getRound().w;
        this.directRadii[3] = shape.getRound().y;
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

    public void setup() {
        class_276 buffer = mc.method_1522();
        if (this.input == null) {
            this.input = new class_6367("recoin_blur", mc.method_22683().method_4489(), mc.method_22683().method_4506(), false);
        }
        if (this.input != null && (this.input.field_1482 != mc.method_22683().method_4489() || this.input.field_1481 != mc.method_22683().method_4506())) {
            this.input.method_1234(mc.method_22683().method_4489(), mc.method_22683().method_4506());
        }
        this.resolution.set((float)buffer.field_1482, (float)buffer.field_1481);
    }

    private void ensureInputSnapshot() {
        class_276 buffer = mc.method_1522();
        if (this.input == null || buffer == null || buffer.method_71639() == null) {
            return;
        }
        long now = System.nanoTime();
        if (now - this.lastFramebufferCopyNanos < 1000000L) {
            return;
        }
        buffer.method_68445(this.input.method_71639());
        this.lastFramebufferCopyNanos = now;
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

