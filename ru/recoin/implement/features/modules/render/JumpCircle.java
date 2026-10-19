/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_1657
 *  net.minecraft.class_243
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_3532
 *  net.minecraft.class_4587
 *  net.minecraft.class_4587$class_4665
 *  net.minecraft.class_638
 *  net.minecraft.class_7833
 *  net.minecraft.class_9801
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionfc
 *  org.joml.Vector4i
 */
package ru.recoin.implement.features.modules.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_638;
import net.minecraft.class_7833;
import net.minecraft.class_9801;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.joml.Vector4i;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.common.util.render.pipeline.HitBubblePipeline;
import ru.recoin.core.Main;
import ru.recoin.implement.events.player.JumpEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;

public class JumpCircle
extends Module {
    private final SelectSetting style = new SelectSetting("Style", "Jump circle appearance").value("Liquid Glass", "Classic").selected("Liquid Glass");
    private final ValueSetting sizeSetting = new ValueSetting("Size", "Maximum jump circle size").setValue(2.8f).range(1.0f, 6.0f);
    private final ValueSetting durationSetting = new ValueSetting("\u0412\u0440\u0435\u043c\u044f \u0436\u0438\u0437\u043d\u0438", "\u0412\u0440\u0435\u043c\u044f \u0436\u0438\u0437\u043d\u0438 \u043a\u0440\u0443\u0433\u0430 \u0432 \u043c\u0438\u043b\u043b\u0438\u0441\u0435\u043a\u0443\u043d\u0434\u0430\u0445").setValue(900.0f).range(250.0f, 5000.0f).step(50.0f);
    private final ValueSetting distortion = new ValueSetting("\u0418\u0441\u043a\u0430\u0436\u0435\u043d\u0438\u0435", "\u0421\u0438\u043b\u0430 \u043f\u0440\u0435\u043b\u043e\u043c\u043b\u0435\u043d\u0438\u044f \u0432\u043d\u0443\u0442\u0440\u0438 \u0441\u0442\u0435\u043a\u043b\u044f\u043d\u043d\u043e\u0433\u043e \u043a\u0440\u0443\u0433\u0430").setValue(1.0f).range(0.1f, 3.0f).step(0.05f).visible(() -> this.style.isSelected("Liquid Glass"));
    private final List<CircleParticle> circles = new ArrayList<CircleParticle>();
    private final JumpCircleFadeRenderer fadeRenderer = new JumpCircleFadeRenderer();
    private final HitBubblePipeline glassPipeline = new HitBubblePipeline();
    private class_638 effectWorld;

    public JumpCircle() {
        super("JumpCircle", "Jump Circle", ModuleCategory.RENDER);
        this.setup(this.style, this.sizeSetting, this.durationSetting, this.distortion);
        Main.getInstance().getEventManager().register(this.fadeRenderer);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @EventHandler
    public void onJump(JumpEvent e) {
        if (JumpCircle.mc.field_1724 == null || JumpCircle.mc.field_1687 == null || e.isCancelled()) {
            return;
        }
        class_1657 player = e.getPlayer();
        if (player != JumpCircle.mc.field_1724) {
            return;
        }
        this.synchronizeWorld();
        if (this.circles.size() >= 24) {
            this.circles.removeFirst();
        }
        this.circles.add(new CircleParticle(player.method_73189(), System.nanoTime(), (double)this.durationSetting.getValue() * 1000000.0, this.sizeSetting.getValue(), ColorUtil.getClientColor()));
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        this.renderCircles(e);
    }

    private void renderCircles(WorldRenderEvent e) {
        this.synchronizeWorld();
        if (JumpCircle.mc.field_1724 == null || JumpCircle.mc.field_1687 == null || this.circles.isEmpty()) {
            return;
        }
        long now = System.nanoTime();
        this.circles.removeIf(circle -> (double)(now - circle.startTime) >= circle.lifetime);
        if (this.circles.isEmpty()) {
            this.glassPipeline.close();
            return;
        }
        if (this.style.isSelected("Liquid Glass")) {
            this.renderGlassCircles(e, now);
            return;
        }
        class_2960 texture = this.getTexture();
        class_4587 matrices = e.getStack();
        for (CircleParticle circle2 : this.circles) {
            float progress = (float)((double)(now - circle2.startTime) / circle2.lifetime);
            float eased = this.easeOutCubic(progress);
            float fadeIn = class_3532.method_15363((float)(progress / 0.18f), (float)0.0f, (float)1.0f);
            float alpha = (1.0f - eased) * this.easeOutCubic(fadeIn);
            float appear = this.easeOutCubic(fadeIn);
            float size = class_3532.method_16439((float)eased, (float)(0.2f + appear * 0.15f), (float)circle2.size);
            matrices.method_22903();
            class_243 cameraPos = JumpCircle.mc.field_1773.method_19418().method_71156();
            class_243 renderPos = circle2.pos.method_1031(0.0, 0.003, 0.0);
            matrices.method_22904(renderPos.field_1352 - cameraPos.field_1352, renderPos.field_1351 - cameraPos.field_1351, renderPos.field_1350 - cameraPos.field_1350);
            matrices.method_22907((Quaternionfc)class_7833.field_40714.rotationDegrees(90.0f));
            Vector4i baseColor = ColorUtil.multDark(ColorUtil.multRedAndAlpha(new Vector4i(ColorUtil.fade(270), ColorUtil.fade(0), ColorUtil.fade(180), ColorUtil.fade(90)), 1.0f, alpha), alpha);
            Vector4i glowColor = ColorUtil.multDark(ColorUtil.multRedAndAlpha(new Vector4i(ColorUtil.fade(270), ColorUtil.fade(0), ColorUtil.fade(180), ColorUtil.fade(90)), 1.0f, 1.0f * alpha), alpha);
            class_4587.class_4665 entry = matrices.method_23760().method_56822();
            Render3DUtil.drawTexture(entry, texture, -size / 2.0f, -size / 2.0f, size, size, baseColor, true);
            Render3DUtil.drawTextureAdditive(entry, texture, -size / 2.0f, -size / 2.0f, size, size, glowColor, true);
            matrices.method_22909();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void renderGlassCircles(WorldRenderEvent event, long now) {
        class_243 cameraPos = JumpCircle.mc.field_1773.method_19418().method_71156();
        class_4587 matrices = event.getStack();
        class_287 buffer = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27382, class_290.field_1575);
        for (CircleParticle circle : this.circles) {
            float progress = (float)((double)(now - circle.startTime) / circle.lifetime);
            float radius = 0.045f + circle.size * 0.5f * this.easeOutCubic(progress);
            int color = circle.color & 0xFFFFFF | Math.round((1.0f - progress) * 255.0f) << 24;
            matrices.method_22903();
            try {
                matrices.method_22904(circle.pos.field_1352 - cameraPos.field_1352, circle.pos.field_1351 - cameraPos.field_1351 + 0.015, circle.pos.field_1350 - cameraPos.field_1350);
                matrices.method_22907((Quaternionfc)class_7833.field_40714.rotationDegrees(90.0f));
                Matrix4f matrix = matrices.method_23760().method_23761();
                buffer.method_22918((Matrix4fc)matrix, -radius, -radius, 0.0f).method_22913(-1.0f, -1.0f).method_39415(color);
                buffer.method_22918((Matrix4fc)matrix, radius, -radius, 0.0f).method_22913(1.0f, -1.0f).method_39415(color);
                buffer.method_22918((Matrix4fc)matrix, radius, radius, 0.0f).method_22913(1.0f, 1.0f).method_39415(color);
                buffer.method_22918((Matrix4fc)matrix, -radius, radius, 0.0f).method_22913(-1.0f, 1.0f).method_39415(color);
            }
            finally {
                matrices.method_22909();
            }
        }
        class_9801 built = buffer.method_60794();
        if (built != null) {
            this.glassPipeline.draw(built, this.distortion.getValue());
        }
    }

    private void synchronizeWorld() {
        if (this.effectWorld == JumpCircle.mc.field_1687) {
            return;
        }
        this.circles.clear();
        this.glassPipeline.close();
        this.effectWorld = JumpCircle.mc.field_1687;
    }

    private class_2960 getTexture() {
        return class_2960.method_60654((String)"textures/circle_bold.png");
    }

    private float easeOutCubic(float value) {
        float clamped = class_3532.method_15363((float)value, (float)0.0f, (float)1.0f);
        return 1.0f - (float)Math.pow(1.0f - clamped, 3.0);
    }

    private class JumpCircleFadeRenderer {
        private JumpCircleFadeRenderer() {
        }

        @EventHandler
        public void onTick(TickEvent event) {
            JumpCircle.this.synchronizeWorld();
        }

        @EventHandler
        public void onWorldRender(WorldRenderEvent e) {
            if (JumpCircle.this.isState() || JumpCircle.this.circles.isEmpty()) {
                return;
            }
            JumpCircle.this.renderCircles(e);
        }
    }

    private record CircleParticle(class_243 pos, long startTime, double lifetime, float size, int color) {
    }
}

