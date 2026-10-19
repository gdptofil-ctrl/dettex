/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 */
package ru.recoin.implement.screens.hud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import ru.recoin.api.feature.draggable.AbstractDraggable;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.GuiRenderFlusher;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.common.util.render.pipeline.DropShadowPipeline;
import ru.recoin.core.Main;
import ru.recoin.implement.features.draggables.Notifications;
import ru.recoin.implement.features.draggables.Potions;
import ru.recoin.implement.features.draggables.TargetHud;
import ru.recoin.implement.features.draggables.Watermark;
import ru.recoin.implement.features.modules.render.Hud;

public final class HudEditorOverlay
implements QuickImports {
    private static float hintAlpha;
    private static AbstractDraggable lastHovered;
    private static final float ROW_H = 12.0f;
    private static final float HEADER_H = 16.0f;
    private static final float PANEL_W = 142.0f;
    private static final DropShadowPipeline panelShadow;
    private static final DropShadowPipeline accentShadow;
    private static final Map<AbstractDraggable, PanelState> panels;
    private static final Map<String, Float> optionAnimations;
    private static final Map<String, Boolean> optionStateCache;

    private HudEditorOverlay() {
    }

    public static void render(class_332 context, int mouseX, int mouseY) {
        HudEditorOverlay.renderInternal(context, mouseX, mouseY, true);
    }

    public static void renderClosing(class_332 context) {
        panels.values().forEach(panel -> {
            panel.open = false;
        });
        lastHovered = null;
        HudEditorOverlay.renderInternal(context, -10000, -10000, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void renderInternal(class_332 context, int mouseX, int mouseY, boolean editorOpen) {
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        FontRenderer font = Fonts.getSize(11, Fonts.Type.INTER_BOLD);
        AbstractDraggable hovered = null;
        if (editorOpen) {
            List<AbstractDraggable> draggables;
            List<AbstractDraggable> list = draggables = Main.getInstance().getDraggableRepository().draggable();
            synchronized (list) {
                for (int i = draggables.size() - 1; i >= 0; --i) {
                    AbstractDraggable d = draggables.get(i);
                    if (!d.canDraw(Hud.getInstance(), d) || !d.isHovered(mouseX, mouseY)) continue;
                    hovered = d;
                    break;
                }
            }
        }
        if (hovered != null) {
            lastHovered = hovered;
        }
        hintAlpha = MathUtil.interpolateSmooth(1.55, hintAlpha, hovered != null ? 1.0f : 0.0f);
        if (!editorOpen) {
            panels.values().forEach(panel -> {
                panel.open = false;
            });
        }
        Iterator<Map.Entry<AbstractDraggable, PanelState>> iterator2 = panels.entrySet().iterator();
        while (iterator2.hasNext()) {
            Map.Entry<AbstractDraggable, PanelState> entry = iterator2.next();
            AbstractDraggable draggable = entry.getKey();
            PanelState panel2 = entry.getValue();
            if (editorOpen && panel2.open && !draggable.canDraw(Hud.getInstance(), draggable)) {
                panel2.open = false;
            }
            HudEditorOverlay.updatePanelAlpha(panel2);
            if (!panel2.open && panel2.alpha <= 0.005f) {
                iterator2.remove();
                continue;
            }
            List<Option> options = HudEditorOverlay.options(draggable);
            if (options.isEmpty()) {
                iterator2.remove();
                continue;
            }
            HudEditorOverlay.positionPanel(draggable, panel2, options.size());
            HudEditorOverlay.drawPanel(context, matrix, font, draggable, panel2, options, mouseX, mouseY);
        }
    }

    private static void updatePanelAlpha(PanelState panel) {
        panel.alpha = MathUtil.interpolateSmooth(2.0, panel.alpha, panel.open ? 1.0f : 0.0f);
        if (panel.open && panel.alpha > 0.995f) {
            panel.alpha = 1.0f;
        }
        if (!panel.open && panel.alpha < 0.005f) {
            panel.alpha = 0.0f;
        }
    }

    public static void renderHintBehind(class_332 context) {
        if (lastHovered == null || hintAlpha <= 0.02f) {
            return;
        }
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        HudEditorOverlay.drawHint(matrix, Fonts.getSize(12, Fonts.Type.INTER_BOLD), lastHovered, hintAlpha);
        FontRenderer.flushQueuedText(context);
    }

    private static void positionPanel(AbstractDraggable draggable, PanelState panel, int optionCount) {
        panel.w = 142.0f;
        panel.h = 16.0f + (float)optionCount * 12.0f + 4.0f;
        float selectedScale = draggable.getRenderedScale();
        float extraWidth = (float)draggable.getWidth() * (selectedScale - 1.0f) / 2.0f;
        float extraHeight = (float)draggable.getHeight() * (selectedScale - 1.0f) / 2.0f;
        float selectedTop = (float)draggable.getY() - extraHeight;
        float rightX = (float)(draggable.getX() + draggable.getWidth()) + extraWidth + 6.0f;
        float leftX = (float)draggable.getX() - extraWidth - panel.w - 6.0f;
        if (rightX + panel.w <= (float)window.getScaledWidth() - 3.0f) {
            panel.x = rightX;
            panel.y = Math.max(3.0f, Math.min(selectedTop, (float)window.getScaledHeight() - panel.h - 3.0f));
        } else if (leftX >= 3.0f) {
            panel.x = leftX;
            panel.y = Math.max(3.0f, Math.min(selectedTop, (float)window.getScaledHeight() - panel.h - 3.0f));
        } else {
            panel.x = Math.max(3.0f, Math.min((float)draggable.getX() + ((float)draggable.getWidth() - panel.w) / 2.0f, (float)window.getScaledWidth() - panel.w - 3.0f));
            float below = (float)(draggable.getY() + draggable.getHeight()) + extraHeight + 6.0f;
            panel.y = below + panel.h <= (float)window.getScaledHeight() - 3.0f ? below : Math.max(3.0f, selectedTop - panel.h - 6.0f);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void drawPanel(class_332 context, class_4587 matrix, FontRenderer font, AbstractDraggable draggable, PanelState panel, List<Option> options, int mouseX, int mouseY) {
        float previousAlpha = Render2DUtil.getRenderAlpha();
        float previousTextYOffset = Render2DUtil.getTextYOffset();
        boolean previousHudDecoration = Render2DUtil.isHudDecorationActive();
        Render2DUtil.setHudDecorationActive(true);
        Render2DUtil.setRenderAlpha(previousAlpha * panel.alpha);
        Render2DUtil.setTextYOffset(0.0f);
        try {
            HudEditorOverlay.drawPanelShadow(matrix, panel);
            blur.render(ShapeProperties.create(matrix, panel.x, panel.y, panel.w, panel.h).round(5.0f).softness(3.5f).thickness(0.0f).quality(32.0f).outlineColor(0).color(-268435456).build());
            rectangle.render(ShapeProperties.create(matrix, panel.x, panel.y, panel.w, panel.h).round(5.0f).softness(0.5f).thickness(1.0f).quality(50.0f).outlineColor(ColorUtil.multAlpha(Hud.getInstance().colorSetting.getColor(), 0.3f)).color(0x26000000).build());
            String elementName = draggable.getName();
            font.drawString(matrix, HudEditorOverlay.fitText(elementName, panel.w - 19.0f, font), panel.x + 8.0f, panel.y + 3.0f, ColorUtil.getText(0.75f));
            rectangle.render(ShapeProperties.create(matrix, panel.x + 4.0f, panel.y + 16.0f - 1.0f, panel.w - 8.0f, 1.0).round(0.5f).color(ColorUtil.multAlpha(Hud.getInstance().colorSetting.getColor(), 0.42f)).build());
            FontRenderer settingIconFont = Fonts.getSize(12, Fonts.Type.COMBINED_ICONS);
            for (int i = 0; i < options.size(); ++i) {
                boolean hovered;
                Option option = options.get(i);
                float y = panel.y + 16.0f + (float)i * 12.0f;
                boolean bl = hovered = (float)mouseX >= panel.x && (float)mouseX <= panel.x + panel.w && (float)mouseY >= y && (float)mouseY <= y + 12.0f;
                if (hovered) {
                    rectangle.render(ShapeProperties.create(matrix, panel.x + 3.0f, y + 0.5f, panel.w - 6.0f, 11.0).round(3.0f).color(ColorUtil.multAlpha(Hud.getInstance().colorSetting.getColor(), 0.12f)).build());
                }
                if (option.scale) {
                    HudEditorOverlay.drawScaleAction(matrix, font, draggable, panel, y);
                    continue;
                }
                settingIconFont.drawString(matrix, "\ue09b", panel.x + 7.0f, y + 2.4f, ColorUtil.getClientColor());
                font.drawString(matrix, option.name, panel.x + 22.0f, y + 2.5f, ColorUtil.getText());
                String animationKey = draggable.getName() + "\u0000" + option.name;
                HudEditorOverlay.drawStateIcon(matrix, animationKey, option.enabled(), panel.x + panel.w - 11.0f, y + 6.0f);
            }
            Render2DUtil.onRender(context);
            context.method_73199();
            FontRenderer.flushQueuedText(context);
            GuiRenderFlusher.flush();
        }
        finally {
            Render2DUtil.setRenderAlpha(previousAlpha);
            Render2DUtil.setTextYOffset(previousTextYOffset);
            Render2DUtil.setHudDecorationActive(previousHudDecoration);
        }
    }

    private static void drawPanelShadow(class_4587 matrix, PanelState panel) {
        Hud hud = Hud.getInstance();
        if (hud.shouldDisableShadows(false, true)) {
            return;
        }
        Matrix4f transform = matrix.method_23760().method_23761();
        Vector3f position = transform.transformPosition(panel.x, panel.y, 0.0f, new Vector3f());
        float scale = Math.abs(transform.m00());
        float width = panel.w * scale;
        float height = panel.h * scale;
        float x = position.x;
        float y = position.y;
        accentShadow.draw(x, y, width, height, new float[]{5.0f * scale, 5.0f * scale, 5.0f * scale, 5.0f * scale}, 13.0f * scale, 0.8f * scale, Render2DUtil.applyRenderAlpha(ColorUtil.multAlpha(hud.colorSetting.getColor(), 0.13f)));
        panelShadow.draw(x, y, width, height, new float[]{5.0f * scale, 5.0f * scale, 5.0f * scale, 5.0f * scale}, 11.0f * scale, 1.5f * scale, Render2DUtil.applyRenderAlpha(-1459617792));
    }

    private static String fitText(String text, float width, FontRenderer font) {
        int end;
        if (font.getStringWidth(text) <= width) {
            return text;
        }
        String suffix = "\u2026";
        for (end = text.length(); end > 0 && font.getStringWidth(text.substring(0, end) + suffix) > width; --end) {
        }
        return end == 0 ? suffix : text.substring(0, end) + suffix;
    }

    private static void drawScaleAction(class_4587 matrix, FontRenderer font, AbstractDraggable draggable, PanelState panel, float y) {
        float buttonSize = 9.0f;
        float minusX = panel.x + 4.0f;
        float plusX = panel.x + panel.w - buttonSize - 4.0f;
        rectangle.render(ShapeProperties.create(matrix, minusX, y + 1.0f, buttonSize, buttonSize).round(3.0f).softness(0.3f).color(1109666616).build());
        rectangle.render(ShapeProperties.create(matrix, plusX, y + 1.0f, buttonSize, buttonSize).round(3.0f).softness(0.3f).color(1109666616).build());
        font.drawCenteredString(matrix, "\u2212", minusX + buttonSize / 2.0f, y + 1.5f, ColorUtil.getText());
        font.drawCenteredString(matrix, "+", plusX + buttonSize / 2.0f, y + 1.5f, ColorUtil.getText());
        font.drawString(matrix, "\u041c\u0430\u0441\u0448\u0442\u0430\u0431", panel.x + 18.0f, y + 2.0f, ColorUtil.getText());
        String value = Math.round(draggable.getElementScale() * 100.0f) + "%";
        float valueWidth = font.getStringWidth(value);
        font.drawString(matrix, value, plusX - valueWidth - 4.0f, y + 2.0f, ColorUtil.getClientColor());
    }

    private static void drawStateIcon(class_4587 matrix, String key, boolean enabled, float centerX, float y) {
        Boolean cached = optionStateCache.put(key, enabled);
        if (cached != null && cached != enabled) {
            optionAnimations.put(key, Float.valueOf(0.0f));
        }
        float progress = optionAnimations.getOrDefault(key, Float.valueOf(1.0f)).floatValue();
        if ((progress = MathUtil.interpolateSmooth(1.75, progress, 1.0f)) > 0.985f) {
            progress = 1.0f;
        }
        optionAnimations.put(key, Float.valueOf(progress));
        float ease = 1.0f - (float)Math.pow(1.0f - progress, 3.0);
        float bounce = (float)Math.sin((double)ease * Math.PI) * 0.16f;
        int size = Math.round(10.0f + 3.0f * ease + 2.0f * bounce);
        int color = ColorUtil.multAlpha(enabled ? -12197777 : -41370, 0.32f + 0.68f * ease);
        String marker = enabled ? "\ue05a" : "\ue05c";
        float box = 9.0f + bounce;
        rectangle.render(ShapeProperties.create(matrix, centerX - box / 2.0f, y - 3.6f, box, box).round(3.0f).softness(0.35f).thickness(1.0f).quality(16.0f).outlineColor(0x2FFFFFFF).color(1109666616).build());
        float iconX = centerX;
        float iconY = y - 1.1f * (1.0f - ease);
        if (enabled) {
            iconX += 0.6f;
            iconY -= 1.9f;
        } else {
            iconX += 1.25f;
            iconY -= 1.9f;
        }
        Fonts.getMenuSize(size, Fonts.Type.COMBINED_ICONS).drawCenteredString(matrix, marker, iconX, iconY, color);
    }

    private static void drawHint(class_4587 matrix, FontRenderer font, AbstractDraggable d, float alpha) {
        boolean configurable;
        String first = "\u041b\u041a\u041c \u043f\u0435\u0440\u0435\u043c\u0435\u0449\u0430\u0442\u044c";
        String second = "\u041f\u041a\u041c \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u044f";
        boolean movable = d.canDragX() || d.canDragY();
        boolean bl = configurable = !HudEditorOverlay.options(d).isEmpty();
        if (!movable && !configurable) {
            return;
        }
        float width = Math.max(movable ? font.getStringWidth(first) : 0.0f, configurable ? font.getStringWidth(second) : 0.0f);
        float x = (float)d.getX() + ((float)d.getWidth() - width) / 2.0f;
        float y = (float)d.getY() - (movable && configurable ? 17.0f : 9.0f) + (1.0f - alpha) * 6.0f;
        if (movable) {
            font.drawString(matrix, first, x, y, ColorUtil.getText(alpha));
        }
        if (configurable) {
            font.drawString(matrix, second, x, y + (movable ? 8.0f : 0.0f), ColorUtil.getText(alpha * 0.75f));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean click(double mouseX, double mouseY, int button) {
        if (button == 1) {
            List<AbstractDraggable> draggables = Main.getInstance().getDraggableRepository().draggable();
            AbstractDraggable clicked = null;
            List<AbstractDraggable> list = draggables;
            synchronized (list) {
                for (int i = draggables.size() - 1; i >= 0; --i) {
                    AbstractDraggable d = draggables.get(i);
                    if (!d.canDraw(Hud.getInstance(), d) || !d.isHovered(mouseX, mouseY)) continue;
                    clicked = d;
                    break;
                }
            }
            if (clicked == null) {
                return false;
            }
            Main.getInstance().getDraggableRepository().bringToFront(clicked);
            boolean bl = panels.get(clicked) == null || !HudEditorOverlay.panels.get(clicked).open;
            PanelState panel = panels.computeIfAbsent(clicked, ignored -> new PanelState());
            panel.open = bl;
            HudEditorOverlay.bringToFront(clicked, panel);
            return true;
        }
        if (button != 0 || panels.isEmpty()) {
            return false;
        }
        ArrayList<Map.Entry<AbstractDraggable, PanelState>> entries = new ArrayList<Map.Entry<AbstractDraggable, PanelState>>(panels.entrySet());
        Collections.reverse(entries);
        for (Map.Entry entry : entries) {
            AbstractDraggable draggable = (AbstractDraggable)entry.getKey();
            PanelState panel = (PanelState)entry.getValue();
            if (!panel.open || panel.alpha <= 0.02f || mouseX < (double)panel.x || mouseX > (double)(panel.x + panel.w) || mouseY < (double)panel.y || mouseY > (double)(panel.y + panel.h)) continue;
            HudEditorOverlay.bringToFront(draggable, panel);
            List<Option> options = HudEditorOverlay.options(draggable);
            for (int i = 0; i < options.size(); ++i) {
                float y = panel.y + 16.0f + (float)i * 12.0f;
                if (!(mouseY >= (double)y) || !(mouseY <= (double)(y + 12.0f))) continue;
                Option option = options.get(i);
                if (option.scale) {
                    float buttonSize = 9.0f;
                    if (mouseX >= (double)(panel.x + 4.0f) && mouseX <= (double)(panel.x + 4.0f + buttonSize)) {
                        draggable.setElementScale(draggable.getElementScale() - 0.05f);
                    } else if (mouseX >= (double)(panel.x + panel.w - buttonSize - 4.0f) && mouseX <= (double)(panel.x + panel.w - 4.0f)) {
                        draggable.setElementScale(draggable.getElementScale() + 0.05f);
                    }
                } else {
                    option.toggle.run();
                }
                return true;
            }
            return true;
        }
        return false;
    }

    private static void bringToFront(AbstractDraggable draggable, PanelState panel) {
        if (draggable != null && panel != null) {
            panels.remove(draggable);
            panels.put(draggable, panel);
        }
    }

    private static List<Option> options(AbstractDraggable draggable) {
        Hud hud = Hud.getInstance();
        ArrayList<Option> result = new ArrayList<Option>();
        result.add(Option.scaleAction());
        if (draggable instanceof TargetHud) {
            result.add(HudEditorOverlay.bool("\u041a\u0440\u0430\u0441\u043d\u0435\u0442\u044c \u043f\u0440\u0438 \u0443\u0434\u0430\u0440\u0435", hud.targetRedOnHit));
            result.add(HudEditorOverlay.bool("\u041f\u0430\u0440\u0442\u0438\u043a\u043b\u044b \u043f\u0440\u0438 \u0443\u0434\u0430\u0440\u0435", hud.targetHitParticles));
            result.add(HudEditorOverlay.bool("\u041e\u0442\u043e\u0431\u0440\u0430\u0436\u0430\u0442\u044c \u043f\u0440\u0438 \u043d\u0430\u0432\u043e\u0434\u043a\u0435", hud.targetShowOnHover));
            result.add(HudEditorOverlay.bool("\u041f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u044c \u0431\u0440\u043e\u043d\u0438 FunTime", hud.targetFunTimeDurability));
        } else if (draggable instanceof Notifications) {
            result.add(HudEditorOverlay.multi("\u041f\u043e\u0434\u0431\u043e\u0440 \u0434\u043e\u043d\u0430\u0442 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432", "Item Pick Up"));
            result.add(HudEditorOverlay.multi("\u041e\u043a\u043e\u043d\u0447\u0430\u043d\u0438\u0435 \u044d\u0444\u0444\u0435\u043a\u0442\u043e\u0432", "Potion Expire"));
            result.add(HudEditorOverlay.multi("\u041b\u043e\u043c\u0430\u043d\u0438\u0435 \u0449\u0438\u0442\u0430", "Break Shield"));
            result.add(HudEditorOverlay.multi("\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432", "Item Cooldown"));
            result.add(HudEditorOverlay.multi("\u0421\u043d\u0435\u0441\u0435\u043d\u0438\u0435 \u0442\u043e\u0442\u0435\u043c\u043e\u0432", "Totem Pop"));
            result.add(HudEditorOverlay.multi("\u0417\u0430\u0445\u043e\u0434 \u043c\u043e\u0434\u0435\u0440\u0430\u0442\u043e\u0440\u0430", "Staff Join"));
            result.add(HudEditorOverlay.multi("\u041d\u0438\u0437\u043a\u0430\u044f \u043f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u044c", "Low Durability"));
        } else if (draggable instanceof Potions) {
            result.add(HudEditorOverlay.bool("\u0411\u043e\u043a\u043e\u0432\u043e\u0435 \u043e\u0442\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u0435", hud.potionsSide));
        } else if (draggable instanceof Watermark) {
            result.add(HudEditorOverlay.bool("\u041f\u043e \u0446\u0435\u043d\u0442\u0440\u0443 \u0441\u0432\u0435\u0440\u0445\u0443", hud.watermarkCentered));
            result.add(HudEditorOverlay.bool("\u0420\u0430\u0437\u0434\u0435\u043b\u044f\u0442\u044c \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u044b", hud.watermarkSeparateElements));
            for (String metric : List.of("BPS", "COORDS", "PING", "TIME", "TPS", "FPS")) {
                result.add(HudEditorOverlay.metric(metric));
            }
        }
        return result;
    }

    private static Option bool(String name, BooleanSetting setting) {
        return new Option(name, setting::isValue, () -> setting.setValue(!setting.isValue()), false);
    }

    private static Option multi(String name, String key) {
        return new Option(name, () -> Hud.getInstance().notificationSettings.isSelected(key), () -> HudEditorOverlay.toggle(Hud.getInstance().notificationSettings.getSelected(), key), false);
    }

    private static Option metric(String key) {
        return new Option(key, () -> Hud.getInstance().watermarkMetrics.isSelected(key), () -> HudEditorOverlay.toggle(Hud.getInstance().watermarkMetrics.getSelected(), key), false);
    }

    private static void toggle(List<String> selected, String key) {
        if (!selected.remove(key)) {
            selected.add(key);
        }
    }

    static {
        panelShadow = new DropShadowPipeline();
        accentShadow = new DropShadowPipeline();
        panels = new LinkedHashMap<AbstractDraggable, PanelState>();
        optionAnimations = new HashMap<String, Float>();
        optionStateCache = new HashMap<String, Boolean>();
    }

    private static final class PanelState {
        float x;
        float y;
        float w = 142.0f;
        float h;
        float alpha;
        boolean open;

        private PanelState() {
        }
    }

    private record Option(String name, BooleanSupplier state, Runnable toggle, boolean scale) {
        static Option scaleAction() {
            return new Option("\u041c\u0430\u0441\u0448\u0442\u0430\u0431", () -> true, () -> {}, true);
        }

        boolean enabled() {
            return this.state.getAsBoolean();
        }
    }
}

