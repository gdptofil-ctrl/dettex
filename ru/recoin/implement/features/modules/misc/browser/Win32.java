package ru.recoin.implement.features.modules.misc.browser;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The few Windows functions the app screens need (through JNA, which the client already carries):
 * list windows, copy a window's picture, and post mouse / keyboard messages to it.
 */
final class Win32 {
    private Win32() {
    }

    interface User32 extends Library {
        interface EnumProc extends Callback {
            boolean invoke(Pointer hwnd, Pointer data);
        }

        boolean EnumWindows(EnumProc proc, Pointer data);

        boolean IsWindow(Pointer h);

        boolean IsWindowVisible(Pointer h);

        boolean IsIconic(Pointer h);

        boolean ShowWindow(Pointer h, int cmd);

        int GetWindowTextW(Pointer h, char[] buf, int max);

        int GetWindowThreadProcessId(Pointer h, int[] pid);

        boolean GetClientRect(Pointer h, int[] rect);

        Pointer GetWindow(Pointer h, int cmd);

        int GetWindowLongW(Pointer h, int index);

        Pointer GetDC(Pointer h);

        int ReleaseDC(Pointer h, Pointer dc);

        boolean PrintWindow(Pointer h, Pointer dc, int flags);

        boolean PostMessageW(Pointer h, int msg, long wParam, long lParam);

        /** POINT is passed by value; on x64 an 8-byte struct goes in one register, so a long works. */
        Pointer ChildWindowFromPointEx(Pointer parent, long point, int flags);

        int MapWindowPoints(Pointer from, Pointer to, int[] points, int count);

        boolean ClientToScreen(Pointer h, int[] point);
    }

    interface Gdi32 extends Library {
        Pointer CreateCompatibleDC(Pointer dc);

        Pointer CreateCompatibleBitmap(Pointer dc, int w, int h);

        Pointer SelectObject(Pointer dc, Pointer obj);

        boolean DeleteObject(Pointer obj);

        boolean DeleteDC(Pointer dc);

        int GetDIBits(Pointer dc, Pointer bitmap, int start, int lines, Pointer bits, Pointer info, int usage);
    }

    interface Dwm extends Library {
        int DwmGetWindowAttribute(Pointer h, int attribute, int[] value, int size);
    }

    private static User32 user32;
    private static Gdi32 gdi32;
    private static Dwm dwm;
    private static boolean failed;

    static boolean available() {
        if (failed) return false;
        if (user32 != null) return true;
        if (!System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
            failed = true;
            return false;
        }
        try {
            user32 = Native.load("user32", User32.class);
            gdi32 = Native.load("gdi32", Gdi32.class);
            try {
                dwm = Native.load("dwmapi", Dwm.class);
            } catch (Throwable ignored) {
                dwm = null;
            }
            return true;
        } catch (Throwable e) {
            failed = true;
            return false;
        }
    }

    static User32 u() {
        return user32;
    }

    static long id(Pointer h) {
        return h == null ? 0L : Pointer.nativeValue(h);
    }

    static Pointer handle(long id) {
        return id == 0L ? null : new Pointer(id);
    }

    // ------------------------------------------------------------------ windows

    record WindowInfo(long hwnd, String title, int pid) {
    }

    static String title(Pointer h) {
        char[] buf = new char[512];
        int n = user32.GetWindowTextW(h, buf, buf.length);
        return n <= 0 ? "" : new String(buf, 0, n);
    }

    /** Normal visible program windows (no tool windows, no hidden UWP shells, not Minecraft itself). */
    static List<WindowInfo> windows() {
        List<WindowInfo> out = new ArrayList<>();
        if (!Win32.available()) return out;
        long self = ProcessHandle.current().pid();
        user32.EnumWindows((h, data) -> {
            try {
                if (!user32.IsWindowVisible(h)) return true;
                if (user32.GetWindow(h, 4) != null) return true;                       // GW_OWNER: dialogs
                int ex = user32.GetWindowLongW(h, -20);                               // GWL_EXSTYLE
                if ((ex & 0x80) != 0) return true;                                    // WS_EX_TOOLWINDOW
                if (dwm != null) {
                    int[] cloaked = new int[1];
                    if (dwm.DwmGetWindowAttribute(h, 14, cloaked, 4) == 0 && cloaked[0] != 0) return true;   // DWMWA_CLOAKED
                }
                String t = Win32.title(h);
                if (t.isBlank() || t.equals("Program Manager")) return true;
                int[] pid = new int[1];
                user32.GetWindowThreadProcessId(h, pid);
                if (pid[0] == self) return true;
                out.add(new WindowInfo(Win32.id(h), t, pid[0]));
            } catch (Throwable ignored) {
            }
            return true;
        }, null);
        return out;
    }

