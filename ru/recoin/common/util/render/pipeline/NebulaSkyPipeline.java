/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.DepthTestFunction
 *  com.mojang.blaze3d.systems.CommandEncoder
 *  com.mojang.blaze3d.systems.GpuDevice
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_10789
 *  net.minecraft.class_10799
 *  net.minecraft.class_276
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_4184
 *  org.joml.Quaternionf
 *  org.lwjgl.system.MemoryStack
 */
package ru.recoin.common.util.render.pipeline;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import net.minecraft.class_10789;
import net.minecraft.class_10799;
import net.minecraft.class_276;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_4184;
import org.joml.Quaternionf;
import org.lwjgl.system.MemoryStack;

public final class NebulaSkyPipeline {
    private static final class_2960 SHADER = class_2960.method_60655((String)"insidethesystem", (String)"core/nebula_sky");
    private static final RenderPipeline PIPELINE = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"recoin", (String)"pipeline/nebula_sky")).withVertexShader(SHADER).withFragmentShader(SHADER).withVertexFormat(class_290.field_60033, VertexFormat.class_5596.field_27379).withUniform("SkyShaderData", class_10789.field_60031).withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());
    private static final NebulaSkyPipeline INSTANCE = new NebulaSkyPipeline();
    private static final long START = System.nanoTime();
    private GpuBuffer uniform;
    private GpuBuffer vertex;

    public static NebulaSkyPipeline getInstance() {
        return INSTANCE;
    }

    public void draw(class_276 target, class_4184 camera, float fov, int primary, int secondary) {
        if (target == null || target.method_71639() == null || camera == null) {
            return;
        }
        GpuDevice device = RenderSystem.getDevice();
        if (this.uniform == null) {
            this.uniform = device.createBuffer(() -> "recoin:nebula_uniform", 136, 160L);
            this.vertex = device.createBuffer(() -> "recoin:nebula_vertex", 32, 4L);
        }
        try (MemoryStack stack = MemoryStack.stackPush();){
            ByteBuffer data = stack.malloc(160);
            data.putFloat(target.field_1482).putFloat(target.field_1481).putFloat((float)(System.nanoTime() - START) / 1.0E9f).putFloat(1.0f);
            data.putFloat(1.15f).putFloat(0.92f).putFloat(fov).putFloat(0.0f);
            Quaternionf rotation = camera.method_23767();
            data.putFloat(rotation.x()).putFloat(rotation.y()).putFloat(rotation.z()).putFloat(rotation.w());
            NebulaSkyPipeline.color(data, 1054511);
            NebulaSkyPipeline.color(data, 5856131);
            NebulaSkyPipeline.color(data, primary);
            NebulaSkyPipeline.color(data, secondary);
            NebulaSkyPipeline.color(data, 16773323);
            NebulaSkyPipeline.color(data, 0x8998BB);
            data.putFloat(0.0f).putFloat(1.0f).putFloat(0.0f).putFloat(0.0f);
            data.flip();
            CommandEncoder encoder = device.createCommandEncoder();
            encoder.writeToBuffer(this.uniform.slice(), data);
            try (RenderPass pass = encoder.createRenderPass(() -> "recoin:nebula_sky", target.method_71639(), OptionalInt.empty());){
                pass.setPipeline(PIPELINE);
                pass.setVertexBuffer(0, this.vertex);
                pass.setUniform("SkyShaderData", this.uniform);
                pass.draw(0, 3);
            }
        }
    }

    private static void color(ByteBuffer data, int color) {
        data.putFloat((float)(color >> 16 & 0xFF) / 255.0f).putFloat((float)(color >> 8 & 0xFF) / 255.0f).putFloat((float)(color & 0xFF) / 255.0f).putFloat(1.0f);
    }
}

