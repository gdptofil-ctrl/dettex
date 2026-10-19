/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11905
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.screens.autobuy;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import ru.recoin.api.system.animation.Animation;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.common.util.render.ScissorManager;
import ru.recoin.core.Main;
import ru.recoin.implement.features.modules.player.AutoBuy;
import ru.recoin.implement.screens.autobuy.SpecialItemIconRenderer;
import ru.recoin.implement.screens.menu.MenuStyle;

public final class AutoBuyScreen
extends class_437
implements QuickImports {
    private static final int PANEL_W = 300;
    private static final int PANEL_H = 300;
    private static final int COLUMNS = 11;
    private static final int CELL = 24;
    private static final int TITLE_ICON_SIZE = 18;
    private static final int ACTION_ICON_SIZE = 18;
    private static final long OPEN_ANIMATION_MS = 190L;
    private static final long CLOSE_ANIMATION_MS = 165L;
    private static final float BOUNCE_OVERSHOOT = 0.65f;
    private final AutoBuy autoBuy;
    private final Map<String, Float> itemHighlight = new HashMap<String, Float>();
    private final Map<String, HoverTransition> itemHover = new HashMap<String, HoverTransition>();
    private final Map<String, Float> itemEnabledColor = new HashMap<String, Float>();
    private final Map<String, Float> buttonHover = new HashMap<String, Float>();
    private final Map<String, Animation> editorToggleAnimation = new HashMap<String, Animation>();
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;
    private int gridTop;
    private int gridRows;
    private int scrollRow;
    private AutoBuy.CatalogItem focused;
    private String priceInput = "500000";
    private boolean priceFocused;
    private boolean priceDragging;
    private int priceCursor;
    private int priceSelectionAnchor = -1;
    private boolean closing;
    private long openAnimStart = System.currentTimeMillis();
    private long closeAnimStart;
    private AutoBuy.CatalogItem searchAfterClose;
    private boolean embedded;
    private int embeddedX;
    private int embeddedY;
    private int gridColumns = 11;
    private int listWidth;
    private float smoothScrollRow;

    public AutoBuyScreen(AutoBuy autoBuy) {
        super((class_2561)class_2561.method_43470((String)"AutoBuy"));
        this.autoBuy = autoBuy;
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        super.method_25394(context, mouseX, mouseY, delta);
    }

    public void method_25393() {
        if (this.closing && System.currentTimeMillis() - this.closeAnimStart >= 165L) {
            super.method_25419();
            if (this.searchAfterClose != null) {
                this.autoBuy.searchOnAuction(this.searchAfterClose);
            }
            this.searchAfterClose = null;
        }
        super.method_25393();
    }

    public void method_25419() {
        if (this.closing) {
            return;
        }
        this.closing = true;
        this.closeAnimStart = System.currentTimeMillis();
        this.priceFocused = false;
        this.priceDragging = false;
    }

    public void renderOverlay(class_332 context, int mouseX, int mouseY, float delta) {
        this.renderOverlay(context, mouseX, mouseY, delta, false);
    }

    public void renderEmbedded(class_332 context, int mouseX, int mouseY, float delta, int x, int y, int areaWidth, int areaHeight) {
        this.field_22789 = areaWidth;
        this.field_22790 = areaHeight;
        this.embedded = true;
        this.embeddedX = x;
        this.embeddedY = y;
        context.method_51448().pushMatrix();
        context.method_51448().translate((float)x, (float)y);
        this.renderListEditor(context, mouseX - x, mouseY - y);
        context.method_51448().popMatrix();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void renderListEditor(class_332 context, int mouseX, int mouseY) {
        this.panelWidth = this.field_22789;
        this.panelHeight = this.field_22790;
        this.top = 0;
        this.left = 0;
        this.listWidth = Math.round((float)this.field_22789 * 0.43f);
        this.gridTop = 4;
        this.gridRows = Math.max(1, (this.field_22790 - 8) / 32);
        this.gridColumns = 1;
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        FontRenderer font = MenuStyle.font(9.0f);
        FontRenderer small = MenuStyle.font(8.0f);
        List<AutoBuy.CatalogItem> catalog = this.autoBuy.catalog();
        this.scrollRow = Math.max(0, Math.min(this.scrollRow, Math.max(0, catalog.size() - this.gridRows)));
        this.smoothScrollRow = MathUtil.interpolateSmooth(2.0, this.smoothScrollRow, (float)this.scrollRow);
        this.smoothScrollRow = Math.max(0.0f, Math.min(this.smoothScrollRow, (float)Math.max(0, catalog.size() - this.gridRows)));
        MenuStyle.fill(matrix, 0.0f, 0.0f, this.listWidth, this.field_22790, 7.0f, MenuStyle.moduleBackground());
        int rx = this.listWidth + 8;
        int rw = this.field_22789 - rx;
        MenuStyle.fill(matrix, rx, 0.0f, rw, this.field_22790, 7.0f, MenuStyle.moduleBackground());
        ScissorManager scissor = Main.getInstance().getScissorManager();
        scissor.push(matrix.method_23760().method_23761(), 4.0f, this.gridTop, this.listWidth - 8, this.field_22790 - 8);
        context.method_44379(4, this.gridTop, this.listWidth - 4, this.field_22790 - 4);
        try {
            int firstRow = (int)Math.floor(this.smoothScrollRow);
            for (int row = 0; row <= this.gridRows + 1 && firstRow + row < catalog.size(); ++row) {
                boolean current;
                AutoBuy.CatalogItem item = catalog.get(firstRow + row);
                float iy = (float)this.gridTop + ((float)(firstRow + row) - this.smoothScrollRow) * 32.0f;
                boolean bl = current = this.focused != null && this.focused.name().equals(item.name());
                float hover = this.itemHover.computeIfAbsent(item.name(), key -> new HoverTransition()).update(current ? 1.0f : (AutoBuyScreen.inside(mouseX, mouseY, 4.0, iy, this.listWidth - 8, 30.0) ? 0.35f : 0.0f));
                MenuStyle.fill(matrix, 4.0f, iy, this.listWidth - 8, 30.0f, 5.0f, ColorUtil.multAlpha(-7301470, hover * 0.15f));
                context.method_51448().pushMatrix();
                context.method_51448().translate(0.0f, iy - (float)Math.floor(iy));
                SpecialItemIconRenderer.draw(context, item, 9, (int)Math.floor(iy) + 6);
                context.method_51448().popMatrix();
                float target = this.autoBuy.selected(item.name()) ? 1.0f : 0.0f;
                float enabled = Math.clamp(MathUtil.interpolateSmooth(1.0, this.itemEnabledColor.getOrDefault(item.name(), Float.valueOf(target)).floatValue(), target), 0.0f, 1.0f);
                if (Math.abs(enabled - target) < 0.001f) {
                    enabled = target;
                }
                this.itemEnabledColor.put(item.name(), Float.valueOf(enabled));
                int textColor = ColorUtil.overCol(-7301470, -1, enabled);
                font.drawString(matrix, MenuStyle.fit(item.name(), (float)(this.listWidth - 42), 9.0f), 32.0, iy + 5.0f, textColor);
                small.drawString(matrix, "$ " + NumberFormat.getIntegerInstance(Locale.US).format(this.autoBuy.selectedPrices().getOrDefault(item.name(), 500000L)), 32.0, iy + 18.0f, textColor);
            }
        }
        finally {
            context.method_44380();
            scissor.pop();
        }
        if (catalog.size() > this.gridRows) {
            float thumb = Math.max(12.0f, ((float)this.field_22790 - 8.0f) * (float)this.gridRows / (float)catalog.size());
            float sy = 4.0f + ((float)(this.field_22790 - 8) - thumb) * this.smoothScrollRow / (float)Math.max(1, catalog.size() - this.gridRows);
            MenuStyle.fill(matrix, this.listWidth - 3, sy, 1.0f, thumb, 0.5f, -7301470);
        }
        if (this.focused == null) {
            font.drawCenteredString(matrix, "\u0412\u044b\u0431\u0435\u0440\u0438 \u043f\u0440\u0435\u0434\u043c\u0435\u0442", (float)rx + (float)rw / 2.0f, (float)this.field_22790 / 2.0f - 5.0f, -7301470);
            return;
        }
        SpecialItemIconRenderer.draw(context, this.focused, rx + 10, 17);
        MenuStyle.medium(10.0f).drawString(matrix, MenuStyle.fitMedium(this.focused.name(), rw - 67, 10.0f), rx + 33, 22.0, -986118);
        this.drawEditorToggle(matrix, rx + rw - 34, 22.0f, this.autoBuy.selected(this.focused.name()));
        font.drawString(matrix, "\u041c\u0430\u043a\u0441. \u0446\u0435\u043d\u0430", rx + 10, 70.0, -986118);
        MenuStyle.fill(matrix, rx + 10, 87.0f, rw - 20, 23.0f, 4.0f, -1795030008);
        String formatted = AutoBuyScreen.formatPriceDigits(this.priceInput);
        if (this.priceFocused && this.hasPriceSelection()) {
            int first = AutoBuyScreen.formattedCursorIndex(formatted, Math.min(this.priceCursor, this.priceSelectionAnchor));
            int last = AutoBuyScreen.formattedCursorIndex(formatted, Math.max(this.priceCursor, this.priceSelectionAnchor));
            float sx = (float)(rx + 17) + font.getStringWidth("$ " + formatted.substring(0, first));
            MenuStyle.fill(matrix, sx, 93.0f, font.getStringWidth(formatted.substring(first, last)), 11.0f, 1.0f, ColorUtil.multAlpha(MenuStyle.accent(), 0.35f));
        }
        font.drawString(matrix, "$ " + formatted, rx + 17, 94.0, -986118);
        if (this.priceFocused && !this.hasPriceSelection() && System.currentTimeMillis() % 1000L < 500L) {
            float cx = (float)(rx + 17) + font.getStringWidth("$ " + formatted.substring(0, AutoBuyScreen.formattedCursorIndex(formatted, this.priceCursor)));
            MenuStyle.fill(matrix, cx, 92.0f, 0.65f, 12.0f, 0.0f, -986118);
        }
        font.drawString(matrix, "\u0410\u0432\u0442\u043e-\u043f\u0430\u0440\u0441", rx + 10, 135.0, -986118);
        this.drawEditorToggle(matrix, rx + rw - 34, 134.0f, this.autoBuy.autoReparse.isValue());
    }

    private void drawEditorToggle(class_4587 matrix, float x, float y, boolean enabled) {
        String key = y < 100.0f ? "selected:" + this.focused.name() : "autoReparse";
        float active = MenuStyle.toggleProgress(this.editorToggleAnimation, key, enabled);
        MenuStyle.toggle(matrix, x, y, active);
    }

    private boolean handleEditorClick(double mx, double my, int button) {
        if (button != 0) {
            return false;
        }
        if (AutoBuyScreen.inside(mx, my, 4.0, this.gridTop, this.listWidth - 8, this.gridRows * 32)) {
            int index = (int)Math.floor((double)this.smoothScrollRow + (my - (double)this.gridTop) / 32.0);
            if (index < this.autoBuy.catalog().size()) {
                this.focused = this.autoBuy.catalog().get(index);
                this.priceInput = Long.toString(this.autoBuy.selectedPrices().getOrDefault(this.focused.name(), 500000L));
                this.priceCursor = this.priceInput.length();
                this.priceSelectionAnchor = -1;
                this.priceDragging = false;
                this.priceFocused = false;
            }
            return true;
        }
        int rx = this.listWidth + 8;
        int rw = this.field_22789 - rx;
        if (this.focused == null) {
            return true;
        }
        this.priceDragging = this.priceFocused = AutoBuyScreen.inside(mx, my, rx + 10, 87.0, rw - 20, 23.0);
        if (this.priceFocused) {
            this.priceSelectionAnchor = this.priceCursor = this.priceIndexAt(mx);
            return true;
        }
        this.priceSelectionAnchor = -1;
        if (AutoBuyScreen.inside(mx, my, rx + rw - 34, 18.0, 24.0, 22.0)) {
            if (this.autoBuy.selected(this.focused.name())) {
                this.autoBuy.toggle(this.focused);
            } else {
                this.autoBuy.setPrice(this.focused, this.parsePrice());
            }
        } else if (AutoBuyScreen.inside(mx, my, rx + rw - 34, 130.0, 24.0, 22.0)) {
            this.autoBuy.autoReparse.setValue(!this.autoBuy.autoReparse.isValue());
        }
        return true;
    }

    private void renderOverlay(class_332 context, int mouseX, int mouseY, float delta, boolean embedded) {
        String displayedPrice;
        this.panelWidth = Math.min(300, this.field_22789 - 20);
        this.panelHeight = Math.min(300, this.field_22790 - 20);
        if (embedded) {
            this.panelWidth = Math.max(180, this.field_22789);
            this.panelHeight = Math.max(180, this.field_22790);
        }
        this.left = embedded ? 0 : (this.field_22789 - this.panelWidth) / 2;
        this.top = embedded ? 0 : (this.field_22790 - this.panelHeight) / 2;
        this.gridTop = this.top + 33;
        this.gridRows = Math.max(1, (this.panelHeight - 108) / 24);
        this.gridColumns = Math.max(1, Math.min(18, (this.panelWidth - 22) / 24));
        float scale = this.renderScale();
        float centerX = (float)this.left + (float)this.panelWidth / 2.0f;
        float centerY = (float)this.top + (float)this.panelHeight / 2.0f;
        context.method_51448().pushMatrix();
        context.method_51448().translate(0.0f, (1.0f - scale) * 22.0f);
        context.method_51448().rotateAbout(-(1.0f - scale) * 0.035f, centerX, centerY);
        context.method_51448().translate(centerX, centerY);
        context.method_51448().scale(scale, scale);
        context.method_51448().translate(-centerX, -centerY);
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        double guiMouseX = this.logicalMouse(mouseX, centerX);
        double guiMouseY = this.logicalMouse((float)mouseY - (1.0f - scale) * 22.0f, centerY);
        int accent = ColorUtil.getClientColor();
        FontRenderer title = Fonts.getMenuSize(18, Fonts.Type.INTER_BOLD);
        FontRenderer font = Fonts.getMenuSize(15, Fonts.Type.INTER_BOLD);
        FontRenderer small = Fonts.getMenuSize(12, Fonts.Type.INTER_BOLD);
        FontRenderer titleIcon = Fonts.getMenuSize(18, Fonts.Type.COMBINED_ICONS);
        FontRenderer actionIcons = Fonts.getMenuSize(18, Fonts.Type.COMBINED_ICONS);
        FontRenderer actionFont = small;
        if (!embedded) {
            blur.beginFrame();
        }
        this.panel(matrix);
        this.rect(matrix, this.left + 1, this.top + 1, this.panelWidth - 2, 28.0f, 7.0f, 1745883929, 0);
        this.rect(matrix, this.left + 1, this.top + 29, this.panelWidth - 2, 1.0f, 0.0f, -14802388, 0);
        title.drawCenteredString(matrix, "AutoBuy", centerX, (double)this.top + 9.5, -1578509);
        titleIcon.drawString(matrix, "\ue000", centerX - title.getStringWidth("AutoBuy") / 2.0f - titleIcon.getStringWidth("\ue000") - 4.0f, this.top + 10, accent);
        List<AutoBuy.CatalogItem> catalog = this.autoBuy.catalog();
        int cellSize = Math.min(24, (this.panelWidth - 22) / this.gridColumns);
        int gridX = this.left + (this.panelWidth - (cellSize * this.gridColumns - 2)) / 2;
        int totalRows = (catalog.size() + this.gridColumns - 1) / this.gridColumns;
        this.scrollRow = Math.max(0, Math.min(this.scrollRow, Math.max(0, totalRows - this.gridRows)));
        for (int row = 0; row < this.gridRows; ++row) {
            int index;
            for (int col = 0; col < this.gridColumns && (index = (this.scrollRow + row) * this.gridColumns + col) < catalog.size(); ++col) {
                AutoBuy.CatalogItem item = catalog.get(index);
                int x = gridX + col * cellSize;
                int y = this.gridTop + row * 24;
                boolean enabled = this.autoBuy.selected(item.name());
                boolean current = this.focused != null && this.focused.name().equals(item.name());
                boolean hovered = AutoBuyScreen.inside(guiMouseX, guiMouseY, x, y, cellSize - 2, cellSize - 2);
                float highlight = AutoBuyScreen.animate(this.itemHighlight, item.name(), current || enabled ? 1.0f : (hovered ? 0.18f : 0.0f));
                int outline = ColorUtil.overCol(-14473423, accent, highlight);
                int slotBg = enabled ? ColorUtil.multAlpha(accent, 0.22f) : (hovered ? 1612390958 : 940709408);
                this.drawItemFill(matrix, x, y, cellSize - 2, slotBg);
                this.drawItemOutline(matrix, x, y, cellSize - 2, outline);
                SpecialItemIconRenderer.draw(context, item, x + (cellSize - 18) / 2, y + (cellSize - 16) / 2 - 1);
                if (!hovered) continue;
                context.method_51438(this.field_22793, (class_2561)class_2561.method_43470((String)item.name()), (int)guiMouseX, (int)guiMouseY);
            }
        }
        if (totalRows > this.gridRows) {
            float trackX = this.left + this.panelWidth - 9;
            float trackH = this.gridRows * 24 - 3;
            float thumbH = Math.max(18.0f, trackH * (float)this.gridRows / (float)totalRows);
            float thumbY = (float)this.gridTop + (trackH - thumbH) * (float)this.scrollRow / (float)Math.max(1, totalRows - this.gridRows);
            this.rect(matrix, trackX, this.gridTop, 2.5f, trackH, 1.25f, 1716212322, 0);
            this.rect(matrix, trackX, thumbY, 2.5f, thumbH, 1.25f, accent, 0);
        }
        int footerY = this.top + this.panelHeight - 67;
        this.rect(matrix, this.left + 8, footerY, this.panelWidth - 16, 59.0f, 5.0f, -670035175, -1876612298);
        this.drawRoundedOutline(matrix, this.left + 8, footerY, this.panelWidth - 16, 59.0f, 5.0f, -1876612298);
        font.drawString(matrix, this.focused == null ? "\u0412\u044b\u0431\u0435\u0440\u0438 \u043f\u0440\u0435\u0434\u043c\u0435\u0442" : AutoBuyScreen.ellipsis(this.focused.name(), font, 157.0f), this.left + 15, footerY + 8, this.focused == null ? -7235158 : -1972752);
        int fieldX = this.left + 13;
        int fieldY = footerY + 30;
        int fieldW = (this.panelWidth - 136) * 5 / 6;
        this.rect(matrix, fieldX, fieldY, fieldW, 19.0f, 4.0f, -2012475628, this.priceFocused ? ColorUtil.multAlpha(accent, 0.7f) : -14341322);
        this.drawRoundedOutline(matrix, fieldX, fieldY, fieldW, 19.0f, 4.0f, this.priceFocused ? ColorUtil.multAlpha(accent, 0.7f) : -14341322);
        String formattedPrice = AutoBuyScreen.formatPriceDigits(this.priceInput);
        String string = displayedPrice = this.priceFocused ? "$ " + formattedPrice : "$ " + NumberFormat.getIntegerInstance(Locale.US).format(this.parsePrice());
        if (this.priceFocused && this.hasPriceSelection()) {
            int start = Math.min(this.priceSelectionAnchor, this.priceCursor);
            int end = Math.max(this.priceSelectionAnchor, this.priceCursor);
            int formattedStart = AutoBuyScreen.formattedCursorIndex(formattedPrice, start);
            int formattedEnd = AutoBuyScreen.formattedCursorIndex(formattedPrice, end);
            float selectionX = (float)(fieldX + 7) + small.getStringWidth("$ " + formattedPrice.substring(0, formattedStart));
            float selectionW = small.getStringWidth(formattedPrice.substring(formattedStart, formattedEnd));
            this.rect(matrix, selectionX, fieldY + 5, selectionW, 9.0f, 1.0f, ColorUtil.multAlpha(accent, 0.42f), 0);
        }
        small.drawString(matrix, displayedPrice, fieldX + 7, (double)fieldY + 5.5, this.focused == null ? -8353639 : -1841168);
        if (this.priceFocused && !this.hasPriceSelection() && System.currentTimeMillis() % 1000L < 500L) {
            int formattedCursor = AutoBuyScreen.formattedCursorIndex(formattedPrice, this.priceCursor);
            float cursorX = (float)(fieldX + 7) + small.getStringWidth("$ " + formattedPrice.substring(0, formattedCursor));
            this.rect(matrix, cursorX, fieldY + 5, 1.0f, 9.0f, 0.0f, -1710098, 0);
        }
        int buttonsX = this.left + this.panelWidth - 100;
        this.button(matrix, buttonsX, footerY + 7, 87, 19, "\u041d\u0430\u0439\u0442\u0438 \u043d\u0430 /ah", "\ue045", false, guiMouseX, guiMouseY, actionFont, actionIcons, accent);
        Render2DUtil.onRender(context);
        context.method_73199();
        FontRenderer.flushQueuedText(context);
        context.method_51448().popMatrix();
    }

    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return this.handleMouseScrolled(mouseX, mouseY, verticalAmount, true);
    }

    public boolean mouseScrolledEmbedded(double mouseX, double mouseY, double verticalAmount) {
        return this.handleMouseScrolled(mouseX - (double)this.embeddedX, mouseY - (double)this.embeddedY, verticalAmount, false);
    }

    private boolean handleMouseScrolled(double mouseX, double mouseY, double verticalAmount, boolean forward) {
        double my;
        if (this.closing) {
            return true;
        }
        if (this.embedded) {
            if (!AutoBuyScreen.inside(mouseX, mouseY, 0.0, 0.0, this.listWidth, this.field_22790)) {
                return false;
            }
            this.scrollRow = Math.max(0, Math.min(Math.max(0, this.autoBuy.catalog().size() - this.gridRows), this.scrollRow - (int)Math.signum(verticalAmount)));
            return true;
        }
        double mx = this.logicalMouse(mouseX, (float)this.left + (float)this.panelWidth / 2.0f);
        if (AutoBuyScreen.inside(mx, my = this.logicalMouse(mouseY - (double)((1.0f - this.renderScale()) * 22.0f), (float)this.top + (float)this.panelHeight / 2.0f), this.left + 10, this.gridTop, this.panelWidth - 20, this.gridRows * 24)) {
            int totalRows = (this.autoBuy.catalog().size() + this.gridColumns - 1) / this.gridColumns;
            this.scrollRow = Math.max(0, Math.min(Math.max(0, totalRows - this.gridRows), this.scrollRow - (int)Math.signum(verticalAmount)));
            return true;
        }
        return false;
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        return this.handleMouseClicked(click.comp_4798(), click.comp_4799(), click.comp_4800().comp_4801(), doubled, click);
    }

    public boolean mouseClickedEmbedded(double mouseX, double mouseY, int button) {
        return this.handleMouseClicked(mouseX - (double)this.embeddedX, mouseY - (double)this.embeddedY, button, false, null);
    }

    private boolean handleMouseClicked(double mouseX, double mouseY, int mouseButton, boolean doubled, class_11909 source) {
        int buttonsX;
        boolean clickedPrice;
        if (this.closing) {
            return true;
        }
        if (this.embedded) {
            return this.handleEditorClick(mouseX, mouseY, mouseButton);
        }
        double mx = this.logicalMouse(mouseX, (float)this.left + (float)this.panelWidth / 2.0f);
        double my = this.logicalMouse(mouseY - (double)((1.0f - this.renderScale()) * 22.0f), (float)this.top + (float)this.panelHeight / 2.0f);
        if (mouseButton != 0 && mouseButton != 1) {
            return source != null && super.method_25402(source, doubled);
        }
        int cellSize = Math.min(24, (this.panelWidth - 22) / this.gridColumns);
        int gridX = this.left + (this.panelWidth - (cellSize * this.gridColumns - 2)) / 2;
        List<AutoBuy.CatalogItem> catalog = this.autoBuy.catalog();
        for (int row = 0; row < this.gridRows; ++row) {
            int index;
            for (int col = 0; col < this.gridColumns && (index = (this.scrollRow + row) * this.gridColumns + col) < catalog.size(); ++col) {
                int x = gridX + col * cellSize;
                int y = this.gridTop + row * 24;
                if (!AutoBuyScreen.inside(mx, my, x, y, cellSize - 2, cellSize - 2)) continue;
                AutoBuy.CatalogItem item = catalog.get(index);
                if (mouseButton == 1) {
                    if (this.autoBuy.selected(item.name())) {
                        this.autoBuy.toggle(item);
                    }
                    if (this.focused != null && this.focused.name().equals(item.name())) {
                        this.focused = null;
                    }
                } else {
                    this.focused = item;
                    this.priceInput = Long.toString(this.autoBuy.selectedPrices().getOrDefault(item.name(), 500000L));
                    this.priceFocused = false;
                    this.priceDragging = false;
                    this.priceCursor = this.priceInput.length();
                    this.priceSelectionAnchor = -1;
                }
                return true;
            }
        }
        int footerY = this.top + this.panelHeight - 67;
        this.priceFocused = clickedPrice = mouseButton == 0 && this.focused != null && AutoBuyScreen.inside(mx, my, this.left + 13, footerY + 28, (this.panelWidth - 136) * 5 / 6, 23.0);
        if (clickedPrice) {
            this.priceCursor = this.priceIndexAt(mx);
            if (doubled) {
                this.priceSelectionAnchor = 0;
                this.priceCursor = this.priceInput.length();
                this.priceDragging = false;
            } else {
                this.priceSelectionAnchor = this.priceCursor;
                this.priceDragging = true;
            }
        } else {
            this.priceDragging = false;
            this.priceSelectionAnchor = -1;
        }
        if (mouseButton == 0 && this.focused != null && AutoBuyScreen.inside(mx, my, buttonsX = this.left + this.panelWidth - 100, footerY + 7, 87.0, 19.0)) {
            if (this.embedded) {
                this.autoBuy.searchOnAuction(this.focused);
            } else {
                this.searchAfterClose = this.focused;
                this.method_25419();
            }
            return true;
        }
        if (clickedPrice) {
            return true;
        }
        return source != null && super.method_25402(source, doubled);
    }

    public boolean method_25403(class_11909 click, double deltaX, double deltaY) {
        return this.handleMouseDragged(click.comp_4798(), click.comp_4799(), click);
    }

    public boolean mouseDraggedEmbedded(double mouseX, double mouseY) {
        return this.handleMouseDragged(mouseX - (double)this.embeddedX, mouseY - (double)this.embeddedY, null);
    }

    private boolean handleMouseDragged(double mouseX, double mouseY, class_11909 source) {
        if (this.priceDragging && this.priceFocused) {
            double mx = this.logicalMouse(mouseX, (float)this.left + (float)this.panelWidth / 2.0f);
            this.priceCursor = this.priceIndexAt(mx);
            return true;
        }
        return false;
    }

    public boolean method_25406(class_11909 click) {
        return this.handleMouseReleased();
    }

    public boolean mouseReleasedEmbedded() {
        return this.handleMouseReleased();
    }

    private boolean handleMouseReleased() {
        if (this.priceDragging) {
            this.priceDragging = false;
            return true;
        }
        return false;
    }

    public boolean method_25404(class_11908 input) {
        return this.handleKeyPressed(input.comp_4795(), input.comp_4797(), input);
    }

    public boolean keyPressedEmbedded(int key, int modifiers) {
        return this.handleKeyPressed(key, modifiers, null);
    }

    private boolean handleKeyPressed(int key, int modifiers, class_11908 source) {
        if (this.closing) {
            return true;
        }
        if (this.focused != null && (key == 257 || key == 335)) {
            this.savePrice();
            return true;
        }
        if (this.priceFocused) {
            boolean shift;
            boolean control = (modifiers & 2) != 0;
            boolean bl = shift = (modifiers & 1) != 0;
            if (control) {
                switch (key) {
                    case 65: {
                        this.priceSelectionAnchor = 0;
                        this.priceCursor = this.priceInput.length();
                        return true;
                    }
                    case 67: {
                        if (this.hasPriceSelection()) {
                            AutoBuyScreen.mc.field_1774.method_1455(this.selectedPriceText());
                        }
                        return true;
                    }
                    case 88: {
                        if (this.hasPriceSelection()) {
                            AutoBuyScreen.mc.field_1774.method_1455(this.selectedPriceText());
                            this.replacePriceSelection("");
                        }
                        return true;
                    }
                    case 86: {
                        this.insertPriceDigits(AutoBuyScreen.mc.field_1774.method_1460());
                        return true;
                    }
                }
            }
            switch (key) {
                case 259: {
                    if (this.hasPriceSelection()) {
                        this.replacePriceSelection("");
                    } else if (this.priceCursor > 0) {
                        this.replacePriceRange(this.priceCursor - 1, this.priceCursor, "");
                    }
                    return true;
                }
                case 261: {
                    if (this.hasPriceSelection()) {
                        this.replacePriceSelection("");
                    } else if (this.priceCursor < this.priceInput.length()) {
                        this.replacePriceRange(this.priceCursor, this.priceCursor + 1, "");
                    }
                    return true;
                }
                case 263: {
                    this.movePriceCursor(this.hasPriceSelection() && !shift ? Math.min(this.priceCursor, this.priceSelectionAnchor) : this.priceCursor - 1, shift);
                    return true;
                }
                case 262: {
                    this.movePriceCursor(this.hasPriceSelection() && !shift ? Math.max(this.priceCursor, this.priceSelectionAnchor) : this.priceCursor + 1, shift);
                    return true;
                }
                case 268: {
                    this.movePriceCursor(0, shift);
                    return true;
                }
                case 269: {
                    this.movePriceCursor(this.priceInput.length(), shift);
                    return true;
                }
            }
        }
        if (key == 256) {
            if (this.embedded) {
                this.priceFocused = false;
                this.priceDragging = false;
                return true;
            }
            this.method_25419();
            return true;
        }
        return source != null && super.method_25404(source);
    }

    public boolean method_25400(class_11905 input) {
        if (this.priceFocused) {
            this.insertPriceDigits(input.method_74226());
            return true;
        }
        return super.method_25400(input);
    }

    public boolean charTypedEmbedded(char chr) {
        if (!this.priceFocused) {
            return false;
        }
        this.insertPriceDigits(String.valueOf(chr));
        return true;
    }

    private void insertPriceDigits(String value) {
        String digits = value.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return;
        }
        int start = this.hasPriceSelection() ? Math.min(this.priceSelectionAnchor, this.priceCursor) : this.priceCursor;
        int end = this.hasPriceSelection() ? Math.max(this.priceSelectionAnchor, this.priceCursor) : this.priceCursor;
        int remaining = 10 - (this.priceInput.length() - (end - start));
        if (remaining <= 0) {
            return;
        }
        this.replacePriceRange(start, end, digits.substring(0, Math.min(remaining, digits.length())));
    }

    private void replacePriceSelection(String replacement) {
        this.replacePriceRange(Math.min(this.priceSelectionAnchor, this.priceCursor), Math.max(this.priceSelectionAnchor, this.priceCursor), replacement);
    }

    private void replacePriceRange(int start, int end, String replacement) {
        this.priceInput = this.priceInput.substring(0, start) + replacement + this.priceInput.substring(end);
        this.priceCursor = start + replacement.length();
        this.priceSelectionAnchor = -1;
        if (this.focused != null && !this.priceInput.isEmpty()) {
            this.autoBuy.setPrice(this.focused, this.parsePrice());
        }
    }

    private void movePriceCursor(int position, boolean shift) {
        if (shift && this.priceSelectionAnchor < 0) {
            this.priceSelectionAnchor = this.priceCursor;
        }
        this.priceCursor = Math.max(0, Math.min(this.priceInput.length(), position));
        if (!shift) {
            this.priceSelectionAnchor = -1;
        }
    }

    private boolean hasPriceSelection() {
        return this.priceSelectionAnchor >= 0 && this.priceSelectionAnchor != this.priceCursor;
    }

    private String selectedPriceText() {
        return this.priceInput.substring(Math.min(this.priceSelectionAnchor, this.priceCursor), Math.max(this.priceSelectionAnchor, this.priceCursor));
    }

    private int priceIndexAt(double mouseX) {
        FontRenderer font = this.embedded ? MenuStyle.font(9.0f) : Fonts.getMenuSize(9, Fonts.Type.INTER_BOLD);
        int fieldX = this.embedded ? this.listWidth + 18 : this.left + 13;
        float relativeX = (float)mouseX - (float)fieldX - 7.0f - font.getStringWidth("$ ");
        String formatted = AutoBuyScreen.formatPriceDigits(this.priceInput);
        for (int i = 0; i < this.priceInput.length(); ++i) {
            float width = font.getStringWidth(formatted.substring(0, AutoBuyScreen.formattedCursorIndex(formatted, i + 1)));
            float previous = font.getStringWidth(formatted.substring(0, AutoBuyScreen.formattedCursorIndex(formatted, i)));
            if (!(relativeX < (previous + width) / 2.0f)) continue;
            return i;
        }
        return this.priceInput.length();
    }

    private static String formatPriceDigits(String digits) {
        StringBuilder formatted = new StringBuilder(digits.length() + digits.length() / 3);
        for (int i = 0; i < digits.length(); ++i) {
            if (i > 0 && (digits.length() - i) % 3 == 0) {
                formatted.append(',');
            }
            formatted.append(digits.charAt(i));
        }
        return formatted.toString();
    }

    private static int formattedCursorIndex(String formatted, int digitPosition) {
        int digitsSeen = 0;
        for (int i = 0; i < formatted.length(); ++i) {
            if (digitsSeen == digitPosition) {
                return i;
            }
            if (formatted.charAt(i) == ',') continue;
            ++digitsSeen;
        }
        return formatted.length();
    }

    private long parsePrice() {
        if (this.priceInput.isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(this.priceInput);
        }
        catch (NumberFormatException exception) {
            return 1000000000L;
        }
    }

    private void savePrice() {
        if (this.focused == null) {
            return;
        }
        this.autoBuy.setPrice(this.focused, this.parsePrice());
        this.priceInput = Long.toString(this.autoBuy.selectedPrices().get(this.focused.name()));
        this.priceFocused = false;
        this.priceCursor = this.priceInput.length();
        this.priceSelectionAnchor = -1;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void panel(class_4587 matrix) {
        boolean previousHud = Render2DUtil.isHudDecorationActive();
        boolean previousGui = Render2DUtil.isGuiDecorationActive();
        Render2DUtil.setHudDecorationActive(true);
        Render2DUtil.setGuiDecorationActive(true);
        try {
            blur.setLiveMode(true);
            blur.setStyledBlurRadius(28.0f);
            blur.render(ShapeProperties.create(matrix, this.left, this.top, this.panelWidth, this.panelHeight).round(8.0f).softness(5.0f).thickness(0.0f).quality(50.0f).outlineColor(0).color(ColorUtil.DARK_BLACK).build());
        }
        finally {
            blur.resetStyledBlurRadius();
            blur.setLiveMode(false);
            Render2DUtil.setHudDecorationActive(previousHud);
            Render2DUtil.setGuiDecorationActive(previousGui);
        }
        this.rect(matrix, this.left, this.top, this.panelWidth, this.panelHeight, 8.0f, -669574363, -14341322);
    }

    private void rect(class_4587 matrix, float x, float y, float w, float h, float radius, int color, int outline) {
        rectangle.render(ShapeProperties.create(matrix, x, y, w, h).round(radius).softness(0.6f).thickness(outline == 0 ? 0.0f : 1.5f).quality(24.0f).outlineColor(outline).color(color).build());
    }

    private void drawItemOutline(class_4587 matrix, float x, float y, float size, int color) {
        this.drawRoundedOutline(matrix, x, y, size, size, 6.0f, color);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void drawRoundedOutline(class_4587 matrix, float x, float y, float width, float height, float radius, int color) {
        boolean queued = Render2DUtil.isQueuedShapeActive();
        Render2DUtil.setQueuedShapeActive(false);
        try {
            rectangle.render(ShapeProperties.create(matrix, x, y, width, height).round(radius).softness(-0.32f).thickness(0.0f).quality(18.0f).outlineColor(0).color(color).build());
        }
        finally {
            Render2DUtil.setQueuedShapeActive(queued);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void drawItemFill(class_4587 matrix, float x, float y, float size, int color) {
        boolean queued = Render2DUtil.isQueuedShapeActive();
        Render2DUtil.setQueuedShapeActive(false);
        try {
            rectangle.render(ShapeProperties.create(matrix, x, y, size, size).round(6.0f).softness(0.3f).quality(18.0f).color(color).build());
        }
        finally {
            Render2DUtil.setQueuedShapeActive(queued);
        }
    }

    private void button(class_4587 matrix, int x, int y, int w, int h, String label, String icon, boolean active, double mouseX, double mouseY, FontRenderer font, FontRenderer icons, int accent) {
        boolean hovered = AutoBuyScreen.inside(mouseX, mouseY, x, y, w, h);
        float hover = AutoBuyScreen.animate(this.buttonHover, label, hovered ? 1.0f : 0.0f);
        int base = active ? ColorUtil.overCol(-15460321, accent, 0.16f) : -15460321;
        int outline = active ? ColorUtil.multAlpha(accent, 0.66f) : -13945788;
        this.rect(matrix, x, y, w, h, 5.0f, ColorUtil.multAlpha(ColorUtil.overCol(base, -15195856, hover), 0.52f), ColorUtil.multAlpha(ColorUtil.overCol(outline, -12958118, hover), 0.55f));
        this.drawRoundedOutline(matrix, x, y, w, h, 5.0f, ColorUtil.multAlpha(ColorUtil.overCol(outline, -12958118, hover), 0.55f));
        float textWidth = font.getStringWidth(label);
        float iconWidth = icons.getStringWidth(icon);
        float groupX = (float)x + ((float)w - textWidth - iconWidth - 8.0f) / 2.0f;
        float verticalOffset = ((float)h - 23.0f) / 2.0f;
        icons.drawString(matrix, icon, groupX, (float)(y + 6) + verticalOffset, accent);
        font.drawString(matrix, label, groupX + iconWidth + 4.0f, (double)y + 7.5 + (double)verticalOffset, -1775374);
    }

    private static float animate(Map<String, Float> states, String key, float target) {
        float current = states.getOrDefault(key, Float.valueOf(0.0f)).floatValue();
        float next = MathUtil.interpolateSmooth(4.0, current, target);
        if (Math.abs(next - target) < 0.001f) {
            next = target;
        }
        if (next == 0.0f && target == 0.0f) {
            states.remove(key);
        } else {
            states.put(key, Float.valueOf(next));
        }
        return next;
    }

    private float renderScale() {
        long elapsed = System.currentTimeMillis() - (this.closing ? this.closeAnimStart : this.openAnimStart);
        float duration = this.closing ? 165.0f : 190.0f;
        float t = Math.max(0.0f, Math.min(1.0f, (float)elapsed / duration));
        if (this.closing) {
            return 1.0f - AutoBuyScreen.easeInBack(t);
        }
        return AutoBuyScreen.easeOutBack(t);
    }

    private static float easeOutBack(float t) {
        float shifted = t - 1.0f;
        float coefficient = 1.65f;
        return 1.0f + coefficient * shifted * shifted * shifted + 0.65f * shifted * shifted;
    }

    private static float easeInBack(float t) {
        return 1.65f * t * t * t - 0.65f * t * t;
    }

    private double logicalMouse(double coordinate, float center) {
        float scale = Math.max(this.renderScale(), 0.001f);
        return (double)center + (coordinate - (double)center) / (double)scale;
    }

    private static String ellipsis(String value, FontRenderer font, float maxWidth) {
        if (font.getStringWidth(value) <= maxWidth) {
            return value;
        }
        while (!value.isEmpty() && font.getStringWidth(value + "...") > maxWidth) {
            value = value.substring(0, value.length() - 1);
        }
        return value + "...";
    }

    private static boolean inside(double x, double y, double bx, double by, double bw, double bh) {
        return x >= bx && x < bx + bw && y >= by && y < by + bh;
    }

    public boolean method_25421() {
        return false;
    }

    private static final class HoverTransition {
        private float from;
        private float target;
        private long started;

        private HoverTransition() {
        }

        private float update(float nextTarget) {
            long now = System.nanoTime();
            float progress = Math.clamp((float)(now - this.started) / 1.5E8f, 0.0f, 1.0f);
            float eased = progress * progress * (3.0f - 2.0f * progress);
            float value = this.from + (this.target - this.from) * eased;
            if (nextTarget != this.target) {
                this.from = value;
                this.target = nextTarget;
                this.started = now;
            }
            return value;
        }
    }
}

