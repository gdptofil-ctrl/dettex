/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_1799
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 */
package ru.recoin.implement.features.modules.render;

import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.entity.EntityCompat;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.implement.events.render.WorldRenderEvent;

public class SkeletonESP
extends Module {
    private final MultiSelectSetting targets = new MultiSelectSetting("Targets", "Players to display").value("Self", "Players", "Naked Player").selected("Players", "Naked Player");
    private final ValueSetting lineWidth = new ValueSetting("Line Width", "Skeleton line width").setValue(1.4f).range(0.5f, 3.0f).step(0.1f);
    private final BooleanSetting throughWalls = new BooleanSetting("Through Walls", "Render skeleton through blocks").setValue(false);

    public SkeletonESP() {
        super("SkeletonESP", "Skeleton ESP", ModuleCategory.RENDER);
        this.setup(this.targets, this.lineWidth, this.throughWalls);
    }

    public static SkeletonESP getInstance() {
        return Instance.get(SkeletonESP.class);
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (SkeletonESP.mc.field_1724 == null || SkeletonESP.mc.field_1687 == null || SkeletonESP.mc.field_1773 == null) {
            return;
        }
        class_243 camera = SkeletonESP.mc.field_1773.method_19418().method_71156();
        for (class_1657 player : SkeletonESP.mc.field_1687.method_18456()) {
            if (player.method_31481() || player.method_29504() || player.method_5767() || !this.isTarget(player) || player == SkeletonESP.mc.field_1724 && SkeletonESP.mc.field_1690.method_31044().method_31034() || player.method_5707(camera) > 4096.0) continue;
            this.drawSkeleton(player, event.getPartialTicks());
        }
    }

    private boolean isTarget(class_1657 player) {
        if (player == SkeletonESP.mc.field_1724) {
            return this.targets.isSelected("Self");
        }
        boolean naked = true;
        for (class_1799 stack : EntityCompat.getArmorItems((class_1309)player)) {
            if (stack.method_7960()) continue;
            naked = false;
            break;
        }
        return this.targets.isSelected(naked ? "Naked Player" : "Players");
    }

    private void drawSkeleton(class_1657 player, float tickDelta) {
        class_243 position = MathUtil.interpolate((class_1297)player);
        float yaw = class_3532.method_17821((float)tickDelta, (float)player.field_6220, (float)player.field_6283);
        double radians = Math.toRadians(yaw);
        class_243 right = new class_243(-Math.cos(radians), 0.0, -Math.sin(radians));
        class_243 forward = new class_243(-Math.sin(radians), 0.0, Math.cos(radians));
        float limbSwing = player.field_42108.method_48572(tickDelta);
        float limbAmount = class_3532.method_15363((float)player.field_42108.method_48570(tickDelta), (float)0.0f, (float)1.0f);
        float armSwing = class_3532.method_15362((double)(limbSwing * 0.6662f + (float)Math.PI)) * limbAmount * 0.75f;
        int leftColor = ColorUtil.getClientColor();
        int rightColor = ColorUtil.overCol(leftColor, ColorUtil.getText(), 0.22f);
        float width = this.lineWidth.getValue();
        class_243 neck = this.point(position, right, forward, 0.0, 1.48, 0.0);
        class_243 chest = this.point(position, right, forward, 0.0, 1.35, 0.0);
        class_243 waist = this.point(position, right, forward, 0.0, 0.78, 0.0);
        class_243 leftShoulder = this.point(position, right, forward, -0.3, 1.38, 0.0);
        class_243 rightShoulder = this.point(position, right, forward, 0.3, 1.38, 0.0);
        class_243 leftHip = this.point(position, right, forward, -0.11, 0.76, 0.0);
        class_243 rightHip = this.point(position, right, forward, 0.11, 0.76, 0.0);
        this.drawSegment(neck, chest, leftColor, rightColor, width);
        this.drawSegment(neck, this.point(position, right, forward, 0.0, 1.75, 0.0), leftColor, rightColor, width);
        this.drawSegment(chest, waist, rightColor, leftColor, width);
        this.drawSegment(leftShoulder, rightShoulder, leftColor, rightColor, width);
        this.drawSegment(leftHip, rightHip, rightColor, leftColor, width);
        this.drawArm(position, right, forward, -1.0, armSwing, leftColor, rightColor, width);
        this.drawArm(position, right, forward, 1.0, -armSwing, rightColor, leftColor, width);
        float legSwing = class_3532.method_15362((double)(limbSwing * 0.6662f)) * limbAmount * 0.72f;
        this.drawLeg(position, right, forward, -1.0, legSwing, leftColor, rightColor, width);
        this.drawLeg(position, right, forward, 1.0, -legSwing, rightColor, leftColor, width);
    }

    private void drawArm(class_243 origin, class_243 right, class_243 forward, double side, float swing, int shoulderColor, int handColor, float width) {
        class_243 shoulder = this.point(origin, right, forward, side * 0.3, 1.38, 0.0);
        class_243 arm = forward.method_1021(Math.sin(swing) * 0.6).method_1031(0.0, -Math.cos(swing) * 0.6, 0.0);
        class_243 elbow = shoulder.method_1019(arm.method_1021(0.5));
        class_243 wrist = shoulder.method_1019(arm);
        this.drawSegment(shoulder, elbow, shoulderColor, handColor, width);
        this.drawSegment(elbow, wrist, handColor, shoulderColor, width);
    }

    private void drawLeg(class_243 origin, class_243 right, class_243 forward, double side, float swing, int hipColor, int footColor, float width) {
        class_243 hip = this.point(origin, right, forward, side * 0.11, 0.76, 0.0);
        double swingZ = Math.sin(swing) * 0.17;
        class_243 knee = this.point(origin, right, forward, side * 0.12, 0.4, swingZ);
        class_243 ankle = this.point(origin, right, forward, side * 0.13, 0.08 + Math.max(0.0, Math.cos(swing)) * 0.06, swingZ * 1.8);
        this.drawSegment(hip, knee, hipColor, footColor, width);
        this.drawSegment(knee, ankle, footColor, hipColor, width);
    }

    private class_243 point(class_243 origin, class_243 right, class_243 forward, double side, double height, double depth) {
        return origin.method_1019(right.method_1021(side)).method_1031(0.0, height, 0.0).method_1019(forward.method_1021(depth));
    }

    private void drawSegment(class_243 start, class_243 end, int startColor, int endColor, float width) {
        boolean depth = !this.throughWalls.isValue();
        Render3DUtil.drawAdditiveLineGradient(start, end, ColorUtil.multAlpha(startColor, 0.34f), ColorUtil.multAlpha(endColor, 0.34f), width * 3.0f, depth);
        Render3DUtil.drawLineGradient(start, end, startColor, endColor, width, depth);
    }
}

