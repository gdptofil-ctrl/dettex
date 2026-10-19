/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  org.joml.Matrix4f
 */
package ru.recoin.api.system.shape.implement;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import org.joml.Matrix4f;
import ru.recoin.api.system.shape.Shape;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.render.ShapeShaderRenderer;
import ru.recoin.compat.render.BufferRenderer;

public class Arc
implements Shape,
QuickImports {
    @Override
    public void render(ShapeProperties shape) {
        if (ShapeShaderRenderer.renderArc(shape)) {
            return;
        }
        Matrix4f matrix4f = shape.getMatrix().method_23760().method_23761();
        class_287 buffer = ((class_289)tessellator.get()).method_60827(VertexFormat.class_5596.field_27382, class_290.field_1576);
        drawEngine.quad(matrix4f, buffer, shape.getX(), shape.getY(), shape.getWidth(), shape.getHeight(), shape.getColor().x);
        BufferRenderer.drawWithGlobalProgram(buffer.method_60800());
    }
}

