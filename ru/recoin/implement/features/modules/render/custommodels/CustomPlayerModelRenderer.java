/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10055
 *  net.minecraft.class_1058
 *  net.minecraft.class_11659
 *  net.minecraft.class_11683$class_11792
 *  net.minecraft.class_12249
 *  net.minecraft.class_1306
 *  net.minecraft.class_1921
 *  net.minecraft.class_2960
 *  net.minecraft.class_4587
 *  net.minecraft.class_5603
 *  net.minecraft.class_5605
 *  net.minecraft.class_5606
 *  net.minecraft.class_5607
 *  net.minecraft.class_5609
 *  net.minecraft.class_5610
 *  net.minecraft.class_591
 *  net.minecraft.class_630
 */
package ru.recoin.implement.features.modules.render.custommodels;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.class_10055;
import net.minecraft.class_1058;
import net.minecraft.class_11659;
import net.minecraft.class_11683;
import net.minecraft.class_12249;
import net.minecraft.class_1306;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_5603;
import net.minecraft.class_5605;
import net.minecraft.class_5606;
import net.minecraft.class_5607;
import net.minecraft.class_5609;
import net.minecraft.class_5610;
import net.minecraft.class_591;
import net.minecraft.class_630;

public final class CustomPlayerModelRenderer {
    private static final Map<String, Supplier<CustomModel>> MODEL_FACTORIES = new HashMap<String, Supplier<CustomModel>>();
    private static final Map<String, CustomModel> MODEL_CACHE = new HashMap<String, CustomModel>();

    public static boolean render(String modelName, class_591 vanillaModel, class_10055 state, class_4587 matrices, class_11659 queue, int light, int overlay, int color, int outlineColor) {
        CustomModel customModel = CustomPlayerModelRenderer.getModel(modelName, state.field_53528);
        if (customModel == null) {
            return false;
        }
        customModel.copyAngles(vanillaModel, state);
        matrices.method_22903();
        customModel.transform(matrices, state);
        class_1921 layer = class_12249.method_76000((class_2960)customModel.texture);
        queue.method_73494(customModel.root, matrices, layer, light, overlay, (class_1058)null, false, false, color, (class_11683.class_11792)null, outlineColor);
        matrices.method_22909();
        return true;
    }

    public static boolean setArmAngle(String modelName, class_10055 state, class_1306 arm, class_4587 matrices) {
        CustomModel customModel = CustomPlayerModelRenderer.getModel(modelName, state.field_53528);
        if (customModel == null) {
            return false;
        }
        customModel.setArmAngle(state, arm, matrices);
        return true;
    }

    private static CustomModel getModel(String modelName, int entityId) {
        Supplier<CustomModel> factory = MODEL_FACTORIES.get(modelName);
        if (factory == null) {
            return null;
        }
        if (MODEL_CACHE.size() > 128) {
            MODEL_CACHE.clear();
        }
        return MODEL_CACHE.computeIfAbsent(modelName + "#" + entityId, ignored -> (CustomModel)factory.get());
    }

