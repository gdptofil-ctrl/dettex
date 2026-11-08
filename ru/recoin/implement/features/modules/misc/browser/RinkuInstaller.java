package ru.recoin.implement.features.modules.misc.browser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.function.Consumer;
import ru.recoin.common.util.logger.LoggerUtil;

/**
 * Puts the Rinku mod (Chromium for Minecraft, LGPL, by Keksuccino) into the game's mods folder when
 * it is missing: asks Modrinth for the newest Fabric 1.21.11 file, checks its SHA-512 and saves it.
 * Rinku loads on the next game start. The client jar built with Gradle already carries Rinku inside,
 * so this is only a fallback for builds without it.
 */
public final class RinkuInstaller {
    private static final String API = "https://api.modrinth.com/v2/project/rinku/version?loaders=%s&game_versions=%s";
    private static final String MC = "1.21.11";
    private static volatile boolean running;
    private static volatile Path installed;

    private RinkuInstaller() {
    }

    /** Already downloaded during this session (waiting for a restart). */
    public static boolean pendingRestart() {
        return installed != null;
    }

    public static boolean running() {
        return running;
    }

    public static synchronized void install(Consumer<String> status) {
        if (running || installed != null) return;
        running = true;
        Thread t = new Thread(() -> {
            try {
                Path mods = net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir().resolve("mods");
                Files.createDirectories(mods);
                try (var list = Files.list(mods)) {
                    Path existing = list.filter(p -> p.getFileName().toString().toLowerCase().startsWith("rinku") && p.toString().endsWith(".jar")).findFirst().orElse(null);
                    if (existing != null) {
                        installed = existing;
                        status.accept("Rinku уже лежит в папке mods (" + existing.getFileName() + "). Перезапусти игру.");
                        return;
                    }
                }
                status.accept("Скачиваю Rinku (Chromium для Minecraft) с Modrinth...");
                String url = String.format(API, URLEncoder.encode("[\"fabric\"]", StandardCharsets.UTF_8), URLEncoder.encode("[\"" + MC + "\"]", StandardCharsets.UTF_8));
                String body;
                try (var in = RinkuInstaller.open(url).getInputStream()) {
                    body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                }
                JsonArray versions = JsonParser.parseString(body).getAsJsonArray();
                if (versions.isEmpty()) throw new IllegalStateException("на Modrinth нет Rinku для Fabric " + MC);
                JsonObject file = RinkuInstaller.primaryFile(versions.get(0).getAsJsonObject());
                String name = file.get("filename").getAsString();
                String sha512 = file.getAsJsonObject("hashes").get("sha512").getAsString();
                Path tmp = mods.resolve(name + ".part");
                try (var in = RinkuInstaller.open(file.get("url").getAsString()).getInputStream()) {
                    Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
                }
                String got = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-512").digest(Files.readAllBytes(tmp)));
                if (!got.equalsIgnoreCase(sha512)) {
                    Files.deleteIfExists(tmp);
                    throw new IllegalStateException("файл повреждён (SHA-512 не совпал)");
                }
                Path target = mods.resolve(name);
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
                installed = target;
                status.accept("Rinku установлен (" + name + "). Перезапусти игру, и браузер заработает. При первом запуске он докачает Chromium (~200 МБ).");
            } catch (Throwable e) {
                LoggerUtil.info("[Browser] Rinku install failed: " + e);
                status.accept("Не получилось скачать Rinku: " + e.getMessage() + ". Скачай вручную: modrinth.com/mod/rinku (Fabric " + MC + ") и положи в папку mods.");
            } finally {
                running = false;
            }
        }, "Dettex-Rinku-Installer");
        t.setDaemon(true);
        t.start();
    }

    /** Goes through BrowserNet's proxy choice (system proxy / VPN, manual HTTP or SOCKS, direct). */
    private static java.net.HttpURLConnection open(String url) throws java.io.IOException {
        java.net.HttpURLConnection c = (java.net.HttpURLConnection)URI.create(url).toURL().openConnection();
        c.setRequestProperty("User-Agent", "DettexClient/1.1 (browser module)");
        c.setConnectTimeout(15_000);
        c.setReadTimeout(180_000);
        c.setInstanceFollowRedirects(true);
        int code = c.getResponseCode();
        if (code / 100 == 3) {   // http -> https redirects are not followed automatically
            String loc = c.getHeaderField("Location");
            c.disconnect();
            if (loc != null) return RinkuInstaller.open(URI.create(url).resolve(loc).toString());
        }
        if (code != 200) throw new IllegalStateException("HTTP " + code + " (" + URI.create(url).getHost() + ")");
        return c;
    }

    private static JsonObject primaryFile(JsonObject version) {
        JsonArray files = version.getAsJsonArray("files");
        for (JsonElement f : files) {
            JsonObject o = f.getAsJsonObject();
            if (o.has("primary") && o.get("primary").getAsBoolean()) return o;
        }
        return files.get(0).getAsJsonObject();
    }
}
