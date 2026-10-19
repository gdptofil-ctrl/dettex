/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.DepthTestFunction
 *  net.minecraft.class_10799
 *  net.minecraft.class_2960
 *  net.minecraft.class_332
 */
package ru.recoin.common.util.render.pipeline;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_332;

public final class MainMenuBackgroundPipeline {
    private static final class_2960 SHADER = class_2960.method_60655((String)"insidethesystem", (String)"core/recoin_menu_background");
    private static final RenderPipeline PIPELINE = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_56863}).withLocation(class_2960.method_60655((String)"recoin", (String)"pipeline/menu_background")).withVertexShader(SHADER).withFragmentShader(SHADER).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());

    public void draw(class_332 context, int width, int height, float mouseX, float mouseY, float energy) {
        if (context == null || width <= 0 || height <= 0) {
            return;
        }
        long cycleNanos = 60000000000L;
        int time = Math.round((float)(System.nanoTime() % cycleNanos) / (float)cycleNanos * 65535.0f);
        int pointerX = Math.clamp((long)Math.round(mouseX / (float)width * 255.0f), 0, 255);
        int pointerY = Math.clamp((long)Math.round(mouseY / (float)height * 254.0f), 0, 254) + 1;
        int packed = pointerY << 24 | (time >>> 8 & 0xFF) << 16 | (time & 0xFF) << 8 | pointerX;
        context.method_71048();
        context.method_48196(PIPELINE, 0, 0, width, height, packed);
        context.method_71048();
    }

    public void draw(class_332 context, int x1, int y1, int x2, int y2, float mouseX, float mouseY) {
        if (context == null || x2 <= x1 || y2 <= y1) {
            return;
        }
        long cycleNanos = 60000000000L;
        int time = Math.round((float)(System.nanoTime() % cycleNanos) / (float)cycleNanos * 65535.0f);
        float w = x2 - x1;
        float h = y2 - y1;
        int pointerX = Math.clamp((long)Math.round((mouseX - (float)x1) / w * 255.0f), 0, 255);
        int pointerY = Math.clamp((long)Math.round((mouseY - (float)y1) / h * 254.0f), 0, 254) + 1;
        int packed = pointerY << 24 | (time >>> 8 & 0xFF) << 16 | (time & 0xFF) << 8 | pointerX;
        context.method_71048();
        context.method_48196(PIPELINE, x1, y1, x2, y2, packed);
        context.method_71048();
    }
}