    private static CustomModel createRabbit() {
        class_630 root = CustomPlayerModelRenderer.createRoot(64, 64, data -> {
            class_5610 rabbit = data.method_32117("rabbit", CustomPlayerModelRenderer.box(28, 45, -5.0f, -13.0f, -5.0f, 10.0f, 11.0f, 8.0f), class_5603.method_32090((float)0.0f, (float)24.0f, (float)0.0f));
            rabbit.method_32117("right_leg", CustomPlayerModelRenderer.box(0, 32, -2.0f, 0.0f, -2.0f, 4.0f, 2.0f, 4.0f), class_5603.method_32090((float)-3.0f, (float)-2.0f, (float)-1.0f));
            rabbit.method_32117("left_leg", CustomPlayerModelRenderer.box(0, 32, -2.0f, 0.0f, -2.0f, 4.0f, 2.0f, 4.0f), class_5603.method_32090((float)3.0f, (float)-2.0f, (float)-1.0f));
            rabbit.method_32117("left_arm", CustomPlayerModelRenderer.box(24, 16, 0.0f, 0.0f, -2.0f, 2.0f, 8.0f, 4.0f), class_5603.method_32091((float)5.0f, (float)-13.0f, (float)-1.0f, (float)0.0f, (float)0.0f, (float)-0.0873f));
            rabbit.method_32117("right_arm", CustomPlayerModelRenderer.box(24, 16, -2.0f, 0.0f, -2.0f, 2.0f, 8.0f, 4.0f), class_5603.method_32091((float)-5.0f, (float)-13.0f, (float)-1.0f, (float)0.0f, (float)0.0f, (float)0.0873f));
            class_5610 head = rabbit.method_32117("head", class_5606.method_32108().method_32101(0, 0).method_32097(-3.0f, 0.0f, -4.0f, 6.0f, 1.0f, 6.0f).method_32101(0, 45).method_32097(-4.0f, -11.0f, -4.0f, 8.0f, 11.0f, 8.0f).method_32101(56, 0).method_32097(-5.0f, -9.0f, -5.0f, 2.0f, 3.0f, 2.0f).method_32101(56, 0).method_32097(3.0f, -9.0f, -5.0f, 2.0f, 3.0f, 2.0f).method_32101(46, 0).method_32097(1.0f, -20.0f, 0.0f, 3.0f, 9.0f, 1.0f).method_32101(46, 0).method_32097(-4.0f, -20.0f, 0.0f, 3.0f, 9.0f, 1.0f), class_5603.method_32090((float)0.0f, (float)-14.0f, (float)-1.0f));
            head.method_32117("nose", CustomPlayerModelRenderer.box(0, 7, -1.5f, -4.0f, -5.5f, 3.0f, 2.0f, 1.0f), class_5603.field_27701);
        });
        return new CustomModel("CrazyRabbit", class_2960.method_60655((String)"recoin", (String)"textures/models/rabbit.png"), root, root.method_32086("rabbit"), root.method_32086("rabbit").method_32086("head"), root.method_32086("rabbit"), root.method_32086("rabbit").method_32086("left_arm"), root.method_32086("rabbit").method_32086("right_arm"), root.method_32086("rabbit").method_32086("left_leg"), root.method_32086("rabbit").method_32086("right_leg")){

            @Override
            void transform(class_4587 matrices, class_10055 state) {
                matrices.method_22905(1.25f, 1.25f, 1.25f);
                matrices.method_46416(0.0f, -0.3f, 0.0f);
            }

            @Override
            void copyAngles(class_591 vanilla, class_10055 state) {
                this.copyHeadAngles(vanilla);
                this.clearAngles(this.body);
                this.copyLimbAngles(vanilla);
                this.leftArm.field_3674 -= 0.0873f;
                this.rightArm.field_3674 += 0.0873f;
            }

            @Override
            void setArmAngle(class_10055 state, class_1306 arm, class_4587 matrices) {
                this.transform(matrices, state);
                this.root.method_22703(matrices);
                this.bodyRoot.method_22703(matrices);
                class_630 armPart = arm == class_1306.field_6182 ? this.leftArm : this.rightArm;
                armPart.method_22703(matrices);
                matrices.method_46416(0.0f, -0.21875f, 0.0f);
            }
        };
    }

