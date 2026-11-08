package ru.recoin.implement.features.modules.render.music;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
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
 * Lyrics in the world: every sung word pops up in the air in front of the player and flies away
 * (up and to the side, slowing down, gently swaying) before fading out. Words keep their place in the
 * world, so you can walk past them. Without lyrics for the song, the words of the track name fly instead.
 */
public final class WorldLyrics {
    private static final int MAX_WORDS = 48;
    private static final long FALLBACK_EVERY_MS = 900L;
    private static final Random RANDOM = new Random();
    private static final List<Word> WORDS = new ArrayList<>();
    private static int lineIndex = -2;
    private static int nextWord;
    private static long lastFallback;
    private static int fallbackIndex;
    private static String fallbackTrack = "";
    private static String[] fallbackWords = new String[0];
    private static final String[] NOTES = {"\u266a", "\u266b", "\u266c", "\u266a\u266a"};

    private record Word(String text, double x, double y, double z, double vx, double vy, double vz, long born, long life, boolean accent, float phase) {
    }

    private WorldLyrics() {
    }

    public static void clear() {
        WORDS.clear();
        lineIndex = -2;
        nextWord = 0;
    }

    /** Spawns the words sung since the last call. Without lyrics, the track name words are used. */
    public static void update(long positionMs, long lifeMs, double distance, String trackName) {
        class_746 player = class_310.method_1551().field_1724;
        if (player == null) {
            return;
        }
        List<LyricsService.Line> lines = LyricsService.lines();
        long now = System.currentTimeMillis();
        if (lines.isEmpty() || LyricsService.state() != LyricsService.State.FOUND) {
            // no lyrics for this song: nothing flies (never the title / artist)
            return;
        }
        int index = LyricsService.indexAt(lines, positionMs);
        if (index != lineIndex) {
            lineIndex = index;
            nextWord = 0;
        }
        if (index < 0 || lines.get(index).text().isEmpty()) {
            return;
        }
        LyricsService.Line line = lines.get(index);
        long end = index + 1 < lines.size() ? lines.get(index + 1).timeMs() : line.timeMs() + 5000L;
        String[] words = line.text().split(" ");
        float[] timing = LyricsService.wordTiming(words, line.timeMs(), end);
        while (nextWord < words.length && positionMs >= timing[nextWord * 2]) {
            String w = words[nextWord];
            boolean last = nextWord == words.length - 1;
            ++nextWord;
            if (!w.isBlank()) {
                WorldLyrics.spawn(player, w, now, lifeMs, distance, last || w.length() > 6);
            }
        }
    }

    private static void spawn(class_746 player, String text, long now, long lifeMs, double distance, boolean accent) {
        // a random spot in front of the player, within ~60 degrees of where he looks
        double yaw = Math.toRadians(player.method_36454() + (RANDOM.nextDouble() * 2.0 - 1.0) * 60.0);
        double dist = distance * (0.6 + RANDOM.nextDouble() * 0.5);
        class_243 eye = player.method_33571();
        double x = eye.field_1352 - Math.sin(yaw) * dist;
        double z = eye.field_1350 + Math.cos(yaw) * dist;
        double y = eye.field_1351 - 0.4 + RANDOM.nextDouble() * 1.6;
        // flies up and sideways (blocks per second at the start, then slows down)
        double side = (RANDOM.nextDouble() * 2.0 - 1.0) * 1.2;
        double vx = Math.cos(yaw) * side;
        double vz = Math.sin(yaw) * side;
        double vy = 0.6 + RANDOM.nextDouble() * 0.8;
        WORDS.add(new Word(text, x, y, z, vx, vy, vz, now, lifeMs, accent, RANDOM.nextFloat() * 6.28f));
        if (WORDS.size() > MAX_WORDS) {
            WORDS.remove(0);
        }
    }

    public static void render(class_332 context, int accent, float baseSize) {
        if (WORDS.isEmpty()) {
            return;
        }
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null || mc.field_1773 == null) {
            return;
        }
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        class_243 cam = mc.field_1773.method_19418().method_71156();
        FontRenderer font = Fonts.getSize(32, Fonts.Type.INTER_BOLD);
        long now = System.currentTimeMillis();
        Iterator<Word> it = WORDS.iterator();
        while (it.hasNext()) {
            Word w = it.next();
            long age = now - w.born;
            if (age > w.life) {
                it.remove();
                continue;
            }
            float t = age / (float)w.life;
            double sec = age / 1000.0;
            double drift = (1.0 - Math.exp(-1.6 * sec)) / 1.6;   // fast start, then slows down
            class_243 pos = new class_243(w.x + w.vx * drift, w.y + w.vy * drift + 0.15 * sec, w.z + w.vz * drift);
            class_243 screen = ProjectionUtil.worldSpaceToScreenSpace(pos);
            if (screen.field_1350 <= 0.0 || screen.field_1350 >= 1.0) {
                continue;   // behind the camera
            }
            double d = Math.max(0.5, cam.method_1022(pos));
            float pop = Math.min(1.0f, age / 250.0f);
            float popScale = 1.0f + 0.6f * (1.0f - pop) * (1.0f - pop);
            float scale = (float)(baseSize / 32.0 * Math.min(3.0, 5.0 / d)) * popScale;
            float alpha = Math.min(pop, Math.min(1.0f, (1.0f - t) / 0.35f));
            if (alpha <= 0.02f || scale < 0.05f) continue;
            int color = w.accent ? accent : 0xFFFFFFFF;
            String text = font.hasGlyph(w.text.codePointAt(0)) ? w.text : w.text.replaceAll("[\u266a-\u266c]", "~");
            float width = font.getStringWidth(text);
            float height = font.getStringHeight(text);
            float sway = (float)Math.sin(age / 380.0 + w.phase) * 0.12f;   // radians
            context.method_51448().pushMatrix();
            context.method_51448().translate((float)screen.field_1352, (float)screen.field_1351);
            context.method_51448().rotate(sway);
            context.method_51448().scale(scale, scale);
            font.drawString(matrix, text, -width / 2.0f + 1.2f, -height / 2.0f + 1.2f, ColorUtil.multAlpha(0xFF000000, alpha * 0.6f));
            font.drawString(matrix, text, -width / 2.0f, -height / 2.0f, ColorUtil.multAlpha(color, alpha));
            context.method_51448().popMatrix();
        }
    }
}
