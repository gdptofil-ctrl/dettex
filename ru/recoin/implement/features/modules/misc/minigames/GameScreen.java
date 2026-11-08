package ru.recoin.implement.features.modules.misc.minigames;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1109;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3414;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.implement.features.modules.misc.Shrift;

/** Shared bits of the mini game screens: panels, buttons that are hit-tested each frame, sounds. */
abstract class GameScreen extends class_437 {
    protected static final int BG = 0xE0141821;
    protected static final int CARD = 0xFF1E2430;
    protected static final int CARD_HOVER = 0xFF283041;
    protected static final int TEXT = 0xFFFFFFFF;
    protected static final int MUTED = 0xFF8A93A6;
    protected static final int GREEN = 0xFF4CD964;
    protected static final int RED = 0xFFFF4D5E;
    protected static final int GOLD = 0xFFFFC23D;
    protected static final int BLUE = 0xFF4B8BFF;

    private record Button(int x, int y, int w, int h, Runnable action) {
    }

    private final List<Button> buttons = new ArrayList<>();
    protected int mouseX;
    protected int mouseY;

    protected GameScreen(String title) {
        super(class_2561.method_43470(title));
    }

    /** Body text size and title size of the client fonts (Fonts.getSize units). */
    protected static final int BODY = 18;
    protected static final int TITLE = 24;
    private int textSize = BODY;
    private class_4587 matrix;

    @Override
    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        super.method_25394(context, mouseX, mouseY, delta);
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.buttons.clear();
        this.matrix = MatrixStackCompat.from(context.method_51448());
        Render2DUtil.setActiveContext(context);
        try {
            this.draw(context, delta);
            FontRenderer.flushQueuedText(context);   // client fonts are queued and drawn on top
        } finally {
            Render2DUtil.setActiveContext(null);
        }
    }

    /** Text drawn by the next text()/centered()/width() calls uses this size. */
    protected void size(int size) {
        this.textSize = size;
    }

    private FontRenderer main() {
        return Fonts.getSize(this.textSize, Shrift.miniGameFont());
    }

    private FontRenderer symbols() {
        return Fonts.getSize(this.textSize, Fonts.Type.INTER_BOLD);
    }

    /** Hearts, stars, coins and other symbols are not in the custom fonts: they are drawn with Inter. */
    private static boolean isSymbol(int cp) {
        return cp >= 0x2500;
    }

    private float drawRuns(String s, float x, float y, int color, boolean draw) {
        float cx = x;
        int i = 0;
        while (i < s.length()) {
            boolean sym = GameScreen.isSymbol(s.codePointAt(i));
            int j = i;
            while (j < s.length() && GameScreen.isSymbol(s.codePointAt(j)) == sym) {
                j += Character.charCount(s.codePointAt(j));
            }
            String run = s.substring(i, j);
            FontRenderer fr = sym ? this.symbols() : this.main();
            if (draw) {
                fr.drawString(this.matrix, run, cx + 0.7f, y + 0.7f, 0xA0000000 & color | 0x000000);   // soft shadow
                fr.drawString(this.matrix, run, cx, y, color);
            }
            cx += fr.getStringWidth(run);
            i = j;
        }
        return cx - x;
    }

    /** Splits text into lines not wider than maxWidth. */
    protected List<String> wrap(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (line.length() > 0 && this.width(test) > maxWidth) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(test);
            }
        }
        if (line.length() > 0) lines.add(line.toString());
        return lines;
    }

    /** Height of a text line at the current size. */
    protected int lineHeight() {
        return Math.round(this.main().getStringHeight("Ag")) + 3;
    }

    protected abstract void draw(class_332 context, float delta);

    protected boolean hovered(int x, int y, int w, int h) {
        return this.mouseX >= x && this.mouseX < x + w && this.mouseY >= y && this.mouseY < y + h;
    }

    protected void panel(class_332 c, int x, int y, int w, int h, int color) {
        c.method_25294(x + 1, y, x + w - 1, y + h, color);
        c.method_25294(x, y + 1, x + w, y + h - 1, color);
    }

    protected void frame(class_332 c, int x, int y, int w, int h, int color) {
        c.method_73198(x, y, w, h, color);
    }

    /** A clickable card; returns true while hovered. */
    protected boolean button(class_332 c, int x, int y, int w, int h, String label, int color, int textColor, boolean enabled, Runnable action) {
        boolean hover = enabled && this.hovered(x, y, w, h);
        this.panel(c, x, y, w, h, hover ? GameScreen.lighten(color) : color);
        if (!enabled) {
            c.method_25294(x, y, x + w, y + h, 0x80000000);
        }
        this.centered(c, label, x + w / 2, y + (h - Math.round(this.main().getStringHeight("Ag"))) / 2, enabled ? textColor : MUTED);
        if (enabled && action != null) {
            this.buttons.add(new Button(x, y, w, h, action));
        }
        return hover;
    }

    protected void hitArea(int x, int y, int w, int h, Runnable action) {
        this.buttons.add(new Button(x, y, w, h, action));
    }

    protected void text(class_332 c, String s, int x, int y, int color) {
        this.drawRuns(s, x, y, color, true);
    }

    protected void centered(class_332 c, String s, int cx, int y, int color) {
        this.drawRuns(s, cx - this.drawRuns(s, 0, 0, color, false) / 2.0f, y, color, true);
    }

    protected int width(String s) {
        return Math.round(this.drawRuns(s, 0, 0, 0, false));
    }

    protected static int lighten(int color) {
        int a = color >>> 24;
        int r = Math.min(255, (color >> 16 & 0xFF) + 22);
        int g = Math.min(255, (color >> 8 & 0xFF) + 22);
        int b = Math.min(255, (color & 0xFF) + 22);
        return a << 24 | r << 16 | g << 8 | b;
    }

    protected static void sound(String id, float pitch) {
        try {
            class_3414 event = class_3414.method_47908(class_2960.method_60654(id));
            class_310.method_1551().method_1483().method_4873(class_1109.method_4757(event, pitch, 0.6f));
        } catch (Throwable ignored) {
        }
    }

    @Override
    public boolean method_25402(class_11909 click, boolean doubled) {
        if (click.comp_4800().comp_4801() != 0) {
            return true;
        }
        double mx = click.comp_4798();
        double my = click.comp_4799();
        for (int i = this.buttons.size() - 1; i >= 0; --i) {
            Button b = this.buttons.get(i);
            if (mx >= b.x && mx < b.x + b.w && my >= b.y && my < b.y + b.h) {
                GameScreen.sound("minecraft:ui.button.click", 1.0f);
                b.action.run();
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean method_25404(class_11908 key) {
        if (this.keyPressed(key.comp_4795())) {
            return true;
        }
        return super.method_25404(key);
    }

    /** Key code (GLFW); return true if handled. */
    protected boolean keyPressed(int key) {
        return false;
    }

    @Override
    public boolean method_25421() {
        return false;   // never pause the game (multiplayer keeps running anyway)
    }
}
