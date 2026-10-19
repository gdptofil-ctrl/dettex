/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1922
 *  net.minecraft.class_1944
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_2374
 *  net.minecraft.class_238
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_3965
 *  net.minecraft.class_9801
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package ru.recoin.common.util.render.effects;

import net.minecraft.class_1297;
import net.minecraft.class_1922;
import net.minecraft.class_1944;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2374;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_9801;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.common.util.render.effects.BloodMesh;
import ru.recoin.common.util.render.effects.BloodSimulation;
import ru.recoin.common.util.render.pipeline.BloodRenderPipeline;
import ru.recoin.implement.events.render.WorldRenderEvent;

public final class BloodKillEffect
implements QuickImports,
AutoCloseable {
    private final BloodSimulation simulation = new BloodSimulation();
    private BloodMesh mesh;
    private BloodRenderPipeline pipeline;
    private final BloodSimulation.Environment environment = new BloodSimulation.Environment(){

        @Override
        public BloodSimulation.Surface trace(class_243 from, class_243 to) {
            if (QuickImports.mc.field_1687 == null || QuickImports.mc.field_1724 == null) {
                return null;
            }
            class_3965 hit = QuickImports.mc.field_1687.method_17742(new class_3959(from, to, class_3959.class_3960.field_17558, class_3959.class_242.field_1347, (class_1297)QuickImports.mc.field_1724));
            if (hit.method_17783() != class_239.class_240.field_1332) {
                return null;
            }
            class_2338 block = hit.method_17777();
            class_243 normal = hit.method_17780().method_62676();
            if (!QuickImports.mc.field_1687.method_8316(block).method_15769()) {
                return new BloodSimulation.Surface(hit.method_17784(), normal, new class_238(block), block.method_10063(), true);
            }
            class_238 surfaceBounds = null;
            double nearest = Double.POSITIVE_INFINITY;
            for (class_238 local : QuickImports.mc.field_1687.method_8320(block).method_26220((class_1922)QuickImports.mc.field_1687, block).method_1090()) {
                class_238 box = local.method_996(block);
                double distance = BloodKillEffect.faceDistance(box, hit.method_17784(), hit.method_17780());
                if (!(distance < nearest)) continue;
                nearest = distance;
                surfaceBounds = box;
            }
            if (surfaceBounds == null) {
                return null;
            }
            long key = (block.method_10063() * 31L + (long)hit.method_17780().ordinal()) * 31L + (long)surfaceBounds.hashCode();
            return new BloodSimulation.Surface(hit.method_17784(), normal, surfaceBounds, key, false);
        }

        @Override
        public float light(class_243 position) {
            if (QuickImports.mc.field_1687 == null) {
                return 1.0f;
            }
            class_2338 block = class_2338.method_49638((class_2374)position);
            float day = 0.1f + 0.9f * Math.max(0.0f, (float)Math.sin((double)Math.floorMod(QuickImports.mc.field_1687.method_8532(), 24000L) / 24000.0 * Math.PI * 2.0));
            float sky = (float)QuickImports.mc.field_1687.method_8314(class_1944.field_9284, block) * (day *= 1.0f - QuickImports.mc.field_1687.method_8430(1.0f) * 0.35f);
            float artificial = QuickImports.mc.field_1687.method_8314(class_1944.field_9282, block);
            return 0.1f + 0.9f * Math.max(sky, artificial) / 15.0f;
        }

        @Override
        public boolean contains(class_243 position) {
            return QuickImports.mc.field_1687 != null && position.field_1351 >= (double)QuickImports.mc.field_1687.method_31607() - 1.0 && QuickImports.mc.field_1687.method_8393(class_3532.method_15357((double)position.field_1352) >> 4, class_3532.method_15357((double)position.field_1350) >> 4);
        }

        @Override
        public boolean supports(BloodSimulation.Surface surface) {
            class_243 offset = surface.normal().method_1021(0.03);
            BloodSimulation.Surface current = this.trace(surface.position().method_1019(offset), surface.position().method_1020(offset));
            return current != null && !current.liquid() && current.position().method_1025(surface.position()) < 1.0E-4 && current.normal().method_1026(surface.normal()) > 0.99 && current.bounds().equals((Object)surface.bounds());
        }
    };

    public void burst(class_243 center, float width, float height, int count, float power, float size, int color, float seconds, class_243 impulse) {
        this.simulation.burst(center, width, height, count, power, size, color, Math.round(seconds * 20.0f), impulse, this.environment);
    }

    public void tick(boolean wallDrips) {
        if (!this.simulation.isEmpty()) {
            this.simulation.tick(this.environment, wallDrips);
        }
    }

    public void render(WorldRenderEvent event, boolean wallDrips) {
        if (this.simulation.isEmpty() || BloodKillEffect.mc.field_1687 == null || BloodKillEffect.mc.field_1773 == null) {
            return;
        }
        if (this.mesh == null) {
            this.mesh = new BloodMesh();
        }
        class_243 camera = BloodKillEffect.mc.field_1773.method_19418().method_71156();
        try (class_9801 geometry = this.mesh.build(this.simulation, camera, event.getPartialTicks(), wallDrips);){
            if (geometry == null) {
                return;
            }
            if (this.pipeline == null) {
                this.pipeline = new BloodRenderPipeline();
            }
            Matrix4f matrix = new Matrix4f((Matrix4fc)Render3DUtil.lastProjMat).mul((Matrix4fc)event.getStack().method_23760().method_23761());
            this.pipeline.draw(geometry, matrix, camera, BloodKillEffect.mc.field_1687.method_8532(), BloodKillEffect.mc.field_1687.method_8430(event.getPartialTicks()));
        }
    }

    public void clear() {
        this.simulation.clear();
    }

    public boolean isEmpty() {
        return this.simulation.isEmpty();
    }

    @Override
    public void close() {
        this.clear();
        if (this.mesh != null) {
            this.mesh.close();
        }
        if (this.pipeline != null) {
            this.pipeline.close();
        }
        this.mesh = null;
        this.pipeline = null;
    }

    private static double faceDistance(class_238 box, class_243 point, class_2350 direction) {
        double plane = switch (direction) {
            default -> throw new MatchException(null, null);
            case class_2350.field_11036 -> Math.abs(point.field_1351 - box.field_1325);
            case class_2350.field_11033 -> Math.abs(point.field_1351 - box.field_1322);
            case class_2350.field_11034 -> Math.abs(point.field_1352 - box.field_1320);
            case class_2350.field_11039 -> Math.abs(point.field_1352 - box.field_1323);
            case class_2350.field_11035 -> Math.abs(point.field_1350 - box.field_1324);
            case class_2350.field_11043 -> Math.abs(point.field_1350 - box.field_1321);
        };
        class_243 clamped = new class_243(class_3532.method_15350((double)point.field_1352, (double)box.field_1323, (double)box.field_1320), class_3532.method_15350((double)point.field_1351, (double)box.field_1322, (double)box.field_1325), class_3532.method_15350((double)point.field_1350, (double)box.field_1321, (double)box.field_1324));
        return plane + clamped.method_1022(point);
    }
}