    private static CustomModel createFreddy() {
        class_630 root = CustomPlayerModelRenderer.createRoot(100, 80, data -> {
            class_5610 body = data.method_32117("body", CustomPlayerModelRenderer.box(0, 0, -1.0f, -14.0f, -1.0f, 2.0f, 24.0f, 2.0f), class_5603.method_32090((float)0.0f, (float)-9.0f, (float)0.0f));
            body.method_32117("torso", CustomPlayerModelRenderer.box(8, 0, -6.0f, -9.0f, -4.0f, 12.0f, 18.0f, 8.0f), class_5603.method_32092((float)((float)Math.PI / 180), (float)0.0f, (float)0.0f));
            body.method_32117("crotch", CustomPlayerModelRenderer.box(56, 0, -5.5f, 0.0f, -3.5f, 11.0f, 3.0f, 7.0f), class_5603.method_32090((float)0.0f, (float)9.5f, (float)0.0f));
            class_5610 head = body.method_32117("head", CustomPlayerModelRenderer.box(39, 22, -5.5f, -8.0f, -4.5f, 11.0f, 8.0f, 9.0f), class_5603.method_32090((float)0.0f, (float)-13.0f, (float)-0.5f));
            head.method_32117("jaw", CustomPlayerModelRenderer.box(49, 65, -5.0f, 0.0f, -4.5f, 10.0f, 3.0f, 9.0f), class_5603.method_32091((float)0.0f, (float)0.5f, (float)0.0f, (float)0.08726646f, (float)0.0f, (float)0.0f));
            head.method_32117("nose", CustomPlayerModelRenderer.box(17, 67, -4.0f, -2.0f, -3.0f, 8.0f, 4.0f, 3.0f), class_5603.method_32090((float)0.0f, (float)-2.0f, (float)-4.5f));
            class_5610 rightEar = head.method_32117("right_ear", CustomPlayerModelRenderer.box(8, 0, -1.0f, -3.0f, -0.5f, 2.0f, 3.0f, 1.0f), class_5603.method_32091((float)-4.5f, (float)-5.5f, (float)0.0f, (float)0.05235988f, (float)0.0f, (float)-1.0471976f));
            rightEar.method_32117("right_ear_pad", CustomPlayerModelRenderer.box(85, 0, -2.0f, -5.0f, -1.0f, 4.0f, 4.0f, 2.0f), class_5603.method_32090((float)0.0f, (float)-1.0f, (float)0.0f));
            class_5610 leftEar = head.method_32117("left_ear", CustomPlayerModelRenderer.box(40, 0, -1.0f, -3.0f, -0.5f, 2.0f, 3.0f, 1.0f), class_5603.method_32091((float)4.5f, (float)-5.5f, (float)0.0f, (float)0.05235988f, (float)0.0f, (float)1.0471976f));
            leftEar.method_32117("left_ear_pad", CustomPlayerModelRenderer.box(40, 39, -2.0f, -5.0f, -1.0f, 4.0f, 4.0f, 2.0f), class_5603.method_32090((float)0.0f, (float)-1.0f, (float)0.0f));
            class_5610 hat = head.method_32117("hat", CustomPlayerModelRenderer.box(70, 24, -3.0f, -0.5f, -3.0f, 6.0f, 1.0f, 6.0f), class_5603.method_32091((float)0.0f, (float)-8.4f, (float)0.0f, (float)((float)(-Math.PI) / 180), (float)0.0f, (float)0.0f));
            hat.method_32117("hat_top", CustomPlayerModelRenderer.box(78, 61, -2.0f, -4.0f, -2.0f, 4.0f, 4.0f, 4.0f), class_5603.method_32090((float)0.0f, (float)0.1f, (float)0.0f));
            class_5610 rightArm = body.method_32117("right_arm", CustomPlayerModelRenderer.box(48, 0, -1.0f, 0.0f, -1.0f, 2.0f, 10.0f, 2.0f), class_5603.method_32091((float)-6.5f, (float)-8.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.2617994f));
            rightArm.method_32117("right_arm_pad", CustomPlayerModelRenderer.box(70, 10, -2.5f, 0.0f, -2.5f, 5.0f, 9.0f, 5.0f), class_5603.method_32090((float)0.0f, (float)0.5f, (float)0.0f));
            class_5610 rightForearm = rightArm.method_32117("right_forearm", CustomPlayerModelRenderer.box(90, 20, -1.0f, 0.0f, -1.0f, 2.0f, 8.0f, 2.0f), class_5603.method_32091((float)0.0f, (float)9.6f, (float)0.0f, (float)-0.17453292f, (float)0.0f, (float)0.0f));
            rightForearm.method_32117("right_hand", CustomPlayerModelRenderer.box(20, 26, -2.0f, 0.0f, -2.5f, 4.0f, 4.0f, 5.0f), class_5603.method_32091((float)0.0f, (float)8.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.05235988f));
            class_5610 leftArm = body.method_32117("left_arm", CustomPlayerModelRenderer.box(62, 10, -1.0f, 0.0f, -1.0f, 2.0f, 10.0f, 2.0f), class_5603.method_32091((float)6.5f, (float)-8.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.2617994f));
            leftArm.method_32117("left_arm_pad", CustomPlayerModelRenderer.box(38, 54, -2.5f, 0.0f, -2.5f, 5.0f, 9.0f, 5.0f), class_5603.method_32090((float)0.0f, (float)0.5f, (float)0.0f));
            class_5610 leftForearm = leftArm.method_32117("left_forearm", CustomPlayerModelRenderer.box(90, 48, -1.0f, 0.0f, -1.0f, 2.0f, 8.0f, 2.0f), class_5603.method_32091((float)0.0f, (float)9.6f, (float)0.0f, (float)-0.17453292f, (float)0.0f, (float)0.0f));
            leftForearm.method_32117("left_hand", CustomPlayerModelRenderer.box(58, 56, -1.0f, 0.0f, -2.5f, 4.0f, 4.0f, 5.0f), class_5603.method_32091((float)0.0f, (float)8.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.05235988f));
            class_5610 rightLeg = body.method_32117("right_leg", CustomPlayerModelRenderer.box(90, 8, -1.0f, 0.0f, -1.0f, 2.0f, 10.0f, 2.0f), class_5603.method_32090((float)-3.3f, (float)12.5f, (float)0.0f));
            rightLeg.method_32117("right_leg_pad", CustomPlayerModelRenderer.box(73, 33, -3.0f, 0.0f, -3.0f, 6.0f, 9.0f, 6.0f), class_5603.method_32090((float)0.0f, (float)0.5f, (float)0.0f));
            class_5610 rightLowerLeg = rightLeg.method_32117("right_lower_leg", CustomPlayerModelRenderer.box(20, 35, -1.0f, 0.0f, -1.0f, 2.0f, 8.0f, 2.0f), class_5603.method_32091((float)0.0f, (float)9.6f, (float)0.0f, (float)((float)Math.PI / 90), (float)0.0f, (float)0.0f));
            rightLowerLeg.method_32117("right_lower_leg_pad", CustomPlayerModelRenderer.box(0, 39, -2.5f, 0.0f, -3.0f, 5.0f, 7.0f, 6.0f), class_5603.method_32090((float)0.0f, (float)0.5f, (float)0.0f));
            rightLowerLeg.method_32117("right_foot", CustomPlayerModelRenderer.box(22, 39, -2.5f, 0.0f, -6.0f, 5.0f, 3.0f, 8.0f), class_5603.method_32091((float)0.0f, (float)8.0f, (float)0.0f, (float)((float)(-Math.PI) / 90), (float)0.0f, (float)0.0f));
            class_5610 leftLeg = body.method_32117("left_leg", CustomPlayerModelRenderer.box(54, 10, -1.0f, 0.0f, -1.0f, 2.0f, 10.0f, 2.0f), class_5603.method_32090((float)3.3f, (float)12.5f, (float)0.0f));
            leftLeg.method_32117("left_leg_pad", CustomPlayerModelRenderer.box(48, 39, -3.0f, 0.0f, -3.0f, 6.0f, 9.0f, 6.0f), class_5603.method_32090((float)0.0f, (float)0.5f, (float)0.0f));
            class_5610 leftLowerLeg = leftLeg.method_32117("left_lower_leg", CustomPlayerModelRenderer.box(72, 48, -1.0f, 0.0f, -1.0f, 2.0f, 8.0f, 2.0f), class_5603.method_32091((float)0.0f, (float)9.6f, (float)0.0f, (float)((float)Math.PI / 90), (float)0.0f, (float)0.0f));
            leftLowerLeg.method_32117("left_lower_leg_pad", CustomPlayerModelRenderer.box(16, 50, -2.5f, 0.0f, -3.0f, 5.0f, 7.0f, 6.0f), class_5603.method_32090((float)0.0f, (float)0.5f, (float)0.0f));
            leftLowerLeg.method_32117("left_foot", CustomPlayerModelRenderer.box(72, 50, -2.5f, 0.0f, -6.0f, 5.0f, 3.0f, 8.0f), class_5603.method_32091((float)0.0f, (float)8.0f, (float)0.0f, (float)((float)(-Math.PI) / 90), (float)0.0f, (float)0.0f));
        });
        class_630 body = root.method_32086("body");
        class_630 rightArm = body.method_32086("right_arm");
        final class_630 rightForearm = rightArm.method_32086("right_forearm");
        final class_630 rightHand = rightForearm.method_32086("right_hand");
        class_630 leftArm = body.method_32086("left_arm");
        final class_630 leftForearm = leftArm.method_32086("left_forearm");
        final class_630 leftHand = leftForearm.method_32086("left_hand");
        return new CustomModel("Beer", class_2960.method_60655((String)"recoin", (String)"textures/models/freddy.png"), root, body, body.method_32086("head"), body, leftArm, rightArm, body.method_32086("left_leg"), body.method_32086("right_leg")){

            @Override
            void transform(class_4587 matrices, class_10055 state) {
                matrices.method_22905(0.75f, 0.65f, 0.75f);
                matrices.method_46416(0.0f, 0.85f, 0.0f);
            }

            @Override
            void copyAngles(class_591 vanilla, class_10055 state) {
                this.copyHeadAngles(vanilla);
                this.clearAngles(this.body);
                this.copyLimbAngles(vanilla);
            }

            @Override
            void setArmAngle(class_10055 state, class_1306 arm, class_4587 matrices) {
                this.transform(matrices, state);
                this.root.method_22703(matrices);
                this.body.method_22703(matrices);
                if (arm == class_1306.field_6183) {
                    this.rightArm.method_22703(matrices);
                    rightForearm.method_22703(matrices);
                    rightHand.method_22703(matrices);
                } else {
                    this.leftArm.method_22703(matrices);
                    leftForearm.method_22703(matrices);
                    leftHand.method_22703(matrices);
                }
                matrices.method_46416(0.0f, -0.5f, 0.0f);
            }
        };
    }

