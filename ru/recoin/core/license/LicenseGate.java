package ru.recoin.core.license;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Locale;
import ru.recoin.common.util.logger.LoggerUtil;

/**
 * Online licence for DettexClient. The server ({@code sync-server/license.js}) hands out a licence
 * signed with Ed25519 and bound to this computer (HWID) and Minecraft account. The client holds only
 * the public key (baked into the jar at build time, {@code assets/recoin/license_pub.txt}), so a
 * valid licence cannot be forged without the private key, which never leaves the server.
 *
 * <p>Protection turns on only once a public key is embedded (run the licence setup in the deploy
 * script). Without it the client runs unlocked, so development builds are not bricked. With it, a
 * valid signed licence is required; a cached licence keeps working offline for a few days.
 */
public final class LicenseGate {
    private static final LicenseGate INSTANCE = new LicenseGate();
    private static final String PUB_RESOURCE = "/assets/recoin/license_pub.txt";
    private static final long OFFLINE_GRACE_MS = 5L * 24 * 3600 * 1000;   // cached licence works offline for 5 days
    private static final long RECHECK_MS = 30L * 60 * 1000;               // re-check with the server every 30 min

    private volatile boolean protectedBuild;   // a public key is embedded
    private volatile PublicKey publicKey;
    private volatile License license;          // current valid licence, or null
    private volatile String reason = "лицензия ещё не проверена";
    private volatile boolean checking;
    private volatile long lastCheck;
    private String hwid;
    private boolean warned;

    public record License(String role, long expires, String[] features, long issued) {
        boolean has(String feature) {
            if (this.features == null) return true;   // no feature list = everything
            for (String f : this.features) {
                if (f.equalsIgnoreCase("all") || f.equalsIgnoreCase(feature)) return true;
            }
            return false;
        }
    }

    private LicenseGate() {
        try {
            String b64 = LicenseGate.readResource(PUB_RESOURCE);
            if (b64 != null && !b64.isBlank()) {
                byte[] der = Base64.getDecoder().decode(b64.trim());
                this.publicKey = KeyFactory.getInstance("Ed25519").generatePublic(new X509EncodedKeySpec(der));
                this.protectedBuild = true;
            }
        } catch (Throwable e) {
            LoggerUtil.warn("[License] public key not loaded: " + e);
        }
    }

    public static LicenseGate get() {
        return INSTANCE;
    }

    /** Protection is active for this build (a public key is embedded). */
    public boolean enabled() {
        return this.protectedBuild;
    }

    /** May the client enable its features right now? Unprotected builds always may. */
    public boolean ok() {
        if (!this.protectedBuild) return true;
        License l = this.license;
        return l != null && l.expires > System.currentTimeMillis();
    }

    public boolean allows(String feature) {
        if (!this.protectedBuild) return true;
        License l = this.license;
        return l != null && l.expires > System.currentTimeMillis() && l.has(feature);
    }

    public License license() {
        return this.license;
    }

    public String reason() {
        return this.reason;
    }

    public String role() {
        License l = this.license;
        return l != null ? l.role : null;
    }

    public long expires() {
        License l = this.license;
        return l != null ? l.expires : 0L;
    }

    // ------------------------------------------------------------------ lifecycle

    /** Loads the cached licence and starts background checking. Call once at client start. */
    public void start() {
        if (!this.protectedBuild) {
            this.reason = "сборка без защиты (нет публичного ключа)";
            return;
        }
        this.loadCache();
        Thread t = new Thread(this::loop, "Dettex-License");
        t.setDaemon(true);
        t.start();
    }

