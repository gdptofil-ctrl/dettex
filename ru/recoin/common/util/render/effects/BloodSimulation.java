/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 */
package ru.recoin.common.util.render.effects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SplittableRandom;
import java.util.random.RandomGenerator;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;

public final class BloodSimulation {
    public static final int MAX_DROPS = 900;
    public static final int MAX_STAINS = 480;
    public static final int STAIN_SEGMENTS = 24;
    private static final double GRAVITY = 0.038;
    private static final double FILM_THICKNESS = 0.005;
    private final RandomGenerator random;
    final List<Drop> drops = new ArrayList<Drop>();
    final List<Stain> stains = new ArrayList<Stain>();

    public BloodSimulation() {
        this(new SplittableRandom());
    }

    public BloodSimulation(RandomGenerator random) {
        this.random = random;
    }

    public void burst(class_243 center, float width, float height, int count, float power, float size, int color, int lifetime, class_243 impulse, Environment environment) {
        int amount = class_3532.method_15340((int)count, (int)1, (int)250);
        float force = class_3532.method_15363((float)power, (float)0.3f, (float)2.5f);
        float scale = class_3532.method_15363((float)size, (float)0.3f, (float)3.0f);
        for (int i = 0; i < amount; ++i) {
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            double radius = (0.008 + Math.pow(this.random.nextDouble(), 2.6) * 0.038) * (double)scale;
            double massDrag = class_3532.method_15350((double)(0.014 / radius), (double)0.35, (double)1.8);
            double speed = (0.09 + this.random.nextDouble() * 0.23) * (double)force / Math.sqrt(massDrag);
            class_243 origin = center.method_1031((this.random.nextDouble() - 0.5) * (double)width * 0.55, (this.random.nextDouble() - 0.5) * (double)height * 0.42, (this.random.nextDouble() - 0.5) * (double)width * 0.55);
            class_243 velocity = new class_243(Math.cos(angle) * speed, (0.07 + this.random.nextDouble() * 0.3) * (double)force, Math.sin(angle) * speed).method_1019(impulse.method_1021(0.65 + this.random.nextDouble() * 0.35));
            int variation = BloodSimulation.shade(color, 0.8f + this.random.nextFloat() * 0.32f);
            this.addDrop(new Drop(origin, velocity, radius, variation, lifetime, false, environment.light(origin)));
        }
    }

    public void tick(Environment environment, boolean wallDrips) {
        ArrayList<Drop> children = new ArrayList<Drop>();
        Iterator<Stain> stainIterator = this.stains.iterator();
        while (stainIterator.hasNext()) {
            Stain stain = stainIterator.next();
            if (++stain.age >= stain.lifetime || !environment.contains(stain.position)) {
                stainIterator.remove();
                continue;
            }
            if ((stain.age & 0xF) == 0) {
                if (!environment.supports(stain.surface)) {
                    stainIterator.remove();
                    continue;
                }
                stain.light = environment.light(stain.position.method_1019(stain.surface.normal.method_1021(0.02)));
            }
            if (!wallDrips || !(Math.abs(stain.surface.normal.field_1351) < 0.1) || !(stain.radius > 0.025)) continue;
            double bottom = stain.position.field_1351 - stain.surface.bounds.field_1322;
            stain.dripLength = Math.min(bottom, stain.dripLength + 0.005 * (double)stain.wetness());
            if (stain.dripped || !(bottom > 0.04) || !(stain.dripLength >= bottom - 0.003)) continue;
            stain.dripped = true;
            double volume = stain.volume * 0.16;
            stain.volume -= volume;
            stain.updateRadius();
            class_243 origin = stain.position.method_1031(0.0, -bottom - 0.015, 0.0).method_1019(stain.surface.normal.method_1021(0.012));
            children.add(new Drop(origin, new class_243(0.0, -0.035, 0.0), BloodSimulation.radiusForVolume(volume), stain.color, stain.lifetime, true, stain.light));
        }
        Iterator<Drop> iterator2 = this.drops.iterator();
        while (iterator2.hasNext()) {
            Drop drop = iterator2.next();
            drop.previous = drop.position;
            if (++drop.age > 120 || !environment.contains(drop.position)) {
                iterator2.remove();
                continue;
            }
            double drag = Math.exp(-0.008 * class_3532.method_15350((double)(0.018 / drop.radius), (double)0.3, (double)2.0));
            boolean impacted = false;
            for (int step = 0; step < 2; ++step) {
                drop.velocity = drop.velocity.method_1021(drag).method_1031(0.0, -0.019, 0.0);
                class_243 next = drop.position.method_1019(drop.velocity.method_1021(0.5));
                Surface surface = environment.trace(drop.position, next);
                if (surface != null) {
                    if (!surface.liquid) {
                        this.impact(drop, surface, children, environment);
                    }
                    impacted = true;
                    break;
                }
                drop.position = next;
            }
            if (impacted) {
                iterator2.remove();
                continue;
            }
            if ((drop.age & 3) != 0) continue;
            drop.light = environment.light(drop.position);
        }
        for (Drop child : children) {
            this.addDrop(child);
        }
    }

