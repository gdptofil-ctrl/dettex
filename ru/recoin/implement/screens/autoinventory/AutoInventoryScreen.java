/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_332
 *  net.minecraft.class_3532
 *  net.minecraft.class_437
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.screens.autoinventory;

import java.util.List;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.implement.features.modules.misc.AutoInventory;
import ru.recoin.implement.features.modules.render.Hud;

public final class AutoInventoryScreen
extends class_437
implements QuickImports {
    private static final String LOGO_ICON = "\ue0c6";
    private static final int COLUMNS = 10;
    private static final int PANEL_WIDTH = 590;
    private static final int PANEL_HEIGHT = 338;
    private static final float DISPLAY_SCALE = 0.75f;
    private static final int HEADER_HEIGHT = 40;
    private static final int GRID_SLOT = 28;
    private static final int GRID_GAP = 4;
    private static final int TEXT = -1446923;
    private static final int MUTED = -9143926;
    private static final long OPEN_TIME = 180L;
    private final AutoInventory owner;
    private final long openedAt = System.currentTimeMillis();
    private AutoInventory.Entry focusedEntry;
    private AutoInventory.Entry hoveredEntry;
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;

    public AutoInventoryScreen(AutoInventory owner) {
        super((class_2561)class_2561.method_43470((String)"AutoInventory Collector"));
        this.owner = owner;
        this.focusedEntry = owner.catalog().stream().filter(entry -> owner.selected(entry.name())).findFirst().orElse(owner.catalog().isEmpty() ? null : owner.catalog().getFirst());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        Render2DUtil.setActiveContext(context);
        Render2DUtil.setQueuedShapeActive(true);
        try {
            this.panelWidth = Math.min(590, Math.round(((float)this.field_22789 - 20.0f) / 0.75f));
            this.panelHeight = Math.min(338, Math.round(((float)this.field_22790 - 20.0f) / 0.75f));
            this.left = Math.round((this.virtualWidth() - (float)this.panelWidth) / 2.0f);
            this.top = Math.round((this.virtualHeight() - (float)this.panelHeight) / 2.0f);
            float scale = this.openScale() * 0.75f;
            int virtualMouseX = this.toVirtualX(mouseX);
            int virtualMouseY = this.toVirtualY(mouseY);
            context.method_51448().pushMatrix();
            context.method_51448().translate((float)this.field_22789 / 2.0f, (float)this.field_22790 / 2.0f);
            context.method_51448().scale(scale, scale);
            context.method_51448().translate(-this.virtualWidth() / 2.0f, -this.virtualHeight() / 2.0f);
            try {
                this.renderCollector(context, MatrixStackCompat.from(context.method_51448()), virtualMouseX, virtualMouseY);
            }
            finally {
                context.method_51448().popMatrix();
            }
            super.method_25394(context, mouseX, mouseY, delta);
        }
        finally {
            blur.setLiveMode(false);
            Render2DUtil.setActiveContext(null);
            Render2DUtil.setQueuedShapeActive(false);
        }
    }

    private void renderCollector(class_332 context, class_4587 matrix, int mouseX, int mouseY) {
        int accent = ColorUtil.getClientColor();
        Layout layout = this.layout();
        this.hoveredEntry = null;
        this.owner.refreshCatalogIcons();
        blur.beginFrame();
        this.drawPanel(matrix, this.left, this.top, this.panelWidth, this.panelHeight);
        this.drawImmediateRect(matrix, (float)this.left + 14.0f, this.top + 40, (float)this.panelWidth - 28.0f, 1.0f, 0.5f, 0.0f, 608059986);
        this.drawImmediateRect(matrix, layout.dividerX, (float)(this.top + 40) + 13.0f, 1.0f, (float)(this.panelHeight - 40) - 26.0f, 0.5f, 0.0f, 608059986);
        this.drawCollectorFrames(matrix, layout, accent);
        this.drawHeaderShapes(matrix, accent);
        this.drawGridShapesAndItems(context, matrix, layout, mouseX, mouseY, accent);
        this.drawDetailShapesAndItems(context, matrix, layout, mouseX, mouseY, accent);
        context.method_73199();
        this.drawSelectedItemIndicators(matrix, layout, accent);
        this.drawHeaderText(matrix, accent);
        this.drawGridText(matrix, layout);
        this.drawDetailText(matrix, layout, accent);
        FontRenderer.flushQueuedText(context);
        if (this.hoveredEntry != null) {
            this.drawTooltip(context, matrix, mouseX, mouseY);
        }
    }

    private void drawCollectorFrames(class_4587 matrix, Layout layout, int accent) {
        float sectionY = (float)(this.top + 40) + 5.0f;
        float sectionH = (float)(this.panelHeight - 40) - 15.0f;
        int outline = ColorUtil.multAlpha(accent, 0.88f);
        this.drawImmediateOutline(matrix, (float)this.left + 9.0f, sectionY, layout.dividerX - (float)this.left - 16.0f, sectionH, 9.0f, 2.3f, outline);
        this.drawImmediateOutline(matrix, layout.dividerX + 7.0f, sectionY, (float)(this.left + this.panelWidth) - layout.dividerX - 16.0f, sectionH, 9.0f, 2.3f, outline);
        this.drawImmediateOutline(matrix, layout.gridX - 5.0f, (float)(this.top + 40) + 5.0f, 72.0f, 21.0f, 7.0f, 2.0f, ColorUtil.multAlpha(accent, 0.94f));
        this.drawImmediateOutline(matrix, layout.gridX + 74.0f, (float)(this.top + 40) + 5.0f, 226.0f, 21.0f, 7.0f, 2.0f, ColorUtil.multAlpha(accent, 0.86f));
        this.drawImmediateOutline(matrix, layout.gridX - 5.0f, (float)(this.top + this.panelHeight) - 27.0f, 230.0f, 21.0f, 7.0f, 2.0f, ColorUtil.multAlpha(accent, 0.82f));
    }

    private void drawHeaderShapes(class_4587 matrix, int accent) {
        this.drawImmediateOutline(matrix, (float)this.left + 15.0f, (float)this.top + 8.0f, 24.0f, 24.0f, 7.0f, ColorUtil.multAlpha(accent, 0.72f));
        this.drawImmediateRect(matrix, (float)this.left + 51.0f, (float)this.top + 11.0f, 1.0f, 18.0f, 0.5f, 0.0f, 944262494);
    }

    private void drawGridShapesAndItems(class_332 context, class_4587 matrix, Layout layout, int mouseX, int mouseY, int accent) {
        List<AutoInventory.Entry> entries = this.owner.catalog();
        for (int index = 0; index < entries.size(); ++index) {
            AutoInventory.Entry entry = entries.get(index);
            float x = layout.gridX + (float)(index % 10 * 32);
            float y = layout.gridY + (float)(index / 10 * 32);
            boolean focused = entry == this.focusedEntry;
            boolean selected = this.owner.selected(entry.name());
            boolean hovered = AutoInventoryScreen.inside(mouseX, mouseY, x, y, 28.0, 28.0);
            if (hovered) {
                this.hoveredEntry = entry;
            }
            if (focused || hovered) {
                this.drawImmediateRect(matrix, x, y, 28.0f, 28.0f, 8.0f, 0.2f, focused ? 387719216 : 253501488);
            }
            this.drawImmediateOutline(matrix, x, y, 28.0f, 28.0f, 8.0f, focused ? ColorUtil.multAlpha(accent, 0.72f) : ColorUtil.multAlpha(Hud.getInstance().themeOutlineColor(), 0.55f));
            context.method_51428(this.owner.iconStack(entry), Math.round(x + 6.0f), Math.round(y + 6.0f), 200);
        }
    }

    private void drawSelectedItemIndicators(class_4587 matrix, Layout layout, int accent) {
        List<AutoInventory.Entry> entries = this.owner.catalog();
        for (int index = 0; index < entries.size(); ++index) {
            AutoInventory.Entry entry = entries.get(index);
            if (!this.owner.selected(entry.name())) continue;
            float x = layout.gridX + (float)(index % 10 * 32);
            float y = layout.gridY + (float)(index / 10 * 32);
            this.drawImmediateRect(matrix, x + 28.0f - 7.5f, y + 1.5f, 6.0f, 6.0f, 3.0f, 0.0f, -703919085);
            this.drawImmediateRect(matrix, x + 28.0f - 6.5f, y + 2.5f, 4.0f, 4.0f, 2.0f, 0.0f, accent);
        }
    }

    private void drawDetailShapesAndItems(class_332 context, class_4587 matrix, Layout layout, int mouseX, int mouseY, int accent) {
        if (this.focusedEntry == null) {
            return;
        }
        float x = layout.detailX;
        float w = layout.detailW;
        float titleY = layout.detailY;
        this.drawImmediateOutline(matrix, x, titleY, 34.0f, 34.0f, 8.0f, ColorUtil.multAlpha(accent, 0.72f));
        context.method_51428(this.owner.iconStack(this.focusedEntry), Math.round(x + 9.0f), Math.round(titleY + 8.0f), 220);
        this.drawToggle(matrix, x + w - 31.0f, titleY + 8.0f, this.owner.selected(this.focusedEntry.name()), accent);
        float amountY = titleY + 48.0f;
        this.drawControlRow(matrix, x, amountY, w, mouseX, mouseY, accent);
        this.drawSmallButton(matrix, x + w - 62.0f, amountY + 5.0f, 20.0f, 18.0f, AutoInventoryScreen.inside(mouseX, mouseY, x + w - 62.0f, amountY + 5.0f, 20.0, 18.0), accent);
        this.drawSmallButton(matrix, x + w - 24.0f, amountY + 5.0f, 20.0f, 18.0f, AutoInventoryScreen.inside(mouseX, mouseY, x + w - 24.0f, amountY + 5.0f, 20.0, 18.0), accent);
        List<String> options = this.enchantOptions(this.focusedEntry);
        float rowY = amountY + 43.0f;
        for (String option : options) {
            boolean selected = this.enchantSelected(this.focusedEntry, option);
            boolean hovered = AutoInventoryScreen.inside(mouseX, mouseY, x, rowY, w, 28.0);
            this.drawOptionRow(matrix, x, rowY, w, selected, hovered, accent);
            this.drawToggle(matrix, x + w - 31.0f, rowY + 9.5f, selected, accent);
            rowY += 34.0f;
        }
        if (this.focusedEntry.filter() == AutoInventory.Filter.ARMOR) {
            boolean selected = this.owner.rejectThorns.isValue();
            boolean hovered = AutoInventoryScreen.inside(mouseX, mouseY, x, rowY, w, 28.0);
            this.drawOptionRow(matrix, x, rowY, w, selected, hovered, -1811608);
            this.drawToggle(matrix, x + w - 31.0f, rowY + 9.5f, selected, -1811608);
            selected = this.owner.repairedArmor.isValue();
            hovered = AutoInventoryScreen.inside(mouseX, mouseY, x, rowY += 34.0f, w, 28.0);
            this.drawOptionRow(matrix, x, rowY, w, selected, hovered, accent);
            this.drawToggle(matrix, x + w - 31.0f, rowY + 9.5f, selected, accent);
        }
    }

    private void drawHeaderText(class_4587 matrix, int accent) {
        FontRenderer logo = Fonts.getMenuSize(23, Fonts.Type.COMBINED_ICONS);
        FontRenderer site = Fonts.getMenuSize(12, Fonts.Type.INTER_BOLD);
        logo.drawCenteredString(matrix, LOGO_ICON, (float)this.left + 27.0f, (float)this.top + 12.5f, accent);
        site.drawString(matrix, "DettexClient", (float)this.left + 62.0f, (float)this.top + 15.0f, -4670005);
    }

    private void drawGridText(class_4587 matrix, Layout layout) {
        FontRenderer section = Fonts.getMenuSize(11, Fonts.Type.INTER_BOLD);
        FontRenderer small = Fonts.getMenuSize(10, Fonts.Type.INTER_BOLD);
        section.drawString(matrix, "\u041f\u0420\u0415\u0414\u041c\u0415\u0422\u042b", layout.gridX, (float)(this.top + 40) + 12.0f, -1446923);
        small.drawString(matrix, "\u041b\u041a\u041c \u2014 \u0432\u043a\u043b/\u0432\u044b\u043a\u043b   \u2022   \u041f\u041a\u041c \u2014 \u043e\u0442\u043a\u0440\u044b\u0442\u044c", layout.gridX + 83.0f, (float)(this.top + 40) + 13.0f, -9143926);
        small.drawString(matrix, "\u0410\u043a\u0442\u0438\u0432\u043d\u044b\u0435 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b \u043e\u0442\u043c\u0435\u0447\u0435\u043d\u044b \u0442\u043e\u0447\u043a\u043e\u0439", layout.gridX, (float)(this.top + this.panelHeight) - 19.0f, -9143926);
    }

    private void drawDetailText(class_4587 matrix, Layout layout, int accent) {
        if (this.focusedEntry == null) {
            return;
        }
        FontRenderer font = Fonts.getMenuSize(12, Fonts.Type.INTER_BOLD);
        FontRenderer small = Fonts.getMenuSize(10, Fonts.Type.INTER_BOLD);
        float x = layout.detailX;
        float w = layout.detailW;
        float titleY = layout.detailY;
        font.drawString(matrix, this.ellipsis(this.focusedEntry.name(), font, w - 75.0f), x + 43.0f, titleY + 7.0f, -1446923);
        small.drawString(matrix, this.owner.selected(this.focusedEntry.name()) ? "\u0414\u043e\u0431\u0430\u0432\u043b\u0435\u043d \u0432 \u0441\u0431\u043e\u0440\u0449\u0438\u043a" : "\u041d\u0435 \u0441\u043e\u0431\u0438\u0440\u0430\u0435\u0442\u0441\u044f", x + 43.0f, titleY + 20.0f, this.owner.selected(this.focusedEntry.name()) ? accent : -9143926);
        float amountY = titleY + 48.0f;
        font.drawString(matrix, "\u041a\u043e\u043b\u0438\u0447\u0435\u0441\u0442\u0432\u043e", x + 10.0f, amountY + 10.0f, -3880748);
        small.drawCenteredString(matrix, "\u2212", x + w - 52.0f, amountY + 11.0f, -2499353);
        small.drawCenteredString(matrix, String.valueOf(this.owner.target(this.focusedEntry.name())), x + w - 32.5f, amountY + 11.0f, -1446923);
        small.drawCenteredString(matrix, "+", x + w - 14.0f, amountY + 11.0f, -2499353);
        List<String> options = this.enchantOptions(this.focusedEntry);
        float rowY = amountY + 43.0f;
        if (options.isEmpty()) {
            small.drawString(matrix, "\u0414\u043b\u044f \u044d\u0442\u043e\u0433\u043e \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430 \u043d\u0435\u0442 \u0444\u0438\u043b\u044c\u0442\u0440\u043e\u0432 \u0447\u0430\u0440.", x + 10.0f, rowY + 5.0f, -9143926);
            small.drawString(matrix, "\u0421\u0431\u043e\u0440\u0449\u0438\u043a \u043f\u0440\u043e\u0432\u0435\u0440\u0438\u0442 \u0438\u043c\u044f \u0438 \u0442\u0438\u043f \u0432\u0435\u0449\u0438.", x + 10.0f, rowY + 19.0f, -9143926);
            return;
        }
        small.drawString(matrix, "\u0424\u0418\u041b\u042c\u0422\u0420\u042b \u0427\u0410\u0420", x + 10.0f, rowY - 7.0f, -9143926);
        for (String option : options) {
            font.drawString(matrix, option, x + 10.0f, rowY + 5.0f, this.enchantSelected(this.focusedEntry, option) ? -1446923 : -7499103);
            rowY += 34.0f;
        }
        if (this.focusedEntry.filter() == AutoInventory.Filter.ARMOR) {
            font.drawString(matrix, "\u041d\u0435 \u0431\u0440\u0430\u0442\u044c \u0441 \u0428\u0438\u043f\u0430\u043c\u0438", x + 10.0f, rowY + 10.0f, this.owner.rejectThorns.isValue() ? -9507 : -6648944);
            font.drawString(matrix, "\u0422\u043e\u043b\u044c\u043a\u043e \u043f\u043e\u0447\u0438\u043d\u0435\u043d\u043d\u0430\u044f", x + 10.0f, (rowY += 34.0f) + 10.0f, this.owner.repairedArmor.isValue() ? -1446923 : -6648944);
        }
    }

    private void drawControlRow(class_4587 matrix, float x, float y, float w, int mouseX, int mouseY, int accent) {
        boolean hovered = AutoInventoryScreen.inside(mouseX, mouseY, x, y, w, 28.0);
        if (hovered) {
            this.drawImmediateRect(matrix, x, y, w, 28.0f, 7.0f, 0.2f, 404496432);
        }
        this.drawImmediateOutline(matrix, x, y, w, 28.0f, 7.0f, hovered ? ColorUtil.multAlpha(accent, 0.5f) : ColorUtil.multAlpha(Hud.getInstance().themeOutlineColor(), 0.55f));
    }

    private void drawOptionRow(class_4587 matrix, float x, float y, float w, boolean selected, boolean hovered, int color) {
        this.drawImmediateOutline(matrix, x, y, w, 28.0f, 7.0f, selected ? ColorUtil.multAlpha(color, 0.72f) : (hovered ? ColorUtil.multAlpha(Hud.getInstance().themeOutlineColor(), 0.68f) : ColorUtil.multAlpha(Hud.getInstance().themeOutlineColor(), 0.5f)));
    }

    private void drawSmallButton(class_4587 matrix, float x, float y, float w, float h, boolean hovered, int accent) {
        if (hovered) {
            this.drawImmediateRect(matrix, x, y, w, h, 5.0f, 0.2f, ColorUtil.multAlpha(accent, 0.1f));
        }
        this.drawImmediateOutline(matrix, x, y, w, h, 5.0f, hovered ? ColorUtil.multAlpha(accent, 0.7f) : ColorUtil.multAlpha(Hud.getInstance().themeOutlineColor(), 0.54f));
    }

    private void drawToggle(class_4587 matrix, float x, float y, boolean state, int accent) {
        float w = 25.0f;
        float h = 11.0f;
        this.drawImmediateRect(matrix, x, y, w, h, 6.0f, 0.25f, state ? ColorUtil.multAlpha(accent, 0.72f) : -1088083403);
        this.drawImmediateOutline(matrix, x, y, w, h, 6.0f, state ? ColorUtil.multAlpha(accent, 0.95f) : ColorUtil.multAlpha(Hud.getInstance().themeOutlineColor(), 0.62f));
        this.drawImmediateRect(matrix, state ? x + 15.5f : x + 2.5f, y + 2.0f, 7.0f, 7.0f, 3.5f, 0.3f, state ? -1 : -4801851);
    }

    private void drawTooltip(class_332 context, class_4587 matrix, int mouseX, int mouseY) {
        FontRenderer font = Fonts.getMenuSize(11, Fonts.Type.INTER_BOLD);
        float tooltipW = Math.min(260.0f, font.getStringWidth(this.hoveredEntry.name()) + 14.0f);
        float x = Math.min((float)mouseX + 10.0f, this.virtualWidth() - tooltipW - 5.0f);
        float y = Math.min((float)mouseY + 9.0f, this.virtualHeight() - 23.0f);
        this.drawImmediateRect(matrix, x, y, tooltipW, 20.0f, 5.0f, 0.3f, -200603374);
        this.drawImmediateOutline(matrix, x, y, tooltipW, 20.0f, 5.0f, ColorUtil.multAlpha(Hud.getInstance().themeOutlineColor(), 0.7f));
        font.drawString(matrix, this.ellipsis(this.hoveredEntry.name(), font, tooltipW - 12.0f), x + 7.0f, y + 7.0f, -1446923);
        FontRenderer.flushQueuedText(context);
    }

    private List<String> enchantOptions(AutoInventory.Entry entry) {
        if (entry.filter() == AutoInventory.Filter.ARMOR) {
            return List.of("\u0417\u0430\u0449\u0438\u0442\u0430 5", "\u041f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u044c 5", "\u041f\u043e\u0447\u0438\u043d\u043a\u0430");
        }
        if (entry.filter() == AutoInventory.Filter.SWORD) {
            return List.of("\u042f\u0434", "\u041e\u043a\u0438\u0441\u043b\u0435\u043d\u0438\u0435", "\u0417\u0430\u0433\u043e\u0432\u043e\u0440 \u043e\u0433\u043d\u044f 2", "\u041e\u0441\u0442\u0440\u043e\u0442\u0430 7");
        }
        return List.of();
    }

    private boolean enchantSelected(AutoInventory.Entry entry, String option) {
        return entry.filter() == AutoInventory.Filter.ARMOR ? this.owner.armorEnchants.isSelected(option) : this.owner.swordEnchants.isSelected(option);
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        double mouseX = this.toVirtualX(click.comp_4798());
        double mouseY = this.toVirtualY(click.comp_4799());
        int button = click.comp_4800().comp_4801();
        Layout layout = this.layout();
        List<AutoInventory.Entry> entries = this.owner.catalog();
        for (int index = 0; index < entries.size(); ++index) {
            float x = layout.gridX + (float)(index % 10 * 32);
            float y = layout.gridY + (float)(index / 10 * 32);
            if (!AutoInventoryScreen.inside(mouseX, mouseY, x, y, 28.0, 28.0)) continue;
            this.focusedEntry = entries.get(index);
            if (button == 0) {
                this.owner.toggle(this.focusedEntry.name());
            }
            return true;
        }
        if (this.focusedEntry != null) {
            float x = layout.detailX;
            float w = layout.detailW;
            float titleY = layout.detailY;
            if (button == 0 && AutoInventoryScreen.inside(mouseX, mouseY, x + w - 31.0f, titleY + 8.0f, 25.0, 11.0)) {
                this.owner.toggle(this.focusedEntry.name());
                return true;
            }
            float amountY = titleY + 48.0f;
            if (button == 0 && AutoInventoryScreen.inside(mouseX, mouseY, x + w - 62.0f, amountY + 5.0f, 20.0, 18.0)) {
                this.owner.changeTarget(this.focusedEntry, -1);
                return true;
            }
            if (button == 0 && AutoInventoryScreen.inside(mouseX, mouseY, x + w - 24.0f, amountY + 5.0f, 20.0, 18.0)) {
                this.owner.changeTarget(this.focusedEntry, 1);
                return true;
            }
            List<String> options = this.enchantOptions(this.focusedEntry);
            float rowY = amountY + 43.0f;
            for (String option : options) {
                if (button == 0 && AutoInventoryScreen.inside(mouseX, mouseY, x, rowY, w, 28.0)) {
                    if (this.focusedEntry.filter() == AutoInventory.Filter.ARMOR) {
                        this.owner.toggleArmorEnchant(option);
                    } else {
                        this.owner.toggleSwordEnchant(option);
                    }
                    return true;
                }
                rowY += 34.0f;
            }
            if (this.focusedEntry.filter() == AutoInventory.Filter.ARMOR && button == 0 && AutoInventoryScreen.inside(mouseX, mouseY, x, rowY, w, 28.0)) {
                this.owner.rejectThorns.setValue(!this.owner.rejectThorns.isValue());
                return true;
            }
            if (this.focusedEntry.filter() == AutoInventory.Filter.ARMOR && button == 0 && AutoInventoryScreen.inside(mouseX, mouseY, x, rowY += 34.0f, w, 28.0)) {
                this.owner.repairedArmor.setValue(!this.owner.repairedArmor.isValue());
                return true;
            }
        }
        return super.method_25402(click, doubled);
    }

    private Layout layout() {
        float gridX = (float)this.left + 15.0f;
        float gridY = (float)(this.top + 40) + 34.0f;
        float dividerX = (float)this.left + Math.min(378.0f, (float)this.panelWidth * 0.6f);
        float detailX = dividerX + 14.0f;
        float detailW = (float)(this.left + this.panelWidth) - 15.0f - detailX;
        return new Layout(gridX, gridY, dividerX, detailX, detailW, (float)(this.top + 40) + 22.0f);
    }

    private float virtualWidth() {
        return (float)this.field_22789 / 0.75f;
    }

    private float virtualHeight() {
        return (float)this.field_22790 / 0.75f;
    }

    private int toVirtualX(double x) {
        return (int)Math.round(x / 0.75);
    }

    private int toVirtualY(double y) {
        return (int)Math.round(y / 0.75);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void drawPanel(class_4587 matrix, float x, float y, float w, float h) {
        boolean oldHud = Render2DUtil.isHudDecorationActive();
        boolean oldGui = Render2DUtil.isGuiDecorationActive();
        Render2DUtil.setHudDecorationActive(true);
        Render2DUtil.setGuiDecorationActive(true);
        try {
            blur.setLiveMode(true);
            blur.setColorlessMode(true);
            blur.setStyledBlurRadius(92.0f);
            blur.render(ShapeProperties.create(matrix, x, y, w, h).round(8.0f).softness(5.0f).thickness(0.0f).quality(50.0f).outlineColor(0).color(-268435456).build());
        }
        finally {
            blur.resetStyledBlurRadius();
            blur.setColorlessMode(false);
            blur.setLiveMode(false);
            Render2DUtil.setHudDecorationActive(oldHud);
            Render2DUtil.setGuiDecorationActive(oldGui);
        }
        this.drawImmediateOutline(matrix, x, y, w, h, 8.0f, ColorUtil.multAlpha(Hud.getInstance().themeOutlineColor(), 0.72f));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void drawImmediateRect(class_4587 matrix, float x, float y, float w, float h, float round, float softness, int color) {
        boolean queued = Render2DUtil.isQueuedShapeActive();
        Render2DUtil.setQueuedShapeActive(false);
        try {
            rectangle.render(ShapeProperties.create(matrix, x, y, w, h).round(round).softness(softness).quality(18.0f).color(color).build());
        }
        finally {
            Render2DUtil.setQueuedShapeActive(queued);
        }
    }

    private void drawImmediateOutline(class_4587 matrix, float x, float y, float w, float h, float round, int color) {
        this.drawImmediateOutline(matrix, x, y, w, h, round, 2.0f, color);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void drawImmediateOutline(class_4587 matrix, float x, float y, float w, float h, float round, float thickness, int color) {
        boolean queued = Render2DUtil.isQueuedShapeActive();
        Render2DUtil.setQueuedShapeActive(false);
        try {
            rectangle.render(ShapeProperties.create(matrix, x, y, w, h).round(round).softness(0.7f).thickness(thickness).quality(28.0f).outlineColor(color).color(0x74000000).build());
        }
        finally {
            Render2DUtil.setQueuedShapeActive(queued);
        }
    }

    private float openScale() {
        float t = class_3532.method_15363((float)((float)(System.currentTimeMillis() - this.openedAt) / 180.0f), (float)0.0f, (float)1.0f);
        float shifted = t - 1.0f;
        return 1.0f + 1.7f * shifted * shifted * shifted + 0.7f * shifted * shifted;
    }

    private String ellipsis(String value, FontRenderer font, float maxWidth) {
        if (font.getStringWidth(value) <= maxWidth) {
            return value;
        }
        String result = value;
        while (result.length() > 1 && font.getStringWidth(result + "\u2026") > maxWidth) {
            result = result.substring(0, result.length() - 1);
        }
        return result + "\u2026";
    }

    private static boolean inside(double mx, double my, double x, double y, double w, double h) {
        return mx >= x && my >= y && mx <= x + w && my <= y + h;
    }

    public boolean method_25421() {
        return false;
    }

    private record Layout(float gridX, float gridY, float dividerX, float detailX, float detailW, float detailY) {
    }
}