    private void loop() {
        while (true) {
            try {
                this.check();
            } catch (Throwable e) {
                LoggerUtil.warn("[License] check failed: " + e);
            }
            try {
                Thread.sleep(RECHECK_MS);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    /** One server check; keeps the cached licence when the server is unreachable. */
    public synchronized void check() {
        if (!this.protectedBuild || this.checking) return;
        String server = LicenseGate.server();
        if (server.isEmpty()) {
            // protected build but no server set: fall back to the cache, within its grace window
            if (this.license == null) this.reason = "сервер лицензий не указан";
            return;
        }
        this.checking = true;
        try {
            JsonObject body = new JsonObject();
            body.addProperty("hwid", this.hwid());
            body.addProperty("uuid", LicenseGate.mcUuid());
            body.addProperty("name", LicenseGate.mcName());
            JsonObject r = LicenseGate.post(server, "/v1/license/check", body);
            if (r == null) {
                this.offlineFallback();
                return;
            }
            if (!r.has("token") || !r.has("sig")) {
                this.license = null;
                this.reason = r.has("reason") ? r.get("reason").getAsString() : "нет лицензии на этом аккаунте";
                this.saveCache(null);
                return;
            }
            License l = this.verify(r.get("token").getAsString(), r.get("sig").getAsString());
            if (l == null) {
                this.license = null;
                this.reason = "подпись лицензии неверна";
                return;
            }
            this.license = l;
            this.reason = "ok";
            this.lastCheck = System.currentTimeMillis();
            this.saveCache(r.get("token").getAsString() + "." + r.get("sig").getAsString());
        } finally {
            this.checking = false;
        }
    }

    private void offlineFallback() {
        License l = this.license;
        if (l == null) {
            this.reason = "нет связи с сервером лицензий и нет сохранённой лицензии";
        } else if (System.currentTimeMillis() - this.lastCheck > OFFLINE_GRACE_MS) {
            this.license = null;
            this.reason = "офлайн дольше 5 дней, нужна проверка лицензии";
        } else {
            this.reason = "офлайн, лицензия из кэша";
        }
    }

    // ------------------------------------------------------------------ verify + cache

    /** token is base64(JSON); checks the Ed25519 signature, HWID, account and expiry. */
    private License verify(String tokenB64, String sigB64) {
        try {
            byte[] tokenBytes = Base64.getUrlDecoder().decode(tokenB64);
            byte[] sig = Base64.getDecoder().decode(sigB64);
            Signature s = Signature.getInstance("Ed25519");
            s.initVerify(this.publicKey);
            s.update(tokenBytes);
            if (!s.verify(sig)) return null;
            JsonObject t = JsonParser.parseString(new String(tokenBytes, StandardCharsets.UTF_8)).getAsJsonObject();
            String hwid = t.has("hwid") ? t.get("hwid").getAsString() : "";
            if (!hwid.isEmpty() && !hwid.equals(this.hwid())) {
                this.reason = "лицензия выдана на другой компьютер";
                return null;
            }
            String uuid = t.has("uuid") ? t.get("uuid").getAsString() : "";
            if (!uuid.isEmpty() && !uuid.equalsIgnoreCase(LicenseGate.mcUuid())) {
                this.reason = "лицензия выдана на другой аккаунт";
                return null;
            }
            long expires = t.has("expires") ? t.get("expires").getAsLong() : 0L;
            if (expires > 0 && expires < System.currentTimeMillis()) {
                this.reason = "срок лицензии истёк";
                return null;
            }
            String role = t.has("role") ? t.get("role").getAsString() : "User";
            String[] features = null;
            if (t.has("features") && t.get("features").isJsonArray()) {
                var arr = t.getAsJsonArray("features");
                features = new String[arr.size()];
                for (int i = 0; i < arr.size(); i++) features[i] = arr.get(i).getAsString();
            }
            long issued = t.has("issued") ? t.get("issued").getAsLong() : 0L;
            return new License(role, expires == 0 ? Long.MAX_VALUE : expires, features, issued);
        } catch (Throwable e) {
            LoggerUtil.warn("[License] token verify error: " + e);
            return null;
        }
    }

    private Path cacheFile() {
        try {
            return net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir().resolve("config").resolve("dettex").resolve("license.bin");
        } catch (Throwable e) {
            return Path.of("config", "dettex", "license.bin");
        }
    }

    private void loadCache() {
        try {
            Path f = this.cacheFile();
            if (!Files.exists(f)) return;
            String[] parts = Files.readString(f, StandardCharsets.UTF_8).trim().split("\\|", 2);
            if (parts.length != 2) return;
            this.lastCheck = Long.parseLong(parts[0]);
            String[] ts = parts[1].split("\\.", 2);
            if (ts.length != 2) return;
            License l = this.verify(ts[0], ts[1]);
            if (l != null) {
                this.license = l;
                this.reason = "лицензия из кэша, проверяю...";
            }
        } catch (Throwable e) {
            LoggerUtil.warn("[License] cache not read: " + e);
        }
    }

    private void saveCache(String tokenDotSig) {
        try {
            Path f = this.cacheFile();
            if (tokenDotSig == null) {
                Files.deleteIfExists(f);
                return;
            }
            Files.createDirectories(f.getParent());
            Files.writeString(f, System.currentTimeMillis() + "|" + tokenDotSig, StandardCharsets.UTF_8);
        } catch (Throwable e) {
            LoggerUtil.warn("[License] cache not saved: " + e);
        }
    }

    private volatile long lastDeny;

    /** Tells the player why a module would not turn on (throttled to avoid chat spam). */
    public void denyToggle(String moduleName) {
        long now = System.currentTimeMillis();
        if (now - this.lastDeny < 4000L) return;
        this.lastDeny = now;
        String msg = "\u00a7c[Dettex] \u00a7f\u041c\u043e\u0434\u0443\u043b\u044c \u00a7e" + moduleName
                + "\u00a7f \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d: " + this.reason
                + ". \u00a77\u041f\u0440\u0438\u0432\u044f\u0436\u0438 \u043b\u0438\u0446\u0435\u043d\u0437\u0438\u044e (Telegram API / \u043f\u043e\u043a\u0443\u043f\u043a\u0430).";
        try {
            net.minecraft.class_310 mc = net.minecraft.class_310.method_1551();
            mc.execute(() -> {
                if (mc.field_1705 != null) mc.field_1705.method_1743().method_1812(net.minecraft.class_2561.method_43470(msg));
            });
        } catch (Throwable ignored) {
        }
    }

    public boolean warnOnce() {
        if (this.warned) return false;
        this.warned = true;
        return true;
    }

    // ------------------------------------------------------------------ identity + http

    /** A stable id of this computer (Windows MachineGuid + user + os), hashed. */
    public String hwid() {
        if (this.hwid != null) return this.hwid;
        StringBuilder sb = new StringBuilder();
        sb.append(System.getProperty("os.name", "")).append('|');
        sb.append(System.getProperty("user.name", "")).append('|');
        sb.append(System.getProperty("os.arch", "")).append('|');
        sb.append(LicenseGate.machineGuid());
        try {
            byte[] h = MessageDigest.getInstance("SHA-256").digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            this.hwid = Base64.getUrlEncoder().withoutPadding().encodeToString(h).substring(0, 32);
        } catch (Exception e) {
            this.hwid = Integer.toHexString(sb.toString().hashCode());
        }
        return this.hwid;
    }

    private static String machineGuid() {
        if (!System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
            return System.getenv().getOrDefault("HOSTNAME", "nohost");
        }
        try {
            Process p = new ProcessBuilder("reg", "query",
                    "HKLM\\SOFTWARE\\Microsoft\\Cryptography", "/v", "MachineGuid").redirectErrorStream(true).start();
            String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            p.waitFor();
            for (String line : out.split("\\r?\\n")) {
                String t = line.trim();
                if (t.startsWith("MachineGuid")) {
                    String[] parts = t.split("\\s+");
                    return parts[parts.length - 1];
                }
            }
        } catch (Throwable ignored) {
        }
        return System.getenv().getOrDefault("COMPUTERNAME", "nocomputer");
    }

    private static String mcUuid() {
        try {
            return net.minecraft.class_310.method_1551().method_1548().method_44717().toString();
        } catch (Throwable e) {
            return "";
        }
    }

    private static String mcName() {
        try {
            return net.minecraft.class_310.method_1551().method_1548().method_1676();
        } catch (Throwable e) {
            return "";
        }
    }

    static String server() {
        String s = "";
        try {
            var c = ru.recoin.implement.features.modules.render.cosmetics.Cosmetics.getInstance();
            if (c != null) s = c.getSyncServer();
        } catch (Throwable ignored) {
        }
        if (s == null || s.isBlank()) {
            s = ru.recoin.implement.features.modules.render.cosmetics.Cosmetics.DEFAULT_SYNC_SERVER;
        }
        return LicenseGate.normalize(s);
    }

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

    private static JsonObject post(String server, String path, JsonObject body) {
        try {
            HttpURLConnection c = (HttpURLConnection) URI.create(server + path).toURL().openConnection();
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
                return JsonParser.parseString(text).getAsJsonObject();
            }
        } catch (Throwable e) {
            return null;   // network problem: the caller keeps the cached licence
        }
    }

    private static String readResource(String path) {
        try (InputStream in = LicenseGate.class.getResourceAsStream(path)) {
            if (in == null) return null;
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Throwable e) {
            return null;
        }
    }
}
