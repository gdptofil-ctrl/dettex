/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1531
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1935
 *  net.minecraft.class_2281
 *  net.minecraft.class_2338
 *  net.minecraft.class_243
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.features.modules.render;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_1297;
import net.minecraft.class_1531;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;
import net.minecraft.class_2281;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.Setting;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.ProjectionUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.common.util.warden.WardenTimerCache;
import ru.recoin.common.util.world.ServerUtil;
import ru.recoin.implement.events.render.DrawEvent;
import ru.recoin.implement.features.modules.render.Hud;

public final class WardenHelper
extends Module
implements QuickImports {
    private static final Pattern CLOCK = Pattern.compile("(\\d{1,2}:\\d{2}(?::\\d{2})?)");
    private static final Pattern SECONDS = Pattern.compile("(\\d+)\\s*(?:\u0441|s|\u0441\u0435\u043a|sec)", 66);
    private static final Pattern MINUTES = Pattern.compile("(\\d+)\\s*(?:\u043c|m|\u043c\u0438\u043d|min(?:ute)?)\\s*(?:(\\d+)\\s*(?:\u0441|s|\u0441\u0435\u043a|sec(?:ond)?))?", 66);

    public WardenHelper() {
        super("WardenHelper", "Warden Helper", ModuleCategory.RENDER);
        this.setup(new Setting[0]);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventHandler
    public void onDraw(DrawEvent event) {
        if (WardenHelper.mc.field_1724 == null || WardenHelper.mc.field_1687 == null) {
            return;
        }
        long now = System.currentTimeMillis();
        for (class_1297 class_12972 : WardenHelper.mc.field_1687.method_18112()) {
            if (!(class_12972 instanceof class_1531)) continue;
            class_1531 stand = (class_1531)class_12972;
            class_2338 chest = this.findChestBelow(stand.method_24515());
            long duration = this.parseDuration(stand.method_5477().getString());
            if (chest == null || duration <= 0L) continue;
            WardenTimerCache.update(ServerUtil.getAnarchy(), chest, now + duration);
        }
        for (Map.Entry entry : WardenTimerCache.snapshot(ServerUtil.getAnarchy(), now).entrySet()) {
            String time = this.formatTime((Long)entry.getValue() - now);
            class_243 screen = ProjectionUtil.worldSpaceToScreenSpace(((class_2338)entry.getKey()).method_46558().method_1031(0.0, 1.0, 0.0));
            if (screen.field_1350 <= 0.0 || screen.field_1350 >= 1.0) continue;
            FontRenderer font = Fonts.getMenuSize(12, Fonts.Type.INTER_BOLD);
            int width = Math.max(Math.round(font.getStringWidth(time)) + 15, 36);
            int height = 14;
            int x = Math.round((float)screen.field_1352 - (float)width / 2.0f);
            int y = Math.round((float)screen.field_1351);
            class_4587 matrix = MatrixStackCompat.from(event.getDrawContext().method_51448());
            boolean previousHud = Render2DUtil.isHudDecorationActive();
            boolean previousGui = Render2DUtil.isGuiDecorationActive();
            Render2DUtil.setHudDecorationActive(false);
            Render2DUtil.setGuiDecorationActive(true);
            try {
                blur.setLiveMode(true);
                blur.setColorlessMode(true);
                blur.setStyledBlurRadius(92.0f);
                blur.render(ShapeProperties.create(matrix, x, y, width, height).round(5.0f).softness(5.0f).thickness(0.0f).quality(50.0f).outlineColor(0).color(-268435456).build());
            }
            finally {
                blur.resetStyledBlurRadius();
                blur.setColorlessMode(false);
                blur.setLiveMode(false);
                Render2DUtil.setHudDecorationActive(previousHud);
                Render2DUtil.setGuiDecorationActive(previousGui);
            }
            boolean queued = Render2DUtil.isQueuedShapeActive();
            Render2DUtil.setQueuedShapeActive(false);
            try {
                rectangle.render(ShapeProperties.create(matrix, x, y, width, height).round(5.0f).softness(0.6f).thickness(1.2f).quality(22.0f).outlineColor(ColorUtil.multAlpha(Hud.getInstance().themeOutlineColor(), 0.72f)).color(-519435246).build());
            }
            finally {
                Render2DUtil.setQueuedShapeActive(queued);
            }
            float iconScale = 0.75f;
            event.getDrawContext().method_51448().pushMatrix();
            event.getDrawContext().method_51448().translate((float)x + 1.0f, (float)y + 0.5f);
            event.getDrawContext().method_51448().scale(iconScale, iconScale);
            event.getDrawContext().method_51427(new class_1799((class_1935)class_1802.field_8106), 0, 0);
            event.getDrawContext().method_51448().popMatrix();
            font.drawString(matrix, time, (float)x + 14.0f, (float)y + 5.5f, -855049);
            FontRenderer.flushQueuedText(event.getDrawContext());
        }
    }

    private long parseDuration(String text) {
        String value = this.extractTime(text);
        if (value == null) {
            return -1L;
        }
        String[] clock = value.split(":");
        try {
            if (clock.length == 3) {
                return (Long.parseLong(clock[0]) * 3600L + Long.parseLong(clock[1]) * 60L + Long.parseLong(clock[2])) * 1000L;
            }
            if (clock.length == 2) {
                return (Long.parseLong(clock[0]) * 60L + Long.parseLong(clock[1])) * 1000L;
            }
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
        Matcher matcher = MINUTES.matcher(value);
        if (matcher.find()) {
            long minutes = Long.parseLong(matcher.group(1));
            long seconds = matcher.group(2) == null ? 0L : Long.parseLong(matcher.group(2));
            return (minutes * 60L + seconds) * 1000L;
        }
        matcher = SECONDS.matcher(value);
        return matcher.find() ? Long.parseLong(matcher.group(1)) * 1000L : -1L;
    }

    private String formatTime(long remainingMs) {
        long seconds = Math.max(0L, (remainingMs + 999L) / 1000L);
        if (seconds >= 3600L) {
            return String.format("%02d:%02d:%02d", seconds / 3600L, seconds / 60L % 60L, seconds % 60L);
        }
        return String.format("%02d:%02d", seconds / 60L, seconds % 60L);
    }

    private String extractTime(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        Matcher matcher = CLOCK.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        matcher = MINUTES.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        matcher = SECONDS.matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

    private class_2338 findChestBelow(class_2338 origin) {
        for (int offset = 1; offset <= 3; ++offset) {
            class_2338 below = origin.method_10087(offset);
            if (!(WardenHelper.mc.field_1687.method_8320(below).method_26204() instanceof class_2281)) continue;
            return below;
        }
        return null;
    }
}

