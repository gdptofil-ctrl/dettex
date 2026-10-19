/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11228
 *  net.minecraft.class_11246
 *  net.minecraft.class_758
 *  net.minecraft.class_758$class_4596
 */
package ru.recoin.common.util.render;

import net.minecraft.class_11228;
import net.minecraft.class_11246;
import net.minecraft.class_758;

public final class GuiRenderFlusher {
    private static class_11228 guiRenderer;
    private static class_11246 guiState;
    private static class_758 fogRenderer;

    private GuiRenderFlusher() {
    }

    public static void bind(class_11228 renderer, class_11246 state, class_758 fog) {
        guiRenderer = renderer;
        guiState = state;
        fogRenderer = fog;
    }

    public static void flush() {
        if (guiRenderer != null && guiState != null && fogRenderer != null) {
            guiRenderer.method_70890(fogRenderer.method_71109(class_758.class_4596.field_60101));
            guiState.method_70926();
        }
    }
}

