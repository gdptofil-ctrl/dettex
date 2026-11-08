package ru.recoin.implement.features.modules.misc.browser;

import com.sun.jna.Pointer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.lwjgl.system.MemoryUtil;
import ru.recoin.common.util.logger.LoggerUtil;

/**
 * The live picture of a program's window (Windows), shown as a screen in the world. A background
 * thread copies the window several times a second; clicks, wheel and typing are posted to it.
 * Works with most normal programs (Notepad, Explorer, Telegram, players, editors...). Games and
 * some apps that draw with DirectX or ignore posted input may show black or not react.
 */
public final class AppPanel extends Panel {
    private final long hwnd;
    private final String name;
    private final class_2960 textureId;
    private final Object lock = new Object();
    private volatile boolean running = true;
    private volatile boolean dead;
    private volatile int fps;

    // frame from the capture thread (RGBA, ready for NativeImage)
    private ByteBuffer frame;
    private int frameW;
    private int frameH;
    private boolean frameNew;

    // render thread
    private class_1043 texture;
    private class_1011 image;
    private int texW = 16;
    private int texH = 9;
    private boolean hasPicture;

    // input target remembered from the last click (keys go there)
    private volatile long focusHwnd;

    public AppPanel(long hwnd, String name, int fps, class_243 anchor, float yaw, double width) {
        super(anchor, yaw, width);
        this.hwnd = hwnd;
        this.name = name;
        this.fps = Math.max(1, Math.min(60, fps));
        this.textureId = class_2960.method_60655("insidethesystem", "app_window_" + this.id);
        Pointer h = Win32.handle(hwnd);
        if (Win32.u().IsIconic(h)) {
            Win32.u().ShowWindow(h, 4);   // SW_SHOWNOACTIVATE: minimized windows draw nothing
        }
        int[] size = Win32.clientSize(h);
        if (size[0] > 0 && size[1] > 0) {
            this.texW = size[0];
            this.texH = size[1];
        }
        Thread t = new Thread(this::captureLoop, "Dettex-App-Capture-" + this.id);
        t.setDaemon(true);
        t.start();
    }

    public long hwnd() {
        return this.hwnd;
    }

    public void setFps(int fps) {
        this.fps = Math.max(1, Math.min(60, fps));
    }

