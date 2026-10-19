/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_4587
 *  org.joml.Matrix3x2fc
 */
package ru.recoin.common.util.render;

import net.minecraft.class_4587;
import org.joml.Matrix3x2fc;

public final class MatrixStackCompat {
    private static final ThreadLocal<class_4587> REUSABLE_STACK = ThreadLocal.withInitial(class_4587::new);

    private MatrixStackCompat() {
    }

    public static class_4587 from(Object source) {
        if (source instanceof class_4587) {
            class_4587 stack = (class_4587)source;
            return stack;
        }
        class_4587 stack = REUSABLE_STACK.get();
        if (source instanceof Matrix3x2fc) {
            Matrix3x2fc matrix = (Matrix3x2fc)source;
            stack.method_23760().method_23761().set(matrix.m00(), matrix.m01(), 0.0f, 0.0f, matrix.m10(), matrix.m11(), 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, matrix.m20(), matrix.m21(), 0.0f, 1.0f);
        }
        return stack;
    }
}

