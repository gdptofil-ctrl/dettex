/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_3966
 *  org.joml.Matrix3x2fStack
 */
package ru.recoin.implement.features.modules.render;

import net.minecraft.class_332;
import net.minecraft.class_3966;
import org.joml.Matrix3x2fStack;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.other.Instance;

public class CrossHair
extends Module {
    private final ValueSetting attackSetting = new ValueSetting("Attack Indent", "Item Cooldown Indent").setValue(10.0f).range(0, 20);
    private final ValueSetting indentSetting = new ValueSetting("Indent", "Indent from the center of the screen").setValue(0.0f).range(0, 5);
    private final ValueSetting size1Setting = new ValueSetting("Width", "Width Cross Hair").setValue(4.0f).range(2, 10);
    private final ValueSetting size2Setting = new ValueSetting("Height", "Height Cross Hair").setValue(1.0f).range(1, 4);
    private final BooleanSetting outlineSetting = new BooleanSetting("Outline", "Draw black border around crosshair").setValue(true);

    public static CrossHair getInstance() {
        return Instance.get(CrossHair.class);
    }

    public CrossHair() {
        super("CrossHair", "Cross Hair", ModuleCategory.RENDER);
        this.setup(this.attackSetting, this.indentSetting, this.size1Setting, this.size2Setting, this.outlineSetting);
    }

    public void onRenderCrossHair(class_332 context) {
        if (CrossHair.mc.field_1724 == null || CrossHair.mc.field_1690 == null || !CrossHair.mc.field_1690.method_31044().method_31034()) {
            return;
        }
        int color = CrossHair.mc.field_1765 instanceof class_3966 ? ColorUtil.getColor(255, 64, 64) : ColorUtil.WHITE;
        float centerX = (float)window.getScaledWidth() / 2.0f;
        float centerY = (float)window.getScaledHeight() / 2.0f;
        float cooldown = (float)this.attackSetting.getInt() - (float)this.attackSetting.getInt() * CrossHair.mc.field_1724.method_7261(tickCounter.getTickProgress(false));
        float length = this.size1Setting.getValue();
        float thickness = this.size2Setting.getValue();
        float gap = (float)this.indentSetting.getInt() + cooldown;
        this.renderCrosshair(context, centerX, centerY, length, thickness, gap, color, this.outlineSetting.isValue());
    }

    private void renderCrosshair(class_332 context, float centerX, float centerY, float length, float thickness, float gap, int color, boolean outline) {
        float halfThick = thickness / 2.0f;
        float outlineThick = 0.5f;
        if (gap <= 0.0f) {
            if (outline) {
                this.drawRect(context, centerX - length - outlineThick, centerY - halfThick - outlineThick, 2.0f * length + 2.0f * outlineThick, thickness + 2.0f * outlineThick, ColorUtil.BLACK);
                this.drawRect(context, centerX - halfThick - outlineThick, centerY - length - outlineThick, thickness + 2.0f * outlineThick, 2.0f * length + 2.0f * outlineThick, ColorUtil.BLACK);
            }
            this.drawRect(context, centerX - length, centerY - halfThick, 2.0f * length, thickness, color);
            this.drawRect(context, centerX - halfThick, centerY - length, thickness, 2.0f * length, color);
            return;
        }
        if (outline) {
            this.drawRect(context, centerX + gap - outlineThick, centerY - halfThick - outlineThick, length + 2.0f * outlineThick, thickness + 2.0f * outlineThick, ColorUtil.BLACK);
            this.drawRect(context, centerX - gap - length - outlineThick, centerY - halfThick - outlineThick, length + 2.0f * outlineThick, thickness + 2.0f * outlineThick, ColorUtil.BLACK);
            this.drawRect(context, centerX - halfThick - outlineThick, centerY - gap - length - outlineThick, thickness + 2.0f * outlineThick, length + 2.0f * outlineThick, ColorUtil.BLACK);
            this.drawRect(context, centerX - halfThick - outlineThick, centerY + gap - outlineThick, thickness + 2.0f * outlineThick, length + 2.0f * outlineThick, ColorUtil.BLACK);
        }
        this.drawRect(context, centerX + gap, centerY - halfThick, length, thickness, color);
        this.drawRect(context, centerX - gap - length, centerY - halfThick, length, thickness, color);
        this.drawRect(context, centerX - halfThick, centerY - gap - length, thickness, length, color);
        this.drawRect(context, centerX - halfThick, centerY + gap, thickness, length, color);
    }

    private void drawRect(class_332 context, float x, float y, float width, float height, int color) {
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate(x, y);
        matrices.scale(width, height);
        context.method_25294(0, 0, 1, 1, color);
        matrices.popMatrix();
    }
}