    private void captureLoop() {
        Win32.Grabber g = new Win32.Grabber();
        Pointer h = Win32.handle(this.hwnd);
        int misses = 0;
        while (this.running) {
            long start = System.nanoTime();
            try {
                if (!Win32.u().IsWindow(h)) {
                    this.dead = true;
                    break;
                }
                if (g.grab(h)) {
                    misses = 0;
                    int w = g.width;
                    int hh = g.height;
                    IntBuffer src = g.bits().getByteBuffer(0, (long)w * hh * 4L).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
                    synchronized (this.lock) {
                        int need = w * hh * 4;
                        if (this.frame == null || this.frame.capacity() != need) {
                            this.frame = ByteBuffer.allocateDirect(need).order(ByteOrder.LITTLE_ENDIAN);
                        }
                        IntBuffer dst = this.frame.asIntBuffer();
                        int n = w * hh;
                        for (int i = 0; i < n; ++i) {
                            int v = src.get(i);   // bytes B G R X
                            dst.put(i, 0xFF000000 | (v & 0xFF) << 16 | v & 0xFF00 | v >> 16 & 0xFF);   // bytes R G B A
                        }
                        this.frameW = w;
                        this.frameH = hh;
                        this.frameNew = true;
                    }
                } else if (++misses > 50) {
                    Thread.sleep(500L);   // minimized or hidden: check rarely
                }
            } catch (InterruptedException e) {
                break;
            } catch (Throwable e) {
                LoggerUtil.info("[Browser] window capture error: " + e);
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException ie) {
                    break;
                }
            }
            long spent = (System.nanoTime() - start) / 1_000_000L;
            long wait = 1000L / this.fps - spent;
            if (wait > 0) {
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    @Override
    public void renderTick() {
        synchronized (this.lock) {
            if (!this.frameNew || this.frame == null) return;
            this.frameNew = false;
            try {
                if (this.image == null || this.texW != this.frameW || this.texH != this.frameH) {
                    this.texW = this.frameW;
                    this.texH = this.frameH;
                    class_1011 img = new class_1011(this.texW, this.texH, false);
                    class_1043 tex = new class_1043(() -> "dettex_app_window", img);
                    class_310.method_1551().method_1531().method_4616(this.textureId, tex);   // replaces and closes the old one
                    this.image = img;
                    this.texture = tex;
                }
                MemoryUtil.memCopy(MemoryUtil.memAddress(this.frame), this.image.field_4988, (long)this.texW * this.texH * 4L);
                this.texture.method_4524();
                this.hasPicture = true;
            } catch (Throwable e) {
                LoggerUtil.info("[Browser] window upload error: " + e);
            }
        }
    }

    @Override
    public String title() {
        String t = this.name;
        return t.length() > 40 ? t.substring(0, 40) + "…" : t;
    }

    @Override
    public class_2960 texture() {
        return this.hasPicture ? this.textureId : null;
    }

    @Override
    public int pixelWidth() {
        return this.texW;
    }

    @Override
    public int pixelHeight() {
        return this.texH;
    }

    @Override
    public String status() {
        return this.dead ? "Окно закрыто" : "Захватываю окно «" + this.title() + "»...";
    }

    @Override
    public boolean alive() {
        return !this.dead;
    }

    // ------------------------------------------------------------------ input

    private static final int WM_MOUSEMOVE = 0x200;
    private static final int WM_LBUTTONDOWN = 0x201;
    private static final int WM_LBUTTONUP = 0x202;
    private static final int WM_RBUTTONDOWN = 0x204;
    private static final int WM_RBUTTONUP = 0x205;
    private static final int WM_MBUTTONDOWN = 0x207;
    private static final int WM_MBUTTONUP = 0x208;
    private static final int WM_MOUSEWHEEL = 0x20A;
    private static final int WM_KEYDOWN = 0x100;
    private static final int WM_KEYUP = 0x101;
    private static final int WM_CHAR = 0x102;

    private int held;   // MK_ flags of buttons held

    private void post(int msg, int x, int y, long wParam) {
        try {
            Pointer top = Win32.handle(this.hwnd);
            long[] c = Win32.childAt(top, x, y);
            Win32.u().PostMessageW(Win32.handle(c[0]), msg, wParam, Win32.lparam((int)c[1], (int)c[2]));
            if (msg == WM_LBUTTONDOWN || msg == WM_RBUTTONDOWN || msg == WM_MBUTTONDOWN) this.focusHwnd = c[0];
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void mouseMove(int x, int y) {
        this.post(WM_MOUSEMOVE, x, y, this.held);
    }

    @Override
    public void mousePress(int x, int y, int button) {
        int[] m = AppPanel.buttonMsgs(button);
        if (m == null) return;
        this.held |= m[2];
        this.post(m[0], x, y, this.held);
    }

    @Override
    public void mouseRelease(int x, int y, int button) {
        int[] m = AppPanel.buttonMsgs(button);
        if (m == null) return;
        this.held &= ~m[2];
        this.post(m[1], x, y, this.held);
    }

    private static int[] buttonMsgs(int button) {
        return switch (button) {
            case 0 -> new int[]{WM_LBUTTONDOWN, WM_LBUTTONUP, 0x1};
            case 1 -> new int[]{WM_RBUTTONDOWN, WM_RBUTTONUP, 0x2};
            case 2 -> new int[]{WM_MBUTTONDOWN, WM_MBUTTONUP, 0x10};
            default -> null;
        };
    }

    @Override
    public void wheel(int x, int y, double amount) {
        try {
            Pointer top = Win32.handle(this.hwnd);
            long[] c = Win32.childAt(top, x, y);
            int[] screen = {x, y};
            Win32.u().ClientToScreen(top, screen);   // the wheel message wants screen coordinates
            int delta = (int)Math.round(amount * 120.0);
            long w = ((long)(delta & 0xFFFF)) << 16 | this.held;
            Win32.u().PostMessageW(Win32.handle(c[0]), WM_MOUSEWHEEL, w, Win32.lparam(screen[0], screen[1]));
        } catch (Throwable ignored) {
        }
    }

    private Pointer keyTarget() {
        long f = this.focusHwnd;
        return Win32.handle(f != 0L && Win32.u().IsWindow(Win32.handle(f)) ? f : this.hwnd);
    }

    @Override
    public void keyPress(int key, int scancode, int mods) {
        int vk = Win32.vk(key);
        // letters, digits and space arrive as characters (WM_CHAR); a key message too would type them twice
        if (vk == 0 || vk == 0x20 || vk >= 0x30 && vk <= 0x5A) return;
        Win32.u().PostMessageW(this.keyTarget(), WM_KEYDOWN, vk, 1L | ((long)scancode & 0xFF) << 16);
    }

    @Override
    public void keyRelease(int key, int scancode, int mods) {
        int vk = Win32.vk(key);
        // letters, digits and space arrive as characters (WM_CHAR); a key message too would type them twice
        if (vk == 0 || vk == 0x20 || vk >= 0x30 && vk <= 0x5A) return;
        Win32.u().PostMessageW(this.keyTarget(), WM_KEYUP, vk, 1L | ((long)scancode & 0xFF) << 16 | 0xC0000000L);
    }

    @Override
    public void charTyped(char c, int mods) {
        Win32.u().PostMessageW(this.keyTarget(), WM_CHAR, c, 1L);
    }

    @Override
    public void close() {
        this.running = false;
        class_310 mc = class_310.method_1551();
        mc.execute(() -> {
            try {
                mc.method_1531().method_4615(this.textureId);
            } catch (Throwable ignored) {
            }
        });
    }
}
