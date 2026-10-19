/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1799
 *  net.minecraft.class_332
 *  org.joml.Matrix3x2fStack
 */
package ru.recoin.common.util.render;

import java.util.Objects;
import net.minecraft.class_1799;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;
import ru.recoin.common.util.render.Render2DUtil;

public class ItemStackFade {
    private static final long FADE_MS = 160L;
    private class_1799 visibleStack = class_1799.field_8037;
    private class_1799 fadingStack = class_1799.field_8037;
    private long fadeStart;
    private long appearStart;

    public boolean isActive() {
        return !this.visibleStack.method_7960() || !this.fadingStack.method_7960() && System.currentTimeMillis() - this.fadeStart < 160L;
    }

    public void reset() {
        this.visibleStack = class_1799.field_8037;
        this.fadingStack = class_1799.field_8037;
        this.fadeStart = 0L;
        this.appearStart = 0L;
    }

    public void draw(class_332 context, class_1799 currentStack, float x, float y, boolean rect, boolean drawItemInSlot, float scale) {
        class_1799 stack = currentStack == null ? class_1799.field_8037 : currentStack;
        long now = System.currentTimeMillis();
        if (!this.isSameVisualStack(stack, this.visibleStack)) {
            if (!this.visibleStack.method_7960()) {
                this.fadingStack = this.visibleStack.method_7972();
                this.fadeStart = now;
            }
            class_1799 class_17992 = this.visibleStack = stack.method_7960() ? class_1799.field_8037 : stack.method_7972();
            if (!this.visibleStack.method_7960()) {
                this.appearStart = now;
            }
        } else if (!stack.method_7960()) {
            this.visibleStack.method_7939(stack.method_7947());
        }
        this.drawFadingStack(context, x, y, rect, drawItemInSlot, scale, now);
        if (!stack.method_7960()) {
            float appearAlpha = Math.min(1.0f, Math.max(0.01f, (float)(now - this.appearStart) / 160.0f));
            Render2DUtil.defaultDrawStack(context, stack, x, y, rect, drawItemInSlot, scale, appearAlpha);
        }
    }

    public void drawScaled(class_332 context, class_1799 currentStack, float x, float y, boolean rect, boolean drawItemInSlot, float scale) {
        class_1799 stack = currentStack == null ? class_1799.field_8037 : currentStack;
        long now = System.currentTimeMillis();
        this.updateStacks(stack, now);
        if (!this.fadingStack.method_7960()) {
            float progress = Math.min(1.0f, (float)(now - this.fadeStart) / 160.0f);
            float fadeScale = 1.0f - progress;
            if (fadeScale <= 0.01f) {
                this.fadingStack = class_1799.field_8037;
            } else {
                this.drawWithScale(context, this.fadingStack, x, y, rect, drawItemInSlot, scale, fadeScale);
            }
        }
        if (!this.visibleStack.method_7960()) {
            float appearScale = Math.min(1.0f, (float)(now - this.appearStart) / 160.0f);
            this.drawWithScale(context, this.visibleStack, x, y, rect, drawItemInSlot, scale, Math.max(0.01f, appearScale));
        }
    }

    private void updateStacks(class_1799 stack, long now) {
        if (!this.isSameVisualStack(stack, this.visibleStack)) {
            if (!this.visibleStack.method_7960()) {
                this.fadingStack = this.visibleStack.method_7972();
                this.fadeStart = now;
            }
            class_1799 class_17992 = this.visibleStack = stack.method_7960() ? class_1799.field_8037 : stack.method_7972();
            if (!this.visibleStack.method_7960()) {
                this.appearStart = now;
            }
        } else if (!stack.method_7960()) {
            this.visibleStack.method_7939(stack.method_7947());
        }
    }

    private void drawWithScale(class_332 context, class_1799 stack, float x, float y, boolean rect, boolean drawItemInSlot, float itemScale, float animationScale) {
        float renderedSize = 16.0f * itemScale;
        float centerX = x + 1.0f + renderedSize / 2.0f;
        float centerY = y + 1.0f + renderedSize / 2.0f;
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate(centerX, centerY);
        matrices.scale(animationScale, animationScale);
        matrices.translate(-centerX, -centerY);
        Render2DUtil.defaultDrawStack(context, stack, x, y, rect, drawItemInSlot, itemScale);
        matrices.popMatrix();
    }

    private void drawFadingStack(class_332 context, float x, float y, boolean rect, boolean drawItemInSlot, float scale, long now) {
        if (this.fadingStack.method_7960()) {
            return;
        }
        float alpha = 1.0f - Math.min(1.0f, (float)(now - this.fadeStart) / 160.0f);
        if (alpha <= 0.0f) {
            this.fadingStack = class_1799.field_8037;
            return;
        }
        Render2DUtil.defaultDrawStack(context, this.fadingStack, x, y, rect, drawItemInSlot, scale, alpha);
    }

    private boolean isSameVisualStack(class_1799 first, class_1799 second) {
        if (first.method_7960() || second.method_7960()) {
            return first.method_7960() && second.method_7960();
        }
        return first.method_7909() == second.method_7909() && Objects.equals(first.method_57353(), second.method_57353());
    }
}

