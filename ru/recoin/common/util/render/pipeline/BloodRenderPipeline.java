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
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_10789
 *  net.minecraft.class_10799
 *  net.minecraft.class_243
 *  net.minecraft.class_276
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_9801
 *  org.joml.Matrix4f
 *  org.lwjgl.system.MemoryUtil
 */
package ru.recoin.common.util.render.pipeline;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.class_10789;
import net.minecraft.class_10799;
import net.minecraft.class_243;
import net.minecraft.class_276;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_9801;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

public final class BloodRenderPipeline
implements AutoCloseable {
    private static final class_2960 SHADER = class_2960.method_60655((String)"minecraft", (String)"core/recoin_blood");
    private static final RenderPipeline PIPELINE = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"recoin", (String)"pipeline/blood")).withVertexShader(SHADER).withFragmentShader(SHADER).withVertexFormat(class_290.field_1577, VertexFormat.class_5596.field_27379).withUniform("BloodScene", class_10789.field_60031).withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST).withDepthWrite(false).withCull(false).withBlend(BlendFunction.TRANSLUCENT).build());
    private final GpuBuffer[] vertices = new GpuBuffer[3];
    private final GpuBuffer[] uniforms = new GpuBuffer[3];
    private final ByteBuffer data = MemoryUtil.memAlloc((int)96);
    private int frame;

    public void draw(class_9801 mesh, Matrix4f viewProjection, class_243 camera, long worldTime, float rain) {
        double phase;
        float sunHeight;
        class_276 framebuffer = class_310.method_1551().method_1522();
        if (framebuffer == null || framebuffer.method_71639() == null || framebuffer.method_71640() == null) {
            return;
        }
        int slot = this.frame;
        this.frame = (this.frame + 1) % this.vertices.length;
        ByteBuffer geometry = mesh.method_60818();
        if (this.vertices[slot] == null || this.vertices[slot].size() < (long)geometry.remaining()) {
            if (this.vertices[slot] != null) {
                this.vertices[slot].close();
            }
            int capacity = Math.max(65536, Integer.highestOneBit(geometry.remaining() - 1) << 1);
            this.vertices[slot] = RenderSystem.getDevice().createBuffer(() -> "recoin:blood_vertices", 40, (long)capacity);
        }
        if (this.uniforms[slot] == null) {
            this.uniforms[slot] = RenderSystem.getDevice().createBuffer(() -> "recoin:blood_scene", 136, (long)this.data.capacity());
        }
        float direction = (sunHeight = (float)Math.sin(phase = (double)Math.floorMod(worldTime, 24000L) / 24000.0 * Math.PI * 2.0)) >= 0.0f ? 1.0f : -1.0f;
        this.data.clear();
        viewProjection.get(this.data);
        this.data.position(64);
        this.data.putFloat((float)Math.cos(phase) * direction).putFloat(sunHeight * direction).putFloat(0.3f).putFloat((sunHeight >= 0.0f ? 1.0f : 0.12f) * (1.0f - rain * 0.65f));
        this.data.putFloat((float)(camera.field_1352 % 64.0)).putFloat((float)(camera.field_1351 % 64.0)).putFloat((float)(camera.field_1350 % 64.0)).putFloat(64.0f).flip();
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.writeToBuffer(this.vertices[slot].slice(0L, (long)geometry.remaining()), geometry);
        encoder.writeToBuffer(this.uniforms[slot].slice(), this.data);
        try (RenderPass pass = encoder.createRenderPass(() -> "recoin:blood_pass", framebuffer.method_71639(), OptionalInt.empty(), framebuffer.method_71640(), OptionalDouble.empty());){
            pass.setPipeline(PIPELINE);
            pass.setUniform("BloodScene", this.uniforms[slot]);
            pass.setVertexBuffer(0, this.vertices[slot]);
            pass.draw(0, mesh.method_60822().comp_750());
        }
    }

    @Override
    public void close() {
        for (int i = 0; i < this.vertices.length; ++i) {
            if (this.vertices[i] != null) {
                this.vertices[i].close();
            }
            if (this.uniforms[i] != null) {
                this.uniforms[i].close();
            }
            this.vertices[i] = null;
            this.uniforms[i] = null;
        }
        MemoryUtil.memFree((Buffer)this.data);
    }
}

