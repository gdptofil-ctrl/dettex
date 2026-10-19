/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1068
 *  net.minecraft.class_11905
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_320
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.screens.mainmenu;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_1068;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_320;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.render.ItemGuiAlpha;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.common.util.render.ScissorManager;
import ru.recoin.core.Main;
import ru.recoin.implement.screens.mainmenu.AltAccountStore;
import ru.recoin.implement.screens.mainmenu.MenuMotion;
import ru.recoin.implement.screens.mainmenu.ParallaxMenuBackground;
import ru.recoin.implement.screens.mainmenu.RandomNicknamePool;
import ru.recoin.implement.screens.mainmenu.RecoinPostGuiScreen;
import ru.recoin.mixins.MinecraftClientSessionAccessor;

final class AltManagerScreen
extends class_437
implements QuickImports,
RecoinPostGuiScreen {
    private static final float PANEL_WIDTH = 286.0f;
    private static final float PANEL_HEIGHT = 298.0f;
    private static final float PADDING = 12.0f;
    private static final float INPUT_HEIGHT = 24.0f;
    private static final float TOP_BUTTON_SIZE = 24.0f;
    private static final float ROW_HEIGHT = 35.0f;
    private static final float ROW_GAP = 5.0f;
    private static final int VISIBLE_ACCOUNTS = 5;
    private static final long OPEN_ANIMATION_MS = 160L;
    private static final long CLOSE_ANIMATION_MS = 130L;
    private static final int ACCENT = -10314753;
    private static final int PANEL_FILL = -1710220734;
    private static final int ROW_FILL = -1710220734;
    private static final int GLASS_OUTLINE = 0;
    private static final String TRASH_ICON = "\ue070";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final class_437 parent;
    private final List<String> accounts = new ArrayList<String>();
    private final Set<String> favorites = new LinkedHashSet<String>();
    private final Set<String> generatedNicknames = new LinkedHashSet<String>();
    private final Map<String, String> dates = new LinkedHashMap<String, String>();
    private final Map<String, MenuMotion> rowHover = new LinkedHashMap<String, MenuMotion>();
    private final Map<String, MenuMotion> rowAppear = new LinkedHashMap<String, MenuMotion>();
    private final Map<String, Float> animatedRowPositions = new LinkedHashMap<String, Float>();
    private final MenuMotion[] topHover = new MenuMotion[]{new MenuMotion(), new MenuMotion()};
    private final MenuMotion inputHover = new MenuMotion();
    private final ParallaxMenuBackground background = new ParallaxMenuBackground();
    private String input = "";
    private String selected;
    private String lastAccount;
    private boolean inputFocused = true;
    private float scroll;
    private float animatedScroll;
    private String pressedAccount;
    private double pressedAccountY;
    private double dragPointerY;
    private boolean draggingAccount;
    private long openedAt;
    private long closingAt;
    private long lastFrameNanos;
    private long invalidUntil;
    private boolean initialized;
    private boolean closing;
    private boolean animationStarted;

    AltManagerScreen(class_437 parent) {
        super((class_2561)class_2561.method_43470((String)"\u041c\u0435\u043d\u0435\u0434\u0436\u0435\u0440 \u0430\u043a\u043a\u0430\u0443\u043d\u0442\u043e\u0432"));
        this.parent = parent;
    }

    protected void method_25426() {
        if (!this.initialized) {
            AltAccountStore.Snapshot snapshot = AltAccountStore.load();
            this.accounts.addAll(snapshot.accounts());
            this.favorites.addAll(snapshot.favorites());
            this.generatedNicknames.addAll(snapshot.generated());
            this.lastAccount = snapshot.lastAccount();
            this.dates.putAll(snapshot.dates());
            this.selected = this.stored(mc.method_1548().method_1676());
            this.ensureDates();
            for (int index = 0; index < this.accounts.size(); ++index) {
                this.animatedRowPositions.put(this.accounts.get(index), Float.valueOf(index));
            }
            this.initialized = true;
        }
        this.clampScroll();
        this.animatedScroll = this.scroll;
        this.openedAt = 0L;
        this.lastFrameNanos = System.nanoTime();
        this.animationStarted = false;
        for (MenuMotion motion2 : this.topHover) {
            motion2.reset(0.0f);
        }
        this.inputHover.reset(0.0f);
        this.rowHover.values().forEach(motion -> motion.reset(0.0f));
        this.rowAppear.clear();
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        this.background.render(context, this.field_22789, this.field_22790, mouseX, mouseY);
    }

    @Override
    public void renderRecoinOverlay(class_332 context, int mouseX, int mouseY, float delta) {
        this.renderMenuContent(context, mouseX, mouseY, delta);
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
            this.updateAnimations(mouseX, mouseY, frameSeconds);
            float alpha = this.transitionAnimation();
            float s = this.scale();
            float x = this.panelX();
            float y = this.panelY();
            float oldAlpha = Render2DUtil.getRenderAlpha();
            Render2DUtil.setRenderAlpha(oldAlpha * alpha);
            context.method_51448().pushMatrix();
            class_4587 matrix = MatrixStackCompat.from(context.method_51448());
            try {
                this.drawGlass(matrix, x, y, 286.0f * s, 298.0f * s, 11.0f * s, -1710220734, 0);
                this.drawTop(context, matrix, x, y, mouseX, mouseY, s);
                this.drawAccounts(context, matrix, x, y, mouseX, mouseY, s);
                this.drawFooter(matrix, x, y, s);
                FontRenderer.flushQueuedText(context);
            }
            finally {
                context.method_51448().popMatrix();
                Render2DUtil.setRenderAlpha(oldAlpha);
            }
        }
        finally {
            blur.setLiveMode(false);
            Render2DUtil.setActiveContext(null);
            Render2DUtil.setQueuedShapeActive(false);
        }
    }

    private void drawTop(class_332 context, class_4587 matrix, float panelX, float panelY, int mouseX, int mouseY, float s) {
        float inputX = panelX + 12.0f * s;
        float inputY = panelY + 12.0f * s;
        float inputWidth = 202.0f * s;
        float inputHeight = 24.0f * s;
        boolean invalid = System.currentTimeMillis() < this.invalidUntil;
        boolean inputHovered = AltManagerScreen.inside(mouseX, mouseY, inputX, inputY, inputWidth, inputHeight);
        this.drawGlass(matrix, inputX, inputY, inputWidth, inputHeight, 6.0f * s, -1710220734, invalid ? -654352781 : (this.inputFocused || inputHovered ? -1704813569 : 0), this.inputHover.value());
        FontRenderer font = Fonts.getMenuSize(AltManagerScreen.scaled(12, s), Fonts.Type.INTER_BOLD);
        String value = this.input.isEmpty() ? "\u0412\u0432\u0435\u0434\u0438\u0442\u0435 \u043d\u0438\u043a..." : this.input;
        int color = this.input.isEmpty() ? -9866880 : -986119;
        font.drawString(matrix, value, inputX + 9.0f * s, AltManagerScreen.centeredTextY(font, value, inputY, inputHeight), color);
        if (this.inputFocused && !this.input.isEmpty() && System.currentTimeMillis() / 500L % 2L == 0L) {
            float caret = inputX + 9.0f * s + font.getStringWidth(this.input) + 0.5f * s;
            rectangle.render(ShapeProperties.create(matrix, caret, inputY + 6.0f * s, Math.max(0.65f, 0.7f * s), 12.0f * s).round(0.4f * s).color(-10314753).build());
        }
        float firstX = panelX + 220.0f * s;
        float secondX = panelX + 250.0f * s;
        this.drawTopButton(matrix, firstX, inputY, "\ue01a", this.topHover[0].value(), 1.0f * s, s);
        this.drawTopButton(matrix, secondX, inputY, "\ue06a", this.topHover[1].value(), 3.2f * s, s);
    }

    private void drawTopButton(class_4587 matrix, float x, float y, String icon, float hover, float iconOffsetX, float s) {
        float size = 24.0f * s;
        this.drawGlass(matrix, x, y, size, size, 6.0f * s, -1710220734, 0, hover);
        FontRenderer iconFont = Fonts.getMenuSize(AltManagerScreen.scaled(25, s), Fonts.Type.COMBINED_ICONS);
        iconFont.drawCenteredString(matrix, icon, x + size / 2.0f + iconOffsetX + -1.0f, AltManagerScreen.centeredTextY(iconFont, icon, y, size) + 0.3f * s, AltManagerScreen.mix(-9527313, -1, hover));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void drawAccounts(class_332 context, class_4587 matrix, float panelX, float panelY, int mouseX, int mouseY, float s) {
        float listX = panelX + 12.0f * s;
        float listY = panelY + 49.0f * s;
        float listWidth = 262.0f * s;
        float viewportHeight = AltManagerScreen.listViewportHeight() * s;
        int start = Math.max(0, (int)Math.floor(this.animatedScroll) - 2);
        int end = Math.min(this.accounts.size(), (int)Math.ceil(this.animatedScroll + 5.0f) + 2);
        ScissorManager scissor = Main.getInstance().getScissorManager();
        scissor.push(matrix.method_23760().method_23761(), listX, listY, listWidth, viewportHeight);
        FontRenderer nameFont = Fonts.getMenuSize(AltManagerScreen.scaled(18, s), Fonts.Type.INTER_BOLD);
        FontRenderer smallFont = Fonts.getMenuSize(AltManagerScreen.scaled(13, s), Fonts.Type.INTER_BOLD);
        FontRenderer starFont = Fonts.getMenuSize(AltManagerScreen.scaled(18, s), Fonts.Type.COMBINED_ICONS);
        FontRenderer trashFont = Fonts.getMenuSize(AltManagerScreen.scaled(18, s), Fonts.Type.COMBINED_ICONS);
        for (int index = start; index < end; ++index) {
            String account = this.accounts.get(index);
            float rowPosition = this.animatedRowPositions.getOrDefault(account, Float.valueOf(index)).floatValue();
            float rowY = listY + (rowPosition - this.animatedScroll) * 40.0f * s;
            if (this.draggingAccount && account.equalsIgnoreCase(this.pressedAccount)) {
                rowY = (float)Math.max((double)(listY - 35.0f * s * 0.35f), Math.min(this.dragPointerY - (double)(35.0f * s / 2.0f), (double)(listY + viewportHeight - 35.0f * s * 0.65f)));
            }
            float intro = this.rowAppear.computeIfAbsent(account, ignored -> new MenuMotion()).value();
            float rowX = listX;
            float rowAlpha = intro;
            float oldAlpha = Render2DUtil.getRenderAlpha();
            Render2DUtil.setRenderAlpha(oldAlpha * rowAlpha);
            try {
                float hover = this.rowHover.computeIfAbsent(account, ignored -> new MenuMotion()).value();
                boolean chosen = account.equalsIgnoreCase(this.selected == null ? "" : this.selected);
                this.drawGlass(matrix, rowX, rowY, listWidth, 35.0f * s, 5.5f * s, chosen ? -485352661 : -1710220734, chosen ? 2036174039 : 0, hover);
                float face = 22.0f * s;
                float faceX = rowX + 7.0f * s;
                float faceY = rowY + 6.5f * s;
                ItemGuiAlpha.render(rowAlpha, () -> Render2DUtil.drawRoundedTexture(context, class_1068.method_4648((UUID)AltManagerScreen.offlineUuid(account)).comp_1626().comp_3627(), faceX, faceY, face, 5.0f * s, 8.0f, 8.0f, 8, 8, 64, 64, -1));
                nameFont.drawString(matrix, account, rowX + 36.0f * s, AltManagerScreen.centeredTextY(nameFont, account, rowY, 35.0f * s), account.equalsIgnoreCase(mc.method_1548().method_1676()) ? -722948 : -2498840);
                boolean favorite = this.isFavorite(account);
                String star = "\ue01f";
                float starX = rowX + listWidth - 91.0f * s;
                starFont.drawCenteredString(matrix, star, starX, AltManagerScreen.centeredTextY(starFont, star, rowY, 35.0f * s), favorite ? -14001 : -10327432);
                float trashX = rowX + listWidth - 66.0f * s;
                boolean trashHovered = AltManagerScreen.inside(mouseX, mouseY, trashX - 5.0f * s, rowY, 21.0f * s, 35.0f * s);
                trashFont.drawCenteredString(matrix, TRASH_ICON, trashX, AltManagerScreen.centeredTextY(trashFont, TRASH_ICON, rowY, 35.0f * s), trashHovered ? -39561 : -9932418);
                String date = this.dateFor(account);
                smallFont.drawString(matrix, date, rowX + listWidth - 52.0f * s, AltManagerScreen.centeredTextY(smallFont, date, rowY, 35.0f * s), -10063745);
                continue;
            }
            finally {
                Render2DUtil.setRenderAlpha(oldAlpha);
            }
        }
        FontRenderer.flushQueuedText(context);
        scissor.pop();
    }

    private void drawFooter(class_4587 matrix, float panelX, float panelY, float s) {
        float footerY = panelY + 255.0f * s;
        FontRenderer textFont = Fonts.getMenuSize(AltManagerScreen.scaled(14, s), Fonts.Type.INTER_BOLD);
        String current = "\u0422\u0435\u043a\u0443\u0449\u0438\u0439 \u0430\u043a\u043a\u0430\u0443\u043d\u0442: " + mc.method_1548().method_1676();
        textFont.drawCenteredString(matrix, current, panelX + 286.0f * s / 2.0f, footerY + 2.0f * s, -1);
        String count = this.accounts.size() + " \u0430\u043a\u043a\u0430\u0443\u043d\u0442\u043e\u0432";
        textFont.drawCenteredString(matrix, count, panelX + 286.0f * s / 2.0f, footerY + 12.0f * s, -7695196);
    }

    private void drawGlass(class_4587 matrix, float x, float y, float width, float height, float round, int fill, int outline) {
        this.drawGlass(matrix, x, y, width, height, round, fill, outline, 0.0f);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void drawGlass(class_4587 matrix, float x, float y, float width, float height, float round, int fill, int outline, float hover) {
        boolean oldHud = Render2DUtil.isHudDecorationActive();
        boolean oldGui = Render2DUtil.isGuiDecorationActive();
        Render2DUtil.setHudDecorationActive(true);
        Render2DUtil.setGuiDecorationActive(true);
        try {
            blur.setLiveMode(true);
            blur.setShadowEnabled(false);
            blur.setColorlessMode(true);
            blur.setStyledBlurRadius(28.0f);
            blur.render(ShapeProperties.create(matrix, x, y, width, height).round(round).softness(5.0f).quality(50.0f).thickness(0.65f).outlineColor(outline).color(ColorUtil.multAlpha(fill, 1.0f - hover * 0.18f)).build());
        }
        finally {
            blur.resetStyledBlurRadius();
            blur.setColorlessMode(false);
            blur.setShadowEnabled(true);
            blur.setLiveMode(false);
            Render2DUtil.setHudDecorationActive(oldHud);
            Render2DUtil.setGuiDecorationActive(oldGui);
        }
        this.drawImmediateGlassLayer(matrix, x, y, width, height, round, 0.5f, ColorUtil.multAlpha(1375797768, 1.0f - hover * 0.34f));
        this.drawImmediateGlassLayer(matrix, x, y, width, height, round, -0.1f, ColorUtil.getOutline(0.8f - hover * 0.16f));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void drawImmediateGlassLayer(class_4587 matrix, float x, float y, float width, float height, float round, float softness, int color) {
        boolean queued = Render2DUtil.isQueuedShapeActive();
        Render2DUtil.setQueuedShapeActive(false);
        try {
            rectangle.render(ShapeProperties.create(matrix, x, y, width, height).round(round).softness(softness).thickness(0.0f).quality(50.0f).outlineColor(0).color(color).build());
        }
        finally {
            Render2DUtil.setQueuedShapeActive(queued);
        }
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        if (this.closing || click.comp_4800().comp_4801() != 0 || this.transitionAnimation() < 0.8f) {
            return true;
        }
        this.pressedAccount = null;
        this.draggingAccount = false;
        float s = this.scale();
        float x = this.panelX();
        float y = this.panelY();
        double mx = click.comp_4798();
        double my = click.comp_4799();
        this.inputFocused = AltManagerScreen.inside(mx, my, x + 12.0f * s, y + 12.0f * s, 202.0f * s, 24.0f * s);
        if (AltManagerScreen.inside(mx, my, x + 220.0f * s, y + 12.0f * s, 24.0f * s, 24.0f * s)) {
            this.addRandomAccount();
            return true;
        }
        if (AltManagerScreen.inside(mx, my, x + 250.0f * s, y + 12.0f * s, 24.0f * s, 24.0f * s)) {
            this.saveInput();
            return true;
        }
        float listX = x + 12.0f * s;
        float listY = y + 49.0f * s;
        float listWidth = 262.0f * s;
        int start = Math.max(0, (int)Math.floor(this.animatedScroll) - 2);
        int end = Math.min(this.accounts.size(), (int)Math.ceil(this.animatedScroll + 5.0f) + 2);
        for (int index = start; index < end; ++index) {
            String account = this.accounts.get(index);
            float rowPosition = this.animatedRowPositions.getOrDefault(account, Float.valueOf(index)).floatValue();
            float rowY = listY + (rowPosition - this.animatedScroll) * 40.0f * s;
            if (!AltManagerScreen.inside(mx, my, listX, listY, listWidth, AltManagerScreen.listViewportHeight() * s) || !AltManagerScreen.inside(mx, my, listX, rowY, listWidth, 35.0f * s)) continue;
            float starX = listX + listWidth - 99.0f * s;
            float trashX = listX + listWidth - 76.0f * s;
            if (AltManagerScreen.inside(mx, my, starX, rowY, 20.0f * s, 35.0f * s)) {
                this.toggleFavorite(account);
            } else if (AltManagerScreen.inside(mx, my, trashX, rowY, 20.0f * s, 35.0f * s)) {
                this.delete(account);
            } else {
                this.selected = account;
                this.pressedAccount = account;
                this.pressedAccountY = my;
                this.dragPointerY = my;
            }
            return true;
        }
        return true;
    }

    public boolean method_25403(class_11909 click, double offsetX, double offsetY) {
        double mouseY;
        if (this.closing || click.comp_4800().comp_4801() != 0 || this.pressedAccount == null) {
            return false;
        }
        this.dragPointerY = mouseY = click.comp_4799();
        if (!this.draggingAccount && Math.abs(mouseY - this.pressedAccountY) >= 3.0) {
            this.draggingAccount = true;
        }
        if (!this.draggingAccount) {
            return true;
        }
        float s = this.scale();
        float listY = this.panelY() + 49.0f * s;
        float viewportHeight = AltManagerScreen.listViewportHeight() * s;
        float max = Math.max(0.0f, (float)(this.accounts.size() - 5));
        if (mouseY < (double)(listY + 14.0f * s)) {
            this.scroll = Math.max(0.0f, this.scroll - 0.18f);
        }
        if (mouseY > (double)(listY + viewportHeight - 14.0f * s)) {
            this.scroll = Math.min(max, this.scroll + 0.18f);
        }
        int current = this.indexOfAccount(this.pressedAccount);
        int target = Math.max(0, Math.min(this.accounts.size() - 1, (int)Math.floor((mouseY - (double)listY) / (double)(40.0f * s) + (double)this.animatedScroll)));
        if (current >= 0 && target != current) {
            String account = this.accounts.remove(current);
            this.accounts.add(target, account);
        }
        return true;
    }

    public boolean method_25406(class_11909 click) {
        if (this.pressedAccount != null) {
            String account = this.pressedAccount;
            boolean wasDragging = this.draggingAccount;
            if (wasDragging) {
                float s = this.scale();
                float listY = this.panelY() + 49.0f * s;
                float position = (float)((this.dragPointerY - (double)(35.0f * s / 2.0f) - (double)listY) / (double)(40.0f * s) + (double)this.animatedScroll);
                this.animatedRowPositions.put(account, Float.valueOf(position));
            }
            this.pressedAccount = null;
            this.draggingAccount = false;
            if (wasDragging) {
                this.saveAccounts();
            } else {
                this.switchAccount(account);
            }
            return true;
        }
        return super.method_25406(click);
    }

    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.closing) {
            return true;
        }
        float s = this.scale();
        if (!AltManagerScreen.inside(mouseX, mouseY, this.panelX() + 12.0f * s, this.panelY() + 49.0f * s, 262.0f * s, AltManagerScreen.listViewportHeight() * s)) {
            return false;
        }
        float max = Math.max(0.0f, (float)(this.accounts.size() - 5));
        this.scroll = Math.max(0.0f, Math.min(max, this.scroll - (float)verticalAmount * 0.68f));
        return true;
    }

    public boolean method_25404(class_11908 key) {
        if (this.closing) {
            return true;
        }
        if (key.comp_4795() == 256) {
            this.method_25419();
            return true;
        }
        if (!this.inputFocused) {
            return true;
        }
        if (key.comp_4795() == 259 && !this.input.isEmpty()) {
            this.input = this.input.substring(0, this.input.length() - 1);
            return true;
        }
        if (key.comp_4795() == 86 && (key.comp_4797() & 2) != 0) {
            this.appendInput(AltManagerScreen.mc.field_1774.method_1460());
            return true;
        }
        if (key.comp_4795() == 257 || key.comp_4795() == 335) {
            this.saveInput();
            return true;
        }
        return true;
    }

    public boolean method_25400(class_11905 chars) {
        if (this.closing || !this.inputFocused) {
            return true;
        }
        this.appendInput(chars.method_74226());
        return true;
    }

    private void addRandomAccount() {
        String generated = RandomNicknamePool.next(this.generatedNicknames, this.accounts);
        if (generated == null) {
            this.invalidUntil = System.currentTimeMillis() + 1200L;
            return;
        }
        this.generatedNicknames.add(generated);
        this.input = generated;
        this.inputFocused = true;
        this.saveInput();
    }

    private void appendInput(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        for (char raw : text.toCharArray()) {
            char character = AltManagerScreen.keyboardLayoutCharacter(raw);
            if (!AltManagerScreen.validCharacter(character) || this.input.length() >= 16) continue;
            this.input = this.input + character;
        }
    }

    private static char keyboardLayoutCharacter(char character) {
        String russian = "\u0439\u0446\u0443\u043a\u0435\u043d\u0433\u0448\u0449\u0437\u0444\u044b\u0432\u0430\u043f\u0440\u043e\u043b\u0434\u044f\u0447\u0441\u043c\u0438\u0442\u044c";
        String english = "qwertyuiopasdfghjklzxcvbnm";
        boolean upper = Character.isUpperCase(character);
        int index = russian.indexOf(Character.toLowerCase(character));
        if (index < 0) {
            return character;
        }
        char mapped = english.charAt(index);
        return upper ? Character.toUpperCase(mapped) : mapped;
    }

    private void saveInput() {
        String name = this.input.trim();
        if (!AltManagerScreen.valid(name)) {
            this.invalidUntil = System.currentTimeMillis() + 1200L;
            return;
        }
        String existing = this.stored(name);
        if (existing == null) {
            this.accounts.add(name);
            this.selected = name;
            this.dates.put(name, LocalDate.now().format(DATE_FORMAT));
            this.animatedRowPositions.put(name, Float.valueOf(this.accounts.size() - 1));
            this.scroll = Math.max(0, this.accounts.size() - 5);
            this.saveAccounts();
        } else {
            this.selected = existing;
        }
        this.input = "";
    }

    private void delete(String name) {
        this.accounts.removeIf(account -> account.equalsIgnoreCase(name));
        this.favorites.removeIf(account -> account.equalsIgnoreCase(name));
        this.dates.keySet().removeIf(account -> account.equalsIgnoreCase(name));
        this.rowHover.keySet().removeIf(account -> account.equalsIgnoreCase(name));
        this.animatedRowPositions.keySet().removeIf(account -> account.equalsIgnoreCase(name));
        if (name.equalsIgnoreCase(this.selected == null ? "" : this.selected)) {
            this.selected = this.stored(mc.method_1548().method_1676());
        }
        this.clampScroll();
        this.saveAccounts();
    }

    private void toggleFavorite(String name) {
        if (this.isFavorite(name)) {
            this.favorites.removeIf(account -> account.equalsIgnoreCase(name));
        } else {
            this.favorites.add(name);
            int index = this.indexOfAccount(name);
            if (index > 0) {
                String account2 = this.accounts.remove(index);
                this.accounts.add(0, account2);
            }
            this.scroll = 0.0f;
            this.selected = name;
        }
        this.saveAccounts();
    }

    private boolean isFavorite(String name) {
        return this.favorites.stream().anyMatch(account -> account.equalsIgnoreCase(name));
    }

    private void switchAccount(String name) {
        ((MinecraftClientSessionAccessor)mc).recoin$setSession(new class_320(name, AltManagerScreen.offlineUuid(name), "", Optional.empty(), Optional.empty()));
        this.selected = name;
        this.lastAccount = name;
        this.saveAccounts();
    }

    private void saveAccounts() {
        AltAccountStore.save(new AltAccountStore.Snapshot(this.accounts, this.favorites, this.dates, this.generatedNicknames, this.lastAccount));
    }

    private void ensureDates() {
        String today = LocalDate.now().format(DATE_FORMAT);
        for (String account : this.accounts) {
            if (this.dateForExisting(account) != null) continue;
            this.dates.put(account, today);
        }
        this.saveAccounts();
    }

    private String dateFor(String account) {
        String date = this.dateForExisting(account);
        return date == null ? LocalDate.now().format(DATE_FORMAT) : date;
    }

    private String dateForExisting(String account) {
        return this.dates.entrySet().stream().filter(entry -> ((String)entry.getKey()).equalsIgnoreCase(account)).map(Map.Entry::getValue).findFirst().orElse(null);
    }

    private String stored(String name) {
        if (name == null) {
            return null;
        }
        return this.accounts.stream().filter(account -> account.equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    private int indexOfAccount(String name) {
        if (name == null) {
            return -1;
        }
        for (int index = 0; index < this.accounts.size(); ++index) {
            if (!this.accounts.get(index).equalsIgnoreCase(name)) continue;
            return index;
        }
        return -1;
    }

    private void updateAnimations(int mouseX, int mouseY, float seconds) {
        this.animatedScroll = MenuMotion.interpolate(this.animatedScroll, this.scroll, 2.0f, seconds);
        if (Math.abs(this.animatedScroll - this.scroll) < 0.001f) {
            this.animatedScroll = this.scroll;
        }
        float s = this.scale();
        float x = this.panelX();
        float y = this.panelY();
        this.topHover[0].update(AltManagerScreen.inside(mouseX, mouseY, x + 220.0f * s, y + 12.0f * s, 24.0f * s, 24.0f * s) ? 1.0f : 0.0f, 4.0f, seconds);
        this.topHover[1].update(AltManagerScreen.inside(mouseX, mouseY, x + 250.0f * s, y + 12.0f * s, 24.0f * s, 24.0f * s) ? 1.0f : 0.0f, 4.0f, seconds);
        this.inputHover.update(AltManagerScreen.inside(mouseX, mouseY, x + 12.0f * s, y + 12.0f * s, 202.0f * s, 24.0f * s) ? 1.0f : 0.0f, 4.0f, seconds);
        float listX = x + 12.0f * s;
        float listY = y + 49.0f * s;
        float listWidth = 262.0f * s;
        for (int index = 0; index < this.accounts.size(); ++index) {
            String account = this.accounts.get(index);
            float position = this.animatedRowPositions.getOrDefault(account, Float.valueOf(index)).floatValue();
            if (!this.draggingAccount || !account.equalsIgnoreCase(this.pressedAccount)) {
                if (Math.abs((position = MenuMotion.interpolate(position, index, 1.5f, seconds)) - (float)index) < 0.001f) {
                    position = index;
                }
                this.animatedRowPositions.put(account, Float.valueOf(position));
            }
            float rowY = listY + (position - this.animatedScroll) * 40.0f * s;
            boolean hovered = AltManagerScreen.inside(mouseX, mouseY, listX, listY, listWidth, AltManagerScreen.listViewportHeight() * s) && AltManagerScreen.inside(mouseX, mouseY, listX, rowY, listWidth, 35.0f * s);
            this.rowHover.computeIfAbsent(account, ignored -> new MenuMotion()).update(hovered ? 1.0f : 0.0f, 4.0f, seconds);
            this.rowAppear.computeIfAbsent(account, ignored -> new MenuMotion()).update(1.0f, 0.75f, seconds);
        }
    }

    private float frameSeconds() {
        long now = System.nanoTime();
        float seconds = Math.min(0.05f, Math.max(0.0f, (float)(now - this.lastFrameNanos) / 1.0E9f));
        this.lastFrameNanos = now;
        return seconds;
    }

    private void beginAnimationFrame() {
        if (this.animationStarted) {
            return;
        }
        this.lastFrameNanos = this.openedAt = System.nanoTime();
        this.animationStarted = true;
    }

    private float transitionAnimation() {
        if (!this.animationStarted) {
            return 0.0f;
        }
        long elapsed = System.nanoTime() - (this.closing ? this.closingAt : this.openedAt);
        float duration = (float)(this.closing ? 130L : 160L) * 1000000.0f;
        float t = Math.max(0.0f, Math.min(1.0f, (float)elapsed / duration));
        float fade = t * t * (3.0f - 2.0f * t);
        return this.closing ? 1.0f - fade : fade;
    }

    private void clampScroll() {
        float max = Math.max(0.0f, (float)(this.accounts.size() - 5));
        this.scroll = Math.max(0.0f, Math.min(this.scroll, max));
        this.animatedScroll = Math.max(0.0f, Math.min(this.animatedScroll, max));
    }

    private static float listViewportHeight() {
        return 195.0f;
    }

    private float scale() {
        return Math.max(0.72f, Math.min(1.0f, Math.min((float)this.field_22789 / 520.0f, (float)this.field_22790 / 390.0f)));
    }

    private float panelX() {
        return ((float)this.field_22789 - 286.0f * this.scale()) / 2.0f;
    }

    private float panelY() {
        return ((float)this.field_22790 - 298.0f * this.scale()) / 2.0f;
    }

    private static int scaled(int size, float scale) {
        return Math.max(6, Math.round((float)size * scale));
    }

    private static float centeredTextY(FontRenderer font, String text, float y, float height) {
        return y + (height - font.getStringHeight(text)) / 2.0f;
    }

    private static UUID offlineUuid(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }

    private static boolean valid(String name) {
        return name != null && name.matches("[A-Za-z0-9_]{3,16}");
    }

    private static boolean validCharacter(char character) {
        return character >= 'a' && character <= 'z' || character >= 'A' && character <= 'Z' || character >= '0' && character <= '9' || character == '_';
    }

    private static boolean inside(double mx, double my, float x, float y, float w, float h) {
        return mx >= (double)x && mx <= (double)(x + w) && my >= (double)y && my <= (double)(y + h);
    }

    private static int mix(int from, int to, float value) {
        value = Math.max(0.0f, Math.min(1.0f, value));
        int a = Math.round((float)(from >>> 24 & 0xFF) + (float)((to >>> 24 & 0xFF) - (from >>> 24 & 0xFF)) * value);
        int r = Math.round((float)(from >>> 16 & 0xFF) + (float)((to >>> 16 & 0xFF) - (from >>> 16 & 0xFF)) * value);
        int g = Math.round((float)(from >>> 8 & 0xFF) + (float)((to >>> 8 & 0xFF) - (from >>> 8 & 0xFF)) * value);
        int b = Math.round((float)(from & 0xFF) + (float)((to & 0xFF) - (from & 0xFF)) * value);
        return a << 24 | r << 16 | g << 8 | b;
    }

    public void method_25419() {
        if (this.closing) {
            return;
        }
        this.closing = true;
        this.closingAt = System.nanoTime();
    }

    public void method_25393() {
        if (this.closing && System.nanoTime() - this.closingAt >= 130000000L) {
            mc.method_1507(this.parent);
            return;
        }
        super.method_25393();
    }
}

