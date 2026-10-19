/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11659
 *  net.minecraft.class_12075
 *  net.minecraft.class_12249
 *  net.minecraft.class_1921
 *  net.minecraft.class_2246
 *  net.minecraft.class_2960
 *  net.minecraft.class_4587
 *  net.minecraft.class_4608
 *  net.minecraft.class_5617$class_5618
 *  net.minecraft.class_7833
 *  org.joml.Quaternionfc
 */
package ru.recoin.implement.features.modules.render.custompet.render;

import net.minecraft.class_11659;
import net.minecraft.class_12075;
import net.minecraft.class_12249;
import net.minecraft.class_1921;
import net.minecraft.class_2246;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4608;
import net.minecraft.class_5617;
import net.minecraft.class_7833;
import org.joml.Quaternionfc;
import ru.recoin.api.mods.geckolib.renderer.GeoEntityRenderer;
import ru.recoin.api.mods.geckolib.renderer.base.BoneSnapshots;
import ru.recoin.api.mods.geckolib.renderer.base.RenderPassInfo;
import ru.recoin.implement.features.modules.render.custompet.CustomPetVariant;
import ru.recoin.implement.features.modules.render.custompet.entity.CustomPetEntity;
import ru.recoin.implement.features.modules.render.custompet.model.CustomPetModel;
import ru.recoin.implement.features.modules.render.custompet.render.CustomPetRenderState;

