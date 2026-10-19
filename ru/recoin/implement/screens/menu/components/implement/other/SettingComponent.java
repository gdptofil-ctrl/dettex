/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package ru.recoin.implement.screens.menu.components.implement.other;

import net.minecraft.class_332;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.implement.screens.menu.components.AbstractComponent;

public class SettingComponent
extends AbstractComponent {
    private Runnable runnable;

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        Fonts.getMenuSize(17, Fonts.Type.COMBINED_ICONS).drawString(MatrixStackCompat.from(context.method_51448()), "\ue07c", this.x, this.y - 0.5f, -2762524);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtil.isHovered(mouseX, mouseY, this.x, this.y, 7.0, 7.0) && button == 0) {
            this.runnable.run();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public SettingComponent setRunnable(Runnable runnable) {
        this.runnable = runnable;
        return this;
    }
}

