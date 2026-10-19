/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.DepthTestFunction
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_10789
 *  net.minecraft.class_10799
 *  net.minecraft.class_12247
 *  net.minecraft.class_1921
 *  net.minecraft.class_1922
 *  net.minecraft.class_2338
 *  net.minecraft.class_238
 *  net.minecraft.class_239
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_265
 *  net.minecraft.class_2680
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_3532
 *  net.minecraft.class_3965
 *  net.minecraft.class_4587$class_4665
 *  net.minecraft.class_9801
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package ru.recoin.implement.features.modules.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.class_10789;
import net.minecraft.class_10799;
import net.minecraft.class_12247;
import net.minecraft.class_1921;
import net.minecraft.class_1922;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_3965;
import net.minecraft.class_4587;
import net.minecraft.class_9801;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.ColorSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.render.LegacyRenderSystem;
import ru.recoin.compat.render.BufferRenderer;
import ru.recoin.compat.render.ShaderProgramKeys;
import ru.recoin.implement.events.render.WorldRenderEvent;

public final class BlockOverlay
extends Module {
    private static final class_2960 FILL_SHADER = class_2960.method_60655((String)"minecraft", (String)"core/recoin_block_overlay");
    private static final class_1921[] FILL_LAYERS_DEPTH = new class_1921[8];
    private static final class_1921[] FILL_LAYERS_NO_DEPTH = new class_1921[8];
    private final SelectSetting fillShader = new SelectSetting("Fill Shader", "Animated overlay fill").value("Off", "Energy", "Solid", "Theme", "Starfield", "Plasma", "Nebula", "Cobweb").selected("Energy");
    private final BooleanSetting outline = new BooleanSetting("Outline", "Draw block outline").setValue(true);
    private final BooleanSetting throughBlocks = new BooleanSetting("Through Blocks", "Render overlay and outline through blocks").setValue(false);
    private final ColorSetting color = new ColorSetting("Color", "Solid overlay color").setColor(-428999425).visible(() -> this.fillShader.isSelected("Solid"));
    private final ValueSetting opacity = new ValueSetting("Opacity", "Overlay fill opacity").setValue(70.0f).range(5.0f, 100.0f).step(1.0f).visible(() -> !this.fillShader.isSelected("Off"));
    private final ValueSetting lineWidth = new ValueSetting("Line Width", "Outline thickness").setValue(1.5f).range(0.5f, 4.0f).step(0.1f).visible(this.outline::isValue);
    private final ValueSetting smoothness = new ValueSetting("Smoothness", "How smoothly the ESP moves between blocks").setValue(6.0f).range(1.0f, 10.0f).step(1.0f);
    private class_238 currentBox;
    private Object lastWorld;
    private long lastFrameTime;
    private float visibility;

    private static class_1921 createFillLayer(int mode, boolean depthTest) {
        class_2960 fragmentShader = switch (mode) {
            case 3 -> class_2960.method_60655((String)"minecraft", (String)"core/block_starfield");
            case 4 -> class_2960.method_60655((String)"minecraft", (String)"core/block_plasma");
            case 5 -> class_2960.method_60655((String)"minecraft", (String)"core/block_nebula");
            case 6 -> class_2960.method_60655((String)"minecraft", (String)"core/block_cobweb");
            default -> FILL_SHADER;
        };
        RenderPipeline pipeline = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"recoin", (String)("pipeline/block_overlay_fill_" + mode + (depthTest ? "_depth" : "_nodepth")))).withVertexShader(FILL_SHADER).withFragmentShader(fragmentShader).withShaderDefine("OVERLAY_MODE", mode).withUniform("DynamicTransforms", class_10789.field_60031).withUniform("Projection", class_10789.field_60031).withVertexFormat(class_290.field_1577, VertexFormat.class_5596.field_27382).withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(depthTest ? DepthTestFunction.LEQUAL_DEPTH_TEST : DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());
        return class_1921.method_75940((String)("recoin_block_overlay_fill_" + mode + (depthTest ? "_depth" : "_nodepth")), (class_12247)class_12247.method_75927((RenderPipeline)pipeline).method_75937().method_75929(256).method_75938());
    }

    public static BlockOverlay getInstance() {
        return Instance.get(BlockOverlay.class);
    }

    public BlockOverlay() {
        super("BlockOverlay", "Block Overlay", ModuleCategory.RENDER);
        this.setup(this.fillShader, this.outline, this.throughBlocks, this.color, this.opacity, this.lineWidth, this.smoothness);
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        float amount;
        if (BlockOverlay.mc.field_1687 == null || BlockOverlay.mc.field_1724 == null) {
            return;
        }
        long now = System.nanoTime();
        float dt = this.lastFrameTime == 0L ? 0.016666668f : Math.min(0.1f, (float)(now - this.lastFrameTime) / 1.0E9f);
        this.lastFrameTime = now;
        class_238 targetBox = this.targetBox();
        if (this.lastWorld != BlockOverlay.mc.field_1687) {
            this.lastWorld = BlockOverlay.mc.field_1687;
            this.currentBox = targetBox;
            this.visibility = 0.0f;
        }
        if ((amount = this.smoothness.getValue()) <= 1.0f) {
            this.currentBox = targetBox;
            this.visibility = targetBox == null ? 0.0f : 1.0f;
        } else {
            float progress = class_3532.method_15363((float)((amount - 2.0f) / 8.0f), (float)0.0f, (float)1.0f);
            float response = class_3532.method_16439((float)progress, (float)18.0f, (float)3.0f);
            float easing = 1.0f - (float)Math.exp(-response * dt);
            if (targetBox != null) {
                this.currentBox = this.currentBox == null ? targetBox : BlockOverlay.lerpBox(this.currentBox, targetBox, easing);
                this.visibility += (1.0f - this.visibility) * easing;
            } else {
                this.visibility += (0.0f - this.visibility) * easing;
                if (this.visibility < 0.01f) {
                    this.currentBox = null;
                }
            }
        }
        if (this.currentBox == null) {
            return;
        }
        int overlayColor = this.shaderVertexColor(this.currentBox);
        if (!this.fillShader.isSelected("Off")) {
            this.renderShaderFill(event, this.currentBox, overlayColor);
        }
        if (this.outline.isValue()) {
            this.renderOutline(event, this.currentBox, this.animatedColor(this.currentBox), this.visibility);
        }
    }

    private class_238 targetBox() {
        class_3965 result;
        class_239 class_2392 = BlockOverlay.mc.field_1765;
        if (!(class_2392 instanceof class_3965) || (result = (class_3965)class_2392).method_17783() != class_239.class_240.field_1332) {
            return null;
        }
        class_2338 pos = result.method_17777();
        class_2680 state = BlockOverlay.mc.field_1687.method_8320(pos);
        if (state.method_26215()) {
            return null;
        }
        class_265 shape = state.method_26218((class_1922)BlockOverlay.mc.field_1687, pos);
        return shape.method_1110() ? null : shape.method_1107().method_996(pos);
    }

    private static class_238 lerpBox(class_238 from, class_238 to, float delta) {
        return new class_238(class_3532.method_16436((double)delta, (double)from.field_1323, (double)to.field_1323), class_3532.method_16436((double)delta, (double)from.field_1322, (double)to.field_1322), class_3532.method_16436((double)delta, (double)from.field_1321, (double)to.field_1321), class_3532.method_16436((double)delta, (double)from.field_1320, (double)to.field_1320), class_3532.method_16436((double)delta, (double)from.field_1325, (double)to.field_1325), class_3532.method_16436((double)delta, (double)from.field_1324, (double)to.field_1324));
    }

    private void renderShaderFill(WorldRenderEvent event, class_238 worldBox, int overlayColor) {
        class_9801 built;
        class_287 buffer = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27382, class_290.field_1577);
        class_243 camera = BlockOverlay.mc.field_1773.method_19418().method_71156();
        class_4587.class_4665 entry = event.getStack().method_23760();
        class_238 b = worldBox.method_989(-camera.field_1352, -camera.field_1351, -camera.field_1350).method_1014(0.002);
        if (camera.field_1351 <= worldBox.field_1322) {
            this.quad(buffer, entry, b.field_1323, b.field_1322, b.field_1321, b.field_1320, b.field_1322, b.field_1321, b.field_1320, b.field_1322, b.field_1324, b.field_1323, b.field_1322, b.field_1324, overlayColor, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f);
        }
        if (camera.field_1351 >= worldBox.field_1325) {
            this.quad(buffer, entry, b.field_1323, b.field_1325, b.field_1324, b.field_1320, b.field_1325, b.field_1324, b.field_1320, b.field_1325, b.field_1321, b.field_1323, b.field_1325, b.field_1321, overlayColor, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f);
        }
        if (camera.field_1352 <= worldBox.field_1323) {
            this.quad(buffer, entry, b.field_1323, b.field_1322, b.field_1321, b.field_1323, b.field_1322, b.field_1324, b.field_1323, b.field_1325, b.field_1324, b.field_1323, b.field_1325, b.field_1321, overlayColor, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f);
        }
        if (camera.field_1352 >= worldBox.field_1320) {
            this.quad(buffer, entry, b.field_1320, b.field_1322, b.field_1324, b.field_1320, b.field_1322, b.field_1321, b.field_1320, b.field_1325, b.field_1321, b.field_1320, b.field_1325, b.field_1324, overlayColor, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f);
        }
        if (camera.field_1350 >= worldBox.field_1324) {
            this.quad(buffer, entry, b.field_1323, b.field_1322, b.field_1324, b.field_1320, b.field_1322, b.field_1324, b.field_1320, b.field_1325, b.field_1324, b.field_1323, b.field_1325, b.field_1324, overlayColor, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f);
        }
        if (camera.field_1350 <= worldBox.field_1321) {
            this.quad(buffer, entry, b.field_1320, b.field_1322, b.field_1321, b.field_1323, b.field_1322, b.field_1321, b.field_1323, b.field_1325, b.field_1321, b.field_1320, b.field_1325, b.field_1321, overlayColor, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f);
        }
        if ((built = buffer.method_60794()) != null) {
            (this.throughBlocks.isValue() ? FILL_LAYERS_NO_DEPTH : FILL_LAYERS_DEPTH)[this.fillMode()].method_60895(built);
        }
    }

    private void quad(class_287 b, class_4587.class_4665 e, double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, int color, float u1, float v1, float u2, float v2, float u3, float v3, float u4, float v4) {
        float alpha = this.opacity.getValue() / 100.0f * this.visibility;
        b.method_56824(e, (float)x1, (float)y1, (float)z1).method_22913(u1, v1).method_39415(color).method_22914(alpha, 0.0f, 0.0f);
        b.method_56824(e, (float)x2, (float)y2, (float)z2).method_22913(u2, v2).method_39415(color).method_22914(alpha, 0.0f, 0.0f);
        b.method_56824(e, (float)x3, (float)y3, (float)z3).method_22913(u3, v3).method_39415(color).method_22914(alpha, 0.0f, 0.0f);
        b.method_56824(e, (float)x4, (float)y4, (float)z4).method_22913(u4, v4).method_39415(color).method_22914(alpha, 0.0f, 0.0f);
    }

    private int animatedColor(class_238 box) {
        float time = (float)(System.currentTimeMillis() % 12000L) / 12000.0f;
        float spatial = (float)(box.method_1005().field_1352 * (double)0.071f + box.method_1005().field_1351 * (double)0.113f + box.method_1005().field_1350 * (double)0.053f);
        float wave = 0.5f + 0.5f * (float)Math.sin((double)(time * 2.0f + spatial) * Math.PI * 2.0);
        int result = switch (this.fillShader.getSelected()) {
            case "Solid", "Energy" -> this.color.getColor();
            case "Theme" -> ColorUtil.getClientColor();
            case "Nebula" -> ColorUtil.overCol(-8692481, -13051393, wave);
            case "Starfield", "Plasma", "Cobweb" -> ColorUtil.getClientColor();
            default -> ColorUtil.getClientColor();
        };
        return ColorUtil.replAlpha(result, this.fillShader.isSelected("Off") ? 0.92f : 0.78f);
    }

    private int fillMode() {
        return switch (this.fillShader.getSelected()) {
            case "Off" -> 0;
            case "Solid" -> 1;
            case "Theme" -> 2;
            case "Starfield" -> 3;
            case "Plasma" -> 4;
            case "Nebula" -> 5;
            case "Cobweb" -> 6;
            case "Energy" -> 7;
            default -> 7;
        };
    }

    private int shaderVertexColor(class_238 box) {
        int rgb = this.animatedColor(box) & 0xFFFFFF;
        int phase = (int)(System.currentTimeMillis() % 10000L * 255L / 10000L);
        return phase << 24 | rgb;
    }

    public int vanillaOutlineColor() {
        return 0;
    }

    public float vanillaLineWidth() {
        return this.lineWidth.getValue();
    }

    public void enqueueVanillaFill() {
    }

    private void renderOutline(WorldRenderEvent event, class_238 worldBox, int color, float alpha) {
        class_243 camera = BlockOverlay.mc.field_1773.method_19418().method_71156();
        class_238 b = worldBox.method_989(-camera.field_1352, -camera.field_1351, -camera.field_1350).method_1014(0.003);
        Matrix4f matrix = event.getStack().method_23760().method_23761();
        int outlineColor = ColorUtil.replAlpha(color, class_3532.method_15363((float)alpha, (float)0.0f, (float)1.0f));
        LegacyRenderSystem.enableBlend();
        if (this.throughBlocks.isValue()) {
            LegacyRenderSystem.disableDepthTest();
        } else {
            LegacyRenderSystem.enableDepthTest();
        }
        LegacyRenderSystem.lineWidth(this.lineWidth.getValue());
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        class_287 buffer = class_289.method_1348().method_60827(VertexFormat.class_5596.field_29344, class_290.field_1576);
        BlockOverlay.line(buffer, matrix, b.field_1323, b.field_1322, b.field_1321, b.field_1320, b.field_1322, b.field_1321, outlineColor);
        BlockOverlay.line(buffer, matrix, b.field_1320, b.field_1322, b.field_1321, b.field_1320, b.field_1322, b.field_1324, outlineColor);
        BlockOverlay.line(buffer, matrix, b.field_1320, b.field_1322, b.field_1324, b.field_1323, b.field_1322, b.field_1324, outlineColor);
        BlockOverlay.line(buffer, matrix, b.field_1323, b.field_1322, b.field_1324, b.field_1323, b.field_1322, b.field_1321, outlineColor);
        BlockOverlay.line(buffer, matrix, b.field_1323, b.field_1325, b.field_1321, b.field_1320, b.field_1325, b.field_1321, outlineColor);
        BlockOverlay.line(buffer, matrix, b.field_1320, b.field_1325, b.field_1321, b.field_1320, b.field_1325, b.field_1324, outlineColor);
        BlockOverlay.line(buffer, matrix, b.field_1320, b.field_1325, b.field_1324, b.field_1323, b.field_1325, b.field_1324, outlineColor);
        BlockOverlay.line(buffer, matrix, b.field_1323, b.field_1325, b.field_1324, b.field_1323, b.field_1325, b.field_1321, outlineColor);
        BlockOverlay.line(buffer, matrix, b.field_1323, b.field_1322, b.field_1321, b.field_1323, b.field_1325, b.field_1321, outlineColor);
        BlockOverlay.line(buffer, matrix, b.field_1320, b.field_1322, b.field_1321, b.field_1320, b.field_1325, b.field_1321, outlineColor);
        BlockOverlay.line(buffer, matrix, b.field_1320, b.field_1322, b.field_1324, b.field_1320, b.field_1325, b.field_1324, outlineColor);
        BlockOverlay.line(buffer, matrix, b.field_1323, b.field_1322, b.field_1324, b.field_1323, b.field_1325, b.field_1324, outlineColor);
        BufferRenderer.drawWithGlobalProgram(buffer.method_60800());
        LegacyRenderSystem.lineWidth(1.0f);
        LegacyRenderSystem.enableDepthTest();
        LegacyRenderSystem.disableBlend();
    }

    private static void line(class_287 buffer, Matrix4f matrix, double x1, double y1, double z1, double x2, double y2, double z2, int color) {
        buffer.method_22918((Matrix4fc)matrix, (float)x1, (float)y1, (float)z1).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, (float)x2, (float)y2, (float)z2).method_39415(color);
    }

    @Override
    public void deactivate() {
        this.currentBox = null;
        this.lastWorld = null;
        this.lastFrameTime = 0L;
        this.visibility = 0.0f;
    }

    static {
        for (int mode = 1; mode <= 7; ++mode) {
            BlockOverlay.FILL_LAYERS_DEPTH[mode] = BlockOverlay.createFillLayer(mode, true);
            BlockOverlay.FILL_LAYERS_NO_DEPTH[mode] = BlockOverlay.createFillLayer(mode, false);
        }
    }
}

