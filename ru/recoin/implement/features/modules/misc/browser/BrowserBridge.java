package ru.recoin.implement.features.modules.misc.browser;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_2960;
import ru.recoin.common.util.logger.LoggerUtil;

/**
 * Talks to the Rinku mod (Chromium in Minecraft, LGPL, by Keksuccino) by reflection, so the client
 * builds and runs without it. The Gradle build nests Rinku into the client jar; otherwise
 * RinkuInstaller downloads it into the mods folder. Rinku downloads Chromium (~200 MB) once.
 */
public final class BrowserBridge {
    private final Object browser;
    private final Map<String, Method> methods = new HashMap<>();

    private BrowserBridge(Object browser) {
        this.browser = browser;
    }

    public static boolean installed() {
        try {
            return net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("rinku");
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static Class<?> rinku() throws ClassNotFoundException {
        return Class.forName("de.keksuccino.rinku.Rinku");
    }

    /** Chromium is downloaded and started (it takes a while on the very first launch). */
    public static boolean ready() {
        try {
            return (Boolean)BrowserBridge.rinku().getMethod("isInitialized").invoke(null);
        } catch (Throwable ignored) {
            return false;
        }
    }

    // ------------------------------------------------------------------ Chromium download state

    private static Object listener() throws Exception {
        return Class.forName("de.keksuccino.rinku.binarydownload.RinkuDownloadListener").getField("INSTANCE").get(null);
    }

    private static boolean listenerFlag(String getter) {
        try {
            Object l = BrowserBridge.listener();
            return (Boolean)l.getClass().getMethod(getter).invoke(l);
        } catch (Throwable ignored) {
            return false;
        }
    }

    /** Rinku's own Chromium download (at game start) failed, e.g. no internet at that moment. */
    public static boolean downloadFailed() {
        return !BrowserBridge.ready() && BrowserBridge.listenerFlag("isFailed");
    }

    /** Chromium is downloaded but not started yet. */
    public static boolean downloadDone() {
        return BrowserBridge.listenerFlag("isDone") && !BrowserBridge.listenerFlag("isFailed");
    }

    /** Chromium download progress 0..1. */
    public static float progress() {
        try {
            Object l = BrowserBridge.listener();
            return (Float)l.getClass().getMethod("getProgress").invoke(l);
        } catch (Throwable ignored) {
            return 0.0f;
        }
    }

    private static volatile boolean retrying;

    public static boolean retrying() {
        return retrying;
    }

    /**
     * Downloads Chromium again (the same steps Rinku does once at game start) and starts it, without
     * restarting the game. Calls done with the error text, or with null when it worked.
     */
    public static synchronized void retryDownload(java.util.function.Consumer<String> done) {
        if (retrying) return;
        retrying = true;
        Thread t = new Thread(() -> {
            String error = null;
            try {
                // the "failed" flag stays on while downloading, otherwise Rinku swaps every menu you
                // open for its own loading screen
                Object l = BrowserBridge.listener();
                Object settings = BrowserBridge.rinku().getMethod("getSettings").invoke(null);
                Class<?> platformClass = Class.forName("de.keksuccino.rinku.OSPlatform");
                Object platform = platformClass.getMethod("getPlatform").invoke(null);
                String mirror = (String)settings.getClass().getMethod("getDownloadMirror").invoke(settings);
                Class<?> dlClass = Class.forName("de.keksuccino.rinku.binarydownload.RinkuDownloader");
                Object downloader;
                try {
                    Object policy = settings.getClass().getMethod("createDownloadPolicy").invoke(settings);
                    downloader = dlClass.getConstructor(String.class, platformClass, policy.getClass()).newInstance(mirror, platform, policy);
                } catch (NoSuchMethodException old) {
                    downloader = dlClass.getConstructor(String.class, platformClass).newInstance(mirror, platform);
                }
                boolean skip = (Boolean)settings.getClass().getMethod("isSkipDownload").invoke(settings);
                Object result = dlClass.getMethod("installOrUpdate", boolean.class).invoke(downloader, skip);
                java.nio.file.Path dir = (java.nio.file.Path)result.getClass().getMethod("installationDirectory").invoke(result);
                System.setProperty("jcef.path", dir.toRealPath().toString());
                l.getClass().getMethod("setDone", boolean.class).invoke(l, true);
                l.getClass().getMethod("setFailed", boolean.class).invoke(l, false);
                // start Chromium on the render thread (what Rinku does after its own download)
                net.minecraft.class_310.method_1551().execute(() -> {
                    try {
                        if ((Boolean)BrowserBridge.rinku().getMethod("isInitializationAllowed").invoke(null)) {
                            BrowserBridge.rinku().getMethod("initialize").invoke(null);
                        }
                    } catch (Throwable e) {
                        LoggerUtil.info("[Browser] Chromium start failed: " + e);
                    }
                });
            } catch (Throwable e) {
                Throwable c = e instanceof java.lang.reflect.InvocationTargetException && e.getCause() != null ? e.getCause() : e;
                Throwable root = c;
                while (root.getCause() != null && root.getCause() != root) root = root.getCause();
                LoggerUtil.info("[Browser] Chromium download failed: " + c + " / " + root);
                boolean noNet = false;
                for (Throwable x = c; x != null && !noNet; x = x.getCause() == x ? null : x.getCause()) {
                    String m = String.valueOf(x.getMessage());
                    noNet = x instanceof java.net.UnknownHostException || x instanceof java.net.ConnectException || m.contains("UnknownHost") || m.contains("ConnectException") || m.contains("SocketTimeout");
                }
                error = noNet
                        ? "нет интернета или github.com не открывается"
                        : root.getClass().getSimpleName() + (root.getMessage() == null ? "" : ": " + root.getMessage());
                try {
                    Object l = BrowserBridge.listener();
                    l.getClass().getMethod("setFailed", boolean.class).invoke(l, true);
                } catch (Throwable ignored) {
                }
            } finally {
                retrying = false;
            }
            done.accept(error);
        }, "Dettex-Chromium-Download");
        t.setDaemon(true);
        t.start();
    }

    public static BrowserBridge create(String url, int width, int height) {
        try {
            Object b;
            try {
                b = BrowserBridge.rinku().getMethod("createBrowser", String.class, boolean.class, int.class, int.class).invoke(null, url, false, width, height);
            } catch (NoSuchMethodException old) {
                b = BrowserBridge.rinku().getMethod("createBrowser", String.class, boolean.class).invoke(null, url, false);
            }
            if (b == null) return null;
            BrowserBridge bridge = new BrowserBridge(b);
            bridge.resize(width, height);
            return bridge;
        } catch (Throwable e) {
            LoggerUtil.info("[Browser] could not create a browser: " + e);
            return null;
        }
    }

    private Object call(String name, Class<?>[] types, Object... args) {
        try {
            String key = name + types.length;
            Method m = this.methods.get(key);
            if (m == null) {
                m = this.browser.getClass().getMethod(name, types);
                this.methods.put(key, m);
            }
            return m.invoke(this.browser, args);
        } catch (Throwable e) {
            return null;
        }
    }

    private static final Class<?>[] NONE = new Class<?>[0];

    public boolean textureReady() {
        return Boolean.TRUE.equals(this.call("isTextureReady", NONE));
    }

    /** The browser picture as a Minecraft texture (null until the first frame). */
    public class_2960 texture() {
        Object id = this.call("getTextureIdentifier", NONE);
        return id instanceof class_2960 c ? c : null;
    }

    public void resize(int w, int h) {
        this.call("resize", new Class<?>[]{int.class, int.class}, Math.max(16, w), Math.max(16, h));
    }

    public void load(String url) {
        this.call("loadURL", new Class<?>[]{String.class}, url);
    }

    public String url() {
        Object u = this.call("getURL", NONE);
        return u == null ? "" : u.toString();
    }

    public boolean loading() {
        return Boolean.TRUE.equals(this.call("isLoading", NONE));
    }

    public boolean canBack() {
        return Boolean.TRUE.equals(this.call("canGoBack", NONE));
    }

    public boolean canForward() {
        return Boolean.TRUE.equals(this.call("canGoForward", NONE));
    }

    public void back() {
        this.call("goBack", NONE);
    }

    public void forward() {
        this.call("goForward", NONE);
    }

    public void reload() {
        this.call("reload", NONE);
    }

    public void focus(boolean f) {
        this.call("setFocus", new Class<?>[]{boolean.class}, f);
    }

    public void mouseMove(int x, int y) {
        this.call("sendMouseMove", new Class<?>[]{int.class, int.class}, x, y);
    }

    public void mousePress(int x, int y, int button) {
        this.call("sendMousePress", new Class<?>[]{int.class, int.class, int.class}, x, y, button);
    }

    public void mouseRelease(int x, int y, int button) {
        this.call("sendMouseRelease", new Class<?>[]{int.class, int.class, int.class}, x, y, button);
    }

    public void wheel(int x, int y, double amount) {
        this.call("sendMouseWheel", new Class<?>[]{int.class, int.class, double.class, int.class}, x, y, amount, 0);
    }

    public void keyPress(int key, long scancode, int mods) {
        this.call("sendKeyPress", new Class<?>[]{int.class, long.class, int.class}, key, scancode, mods);
    }

    public void keyRelease(int key, long scancode, int mods) {
        this.call("sendKeyRelease", new Class<?>[]{int.class, long.class, int.class}, key, scancode, mods);
    }

    public void keyTyped(char c, int mods) {
        this.call("sendKeyTyped", new Class<?>[]{char.class, int.class}, c, mods);
    }

    public void close() {
        this.call("close", NONE);
    }

    /** The Rinku browser object itself (to tell which page a console message came from). */
    public Object handle() {
        return this.browser;
    }

    /** Runs JavaScript in the page's main frame. */
    public void runJs(String code) {
        this.call("executeJavaScript", new Class<?>[]{String.class, String.class, int.class}, code, "dettex://focus", 0);
    }

    /** Turns what the player typed into a URL: an address stays an address, anything else becomes a search. */
    public static String toUrl(String input, String searchUrl) {
        String t = input.trim();
        if (t.isEmpty()) return searchUrl.replace("%s", "");
        if (t.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*")) return t;
        if (!t.contains(" ") && t.matches("^[\\w.-]+\\.[a-zA-Z]{2,}(/.*)?$")) return "https://" + t;
        return searchUrl.replace("%s", java.net.URLEncoder.encode(t, java.nio.charset.StandardCharsets.UTF_8));
    }
}
