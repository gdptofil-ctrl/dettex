/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_10055
 *  net.minecraft.class_11659
 *  net.minecraft.class_1297
 *  net.minecraft.class_1921
 *  net.minecraft.class_243
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  net.minecraft.class_3883
 *  net.minecraft.class_3887
 *  net.minecraft.class_4587
 *  net.minecraft.class_4587$class_4665
 *  net.minecraft.class_4588
 *  net.minecraft.class_481
 *  net.minecraft.class_490
 *  net.minecraft.class_591
 *  net.minecraft.class_7833
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionfc
 */
package ru.recoin.common.util.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.class_10055;
import net.minecraft.class_11659;
import net.minecraft.class_1297;
import net.minecraft.class_1921;
import net.minecraft.class_243;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_481;
import net.minecraft.class_490;
import net.minecraft.class_591;
import net.minecraft.class_7833;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.compat.render.BufferRenderer;
import ru.recoin.core.Main;
import ru.recoin.implement.events.render.WorldRenderEvent;
import ru.recoin.implement.features.modules.render.PenisESP;

public final class PenisESPFeatureRenderer
extends class_3887<class_10055, class_591> {
    private static final int SEGMENTS = 12;
    private static final int RINGS = 6;
    private static final int HEAD_COLOR = -812374;
    private static final float[] SPHERE = PenisESPFeatureRenderer.createSphere(12, 6);
    private static final float[] HEAD = PenisESPFeatureRenderer.createSphere(24, 12);
    private static final class_1921 LAYER = BufferRenderer.translucentPositionColorDepthLayer("penis_esp", VertexFormat.class_5596.field_27382, 32768);

    public PenisESPFeatureRenderer(class_3883<class_10055, class_591> context) {
        super(context);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void method_4199(class_4587 matrices, class_11659 queue, int light, class_10055 state, float limbAngle, float limbDistance) {
        class_310 client = class_310.method_1551();
        if (client.field_1724 == null || state.field_53528 != client.field_1724.method_5628() || client.field_1724.method_7325() || Main.getInstance() == null || Main.getInstance().getModuleProvider() == null) {
            return;
        }
        PenisESP module = Main.getInstance().getModuleProvider().get(PenisESP.class);
        if (module == null || !module.isState()) {
            return;
        }
        if (client.field_1690.method_31044().method_31034() && !(client.field_1755 instanceof class_490) && !(client.field_1755 instanceof class_481)) {
            return;
        }
        float length = module.getLength().getValue();
        float radius = module.getRadius().getValue();
        int color = module.getColor().getColor();
        matrices.method_22903();
        try {
            ((class_591)this.method_17165()).field_3391.method_22703(matrices);
            matrices.method_46416(0.0f, 0.72f, -0.15f);
            queue.method_73483(matrices, LAYER, (entry, consumer) -> PenisESPFeatureRenderer.drawGeometry(entry, consumer, length, radius, color));
        }
        finally {
            matrices.method_22909();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void renderFirstPerson(WorldRenderEvent event, PenisESP module) {
        class_310 client = class_310.method_1551();
        if (client.field_1724 == null || client.field_1687 == null || client.field_1773 == null || !module.isState()) {
            return;
        }
        class_243 relative = MathUtil.interpolate((class_1297)client.field_1724).method_1020(client.field_1773.method_19418().method_71156());
        float yaw = class_3532.method_17821((float)event.getPartialTicks(), (float)client.field_1724.field_6220, (float)client.field_1724.field_6283);
        class_4587 matrices = event.getStack();
        matrices.method_22903();
        try {
            matrices.method_22904(relative.field_1352, relative.field_1351 + (double)(client.field_1724.method_17682() * 0.43f), relative.field_1350);
            matrices.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees(180.0f - yaw));
            matrices.method_22905(1.0f, -1.0f, 1.0f);
            matrices.method_46416(0.0f, 0.0f, -0.15f);
            class_287 buffer = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27382, class_290.field_1576);
            PenisESPFeatureRenderer.drawGeometry(matrices.method_23760(), (class_4588)buffer, module.getLength().getValue(), module.getRadius().getValue(), module.getColor().getColor());
            LAYER.method_60895(buffer.method_60800());
        }
        finally {
            matrices.method_22909();
        }
    }

    private static void drawGeometry(class_4587.class_4665 entry, class_4588 consumer, float length, float radius, int color) {
        for (int segment = 0; segment < 12; ++segment) {
            double a = (double)segment * Math.PI * 2.0 / 12.0;
            double b = (double)(segment + 1) * Math.PI * 2.0 / 12.0;
            float ax = (float)Math.cos(a) * radius;
            float ay = (float)Math.sin(a) * radius;
            float bx = (float)Math.cos(b) * radius;
            float by = (float)Math.sin(b) * radius;
            int shade = ColorUtil.multBright(color, 0.78f + 0.18f * (float)((Math.cos(a) + 1.0) / 2.0));
            PenisESPFeatureRenderer.vertex(entry, consumer, ax, ay, 0.0f, shade);
            PenisESPFeatureRenderer.vertex(entry, consumer, bx, by, 0.0f, shade);
            PenisESPFeatureRenderer.vertex(entry, consumer, bx, by, -length, shade);
            PenisESPFeatureRenderer.vertex(entry, consumer, ax, ay, -length, shade);
            PenisESPFeatureRenderer.vertex(entry, consumer, 0.0f, 0.0f, 0.0f, shade);
            PenisESPFeatureRenderer.vertex(entry, consumer, ax, ay, 0.0f, shade);
            PenisESPFeatureRenderer.vertex(entry, consumer, bx, by, 0.0f, shade);
            PenisESPFeatureRenderer.vertex(entry, consumer, 0.0f, 0.0f, 0.0f, shade);
        }
        PenisESPFeatureRenderer.ellipsoid(entry, consumer, HEAD, 0.0f, 0.0f, -length, radius * 1.18f, radius * 1.1f, radius * 0.95f, ColorUtil.replAlpha(-812374, ColorUtil.alpha(color)));
        PenisESPFeatureRenderer.sphere(entry, consumer, -radius * 0.85f, radius * 0.7f, -radius * 0.2f, radius * 1.05f, color);
        PenisESPFeatureRenderer.sphere(entry, consumer, radius * 0.85f, radius * 0.7f, -radius * 0.2f, radius * 1.05f, color);
    }

    private static void sphere(class_4587.class_4665 entry, class_4588 consumer, float x, float y, float z, float radius, int color) {
        PenisESPFeatureRenderer.ellipsoid(entry, consumer, SPHERE, x, y, z, radius, radius, radius, color);
    }

    private static void ellipsoid(class_4587.class_4665 entry, class_4588 consumer, float[] mesh, float x, float y, float z, float radiusX, float radiusY, float radiusZ, int color) {
        for (int i = 0; i < mesh.length; i += 12) {
            for (int corner = 0; corner < 4; ++corner) {
                int offset = i + corner * 3;
                int shade = ColorUtil.multBright(color, 0.87f - mesh[offset + 1] * 0.11f);
                PenisESPFeatureRenderer.vertex(entry, consumer, x + mesh[offset] * radiusX, y + mesh[offset + 1] * radiusY, z + mesh[offset + 2] * radiusZ, shade);
            }
        }
    }

    private static void vertex(class_4587.class_4665 entry, class_4588 consumer, float x, float y, float z, int color) {
        consumer.method_22918((Matrix4fc)entry.method_23761(), x, y, z).method_39415(color);
    }

    private static float[] createSphere(int segments, int rings) {
        float[] vertices = new float[rings * segments * 12];
        int offset = 0;
        for (int ring = 0; ring < rings; ++ring) {
            for (int segment = 0; segment < segments; ++segment) {
                for (int corner = 0; corner < 4; ++corner) {
                    double latitude = -1.5707963267948966 + Math.PI * (double)(ring + (corner >= 2 ? 1 : 0)) / (double)rings;
                    double longitude = Math.PI * 2 * (double)(segment + (corner == 1 || corner == 2 ? 1 : 0)) / (double)segments;
                    vertices[offset++] = (float)(Math.cos(latitude) * Math.cos(longitude));
                    vertices[offset++] = (float)(Math.cos(latitude) * Math.sin(longitude));
                    vertices[offset++] = (float)Math.sin(latitude);
                }
            }
        }
        return vertices;
    }
}