    private static CustomModel createTung() {
        class_630 root = CustomPlayerModelRenderer.createRoot(64, 64, data -> {
            class_5610 tung = data.method_32117("tung", class_5606.method_32108(), class_5603.method_32090((float)0.0f, (float)24.0f, (float)0.0f));
            class_5610 chest = tung.method_32117("chest", class_5606.method_32108(), class_5603.method_32090((float)0.0f, (float)-11.0f, (float)1.0f));
            chest.method_32117("body", CustomPlayerModelRenderer.box(0, 18, -3.0f, -5.5f, -2.5f, 6.0f, 11.0f, 5.0f, 0.3f), class_5603.method_32090((float)0.0f, (float)-5.5f, (float)0.0f));
            class_5610 head = chest.method_32117("head", class_5606.method_32108().method_32101(0, 0).method_32098(-3.5f, -11.17242f, -2.89262f, 7.0f, 12.0f, 6.0f, new class_5605(0.15f)).method_32101(35, 4).method_32098(-3.45f, -7.67242f, -2.99262f, 2.0f, 2.0f, 0.9f, new class_5605(0.5f)).method_32101(35, 4).method_32096().method_32098(1.45f, -7.67242f, -2.99262f, 2.0f, 2.0f, 0.9f, new class_5605(0.5f)).method_32106(false).method_32101(36, 29).method_32097(-4.1f, -8.42242f, -3.69262f, 3.25f, 0.5f, 2.25f).method_32101(36, 29).method_32097(0.85f, -8.42242f, -3.69262f, 3.25f, 0.5f, 2.25f), class_5603.method_32090((float)0.0f, (float)-12.12758f, (float)-0.10738f));
            head.method_32117("nose", CustomPlayerModelRenderer.box(30, 24, -1.0f, -2.5f, -1.0f, 1.5f, 5.0f, 2.0f, 0.15f), class_5603.method_32091((float)0.25f, (float)-5.67242f, (float)-2.64262f, (float)0.34906584f, (float)0.0f, (float)0.0f));
            head.method_32117("brow_left", CustomPlayerModelRenderer.box(30, 31, -1.5f, -0.5f, -1.0f, 3.0f, 1.0f, 2.0f), class_5603.method_32090((float)-2.25f, (float)-9.27242f, (float)-2.64262f));
            head.method_32117("brow_right", CustomPlayerModelRenderer.box(30, 31, -1.5f, -0.5f, -1.0f, 3.0f, 1.0f, 2.0f), class_5603.method_32090((float)2.25f, (float)-9.27242f, (float)-2.64262f));
            head.method_32117("eye_left", CustomPlayerModelRenderer.box(6, 34, -0.5f, -0.5f, -0.5f, 1.0f, 1.0f, 1.0f, 0.4f), class_5603.method_32090((float)-2.35f, (float)-5.97242f, (float)-2.69262f));
            head.method_32117("eye_right", CustomPlayerModelRenderer.box(6, 34, -0.5f, -0.5f, -0.5f, 1.0f, 1.0f, 1.0f, 0.4f), class_5603.method_32090((float)2.35f, (float)-5.97242f, (float)-2.69262f));
            class_5610 leftArm = chest.method_32117("left_arm", CustomPlayerModelRenderer.box(26, 0, 0.25f, 5.15f, -1.0f, 2.0f, 5.0f, 2.0f, 0.1f), class_5603.method_32090((float)3.75f, (float)-10.35f, (float)0.0f));
            leftArm.method_32117("left_arm_top", CustomPlayerModelRenderer.box(22, 24, -1.0f, -3.0f, -1.0f, 2.0f, 6.0f, 2.0f), class_5603.method_32091((float)0.75f, (float)2.35f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.17453292f));
            class_5610 rightArm = chest.method_32117("right_arm", CustomPlayerModelRenderer.box(26, 0, -2.25f, 5.15f, -1.0f, 2.0f, 5.0f, 2.0f, 0.1f), class_5603.method_32090((float)-3.75f, (float)-10.35f, (float)0.0f));
            rightArm.method_32117("right_arm_top", CustomPlayerModelRenderer.box(22, 24, -1.0f, -3.0f, -1.0f, 2.0f, 6.0f, 2.0f), class_5603.method_32091((float)-0.75f, (float)2.35f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.17453292f));
            rightArm.method_32117("bat", CustomPlayerModelRenderer.box(34, 6, 0.0f, -1.0f, -1.0f, 12.0f, 2.0f, 2.0f), class_5603.method_32091((float)-1.25f, (float)9.1f, (float)0.1f, (float)-1.5707964f, (float)1.134464f, (float)-1.5707964f));
            class_5610 leftLeg = tung.method_32117("left_leg", class_5606.method_32108().method_32101(26, 7).method_32098(-1.0f, 0.0f, -1.0f, 2.0f, 4.0f, 2.0f, new class_5605(0.15f)).method_32101(22, 32).method_32098(-1.0f, 3.0f, -1.0f, 2.0f, 2.0f, 2.0f, new class_5605(0.3f)).method_32101(26, 0).method_32097(-1.0f, 4.0f, -1.0f, 2.0f, 5.0f, 2.0f).method_32101(22, 18).method_32097(-1.5f, 9.0f, -3.0f, 3.0f, 2.0f, 4.0f), class_5603.method_32090((float)2.0f, (float)-11.0f, (float)1.0f));
            class_5610 rightLeg = tung.method_32117("right_leg", class_5606.method_32108().method_32101(26, 7).method_32098(-1.0f, 0.0f, -1.0f, 2.0f, 4.0f, 2.0f, new class_5605(0.15f)).method_32101(22, 32).method_32098(-1.0f, 3.0f, -1.0f, 2.0f, 2.0f, 2.0f, new class_5605(0.3f)).method_32101(26, 0).method_32097(-1.0f, 4.0f, -1.0f, 2.0f, 5.0f, 2.0f).method_32101(22, 18).method_32097(-1.5f, 9.0f, -3.0f, 3.0f, 2.0f, 4.0f), class_5603.method_32090((float)-2.0f, (float)-11.0f, (float)1.0f));
        });
        class_630 tung = root.method_32086("tung");
        class_630 chest = tung.method_32086("chest");
        return new CustomModel("Sahur", class_2960.method_60655((String)"recoin", (String)"textures/models/tung.png"), root, tung, chest.method_32086("head"), chest, chest.method_32086("left_arm"), chest.method_32086("right_arm"), tung.method_32086("left_leg"), tung.method_32086("right_leg")){

            @Override
            void setArmAngle(class_10055 state, class_1306 arm, class_4587 matrices) {
                this.transform(matrices, state);
                this.root.method_22703(matrices);
                this.bodyRoot.method_22703(matrices);
                this.body.method_22703(matrices);
                class_630 armPart = arm == class_1306.field_6182 ? this.leftArm : this.rightArm;
                armPart.method_22703(matrices);
                matrices.method_46416(0.0f, -0.146875f, 0.0f);
            }
        };
    }

