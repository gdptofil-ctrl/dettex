/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1922
 *  net.minecraft.class_1937
 *  net.minecraft.class_2338
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_2680
 */
package ru.recoin.common.util.spatial;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.class_1922;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import ru.recoin.common.util.spatial.SpatialGrid3D;

public class BlockCollisionGrid {
    private final SpatialGrid3D<class_2338> grid;
    private Set<class_2338> trackedBlocks;
    private Set<class_2338> nextBlocks = new HashSet<class_2338>();
    private final float cellSize = 1.0f;
    private final float collisionRadius;
    private final float repulsionStrength;
    private double lastUpdateX = Double.NaN;
    private double lastUpdateY = Double.NaN;
    private double lastUpdateZ = Double.NaN;
    private int ticksSinceUpdate = 0;
    private static final int UPDATE_INTERVAL = 5;
    private static final double MOVEMENT_THRESHOLD = 3.0;

    public BlockCollisionGrid(float collisionRadius, float repulsionStrength) {
        this.grid = new SpatialGrid3D(1.0f);
        this.trackedBlocks = new HashSet<class_2338>();
        this.collisionRadius = collisionRadius;
        this.repulsionStrength = repulsionStrength;
    }

    public void updateBlocks(class_1937 world, double centerX, double centerY, double centerZ, int radius) {
        if (world == null) {
            return;
        }
        if (Double.isNaN(this.lastUpdateX)) {
            this.ticksSinceUpdate = 5;
        } else {
            double dz;
            double dy;
            double dx;
            double distSq;
            ++this.ticksSinceUpdate;
            if (this.ticksSinceUpdate < 5 && (distSq = (dx = centerX - this.lastUpdateX) * dx + (dy = centerY - this.lastUpdateY) * dy + (dz = centerZ - this.lastUpdateZ) * dz) < 9.0) {
                return;
            }
        }
        this.ticksSinceUpdate = 0;
        this.lastUpdateX = centerX;
        this.lastUpdateY = centerY;
        this.lastUpdateZ = centerZ;
        int optimizedRadius = Math.min(radius, 40);
        Set<class_2338> newBlocks = this.nextBlocks;
        newBlocks.clear();
        class_2338.class_2339 scanPos = new class_2338.class_2339();
        int minX = (int)Math.floor(centerX - (double)optimizedRadius);
        int maxX = (int)Math.floor(centerX + (double)optimizedRadius);
        int minY = (int)Math.floor(centerY - (double)optimizedRadius);
        int maxY = (int)Math.floor(centerY + (double)optimizedRadius);
        int minZ = (int)Math.floor(centerZ - (double)optimizedRadius);
        int maxZ = (int)Math.floor(centerZ + (double)optimizedRadius);
        minY = Math.max(-64, minY);
        maxY = Math.min(319, maxY);
        double radiusSq = optimizedRadius * optimizedRadius;
        for (int x = minX; x <= maxX; ++x) {
            double dx = (double)x + 0.5 - centerX;
            double dxSq = dx * dx;
            for (int y = minY; y <= maxY; ++y) {
                double dy = (double)y + 0.5 - centerY;
                double xySq = dxSq + dy * dy;
                if (xySq > radiusSq) continue;
                for (int z = minZ; z <= maxZ; ++z) {
                    class_2338.class_2339 pos;
                    class_2680 state;
                    double dz = (double)z + 0.5 - centerZ;
                    if (xySq + dz * dz > radiusSq || (state = world.method_8320((class_2338)(pos = scanPos.method_10103(x, y, z)))).method_26215() || state.method_26220((class_1922)world, (class_2338)pos).method_1110()) continue;
                    newBlocks.add(pos.method_10062());
                }
            }
        }
        for (class_2338 pos : this.trackedBlocks) {
            if (newBlocks.contains(pos)) continue;
            this.grid.remove(pos, (double)pos.method_10263() + 0.5, (double)pos.method_10264() + 0.5, (double)pos.method_10260() + 0.5);
        }
        for (class_2338 pos : newBlocks) {
            if (this.trackedBlocks.contains(pos)) continue;
            this.grid.insert(pos, (double)pos.method_10263() + 0.5, (double)pos.method_10264() + 0.5, (double)pos.method_10260() + 0.5);
        }
        this.nextBlocks = this.trackedBlocks;
        this.trackedBlocks = newBlocks;
        this.nextBlocks.clear();
    }

    public float[] calculateRepulsion(float particleX, float particleY, float particleZ) {
        float[] force = new float[]{0.0f, 0.0f, 0.0f};
        List<SpatialGrid3D.GridEntry<class_2338>> nearbyBlocks = this.grid.queryRadiusWithPositions(particleX, particleY, particleZ, this.collisionRadius);
        if (nearbyBlocks.isEmpty()) {
            return force;
        }
        for (SpatialGrid3D.GridEntry<class_2338> entry : nearbyBlocks) {
            float dist;
            float strength;
            float blockZ;
            float dz;
            float blockY;
            float dy;
            float blockX = (float)entry.getX();
            float dx = particleX - blockX;
            float distSq = dx * dx + (dy = particleY - (blockY = (float)entry.getY())) * dy + (dz = particleZ - (blockZ = (float)entry.getZ())) * dz;
            if (distSq < 0.01f) {
                distSq = 0.01f;
            }
            if (!((strength = this.repulsionStrength * (1.0f - (dist = (float)Math.sqrt(distSq)) / this.collisionRadius)) > 0.0f)) continue;
            float invDist = 1.0f / dist;
            force[0] = force[0] + dx * invDist * strength;
            force[1] = force[1] + dy * invDist * strength;
            force[2] = force[2] + dz * invDist * strength;
        }
        return force;
    }

    public boolean hasCollision(float x, float y, float z) {
        int blockX = (int)Math.floor(x);
        int blockY = (int)Math.floor(y);
        int blockZ = (int)Math.floor(z);
        float margin = 0.08f;
        class_2338.class_2339 pos = new class_2338.class_2339();
        for (int offsetX = -1; offsetX <= 1; ++offsetX) {
            for (int offsetY = -1; offsetY <= 1; ++offsetY) {
                for (int offsetZ = -1; offsetZ <= 1; ++offsetZ) {
                    pos.method_10103(blockX + offsetX, blockY + offsetY, blockZ + offsetZ);
                    if (!(x >= (float)pos.method_10263() - margin) || !(x <= (float)pos.method_10263() + 1.0f + margin) || !(y >= (float)pos.method_10264() - margin) || !(y <= (float)pos.method_10264() + 1.0f + margin) || !(z >= (float)pos.method_10260() - margin) || !(z <= (float)pos.method_10260() + 1.0f + margin) || !this.trackedBlocks.contains(pos)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public void clear() {
        this.grid.clear();
        this.trackedBlocks.clear();
        this.nextBlocks.clear();
    }

    public int getBlockCount() {
        return this.trackedBlocks.size();
    }
}

