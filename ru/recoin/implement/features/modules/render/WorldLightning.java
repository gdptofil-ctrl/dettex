/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_243
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  net.minecraft.class_2902$class_2903
 *  net.minecraft.class_3532
 *  net.minecraft.class_4184
 *  net.minecraft.class_9801
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package ru.recoin.implement.features.modules.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.class_243;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2902;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_9801;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
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
import ru.recoin.implement.events.render.WorldRenderEvent;

public final class WorldLightning
extends Module {
    private static final long BOLT_LIFETIME_MS = 900L;
    private static final int MAX_BOLTS = 4;
    private final BooleanSetting advanced = new BooleanSetting("\u0420\u0430\u0441\u0448\u0438\u0440\u0435\u043d\u043d\u044b\u0435", "\u041e\u0442\u043a\u0440\u044b\u0432\u0430\u0435\u0442 \u0440\u0443\u0447\u043d\u044b\u0435 \u043f\u0430\u0440\u0430\u043c\u0435\u0442\u0440\u044b \u0448\u0442\u043e\u0440\u043c\u0430");
    private final SelectSetting source = new SelectSetting("\u0418\u0441\u0442\u043e\u0447\u043d\u0438\u043a \u0448\u0442\u043e\u0440\u043c\u0430", "\u0420\u0443\u0447\u043d\u043e\u0439 \u0448\u0442\u043e\u0440\u043c \u0438\u043b\u0438 \u043d\u0430\u0441\u0442\u043e\u044f\u0449\u0430\u044f \u0433\u0440\u043e\u0437\u0430").value("Manual", "Weather").selected("Manual");
    private final ValueSetting stormPower = new ValueSetting("\u0421\u0438\u043b\u0430 \u0448\u0442\u043e\u0440\u043c\u0430", "\u0418\u043d\u0442\u0435\u043d\u0441\u0438\u0432\u043d\u043e\u0441\u0442\u044c \u0438 \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0440\u0430\u0437\u0440\u044f\u0434\u043e\u0432").setValue(0.5f).range(0.0f, 1.0f).visible(() -> this.advanced.isValue() && this.source.isSelected("Manual"));
    private final ValueSetting strikeRate = new ValueSetting("\u0427\u0430\u0441\u0442\u043e\u0442\u0430 \u0443\u0434\u0430\u0440\u043e\u0432", "\u041c\u043d\u043e\u0436\u0438\u0442\u0435\u043b\u044c \u0447\u0430\u0441\u0442\u043e\u0442\u044b \u043c\u043e\u043b\u043d\u0438\u0439").setValue(1.0f).range(0.25f, 4.0f).visible(this.advanced::isValue);
    private final ValueSetting strikeDistance = new ValueSetting("\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f \u0443\u0434\u0430\u0440\u043e\u0432", "\u041c\u0430\u043a\u0441\u0438\u043c\u0430\u043b\u044c\u043d\u043e\u0435 \u0440\u0430\u0441\u0441\u0442\u043e\u044f\u043d\u0438\u0435 \u043e\u0442 \u043a\u0430\u043c\u0435\u0440\u044b").setValue(90.0f).range(40.0f, 110.0f).visible(this.advanced::isValue);
    private final ColorSetting boltColor = new ColorSetting("\u0426\u0432\u0435\u0442 \u043c\u043e\u043b\u043d\u0438\u0438", "\u0426\u0432\u0435\u0442 \u044f\u0434\u0440\u0430 \u0438 \u0432\u0435\u0442\u0432\u0435\u0439 \u043c\u043e\u043b\u043d\u0438\u0438").setColor(-3368449).visible(this.advanced::isValue);
    private final ValueSetting boltWidth = new ValueSetting("\u0422\u043e\u043b\u0449\u0438\u043d\u0430 \u043c\u043e\u043b\u043d\u0438\u0438", "\u0428\u0438\u0440\u0438\u043d\u0430 \u0441\u0432\u0435\u0442\u044f\u0449\u0435\u0433\u043e\u0441\u044f \u044f\u0434\u0440\u0430").setValue(1.6f).range(0.5f, 5.0f).visible(this.advanced::isValue);
    private final ValueSetting boltBranches = new ValueSetting("\u0412\u0435\u0442\u0432\u0438 \u043c\u043e\u043b\u043d\u0438\u0438", "\u041a\u043e\u043b\u0438\u0447\u0435\u0441\u0442\u0432\u043e \u0431\u043e\u043a\u043e\u0432\u044b\u0445 \u0440\u0430\u0437\u0440\u044f\u0434\u043e\u0432").setValue(3.0f).range(0.0f, 4.0f).visible(this.advanced::isValue);
    private final List<Bolt> bolts = new ArrayList<Bolt>();
    private long nextStrikeAt;
    private long randomSeed = System.nanoTime();
    private Object strikeWorld;

    public WorldLightning() {
        super("WorldLightning", "World Lightning", ModuleCategory.RENDER);
        this.setup(this.advanced, this.source, this.stormPower, this.strikeRate, this.strikeDistance, this.boltColor, this.boltWidth, this.boltBranches);
    }

    public static WorldLightning getInstance() {
        return Instance.get(WorldLightning.class);
    }

    @Override
    public void activate() {
        this.bolts.clear();
        this.nextStrikeAt = 0L;
        this.strikeWorld = WorldLightning.mc.field_1687;
    }

    @Override
    public void deactivate() {
        this.bolts.clear();
        this.nextStrikeAt = 0L;
        this.strikeWorld = null;
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (WorldLightning.mc.field_1687 == null || WorldLightning.mc.field_1724 == null || WorldLightning.mc.field_1773 == null) {
            return;
        }
        if (this.strikeWorld != WorldLightning.mc.field_1687) {
            this.bolts.clear();
            this.strikeWorld = WorldLightning.mc.field_1687;
            this.nextStrikeAt = 0L;
        }
        long now = System.currentTimeMillis();
        float power = this.source.isSelected("Weather") ? (WorldLightning.mc.field_1687.method_8546() && WorldLightning.mc.field_1687.method_8419() ? 1.0f : 0.0f) : (this.advanced.isValue() ? this.stormPower.getValue() : 0.5f);
        if (power > 0.0f && now >= this.nextStrikeAt) {
            boolean spawned = this.spawnBolt(WorldLightning.mc.field_1773.method_19418(), now, power);
            float rate = Math.max(0.25f, this.strikeRate.getValue());
            long interval = (long)(1800.0f / (rate * (0.25f + power * 0.75f)));
            this.nextStrikeAt = now + (spawned ? Math.max(700L, interval + WorldLightning.randomBetween(-interval / 3L, interval / 3L)) : 250L);
        }
        this.bolts.removeIf(bolt -> now - bolt.bornAt >= 900L);
        if (!this.bolts.isEmpty()) {
            this.renderBolts(event, WorldLightning.mc.field_1773.method_19418(), now);
        }
    }

    private boolean spawnBolt(class_4184 camera, long now, float power) {
        int i;
        Random random = new Random(++this.randomSeed);
        class_243 cameraPos = camera.method_71156();
        double maxDistance = Math.min(110.0, Math.min((double)this.strikeDistance.getValue(), Math.max(28.0, (double)((Integer)WorldLightning.mc.field_1690.method_42503().method_41753()).intValue() * 14.0)));
        double minDistance = Math.min(20.0, maxDistance - 2.0);
        float fov = ((Integer)WorldLightning.mc.field_1690.method_41808().method_41753()).floatValue();
        double spread = Math.toRadians(class_3532.method_15363((float)(fov * 0.32f), (float)6.0f, (float)25.0f));
        int blockX = 0;
        int blockZ = 0;
        boolean loadedPosition = false;
        for (int attempt = 0; attempt < 12; ++attempt) {
            double distance = minDistance + random.nextDouble() * (maxDistance - minDistance);
            double angle = Math.toRadians(camera.method_19330()) + 1.5707963267948966 + (random.nextDouble() * 2.0 - 1.0) * spread;
            blockX = class_3532.method_15357((double)(cameraPos.field_1352 + Math.cos(angle) * distance));
            if (!WorldLightning.mc.field_1687.method_8393(blockX >> 4, (blockZ = class_3532.method_15357((double)(cameraPos.field_1350 + Math.sin(angle) * distance))) >> 4)) continue;
            loadedPosition = true;
            break;
        }
        if (!loadedPosition) {
            return false;
        }
        int ground = WorldLightning.mc.field_1687.method_8624(class_2902.class_2903.field_13197, blockX, blockZ);
        double startY = Math.min((double)ground + 128.0, Math.max(cameraPos.field_1351 + 34.0, (double)ground + 48.0));
        double endY = (double)ground + 1.0;
        class_243 start = new class_243((double)blockX + 0.5, startY, (double)blockZ + 0.5);
        class_243 end = new class_243((double)blockX + random.nextDouble(), endY, (double)blockZ + random.nextDouble());
        ArrayList<Segment> segments = new ArrayList<Segment>(42);
        ArrayList<class_243> spine = new ArrayList<class_243>(13);
        spine.add(start);
        for (i = 1; i < 12; ++i) {
            double t = (double)i / 12.0;
            double envelope = Math.sin(Math.PI * t);
            spine.add(new class_243(class_3532.method_16436((double)t, (double)start.field_1352, (double)end.field_1352) + (random.nextDouble() - 0.5) * 4.2 * envelope, class_3532.method_16436((double)t, (double)start.field_1351, (double)end.field_1351), class_3532.method_16436((double)t, (double)start.field_1350, (double)end.field_1350) + (random.nextDouble() - 0.5) * 4.2 * envelope));
        }
        spine.add(end);
        for (i = 1; i < spine.size(); ++i) {
            float taper = 0.72f + 0.28f * (float)Math.sin(Math.PI * (double)i / ((double)spine.size() - 1.0));
            segments.add(new Segment((class_243)spine.get(i - 1), (class_243)spine.get(i), taper));
        }
        int branchCount = Math.round(this.boltBranches.getValue() * (0.35f + power * 0.65f));
        for (int branch = 0; branch < branchCount; ++branch) {
            int from = 2 + random.nextInt(8);
            class_243 cursor = (class_243)spine.get(from);
            int steps = 3 + random.nextInt(3);
            double branchAngle = random.nextDouble() * Math.PI * 2.0;
            for (int step = 0; step < steps; ++step) {
                double length = 0.9 + random.nextDouble() * 2.1;
                double stepAngle = branchAngle + (random.nextDouble() - 0.5) * 0.9;
                class_243 next = cursor.method_1031(Math.cos(stepAngle) * length, -(random.nextDouble() * 1.8 + 0.7), Math.sin(stepAngle) * length);
                segments.add(new Segment(cursor, next, 0.62f - (float)step * 0.1f));
                cursor = next;
            }
        }
        if (this.bolts.size() >= 4) {
            this.bolts.remove(0);
        }
        this.bolts.add(new Bolt(now, segments, 0.72f + random.nextFloat() * 0.28f, power));
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void renderBolts(WorldRenderEvent event, class_4184 camera, long now) {
        class_243 cameraPos = camera.method_71156();
        Matrix4f matrix = event.getStack().method_23760().method_23761();
        int color = this.boltColor.getColor();
        float widthScale = this.boltWidth.getValue() / 1.6f;
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE);
        LegacyRenderSystem.enableDepthTest();
        LegacyRenderSystem.depthMask(false);
        LegacyRenderSystem.disableCull();
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        try {
            class_287 buffer = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27379, class_290.field_1576);
            for (Bolt bolt : this.bolts) {
                float flicker;
                int colorAlpha = color >>> 24 & 0xFF;
                float age = (float)(now - bolt.bornAt) / 900.0f;
                float envelope = Math.min(1.0f, age * 12.0f) * (1.0f - class_3532.method_15363((float)((age - 0.38f) / 0.62f), (float)0.0f, (float)1.0f));
                int alpha = Math.round((float)colorAlpha * envelope * (flicker = (float)(0.76 + 0.24 * Math.sin((double)now / 19.0 + (double)bolt.seed))) * (0.65f + bolt.power * 0.35f));
                if (alpha < 2) continue;
                for (Segment segment : bolt.segments) {
                    double distance = segment.from.method_1019(segment.to).method_1021(0.5).method_1022(cameraPos);
                    float width = (0.032f + (float)Math.min(distance, 100.0) * 2.8E-4f) * widthScale * segment.widthScale;
                    WorldLightning.emitBillboardSegment(buffer, matrix, cameraPos, segment.from, segment.to, width * 5.0f, color, Math.round((float)alpha * 0.16f));
                    WorldLightning.emitBillboardSegment(buffer, matrix, cameraPos, segment.from, segment.to, width * 2.25f, color, Math.round((float)alpha * 0.38f));
                    WorldLightning.emitBillboardSegment(buffer, matrix, cameraPos, segment.from, segment.to, width * 0.72f, -722177, alpha);
                }
            }
            class_9801 built = buffer.method_60794();
            if (built != null) {
                BufferRenderer.drawWithGlobalProgram(built);
            }
        }
        finally {
            LegacyRenderSystem.depthMask(true);
            LegacyRenderSystem.enableCull();
            LegacyRenderSystem.defaultBlendFunc();
            LegacyRenderSystem.disableBlend();
        }
    }

    private static void emitBillboardSegment(class_287 buffer, Matrix4f matrix, class_243 camera, class_243 start, class_243 end, float halfWidth, int color, int alpha) {
        class_243 midpoint;
        class_243 toCamera;
        class_243 direction = end.method_1020(start).method_1029();
        class_243 side = direction.method_1036(toCamera = (midpoint = start.method_1019(end).method_1021(0.5).method_1020(camera)).method_1021(-1.0).method_1029()).method_1029();
        if (side.method_1027() < 1.0E-6) {
            side = new class_243(1.0, 0.0, 0.0);
        }
        side = side.method_1021((double)halfWidth);
        class_243 a = start.method_1020(camera).method_1020(side);
        class_243 b = start.method_1020(camera).method_1019(side);
        class_243 c = end.method_1020(camera).method_1019(side);
        class_243 d = end.method_1020(camera).method_1020(side);
        int red = color >>> 16 & 0xFF;
        int green = color >>> 8 & 0xFF;
        int blue = color & 0xFF;
        WorldLightning.vertex(buffer, matrix, a, red, green, blue, alpha);
        WorldLightning.vertex(buffer, matrix, b, red, green, blue, alpha);
        WorldLightning.vertex(buffer, matrix, c, red, green, blue, alpha);
        WorldLightning.vertex(buffer, matrix, a, red, green, blue, alpha);
        WorldLightning.vertex(buffer, matrix, c, red, green, blue, alpha);
        WorldLightning.vertex(buffer, matrix, d, red, green, blue, alpha);
    }

    private static void vertex(class_287 buffer, Matrix4f matrix, class_243 pos, int r, int g, int b, int a) {
        buffer.method_22918((Matrix4fc)matrix, (float)pos.field_1352, (float)pos.field_1351, (float)pos.field_1350).method_1336(r, g, b, a);
    }

    private static long randomBetween(long min, long max) {
        if (max <= min) {
            return min;
        }
        return min + (long)(Math.random() * (double)(max - min));
    }

    private record Segment(class_243 from, class_243 to, float widthScale) {
    }

    private record Bolt(long bornAt, List<Segment> segments, float seed, float power) {
    }
}