    private static class_630 createRoot(int textureWidth, int textureHeight, Consumer<class_5610> builder) {
        class_5609 data = new class_5609();
        builder.accept(data.method_32111());
        return class_5607.method_32110((class_5609)data, (int)textureWidth, (int)textureHeight).method_32109();
    }

    private static class_5606 box(int u, int v, float x, float y, float z, float width, float height, float depth) {
        return class_5606.method_32108().method_32101(u, v).method_32097(x, y, z, width, height, depth);
    }

    private static class_5606 box(int u, int v, float x, float y, float z, float width, float height, float depth, float dilation) {
        return class_5606.method_32108().method_32101(u, v).method_32098(x, y, z, width, height, depth, new class_5605(dilation));
    }

    private CustomPlayerModelRenderer() {
    }

    static {
        MODEL_FACTORIES.put("CrazyRabbit", CustomPlayerModelRenderer::createRabbit);
        MODEL_FACTORIES.put("Beer", CustomPlayerModelRenderer::createFreddy);
        MODEL_FACTORIES.put("Sahur", CustomPlayerModelRenderer::createTung);
    }

    private static class CustomModel {
        protected final String name;
        protected final class_2960 texture;
        protected final class_630 root;
        protected final class_630 bodyRoot;
        protected final class_630 head;
        protected final class_630 body;
        protected final class_630 leftArm;
        protected final class_630 rightArm;
        protected final class_630 leftLeg;
        protected final class_630 rightLeg;

