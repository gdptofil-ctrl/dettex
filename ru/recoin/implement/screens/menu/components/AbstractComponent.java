/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.screens.menu.components;

import ru.recoin.common.QuickImports;
import ru.recoin.common.trait.ResizableMovable;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.implement.screens.menu.components.Component;

public abstract class AbstractComponent
implements Component,
QuickImports,
ResizableMovable {
    public float x;
    public float y;
    public float width;
    public float height;
    public double scroll = 0.0;
    public double smoothedScroll = 0.0;

    @Override
    public ResizableMovable position(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    @Override
    public ResizableMovable size(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }

    @Override
    public void tick() {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return false;
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        return MathUtil.isHovered(mouseX, mouseY, this.x, this.y, this.width, this.height);
    }
}

