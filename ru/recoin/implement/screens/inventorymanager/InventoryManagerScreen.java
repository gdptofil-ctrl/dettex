/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_1799
 *  net.minecraft.class_2561
 *  net.minecraft.class_332
 *  net.minecraft.class_3532
 *  net.minecraft.class_437
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.screens.inventorymanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_1799;
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
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.ItemGuiAlpha;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.implement.features.modules.misc.AutoInventory;
import ru.recoin.implement.features.modules.misc.InventoryManager;
import ru.recoin.implement.features.modules.render.Hud;
import ru.recoin.implement.screens.menu.MenuDescriptionLocalizer;
import ru.recoin.implement.screens.menu.MenuStyle;

public final class InventoryManagerScreen
extends class_437
implements QuickImports {
    private static final String LOGO_ICON = "\ue0c6";
    private static final int PANEL_WIDTH = 650;
    private static final int PANEL_HEIGHT = 370;
    private static final float DISPLAY_SCALE = 0.75f;
    private static final int HEADER_HEIGHT = 42;
    private static final int SLOT = 24;
    private static final int GAP = 3;
    private static final int CATALOG_COLUMNS = 8;
    private static final int CATALOG_ROWS = 4;
    private static final int PAGE_SIZE = 32;
    private static final int TEXT = -986118;
    private static final int MUTED = -7301470;
    private static final long OPEN_TIME = 190L;
    private static final long CLOSE_TIME = 165L;
    private final InventoryManager owner;
    private final Map<Long, Float> buttonHover = new HashMap<Long, Float>();
    private final Map<Long, Float> selectionAnimation = new HashMap<Long, Float>();
    private float displayScale = 0.75f;
    private final long openedAt = System.currentTimeMillis();
    private long closedAt;
    private boolean closing;
    private float closeStartAlpha;
    private List<InventoryManager.LayoutSlot> layout;
    private AutoInventory.Entry hoveredEntry;
    private int selectedCatalog = -1;
    private int selectedInventory = -1;
    private int page;
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;

    public InventoryManagerScreen(InventoryManager owner) {
        super((class_2561)class_2561.method_43470((String)"Inventory Manager"));
        this.owner = owner;
        this.layout = new ArrayList<InventoryManager.LayoutSlot>(owner.readLayout());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        float previousAlpha = Render2DUtil.getRenderAlpha();
        Render2DUtil.setRenderAlpha(previousAlpha * this.guiAlpha());
        Render2DUtil.setActiveContext(context);
        Render2DUtil.setQueuedShapeActive(true);
        try {
            this.displayScale = Math.max(0.05f, Math.min(0.75f, Math.min(((float)this.field_22789 - 20.0f) / 650.0f, ((float)this.field_22790 - 20.0f) / 370.0f)));
            this.panelWidth = 650;
            this.panelHeight = 370;
            this.left = Math.round((this.virtualWidth() - (float)this.panelWidth) / 2.0f);
            this.top = Math.round((this.virtualHeight() - (float)this.panelHeight) / 2.0f);
            float scale = this.displayScale;
            int virtualMouseX = this.toVirtualX(mouseX);
            int virtualMouseY = this.toVirtualY(mouseY);
            context.method_51448().pushMatrix();
            context.method_51448().translate((float)this.field_22789 / 2.0f, (float)this.field_22790 / 2.0f);
            context.method_51448().scale(scale, scale);
            context.method_51448().translate(-this.virtualWidth() / 2.0f, -this.virtualHeight() / 2.0f);
            try {
                ItemGuiAlpha.render(this.guiAlpha(), () -> this.renderManager(context, MatrixStackCompat.from(context.method_51448()), virtualMouseX, virtualMouseY));
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
            Render2DUtil.setRenderAlpha(previousAlpha);
        }
    }

    private void renderManager(class_332 context, class_4587 matrix, int mouseX, int mouseY) {
        int accent = MenuStyle.accent();
        this.hoveredEntry = null;
        this.owner.catalogOwner().refreshCatalogIcons();
        if (this.selectedCatalog >= this.owner.catalogOwner().catalog().size()) {
            this.selectedCatalog = -1;
        }
        blur.beginFrame();
        this.drawPanel(matrix, this.left, this.top, this.panelWidth, this.panelHeight);
        MenuStyle.separator(matrix, this.left, this.top + 42, this.panelWidth, false);
        this.drawHeader(matrix, mouseX, mouseY, accent);
        this.drawInventory(context, matrix, mouseX, mouseY, accent);
        this.drawCatalog(context, matrix, mouseX, mouseY, accent);
        this.drawFooter(matrix, mouseX, mouseY, accent);
        context.method_73199();
        FontRenderer.flushQueuedText(context);
        if (this.hoveredEntry != null && !this.closing) {
            this.drawTooltip(context, matrix, mouseX, mouseY);
        }
    }

    private void drawHeader(class_4587 matrix, int mouseX, int mouseY, int accent) {
        MenuStyle.separator(matrix, (float)this.left + 51.0f, (float)this.top + 11.0f, 18.0f, true);
        FontRenderer logo = Fonts.getMenuSize(23, Fonts.Type.COMBINED_ICONS);
        FontRenderer title = MenuStyle.medium(13.0f);
        FontRenderer small = MenuStyle.font(10.0f);
        this.drawCenteredText(matrix, logo, LOGO_ICON, (float)this.left + 15.0f, (float)this.top + 8.0f, 24.0f, 24.0f, accent);
        this.drawLeftText(matrix, title, "Inventory Manager", (float)this.left + 62.0f, (float)this.top + 9.0f, 24.0f, -986118);
        for (int index = 0; index < 3; ++index) {
            float x = (float)this.left + 356.0f + (float)index * 91.0f;
            boolean active = this.owner.activePreset() == index;
            this.drawButton(matrix, x, (float)this.top + 9.0f, 82.0f, 24.0f, active, InventoryManagerScreen.inside(mouseX, mouseY, x, (float)this.top + 9.0f, 82.0, 24.0), accent);
            this.drawCenteredText(matrix, small, this.tr("\u0418\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044c ", "Inventory ") + (index + 1), x, (float)this.top + 9.0f, 82.0f, 24.0f, active ? -986118 : -7301470);
        }
    }

    private void drawInventory(class_332 context, class_4587 matrix, int mouseX, int mouseY, int accent) {
        float gridX = (float)this.left + 20.0f;
        float gridY = (float)this.top + 78.0f;
        this.drawSectionFrame(matrix, (float)this.left + 14.0f, (float)this.top + 49.0f, 278.0f, 163.0f, accent);
        this.drawLabelFrame(matrix, gridX - 5.0f, (float)this.top + 50.0f, 240.0f, 22.0f, accent);
        FontRenderer section = MenuStyle.medium(11.0f);
        FontRenderer small = MenuStyle.font(10.0f);
        this.drawLeftText(matrix, section, this.tr("\u0420\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0430 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044f", "Inventory layout"), gridX, (float)this.top + 50.0f, 22.0f, -986118);
        for (int visual = 0; visual < 36; ++visual) {
            int row = visual / 9;
            int column = visual % 9;
            float x = gridX + (float)(column * 27);
            float y = gridY + (float)(row * 27) + (row == 3 ? 9.0f : 0.0f);
            int inventoryIndex = this.visualToInventory(visual);
            boolean hovered = InventoryManagerScreen.inside(mouseX, mouseY, x, y, 24.0, 24.0);
            boolean selected = inventoryIndex == this.selectedInventory;
            this.drawButton(matrix, x, y, 24.0f, 24.0f, selected, hovered, accent);
            InventoryManager.LayoutSlot desired = this.layout.get(inventoryIndex);
            AutoInventory.Entry entry = this.owner.entry(desired);
            if (entry == null) continue;
            context.method_51428(this.owner.catalogOwner().iconStack(entry), Math.round(x + 4.0f), Math.round(y + 4.0f), 210);
            if (desired.count() <= 1) continue;
            String count = String.valueOf(desired.count());
            FontRenderer countFont = MenuStyle.font(9.0f);
            FontRenderer.TextBounds bounds = countFont.getStringBounds(count);
            countFont.drawString(matrix, count, x + 24.0f - 2.0f - bounds.width() - bounds.x(), y + 24.0f - 2.0f - bounds.height() - bounds.y(), -986118);
        }
        MenuStyle.separator(matrix, gridX, gridY + 85.0f, 240.0f, false);
        this.drawLeftText(matrix, small, this.tr("\u0425\u043e\u0442\u0431\u0430\u0440", "Hotbar"), gridX, gridY + 117.0f, 15.0f, -7301470);
    }

    private void drawCatalog(class_332 context, class_4587 matrix, int mouseX, int mouseY, int accent) {
        float x0 = (float)this.left + 342.0f;
        float y0 = (float)this.top + 78.0f;
        this.drawSectionFrame(matrix, (float)this.left + 328.0f, (float)this.top + 49.0f, 252.0f, 157.0f, accent);
        this.drawLabelFrame(matrix, x0 - 5.0f, (float)this.top + 50.0f, 118.0f, 22.0f, accent);
        this.drawLabelFrame(matrix, x0 + 198.0f, (float)this.top + 50.0f, 32.0f, 22.0f, accent);
        List<AutoInventory.Entry> entries = this.owner.catalogOwner().catalog();
        int pages = Math.max(1, (entries.size() + 32 - 1) / 32);
        this.page = Math.min(this.page, pages - 1);
        FontRenderer section = MenuStyle.medium(11.0f);
        FontRenderer small = MenuStyle.font(10.0f);
        this.drawLeftText(matrix, section, this.tr("\u041f\u0440\u0435\u0434\u043c\u0435\u0442\u044b \u0441\u0435\u0442\u0430", "Set items"), x0, (float)this.top + 50.0f, 22.0f, -986118);
        this.drawCenteredText(matrix, small, this.page + 1 + "/" + pages, x0 + 198.0f, (float)this.top + 50.0f, 32.0f, 22.0f, -7301470);
        int start = this.page * 32;
        int end = Math.min(entries.size(), start + 32);
        for (int index = start; index < end; ++index) {
            int local = index - start;
            float x = x0 + (float)(local % 8 * 27);
            float y = y0 + (float)(local / 8 * 27);
            boolean hovered = InventoryManagerScreen.inside(mouseX, mouseY, x, y, 24.0, 24.0);
            boolean selected = this.selectedCatalog == index;
            this.drawButton(matrix, x, y, 24.0f, 24.0f, selected, hovered, accent);
            context.method_51428(this.owner.catalogOwner().iconStack(entries.get(index)), Math.round(x + 4.0f), Math.round(y + 4.0f), 210);
            if (!hovered) continue;
            this.hoveredEntry = entries.get(index);
        }
        this.drawButton(matrix, x0, y0 + 143.0f, 42.0f, 23.0f, false, InventoryManagerScreen.inside(mouseX, mouseY, x0, y0 + 143.0f, 42.0, 23.0), accent);
        this.drawButton(matrix, x0 + 180.0f, y0 + 143.0f, 42.0f, 23.0f, false, InventoryManagerScreen.inside(mouseX, mouseY, x0 + 180.0f, y0 + 143.0f, 42.0, 23.0), accent);
        this.drawChevron(matrix, x0 + 21.0f, y0 + 154.5f, false, -986118);
        this.drawChevron(matrix, x0 + 201.0f, y0 + 154.5f, true, -986118);
        AutoInventory.Entry selected = this.selectedCatalog < 0 || this.selectedCatalog >= entries.size() ? null : entries.get(this.selectedCatalog);
        String selectedName = selected == null ? this.tr("\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u043f\u0440\u0435\u0434\u043c\u0435\u0442", "Select an item") : selected.name();
        FontRenderer navigation = MenuStyle.font(11.0f);
        this.drawLabelFrame(matrix, x0 + 48.0f, y0 + 142.0f, 126.0f, 25.0f, accent);
        this.drawCenteredText(matrix, navigation, this.ellipsis(selectedName, navigation, 116.0f), x0 + 48.0f, y0 + 142.0f, 126.0f, 25.0f, selected == null ? -7301470 : -986118);
        this.drawLabelFrame(matrix, x0 - 4.0f, y0 + 175.0f, 230.0f, 25.0f, accent);
        this.drawCenteredText(matrix, small, this.tr("\u041b\u041a\u041c \u2014 \u043f\u043e\u043b\u043e\u0436\u0438\u0442\u044c  \u2022  \u041f\u041a\u041c \u2014 \u043e\u0447\u0438\u0441\u0442\u0438\u0442\u044c", "LMB \u2014 assign  \u2022  RMB \u2014 clear"), x0 - 4.0f, y0 + 175.0f, 230.0f, 25.0f, -7301470);
    }

    private void drawFooter(class_4587 matrix, int mouseX, int mouseY, int accent) {
        float y = (float)(this.top + this.panelHeight) - 39.0f;
        float captureX = (float)this.left + 342.0f;
        float applyX = (float)this.left + 474.0f;
        this.drawButton(matrix, captureX, y, 122.0f, 26.0f, false, InventoryManagerScreen.inside(mouseX, mouseY, captureX, y, 122.0, 26.0), accent);
        this.drawButton(matrix, applyX, y, 158.0f, 26.0f, false, InventoryManagerScreen.inside(mouseX, mouseY, applyX, y, 158.0, 26.0), accent);
        FontRenderer font = MenuStyle.font(11.0f);
        this.drawCenteredText(matrix, font, this.tr("\u0421\u043d\u044f\u0442\u044c \u0442\u0435\u043a\u0443\u0449\u0443\u044e", "Capture layout"), captureX, y, 122.0f, 26.0f, -986118);
        this.drawCenteredText(matrix, font, this.tr("\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u0438 \u0440\u0430\u0437\u043b\u043e\u0436\u0438\u0442\u044c", "Save and apply"), applyX, y, 158.0f, 26.0f, -986118);
    }

    private void drawButton(class_4587 matrix, float x, float y, float w, float h, boolean active, boolean hovered, int accent) {
        long key = (long)Float.floatToIntBits(x - (float)this.left) << 32 | (long)Float.floatToIntBits(y - (float)this.top) & 0xFFFFFFFFL;
        float hover = MathUtil.interpolateSmooth(2.2, this.buttonHover.getOrDefault(key, Float.valueOf(0.0f)).floatValue(), hovered && !this.closing ? 1.0f : 0.0f);
        this.buttonHover.put(key, Float.valueOf(hover));
        float target = active ? 1.0f : 0.0f;
        float selected = MathUtil.interpolateSmooth(1.75, this.selectionAnimation.getOrDefault(key, Float.valueOf(target)).floatValue(), target);
        this.selectionAnimation.put(key, Float.valueOf(selected));
        this.drawImmediateRect(matrix, x, y, w, h, 6.0f, 0.2f, MenuStyle.moduleBackground());
        float highlight = selected * 0.26f + hover * 0.1f * (1.0f - selected);
        if (highlight > 0.001f) {
            this.drawImmediateRect(matrix, x, y, w, h, 6.0f, 0.2f, ColorUtil.multAlpha(accent, highlight));
        }
    }

    private void drawSectionFrame(class_4587 matrix, float x, float y, float w, float h, int accent) {
        this.drawImmediateRect(matrix, x, y, w, h, 9.0f, 0.2f, MenuStyle.moduleBackground());
    }

    private void drawLabelFrame(class_4587 matrix, float x, float y, float w, float h, int accent) {
        this.drawImmediateRect(matrix, x, y, w, h, 7.0f, 0.2f, MenuStyle.cardHover());
    }

    private void drawChevron(class_4587 matrix, float x, float y, boolean right, int color) {
        FontRenderer font = MenuStyle.medium(11.0f);
        this.drawCenteredText(matrix, font, right ? ">" : "<", x - 9.0f, y - 9.0f, 18.0f, 18.0f, color);
    }

    private void drawCenteredText(class_4587 matrix, FontRenderer font, String text, float x, float y, float w, float h, int color) {
        FontRenderer.TextBounds bounds = font.getStringBounds(text);
        font.drawString(matrix, text, x + (w - bounds.width()) / 2.0f - bounds.x(), y + (h - bounds.height()) / 2.0f - bounds.y(), color);
    }

    private String tr(String ru, String en) {
        return MenuDescriptionLocalizer.isRussian() ? ru : en;
    }

    private void drawLeftText(class_4587 matrix, FontRenderer font, String text, float x, float y, float h, int color) {
        FontRenderer.TextBounds bounds = font.getStringBounds(text);
        font.drawString(matrix, text, x - bounds.x(), y + (h - bounds.height()) / 2.0f - bounds.y(), color);
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        if (this.closing) {
            return true;
        }
        double mx = this.toVirtualX(click.comp_4798());
        double my = this.toVirtualY(click.comp_4799());
        int button = click.comp_4800().comp_4801();
        for (int index = 0; index < 3; ++index) {
            float x = (float)this.left + 356.0f + (float)index * 91.0f;
            if (button != 0 || !InventoryManagerScreen.inside(mx, my, x, (float)this.top + 9.0f, 82.0, 24.0)) continue;
            this.owner.saveLayout(this.layout);
            this.owner.selectPreset(index);
            this.layout = new ArrayList<InventoryManager.LayoutSlot>(this.owner.readLayout());
            this.selectedInventory = -1;
            return true;
        }
        float gridX = (float)this.left + 20.0f;
        float gridY = (float)this.top + 78.0f;
        for (int visual = 0; visual < 36; ++visual) {
            int inventoryIndex;
            float x = gridX + (float)(visual % 9 * 27);
            int row = visual / 9;
            float y = gridY + (float)(row * 27) + (row == 3 ? 9.0f : 0.0f);
            if (!InventoryManagerScreen.inside(mx, my, x, y, 24.0, 24.0)) continue;
            this.selectedInventory = inventoryIndex = this.visualToInventory(visual);
            if (button == 1) {
                this.layout.set(inventoryIndex, null);
            } else if (button == 0 && this.selectedCatalog >= 0 && this.selectedCatalog < this.owner.catalogOwner().catalog().size()) {
                AutoInventory.Entry entry = this.owner.catalogOwner().catalog().get(this.selectedCatalog);
                this.layout.set(inventoryIndex, new InventoryManager.LayoutSlot(this.selectedCatalog, Math.max(1, this.owner.catalogOwner().target(entry.name()))));
            }
            this.owner.saveLayout(this.layout);
            return true;
        }
        float x0 = (float)this.left + 342.0f;
        float y0 = (float)this.top + 78.0f;
        List<AutoInventory.Entry> entries = this.owner.catalogOwner().catalog();
        int start = this.page * 32;
        int end = Math.min(entries.size(), start + 32);
        for (int index = start; index < end; ++index) {
            int local = index - start;
            float x = x0 + (float)(local % 8 * 27);
            float y = y0 + (float)(local / 8 * 27);
            if (button != 0 || !InventoryManagerScreen.inside(mx, my, x, y, 24.0, 24.0)) continue;
            this.selectedCatalog = index;
            return true;
        }
        int pages = Math.max(1, (entries.size() + 32 - 1) / 32);
        if (button == 0 && InventoryManagerScreen.inside(mx, my, x0, y0 + 143.0f, 42.0, 23.0)) {
            this.page = Math.max(0, this.page - 1);
            return true;
        }
        if (button == 0 && InventoryManagerScreen.inside(mx, my, x0 + 180.0f, y0 + 143.0f, 42.0, 23.0)) {
            this.page = Math.min(pages - 1, this.page + 1);
            return true;
        }
        float footerY = (float)(this.top + this.panelHeight) - 39.0f;
        if (button == 0 && InventoryManagerScreen.inside(mx, my, (float)this.left + 342.0f, footerY, 122.0, 26.0)) {
            this.captureCurrentInventory();
            this.owner.saveLayout(this.layout);
            return true;
        }
        if (button == 0 && InventoryManagerScreen.inside(mx, my, (float)this.left + 474.0f, footerY, 158.0, 26.0)) {
            this.owner.saveLayout(this.layout);
            this.owner.applyActiveLayout();
            return true;
        }
        return super.method_25402(click, doubled);
    }

    private void captureCurrentInventory() {
        if (this.field_22787 == null || this.field_22787.field_1724 == null) {
            return;
        }
        for (int inventoryIndex = 0; inventoryIndex < 36; ++inventoryIndex) {
            class_1799 stack = this.field_22787.field_1724.method_31548().method_5438(inventoryIndex);
            InventoryManager.LayoutSlot found = null;
            for (int catalogIndex = 0; catalogIndex < this.owner.catalogOwner().catalog().size(); ++catalogIndex) {
                AutoInventory.Entry entry = this.owner.catalogOwner().catalog().get(catalogIndex);
                if (!this.owner.catalogOwner().matches(entry, stack)) continue;
                found = new InventoryManager.LayoutSlot(catalogIndex, stack.method_7947());
                break;
            }
            this.layout.set(inventoryIndex, found);
        }
    }

    private int visualToInventory(int visual) {
        int row = visual / 9;
        return row == 3 ? visual - 27 : visual + 9;
    }

    private void drawTooltip(class_332 context, class_4587 matrix, int mouseX, int mouseY) {
        FontRenderer font = MenuStyle.font(10.0f);
        float tooltipW = Math.min(250.0f, font.getStringWidth(this.hoveredEntry.name()) + 14.0f);
        float x = Math.min((float)mouseX + 10.0f, this.virtualWidth() - tooltipW - 5.0f);
        float y = Math.min((float)mouseY + 9.0f, this.virtualHeight() - 23.0f);
        this.drawImmediateRect(matrix, x, y, tooltipW, 20.0f, 5.0f, 0.3f, Hud.getInstance().themePanelColor());
        this.drawLeftText(matrix, font, this.ellipsis(this.hoveredEntry.name(), font, tooltipW - 12.0f), x + 7.0f, y, 20.0f, -986118);
        FontRenderer.flushQueuedText(context);
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
            blur.setLiveMode(false);
            blur.setColorlessMode(true);
            blur.setStyledBlurRadius(48.0f);
            MenuStyle.shadow(matrix, x, y, w, h, 12.0f);
            blur.render(ShapeProperties.create(matrix, x, y, w, h).round(12.0f).softness(5.0f).thickness(0.0f).quality(50.0f).outlineColor(0).color(MenuStyle.surface()).build());
        }
        finally {
            blur.setLiveMode(false);
            blur.setColorlessMode(false);
            blur.resetStyledBlurRadius();
            Render2DUtil.setHudDecorationActive(oldHud);
            Render2DUtil.setGuiDecorationActive(oldGui);
        }
        this.drawImmediateRect(matrix, x, y, w, h, 12.0f, 0.2f, MenuStyle.surface());
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

    private float guiAlpha() {
        float t = class_3532.method_15363((float)((float)(System.currentTimeMillis() - (this.closing ? this.closedAt : this.openedAt)) / (float)(this.closing ? 165L : 190L)), (float)0.0f, (float)1.0f);
        float eased = t * t * (3.0f - 2.0f * t);
        return this.closing ? this.closeStartAlpha * (1.0f - eased) : eased;
    }

    public void method_25393() {
        if (this.closing && System.currentTimeMillis() - this.closedAt >= 165L) {
            super.method_25419();
        }
        super.method_25393();
    }

    public boolean method_25404(class_11908 input) {
        if (input.comp_4795() == 256 || input.comp_4795() == 344) {
            this.method_25419();
            return true;
        }
        return this.closing || super.method_25404(input);
    }

    private float virtualWidth() {
        return (float)this.field_22789 / this.displayScale;
    }

    private float virtualHeight() {
        return (float)this.field_22790 / this.displayScale;
    }

    private int toVirtualX(double x) {
        return (int)Math.round(x / (double)this.displayScale);
    }

    private int toVirtualY(double y) {
        return (int)Math.round(y / (double)this.displayScale);
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

    public void method_25419() {
        if (this.closing) {
            return;
        }
        this.owner.saveLayout(this.layout);
        this.closeStartAlpha = this.guiAlpha();
        this.closedAt = System.currentTimeMillis();
        this.closing = true;
    }

    public boolean method_25421() {
        return false;
    }
}

