package ru.recoin.implement.features.modules.render.perf;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_1297;
import net.minecraft.class_1303;
import net.minecraft.class_1530;
import net.minecraft.class_1531;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2338;
import net.minecraft.class_2580;
import net.minecraft.class_2586;
import net.minecraft.class_2640;
import net.minecraft.class_2643;
import net.minecraft.class_310;
import net.minecraft.class_638;
import ru.recoin.api.feature.module.Module;
import ru.recoin.common.util.other.Instance;

/**
 * Render culling used by Optimization:
 * - occlusion culling: entities and block entities (chests, signs, banners, heads, ...) fully hidden
 *   behind solid blocks are not drawn. Visibility is checked with a few rays through the block grid
 *   from the camera to the object's box and cached for a short time, so it costs very little;
 * - distance culling: small entities (items, XP orbs, armor stands, item frames, paintings),
 *   block entities and particles are not drawn / spawned beyond the set distances.
 * Called from the render mixins on the render thread only.
 */
public final class RenderCulling {
    // settings, written by Optimization
    public static volatile boolean enabled;
    public static boolean entityOcclusion = true;
    public static boolean cullPlayers;
    public static boolean blockEntityOcclusion = true;
    public static double blockEntityDistance = 64.0;   // 0 = no limit
    public static double smallEntityDistance = 32.0;   // 0 = no limit
    public static double particleDistance = 24.0;      // 0 = no limit

    private static final long VISIBLE_RECHECK_MS = 250L;
    private static final long HIDDEN_RECHECK_MS = 60L;
    private static final double MAX_OCCLUSION_DISTANCE = 128.0;
    private static final double NEAR_DISTANCE = 3.0;
    private static final Map<Integer, long[]> ENTITY_CACHE = new HashMap<>();   // id -> {checkedAt, visible(1/0)}
    private static final Map<Long, long[]> BLOCK_CACHE = new HashMap<>();
    private static final class_2338.class_2339 CURSOR = new class_2338.class_2339();
    private static class_638 cacheWorld;
    private static long lastPurge;
    private static long espCheckedAt;
    private static boolean espActive;

    private RenderCulling() {
    }

    /** The EntityCulling mod already hides entities and block entities behind walls: don't do the same work twice. */
    public static final boolean ENTITY_CULLING_MOD = RenderCulling.modLoaded("entityculling");

