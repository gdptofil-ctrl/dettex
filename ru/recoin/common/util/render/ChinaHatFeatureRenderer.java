/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_10055
 *  net.minecraft.class_10192
 *  net.minecraft.class_11659
 *  net.minecraft.class_1297
 *  net.minecraft.class_1304
 *  net.minecraft.class_1657
 *  net.minecraft.class_1921
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
 *  net.minecraft.class_9334
 *  org.joml.Matrix3fc
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionfc
 */
package ru.recoin.common.util.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_10055;
import net.minecraft.class_10192;
import net.minecraft.class_11659;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_1921;
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
import net.minecraft.class_9334;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.compat.render.BufferRenderer;
import ru.recoin.implement.features.modules.render.ChinaHat;
import ru.recoin.implement.features.modules.render.Hud;

public class ChinaHatFeatureRenderer
extends class_3887<class_10055, class_591> {
    private static final int STEP = 2;
    private static final float CENTER_Y = 0.3f;
    private static final float BRIM_Y = 0.0f;
    private static final float DEFAULT_RADIUS = 0.6f;
    private static final float OUTLINE_HALF_WIDTH = 0.0015f;
    private static final int FILL_EDGE_ALPHA = 190;
    private static final class_1921 HAT_LAYER = BufferRenderer.translucentPositionColorDepthLayer("china_hat", VertexFormat.class_5596.field_27380, 4096);
    private static final class_1921 HAT_OUTLINE_LAYER = BufferRenderer.translucentPositionColorDepthReadLayer("china_hat_outline", VertexFormat.class_5596.field_27380, 4096);
    private static final List<PendingHat> PENDING_HATS = new ArrayList<PendingHat>();

    public ChinaHatFeatureRenderer(class_3883<class_10055, class_591> context) {
        super(context);
    }

    @Override
    public void method_4199(class_4587 matrices, class_11659 queue, int light, class_10055 state, float limbAngle, float limbDistance) {
        class_1657 player;
        class_310 client = class_310.method_1551();
        ChinaHat chinaHat = ChinaHat.getInstance();
        if (chinaHat == null || !chinaHat.isState() || client.field_1724 == null) {
            return;
        }
        if (!this.shouldRender(state, client, chinaHat)) {
            return;
        }
        matrices.method_22903();
        ((class_591)this.method_17165()).field_3398.method_22703(matrices);
        class_1297 trackedEntity = client.field_1687 == null ? null : client.field_1687.method_8469(state.field_53528);
        class_1657 trackedPlayer = trackedEntity instanceof class_1657 ? (player = (class_1657)trackedEntity) : null;
        class_10192 headEquipment = trackedPlayer == null ? null : (class_10192)trackedPlayer.method_6118(class_1304.field_6169).method_58694(class_9334.field_54196);
        boolean hasHelmet = headEquipment != null && headEquipment.comp_3176().isPresent();
        boolean hasHatLayer = state.field_53543;
        float radius = trackedPlayer == null ? 0.6f : (float)trackedPlayer.method_5829().method_17939();
        matrices.method_46416(0.0f, -0.38f - (hasHelmet ? 0.11f : 0.0f) - (hasHatLayer ? 0.025f : 0.0f), 0.0f);
        matrices.method_22907((Quaternionfc)class_7833.field_40714.rotationDegrees(180.0f));
        matrices.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees(90.0f));
        class_4587 hatMatrices = new class_4587();
        hatMatrices.method_23760().method_23761().set((Matrix4fc)matrices.method_23760().method_23761());
        hatMatrices.method_23760().method_23762().set((Matrix3fc)matrices.method_23760().method_23762());
        if (state.field_53528 == client.field_1724.method_5628() && (client.field_1755 instanceof class_490 || client.field_1755 instanceof class_481)) {
            queue.method_73483(hatMatrices, HAT_LAYER, (entry, consumer) -> ChinaHatFeatureRenderer.renderHatGeometry(entry, consumer, radius));
            queue.method_73483(hatMatrices, HAT_OUTLINE_LAYER, (entry, consumer) -> ChinaHatFeatureRenderer.renderOutline(entry, consumer, radius));
        } else {
            PENDING_HATS.add(new PendingHat(hatMatrices.method_23760().method_56822(), radius));
        }
        matrices.method_22909();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void renderPending() {
        try {
            for (PendingHat hat : PENDING_HATS) {
                class_287 fill = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27380, class_290.field_1576);
                ChinaHatFeatureRenderer.renderHatGeometry(hat.entry(), (class_4588)fill, hat.radius());
                HAT_LAYER.method_60895(fill.method_60800());
                class_287 outline = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27380, class_290.field_1576);
                ChinaHatFeatureRenderer.renderOutline(hat.entry(), (class_4588)outline, hat.radius());
                HAT_OUTLINE_LAYER.method_60895(outline.method_60800());
            }
        }
        finally {
            PENDING_HATS.clear();
        }
    }

    private boolean shouldRender(class_10055 state, class_310 client, ChinaHat chinaHat) {
        class_1297 trackedEntity;
        if (state.field_53528 == client.field_1724.method_5628()) {
            return !client.field_1690.method_31044().method_31034() || client.field_1755 instanceof class_490 || client.field_1755 instanceof class_481;
        }
        if (!chinaHat.getFriends().isValue()) {
            return false;
        }
        class_1297 class_12972 = trackedEntity = client.field_1687 == null ? null : client.field_1687.method_8469(state.field_53528);
        if (trackedEntity != null && FriendUtils.isFriend(trackedEntity)) {
            return true;
        }
        String playerName = state.field_53525 == null ? null : state.field_53525.getString();
        return playerName != null && FriendUtils.isFriend(playerName);
    }

    private static void renderHatGeometry(class_4587.class_4665 entry, class_4588 consumer, float radius) {
        Matrix4f matrix = entry.method_23761();
        int centerColor = ChinaHatFeatureRenderer.themeColor(0, 255);
        for (int segment = 0; segment <= 180; ++segment) {
            float angle = (float)(segment * 2) * ((float)Math.PI / 180);
            float x = class_3532.method_15374((double)angle) * radius;
            float z = class_3532.method_15362((double)angle) * radius;
            int edgeColor = ChinaHatFeatureRenderer.themeColor(segment * 4, 190);
            consumer.method_22918((Matrix4fc)matrix, x, 0.0f, z).method_39415(edgeColor);
            consumer.method_22918((Matrix4fc)matrix, 0.0f, 0.3f, 0.0f).method_39415(centerColor);
        }
    }

    private static void renderOutline(class_4587.class_4665 entry, class_4588 consumer, float radius) {
        Matrix4f matrix = entry.method_23761();
        float innerRadius = radius - 0.0015f;
        float outerRadius = radius + 0.0015f;
        for (int segment = 0; segment <= 180; ++segment) {
            float angle = (float)(segment * 2) * ((float)Math.PI / 180);
            float sin = class_3532.method_15374((double)angle);
            float cos = class_3532.method_15362((double)angle);
            int color = ChinaHatFeatureRenderer.themeColor(segment * 4, 255);
            consumer.method_22918((Matrix4fc)matrix, sin * innerRadius, 0.0f, cos * innerRadius).method_39415(color);
            consumer.method_22918((Matrix4fc)matrix, sin * outerRadius, 0.0f, cos * outerRadius).method_39415(color);
        }
    }

    private static int themeColor(int offset, int alpha) {
        return ColorUtil.replAlpha(Hud.getInstance().themeFadeColor(7, offset), alpha);
    }

    private record PendingHat(class_4587.class_4665 entry, float radius) {
    }
}

