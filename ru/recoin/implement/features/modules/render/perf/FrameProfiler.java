package ru.recoin.implement.features.modules.render.perf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Measures how much time each client module spends in its event handlers (render, tick, packets...),
 * so the heaviest ones can be found and turned off. Costs nothing while disabled.
 */
public final class FrameProfiler {
    public static volatile boolean enabled;
    private static final Map<String, long[]> TIMES = new HashMap<>();   // module or section -> {nanos}
    private static long windowStart;
    private static List<String> report = List.of();
    private static long frames;
    private static long framesInWindow;

    private FrameProfiler() {
    }

    /** Called by the event manager around every handler call (render thread and others). */
    public static synchronized void add(Object source, long nanos) {
        TIMES.computeIfAbsent(source.getClass().getSimpleName(), k -> new long[1])[0] += nanos;
    }

    /** Time of a part of the client's frame that is not an event handler (HUD drawing, blur...). */
    public static synchronized void section(String name, long nanos) {
        TIMES.computeIfAbsent("[" + name + "]", k -> new long[1])[0] += nanos;
    }

    private static int windows;

    public static synchronized void frame() {
        ++framesInWindow;
        long now = System.nanoTime();
        if (windowStart == 0L) {
            windowStart = now;
            return;
        }
        long window = now - windowStart;
        if (window < 2_000_000_000L) {
            return;
        }
        List<Map.Entry<String, long[]>> list = new ArrayList<>(TIMES.entrySet());
        list.sort((a, b) -> Long.compare(b.getValue()[0], a.getValue()[0]));
        double fps = framesInWindow * 1e9 / window;
        List<String> lines = new ArrayList<>();
        double total = 0.0;
        for (Map.Entry<String, long[]> e : list) {
            if (e.getKey().startsWith("[")) continue;   // sections already contain the handlers they call
            total += e.getValue()[0];
        }
        double frameMs = fps > 0 ? 1000.0 / fps : 0.0;
        lines.add(String.format("Client modules: %.2f ms per frame (frame %.2f ms, %.0f FPS)", total / 1e6 / Math.max(1, framesInWindow), frameMs, fps));
        for (int i = 0; i < Math.min(8, list.size()); ++i) {
            double ms = list.get(i).getValue()[0] / 1e6 / Math.max(1, framesInWindow);
            if (ms < 0.005) break;
            double share = frameMs > 0 ? ms / frameMs * 100.0 : 0.0;
            lines.add(String.format("%-22s %.3f ms  (%.0f%% of frame)", list.get(i).getKey(), ms, share));
        }
        report = List.copyOf(lines);
        if (++windows % 5 == 0) {
            // every 10 seconds into latest.log as well, so the numbers can be sent / compared later
            for (String line : report) {
                ru.recoin.common.util.logger.LoggerUtil.info("[Perf] " + line);
            }
        }
        TIMES.clear();
        windowStart = now;
        framesInWindow = 0L;
    }

    public static synchronized List<String> report() {
        return report;
    }

    public static synchronized void reset() {
        TIMES.clear();
        windowStart = 0L;
        framesInWindow = 0L;
        report = List.of();
    }
}
