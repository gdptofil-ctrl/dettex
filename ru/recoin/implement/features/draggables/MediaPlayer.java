/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1011
 *  net.minecraft.class_1043
 *  net.minecraft.class_1044
 *  net.minecraft.class_2960
 *  net.minecraft.class_332
 *  net.minecraft.class_3532
 *  net.minecraft.class_4587
 *  org.lwjgl.BufferUtils
 */
package ru.recoin.implement.features.draggables;

import dev.redstones.mediaplayerinfo.IMediaSession;
import dev.redstones.mediaplayerinfo.MediaInfo;
import dev.redstones.mediaplayerinfo.MediaPlayerInfoKt;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import org.lwjgl.BufferUtils;
import ru.recoin.api.feature.draggable.AbstractDraggable;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.other.StopWatch;
import ru.recoin.common.util.other.StringUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.common.util.render.ScissorManager;
import ru.recoin.core.Main;
import ru.recoin.implement.features.modules.render.Hud;
import ru.recoin.implement.features.modules.render.NewHud;

public class MediaPlayer
extends AbstractDraggable {
    private static final float CONTROL_SIZE = 6.0f;
    private static final float CONTROL_GAP = 9.5f;
    private static final float VISUALIZER_WIDTH = 22.0f;
    private static final int HUD_ARTWORK_SIZE = 20;
    private static final int MENU_ARTWORK_SIZE = 22;
    private static final int MENU_WIDTH = 150;
    private static final int MENU_HEIGHT = 50;
    private static final float ARTWORK_OFFSET_X = 4.0f;
    private static final float ARTWORK_OFFSET_Y = 4.0f;
    private static final float LOWER_ROW_LEFT_GAP = 4.0f;
    private static final float LOWER_ROW_RIGHT_GAP = 3.0f;
    private static final String DEFAULT_TITLE = "\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u0442\u0440\u0435\u043a\u0430";
    private static final String DEFAULT_ARTIST = "\u0410\u0440\u0442\u0438\u0441\u0442";
    private static final class_2960 PREV_TEXTURE = class_2960.method_60654((String)"textures/prev.png");
    private static final class_2960 PLAY_TEXTURE = class_2960.method_60654((String)"textures/play.png");
    private static final class_2960 PAUSE_TEXTURE = class_2960.method_60654((String)"textures/pause.png");
    private static final class_2960 NEXT_TEXTURE = class_2960.method_60654((String)"textures/next.png");
    private static final class_2960 MENU_ARTWORK = class_2960.method_60654((String)"textures/recoin_logo.png");
    private volatile MediaInfo mediaInfo = new MediaInfo("\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u0442\u0440\u0435\u043a\u0430", "\u0410\u0440\u0442\u0438\u0441\u0442", new byte[0], 43L, 150L, false);
    private volatile long mediaFetchedAtMs = System.currentTimeMillis();
    private volatile class_2960 artwork;
    private volatile boolean artworkReady;
    private final StopWatch lastMedia = new StopWatch();
    private volatile int lastArtworkHash;
    private int emptyArtworkTicks;
    private final StopWatch lastNativeCall = new StopWatch();
    private float widthDuration;
    private boolean mainMenuMode;
    private boolean menuPositionLoaded;
    private int menuX;
    private int menuY;
    private float menuDragX;
    private float menuDragY;
    private volatile int nativeFailCount;
    private volatile boolean nativeDisabled;
    private final AtomicBoolean nativeRequestPending = new AtomicBoolean();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(r -> {
        Thread thread2 = new Thread(r, "Recoin-MediaPlayer");
        thread2.setDaemon(true);
        thread2.setUncaughtExceptionHandler((t, e) -> {
            ++this.nativeFailCount;
            if (this.nativeFailCount >= 3) {
                this.nativeDisabled = true;
            }
        });
        return thread2;
    });

    public static MediaPlayer getInstance() {
        return Instance.getDraggable(MediaPlayer.class);
    }

    public MediaPlayer() {
        super("Media Player", 10, 400, 100, 40, true);
    }

    @Override
    public boolean visible() {
        return this.mainMenuMode || !this.lastMedia.finished(2000.0) || PlayerIntersectionUtil.isChat(MediaPlayer.mc.field_1755);
    }

    @Override
    public void tick() {
        this.syncNewHud();
        if (this.nativeDisabled) {
            return;
        }
        if (!this.lastNativeCall.finished(750.0) || !this.nativeRequestPending.compareAndSet(false, true)) {
            return;
        }
        if (Hud.getInstance() != null && Hud.getInstance().isState() && Hud.getInstance().interfaceSettings.isSelected("Media Player")) {
            this.lastNativeCall.reset();
            this.executorService.submit(() -> {
                try {
                    List<IMediaSession> sessions = MediaPlayerInfoKt.getSystemMediaPlayerInfo().getMediaSessions();
                    MediaInfo fallback = null;
                    for (IMediaSession session : sessions) {
                        MediaInfo info = session.getMedia();
                        if (info == null || info.getTitle().isBlank() && info.getArtist().isBlank()) continue;
                        if (fallback == null) {
                            fallback = info;
                        }
                        if (!info.getPlaying()) continue;
                        fallback = info;
                        break;
                    }
                    if (fallback != null) {
                        this.mediaInfo = fallback;
                        this.mediaFetchedAtMs = System.currentTimeMillis();
                        this.updateArtwork(fallback);
                        this.lastMedia.reset();
                    }
                    this.nativeFailCount = 0;
                }
                catch (Throwable ignored) {
                    ++this.nativeFailCount;
                    if (this.nativeFailCount >= 10) {
                        this.nativeDisabled = true;
                    }
                }
                finally {
                    this.nativeRequestPending.set(false);
                }
            });
        } else {
            this.nativeRequestPending.set(false);
        }
    }

    // Прокидывает текущий трек в NewHud (стиль "Hud 2").
    private void syncNewHud() {
        MediaInfo info = this.mediaInfo;
        if (info == null) {
            return;
        }
        int duration = (int) info.getDuration();
        int position = class_3532.method_15340((int) ((int) info.getPosition() + (info.getPlaying() ? (int) ((System.currentTimeMillis() - this.mediaFetchedAtMs) / 1000L) : 0)), 0, Math.max(0, duration));
        NewHud.mediaTitle = info.getTitle() == null ? "" : info.getTitle();
        NewHud.mediaArtist = info.getArtist() == null ? "" : info.getArtist();
        NewHud.mediaPos = position;
        NewHud.mediaLen = duration;
    }

    @Override
    public void drawDraggable(class_332 context) {
        if (!this.mainMenuMode && Hud.getInstance().isHud2()) {
            this.drawHud2(context);
            return;
        }
        if (!this.mainMenuMode) {
            this.setWidth(100);
            this.setHeight(40);
        }
        MediaInfo displayedMedia = this.mediaInfo;
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        ScissorManager scissor = Main.getInstance().getScissorManager();
        FontRenderer big = Fonts.getSize(this.mainMenuMode ? 13 : 15, Fonts.Type.INTER_BOLD);
        FontRenderer mini = Fonts.getSize(this.mainMenuMode ? 10 : 12, Fonts.Type.INTER_BOLD);
        float contentX = this.getContentX();
        float contentRight = this.getContentRight();
        float progressX = this.getProgressX();
        float progressY = this.displayY() + (float)this.panelHeight() - 7.0f;
        float progressWidth = this.getProgressWidth();
        int duration = (int)displayedMedia.getDuration();
        int position = class_3532.method_15340((int)((int)displayedMedia.getPosition() + (displayedMedia.getPlaying() ? (int)((System.currentTimeMillis() - this.mediaFetchedAtMs) / 1000L) : 0)), (int)0, (int)duration);
        String timeDuration = StringUtil.getDuration(duration);
        this.widthDuration = duration <= 0 ? 1.0f : class_3532.method_15363((float)MathUtil.interpolateSmooth(1.0, this.widthDuration, (float)Math.round((float)position / (float)duration * progressWidth)), (float)1.0f, (float)progressWidth);
        blur.render(ShapeProperties.create(matrix, this.displayX(), this.displayY(), this.panelWidth(), this.panelHeight()).thickness(2.25f).softness(2.0f).round(5.0f).quality(50.0f).thickness(0.0f).outlineColor(0).color(ColorUtil.DARK_BLACK).build());
        float textWidth = Math.max(28.0f, contentRight - contentX - 22.0f - 1.0f);
        scissor.push(matrix.method_23760().method_23761(), contentX, this.displayY(), textWidth, this.panelHeight());
        big.drawStringWithScroll(matrix, displayedMedia.getTitle(), contentX, this.displayY() + (this.mainMenuMode ? 6.5f : 7.0f), textWidth, ColorUtil.getText());
        mini.drawStringWithScroll(matrix, displayedMedia.getArtist(), contentX, this.displayY() + (this.mainMenuMode ? 16.0f : 15.5f), textWidth, ColorUtil.getText(this.mainMenuMode ? 0.72f : 0.75f));
        scissor.pop();
        this.drawAudioVisualizer(matrix, displayedMedia.getPlaying());
        int artworkSize = this.artworkSize();
        rectangle.render(ShapeProperties.create(matrix, this.displayX() + 4.0f, this.displayY() + 4.0f, artworkSize, artworkSize).round(5.0f).color(ColorUtil.getRect(1.0f)).build());
        if (this.artworkReady && this.artwork != null) {
            Render2DUtil.drawRoundedTexture(context, this.artwork, this.displayX() + 4.0f, this.displayY() + 4.0f, artworkSize, 4.0f, -1);
        } else if (this.mainMenuMode) {
            float logoSize = 15.0f;
            float logoInset = ((float)artworkSize - logoSize) / 2.0f;
            Render2DUtil.drawRoundedTexture(context, MENU_ARTWORK, this.displayX() + 4.0f + logoInset, this.displayY() + 4.0f + logoInset, logoSize, 3.0f, -1);
        }
        float lowerTextY = this.displayY() + (this.mainMenuMode ? (float)this.panelHeight() - 16.0f : 27.5f);
        mini.drawString(matrix, StringUtil.getDuration(position), progressX, lowerTextY, ColorUtil.getText());
        mini.drawString(matrix, timeDuration, progressX + progressWidth - mini.getStringWidth(timeDuration), lowerTextY, ColorUtil.getText());
        rectangle.render(ShapeProperties.create(matrix, progressX, progressY, progressWidth, 2.0).round(1.5f).color(ColorUtil.getRectDarker(0.75f)).build());
        rectangle.render(ShapeProperties.create(matrix, progressX, progressY, this.widthDuration, 2.0).softness(4.0f).round(1.5f).color(ColorUtil.roundClientColor(0.2f)).build());
        rectangle.render(ShapeProperties.create(matrix, progressX, progressY, this.widthDuration, 2.0).round(1.5f).color(ColorUtil.roundClientColor(1.0f)).build());
        float controlsCenterX = this.getControlsCenterX();
        float controlsY = this.getControlsY();
        class_2960 centerControl = displayedMedia.getPlaying() ? PAUSE_TEXTURE : PLAY_TEXTURE;
        float centerControlSize = displayedMedia.getPlaying() ? 5.0f : 6.0f;
        float centerControlX = controlsCenterX + (6.0f - centerControlSize) / 2.0f;
        float centerControlY = controlsY + (6.0f - centerControlSize) / 2.0f;
        Render2DUtil.drawTexture(context, PREV_TEXTURE, controlsCenterX - 9.5f, controlsY, 6);
        Render2DUtil.drawTexture(context, centerControl, centerControlX, centerControlY, (int)centerControlSize);
        Render2DUtil.drawTexture(context, NEXT_TEXTURE, controlsCenterX + 9.5f, controlsY, 6);
    }

    private void drawHud2(class_332 context) {
        this.setWidth(150);
        this.setHeight(42);
        MediaInfo displayedMedia = this.mediaInfo;
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        ScissorManager scissor = Main.getInstance().getScissorManager();
        FontRenderer titleFont = Fonts.getSize(14, Fonts.Type.INTER_BOLD);
        FontRenderer subFont = Fonts.getSize(11, Fonts.Type.INTER_BOLD);
        float x = this.displayX();
        float y = this.displayY();
        float w = (float)this.panelWidth();
        float h = (float)this.panelHeight();
        int duration = (int)displayedMedia.getDuration();
        int position = class_3532.method_15340((int)((int)displayedMedia.getPosition() + (displayedMedia.getPlaying() ? (int)((System.currentTimeMillis() - this.mediaFetchedAtMs) / 1000L) : 0)), (int)0, (int)Math.max(0, duration));
        blur.render(ShapeProperties.create(matrix, x, y, w, h).round(7.0f).softness(4.0f).thickness(0.0f).quality(50.0f).outlineColor(0).color(ColorUtil.getColor(10, 12, 18, 0.86f)).build());
        rectangle.render(ShapeProperties.create(matrix, x, y, w, h).round(7.0f).softness(1.0f).thickness(1.0f).quality(50.0f).outlineColor(ColorUtil.getOutline(0.35f)).color(151587081).build());
        float art = 30.0f;
        float artX = x + 6.0f;
        float artY = y + 6.0f;
        rectangle.render(ShapeProperties.create(matrix, artX, artY, art, art).round(6.0f).color(ColorUtil.getRect(1.0f)).build());
        if (this.artworkReady && this.artwork != null) {
            Render2DUtil.drawRoundedTexture(context, this.artwork, artX, artY, art, 6.0f, -1);
        }
        rectangle.render(ShapeProperties.create(matrix, artX, artY, art, art).round(6.0f).softness(1.0f).thickness(1.0f).quality(50.0f).outlineColor(ColorUtil.getOutline(0.45f)).color(0).build());
        float textX = artX + art + 8.0f;
        float textWidth = Math.max(24.0f, x + w - 8.0f - textX);
        scissor.push(matrix.method_23760().method_23761(), textX, y, textWidth, h);
        titleFont.drawStringWithScroll(matrix, displayedMedia.getTitle(), textX, y + 6.5f, textWidth, ColorUtil.getText());
        subFont.drawStringWithScroll(matrix, displayedMedia.getArtist(), textX, y + 17.0f, textWidth, ColorUtil.getText(0.55f));
        scissor.pop();
        String timeText = StringUtil.getDuration(position) + " / " + StringUtil.getDuration(duration);
        subFont.drawString(matrix, timeText, textX, y + 27.0f, ColorUtil.getText(0.55f));
        float barX = artX;
        float barW = x + w - 8.0f - barX;
        float barY = y + h - 4.5f;
        float fill = duration <= 0 ? 0.0f : class_3532.method_15363((float)position / (float)duration, 0.0f, 1.0f) * barW;
        rectangle.render(ShapeProperties.create(matrix, barX, barY, barW, 2.0f).round(1.0f).color(ColorUtil.getOutline(0.28f)).build());
        if (fill > 0.5f) {
            rectangle.render(ShapeProperties.create(matrix, barX, barY, fill, 2.0f).round(1.0f).color(ColorUtil.getClientColor()).build());
        }
    }

    private void drawAudioVisualizer(class_4587 matrix, boolean mediaPlaying) {
        float startX = this.displayX() + (float)this.panelWidth() - 17.0f;
        float centerY = this.displayY() + (this.mainMenuMode ? 12.5f : 11.5f);
        float phase = (float)(System.currentTimeMillis() % 100000L) / 180.0f;
        float playing = mediaPlaying ? 1.0f : 0.0f;
        int color = ColorUtil.getText(0.58f);
        this.drawVisualizerDot(matrix, startX, centerY, color);
        for (int i = 0; i < 4; ++i) {
            float base = i == 1 || i == 2 ? 6.2f : 4.1f;
            float wave = (float)Math.sin(phase + (float)i * 0.95f) * 1.6f * playing;
            float height = class_3532.method_15363((float)(base + wave), (float)2.0f, (float)8.0f);
            float x = startX + 3.1f + (float)i * 2.35f;
            rectangle.render(ShapeProperties.create(matrix, x, centerY - height / 2.0f, 1.35f, height).round(0.68f).color(color).build());
        }
        this.drawVisualizerDot(matrix, startX + 12.6f, centerY, color);
    }

    private void drawVisualizerDot(class_4587 matrix, float x, float centerY, int color) {
        rectangle.render(ShapeProperties.create(matrix, x, centerY - 0.75f, 1.5, 1.5).round(0.75f).color(color).build());
    }

    private void updateArtwork(MediaInfo info) {
        byte[] artworkPng = info.getArtworkPng();
        if (artworkPng != null && artworkPng.length > 0) {
            this.emptyArtworkTicks = 0;
            int artworkHash = Arrays.hashCode(artworkPng);
            if ((artworkHash != this.lastArtworkHash || !this.artworkReady) && this.registerArtwork(class_2960.method_60655((String)"recoin", (String)("dynamic/media_" + Integer.toUnsignedString(artworkHash, 16))), artworkPng)) {
                this.lastArtworkHash = artworkHash;
            }
            return;
        }
        BufferedImage image = info.getArtwork();
        if (image != null && image.getWidth() > 1 && image.getHeight() > 1) {
            this.emptyArtworkTicks = 0;
            int artworkHash = this.getArtworkHash(image, info);
            if ((artworkHash != this.lastArtworkHash || !this.artworkReady) && this.registerArtwork(class_2960.method_60655((String)"recoin", (String)("dynamic/media_" + Integer.toUnsignedString(artworkHash, 16))), image)) {
                this.lastArtworkHash = artworkHash;
            }
            return;
        }
        if (++this.emptyArtworkTicks > 2 && this.lastArtworkHash != 0) {
            this.lastArtworkHash = 0;
            mc.execute(() -> {
                if (this.lastArtworkHash != 0) {
                    return;
                }
                if (this.artwork != null) {
                    mc.method_1531().method_4615(this.artwork);
                }
                this.artwork = null;
                this.artworkReady = false;
            });
        }
    }

    private boolean registerArtwork(class_2960 id, BufferedImage image) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            if (!ImageIO.write((RenderedImage)image, "png", output)) {
                return false;
            }
            return this.registerArtwork(id, output.toByteArray());
        }
        catch (Exception ignored) {
            return false;
        }
    }

    private boolean registerArtwork(class_2960 id, byte[] bytes) {
        try {
            ByteArrayOutputStream output;
            BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(bytes));
            byte[] encoded = bytes;
            if (decoded != null && decoded.getWidth() > 1 && decoded.getHeight() > 1 && ImageIO.write((RenderedImage)decoded, "png", output = new ByteArrayOutputStream())) {
                encoded = output.toByteArray();
            }
            ByteBuffer data = BufferUtils.createByteBuffer((int)encoded.length).put(encoded);
            data.flip();
            class_1011 nativeImage = class_1011.method_4324((ByteBuffer)data);
            mc.execute(() -> {
                class_1043 texture = null;
                try {
                    texture = new class_1043(() -> "recoin_media_artwork", nativeImage);
                    class_2960 previous = this.artwork;
                    mc.method_1531().method_4616(id, (class_1044)texture);
                    this.artwork = id;
                    this.artworkReady = true;
                    if (previous != null && !previous.equals((Object)id)) {
                        mc.method_1531().method_4615(previous);
                    }
                }
                catch (Exception ignored) {
                    if (texture != null) {
                        try {
                            texture.close();
                        }
                        catch (Exception exception) {}
                    }
                    try {
                        nativeImage.close();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            });
            return true;
        }
        catch (Exception ignored) {
            return false;
        }
    }

    private int getArtworkHash(BufferedImage image, MediaInfo info) {
        int hash = 31 * image.getWidth() + image.getHeight();
        int stepX = Math.max(1, image.getWidth() / 8);
        int stepY = Math.max(1, image.getHeight() / 8);
        for (int y = 0; y < image.getHeight(); y += stepY) {
            for (int x = 0; x < image.getWidth(); x += stepX) {
                hash = 31 * hash + image.getRGB(x, y);
            }
        }
        hash = 31 * hash + info.getTitle().hashCode();
        hash = 31 * hash + info.getArtist().hashCode();
        return hash;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.mainMenuMode && button == 0) {
            if (this.isMenuHovered(mouseX, mouseY)) {
                this.setDragging(true);
                this.menuDragX = (float)this.menuX - (float)mouseX;
                this.menuDragY = (float)this.menuY - (float)mouseY;
                return true;
            }
            return false;
        }
        if (button == 0) {
            if (!this.mainMenuMode && Hud.getInstance().isHud2() && this.isHovered(mouseX, mouseY)) {
                this.sendMediaKey("MediaPlayPause");
                return true;
            }
            if (this.isControlHovered(mouseX, mouseY, this.getControlsCenterX() - 9.5f, this.getControlsY(), 6.0f)) {
                this.sendMediaKey("MediaPreviousTrack");
                return true;
            }
            if (this.isControlHovered(mouseX, mouseY, this.getControlsCenterX(), this.getControlsY(), 6.0f)) {
                this.sendMediaKey("MediaPlayPause");
                return true;
            }
            if (this.isControlHovered(mouseX, mouseY, this.getControlsCenterX() + 9.5f, this.getControlsY(), 6.0f)) {
                this.sendMediaKey("MediaNextTrack");
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void sendMediaKey(String key) {
        this.executorService.submit(() -> {
            try {
                String script = "$code = @'\nusing System; using System.Runtime.InteropServices;\npublic class MK { [DllImport(\"user32.dll\")] public static extern void keybd_event(byte k, byte s, uint f, UIntPtr e); }\n'@;\nAdd-Type -TypeDefinition $code -Language CSharp;\n" + (key.equals("MediaPlayPause") ? "$k = 0xB3;" : (key.equals("MediaNextTrack") ? "$k = 0xB0;" : "$k = 0xB1;")) + "\n[MK]::keybd_event($k, 0, 0, [UIntPtr]::Zero);\n[MK]::keybd_event($k, 0, 2, [UIntPtr]::Zero);";
                new ProcessBuilder("powershell", "-NoProfile", "-NonInteractive", "-Command", script).redirectErrorStream(true).start().waitFor(3L, TimeUnit.SECONDS);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        });
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.mainMenuMode) {
            this.setDragging(false);
            this.saveMenuPosition();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public void enterMainMenu(int screenWidth, int screenHeight) {
        this.mainMenuMode = true;
        if (!this.menuPositionLoaded) {
            this.loadMenuPosition(screenWidth);
        }
        this.clampMenuPosition(screenWidth, screenHeight);
    }

    public void leaveMainMenu() {
        if (!this.mainMenuMode) {
            return;
        }
        this.saveMenuPosition();
        this.setDragging(false);
        this.mainMenuMode = false;
    }

    public void renderMainMenu(class_332 context, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        if (!this.mainMenuMode) {
            this.enterMainMenu(screenWidth, screenHeight);
        }
        if (this.isDragging()) {
            this.menuX = Math.round((float)mouseX + this.menuDragX);
            this.menuY = Math.round((float)mouseY + this.menuDragY);
            this.clampMenuPosition(screenWidth, screenHeight);
        }
        this.drawDraggable(context);
    }

    private float getContentX() {
        return this.displayX() + 4.0f + (float)this.artworkSize() + 5.0f;
    }

    private float getContentRight() {
        return this.displayX() + (float)this.panelWidth() - 5.0f;
    }

    private float getProgressX() {
        return this.displayX() + 4.0f;
    }

    private float getProgressWidth() {
        return Math.max(24.0f, this.displayX() + (float)this.panelWidth() - 3.0f - this.getProgressX());
    }

    private float getControlsCenterX() {
        return this.getProgressX() + this.getProgressWidth() / 2.0f - 3.0f - 2.0f;
    }

    private float getControlsY() {
        return this.displayY() + (this.mainMenuMode ? (float)this.panelHeight() - 18.0f : 25.5f);
    }

    private boolean isControlHovered(double mouseX, double mouseY, float x, float y, float size) {
        return mouseX >= (double)x && mouseX <= (double)(x + size) && mouseY >= (double)y && mouseY <= (double)(y + size);
    }

    private float displayX() {
        return this.mainMenuMode ? (float)this.menuX : (float)this.getX();
    }

    private float displayY() {
        return this.mainMenuMode ? (float)this.menuY : (float)this.getY();
    }

    private int panelWidth() {
        return this.mainMenuMode ? 150 : this.getWidth();
    }

    private int panelHeight() {
        return this.mainMenuMode ? 50 : this.getHeight();
    }

    private int artworkSize() {
        return this.mainMenuMode ? 22 : 20;
    }

    private boolean isMenuHovered(double mouseX, double mouseY) {
        return mouseX >= (double)this.menuX && mouseX <= (double)(this.menuX + 150) && mouseY >= (double)this.menuY && mouseY <= (double)(this.menuY + 50);
    }

    private void loadMenuPosition(int screenWidth) {
        this.menuX = (screenWidth - 150) / 2;
        this.menuY = 12;
        Path file = this.menuPositionFile();
        if (Files.isRegularFile(file, new LinkOption[0])) {
            try {
                MenuPosition position = (MenuPosition)gson.fromJson(Files.readString(file, StandardCharsets.UTF_8), MenuPosition.class);
                if (position != null && position.width() == 150) {
                    this.menuX = position.x();
                    this.menuY = position.y();
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        this.menuPositionLoaded = true;
    }

    private void saveMenuPosition() {
        if (!this.menuPositionLoaded) {
            return;
        }
        try {
            Path file = this.menuPositionFile();
            Files.createDirectories(file.getParent(), new FileAttribute[0]);
            Files.writeString(file, (CharSequence)gson.toJson((Object)new MenuPosition(this.menuX, this.menuY, 150)), StandardCharsets.UTF_8, new OpenOption[0]);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void clampMenuPosition(int screenWidth, int screenHeight) {
        this.menuX = Math.max(0, Math.min(this.menuX, Math.max(0, screenWidth - 150)));
        this.menuY = Math.max(0, Math.min(this.menuY, Math.max(0, screenHeight - 50)));
    }

    private Path menuPositionFile() {
        return MediaPlayer.mc.field_1697.toPath().resolve("recoin").resolve("main_menu_media_player.json");
    }

    private record MenuPosition(int x, int y, int width) {
    }
}

