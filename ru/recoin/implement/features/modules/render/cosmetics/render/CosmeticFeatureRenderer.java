package ru.recoin.implement.features.modules.render.cosmetics.render;

import java.util.List;
import java.util.UUID;
import net.minecraft.class_10055;
import net.minecraft.class_11659;
import net.minecraft.class_12249;
import net.minecraft.class_1297;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_591;
import net.minecraft.class_7833;
import net.minecraft.class_746;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import ru.recoin.implement.features.modules.render.Holiday;
import ru.recoin.implement.features.modules.render.cosmetics.Cosmetics;
import ru.recoin.implement.features.modules.render.cosmetics.model.CosmeticModel;
import ru.recoin.implement.features.modules.render.cosmetics.model.ModelPosition;

/**
 * Draws wings, backpacks, hats and halos. The attachment transform (body/head part, offsets)
 * is applied before the command is queued, so each player uses his own pose even though the
 * queued geometry is drawn later.
 */
public class CosmeticFeatureRenderer
extends class_3887<class_10055, class_591> {
    public CosmeticFeatureRenderer(class_3883<class_10055, class_591> context) {
        super(context);
    }

    @Override
    public void method_4199(class_4587 matrices, class_11659 commandQueue, int light, class_10055 state, float limbAngle, float tickDelta) {
        class_310 client = class_310.method_1551();
        class_746 player = client.field_1724;
        if (player == null || commandQueue == null || state == null || state.field_53542 || state.field_53333) {
            return;
        }
        Cosmetics cosmetics = Cosmetics.getInstance();
        String holidayHat = Holiday.hatName(state.field_53332);
        if (cosmetics == null || !cosmetics.isState() && holidayHat == null) {
            return;
        }
        boolean isSelf = state.field_53528 == player.method_5628();
        if (isSelf && client.field_1690.method_31044().method_31034()) {
            return;
        }
        String name = state.field_53525 == null ? null : state.field_53525.getString();
        class_1297 entity = isSelf || client.field_1687 == null ? null : client.field_1687.method_8469(state.field_53528);
        UUID uuid = isSelf ? player.method_5667() : entity == null ? null : entity.method_5667();
        List<CosmeticModel> accessories = cosmetics.isState() ? cosmetics.accessoriesFor(isSelf, uuid, name, state.field_53332) : List.of();
        if (holidayHat != null && accessories.stream().noneMatch(Cosmetics::isHatModel)) {
            CosmeticModel hat = cosmetics.hatModel(holidayHat);
            if (hat != null) {
                accessories = new java.util.ArrayList<CosmeticModel>(accessories);
                accessories.add(hat);
            }
        }
        if (accessories.isEmpty()) {
            return;
        }
        class_591 model = (class_591)this.method_17165();
        for (CosmeticModel cosmetic : accessories) {
            if (cosmetic == null || cosmetic.getTextureId() == null) continue;
            matrices.method_22903();
            CosmeticFeatureRenderer.applyAttachment(cosmetic, model, matrices);
            class_1921 layer = class_12249.method_75994((class_2960)cosmetic.getTextureId());
            commandQueue.method_73483(matrices, layer, (entry, vertexConsumer) -> {
                class_4587 localMatrices = new class_4587();
                localMatrices.method_23760().method_23761().set((Matrix4fc)entry.method_23761());
                localMatrices.method_23760().method_23762().set((Matrix3fc)entry.method_23762());
                GeckolibCosmeticRenderer.getInstance().renderCosmetic(cosmetic, localMatrices, vertexConsumer, light);
            });
            matrices.method_22909();
        }
    }

    /** Same placement as CosmeticRenderer.renderCosmetic, applied directly to the given matrices. */
    public static void applyAttachment(CosmeticModel cosmetic, class_591 model, class_4587 matrices) {
        float yOffset = 0.0f;
        ModelPosition pos = cosmetic.getPosition();
        if (model != null) {
            switch (pos) {
                case HEAD: {
                    model.field_3398.method_22703(matrices);
                    yOffset = 0.5f;
                    break;
                }
                case ABOVE_HEAD: {
                    yOffset = 0.75f;
                    break;
                }
                case BODY: {
                    model.field_3391.method_22703(matrices);
                    yOffset = -0.3f;
                    break;
                }
                case RIGHT_ARM: {
                    model.field_3401.method_22703(matrices);
                    yOffset = -0.25f;
                    break;
                }
                case LEFT_ARM: {
                    model.field_27433.method_22703(matrices);
                    yOffset = -0.25f;
                    break;
                }
                case RIGHT_LEG: {
                    model.field_3392.method_22703(matrices);
                    yOffset = -0.35f;
                    break;
                }
                case LEFT_LEG: {
                    model.field_3397.method_22703(matrices);
                    yOffset = -0.35f;
                    break;
                }
                default: {
                    break;
                }
            }
        }
        matrices.method_22907((Quaternionfc)class_7833.field_40718.rotationDegrees(180.0f));
        matrices.method_46416(cosmetic.getX(), cosmetic.getY() + yOffset, cosmetic.getZ());
        if (cosmetic.getYaw() != 0.0f) {
            matrices.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees(cosmetic.getYaw()));
        }
        if (cosmetic.getPitch() != 0.0f) {
            matrices.method_22907((Quaternionfc)class_7833.field_40714.rotationDegrees(cosmetic.getPitch()));
        }
        if (cosmetic.getRoll() != 0.0f) {
            matrices.method_22907((Quaternionfc)class_7833.field_40718.rotationDegrees(cosmetic.getRoll()));
        }
        matrices.method_22905(cosmetic.getScale(), cosmetic.getScale(), cosmetic.getScale());
    }
}
