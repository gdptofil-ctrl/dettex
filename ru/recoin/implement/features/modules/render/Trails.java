/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_1937
 *  net.minecraft.class_243
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.features.modules.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import ru.recoin.api.accessor.IEntityTrails;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.IParticleModule;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.particle.ParticleUpdateExecutor;
import ru.recoin.common.util.render.TrailRenderer;
import ru.recoin.common.util.spatial.BlockCollisionGrid;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;
import ru.recoin.implement.features.modules.particles.types.WorldParticle;

public class Trails
extends Module
implements IParticleModule {
    public static final float FIXED_OUTLINE_WIDTH = 1.27f;
    public static final float FIXED_OUTLINE_ALPHA = 255.54f;
    public static final float FIXED_DOWN = 0.0f;
    public static final float FIXED_HEIGHT = 100.0f;
    public final SelectSetting players = new SelectSetting("Players", "What kind of trail to render").value("Trail", "Particles").selected("Trail");
    public final BooleanSetting friends = new BooleanSetting("Friends", "Render trails for friends").setValue(false);
    public final SelectSetting style = new SelectSetting("Style", "Trail vertical fade style").value("Solid", "Faded", "Invert").selected("Solid").visible(() -> this.players.isSelected("Trail"));
    public final ValueSetting alphaFactor = new ValueSetting("Alpha Factor", "Invert alpha center factor").setValue(50.0f).range(0.0f, 100.0f).visible(() -> this.players.isSelected("Trail") && this.style.isSelected("Invert"));
    public final ValueSetting trailAlpha = new ValueSetting("Alpha", "Trail alpha").setValue(100.0f).range(0.0f, 100.0f).visible(() -> this.players.isSelected("Trail"));
    public final BooleanSetting outline = new BooleanSetting("Outline", "Draw trail outline").setValue(true).visible(() -> this.players.isSelected("Trail"));
    public final ValueSetting trailDuration = new ValueSetting("Duration", "Trail duration in seconds").setValue(1.5f).range(0.1f, 4.0f).step(0.05f).visible(() -> this.players.isSelected("Trail"));
    public final SelectSetting particlePhysics = new SelectSetting("Particle Physics", "Particle movement behavior").value("On", "Off").selected("On").visible(() -> this.players.isSelected("Particles"));
    public final SelectSetting particleType = new SelectSetting("Particle Type", "Shared particle appearance").value("Stars", "Hearts", "Bloom", "Blink", "Coron", "Dollar", "Flame", "Snowflake", "Virus").selected("Bloom").visible(() -> this.players.isSelected("Particles"));
    public final SelectSetting particleColorMode = new SelectSetting("Particle Color Mode", "Trail particle color mode").value("Sync").selected("Sync");
    public final SelectSetting particleColorAnimation = new SelectSetting("Particle Color Animation", "Trail particle color animation").value("Wave").selected("Wave");
    public final List<WorldParticle> particles = new ArrayList<WorldParticle>();
    public final Map<Integer, Long> particleSpawnTimes = new HashMap<Integer, Long>();
    public final BlockCollisionGrid particleCollisionGrid = new BlockCollisionGrid(0.25f, 0.006f);
    public long lastTrailUpdateTime;

    public static Trails getInstance() {
        return Instance.get(Trails.class);
    }

    public Trails() {
        super("Trails", ModuleCategory.RENDER);
        this.setup(this.players, this.friends, this.style, this.alphaFactor, this.trailAlpha, this.outline, this.trailDuration, this.particleType, this.particlePhysics);
    }

    @Override
    public SelectSetting getColorMode() {
        return this.particleColorMode;
    }

    @Override
    public SelectSetting getColorAnimation() {
        return this.particleColorAnimation;
    }

    @Override
    public int[] getCustomColors() {
        return null;
    }

    @Override
    public void deactivate() {
        if (Trails.mc.field_1687 != null) {
            Trails.mc.field_1687.method_18456().forEach(player -> ((IEntityTrails)player).getTrails().clear());
        }
        this.particles.clear();
        this.particleSpawnTimes.clear();
        this.particleCollisionGrid.clear();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (Trails.mc.field_1687 == null || Trails.mc.field_1724 == null) {
            return;
        }
        this.cleanupExpiredTrails();
        this.updateParticles();
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (Trails.mc.field_1687 == null || Trails.mc.field_1724 == null) {
            return;
        }
        if (this.players.isSelected("Particles")) {
            this.spawnParticles();
            this.renderParticles(event);
            return;
        }
        this.particles.clear();
        this.particleSpawnTimes.clear();
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastTrailUpdateTime > 0L) {
            this.lastTrailUpdateTime = currentTime;
            this.updateTrails();
        }
        this.queuePlayerTrails(event.getStack());
        TrailRenderer.renderBatches();
    }

    private void spawnParticles() {
        long now = System.currentTimeMillis();
        for (class_1657 player : Trails.mc.field_1687.method_18456()) {
            long lastSpawn;
            if (!this.shouldRenderFor(player) || !this.isMoving(player) || now - (lastSpawn = this.particleSpawnTimes.getOrDefault(player.method_5628(), 0L).longValue()) < 45L) continue;
            this.particleSpawnTimes.put(player.method_5628(), now);
            ThreadLocalRandom random = ThreadLocalRandom.current();
            class_243 center = MathUtil.interpolate((class_1297)player).method_1031(random.nextDouble(-0.22, 0.22), (double)player.method_17682() * 0.54 + random.nextDouble(-0.12, 0.12), random.nextDouble(-0.22, 0.22));
            boolean physics = this.particlePhysics.isSelected("On");
            this.particles.add(new WorldParticle(center.field_1352, center.field_1351, center.field_1350, new Color(ColorUtil.getClientColor(), true), random.nextFloat() * 360.0f, random.nextFloat(4.0f, 14.0f), 0.0f, this.particleType.getSelected(), physics ? "Fall" : "Emerge", null, random.nextFloat(1.1f, 1.75f), random.nextFloat(1.15f, 1.85f), 1.0f, this));
        }
        if (this.particles.size() > 450) {
            this.particles.subList(0, this.particles.size() - 450).clear();
        }
    }

    private void updateParticles() {
        if (Trails.mc.field_1687 == null || !this.players.isSelected("Particles")) {
            this.particles.clear();
            return;
        }
        long now = System.currentTimeMillis();
        BlockCollisionGrid collisionGrid = null;
        if (this.particlePhysics.isSelected("On")) {
            this.particleCollisionGrid.updateBlocks((class_1937)Trails.mc.field_1687, Trails.mc.field_1724.method_23317(), Trails.mc.field_1724.method_23318(), Trails.mc.field_1724.method_23321(), 28);
            collisionGrid = this.particleCollisionGrid;
        }
        List<WorldParticle> expired = ParticleUpdateExecutor.updateParticles(this.particles, now, particle -> true, collisionGrid);
        this.particles.removeAll(expired);
    }

    private void renderParticles(WorldRenderEvent event) {
        long now = System.currentTimeMillis();
        for (WorldParticle particle : this.particles) {
            particle.render(event.getStack(), now);
        }
    }

    private boolean shouldRenderFor(class_1657 player) {
        if (player == Trails.mc.field_1724) {
            return !Trails.mc.field_1690.method_31044().method_31034();
        }
        return this.friends.isValue() && FriendUtils.isFriend((class_1297)player);
    }

    private boolean isMoving(class_1657 player) {
        return Math.abs(player.method_23317() - player.field_6014) > 0.001 || Math.abs(player.method_23321() - player.field_5969) > 0.001;
    }

    private void queuePlayerTrails(class_4587 stack) {
        class_243 cameraPos = Trails.mc.field_1773.method_19418().method_71156();
        double maxRenderDistSq = 4096.0;
        Trails.mc.field_1687.method_18456().stream().filter(player -> {
            if (player == Trails.mc.field_1724) {
                return !Trails.mc.field_1690.method_31044().method_31034();
            }
            return this.friends.isValue() && FriendUtils.isFriend((class_1297)player);
        }).filter(player -> player.method_5707(cameraPos) <= maxRenderDistSq).map(player -> ((IEntityTrails)player).getTrails()).filter(list -> !list.isEmpty()).forEach(list -> {
            TrailRenderer.queueTrails(stack, list, this);
            if (this.outline.isValue()) {
                TrailRenderer.queueDebugLines(stack, list, this);
            }
        });
    }

    private void queuePlayerTails(class_4587 stack) {
        class_243 cameraPos = Trails.mc.field_1773.method_19418().method_71156();
        double maxRenderDistSq = 4096.0;
        Trails.mc.field_1687.method_18456().stream().filter(player -> {
            if (player == Trails.mc.field_1724) {
                return !Trails.mc.field_1690.method_31044().method_31034();
            }
            return this.friends.isValue() && FriendUtils.isFriend((class_1297)player);
        }).filter(player -> player.method_5707(cameraPos) <= maxRenderDistSq).map(player -> ((IEntityTrails)player).getTrails()).filter(list -> !list.isEmpty()).forEach(list -> TrailRenderer.queueTails(stack, list, this));
    }

    private void updateTrails() {
        Color baseColor = new Color(ColorUtil.getClientColor(), true);
        for (class_1657 player : Trails.mc.field_1687.method_18456()) {
            List<Trail> trails = ((IEntityTrails)player).getTrails();
            boolean shouldCreateTrail = false;
            if (player != Trails.mc.field_1724 && (player.method_23321() != player.field_5969 || player.method_23317() != player.field_6014) && this.friends.isValue() && FriendUtils.isFriend((class_1297)player)) {
                shouldCreateTrail = true;
            }
            if (!(player != Trails.mc.field_1724 || Trails.mc.field_1724.method_18798().field_1352 == 0.0 && Trails.mc.field_1724.method_18798().field_1350 == 0.0 || Trails.mc.field_1690.method_31044().method_31034())) {
                shouldCreateTrail = true;
            }
            if (!shouldCreateTrail) continue;
            class_243 interpolatedPos = MathUtil.interpolate((class_1297)player);
            class_243 lastPos = trails.isEmpty() ? new class_243(player.field_6014, player.field_6036, player.field_5969) : trails.getLast().currentPos;
            long lifetime = (long)(this.trailDuration.getValue() * 1000.0f);
            double distance = lastPos.method_1022(interpolatedPos);
            int subdivisions = Math.max(1, (int)Math.ceil(distance * 3.0));
            subdivisions = Math.min(subdivisions, 10);
            for (int sub = 0; sub < subdivisions; ++sub) {
                float t = (float)sub / (float)subdivisions;
                float nextT = (float)(sub + 1) / (float)subdivisions;
                class_243 subFrom = new class_243(lastPos.field_1352 + (interpolatedPos.field_1352 - lastPos.field_1352) * (double)t, lastPos.field_1351 + (interpolatedPos.field_1351 - lastPos.field_1351) * (double)t, lastPos.field_1350 + (interpolatedPos.field_1350 - lastPos.field_1350) * (double)t);
                class_243 subTo = new class_243(lastPos.field_1352 + (interpolatedPos.field_1352 - lastPos.field_1352) * (double)nextT, lastPos.field_1351 + (interpolatedPos.field_1351 - lastPos.field_1351) * (double)nextT, lastPos.field_1350 + (interpolatedPos.field_1350 - lastPos.field_1350) * (double)nextT);
                trails.add(new Trail(subFrom, subTo, baseColor, player.method_17682(), lifetime));
            }
        }
    }

    private void cleanupExpiredTrails() {
        for (class_1657 player : Trails.mc.field_1687.method_18456()) {
            ((IEntityTrails)player).getTrails().removeIf(Trail::shouldRemove);
        }
    }

    public SelectSetting getPlayers() {
        return this.players;
    }

    public BooleanSetting getFriends() {
        return this.friends;
    }

    public SelectSetting getStyle() {
        return this.style;
    }

    public ValueSetting getAlphaFactor() {
        return this.alphaFactor;
    }

    public ValueSetting getTrailAlpha() {
        return this.trailAlpha;
    }

    public BooleanSetting getOutline() {
        return this.outline;
    }

    public ValueSetting getTrailDuration() {
        return this.trailDuration;
    }

    public SelectSetting getParticlePhysics() {
        return this.particlePhysics;
    }

    public SelectSetting getParticleType() {
        return this.particleType;
    }

    public SelectSetting getParticleColorMode() {
        return this.particleColorMode;
    }

    public SelectSetting getParticleColorAnimation() {
        return this.particleColorAnimation;
    }

    public List<WorldParticle> getParticles() {
        return this.particles;
    }

    public Map<Integer, Long> getParticleSpawnTimes() {
        return this.particleSpawnTimes;
    }

    public BlockCollisionGrid getParticleCollisionGrid() {
        return this.particleCollisionGrid;
    }

    public long getLastTrailUpdateTime() {
        return this.lastTrailUpdateTime;
    }

    public static class Trail
    implements QuickImports {
        private final class_243 prevPos;
        public class_243 currentPos;
        private final Color color;
        public final float entityHeight;
        private final long creationTime;
        private final long maxLifetime;
        private long lastCacheTime = -1L;
        private float cachedProgress;
        private float cachedAlpha = 1.0f;

        public Trail(class_243 from, class_243 to, Color color, float entityHeight, long maxLifetimeMs) {
            this.prevPos = from;
            this.currentPos = to;
            this.color = color;
            this.entityHeight = entityHeight;
            this.creationTime = System.currentTimeMillis();
            this.maxLifetime = maxLifetimeMs;
        }

        public class_243 interpolate(float pt) {
            double x = this.prevPos.field_1352 + (this.currentPos.field_1352 - this.prevPos.field_1352) * (double)pt;
            double y = this.prevPos.field_1351 + (this.currentPos.field_1351 - this.prevPos.field_1351) * (double)pt;
            double z = this.prevPos.field_1350 + (this.currentPos.field_1350 - this.prevPos.field_1350) * (double)pt;
            return new class_243(x, y, z);
        }

        public Color color() {
            return this.color;
        }

        private void updateCache() {
            long currentTime = System.currentTimeMillis();
            if (currentTime != this.lastCacheTime) {
                this.lastCacheTime = currentTime;
                long age = currentTime - this.creationTime;
                this.cachedProgress = Math.min(1.0f, (float)age / (float)this.maxLifetime);
                this.cachedAlpha = 1.0f - this.cachedProgress;
            }
        }

        public float getProgress() {
            this.updateCache();
            return this.cachedProgress;
        }

        public float getAlpha() {
            this.updateCache();
            return this.cachedAlpha;
        }

        public boolean shouldRemove() {
            return System.currentTimeMillis() - this.creationTime >= this.maxLifetime;
        }
    }
}

