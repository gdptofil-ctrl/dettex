/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1041
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_3675
 *  net.minecraft.class_4587
 *  org.lwjgl.glfw.GLFW
 */
package ru.recoin.implement.screens.menu.components.implement.settings;

import net.minecraft.class_1041;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3675;
import net.minecraft.class_4587;
import org.lwjgl.glfw.GLFW;
import ru.recoin.api.feature.module.setting.implement.TextSetting;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.other.StringUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.ScissorManager;
import ru.recoin.core.Main;
import ru.recoin.implement.features.modules.render.Hud;
import ru.recoin.implement.screens.menu.MenuDescriptionLocalizer;
import ru.recoin.implement.screens.menu.components.implement.other.DropdownMenuComponent;
import ru.recoin.implement.screens.menu.components.implement.settings.AbstractSettingComponent;

public class TextComponent
extends AbstractSettingComponent {
    public static boolean typing;
    private static TextComponent focused;
    private final TextSetting setting;
    private float rectX;
    private float rectY;
    private float rectWidth;
    private float rectHeight;
    private boolean dragging;
    private int cursorPosition = 0;
    private int selectionStart = -1;
    private int selectionEnd = -1;
    private long lastClickTime = 0L;
    private float xOffset = 0.0f;
    private String text = "";

    public TextComponent(TextSetting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        boolean caretBlink;
        int end;
        int start;
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        String description = StringUtil.ellipsize(MenuDescriptionLocalizer.localize(this.setting.getDescription()), this.width - 108.0f, 10);
        FontRenderer font = Fonts.getMenuSize(12);
        this.height = 34.0f;
        this.rectX = this.x + this.width - 86.0f;
        this.rectY = this.y + 9.0f;
        this.rectWidth = 76.0f;
        this.rectHeight = 16.0f;
        rectangle.render(ShapeProperties.create(matrix, this.rectX, this.rectY, this.rectWidth, this.rectHeight).round(4.0f).thickness(1.0f).outlineColor(-13945788).color(-15394011).build());
        Fonts.getMenuSize(11, Fonts.Type.BOLD).drawString(matrix, DropdownMenuComponent.settingName(this.setting.getName()), this.x + 20.0f, this.y + 11.0f, -1446412);
        Fonts.getMenuSize(9).drawString(matrix, description, this.x + 20.0f, this.y + 21.0f, -10656136);
        this.updateXOffset(font, this.cursorPosition);
        ScissorManager scissor = Main.getInstance().getScissorManager();
        scissor.push(matrix.method_23760().method_23761(), this.rectX + 1.0f, (float)window.getScaledHeight() / 2.0f - 96.0f, this.rectWidth - 3.0f, 220.0f);
        boolean active = this.active();
        if (active && this.selectionStart != -1 && this.selectionEnd != -1 && this.selectionStart != this.selectionEnd && (start = Math.max(0, Math.min(this.getStartOfSelection(), this.text.length()))) < (end = Math.max(0, Math.min(this.getEndOfSelection(), this.text.length())))) {
            float selectionXStart = this.rectX + 3.0f - this.xOffset + font.getStringWidth(this.text.substring(0, start));
            float selectionXEnd = this.rectX + 3.0f - this.xOffset + font.getStringWidth(this.text.substring(0, end));
            float selectionWidth = selectionXEnd - selectionXStart;
            rectangle.render(ShapeProperties.create(matrix, selectionXStart, this.rectY + this.rectHeight / 2.0f - 4.0f, selectionWidth, 8.0).color(ColorUtil.multAlpha(Hud.getInstance().colorSetting.getColor(), 0.45f)).build());
        }
        font.drawString(matrix, this.text, this.rectX + 3.0f - this.xOffset, this.rectY + this.rectHeight / 2.0f - 1.0f, active ? -1 : -7894892);
        if (!active && this.text.isEmpty()) {
            this.text = this.setting.getText();
            font.drawString(matrix, this.text, this.rectX + 3.0f, this.rectY + this.rectHeight / 2.0f - 1.0f, -7894892);
        }
        scissor.pop();
        long currentTime = System.currentTimeMillis();
        boolean bl = caretBlink = active && currentTime % 1000L < 500L;
        if (caretBlink && (this.selectionStart == -1 || this.selectionStart == this.selectionEnd)) {
            float cursorX = font.getStringWidth(this.text.substring(0, this.cursorPosition));
            rectangle.render(ShapeProperties.create(matrix, this.rectX + 3.0f - this.xOffset + cursorX, this.rectY + this.rectHeight / 2.0f - 3.5f, 0.5, 7.0).color(-1).build());
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
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        this.dragging = true;
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtil.isHovered(mouseX, mouseY, this.rectX, this.rectY, this.rectWidth, this.rectHeight) && button == 0) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - this.lastClickTime < 250L) {
                focused = this;
                typing = true;
                this.selectionStart = 0;
                this.selectionEnd = this.text.length();
            } else {
                focused = this;
                typing = true;
                this.dragging = true;
                this.lastClickTime = currentTime;
                this.selectionStart = this.cursorPosition = this.getCursorIndexAt(mouseX);
                this.selectionEnd = this.cursorPosition;
            }
        } else {
            if (focused == this) {
                focused = null;
                typing = false;
            }
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
        if (this.active() && this.text.length() < this.setting.getMax()) {
            this.deleteSelectedText();
            this.text = this.text.substring(0, this.cursorPosition) + chr + this.text.substring(this.cursorPosition);
            ++this.cursorPosition;
            this.clearSelection();
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.active()) {
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
        return super.keyPressed(keyCode, scanCode, modifiers);
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
        } else if (keyCode == 257 && this.text.length() >= this.setting.getMin() && this.text.length() <= this.setting.getMax()) {
            this.setting.setText(this.text);
            typing = false;
            focused = null;
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
        FontRenderer font = Fonts.getMenuSize(12, Fonts.Type.BOLD);
        float relativeX = (float)mouseX - this.rectX - 3.0f + this.xOffset;
        for (position = 0; position < this.text.length() && !((textWidth = font.getStringWidth(this.text.substring(0, position + 1))) > relativeX); ++position) {
        }
        return position;
    }

    private void updateXOffset(FontRenderer font, int cursorPosition) {
        float cursorX = font.getStringWidth(this.text.substring(0, cursorPosition));
        if (cursorX < this.xOffset) {
            this.xOffset = cursorX;
        } else if (cursorX - this.xOffset > this.rectWidth - 7.0f) {
            this.xOffset = cursorX - (this.rectWidth - 7.0f);
        }
    }

    private boolean active() {
        return typing && focused == this;
    }

    private boolean hasControlDown() {
        return class_3675.method_15987((class_1041)class_310.method_1551().method_22683(), (int)341) || class_3675.method_15987((class_1041)class_310.method_1551().method_22683(), (int)345);
    }

    private boolean hasShiftDown() {
        return class_3675.method_15987((class_1041)class_310.method_1551().method_22683(), (int)340) || class_3675.method_15987((class_1041)class_310.method_1551().method_22683(), (int)344);
    }

    private void deleteSelectedText() {
        if (this.hasSelection()) {
            this.replaceText(this.getStartOfSelection(), this.getEndOfSelection(), "");
        }
    }
}

