package ru.recoin.implement.features.modules.render.music;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_746;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.ProjectionUtil;
import ru.recoin.common.util.render.MatrixStackCompat;

/**
 * Lyrics lines in the world, in time with the song: every new line appears in the air in front of the
 * player (alternating a little left / centre / right), its words light up one by one as they are sung,
 * the sung word jumps and the whole line pulses on every word (the beat of the singing). When the next
 * line starts, the old one floats up and fades. Lines keep their place in the world.
 */
public final class WorldLyricLines {
    private static final long APPEAR_MS = 260L;
    private static final long CLOSE_MS = 1400L;
    private static final int MAX_LINES = 3;
    private static final double[][] SLOTS = {{0.0, 0.0}, {-0.9, 0.35}, {0.9, 0.15}};   // sideways, up (blocks, scaled by distance)
    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static int lineIndex = -2;
    private static int slot;
    private static String fallbackTrack = "";

    private static final class Entry {
        final String[] words;
        final float[] timing;   // [start, duration] per word, in song milliseconds
        final double x, y, z;
        final long born;
        long closeAt;

        Entry(String[] words, float[] timing, double x, double y, double z, long born) {
            this.words = words;
            this.timing = timing;
            this.x = x;
            this.y = y;
            this.z = z;
            this.born = born;
        }
    }

    private WorldLyricLines() {
    }

    public static void clear() {
        ENTRIES.clear();
        lineIndex = -2;
        fallbackTrack = "";
    }

    public static void update(long positionMs, double distance, String trackName) {
        class_746 player = class_310.method_1551().field_1724;
        if (player == null) {
            return;
        }
        long now = System.currentTimeMillis();
        List<LyricsService.Line> lines = LyricsService.lines();
        if (lines.isEmpty() || LyricsService.state() != LyricsService.State.FOUND) {
            // no lyrics for this song: nothing is shown in the world (never the title / artist)
            WorldLyricLines.closeAll(now);
            WorldLyricLines.expire(now, 6000L);
            return;
        }
        fallbackTrack = "";
        int index = LyricsService.indexAt(lines, positionMs);
        if (index != lineIndex) {
            lineIndex = index;
            WorldLyricLines.closeAll(now);
            if (index >= 0 && !lines.get(index).text().isBlank()) {
                LyricsService.Line line = lines.get(index);
                long end = index + 1 < lines.size() ? lines.get(index + 1).timeMs() : line.timeMs() + 5000L;
                String[] words = line.text().trim().split("\\s+");
                WorldLyricLines.spawn(player, words, LyricsService.wordTiming(words, line.timeMs(), end), distance, now);
            }
        }
        WorldLyricLines.expire(now, 20000L);
    }

    private static void spawn(class_746 player, String[] words, float[] timing, double distance, long now) {
        double[] s = SLOTS[slot++ % SLOTS.length];
        double yaw = Math.toRadians(player.method_36454());
        double fx = -Math.sin(yaw);
        double fz = Math.cos(yaw);
        class_243 eye = player.method_33571();
        double side = s[0] * distance * 0.25;
        double x = eye.field_1352 + fx * distance - fz * side;
        double z = eye.field_1350 + fz * distance + fx * side;
        double y = eye.field_1351 + 0.25 + s[1] * distance * 0.2;
        ENTRIES.add(new Entry(words, timing, x, y, z, now));
        while (ENTRIES.size() > MAX_LINES) {
            ENTRIES.remove(0);
        }
    }

    private static void closeAll(long now) {
        for (Entry e : ENTRIES) {
            if (e.closeAt == 0L) {
                e.closeAt = now;
            }
        }
    }

    private static void expire(long now, long maxLife) {
        ENTRIES.removeIf(e -> e.closeAt != 0L && now - e.closeAt > CLOSE_MS || now - e.born > maxLife + CLOSE_MS);
        for (Entry e : ENTRIES) {
            if (e.closeAt == 0L && now - e.born > maxLife) {
                e.closeAt = now;
            }
        }
    }