        protected CustomModel(String name, class_2960 texture, class_630 root, class_630 bodyRoot, class_630 head, class_630 body, class_630 leftArm, class_630 rightArm, class_630 leftLeg, class_630 rightLeg) {
            this.name = name;
            this.texture = texture;
            this.root = root;
            this.bodyRoot = bodyRoot;
            this.head = head;
            this.body = body;
            this.leftArm = leftArm;
            this.rightArm = rightArm;
            this.leftLeg = leftLeg;
            this.rightLeg = rightLeg;
        }

        void transform(class_4587 matrices, class_10055 state) {
        }

        void setArmAngle(class_10055 state, class_1306 arm, class_4587 matrices) {
            this.transform(matrices, state);
            this.root.method_22703(matrices);
            if (this.bodyRoot != this.root) {
                this.bodyRoot.method_22703(matrices);
            }
            if (this.body != this.bodyRoot && this.body != this.root) {
                this.body.method_22703(matrices);
            }
            class_630 armPart = arm == class_1306.field_6182 ? this.leftArm : this.rightArm;
            armPart.method_22703(matrices);
        }

        void copyAngles(class_591 vanilla, class_10055 state) {
            this.copyHeadAngles(vanilla);
            this.copyBodyAngles(vanilla);
            this.copyLimbAngles(vanilla);
        }

