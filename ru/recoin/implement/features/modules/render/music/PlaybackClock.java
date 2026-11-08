package ru.recoin.implement.features.modules.render.music;

/**
 * Smooth playback position in milliseconds. The system media API only reports whole seconds a few
 * times per second, so the clock runs on its own and is only corrected on a seek, a pause or a new track.
 */
public final class PlaybackClock {
    private static String track = "";
    private static long anchorPosMs;
    private static long anchorWallMs;
    private static boolean playing;
    // Browsers and some players (Yandex Music in a browser...) report position 0 forever: then the
    // reported position is ignored and the clock simply runs from the moment the track started.
    private static long lastRawMs = -1L;
    private static long lastRawChangeWall;
    private static boolean frozenSource;
    private static long trackStartWall;
    private static long pausedTotal;
    private static long pausedAt;

    private PlaybackClock() {
    }

    /**
     * rawMs: position exactly as the media session reports it;
     * measuredMs: the same, advanced by the time since it was read.
     */
    public static void sync(String trackKey, long rawMs, long measuredMs, boolean nowPlaying) {
        long now = System.currentTimeMillis();
        if (!trackKey.equals(track)) {
            track = trackKey;
            anchorPosMs = Math.max(0L, measuredMs);
            anchorWallMs = now;
            playing = nowPlaying;
            lastRawMs = rawMs;
            lastRawChangeWall = now;
            frozenSource = false;
            trackStartWall = now - Math.max(0L, measuredMs);
            pausedTotal = 0L;
            pausedAt = nowPlaying ? 0L : now;
            return;
        }
        if (nowPlaying != playing) {
            if (!nowPlaying) {
                pausedAt = now;
            } else if (pausedAt != 0L) {
                pausedTotal += now - pausedAt;
                pausedAt = 0L;
            }
        }
        if (rawMs != lastRawMs) {
            lastRawMs = rawMs;
            lastRawChangeWall = now;
            frozenSource = false;
        } else if (nowPlaying && playing && now - lastRawChangeWall > 2500L && !frozenSource) {
            frozenSource = true;   // playing, but the reported position does not move
            anchorPosMs = Math.max(0L, now - trackStartWall - pausedTotal);   // time since the track started
            anchorWallMs = now;
        }
        if (frozenSource) {
            if (nowPlaying != playing) {
                anchorPosMs = PlaybackClock.positionMs();   // pause / resume: keep our own position
                anchorWallMs = now;
                playing = nowPlaying;
            }
            return;
        }
        long predicted = PlaybackClock.positionMs();
        if (nowPlaying != playing || Math.abs(predicted - measuredMs) > 1200L) {
            anchorPosMs = Math.max(0L, measuredMs);
            anchorWallMs = now;
            playing = nowPlaying;
        }
    }

    /** Old signature, kept for callers that have no raw position. */
    public static void sync(String trackKey, long measuredMs, boolean nowPlaying) {
        PlaybackClock.sync(trackKey, measuredMs, measuredMs, nowPlaying);
    }

    public static long positionMs() {
        return playing ? anchorPosMs + (System.currentTimeMillis() - anchorWallMs) : anchorPosMs;
    }

    public static boolean isPlaying() {
        return playing;
    }

    /** True when the player does not report a moving position and our own clock is used. */
    public static boolean isEstimated() {
        return frozenSource;
    }
}
