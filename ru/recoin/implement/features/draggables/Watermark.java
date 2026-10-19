/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_2338
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.features.draggables;

import com.adl.nativeprotect.User;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import net.minecraft.class_1297;
import net.minecraft.class_2338;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_897;
import net.minecraft.class_922;
import net.minecraft.class_10042;
import ru.recoin.api.feature.draggable.AbstractDraggable;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.entity.MovingUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.common.util.world.ServerUtil;
import ru.recoin.implement.features.modules.render.Hud;

public class Watermark
extends AbstractDraggable {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final String LOGO_ICON = "\ue0c6";
    private static final String USER_ICON = "\ue08d";
    private static final String FPS_ICON = "\ue07a";
    private static final String TPS_ICON = "\ue074";
    private static final String PING_ICON = "\ue073";
    private static final String TIME_ICON = "\ue003";
    private static final String BPS_ICON = "\ue044";
    private static final String XYZ_ICON = "\ue040";
    private static final float MAIN_HEIGHT = 16.0f;
    private static final float INFO_TOP_GAP = 2.0f;
    private static final float LOGO_SIZE = 10.0f;
    private static final float HUD_ICON_SIZE = 9.5f;
    private static final float HUD_ICON_OFFSET_Y = 6.0f;
    private static final float ELEMENT_GAP = 2.0f;
    private int fpsCount = 0;
    private long lastTimeUpdate;
    private String cachedTime = "00:00";

    public Watermark() {
        super("Watermark", 10, 10, 92, 45, true);
    }

    @Override
    public void tick() {
        this.fpsCount = (int)MathUtil.interpolate(this.fpsCount, mc.method_47599());
    }

    @Override
    public void drawDraggable(class_332 drawContext) {
        if (Hud.getInstance().isHud2()) {
            this.drawHud2(drawContext);
            return;
        }
        boolean timeSeparator;
        float x;
        boolean centered = Hud.getInstance().watermarkCentered.isValue();
        this.setCanDrag(false);
        if (centered) {
            this.setX((window.getScaledWidth() - this.getWidth()) / 2);
            this.setY(this.centeredYBelowBossBar());
        } else {
            this.setX(6);
            this.setY(6);
        }
        class_4587 matrix = MatrixStackCompat.from(drawContext.method_51448());
        FontRenderer font = Fonts.getSize(14, Fonts.Type.INTER_BOLD);
        FontRenderer titleFont = Fonts.getSize(14, Fonts.Type.INTER_BOLD);
        FontRenderer iconFont = Fonts.getSize(18, Fonts.Type.COMBINED_ICONS);
        FontRenderer timeIconFont = Fonts.getSize(16, Fonts.Type.COMBINED_ICONS);
        FontRenderer logoFont = Fonts.getSize(21, Fonts.Type.COMBINED_ICONS);
        String customName = Hud.getInstance().watermarkNameSetting.getText();
        String profileName = User.getInstance().profile("username");
        String name = customName != null && !customName.isBlank() ? customName : (profileName != null && !profileName.isBlank() ? profileName : "DettexClient");
        String fps = this.fpsCount + " FPS";
        int pingValue = Watermark.mc.field_1724 != null && mc.method_1562() != null && mc.method_1562().method_2871(Watermark.mc.field_1724.method_5667()) != null ? mc.method_1562().method_2871(Watermark.mc.field_1724.method_5667()).method_2959() : 0;
        String ping = pingValue + " ms";
        String tps = MathUtil.round(ServerUtil.TPS, 0.1) + " TPS";
        long now = System.currentTimeMillis();
        if (now - this.lastTimeUpdate >= 1000L) {
            this.cachedTime = LocalTime.now().format(TIME_FORMAT);
            this.lastTimeUpdate = now;
        }
        String time = this.cachedTime;
        boolean showBps = Hud.getInstance().watermarkMetrics.isSelected("BPS");
        boolean showCoords = Hud.getInstance().watermarkMetrics.isSelected("COORDS");
        boolean singleInfoMetric = showBps ^ showCoords;
        class_2338 playerPos = Watermark.mc.field_1724 == null ? class_2338.field_10980 : Watermark.mc.field_1724.method_24515();
        String bps = Watermark.mc.field_1724 == null ? "0.0" : String.valueOf(MathUtil.round(MovingUtil.getSpeedSqrt((class_1297)Watermark.mc.field_1724) * 20.0, 0.1f));
        String xyz = playerPos.method_10263() + ", " + playerPos.method_10264() + ", " + playerPos.method_10260();
        boolean separate = Hud.getInstance().watermarkSeparateElements.isValue();
        float userIconWidth = iconFont.getStringWidth(USER_ICON);
        float y = (float)this.getY() + 6.5f;
        if (separate) {
            float logoBlockWidth = 19.0f;
            blur.render(ShapeProperties.create(matrix, this.getX(), this.getY(), logoBlockWidth, 16.0).round(5.0f).softness(5.0f).thickness(0.0f).quality(50.0f).outlineColor(0).color(ColorUtil.DARK_BLACK).build());
            logoFont.drawString(matrix, LOGO_ICON, (float)this.getX() + 4.5f, (float)this.getY() + 5.6f, Hud.getInstance().themeColor());
            float userBlockX = (float)this.getX() + logoBlockWidth + 2.0f;
            float userBlockWidth = 4.5f + userIconWidth + 3.0f + titleFont.getStringWidth(name) + 4.5f;
            blur.render(ShapeProperties.create(matrix, userBlockX, this.getY(), userBlockWidth, 16.0).round(5.0f).softness(5.0f).thickness(0.0f).quality(50.0f).outlineColor(0).color(ColorUtil.DARK_BLACK).build());
            iconFont.drawString(matrix, USER_ICON, userBlockX + 4.5f, y - 0.5f, ColorUtil.getClientColor());
            titleFont.drawString(matrix, name, userBlockX + 4.5f + userIconWidth + 3.0f, y, ColorUtil.getText());
            x = userBlockX + userBlockWidth;
        } else {
            float titleBlockWidth = 22.0f + userIconWidth + 3.0f + titleFont.getStringWidth(name) + 4.5f;
            blur.render(ShapeProperties.create(matrix, this.getX(), this.getY(), this.getWidth(), 16.0).round(5.0f).softness(5.0f).thickness(0.0f).quality(50.0f).outlineColor(0).color(ColorUtil.DARK_BLACK).build());
            x = (float)this.getX() + 4.5f;
            logoFont.drawString(matrix, LOGO_ICON, x, (float)this.getY() + 5.6f, Hud.getInstance().themeColor());
            float iconDividerX = x + 10.0f + 3.0f;
            rectangle.render(ShapeProperties.create(matrix, iconDividerX, (float)this.getY() + 3.5f, 0.5, 9.0).round(5.0f).color(ColorUtil.getOutline(0.75f, 0.5f)).build());
            x = iconDividerX + 4.0f;
            iconFont.drawString(matrix, USER_ICON, x, y - 0.5f, ColorUtil.getClientColor());
            titleFont.drawString(matrix, name, x += userIconWidth + 3.0f, y, ColorUtil.getText());
            rectangle.render(ShapeProperties.create(matrix, x += titleFont.getStringWidth(name) + 4.0f, (float)this.getY() + 3.5f, 0.5, 9.0).round(5.0f).color(ColorUtil.getOutline(0.75f, 0.5f)).build());
            x += 4.5f;
        }
        if (Hud.getInstance().watermarkMetrics.isSelected("FPS")) {
            x = this.drawMetric(matrix, iconFont, FPS_ICON, x, y, fps, font, 0.0f, !separate, separate);
        }
        if (Hud.getInstance().watermarkMetrics.isSelected("TPS")) {
            x = this.drawMetric(matrix, iconFont, TPS_ICON, x, y, tps, font, 0.0f, !separate, separate);
        }
        if (Hud.getInstance().watermarkMetrics.isSelected("PING")) {
            x = this.drawMetric(matrix, iconFont, PING_ICON, x, y, ping, font, 0.0f, !separate, separate);
        }
        boolean bl = timeSeparator = !separate && singleInfoMetric && (showBps || showCoords);
        if (Hud.getInstance().watermarkMetrics.isSelected("TIME")) {
            x = this.drawMetric(matrix, timeIconFont, TIME_ICON, x, y, time, font, 1.0f, timeSeparator, separate);
        }
        if (singleInfoMetric && showBps) {
            x = this.drawMetric(matrix, iconFont, BPS_ICON, x, y, "BPS " + bps, font, 0.0f, false, separate);
        }
        if (singleInfoMetric && showCoords) {
            x = this.drawMetric(matrix, iconFont, XYZ_ICON, x, y, "XYZ " + xyz, font, 0.0f, false, separate);
        }
        float mainWidth = x - (float)this.getX() + -4.0f;
        this.drawPlayerInfo(matrix, font, iconFont, mainWidth);
        this.setWidth((int)Math.ceil(mainWidth));
        this.setHeight((int)Math.ceil(showBps && showCoords ? 34.0 : 16.0));
    }

    private void drawHud2(class_332 context) {
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        FontRenderer textFont = Fonts.getSize(12, Fonts.Type.INTER_BOLD);
        FontRenderer iconFont = Fonts.getSize(12, Fonts.Type.COMBINED_ICONS);
        FontRenderer logoFont = Fonts.getSize(13, Fonts.Type.COMBINED_ICONS);
        Hud hud = Hud.getInstance();
        int accent = hud.themeColor();
        int textColor = ColorUtil.getText();
        String customName = hud.watermarkNameSetting.getText();
        String profileName = User.getInstance().profile("username");
        String name = customName != null && !customName.isBlank() ? customName : (profileName != null && !profileName.isBlank() ? profileName : "DettexClient");
        int pingValue = Watermark.mc.field_1724 != null && mc.method_1562() != null && mc.method_1562().method_2871(Watermark.mc.field_1724.method_5667()) != null ? mc.method_1562().method_2871(Watermark.mc.field_1724.method_5667()).method_2959() : 0;
        String ping = pingValue + " ms";
        String fps = this.fpsCount + " FPS";
        class_2338 pos = Watermark.mc.field_1724 == null ? class_2338.field_10980 : Watermark.mc.field_1724.method_24515();
        String xyz = pos.method_10263() + " " + pos.method_10264() + " " + pos.method_10260();
        long now = System.currentTimeMillis();
        if (now - this.lastTimeUpdate >= 1000L) {
            this.cachedTime = LocalTime.now().format(TIME_FORMAT);
            this.lastTimeUpdate = now;
        }
        String time = this.cachedTime;
        float h = 18.0f;
        float radius = 9.0f;
        float x = this.getX();
        float y = this.getY();
        float pad = 7.0f;
        float faceSize = 12.0f;
        float groupGap = 8.0f;
        float iconGap = 3.5f;
        float logoW = logoFont.getStringWidth(LOGO_ICON);
        float nameW = textFont.getStringWidth(name);
        float pingW = iconFont.getStringWidth(PING_ICON);
        float fpsW = iconFont.getStringWidth(FPS_ICON);
        float xyzW = iconFont.getStringWidth(XYZ_ICON);
        float timeW = iconFont.getStringWidth(TIME_ICON);
        float pingTextW = textFont.getStringWidth(ping);
        float fpsTextW = textFont.getStringWidth(fps);
        float xyzTextW = textFont.getStringWidth(xyz);
        float timeTextW = textFont.getStringWidth(time);
        float w = pad;
        w += logoW + 7.0f;
        w += faceSize + 5.0f;
        w += nameW + groupGap;
        w += pingW + iconGap + pingTextW + groupGap;
        w += fpsW + iconGap + fpsTextW + groupGap;
        w += xyzW + iconGap + xyzTextW + groupGap;
        w += timeW + iconGap + timeTextW + pad;
        this.setWidth((int)Math.ceil(w));
        this.setHeight((int)Math.ceil(h));
        blur.render(ShapeProperties.create(matrix, x, y, w, h).round(radius).softness(4.0f).thickness(0.0f).quality(50.0f).outlineColor(0).color(ColorUtil.getColor(20, 20, 25, 0.5f)).build());
        rectangle.render(ShapeProperties.create(matrix, x, y, w, h).round(radius).softness(1.0f).thickness(1.0f).quality(50.0f).outlineColor(ColorUtil.getOutline(0.35f)).color(0).build());
        float cy = y + h / 2.0f;
        float tx = x + pad;
        HudIconRail.drawText(matrix, logoFont, LOGO_ICON, tx, y, h, accent);
        tx += logoW + 7.0f;
        this.drawHeadHud2(context, tx, cy - faceSize * 0.5f, faceSize);
        tx += faceSize + 5.0f;
        HudIconRail.drawText(matrix, textFont, name, tx, y, h, textColor);
        tx += nameW + groupGap;
        HudIconRail.drawText(matrix, iconFont, PING_ICON, tx, y, h, accent);
        tx += pingW + iconGap;
        HudIconRail.drawText(matrix, textFont, ping, tx, y, h, textColor);
        tx += pingTextW + groupGap;
        HudIconRail.drawText(matrix, iconFont, FPS_ICON, tx, y, h, accent);
        tx += fpsW + iconGap;
        HudIconRail.drawText(matrix, textFont, fps, tx, y, h, textColor);
        tx += fpsTextW + groupGap;
        HudIconRail.drawText(matrix, iconFont, XYZ_ICON, tx, y, h, accent);
        tx += xyzW + iconGap;
        HudIconRail.drawText(matrix, textFont, xyz, tx, y, h, textColor);
        tx += xyzTextW + groupGap;
        HudIconRail.drawText(matrix, iconFont, TIME_ICON, tx, y, h, accent);
        tx += timeW + iconGap;
        HudIconRail.drawText(matrix, textFont, time, tx, y, h, textColor);
    }

    private void drawHeadHud2(class_332 context, float fx, float fy, float size) {
        if (Watermark.mc.field_1724 == null) {
            return;
        }
        class_897 baseRenderer = mc.method_1561().method_3953((class_1297)Watermark.mc.field_1724);
        if (!(baseRenderer instanceof class_922)) {
            return;
        }
        class_922 renderer = (class_922)baseRenderer;
        class_10042 state = (class_10042)renderer.method_62425((class_1297)Watermark.mc.field_1724, tickCounter.getTickProgress(false));
        class_2960 textureLocation = renderer.method_3885(state);
        Render2DUtil.drawRoundedTexture(context, textureLocation, fx, fy, size, size * 0.5f, 8.0f, 8.0f, 8, 8, 64, 64, -1);
    }

    private int centeredYBelowBossBar() {
        if (Watermark.mc.field_1705.method_1740().field_2060.isEmpty()) {
            return 2;
        }
        int bossCount = Watermark.mc.field_1705.method_1740().field_2060.size();
        int bossBottom = 0 + (bossCount - 1) * 22 + 18;
        return bossBottom + 1;
    }

    private void drawPlayerInfo(class_4587 matrix, FontRenderer font, FontRenderer iconFont, float mainWidth) {
        float targetBlockX;
        if (Watermark.mc.field_1724 == null) {
            return;
        }
        boolean showBps = Hud.getInstance().watermarkMetrics.isSelected("BPS");
        boolean showCoords = Hud.getInstance().watermarkMetrics.isSelected("COORDS");
        if (!showBps || !showCoords) {
            return;
        }
        class_2338 pos = Objects.requireNonNull(Watermark.mc.field_1724).method_24515();
        String bps = String.valueOf(MathUtil.round(MovingUtil.getSpeedSqrt((class_1297)Watermark.mc.field_1724) * 20.0, 0.1f));
        String xyz = pos.method_10263() + ", " + pos.method_10264() + ", " + pos.method_10260();
        float blockX = this.getX();
        float blockY = (float)this.getY() + 16.0f + 2.0f;
        float padding = 4.5f;
        float bpsTextWidth = font.getStringWidth("BPS " + bps);
        float xyzTextWidth = font.getStringWidth("XYZ " + xyz);
        if (Hud.getInstance().watermarkSeparateElements.isValue()) {
            float separateX;
            float bpsWidth = showBps ? padding + 9.5f + 2.0f + bpsTextWidth + padding : 0.0f;
            float xyzWidth = showCoords ? padding + 9.5f + 2.0f + xyzTextWidth + padding : 0.0f;
            float totalWidth = bpsWidth + xyzWidth + (showBps && showCoords ? 2.0f : 0.0f);
            float f = separateX = Hud.getInstance().watermarkCentered.isValue() ? ((float)window.getScaledWidth() - totalWidth) / 2.0f : (float)this.getX();
            if (showBps) {
                this.drawInfoMetric(matrix, font, iconFont, separateX, blockY, bpsWidth, BPS_ICON, "BPS " + bps);
                separateX += bpsWidth + (showCoords ? 2.0f : 0.0f);
            }
            if (showCoords) {
                this.drawInfoMetric(matrix, font, iconFont, separateX, blockY, xyzWidth, XYZ_ICON, "XYZ " + xyz);
            }
            return;
        }
        float width = padding + (showBps ? 11.5f + bpsTextWidth : 0.0f) + (showBps && showCoords ? 8.0f : 0.0f) + (showCoords ? 11.5f + xyzTextWidth : 0.0f) + padding;
        blockX = targetBlockX = this.getSyncedInfoBlockX(mainWidth, width);
        rectangle.render(ShapeProperties.create(matrix, blockX, blockY, width, 16.0).round(5.0f).softness(1.0f).thickness(1.2f).quality(50.0f).color(0).build());
        blur.render(ShapeProperties.create(matrix, blockX, blockY, width, 16.0).round(5.0f).softness(5.0f).thickness(0.0f).quality(50.0f).outlineColor(0).color(ColorUtil.DARK_BLACK).build());
        float x = blockX + padding;
        float textY = blockY + 6.5f;
        if (showBps) {
            iconFont.drawString(matrix, BPS_ICON, x, blockY + 6.0f, ColorUtil.getClientColor());
            font.drawString(matrix, "BPS " + bps, x += 11.5f, textY, ColorUtil.getText());
            x += font.getStringWidth("BPS " + bps) + (showCoords ? 8.0f : 0.0f);
        }
        if (showBps && showCoords) {
            rectangle.render(ShapeProperties.create(matrix, x - 4.0f, blockY + 3.5f, 0.5, 9.0).round(5.0f).color(ColorUtil.getOutline(0.75f, 0.5f)).build());
        }
        if (showCoords) {
            iconFont.drawString(matrix, XYZ_ICON, x, blockY + 6.0f, ColorUtil.getClientColor());
            font.drawString(matrix, "XYZ " + xyz, x += 11.5f, textY, ColorUtil.getText());
        }
    }

    private void drawInfoMetric(class_4587 matrix, FontRenderer font, FontRenderer iconFont, float x, float y, float width, String icon, String text) {
        rectangle.render(ShapeProperties.create(matrix, x, y, width, 16.0).round(5.0f).softness(1.0f).thickness(1.2f).quality(50.0f).color(0).build());
        blur.render(ShapeProperties.create(matrix, x, y, width, 16.0).round(5.0f).softness(5.0f).thickness(0.0f).quality(50.0f).outlineColor(0).color(ColorUtil.DARK_BLACK).build());
        float contentX = x + 4.5f;
        iconFont.drawString(matrix, icon, contentX, y + 6.0f, ColorUtil.getClientColor());
        font.drawString(matrix, text, contentX + 9.5f + 2.0f, y + 6.5f, ColorUtil.getText());
    }

    private float getSyncedInfoBlockX(float mainWidth, float infoWidth) {
        if (Hud.getInstance().watermarkCentered.isValue()) {
            return ((float)window.getScaledWidth() - infoWidth) / 2.0f;
        }
        return this.getX();
    }

    private float drawMetric(class_4587 matrix, FontRenderer iconFont, String icon, float x, float y, String value, FontRenderer font) {
        return this.drawMetric(matrix, iconFont, icon, x, y, value, font, 0.0f);
    }

    private float drawMetric(class_4587 matrix, FontRenderer iconFont, String icon, float x, float y, String value, FontRenderer font, float iconYOffset) {
        return this.drawMetric(matrix, iconFont, icon, x, y, value, font, iconYOffset, true);
    }

    private float drawMetric(class_4587 matrix, FontRenderer iconFont, String icon, float x, float y, String value, FontRenderer font, float iconYOffset, boolean separator) {
        return this.drawMetric(matrix, iconFont, icon, x, y, value, font, iconYOffset, separator, false);
    }

    private float drawMetric(class_4587 matrix, FontRenderer iconFont, String icon, float x, float y, String value, FontRenderer font, float iconYOffset, boolean separator, boolean separate) {
        if (separate) {
            float blockWidth = 16.0f + font.getStringWidth(value) + 4.5f;
            blur.render(ShapeProperties.create(matrix, x += 2.0f, this.getY(), blockWidth, 16.0).round(5.0f).softness(5.0f).thickness(0.0f).quality(50.0f).outlineColor(0).color(ColorUtil.DARK_BLACK).build());
            iconFont.drawString(matrix, icon, x += 4.5f, (float)this.getY() + 6.0f + iconYOffset, ColorUtil.getClientColor());
            float textX = x + 9.5f + 2.0f;
            font.drawString(matrix, value, textX, y, ColorUtil.getText());
            return x - 4.5f + blockWidth;
        }
        iconFont.drawString(matrix, icon, x, (float)this.getY() + 6.0f + iconYOffset, ColorUtil.getClientColor());
        float textX = x + 9.5f + 2.0f;
        font.drawString(matrix, value, textX, y, ColorUtil.getText());
        float nextX = textX + font.getStringWidth(value) + 8.0f;
        if (separator) {
            rectangle.render(ShapeProperties.create(matrix, nextX - 4.0f, (float)this.getY() + 3.5f, 0.5, 9.0).round(5.0f).color(ColorUtil.getOutline(0.75f, 0.5f)).build());
        }
        return nextX;
    }
}

