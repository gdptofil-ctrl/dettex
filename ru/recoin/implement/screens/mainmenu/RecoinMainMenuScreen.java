/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10799
 *  net.minecraft.class_11909
 *  net.minecraft.class_156
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_429
 *  net.minecraft.class_437
 *  net.minecraft.class_4587
 *  net.minecraft.class_500
 *  net.minecraft.class_526
 */
package ru.recoin.implement.screens.mainmenu;

import java.net.URI;
import java.util.List;
import net.minecraft.class_11909;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_429;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import net.minecraft.class_500;
import net.minecraft.class_526;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.common.util.render.pipeline.MenuGlowPipeline;
import ru.recoin.common.util.render.pipeline.MenuLogoPipeline;
import ru.recoin.implement.features.modules.render.Hud;
import ru.recoin.implement.screens.mainmenu.AltAccountStore;
import ru.recoin.implement.screens.mainmenu.AltManagerScreen;
import ru.recoin.implement.screens.mainmenu.MenuMotion;
import ru.recoin.implement.screens.mainmenu.ParallaxMenuBackground;
import ru.recoin.implement.screens.mainmenu.RecoinPostGuiScreen;
import ru.recoin.implement.screens.mainmenu.WallpaperSelectorScreen;

public final class RecoinMainMenuScreen
extends class_437
implements QuickImports,
RecoinPostGuiScreen {
    private static final String LOGO_ICON = "\ue0c6";
    private static final String TELEGRAM_ICON = "\ue100";
    private static final String DISCORD_ICON = "\ue101";
    private static final MenuGlowPipeline GLOW = new MenuGlowPipeline();
    private static final MenuLogoPipeline LOGO = new MenuLogoPipeline();
    private static final String[] BUTTON_ICONS = new String[]{"\ue022", "\ue036", "\ue00a", "\ue00b", "\ue06b"};
    private final MenuMotion[] hover = RecoinMainMenuScreen.motions(8);
    private final MenuMotion[] buttonAppear = RecoinMainMenuScreen.motions(8);
    private final MenuMotion wallpaperHover = new MenuMotion();
    private final ParallaxMenuBackground background = new ParallaxMenuBackground();
    private long openedAt;
    private long lastFrameNanos;
    private long closingAt;
    private boolean animationStarted;
    private Runnable pendingAction;

    public RecoinMainMenuScreen() {
        super((class_2561)class_2561.method_43470((String)"DettexClient"));
    }

    public static boolean isRecoinMenu(class_437 screen) {
        return screen instanceof RecoinMainMenuScreen || screen instanceof AltManagerScreen || screen instanceof WallpaperSelectorScreen;
    }

    protected void method_25426() {
        AltAccountStore.restoreLastAccount();
        this.openedAt = 0L;
        this.lastFrameNanos = System.nanoTime();
        this.closingAt = 0L;
        this.animationStarted = false;
        this.pendingAction = null;
        for (MenuMotion motion : this.hover) {
            motion.reset(0.0f);
        }
        for (MenuMotion motion : this.buttonAppear) {
            motion.reset(0.0f);
        }
        this.wallpaperHover.reset(0.0f);
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        this.background.render(context, this.field_22789, this.field_22790, mouseX, mouseY);
    }

    @Override
    public void renderRecoinOverlay(class_332 context, int mouseX, int mouseY, float delta) {
        this.renderMenuContent(context, mouseX, mouseY, delta);
    }

    public void method_25393() {
        if (this.pendingAction != null && System.nanoTime() - this.closingAt >= 130000000L) {
            Runnable action = this.pendingAction;
            this.pendingAction = null;
            action.run();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void renderMenuContent(class_332 context, int mouseX, int mouseY, float delta) {
        Render2DUtil.setActiveContext(context);
        Render2DUtil.setQueuedShapeActive(true);
        try {
            blur.beginFrame();
            this.beginAnimationFrame();
            float frameSeconds = this.frameSeconds();
            float intro = this.introAnimation();
            float previousAlpha = Render2DUtil.getRenderAlpha();
            Render2DUtil.setRenderAlpha(previousAlpha * intro);
            context.method_51448().pushMatrix();
            try {
                int index;
                float scale = this.scale();
                this.drawHero(context, scale);
                List<ButtonLayout> buttons = this.layouts(scale);
                int hovered = -1;
                for (index = 0; index < buttons.size(); ++index) {
                    ButtonLayout button = buttons.get(index);
                    if (!RecoinMainMenuScreen.inside(mouseX, mouseY, button.x(), button.y(), button.width(), button.height())) continue;
                    hovered = index;
                }
                for (index = 0; index < buttons.size(); ++index) {
                    float target = index == hovered ? 1.0f : 0.0f;
                    float hoverValue = this.hover[index].update(target, 1.15f, frameSeconds);
                    float elapsedMs = (float)(System.nanoTime() - this.openedAt) / 1000000.0f;
                    float buttonIntro = this.buttonAppear[index].update(elapsedMs >= (float)index * 20.0f ? 1.0f : 0.0f, 0.75f, frameSeconds);
                    float buttonAlpha = Render2DUtil.getRenderAlpha();
                    Render2DUtil.setRenderAlpha(buttonAlpha * buttonIntro);
                    this.drawButton(context, buttons.get(index), index, hoverValue, scale);
                    Render2DUtil.setRenderAlpha(buttonAlpha);
                }
                float wpBtnSize = 26.0f * scale;
                float wpBtnX = (float)this.field_22789 - wpBtnSize - 16.0f * scale;
                float wpBtnY = 16.0f * scale;
                boolean wpHovered = RecoinMainMenuScreen.inside(mouseX, mouseY, wpBtnX, wpBtnY, wpBtnSize, wpBtnSize);
                float wpHoverValue = this.wallpaperHover.update(wpHovered ? 1.0f : 0.0f, 1.15f, frameSeconds);
                this.drawWallpaperButton(MatrixStackCompat.from(context.method_51448()), wpBtnX, wpBtnY, wpBtnSize, wpHoverValue, scale);
                FontRenderer.flushQueuedText(context);
                context.method_71048();
                this.drawCursorGlow(context, scale);
            }
            finally {
                context.method_51448().popMatrix();
                Render2DUtil.setRenderAlpha(previousAlpha);
            }
        }
        finally {
            blur.setLiveMode(false);
            Render2DUtil.setActiveContext(null);
            Render2DUtil.setQueuedShapeActive(false);
        }
    }

    private void drawHero(class_332 context, float scale) {
        FontRenderer logo = Fonts.getMenuSize(RecoinMainMenuScreen.scaled(190, scale), Fonts.Type.COMBINED_ICONS);
        int logoColor = Hud.getInstance().themeColor();
        float x = (float)this.field_22789 / 2.0f;
        float y = (float)this.field_22790 / 1.6f - 218.0f * scale;
        FontRenderer.TextBounds bounds = logo.getStringBounds(LOGO_ICON);
        float logoX = x - logo.getStringWidth(LOGO_ICON) / 2.0f + bounds.x() + bounds.width() * 2.5f / 62.0f;
        float logoY = y + Render2DUtil.getTextYOffset() + bounds.y() + bounds.height() * 2.5f / 50.0f;
        LOGO.draw(context, logoX, logoY, bounds.width() * 57.0f / 62.0f, bounds.height() * 45.0f / 50.0f, logoColor);
    }

    private void drawCursorGlow(class_332 context, float scale) {
        if (!mc.method_1569()) {
            return;
        }
        float x = (float)RecoinMainMenuScreen.mc.field_1729.method_68879(mc.method_22683());
        float y = (float)RecoinMainMenuScreen.mc.field_1729.method_68883(mc.method_22683());
        if (x < 0.0f || y < 0.0f || x > (float)this.field_22789 || y > (float)this.field_22790) {
            return;
        }
        int color = Hud.getInstance().themeColor();
        GLOW.draw(context, x, y, 88.0f * scale, 88.0f * scale, RecoinMainMenuScreen.alpha(color, 0.24f));
        GLOW.draw(context, x, y, 34.0f * scale, 34.0f * scale, RecoinMainMenuScreen.alpha(RecoinMainMenuScreen.mix(color, -1510145, 0.35f), 0.14f));
    }

    private void drawButton(class_332 context, ButtonLayout button, int index, float animation, float scale) {
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        this.drawGuiBlur(matrix, button.x(), button.y(), button.width(), button.height(), 6.0f * scale, animation);
        int textColor = RecoinMainMenuScreen.mix(-7170399, -1, animation);
        if (index < 5) {
            float textX = button.x() + 10.0f * scale;
            float textY = button.y() + 10.5f * scale;
            Fonts.getMenuSize(RecoinMainMenuScreen.scaled(13, scale), Fonts.Type.INTER_BOLD).drawString(matrix, button.action().label(), textX, textY, textColor);
            this.drawButtonIcon(matrix, index, button.x() + button.width() - 14.0f * scale, button.y() + button.height() / 2.0f, textColor, scale);
        } else {
            String icon = index == 5 ? TELEGRAM_ICON : DISCORD_ICON;
            FontRenderer social = Fonts.getMenuSize(RecoinMainMenuScreen.scaled(30, scale), Fonts.Type.COMBINED_ICONS);
            social.drawCenteredString(matrix, icon, button.x() + button.width() / 1.9f, button.y() + 5.0f * scale, textColor);
        }
    }

    private void drawButtonIcon(class_4587 matrix, int index, float x, float y, int color, float scale) {
        if (index < 0 || index >= BUTTON_ICONS.length) {
            return;
        }
        FontRenderer iconFont = Fonts.getMenuSize(RecoinMainMenuScreen.scaled(18, scale), Fonts.Type.COMBINED_ICONS);
        float opticalOffsetY = index == 4 ? -1.0f * scale : 0.0f;
        iconFont.drawCenteredString(matrix, BUTTON_ICONS[index], x, y - 5.2f * scale + opticalOffsetY, color);
    }

    private List<ButtonLayout> layouts(float scale) {
        float fullWidth = 270.0f * scale;
        float x = ((float)this.field_22789 - fullWidth) / 2.0f;
        float height = 30.0f * scale;
        float half = 131.0f * scale;
        float y = (float)this.field_22790 / 2.0f - 28.0f * scale;
        List<MenuAction> actions = List.of(new MenuAction("\u041e\u0434\u0438\u043d\u043e\u0447\u043d\u0430\u044f \u0438\u0433\u0440\u0430", () -> mc.method_1507((class_437)new class_526((class_437)this))), new MenuAction("\u041c\u043d\u043e\u0433\u043e\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u0435\u043b\u044c\u0441\u043a\u0430\u044f \u0438\u0433\u0440\u0430", () -> mc.method_1507((class_437)new class_500((class_437)this))), new MenuAction("\u0421\u043c\u0435\u043d\u0438\u0442\u044c \u0430\u043a\u043a\u0430\u0443\u043d\u0442", () -> mc.method_1507((class_437)new AltManagerScreen(this))), new MenuAction("\u041e\u043f\u0446\u0438\u0438", () -> mc.method_1507((class_437)new class_429((class_437)this, RecoinMainMenuScreen.mc.field_1690))), new MenuAction("\u0412\u044b\u0445\u043e\u0434", () -> ((class_310)mc).method_1592()), new MenuAction("Telegram", () -> RecoinMainMenuScreen.openUrl("https://t.me/DettexClient")), new MenuAction("Discord", () -> RecoinMainMenuScreen.openUrl("https://discord.gg/F9EC8g4Qz")));
        return List.of(new ButtonLayout(actions.get(0), x, y, fullWidth, height), new ButtonLayout(actions.get(1), x, y + 36.0f * scale, fullWidth, height), new ButtonLayout(actions.get(2), x, y + 72.0f * scale, fullWidth, height), new ButtonLayout(actions.get(3), x, y + 108.0f * scale, half, height), new ButtonLayout(actions.get(4), x + 139.0f * scale, y + 108.0f * scale, half, height), new ButtonLayout(actions.get(5), (float)this.field_22789 / 2.0f - 42.0f * scale, y + 146.0f * scale, 36.0f * scale, 24.0f * scale), new ButtonLayout(actions.get(6), (float)this.field_22789 / 2.0f + 6.0f * scale, y + 146.0f * scale, 36.0f * scale, 24.0f * scale));
    }

    private static void openUrl(String url) {
        class_156.method_668().method_673(URI.create(url));
    }

    private float introAnimation() {
        float t = Math.max(0.0f, Math.min(1.0f, (float)(System.nanoTime() - this.openedAt) / 1.6E8f));
        float appear = t * t * (3.0f - 2.0f * t);
        if (this.pendingAction == null) {
            return appear;
        }
        float closing = Math.max(0.0f, Math.min(1.0f, (float)(System.nanoTime() - this.closingAt) / 1.3E8f));
        return appear * (1.0f - closing * closing * (3.0f - 2.0f * closing));
    }

    private void beginAnimationFrame() {
        if (this.animationStarted) {
            return;
        }
        this.lastFrameNanos = this.openedAt = System.nanoTime();
        this.animationStarted = true;
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        if (this.pendingAction != null) {
            return true;
        }
        if (click.comp_4800().comp_4801() == 0) {
            float scale = this.scale();
            float wpBtnSize = 26.0f * scale;
            float wpBtnX = (float)this.field_22789 - wpBtnSize - 16.0f * scale;
            float wpBtnY = 16.0f * scale;
            if (RecoinMainMenuScreen.inside(click.comp_4798(), click.comp_4799(), wpBtnX, wpBtnY, wpBtnSize, wpBtnSize)) {
                this.startClosing(() -> mc.method_1507((class_437)new WallpaperSelectorScreen(this)));
                return true;
            }
            for (ButtonLayout button : this.layouts(scale)) {
                if (!RecoinMainMenuScreen.inside(click.comp_4798(), click.comp_4799(), button.x(), button.y(), button.width(), button.height())) continue;
                this.startClosing(button.action().action());
                return true;
            }
        }
        return super.method_25402(click, doubled);
    }

    private void startClosing(Runnable action) {
        this.pendingAction = action;
        this.closingAt = System.nanoTime();
    }

    private void drawWallpaperButton(class_4587 matrix, float x, float y, float size, float animation, float scale) {
        float centerX = x + size / 1.85f;
        float centerY = y + size / 2.0f;
        this.drawGuiBlur(matrix, x, y, size, size, 6.0f * scale, animation);
        int iconColor = RecoinMainMenuScreen.mix(-7170399, -1, animation);
        FontRenderer iconFont = Fonts.getMenuSize(RecoinMainMenuScreen.scaled(25, scale), Fonts.Type.COMBINED_ICONS);
        float iconH = iconFont.getStringHeight("\ue057");
        iconFont.drawCenteredString(matrix, "\ue057", centerX, centerY - iconH / 2.0f, iconColor);
    }

    public boolean method_25406(class_11909 click) {
        return super.method_25406(click);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void drawGuiBlur(class_4587 matrix, float x, float y, float w, float h, float round, float hover) {
        boolean oldHud = Render2DUtil.isHudDecorationActive();
        boolean oldGui = Render2DUtil.isGuiDecorationActive();
        Render2DUtil.setHudDecorationActive(true);
        Render2DUtil.setGuiDecorationActive(true);
        try {
            blur.setLiveMode(true);
            blur.setShadowEnabled(false);
            blur.setColorlessMode(true);
            blur.setStyledBlurRadius(28.0f);
            blur.render(ShapeProperties.create(matrix, x, y, w, h).round(round).softness(5.0f).thickness(0.0f).quality(50.0f).outlineColor(0).color(RecoinMainMenuScreen.alpha(RecoinMainMenuScreen.mix(-15724006, Hud.getInstance().themeColor(), 0.22f), 0.6f * (1.0f - hover * 0.18f))).build());
        }
        finally {
            blur.resetStyledBlurRadius();
            blur.setColorlessMode(false);
            blur.setShadowEnabled(true);
            blur.setLiveMode(false);
            Render2DUtil.setHudDecorationActive(oldHud);
            Render2DUtil.setGuiDecorationActive(oldGui);
        }
        this.drawImmediateGlassLayer(matrix, x, y, w, h, round, 0.5f, RecoinMainMenuScreen.alpha(1375797768, 1.0f - hover * 0.34f));
        this.drawImmediateGlassLayer(matrix, x, y, w, h, round, -0.1f, ColorUtil.getOutline(0.8f - hover * 0.16f));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void drawImmediateGlassLayer(class_4587 matrix, float x, float y, float w, float h, float round, float softness, int color) {
        boolean queued = Render2DUtil.isQueuedShapeActive();
        Render2DUtil.setQueuedShapeActive(false);
        try {
            rectangle.render(ShapeProperties.create(matrix, x, y, w, h).round(round).softness(softness).thickness(0.0f).quality(50.0f).outlineColor(0).color(color).build());
        }
        finally {
            Render2DUtil.setQueuedShapeActive(queued);
        }
    }

    private float scale() {
        return Math.max(0.82f, Math.min(1.35f, (float)this.field_22790 / 701.0f));
    }

    private float frameSeconds() {
        long now = System.nanoTime();
        float seconds = Math.min(0.05f, Math.max(0.0f, (float)(now - this.lastFrameNanos) / 1.0E9f));
        this.lastFrameNanos = now;
        return seconds;
    }

    private static MenuMotion[] motions(int count) {
        MenuMotion[] motions = new MenuMotion[count];
        for (int index = 0; index < count; ++index) {
            motions[index] = new MenuMotion();
        }
        return motions;
    }

    private static int scaled(int size, float scale) {
        return Math.max(5, Math.round((float)size * scale));
    }

    private static boolean inside(double mx, double my, float x, float y, float w, float h) {
        return mx >= (double)x && mx <= (double)(x + w) && my >= (double)y && my <= (double)(y + h);
    }

    private static int alpha(int color, float alpha) {
        return Math.round((float)(color >>> 24 & 0xFF) * Math.max(0.0f, Math.min(1.0f, alpha))) << 24 | color & 0xFFFFFF;
    }

    private static int mix(int from, int to, float value) {
        value = Math.max(0.0f, Math.min(1.0f, value));
        int a = Math.round((float)(from >>> 24 & 0xFF) + (float)((to >>> 24 & 0xFF) - (from >>> 24 & 0xFF)) * value);
        int r = Math.round((float)(from >>> 16 & 0xFF) + (float)((to >>> 16 & 0xFF) - (from >>> 16 & 0xFF)) * value);
        int g = Math.round((float)(from >>> 8 & 0xFF) + (float)((to >>> 8 & 0xFF) - (from >>> 8 & 0xFF)) * value);
        int b = Math.round((float)(from & 0xFF) + (float)((to & 0xFF) - (from & 0xFF)) * value);
        return a << 24 | r << 16 | g << 8 | b;
    }

    public boolean method_25422() {
        return false;
    }

    private record ButtonLayout(MenuAction action, float x, float y, float width, float height) {
    }

    private record MenuAction(String label, Runnable action) {
    }
}

