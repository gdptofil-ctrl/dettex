package ru.recoin.implement.features.modules.misc.browser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import ru.recoin.common.util.logger.LoggerUtil;

/**
 * How the Browser reaches the internet: works with no VPN / proxy, with a VPN (it simply routes all
 * traffic, nothing to set up) and with a proxy.
 *
 * Auto:   Windows system proxy (Settings -> Network -> Proxy, also what most VPN / proxy apps set),
 *         otherwise direct. Chromium does the same by itself.
 * Manual: your proxy, e.g. "socks5://127.0.0.1:1080" or "http://user:pass@1.2.3.4:8080".
 * Direct: never use a proxy.
 *
 * Covers both Java downloads (Rinku, Chromium) and Chromium pages. Chromium reads the proxy when it
 * starts, so after changing it for Chromium restart the game once.
 */
public final class BrowserNet {
    public enum Mode {
        AUTO, MANUAL, DIRECT
    }

    private static volatile Mode mode = Mode.AUTO;
    private static volatile String manual = "";
    private static volatile boolean installed;
    private static volatile List<Proxy> systemCache;
    private static volatile long systemCacheAt;

    private BrowserNet() {
    }

    // ------------------------------------------------------------------ settings

    private static Path file() {
        try {
            return net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir().resolve("recoin").resolve("browser-proxy.txt");
        } catch (Throwable e) {
            return Path.of("recoin", "browser-proxy.txt");
        }
    }

    /** Reads the saved choice (needed before the module config is loaded: Chromium starts early). */
    public static synchronized void init() {
        try {
            Path f = BrowserNet.file();
            if (Files.exists(f)) {
                List<String> lines = Files.readAllLines(f, StandardCharsets.UTF_8);
                if (!lines.isEmpty()) mode = BrowserNet.parseMode(lines.get(0));
                if (lines.size() > 1) manual = lines.get(1).trim();
            }
        } catch (Throwable e) {
            LoggerUtil.info("[Browser] proxy settings not read: " + e);
        }
        BrowserNet.install();
    }

    public static Mode parseMode(String s) {
        String t = s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
        if (t.startsWith("man")) return Mode.MANUAL;
        if (t.startsWith("dir") || t.startsWith("off")) return Mode.DIRECT;
        return Mode.AUTO;
    }

    public static Mode mode() {
        return mode;
    }

    public static String manual() {
        return manual;
    }

    /** Called when the settings change. */
    public static synchronized void configure(Mode m, String address) {
        String a = address == null ? "" : address.trim();
        if (m == mode && a.equals(manual) && installed) return;
        mode = m;
        manual = a;
        systemCache = null;
        try {
            Path f = BrowserNet.file();
            Files.createDirectories(f.getParent());
            Files.writeString(f, m.name() + "\n" + a + "\n", StandardCharsets.UTF_8);
        } catch (Throwable e) {
            LoggerUtil.info("[Browser] proxy settings not saved: " + e);
        }
        BrowserNet.install();
    }

    // ------------------------------------------------------------------ Java side

