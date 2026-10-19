/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.common.util.particle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import ru.recoin.common.util.particle.AbstractParticle;
import ru.recoin.common.util.spatial.BlockCollisionGrid;

public class ParticleUpdateExecutor {
    private static final int THREAD_COUNT = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
    private static final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT, r -> {
        Thread thread2 = new Thread(r);
        thread2.setName("ParticleUpdate-" + thread2.getId());
        thread2.setDaemon(true);
        return thread2;
    });

    public static <T extends AbstractParticle> List<T> updateParticles(List<T> particles, long currentTime, Predicate<T> isVisible) {
        return ParticleUpdateExecutor.updateParticles(particles, currentTime, isVisible, null);
    }

    public static <T extends AbstractParticle> List<T> updateParticles(List<T> particles, long currentTime, Predicate<T> isVisible, BlockCollisionGrid collisionGrid) {
        if (particles.isEmpty()) {
            return new ArrayList<T>();
        }
        if (particles.size() < 1000) {
            ArrayList<T> toRemove = new ArrayList<T>();
            for (int i = 0; i < particles.size(); ++i) {
                T particle = particles.get(i);
                if (!particle.update(currentTime, isVisible.test(particle), collisionGrid)) continue;
                toRemove.add(particle);
            }
            return toRemove;
        }
        int chunkSize = Math.max(1, particles.size() / THREAD_COUNT);
        ArrayList<Future<List<T>>> futures = new ArrayList<Future<List<T>>>();
        for (int i = 0; i < particles.size(); i += chunkSize) {
            int start = i;
            int n = Math.min(i + chunkSize, particles.size());
            futures.add(executor.submit(() -> {
                ArrayList<T> toRemove = new ArrayList<T>();
                for (int j = start; j < n; ++j) {
                    T particle = particles.get(j);
                    if (!particle.update(currentTime, isVisible.test(particle), collisionGrid)) continue;
                    toRemove.add(particle);
                }
                return toRemove;
            }));
        }
        ArrayList<T> toRemove = new ArrayList<T>();
        for (Future<List<T>> future : futures) {
            try {
                toRemove.addAll(future.get());
            }
            catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return toRemove;
    }

    public static void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1L, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
        catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}

