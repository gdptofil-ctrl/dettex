/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10055
 *  net.minecraft.class_11659
 *  net.minecraft.class_12249
 *  net.minecraft.class_1921
 *  net.minecraft.class_2960
 *  net.minecraft.class_3532
 *  net.minecraft.class_4587
 *  net.minecraft.class_4587$class_4665
 *  net.minecraft.class_4588
 *  net.minecraft.class_4608
 *  net.minecraft.class_742
 *  net.minecraft.class_7833
 *  org.joml.Quaternionfc
 */
package ru.recoin.common.util.render;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_10055;
import net.minecraft.class_11659;
import net.minecraft.class_12249;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4608;
import net.minecraft.class_742;
import net.minecraft.class_7833;
import org.joml.Quaternionfc;

public final class WaveyCapeRenderer {
    private static final int SEGMENTS = 16;
    private static final float CAPE_WIDTH = 0.625f;
    private static final float CAPE_HEIGHT = 1.0f;
    private static final float CAPE_DEPTH = 0.0625f;
    private static final Map<Integer, Simulation> SIMULATIONS = new HashMap<Integer, Simulation>();

    private WaveyCapeRenderer() {
    }

    public static void update(class_742 player, float tickDelta) {
        Simulation simulation = SIMULATIONS.computeIfAbsent(player.method_5628(), ignored -> new Simulation(player));
        simulation.tickDelta = tickDelta;
        simulation.update(player);
    }

    public static void render(class_4587 matrices, class_11659 queue, int light, class_10055 state, class_2960 texture) {
        Simulation simulation = SIMULATIONS.get(state.field_53528);
        if (simulation == null) {
            return;
        }
        float tickDelta = simulation.tickDelta;
        class_1921 layer = class_12249.method_75982((class_2960)texture);
        class_4587 queuedMatrices = new class_4587();
        queuedMatrices.method_23760().method_66521(matrices.method_23760());
        queue.method_73483(queuedMatrices, layer, (stack, consumer) -> WaveyCapeRenderer.renderSmoothCape(stack, consumer, light, simulation, tickDelta));
    }

