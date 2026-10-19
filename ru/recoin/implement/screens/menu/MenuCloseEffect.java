/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.DepthTestFunction
 *  com.mojang.blaze3d.systems.CommandEncoder
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.FilterMode
 *  com.mojang.blaze3d.textures.GpuTexture
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.textures.TextureFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_10789
 *  net.minecraft.class_10799
 *  net.minecraft.class_276
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  org.lwjgl.system.MemoryStack
 */
package ru.recoin.implement.screens.menu;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import net.minecraft.class_10789;
import net.minecraft.class_10799;
import net.minecraft.class_276;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.lwjgl.system.MemoryStack;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.render.pipeline.DropShadowPipeline;
import ru.recoin.common.util.render.pipeline.EffectUniforms;
import ru.recoin.implement.features.modules.render.Hud;

public final class MenuCloseEffect
implements AutoCloseable {
    private static final class_2960 SHADER = class_2960.method_60655((String)"minecraft", (String)"core/recoin_menu_particles");
    private static final RenderPipeline PIPELINE = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"recoin", (String)"pipeline/menu_particles")).withVertexShader(SHADER).withFragmentShader(SHADER).withVertexFormat(class_290.field_60033, VertexFormat.class_5596.field_27379).withUniform("MenuParticlesData", class_10789.field_60031).withSampler("MenuSampler").withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());
    private static final int COLUMNS = 64;
    private static final int ROWS = 40;
    private final DropShadowPipeline shadow = new DropShadowPipeline();
    private GpuTexture texture;
    private GpuTextureView textureView;
    private GpuBuffer vertices;
    private float x;
    private float y;
    private float width;
    private float height;
    private float radius;
    private int screenWidth;
    private int screenHeight;

    public boolean isCaptured() {
        return this.textureView != null;
    }

    public void capture(float x, float y, float width, float height, float radius) {
        this.close();
        class_310 client = class_310.method_1551();
        class_276 framebuffer = client.method_1522();
        if (framebuffer == null || framebuffer.method_30277() == null) {
            return;
        }
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.radius = radius;
        this.screenWidth = client.method_22683().method_4486();
        this.screenHeight = client.method_22683().method_4502();
        this.texture = RenderSystem.getDevice().createTexture(() -> "recoin:menu_snapshot", 5, TextureFormat.RGBA8, framebuffer.field_1482, framebuffer.field_1481, 1, 1);
        this.textureView = RenderSystem.getDevice().createTextureView(this.texture);
        RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(framebuffer.method_30277(), this.texture, 0, 0, 0, 0, 0, framebuffer.field_1482, framebuffer.field_1481);
        try (MemoryStack stack = MemoryStack.stackPush();){
            this.vertices = RenderSystem.getDevice().createBuffer(() -> "recoin:menu_particles_vertex", 32, stack.malloc(4).putInt(0).flip());
        }
    }

    public void draw(float progress) {
        if (!this.isCaptured()) {
            return;
        }
        class_310 client = class_310.method_1551();
        if (this.screenWidth != client.method_22683().method_4486() || this.screenHeight != client.method_22683().method_4502()) {
            this.close();
            return;
        }
        float scale = 1.0f;
        Hud hud = Hud.getInstance();
        if (hud == null || !hud.shouldDisableShadows(false, true)) {
            float strength = hud != null ? hud.getShadowStrength() : 1.0f;
            float r = this.radius * scale;
            this.shadow.draw(this.x + this.width * (1.0f - scale) * 0.5f, this.y + this.height * (1.0f - scale) * 0.5f, this.width * scale, this.height * scale, new float[]{r, r, r, r}, this.radius * scale, 1.5f * scale, ColorUtil.multAlpha(-1207959552, (1.0f - MenuCloseEffect.smoothStep(0.18f, 0.58f, progress)) * strength));
        }
        try (MemoryStack stack = MemoryStack.stackPush();){
            ByteBuffer data = stack.malloc(64);
            data.putFloat(this.x).putFloat(this.y).putFloat(this.width).putFloat(this.height);
            data.putFloat(this.screenWidth).putFloat(this.screenHeight).putFloat(this.radius).putFloat(progress);
            data.putFloat(64.0f).putFloat(40.0f).putFloat(0.0f).putFloat(0.0f);
            data.putFloat(0.0f).putFloat(0.0f).putFloat(0.0f).putFloat(0.0f).flip();
            GpuBufferSlice uniform = EffectUniforms.write(data);
            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            try (RenderPass pass = encoder.createRenderPass(() -> "recoin:menu_particles", client.method_1522().method_71639(), OptionalInt.empty());){
                pass.setPipeline(PIPELINE);
                pass.setVertexBuffer(0, this.vertices);
                pass.setUniform("MenuParticlesData", uniform);
                pass.bindTexture("MenuSampler", this.textureView, RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR));
                pass.draw(0, 15360);
            }
        }
    }

    private static float smoothStep(float start, float end, float value) {
        float t = Math.max(0.0f, Math.min(1.0f, (value - start) / (end - start)));
        return t * t * (3.0f - 2.0f * t);
    }

    @Override
    public void close() {
        if (this.textureView != null) {
            this.textureView.close();
        }
        if (this.texture != null) {
            this.texture.close();
        }
        if (this.vertices != null) {
            this.vertices.close();
        }
        this.textureView = null;
        this.texture = null;
        this.vertices = null;
    }
}

