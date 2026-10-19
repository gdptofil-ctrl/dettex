/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_11659
 *  net.minecraft.class_1297
 *  net.minecraft.class_1304
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_1799
 *  net.minecraft.class_1921
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  net.minecraft.class_3532
 *  net.minecraft.class_3882
 *  net.minecraft.class_4587
 *  net.minecraft.class_4587$class_4665
 *  net.minecraft.class_4588
 *  net.minecraft.class_4597
 *  net.minecraft.class_481
 *  net.minecraft.class_490
 *  net.minecraft.class_5498
 *  net.minecraft.class_630
 *  net.minecraft.class_7833
 *  org.joml.Matrix3fc
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.lwjgl.opengl.GL11
 */
package ru.recoin.common.util.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.class_11659;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1921;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_3532;
import net.minecraft.class_3882;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_481;
import net.minecraft.class_490;
import net.minecraft.class_5498;
import net.minecraft.class_630;
import net.minecraft.class_7833;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.render.LegacyRenderSystem;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.compat.render.BufferRenderer;
import ru.recoin.compat.render.GlStateManager;
import ru.recoin.compat.render.ShaderProgramKeys;
import ru.recoin.implement.features.modules.render.ChinaHat;

public final class ChinaHatRenderer
implements QuickImports {
    private static final int STEP = 9;
    private static final float CENTER_Y = 0.22f;
    private static final float BRIM_Y = 0.0f;
    private static final float RADIUS = 0.65f;
    private static final float CENTER_ALPHA = 1.0f;
    private static final float EDGE_ALPHA = 0.4f;
    public static final class_1921 HAT_LAYER = BufferRenderer.translucentPositionColorLayer("china_hat", VertexFormat.class_5596.field_27379, 1536);
    public static final class_1921 HAT_OUTLINE_LAYER = BufferRenderer.translucentPositionColorLayer("china_hat_outline", VertexFormat.class_5596.field_27377, 512);
    private static final int STEPS_COUNT = 40;
    private static final float[] PRECOMPUTED_X = new float[41];
    private static final float[] PRECOMPUTED_Z = new float[41];

    public static void render(class_1309 entity, class_4587 matrices, class_4597 vertexConsumers, class_3882 modelWithHead) {
        boolean shouldRender;
        class_1657 player;
        ChinaHat module = ChinaHat.getInstance();
        if (!module.isState() || !(entity instanceof class_1657) || !(player = (class_1657)entity).method_5805() || player.method_5767()) {
            return;
        }
        boolean bl = shouldRender = player == ChinaHatRenderer.mc.field_1724 || module.getFriends().isValue() && FriendUtils.isFriend((class_1297)player);
        if (!shouldRender) {
            return;
        }
        if (player == ChinaHatRenderer.mc.field_1724 && ChinaHatRenderer.mc.field_1690.method_31044() == class_5498.field_26664) {
            return;
        }
        matrices.method_22903();
        class_630 head = modelWithHead.method_2838();
        class_1799 headItem = player.method_6118(class_1304.field_6169);
        matrices.method_46416(0.0f, player.method_17682() + (headItem.method_7960() ? 0.08f : 0.16f), 0.0f);
        ChinaHatRenderer.applyHeadRotation(matrices, head);
        ChinaHatRenderer.renderHatGeometry(matrices, vertexConsumers);
        matrices.method_22909();
    }

    public static void renderHeadLayer(class_1309 entity, class_4587 matrices, class_11659 queue, class_3882 modelWithHead) {
        boolean shouldRender;
        class_1657 player;
        ChinaHat module = ChinaHat.getInstance();
        if (!module.isState() || !(entity instanceof class_1657) || !(player = (class_1657)entity).method_5805() || player.method_5767()) {
            return;
        }
        boolean bl = shouldRender = player == ChinaHatRenderer.mc.field_1724 || module.getFriends().isValue() && FriendUtils.isFriend((class_1297)player);
        if (!shouldRender) {
            return;
        }
        if (player == ChinaHatRenderer.mc.field_1724 && ChinaHatRenderer.mc.field_1690.method_31044() == class_5498.field_26664) {
            return;
        }
        matrices.method_22903();
        class_630 head = modelWithHead.method_2838();
        class_1799 headItem = player.method_6118(class_1304.field_6169);
        matrices.method_46416(0.0f, player.method_17682() + (headItem.method_7960() ? 0.08f : 0.16f), 0.0f);
        ChinaHatRenderer.applyHeadRotation(matrices, head);
        class_4587 hatMatrices = new class_4587();
        hatMatrices.method_23760().method_23761().set((Matrix4fc)matrices.method_23760().method_23761());
        hatMatrices.method_23760().method_23762().set((Matrix3fc)matrices.method_23760().method_23762());
        queue.method_73483(hatMatrices, HAT_LAYER, (entry, vertexConsumer) -> ChinaHatRenderer.renderHatGeometry(entry, vertexConsumer));
        queue.method_73483(hatMatrices, HAT_OUTLINE_LAYER, (entry, vertexConsumer) -> ChinaHatRenderer.renderHatOutline(entry, vertexConsumer));
        matrices.method_22909();
    }

    private static void renderHatGeometry(class_4587 matrices, class_4597 vertexConsumers) {
        ChinaHatRenderer.renderHatGeometry(matrices.method_23760(), vertexConsumers.method_73477(HAT_LAYER));
        ChinaHatRenderer.renderHatOutline(matrices);
    }

    private static void renderHatGeometry(class_4587.class_4665 entry, class_4588 vertexConsumer) {
        Matrix4f matrix = entry.method_23761();
        int clientColor = ColorUtil.getClientColor();
        int edgeColor = ChinaHatRenderer.withAlpha(clientColor, 0.4f);
        int centerColor = ChinaHatRenderer.withAlpha(clientColor, 1.0f);
        for (int i = 0; i < 40; ++i) {
            float x1 = PRECOMPUTED_X[i];
            float z1 = PRECOMPUTED_Z[i];
            float x2 = PRECOMPUTED_X[i + 1];
            float z2 = PRECOMPUTED_Z[i + 1];
            vertexConsumer.method_22918((Matrix4fc)matrix, 0.0f, 0.22f, 0.0f).method_39415(centerColor);
            vertexConsumer.method_22918((Matrix4fc)matrix, x1, 0.0f, z1).method_39415(edgeColor);
            vertexConsumer.method_22918((Matrix4fc)matrix, x2, 0.0f, z2).method_39415(edgeColor);
        }
        int darkEdgeColor = ChinaHatRenderer.withAlpha(ColorUtil.multDark(clientColor, 0.72f), 0.4f);
        int darkCenterColor = ChinaHatRenderer.withAlpha(ColorUtil.multDark(clientColor, 0.82f), 1.0f);
        for (int i = 0; i < 40; ++i) {
            float x1 = PRECOMPUTED_X[i];
            float z1 = PRECOMPUTED_Z[i];
            float x2 = PRECOMPUTED_X[i + 1];
            float z2 = PRECOMPUTED_Z[i + 1];
            vertexConsumer.method_22918((Matrix4fc)matrix, x2, 0.0f, z2).method_39415(darkEdgeColor);
            vertexConsumer.method_22918((Matrix4fc)matrix, x1, 0.0f, z1).method_39415(darkEdgeColor);
            vertexConsumer.method_22918((Matrix4fc)matrix, 0.0f, 0.22f, 0.0f).method_39415(darkCenterColor);
        }
    }

    private static void applyHeadRotation(class_4587 matrices, class_630 head) {
        matrices.method_22907((Quaternionfc)class_7833.field_40716.rotation(head.field_3675));
        matrices.method_22907((Quaternionfc)class_7833.field_40714.rotation(head.field_3654));
        matrices.method_22907((Quaternionfc)class_7833.field_40718.rotation(head.field_3674));
    }

    private static void renderHatOutline(class_4587 matrices) {
        if (ChinaHatRenderer.mc.field_1755 instanceof class_481 || ChinaHatRenderer.mc.field_1755 instanceof class_490) {
            return;
        }
        GL11.glHint((int)3154, (int)4354);
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.disableCull();
        LegacyRenderSystem.enableDepthTest();
        LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        LegacyRenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);
        LegacyRenderSystem.lineWidth(1.2f);
        class_287 buffer = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27377, class_290.field_1576);
        class_4587.class_4665 entry = matrices.method_23760();
        int color = ColorUtil.multDark(ColorUtil.getClientColor(), 0.9f) | 0xFF000000;
        for (int i = 0; i < 40; ++i) {
            float x1 = PRECOMPUTED_X[i];
            float z1 = PRECOMPUTED_Z[i];
            float x2 = PRECOMPUTED_X[i + 1];
            float z2 = PRECOMPUTED_Z[i + 1];
            Vector3f start = new Vector3f(x1, 0.0f, z1);
            Vector3f end = new Vector3f(x2, 0.0f, z2);
            Render3DUtil.vertexLine(entry, (class_4588)buffer, start, end, color, color);
        }
        BufferRenderer.drawWithGlobalProgram(buffer.method_60800());
        LegacyRenderSystem.lineWidth(1.0f);
        LegacyRenderSystem.enableCull();
        LegacyRenderSystem.disableBlend();
    }

    private static void renderHatOutline(class_4587.class_4665 entry, class_4588 vertexConsumer) {
        Matrix4f matrix = entry.method_23761();
        int color = ColorUtil.multDark(ColorUtil.getClientColor(), 0.9f) | 0xFF000000;
        for (int i = 0; i < 40; ++i) {
            float x1 = PRECOMPUTED_X[i];
            float z1 = PRECOMPUTED_Z[i];
            float x2 = PRECOMPUTED_X[i + 1];
            float z2 = PRECOMPUTED_Z[i + 1];
            vertexConsumer.method_22918((Matrix4fc)matrix, x1, 0.002f, z1).method_39415(color);
            vertexConsumer.method_22918((Matrix4fc)matrix, x2, 0.002f, z2).method_39415(color);
        }
    }

    private static void renderHatDebugLines(class_4587.class_4665 entry, class_4588 vertexConsumer) {
        Matrix4f matrix = entry.method_23761();
        int color = -49088;
        for (int i = 0; i < 360; i += 18) {
            float x = class_3532.method_15374((double)((float)i * ((float)Math.PI / 180))) * 0.65f;
            float z = -class_3532.method_15362((double)((float)i * ((float)Math.PI / 180))) * 0.65f;
            vertexConsumer.method_22918((Matrix4fc)matrix, 0.0f, 0.22f, 0.0f).method_39415(color);
            vertexConsumer.method_22918((Matrix4fc)matrix, x, 0.0f, z).method_39415(color);
        }
        vertexConsumer.method_22918((Matrix4fc)matrix, -0.65f, 0.01f, 0.0f).method_39415(color);
        vertexConsumer.method_22918((Matrix4fc)matrix, 0.65f, 0.01f, 0.0f).method_39415(color);
        vertexConsumer.method_22918((Matrix4fc)matrix, 0.0f, 0.01f, -0.65f).method_39415(color);
        vertexConsumer.method_22918((Matrix4fc)matrix, 0.0f, 0.01f, 0.65f).method_39415(color);
    }

    private static int withAlpha(int color, float alpha) {
        return color & 0xFFFFFF | (int)(class_3532.method_15363((float)alpha, (float)0.0f, (float)1.0f) * 255.0f) << 24;
    }

    private ChinaHatRenderer() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    static {
        for (int i = 0; i <= 40; ++i) {
            float angle = (float)(i * 9) * ((float)Math.PI / 180);
            ChinaHatRenderer.PRECOMPUTED_X[i] = class_3532.method_15374((double)angle) * 0.65f;
            ChinaHatRenderer.PRECOMPUTED_Z[i] = -class_3532.method_15362((double)angle) * 0.65f;
        }
    }
}

