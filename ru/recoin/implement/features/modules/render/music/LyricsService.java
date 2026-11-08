package ru.recoin.implement.features.modules.render.music;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ru.recoin.common.util.logger.LoggerUtil;

/**
 * Synced lyrics for the track playing on the PC, from the free LRCLIB database (lrclib.net).
 * Lyrics are downloaded on a background thread and kept in a small cache; nothing is shipped with the client.
 */
public final class LyricsService {
    public record Line(long timeMs, String text) {
    }

    /** Result of a lyric lookup: the lines, whether they carry timings, and which site answered. */
    record FetchResult(List<Line> lines, boolean synced, String source) {
    }

    private record Picked(List<Line> lines, boolean synced) {
    }

    public enum State { IDLE, LOADING, FOUND, NOT_FOUND }

    private static final String API = System.getProperty("dettex.lyrics.api", "https://lrclib.net/api");
    private static final String NETEASE = System.getProperty("dettex.netease.api", "https://music.163.com");
    private static final String LYRICS_OVH = System.getProperty("dettex.lyricsovh.api", "https://api.lyrics.ovh/v1");
    private static final String LYRIST = System.getProperty("dettex.lyrist.api", "https://lyrist.vercel.app/api");
    private static final String USER_AGENT = "DettexClient/1.0 (https://github.com/sagyndyk571-droid/dettex)";
    private static final Pattern TAG = Pattern.compile("\\[(\\d{1,3}):(\\d{1,2})(?:[.:](\\d{1,3}))?]");
    private static final Pattern NOISE = Pattern.compile("(?i)\\s*[(\\[][^)\\]]*(official|video|audio|lyric|visuali[sz]er|music video|mv|hd|4k|remaster|clip|клип|премьера|live|cover|кавер|remix|ремикс|prod|slowed|sped up|speed up|nightcore|bass boost|текст)[^)\\]]*[)\\]]");
    private static final Pattern TAIL = Pattern.compile("(?i)\\s*(на всех площадках|премьера( клипа| трека)?|official (music )?(video|audio)|lyrics?|текст песни|\\|.*)\\s*$");
    private static final Pattern CREDIT = Pattern.compile("^(作词|作曲|编曲|制作人|混音|母带|和声|吉他|贝斯|鼓|录音|监制|出品|OP|SP|词|曲)\\s*[:：].*");
    private static final Pattern VERSION = Pattern.compile("(?i)^(\\d{4}\\s+)?(remaster(ed)?|live|radio edit|edit|version|mono|stereo|acoustic|remix|mix|single|bonus)\\b");
    private static final Pattern REMIX_TAIL = Pattern.compile("(?i)\\s+((gay|гей|phonk|фонк|funk|фанк|hardstyle|slowed|sped up|speed up|bass boosted|tiktok|тикток)\\s+)*(remix|ремикс|rmx|mix|edit|эдит|slowed|sped up|speed up|reverb|bass boosted|nightcore|version|версия)\\s*$");
    private static final Pattern DEFAULT_USER = Pattern.compile("(?i)^(user[\\s-]*\\d+|unknown|неизвестен|unknown artist|неизвестный исполнитель)$");
    private static final Pattern FEAT = Pattern.compile("(?i)\\s*[(\\[]?\\s*(feat\\.?|ft\\.?|featuring)\\s+[^)\\]]*[)\\]]?");

