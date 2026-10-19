/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_243
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  net.minecraft.class_3532
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  net.minecraft.class_638
 *  net.minecraft.class_7833
 *  net.minecraft.class_9801
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
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
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_638;
import net.minecraft.class_7833;
import net.minecraft.class_9801;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.render.LegacyRenderSystem;
import ru.recoin.common.util.render.pipeline.WinterPipeline;
import ru.recoin.compat.render.BufferRenderer;
import ru.recoin.compat.render.GlStateManager;
import ru.recoin.compat.render.ShaderProgramKeys;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.FogEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;
import ru.recoin.implement.features.modules.render.Atmosphere;
import ru.recoin.implement.features.modules.render.WorldTweaks;

public final class Winter
extends Module {
    private static final Matrix4f curProj = new Matrix4f();
    private static final Matrix4f curView = new Matrix4f();
    private static final Vector3f curCam = new Vector3f();
    private static float curFov = 70.0f;
    private static boolean hasCurrent;
    private static final WinterPipeline pipeline;
    private static final long startTime;
    private static final int BASE_SNOW_COUNT = 3000;
    private static final float SNOW_RADIUS = 38.0f;
    private static final float SNOW_DENSITY = 1.0f;
    private static final float WIND = 1.0f;
    private static final float FALL_SPEED = 1.0f;
    private static final float SNOW_COVERAGE = 1.0f;
    private static final float FOG_DISTANCE = 54.0f;
    private static final boolean FALLING_SNOW = true;
    private static final boolean COLD_GRADING = true;
    private static final boolean WORLD_SNOW = true;
    private static final boolean SPARKLES = true;
    private static final boolean WINTER_FOG = true;
    private final List<Snowflake> snowflakes = new ArrayList<Snowflake>();
    private final Random random = new Random();
    private long lastFrameNanos;
    private float clock;
    private class_638 snowWorld;

    public Winter() {
        super("Winter", "Winter", ModuleCategory.RENDER);
    }

    public static Winter getInstance() {
        return Instance.get(Winter.class);
    }

    private int getTargetSnowCount() {
        return (int)class_3532.method_15363((float)3000.0f, (float)500.0f, (float)4500.0f);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void activate() {
        super.activate();
        hasCurrent = false;
        this.lastFrameNanos = 0L;
        this.clock = 0.0f;
        this.snowWorld = Winter.mc.field_1687;
        List<Snowflake> list = this.snowflakes;
        synchronized (list) {
            this.snowflakes.clear();
            if (Winter.mc.field_1724 != null) {
                class_243 p = Winter.mc.field_1724.method_33571();
                int target = this.getTargetSnowCount();
                for (int i = 0; i < target; ++i) {
                    this.spawnSnowflake(p, 38.0f, false);
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void deactivate() {
        super.deactivate();
        hasCurrent = false;
        this.snowWorld = null;
        List<Snowflake> list = this.snowflakes;
        synchronized (list) {
            this.snowflakes.clear();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventHandler
    public void onTick(TickEvent event) {
        if (Winter.mc.field_1724 == null || Winter.mc.field_1687 != this.snowWorld) {
            List<Snowflake> list = this.snowflakes;
            synchronized (list) {
                this.snowflakes.clear();
            }
            this.snowWorld = Winter.mc.field_1687;
            this.lastFrameNanos = 0L;
        }
    }

    @EventHandler
    public void onFog(FogEvent event) {
        Atmosphere atmosphere = Atmosphere.getInstance();
        if (atmosphere != null && atmosphere.isState()) {
            return;
        }
        if (!this.isState()) {
            return;
        }
        WorldTweaks wt = WorldTweaks.getInstance();
        if (wt != null && wt.customFogEnabled()) {
            return;
        }
        event.setDistance(54.0f);
        event.setColor(-2759952);
        event.cancel();
    }

    public static void updateMatrices(Matrix4f positionMatrix, Matrix4f projectionMatrix, class_4184 camera, float fov) {
        Winter mod = Winter.getInstance();
        if (mod == null || !mod.isState() || camera == null || positionMatrix == null || projectionMatrix == null) {
            hasCurrent = false;
            return;
        }
        if (Winter.mc.field_1773.method_35765()) {
            hasCurrent = false;
            return;
        }
        curProj.set((Matrix4fc)projectionMatrix);
        curView.set((Matrix4fc)positionMatrix);
        class_243 p = camera.method_71156();
        curCam.set((float)p.field_1352, (float)p.field_1351, (float)p.field_1350);
        curFov = fov;
        hasCurrent = true;
    }

    public static void render() {
        Winter mod = Winter.getInstance();
        if (mod == null || !mod.isState() || Winter.mc.field_1687 == null || Winter.mc.field_1773 == null) {
            return;
        }
        class_4184 camera = Winter.mc.field_1773.method_19418();
        if (camera == null) {
            return;
        }
        float cachedFov = LegacyRenderSystem.getLastFov();
        float fov = cachedFov > 0.0f ? cachedFov : (Winter.mc.field_1690 != null ? ((Integer)Winter.mc.field_1690.method_41808().method_41753()).floatValue() : 70.0f);
        Matrix4f proj = Winter.mc.field_1773.method_22973(fov);
        if (proj == null) {
            return;
        }
        Matrix4f invProj = new Matrix4f((Matrix4fc)proj).invert();
        Matrix4f viewRot = new Matrix4f().rotation((Quaternionfc)camera.method_23767());
        class_243 p = camera.method_71156();
        Vector3f camPos = new Vector3f((float)p.field_1352, (float)p.field_1351, (float)p.field_1350);
        float time = (float)((double)(System.currentTimeMillis() - startTime) / 1000.0);
        float snowAmt = 1.0f;
        float sparkleInt = 0.85f;
        float cold = 0.85f;
        float fogActive = 1.0f;
        float fogStart = 9.0f;
        float fogEnd = 54.0f;
        float fogR = 0.78f;
        float fogG = 0.85f;
        float fogB = 0.94f;
        float fogA = 0.54f;
        pipeline.draw(proj, invProj, viewRot, camPos, fov, time, snowAmt, sparkleInt, 0.0f, 0.0f, 0.45f, cold, fogStart, fogEnd, fogActive, fogR, fogG, fogB, fogA, null, 0);
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        Atmosphere atmosphere = Atmosphere.getInstance();
        if (atmosphere != null && atmosphere.isState()) {
            return;
        }
        if (!this.isState() || Winter.mc.field_1724 == null || Winter.mc.field_1687 == null || Winter.mc.field_1773 == null || event.getStack() == null) {
            return;
        }
        class_4184 camera = Winter.mc.field_1773.method_19418();
        if (camera == null) {
            return;
        }
        long now = System.nanoTime();
        if (this.lastFrameNanos == 0L) {
            this.lastFrameNanos = now;
        }
        float dt = (float)((double)(now - this.lastFrameNanos) / 1.0E9);
        this.lastFrameNanos = now;
        dt = class_3532.method_15363((float)dt, (float)0.0f, (float)0.05f);
        this.clock += dt;
        class_243 cam = camera.method_71156();
        this.updateSnowflakes(cam, dt);
        if (this.snowflakes.isEmpty()) {
            return;
        }
        class_4587 matrices = event.getStack();
        matrices.method_22903();
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.enableDepthTest();
        LegacyRenderSystem.depthMask(false);
        LegacyRenderSystem.disableCull();
        LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        this.renderSnowflakes(matrices, camera, cam);
        LegacyRenderSystem.depthMask(true);
        LegacyRenderSystem.enableCull();
        LegacyRenderSystem.defaultBlendFunc();
        LegacyRenderSystem.disableBlend();
        matrices.method_22909();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void updateSnowflakes(class_243 center, float dt) {
        float speedMul = 1.0f;
        float windMul = 1.0f;
        int target = this.getTargetSnowCount();
        List<Snowflake> list = this.snowflakes;
        synchronized (list) {
            this.snowflakes.removeIf(s -> {
                double dx = s.x - center.field_1352;
                double dz = s.z - center.field_1350;
                return s.dead || dx * dx + dz * dz > 2116.0 || s.y < center.field_1351 - 14.0 || s.y > center.field_1351 + 28.0;
            });
            boolean fill = this.snowflakes.size() < target / 2;
            int spawnBudget = fill ? Math.min(target - this.snowflakes.size(), 400) : Math.min(target - this.snowflakes.size(), 120);
            for (int i = 0; i < spawnBudget; ++i) {
                this.spawnSnowflake(center, 38.0f, !fill && this.random.nextFloat() > 0.25f);
            }
            double gust = Math.sin((double)this.clock * 0.45) * 0.45 + Math.sin((double)this.clock * 1.25) * 0.2;
            double windX = (double)windMul * (0.8 + gust * 0.5);
            double windZ = (double)windMul * (0.4 + Math.cos((double)this.clock * 0.38) * 0.35);
            int size = this.snowflakes.size();
            for (int i = 0; i < size; ++i) {
                Snowflake s2 = this.snowflakes.get(i);
                double swayX = Math.sin(this.clock * s2.swayFreq + s2.phase) * (double)s2.swayAmp;
                double swayZ = Math.cos((double)(this.clock * s2.swayFreq) * 0.78 + (double)s2.phase * 0.65) * (double)s2.swayAmp;
                s2.vx = (windX + swayX) * (double)s2.weight;
                s2.vz = (windZ + swayZ) * (double)s2.weight;
                s2.vy = (double)(-speedMul) * 0.95 * (1.3 + 0.8 * (double)s2.weight);
                s2.prevX = s2.x;
                s2.prevY = s2.y;
                s2.prevZ = s2.z;
                s2.x += s2.vx * (double)dt;
                s2.y += s2.vy * (double)dt;
                s2.z += s2.vz * (double)dt;
                s2.rotation += s2.rotSpeed * dt;
            }
        }
    }

    private void spawnSnowflake(class_243 center, float radius, boolean fromTop) {
        double angle = this.random.nextDouble() * Math.PI * 2.0;
        double dist = Math.sqrt(this.random.nextDouble()) * (double)radius;
        double x = center.field_1352 + Math.cos(angle) * dist;
        double z = center.field_1350 + Math.sin(angle) * dist;
        double y = fromTop ? center.field_1351 + 16.0 + (double)this.random.nextFloat() * 8.0 : center.field_1351 + (double)this.random.nextFloat() * 26.0 - 8.0;
        Snowflake s = new Snowflake();
        s.x = x;
        s.y = y;
        s.z = z;
        s.prevX = x;
        s.prevY = y;
        s.prevZ = z;
        s.weight = 0.55f + this.random.nextFloat() * 0.85f;
        s.baseSize = 0.08f + this.random.nextFloat() * 0.18f;
        s.phase = this.random.nextFloat() * 62.8f;
        s.swayAmp = 0.55f + this.random.nextFloat() * 0.65f;
        s.swayFreq = 1.3f + this.random.nextFloat() * 1.5f;
        s.rotSpeed = (this.random.nextFloat() - 0.5f) * 90.0f;
        s.rotation = this.random.nextFloat() * 360.0f;
        s.alpha = 0.75f + this.random.nextFloat() * 0.25f;
        s.type = this.random.nextInt(3);
        this.snowflakes.add(s);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void renderSnowflakes(class_4587 ms, class_4184 camera, class_243 cam) {
        float yaw = camera.method_19330();
        float pitch = camera.method_19329();
        class_287 buffer = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27382, class_290.field_1576);
        List<Snowflake> list = this.snowflakes;
        synchronized (list) {
            int size = this.snowflakes.size();
            for (int i = 0; i < size; ++i) {
                double dist;
                float distFade;
                float a;
                Snowflake s = this.snowflakes.get(i);
                double dx = s.x - cam.field_1352;
                double dy = s.y - cam.field_1351;
                double dz = s.z - cam.field_1350;
                double distSq = dx * dx + dy * dy + dz * dz;
                if (distSq > 1764.0 || distSq < 0.2 || (a = s.alpha * (distFade = (dist = Math.sqrt(distSq)) < 0.85 ? (float)(dist / 0.85) : (dist > 28.5 ? 1.0f - (float)((dist - 28.5) / 9.5) : 1.0f))) <= 0.01f) continue;
                ms.method_22903();
                ms.method_22904(dx, dy, dz);
                ms.method_22907((Quaternionfc)camera.method_23767());
                ms.method_22907((Quaternionfc)class_7833.field_40718.rotationDegrees(s.rotation));
                Matrix4f m = ms.method_23760().method_23761();
                if (s.type == 0) {
                    this.drawCrystalFlake(buffer, m, s.baseSize, a);
                } else if (s.type == 1) {
                    this.drawStarFlake(buffer, m, s.baseSize, a);
                } else {
                    this.drawFluffyFlake(buffer, m, s.baseSize, a);
                }
                ms.method_22909();
            }
        }
        class_9801 built = buffer.method_60794();
        if (built != null) {
            BufferRenderer.drawWithGlobalProgram(built);
        }
    }

    private void drawCrystalFlake(class_287 b, Matrix4f m, float size, float a) {
        float armLen = size * 0.55f;
        float armWidth = size * 0.06f;
        float branchLen = size * 0.22f;
        float branchWidth = size * 0.045f;
        float cr = size * 0.12f;
        b.method_22918((Matrix4fc)m, -cr, -cr, 0.0f).method_22915(0.96f, 0.98f, 1.0f, a);
        b.method_22918((Matrix4fc)m, cr, -cr, 0.0f).method_22915(0.96f, 0.98f, 1.0f, a);
        b.method_22918((Matrix4fc)m, cr, cr, 0.0f).method_22915(0.96f, 0.98f, 1.0f, a);
        b.method_22918((Matrix4fc)m, -cr, cr, 0.0f).method_22915(0.96f, 0.98f, 1.0f, a);
        for (int i = 0; i < 6; ++i) {
            double angle = (double)i * 1.0471975511965976;
            float cos = (float)Math.cos(angle);
            float sin = (float)Math.sin(angle);
            float px = -sin * armWidth * 0.5f;
            float py = cos * armWidth * 0.5f;
            float tipX = cos * armLen;
            float tipY = sin * armLen;
            b.method_22918((Matrix4fc)m, -px, -py, 0.0f).method_22915(0.96f, 0.98f, 1.0f, a);
            b.method_22918((Matrix4fc)m, px, py, 0.0f).method_22915(0.96f, 0.98f, 1.0f, a);
            b.method_22918((Matrix4fc)m, tipX + px * 0.5f, tipY + py * 0.5f, 0.0f).method_22915(0.92f, 0.96f, 1.0f, a * 0.85f);
            b.method_22918((Matrix4fc)m, tipX - px * 0.5f, tipY - py * 0.5f, 0.0f).method_22915(0.92f, 0.96f, 1.0f, a * 0.85f);
            float nodeDist = armLen * 0.55f;
            float nodeX = cos * nodeDist;
            float nodeY = sin * nodeDist;
            double bAngle1 = angle + 1.0471975511965976;
            float bx1 = (float)Math.cos(bAngle1) * branchLen;
            float by1 = (float)Math.sin(bAngle1) * branchLen;
            float bpx1 = (float)(-Math.sin(bAngle1)) * branchWidth * 0.5f;
            float bpy1 = (float)Math.cos(bAngle1) * branchWidth * 0.5f;
            b.method_22918((Matrix4fc)m, nodeX - bpx1, nodeY - bpy1, 0.0f).method_22915(0.96f, 0.98f, 1.0f, a * 0.9f);
            b.method_22918((Matrix4fc)m, nodeX + bpx1, nodeY + bpy1, 0.0f).method_22915(0.96f, 0.98f, 1.0f, a * 0.9f);
            b.method_22918((Matrix4fc)m, nodeX + bx1 + bpx1 * 0.4f, nodeY + by1 + bpy1 * 0.4f, 0.0f).method_22915(0.9f, 0.95f, 1.0f, a * 0.65f);
            b.method_22918((Matrix4fc)m, nodeX + bx1 - bpx1 * 0.4f, nodeY + by1 - bpy1 * 0.4f, 0.0f).method_22915(0.9f, 0.95f, 1.0f, a * 0.65f);
            double bAngle2 = angle - 1.0471975511965976;
            float bx2 = (float)Math.cos(bAngle2) * branchLen;
            float by2 = (float)Math.sin(bAngle2) * branchLen;
            float bpx2 = (float)(-Math.sin(bAngle2)) * branchWidth * 0.5f;
            float bpy2 = (float)Math.cos(bAngle2) * branchWidth * 0.5f;
            b.method_22918((Matrix4fc)m, nodeX - bpx2, nodeY - bpy2, 0.0f).method_22915(0.96f, 0.98f, 1.0f, a * 0.9f);
            b.method_22918((Matrix4fc)m, nodeX + bpx2, nodeY + bpy2, 0.0f).method_22915(0.96f, 0.98f, 1.0f, a * 0.9f);
            b.method_22918((Matrix4fc)m, nodeX + bx2 + bpx2 * 0.4f, nodeY + by2 + bpy2 * 0.4f, 0.0f).method_22915(0.9f, 0.95f, 1.0f, a * 0.65f);
            b.method_22918((Matrix4fc)m, nodeX + bx2 - bpx2 * 0.4f, nodeY + by2 - bpy2 * 0.4f, 0.0f).method_22915(0.9f, 0.95f, 1.0f, a * 0.65f);
        }
    }

    private void drawStarFlake(class_287 b, Matrix4f m, float size, float a) {
        float h = size * 0.55f;
        float w = size * 0.08f;
        b.method_22918((Matrix4fc)m, -w, 0.0f, 0.0f).method_22915(0.96f, 0.98f, 1.0f, a);
        b.method_22918((Matrix4fc)m, w, 0.0f, 0.0f).method_22915(0.96f, 0.98f, 1.0f, a);
        b.method_22918((Matrix4fc)m, 0.0f, h, 0.0f).method_22915(0.9f, 0.95f, 1.0f, 0.0f);
        b.method_22918((Matrix4fc)m, 0.0f, -h, 0.0f).method_22915(0.9f, 0.95f, 1.0f, 0.0f);
        b.method_22918((Matrix4fc)m, 0.0f, -w, 0.0f).method_22915(0.96f, 0.98f, 1.0f, a);
        b.method_22918((Matrix4fc)m, 0.0f, w, 0.0f).method_22915(0.96f, 0.98f, 1.0f, a);
        b.method_22918((Matrix4fc)m, h, 0.0f, 0.0f).method_22915(0.9f, 0.95f, 1.0f, 0.0f);
        b.method_22918((Matrix4fc)m, -h, 0.0f, 0.0f).method_22915(0.9f, 0.95f, 1.0f, 0.0f);
        float hd = h * 0.65f;
        float wd = w * 0.7f;
        for (int i = 0; i < 2; ++i) {
            float angle = (float)(0.7853981633974483 + (double)i * Math.PI / 2.0);
            float cos = (float)Math.cos(angle);
            float sin = (float)Math.sin(angle);
            float px = -sin * wd;
            float py = cos * wd;
            b.method_22918((Matrix4fc)m, -px, -py, 0.0f).method_22915(0.96f, 0.98f, 1.0f, a * 0.8f);
            b.method_22918((Matrix4fc)m, px, py, 0.0f).method_22915(0.96f, 0.98f, 1.0f, a * 0.8f);
            b.method_22918((Matrix4fc)m, cos * hd, sin * hd, 0.0f).method_22915(0.9f, 0.95f, 1.0f, 0.0f);
            b.method_22918((Matrix4fc)m, -cos * hd, -sin * hd, 0.0f).method_22915(0.9f, 0.95f, 1.0f, 0.0f);
        }
    }

    private void drawFluffyFlake(class_287 b, Matrix4f m, float size, float a) {
        float hs = size * 0.5f;
        float qs = size * 0.22f;
        b.method_22918((Matrix4fc)m, -qs, -qs, 0.0f).method_22915(0.96f, 0.98f, 1.0f, a);
        b.method_22918((Matrix4fc)m, qs, -qs, 0.0f).method_22915(0.96f, 0.98f, 1.0f, a);
        b.method_22918((Matrix4fc)m, qs, qs, 0.0f).method_22915(0.96f, 0.98f, 1.0f, a);
        b.method_22918((Matrix4fc)m, -qs, qs, 0.0f).method_22915(0.96f, 0.98f, 1.0f, a);
        float hw = size * 0.08f;
        b.method_22918((Matrix4fc)m, -hw, 0.0f, 0.0f).method_22915(0.96f, 0.98f, 1.0f, a * 0.7f);
        b.method_22918((Matrix4fc)m, hw, 0.0f, 0.0f).method_22915(0.96f, 0.98f, 1.0f, a * 0.7f);
        b.method_22918((Matrix4fc)m, 0.0f, hs, 0.0f).method_22915(0.9f, 0.95f, 1.0f, 0.0f);
        b.method_22918((Matrix4fc)m, 0.0f, -hs, 0.0f).method_22915(0.9f, 0.95f, 1.0f, 0.0f);
        b.method_22918((Matrix4fc)m, 0.0f, -hw, 0.0f).method_22915(0.96f, 0.98f, 1.0f, a * 0.7f);
        b.method_22918((Matrix4fc)m, 0.0f, hw, 0.0f).method_22915(0.96f, 0.98f, 1.0f, a * 0.7f);
        b.method_22918((Matrix4fc)m, hs, 0.0f, 0.0f).method_22915(0.9f, 0.95f, 1.0f, 0.0f);
        b.method_22918((Matrix4fc)m, -hs, 0.0f, 0.0f).method_22915(0.9f, 0.95f, 1.0f, 0.0f);
    }

    static {
        pipeline = new WinterPipeline();
        startTime = System.currentTimeMillis();
    }

    private static final class Snowflake {
        double x;
        double y;
        double z;
        double prevX;
        double prevY;
        double prevZ;
        double vx;
        double vy;
        double vz;
        float baseSize;
        float phase;
        float rotation;
        float rotSpeed;
        float alpha;
        float swayAmp;
        float swayFreq;
        float weight;
        int type;
        boolean dead;

        private Snowflake() {
        }
    }
}

