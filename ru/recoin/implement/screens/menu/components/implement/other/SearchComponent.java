/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1041
 *  net.minecraft.class_332
 *  net.minecraft.class_3675
 *  net.minecraft.class_4587
 *  org.lwjgl.glfw.GLFW
 */
package ru.recoin.implement.screens.menu.components.implement.other;

import net.minecraft.class_1041;
import net.minecraft.class_332;
import net.minecraft.class_3675;
import net.minecraft.class_4587;
import org.lwjgl.glfw.GLFW;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.common.util.render.ScissorManager;
import ru.recoin.core.Main;
import ru.recoin.implement.screens.menu.MenuDescriptionLocalizer;
import ru.recoin.implement.screens.menu.MenuStyle;
import ru.recoin.implement.screens.menu.components.AbstractComponent;

public class SearchComponent
extends AbstractComponent {
    private float languageX;
    private float languageY;
    private float languageWidth;
    private float languageHeight;
    public static boolean typing = false;
    private boolean dragging;
    private int cursorPosition = 0;
    private int selectionStart = -1;
    private int selectionEnd = -1;
    private long lastClickTime = 0L;
    private float xOffset = 0.0f;
    private float expansion;
    private long expansionTime;
    private float clearProgress;
    private String text = "";

    public void reset() {
        this.text = "";
        typing = false;
        this.dragging = false;
        this.cursorPosition = 0;
        this.selectionStart = -1;
        this.selectionEnd = -1;
        this.xOffset = 0.0f;
    }

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        boolean focused;
        float textX;
        int end;
        int start;
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        FontRenderer font = MenuStyle.font(9.0f);
        FontRenderer iconFont = Fonts.getSize(22, Fonts.Type.COMBINED_ICONS);
        long now = System.nanoTime();
        float elapsed = this.expansionTime == 0L ? 0.016666668f : Math.min(0.05f, (float)(now - this.expansionTime) / 1.0E9f);
        this.expansionTime = now;
        float blend = 1.0f - (float)Math.exp(-elapsed / 0.045f);
        this.expansion += ((typing ? 1.0f : 0.0f) - this.expansion) * blend;
        this.clearProgress += ((typing || !this.text.isEmpty() ? 1.0f : 0.0f) - this.clearProgress) * blend;
        float clearAlpha = SearchComponent.smoothProgress((this.clearProgress - 0.35f) / 0.65f);
        float shortcutAlpha = 1.0f - SearchComponent.smoothProgress(this.clearProgress / 0.35f);
        this.x -= 252.0f * this.expansion;
        this.width = 228.0f + 251.0f * this.expansion;
        this.height = 23.0f;
        this.updateXOffset(font, this.cursorPosition);
        this.languageWidth = 0.0f;
        this.languageHeight = 0.0f;
        this.languageX = 0.0f;
        this.languageY = 0.0f;
        MenuStyle.fill(matrix, this.x, this.y, this.width, this.height, 7.0f, MenuStyle.moduleBackground());
        MenuStyle.outline(matrix, this.x, this.y, this.width, this.height, 7.0f, 0.35f, typing ? 1883724407 : 640695635);
        String emptyHint = MenuDescriptionLocalizer.isRussian() ? "\u041f\u043e\u0438\u0441\u043a..." : "Search...";
        String displayText = this.text.equalsIgnoreCase("") && !typing ? emptyHint : this.text;
        ScissorManager scissor = Main.getInstance().getScissorManager();
        scissor.push(matrix.method_23760().method_23761(), this.x + 7.0f, this.y, this.textWidth(), this.height);
        if (typing && this.selectionStart != -1 && this.selectionEnd != -1 && this.selectionStart != this.selectionEnd && (start = Math.max(0, Math.min(this.getStartOfSelection(), this.text.length()))) < (end = Math.max(0, Math.min(this.getEndOfSelection(), this.text.length())))) {
            float selectionXStart = this.x + 8.0f - this.xOffset + font.getStringWidth(this.text.substring(0, start));
            float selectionXEnd = this.x + 8.0f - this.xOffset + font.getStringWidth(this.text.substring(0, end));
            float selectionWidth = selectionXEnd - selectionXStart;
            rectangle.render(ShapeProperties.create(matrix, selectionXStart, this.y + 5.5f, selectionWidth, 12.0).color(ColorUtil.multAlpha(-8145409, 0.25f)).build());
        }
        float searchIconWidth = iconFont.getStringWidth("\ue045");
        float f = textX = this.text.isEmpty() && !typing ? this.x + 9.0f + searchIconWidth + 5.0f : this.x + 8.5f;
        if (this.text.isEmpty() && !typing) {
            iconFont.drawString(matrix, "\ue045", this.x + 7.0f, this.y + 6.0f, -8025449);
        }
        float drawY = this.y + 6.8f;
        font.drawString(matrix, displayText, textX - this.xOffset, drawY, typing ? -986118 : -7301470);
        long currentTime = System.currentTimeMillis();
        boolean bl = focused = typing && currentTime % 1000L < 500L;
        if (focused && (this.selectionStart == -1 || this.selectionStart == this.selectionEnd)) {
            float cursorX = font.getStringWidth(this.text.substring(0, this.cursorPosition));
            rectangle.render(ShapeProperties.create(matrix, this.x + 8.0f - this.xOffset + cursorX, this.y + 5.5f, 0.65f, 12.0).color(-1710098).build());
        }
        scissor.pop();
        if (clearAlpha > 0.001f) {
            boolean hovered = MathUtil.isHovered(mouseX, mouseY, this.x + this.width - 23.0f, this.y, 23.0, this.height);
            this.drawIcon(matrix, Fonts.getSize(20, Fonts.Type.COMBINED_ICONS), "\ue05c", this.x + this.width - 12.0f, this.y + this.height / 2.0f, ColorUtil.multAlpha(hovered ? -986118 : -7301470, clearAlpha));
        }
        if (shortcutAlpha > 0.001f && this.text.isEmpty()) {
            int shortcutColor = ColorUtil.multAlpha(-7301470, shortcutAlpha);
            MenuStyle.font(7.0f).drawString(matrix, "CTRL + F", this.x + this.width - 55.0f, this.y + 8.0f, shortcutColor);
            this.drawIcon(matrix, Fonts.getSize(14, Fonts.Type.COMBINED_ICONS), "\ue01d", this.x + this.width - 12.0f, this.y + this.height / 2.0f, shortcutColor);
        }
        if (this.dragging) {
            this.cursorPosition = this.getCursorIndexAt(mouseX);
            if (this.selectionStart == -1) {
                this.selectionStart = this.cursorPosition + 1;
            }
            this.selectionEnd = this.cursorPosition;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if ((typing || !this.text.isEmpty()) && button == 0 && MathUtil.isHovered(mouseX, mouseY, this.x + this.width - 23.0f, this.y, 23.0, this.height)) {
            this.reset();
            return true;
        }
        if (MathUtil.isHovered(mouseX, mouseY, this.x, this.y, this.width, this.height) && button == 0) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - this.lastClickTime < 250L) {
                this.selectionStart = 0;
                this.selectionEnd = this.text.length();
            } else {
                typing = true;
                this.dragging = true;
                this.lastClickTime = currentTime;
                this.selectionStart = this.cursorPosition = this.getCursorIndexAt(mouseX);
                this.selectionEnd = this.cursorPosition;
            }
        } else {
            typing = false;
            this.clearSelection();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (typing && MenuStyle.font(9.0f).getStringWidth(this.text) < this.width - 18.0f) {
            this.deleteSelectedText();
            this.text = this.text.substring(0, this.cursorPosition) + chr + this.text.substring(this.cursorPosition);
            ++this.cursorPosition;
            this.clearSelection();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 70 && (modifiers & 2) != 0) {
            typing = true;
            return true;
        }
        if (typing) {
            if (this.hasControlDown()) {
                switch (keyCode) {
                    case 65: {
                        this.selectAllText();
                        break;
                    }
                    case 86: {
                        this.pasteFromClipboard();
                        break;
                    }
                    case 67: {
                        this.copyToClipboard();
                    }
                }
            } else {
                switch (keyCode) {
                    case 257: 
                    case 259: {
                        this.handleTextModification(keyCode);
                        break;
                    }
                    case 262: 
                    case 263: {
                        this.moveCursor(keyCode);
                    }
                }
            }
        }
        return typing || super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void pasteFromClipboard() {
        String clipboardText = GLFW.glfwGetClipboardString((long)window.getHandle());
        if (clipboardText != null) {
            this.replaceText(this.cursorPosition, this.cursorPosition, clipboardText);
        }
    }

    private void copyToClipboard() {
        if (this.hasSelection()) {
            GLFW.glfwSetClipboardString((long)window.getHandle(), (CharSequence)this.getSelectedText());
        }
    }

    private void selectAllText() {
        this.selectionStart = 0;
        this.selectionEnd = this.text.length();
    }

    private void handleTextModification(int keyCode) {
        if (keyCode == 259) {
            if (this.hasSelection()) {
                this.replaceText(this.getStartOfSelection(), this.getEndOfSelection(), "");
            } else if (this.cursorPosition > 0) {
                this.replaceText(this.cursorPosition - 1, this.cursorPosition, "");
            }
        } else if (keyCode == 257) {
            typing = false;
        }
    }

    private void moveCursor(int keyCode) {
        if (keyCode == 263 && this.cursorPosition > 0) {
            --this.cursorPosition;
        } else if (keyCode == 262 && this.cursorPosition < this.text.length()) {
            ++this.cursorPosition;
        }
        this.updateSelectionAfterCursorMove();
    }

    private void updateSelectionAfterCursorMove() {
        if (this.hasShiftDown()) {
            if (this.selectionStart == -1) {
                this.selectionStart = this.cursorPosition;
            }
            this.selectionEnd = this.cursorPosition;
        } else {
            this.clearSelection();
        }
    }

    private void replaceText(int start, int end, String replacement) {
        if (start < 0) {
            start = 0;
        }
        if (end > this.text.length()) {
            end = this.text.length();
        }
        if (start > end) {
            start = end;
        }
        this.text = this.text.substring(0, start) + replacement + this.text.substring(end);
        this.cursorPosition = start + replacement.length();
        this.clearSelection();
    }

    private boolean hasSelection() {
        return this.selectionStart != -1 && this.selectionEnd != -1 && this.selectionStart != this.selectionEnd;
    }

    private String getSelectedText() {
        return this.text.substring(this.getStartOfSelection(), this.getEndOfSelection());
    }

    private int getStartOfSelection() {
        return Math.min(this.selectionStart, this.selectionEnd);
    }

    private int getEndOfSelection() {
        return Math.max(this.selectionStart, this.selectionEnd);
    }

    private void clearSelection() {
        this.selectionStart = -1;
        this.selectionEnd = -1;
    }

    private int getCursorIndexAt(double mouseX) {
        float textWidth;
        int position;
        FontRenderer font = MenuStyle.font(9.0f);
        float relativeX = (float)mouseX - this.x - 7.0f + this.xOffset;
        for (position = 0; position < this.text.length() && !((textWidth = font.getStringWidth(this.text.substring(0, position + 1))) > relativeX); ++position) {
        }
        return position;
    }

    private boolean hasControlDown() {
        return class_3675.method_15987((class_1041)mc.method_22683(), (int)341) || class_3675.method_15987((class_1041)mc.method_22683(), (int)345);
    }

    private boolean hasShiftDown() {
        return class_3675.method_15987((class_1041)mc.method_22683(), (int)340) || class_3675.method_15987((class_1041)mc.method_22683(), (int)344);
    }

    private void updateXOffset(FontRenderer font, int cursorPosition) {
        float cursorX = font.getStringWidth(this.text.substring(0, cursorPosition));
        if (cursorX < this.xOffset) {
            this.xOffset = cursorX;
        } else if (cursorX - this.xOffset > this.textWidth() - 2.0f) {
            this.xOffset = cursorX - (this.textWidth() - 2.0f);
        }
    }

    private float textWidth() {
        return Math.max(1.0f, this.width - (float)(this.text.isEmpty() && !typing ? 70 : 31));
    }

    private static float smoothProgress(float value) {
        float progress = Math.max(0.0f, Math.min(1.0f, value));
        return progress * progress * (3.0f - 2.0f * progress);
    }

    private void drawIcon(class_4587 matrix, FontRenderer font, String glyph, float centerX, float centerY, int color) {
        FontRenderer.TextBounds bounds = font.getStringBounds(glyph);
        font.drawString(matrix, glyph, centerX - bounds.x() - bounds.width() / 2.0f, centerY - bounds.y() - bounds.height() / 2.0f - Render2DUtil.getTextYOffset(), color);
    }

    private void deleteSelectedText() {
        if (this.hasSelection()) {
            this.replaceText(this.getStartOfSelection(), this.getEndOfSelection(), "");
        }
    }

    public float getExpansion() {
        return this.expansion;
    }

    public String getText() {
        return this.text;
    }
}

