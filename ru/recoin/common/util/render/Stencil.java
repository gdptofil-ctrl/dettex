/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
package ru.recoin.common.util.render;

import org.lwjgl.opengl.GL11;
import ru.recoin.common.QuickImports;

public final class Stencil
implements QuickImports {
    public static void push() {
        GL11.glStencilMask((int)255);
        GL11.glClear((int)1024);
        GL11.glEnable((int)2960);
        GL11.glStencilFunc((int)519, (int)1, (int)1);
        GL11.glStencilOp((int)7681, (int)7681, (int)7681);
        GL11.glDisable((int)2929);
        GL11.glColorMask((boolean)false, (boolean)false, (boolean)false, (boolean)false);
    }

    public static void read(int ref) {
        GL11.glColorMask((boolean)true, (boolean)true, (boolean)true, (boolean)true);
        GL11.glStencilFunc((int)514, (int)ref, (int)1);
        GL11.glStencilOp((int)7680, (int)7680, (int)7680);
    }

    public static void pop() {
        GL11.glDisable((int)2960);
        GL11.glEnable((int)2929);
    }

    private Stencil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

