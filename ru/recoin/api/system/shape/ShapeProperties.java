/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_4587
 *  org.joml.Vector4f
 *  org.joml.Vector4i
 */
package ru.recoin.api.system.shape;

import net.minecraft.class_4587;
import org.joml.Vector4f;
import org.joml.Vector4i;

public class ShapeProperties {
    private class_4587 matrix;
    private float x;
    private float y;
    private float width;
    private float height;
    private float softness;
    private float thickness;
    private float start;
    private float end;
    private float quality;
    private Vector4f round;
    private int outlineColor;
    private Vector4i color;
    private static final Vector4f ZERO_ROUND = new Vector4f(0.0f);
    private static final Vector4i DEFAULT_COLOR = new Vector4i(-1);

    private ShapeProperties(class_4587 matrix, float x, float y, float width, float height, float softness, float thickness, float start, float end, float quality, Vector4f round, int outlineColor, Vector4i color) {
        this.matrix = matrix;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.softness = softness;
        this.thickness = thickness;
        this.round = round != null ? round : ZERO_ROUND;
        this.outlineColor = outlineColor;
        this.color = color != null ? color : DEFAULT_COLOR;
        this.start = start;
        this.end = end;
        this.quality = quality;
    }

    public static ShapePropertiesBuilder create(class_4587 matrix, double x, double y, double width, double height) {
        return ShapeProperties.builder().matrix(matrix).x((float)x).y((float)y).width((float)width).height((float)height);
    }

    private static float $default$quality() {
        return 20.0f;
    }

    private static int $default$outlineColor() {
        return -1;
    }

    public static ShapePropertiesBuilder builder() {
        return new ShapePropertiesBuilder();
    }

    public ShapePropertiesBuilder toBuilder() {
        return new ShapePropertiesBuilder().matrix(this.matrix).x(this.x).y(this.y).width(this.width).height(this.height).softness(this.softness).thickness(this.thickness).start(this.start).end(this.end).quality(this.quality).round(this.round).outlineColor(this.outlineColor).color(this.color);
    }

    public class_4587 getMatrix() {
        return this.matrix;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getWidth() {
        return this.width;
    }

    public float getHeight() {
        return this.height;
    }

    public float getSoftness() {
        return this.softness;
    }

    public float getThickness() {
        return this.thickness;
    }

    public float getStart() {
        return this.start;
    }

    public float getEnd() {
        return this.end;
    }

    public float getQuality() {
        return this.quality;
    }

    public Vector4f getRound() {
        return this.round;
    }

    public int getOutlineColor() {
        return this.outlineColor;
    }

    public Vector4i getColor() {
        return this.color;
    }

    public void setMatrix(class_4587 matrix) {
        this.matrix = matrix;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setSoftness(float softness) {
        this.softness = softness;
    }

    public void setThickness(float thickness) {
        this.thickness = thickness;
    }

    public void setStart(float start) {
        this.start = start;
    }

    public void setEnd(float end) {
        this.end = end;
    }

    public void setQuality(float quality) {
        this.quality = quality;
    }

    public void setRound(Vector4f round) {
        this.round = round;
    }

    public void setOutlineColor(int outlineColor) {
        this.outlineColor = outlineColor;
    }

    public void setColor(Vector4i color) {
        this.color = color;
    }

    public static class ShapePropertiesBuilder {
        private class_4587 matrix;
        private float x;
        private float y;
        private float width;
        private float height;
        private float softness;
        private float thickness;
        private float start;
        private float end;
        private boolean quality$set;
        private float quality$value;
        private Vector4f round;
        private boolean outlineColor$set;
        private int outlineColor$value;
        private Vector4i color;
        private float quality;
        private int outlineColor;

        public ShapePropertiesBuilder color(int color) {
            this.color = color == -1 ? DEFAULT_COLOR : new Vector4i(color);
            return this;
        }

        public ShapePropertiesBuilder color(Vector4i color) {
            this.color = color;
            return this;
        }

        public ShapePropertiesBuilder color(int ... color) {
            this.color = new Vector4i(color);
            return this;
        }

        public ShapePropertiesBuilder round(float round) {
            this.round = round == 0.0f ? ZERO_ROUND : new Vector4f(round);
            return this;
        }

        public ShapePropertiesBuilder round(Vector4f round) {
            this.round = round;
            return this;
        }

        public ShapePropertiesBuilder round(float ... round) {
            this.round = new Vector4f(round);
            return this;
        }

        ShapePropertiesBuilder() {
        }

        public ShapePropertiesBuilder matrix(class_4587 matrix) {
            this.matrix = matrix;
            return this;
        }

        public ShapePropertiesBuilder x(float x) {
            this.x = x;
            return this;
        }

        public ShapePropertiesBuilder y(float y) {
            this.y = y;
            return this;
        }

        public ShapePropertiesBuilder width(float width) {
            this.width = width;
            return this;
        }

        public ShapePropertiesBuilder height(float height) {
            this.height = height;
            return this;
        }

        public ShapePropertiesBuilder softness(float softness) {
            this.softness = softness;
            return this;
        }

        public ShapePropertiesBuilder thickness(float thickness) {
            this.thickness = thickness;
            return this;
        }

        public ShapePropertiesBuilder start(float start) {
            this.start = start;
            return this;
        }

        public ShapePropertiesBuilder end(float end) {
            this.end = end;
            return this;
        }

        public ShapePropertiesBuilder quality(float quality) {
            this.quality$value = quality;
            this.quality$set = true;
            return this;
        }

        public ShapePropertiesBuilder outlineColor(int outlineColor) {
            this.outlineColor$value = outlineColor;
            this.outlineColor$set = true;
            return this;
        }

        public ShapeProperties build() {
            float quality$value = this.quality$value;
            if (!this.quality$set) {
                quality$value = ShapeProperties.$default$quality();
            }
            int outlineColor$value = this.outlineColor$value;
            if (!this.outlineColor$set) {
                outlineColor$value = ShapeProperties.$default$outlineColor();
            }
            return new ShapeProperties(this.matrix, this.x, this.y, this.width, this.height, this.softness, this.thickness, this.start, this.end, quality$value, this.round, outlineColor$value, this.color);
        }

        public String toString() {
            return "ShapeProperties.ShapePropertiesBuilder(matrix=" + String.valueOf(this.matrix) + ", x=" + this.x + ", y=" + this.y + ", width=" + this.width + ", height=" + this.height + ", softness=" + this.softness + ", thickness=" + this.thickness + ", start=" + this.start + ", end=" + this.end + ", quality$value=" + this.quality$value + ", round=" + String.valueOf(this.round) + ", outlineColor$value=" + this.outlineColor$value + ", color=" + String.valueOf(this.color) + ")";
        }
    }
}

