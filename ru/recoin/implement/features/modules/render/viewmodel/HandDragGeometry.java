/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.class_1268
 *  net.minecraft.class_238
 *  net.minecraft.class_408
 *  net.minecraft.class_4587
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector4f
 */
package ru.recoin.implement.features.modules.render.viewmodel;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.class_1268;
import net.minecraft.class_238;
import net.minecraft.class_408;
import net.minecraft.class_4587;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import ru.recoin.common.QuickImports;

public final class HandDragGeometry
implements QuickImports {
    private final Matrix4f projection = new Matrix4f();
    private final Matrix4f[] translationBases = new Matrix4f[]{new Matrix4f(), new Matrix4f()};
    private final Region[] regions = new Region[2];
    private boolean valid;

    public void reset() {
        this.regions[1] = null;
        this.regions[0] = null;
        this.valid = false;
    }

    public void beginFrame(Matrix4f projection) {
        this.projection.set((Matrix4fc)projection);
        this.regions[1] = null;
        this.regions[0] = null;
        this.valid = true;
    }

    public void captureTranslation(class_1268 hand, class_4587 matrices) {
        this.translationBases[hand.ordinal()].set((Matrix4fc)RenderSystem.getModelViewMatrix()).mul((Matrix4fc)matrices.method_23760().method_23761());
    }

    public void capture(class_1268 hand, class_4587 matrices, class_238 bounds) {
        this.capture(hand, matrices.method_23760().method_23761(), bounds);
    }

    public void capture(class_1268 hand, Matrix4f position, class_238 bounds) {
        if (!this.valid || HandDragGeometry.mc.field_1724 == null || !(HandDragGeometry.mc.field_1755 instanceof class_408)) {
            return;
        }
        Matrix4f clipMatrix = new Matrix4f((Matrix4fc)this.projection).mul((Matrix4fc)RenderSystem.getModelViewMatrix()).mul((Matrix4fc)position);
        double width = mc.method_22683().method_4486();
        double height = mc.method_22683().method_4502();
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (int corner = 0; corner < 8; ++corner) {
            Vector4f clip = clipMatrix.transform(new Vector4f((float)((corner & 1) == 0 ? bounds.field_1323 : bounds.field_1320), (float)((corner & 2) == 0 ? bounds.field_1322 : bounds.field_1325), (float)((corner & 4) == 0 ? bounds.field_1321 : bounds.field_1324), 1.0f));
            if (clip.w <= 0.001f) continue;
            double screenX = ((double)(clip.x / clip.w) + 1.0) * width * 0.5;
            double screenY = (1.0 - (double)(clip.y / clip.w)) * height * 0.5;
            minX = Math.min(minX, screenX);
            maxX = Math.max(maxX, screenX);
            minY = Math.min(minY, screenY);
            maxY = Math.max(maxY, screenY);
        }
        if (!Double.isFinite(minX)) {
            return;
        }
        Vector4f center = clipMatrix.transform(new Vector4f((float)bounds.method_1005().field_1352, (float)bounds.method_1005().field_1351, (float)bounds.method_1005().field_1350, 1.0f));
        if (center.w <= 0.001f) {
            return;
        }
        Matrix4f translation = new Matrix4f((Matrix4fc)this.projection).mul((Matrix4fc)this.translationBases[hand.ordinal()]);
        Vector4f horizontal = translation.transform(new Vector4f(1.0f, 0.0f, 0.0f, 0.0f));
        Vector4f vertical = translation.transform(new Vector4f(0.0f, 1.0f, 0.0f, 0.0f));
        double divisor = center.w * center.w;
        double xx = (double)(horizontal.x * center.w - center.x * horizontal.w) / divisor * width * 0.5;
        double xy = (double)(vertical.x * center.w - center.x * vertical.w) / divisor * width * 0.5;
        double yx = (double)(-(horizontal.y * center.w - center.y * horizontal.w)) / divisor * height * 0.5;
        double yy = (double)(-(vertical.y * center.w - center.y * vertical.w)) / divisor * height * 0.5;
        Region region = new Region(Math.max(0.0, minX - 5.0), Math.max(0.0, minY - 5.0), Math.min(width, maxX + 5.0), Math.min(height, maxY + 5.0), xx, xy, yx, yy);
        Region previous = this.regions[hand.ordinal()];
        if (previous != null) {
            region = new Region(Math.min(previous.minX, region.minX), Math.min(previous.minY, region.minY), Math.max(previous.maxX, region.maxX), Math.max(previous.maxY, region.maxY), xx, xy, yx, yy);
        }
        this.regions[hand.ordinal()] = region;
    }

    public Region region(boolean main) {
        return this.regions[main ? 0 : 1];
    }

    public record Region(double minX, double minY, double maxX, double maxY, double xx, double xy, double yx, double yy) {
        public boolean contains(double x, double y) {
            return x >= this.minX && x <= this.maxX && y >= this.minY && y <= this.maxY;
        }

        public double localX(double x, double y) {
            return (x * this.yy - y * this.xy) / (this.xx * this.yy - this.xy * this.yx);
        }

        public double localY(double x, double y) {
            return (y * this.xx - x * this.yx) / (this.xx * this.yy - this.xy * this.yx);
        }

        public boolean draggable() {
            return Math.abs(this.xx * this.yy - this.xy * this.yx) > 1.0E-6;
        }
    }
}

