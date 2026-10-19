/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  net.minecraft.class_4587$class_4665
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector4i
 */
package ru.recoin.api.system.draw;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_4587;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4i;
import ru.recoin.api.system.draw.DrawEngine;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.render.LegacyRenderSystem;
import ru.recoin.compat.render.BufferRenderer;
import ru.recoin.compat.render.ShaderProgramKeys;

public class DrawEngineImpl
implements DrawEngine,
QuickImports {
    @Override
    public void quad(Matrix4f matrix4f, class_287 buffer, float x, float y, float width, float height) {
        buffer.method_22918((Matrix4fc)matrix4f, x, y, 0.0f);
        buffer.method_22918((Matrix4fc)matrix4f, x, y + height, 0.0f);
        buffer.method_22918((Matrix4fc)matrix4f, x + width, y + height, 0.0f);
        buffer.method_22918((Matrix4fc)matrix4f, x + width, y, 0.0f);
    }

    @Override
    public void quad(Matrix4f matrix4f, class_287 buffer, float x, float y, float width, float height, int color) {
        buffer.method_22918((Matrix4fc)matrix4f, x, y, 0.0f).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix4f, x, y + height, 0.0f).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix4f, x + width, y + height, 0.0f).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix4f, x + width, y, 0.0f).method_39415(color);
    }

    @Override
    public void quad(Matrix4f matrix4f, float x, float y, float width, float height, int color) {
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        class_287 buffer = ((class_289)tessellator.get()).method_60827(VertexFormat.class_5596.field_27382, class_290.field_1575);
        buffer.method_22918((Matrix4fc)matrix4f, x, y + height, 0.0f).method_22913(0.0f, 0.0f).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix4f, x + width, y + height, 0.0f).method_22913(0.0f, 1.0f).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix4f, x + width, y, 0.0f).method_22913(1.0f, 1.0f).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix4f, x, y, 0.0f).method_22913(1.0f, 0.0f).method_39415(color);
        BufferRenderer.drawWithGlobalProgram(buffer.method_60800());
    }

    @Override
    public void quadTexture(class_4587.class_4665 entry, class_287 buffer, float x, float y, float width, float height, Vector4i color) {
        buffer.method_56824(entry, x, y + height, 0.0f).method_22913(0.0f, 0.0f).method_39415(color.x);
        buffer.method_56824(entry, x + width, y + height, 0.0f).method_22913(0.0f, 1.0f).method_39415(color.y);
        buffer.method_56824(entry, x + width, y, 0.0f).method_22913(1.0f, 1.0f).method_39415(color.w);
        buffer.method_56824(entry, x, y, 0.0f).method_22913(1.0f, 0.0f).method_39415(color.z);
    }
}

