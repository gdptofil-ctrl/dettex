/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 */
package ru.recoin.common.util.render;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import ru.recoin.api.feature.draggable.AbstractDraggable;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.render.GuiRenderFlusher;
import ru.recoin.common.util.render.LegacyRenderSystem;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.core.Main;
import ru.recoin.implement.features.draggables.Notifications;
import ru.recoin.implement.features.modules.render.Hud;
import ru.recoin.implement.features.modules.render.NewHud;

public final class HudFinalPass
implements QuickImports {
    private static class_332 context;
    private static float tickDelta;
    private static boolean rendering;

    private HudFinalPass() {
    }

    public static void beginFrame(class_332 drawContext, float delta) {
        context = drawContext;
        tickDelta = delta;
    }

    public static void clearFrame(class_332 drawContext) {
        if (context == drawContext) {
            context = null;
        }
    }

    public static boolean shouldRenderDraggablesInFinalPass() {
        return true;
    }

    public static void render() {
        HudFinalPass.render(context);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(class_332 drawContext) {
        if (rendering || drawContext == null) {
            return;
        }
        class_332 previousFrameContext = context;
        context = drawContext;
        Hud hud = Hud.getInstance();
        if (hud == null || !hud.isState()) {
            context = previousFrameContext;
            return;
        }
        // Both "Hud 1" and "Hud 2" render through the shared draggable pass.
        rendering = true;
        class_332 previousContext = Render2DUtil.getActiveContext();
        float previousTextYOffset = Render2DUtil.getTextYOffset();
        float previousAlpha = Render2DUtil.getRenderAlpha();
        boolean previousHudDecoration = Render2DUtil.isHudDecorationActive();
        boolean previousGuiDecoration = Render2DUtil.isGuiDecorationActive();
        try {
            List<AbstractDraggable> originalList;
            Render2DUtil.setActiveContext(context);
            Render2DUtil.setTextYOffset(-2.5f);
            Render2DUtil.setHudDecorationActive(true);
            Render2DUtil.setGuiDecorationActive(false);
            Render2DUtil.setRenderAlpha(previousAlpha);
            float hudScale = hud.getRenderedHudScale();
            class_4587 matrix = MatrixStackCompat.from(context.method_51448());
            List<AbstractDraggable> draggables = originalList = Main.getInstance().getDraggableRepository().draggable();
            boolean anyDragging = false;
            for (int i = 0; i < originalList.size(); ++i) {
                if (!originalList.get(i).isDragging()) continue;
                anyDragging = true;
                break;
            }
            if (anyDragging) {
                draggables = new ArrayList<AbstractDraggable>(originalList);
                draggables.sort(Comparator.comparingInt(d -> d.isDragging() ? 1 : 0));
            }
            int draggablesSize = draggables.size();
            for (int i = 0; i < draggablesSize; ++i) {
                AbstractDraggable draggable = draggables.get(i);
                boolean canDraw = false;
                try {
                    canDraw = draggable.canDraw(hud, draggable);
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
                if (canDraw) {
                    draggable.startAnimation();
                } else {
                    draggable.stopAnimation();
                }
                if (draggable.isCloseAnimationFinished()) continue;
                float scale = draggable.getScaleAnimation().getOutput().floatValue();
                draggable.validPosition();
                try {
                    float centerX = (float)draggable.getX() + (float)draggable.getWidth() / 2.0f;
                    float centerY = (float)draggable.getY() + (float)draggable.getHeight() / 2.0f;
                    boolean isNotification = draggable instanceof Notifications;
                    boolean doScale = !isNotification && scale != 1.0f;
                    float[] shaderColor = LegacyRenderSystem.getShaderColor();
                    float prevShaderAlpha = shaderColor[3];
                    if (doScale) {
                        float scale2 = 0.5f + scale / 2.0f;
                        matrix.method_22903();
                        matrix.method_46416(centerX, centerY, 0.0f);
                        matrix.method_22905(scale2, scale2, 1.0f);
                        matrix.method_46416(-centerX, -centerY, 0.0f);
                        LegacyRenderSystem.setShaderColor(shaderColor[0], shaderColor[1], shaderColor[2], scale);
                    }
                    context.method_51448().pushMatrix();
                    context.method_51448().translate(centerX, centerY);
                    float elementScale = draggable.getRenderedScale();
                    context.method_51448().scale(elementScale, elementScale);
                    context.method_51448().translate(-centerX, -centerY);
                    Render2DUtil.setRenderAlpha(previousAlpha * scale);
                    try {
                        draggable.drawDraggable(context);
                        Render2DUtil.onRender(context);
                        context.method_73199();
                        FontRenderer.flushQueuedText(context);
                        GuiRenderFlusher.flush();
                        continue;
                    }
                    finally {
                        context.method_51448().popMatrix();
                        if (doScale) {
                            LegacyRenderSystem.setShaderColor(shaderColor[0], shaderColor[1], shaderColor[2], prevShaderAlpha);
                            matrix.method_22909();
                        }
                    }
                }
                catch (RuntimeException runtimeException) {
                    // empty catch block
                }
            }
        }
        finally {
            Render2DUtil.setActiveContext(previousContext);
            Render2DUtil.setTextYOffset(previousTextYOffset);
            Render2DUtil.setRenderAlpha(previousAlpha);
            Render2DUtil.setHudDecorationActive(previousHudDecoration);
            Render2DUtil.setGuiDecorationActive(previousGuiDecoration);
            rendering = false;
            context = previousFrameContext;
        }
    }
}

