package ru.recoin.implement.features.modules.render.cosmetics.sync;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_310;
import net.minecraft.class_634;
import net.minecraft.class_640;

/**
 * Shares the cosmetics picked in the Cosmetics module with every other Dettex user through a small
 * sync server (see sync-server/ in the project). Each client announces its own selection and looks up
 * the players it can see in the tab list; players found on the server are Dettex users.
 * All network work runs on one background thread, so the game thread never waits for the server.
 */
public final class CosmeticSync {
    /** Cosmetics another Dettex user has chosen; a null field means that slot is off. */
    public record Profile(String model, String wings, String hat, String backpack, String halo, String cape, String mace, String sword) {
        static final Profile EMPTY = new Profile(null, null, null, null, null, null, null, null);

        JsonObject toJson() {
            JsonObject o = new JsonObject();
            put(o, "model", this.model); put(o, "wings", this.wings); put(o, "hat", this.hat); put(o, "backpack", this.backpack);
            put(o, "halo", this.halo); put(o, "cape", this.cape); put(o, "mace", this.mace); put(o, "sword", this.sword);
            return o;
        }

        static Profile fromJson(JsonObject o) {
            return new Profile(str(o, "model"), str(o, "wings"), str(o, "hat"), str(o, "backpack"), str(o, "halo"), str(o, "cape"), str(o, "mace"), str(o, "sword"));
        }

        private static void put(JsonObject o, String key, String value) {
            if (value != null) {
                o.addProperty(key, value);
            }
        }

        private static String str(JsonObject o, String key) {
            JsonElement e = o.get(key);
            if (e == null || !e.isJsonPrimitive()) {
                return null;
            }
            String s = e.getAsString();
            return s.isEmpty() || s.length() > 64 ? null : s;
        }
    }

    /** misses = how many lookups in a row said "not a Dettex user" (new players are re-checked sooner). */
    private record Entry(Profile profile, long checkedAt, int misses) {
        long recheckAfter() {
            if (this.profile != null) {
                return RECHECK_AFTER_MS;
            }
            return this.misses <= 2 ? 15_000L : this.misses <= 4 ? 60_000L : RECHECK_AFTER_MS;
        }
    }

    private static final long ANNOUNCE_EVERY_MS = 5 * 60_000L;   // refresh our record even if nothing changed
    private static final long LOOKUP_EVERY_MS = 4_000L;          // how often new tab-list players are looked up
    private static final long RECHECK_AFTER_MS = 10 * 60_000L;   // re-ask the server about a player already checked
    private static final long BACKOFF_MS = 60_000L;              // pause after the server fails
    private static final int LOOKUP_BATCH = 100;