    private static final ProxySelector SELECTOR = new ProxySelector() {
        @Override
        public List<Proxy> select(URI uri) {
            String host = uri == null || uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
            if (host.isEmpty() || host.equals("localhost") || host.startsWith("127.") || host.equals("[::1]") || host.equals("::1")) {
                return List.of(Proxy.NO_PROXY);
            }
            return switch (mode) {
                case DIRECT -> List.of(Proxy.NO_PROXY);
                case MANUAL -> {
                    Proxy p = BrowserNet.toProxy(BrowserNet.parse(manual));
                    yield List.of(p != null ? p : Proxy.NO_PROXY);
                }
                default -> BrowserNet.systemProxies(host);
            };
        }

        @Override
        public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
            systemCache = null;   // re-read the system proxy next time
        }
    };

    private static synchronized void install() {
        try {
            ProxySelector.setDefault(SELECTOR);
            Parsed p = BrowserNet.parse(manual);
            if (mode == Mode.MANUAL && p != null && p.user != null) {
                // Java blocks Basic auth for HTTPS through a proxy unless this is cleared
                System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
                System.setProperty("jdk.http.auth.proxying.disabledSchemes", "");
                Authenticator.setDefault(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        Parsed now = BrowserNet.parse(manual);
                        if (this.getRequestorType() == RequestorType.PROXY && now != null && now.user != null) {
                            return new PasswordAuthentication(now.user, now.pass.toCharArray());
                        }
                        return null;
                    }
                });
            }
            installed = true;
        } catch (Throwable e) {
            LoggerUtil.info("[Browser] proxy not installed: " + e);
        }
    }

    /** A proxy address split into parts. scheme: http or socks5 / socks4. */
    public record Parsed(String scheme, String host, int port, String user, String pass) {
        boolean socks() {
            return this.scheme.startsWith("socks");
        }
    }

    public static Parsed parse(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        String scheme = "http";
        int i = t.indexOf("://");
        if (i > 0) {
            scheme = t.substring(0, i).toLowerCase(Locale.ROOT);
            t = t.substring(i + 3);
        }
        if (scheme.equals("https")) scheme = "http";
        if (scheme.equals("socks")) scheme = "socks5";
        if (t.endsWith("/")) t = t.substring(0, t.length() - 1);
        String user = null;
        String pass = null;
        int at = t.lastIndexOf('@');
        if (at > 0) {
            String cred = t.substring(0, at);
            t = t.substring(at + 1);
            int c = cred.indexOf(':');
            user = c >= 0 ? cred.substring(0, c) : cred;
            pass = c >= 0 ? cred.substring(c + 1) : "";
        }
        int colon = t.lastIndexOf(':');
        if (colon <= 0) return null;
        String host = t.substring(0, colon).replace("[", "").replace("]", "");
        int port;
        try {
            port = Integer.parseInt(t.substring(colon + 1).trim());
        } catch (NumberFormatException e) {
            return null;
        }
        if (host.isBlank() || port <= 0 || port > 65535) return null;
        return new Parsed(scheme, host, port, user, pass);
    }

    static Proxy toProxy(Parsed p) {
        if (p == null) return null;
        return new Proxy(p.socks() ? Proxy.Type.SOCKS : Proxy.Type.HTTP, InetSocketAddress.createUnresolved(p.host, p.port));
    }

    private static List<Proxy> systemProxies(String host) {
        long now = System.currentTimeMillis();
        List<Proxy> c = systemCache;
        if (c == null || now - systemCacheAt > 30_000L) {
            c = BrowserNet.readSystemProxies();
            systemCache = c;
            systemCacheAt = now;
        }
        return c;
    }

    /** Windows proxy (registry, what Settings -> Proxy writes), then HTTPS_PROXY / ALL_PROXY. */
    static List<Proxy> readSystemProxies() {
        List<Proxy> out = new ArrayList<>();
        try {
            if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
                String key = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings";
                String enable = BrowserNet.regValue(key, "ProxyEnable");
                if (enable != null && (enable.endsWith("1") || enable.equalsIgnoreCase("0x1"))) {
                    Proxy p = BrowserNet.fromWindowsServer(BrowserNet.regValue(key, "ProxyServer"));
                    if (p != null) out.add(p);
                }
            }
        } catch (Throwable e) {
            LoggerUtil.info("[Browser] system proxy not read: " + e);
        }
        if (out.isEmpty()) {
            for (String env : new String[]{"HTTPS_PROXY", "https_proxy", "ALL_PROXY", "all_proxy", "HTTP_PROXY", "http_proxy"}) {
                Proxy p = BrowserNet.toProxy(BrowserNet.parse(System.getenv(env)));
                if (p != null) {
                    out.add(p);
                    break;
                }
            }
        }
        out.add(Proxy.NO_PROXY);   // if the proxy fails, try direct (a VPN routes it anyway)
        return List.copyOf(out);
    }

    /** "host:port" or "http=h:p;https=h:p;socks=h:p". */
    static Proxy fromWindowsServer(String v) {
        if (v == null || v.isBlank()) return null;
        String s = v.trim();
        if (!s.contains("=")) {
            return BrowserNet.toProxy(BrowserNet.parse(s.contains("://") ? s : "http://" + s));
        }
        String http = null;
        String socks = null;
        for (String part : s.split(";")) {
            int eq = part.indexOf('=');
            if (eq <= 0) continue;
            String k = part.substring(0, eq).trim().toLowerCase(Locale.ROOT);
            String val = part.substring(eq + 1).trim();
            if (k.equals("https")) http = val;
            else if (k.equals("http") && http == null) http = val;
            else if (k.equals("socks")) socks = val;
        }
        if (http != null) return BrowserNet.toProxy(BrowserNet.parse("http://" + http));
        if (socks != null) return BrowserNet.toProxy(BrowserNet.parse("socks4://" + socks));
        return null;
    }

    private static String regValue(String key, String name) throws IOException, InterruptedException {
        Process p = new ProcessBuilder("reg", "query", key, "/v", name).redirectErrorStream(true).start();
        String result = null;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                String t = line.trim();
                if (t.startsWith(name)) {
                    String[] parts = t.split("\\s+", 3);
                    if (parts.length == 3) result = parts[2].trim();
                }
            }
        }
        p.waitFor();
        return result;
    }

    // ------------------------------------------------------------------ Chromium side

    /** Extra Chromium start switches for the chosen mode (added to Rinku's own). */
    public static String[] cefSwitches(String[] base) {
        List<String> list = new ArrayList<>();
        if (base != null) {
            for (String s : base) {
                if (!s.startsWith("--proxy-server") && !s.equals("--no-proxy-server")) list.add(s);
            }
        }
        switch (mode) {
            case DIRECT -> list.add("--no-proxy-server");
            case MANUAL -> {
                Parsed p = BrowserNet.parse(manual);
                if (p != null) list.add("--proxy-server=" + p.scheme + "://" + (p.host.contains(":") ? "[" + p.host + "]" : p.host) + ":" + p.port);
            }
            default -> {
                // Chromium follows the Windows proxy / VPN by itself
            }
        }
        return list.toArray(String[]::new);
    }

    /** Short line for chat about how pages go out. */
    public static String describe() {
        return switch (mode) {
            case DIRECT -> "напрямую, без прокси";
            case MANUAL -> {
                Parsed p = BrowserNet.parse(manual);
                yield p == null ? "прокси вручную, но адрес не понят (пример: socks5://127.0.0.1:1080)" : "через прокси " + p.scheme + "://" + p.host + ":" + p.port;
            }
            default -> {
                List<Proxy> l = BrowserNet.readSystemProxies();
                yield l.size() > 1 ? "через системный прокси " + l.get(0).address() : "напрямую или через VPN (системного прокси нет)";
            }
        };
    }
}
