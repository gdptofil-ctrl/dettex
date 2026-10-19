/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.common.util.spatial;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SpatialGrid3D<T> {
    private final float cellSize;
    private final Map<GridKey, List<GridEntry<T>>> grid;

    public SpatialGrid3D(float cellSize) {
        this.cellSize = cellSize;
        this.grid = new ConcurrentHashMap<GridKey, List<GridEntry<T>>>();
    }

    public void insert(T object, double x, double y, double z) {
        GridKey key = this.getGridKey(x, y, z);
        this.grid.computeIfAbsent(key, k -> new ArrayList()).add(new GridEntry<T>(object, x, y, z));
    }

    public void update(T object, double oldX, double oldY, double oldZ, double newX, double newY, double newZ) {
        GridKey newKey;
        GridKey oldKey = this.getGridKey(oldX, oldY, oldZ);
        if (oldKey.equals(newKey = this.getGridKey(newX, newY, newZ))) {
            List<GridEntry<T>> entries = this.grid.get(oldKey);
            if (entries != null) {
                for (GridEntry<T> entry : entries) {
                    if (!entry.object.equals(object)) continue;
                    entry.x = newX;
                    entry.y = newY;
                    entry.z = newZ;
                    return;
                }
            }
        } else {
            this.remove(object, oldX, oldY, oldZ);
            this.insert(object, newX, newY, newZ);
        }
    }

    public void remove(T object, double x, double y, double z) {
        GridKey key = this.getGridKey(x, y, z);
        List<GridEntry<T>> entries = this.grid.get(key);
        if (entries != null) {
            entries.removeIf(entry -> entry.object.equals(object));
            if (entries.isEmpty()) {
                this.grid.remove(key);
            }
        }
    }

    public List<T> queryRadius(double x, double y, double z, float radius) {
        ArrayList results = new ArrayList();
        double radiusSquared = radius * radius;
        int minCellX = (int)Math.floor((x - (double)radius) / (double)this.cellSize);
        int maxCellX = (int)Math.floor((x + (double)radius) / (double)this.cellSize);
        int minCellY = (int)Math.floor((y - (double)radius) / (double)this.cellSize);
        int maxCellY = (int)Math.floor((y + (double)radius) / (double)this.cellSize);
        int minCellZ = (int)Math.floor((z - (double)radius) / (double)this.cellSize);
        int maxCellZ = (int)Math.floor((z + (double)radius) / (double)this.cellSize);
        for (int cellX = minCellX; cellX <= maxCellX; ++cellX) {
            for (int cellY = minCellY; cellY <= maxCellY; ++cellY) {
                for (int cellZ = minCellZ; cellZ <= maxCellZ; ++cellZ) {
                    GridKey key = new GridKey(cellX, cellY, cellZ);
                    List<GridEntry<T>> entries = this.grid.get(key);
                    if (entries == null) continue;
                    for (GridEntry<T> entry : entries) {
                        double dx = entry.x - x;
                        double dy = entry.y - y;
                        double dz = entry.z - z;
                        double distSquared = dx * dx + dy * dy + dz * dz;
                        if (!(distSquared <= radiusSquared)) continue;
                        results.add(entry.object);
                    }
                }
            }
        }
        return results;
    }

    public List<GridEntry<T>> queryRadiusWithPositions(double x, double y, double z, float radius) {
        ArrayList<GridEntry<T>> results = new ArrayList<GridEntry<T>>();
        double radiusSquared = radius * radius;
        int minCellX = (int)Math.floor((x - (double)radius) / (double)this.cellSize);
        int maxCellX = (int)Math.floor((x + (double)radius) / (double)this.cellSize);
        int minCellY = (int)Math.floor((y - (double)radius) / (double)this.cellSize);
        int maxCellY = (int)Math.floor((y + (double)radius) / (double)this.cellSize);
        int minCellZ = (int)Math.floor((z - (double)radius) / (double)this.cellSize);
        int maxCellZ = (int)Math.floor((z + (double)radius) / (double)this.cellSize);
        for (int cellX = minCellX; cellX <= maxCellX; ++cellX) {
            for (int cellY = minCellY; cellY <= maxCellY; ++cellY) {
                for (int cellZ = minCellZ; cellZ <= maxCellZ; ++cellZ) {
                    GridKey key = new GridKey(cellX, cellY, cellZ);
                    List<GridEntry<T>> entries = this.grid.get(key);
                    if (entries == null) continue;
                    for (GridEntry<T> entry : entries) {
                        double dx = entry.x - x;
                        double dy = entry.y - y;
                        double dz = entry.z - z;
                        double distSquared = dx * dx + dy * dy + dz * dz;
                        if (!(distSquared <= radiusSquared)) continue;
                        results.add(entry);
                    }
                }
            }
        }
        return results;
    }

    public void clear() {
        this.grid.clear();
    }

    public int size() {
        return this.grid.values().stream().mapToInt(List::size).sum();
    }

    private GridKey getGridKey(double x, double y, double z) {
        return new GridKey((int)Math.floor(x / (double)this.cellSize), (int)Math.floor(y / (double)this.cellSize), (int)Math.floor(z / (double)this.cellSize));
    }

    private record GridKey(int x, int y, int z) {
    }

    public static class GridEntry<T> {
        public final T object;
        public double x;
        public double y;
        public double z;

        public GridEntry(T object, double x, double y, double z) {
            this.object = object;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public double getX() {
            return this.x;
        }

        public double getY() {
            return this.y;
        }

        public double getZ() {
            return this.z;
        }

        public T getObject() {
            return this.object;
        }
    }
}

