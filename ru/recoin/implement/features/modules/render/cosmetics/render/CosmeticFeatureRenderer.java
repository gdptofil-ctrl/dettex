/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10055
 *  net.minecraft.class_11659
 *  net.minecraft.class_12249
 *  net.minecraft.class_1921
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3883
 *  net.minecraft.class_3887
 *  net.minecraft.class_4587
 *  net.minecraft.class_4587$class_4665
 *  net.minecraft.class_4588
 *  net.minecraft.class_591
 *  net.minecraft.class_742
 *  net.minecraft.class_746
 *  org.joml.Matrix3fc
 *  org.joml.Matrix4fc
 */
package ru.recoin.implement.features.modules.render.cosmetics.render;

import net.minecraft.class_10055;
import net.minecraft.class_11659;
import net.minecraft.class_12249;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_591;
import net.minecraft.class_742;
import net.minecraft.class_746;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import ru.recoin.implement.features.modules.render.cosmetics.Cosmetics;
import ru.recoin.implement.features.modules.render.cosmetics.model.CosmeticModel;
import ru.recoin.implement.features.modules.render.cosmetics.render.CosmeticRenderer;

public class CosmeticFeatureRenderer
extends class_3887<class_10055, class_591> {
    public CosmeticFeatureRenderer(class_3883<class_10055, class_591> context) {
        super(context);
    }

    @Override
    public void method_4199(class_4587 matrices, class_11659 commandQueue, int light, class_10055 state, float limbAngle, float tickDelta) {
        CosmeticModel backpackModel;
        class_1921 layer;
        CosmeticModel wingModel;
        boolean isSelf;
        class_310 client = class_310.method_1551();
        class_746 player = client.field_1724;
        if (player == null || commandQueue == null || state == null || state.field_53542) {
            return;
        }
        Cosmetics cosmetics = Cosmetics.getInstance();
        if (cosmetics == null || !cosmetics.isState()) {
            return;
        }
        boolean bl = isSelf = state.field_53528 == player.method_5628();
        if (isSelf && client.field_1690.method_31044().method_31034()) {
            return;
        }
        if (cosmetics.getWingsGroup().isValue() && isSelf && (wingModel = cosmetics.getActiveWingModel()) != null && wingModel.getTextureId() != null) {
            layer = class_12249.method_75994((class_2960)wingModel.getTextureId());
            commandQueue.method_73483(matrices, layer, (arg_0, arg_1) -> this.lambda$render$0(wingModel, (class_742)player, light, tickDelta, arg_0, arg_1));
        }
        if (cosmetics.getBackpacksGroup().isValue() && isSelf && (backpackModel = cosmetics.getActiveBackpackModel()) != null && backpackModel.getTextureId() != null) {
            layer = class_12249.method_75994((class_2960)backpackModel.getTextureId());
            commandQueue.method_73483(matrices, layer, (arg_0, arg_1) -> this.lambda$render$1(backpackModel, (class_742)player, light, tickDelta, arg_0, arg_1));
        }
    }

    private /* synthetic */ void lambda$render$1(CosmeticModel backpackModel, class_742 player, int light, float tickDelta, class_4587.class_4665 entry, class_4588 vertexConsumer) {
        class_4587 localMatrices = new class_4587();
        localMatrices.method_23760().method_23761().set((Matrix4fc)entry.method_23761());
        localMatrices.method_23760().method_23762().set((Matrix3fc)entry.method_23762());
        CosmeticRenderer.getInstance().renderCosmetic(backpackModel, player, localMatrices, vertexConsumer, light, (class_591)this.method_17165(), tickDelta);
    }

    private /* synthetic */ void lambda$render$0(CosmeticModel wingModel, class_742 player, int light, float tickDelta, class_4587.class_4665 entry, class_4588 vertexConsumer) {
        class_4587 localMatrices = new class_4587();
        localMatrices.method_23760().method_23761().set((Matrix4fc)entry.method_23761());
        localMatrices.method_23760().method_23762().set((Matrix3fc)entry.method_23762());
        CosmeticRenderer.getInstance().renderCosmetic(wingModel, player, localMatrices, vertexConsumer, light, (class_591)this.method_17165(), tickDelta);
    }
}

