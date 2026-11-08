package ru.recoin.implement.features.modules.misc.browser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import ru.recoin.common.util.logger.LoggerUtil;

/**
 * Programs you can put on a screen: windows that are open right now, shortcuts on the desktop and
 * in the Start menu, and programs (.exe) in Downloads. Launching waits for the program's window.
 */
public final class AppCatalog {
    public record Entry(String name, String where, Path path, long hwnd) {
        public boolean isWindow() {
            return this.hwnd != 0L;
        }
    }

    private static final String[] EXT = {".lnk", ".exe", ".url", ".appref-ms"};

    private AppCatalog() {
    }

    public static boolean supported() {
        return Win32.available();
    }

    public static List<Entry> openWindows() {
        List<Entry> out = new ArrayList<>();
        for (Win32.WindowInfo w : Win32.windows()) {
            out.add(new Entry(w.title(), "окно", null, w.hwnd()));
        }
        return out;
    }

    /** Desktop (yours and Public, also OneDrive), Start menu, Downloads. */
    public static List<Entry> programs() {
        Map<String, Entry> out = new LinkedHashMap<>();
        String home = System.getProperty("user.home", "");
        String pub = System.getenv("PUBLIC");
        String appData = System.getenv("APPDATA");
        String programData = System.getenv("ProgramData");
        AppCatalog.scan(out, Path.of(home, "Desktop"), "рабочий стол", 2, false);
        AppCatalog.scan(out, Path.of(home, "OneDrive", "Desktop"), "рабочий стол", 2, false);
        AppCatalog.scan(out, Path.of(home, "OneDrive", "Рабочий стол"), "рабочий стол", 2, false);
        if (pub != null) AppCatalog.scan(out, Path.of(pub, "Desktop"), "рабочий стол", 2, false);
        AppCatalog.scan(out, Path.of(home, "Downloads"), "загрузки", 2, true);
        if (appData != null) AppCatalog.scan(out, Path.of(appData, "Microsoft", "Windows", "Start Menu", "Programs"), "пуск", 4, false);
        if (programData != null) AppCatalog.scan(out, Path.of(programData, "Microsoft", "Windows", "Start Menu", "Programs"), "пуск", 4, false);
        List<Entry> list = new ArrayList<>(out.values());
        list.sort((a, b) -> {
            int pa = a.where().equals("рабочий стол") ? 0 : a.where().equals("загрузки") ? 1 : 2;
            int pb = b.where().equals("рабочий стол") ? 0 : b.where().equals("загрузки") ? 1 : 2;
            return pa != pb ? Integer.compare(pa, pb) : a.name().compareToIgnoreCase(b.name());
        });
        return list;
    }

    private static void scan(Map<String, Entry> out, Path dir, String where, int depth, boolean exeOnly) {
        if (!Files.isDirectory(dir)) return;
        try (Stream<Path> s = Files.walk(dir, depth)) {
            s.filter(Files::isRegularFile).forEach(p -> {
                String file = p.getFileName().toString();
                String lower = file.toLowerCase(Locale.ROOT);
                String ext = null;
                for (String e : EXT) {
                    if (lower.endsWith(e)) ext = e;
                }
                if (ext == null || exeOnly && !ext.equals(".exe")) return;
                String name = file.substring(0, file.length() - ext.length());
                String ln = name.toLowerCase(Locale.ROOT);
                if (ln.contains("uninstall") || ln.contains("удален") || ln.contains("удалить") || ln.startsWith("unins")) return;
                out.putIfAbsent(ln, new Entry(name, where, p, 0L));
            });
        } catch (Throwable e) {
            LoggerUtil.info("[Browser] cannot list " + dir + ": " + e);
        }
    }

    public static List<Entry> filter(List<Entry> list, String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) return list;
        List<Entry> out = new ArrayList<>();
        for (Entry e : list) {
            if (e.name().toLowerCase(Locale.ROOT).contains(q)) out.add(e);
        }
        return out;
    }

    /**
     * Starts the program and calls found with its window (on a background thread), or with null when
     * no new window showed up within 40 seconds.
     */
    public static void launch(Entry e, Consumer<Win32.WindowInfo> found) {
        Thread t = new Thread(() -> {
            try {
                Set<Long> before = new HashSet<>();
                for (Win32.WindowInfo w : Win32.windows()) before.add(w.hwnd());
                // explorer opens .lnk / .url / .exe the same way a double click does
                new ProcessBuilder("explorer.exe", e.path().toString()).start();
                long until = System.currentTimeMillis() + 40_000L;
                String key = e.name().toLowerCase(Locale.ROOT);
                while (System.currentTimeMillis() < until) {
                    Thread.sleep(500L);
                    Win32.WindowInfo best = null;
                    for (Win32.WindowInfo w : Win32.windows()) {
                        if (!before.contains(w.hwnd())) {
                            int[] size = Win32.clientSize(Win32.handle(w.hwnd()));
                            if (size[0] < 120 || size[1] < 80) continue;   // splash or tiny helper
                            best = w;
                            break;
                        }
                    }
                    if (best != null) {
                        Thread.sleep(800L);   // let it draw its first picture
                        found.accept(best);
                        return;
                    }
                }
                // the program may have been running already: take its existing window by name
                for (Win32.WindowInfo w : Win32.windows()) {
                    String title = w.title().toLowerCase(Locale.ROOT);
                    if (title.contains(key) || key.contains(title)) {
                        found.accept(w);
                        return;
                    }
                }
                found.accept(null);
            } catch (Throwable ex) {
                LoggerUtil.info("[Browser] launch failed: " + ex);
                found.accept(null);
            }
        }, "Dettex-App-Launch");
        t.setDaemon(true);
        t.start();
    }
}
