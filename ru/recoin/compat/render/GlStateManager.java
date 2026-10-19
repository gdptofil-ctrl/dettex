/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL13
 */
package ru.recoin.compat.render;

import org.lwjgl.opengl.GL13;

public final class GlStateManager {
    private GlStateManager() {
    }

    public static void _activeTexture(int texture) {
        GL13.glActiveTexture((int)texture);
    }

    public static enum DstFactor {
        ZERO(0),
        ONE(1),
        ONE_MINUS_SRC_ALPHA(771),
        ONE_MINUS_CONSTANT_ALPHA(32772),
        ONE_MINUS_SRC_COLOR(769);

        public final int value;

        private DstFactor(int value) {
            this.value = value;
        }
    }

    public static enum SrcFactor {
        ZERO(0),
        ONE(1),
        SRC_ALPHA(770),
        ONE_MINUS_SRC_ALPHA(771);

        public final int value;

        private SrcFactor(int value) {
            this.value = value;
        }
    }
}

