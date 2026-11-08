/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.features.draggables;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import ru.recoin.api.feature.draggable.AbstractDraggable;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.other.StringUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.core.Main;
import ru.recoin.implement.features.draggables.HudIconRail;
import ru.recoin.implement.features.modules.render.Hud;

public class HotKeys
extends AbstractDraggable {
    private static final int HUD_ICON_SIZE = 15;
    private static final float TEXT_INSET = 2.0f;
    private final List<Module> keysList = new ArrayList<Module>();

    public HotKeys() {
        super("Hot Keys", 300, 10, 100, 23, true);
    }

    @Override
    public boolean visible() {
        return !this.keysList.isEmpty() || PlayerIntersectionUtil.isChat(HotKeys.mc.field_1755);
    }

    @Override
    public void tick() {
        this.keysList.clear();
        for (Module module : Main.getInstance().getModuleProvider().getModules()) {
            if (module.getKey() == -1 || module.getAnimation().getOutput().floatValue() == 0.0f) continue;
            this.keysList.add(module);
        }
    }

    @Override
    public void drawDraggable(class_332 e) {
        if (Hud.getInstance().isHud2()) {
            this.drawHud2(e);
            return;
        }
        class_4587 matrix = MatrixStackCompat.from(e.method_51448());
        String title = "HotKeys";
        FontRenderer font = Fonts.getSize(15, Fonts.Type.INTER_BOLD);
        FontRenderer fontModule = Fonts.getSize(13, Fonts.Type.INTER_BOLD);
        FontRenderer iconFont = Fonts.getSize(15, Fonts.Type.COMBINED_ICONS);
        int maxWidth = Math.max(HudIconRail.headerWidth(font, title), (int)Math.ceil(26.0f + font.getStringWidth(title) + 4.0f));
        float rows = 0.0f;
        for (Module module : this.keysList) {
            float bindWidth = Math.max(10.0f, fontModule.getStringBounds(StringUtil.getBindName(module.getKey())).width() + 6.0f);
            maxWidth = Math.max(maxWidth, (int)Math.ceil(26.0f + fontModule.getStringWidth(module.getName()) + 4.0f + bindWidth + 4.0f));
            rows += Math.max(0.0f, Math.min(1.0f, module.getAnimation().getOutput().floatValue()));
        }
        this.setWidth(maxWidth);
        this.setHeight(HudIconRail.height(rows));
        HudIconRail.draw(this, matrix, blur, rectangle, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        HudIconRail.drawHeader(this, matrix, font, iconFont, title, "\ue01d", 2.0f);
        float offset = 17.0f;
        for (Module module : this.keysList) {
            String bind = StringUtil.getBindName(module.getKey());
            float rowAlpha = Math.max(0.0f, Math.min(1.0f, module.getAnimation().getOutput().floatValue()));
            float bindWidth = fontModule.getStringBounds(bind).width();
            float rowTop = (float)this.getY() + offset;
            float bindBoxWidth = Math.max(10.0f, bindWidth + 6.0f);
            float bindBoxHeight = 9.0f;
            float bindBoxX = (float)(this.getX() + this.getWidth()) - 4.0f - bindBoxWidth;
            float bindTextX = bindBoxX + (bindBoxWidth - bindWidth) * 0.5f;
            float bindBoxY = rowTop + (9.0f - bindBoxHeight) * 0.5f;
            String categoryIcon = this.categoryIcon(module);
            matrix.method_22903();
            this.withRenderAlpha(rowAlpha, () -> {
                HudIconRail.drawText(matrix, iconFont, categoryIcon, HudIconRail.iconX(this, this.getX(), iconFont.getStringBounds(categoryIcon).width()), rowTop, 9.0f, ColorUtil.getClientColor());
                HudIconRail.drawText(matrix, fontModule, module.getName(), (float)this.getX() + 24.0f + 2.0f, rowTop, 9.0f, ColorUtil.getText());
                rectangle.render(ShapeProperties.create(matrix, bindBoxX, bindBoxY, bindBoxWidth, bindBoxHeight).round(3.0f).softness(1.0f).thickness(0.8f).quality(50.0f).outlineColor(ColorUtil.getOutline(0.8f)).color(ColorUtil.getOutline(0.25f)).build());
                HudIconRail.drawText(matrix, fontModule, bind, bindTextX, rowTop, 9.0f, ColorUtil.getText());
            });
            matrix.method_22909();
            offset += rowAlpha * 11.0f;
        }
    }

    private void drawHud2(class_332 e) {
        class_4587 matrix = MatrixStackCompat.from(e.method_51448());
        FontRenderer headerFont = Fonts.getSize(12, Fonts.Type.INTER_BOLD);
        FontRenderer rowFont = Fonts.getSize(11, Fonts.Type.INTER_BOLD);
        FontRenderer iconFont = Fonts.getSize(12, Fonts.Type.COMBINED_ICONS);
        int accent = Hud.getInstance().themeColor();
        int bg = ColorUtil.getColor(20, 20, 25, 0.47f);
        String headerIcon = "\ue01d";
        float headerH = 14.0f;
        float rowH = 14.0f;
        float width = headerFont.getStringWidth("Hotkeys") + 6.0f + iconFont.getStringWidth(headerIcon) + 4.0f + 6.0f;
        for (Module module : this.keysList) {
            String bind = StringUtil.getBindName(module.getKey());
            float bindW = rowFont.getStringWidth(bind) + 10.0f;
            width = Math.max(width, 6.0f + iconFont.getStringWidth(this.categoryIcon(module)) + 4.0f + rowFont.getStringWidth(module.getName()) + 10.0f + bindW + 6.0f);
        }
        int rows = Math.max(1, this.keysList.size());
        float height = headerH + (float)rows * rowH + 2.0f;
        float x = this.getX();
        float y = this.getY();
        this.setWidth((int)Math.ceil(width));
        this.setHeight((int)Math.ceil(height));
        blur.render(ShapeProperties.create(matrix, x, y, width, height).round(6.0f).softness(3.5f).thickness(0.0f).quality(50.0f).outlineColor(0).color(bg).build());
        rectangle.render(ShapeProperties.create(matrix, x, y, width, height).round(6.0f).softness(1.0f).thickness(1.0f).quality(50.0f).outlineColor(ColorUtil.getOutline(0.3f)).color(0).build());
        iconFont.drawString(matrix, headerIcon, x + 6.0f, y + 2.5f, accent);
        headerFont.drawString(matrix, "Hotkeys", x + 6.0f + iconFont.getStringWidth(headerIcon) + 4.0f, y + 3.0f, ColorUtil.getText());
        rectangle.render(ShapeProperties.create(matrix, x + 5.0f, y + headerH, width - 10.0f, 0.5f).color(ColorUtil.getOutline(0.25f)).build());
        float rowY = y + headerH + 1.0f;
        if (this.keysList.isEmpty()) {
            HudIconRail.drawText(matrix, rowFont, "\u041d\u0435\u0442 \u0431\u0438\u043d\u0434\u043e\u0432", x + 6.0f, rowY, rowH, ColorUtil.getText(0.7f));
            return;
        }
        for (Module module : this.keysList) {
            String bind = StringUtil.getBindName(module.getKey());
            String categoryIcon = this.categoryIcon(module);
            HudIconRail.drawText(matrix, iconFont, categoryIcon, x + 6.0f, rowY, rowH, accent);
            float nameX = x + 6.0f + iconFont.getStringWidth(categoryIcon) + 4.0f;
            HudIconRail.drawText(matrix, rowFont, module.getName(), nameX, rowY, rowH, ColorUtil.getText());
            float bindW = rowFont.getStringWidth(bind);
            HudIconRail.drawText(matrix, rowFont, bind, x + width - 6.0f - bindW, rowY, rowH, accent);
            rowY += rowH;
        }
    }

    private String categoryIcon(Module module) {
        return switch (module.getCategory()) {
            default -> throw new MatchException(null, null);
            case ModuleCategory.COMBAT -> "\ue052";
            case ModuleCategory.MOVEMENT -> "\ue044";
            case ModuleCategory.PLAYER -> "\ue079";
            case ModuleCategory.PVE -> "\ue007";
            case ModuleCategory.RENDER -> "\ue07a";
            case ModuleCategory.MISC -> "\ue023";
        };
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void withRenderAlpha(float alpha, Runnable runnable) {
        float previous = Render2DUtil.getRenderAlpha();
        Render2DUtil.setRenderAlpha(previous * alpha);
        try {
            runnable.run();
        }
        finally {
            Render2DUtil.setRenderAlpha(previous);
        }
    }
}

