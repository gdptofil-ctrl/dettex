package ru.recoin.implement.features.draggables;

import java.util.List;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import ru.recoin.api.feature.draggable.AbstractDraggable;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.implement.features.modules.render.Hud;
import ru.recoin.implement.features.modules.render.MusicHud;
import ru.recoin.implement.features.modules.render.music.LyricsService;

/**
 * On-screen lyrics of the song playing on the PC, word by word in time with the music.
 * Movable like any HUD element (open chat and drag it).
 */
public class Lyrics
extends AbstractDraggable {
    private static final long LINE_SWITCH_MS = 320L;
    private static final long WORD_POP_MS = 220L;
    private int shownIndex = -2;
    private int previousIndex = -1;
    private long switchedAt;
    private boolean placed;

    public Lyrics() {
        super("Lyrics", 0, 0, 160, 24, true);
    }

    @Override
    public boolean canDraw(Hud hud, AbstractDraggable draggable) {
        MusicHud music = MusicHud.getInstance();
        return music != null && music.lyricsVisible() && this.visible();
    }

    @Override
    public boolean visible() {
        if (PlayerIntersectionUtil.isChat(mc.field_1755)) {
            return true;   // always visible in chat so it can be moved
        }
        return MusicHud.musicPlaying();
    }

    @Override
    public void drawDraggable(class_332 context) {
        MusicHud music = MusicHud.getInstance();
        if (music == null) {
            return;
        }
        if (!this.placed) {
            this.placed = true;
            if (this.getX() == 0 && this.getY() == 0) {
                this.setX((window.getScaledWidth() - this.getWidth()) / 2);
                this.setY((int)(window.getScaledHeight() * 0.70f));
            }
        }
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        int fontSize = Math.round(music.size.getValue());
        FontRenderer font = Fonts.getSize(fontSize, Fonts.Type.INTER_BOLD);
        FontRenderer small = Fonts.getSize(Math.max(8, Math.round(fontSize * 0.68f)), Fonts.Type.INTER_BOLD);
        float lineH = font.getStringHeight("Ag");
        float smallH = small.getStringHeight("Ag");
        Hud hud = Hud.getInstance();
        int accent = hud != null ? hud.themeColor() : 0xFF55AAFF;
        if (accent == 0) {
            accent = 0xFF55AAFF;
        }

        List<LyricsService.Line> lines = LyricsService.lines();
        long pos = music.lyricsPositionMs();
        boolean hasLyrics = !lines.isEmpty() && LyricsService.state() == LyricsService.State.FOUND;
        int index;
        if (hasLyrics) {
            index = lines.isEmpty() ? -1 : LyricsService.indexAt(lines, pos);
        } else {
            index = -1;
        }
        long now = System.currentTimeMillis();
        if (index != this.shownIndex) {
            this.previousIndex = this.shownIndex;
            this.shownIndex = index;
            this.switchedAt = now;
        }
        float t = Math.min(1.0f, (float)(now - this.switchedAt) / (float)LINE_SWITCH_MS);
        float ease = 1.0f - (1.0f - t) * (1.0f - t) * (1.0f - t);

        String current;
        long start;
        long end;
        if (hasLyrics && index >= 0) {
            LyricsService.Line line = lines.get(index);
            current = line.text().isEmpty() ? "\u266a \u266a \u266a" : line.text();
            start = line.timeMs();
            end = index + 1 < lines.size() ? lines.get(index + 1).timeMs() : start + 5000L;
        } else if (hasLyrics && !lines.isEmpty()) {
            current = "\u266a \u266a \u266a";
            start = 0L;
            end = lines.get(0).timeMs();
        } else if (LyricsService.state() == LyricsService.State.LOADING) {
            current = "\u266a searching lyrics...";
            start = 0L;
            end = 1L;
        } else {
            // no lyrics for this track: never the title / artist, only a hint where to add your own
            current = !MusicHud.musicPlaying() ? "\u266a Lyrics (no music playing)" : "\u266a no lyrics online: put a .lrc/.txt into config/dettex/lyrics";
            start = 0L;
            end = 1L;
        }
        current = Lyrics.safe(font, current);
        String next = music.nextLine.isValue() && index + 1 < lines.size() && index + 1 >= 0 ? Lyrics.safe(small, lines.get(index + 1).text()) : "";

        float curW = font.getStringWidth(current);
        float nextW = next.isEmpty() ? 0.0f : small.getStringWidth(next);
        float pad = 8.0f;
        float boxW = Math.max(curW, nextW) + pad * 2.0f;
        float boxH = lineH + (next.isEmpty() ? 0.0f : smallH + 4.0f) + pad * 1.5f;
        // keep the element centred on the same point while its width follows the line length
        float centerX = this.getX() + this.getWidth() / 2.0f;
        int newW = (int)Math.ceil(Math.max(60.0f, boxW));
        if (newW != this.getWidth()) {
            this.setWidth(newW);
            this.setX(Math.round(centerX - newW / 2.0f));
        }
        this.setHeight((int)Math.ceil(boxH));
        float x = this.getX();
        float y = this.getY();
        float cx = x + this.getWidth() / 2.0f;

        if (music.background.isValue()) {
            blur.render(ShapeProperties.create(matrix, x, y, this.getWidth(), boxH).round(7.0f).softness(4.0f).thickness(0.0f).quality(50.0f).outlineColor(0).color(ColorUtil.getColor(10, 12, 18, 0.55f)).build());
        }
        float textY = y + pad * 0.75f;

        // previous line slides up and fades out
        if (t < 1.0f && this.previousIndex >= 0 && this.previousIndex < lines.size()) {
            String prev = Lyrics.safe(font, lines.get(this.previousIndex).text());
            float a = 1.0f - ease;
            font.drawCenteredString(matrix, prev, cx, textY - lineH * 0.9f * ease, ColorUtil.multAlpha(ColorUtil.getText(), a * 0.8f));
        }
        float lineAlpha = ease;
        float slide = (1.0f - ease) * lineH * 0.6f;
        String mode = music.animation.getSelected();
        float left = cx - curW / 2.0f;
        switch (mode) {
            case "Fade": {
                font.drawString(matrix, current, left, textY + slide, ColorUtil.multAlpha(ColorUtil.getText(), lineAlpha));
                break;
            }
            case "Typewriter": {
                float progress = Lyrics.progress(pos, start, end);
                int chars = Math.max(0, Math.min(current.length(), Math.round(current.length() * progress)));
                String typed = current.substring(0, chars);
                font.drawString(matrix, typed, left, textY, ColorUtil.getText());
                if (chars < current.length() && (now / 400L) % 2L == 0L) {
                    font.drawString(matrix, "|", left + font.getStringWidth(typed), textY, accent);
                }
                break;
            }
            case "Karaoke": {
                String[] words = current.split(" ");
                float wx = left;
                float space = font.getStringWidth(" ");
                float[] timing = LyricsService.wordTiming(words, start, end);
                for (int i = 0; i < words.length; ++i) {
                    float fill = Lyrics.clamp((pos - timing[i * 2]) / Math.max(1.0f, timing[i * 2 + 1]));
                    int color = Lyrics.mix(ColorUtil.multAlpha(ColorUtil.getText(), 0.45f), accent, fill);
                    font.drawString(matrix, words[i], wx, textY + slide, ColorUtil.multAlpha(color, lineAlpha));
                    wx += font.getStringWidth(words[i]) + space;
                }
                break;
            }
            default: {   // Pop: each word jumps in when it is sung
                String[] words = current.split(" ");
                float wx = left;
                float space = font.getStringWidth(" ");
                float[] timing = LyricsService.wordTiming(words, start, end);
                for (int i = 0; i < words.length; ++i) {
                    float ww = font.getStringWidth(words[i]);
                    float a = Lyrics.clamp((pos - timing[i * 2]) / (float)WORD_POP_MS);
                    if (a > 0.0f) {
                        float pop = 1.0f - (1.0f - a) * (1.0f - a);
                        float scale = 1.0f + 0.35f * (1.0f - pop);
                        float wy = textY + (1.0f - pop) * lineH * 0.5f;
                        boolean singing = pos < timing[i * 2] + timing[i * 2 + 1];
                        int color = singing ? Lyrics.mix(accent, ColorUtil.getText(), pop * 0.4f) : ColorUtil.getText();
                        context.method_51448().pushMatrix();
                        context.method_51448().translate(wx + ww / 2.0f, wy + lineH / 2.0f);
                        context.method_51448().scale(scale, scale);
                        context.method_51448().translate(-(wx + ww / 2.0f), -(wy + lineH / 2.0f));
                        font.drawString(matrix, words[i], wx, wy, ColorUtil.multAlpha(color, pop));
                        context.method_51448().popMatrix();
                    }
                    wx += ww + space;
                }
                break;
            }
        }
        if (!next.isEmpty()) {
            small.drawCenteredString(matrix, next, cx, textY + lineH + 4.0f, ColorUtil.multAlpha(ColorUtil.getText(), 0.45f * lineAlpha));
        }
    }

    private static float progress(long pos, long start, long end) {
        float span = Math.max(400.0f, Math.min(end - start, 12000L) * 0.8f);
        return Lyrics.clamp((pos - start) / span);
    }

    private static float clamp(float v) {
        return v < 0.0f ? 0.0f : Math.min(1.0f, v);
    }

    private static int mix(int a, int b, float t) {
        int aa = a >>> 24, ar = a >> 16 & 0xFF, ag = a >> 8 & 0xFF, ab = a & 0xFF;
        int ba = b >>> 24, br = b >> 16 & 0xFF, bg = b >> 8 & 0xFF, bb = b & 0xFF;
        return Math.round(aa + (ba - aa) * t) << 24 | Math.round(ar + (br - ar) * t) << 16 | Math.round(ag + (bg - ag) * t) << 8 | Math.round(ab + (bb - ab) * t);
    }

    /** Replaces the note sign when the font has no glyph for it. */
    private static String safe(FontRenderer font, String text) {
        return font.hasGlyph(0x266A) ? text : text.replace('♪', '~');
    }
}
