/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1259$class_1260
 *  net.minecraft.class_332
 *  net.minecraft.class_345
 *  net.minecraft.class_4587
 *  org.joml.Vector4f
 */
package ru.recoin.implement.features.draggables;

import net.minecraft.class_1259;
import net.minecraft.class_332;
import net.minecraft.class_345;
import net.minecraft.class_4587;
import org.joml.Vector4f;
import ru.recoin.api.feature.draggable.AbstractDraggable;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.render.MatrixStackCompat;

public class BossBars
extends AbstractDraggable {
    private static final float BAR_HEIGHT = 3.5f;
    private static final Vector4f FULL_ROUND = new Vector4f(1.75f);
    private static final Vector4f PARTIAL_ROUND = new Vector4f(0.0f, 0.0f, 1.75f, 1.75f);

    public BossBars() {
        super("Boss Bars", 0, 0, 0, 0, false);
    }

    @Override
    public boolean visible() {
        return !BossBars.mc.field_1705.method_1740().field_2060.isEmpty();
    }

    @Override
    public void drawDraggable(class_332 context) {
        this.setX(mc.method_22683().method_4486() / 2);
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        float y = 10.0f;
        float width = 156.0f;
        FontRenderer font = Fonts.getSize(18, Fonts.Type.INTER_BOLD);
        for (class_345 bossInfo : BossBars.mc.field_1705.method_1740().field_2060.values()) {
            Vector4f rounds = bossInfo.method_5412() != 1.0f ? PARTIAL_ROUND : FULL_ROUND;
            int color = this.getColor(bossInfo.method_5420());
            rectangle.render(ShapeProperties.create(matrix, (float)this.getX() - width / 2.0f, y + 10.0f, width, 3.5).color(ColorUtil.getRect(0.8f)).round(5.0f).build());
            rectangle.render(ShapeProperties.create(matrix, (float)this.getX() - width / 2.0f, y + 10.0f, width, 3.5).color(ColorUtil.multAlpha(color, 0.2f)).round(5.0f).build());
            rectangle.render(ShapeProperties.create(matrix, (float)this.getX() - width / 2.0f, y + 10.0f, width * bossInfo.method_5412(), 3.5).color(ColorUtil.multAlpha(color, 0.8f)).round(rounds).build());
            font.drawText(matrix, bossInfo.method_5414(), (int)((float)this.getX() - font.getStringWidth(bossInfo.method_5414()) / 2.0f), y);
            y += 22.0f;
        }
    }

    public int getColor(class_1259.class_1260 color) {
        return switch (color) {
            case class_1259.class_1260.field_5788 -> -42316;
            case class_1259.class_1260.field_5783 -> -8307457;
            case class_1259.class_1260.field_5784 -> -51401;
            case class_1259.class_1260.field_5780 -> -16736001;
            case class_1259.class_1260.field_5785 -> -11141291;
            default -> 0xFF000000 | color.method_5423().method_532();
        };
    }
}