    private static void renderSmoothCape(class_4587.class_4665 base, class_4588 consumer, int light, Simulation simulation, float tickDelta) {
        int part;
        class_4587.class_4665[] transforms = new class_4587.class_4665[16];
        Point root = simulation.points[0];
        for (part = 0; part < 16; ++part) {
            Point point = simulation.points[part];
            Point next = part + 1 < 16 ? simulation.points[part + 1] : point;
            double rootX = root.lerpX(tickDelta);
            double rootY = root.lerpY(tickDelta);
            double rootZ = root.lerpZ(tickDelta);
            double x = Math.min(0.0, point.lerpX(tickDelta) - rootX);
            double y = rootY - (double)part - point.lerpY(tickDelta);
            double z = rootZ - point.lerpZ(tickDelta);
            double dx = next.lerpX(tickDelta) - point.lerpX(tickDelta);
            double dy = next.lerpY(tickDelta) - point.lerpY(tickDelta);
            if (part == 15) {
                Point previous = simulation.points[part - 1];
                dx = point.lerpX(tickDelta) - previous.lerpX(tickDelta);
                dy = point.lerpY(tickDelta) - previous.lerpY(tickDelta);
            }
            float rotation = (float)Math.toDegrees(Math.atan2(dx, dy)) + 180.0f;
            class_4587 partStack = new class_4587();
            partStack.method_23760().method_66521(base);
            partStack.method_46416(0.0f, 0.0f, 0.125f);
            partStack.method_22907((Quaternionfc)class_7833.field_40714.rotationDegrees(6.0f));
            partStack.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees(180.0f));
            partStack.method_22904(-z / 16.0, y / 16.0, x / 16.0);
            partStack.method_22904(0.0, 0.03, -0.03);
            partStack.method_22904(0.0, (double)part / 16.0, 0.0);
            partStack.method_22907((Quaternionfc)class_7833.field_40714.rotationDegrees(-rotation));
            partStack.method_22904(0.0, (double)(-part) / 16.0, 0.0);
            partStack.method_22904(0.0, -0.03, 0.03);
            transforms[part] = partStack.method_23760().method_56822();
        }
        for (part = 0; part < 16; ++part) {
            class_4587.class_4665 top = transforms[Math.max(part - 1, 0)];
            class_4587.class_4665 bottom = transforms[part];
            float y0 = (float)part * 0.0625f;
            float y1 = (float)(part + 1) * 0.0625f;
            float v0 = 0.03125f * (float)(part + 1);
            float v1 = v0 + 0.03125f;
            if (part == 0) {
                WaveyCapeRenderer.vertex(consumer, bottom, 0.3125f, 0.0f, 0.0f, 0.171875f, 0.03125f, light, 0.0f, -1.0f, 0.0f);
                WaveyCapeRenderer.vertex(consumer, bottom, -0.3125f, 0.0f, 0.0f, 0.015625f, 0.03125f, light, 0.0f, -1.0f, 0.0f);
                WaveyCapeRenderer.vertex(consumer, bottom, -0.3125f, 0.0f, -0.0625f, 0.015625f, 0.0f, light, 0.0f, -1.0f, 0.0f);
                WaveyCapeRenderer.vertex(consumer, bottom, 0.3125f, 0.0f, -0.0625f, 0.171875f, 0.0f, light, 0.0f, -1.0f, 0.0f);
            }
            if (part == 15) {
                WaveyCapeRenderer.vertex(consumer, bottom, 0.3125f, 1.0f, -0.0625f, 0.328125f, 0.0f, light, 0.0f, 1.0f, 0.0f);
                WaveyCapeRenderer.vertex(consumer, bottom, -0.3125f, 1.0f, -0.0625f, 0.171875f, 0.0f, light, 0.0f, 1.0f, 0.0f);
                WaveyCapeRenderer.vertex(consumer, bottom, -0.3125f, 1.0f, 0.0f, 0.171875f, 0.03125f, light, 0.0f, 1.0f, 0.0f);
                WaveyCapeRenderer.vertex(consumer, bottom, 0.3125f, 1.0f, 0.0f, 0.328125f, 0.03125f, light, 0.0f, 1.0f, 0.0f);
            }
            WaveyCapeRenderer.vertex(consumer, bottom, -0.3125f, y1, 0.0f, 0.0f, v1, light, -1.0f, 0.0f, 0.0f);
            WaveyCapeRenderer.vertex(consumer, bottom, -0.3125f, y1, -0.0625f, 0.015625f, v1, light, -1.0f, 0.0f, 0.0f);
            WaveyCapeRenderer.vertex(consumer, top, -0.3125f, y0, -0.0625f, 0.015625f, v0, light, -1.0f, 0.0f, 0.0f);
            WaveyCapeRenderer.vertex(consumer, top, -0.3125f, y0, 0.0f, 0.0f, v0, light, -1.0f, 0.0f, 0.0f);
            WaveyCapeRenderer.vertex(consumer, bottom, 0.3125f, y1, -0.0625f, 0.171875f, v1, light, 1.0f, 0.0f, 0.0f);
            WaveyCapeRenderer.vertex(consumer, bottom, 0.3125f, y1, 0.0f, 0.1875f, v1, light, 1.0f, 0.0f, 0.0f);
            WaveyCapeRenderer.vertex(consumer, top, 0.3125f, y0, 0.0f, 0.1875f, v0, light, 1.0f, 0.0f, 0.0f);
            WaveyCapeRenderer.vertex(consumer, top, 0.3125f, y0, -0.0625f, 0.171875f, v0, light, 1.0f, 0.0f, 0.0f);
            WaveyCapeRenderer.vertex(consumer, top, 0.3125f, y0, -0.0625f, 0.171875f, v0, light, 0.0f, 0.0f, -1.0f);
            WaveyCapeRenderer.vertex(consumer, top, -0.3125f, y0, -0.0625f, 0.015625f, v0, light, 0.0f, 0.0f, -1.0f);
            WaveyCapeRenderer.vertex(consumer, bottom, -0.3125f, y1, -0.0625f, 0.015625f, v1, light, 0.0f, 0.0f, -1.0f);
            WaveyCapeRenderer.vertex(consumer, bottom, 0.3125f, y1, -0.0625f, 0.171875f, v1, light, 0.0f, 0.0f, -1.0f);
            WaveyCapeRenderer.vertex(consumer, top, 0.3125f, y0, 0.0f, 0.1875f, v0, light, 0.0f, 0.0f, 1.0f);
            WaveyCapeRenderer.vertex(consumer, top, -0.3125f, y0, 0.0f, 0.34375f, v0, light, 0.0f, 0.0f, 1.0f);
            WaveyCapeRenderer.vertex(consumer, bottom, -0.3125f, y1, 0.0f, 0.34375f, v1, light, 0.0f, 0.0f, 1.0f);
            WaveyCapeRenderer.vertex(consumer, bottom, 0.3125f, y1, 0.0f, 0.1875f, v1, light, 0.0f, 0.0f, 1.0f);
        }
    }

    private static void vertex(class_4588 consumer, class_4587.class_4665 entry, float x, float y, float z, float u, float v, int light, float nx, float ny, float nz) {
        consumer.method_56824(entry, x, y, z).method_39415(-1).method_22913(u, v).method_22922(class_4608.field_21444).method_60803(light).method_60831(entry, nx, ny, nz);
    }

    private static final class Simulation {
        private final Point[] points = new Point[16];
        private int lastAge = Integer.MIN_VALUE;
        private boolean sneaking;
        private float tickDelta;
        private boolean stabilized;

        private Simulation(class_742 player) {
            for (int i = 0; i < this.points.length; ++i) {
                this.points[i] = new Point(-i, -i, 0.0);
            }
        }

        private void update(class_742 player) {
            if (this.lastAge == player.field_6012) {
                return;
            }
            this.lastAge = player.field_6012;
            if (!this.stabilized) {
                this.points[0].x += 1.0;
                this.points[0].y += 1.0;
                for (int i = 0; i < 5; ++i) {
                    this.simulate(player);
                }
                this.stabilized = true;
            }
            this.simulate(player);
        }

        private void simulate(class_742 player) {
            for (Point point : this.points) {
                point.snapshot();
            }
            double yaw = Math.toRadians(player.field_6283);
            double dx = player.method_23317() - player.field_6014;
            double dz = player.method_23321() - player.field_5969;
            double fall = class_3532.method_15350((double)((player.field_6036 - player.method_23318()) * 10.0), (double)0.0, (double)1.0);
            double changeX = (player.method_74192().method_74200(this.tickDelta) - player.method_23317()) * Math.sin(yaw) + (player.method_74192().method_74202(this.tickDelta) - player.method_23321()) * -Math.cos(yaw) + fall;
            double changeY = (player.method_23318() - player.field_6036) * 6.0;
            double changeZ = -(dx * Math.cos(yaw) + dz * Math.sin(yaw)) * 2.0;
            if (player.method_5715() && !this.sneaking) {
                changeX += 3.0;
                changeY += 1.0;
            }
            this.sneaking = player.method_5715();
            this.points[0].x += changeX;
            this.points[0].y += changeY;
            this.points[0].z += changeZ;
            double gravity = (player.method_5869() ? 2.5 : 25.0) * 0.05;
            for (int i = 1; i < this.points.length; ++i) {
                this.points[i].y -= gravity;
            }
            this.preventClipping();
            this.preventSelfClipping();
            this.solveSticks();
            this.preventSelfClipping();
            this.preventHardBends();
            this.limitLength();
        }

        private void solveSticks() {
            for (int pass = 0; pass < 3; ++pass) {
                for (int i = this.points.length - 2; i >= 0; --i) {
                    Point a = this.points[i];
                    Point b = this.points[i + 1];
                    double dx = b.x - a.x;
                    double dy = b.y - a.y;
                    double dz = b.z - a.z;
                    double length = Math.max(1.0E-6, Math.sqrt(dx * dx + dy * dy + dz * dz));
                    double difference = (length - 1.0) / length * 0.5;
                    if (i != 0) {
                        a.x += dx * difference;
                        a.y += dy * difference;
                        a.z += dz * difference;
                    }
                    b.x -= dx * difference;
                    b.y -= dy * difference;
                    b.z -= dz * difference;
                }
            }
        }

        private void preventClipping() {
            Point base = this.points[0];
            for (int i = 1; i < this.points.length; ++i) {
                Point point = this.points[i];
                if (point.x > base.x) {
                    point.x = base.x;
                }
                double maxZ = Math.pow((double)i / (double)this.points.length, 2.0) * 5.0;
                point.z = class_3532.method_15350((double)point.z, (double)(base.z - maxZ), (double)(base.z + maxZ));
            }
        }

        private void preventSelfClipping() {
            boolean changed;
            int runs = 0;
            do {
                changed = false;
                for (int i = 0; i < this.points.length; ++i) {
                    for (int j = i + 1; j < this.points.length; ++j) {
                        Point a = this.points[i];
                        Point b = this.points[j];
                        double dx = b.x - a.x;
                        double dy = b.y - a.y;
                        double dz = b.z - a.z;
                        double squared = dx * dx + dy * dy + dz * dz;
                        if (squared >= 0.99 || squared < 1.0E-8) continue;
                        double length = Math.sqrt(squared);
                        double nx = dx / length;
                        double ny = dy / length;
                        double nz = dz / length;
                        double cx = (a.x + b.x) * 0.5;
                        double cy = (a.y + b.y) * 0.5;
                        double cz = (a.z + b.z) * 0.5;
                        if (i != 0) {
                            a.x = cx - nx * 0.5;
                            a.y = cy - ny * 0.5;
                            a.z = cz - nz * 0.5;
                        }
                        b.x = cx + nx * 0.5;
                        b.y = cy + ny * 0.5;
                        b.z = cz + nz * 0.5;
                        ++runs;
                        changed = true;
                    }
                }
            } while (changed && runs < 32);
        }

        private void preventHardBends() {
            for (int i = 1; i < this.points.length - 2; ++i) {
                Point a = this.points[i - 1];
                Point b = this.points[i];
                Point c = this.points[i + 1];
                double abx = a.x - b.x;
                double cby = a.y - c.y;
                double aby = a.y - b.y;
                double cbx = a.x - c.x;
                double angle = Math.toDegrees(Math.atan2(abx * cby - aby * cbx, abx * cbx + aby * cby));
                if (angle < -20.0) {
                    this.replaceBentPoint(a, b, c, -40.0);
                }
                if (!(angle > 20.0)) continue;
                this.replaceBentPoint(a, b, c, 40.0);
            }
        }

        private void replaceBentPoint(Point previous, Point middle, Point next, double degrees) {
            double x = middle.x - previous.x;
            double y = middle.y - previous.y;
            double radians = Math.toRadians(degrees);
            next.x = middle.x + Math.cos(radians) * x - Math.sin(radians) * y;
            next.y = middle.y + Math.sin(radians) * x + Math.cos(radians) * y;
            next.z = middle.z + (middle.z - previous.z);
        }

        private void limitLength() {
            for (int i = 0; i < this.points.length - 1; ++i) {
                Point a = this.points[i];
                Point b = this.points[i + 1];
                double dx = b.x - a.x;
                double dy = b.y - a.y;
                double dz = b.z - a.z;
                double length = Math.max(1.0E-6, Math.sqrt(dx * dx + dy * dy + dz * dz));
                b.x = a.x + dx / length;
                b.y = a.y + dy / length;
                b.z = a.z + dz / length;
            }
        }
    }

    private static final class Point {
        private double x;
        private double y;
        private double z;
        private double previousX;
        private double previousY;
        private double previousZ;

        private Point(double x, double y, double z) {
            this.x = this.previousX = x;
            this.y = this.previousY = y;
            this.z = this.previousZ = z;
        }

        private void snapshot() {
            this.previousX = this.x;
            this.previousY = this.y;
            this.previousZ = this.z;
        }

        private double lerpX(float delta) {
            return class_3532.method_16436((double)delta, (double)this.previousX, (double)this.x);
        }

        private double lerpY(float delta) {
            return class_3532.method_16436((double)delta, (double)this.previousY, (double)this.y);
        }

        private double lerpZ(float delta) {
            return class_3532.method_16436((double)delta, (double)this.previousZ, (double)this.z);
        }
    }
}

