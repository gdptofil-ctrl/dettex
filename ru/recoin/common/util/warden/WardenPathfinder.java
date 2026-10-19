/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.common.util.warden;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public final class WardenPathfinder {
    private static final int[][] DIRECTIONS = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private static final int[] HEIGHTS = new int[]{0, 1, -1};

    private WardenPathfinder() {
    }

    public static List<Point> find(Point start, Point goal, int radius, Terrain terrain, int maxVisited) {
        PriorityQueue<Entry> open = new PriorityQueue<Entry>();
        HashMap<Point, Double> costs = new HashMap<Point, Double>();
        HashMap<Point, Point> previous = new HashMap<Point, Point>();
        costs.put(start, 0.0);
        open.add(new Entry(start, 0.0, WardenPathfinder.estimate(start, goal)));
        Point closest = start;
        double closestEstimate = WardenPathfinder.estimate(start, goal);
        int visited = 0;
        while (!open.isEmpty() && visited < maxVisited) {
            Entry current = (Entry)open.poll();
            if (current.cost() > costs.getOrDefault(current.point(), (Double)Double.MAX_VALUE)) continue;
            ++visited;
            double remaining = WardenPathfinder.estimate(current.point(), goal);
            if (terrain.canStand(current.point())) {
                if (remaining < closestEstimate) {
                    closest = current.point();
                    closestEstimate = remaining;
                }
                if (current.point().distanceSquared(goal) <= (double)radius * (double)radius) {
                    return WardenPathfinder.reconstruct(current.point(), previous);
                }
            }
            for (int[] direction : DIRECTIONS) {
                for (int dy : HEIGHTS) {
                    Point next = current.point().offset(direction[0], dy, direction[1]);
                    if (Math.abs(next.x() - start.x()) > 96 || Math.abs(next.z() - start.z()) > 96 || Math.abs(next.y() - start.y()) > 16 || !terrain.canStand(next) || !terrain.canTraverse(current.point(), next)) continue;
                    double cost = current.cost() + (dy == 0 ? 1.0 : 1.5);
                    if (cost >= costs.getOrDefault(next, (Double)Double.MAX_VALUE)) continue;
                    costs.put(next, cost);
                    previous.put(next, current.point());
                    open.add(new Entry(next, cost, cost + WardenPathfinder.estimate(next, goal)));
                }
            }
        }
        return closest.equals(start) ? List.of() : WardenPathfinder.reconstruct(closest, previous);
    }

    private static double estimate(Point point, Point goal) {
        return (double)(Math.abs(point.x() - goal.x()) + Math.abs(point.z() - goal.z())) + (double)Math.abs(point.y() - goal.y()) * 0.5;
    }

    private static List<Point> reconstruct(Point end, Map<Point, Point> previous) {
        ArrayList<Point> path = new ArrayList<Point>();
        Point point = end;
        while (point != null) {
            path.add(point);
            point = previous.get(point);
        }
        Collections.reverse(path);
        return path;
    }

    private record Entry(Point point, double cost, double estimate) implements Comparable<Entry>
    {
        @Override
        public int compareTo(Entry other) {
            return Double.compare(this.estimate, other.estimate);
        }
    }

    public record Point(int x, int y, int z) {
        public Point offset(int dx, int dy, int dz) {
            return new Point(this.x + dx, this.y + dy, this.z + dz);
        }

        public double distanceSquared(Point other) {
            double dx = this.x - other.x;
            double dy = this.y - other.y;
            double dz = this.z - other.z;
            return dx * dx + dy * dy + dz * dz;
        }
    }

    public static interface Terrain {
        public boolean canStand(Point var1);

        public boolean canTraverse(Point var1, Point var2);
    }
}

