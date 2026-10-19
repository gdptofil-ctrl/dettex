/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.screens.menu.components.implement.other;

import net.minecraft.class_332;
import net.minecraft.class_4587;
import ru.recoin.api.system.animation.Animation;
import ru.recoin.api.system.animation.Direction;
import ru.recoin.api.system.animation.implement.DecelerateAnimation;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.implement.features.modules.render.Hud;
import ru.recoin.implement.screens.menu.components.AbstractComponent;

public class CheckComponent
extends AbstractComponent {
    private boolean state;
    private Runnable runnable;
    private boolean initialized;
    private final Animation alphaAnimation = new DecelerateAnimation().setMs(180).setValue(1.0);

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        int outlineStateColor;
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        int accent = Hud.getInstance().colorSetting.getColor();
        if (!this.initialized) {
            this.initialized = true;
            this.alphaAnimation.setDirection(this.state ? Direction.FORWARDS : Direction.BACKWARDS);
            this.alphaAnimation.complete();
        }
        this.alphaAnimation.setDirection(this.state ? Direction.FORWARDS : Direction.BACKWARDS);
        float active = this.alphaAnimation.getOutput().floatValue();
        int n = outlineStateColor = this.state ? ColorUtil.multAlpha(accent, 0.35f) : -13617081;
        if (active > 0.03f) {
            image.setTexture("textures/glow.png").render(ShapeProperties.create(matrix, this.x - 10.0f, this.y - 10.0f, 44.0, 32.0).color(ColorUtil.multAlpha(accent, 0.34f * active)).build());
            image.setTexture("textures/glow.png").render(ShapeProperties.create(matrix, this.x - 4.0f, this.y - 4.0f, 32.0, 20.0).color(ColorUtil.multAlpha(accent, 0.22f * active)).build());
        }
        rectangle.render(ShapeProperties.create(matrix, this.x, this.y, 24.0, 12.0).round(6.0f).thickness(1.0f).softness(0.5f).outlineColor(outlineStateColor).color(ColorUtil.overCol(-1256052426, ColorUtil.multAlpha(accent, 0.72f), active)).build());
        rectangle.render(ShapeProperties.create(matrix, this.x + 2.0f + 12.0f * active, this.y + 2.0f, 8.0, 8.0).round(4.0f).color(-1).build());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtil.isHovered(mouseX, mouseY, this.x, this.y, 24.0, 12.0) && button == 0) {
            this.runnable.run();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public CheckComponent setState(boolean state) {
        this.state = state;
        return this;
    }

    public CheckComponent setRunnable(Runnable runnable) {
        this.runnable = runnable;
        return this;
    }

    public CheckComponent setInitialized(boolean initialized) {
        this.initialized = initialized;
        return this;
    }
}

