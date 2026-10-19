/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_3532
 */
package ru.recoin.implement.screens.menu.components.implement.window;

import net.minecraft.class_332;
import net.minecraft.class_3532;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.GuiRenderFlusher;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.implement.screens.menu.components.AbstractComponent;

public abstract class AbstractWindow
extends AbstractComponent {
    private static final long OPEN_ANIMATION_MS = 240L;
    private static final long CLOSE_ANIMATION_MS = 165L;
    private static final float BOUNCE_OVERSHOOT = 0.65f;
    private boolean dragging;
    private boolean draggable;
    private int dragX;
    private int dragY;
    private long openAnimStart = System.currentTimeMillis();
    private long closeAnimStart;
    private boolean closing;

    public AbstractWindow draggable(boolean draggable) {
        this.draggable = draggable;
        return this;
    }

    @Override
    public AbstractWindow size(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }

    @Override
    public AbstractWindow position(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public float getScale() {
        long elapsed = this.closing ? System.currentTimeMillis() - this.closeAnimStart : System.currentTimeMillis() - this.openAnimStart;
        float duration = this.closing ? 165.0f : 240.0f;
        float t = class_3532.method_15363((float)((float)elapsed / duration), (float)0.0f, (float)1.0f);
        return this.closing ? 1.0f - AbstractWindow.easeInBack(t) : AbstractWindow.easeOutBack(t);
    }

    private static float easeOutBack(float t) {
        float shifted = t - 1.0f;
        float coefficient = 1.65f;
        return 1.0f + coefficient * shifted * shifted * shifted + 0.65f * shifted * shifted;
    }

    private static float easeInBack(float t) {
        float coefficient = 1.65f;
        return coefficient * t * t * t - 0.65f * t * t;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovered(mouseX, mouseY) && button == 0 && this.draggable) {
            this.dragging = true;
            this.dragX = (int)((double)this.x - mouseX);
            this.dragY = (int)((double)this.y - mouseY);
            return true;
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        if (this.dragging && this.draggable) {
            this.x = mouseX + this.dragX;
            this.y = mouseY + this.dragY;
        }
        float scale = Math.max(0.001f, this.getScale());
        float centerX = this.x + this.width / 2.0f;
        float centerY = this.y + this.height / 2.0f;
        int transformedMouseX = Math.round(centerX + ((float)mouseX - centerX) / scale);
        int transformedMouseY = Math.round(centerY + ((float)mouseY - centerY) / scale);
        float previousAlpha = Render2DUtil.getRenderAlpha();
        Render2DUtil.setRenderAlpha(previousAlpha * class_3532.method_15363((float)scale, (float)0.0f, (float)1.0f));
        try {
            MathUtil.scale(context.method_51448(), centerX, centerY, scale, () -> {
                this.drawWindow(context, transformedMouseX, transformedMouseY, delta);
                Render2DUtil.onRender(context);
                context.method_73199();
                FontRenderer.flushQueuedText(context);
                GuiRenderFlusher.flush();
            });
        }
        finally {
            Render2DUtil.setRenderAlpha(previousAlpha);
        }
    }

    protected abstract void drawWindow(class_332 var1, int var2, int var3, float var4);

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.dragging = false;
        return true;
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        return this.isHovered(mouseX, mouseY);
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= (double)this.x && mouseX <= (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY <= (double)(this.y + this.height);
    }

    public void startCloseAnimation() {
        if (!this.closing) {
            this.closing = true;
            this.closeAnimStart = System.currentTimeMillis();
        }
    }

    public boolean isCloseAnimationFinished() {
        return this.closing && System.currentTimeMillis() - this.closeAnimStart >= 165L;
    }
}