    private void impact(Drop drop, Surface surface, List<Drop> children, Environment environment) {
        double speed = Math.max(0.0, -drop.velocity.method_1026(surface.normal));
        double splashFraction = !drop.secondary && speed > 0.2 && drop.radius > 0.016 ? class_3532.method_15350((double)(speed * 0.3), (double)0.06, (double)0.24) : 0.0;
        double volume = BloodSimulation.volumeForRadius(drop.radius);
        this.addStain(surface, drop.velocity, volume * (1.0 - splashFraction), drop.color, drop.lifetime, environment.light(surface.position.method_1019(surface.normal.method_1021(0.025))));
        if (splashFraction <= 0.0) {
            return;
        }
        class_243 u = BloodSimulation.tangent(surface.normal);
        class_243 v = surface.normal.method_1036(u).method_1029();
        int count = 2 + this.random.nextInt(3);
        double childRadius = BloodSimulation.radiusForVolume(volume * splashFraction / (double)count);
        class_243 slide = drop.velocity.method_1020(surface.normal.method_1021(drop.velocity.method_1026(surface.normal)));
        for (int i = 0; i < count; ++i) {
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            double scatter = 0.025 + this.random.nextDouble() * Math.min(0.1, speed * 0.18);
            class_243 velocity = u.method_1021(Math.cos(angle) * scatter).method_1019(v.method_1021(Math.sin(angle) * scatter)).method_1019(surface.normal.method_1021(0.035 + this.random.nextDouble() * 0.065)).method_1019(slide.method_1021(0.15));
            class_243 origin = surface.position.method_1019(surface.normal.method_1021(childRadius + 0.003));
            children.add(new Drop(origin, velocity, childRadius, drop.color, drop.lifetime, true, drop.light));
        }
    }

    private void addStain(Surface surface, class_243 velocity, double volume, int color, int lifetime, float light) {
        if (volume <= 0.0) {
            return;
        }
        double radius = BloodSimulation.radiusForFilm(volume);
        for (int i = this.stains.size() - 1; i >= Math.max(0, this.stains.size() - 80); --i) {
            Stain existing = this.stains.get(i);
            if (existing.surface.key != surface.key || !existing.surface.bounds.equals((Object)surface.bounds) || existing.surface.normal.method_1026(surface.normal) < 0.99 || Math.abs(existing.position.method_1020(surface.position).method_1026(surface.normal)) > 0.008 || existing.position.method_1025(surface.position) > Math.pow((existing.radius + radius) * 0.55, 2.0)) continue;
            double combined = existing.volume + volume;
            double fraction = volume / combined;
            existing.position = surface.clip(existing.position.method_35590(surface.position, fraction));
            existing.color = BloodSimulation.mix(existing.color, color, (float)fraction);
            existing.age = (int)((double)existing.age * (1.0 - fraction));
            existing.lifetime = Math.max(existing.lifetime, lifetime);
            existing.volume = combined;
            existing.light = Math.max(existing.light, light);
            existing.updateRadius();
            return;
        }
        class_243 planar = velocity.method_1020(surface.normal.method_1021(velocity.method_1026(surface.normal)));
        class_243 u = planar.method_1027() > 4.0E-4 ? planar.method_1029() : BloodSimulation.tangent(surface.normal);
        class_243 v = surface.normal.method_1036(u).method_1029();
        double stretch = 1.0 + Math.min(0.85, planar.method_1033() * 1.7);
        this.stains.add(new Stain(surface, u, v, volume, stretch, color, lifetime, light, this.random.nextLong()));
        if (this.stains.size() > 480) {
            this.stains.removeFirst();
        }
    }

    private void addDrop(Drop drop) {
        this.drops.add(drop);
        if (this.drops.size() > 900) {
            this.drops.removeFirst();
        }
    }

    public void clear() {
        this.drops.clear();
        this.stains.clear();
    }

    public boolean isEmpty() {
        return this.drops.isEmpty() && this.stains.isEmpty();
    }

    public int dropCount() {
        return this.drops.size();
    }

    public int stainCount() {
        return this.stains.size();
    }

    public double totalVolume() {
        double volume = 0.0;
        for (Drop drop : this.drops) {
            volume += BloodSimulation.volumeForRadius(drop.radius);
        }
        for (Stain stain : this.stains) {
            volume += stain.volume;
        }
        return volume;
    }

