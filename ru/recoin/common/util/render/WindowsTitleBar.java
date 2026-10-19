/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.jna.Function
 *  com.sun.jna.Memory
 *  com.sun.jna.Pointer
 */
package ru.recoin.common.util.render;

import com.sun.jna.Function;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import java.lang.reflect.Method;

public final class WindowsTitleBar {
    private static boolean applied = false;

    private WindowsTitleBar() {
    }

    public static void applyDarkTitleBar(long windowHandle) {
        if (windowHandle == 0L) {
            return;
        }
        if (!System.getProperty("os.name", "").toLowerCase().contains("win")) {
            return;
        }
        try {
            long hwnd = 0L;
            try {
                Class<?> nativeWin32 = Class.forName("org.lwjgl.glfw.GLFWNativeWin32");
                Method method = nativeWin32.getMethod("glfwGetWin32Window", Long.TYPE);
                Object result = method.invoke(null, windowHandle);
                if (result instanceof Long) {
                    Long l = (Long)result;
                    hwnd = l;
                }
            }
            catch (Throwable nativeWin32) {
                // empty catch block
            }
            if (hwnd == 0L) {
                try {
                    Method getPointer;
                    Object ptr;
                    Class<?> user32 = Class.forName("com.sun.jna.platform.win32.User32");
                    Object instance = user32.getField("INSTANCE").get(null);
                    Method getForeground = user32.getMethod("GetForegroundWindow", new Class[0]);
                    Object fg = getForeground.invoke(instance, new Object[0]);
                    if (fg != null && (ptr = (getPointer = fg.getClass().getMethod("getPointer", new Class[0])).invoke(fg, new Object[0])) instanceof Pointer) {
                        Pointer p = (Pointer)ptr;
                        hwnd = Pointer.nativeValue((Pointer)p);
                    }
                }
                catch (Throwable user32) {
                    // empty catch block
                }
            }
            if (hwnd == 0L) {
                return;
            }
            Function dwmSetWindowAttribute = Function.getFunction((String)"dwmapi", (String)"DwmSetWindowAttribute");
            if (dwmSetWindowAttribute != null) {
                Pointer hwndPtr = new Pointer(hwnd);
                Memory pv = new Memory(4L);
                pv.setInt(0L, 1);
                dwmSetWindowAttribute.invokeInt(new Object[]{hwndPtr, 20, pv, 4});
                dwmSetWindowAttribute.invokeInt(new Object[]{hwndPtr, 19, pv, 4});
                pv.setInt(0L, 0);
                dwmSetWindowAttribute.invokeInt(new Object[]{hwndPtr, 35, pv, 4});
                pv.setInt(0L, 0xFFFFFF);
                dwmSetWindowAttribute.invokeInt(new Object[]{hwndPtr, 36, pv, 4});
                applied = true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static boolean isApplied() {
        return applied;
    }
}

