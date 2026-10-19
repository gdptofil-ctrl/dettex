/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  net.minecraft.class_1041
 *  net.minecraft.class_289
 *  net.minecraft.class_310
 *  net.minecraft.class_9779
 */
package ru.recoin.common;

import com.google.gson.Gson;
import java.util.function.Supplier;
import net.minecraft.class_1041;
import net.minecraft.class_289;
import net.minecraft.class_310;
import net.minecraft.class_9779;
import ru.recoin.api.system.draw.DrawEngine;
import ru.recoin.api.system.draw.DrawEngineImpl;
import ru.recoin.api.system.shape.implement.Arc;
import ru.recoin.api.system.shape.implement.Blur;
import ru.recoin.api.system.shape.implement.Image;
import ru.recoin.api.system.shape.implement.Rectangle;
import ru.recoin.common.QuickLogger;
import ru.recoin.implement.screens.menu.components.implement.window.WindowManager;

public interface QuickImports
extends QuickLogger {
    public static final class_310 mc = class_310.method_1551();
    public static final DynamicTickCounter tickCounter = new DynamicTickCounter();
    public static final DynamicWindow window = new DynamicWindow();
    public static final Supplier<class_289> tessellator = class_289::method_1348;
    public static final DrawEngine drawEngine = new DrawEngineImpl();
    public static final Rectangle rectangle = new Rectangle();
    public static final Blur blur = new Blur();
    public static final Arc arc = new Arc();
    public static final Image image = new Image();
    public static final Gson gson = new Gson();
    public static final WindowManager windowManager = new WindowManager();

    public static final class DynamicTickCounter {
        public float getTickProgress(boolean ignoreFreeze) {
            class_9779 counter = class_310.method_1551().method_61966();
            return counter == null ? 0.0f : counter.method_60637(ignoreFreeze);
        }

        public float getDynamicDeltaTicks() {
            class_9779 counter = class_310.method_1551().method_61966();
            return counter == null ? 0.0f : counter.method_60636();
        }
    }

    public static final class DynamicWindow {
        private class_1041 current() {
            return class_310.method_1551().method_22683();
        }

        public boolean isAvailable() {
            return this.current() != null;
        }

        public int getScaledWidth() {
            class_1041 current = this.current();
            return current == null ? 0 : current.method_4486();
        }

        public int getScaledHeight() {
            class_1041 current = this.current();
            return current == null ? 0 : current.method_4502();
        }

        public int getWidth() {
            class_1041 current = this.current();
            return current == null ? 0 : current.method_4480();
        }

        public int getHeight() {
            class_1041 current = this.current();
            return current == null ? 0 : current.method_4507();
        }

        public double getScaleFactor() {
            class_1041 current = this.current();
            return current == null ? 1.0 : (double)current.method_4495();
        }

        public long getHandle() {
            class_1041 current = this.current();
            return current == null ? 0L : current.method_4490();
        }
    }
}

