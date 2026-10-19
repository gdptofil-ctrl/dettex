/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10799
 *  net.minecraft.class_1297
 *  net.minecraft.class_1304
 *  net.minecraft.class_1429
 *  net.minecraft.class_1588
 *  net.minecraft.class_1646
 *  net.minecraft.class_1657
 *  net.minecraft.class_243
 *  net.minecraft.class_2960
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 *  net.minecraft.class_5498
 */
package ru.recoin.implement.features.modules.render;

import net.minecraft.class_10799;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1429;
import net.minecraft.class_1588;
import net.minecraft.class_1646;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_5498;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.GuiRenderFlusher;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.world.GpsMarker;
import ru.recoin.implement.events.render.DrawEvent;

public class Arrows
extends Module {
    private final class_2960 arrowTexture = class_2960.method_60654((String)"textures/arrow.png");
    private final MultiSelectSetting targetsSetting = new MultiSelectSetting("Targets", "Entities shown by arrows").value("Players", "Naked Players", "Mobs", "Animals", "Villagers").selected("Players");
    private final ValueSetting radiusSetting = new ValueSetting("Radius", "Radius of arrows").setValue(50.0f).range(30, 100);
    private final ValueSetting sizeSetting = new ValueSetting("Size", "Size of arrows").setValue(16.0f).range(8, 20);

    public Arrows() {
        super("Arrows", "Arrows", ModuleCategory.RENDER);
        this.setup(this.targetsSetting, this.radiusSetting, this.sizeSetting);
    }

    @EventHandler
    public void onDraw(DrawEvent e) {
        if (Arrows.mc.field_1687 == null || Arrows.mc.field_1724 == null || Arrows.mc.field_1690.field_1842 || !Arrows.mc.field_1690.method_31044().equals((Object)class_5498.field_26664)) {
            return;
        }
        class_332 context = e.getDrawContext();
        boolean hasGpsMarker = GpsMarker.isActive();
        float middleW = (float)mc.method_22683().method_4486() / 2.0f;
        float middleH = (float)mc.method_22683().method_4502() / 2.0f;
        float posY = middleH - this.radiusSetting.getValue();
        float gpsPosY = posY - this.sizeSetting.getValue() - 10.0f;
        float size = this.sizeSetting.getValue();
        boolean hasTargets = false;
        for (class_1297 entity : Arrows.mc.field_1687.method_18112()) {
            if (!this.shouldShow(entity)) continue;
            hasTargets = true;
            int color = this.getTargetColor(entity);
            float yaw = Arrows.getRotations(entity) - Arrows.mc.field_1724.method_36454();
            this.drawArrow(context, middleW, middleH, posY, size, yaw, ColorUtil.multAlpha(color, 1.0f));
            this.drawArrow(context, middleW, middleH, posY, size, yaw, ColorUtil.multAlpha(color, 1.0f));
            this.drawArrow(context, middleW, middleH, posY, size, yaw, ColorUtil.multAlpha(color, 1.0f));
        }
        if (hasGpsMarker) {
            float yaw = Arrows.getRotations(GpsMarker.getX(), GpsMarker.getZ()) - Arrows.mc.field_1724.method_36454();
            this.drawArrow(context, middleW, middleH, gpsPosY, size, yaw, 1927931903);
            this.drawArrow(context, middleW, middleH, gpsPosY, size, yaw, -1447937);
            class_4587 matrix = MatrixStackCompat.from(context.method_51448());
            this.drawGpsLabel(matrix, middleW, middleH, gpsPosY, size);
        }
        if (hasTargets || hasGpsMarker) {
            context.method_73199();
            GuiRenderFlusher.flush();
        }
    }

    private boolean shouldShow(class_1297 entity) {
        if (entity == Arrows.mc.field_1724 || !entity.method_5805()) {
            return false;
        }
        if (entity instanceof class_1657) {
            class_1657 player = (class_1657)entity;
            return this.isNaked(player) ? this.targetsSetting.isSelected("Naked Players") : this.targetsSetting.isSelected("Players");
        }
        if (entity instanceof class_1646) {
            return this.targetsSetting.isSelected("Villagers");
        }
        if (entity instanceof class_1429) {
            return this.targetsSetting.isSelected("Animals");
        }
        return entity instanceof class_1588 && this.targetsSetting.isSelected("Mobs");
    }

    private boolean isNaked(class_1657 player) {
        return player.method_6118(class_1304.field_6169).method_7960() && player.method_6118(class_1304.field_6174).method_7960() && player.method_6118(class_1304.field_6172).method_7960() && player.method_6118(class_1304.field_6166).method_7960();
    }

    private int getTargetColor(class_1297 entity) {
        if (entity instanceof class_1657) {
            class_1657 player = (class_1657)entity;
            return FriendUtils.isFriend((class_1297)player) ? ColorUtil.getFriendColor() : ColorUtil.getClientColor();
        }
        if (entity instanceof class_1588) {
            return -43691;
        }
        if (entity instanceof class_1646) {
            return -22016;
        }
        if (entity instanceof class_1429) {
            return -11141291;
        }
        return ColorUtil.getClientColor();
    }

    private void drawArrow(class_332 context, float middleW, float middleH, float posY, float size, float yaw, int color) {
        context.method_51448().pushMatrix();
        context.method_51448().rotateAbout(yaw * ((float)Math.PI / 180), middleW, middleH);
        context.method_25293(class_10799.field_56883, this.arrowTexture, Math.round(middleW - size / 2.0f), Math.round(posY), 0.0f, 0.0f, Math.round(size), Math.round(size), Math.round(size), Math.round(size), Math.round(size), Math.round(size), color);
        context.method_51448().popMatrix();
    }

    private void drawGpsLabel(class_4587 matrix, float middleW, float middleH, float gpsPosY, float size) {
        FontRenderer font = Fonts.getSize(13);
        float yaw = Arrows.getRotations(GpsMarker.getX(), GpsMarker.getZ()) - Arrows.mc.field_1724.method_36454();
        float localX = 0.0f;
        float localY = gpsPosY + size / 2.0f - middleH;
        double radians = Math.toRadians(yaw);
        float centerX = middleW + (float)((double)localX * Math.cos(radians) - (double)localY * Math.sin(radians));
        float centerY = middleH + (float)((double)localX * Math.sin(radians) + (double)localY * Math.cos(radians));
        int distance = (int)new class_243((double)GpsMarker.getX() - Arrows.mc.field_1724.method_23317(), 0.0, (double)GpsMarker.getZ() - Arrows.mc.field_1724.method_23321()).method_37267();
        String text = distance + "m";
        font.drawString(matrix, text, centerX + size / 2.0f + 4.0f, centerY - 3.0f, ColorUtil.getText());
    }

    public static float getRotations(double x, double z) {
        double diffX = x - MathUtil.interpolate(Arrows.mc.field_1724.field_6014, Arrows.mc.field_1724.method_23317());
        double diffZ = z - MathUtil.interpolate(Arrows.mc.field_1724.field_5969, Arrows.mc.field_1724.method_23321());
        return (float)(-(Math.atan2(diffX, diffZ) * 57.29577951308232));
    }

    public static float getRotations(class_1297 entity) {
        double x = MathUtil.interpolate(entity.field_6014, entity.method_23317()) - MathUtil.interpolate(Arrows.mc.field_1724.field_6014, Arrows.mc.field_1724.method_23317());
        double z = MathUtil.interpolate(entity.field_5969, entity.method_23321()) - MathUtil.interpolate(Arrows.mc.field_1724.field_5969, Arrows.mc.field_1724.method_23321());
        return (float)(-(Math.atan2(x, z) * 57.29577951308232));
    }
}

