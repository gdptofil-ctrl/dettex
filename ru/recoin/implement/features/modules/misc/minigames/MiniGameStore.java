package ru.recoin.implement.features.modules.misc.minigames;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Progress of the client mini games, kept in config/dettex/minigames.json:
 * MineQuiz XP and day streak, and the virtual coins / items of the Upgrader.
 * Everything here is virtual: nothing is sent to a server and nothing can be traded.
 */
public final class MiniGameStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Data data;

    public static final class Data {
        public long coins = 500L;
        public long xp;
        public int streak;
        public String lastLessonDay = "";
        public String lastDailyBonus = "";
        public int lessonsDone;
        public int bestUpgradeX10;   // best won multiplier * 10
        public Map<String, Integer> items = new LinkedHashMap<>();
    }

    private MiniGameStore() {
    }

    public static synchronized Data get() {
        if (data == null) {
            data = MiniGameStore.load();
        }
        return data;
    }

    private static Path file() {
        Path dir;
        try {
            dir = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("dettex");
        } catch (Throwable ignored) {
            dir = Paths.get("config", "dettex");
        }
        return dir.resolve("minigames.json");
    }

    private static Data load() {
        try {
            Path f = MiniGameStore.file();
            if (Files.exists(f)) {
                Data d = GSON.fromJson(Files.readString(f, StandardCharsets.UTF_8), Data.class);
                if (d != null) {
                    if (d.items == null) d.items = new LinkedHashMap<>();
                    return d;
                }
            }
        } catch (Exception ignored) {
        }
        Data d = new Data();
        // starter kit, so the Upgrader can be tried right away
        d.items.put("crusher_sword", 1);
        d.items.put("diamond", 3);
        return d;
    }

    public static synchronized void save() {
        try {
            Path f = MiniGameStore.file();
            Files.createDirectories(f.getParent());
            Files.writeString(f, GSON.toJson(MiniGameStore.get()), StandardCharsets.UTF_8);
        } catch (Exception ignored) {
        }
    }

    public static int count(String id) {
        return MiniGameStore.get().items.getOrDefault(id, 0);
    }

    public static void addItem(String id, int n) {
        Map<String, Integer> items = MiniGameStore.get().items;
        int v = items.getOrDefault(id, 0) + n;
        if (v <= 0) {
            items.remove(id);
        } else {
            items.put(id, v);
        }
    }

    /** A finished lesson: XP, coins and the day streak (kept when you play every day). */
    public static void lessonFinished(int xpGained, int coinsGained) {
        Data d = MiniGameStore.get();
        d.xp += xpGained;
        d.coins += coinsGained;
        d.lessonsDone++;
        String today = LocalDate.now().toString();
        if (!today.equals(d.lastLessonDay)) {
            String yesterday = LocalDate.now().minusDays(1L).toString();
            d.streak = yesterday.equals(d.lastLessonDay) ? d.streak + 1 : 1;
            d.lastLessonDay = today;
        }
        MiniGameStore.save();
    }

    /** Streak shown to the player: it is lost if yesterday was skipped. */
    public static int currentStreak() {
        Data d = MiniGameStore.get();
        String today = LocalDate.now().toString();
        String yesterday = LocalDate.now().minusDays(1L).toString();
        return today.equals(d.lastLessonDay) || yesterday.equals(d.lastLessonDay) ? d.streak : 0;
    }

    public static boolean dailyBonusReady() {
        return !LocalDate.now().toString().equals(MiniGameStore.get().lastDailyBonus);
    }

    public static void takeDailyBonus(long coins) {
        Data d = MiniGameStore.get();
        d.coins += coins;
        d.lastDailyBonus = LocalDate.now().toString();
        MiniGameStore.save();
    }
}