    static int[] clientSize(Pointer h) {
        int[] r = new int[4];
        if (!user32.GetClientRect(h, r)) return new int[]{0, 0};
        return new int[]{r[2] - r[0], r[3] - r[1]};
    }

    // ------------------------------------------------------------------ picture

    /** Reused buffers for one window. */
    static final class Grabber {
        private Memory bits;
        private final Memory info = new Memory(44);
        int width;
        int height;

        /** Copies the window's client area into bits (BGRX). Returns false when it could not. */
        boolean grab(Pointer h) {
            int[] size = Win32.clientSize(h);
            int w = size[0];
            int hh = size[1];
            if (w <= 0 || hh <= 0 || w > 8192 || hh > 8192) return false;
            Pointer winDc = user32.GetDC(h);
            if (winDc == null) return false;
            Pointer memDc = null;
            Pointer bmp = null;
            try {
                memDc = gdi32.CreateCompatibleDC(winDc);
                bmp = gdi32.CreateCompatibleBitmap(winDc, w, hh);
                if (memDc == null || bmp == null) return false;
                Pointer old = gdi32.SelectObject(memDc, bmp);
                boolean ok = user32.PrintWindow(h, memDc, 1 | 2);   // PW_CLIENTONLY | PW_RENDERFULLCONTENT
                gdi32.SelectObject(memDc, old);
                if (!ok) return false;
                long need = (long)w * hh * 4L;
                if (this.bits == null || this.bits.size() != need) this.bits = new Memory(need);
                this.info.clear();
                this.info.setInt(0, 40);     // biSize
                this.info.setInt(4, w);      // biWidth
                this.info.setInt(8, -hh);    // biHeight (negative = top-down)
                this.info.setShort(12, (short)1);    // biPlanes
                this.info.setShort(14, (short)32);   // biBitCount
                this.info.setInt(16, 0);     // BI_RGB
                int lines = gdi32.GetDIBits(memDc, bmp, 0, hh, this.bits, this.info, 0);
                if (lines <= 0) return false;
                this.width = w;
                this.height = hh;
                return true;
            } finally {
                if (bmp != null) gdi32.DeleteObject(bmp);
                if (memDc != null) gdi32.DeleteDC(memDc);
                user32.ReleaseDC(h, winDc);
            }
        }

        Memory bits() {
            return this.bits;
        }
    }

    // ------------------------------------------------------------------ input

    static long lparam(int x, int y) {
        return (long)(y & 0xFFFF) << 16 | (long)(x & 0xFFFF);
    }

    /** Deepest child window under client point (x, y) of top; returns {hwnd, x, y} in its own coords. */
    static long[] childAt(Pointer top, int x, int y) {
        Pointer cur = top;
        int[] pt = {x, y};
        for (int depth = 0; depth < 12; ++depth) {
            long packed = (long)pt[1] << 32 | (long)pt[0] & 0xFFFFFFFFL;
            Pointer child = user32.ChildWindowFromPointEx(cur, packed, 1 | 4);   // skip invisible | transparent
            if (child == null || Win32.id(child) == Win32.id(cur)) break;
            user32.MapWindowPoints(cur, child, pt, 1);
            cur = child;
        }
        return new long[]{Win32.id(cur), pt[0], pt[1]};
    }

    /** GLFW key -> Windows virtual key (0 = none). */
    static int vk(int glfw) {
        if (glfw >= 65 && glfw <= 90) return glfw;          // A-Z
        if (glfw >= 48 && glfw <= 57) return glfw;          // 0-9
        if (glfw >= 290 && glfw <= 301) return 0x70 + glfw - 290;   // F1-F12
        return switch (glfw) {
            case 32 -> 0x20;    // space
            case 256 -> 0x1B;   // esc
            case 257, 335 -> 0x0D;   // enter
            case 258 -> 0x09;   // tab
            case 259 -> 0x08;   // backspace
            case 260 -> 0x2D;   // insert
            case 261 -> 0x2E;   // delete
            case 262 -> 0x27;   // right
            case 263 -> 0x25;   // left
            case 264 -> 0x28;   // down
            case 265 -> 0x26;   // up
            case 266 -> 0x21;   // page up
            case 267 -> 0x22;   // page down
            case 268 -> 0x24;   // home
            case 269 -> 0x23;   // end
            case 340, 344 -> 0x10;   // shift
            case 341, 345 -> 0x11;   // ctrl
            case 342, 346 -> 0x12;   // alt
            default -> 0;
        };
    }
}