    private static boolean modLoaded(String id) {
        try {
            return net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded(id);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static void reset() {
        ENTITY_CACHE.clear();
        BLOCK_CACHE.clear();
        cacheWorld = null;
    }

    /** true = do not draw this entity this frame */
    public static boolean hideEntity(class_1297 entity) {
        if (!enabled || entity == null) {
            return false;
        }
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || mc.field_1773 == null || entity == mc.field_1724 || entity == mc.method_1560()) {
            return false;
        }
        class_243 cam = mc.field_1773.method_19418().method_71156();
        double dx = entity.method_23317() - cam.field_1352;
        double dy = entity.method_23318() - cam.field_1351;
        double dz = entity.method_23321() - cam.field_1350;
        double sq = dx * dx + dy * dy + dz * dz;
        if (smallEntityDistance > 0.0 && RenderCulling.isSmall(entity) && sq > smallEntityDistance * smallEntityDistance) {
            return true;
        }
        if (!entityOcclusion || ENTITY_CULLING_MOD || sq < NEAR_DISTANCE * NEAR_DISTANCE || sq > MAX_OCCLUSION_DISTANCE * MAX_OCCLUSION_DISTANCE) {
            return false;
        }
        if (entity instanceof class_1657 && !cullPlayers || entity.method_5851() || entity.method_5765() || mc.field_1724 != null && (entity.method_5626(mc.field_1724) || mc.field_1724.method_5854() == entity)) {
            return false;
        }
        if (RenderCulling.espActive()) {
            return false;   // ESP outlines must still see entities behind walls
        }
        class_238 box = entity.method_5829();
        if (box.method_17939() > 8.0 || box.method_17940() > 8.0 || box.method_17941() > 8.0) {
            return false;   // huge entities (dragon, ...) are never culled
        }
        RenderCulling.prepare(mc);
        long now = System.currentTimeMillis();
        long[] entry = ENTITY_CACHE.get(entity.method_5628());
        if (entry != null && now - entry[0] < (entry[1] == 1L ? VISIBLE_RECHECK_MS : HIDDEN_RECHECK_MS)) {
            return entry[1] == 0L;
        }
        if (RenderCulling.cameraInsideBlock(mc, cam)) {
            return false;
        }
        boolean visible = RenderCulling.boxVisible(mc.field_1687, cam, box.method_1014(0.25));
        if (entry == null) {
            entry = new long[2];
            ENTITY_CACHE.put(entity.method_5628(), entry);
        }
        entry[0] = now;
        entry[1] = visible ? 1L : 0L;
        return !visible;
    }

    /** true = do not draw this block entity this frame */
    public static boolean hideBlockEntity(class_2586 blockEntity) {
        if (!enabled || blockEntity == null || blockEntity instanceof class_2580 || blockEntity instanceof class_2643 || blockEntity instanceof class_2640) {
            return false;   // beacon beams and portals are seen from far away
        }
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || mc.field_1773 == null) {
            return false;
        }
        class_2338 pos = blockEntity.method_11016();
        class_243 cam = mc.field_1773.method_19418().method_71156();
        double dx = pos.method_10263() + 0.5 - cam.field_1352;
        double dy = pos.method_10264() + 0.5 - cam.field_1351;
        double dz = pos.method_10260() + 0.5 - cam.field_1350;
        double sq = dx * dx + dy * dy + dz * dz;
        if (blockEntityDistance > 0.0 && sq > blockEntityDistance * blockEntityDistance) {
            return true;
        }
        if (!blockEntityOcclusion || ENTITY_CULLING_MOD || sq < NEAR_DISTANCE * NEAR_DISTANCE || sq > MAX_OCCLUSION_DISTANCE * MAX_OCCLUSION_DISTANCE) {
            return false;
        }
        RenderCulling.prepare(mc);
        long now = System.currentTimeMillis();
        long key = pos.method_10063();
        long[] entry = BLOCK_CACHE.get(key);
        if (entry != null && now - entry[0] < (entry[1] == 1L ? VISIBLE_RECHECK_MS * 2L : HIDDEN_RECHECK_MS * 2L)) {
            return entry[1] == 0L;
        }
        if (RenderCulling.cameraInsideBlock(mc, cam)) {
            return false;
        }
        // signs, banners and heads stick out of their block a little: test a slightly bigger box
        class_238 box = new class_238(pos.method_10263() - 0.3, pos.method_10264() - 0.3, pos.method_10260() - 0.3, pos.method_10263() + 1.3, pos.method_10264() + 1.6, pos.method_10260() + 1.3);
        boolean visible = RenderCulling.boxVisible(mc.field_1687, cam, box);
        if (entry == null) {
            entry = new long[2];
            BLOCK_CACHE.put(key, entry);
        }
        entry[0] = now;
        entry[1] = visible ? 1L : 0L;
        return !visible;
    }

    /** true = do not spawn this particle */
    public static boolean skipParticle(double x, double y, double z) {
        if (!enabled || particleDistance <= 0.0) {
            return false;
        }
        class_310 mc = class_310.method_1551();
        if (mc.field_1773 == null) {
            return false;
        }
        class_243 cam = mc.field_1773.method_19418().method_71156();
        double dx = x - cam.field_1352;
        double dy = y - cam.field_1351;
        double dz = z - cam.field_1350;
        return dx * dx + dy * dy + dz * dz > particleDistance * particleDistance;
    }

    private static boolean isSmall(class_1297 entity) {
        return entity instanceof class_1542 || entity instanceof class_1303 || entity instanceof class_1531 || entity instanceof class_1530;
    }

    private static boolean espActive() {
        long now = System.currentTimeMillis();
        if (now - espCheckedAt > 1000L) {
            espCheckedAt = now;
            espActive = RenderCulling.moduleOn("ShaderESP") || RenderCulling.moduleOn("SeeInvisible");
        }
        return espActive;
    }

