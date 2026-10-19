/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.common.util.warden;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

public final class DungeonPatrol {
    private final List<Sector> sectors = new ArrayList<Sector>();
    private final BitSet scanned = new BitSet();
    private final BitSet visited = new BitSet();
    private final int minChunkX;
    private final int maxChunkX;
    private final int minChunkZ;
    private final int maxChunkZ;
    private final long[] retryAfter;
    private final int[] attempts;
    private int cursor;
    private int active = -1;
    private int completedPasses;
    private long lastVisitAt;

    public DungeonPatrol(int minX, int maxX, int minZ, int maxZ) {
        if (minX > maxX || minZ > maxZ) {
            throw new IllegalArgumentException("Invalid patrol bounds");
        }
        this.minChunkX = minX >> 4;
        this.maxChunkX = maxX >> 4;
        this.minChunkZ = minZ >> 4;
        this.maxChunkZ = maxZ >> 4;
        for (int z = this.minChunkZ; z <= this.maxChunkZ; ++z) {
            boolean reverse = (z - this.minChunkZ & 1) != 0;
            for (int offset = 0; offset <= this.maxChunkX - this.minChunkX; ++offset) {
                int x = reverse ? this.maxChunkX - offset : this.minChunkX + offset;
                this.sectors.add(new Sector(x, z, Math.max(minX, x << 4), Math.min(maxX, (x << 4) + 15), Math.max(minZ, z << 4), Math.min(maxZ, (z << 4) + 15)));
            }
        }
        this.retryAfter = new long[this.sectors.size()];
        this.attempts = new int[this.sectors.size()];
    }

    public void recordScan(int chunkX, int chunkZ) {
        int index = this.indexOf(chunkX, chunkZ);
        if (index >= 0) {
            this.scanned.set(index);
        }
    }

    public boolean recordVisit(int blockX, int blockZ, long now) {
        int dz;
        int index = this.indexOf(blockX >> 4, blockZ >> 4);
        if (index < 0 || !this.scanned.get(index) || this.visited.get(index)) {
            return false;
        }
        Sector sector = this.sectors.get(index);
        if (blockX < sector.minX || blockX > sector.maxX || blockZ < sector.minZ || blockZ > sector.maxZ) {
            return false;
        }
        int dx = blockX - sector.centerX();
        if (dx * dx + (dz = blockZ - sector.centerZ()) * dz > 36) {
            return false;
        }
        this.visited.set(index);
        this.lastVisitAt = now;
        this.retryAfter[index] = 0L;
        if (this.visited.cardinality() == this.sectors.size()) {
            ++this.completedPasses;
            this.scanned.clear();
            this.visited.clear();
            Arrays.fill(this.retryAfter, 0L);
            Arrays.fill(this.attempts, 0);
            this.cursor = 0;
            this.active = -1;
        }
        return true;
    }

    public Sector next(long now) {
        if (this.active >= 0 && !this.visited.get(this.active) && this.retryAfter[this.active] <= now) {
            return this.sectors.get(this.active);
        }
        this.active = -1;
        for (int offset = 0; offset < this.sectors.size(); ++offset) {
            int index = (this.cursor + offset) % this.sectors.size();
            if (this.visited.get(index) || this.retryAfter[index] > now) continue;
            this.active = index;
            this.cursor = (index + 1) % this.sectors.size();
            return this.sectors.get(index);
        }
        return null;
    }

    public void defer(long now, long delay) {
        if (this.active < 0) {
            return;
        }
        this.retryAfter[this.active] = now + delay;
        int n = this.active;
        this.attempts[n] = this.attempts[n] + 1;
        this.active = -1;
    }

    public int attempts() {
        return this.active < 0 ? 0 : this.attempts[this.active];
    }

    public int total() {
        return this.sectors.size();
    }

    public int scannedCount() {
        return this.scanned.cardinality();
    }

    public int visitedCount() {
        return this.visited.cardinality();
    }

    public int completedPasses() {
        return this.completedPasses;
    }

    public long lastVisitAt() {
        return this.lastVisitAt;
    }

    public int deferredCount(long now) {
        int count = 0;
        for (int i = 0; i < this.retryAfter.length; ++i) {
            if (this.visited.get(i) || this.retryAfter[i] <= now) continue;
            ++count;
        }
        return count;
    }

    private int indexOf(int chunkX, int chunkZ) {
        if (chunkX < this.minChunkX || chunkX > this.maxChunkX || chunkZ < this.minChunkZ || chunkZ > this.maxChunkZ) {
            return -1;
        }
        int row = chunkZ - this.minChunkZ;
        int column = (row & 1) == 0 ? chunkX - this.minChunkX : this.maxChunkX - chunkX;
        return row * (this.maxChunkX - this.minChunkX + 1) + column;
    }

    public record Sector(int chunkX, int chunkZ, int minX, int maxX, int minZ, int maxZ) {
        public int centerX() {
            return this.minX + this.maxX >> 1;
        }

        public int centerZ() {
            return this.minZ + this.maxZ >> 1;
        }
    }
}