    public static void render(class_332 context, int accent, float baseSize, long positionMs) {
        if (ENTRIES.isEmpty()) {
            return;
        }
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null || mc.field_1773 == null) {
            return;
        }
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        class_243 cam = mc.field_1773.method_19418().method_71156();
        FontRenderer font = Fonts.getSize(32, Fonts.Type.INTER_BOLD);
        float space = font.getStringWidth(" ");
        long now = System.currentTimeMillis();
        for (Entry e : ENTRIES) {
            long age = now - e.born;
            float closing = e.closeAt == 0L ? 0.0f : Math.min(1.0f, (now - e.closeAt) / (float)CLOSE_MS);
            float appear = Math.min(1.0f, age / (float)APPEAR_MS);
            float appearEase = 1.0f - (1.0f - appear) * (1.0f - appear) * (1.0f - appear);
            float alpha = Math.min(appearEase, 1.0f - closing);
            if (alpha <= 0.02f) continue;
            class_243 pos = new class_243(e.x, e.y + closing * closing * 1.2, e.z);
            class_243 screen = ProjectionUtil.worldSpaceToScreenSpace(pos);
            if (screen.field_1350 <= 0.0 || screen.field_1350 >= 1.0) continue;
            double d = Math.max(0.6, cam.method_1022(pos));
            // pulse of the whole line on every sung word = the beat of the singing
            float pulse = 0.0f;
            for (int i = 0; i < e.words.length; ++i) {
                float since = positionMs - e.timing[i * 2];
                if (since >= 0.0f && since < 400.0f) {
                    pulse = Math.max(pulse, (float)Math.exp(-since / 110.0));
                }
            }
            float scale = (float)(baseSize / 32.0 * Math.min(3.0, 6.0 / d)) * (0.75f + 0.25f * appearEase) * (1.0f + 0.07f * pulse * (1.0f - closing));
            if (scale < 0.05f) continue;
            String[] words = new String[e.words.length];
            float[] widths = new float[e.words.length];
            float total = 0.0f;
            for (int i = 0; i < words.length; ++i) {
                words[i] = WorldLyricLines.safe(font, e.words[i]);
                widths[i] = font.getStringWidth(words[i]);
                total += widths[i] + (i > 0 ? space : 0.0f);
            }
            float height = font.getStringHeight("Ag");
            context.method_51448().pushMatrix();
            context.method_51448().translate((float)screen.field_1352, (float)screen.field_1351);
            context.method_51448().scale(scale, scale);
            float wx = -total / 2.0f;
            for (int i = 0; i < words.length; ++i) {
                float start = e.timing[i * 2];
                float dur = Math.max(1.0f, e.timing[i * 2 + 1]);
                float since = positionMs - start;
                boolean sung = since >= 0.0f;
                boolean current = sung && since < dur;
                float pop = sung ? (float)Math.exp(-since / 150.0) : 0.0f;
                int color;
                float a;
                if (current) {
                    color = accent;
                    a = alpha;
                } else if (sung) {
                    color = 0xFFFFFFFF;
                    a = alpha;
                } else {
                    color = 0xFFFFFFFF;
                    a = alpha * 0.35f;
                }
                float cx = wx + widths[i] / 2.0f;
                float wy = -height / 2.0f - pop * height * 0.25f;
                context.method_51448().pushMatrix();
                context.method_51448().translate(cx, 0.0f);
                context.method_51448().scale(1.0f + 0.3f * pop, 1.0f + 0.3f * pop);
                font.drawString(matrix, words[i], -widths[i] / 2.0f + 1.4f, wy + 1.4f, ColorUtil.multAlpha(0xFF000000, a * 0.6f));
                if (current) {   // soft glow behind the word being sung
                    font.drawString(matrix, words[i], -widths[i] / 2.0f, wy, ColorUtil.multAlpha(accent, a * 0.35f));
                }
                font.drawString(matrix, words[i], -widths[i] / 2.0f, wy, ColorUtil.multAlpha(color, a));
                context.method_51448().popMatrix();
                wx += widths[i] + space;
            }
            context.method_51448().popMatrix();
        }
    }

    private static String safe(FontRenderer font, String text) {
        return text.isEmpty() || font.hasGlyph(text.codePointAt(0)) ? text : text.replaceAll("[♪-♬]", "~");
    }
}