    private static boolean moduleOn(String name) {
        try {
            Module module = Instance.get(name);
            return module != null && module.isState();
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void prepare(class_310 mc) {
        if (cacheWorld != mc.field_1687) {
            ENTITY_CACHE.clear();
            BLOCK_CACHE.clear();
            cacheWorld = mc.field_1687;
        }
        long now = System.currentTimeMillis();
        if (now - lastPurge > 10000L) {
            lastPurge = now;
            ENTITY_CACHE.values().removeIf(e -> now - e[0] > 5000L);
            BLOCK_CACHE.values().removeIf(e -> now - e[0] > 5000L);
        }
    }

    private static boolean cameraInsideBlock(class_310 mc, class_243 cam) {
        CURSOR.method_10102(cam.field_1352, cam.field_1351, cam.field_1350);
        return mc.field_1687.method_8320(CURSOR).method_26216();   // spectator / freecam inside walls: never cull
    }

    /** Visible if at least one ray from the camera reaches the centre, the top or one of the corners of the box. */
    private static boolean boxVisible(class_638 world, class_243 cam, class_238 box) {
        double cx = (box.field_1323 + box.field_1320) * 0.5;
        double cy = (box.field_1322 + box.field_1325) * 0.5;
        double cz = (box.field_1321 + box.field_1324) * 0.5;
        if (RenderCulling.rayClear(world, cam, cx, cy, cz)) return true;
        if (RenderCulling.rayClear(world, cam, cx, box.field_1325, cz)) return true;
        // the corners facing the camera first
        double nx = cam.field_1352 < cx ? box.field_1323 : box.field_1320;
        double fx = cam.field_1352 < cx ? box.field_1320 : box.field_1323;
        double nz = cam.field_1350 < cz ? box.field_1321 : box.field_1324;
        double fz = cam.field_1350 < cz ? box.field_1324 : box.field_1321;
        double[] xs = {nx, nx, fx, nx, fx, fx};
        double[] zs = {nz, fz, nz, nz, fz, nz};
        for (int i = 0; i < xs.length; ++i) {
            if (RenderCulling.rayClear(world, cam, xs[i], box.field_1325, zs[i])) return true;
            if (RenderCulling.rayClear(world, cam, xs[i], box.field_1322, zs[i])) return true;
        }
        return false;
    }

    /** Walks the block grid from the camera to the point (Amanatides-Woo); false when a full opaque block is in the way. */
    private static boolean rayClear(class_638 world, class_243 from, double tx, double ty, double tz) {
        double dx = tx - from.field_1352;
        double dy = ty - from.field_1351;
        double dz = tz - from.field_1350;
        int x = (int)Math.floor(from.field_1352);
        int y = (int)Math.floor(from.field_1351);
        int z = (int)Math.floor(from.field_1350);
        int ex = (int)Math.floor(tx);
        int ey = (int)Math.floor(ty);
        int ez = (int)Math.floor(tz);
        int sx = dx > 0 ? 1 : dx < 0 ? -1 : 0;
        int sy = dy > 0 ? 1 : dy < 0 ? -1 : 0;
        int sz = dz > 0 ? 1 : dz < 0 ? -1 : 0;
        double tdx = sx == 0 ? Double.MAX_VALUE : Math.abs(1.0 / dx);
        double tdy = sy == 0 ? Double.MAX_VALUE : Math.abs(1.0 / dy);
        double tdz = sz == 0 ? Double.MAX_VALUE : Math.abs(1.0 / dz);
        double tmx = sx == 0 ? Double.MAX_VALUE : (sx > 0 ? x + 1 - from.field_1352 : from.field_1352 - x) * tdx;
        double tmy = sy == 0 ? Double.MAX_VALUE : (sy > 0 ? y + 1 - from.field_1351 : from.field_1351 - y) * tdy;
        double tmz = sz == 0 ? Double.MAX_VALUE : (sz > 0 ? z + 1 - from.field_1350 : from.field_1350 - z) * tdz;
        int guard = 0;
        while (guard++ < 400) {
            if (tmx < tmy && tmx < tmz) {
                if (tmx > 1.0) break;
                x += sx;
                tmx += tdx;
            } else if (tmy < tmz) {
                if (tmy > 1.0) break;
                y += sy;
                tmy += tdy;
            } else {
                if (tmz > 1.0) break;
                z += sz;
                tmz += tdz;
            }
            if (x == ex && y == ey && z == ez) {
                return true;   // reached the target cell
            }
            CURSOR.method_10103(x, y, z);
            if (world.method_8320(CURSOR).method_26216()) {
                return false;
            }
        }
        return true;
    }
}
