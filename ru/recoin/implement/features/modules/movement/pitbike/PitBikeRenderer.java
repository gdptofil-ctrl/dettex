/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_10017
 *  net.minecraft.class_1007
 *  net.minecraft.class_11659
 *  net.minecraft.class_12075
 *  net.minecraft.class_1297
 *  net.minecraft.class_1664
 *  net.minecraft.class_1921
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  net.minecraft.class_4587
 *  net.minecraft.class_4588
 *  net.minecraft.class_5617$class_5618
 *  net.minecraft.class_742
 *  net.minecraft.class_7833
 *  net.minecraft.class_897
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package ru.recoin.implement.features.modules.movement.pitbike;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.class_10017;
import net.minecraft.class_1007;
import net.minecraft.class_11659;
import net.minecraft.class_12075;
import net.minecraft.class_1297;
import net.minecraft.class_1664;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_5617;
import net.minecraft.class_742;
import net.minecraft.class_7833;
import net.minecraft.class_897;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import ru.recoin.compat.render.BufferRenderer;
import ru.recoin.implement.features.modules.movement.PitBike;
import ru.recoin.implement.features.modules.movement.pitbike.PitBikeEntity;

public final class PitBikeRenderer
extends class_897<PitBikeEntity, State> {
    private static final class_1921 LAYER = BufferRenderer.translucentPositionColorDepthLayer("pitbike", VertexFormat.class_5596.field_27379, 16384);
    private static final int TIRE = -14671323;
    private static final int TREAD = -13552840;
    private static final int RUBBER = -15658216;
    private static final int METAL = -4077617;
    private static final int STEEL = -8945781;
    private static final int FRAME = -2543071;
    private static final int PLASTIC = -1029338;
    private static final int WHITE = -920587;
    private static final int ENGINE = -11446175;
    private static final int GOLD = -3758744;
    private static final int BLACK = -14407891;
    private static final float[] SIDES = new float[]{-1.0f, 1.0f};

    public PitBikeRenderer(class_5617.class_5618 context) {
        super(context);
        this.field_4673 = 0.75f;
    }

    @Override
    public State method_55269() {
        return new State();
    }

    @Override
    public void method_62354(PitBikeEntity entity, State state, float tickProgress) {
        super.method_62354(entity, state, tickProgress);
        state.yaw = entity.method_61415(tickProgress);
        state.wheelAngle = entity.getWheelPhase(tickProgress);
        state.wheelie = entity.getWheelieAngle(tickProgress);
        state.steering = entity.getSteeringAngle(tickProgress);
        state.lean = entity.getLeanAngle(tickProgress);
        state.parked = !entity.method_5782();
        state.terrainPitch = entity.getTerrainPitch(tickProgress);
        state.suspension = entity.getSuspension(tickProgress);
        class_310 client = class_310.method_1551();
        PitBike module = PitBike.getInstance();
        state.handlebarHands = module != null && module.isHandlebarViewActive() && client.field_1724 != null && client.field_1724.method_5854() == entity && !client.field_1724.method_5767();
        state.skin = state.handlebarHands ? client.field_1724.method_52814().comp_1626().comp_3627() : null;
        state.rightSleeve = state.handlebarHands && client.field_1724.method_74091(class_1664.field_7570);
        state.leftSleeve = state.handlebarHands && client.field_1724.method_74091(class_1664.field_7568);
    }

    @Override
    public void method_3936(State state, class_4587 matrices, class_11659 queue, class_12075 camera) {
        super.method_3936(state, matrices, queue, camera);
        matrices.method_22903();
        matrices.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees(-state.yaw));
        matrices.method_46416(0.0f, -state.suspension, 0.0f);
        matrices.method_46416(0.0f, 0.34f, -0.7f);
        matrices.method_22907((Quaternionfc)class_7833.field_40714.rotationDegrees(-state.wheelie - state.terrainPitch));
        matrices.method_46416(0.0f, -0.34f, 0.7f);
        matrices.method_22907((Quaternionfc)class_7833.field_40718.rotationDegrees(state.parked ? 8.0f : state.lean));
        queue.method_73483(matrices, LAYER, (entry, consumer) -> PitBikeRenderer.renderBike(entry.method_23761(), consumer, state));
        if (state.handlebarHands) {
            PitBikeRenderer.renderHandlebarHands(state, matrices, queue);
        }
        matrices.method_22909();
    }

    private static void renderHandlebarHands(State state, class_4587 matrices, class_11659 queue) {
        class_310 client = class_310.method_1551();
        if (client.field_1724 == null || state.skin == null) {
            return;
        }
        class_1007 renderer = client.method_1561().method_74405((class_742)client.field_1724);
        matrices.method_22903();
        matrices.method_22904(0.0, 1.04, 0.37);
        matrices.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees(-state.steering));
        matrices.method_22904(0.0, -1.04, -0.37);
        for (float side : SIDES) {
            matrices.method_22903();
            matrices.method_46416(0.0f, 1.64f, -0.44f);
            matrices.method_22907((Quaternionfc)class_7833.field_40714.rotationDegrees(120.0f));
            if (side < 0.0f) {
                renderer.method_4220(matrices, queue, state.field_61820, state.skin, state.rightSleeve);
            } else {
                renderer.method_4221(matrices, queue, state.field_61820, state.skin, state.leftSleeve);
            }
            matrices.method_22909();
        }
        matrices.method_22909();
    }

    private static void renderBike(Matrix4f m, class_4588 v, State state) {
        PitBikeRenderer.wheel(m, v, -0.7f, 0.34f, 0.34f, state.wheelAngle);
        for (float side : SIDES) {
            float x = side * 0.12f;
            PitBikeRenderer.tube(m, v, x, 0.36f, -0.7f, x, 0.48f, -0.05f, 0.025f, -8945781);
            PitBikeRenderer.tube(m, v, x, 0.48f, -0.05f, x, 0.87f, 0.29f, 0.028f, -2543071);
            PitBikeRenderer.tube(m, v, x, 0.87f, 0.29f, x, 0.9f, -0.23f, 0.024f, -2543071);
            PitBikeRenderer.tube(m, v, x, 0.9f, -0.23f, x, 0.53f, -0.26f, 0.023f, -2543071);
            PitBikeRenderer.tube(m, v, x, 0.53f, -0.26f, x, 0.48f, -0.05f, 0.026f, -2543071);
            PitBikeRenderer.tube(m, v, x, 0.9f, -0.23f, x, 0.82f, -0.64f, 0.019f, -8945781);
            PitBikeRenderer.tube(m, v, x, 0.82f, -0.64f, x, 0.56f, -0.28f, 0.019f, -8945781);
            PitBikeRenderer.tube(m, v, side * 0.15f, 0.44f, -0.12f, side * 0.28f, 0.44f, -0.12f, 0.021f, -4077617);
            for (int tooth = 0; tooth < 4; ++tooth) {
                PitBikeRenderer.box(m, v, side * 0.25f - 0.012f, 0.455f, -0.16f + (float)tooth * 0.025f, side * 0.25f + 0.012f, 0.47f, -0.147f + (float)tooth * 0.025f, -8945781);
            }
        }
        PitBikeRenderer.tube(m, v, -0.13f, 0.48f, -0.05f, 0.13f, 0.48f, -0.05f, 0.03f, -8945781);
        PitBikeRenderer.tube(m, v, 0.0f, 0.86f, -0.22f, 0.0f, 0.44f, -0.47f, 0.035f, -14407891);
        PitBikeRenderer.spring(m, v, 0.0f, 0.84f, -0.23f, 0.0f, 0.47f, -0.44f, 0.055f, -3758744);
        PitBikeRenderer.box(m, v, -0.14f, 0.41f, -0.18f, 0.14f, 0.58f, 0.12f, -11446175);
        PitBikeRenderer.tube(m, v, -0.165f, 0.49f, -0.06f, 0.165f, 0.49f, -0.06f, 0.115f, -11446175);
        PitBikeRenderer.tube(m, v, -0.175f, 0.49f, -0.06f, -0.17f, 0.49f, -0.06f, 0.08f, -8945781);
        PitBikeRenderer.tube(m, v, 0.17f, 0.49f, -0.06f, 0.175f, 0.49f, -0.06f, 0.08f, -8945781);
        PitBikeRenderer.box(m, v, -0.08f, 0.57f, -0.04f, 0.08f, 0.77f, 0.15f, -11446175);
        for (int fin = 0; fin < 7; ++fin) {
            PitBikeRenderer.box(m, v, -0.12f, 0.59f + (float)fin * 0.027f, -0.07f, 0.12f, 0.598f + (float)fin * 0.027f, 0.18f, -4077617);
        }
        PitBikeRenderer.box(m, v, -0.095f, 0.78f, -0.04f, 0.095f, 0.83f, 0.16f, -11446175);
        PitBikeRenderer.tube(m, v, 0.04f, 0.82f, 0.09f, 0.04f, 0.85f, 0.13f, 0.018f, -920587);
        PitBikeRenderer.tube(m, v, 0.04f, 0.85f, 0.13f, 0.04f, 0.89f, -0.1f, 0.007f, -15658216);
        PitBikeRenderer.tube(m, v, 0.0f, 0.72f, -0.07f, 0.0f, 0.71f, -0.24f, 0.048f, -8945781);
        PitBikeRenderer.box(m, v, -0.105f, 0.66f, -0.34f, 0.105f, 0.81f, -0.23f, -14407891);
        PitBikeRenderer.panel(m, v, -0.34f, 0.83f, 0.9f, 0.16f, 0.17f, -1029338);
        PitBikeRenderer.panel(m, v, 0.17f, 0.87f, 1.02f, 0.12f, 0.19f, -1029338);
        PitBikeRenderer.panel(m, v, 0.3f, 0.91f, 1.0f, 0.1f, 0.12f, -1029338);
        PitBikeRenderer.tube(m, v, 0.0f, 1.01f, 0.07f, 0.0f, 1.045f, 0.07f, 0.04f, -14407891);
        PitBikeRenderer.panel(m, v, -0.64f, 0.91f, 1.015f, 0.12f, 0.15f, -15658216);
        PitBikeRenderer.panel(m, v, -0.15f, 0.93f, 1.03f, 0.15f, 0.115f, -15658216);
        for (int rib = 0; rib < 9; ++rib) {
            PitBikeRenderer.box(m, v, -0.11f, 1.016f, -0.6f + (float)rib * 0.04f, 0.11f, 1.024f, -0.588f + (float)rib * 0.04f, -14407891);
        }
        for (float side : SIDES) {
            Matrix4f shroud = new Matrix4f((Matrix4fc)m).translate(side * 0.17f, 0.83f, 0.1f).rotateX(-0.22f);
            PitBikeRenderer.box(shroud, v, -0.016f, -0.13f, -0.14f, 0.016f, 0.09f, 0.2f, -920587);
            PitBikeRenderer.box(shroud, v, -0.02f, -0.09f, -0.11f, 0.02f, 0.04f, 0.13f, -1029338);
            Matrix4f number = new Matrix4f((Matrix4fc)m).translate(side * 0.17f, 0.84f, -0.43f).rotateX(0.16f);
            PitBikeRenderer.box(number, v, -0.018f, -0.12f, -0.17f, 0.018f, 0.08f, 0.12f, -920587);
            PitBikeRenderer.box(number, v, -0.021f, -0.06f, -0.11f, 0.021f, 0.045f, -0.08f, -14407891);
            PitBikeRenderer.box(number, v, -0.021f, -0.06f, -0.04f, 0.021f, 0.045f, -0.01f, -14407891);
        }
        PitBikeRenderer.panel(m, v, -0.94f, 0.8f, 0.855f, 0.1f, 0.14f, -1029338);
        PitBikeRenderer.panel(m, v, -0.61f, 0.87f, 0.915f, 0.14f, 0.13f, -1029338);
        PitBikeRenderer.tube(m, v, 0.13f, 0.76f, 0.14f, 0.2f, 0.61f, 0.22f, 0.024f, -8945781);
        PitBikeRenderer.tube(m, v, 0.2f, 0.61f, 0.22f, 0.21f, 0.53f, -0.12f, 0.024f, -8945781);
        PitBikeRenderer.tube(m, v, 0.21f, 0.53f, -0.12f, 0.22f, 0.74f, -0.42f, 0.025f, -8945781);
        PitBikeRenderer.tube(m, v, 0.22f, 0.74f, -0.42f, 0.22f, 0.82f, -0.76f, 0.058f, -4077617);
        PitBikeRenderer.tube(m, v, 0.22f, 0.82f, -0.76f, 0.22f, 0.835f, -0.8f, 0.038f, -14407891);
        PitBikeRenderer.tube(m, v, -0.135f, 0.34f, -0.71f, -0.15f, 0.34f, -0.71f, 0.135f, -8945781);
        PitBikeRenderer.tube(m, v, -0.135f, 0.48f, -0.12f, -0.15f, 0.48f, -0.12f, 0.058f, -8945781);
        PitBikeRenderer.tube(m, v, -0.15f, 0.45f, -0.71f, -0.15f, 0.535f, -0.12f, 0.012f, -3758744);
        PitBikeRenderer.tube(m, v, -0.15f, 0.22f, -0.71f, -0.15f, 0.425f, -0.12f, 0.012f, -3758744);
        if (state.parked) {
            PitBikeRenderer.tube(m, v, -0.13f, 0.44f, -0.21f, -0.36f, 0.015f, -0.32f, 0.014f, -8945781);
        }
        Matrix4f front = new Matrix4f((Matrix4fc)m).translate(0.0f, 1.04f, 0.37f).rotateY((float)Math.toRadians(-state.steering)).translate(0.0f, -1.04f, -0.37f);
        PitBikeRenderer.wheel(front, v, 0.73f, 0.36f, 0.36f, state.wheelAngle);
        for (float side : SIDES) {
            float x = side * 0.105f;
            PitBikeRenderer.tube(front, v, x, 0.36f, 0.73f, x, 0.73f, 0.55f, 0.031f, -8945781);
            PitBikeRenderer.tube(front, v, x, 0.73f, 0.55f, x, 1.08f, 0.39f, 0.024f, -3758744);
            PitBikeRenderer.tube(front, v, x, 0.41f, 0.71f, x, 0.65f, 0.59f, 0.035f, -1029338);
        }
        PitBikeRenderer.box(front, v, -0.15f, 0.99f, 0.35f, 0.15f, 1.02f, 0.43f, -8945781);
        PitBikeRenderer.box(front, v, -0.15f, 1.065f, 0.33f, 0.15f, 1.1f, 0.42f, -8945781);
        PitBikeRenderer.tube(front, v, -0.12f, 1.15f, 0.38f, 0.12f, 1.15f, 0.38f, 0.019f, -4077617);
        PitBikeRenderer.tube(front, v, -0.12f, 1.15f, 0.38f, -0.31f, 1.2f, 0.32f, 0.019f, -4077617);
        PitBikeRenderer.tube(front, v, 0.12f, 1.15f, 0.38f, 0.31f, 1.2f, 0.32f, 0.019f, -4077617);
        PitBikeRenderer.tube(front, v, -0.31f, 1.2f, 0.32f, -0.43f, 1.2f, 0.32f, 0.027f, -15658216);
        PitBikeRenderer.tube(front, v, 0.31f, 1.2f, 0.32f, 0.43f, 1.2f, 0.32f, 0.027f, -15658216);
        PitBikeRenderer.tube(front, v, -0.27f, 1.19f, 0.35f, -0.41f, 1.19f, 0.39f, 0.009f, -4077617);
        PitBikeRenderer.tube(front, v, 0.27f, 1.19f, 0.35f, 0.41f, 1.19f, 0.39f, 0.009f, -4077617);
        PitBikeRenderer.tube(front, v, 0.26f, 1.18f, 0.34f, 0.13f, 0.83f, 0.54f, 0.006f, -15658216);
        Matrix4f plate = new Matrix4f((Matrix4fc)front).translate(0.0f, 0.99f, 0.51f).rotateX(-0.25f);
        PitBikeRenderer.box(plate, v, -0.145f, -0.13f, -0.015f, 0.145f, 0.14f, 0.015f, -920587);
        PitBikeRenderer.box(plate, v, -0.06f, -0.06f, 0.016f, -0.027f, 0.07f, 0.02f, -14407891);
        PitBikeRenderer.box(plate, v, 0.027f, -0.06f, 0.016f, 0.06f, 0.07f, 0.02f, -14407891);
        PitBikeRenderer.panel(front, v, 0.51f, 0.78f, 0.82f, 0.11f, 0.13f, -1029338);
        PitBikeRenderer.panel(front, v, 0.94f, 0.72f, 0.765f, 0.12f, 0.095f, -1029338);
    }

    private static void wheel(Matrix4f m, class_4588 v, float z, float y, float radius, float spin) {
        float a;
        int spoke;
        Matrix4f wheel = new Matrix4f((Matrix4fc)m).translate(0.0f, y, z).rotateX(spin);
        PitBikeRenderer.torus(wheel, v, radius - 0.06f, 0.06f, -14671323);
        PitBikeRenderer.torus(wheel, v, radius - 0.115f, 0.012f, -4077617);
        for (spoke = 0; spoke < 24; ++spoke) {
            a = (float)spoke * ((float)Math.PI * 2) / 24.0f;
            float b = a + 0.13f;
            PitBikeRenderer.tube(wheel, v, spoke % 2 == 0 ? 0.028f : -0.028f, class_3532.method_15362((double)a) * 0.048f, class_3532.method_15374((double)a) * 0.048f, spoke % 2 == 0 ? 0.06f : -0.06f, class_3532.method_15362((double)b) * (radius - 0.12f), class_3532.method_15374((double)b) * (radius - 0.12f), 0.0035f, -4077617);
        }
        PitBikeRenderer.tube(wheel, v, -0.09f, 0.0f, 0.0f, 0.09f, 0.0f, 0.0f, 0.045f, -8945781);
        PitBikeRenderer.torus(new Matrix4f((Matrix4fc)wheel).translate(0.093f, 0.0f, 0.0f), v, 0.112f, 0.009f, -8945781);
        for (spoke = 0; spoke < 8; ++spoke) {
            a = (float)spoke * ((float)Math.PI * 2) / 8.0f;
            PitBikeRenderer.tube(wheel, v, 0.094f, 0.0f, 0.0f, 0.094f, class_3532.method_15362((double)a) * 0.11f, class_3532.method_15374((double)a) * 0.11f, 0.007f, -8945781);
        }
        PitBikeRenderer.box(m, v, 0.1f, y - 0.015f, z - 0.13f, 0.15f, y + 0.065f, z - 0.065f, -14407891);
        for (int tread = 0; tread < 28; ++tread) {
            a = (float)tread * ((float)Math.PI * 2) / 28.0f;
            Matrix4f block = new Matrix4f((Matrix4fc)wheel).rotateX(a).translate(0.0f, radius - 0.002f, 0.0f);
            PitBikeRenderer.box(block, v, -0.023f, 0.0f, -0.019f, 0.023f, 0.016f, 0.019f, -13552840);
            PitBikeRenderer.box(block, v, -0.064f, -0.009f, -0.017f, -0.037f, 0.01f, 0.017f, -13552840);
            PitBikeRenderer.box(block, v, 0.037f, -0.009f, -0.017f, 0.064f, 0.01f, 0.017f, -13552840);
        }
    }

    private static void torus(Matrix4f m, class_4588 v, float radius, float section, int color) {
        for (int ring = 0; ring < 28; ++ring) {
            float a = (float)ring * ((float)Math.PI * 2) / 28.0f;
            float b = (float)(ring + 1) * ((float)Math.PI * 2) / 28.0f;
            for (int side = 0; side < 8; ++side) {
                float c = (float)side * ((float)Math.PI * 2) / 8.0f;
                float d = (float)(side + 1) * ((float)Math.PI * 2) / 8.0f;
                float rc = radius + class_3532.method_15362((double)c) * section;
                float rd = radius + class_3532.method_15362((double)d) * section;
                PitBikeRenderer.quad(m, v, class_3532.method_15374((double)c) * section, class_3532.method_15362((double)a) * rc, class_3532.method_15374((double)a) * rc, class_3532.method_15374((double)c) * section, class_3532.method_15362((double)b) * rc, class_3532.method_15374((double)b) * rc, class_3532.method_15374((double)d) * section, class_3532.method_15362((double)b) * rd, class_3532.method_15374((double)b) * rd, class_3532.method_15374((double)d) * section, class_3532.method_15362((double)a) * rd, class_3532.method_15374((double)a) * rd, color);
            }
        }
    }

    private static void panel(Matrix4f m, class_4588 v, float z, float bottom, float top, float rearWidth, float frontWidth, int color) {
        float end = z + 0.32f;
        PitBikeRenderer.quad(m, v, -rearWidth, bottom, z, rearWidth, bottom, z, frontWidth, bottom, end, -frontWidth, bottom, end, color);
        PitBikeRenderer.quad(m, v, -rearWidth, top, z, -frontWidth, top - 0.03f, end, frontWidth, top - 0.03f, end, rearWidth, top, z, color);
        PitBikeRenderer.quad(m, v, -rearWidth, bottom, z, -frontWidth, bottom, end, -frontWidth, top - 0.03f, end, -rearWidth, top, z, color);
        PitBikeRenderer.quad(m, v, rearWidth, bottom, z, rearWidth, top, z, frontWidth, top - 0.03f, end, frontWidth, bottom, end, color);
        PitBikeRenderer.quad(m, v, -rearWidth, bottom, z, -rearWidth, top, z, rearWidth, top, z, rearWidth, bottom, z, color);
        PitBikeRenderer.quad(m, v, -frontWidth, bottom, end, frontWidth, bottom, end, frontWidth, top - 0.03f, end, -frontWidth, top - 0.03f, end, color);
    }

    private static void spring(Matrix4f m, class_4588 v, float ax, float ay, float az, float bx, float by, float bz, float radius, int color) {
        Vector3f axis = new Vector3f(bx - ax, by - ay, bz - az).normalize();
        Vector3f u = new Vector3f(1.0f, 0.0f, 0.0f);
        Vector3f w = new Vector3f((Vector3fc)axis).cross((Vector3fc)u).normalize();
        Vector3f last = new Vector3f(ax + u.x * radius, ay + u.y * radius, az + u.z * radius);
        for (int segment = 1; segment <= 64; ++segment) {
            float t = (float)segment / 64.0f;
            float angle = t * ((float)Math.PI * 2) * 8.0f;
            Vector3f point = new Vector3f(ax + (bx - ax) * t, ay + (by - ay) * t, az + (bz - az) * t).fma(class_3532.method_15362((double)angle) * radius, (Vector3fc)u).fma(class_3532.method_15374((double)angle) * radius, (Vector3fc)w);
            PitBikeRenderer.tube(m, v, last.x, last.y, last.z, point.x, point.y, point.z, 0.008f, color);
            last = point;
        }
    }

    private static void tube(Matrix4f m, class_4588 v, float ax, float ay, float az, float bx, float by, float bz, float radius, int color) {
        Vector3f axis = new Vector3f(bx - ax, by - ay, bz - az);
        if (axis.lengthSquared() < 1.0E-8f) {
            return;
        }
        axis.normalize();
        Vector3f u = new Vector3f((Vector3fc)axis).cross((Vector3fc)(Math.abs(axis.y) < 0.9f ? new Vector3f(0.0f, 1.0f, 0.0f) : new Vector3f(1.0f, 0.0f, 0.0f))).normalize();
        Vector3f w = new Vector3f((Vector3fc)axis).cross((Vector3fc)u).normalize();
        for (int side = 0; side < 8; ++side) {
            float a = (float)side * ((float)Math.PI * 2) / 8.0f;
            float b = (float)(side + 1) * ((float)Math.PI * 2) / 8.0f;
            float x1 = (u.x * class_3532.method_15362((double)a) + w.x * class_3532.method_15374((double)a)) * radius;
            float y1 = (u.y * class_3532.method_15362((double)a) + w.y * class_3532.method_15374((double)a)) * radius;
            float z1 = (u.z * class_3532.method_15362((double)a) + w.z * class_3532.method_15374((double)a)) * radius;
            float x2 = (u.x * class_3532.method_15362((double)b) + w.x * class_3532.method_15374((double)b)) * radius;
            float y2 = (u.y * class_3532.method_15362((double)b) + w.y * class_3532.method_15374((double)b)) * radius;
            float z2 = (u.z * class_3532.method_15362((double)b) + w.z * class_3532.method_15374((double)b)) * radius;
            PitBikeRenderer.quad(m, v, ax + x1, ay + y1, az + z1, bx + x1, by + y1, bz + z1, bx + x2, by + y2, bz + z2, ax + x2, ay + y2, az + z2, color);
            PitBikeRenderer.quad(m, v, ax, ay, az, ax + x2, ay + y2, az + z2, ax + x1, ay + y1, az + z1, ax, ay, az, color);
            PitBikeRenderer.quad(m, v, bx, by, bz, bx + x1, by + y1, bz + z1, bx + x2, by + y2, bz + z2, bx, by, bz, color);
        }
    }

    private static void box(Matrix4f m, class_4588 v, float x1, float y1, float z1, float x2, float y2, float z2, int color) {
        PitBikeRenderer.quad(m, v, x1, y1, z1, x2, y1, z1, x2, y2, z1, x1, y2, z1, color);
        PitBikeRenderer.quad(m, v, x2, y1, z2, x1, y1, z2, x1, y2, z2, x2, y2, z2, color);
        PitBikeRenderer.quad(m, v, x1, y1, z2, x1, y1, z1, x1, y2, z1, x1, y2, z2, color);
        PitBikeRenderer.quad(m, v, x2, y1, z1, x2, y1, z2, x2, y2, z2, x2, y2, z1, color);
        PitBikeRenderer.quad(m, v, x1, y2, z1, x2, y2, z1, x2, y2, z2, x1, y2, z2, color);
        PitBikeRenderer.quad(m, v, x1, y1, z2, x2, y1, z2, x2, y1, z1, x1, y1, z1, color);
    }

    private static void quad(Matrix4f m, class_4588 v, float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz, float dx, float dy, float dz, int color) {
        float nx = (by - ay) * (cz - az) - (bz - az) * (cy - ay);
        float ny = (bz - az) * (cx - ax) - (bx - ax) * (cz - az);
        float nz = (bx - ax) * (cy - ay) - (by - ay) * (cx - ax);
        float length = (float)Math.sqrt(nx * nx + ny * ny + nz * nz);
        float light = length < 1.0E-7f ? 0.8f : 0.63f + 0.27f * Math.abs(ny / length) + 0.1f * Math.abs(nx / length);
        int shade = color & 0xFF000000 | (int)((float)(color >> 16 & 0xFF) * light) << 16 | (int)((float)(color >> 8 & 0xFF) * light) << 8 | (int)((float)(color & 0xFF) * light);
        v.method_22918((Matrix4fc)m, ax, ay, az).method_39415(shade);
        v.method_22918((Matrix4fc)m, bx, by, bz).method_39415(shade);
        v.method_22918((Matrix4fc)m, cx, cy, cz).method_39415(shade);
        v.method_22918((Matrix4fc)m, ax, ay, az).method_39415(shade);
        v.method_22918((Matrix4fc)m, cx, cy, cz).method_39415(shade);
        v.method_22918((Matrix4fc)m, dx, dy, dz).method_39415(shade);
    }
}