        protected void copyHeadAngles(class_591 vanilla) {
            this.head.field_3654 = vanilla.field_3398.field_3654;
            this.head.field_3675 = vanilla.field_3398.field_3675;
            this.head.field_3674 = vanilla.field_3398.field_3674;
        }

        protected void copyBodyAngles(class_591 vanilla) {
            this.body.field_3654 = vanilla.field_3391.field_3654;
            this.body.field_3675 = vanilla.field_3391.field_3675;
            this.body.field_3674 = vanilla.field_3391.field_3674;
        }

        protected void copyLimbAngles(class_591 vanilla) {
            this.leftArm.field_3654 = vanilla.field_27433.field_3654;
            this.leftArm.field_3675 = vanilla.field_27433.field_3675;
            this.leftArm.field_3674 = vanilla.field_27433.field_3674;
            this.rightArm.field_3654 = vanilla.field_3401.field_3654;
            this.rightArm.field_3675 = vanilla.field_3401.field_3675;
            this.rightArm.field_3674 = vanilla.field_3401.field_3674;
            this.leftLeg.field_3654 = vanilla.field_3397.field_3654;
            this.leftLeg.field_3675 = vanilla.field_3397.field_3675;
            this.leftLeg.field_3674 = vanilla.field_3397.field_3674;
            this.rightLeg.field_3654 = vanilla.field_3392.field_3654;
            this.rightLeg.field_3675 = vanilla.field_3392.field_3675;
            this.rightLeg.field_3674 = vanilla.field_3392.field_3674;
        }

        protected void clearAngles(class_630 part) {
            part.field_3654 = 0.0f;
            part.field_3675 = 0.0f;
            part.field_3674 = 0.0f;
        }
    }
}

