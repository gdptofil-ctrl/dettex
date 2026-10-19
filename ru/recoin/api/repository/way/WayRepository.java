/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2338
 *  net.minecraft.class_243
 *  net.minecraft.class_4587
 */
package ru.recoin.api.repository.way;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.event.EventManager;
import ru.recoin.api.repository.way.Way;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.QuickImports;
import ru.recoin.common.QuickLogger;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.math.ProjectionUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.implement.events.render.DrawEvent;
import ru.recoin.implement.features.modules.misc.ServerHelper;

public class WayRepository
implements QuickImports,
QuickLogger {
    public List<Way> wayList = new ArrayList<Way>();

    public WayRepository(EventManager eventManager) {
        eventManager.register(this);
    }

    public boolean isEmpty() {
        return this.wayList.isEmpty();
    }

    public void addWay(String name, class_2338 pos, String server) {
        this.wayList.add(new Way(name, pos, server));
    }

    public void addTemporaryWay(String name, class_2338 pos, String server) {
        this.wayList.removeIf(way -> way.temporary() && way.name().equalsIgnoreCase(name));
        this.wayList.add(new Way(name, pos, server, true));
    }

    public void deleteTemporaryWay(String name) {
        this.wayList.removeIf(way -> way.temporary() && way.name().equalsIgnoreCase(name));
    }

    public void clearTemporaryWays() {
        this.wayList.removeIf(Way::temporary);
    }

    public boolean hasWay(String text) {
        return this.wayList.stream().anyMatch(s -> s.name().equalsIgnoreCase(text));
    }

    public void deleteWay(String name) {
        this.wayList.removeIf(macro -> macro.name().equalsIgnoreCase(name));
    }

    public void clearList() {
        if (!this.isEmpty()) {
            this.wayList.clear();
        }
    }

    @EventHandler
    public void onDraw(DrawEvent e) {
        if (this.isEmpty() || mc.method_1562() == null || mc.method_1562().method_45734() == null) {
            return;
        }
        class_4587 matrix = MatrixStackCompat.from(e.getDrawContext().method_51448());
        this.wayList.forEach(way -> {
            class_243 wayVec = way.pos().method_46558();
            class_243 vec = ProjectionUtil.worldSpaceToScreenSpace(wayVec);
            if (ProjectionUtil.canSee(wayVec) && way.server().equalsIgnoreCase(WayRepository.mc.method_1562().method_45734().field_3761)) {
                String eventTime = way.temporary() ? ServerHelper.getInstance().getAutoPointTime(way.name()) : null;
                String text = way.name() + (String)(eventTime == null || eventTime.isBlank() ? "" : " " + eventTime) + " - " + MathUtil.round(WayRepository.mc.method_1561().field_4686.method_71156().method_1022(wayVec), 0.1f) + "m";
                FontRenderer font = Fonts.getSize(14);
                FontRenderer iconFont = Fonts.getSize(28, Fonts.Type.COMBINED_ICONS);
                String icon = "\ue04f";
                float height = font.getStringHeight(text) / 4.0f;
                ArrayList<String> lines = new ArrayList<String>();
                lines.add(text);
                if (way.temporary()) {
                    lines.addAll(ServerHelper.getInstance().getAutoPointLines(way.name()));
                }
                float width = lines.stream().map(font::getStringWidth).max(Float::compare).orElse(Float.valueOf(1.0f)).floatValue();
                float horizontalPadding = 3.0f;
                float verticalPadding = 0.5f;
                double x = vec.method_10216() - (double)(width / 2.0f);
                double y = vec.method_10214() - (double)(height / 2.0f) + 4.0;
                float lineHeight = Math.max(7.0f, height);
                float boxHeight = lineHeight * (float)lines.size();
                blur.render(ShapeProperties.create(matrix, x - (double)horizontalPadding, y - (double)verticalPadding - 1.5, width + horizontalPadding * 2.0f, boxHeight + verticalPadding * 2.0f).round(2.0f).softness(1.0f).color(ColorUtil.HALF_BLACK).build());
                float iconBob = (float)Math.sin((double)System.currentTimeMillis() / 420.0) * 2.0f;
                iconFont.drawString(matrix, icon, (float)(vec.method_10216() - (double)(iconFont.getStringWidth(icon) / 2.0f)), (float)y - 15.0f + iconBob, ColorUtil.getClientColor());
                for (int i = 0; i < lines.size(); ++i) {
                    String line = lines.get(i);
                    float lineX = (float)vec.method_10216() - font.getStringWidth(line) / 2.0f;
                    font.drawString(matrix, line, lineX, (float)y + (float)i * 10.0f, ColorUtil.getText());
                }
            }
        });
    }
}

