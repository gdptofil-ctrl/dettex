/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_243
 *  net.minecraft.class_287
 *  net.minecraft.class_290
 *  net.minecraft.class_3532
 *  net.minecraft.class_9799
 *  net.minecraft.class_9801
 */
package ru.recoin.common.util.render.effects;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.class_243;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_3532;
import net.minecraft.class_9799;
import net.minecraft.class_9801;
import ru.recoin.common.util.render.effects.BloodSimulation;

final class BloodMesh
implements AutoCloseable {
    private static final double[] CLOSE_SPHERE = BloodMesh.sphere(20, 8);
    private static final double[] SMALL_SPHERE = BloodMesh.sphere(12, 5);
    private static final double[] MEDIUM_SPHERE = BloodMesh.sphere(8, 3);
    private static final double[] FAR_SPHERE = BloodMesh.sphere(6, 3);
    private final class_9799 allocator = new class_9799(0x100000);
    private final List<BloodSimulation.Drop> sortedDrops = new ArrayList<BloodSimulation.Drop>();
    private class_243 camera;

    BloodMesh() {
    }

    class_9801 build(BloodSimulation simulation, class_243 camera, float partialTicks, boolean drips) {
        this.camera = camera;
        float partial = class_3532.method_15363((float)partialTicks, (float)0.0f, (float)1.0f);
        class_287 buffer = new class_287(this.allocator, VertexFormat.class_5596.field_27379, class_290.field_1577);
        for (BloodSimulation.Stain stain : simulation.stains) {
            if (!(stain.position.method_1025(camera) <= 4096.0)) continue;
            this.appendStain(buffer, stain, partial, drips);
        }
        this.sortedDrops.clear();
        for (BloodSimulation.Drop drop2 : simulation.drops) {
            if (!(drop2.position.method_1025(camera) <= 4096.0)) continue;
            this.sortedDrops.add(drop2);
        }
        this.sortedDrops.sort(Comparator.comparingDouble((BloodSimulation.Drop drop) -> drop.position.method_1025(camera)).reversed());
        for (BloodSimulation.Drop drop2 : this.sortedDrops) {
            this.appendDrop(buffer, drop2, partial);
        }
        return buffer.method_60794();
    }

    private void appendDrop(class_287 buffer, BloodSimulation.Drop drop, float partial) {
        class_243 center = drop.previous.method_35590(drop.position, (double)partial);
        double speed = drop.velocity.method_1033();
        class_243 axis = speed < 1.0E-5 ? new class_243(0.0, -1.0, 0.0) : drop.velocity.method_1021(1.0 / speed);
        class_243 u = BloodSimulation.tangent(axis);
        class_243 v = axis.method_1036(u).method_1029();
        double length = drop.radius * (1.0 + Math.min(1.1, speed * 2.0));
        double distance = center.method_1025(this.camera);
        double footprint = drop.radius * drop.radius * 250000.0 / Math.max(0.04, distance);
        double[] sphere = footprint > 25.0 ? CLOSE_SPHERE : (footprint > 4.0 ? SMALL_SPHERE : (footprint > 1.0 ? MEDIUM_SPHERE : FAR_SPHERE));
        float fade = Math.min(1.0f, ((float)drop.age + partial) * 0.7f) * (1.0f - BloodSimulation.smooth(((float)drop.age + partial - 100.0f) / 20.0f));
        int color = BloodMesh.alpha(drop.color, fade * 0.96f);
        for (int i = 0; i < sphere.length; i += 3) {
            double x = sphere[i];
            double y = sphere[i + 1];
            double z = sphere[i + 2];
            double nx = (u.field_1352 * x + v.field_1352 * y) / drop.radius + axis.field_1352 * z / length;
            double ny = (u.field_1351 * x + v.field_1351 * y) / drop.radius + axis.field_1351 * z / length;
            double nz = (u.field_1350 * x + v.field_1350 * y) / drop.radius + axis.field_1350 * z / length;
            double magnitude = Math.sqrt(nx * nx + ny * ny + nz * nz);
            buffer.method_22912((float)(center.field_1352 - this.camera.field_1352 + (u.field_1352 * x + v.field_1352 * y) * drop.radius + axis.field_1352 * z * length), (float)(center.field_1351 - this.camera.field_1351 + (u.field_1351 * x + v.field_1351 * y) * drop.radius + axis.field_1351 * z * length), (float)(center.field_1350 - this.camera.field_1350 + (u.field_1350 * x + v.field_1350 * y) * drop.radius + axis.field_1350 * z * length)).method_22913(1.0f, drop.light).method_39415(color).method_22914((float)(nx / magnitude), (float)(ny / magnitude), (float)(nz / magnitude));
        }
    }

    private static double[] sphere(int segments, int rings) {
        double[] vertices = new double[segments * rings * 18];
        int cursor = 0;
        for (int ring = 0; ring < rings; ++ring) {
            for (int segment = 0; segment < segments; ++segment) {
                int[] latitudes = new int[]{ring, ring + 1, ring + 1, ring, ring + 1, ring};
                int[] longitudes = new int[]{segment, segment, segment + 1, segment, segment + 1, segment + 1};
                for (int i = 0; i < 6; ++i) {
                    double latitude = (double)latitudes[i] * Math.PI / (double)rings;
                    double angle = (double)longitudes[i] * Math.PI * 2.0 / (double)segments;
                    vertices[cursor++] = Math.sin(latitude) * Math.cos(angle);
                    vertices[cursor++] = Math.sin(latitude) * Math.sin(angle);
                    vertices[cursor++] = Math.cos(latitude);
                }
            }
        }
        return vertices;
    }

    private void appendStain(class_287 buffer, BloodSimulation.Stain stain, float partial, boolean drips) {
        int i;
        float age = (float)stain.age + partial;
        float life = age / (float)stain.lifetime;
        float fade = 1.0f - BloodSimulation.smooth((life - 0.72f) / 0.28f);
        float grow = 0.35f + 0.65f * BloodSimulation.smooth(age / 6.0f);
        float wet = 1.0f - BloodSimulation.smooth(age / ((float)stain.lifetime * 0.65f));
        class_243 normal = stain.surface.normal();
        class_243 offset = normal.method_1021(0.0018);
        Point center = new Point(stain.position.method_1019(offset).method_1019(normal.method_1021(Math.min(0.0014, stain.radius * 0.012) * (double)wet)), normal);
        int core = BloodMesh.alpha(stain.color, fade * 0.96f);
        int edge = BloodMesh.alpha(stain.color, fade * 0.84f);
        int transparent = BloodMesh.alpha(stain.color, 0.0f);
        double radius = stain.radius * (double)grow;
        Point[] inner = new Point[24];
        Point[] outer = new Point[24];
        for (i = 0; i < inner.length; ++i) {
            inner[i] = BloodMesh.stainPoint(stain, radius * 0.89, i, offset, wet);
            outer[i] = BloodMesh.stainPoint(stain, radius, i, offset, wet);
        }
        for (i = 0; i < 24; ++i) {
            int next = (i + 1) % 24;
            Point inner0 = inner[i];
            Point inner1 = inner[next];
            Point outer0 = outer[i];
            Point outer1 = outer[next];
            this.triangle(buffer, center, inner0, inner1, core, edge, edge, wet, stain.light);
            this.triangle(buffer, inner0, outer0, outer1, edge, transparent, transparent, wet, stain.light);
            this.triangle(buffer, inner0, outer1, inner1, edge, transparent, edge, wet, stain.light);
        }
        if (drips && stain.dripLength > 0.008) {
            double width = Math.min(0.013, stain.radius * 0.2);
            class_243 across = normal.method_1036(new class_243(0.0, -1.0, 0.0)).method_1029();
            for (int segment = 0; segment < 8; ++segment) {
                double p0 = (double)segment / 8.0;
                double p1 = (double)(segment + 1) / 8.0;
                Point a = BloodMesh.dripPoint(stain, across, offset, width, p0, -1);
                Point b = BloodMesh.dripPoint(stain, across, offset, width, p0, 1);
                Point c = BloodMesh.dripPoint(stain, across, offset, width, p1, 1);
                Point d = BloodMesh.dripPoint(stain, across, offset, width, p1, -1);
                this.triangle(buffer, a, b, c, edge, edge, core, wet, stain.light);
                this.triangle(buffer, a, c, d, edge, core, core, wet, stain.light);
            }
            class_243 end = stain.surface.clip(stain.position.method_1031(0.0, -stain.dripLength, 0.0));
            Point tip = new Point(end.method_1019(offset).method_1019(normal.method_1021(8.0E-4 * (double)wet)), normal);
            for (int segment = 0; segment < 12; ++segment) {
                double a0 = (double)segment * Math.PI * 2.0 / 12.0;
                double a1 = (double)(segment + 1) * Math.PI * 2.0 / 12.0;
                Point a = BloodMesh.dripTip(stain, end, across, offset, width, a0);
                Point b = BloodMesh.dripTip(stain, end, across, offset, width, a1);
                this.triangle(buffer, tip, a, b, core, edge, edge, wet, stain.light);
            }
        }
    }

    private static Point stainPoint(BloodSimulation.Stain stain, double radius, int index, class_243 offset, float wet) {
        double angle = (double)index * Math.PI * 2.0 / 24.0;
        double r = radius * (double)stain.edge[index];
        class_243 point = stain.position.method_1019(stain.u.method_1021(Math.cos(angle) * r * Math.sqrt(stain.stretch))).method_1019(stain.v.method_1021(Math.sin(angle) * r / Math.sqrt(stain.stretch)));
        class_243 outward = stain.u.method_1021(Math.cos(angle)).method_1019(stain.v.method_1021(Math.sin(angle)));
        class_243 normal = stain.surface.normal().method_1019(outward.method_1021(Math.min(0.1, 0.0014 / stain.radius) * (double)wet)).method_1029();
        return new Point(stain.surface.clip(point).method_1019(offset), normal);
    }

    private static Point dripPoint(BloodSimulation.Stain stain, class_243 across, class_243 offset, double width, double progress, int side) {
        double curve = Math.sin(progress * Math.PI) * ((double)stain.edge[7] - 0.9) * 0.025;
        double thickness = width * (0.8 - progress * 0.42 + Math.sin(progress * Math.PI * 3.0) * 0.08);
        class_243 point = stain.position.method_1031(0.0, -stain.dripLength * progress, 0.0).method_1019(across.method_1021(curve + thickness * (double)side));
        return new Point(stain.surface.clip(point).method_1019(offset), stain.surface.normal());
    }

    private static Point dripTip(BloodSimulation.Stain stain, class_243 end, class_243 across, class_243 offset, double width, double angle) {
        class_243 point = end.method_1019(across.method_1021(Math.cos(angle) * width * 0.75)).method_1031(0.0, Math.sin(angle) * width * 1.25, 0.0);
        return new Point(stain.surface.clip(point).method_1019(offset), stain.surface.normal());
    }

    private void triangle(class_287 buffer, Point a, Point b, Point c, int ca, int cb, int cc, float wetness, float light) {
        this.vertex(buffer, a, ca, wetness, light);
        this.vertex(buffer, b, cb, wetness, light);
        this.vertex(buffer, c, cc, wetness, light);
    }

    private void vertex(class_287 buffer, Point point, int color, float wetness, float light) {
        class_243 relative = point.position.method_1020(this.camera);
        buffer.method_22912((float)relative.field_1352, (float)relative.field_1351, (float)relative.field_1350).method_22913(wetness, light).method_39415(color).method_22914((float)point.normal.field_1352, (float)point.normal.field_1351, (float)point.normal.field_1350);
    }

    private static int alpha(int color, float alpha) {
        return class_3532.method_15340((int)Math.round(alpha * 255.0f), (int)0, (int)255) << 24 | color & 0xFFFFFF;
    }

    @Override
    public void close() {
        this.sortedDrops.clear();
        this.allocator.close();
    }

    private record Point(class_243 position, class_243 normal) {
    }
}

