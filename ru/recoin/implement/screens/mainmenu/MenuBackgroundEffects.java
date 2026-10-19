/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.screens.mainmenu;

import net.minecraft.class_332;
import net.minecraft.class_4587;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;

final class MenuBackgroundEffects
implements QuickImports {
    private static final float BACKGROUND_BLUR_RADIUS = 7.5f;

    private MenuBackgroundEffects() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void renderSoftBlur(class_332 context, int width, int height) {
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        boolean oldHud = Render2DUtil.isHudDecorationActive();
        boolean oldGui = Render2DUtil.isGuiDecorationActive();
        Render2DUtil.setHudDecorationActive(false);
        Render2DUtil.setGuiDecorationActive(true);
        try {
            blur.setLiveMode(true);
            blur.setColorlessMode(true);
            blur.setStyledBlurRadius(7.5f);
            blur.render(ShapeProperties.create(matrix, 0.0, 0.0, width, height).round(0.0f).softness(1.0f).thickness(0.0f).outlineColor(0).color(-1).build());
        }
        finally {
            blur.resetStyledBlurRadius();
            blur.setColorlessMode(false);
            blur.setLiveMode(false);
            Render2DUtil.setHudDecorationActive(oldHud);
            Render2DUtil.setGuiDecorationActive(oldGui);
        }
        context.method_25294(0, 0, width, height, 268633869);
    }
}

