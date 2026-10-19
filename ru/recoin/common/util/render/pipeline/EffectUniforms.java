/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  net.minecraft.class_11280
 *  net.minecraft.class_11280$class_11281
 */
package ru.recoin.common.util.render.pipeline;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import java.nio.ByteBuffer;
import net.minecraft.class_11280;

public final class EffectUniforms {
    private static class_11280<Upload> storage;

    private EffectUniforms() {
    }

    public static GpuBufferSlice write(ByteBuffer data) {
        if (data.remaining() > 256) {
            throw new IllegalArgumentException("Effect uniform block exceeds 256 bytes");
        }
        if (storage == null) {
            storage = new class_11280("recoin:effect_uniforms", 256, 256);
        }
        return storage.method_71102(new Upload(data));
    }

    public static void beginFrame() {
        if (storage != null) {
            storage.method_71100();
        }
    }

    public static void close() {
        if (storage != null) {
            storage.close();
            storage = null;
        }
    }

    private static final class Upload
    implements class_11280.class_11281 {
        private final ByteBuffer source;

        private Upload(ByteBuffer source) {
            this.source = source;
        }

        public void method_71104(ByteBuffer target) {
            target.put(this.source);
        }
    }
}

