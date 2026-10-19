/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_1297
 *  net.minecraft.class_1667
 *  net.minecraft.class_1685
 *  net.minecraft.class_1937
 *  net.minecraft.class_243
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  net.minecraft.class_4587
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package ru.recoin.implement.features.modules.particles;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import net.minecraft.class_1297;
import net.minecraft.class_1667;
import net.minecraft.class_1685;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_4587;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.IParticleModule;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.GroupSetting;
import ru.recoin.api.feature.module.setting.implement.MultiColorSetting;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.particle.ParticleColorUtil;
import ru.recoin.common.util.render.LegacyRenderSystem;
import ru.recoin.common.util.spatial.BlockCollisionGrid;
import ru.recoin.common.util.spatial.SpatialGrid3D;
import ru.recoin.compat.render.BufferRenderer;
import ru.recoin.compat.render.ShaderProgramKeys;
import ru.recoin.implement.events.player.AttackEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;
import ru.recoin.implement.features.modules.particles.types.FireFlyParticle;
import ru.recoin.implement.features.modules.particles.types.WorldParticle;

public class AmbientParticles
extends Module
implements IParticleModule {
    private static final String SOURCE_WORLD = "\u0412 \u043c\u0438\u0440\u0435";
    private static final String SOURCE_HIT = "\u041f\u0440\u0438 \u0443\u0434\u0430\u0440\u0435";
    private static final String SOURCE_WALK = "\u041f\u0440\u0438 \u0445\u043e\u0434\u044c\u0431\u0435";
    private static final String SOURCE_ARROWS = "\u0421\u0442\u0440\u0435\u043b\u044b";
    private static final String SOURCE_TRIDENTS = "\u0422\u0440\u0435\u0437\u0443\u0431\u0446\u044b";
    private final SelectSetting colorMode = new SelectSetting("Color Mode", "Color mode").value("Sync", "Custom").selected("Sync");
    private final SelectSetting customColorsCount = new SelectSetting("Color Count", "Number of colors in gradient").value("Solo", "Duo", "Triple", "Quartet").selected("Solo").visible(() -> this.colorMode.isSelected("Custom"));
    private final MultiColorSetting customColors = new MultiColorSetting("Gradient Colors", "Colors for custom gradient").colors("Color 1", "Color 2", "Color 3", "Color 4").defaultColors(-1499549, -13273872, -6596170, -409301).visible(() -> this.colorMode.isSelected("Custom"));
    private final SelectSetting colorAnimation = new SelectSetting("Color Animation", "Type of color animation").value("Wave", "Vertex").selected("Wave").visible(() -> this.colorMode.isSelected("Custom"));
    private final MultiSelectSetting mode = new MultiSelectSetting("Mode", "Particle rendering mode").value("Stars", "Hearts", "Bloom", "Blink", "Coron", "Dollar", "Flame", "Snowflake", "Virus", "Sword", "Firefly", "Network", "Cube", "Pyramid", "3D Lines").selected("Stars");
    private final MultiSelectSetting sources = new MultiSelectSetting("Sources", "Where particles are created").value("\u0412 \u043c\u0438\u0440\u0435", "\u041f\u0440\u0438 \u0443\u0434\u0430\u0440\u0435", "\u041f\u0440\u0438 \u0445\u043e\u0434\u044c\u0431\u0435", "\u0421\u0442\u0440\u0435\u043b\u044b", "\u0422\u0440\u0435\u0437\u0443\u0431\u0446\u044b").selected("\u0412 \u043c\u0438\u0440\u0435", "\u041f\u0440\u0438 \u0443\u0434\u0430\u0440\u0435");
    private final MultiSelectSetting physics = new MultiSelectSetting("Physics", "Particle physics").value("Fall", "Fly", "Emerge").selected("Fly");
    private final ValueSetting regularCount = new ValueSetting("Regular Count", "Number of regular particles").setValue(20.0f).range(5, 80);
    private final ValueSetting regularScale = new ValueSetting("Regular Scale", "Regular particle scale").setValue(2.0f).range(0.1f, 8.0f);
    private final ValueSetting fireflyCount = new ValueSetting("Firefly Count", "Number of firefly particles").setValue(8.0f).range(2, 25);
    private final ValueSetting fireflyScale = new ValueSetting("Firefly Scale", "Firefly particle scale").setValue(1.0f).range(0.3f, 3.0f);
    private final ValueSetting fireflyTrailLength = new ValueSetting("Trail Length", "Length of firefly trail in ticks").setValue(20.0f).range(5, 60);
    private final ValueSetting geometricsCount = new ValueSetting("Geometrics Count", "Number of geometric particles").setValue(12.0f).range(2, 35);
    private final ValueSetting geometricsScale = new ValueSetting("Geometrics Scale", "Geometric particle scale").setValue(1.4f).range(0.5f, 4.0f);
    private final ValueSetting linkDistance = new ValueSetting("Link Distance", "Maximum distance for Network links").setValue(5.0f).range(2, 8);
    private final ValueSetting maxLinksPerParticle = new ValueSetting("Max Links", "Maximum links per particle").setValue(5.0f).range(1, 10);
    private final ValueSetting spawnRadius = new ValueSetting("Spawn Radius", "Horizontal spawn radius around player").setValue(20.0f).range(8, 35);
    private final ValueSetting spawnHeight = new ValueSetting("Spawn Height", "Vertical spawn range above player").setValue(4.0f).range(2, 12);
    private final ValueSetting sourceAmount = new ValueSetting("Source Amount", "Particles created by an event").setValue(1.0f).range(1, 3);
    private final ValueSetting sourceLifeTime = new ValueSetting("Source Life Time", "Event particle lifetime").setValue(2.0f).range(0.5f, 6.0f);
    private final ValueSetting sourceSpeed = new ValueSetting("Source Speed", "Event particle movement speed").setValue(2.0f).range(0.1f, 10.0f);
    private final List<WorldParticle> particles = new ArrayList<WorldParticle>();
    private final List<WorldParticle> networkParticles = new ArrayList<WorldParticle>();
    private final List<WorldParticle> geometricsParticles = new ArrayList<WorldParticle>();
    private final List<FireFlyParticle> fireflyParticles = new ArrayList<FireFlyParticle>();
    private final List<WorldParticle> sourceParticles = new ArrayList<WorldParticle>();
    private final Map<Integer, Long> projectileSpawnTimes = new HashMap<Integer, Long>();
    private final BlockCollisionGrid collisionGrid = new BlockCollisionGrid(0.9f, 0.012f);
    private SpatialGrid3D<WorldParticle> spatialGrid = null;
    private long lastTickUpdate;
    private long lastWalkSpawn;
    private double lastWalkX = Double.NaN;
    private double lastWalkZ = Double.NaN;

    public static AmbientParticles getInstance() {
        return Instance.get(AmbientParticles.class);
    }

    public AmbientParticles() {
        super("Particles", "Particles", ModuleCategory.RENDER);
        GroupSetting colorGroup = new GroupSetting("Colors", "Custom color settings", false).settings(this.colorMode, this.customColorsCount, this.customColors, this.colorAnimation);
        GroupSetting appearanceGroup = new GroupSetting("Appearance", "Visual appearance settings", false).settings(this.mode, this.physics);
        GroupSetting regularGroup = new GroupSetting("Regular", "Regular particles settings", false).settings(this.regularCount, this.regularScale).visible(() -> this.mode.getSelected().stream().anyMatch(m -> m.equals("Stars") || m.equals("Hearts") || m.equals("Bloom") || m.equals("Blink") || m.equals("Coron") || m.equals("Dollar") || m.equals("Flame") || m.equals("Snowflake") || m.equals("Virus") || m.equals("Sword") || m.equals("Network")));
        GroupSetting fireflyGroup = new GroupSetting("Firefly", "Settings for firefly particles", false).settings(this.fireflyCount, this.fireflyScale, this.fireflyTrailLength).visible(() -> this.mode.getSelected().contains("Firefly"));
        GroupSetting geometricsGroup = new GroupSetting("Geometrics", "Geometric particle settings", false).settings(this.geometricsCount, this.geometricsScale).visible(() -> this.mode.getSelected().contains("Cube") || this.mode.getSelected().contains("Pyramid") || this.mode.getSelected().contains("3D Lines"));
        GroupSetting networkGroup = new GroupSetting("Network", "Settings for network particle links", false).settings(this.linkDistance, this.maxLinksPerParticle).visible(() -> this.mode.getSelected().contains("Network"));
        GroupSetting spawnGroup = new GroupSetting("Spawn", "Particle spawn settings", false).settings(this.spawnRadius, this.spawnHeight);
        GroupSetting sourceGroup = new GroupSetting("Sources", "Particle source settings", false).settings(this.sources, this.sourceAmount, this.sourceLifeTime, this.sourceSpeed);
        this.setup(sourceGroup, colorGroup, appearanceGroup, regularGroup, fireflyGroup, geometricsGroup, networkGroup, spawnGroup);
    }

    @Override
    public SelectSetting getColorMode() {
        return this.colorMode;
    }

    @Override
    public SelectSetting getColorAnimation() {
        return this.colorAnimation;
    }

    @Override
    public int[] getCustomColors() {
        int[] nArray;
        if (!this.colorMode.isSelected("Custom")) {
            return null;
        }
        switch (this.customColorsCount.getSelected()) {
            case "Solo": {
                int[] nArray2 = new int[1];
                nArray = nArray2;
                nArray2[0] = this.customColors.getColor1().getColor();
                break;
            }
            case "Duo": {
                int[] nArray3 = new int[2];
                nArray3[0] = this.customColors.getColor1().getColor();
                nArray = nArray3;
                nArray3[1] = this.customColors.getColor2().getColor();
                break;
            }
            case "Triple": {
                int[] nArray4 = new int[3];
                nArray4[0] = this.customColors.getColor1().getColor();
                nArray4[1] = this.customColors.getColor2().getColor();
                nArray = nArray4;
                nArray4[2] = this.customColors.getColor3().getColor();
                break;
            }
            case "Quartet": {
                nArray = this.customColors.getColorValues();
                break;
            }
            default: {
                nArray = null;
            }
        }
        return nArray;
    }

    private String getRandomMode() {
        List<String> selected = this.mode.getSelected();
        int eligible = 0;
        for (String value : selected) {
            if (value.equals("Firefly") || value.equals("Cube") || value.equals("Pyramid") || value.equals("3D Lines")) continue;
            ++eligible;
        }
        if (eligible == 0) {
            return null;
        }
        int index = ThreadLocalRandom.current().nextInt(eligible);
        for (String value : selected) {
            if (value.equals("Firefly") || value.equals("Cube") || value.equals("Pyramid") || value.equals("3D Lines") || index-- != 0) continue;
            return value;
        }
        return null;
    }

    private String getRandomGeometricMode() {
        ArrayList<String> geo = new ArrayList<String>();
        if (this.mode.isSelected("Cube")) {
            geo.add("Cube");
        }
        if (this.mode.isSelected("Pyramid")) {
            geo.add("Pyramid");
        }
        if (this.mode.isSelected("3D Lines")) {
            geo.add("3D Lines");
        }
        if (geo.isEmpty()) {
            return "Cube";
        }
        return (String)geo.get(ThreadLocalRandom.current().nextInt(geo.size()));
    }

    private String getRandomPhysics() {
        List<String> selected = this.physics.getSelected();
        if (selected.isEmpty()) {
            return "Fall";
        }
        return selected.get(ThreadLocalRandom.current().nextInt(selected.size()));
    }

    @EventHandler
    public void onTick(TickEvent event) {
        long currentTime;
        if (AmbientParticles.mc.field_1724 == null || AmbientParticles.mc.field_1687 == null) {
            return;
        }
        this.lastTickUpdate = currentTime = System.currentTimeMillis();
        this.collisionGrid.updateBlocks((class_1937)AmbientParticles.mc.field_1687, AmbientParticles.mc.field_1724.method_23317(), AmbientParticles.mc.field_1724.method_23318(), AmbientParticles.mc.field_1724.method_23321(), (int)this.spawnRadius.getValue());
        if (this.mode.isSelected("Firefly")) {
            this.fireflyParticles.removeIf(firefly -> firefly.tick(this::isFireflyVisible, this.collisionGrid));
        } else {
            this.fireflyParticles.clear();
        }
        this.updateAndRemove(this.particles, currentTime, this::isParticleVisible, true);
        this.updateAndRemove(this.geometricsParticles, currentTime, this::isParticleVisible, false);
        if (this.spatialGrid != null && !this.networkParticles.isEmpty()) {
            for (WorldParticle particle : this.networkParticles) {
                this.spatialGrid.update(particle, particle.getPx(), particle.getPy(), particle.getPz(), particle.getX(), particle.getY(), particle.getZ());
            }
        }
        List<String> selectedModes = this.mode.getSelected();
        List<String> selectedPhysics = this.physics.getSelected();
        if (this.sources.isSelected(SOURCE_WORLD) && !selectedModes.isEmpty() && !selectedPhysics.isEmpty()) {
            int i;
            int toSpawn;
            boolean hasRegularParticles = selectedModes.stream().anyMatch(m -> !m.equals("Firefly") && !m.equals("Cube") && !m.equals("Pyramid") && !m.equals("3D Lines"));
            if (hasRegularParticles) {
                int currentRegularCount = this.particles.size();
                int targetRegularCount = (int)this.regularCount.getValue();
                toSpawn = Math.min(2, Math.max(0, targetRegularCount - currentRegularCount));
                for (i = 0; i < toSpawn; ++i) {
                    this.spawnParticle(currentTime);
                }
            } else {
                this.particles.clear();
                this.networkParticles.clear();
                if (this.spatialGrid != null) {
                    this.spatialGrid = null;
                }
            }
            if (selectedModes.contains("Cube") || selectedModes.contains("Pyramid") || selectedModes.contains("3D Lines")) {
                int currentGeometricsCount = this.geometricsParticles.size();
                int targetGeometricsCount = (int)this.geometricsCount.getValue();
                toSpawn = Math.min(2, Math.max(0, targetGeometricsCount - currentGeometricsCount));
                for (i = 0; i < toSpawn; ++i) {
                    this.spawnGeometric(currentTime);
                }
            } else {
                this.geometricsParticles.clear();
            }
            if (selectedModes.contains("Firefly")) {
                int currentFireflyCount = this.fireflyParticles.size();
                int targetFireflyCount = (int)this.fireflyCount.getValue();
                toSpawn = Math.min(1, Math.max(0, targetFireflyCount - currentFireflyCount));
                for (i = 0; i < toSpawn; ++i) {
                    this.spawnFirefly();
                }
            }
        } else {
            this.particles.clear();
            this.geometricsParticles.clear();
            this.fireflyParticles.clear();
            this.networkParticles.clear();
            if (this.spatialGrid != null) {
                this.spatialGrid = null;
            }
        }
        this.updateSourceParticles(currentTime);
        this.spawnMovingSources(currentTime);
    }

    @EventHandler
    public void onAttack(AttackEvent event) {
        if (!this.sources.isSelected(SOURCE_HIT) || event.isPre() || event.getTarget() == null) {
            return;
        }
        class_243 point = event.getHitPos();
        if (point == null) {
            point = event.getTarget().method_73189().method_1031(0.0, (double)event.getTarget().method_17682() * 0.5, 0.0);
        }
        this.spawnSourceParticles(point, (int)this.sourceAmount.getValue());
    }

    private void updateSourceParticles(long currentTime) {
        this.updateAndRemove(this.sourceParticles, currentTime, particle -> true, false);
    }

    private void updateAndRemove(List<WorldParticle> target, long currentTime, Predicate<WorldParticle> visibility, boolean trackNetwork) {
        target.removeIf(particle -> {
            if (!particle.update(currentTime, visibility.test((WorldParticle)particle), this.collisionGrid)) {
                return false;
            }
            if (trackNetwork && particle.getParticleMode().equals("Network")) {
                if (this.spatialGrid != null) {
                    this.spatialGrid.remove((WorldParticle)particle, particle.getX(), particle.getY(), particle.getZ());
                }
                this.networkParticles.remove(particle);
            }
            return true;
        });
    }

    private void spawnMovingSources(long currentTime) {
        if (this.sources.isSelected(SOURCE_WALK)) {
            double playerX = AmbientParticles.mc.field_1724.method_23317();
            double playerZ = AmbientParticles.mc.field_1724.method_23321();
            double dx = Double.isNaN(this.lastWalkX) ? 0.0 : playerX - this.lastWalkX;
            double dz = Double.isNaN(this.lastWalkZ) ? 0.0 : playerZ - this.lastWalkZ;
            this.lastWalkX = playerX;
            this.lastWalkZ = playerZ;
            class_243 velocity = AmbientParticles.mc.field_1724.method_18798();
            double velocitySq = velocity.field_1352 * velocity.field_1352 + velocity.field_1350 * velocity.field_1350;
            double movementSq = dx * dx + dz * dz;
            if ((movementSq > 2.5E-5 || velocitySq > 4.0E-4) && AmbientParticles.mc.field_1724.method_24828() && currentTime - this.lastWalkSpawn >= 240L) {
                double directionZ;
                this.lastWalkSpawn = currentTime;
                ThreadLocalRandom random = ThreadLocalRandom.current();
                double directionX = movementSq > 2.5E-5 ? dx : velocity.field_1352;
                double directionLength = Math.sqrt(directionX * directionX + (directionZ = movementSq > 2.5E-5 ? dz : velocity.field_1350) * directionZ);
                if (directionLength > 1.0E-4) {
                    directionX /= directionLength;
                    directionZ /= directionLength;
                }
                this.spawnSourceParticles(new class_243(playerX - directionX * 0.28 + random.nextDouble(-0.22, 0.22), AmbientParticles.mc.field_1724.method_23318() + 0.32, playerZ - directionZ * 0.28 + random.nextDouble(-0.22, 0.22)), Math.max(1, (int)this.sourceAmount.getValue()));
            }
        } else {
            this.lastWalkX = Double.NaN;
            this.lastWalkZ = Double.NaN;
        }
        if (!this.sources.isSelected(SOURCE_ARROWS) && !this.sources.isSelected(SOURCE_TRIDENTS)) {
            return;
        }
        HashSet<Integer> present = new HashSet<Integer>();
        for (class_1297 entity : AmbientParticles.mc.field_1687.method_18112()) {
            boolean selectedProjectile = entity instanceof class_1667 && this.sources.isSelected(SOURCE_ARROWS) || entity instanceof class_1685 && this.sources.isSelected(SOURCE_TRIDENTS);
            if (!selectedProjectile || entity.method_18798().method_1027() < 0.0025) continue;
            present.add(entity.method_5628());
            long previous = this.projectileSpawnTimes.getOrDefault(entity.method_5628(), 0L);
            if (currentTime - previous < 100L) continue;
            this.projectileSpawnTimes.put(entity.method_5628(), currentTime);
            this.spawnSourceParticles(entity.method_73189(), 1);
        }
        this.projectileSpawnTimes.keySet().removeIf(id -> !present.contains(id));
    }

    private void spawnSourceParticles(class_243 position, int count) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < count; ++i) {
            String selectedMode = this.getRandomMode();
            if (selectedMode == null) {
                selectedMode = this.mode.isSelected("Cube") || this.mode.isSelected("Pyramid") || this.mode.isSelected("3D Lines") ? this.getRandomGeometricMode() : "Stars";
            }
            String selectedPhysics = selectedMode.equals("Network") ? "Fly" : this.getRandomPhysics();
            int color = this.colorMode.isSelected("Sync") ? ColorUtil.getClientColor() : this.firstCustomColor();
            this.sourceParticles.add(new WorldParticle(position.field_1352 + random.nextDouble(-0.08, 0.08), position.field_1351 + random.nextDouble(-0.08, 0.08), position.field_1350 + random.nextDouble(-0.08, 0.08), new Color(color), random.nextFloat() * 360.0f, random.nextFloat() * 20.0f + 5.0f, 0.0f, selectedMode, selectedPhysics, null, this.sourceLifeTime.getValue(), this.regularScale.getValue(), this.sourceSpeed.getValue(), this));
        }
    }

    private int firstCustomColor() {
        int[] colors = this.getCustomColors();
        return colors != null && colors.length > 0 ? colors[0] : -1;
    }

    private void spawnFirefly() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        float radius = this.spawnRadius.getValue();
        float height = this.spawnHeight.getValue();
        double x = AmbientParticles.mc.field_1724.method_23317() + (random.nextDouble() * 2.0 - 1.0) * (double)radius;
        double y = AmbientParticles.mc.field_1724.method_23318() + 2.0 + random.nextDouble() * (double)(height - 2.0f);
        double z = AmbientParticles.mc.field_1724.method_23321() + (random.nextDouble() * 2.0 - 1.0) * (double)radius;
        int[] customColorsArray = null;
        int colorInt = this.colorMode.isSelected("Sync") ? ColorUtil.getClientColor() : ((customColorsArray = this.getCustomColors()) != null && customColorsArray.length > 0 ? customColorsArray[0] : -256);
        FireFlyParticle firefly = new FireFlyParticle(x, y, z, new Color(colorInt), customColorsArray, this.fireflyScale.getValue(), (int)this.fireflyTrailLength.getValue());
        this.fireflyParticles.add(firefly);
    }

    private void spawnGeometric(long currentTime) {
        int[] colors;
        String selectedMode = this.getRandomGeometricMode();
        String selectedPhysics = this.getRandomPhysics();
        boolean emerge = selectedPhysics.equals("Emerge");
        ThreadLocalRandom random = ThreadLocalRandom.current();
        float radius = this.spawnRadius.getValue();
        float height = this.spawnHeight.getValue();
        double x = AmbientParticles.mc.field_1724.method_23317() + (random.nextDouble() * 2.0 - 1.0) * (double)radius;
        double y = emerge ? AmbientParticles.mc.field_1724.method_23318() - 1.0 - random.nextDouble() * 2.0 : AmbientParticles.mc.field_1724.method_23318() + 2.0 + random.nextDouble() * (double)(height - 2.0f);
        double z = AmbientParticles.mc.field_1724.method_23321() + (random.nextDouble() * 2.0 - 1.0) * (double)radius;
        int colorInt = this.colorMode.isSelected("Sync") ? ColorUtil.getClientColor() : ((colors = this.getCustomColors()) != null && colors.length > 0 ? colors[0] : -1);
        WorldParticle particle = new WorldParticle(x, y, z, new Color(colorInt), (float)(random.nextDouble() * 180.0), (float)(random.nextDouble() * 2.0 + 1.2), 0.0f, selectedMode, selectedPhysics, null, 0.0f, this.geometricsScale.getValue(), 0.0f, this);
        this.geometricsParticles.add(particle);
    }

    private void spawnParticle(long currentTime) {
        int[] colors;
        String selectedMode = this.getRandomMode();
        if (selectedMode == null) {
            return;
        }
        String selectedPhysics = selectedMode.equals("Network") ? "Fly" : this.getRandomPhysics();
        boolean drop = selectedPhysics.equals("Fall");
        boolean emerge = selectedPhysics.equals("Emerge");
        ThreadLocalRandom random = ThreadLocalRandom.current();
        float radius = this.spawnRadius.getValue();
        float height = this.spawnHeight.getValue();
        double x = AmbientParticles.mc.field_1724.method_23317() + (random.nextDouble() * 2.0 - 1.0) * (double)radius;
        double y = emerge ? AmbientParticles.mc.field_1724.method_23318() - 1.0 - random.nextDouble() * 2.0 : AmbientParticles.mc.field_1724.method_23318() + 2.0 + random.nextDouble() * (double)(height - 2.0f);
        double z = AmbientParticles.mc.field_1724.method_23321() + (random.nextDouble() * 2.0 - 1.0) * (double)radius;
        int colorInt = this.colorMode.isSelected("Sync") ? ColorUtil.getClientColor() : ((colors = this.getCustomColors()) != null && colors.length > 0 ? colors[0] : -1);
        WorldParticle particle = new WorldParticle(x, y, z, new Color(colorInt), (float)(random.nextDouble() * 180.0), (float)(random.nextDouble() * 5.0 + 5.0), 0.0f, selectedMode, selectedPhysics, null, 0.0f, this.regularScale.getValue(), 0.0f, this);
        this.particles.add(particle);
        if (selectedMode.equals("Network")) {
            if (this.spatialGrid == null) {
                this.spatialGrid = new SpatialGrid3D(this.linkDistance.getValue());
            }
            this.networkParticles.add(particle);
            this.spatialGrid.insert(particle, x, y, z);
        }
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        class_4587 stack = event.getStack();
        if (AmbientParticles.mc.field_1724 != null && AmbientParticles.mc.field_1687 != null) {
            long currentTime = System.currentTimeMillis();
            if (this.mode.isSelected("Firefly") && !this.fireflyParticles.isEmpty()) {
                for (FireFlyParticle fireflyParticle : this.fireflyParticles) {
                    if (!fireflyParticle.isVisible()) continue;
                    fireflyParticle.render(stack);
                }
            }
            for (WorldParticle particle : this.particles) {
                if (!particle.isVisible()) continue;
                particle.render(stack, currentTime);
            }
            for (WorldParticle particle : this.geometricsParticles) {
                if (!particle.isVisible()) continue;
                particle.render(stack, currentTime);
            }
            for (WorldParticle particle : this.sourceParticles) {
                particle.render(stack, currentTime);
            }
            if (this.mode.isSelected("Network") && this.spatialGrid != null && !this.networkParticles.isEmpty()) {
                this.renderNetworkLinks(stack, currentTime);
            }
        }
    }

    private boolean isParticleVisible(WorldParticle particle) {
        float radius = this.spawnRadius.getValue();
        return AmbientParticles.mc.field_1724 != null && AmbientParticles.mc.field_1724.method_5649(particle.getX(), particle.getY(), particle.getZ()) <= (double)(radius * radius * 2.25f);
    }

    private boolean isFireflyVisible(FireFlyParticle firefly) {
        float radius = this.spawnRadius.getValue();
        return AmbientParticles.mc.field_1724 != null && AmbientParticles.mc.field_1724.method_5649(firefly.getPosX(), firefly.getPosY(), firefly.getPosZ()) <= (double)(radius * radius * 2.25f);
    }

    private void renderNetworkLinks(class_4587 matrices, long currentTime) {
        if (this.networkParticles.size() < 2 || this.spatialGrid == null) {
            return;
        }
        float maxLinkDistance = this.linkDistance.getValue();
        int maxLinks = (int)this.maxLinksPerParticle.getValue();
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.defaultBlendFunc();
        LegacyRenderSystem.disableDepthTest();
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        Matrix4f matrix = matrices.method_23760().method_23761();
        class_287 bufferBuilder = class_289.method_1348().method_60827(VertexFormat.class_5596.field_29344, class_290.field_1576);
        LegacyRenderSystem.enableDepthTest();
        int lineCount = 0;
        boolean syncColors = this.colorMode.isSelected("Sync");
        int syncColor = syncColors ? ColorUtil.getClientColor() : 0;
        int[] networkColors = syncColors ? null : this.getCustomColors();
        boolean vertexColors = this.colorAnimation.isSelected("Vertex");
        int estimatedPairs = Math.min(this.networkParticles.size() * maxLinks / 2, 1000);
        HashSet<ParticlePair> processedPairs = new HashSet<ParticlePair>(estimatedPairs);
        double cameraX = AmbientParticles.mc.field_1773.method_19418().method_71156().field_1352;
        double cameraY = AmbientParticles.mc.field_1773.method_19418().method_71156().field_1351;
        double cameraZ = AmbientParticles.mc.field_1773.method_19418().method_71156().field_1350;
        block0: for (WorldParticle p1 : this.networkParticles) {
            double posZ1;
            double posY1;
            if (!p1.isVisible()) continue;
            double posX1 = MathUtil.interpolate(p1.getPx(), p1.getX());
            double camDistSq = (posX1 - cameraX) * (posX1 - cameraX) + ((posY1 = MathUtil.interpolate(p1.getPy(), p1.getY()) + 0.1) - cameraY) * (posY1 - cameraY) + ((posZ1 = MathUtil.interpolate(p1.getPz(), p1.getZ())) - cameraZ) * (posZ1 - cameraZ);
            int effectiveMaxLinks = camDistSq < 100.0 ? maxLinks : (camDistSq < 400.0 ? Math.max(1, maxLinks / 2) : Math.max(1, maxLinks / 4));
            List<SpatialGrid3D.GridEntry<WorldParticle>> nearbyEntries = this.spatialGrid.queryRadiusWithPositions(posX1, posY1, posZ1, maxLinkDistance);
            if (nearbyEntries.size() > effectiveMaxLinks) {
                nearbyEntries.sort((e1, e2) -> {
                    double dist1Sq = (e1.getX() - posX1) * (e1.getX() - posX1) + (e1.getY() - posY1) * (e1.getY() - posY1) + (e1.getZ() - posZ1) * (e1.getZ() - posZ1);
                    double dist2Sq = (e2.getX() - posX1) * (e2.getX() - posX1) + (e2.getY() - posY1) * (e2.getY() - posY1) + (e2.getZ() - posZ1) * (e2.getZ() - posZ1);
                    return Double.compare(dist1Sq, dist2Sq);
                });
                nearbyEntries = nearbyEntries.subList(0, effectiveMaxLinks);
            }
            int linksDrawn = 0;
            for (SpatialGrid3D.GridEntry<WorldParticle> entry : nearbyEntries) {
                int color2;
                int color1;
                double posZ2;
                double dz;
                double posY2;
                double dy;
                ParticlePair pair;
                if (linksDrawn >= effectiveMaxLinks) continue block0;
                WorldParticle p2 = entry.getObject();
                if (p1 == p2 || !p2.isVisible() || processedPairs.contains(pair = new ParticlePair(p1, p2))) continue;
                processedPairs.add(pair);
                double posX2 = MathUtil.interpolate(p2.getPx(), p2.getX());
                double dx = posX2 - posX1;
                double dist = Math.sqrt(dx * dx + (dy = (posY2 = MathUtil.interpolate(p2.getPy(), p2.getY()) + 0.1) - posY1) * dy + (dz = (posZ2 = MathUtil.interpolate(p2.getPz(), p2.getZ())) - posZ1) * dz);
                if (!(dist < (double)maxLinkDistance) || !(dist > 0.01)) continue;
                float lineAlpha = (float)(1.0 - dist / (double)maxLinkDistance);
                float particleAlpha1 = p1.getAlpha();
                float particleAlpha2 = p2.getAlpha();
                float alpha = Math.min(lineAlpha, Math.min(particleAlpha1, particleAlpha2));
                if (camDistSq > 100.0) {
                    float distFade = Math.max(0.1f, 1.0f - (float)(camDistSq - 100.0) / 300.0f);
                    alpha *= distFade;
                }
                if (syncColors) {
                    color2 = color1 = ColorUtil.multAlpha(syncColor, alpha);
                } else if (networkColors != null && networkColors.length > 0) {
                    if (vertexColors) {
                        int offset1 = System.identityHashCode(p1) % 360;
                        int offset2 = System.identityHashCode(p2) % 360;
                        color1 = ParticleColorUtil.getVertexGradientColor(offset1, networkColors, alpha);
                        color2 = ParticleColorUtil.getVertexGradientColor(offset2, networkColors, alpha);
                    } else {
                        color1 = ParticleColorUtil.getWaveColor(networkColors, alpha, p1.getColorOffset());
                        color2 = ParticleColorUtil.getWaveColor(networkColors, alpha, p2.getColorOffset());
                    }
                } else {
                    color2 = color1 = ColorUtil.multAlpha(-1, alpha);
                }
                bufferBuilder.method_22918((Matrix4fc)matrix, (float)(posX1 - cameraX), (float)(posY1 - cameraY), (float)(posZ1 - cameraZ)).method_22915((float)(color1 >> 16 & 0xFF) / 255.0f, (float)(color1 >> 8 & 0xFF) / 255.0f, (float)(color1 & 0xFF) / 255.0f, (float)(color1 >> 24 & 0xFF) / 255.0f);
                bufferBuilder.method_22918((Matrix4fc)matrix, (float)(posX2 - cameraX), (float)(posY2 - cameraY), (float)(posZ2 - cameraZ)).method_22915((float)(color2 >> 16 & 0xFF) / 255.0f, (float)(color2 >> 8 & 0xFF) / 255.0f, (float)(color2 & 0xFF) / 255.0f, (float)(color2 >> 24 & 0xFF) / 255.0f);
                ++lineCount;
                ++linksDrawn;
            }
        }
        if (lineCount > 0) {
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.method_60800());
        }
        LegacyRenderSystem.enableDepthTest();
        LegacyRenderSystem.disableBlend();
        LegacyRenderSystem.disableDepthTest();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.particles.clear();
        this.networkParticles.clear();
        this.geometricsParticles.clear();
        this.fireflyParticles.clear();
        this.sourceParticles.clear();
        this.projectileSpawnTimes.clear();
        this.lastWalkX = Double.NaN;
        this.lastWalkZ = Double.NaN;
        if (this.spatialGrid != null) {
            this.spatialGrid = null;
        }
    }

    public SelectSetting getCustomColorsCount() {
        return this.customColorsCount;
    }

    public MultiSelectSetting getMode() {
        return this.mode;
    }

    public MultiSelectSetting getSources() {
        return this.sources;
    }

    public MultiSelectSetting getPhysics() {
        return this.physics;
    }

    public ValueSetting getRegularCount() {
        return this.regularCount;
    }

    public ValueSetting getRegularScale() {
        return this.regularScale;
    }

    public ValueSetting getFireflyCount() {
        return this.fireflyCount;
    }

    public ValueSetting getFireflyScale() {
        return this.fireflyScale;
    }

    public ValueSetting getFireflyTrailLength() {
        return this.fireflyTrailLength;
    }

    public ValueSetting getGeometricsCount() {
        return this.geometricsCount;
    }

    public ValueSetting getGeometricsScale() {
        return this.geometricsScale;
    }

    public ValueSetting getLinkDistance() {
        return this.linkDistance;
    }

    public ValueSetting getMaxLinksPerParticle() {
        return this.maxLinksPerParticle;
    }

    public ValueSetting getSpawnRadius() {
        return this.spawnRadius;
    }

    public ValueSetting getSpawnHeight() {
        return this.spawnHeight;
    }

    public ValueSetting getSourceAmount() {
        return this.sourceAmount;
    }

    public ValueSetting getSourceLifeTime() {
        return this.sourceLifeTime;
    }

    public ValueSetting getSourceSpeed() {
        return this.sourceSpeed;
    }

    public List<WorldParticle> getParticles() {
        return this.particles;
    }

    public List<WorldParticle> getNetworkParticles() {
        return this.networkParticles;
    }

    public List<WorldParticle> getGeometricsParticles() {
        return this.geometricsParticles;
    }

    public List<FireFlyParticle> getFireflyParticles() {
        return this.fireflyParticles;
    }

    public List<WorldParticle> getSourceParticles() {
        return this.sourceParticles;
    }

    public Map<Integer, Long> getProjectileSpawnTimes() {
        return this.projectileSpawnTimes;
    }

    public BlockCollisionGrid getCollisionGrid() {
        return this.collisionGrid;
    }

    public SpatialGrid3D<WorldParticle> getSpatialGrid() {
        return this.spatialGrid;
    }

    public long getLastTickUpdate() {
        return this.lastTickUpdate;
    }

    public long getLastWalkSpawn() {
        return this.lastWalkSpawn;
    }

    public double getLastWalkX() {
        return this.lastWalkX;
    }

    public double getLastWalkZ() {
        return this.lastWalkZ;
    }

    private record ParticlePair(WorldParticle p1, WorldParticle p2) {
        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        @Override
        public boolean equals(Object obj) {
            WorldParticle other1;
            WorldParticle worldParticle;
            if (!(obj instanceof ParticlePair)) return false;
            ParticlePair particlePair = (ParticlePair)obj;
            try {
                other1 = worldParticle = particlePair.p1();
            }
            catch (Throwable throwable) {
                throw new MatchException(throwable.toString(), throwable);
            }
            WorldParticle other2 = worldParticle = particlePair.p2();
            if (this.p1 == other1) {
                if (this.p2 == other2) return true;
            }
            if (this.p1 != other2) return false;
            if (this.p2 != other1) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this.p1) + System.identityHashCode(this.p2);
        }
    }
}