    static class_243 tangent(class_243 normal) {
        class_243 reference = Math.abs(normal.field_1351) > 0.9 ? new class_243(0.0, 0.0, 1.0) : new class_243(0.0, 1.0, 0.0);
        return normal.method_1036(reference).method_1029();
    }

    static double volumeForRadius(double radius) {
        return 4.1887902047863905 * radius * radius * radius;
    }

    static double radiusForVolume(double volume) {
        return Math.cbrt(volume * 3.0 / (Math.PI * 4));
    }

    static double radiusForFilm(double volume) {
        return class_3532.method_15350((double)Math.sqrt(volume / 0.015707963267948967), (double)0.008, (double)0.48);
    }

    static float smooth(float value) {
        float t = class_3532.method_15363((float)value, (float)0.0f, (float)1.0f);
        return t * t * (3.0f - 2.0f * t);
    }

    static float noise(long seed, int salt) {
        long value = seed + -7046029254386353131L * ((long)salt + 1L);
        value = (value ^ value >>> 30) * -4658895280553007687L;
        value = (value ^ value >>> 27) * -7723592293110705685L;
        return (float)((value ^ value >>> 31) >>> 40) / 1.6777215E7f;
    }

    static int shade(int color, float factor) {
        int r = class_3532.method_15340((int)Math.round((float)(color >>> 16 & 0xFF) * factor), (int)0, (int)255);
        int g = class_3532.method_15340((int)Math.round((float)(color >>> 8 & 0xFF) * factor), (int)0, (int)255);
        int b = class_3532.method_15340((int)Math.round((float)(color & 0xFF) * factor), (int)0, (int)255);
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    static int mix(int first, int second, float fraction) {
        int r = Math.round(class_3532.method_48781((float)fraction, (int)(first >>> 16 & 0xFF), (int)(second >>> 16 & 0xFF)));
        int g = Math.round(class_3532.method_48781((float)fraction, (int)(first >>> 8 & 0xFF), (int)(second >>> 8 & 0xFF)));
        int b = Math.round(class_3532.method_48781((float)fraction, (int)(first & 0xFF), (int)(second & 0xFF)));
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    static final class Drop {
        class_243 position;
        class_243 previous;
        class_243 velocity;
        final double radius;
        final int color;
        final int lifetime;
        final boolean secondary;
        float light;
        int age;

        Drop(class_243 position, class_243 velocity, double radius, int color, int lifetime, boolean secondary, float light) {
            this.position = position;
            this.previous = position;
            this.velocity = velocity;
            this.radius = radius;
            this.color = color;
            this.lifetime = Math.max(20, lifetime);
            this.secondary = secondary;
            this.light = light;
        }
    }

    public static interface Environment {
        public Surface trace(class_243 var1, class_243 var2);

        public float light(class_243 var1);

        public boolean contains(class_243 var1);

        default public boolean supports(Surface surface) {
            return true;
        }
    }

    static final class Stain {
        final Surface surface;
        final class_243 u;
        final class_243 v;
        final double stretch;
        final float[] edge = new float[24];
        class_243 position;
        double volume;
        double radius;
        double dripLength;
        int color;
        int age;
        int lifetime;
        float light;
        boolean dripped;

        Stain(Surface surface, class_243 u, class_243 v, double volume, double stretch, int color, int lifetime, float light, long seed) {
            this.surface = surface;
            this.position = surface.position;
            this.u = u;
            this.v = v;
            this.volume = volume;
            this.stretch = stretch;
            this.color = color;
            this.lifetime = Math.max(20, lifetime);
            this.light = light;
            this.updateRadius();
            double phase = (double)BloodSimulation.noise(seed, 40) * Math.PI * 2.0;
            for (int i = 0; i < this.edge.length; ++i) {
                double angle = (double)i * Math.PI * 2.0 / (double)this.edge.length;
                this.edge[i] = (float)(0.87 + 0.12 * Math.sin(angle * 3.0 + phase) + 0.08 * Math.sin(angle * 5.0 - phase) + (double)BloodSimulation.noise(seed, i) * 0.16);
            }
        }

        void updateRadius() {
            this.radius = BloodSimulation.radiusForFilm(this.volume);
        }

        float wetness() {
            return 1.0f - BloodSimulation.smooth((float)this.age / ((float)this.lifetime * 0.65f));
        }
    }

    public record Surface(class_243 position, class_243 normal, class_238 bounds, long key, boolean liquid) {
        public class_243 clip(class_243 point) {
            return new class_243(class_3532.method_15350((double)point.field_1352, (double)this.bounds.field_1323, (double)this.bounds.field_1320), class_3532.method_15350((double)point.field_1351, (double)this.bounds.field_1322, (double)this.bounds.field_1325), class_3532.method_15350((double)point.field_1350, (double)this.bounds.field_1321, (double)this.bounds.field_1324));
        }
    }
}

