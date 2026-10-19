/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.features.draggables;

import java.util.List;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import ru.recoin.api.feature.draggable.AbstractDraggable;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.other.StringUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.implement.features.draggables.HudIconRail;
import ru.recoin.implement.features.modules.render.OpponentCooldowns;

public final class OpponentCooldownsHud
extends AbstractDraggable {
    public OpponentCooldownsHud() {
        super("\u041a\u0443\u043b\u0434\u0430\u0443\u043d\u044b \u043f\u0440\u043e\u0442\u0438\u0432\u043d\u0438\u043a\u0430", 120, 42, 170, 35, true);
    }

    public static OpponentCooldownsHud getInstance() {
        return Instance.getDraggable(OpponentCooldownsHud.class);
    }

    @Override
    public boolean visible() {
        OpponentCooldowns tracker = OpponentCooldowns.getInstance();
        return tracker != null && tracker.isState() && (tracker.hasHudRows() || PlayerIntersectionUtil.isChat(OpponentCooldownsHud.mc.field_1755));
    }

    @Override
    public void drawDraggable(class_332 context) {
        OpponentCooldowns tracker = OpponentCooldowns.getInstance();
        if (tracker == null || !tracker.isState()) {
            return;
        }
        List<OpponentCooldowns.HudRow> rows = tracker.getHudRows();
        String header = tracker.getTargetName().isBlank() ? this.getName() : tracker.getTargetName();
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        FontRenderer font = Fonts.getSize(15, Fonts.Type.INTER_BOLD);
        FontRenderer rowFont = Fonts.getSize(13, Fonts.Type.INTER_BOLD);
        FontRenderer iconFont = Fonts.getSize(15, Fonts.Type.COMBINED_ICONS);
        int maxWidth = HudIconRail.headerWidth(font, header);
        for (OpponentCooldowns.HudRow row : rows) {
            String duration = StringUtil.getDuration(row.remainingSeconds());
            maxWidth = Math.max(maxWidth, (int)Math.ceil(24.0f + rowFont.getStringWidth(row.label()) + 4.0f + HudIconRail.timerWidth(rowFont, duration) + 4.0f));
        }
        float rowCount = rows.size();
        this.setWidth(maxWidth);
        this.setHeight(HudIconRail.height(rowCount));
        HudIconRail.draw(this, matrix, blur, rectangle, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        HudIconRail.drawHeader(this, matrix, font, iconFont, header, "\ue003");
        float offset = 17.0f;
        for (OpponentCooldowns.HudRow row : rows) {
            float rowTop = (float)this.getY() + offset;
            String duration = StringUtil.getDuration(row.remainingSeconds());
            Render2DUtil.defaultDrawStack(context, row.icon(), HudIconRail.iconX(this, this.getX(), 8.0f) - 1.0f, rowTop + 0.5f - 1.0f, false, false, 0.5f);
            HudIconRail.drawText(matrix, rowFont, row.label(), (float)this.getX() + 24.0f, rowTop, 9.0f, ColorUtil.getText());
            int timerColor = row.remainingSeconds() <= 5 ? ColorUtil.multGreen(ColorUtil.getText(), MathUtil.blinking(1000.0, 8.0f)) : ColorUtil.getText();
            HudIconRail.drawTimer(matrix, rectangle, arc, rowFont, duration, (float)(this.getX() + this.getWidth()) - 4.0f, rowTop, row.progress(), timerColor);
            offset += 11.0f;
        }
    }
}

