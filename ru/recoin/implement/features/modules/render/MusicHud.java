package ru.recoin.implement.features.modules.render;

import dev.redstones.mediaplayerinfo.MediaInfo;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.logger.LoggerUtil;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.draggables.MediaPlayer;
import ru.recoin.implement.features.modules.render.music.LyricsService;
import ru.recoin.implement.features.modules.render.music.PlaybackClock;
import ru.recoin.implement.features.modules.render.music.WorldLyrics;
import ru.recoin.implement.features.modules.render.music.WorldLyricLines;
import ru.recoin.implement.events.render.DrawEvent;

/**
 * Music HUD: while music plays on the PC the watermark shows the track instead of name/FPS/ping,
 * and the lyrics of the song appear on screen in sync (lyrics from lrclib.net).
 */
public final class MusicHud
extends Module {
    public final BooleanSetting replaceWatermark = new BooleanSetting("Replace Watermark", "While music plays, the watermark shows the track").setValue(true);
    public final SelectSetting watermarkText = new SelectSetting("Watermark Text", "What the watermark shows").value("Artist - Title", "Title", "Title + Time").selected("Artist - Title");
    public final BooleanSetting lyrics = new BooleanSetting("Lyrics On Screen", "Song lyrics appear on screen in time with the music").setValue(true);
    public final SelectSetting animation = new SelectSetting("Lyrics Animation", "How the words appear").value("Karaoke", "Pop", "Typewriter", "Fade").selected("Pop");
    public final BooleanSetting nextLine = new BooleanSetting("Show Next Line", "Show the upcoming line under the current one").setValue(true);
    public final BooleanSetting background = new BooleanSetting("Lyrics Background", "Dark blurred plate behind the lyrics").setValue(true);
    public final ValueSetting size = new ValueSetting("Lyrics Size", "Text size of the lyrics").setValue(18.0f).range(10, 30);
    public final BooleanSetting worldLyrics = new BooleanSetting("Lyrics In World", "Sung words appear in the air around you and float away").setValue(true);
    public final SelectSetting worldStyle = new SelectSetting("World Style", "Lines: whole lyric lines appear in front of you in time with the song; Words: single words fly around").value("Lines", "Words").selected("Lines");
    public final ValueSetting worldDistance = new ValueSetting("World Distance", "How far from you the words appear (blocks)").setValue(4.0f).range(2.0f, 10.0f).step(0.5f);
    public final ValueSetting worldLife = new ValueSetting("World Word Time", "How long a word stays in the world (seconds)").setValue(3.0f).range(1.0f, 8.0f).step(0.5f);
    public final ValueSetting worldSize = new ValueSetting("World Text Size", "Size of the words in the world").setValue(24.0f).range(10, 48);
    public final ValueSetting offset = new ValueSetting("Lyrics Offset", "Shift the lyrics in ms if they are early or late").setValue(0.0f).range(-3000, 3000);

    public MusicHud() {
        super("MusicHud", "Music HUD", ModuleCategory.RENDER);
        this.setup(this.replaceWatermark, this.watermarkText, this.lyrics, this.animation, this.nextLine, this.background, this.size, this.worldLyrics, this.worldStyle, this.worldDistance, this.worldLife, this.worldSize, this.offset);
    }

    public static MusicHud getInstance() {
        return Instance.get(MusicHud.class);
    }

    /** MediaPlayer reads the system media session while this is true, even if its HUD element is hidden. */
    public static boolean needsMedia() {
        MusicHud m = MusicHud.getInstance();
        return m != null && m.isState();
    }

    /** Current track, or null when nothing is known. */
    public static MediaInfo media() {
        MediaPlayer player = MediaPlayer.getInstance();
        return player == null || !player.hasFreshMedia() ? null : player.currentMedia();
    }

    public static boolean musicPlaying() {
        MediaInfo info = MusicHud.media();
        return info != null && info.getPlaying() && !(info.getTitle().isBlank() && info.getArtist().isBlank());
    }

    public boolean replacesWatermark() {
        return this.isState() && this.replaceWatermark.isValue() && MusicHud.musicPlaying();
    }

    public boolean lyricsVisible() {
        return this.isState() && this.lyrics.isValue();
    }

    /** Playback position used for the lyrics, including the user offset. */
    public long lyricsPositionMs() {
        return PlaybackClock.positionMs() + (long)this.offset.getValue();
    }

    public String watermarkLine(MediaInfo info) {
        String title = info.getTitle() == null ? "" : info.getTitle().trim();
        String artist = info.getArtist() == null ? "" : info.getArtist().trim();
        if (this.watermarkText.isSelected("Title") || this.watermarkText.isSelected("Title + Time") || artist.isEmpty()) {
            return title.isEmpty() ? artist : title;
        }
        return title.isEmpty() ? artist : artist + " — " + title;
    }

    @EventHandler
    public void onDraw(DrawEvent event) {
        if (!this.worldLyrics.isValue()) {
            return;
        }
        boolean lines = this.worldStyle.isSelected("Lines");
        if (MusicHud.musicPlaying()) {
            if (lines) {
                WorldLyricLines.update(this.lyricsPositionMs(), this.worldDistance.getValue(), this.watermarkLine(MusicHud.media()));
            } else {
                WorldLyrics.update(this.lyricsPositionMs(), (long)(this.worldLife.getValue() * 1000.0f), this.worldDistance.getValue(), this.watermarkLine(MusicHud.media()));
            }
        }
        Hud hud = Hud.getInstance();
        int accent = hud != null ? hud.themeColor() : -1;
        if (lines) {
            WorldLyrics.clear();
            WorldLyricLines.render(event.getDrawContext(), accent, this.worldSize.getValue(), this.lyricsPositionMs());
        } else {
            WorldLyricLines.clear();
            WorldLyrics.render(event.getDrawContext(), accent, this.worldSize.getValue());
        }
    }

    @Override
    public void deactivate() {
        WorldLyrics.clear();
        WorldLyricLines.clear();
        super.deactivate();
    }

    private Boolean loggedPlaying;

    @EventHandler
    public void onTick(TickEvent event) {
        MediaPlayer player = MediaPlayer.getInstance();
        MediaInfo info = MusicHud.media();
        boolean playing = MusicHud.musicPlaying();
        if (this.loggedPlaying == null || this.loggedPlaying != playing) {
            this.loggedPlaying = playing;
            LoggerUtil.info("[MusicHud] " + (playing ? "music playing: " + info.getArtist() + " - " + info.getTitle() : info == null ? "no media session found (is music playing in Spotify/browser/Yandex Music?)" : "music paused"));
        }
        if (player == null || info == null) {
            return;
        }
        String trackKey = info.getArtist() + "\n" + info.getTitle();
        long measured = info.getPosition() * 1000L + (info.getPlaying() ? System.currentTimeMillis() - player.mediaFetchedAt() : 0L);
        PlaybackClock.sync(trackKey, info.getPosition() * 1000L, measured, info.getPlaying());
        LyricsService.update(info.getArtist(), info.getTitle(), info.getDuration());
    }
}
