/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_266
 *  net.minecraft.class_268
 *  net.minecraft.class_270
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 *  net.minecraft.class_5250
 *  net.minecraft.class_8646
 *  net.minecraft.class_9011
 */
package ru.recoin.implement.features.draggables;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.class_2561;
import net.minecraft.class_266;
import net.minecraft.class_268;
import net.minecraft.class_270;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_5250;
import net.minecraft.class_8646;
import net.minecraft.class_9011;
import ru.recoin.api.feature.draggable.AbstractDraggable;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.render.MatrixStackCompat;

public class ScoreBoard
extends AbstractDraggable {
    private final List<class_9011> scoreboardEntries = new ArrayList<class_9011>();
    private class_266 objective;

    public ScoreBoard() {
        super("Score Board", 10, 100, 100, 120, true);
    }

    @Override
    public boolean visible() {
        return !this.scoreboardEntries.isEmpty();
    }

    @Override
    public void tick() {
        this.objective = Objects.requireNonNull(ScoreBoard.mc.field_1687).method_8428().method_1189(class_8646.field_45157);
        this.scoreboardEntries.clear();
        this.scoreboardEntries.addAll(ScoreBoard.mc.field_1687.method_8428().method_1184(this.objective));
        this.scoreboardEntries.sort(Comparator.comparing(class_9011::comp_2128).reversed().thenComparing(class_9011::comp_2127, String.CASE_INSENSITIVE_ORDER));
    }

    @Override
    public void drawDraggable(class_332 context) {
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        FontRenderer font = Fonts.getSize(16, Fonts.Type.INTER_BOLD);
        class_5250 text = class_2561.method_43473();
        class_2561 mainText = this.objective != null ? this.objective.method_1114() : class_2561.method_43473();
        this.scoreboardEntries.forEach(entry -> text.method_10852((class_2561)class_268.method_1142((class_270)Objects.requireNonNull(ScoreBoard.mc.field_1687).method_8428().method_1164(entry.comp_2127()), (class_2561)entry.method_55387())).method_27693("\n"));
        int padding = 3;
        int offsetText = 14;
        int width = (int)Math.max(font.getStringWidth((class_2561)text) + (float)(padding * 2) + 1.0f, 100.0f);
        rectangle.render(ShapeProperties.create(matrix, this.getX(), this.getY(), this.getWidth(), this.getHeight()).round(5.0f).thickness(1.5f).softness(1.0f).quality(50.0f).color(0).build());
        blur.render(ShapeProperties.create(matrix, this.getX(), this.getY(), this.getWidth(), this.getHeight()).quality(50.0f).round(5.0f).thickness(0.0f).softness(3.5f).outlineColor(0).color(ColorUtil.DARK_BLACK).build());
        font.drawText(matrix, (class_2561)mainText, (int)((float)this.getX() + ((float)this.getWidth() - font.getStringWidth((class_2561)mainText)) / 2.0f), (float)(this.getY() + padding) + 1.5f);
        font.drawText(matrix, (class_2561)text, this.getX() + padding, this.getY() + offsetText + padding);
        if (this.getX() > mc.method_22683().method_4486() / 2) {
            this.setX(this.getX() + this.getWidth() - width);
        }
        this.setWidth(width);
        this.setHeight((int)((double)font.getStringHeight((class_2561)text) / 2.16 + (double)offsetText + (double)padding));
    }
}

