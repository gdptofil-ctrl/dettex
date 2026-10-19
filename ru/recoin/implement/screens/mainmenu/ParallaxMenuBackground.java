/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10799
 *  net.minecraft.class_2960
 *  net.minecraft.class_332
 */
package ru.recoin.implement.screens.mainmenu;

import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import ru.recoin.common.util.render.LoadingScreenRenderer;
import ru.recoin.common.util.render.pipeline.MainMenuBackgroundPipeline;
import ru.recoin.implement.screens.mainmenu.MenuBackgroundEffects;
import ru.recoin.implement.screens.mainmenu.WallpaperStore;

final class ParallaxMenuBackground {
    private final MainMenuBackgroundPipeline pipeline = new MainMenuBackgroundPipeline();
    private float mouseX;
    private float mouseY;
    private float lastMouseX;
    private float lastMouseY;
    private float energy;
    private long lastFrameNanos;
    private boolean initialized;

    ParallaxMenuBackground() {
    }

    void reset() {
        this.initialized = false;
        this.energy = 0.0f;
        this.lastFrameNanos = System.nanoTime();
    }

    void render(class_332 context, int width, int height, int rawMouseX, int rawMouseY) {
        class_2960 tex;
        long now = System.nanoTime();
        float seconds = Math.min(0.05f, Math.max(0.0f, (float)(now - this.lastFrameNanos) / 1.0E9f));
        this.lastFrameNanos = now;
        if (!this.initialized) {
            this.mouseX = this.lastMouseX = (float)rawMouseX;
            this.mouseY = this.lastMouseY = (float)rawMouseY;
            this.initialized = true;
        }
        float movement = (float)Math.hypot((float)rawMouseX - this.lastMouseX, (float)rawMouseY - this.lastMouseY);
        float normalizedSpeed = movement / Math.max(1.0f, (float)Math.max(width, height)) / Math.max(seconds, 0.004166667f);
        float targetEnergy = Math.min(1.0f, normalizedSpeed * 1.65f);
        float energyResponse = 1.0f - (float)Math.exp(-seconds * (targetEnergy > this.energy ? 14.0f : 3.8f));
        this.energy += (targetEnergy - this.energy) * energyResponse;
        float pointerResponse = 1.0f - (float)Math.exp(-seconds * 6.5f);
        this.mouseX += ((float)rawMouseX - this.mouseX) * pointerResponse;
        this.mouseY += ((float)rawMouseY - this.mouseY) * pointerResponse;
        this.lastMouseX = rawMouseX;
        this.lastMouseY = rawMouseY;
        WallpaperStore store = WallpaperStore.getInstance();
        if (store.isLoading()) {
            LoadingScreenRenderer.drawBackground(context, width, height);
            return;
        }
        if (!store.isDefault() && (tex = store.activeTexture()) != null) {
            this.renderWallpaper(context, tex, store.activeWidth(), store.activeHeight(), width, height);
            MenuBackgroundEffects.renderSoftBlur(context, width, height);
            return;
        }
        this.pipeline.draw(context, width, height, this.mouseX, this.mouseY, this.energy);
    }

    private void renderWallpaper(class_332 context, class_2960 texture, int imgW, int imgH, int scrW, int scrH) {
        float v;
        float u;
        int regionW;
        int regionH;
        float imgAspect = (float)imgW / (float)imgH;
        float scrAspect = (float)scrW / (float)scrH;
        if (imgAspect > scrAspect) {
            regionH = imgH;
            regionW = Math.round((float)imgH * scrAspect);
            u = (float)(imgW - regionW) / 2.0f;
            v = 0.0f;
        } else {
            regionW = imgW;
            regionH = Math.round((float)imgW / scrAspect);
            u = 0.0f;
            v = (float)(imgH - regionH) / 2.0f;
        }
        context.method_25293(class_10799.field_56883, texture, 0, 0, u, v, scrW, scrH, regionW, regionH, imgW, imgH, -1);
    }
}

