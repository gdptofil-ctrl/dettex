/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.DepthTestFunction
 *  com.mojang.blaze3d.platform.DestFactor
 *  com.mojang.blaze3d.platform.SourceFactor
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_10799
 *  net.minecraft.class_11231
 *  net.minecraft.class_11244
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
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
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.class_10799;
import net.minecraft.class_11231;
import net.minecraft.class_11244;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_4588;
import net.minecraft.class_8030;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.mixins.DrawContextAccessor;

public final class MenuGlowPipeline {
    private static final class_2960 SHADER = class_2960.method_60655((String)"insidethesystem", (String)"core/recoin_menu_glow");
    private static final RenderPipeline PIPELINE = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_60125}).withLocation(class_2960.method_60655((String)"recoin", (String)"pipeline/menu_glow")).withVertexShader(SHADER).withFragmentShader(SHADER).withVertexFormat(class_290.field_1575, VertexFormat.class_5596.field_27382).withBlend(new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE)).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());

    public void draw(class_332 context, float centerX, float centerY, float width, float height, int color) {
        if (context == null || width <= 0.0f || height <= 0.0f) {
            return;
        }
        if ((color = Render2DUtil.applyRenderAlpha(color)) >>> 24 == 0) {
            return;
        }
        float x1 = centerX - width / 2.0f;
        float y1 = centerY - height / 2.0f;
        float x2 = centerX + width / 2.0f;
        float y2 = centerY + height / 2.0f;
        Matrix3x2f matrix = new Matrix3x2f((Matrix3x2fc)context.method_51448());
        int left = (int)Math.floor(x1);
        int top = (int)Math.floor(y1);
        class_8030 bounds = new class_8030(left, top, (int)Math.ceil(x2) - left, (int)Math.ceil(y2) - top).method_71523((Matrix3x2fc)matrix);
        ((DrawContextAccessor)context).recoin$getState().method_70919((class_11244)new GlowElement(matrix, x1, y1, x2, y2, color, bounds));
    }

    private record GlowElement(Matrix3x2f matrix, float x1, float y1, float x2, float y2, int color, class_8030 bounds) implements class_11244 {

        public void method_70917(class_4588 consumer) {
            consumer.method_70815((Matrix3x2fc)this.matrix, this.x1, this.y1).method_22913(-1.0f, -1.0f).method_39415(this.color);
            consumer.method_70815((Matrix3x2fc)this.matrix, this.x1, this.y2).method_22913(-1.0f, 1.0f).method_39415(this.color);
            consumer.method_70815((Matrix3x2fc)this.matrix, this.x2, this.y2).method_22913(1.0f, 1.0f).method_39415(this.color);
            consumer.method_70815((Matrix3x2fc)this.matrix, this.x2, this.y1).method_22913(1.0f, -1.0f).method_39415(this.color);
        }

        public RenderPipeline comp_4055() {
            return PIPELINE;
        }

        public class_11231 comp_4056() {
            return class_11231.method_70899();
        }

        public class_8030 comp_4069() {
            return null;
        }

        public class_8030 comp_4274() {
            return this.bounds;
        }
    }
}

