package ru.recoin.implement.features.modules.misc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.class_124;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_2568;
import net.minecraft.class_310;
import net.minecraft.class_5250;
import ru.recoin.common.util.logger.LoggerUtil;
import ru.recoin.implement.features.modules.render.cosmetics.Cosmetics;

/**
 * Links the client to the player's Telegram through the Dettex bot (@dettexclient_bot):
 * the server hands out a key (DTX-XXXX-XXXX), the player sends it to the bot, the bot confirms,
 * and the client sees the confirmation. The bot token stays on the server (sync-server, tg.js);
 * the client only keeps its own random secret in config/dettex/telegram.json.
 */
public final class TelegramLink {
    public static final String DEFAULT_BOT = "dettexclient_bot";

    private static volatile Thread worker;
    private static volatile boolean stop;
    private static volatile String secret;
    private static volatile String username = "";

    private TelegramLink() {
    }

    public static boolean linked() {
        return secret != null;
    }

    /** Still waiting for a key to be confirmed (or checking the saved link). */
    public static boolean busy() {
        return worker != null;
    }

    public static String username() {
        return username;
    }

    // ------------------------------------------------------------------ chat

    private static void chat(class_2561 text) {
        class_310 mc = class_310.method_1551();
        mc.execute(() -> {
            if (mc.field_1705 != null) mc.field_1705.method_1743().method_1812(text);
        });
    }

    private static void say(String text) {
        chat(class_2561.method_43470("§b[Telegram] §f" + text));
    }

    private static void showKey(String key, String bot) {
        String b = bot == null || bot.isBlank() ? DEFAULT_BOT : bot;
        say("Твой ключ (действует 15 минут):");
        class_5250 keyText = class_2561.method_43470("   " + key + "   ").method_27694(s -> s.method_10977(class_124.field_1060).method_10982(true)
                .method_10958(new class_2558.class_10606(key))
                .method_10949(new class_2568.class_10613(class_2561.method_43470("Нажми, чтобы скопировать"))));
        chat(class_2561.method_43470("").method_10852(keyText).method_10852(class_2561.method_43470("§7[нажми - скопировать]")));
        chat(class_2561.method_43470("§fОтправь его боту ").method_10852(class_2561.method_43470("@" + b).method_27694(s -> s.method_10977(class_124.field_1075).method_30938(true)
                .method_10958(new class_2558.class_10608(URI.create("https://t.me/" + b)))))
                .method_10852(class_2561.method_43470("§f или ")).method_10852(class_2561.method_43470("[открыть бота с ключом]").method_27694(s -> s.method_10977(class_124.field_1054).method_30938(true)
                .method_10958(new class_2558.class_10608(URI.create("https://t.me/" + b + "?start=" + key)))
                .method_10949(new class_2568.class_10613(class_2561.method_43470("Откроет Telegram, останется нажать «Старт»"))))));
    }

    // ------------------------------------------------------------------ server

    static String server() {
        String s = "";
        try {
            Cosmetics c = Cosmetics.getInstance();
            if (c != null) s = c.getSyncServer();
        } catch (Throwable ignored) {
        }
        if (s == null || s.isBlank()) s = Cosmetics.DEFAULT_SYNC_SERVER;
        return TelegramLink.normalize(s);
    }

    /** Same rules as the cosmetics sync: no trailing slash; a bare IP / host:port gets http, a domain https. */
    static String normalize(String url) {
        if (url == null) return "";
        String u = url.trim();
        while (u.endsWith("/")) u = u.substring(0, u.length() - 1);
        if (!u.isEmpty() && !u.startsWith("http://") && !u.startsWith("https://")) {
            boolean plain = u.matches("^(\\d{1,3}(\\.\\d{1,3}){3}|localhost)(:\\d+)?(/.*)?$") || u.matches("^[^/]+:\\d+(/.*)?$");
            u = (plain ? "http://" : "https://") + u;
        }
        return u;
    }

    private static JsonObject post(String server, String path, JsonObject body) throws Exception {
        HttpURLConnection c = (HttpURLConnection)URI.create(server + path).toURL().openConnection();
        c.setRequestMethod("POST");
        c.setConnectTimeout(10_000);
        c.setReadTimeout(20_000);
        c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        c.setRequestProperty("User-Agent", "DettexClient");
        try (OutputStream o = c.getOutputStream()) {
            o.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }
        int code = c.getResponseCode();
        try (InputStream in = code >= 400 ? c.getErrorStream() : c.getInputStream()) {
            String text = in == null ? "{}" : new String(in.readAllBytes(), StandardCharsets.UTF_8);
            JsonObject o = JsonParser.parseString(text).getAsJsonObject();
            if (code >= 400) throw new IllegalStateException(o.has("error") ? o.get("error").getAsString() : "HTTP " + code);
            return o;
        }
    }

    private static Path file() {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir().resolve("config").resolve("dettex").resolve("telegram.json");
    }

