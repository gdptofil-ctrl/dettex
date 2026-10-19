/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_266
 *  net.minecraft.class_269
 *  net.minecraft.class_332
 *  net.minecraft.class_355
 *  net.minecraft.class_437
 *  net.minecraft.class_465
 *  net.minecraft.class_490
 *  net.minecraft.class_8646
 *  org.joml.Matrix3x2fStack
 */
package ru.recoin.implement.features.modules.render;

import net.minecraft.class_266;
import net.minecraft.class_269;
import net.minecraft.class_332;
import net.minecraft.class_355;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_490;
import net.minecraft.class_8646;
import org.joml.Matrix3x2fStack;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.implement.features.modules.render.Animations;

public final class UiAnimationManager
implements QuickImports {
    private static long inventoryStartedAt;
    private static long tabStartedAt;
    private static boolean tabOpen;
    private static boolean tabClosing;
    private static int inventoryTransformDepth;
    private static boolean inventoryClosing;
    private static boolean allowClose;
    private static long inventoryCloseStartedAt;
    private static class_437 closingScreen;

    public static void initialize() {
        long now;
        inventoryStartedAt = tabStartedAt = (now = System.currentTimeMillis());
        tabOpen = false;
        tabClosing = false;
        inventoryTransformDepth = 0;
        allowClose = false;
        inventoryClosing = false;
        closingScreen = null;
    }

    public static void reset() {
        tabClosing = false;
        tabOpen = false;
        inventoryTransformDepth = 0;
        allowClose = false;
        inventoryClosing = false;
        closingScreen = null;
        Render2DUtil.setRenderAlpha(1.0f);
    }

    public static void tick() {
        boolean inventory;
        long duration;
        Animations animations = UiAnimationManager.module();
        if (animations == null || !animations.isState()) {
            return;
        }
        if (inventoryClosing && closingScreen != null && UiAnimationManager.raw(inventoryCloseStartedAt, duration = animations.duration(120L, (inventory = closingScreen instanceof class_490) ? animations.inventorySpeed : animations.chestSpeed)) >= 1.0f) {
            class_437 screen = closingScreen;
            allowClose = true;
            screen.method_25419();
            allowClose = false;
            inventoryClosing = false;
            closingScreen = null;
        }
    }

    public static void onScreenOpened(class_437 screen) {
        if (screen instanceof class_465) {
            inventoryStartedAt = System.currentTimeMillis();
            inventoryClosing = false;
            closingScreen = null;
        }
    }

    public static void onScreenClosed() {
        if (!allowClose) {
            inventoryClosing = false;
            closingScreen = null;
        }
    }

    public static boolean requestClose(class_437 screen) {
        Animations animations = UiAnimationManager.module();
        if (allowClose || animations == null || screen == null || !(screen instanceof class_465)) {
            return false;
        }
        boolean inventory = screen instanceof class_490;
        if (!animations.enabled(inventory ? "Inventory" : "Chests")) {
            return false;
        }
        if (!inventoryClosing || closingScreen != screen) {
            inventoryClosing = true;
            closingScreen = screen;
            inventoryCloseStartedAt = System.currentTimeMillis();
        }
        return true;
    }

    public static float getInventoryScale(class_437 screen) {
        Animations animations = UiAnimationManager.module();
        if (animations == null || screen == null || !animations.isState()) {
            return 1.0f;
        }
        boolean inventory = screen instanceof class_490;
        if (!(screen instanceof class_465) || !animations.enabled(inventory ? "Inventory" : "Chests")) {
            return 1.0f;
        }
        long duration = animations.duration(inventoryClosing ? 120L : 90L, inventory ? animations.inventorySpeed : animations.chestSpeed);
        float progress = inventoryClosing && closingScreen == screen ? 1.0f - animations.easeClosing(UiAnimationManager.raw(inventoryCloseStartedAt, duration)) : animations.ease(UiAnimationManager.raw(inventoryStartedAt, duration));
        return Math.max(0.05f, Math.min(1.25f, progress));
    }

    public static boolean beginInventoryRender(class_332 context, class_437 screen) {
        Animations animations = UiAnimationManager.module();
        if (animations == null || screen == null || !animations.isState()) {
            return false;
        }
        boolean inventory = screen instanceof class_490;
        if (!(screen instanceof class_465) || !animations.enabled(inventory ? "Inventory" : "Chests")) {
            return false;
        }
        long duration = animations.duration(inventoryClosing ? 120L : 90L, inventory ? animations.inventorySpeed : animations.chestSpeed);
        float progress = inventoryClosing && closingScreen == screen ? 1.0f - animations.easeClosing(UiAnimationManager.raw(inventoryCloseStartedAt, duration)) : animations.ease(UiAnimationManager.raw(inventoryStartedAt, duration));
        float scale = Math.max(0.05f, Math.min(1.25f, progress));
        float x = (float)context.method_51421() * 0.5f;
        float y = (float)context.method_51443() * 0.5f;
        context.method_51448().pushMatrix();
        context.method_51448().translate(x, y);
        context.method_51448().scale(scale, scale);
        context.method_51448().translate(-x, -y);
        ++inventoryTransformDepth;
        return true;
    }

    public static void endInventoryRender(class_332 context) {
        if (inventoryTransformDepth <= 0) {
            return;
        }
        --inventoryTransformDepth;
        context.method_51448().popMatrix();
    }

    public static boolean applyChatTransform(Matrix3x2fStack matrices) {
        return false;
    }

    public static boolean applyChatInputTransform(Matrix3x2fStack matrices) {
        return UiAnimationManager.applyChatTransform(matrices);
    }

    public static void endChatTransform(Matrix3x2fStack matrices) {
        matrices.popMatrix();
        Render2DUtil.setRenderAlpha(1.0f);
    }

    public static void onTabVisible(boolean visible) {
        Animations animations = UiAnimationManager.module();
        if (animations == null || !animations.enabled("Tab")) {
            tabOpen = visible;
            tabClosing = false;
            return;
        }
        if (visible == tabOpen) {
            return;
        }
        tabOpen = visible;
        tabClosing = !visible;
        tabStartedAt = System.currentTimeMillis();
    }

    public static boolean shouldApplyTabTransform(boolean visible) {
        Animations animations = UiAnimationManager.module();
        if (animations == null || !animations.enabled("Tab")) {
            return false;
        }
        if (visible) {
            return true;
        }
        return tabClosing && UiAnimationManager.raw(tabStartedAt, animations.duration(240L, animations.tabSpeed)) < 1.0f;
    }

    public static boolean applyTabTransform(Matrix3x2fStack matrices) {
        Animations animations = UiAnimationManager.module();
        if (animations == null || !animations.enabled("Tab")) {
            return false;
        }
        long duration = animations.duration(tabOpen ? 300L : 240L, animations.tabSpeed);
        float raw = UiAnimationManager.raw(tabStartedAt, duration);
        float progress = animations.ease(tabOpen ? raw : 1.0f - raw);
        float center = (float)mc.method_22683().method_4486() * 0.5f;
        matrices.pushMatrix();
        matrices.translate(center, 0.0f);
        matrices.scale(progress, progress);
        matrices.translate(-center, 0.0f);
        Render2DUtil.setRenderAlpha(Math.max(0.0f, Math.min(1.0f, progress)));
        return true;
    }

    public static void endTabTransform(Matrix3x2fStack matrices) {
        matrices.popMatrix();
        Render2DUtil.setRenderAlpha(1.0f);
    }

    public static void renderClosingTab(class_332 context, class_355 hud) {
        Animations animations = UiAnimationManager.module();
        if (!tabClosing || animations == null || UiAnimationManager.mc.field_1687 == null || UiAnimationManager.raw(tabStartedAt, animations.duration(240L, animations.tabSpeed)) >= 1.0f) {
            return;
        }
        class_269 scoreboard = UiAnimationManager.mc.field_1687.method_8428();
        class_266 objective = scoreboard.method_1189(class_8646.field_45156);
        hud.method_1919(context, context.method_51421(), scoreboard, objective);
    }

    public static float getAnimatedCameraDistance(float distance) {
        return distance;
    }

    public static float buttonScale(boolean hovered, long hoverStartedAt) {
        Animations animations = UiAnimationManager.module();
        if (animations == null || !animations.enabled("Buttons")) {
            return 1.0f;
        }
        float progress = animations.ease(UiAnimationManager.raw(hoverStartedAt, animations.duration(120L, animations.buttonSpeed)));
        return 1.0f + (hovered ? progress : 1.0f - progress) * 0.03f;
    }

    private static float raw(long start, long duration) {
        return Math.max(0.0f, Math.min(1.0f, (float)(System.currentTimeMillis() - start) / (float)Math.max(1L, duration)));
    }

    private static Animations module() {
        return Animations.getInstance();
    }

    private UiAnimationManager() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

