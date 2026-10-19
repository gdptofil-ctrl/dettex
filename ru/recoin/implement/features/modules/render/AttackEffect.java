/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_243
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  net.minecraft.class_332
 *  net.minecraft.class_3532
 *  net.minecraft.class_4184
 *  net.minecraft.class_9801
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionfc
 */
package ru.recoin.implement.features.modules.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_9801;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.ColorSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.render.LegacyRenderSystem;
import ru.recoin.compat.render.BufferRenderer;
import ru.recoin.compat.render.GlStateManager;
import ru.recoin.compat.render.ShaderProgramKeys;
import ru.recoin.implement.events.player.AttackEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;

public final class AttackEffect
extends Module {
    private static final long FLASH_DURATION_NANOS = 180000000L;
    private static final int MAX_EFFECTS = 16;
    private final SelectSetting effect = new SelectSetting("Effect", "Attack impact style").value("Flash", "Slash", "Impact").selected("Flash");
    private final BooleanSetting flash = new BooleanSetting("Screen Flash", "Brief screen flash on attack").setValue(true);
    private final ColorSetting colors = new ColorSetting("Color", "Attack effect color").setColor(-1);
    private final ValueSetting duration = new ValueSetting("Duration", "World effect duration in milliseconds").setValue(350.0f).range(100.0f, 1000.0f).step(25.0f);
    private final List<Effect> effects = new ArrayList<Effect>();
    private long flashStarted;

    public AttackEffect() {
        super("AttackEffect", "Attack Effect", ModuleCategory.RENDER);
        this.setup(this.effect, this.flash, this.colors, this.duration);
    }

    public static AttackEffect getInstance() {
        return Instance.get(AttackEffect.class);
    }

    @Override
    public void activate() {
        this.effects.clear();
        this.flashStarted = 0L;
    }

    @Override
    public void deactivate() {
        this.effects.clear();
        this.flashStarted = 0L;
    }

    @EventHandler
    public void onAttack(AttackEvent event) {
        class_243 position;
        class_1309 target;
        class_1297 class_12972;
        if (event.isPre() || AttackEffect.mc.field_1724 == null || AttackEffect.mc.field_1687 == null || !((class_12972 = event.getEntity()) instanceof class_1309) || (target = (class_1309)class_12972).method_31481()) {
            return;
        }
        long now = System.nanoTime();
        class_243 class_2432 = position = event.getHitPos() == null ? target.method_5829().method_1005() : event.getHitPos();
        if (this.effects.size() >= 16) {
            this.effects.removeFirst();
        }
        this.effects.add(new Effect(position, now, (double)this.duration.getValue() * 1000000.0, this.effect.getSelected(), this.colors.getColor()));
        if (this.flash.isValue()) {
            this.flashStarted = now;
        }
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (AttackEffect.mc.field_1724 == null || AttackEffect.mc.field_1687 == null || this.effects.isEmpty()) {
            return;
        }
        long now = System.nanoTime();
        this.effects.removeIf(active -> (double)(now - active.started) >= active.lifetime);
        if (this.effects.isEmpty()) {
            return;
        }
        class_4184 camera = AttackEffect.mc.field_1773.method_19418();
        class_243 cameraPos = camera.method_71156();
        class_287 buffer = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27382, class_290.field_1576);
        Matrix4f matrix = event.getStack().method_23760().method_23761();
        for (Effect active2 : this.effects) {
            float progress = class_3532.method_15363((float)((float)((double)(now - active2.started) / active2.lifetime)), (float)0.0f, (float)1.0f);
            float fade = 1.0f - progress;
            class_243 position = active2.position.method_1020(cameraPos);
            event.getStack().method_22903();
            event.getStack().method_22904(position.field_1352, position.field_1351, position.field_1350);
            event.getStack().method_22907((Quaternionfc)camera.method_23767());
            matrix = event.getStack().method_23760().method_23761();
            if ("Slash".equals(active2.mode)) {
                AttackEffect.renderSlash(buffer, matrix, fade, active2.color, progress);
            } else if ("Impact".equals(active2.mode)) {
                AttackEffect.renderImpact(buffer, matrix, fade, active2.color, progress);
            } else {
                AttackEffect.renderFlash(buffer, matrix, fade, active2.color, progress);
            }
            event.getStack().method_22909();
        }
        class_9801 built = buffer.method_60794();
        if (built == null) {
            return;
        }
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.disableCull();
        LegacyRenderSystem.disableDepthTest();
        LegacyRenderSystem.depthMask(false);
        LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE);
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferRenderer.drawWithGlobalProgram(built);
        LegacyRenderSystem.depthMask(true);
        LegacyRenderSystem.enableDepthTest();
        LegacyRenderSystem.enableCull();
        LegacyRenderSystem.defaultBlendFunc();
        LegacyRenderSystem.disableBlend();
    }

    public static void renderScreenFlash(class_332 context) {
        AttackEffect module = AttackEffect.getInstance();
        if (module == null || !module.isState() || !module.flash.isValue() || module.flashStarted == 0L) {
            return;
        }
        float progress = (float)(System.nanoTime() - module.flashStarted) / 1.8E8f;
        if (progress >= 1.0f) {
            return;
        }
        int alpha = Math.round((1.0f - class_3532.method_15363((float)progress, (float)0.0f, (float)1.0f)) * 64.0f);
        int color = module.colors.getColor();
        context.method_25294(0, 0, context.method_51421(), context.method_51443(), alpha << 24 | color & 0xFFFFFF);
    }

    private static void renderFlash(class_287 buffer, Matrix4f matrix, float fade, int color, float progress) {
        float radius = 0.12f + progress * 0.48f;
        int tint = AttackEffect.withAlpha(color, fade);
        float width = radius * 0.12f;
        AttackEffect.quad(buffer, matrix, -radius, -width, 0.0f, radius, -width, 0.0f, radius, width, 0.0f, -radius, width, 0.0f, tint);
        AttackEffect.quad(buffer, matrix, -width, -radius, 0.0f, width, -radius, 0.0f, width, radius, 0.0f, -width, radius, 0.0f, tint);
    }

    private static void renderSlash(class_287 buffer, Matrix4f matrix, float fade, int color, float progress) {
        float length = 0.24f + progress * 0.48f;
        float width = 0.025f + (1.0f - progress) * 0.035f;
        float dx = length * 0.7071f;
        float dy = length * 0.7071f;
        float nx = width * 0.7071f;
        float ny = -width * 0.7071f;
        int tint = AttackEffect.withAlpha(color, fade);
        buffer.method_22918((Matrix4fc)matrix, -dx + nx, -dy + ny, 0.0f).method_39415(tint);
        buffer.method_22918((Matrix4fc)matrix, dx + nx, dy + ny, 0.0f).method_39415(tint);
        buffer.method_22918((Matrix4fc)matrix, dx - nx, dy - ny, 0.0f).method_39415(tint);
        buffer.method_22918((Matrix4fc)matrix, -dx - nx, -dy - ny, 0.0f).method_39415(tint);
    }

    private static void renderImpact(class_287 buffer, Matrix4f matrix, float fade, int color, float progress) {
        float inner = 0.1f + progress * 0.37f;
        float outer = inner + 0.035f * fade;
        int tint = AttackEffect.withAlpha(color, fade);
        for (int i = 0; i < 24; ++i) {
            double a = Math.PI * 2 * (double)i / 24.0;
            double b = Math.PI * 2 * (double)(i + 1) / 24.0;
            buffer.method_22918((Matrix4fc)matrix, (float)Math.cos(a) * inner, (float)Math.sin(a) * inner, 0.0f).method_39415(tint);
            buffer.method_22918((Matrix4fc)matrix, (float)Math.cos(b) * inner, (float)Math.sin(b) * inner, 0.0f).method_39415(tint);
            buffer.method_22918((Matrix4fc)matrix, (float)Math.cos(b) * outer, (float)Math.sin(b) * outer, 0.0f).method_39415(tint);
            buffer.method_22918((Matrix4fc)matrix, (float)Math.cos(a) * outer, (float)Math.sin(a) * outer, 0.0f).method_39415(tint);
        }
    }

    private static void quad(class_287 buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, int color) {
        buffer.method_22918((Matrix4fc)matrix, x1, y1, z1).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, x2, y2, z2).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, x3, y3, z3).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, x4, y4, z4).method_39415(color);
    }

    private static int withAlpha(int color, float alpha) {
        return Math.round((float)(color >>> 24) * alpha) << 24 | color & 0xFFFFFF;
    }

    private record Effect(class_243 position, long started, double lifetime, String mode, int color) {
    }
}

