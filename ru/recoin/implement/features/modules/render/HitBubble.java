/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_239
 *  net.minecraft.class_243
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  net.minecraft.class_3966
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  net.minecraft.class_638
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
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_3966;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_638;
import net.minecraft.class_9801;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.render.pipeline.HitBubblePipeline;
import ru.recoin.implement.events.player.AttackEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;

public final class HitBubble
extends Module {
    private static final int MAX_BUBBLES = 24;
    private final ValueSetting size = new ValueSetting("\u0420\u0430\u0437\u043c\u0435\u0440", "\u041c\u0430\u043a\u0441\u0438\u043c\u0430\u043b\u044c\u043d\u044b\u0439 \u0440\u0430\u0434\u0438\u0443\u0441 \u0441\u0442\u0435\u043a\u043b\u044f\u043d\u043d\u043e\u0433\u043e \u043a\u043e\u043b\u044c\u0446\u0430").setValue(0.85f).range(0.25f, 2.5f).step(0.05f);
    private final ValueSetting duration = new ValueSetting("\u0412\u0440\u0435\u043c\u044f \u0436\u0438\u0437\u043d\u0438", "\u0412\u0440\u0435\u043c\u044f \u0436\u0438\u0437\u043d\u0438 \u0441\u0442\u0435\u043a\u043b\u044f\u043d\u043d\u043e\u0433\u043e \u043a\u0440\u0443\u0433\u0430 \u0432 \u043c\u0438\u043b\u043b\u0438\u0441\u0435\u043a\u0443\u043d\u0434\u0430\u0445").setValue(700.0f).range(250.0f, 5000.0f).step(50.0f);
    private final ValueSetting distortion = new ValueSetting("\u0418\u0441\u043a\u0430\u0436\u0435\u043d\u0438\u0435", "\u0421\u0438\u043b\u0430 \u043f\u0440\u0435\u043b\u043e\u043c\u043b\u0435\u043d\u0438\u044f \u0444\u043e\u043d\u0430 \u0436\u0438\u0434\u043a\u0438\u043c \u0441\u0442\u0435\u043a\u043b\u043e\u043c").setValue(1.0f).range(0.1f, 3.0f).step(0.05f);
    private final List<Bubble> bubbles = new ArrayList<Bubble>();
    private final HitBubblePipeline pipeline = new HitBubblePipeline();
    private class_638 effectWorld;

    public HitBubble() {
        super("HitBubble", "Hit Bubble", ModuleCategory.RENDER);
        this.setup(this.size, this.duration, this.distortion);
    }

    @Override
    public void deactivate() {
        this.bubbles.clear();
        this.effectWorld = null;
        this.pipeline.close();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        this.synchronizeWorld();
    }

    @EventHandler
    public void onAttack(AttackEvent event) {
        class_1309 target;
        class_1297 class_12972;
        if (event.isPre() || HitBubble.mc.field_1724 == null || HitBubble.mc.field_1687 == null || !((class_12972 = event.getEntity()) instanceof class_1309) || (target = (class_1309)class_12972).method_31481()) {
            return;
        }
        this.synchronizeWorld();
        class_243 position = this.hitPosition(event, target);
        if (this.bubbles.size() >= 24) {
            this.bubbles.removeFirst();
        }
        this.bubbles.add(new Bubble(position, System.nanoTime(), (double)this.duration.getValue() * 1000000.0, this.size.getValue(), ColorUtil.getClientColor()));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        this.synchronizeWorld();
        if (HitBubble.mc.field_1724 == null || HitBubble.mc.field_1687 == null || HitBubble.mc.field_1773 == null || this.bubbles.isEmpty()) {
            return;
        }
        long now = System.nanoTime();
        this.bubbles.removeIf(bubble -> (double)(now - bubble.started) >= bubble.lifetime);
        if (this.bubbles.isEmpty()) {
            return;
        }
        class_4184 camera = HitBubble.mc.field_1773.method_19418();
        class_243 cameraPosition = camera.method_71156();
        class_4587 matrices = event.getStack();
        class_287 buffer = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27382, class_290.field_1575);
        for (Bubble bubble2 : this.bubbles) {
            float age = (float)((double)(now - bubble2.started) / bubble2.lifetime);
            float expansion = 1.0f - (float)Math.pow(1.0f - age, 3.0);
            float radius = 0.045f + bubble2.radius * expansion;
            class_243 position = bubble2.position.method_1020(cameraPosition);
            if (position.method_1027() < 0.01) continue;
            position = position.method_1020(position.method_1029().method_1021(0.04));
            int color = bubble2.color & 0xFFFFFF | Math.round((1.0f - age) * 255.0f) << 24;
            matrices.method_22903();
            try {
                matrices.method_22904(position.field_1352, position.field_1351, position.field_1350);
                matrices.method_22907((Quaternionfc)camera.method_23767());
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
            this.pipeline.draw(built, this.distortion.getValue());
        }
    }

    private class_243 hitPosition(AttackEvent event, class_1309 target) {
        class_3966 hit;
        class_239 class_2392 = HitBubble.mc.field_1765;
        if (class_2392 instanceof class_3966 && (hit = (class_3966)class_2392).method_17782() == target && event.getHitPos() != null) {
            return event.getHitPos();
        }
        class_243 eye = HitBubble.mc.field_1724.method_33571();
        double reach = eye.method_1022(target.method_5829().method_1005()) + (double)target.method_17681() + (double)target.method_17682();
        class_243 direction = RotationController.INSTANCE.getRotation().toVector();
        return target.method_5829().method_992(eye, eye.method_1019(direction.method_1021(reach))).orElseGet(() -> target.method_5829().method_992(eye, target.method_5829().method_1005()).orElse(target.method_5829().method_1005()));
    }

    private void synchronizeWorld() {
        if (this.effectWorld == HitBubble.mc.field_1687) {
            return;
        }
        this.bubbles.clear();
        this.pipeline.close();
        this.effectWorld = HitBubble.mc.field_1687;
    }

    private record Bubble(class_243 position, long started, double lifetime, float radius, int color) {
    }
}

