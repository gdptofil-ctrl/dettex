/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Builder
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.DepthTestFunction
 *  com.mojang.blaze3d.platform.DestFactor
 *  com.mojang.blaze3d.platform.SourceFactor
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.FilterMode
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_1044
 *  net.minecraft.class_10799
 *  net.minecraft.class_11231
 *  net.minecraft.class_11244
 *  net.minecraft.class_11246
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_4588
 *  net.minecraft.class_8030
 *  org.joml.Matrix3x2f
 *  org.joml.Matrix3x2fc
 */
package ru.recoin.common.util.render.pipeline;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.class_1044;
import net.minecraft.class_10799;
import net.minecraft.class_11231;
import net.minecraft.class_11244;
import net.minecraft.class_11246;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4588;
import net.minecraft.class_8030;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import ru.recoin.common.util.render.GuiTextureCache;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.mixins.DrawContextAccessor;

public final class MenuLogoPipeline {
    private static final class_2960 TEXTURE = class_2960.method_60655((String)"insidethesystem", (String)"textures/gui/menu_logo_distance.png");
    private static final RenderPipeline PIPELINE = MenuLogoPipeline.createPipeline(false);
    private static final RenderPipeline GLOW_PIPELINE = MenuLogoPipeline.createPipeline(true);

    private static RenderPipeline createPipeline(boolean glow) {
        RenderPipeline.Builder builder = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_60125}).withLocation(class_2960.method_60655((String)"recoin", (String)(glow ? "pipeline/menu_logo_glow" : "pipeline/menu_logo"))).withVertexShader(class_2960.method_60655((String)"insidethesystem", (String)"core/recoin_menu_glow")).withFragmentShader(class_2960.method_60655((String)"insidethesystem", (String)"core/recoin_menu_logo")).withVertexFormat(class_290.field_1575, VertexFormat.class_5596.field_27382).withSampler("Sampler0").withBlend(glow ? new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE) : BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false);
        if (glow) {
            builder.withShaderDefine("LOGO_GLOW");
        }
        return class_10799.method_67887((RenderPipeline)builder.build());
    }

    public void draw(class_332 context, float x, float y, float width, float height, int color) {
        color = Render2DUtil.applyRenderAlpha(color);
        if (context == null || width <= 0.0f || height <= 0.0f || color >>> 24 == 0) {
            return;
        }
        float paddingX = width / 6.0f;
        float paddingY = height * 128.0f / 605.0f;
        float x1 = x - paddingX;
        float y1 = y - paddingY;
        float x2 = x + width + paddingX;
        float y2 = y + height + paddingY;
        Matrix3x2f matrix = new Matrix3x2f((Matrix3x2fc)context.method_51448());
        int left = (int)Math.floor(x1);
        int top = (int)Math.floor(y1);
        class_8030 bounds = new class_8030(left, top, (int)Math.ceil(x2) - left, (int)Math.ceil(y2) - top).method_71523((Matrix3x2fc)matrix);
        class_11246 state = ((DrawContextAccessor)context).recoin$getState();
        state.method_70919((class_11244)new LogoElement(matrix, x1, y1, x2, y2, color, bounds, true));
        context.method_71048();
        state.method_70919((class_11244)new LogoElement(matrix, x1, y1, x2, y2, color, bounds, false));
    }

    private record LogoElement(Matrix3x2f matrix, float x1, float y1, float x2, float y2, int color, class_8030 bounds, boolean glow) implements class_11244 {

        public void method_70917(class_4588 consumer) {
            consumer.method_70815((Matrix3x2fc)this.matrix, this.x1, this.y1).method_22913(0.0f, 0.0f).method_39415(this.color);
            consumer.method_70815((Matrix3x2fc)this.matrix, this.x1, this.y2).method_22913(0.0f, 1.0f).method_39415(this.color);
            consumer.method_70815((Matrix3x2fc)this.matrix, this.x2, this.y2).method_22913(1.0f, 1.0f).method_39415(this.color);
            consumer.method_70815((Matrix3x2fc)this.matrix, this.x2, this.y1).method_22913(1.0f, 0.0f).method_39415(this.color);
        }

        public RenderPipeline comp_4055() {
            return this.glow ? GLOW_PIPELINE : PIPELINE;
        }

        public class_11231 comp_4056() {
            class_1044 texture = class_310.method_1551().method_1531().method_4619(TEXTURE);
            return GuiTextureCache.get(texture.method_71659(), RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR));
        }

        public class_8030 comp_4069() {
            return null;
        }

        public class_8030 comp_4274() {
            return this.bounds;
        }
    }
}

