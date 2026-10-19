/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1011
 *  net.minecraft.class_1043
 *  net.minecraft.class_1044
 *  net.minecraft.class_1060
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 */
package ru.recoin.implement.screens.mainmenu;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_1060;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

final class LoopingVideoTexture
implements AutoCloseable {
    static final class_2960 TEXTURE_ID = class_2960.method_60655((String)"insidethesystem", (String)"main_menu_video");
    private static final String RESOURCE = "/assets/insidethesystem/video/main_menu.mp4";
    private static final long FRAME_TIME_NS = 20833333L;
    private final class_310 client = class_310.method_1551();
    private final AtomicReference<BufferedImage> pendingFrame = new AtomicReference();
    private volatile boolean running;
    private Thread decoderThread;
    private class_1043 texture;
    private int textureWidth;
    private int textureHeight;

    LoopingVideoTexture() {
    }

    void start() {
        if (this.running) {
            return;
        }
        this.running = true;
        this.decoderThread = new Thread(this::decodeForever, "Recoin main-menu video");
        this.decoderThread.setDaemon(true);
        this.decoderThread.start();
    }

    boolean uploadNewestFrame() {
        class_1011 image;
        BufferedImage frame = this.pendingFrame.getAndSet(null);
        if (frame == null) {
            return this.texture != null;
        }
        int width = frame.getWidth();
        int height = frame.getHeight();
        if (this.texture == null || this.textureWidth != width || this.textureHeight != height) {
            class_1060 textures = this.client.method_1531();
            if (this.texture != null) {
                textures.method_4615(TEXTURE_ID);
            }
            this.textureWidth = width;
            this.textureHeight = height;
            this.texture = new class_1043(() -> "Recoin main-menu video", width, height, false);
            textures.method_4616(TEXTURE_ID, (class_1044)this.texture);
        }
        if ((image = this.texture.method_4525()) == null) {
            return false;
        }
        int[] pixels = frame.getRGB(0, 0, width, height, null, 0, width);
        for (int y = 0; y < height; ++y) {
            int row = y * width;
            for (int x = 0; x < width; ++x) {
                image.method_61941(x, y, pixels[row + x]);
            }
        }
        this.texture.method_4524();
        return true;
    }

    int width() {
        return this.textureWidth;
    }

    int height() {
        return this.textureHeight;
    }

    private void decodeForever() {
        Path video = null;
        try {
            video = LoopingVideoTexture.extractVideo();
            while (this.running) {
                this.decodeOneLoop(video);
            }
        }
        catch (Throwable error) {
            if (this.running) {
                System.err.println("[Recoin] Main-menu video failed: " + error.getMessage());
            }
        }
        finally {
            if (video != null) {
                try {
                    Files.deleteIfExists(video);
                }
                catch (IOException iOException) {}
            }
        }
    }

    private void decodeOneLoop(Path video) throws Exception {
        try (FileChannelWrapper channel = NIOUtils.readableChannel(video.toFile());){
            Picture picture;
            FrameGrab grab = FrameGrab.createFrameGrab(channel);
            long nextFrameAt = System.nanoTime();
            while (this.running && (picture = grab.getNativeFrame()) != null) {
                this.pendingFrame.set(AWTUtil.toBufferedImage(picture));
                this.sleepUntil(nextFrameAt += 20833333L);
            }
        }
    }

    private static Path extractVideo() throws IOException {
        Path target = Files.createTempFile("recoin-main-menu-", ".mp4", new FileAttribute[0]);
        try (InputStream input = LoopingVideoTexture.class.getResourceAsStream(RESOURCE);){
            if (input == null) {
                throw new IOException("Missing resource /assets/insidethesystem/video/main_menu.mp4");
            }
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return target;
    }

    private void sleepUntil(long deadline) throws InterruptedException {
        long remaining = deadline - System.nanoTime();
        if (remaining <= 0L) {
            return;
        }
        long millis = remaining / 1000000L;
        int nanos = (int)(remaining % 1000000L);
        Thread.sleep(millis, nanos);
    }

    @Override
    public void close() {
        this.running = false;
        if (this.decoderThread != null) {
            this.decoderThread.interrupt();
        }
        this.pendingFrame.set(null);
        if (this.texture != null) {
            this.client.method_1531().method_4615(TEXTURE_ID);
            this.texture = null;
        }
    }
}