    private static final Map<UUID, Entry> USERS = new ConcurrentHashMap<>();
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Dettex-CosmeticSync");
        t.setDaemon(true);
        return t;
    });
    private static final AtomicBoolean BUSY = new AtomicBoolean();
    private static HttpClient http;
    private static String token;
    private static String lastAnnounced;
    private static long lastAnnounceAt;
    private static long lastLookupAt;
    private static long pausedUntil;
    private static String serverUrl = "";

    private CosmeticSync() {
    }

    /** Remote profile of a Dettex user, or null when the player is not known as one. */
    public static Profile get(UUID uuid) {
        Entry e = uuid == null ? null : USERS.get(uuid);
        return e == null ? null : e.profile;
    }

    public static boolean isDettexUser(UUID uuid) {
        return get(uuid) != null;
    }

    public static void reset() {
        USERS.clear();
        lastAnnounced = null;
        lastAnnounceAt = 0L;
    }

    private static Object warnedWorld;
    private static final ru.recoin.api.system.logger.implement.MinecraftLogger CHAT = new ru.recoin.api.system.logger.implement.MinecraftLogger();

    /** Once per world: tell the player that without a sync server nobody sees anybody's cosmetics. */
    private static void warnNoServer(class_310 mc) {
        if (warnedWorld == mc.field_1687) {
            return;
        }
        warnedWorld = mc.field_1687;
        String msg = "\u00a7e[Dettex] \u00a7fSync Server is empty: other Dettex players can't see your cosmetics and you can't see theirs. "
                + "Cosmetics -> Sync -> Sync Server (the same address for everyone), see sync-server/README.md";
        ru.recoin.common.util.logger.LoggerUtil.info("[CosmeticSync] no sync server configured, sync is off");
        try {
            CHAT.minecraftLog(new net.minecraft.class_2561[]{net.minecraft.class_2561.method_43470(msg)});
        } catch (Throwable ignored) {
        }
    }

    /** Called every client tick while the Cosmetics module is on. */
    public static void tick(String url, boolean share, Profile own) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null || mc.method_1562() == null) {
            return;
        }
        String base = normalize(url);
        if (!base.equals(serverUrl)) {
            serverUrl = base;
            reset();
        }
        long now = System.currentTimeMillis();
        if (base.isEmpty()) {
            CosmeticSync.warnNoServer(mc);
            return;
        }
        warnedWorld = null;
        if (now < pausedUntil || BUSY.get()) {
            return;
        }
        UUID self = mc.field_1724.method_5667();
        String name = mc.field_1724.method_5477().getString();
        if (share) {
            String body = announceBody(self, name, own);
            if (!body.equals(lastAnnounced) || now - lastAnnounceAt > ANNOUNCE_EVERY_MS) {
                lastAnnounced = body;
                lastAnnounceAt = now;
                USERS.put(self, new Entry(own, now, 0));
                submit(() -> post(base + "/v1/announce", body));
                return;
            }
        }
        if (now - lastLookupAt < LOOKUP_EVERY_MS) {
            return;
        }
        lastLookupAt = now;
        List<UUID> ask = new ArrayList<>();
        class_634 handler = mc.method_1562();
        for (class_640 entry : handler.method_2880()) {
            UUID id = entry.method_2966().id();
            if (id == null || id.equals(self)) continue;
            Entry known = USERS.get(id);
            if (known != null && now - known.checkedAt < known.recheckAfter()) continue;
            ask.add(id);
            if (ask.size() >= LOOKUP_BATCH) break;
        }
        if (ask.isEmpty()) {
            return;
        }
        // mark them as checked right away so the same players are not asked again every few seconds
        for (UUID id : ask) {
            Entry known = USERS.get(id);
            USERS.put(id, new Entry(known == null ? null : known.profile, now, known == null ? 0 : known.misses));
        }
        JsonObject req = new JsonObject();
        JsonArray arr = new JsonArray();
        ask.forEach(id -> arr.add(id.toString()));
        req.add("uuids", arr);
        submit(() -> {
            String response = post(base + "/v1/lookup", req.toString());
            if (response == null) {
                return;
            }
            JsonObject users = JsonParser.parseString(response).getAsJsonObject().getAsJsonObject("users");
            long at = System.currentTimeMillis();
            for (UUID id : ask) {
                JsonElement u = users == null ? null : users.get(id.toString());
                Profile p = u != null && u.isJsonObject() && u.getAsJsonObject().has("c") ? Profile.fromJson(u.getAsJsonObject().getAsJsonObject("c")) : null;
                Entry known = USERS.get(id);
                int misses = p != null ? 0 : (known == null ? 0 : known.misses) + 1;
                USERS.put(id, new Entry(p, at, misses));   // p == null: not a Dettex user (yet), asked again later
            }
        });
    }

    private static String announceBody(UUID self, String name, Profile own) {
        JsonObject o = new JsonObject();
        o.addProperty("uuid", self.toString());
        o.addProperty("name", name);
        o.addProperty("token", token());
        o.add("c", (own == null ? Profile.EMPTY : own).toJson());
        return o.toString();
    }

    private static void submit(Runnable job) {
        if (!BUSY.compareAndSet(false, true)) {
            return;
        }
        WORKER.execute(() -> {
            try {
                job.run();
            } catch (Exception ignored) {
                pausedUntil = System.currentTimeMillis() + BACKOFF_MS;
            } finally {
                BUSY.set(false);
            }
        });
    }

    private static String post(String url, String json) {
        try {
            if (http == null) {
                http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
            }
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(8))
                    .header("Content-Type", "application/json").header("User-Agent", "DettexClient-CosmeticSync/1")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8)).build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() / 100 != 2) {
                pausedUntil = System.currentTimeMillis() + BACKOFF_MS;
                return null;
            }
            return response.body();
        } catch (Exception e) {
            pausedUntil = System.currentTimeMillis() + BACKOFF_MS;
            return null;
        }
    }

    /** The configured sync server as a full base URL ("" when none), even if the Cosmetics module is off. */
    public static String configuredServer() {
        try {
            ru.recoin.implement.features.modules.render.cosmetics.Cosmetics c = ru.recoin.implement.features.modules.render.cosmetics.Cosmetics.getInstance();
            return c == null ? "" : CosmeticSync.normalize(c.getSyncServer());
        } catch (Throwable t) {
            return "";
        }
    }

    private static String normalize(String url) {
        if (url == null) {
            return "";
        }
        String u = url.trim();
        while (u.endsWith("/")) {
            u = u.substring(0, u.length() - 1);
        }
        if (!u.isEmpty() && !u.startsWith("http://") && !u.startsWith("https://")) {
            // a bare IP or host:port is usually a self-hosted node server without TLS; a domain gets https
            boolean plain = u.matches("^(\\d{1,3}(\\.\\d{1,3}){3}|localhost)(:\\d+)?(/.*)?$") || u.matches("^[^/]+:\\d+(/.*)?$");
            u = (plain ? "http://" : "https://") + u;
        }
        return u;
    }

    /** Random secret kept on this PC; the server only lets the first token that claimed a UUID change it. */
    private static String token() {
        if (token != null) {
            return token;
        }
        Path file = FabricLoader.getInstance().getConfigDir().resolve("dettex").resolve("sync_token.txt");
        try {
            if (Files.exists(file)) {
                String saved = Files.readString(file, StandardCharsets.UTF_8).trim();
                if (saved.length() >= 32) {
                    return token = saved;
                }
            }
            byte[] bytes = new byte[24];
            new SecureRandom().nextBytes(bytes);
            token = HexFormat.of().formatHex(bytes);
            Files.createDirectories(file.getParent());
            Files.writeString(file, token, StandardCharsets.UTF_8);
        } catch (Exception e) {
            if (token == null) {
                byte[] bytes = new byte[24];
                new SecureRandom().nextBytes(bytes);
                token = HexFormat.of().formatHex(bytes);
            }
        }
        return token;
    }
}
