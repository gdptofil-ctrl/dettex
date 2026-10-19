/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_4587
 *  net.minecraft.class_7833
 *  org.joml.Quaternionfc
 */
package ru.recoin.implement.features.modules.render.cosmetics.render;

import net.minecraft.class_4587;
import net.minecraft.class_7833;
import org.joml.Quaternionfc;

public class RenderStack {
    private class_4587 stack;

    public void update(class_4587 stack) {
        this.stack = stack;
    }

    public void push() {
        this.stack.method_22903();
    }

    public void pop() {
        this.stack.method_22909();
    }

    public class_4587 get() {
        return this.stack;
    }

    public void translate(float x, float y, float z) {
        this.stack.method_46416(x, y, z);
    }

    public void scale(float sx, float sy, float sz) {
        this.stack.method_22905(sx, sy, sz);
    }

    public void rotateX(float deg) {
        this.stack.method_22907((Quaternionfc)class_7833.field_40714.rotationDegrees(deg));
    }

    public void rotateY(float deg) {
        this.stack.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees(deg));
    }

    public void rotateZ(float deg) {
        this.stack.method_22907((Quaternionfc)class_7833.field_40718.rotationDegrees(deg));
    }

    public void rotate(float rx, float ry, float rz) {
        if (rz != 0.0f) {
            this.rotateZ(rz);
        }
        if (ry != 0.0f) {
            this.rotateY(ry);
        }
        if (rx != 0.0f) {
            this.rotateX(rx);
        }
    }

    public void rotateDegrees(float rx, float ry, float rz) {
        this.rotate(rx, ry, rz);
    }

    public void rotateXDegrees(float deg) {
        this.rotateX(deg);
    }

    public void rotateYDegrees(float deg) {
        this.rotateY(deg);
    }

    public void rotateZDegrees(float deg) {
        this.rotateZ(deg);
    }
}

