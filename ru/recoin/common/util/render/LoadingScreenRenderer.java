/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.DepthTestFunction
 *  com.mojang.blaze3d.shaders.ShaderType
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_1011
 *  net.minecraft.class_1043
 *  net.minecraft.class_1044
 *  net.minecraft.class_10799
 *  net.minecraft.class_11231
 *  net.minecraft.class_11244
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_4588
 *  net.minecraft.class_8030
 *  org.joml.Matrix3x2f
 *  org.joml.Matrix3x2fc
 *  org.slf4j.LoggerFactory
 */
package ru.recoin.common.util.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_10799;
import net.minecraft.class_11231;
import net.minecraft.class_11244;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4588;
import net.minecraft.class_8030;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.slf4j.LoggerFactory;
import ru.recoin.api.system.font.FontAtlas;
import ru.recoin.api.system.font.FontPipeline;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.core.Main;
import ru.recoin.implement.features.modules.render.Hud;
import ru.recoin.mixins.DrawContextAccessor;

public final class LoadingScreenRenderer {
    private static final class_2960 SHADER = class_2960.method_60655((String)"insidethesystem", (String)"core/recoin_loading");
    private static final class_2960 LOGO_ATLAS_TEXTURE = class_2960.method_60655((String)"recoin", (String)"loading/combined_icons_atlas");
    private static final RenderPipeline PIPELINE = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_60125}).withLocation(class_2960.method_60655((String)"recoin", (String)"pipeline/loading")).withVertexShader(SHADER).withFragmentShader(SHADER).withVertexFormat(class_290.field_1584, VertexFormat.class_5596.field_27382).withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());
    private static final long START = System.nanoTime();
    private static String vertexSource;
    private static String fragmentSource;
    private static boolean shaderReady;
    private static boolean failed;
    private static FontAtlas logoAtlas;
    private static FontRenderer logoFont;
    private static int logoFontSize;

    private LoadingScreenRenderer() {
    }

    public static boolean prepare() {
        if (failed) {
            return false;
        }
        try {
            if (vertexSource == null) {
                vertexSource = LoadingScreenRenderer.readShader("vsh");
                fragmentSource = LoadingScreenRenderer.readShader("fsh");
            }
            if (!shaderReady) {
                if (!RenderSystem.getDevice().precompilePipeline(PIPELINE, (id, type) -> type == ShaderType.VERTEX ? vertexSource : fragmentSource).isValid()) {
                    throw new IllegalStateException("Loading shader compilation failed");
                }
                shaderReady = true;
            }
            LoadingScreenRenderer.prepareLogoFont();
            return true;
        }
        catch (IOException | RuntimeException exception) {
            failed = true;
            LoggerFactory.getLogger(LoadingScreenRenderer.class).error("Cannot initialize loading screen", (Throwable)exception);
            return false;
        }
    }

    public static void drawBackground(class_332 context, int width, int height) {
        if (context == null || width <= 0 || height <= 0) {
            return;
        }
        LoadingScreenRenderer.prepare();
        LoadingScreenRenderer.drawThemeBackground(context, width, height, LoadingScreenRenderer.currentThemeColor(), 1.0f);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void draw(class_332 context, float opacity, float progress) {
        int width = context.method_51421();
        int height = context.method_51443();
        int alpha = Math.clamp((long)Math.round(opacity * 255.0f), 0, 255);
        if (alpha == 0 || width <= 0 || height <= 0) {
            return;
        }
        int themeColor = LoadingScreenRenderer.currentThemeColor();
        LoadingScreenRenderer.drawThemeBackground(context, width, height, alpha << 24 | themeColor, (float)alpha / 255.0f);
        float shortSide = Math.min(width, height);
        int logoSize = Math.max(32, Math.round(shortSide * 0.35f));
        if (logoFont == null || logoFontSize != logoSize) {
            logoFont = new FontRenderer(logoAtlas, (float)logoSize / 2.0f, logoSize);
            logoFontSize = logoSize;
        }
        class_332 previousContext = Render2DUtil.getActiveContext();
        Render2DUtil.setActiveContext(context);
        try {
            logoFont.drawCenteredString(MatrixStackCompat.from(context.method_51448()), "\ue0c6", (float)width / 2.0f, (float)height / 2.0f - shortSide * 0.07f, alpha << 24 | themeColor);
            FontRenderer.flushQueuedText(context);
        }
        finally {
            Render2DUtil.setActiveContext(previousContext);
        }
        context.method_71048();
        int barWidth = Math.min(140, width / 3);
        int barX = (width - barWidth) / 2;
        int barY = height / 2 + Math.round((float)Math.min(width, height) * 0.17f);
        context.method_25294(barX, barY, barX + barWidth, barY + 2, Math.round((float)alpha * 0.2f) << 24 | themeColor);
        int filled = Math.clamp((long)Math.round((float)barWidth * progress), 0, barWidth);
        if (filled > 0) {
            context.method_25294(barX, barY, barX + filled, barY + 2, alpha << 24 | themeColor);
        }
    }

    private static void prepareLogoFont() throws IOException {
        if (logoAtlas != null) {
            return;
        }
        FontPipeline.prepareLoadingGuiPipeline();
        FontAtlas atlas = new FontAtlas(class_2960.method_60655((String)"minecraft", (String)"fonts/combined-icons.json"), LOGO_ATLAS_TEXTURE);
        atlas.forceLoad();
        if (atlas.getGlyphCount() == 0) {
            throw new IOException("Loading font atlas has no glyphs");
        }
        try (InputStream stream = LoadingScreenRenderer.class.getResourceAsStream("/assets/minecraft/fonts/combined-icons.png");){
            class_1043 texture;
            if (stream == null) {
                throw new IOException("Missing loading font atlas texture");
            }
            class_1011 image = class_1011.method_4309((InputStream)stream);
            try {
                texture = new class_1043(() -> "recoin_loading_font_atlas", image);
            }
            catch (RuntimeException exception) {
                image.close();
                throw exception;
            }
            try {
                class_310.method_1551().method_1531().method_4616(LOGO_ATLAS_TEXTURE, (class_1044)texture);
            }
            catch (RuntimeException exception) {
                texture.close();
                throw exception;
            }
            logoAtlas = atlas;
        }
    }

    private static int currentThemeColor() {
        try {
            Hud hud = Hud.getInstance();
            return hud == null ? ColorUtil.getClientColor() & 0xFFFFFF : hud.themeColor() & 0xFFFFFF;
        }
        catch (RuntimeException exception) {
            return 11970792;
        }
    }

    private static void drawThemeBackground(class_332 context, int width, int height, int color, float opacity) {
        long cycle = 60000000000L;
        int time = (int)((System.nanoTime() - START) % cycle * 65535L / cycle);
        Matrix3x2f pose = new Matrix3x2f((Matrix3x2fc)context.method_51448());
        float ax = pose.m20();
        float ay = pose.m21();
        float bx = pose.m00() * (float)width + pose.m20();
        float by = pose.m01() * (float)width + pose.m21();
        float cx = pose.m00() * (float)width + pose.m10() * (float)height + pose.m20();
        float cy = pose.m01() * (float)width + pose.m11() * (float)height + pose.m21();
        float dx = pose.m10() * (float)height + pose.m20();
        float dy = pose.m11() * (float)height + pose.m21();
        int left = (int)Math.floor(Math.min(Math.min(ax, bx), Math.min(cx, dx)));
        int top = (int)Math.floor(Math.min(Math.min(ay, by), Math.min(cy, dy)));
        class_8030 bounds = new class_8030(left, top, (int)Math.ceil(Math.max(Math.max(ax, bx), Math.max(cx, dx))) - left, (int)Math.ceil(Math.max(Math.max(ay, by), Math.max(cy, dy))) - top);
        class_8030 scissor = Main.getInstance() == null ? null : Main.getInstance().getScissorManager().currentScreenRect();
        context.method_71048();
        ((DrawContextAccessor)context).recoin$getState().method_70919((class_11244)new LoadingBackgroundElement(ax, ay, bx, by, cx, cy, dx, dy, color & 0xFFFFFF, Math.clamp((long)Math.round(opacity * 255.0f), 0, 255) << 24, time, bounds, scissor));
        context.method_71048();
    }

    private static String readShader(String extension) throws IOException {
        String path = "/assets/insidethesystem/shaders/core/recoin_loading." + extension;
        try (InputStream stream = LoadingScreenRenderer.class.getResourceAsStream(path);){
            if (stream == null) {
                throw new IOException("Missing shader: " + path);
            }
            String string = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            return string;
        }
    }

    private record LoadingBackgroundElement(float ax, float ay, float bx, float by, float cx, float cy, float dx, float dy, int color, int alpha, int time, class_8030 bounds, class_8030 scissorArea) implements class_11244 {

        public void method_70917(class_4588 vertices) {
            int argb = this.alpha | this.color;
            vertices.method_22912(this.ax, this.ay, 0.0f).method_22913(0.0f, 0.0f).method_39415(argb).method_22921(this.time, 0);
            vertices.method_22912(this.dx, this.dy, 0.0f).method_22913(0.0f, 1.0f).method_39415(argb).method_22921(this.time, 0);
            vertices.method_22912(this.cx, this.cy, 0.0f).method_22913(1.0f, 1.0f).method_39415(argb).method_22921(this.time, 0);
            vertices.method_22912(this.bx, this.by, 0.0f).method_22913(1.0f, 0.0f).method_39415(argb).method_22921(this.time, 0);
        }

        public RenderPipeline comp_4055() {
            return PIPELINE;
        }

        public class_11231 comp_4056() {
            return class_11231.method_70899();
        }

        public class_8030 comp_4274() {
            return this.bounds;
        }

        public class_8030 comp_4069() {
            return this.scissorArea;
        }
    }
}

