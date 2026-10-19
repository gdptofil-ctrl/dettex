/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_8030
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 */
package ru.recoin.common.util.render;

import java.util.Stack;
import net.minecraft.class_8030;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.other.Pool;
import ru.recoin.common.util.render.LegacyRenderSystem;

public class ScissorManager
implements QuickImports {
    private final Pool<Scissor> scissorPool = new Pool<Scissor>(Scissor::new);
    private final Stack<Scissor> scissorStack = new Stack();
    private final Vector3f pos = new Vector3f();
    private final Vector3f size = new Vector3f();

    public void push(Matrix4f matrix4f, float x, float y, float width, float height) {
        matrix4f.transformPosition(x, y, 0.0f, this.pos);
        matrix4f.getScale(this.size).mul(width, height, 0.0f);
        Scissor currentScissor = this.scissorPool.get();
        currentScissor.set(this.pos.x, this.pos.y, this.size.x, this.size.y);
        if (!this.scissorStack.isEmpty()) {
            currentScissor.intersect(this.scissorStack.peek());
        }
        this.scissorStack.push(currentScissor);
        this.setScissor(currentScissor);
    }

    public void pop() {
        if (!this.scissorStack.isEmpty()) {
            this.scissorPool.free(this.scissorStack.pop());
            if (this.scissorStack.isEmpty()) {
                LegacyRenderSystem.disableScissor();
            } else {
                this.setScissor(this.scissorStack.peek());
            }
        }
    }

    public class_8030 currentScreenRect() {
        if (this.scissorStack.isEmpty()) {
            return null;
        }
        Scissor scissor = this.scissorStack.peek();
        return new class_8030(scissor.x, scissor.y, scissor.width, scissor.height);
    }

    public boolean copyCurrentBounds(int[] bounds) {
        if (this.scissorStack.isEmpty()) {
            return false;
        }
        Scissor scissor = this.scissorStack.peek();
        bounds[0] = scissor.x;
        bounds[1] = scissor.y;
        bounds[2] = scissor.width;
        bounds[3] = scissor.height;
        return true;
    }

    private void setScissor(Scissor scissor) {
        int scaleFactor = (int)window.getScaleFactor();
        int x = scissor.x * scaleFactor;
        int y = window.getHeight() - (scissor.y * scaleFactor + scissor.height * scaleFactor);
        int width = scissor.width * scaleFactor;
        int height = scissor.height * scaleFactor;
        LegacyRenderSystem.enableScissor(x, y, width, height);
    }

    private static class Scissor {
        public int x;
        public int y;
        public int width;
        public int height;

        private Scissor() {
        }

        public void set(double x, double y, double width, double height) {
            double right = x + width;
            double bottom = y + height;
            int leftPixel = Math.max(0, (int)Math.floor(Math.min(x, right)));
            int topPixel = Math.max(0, (int)Math.floor(Math.min(y, bottom)));
            int rightPixel = Math.max(0, (int)Math.ceil(Math.max(x, right)));
            int bottomPixel = Math.max(0, (int)Math.ceil(Math.max(y, bottom)));
            this.x = leftPixel;
            this.y = topPixel;
            this.width = Math.max(0, rightPixel - leftPixel);
            this.height = Math.max(0, bottomPixel - topPixel);
        }

        Scissor copy() {
            Scissor newScissor = new Scissor();
            newScissor.set(this.x, this.y, this.width, this.height);
            return newScissor;
        }

        void intersect(Scissor other) {
            int newX = Math.max(this.x, other.x);
            int newY = Math.max(this.y, other.y);
            int newRight = Math.min(this.x + this.width, other.x + other.width);
            int newBottom = Math.min(this.y + this.height, other.y + other.height);
            this.x = newX;
            this.y = newY;
            this.width = Math.max(0, newRight - newX);
            this.height = Math.max(0, newBottom - newY);
        }
    }
}