public class CustomPetRenderer
extends GeoEntityRenderer<CustomPetEntity, CustomPetRenderState> {
    private static final float LILY_PAD_SCALE = 1.35f;
    private static final float LILY_PAD_Y_OFFSET = -0.008f;
    private static final float OWL_SCALE = 0.55f;
    private static final String[] OWL_EFFECT_BONES = new String[]{"trick_star", "stumble_star_left", "stumble_star_right", "celebrate_confetti_1", "celebrate_confetti_2", "celebrate_confetti_3", "signature"};
    private static final String[] OWL_GROUND_ROPE_BONES = new String[]{"rope_spin", "left_handle", "right_handle"};
    private static final String[] OWL_FLIGHT_ROPE_BONES = new String[]{"fly_rope_left", "fly_rope_right"};
    private static final String[] CONDITIONAL_BONES = new String[]{"body_default", "body_merchant", "leaf", "gardener_hat", "watering_can", "sourcerer_hat", "accessories", "fishing_rod", "fishing_rod_2", "fishing_rod_3", "guitar", "flute", "bongo", "bass", "umbrella", "umbrella2", "umbrella3", "fisherman_umbrella", "fisherman_umbrella2", "fisherman_umbrella3", "pride"};

    public CustomPetRenderer(class_5617.class_5618 context) {
        super(context, new CustomPetModel());
        this.field_4673 = 0.35f;
    }

    @Override
    protected void applyRotations(RenderPassInfo<CustomPetRenderState> renderPassInfo, class_4587 matrixStack, float f) {
        CustomPetRenderState state = renderPassInfo.renderState();
        float yaw = state.field_53446;
        if (state.field_53456) {
            yaw += (float)(Math.cos((double)state.field_53328 * 3.25) * Math.PI * 0.4);
        }
        matrixStack.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees(180.0f - yaw));
    }

    public void submit(CustomPetRenderState r, class_4587 matrixStack, class_11659 orderedRenderCommandQueue, class_12075 cameraRenderState) {
        super.method_3936(r, matrixStack, orderedRenderCommandQueue, cameraRenderState);
        if (!Boolean.TRUE.equals(r.getOrDefaultGeckolibData(CustomPetModel.AIRBORNE, false))) {
            return;
        }
        if (Boolean.TRUE.equals(r.getOrDefaultGeckolibData(CustomPetModel.OWL, false))) {
            return;
        }
        String string = r.getOrDefaultGeckolibData(CustomPetModel.VARIANT, CustomPetVariant.NITWIT.name());
        CustomPetVariant customPetVariant = CustomPetVariant.fromSerializedName(string);
        if (customPetVariant.isRobot()) {
            return;
        }
        matrixStack.method_22903();
        matrixStack.method_46416(-0.675f, -0.008f, -0.675f);
        matrixStack.method_22905(1.35f, 1.0f, 1.35f);
        orderedRenderCommandQueue.method_73481(matrixStack, class_2246.field_10588.method_9564(), r.field_61820, class_4608.field_21444, r.field_61821);
        matrixStack.method_22909();
    }

    @Override
    public class_1921 getRenderType(CustomPetRenderState r, class_2960 identifier) {
        return class_12249.method_75990((class_2960)identifier);
    }

    @Override
    public float getMotionAnimThreshold(CustomPetEntity customPetEntity) {
        return 5.0E-4f;
    }

    @Override
    public void scaleModelForRender(RenderPassInfo<CustomPetRenderState> renderPassInfo, float f, float f2) {
        if (Boolean.TRUE.equals(renderPassInfo.getOrDefaultGeckolibData(CustomPetModel.OWL, false))) {
            f *= 0.55f;
            f2 *= 0.55f;
        }
        super.scaleModelForRender(renderPassInfo, f, f2);
    }

    private static void adjustOwlBones(BoneSnapshots boneSnapshots, boolean bl) {
        for (String string : OWL_EFFECT_BONES) {
            boneSnapshots.ifPresent(string, boneSnapshot -> boneSnapshot.skipRender(true).skipChildrenRender(true));
        }
        for (String string : OWL_GROUND_ROPE_BONES) {
            boneSnapshots.ifPresent(string, boneSnapshot -> boneSnapshot.skipRender(bl).skipChildrenRender(bl));
        }
        for (String string : OWL_FLIGHT_ROPE_BONES) {
            boneSnapshots.ifPresent(string, boneSnapshot -> boneSnapshot.skipRender(!bl).skipChildrenRender(!bl));
        }
    }

    private static boolean isBoneVisible(String string, CustomPetVariant customPetVariant, boolean bl, boolean bl2) {
        return switch (string) {
            case "body_merchant" -> customPetVariant.usesMerchantBody();
            case "body_default" -> {
                if (!customPetVariant.usesMerchantBody()) {
                    yield true;
                }
                yield false;
            }
            case "leaf" -> {
                if (customPetVariant.usesMerchantLeaf() && !bl) {
                    yield true;
                }
                yield false;
            }
            case "gardener_hat", "watering_can" -> customPetVariant.usesGardenerGear();
            case "sourcerer_hat" -> customPetVariant.usesSorcererHat();
            case "accessories", "fishing_rod", "fishing_rod_2", "fishing_rod_3" -> customPetVariant.usesFishermanGear();
            case "umbrella2" -> {
                if (bl2 && !customPetVariant.usesFishermanGear()) {
                    yield true;
                }
                yield false;
            }
            case "fisherman_umbrella2" -> {
                if (bl2 && customPetVariant.usesFishermanGear()) {
                    yield true;
                }
                yield false;
            }
            case "umbrella3" -> {
                if (!bl2 && bl && !customPetVariant.usesFishermanGear()) {
                    yield true;
                }
                yield false;
            }
            case "fisherman_umbrella3" -> {
                if (!bl2 && bl && customPetVariant.usesFishermanGear()) {
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<CustomPetRenderState> renderPassInfo, BoneSnapshots boneSnapshots) {
        boolean bl = Boolean.TRUE.equals(renderPassInfo.getOrDefaultGeckolibData(CustomPetModel.AIRBORNE, false));
        if (Boolean.TRUE.equals(renderPassInfo.getOrDefaultGeckolibData(CustomPetModel.OWL, false))) {
            CustomPetRenderer.adjustOwlBones(boneSnapshots, bl);
            return;
        }
        String string = renderPassInfo.getOrDefaultGeckolibData(CustomPetModel.VARIANT, CustomPetVariant.NITWIT.name());
        CustomPetVariant customPetVariant = CustomPetVariant.fromSerializedName(string);
        boolean bl2 = Boolean.TRUE.equals(renderPassInfo.getOrDefaultGeckolibData(CustomPetModel.UMBRELLA, false));
        for (String string2 : CONDITIONAL_BONES) {
            boolean bl3 = !CustomPetRenderer.isBoneVisible(string2, customPetVariant, bl2, bl);
            boneSnapshots.ifPresent(string2, boneSnapshot -> boneSnapshot.skipRender(bl3).skipChildrenRender(bl3));
        }
    }

    public CustomPetRenderState createRenderState() {
        return new CustomPetRenderState();
    }

    @Override
    public CustomPetRenderState createRenderState(CustomPetEntity customPetEntity, Void void_) {
        return new CustomPetRenderState();
    }
}