    private static JsonObject load() {
        try {
            Path f = TelegramLink.file();
            if (Files.exists(f)) return JsonParser.parseString(Files.readString(f, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static void save(String server, String sec, String user) {
        try {
            JsonObject o = new JsonObject();
            o.addProperty("server", server);
            o.addProperty("secret", sec);
            o.addProperty("username", user);
            Files.createDirectories(TelegramLink.file().getParent());
            Files.writeString(TelegramLink.file(), o.toString(), StandardCharsets.UTF_8);
        } catch (Throwable e) {
            LoggerUtil.info("[Telegram] link not saved: " + e);
        }
    }

    // ------------------------------------------------------------------ flow

    /** Module turned on: confirm the saved link, or get a key and wait for the bot to confirm it. */
    public static synchronized void start(String playerName) {
        if (worker != null) return;
        stop = false;
        Thread t = new Thread(() -> {
            try {
                TelegramLink.run(playerName);
            } finally {
                worker = null;
            }
        }, "Dettex-Telegram-Link");
        t.setDaemon(true);
        worker = t;
        t.start();
    }

    public static void stop() {
        stop = true;
    }

    private static void run(String playerName) {
        String server = TelegramLink.server();
        if (server.isEmpty()) {
            say("§cСервер Dettex не настроен. Запусти §fsync-server/deploy.bat§c (он же подключает бота) и пересобери клиент, или впиши адрес в Cosmetics -> Sync -> Sync Server.");
            return;
        }
        JsonObject saved = TelegramLink.load();
        if (saved != null && saved.has("secret") && server.equals(saved.has("server") ? saved.get("server").getAsString() : "")) {
            try {
                JsonObject b = new JsonObject();
                b.addProperty("secret", saved.get("secret").getAsString());
                JsonObject me = TelegramLink.post(server, "/v1/tg/me", b);
                if (me.get("linked").getAsBoolean()) {
                    secret = saved.get("secret").getAsString();
                    username = me.has("username") ? me.get("username").getAsString() : "";
                    say("§a✔ Telegram привязан" + (username.isEmpty() ? "" : " к @" + username) + ". §7Новый ключ: включи «New Key» в настройках модуля.");
                    return;
                }
            } catch (Throwable e) {
                say("§cНет связи с сервером Dettex: " + e.getMessage());
                return;
            }
        }
        secret = null;
        try {
            JsonObject b = new JsonObject();
            b.addProperty("name", playerName);
            JsonObject r = TelegramLink.post(server, "/v1/tg/new", b);
            String key = r.get("key").getAsString();
            String sec = r.get("secret").getAsString();
            String bot = r.has("bot") ? r.get("bot").getAsString() : "";
            TelegramLink.showKey(key, bot);
            long until = System.currentTimeMillis() + 15 * 60_000L;
            JsonObject q = new JsonObject();
            q.addProperty("key", key);
            q.addProperty("secret", sec);
            while (!stop && System.currentTimeMillis() < until) {
                Thread.sleep(3000L);
                JsonObject st;
                try {
                    st = TelegramLink.post(server, "/v1/tg/status", q);
                } catch (Exception netError) {
                    continue;   // a short network hiccup: keep waiting
                }
                String status = st.get("status").getAsString();
                if (status.equals("confirmed")) {
                    username = st.has("username") ? st.get("username").getAsString() : "";
                    TelegramLink.save(server, sec, username);
                    secret = sec;
                    say("§a✔ Ключ подтверждён! Telegram привязан" + (username.isEmpty() ? "" : " к @" + username) + ". Всё ок.");
                    return;
                }
                if (status.equals("expired")) break;
            }
            if (!stop) say("§eКлюч устарел. Выключи и снова включи модуль, чтобы получить новый.");
        } catch (Throwable e) {
            String m = String.valueOf(e.getMessage());
            say(m.contains("BOT_TOKEN") ? "§cНа сервере не указан токен бота. Запусти sync-server/deploy.bat и вставь токен." : "§cНе удалось получить ключ: " + m);
        }
    }

    /** Forget the link (here and on the server); the next start gives a new key. */
    public static void unlink() {
        JsonObject saved = TelegramLink.load();
        String s = secret != null ? secret : saved != null && saved.has("secret") ? saved.get("secret").getAsString() : null;
        secret = null;
        username = "";
        try {
            Files.deleteIfExists(TelegramLink.file());
        } catch (Throwable ignored) {
        }
        if (s == null) return;
        String server = TelegramLink.server();
        Thread t = new Thread(() -> {
            try {
                JsonObject b = new JsonObject();
                b.addProperty("secret", s);
                TelegramLink.post(server, "/v1/tg/unlink", b);
            } catch (Throwable ignored) {
            }
        }, "Dettex-Telegram-Unlink");
        t.setDaemon(true);
        t.start();
    }

    /** A notification into the linked Telegram chat (AutoSell reports and the like). */
    public static void send(String text) {
        String s = secret;
        if (s == null || text == null || text.isBlank()) return;
        String server = TelegramLink.server();
        Thread t = new Thread(() -> {
            try {
                JsonObject b = new JsonObject();
                b.addProperty("secret", s);
                b.addProperty("text", text);
                TelegramLink.post(server, "/v1/tg/send", b);
            } catch (Throwable e) {
                LoggerUtil.info("[Telegram] send failed: " + e);
            }
        }, "Dettex-Telegram-Send");
        t.setDaemon(true);
        t.start();
    }
}
