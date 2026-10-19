/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2960
 *  net.minecraft.class_4587
 *  net.minecraft.class_7833
 *  org.joml.Quaternionfc
 */
package ru.recoin.api.system.shape.implement;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import org.joml.Quaternionfc;
import ru.recoin.api.system.shape.Shape;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.render.LegacyRenderSystem;
import ru.recoin.common.util.render.ShapeShaderRenderer;

public class Image
implements Shape,
QuickImports {
    private static final Map<String, class_2960> TEXTURE_CACHE = new ConcurrentHashMap<String, class_2960>();
    private String texture;

    @Override
    public void render(ShapeProperties shape) {
        class_2960 id = TEXTURE_CACHE.computeIfAbsent(this.texture, class_2960::method_60654);
        if (ShapeShaderRenderer.renderIcon(shape, id, shape.getColor().x)) {
            return;
        }
        class_4587 matrix = shape.getMatrix();
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.defaultBlendFunc();
        LegacyRenderSystem.setShaderTexture(0, id);
        float width = shape.getWidth();
        float x = shape.getX() + width;
        float y = shape.getY();
        matrix.method_22903();
        matrix.method_46416(x, y, 0.0f);
        matrix.method_22907((Quaternionfc)class_7833.field_40718.rotationDegrees(90.0f));
        matrix.method_46416(-x, -y, 0.0f);
        drawEngine.quad(matrix.method_23760().method_23761(), x, y, shape.getHeight(), width, shape.getColor().x);
        matrix.method_22909();
        LegacyRenderSystem.disableBlend();
    }

    public Image setTexture(String texture) {
        this.texture = texture;
        return this;
    }
}