    private static final Map<String, List<Line>> CACHE = Collections.synchronizedMap(new LinkedHashMap<String, List<Line>>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, List<Line>> eldest) {
            if (this.size() > 64) {
                SYNCED.remove(eldest.getKey());
                RETRY_AT.remove(eldest.getKey());
                return true;
            }
            return false;
        }
    });
    private static final Map<String, Boolean> SYNCED = new java.util.concurrent.ConcurrentHashMap<String, Boolean>();
    /** Tracks that had no lyrics yet: they are looked up again later instead of staying empty for the whole session. */
    private static final Map<String, Long> RETRY_AT = new java.util.concurrent.ConcurrentHashMap<String, Long>();
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Dettex-Lyrics");
        t.setDaemon(true);
        return t;
    });
    private static HttpClient http;
    private static volatile String key = "";
    private static volatile List<Line> lines = List.of();
    private static volatile State state = State.IDLE;
    private static volatile boolean synced = true;
    private static volatile String status = "";
    private static volatile long retryAt;

    private LyricsService() {
    }

    public static State state() {
        return state;
    }

    /** False when only plain lyrics were found and the timings are estimated. */
    public static boolean isSynced() {
        return synced;
    }

    /** Short human-readable status for the HUD (track + what happened with its lyrics). */
    public static String status() {
        return status;
    }

    public static List<Line> lines() {
        return lines;
    }

    /** Called every tick with the current track; starts a download when the track changes. */
    public static void update(String artist, String title, long durationSec) {
        String a = artist == null ? "" : artist.trim();
        String t = title == null ? "" : title.trim();
        String k = (a + "\n" + t).toLowerCase(Locale.ROOT);
        if (k.equals(key) && state == State.NOT_FOUND && LyricsService.localFolderChanged()) {
            CACHE.clear();   // a lyrics file was added: look again
            key = "";
        }
        long now = System.currentTimeMillis();
        Long due = RETRY_AT.get(k);
        boolean retryDue = due != null && now > due;
        if (k.equals(key) && !retryDue && !(retryAt > 0L && now > retryAt)) {
            return;
        }
        retryAt = 0L;
        key = k;
        if (t.isEmpty()) {
            lines = List.of();
            state = State.IDLE;
            return;
        }
        List<Line> cached = CACHE.get(k);
        Long retry = RETRY_AT.get(k);
        if (cached != null && cached.isEmpty() && retry != null && now > retry) {
            CACHE.remove(k);   // try again: a lyrics site may have been temporarily unavailable
            RETRY_AT.remove(k);
            cached = null;
        }
        if (cached != null) {
            lines = cached;
            synced = !Boolean.FALSE.equals(SYNCED.get(k));
            state = cached.isEmpty() ? State.NOT_FOUND : State.FOUND;
            status = cached.isEmpty() ? "no lyrics for: " + a + " - " + t : cached.size() + " lines (cached)";
            return;
        }
        lines = List.of();
        state = State.LOADING;
        status = "searching: " + a + " - " + t;
        LoggerUtil.info("[MusicHud] track: " + a + " - " + t + " (" + durationSec + "s), searching lyrics");
        WORKER.execute(() -> {
            if (!k.equals(key)) {
                return;   // the track changed while this search was waiting in the queue
            }
            searching = k;
            List<Line> found = List.of();
            boolean foundSynced = true;
            String foundSource = "";
            boolean failed = false;
            try {
                FetchResult result = LyricsService.fetch(a, t, durationSec);
                found = result.lines();
                foundSynced = result.synced();
                foundSource = result.source();
                status = found.isEmpty() ? "no lyrics for: " + a + " - " + t : (foundSynced ? "synced lyrics: " : "plain lyrics: ") + found.size() + " lines (" + foundSource + ")";
            } catch (StaleSearch stale) {
                return;   // the track changed during the search: the new one is already queued
            } catch (Exception e) {
                // every lyrics site unreachable: try again in a minute instead of remembering "no lyrics"
                failed = true;
                status = "lyrics sites unreachable: " + e.getClass().getSimpleName() + (e.getMessage() == null ? "" : " " + e.getMessage());
            }
            LoggerUtil.info("[MusicHud] " + status);
            if (failed) {
                if (k.equals(key)) {
                    retryAt = System.currentTimeMillis() + 60_000L;
                }
            } else {
                CACHE.put(k, found);
                SYNCED.put(k, foundSynced);
                if (found.isEmpty()) {
                    RETRY_AT.put(k, System.currentTimeMillis() + 20 * 60_000L);
                } else {
                    RETRY_AT.remove(k);
                }
            }
            if (k.equals(key)) {
                lines = found;
                synced = foundSynced;
                state = found.isEmpty() ? State.NOT_FOUND : State.FOUND;
            }
        });
    }

    /** Start time and duration of each word: the line's first 85% is split by word length. Pairs [start, duration]. */
    public static float[] wordTiming(String[] words, long start, long end) {
        float[] out = new float[words.length * 2];
        int total = 0;
        for (String w : words) {
            total += w.length() + 1;
        }
        float span = Math.max(400.0f, Math.min(end - start, 12000L) * 0.85f);
        float acc = 0.0f;
        for (int i = 0; i < words.length; ++i) {
            float share = (words[i].length() + 1) / (float)Math.max(1, total);
            out[i * 2] = start + span * acc;
            out[i * 2 + 1] = span * share;
            acc += share;
        }
        return out;
    }

    /** Index of the line sung at this position (-1 before the first line). */
    public static int indexAt(List<Line> list, long positionMs) {
        int lo = 0;
        int hi = list.size() - 1;
        int found = -1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (list.get(mid).timeMs() <= positionMs) {
                found = mid;
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return found;
    }

    // ------------------------------------------------------------------ download

    static FetchResult fetch(String artist, String title, long durationSec) throws Exception {
        String[] at = LyricsService.guessArtistTitle(artist, title);
        // 0) the player's own lyrics files: config/dettex/lyrics/<song>.lrc or .txt
        Picked local = LyricsService.localLyrics(artist, title, at, durationSec);
        if (!local.lines().isEmpty()) {
            return new FetchResult(local.lines(), local.synced(), "local file");
        }
        List<String[]> pairs = LyricsService.searchPairs(title, at);
        List<String> titleQueries = LyricsService.titleQueries(at);
        boolean answered = false;
        Exception lastError = null;
        // 1) LRCLIB directly, 2) LRCLIB through the Dettex sync server (for networks where lrclib.net is blocked)
        for (String base : LyricsService.lrclibBases()) {
            String label = base.contains("lrclib.net") ? "LRCLIB" : "LRCLIB via sync server";
            try {
                for (String[] pair : pairs) {
                    ArrayList<JsonElement> records = new ArrayList<JsonElement>();
                    LyricsService.collect(base, records, pair[0], pair[1], durationSec);
                    Picked found = LyricsService.choose(records, durationSec);
                    if (!found.lines().isEmpty()) {
                        return new FetchResult(found.lines(), found.synced(), label);
                    }
                }
                // the uploader is not the artist (re-uploads, remixes, compilations): search by the song name only,
                // trusting only a recording of the same length so another song's lyrics are not shown
                for (String qt : titleQueries) {
                    ArrayList<JsonElement> byTitle = new ArrayList<JsonElement>();
                    LyricsService.collect(base, byTitle, "", qt, 0L);
                    if (durationSec > 0) {
                        byTitle.removeIf(e -> !LyricsService.sameLength(e, durationSec));
                    }
                    Picked found = LyricsService.choose(byTitle, durationSec);
                    if (!found.lines().isEmpty()) {
                        return new FetchResult(found.lines(), found.synced(), label);
                    }
                }
                answered = true;
                break;   // the sync server mirrors the same database, no point asking it after a valid answer
            } catch (java.io.IOException e) {
                DOWN_UNTIL.put(base, System.currentTimeMillis() + 10 * 60_000L);   // skip this route for a while
                lastError = e;
            }
        }
        // 3) NetEase Cloud Music: a big catalogue with synced lyrics, reachable from most networks
        for (String[] pair : pairs) {
            try {
                List<Line> found = LyricsService.netease(pair[0], pair[1], durationSec);
                answered = true;
                if (!found.isEmpty()) {
                    return new FetchResult(found, true, "NetEase");
                }
            } catch (java.io.IOException e) {
                lastError = e;
            }
        }
        for (String qt : titleQueries) {
            try {
                List<Line> found = LyricsService.netease("", qt, durationSec);
                if (!found.isEmpty()) {
                    return new FetchResult(found, true, "NetEase");
                }
            } catch (java.io.IOException e) {
                lastError = e;
            }
        }
        // 4) plain-lyrics databases for the songs LRCLIB and NetEase do not have
        for (String[] pair : pairs) {
            List<Line> found = LyricsService.lyricsOvh(pair[0], pair[1], durationSec);
            if (!found.isEmpty()) {
                return new FetchResult(found, false, "lyrics.ovh");
            }
            found = LyricsService.lyrist(pair[0], pair[1], durationSec);
            if (!found.isEmpty()) {
                return new FetchResult(found, false, "Lyrist");
            }
        }
        if (!answered && lastError != null) {
            throw lastError;
        }
        return new FetchResult(List.of(), true, "");
    }

    /** Search variants: cleaned artist/title, the raw title, and "Artist - Song" taken from the title itself. */
    private static List<String[]> searchPairs(String rawTitle, String[] at) {
        ArrayList<String[]> pairs = new ArrayList<String[]>();
        LyricsService.addPair(pairs, at[0], at[1]);
        LyricsService.addPair(pairs, at[0], rawTitle);
        int dash = at[1].indexOf(" - ");
        if (dash > 0) {
            LyricsService.addPair(pairs, at[1].substring(0, dash).trim(), at[1].substring(dash + 3).trim());
        }
        return pairs;
    }

    private static void addPair(List<String[]> pairs, String artist, String title) {
        String a = artist == null ? "" : artist.trim();
        String t = title == null ? "" : title.trim();
        if (t.length() < 2) {
            return;
        }
        String key = LyricsService.letters(a) + "\u0000" + LyricsService.letters(t);
        for (String[] pair : pairs) {
            if ((LyricsService.letters(pair[0]) + "\u0000" + LyricsService.letters(pair[1])).equals(key)) {
                return;
            }
        }
        if (pairs.size() < 3) {
            pairs.add(new String[]{a, t});
        }
    }

    private static List<String> titleQueries(String[] at) {
        ArrayList<String> out = new ArrayList<String>();
        LyricsService.addQuery(out, at[1]);
        int dash = at[1].indexOf(" - ");
        if (dash > 0) {
            LyricsService.addQuery(out, at[1].substring(dash + 3).trim());
        }
        return out;
    }

    private static void addQuery(List<String> out, String value) {
        String q = value == null ? "" : value.trim();
        if (q.length() >= 2 && !out.contains(q)) {
            out.add(q);
        }
    }

    /** Plain lyrics from api.lyrics.ovh (no timings; spread over the song length). */
    private static List<Line> lyricsOvh(String artist, String title, long durationSec) {
        if (title.isBlank() || artist.isBlank()) {
            return List.of();
        }
        Long down = DOWN_UNTIL.get(LYRICS_OVH);
        if (down != null && System.currentTimeMillis() < down) {
            return List.of();
        }
        try {
            String body = LyricsService.get(LYRICS_OVH + "/" + enc(artist) + "/" + enc(title), 5);
            if (body == null || body.isBlank()) {
                return List.of();
            }
            JsonObject root = JsonParser.parseString(body).getAsJsonObject();
            if (!root.has("lyrics") || root.get("lyrics").isJsonNull()) {
                return List.of();
            }
            return LyricsService.cleanPlain(root.get("lyrics").getAsString(), durationSec);
        } catch (StaleSearch stale) {
            throw stale;
        } catch (java.io.IOException e) {
            DOWN_UNTIL.put(LYRICS_OVH, System.currentTimeMillis() + 10 * 60_000L);
            return List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    /** Plain lyrics from the free Lyrist API. */
    private static List<Line> lyrist(String artist, String title, long durationSec) {
        if (title.isBlank()) {
            return List.of();
        }
        Long down = DOWN_UNTIL.get(LYRIST);
        if (down != null && System.currentTimeMillis() < down) {
            return List.of();
        }
        try {
            String body = LyricsService.get(LYRIST + "/" + enc(title) + "/" + enc(artist == null ? "" : artist), 5);
            if (body == null || body.isBlank()) {
                return List.of();
            }
            JsonObject root = JsonParser.parseString(body).getAsJsonObject();
            if (!root.has("lyrics") || root.get("lyrics").isJsonNull()) {
                return List.of();
            }
            return LyricsService.cleanPlain(root.get("lyrics").getAsString(), durationSec);
        } catch (StaleSearch stale) {
            throw stale;
        } catch (java.io.IOException e) {
            DOWN_UNTIL.put(LYRIST, System.currentTimeMillis() + 10 * 60_000L);
            return List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    /** Drops section headers ("[Chorus]") and credit rows from plain lyrics, then spreads them over the song. */
    private static List<Line> cleanPlain(String text, long durationSec) {
        StringBuilder clean = new StringBuilder();
        for (String row : text.replace("\r", "").split("\n")) {
            String line = row.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.charAt(0) == '[' && line.charAt(line.length() - 1) == ']') {
                continue;
            }
            if (CREDIT.matcher(line).matches()) {
                continue;
            }
            if (line.equals("\u266a") || line.equals("\u266b") || LyricsService.isInstrumental(line)) {
                continue;
            }
            clean.append(line).append('\n');
        }
        return LyricsService.spreadPlain(clean.toString(), durationSec);
    }

    private static boolean isInstrumental(String text) {
        String s = LyricsService.letters(text);
        return s.equals("\u7eaf\u97f3\u4e50\u8bf7\u6b23\u8d4f") || s.equals("instrumental") || s.equals("instrumentalversion");
    }

    private static boolean sameLength(JsonElement e, long durationSec) {
        if (e == null || !e.isJsonObject() || !e.getAsJsonObject().has("duration") || e.getAsJsonObject().get("duration").isJsonNull()) {
            return false;
        }
        return Math.abs(e.getAsJsonObject().get("duration").getAsDouble() - durationSec) <= 4.0;
    }

    // ------------------------------------------------------------------ local lyrics files

    public static java.nio.file.Path lyricsFolder() {
        java.nio.file.Path dir;
        try {
            dir = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("dettex").resolve("lyrics");
        } catch (Throwable ignored) {
            dir = java.nio.file.Paths.get("config", "dettex", "lyrics");
        }
        try {
            if (!java.nio.file.Files.isDirectory(dir)) {
                java.nio.file.Files.createDirectories(dir);
                java.nio.file.Files.writeString(dir.resolve("README.txt"), String.join(System.lineSeparator(),
                        "Music HUD: your own lyrics for songs that are not found online.",
                        "Put a file here named like the song, for example:",
                        "  Artist - Song.lrc   (synced lyrics: lines like [01:23.45] words)",
                        "  Song.txt            (plain text, one line per row; timings are estimated)",
                        "The name is matched ignoring case, spaces and punctuation; just the song title is enough.",
                        "The client sees new files a few seconds after you add them, no restart needed.",
                        ""), StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {
        }
        return dir;
    }

    private static Picked localLyrics(String artist, String title, String[] cleaned, long durationSec) {
        java.nio.file.Path dir = LyricsService.lyricsFolder();
        String rawArtist = artist == null ? "" : artist;
        String rawTitle = title == null ? "" : title;
        java.util.List<String> wanted = new ArrayList<String>();
        for (String w : new String[]{rawArtist + rawTitle, rawTitle, cleaned[0] + cleaned[1], cleaned[1]}) {
            String l = LyricsService.letters(w);
            if (l.length() >= 2 && !wanted.contains(l)) wanted.add(l);
        }
        java.nio.file.Path best = null;
        int bestRank = Integer.MAX_VALUE;
        try (java.util.stream.Stream<java.nio.file.Path> files = java.nio.file.Files.list(dir)) {
            for (java.nio.file.Path f : (Iterable<java.nio.file.Path>)files::iterator) {
                String name = f.getFileName().toString();
                String lower = name.toLowerCase(Locale.ROOT);
                if (!(lower.endsWith(".lrc") || lower.endsWith(".txt")) || lower.equals("readme.txt")) continue;
                String stem = LyricsService.letters(name.substring(0, name.lastIndexOf('.')));
                if (stem.isEmpty()) continue;
                for (int i = 0; i < wanted.size(); ++i) {
                    String w = wanted.get(i);
                    int rank = stem.equals(w) ? i : stem.length() >= 4 && w.contains(stem) || w.length() >= 4 && stem.contains(w) ? 10 + i : -1;
                    if (rank >= 0 && rank < bestRank) {
                        bestRank = rank;
                        best = f;
                    }
                }
            }
        } catch (Exception ignored) {
            return new Picked(List.of(), true);
        }
        if (best == null) {
            return new Picked(List.of(), true);
        }
        try {
            String text = java.nio.file.Files.readString(best, StandardCharsets.UTF_8).replace("\uFEFF", "");
            List<Line> parsed = LyricsService.parseLrc(text);
            if (!parsed.isEmpty()) {
                return new Picked(parsed, true);
            }
            return new Picked(LyricsService.spreadPlain(text, durationSec), false);
        } catch (Exception e) {
            LoggerUtil.info("[MusicHud] can't read " + best + ": " + e);
            return new Picked(List.of(), true);
        }
    }

    private static long localStamp = -1L;
    private static long localCheckedAt;

    /** A file was added to / changed in the lyrics folder: songs without lyrics are looked up again. */
    private static boolean localFolderChanged() {
        long now = System.currentTimeMillis();
        if (now - localCheckedAt < 3000L) {
            return false;
        }
        localCheckedAt = now;
        long stamp = 0L;
        try (java.util.stream.Stream<java.nio.file.Path> files = java.nio.file.Files.list(LyricsService.lyricsFolder())) {
            for (java.nio.file.Path f : (Iterable<java.nio.file.Path>)files::iterator) {
                stamp = stamp * 31L + f.getFileName().toString().hashCode() + java.nio.file.Files.getLastModifiedTime(f).toMillis();
            }
        } catch (Exception ignored) {
        }
        boolean changed = localStamp != -1L && stamp != localStamp;
        localStamp = stamp;
        return changed;
    }

    private static final Map<String, Long> DOWN_UNTIL = new java.util.concurrent.ConcurrentHashMap<String, Long>();

    private static List<String> lrclibBases() {
        ArrayList<String> out = new ArrayList<String>();
        long now = System.currentTimeMillis();
        String server = ru.recoin.implement.features.modules.render.cosmetics.sync.CosmeticSync.configuredServer();
        for (String base : new String[]{API, server.isEmpty() ? null : server + "/v1/lrclib"}) {
            if (base == null) continue;
            Long down = DOWN_UNTIL.get(base);
            if (down == null || now > down) {
                out.add(base);
            }
        }
        if (out.isEmpty()) {
            out.add(API);   // everything was down: try the direct route again
        }
        return out;
    }

    /** Search NetEase for the song and download its LRC lyrics. */
    private static List<Line> netease(String a, String t, long durationSec) throws Exception {
        String query = (a + " " + t).trim();
        // The plain "search/get/web" endpoint is encrypted for some regions, so try the two open ones first.
        JsonObject result = null;
        for (String url : new String[]{
                NETEASE + "/api/cloudsearch/pc?s=" + enc(query) + "&type=1&offset=0&limit=15",
                NETEASE + "/api/search/get?s=" + enc(query) + "&type=1&offset=0&limit=15",
                NETEASE + "/api/search/get/web?s=" + enc(query) + "&type=1&offset=0&limit=15"}) {
            String body = LyricsService.get(url);
            if (body == null || body.isBlank()) continue;
            try {
                JsonObject root = JsonParser.parseString(body).getAsJsonObject();
                if (!root.has("result") || !root.get("result").isJsonObject()) continue;
                JsonObject candidate = root.getAsJsonObject("result");
                if (!candidate.has("songs") || !candidate.get("songs").isJsonArray()) continue;
                result = candidate;
                break;
            } catch (RuntimeException ignored) {
                // encrypted / non-JSON answer: try the next endpoint
            }
        }
        if (result == null || result.getAsJsonArray("songs").isEmpty()) {
            return List.of();
        }
        String wantTitle = LyricsService.letters(t);
        String wantArtist = LyricsService.letters(a);
        long bestId = -1L;
        double bestScore = 0.0;
        boolean bestArtist = false;
        boolean bestExactTitle = false;
        double bestDuration = 0.0;
        for (JsonElement e : result.getAsJsonArray("songs")) {
            JsonObject song = e.getAsJsonObject();
            if (!song.has("name") || song.get("name").isJsonNull()) continue;
            String rawName = song.get("name").getAsString();
            String name = LyricsService.letters(rawName);
            if (name.isEmpty() || !(name.contains(wantTitle) || wantTitle.contains(name))) continue;
            String lowerName = rawName.toLowerCase(Locale.ROOT);
            if (lowerName.contains("instrumental") || lowerName.contains("karaoke") || lowerName.contains("\u4f34\u594f")
                    || lowerName.contains("cover") || lowerName.contains("\u7eaf\u97f3\u4e50")) continue;
            boolean artistHit = false;
            double score = 2.0;
            JsonArray artists = null;
            if (song.has("ar") && song.get("ar").isJsonArray()) {
                artists = song.getAsJsonArray("ar");
            } else if (song.has("artists") && song.get("artists").isJsonArray()) {
                artists = song.getAsJsonArray("artists");
            }
            if (artists != null) {
                for (JsonElement ar : artists) {
                    if (!ar.isJsonObject() || !ar.getAsJsonObject().has("name")) continue;
                    String an = LyricsService.letters(ar.getAsJsonObject().get("name").getAsString());
                    if (!an.isEmpty() && !wantArtist.isEmpty() && (an.contains(wantArtist) || wantArtist.contains(an))) {
                        score += 2.0;
                        artistHit = true;
                    }
                }
            }
            double duration = 0.0;
            if (song.has("dt") && !song.get("dt").isJsonNull()) {
                duration = song.get("dt").getAsDouble() / 1000.0;
            } else if (song.has("duration") && !song.get("duration").isJsonNull()) {
                duration = song.get("duration").getAsDouble() / 1000.0;
            }
            if (durationSec > 0 && duration > 0.0) {
                double diff = Math.abs(duration - durationSec);
                score += diff <= 3 ? 1.5 : diff <= 15 ? 0.5 : -1.0;
            }
            score += name.equals(wantTitle) ? 0.8 : 0.0;
            if (score > bestScore) {
                bestScore = score;
                bestId = song.get("id").getAsLong();
                bestArtist = artistHit;
                bestExactTitle = name.equals(wantTitle);
                bestDuration = duration;
            }
        }
        if (bestId < 0) {
            return List.of();
        }
        boolean durationOk = durationSec <= 0 || bestDuration <= 0.0 || Math.abs(bestDuration - durationSec) <= 12.0;
        if (!bestArtist && !(bestExactTitle && durationOk)) {
            return List.of();   // no artist match and not clearly the same recording
        }
        String lyric = LyricsService.get(NETEASE + "/api/song/lyric?os=pc&id=" + bestId + "&lv=-1&kv=-1&tv=-1");
        if (lyric == null) {
            lyric = LyricsService.get(NETEASE + "/api/song/lyric?id=" + bestId + "&lv=1&kv=1&tv=-1");
        }
        if (lyric == null) {
            return List.of();
        }
        JsonObject lr = JsonParser.parseString(lyric).getAsJsonObject();
        if (!lr.has("lrc") || !lr.getAsJsonObject("lrc").has("lyric")) {
            return List.of();
        }
        String text = lr.getAsJsonObject("lrc").get("lyric").getAsString();
        if (text.isBlank() || LyricsService.isInstrumental(text)) {
            return List.of();
        }
        ArrayList<Line> out = new ArrayList<Line>();
        for (Line line : LyricsService.parseLrc(text)) {
            if (!CREDIT.matcher(line.text()).matches() && !line.text().isBlank()) {
                out.add(line);
            }
        }
        return List.copyOf(out);
    }

    /** Adds every LRCLIB record that could be this song (exact match first, then searches). */
    private static void collect(String base, List<JsonElement> out, String a, String t, long durationSec) throws Exception {
        if (!a.isEmpty()) {
            String body = LyricsService.get(base + "/get?artist_name=" + enc(a) + "&track_name=" + enc(t) + (durationSec > 0 ? "&duration=" + durationSec : ""));
            if (body != null) {
                out.add(JsonParser.parseString(body));
                if (LyricsService.hasSynced(out.get(out.size() - 1))) {
                    return;
                }
            }
        }
        String query = a.isEmpty() ? base + "/search?q=" + enc(t) : base + "/search?track_name=" + enc(t) + "&artist_name=" + enc(a);
        LyricsService.addArray(out, LyricsService.get(query));
        if (!a.isEmpty() && out.stream().noneMatch(LyricsService::hasSynced)) {
            LyricsService.addArray(out, LyricsService.get(base + "/search?q=" + enc(a + " " + t)));
        }
    }

    private static void addArray(List<JsonElement> out, String body) {
        if (body == null) {
            return;
        }
        JsonElement root = JsonParser.parseString(body);
        if (root.isJsonArray()) {
            root.getAsJsonArray().forEach(out::add);
        }
    }

    private static boolean hasSynced(JsonElement e) {
        return e != null && e.isJsonObject() && e.getAsJsonObject().has("syncedLyrics") && !e.getAsJsonObject().get("syncedLyrics").isJsonNull()
                && !e.getAsJsonObject().get("syncedLyrics").getAsString().isBlank();
    }

    /** Synced lyrics of the closest-length record; otherwise plain lyrics spread evenly over the song. */
    private static Picked choose(List<JsonElement> records, long durationSec) {
        JsonObject bestSynced = null;
        JsonObject bestPlain = null;
        double diffSynced = Double.MAX_VALUE;
        double diffPlain = Double.MAX_VALUE;
        for (JsonElement e : records) {
            if (e == null || !e.isJsonObject()) continue;
            JsonObject o = e.getAsJsonObject();
            double d = o.has("duration") && !o.get("duration").isJsonNull() ? o.get("duration").getAsDouble() : 0.0;
            double diff = durationSec > 0 && d > 0 ? Math.abs(d - durationSec) : 30.0;
            if (LyricsService.hasSynced(o) && diff < diffSynced && !LyricsService.isInstrumental(o.get("syncedLyrics").getAsString())) {
                diffSynced = diff;
                bestSynced = o;
            }
            if (o.has("plainLyrics") && !o.get("plainLyrics").isJsonNull() && !o.get("plainLyrics").getAsString().isBlank()
                    && diff < diffPlain && !LyricsService.isInstrumental(o.get("plainLyrics").getAsString())) {
                diffPlain = diff;
                bestPlain = o;
            }
        }
        // a much longer/shorter version is a different recording: its timings would be wrong
        if (bestSynced != null && diffSynced <= 15.0) {
            return new Picked(LyricsService.parseLrc(bestSynced.get("syncedLyrics").getAsString()), true);
        }
        if (bestPlain != null) {
            return new Picked(LyricsService.spreadPlain(bestPlain.get("plainLyrics").getAsString(), durationSec), false);
        }
        if (bestSynced != null) {
            return new Picked(LyricsService.parseLrc(bestSynced.get("syncedLyrics").getAsString()), true);
        }
        return new Picked(List.of(), true);
    }

    /** Lyrics without timings: lines are spread evenly between 7% and 95% of the song. */
    static List<Line> spreadPlain(String text, long durationSec) {
        ArrayList<String> rows = new ArrayList<String>();
        for (String r : text.split("\\r?\\n")) {
            if (!r.isBlank()) rows.add(r.trim());
        }
        if (rows.isEmpty()) {
            return List.of();
        }
        long dur = durationSec > 0 ? durationSec * 1000L : rows.size() * 4000L + 10000L;
        long start = (long)(dur * 0.07);
        long step = Math.max(1500L, (long)(dur * 0.95 - start) / rows.size());
        ArrayList<Line> out = new ArrayList<Line>();
        for (int i = 0; i < rows.size(); ++i) {
            out.add(new Line(start + step * i, rows.get(i)));
        }
        return List.copyOf(out);
    }

    /** Parses LRC text: "[mm:ss.xx] words" (a line may carry several time tags). */
    public static List<Line> parseLrc(String lrc) {
        ArrayList<Line> out = new ArrayList<Line>();
        for (String raw : lrc.split("\\r?\\n")) {
            Matcher m = TAG.matcher(raw);
            ArrayList<Long> times = new ArrayList<Long>();
            int end = 0;
            while (m.find() && m.start() == end) {
                long min = Long.parseLong(m.group(1));
                long sec = Long.parseLong(m.group(2));
                String frac = m.group(3);
                long ms = frac == null ? 0L : (long)(Integer.parseInt(frac) * Math.pow(10, 3 - frac.length()));
                times.add(min * 60000L + sec * 1000L + ms);
                end = m.end();
            }
            if (times.isEmpty()) continue;
            String text = raw.substring(end).trim();
            for (long time : times) {
                out.add(new Line(time, text));
            }
        }
        out.sort((x, y) -> Long.compare(x.timeMs(), y.timeMs()));
        return List.copyOf(out);
    }

    /** Cleans YouTube-style titles ("Artist - Song (Official Video)") into artist and track. */
    static String[] guessArtistTitle(String artist, String title) {
        String a = artist == null ? "" : artist.replaceAll("(?i)\\s*-\\s*topic$", "").replaceAll("(?i)\\s*vevo$", "").trim();
        boolean channel = artist != null && !a.equals(artist.trim());   // "X - Topic" / "XVEVO": a YouTube channel, not a real artist tag
        String t = TAIL.matcher(NOISE.matcher(title).replaceAll("")).replaceAll("").trim();
        int dash = t.indexOf(" - ");
        if (dash < 0) {
            dash = t.indexOf(" \u2014 ");
        }
        if (dash > 0) {
            String left = t.substring(0, dash).trim();
            String right = t.substring(dash + 3).trim();
            String na = LyricsService.letters(a);
            String nl = LyricsService.letters(left);
            if (VERSION.matcher(right).find()) {
                t = left;   // "Song - Remastered 2011", "Song - Live": the artist tag is already right
            } else if (a.isEmpty() || channel || nl.contains(na) || na.contains(nl)) {
                a = left;   // "Artist - Song" (YouTube style)
                t = right;
            }
        }
        t = FEAT.matcher(t).replaceAll("").replaceAll("[\"\u00ab\u00bb]", "").trim();
        for (int i = 0; i < 3; ++i) {
            String stripped = REMIX_TAIL.matcher(t).replaceAll("").trim();
            if (stripped.isEmpty() || stripped.equals(t)) break;
            t = stripped;   // "Song gay remix" / "Song slowed reverb" -> "Song"
        }
        if (DEFAULT_USER.matcher(a).matches()) {
            a = "";   // "User 638200072": an uploader without a name, not an artist
        }
        a = a.split("(?i)\\s*(,|&| feat\\.? | ft\\.? | x )\\s*")[0].trim();
        return new String[]{a, t};
    }

    private static String letters(String s) {
        return s.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]", "");
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    /** Thrown inside a search when the player has already switched to another track. */
    private static final class StaleSearch extends RuntimeException {
        StaleSearch() {
            super(null, null, false, false);
        }
    }

    /** Key of the track the worker is searching for right now. */
    private static volatile String searching = "";

    private static String get(String url) throws Exception {
        return LyricsService.get(url, 8);
    }

    private static String get(String url, int timeoutSeconds) throws Exception {
        if (!searching.equals(key)) {
            throw new StaleSearch();   // stop wasting time on a track that is no longer playing
        }
        if (http == null) {
            http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).followRedirects(HttpClient.Redirect.NORMAL).build();
        }
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(timeoutSeconds)).GET();
        if (url.contains("music.163.com")) {
            builder.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36").header("Referer", "https://music.163.com/");
        } else {
            builder.header("User-Agent", USER_AGENT);
        }
        HttpRequest request = builder.build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return response.statusCode() == 200 ? response.body() : null;
    }
}
