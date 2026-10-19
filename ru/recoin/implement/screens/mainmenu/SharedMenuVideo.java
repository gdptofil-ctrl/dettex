/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 */
package ru.recoin.implement.screens.mainmenu;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import net.minecraft.class_310;
import ru.recoin.implement.screens.mainmenu.LoopingVideoTexture;

final class SharedMenuVideo {
    private static final ScheduledExecutorService RELEASE_EXECUTOR = Executors.newSingleThreadScheduledExecutor(new VideoThreadFactory());
    private static LoopingVideoTexture texture;
    private static int users;
    private static long generation;

    private SharedMenuVideo() {
    }

    static synchronized LoopingVideoTexture acquire() {
        ++users;
        ++generation;
        if (texture == null) {
            texture = new LoopingVideoTexture();
            texture.start();
        }
        return texture;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void release(LoopingVideoTexture released) {
        Class<SharedMenuVideo> clazz = SharedMenuVideo.class;
        synchronized (SharedMenuVideo.class) {
            if (released == null || released != texture) {
                // ** MonitorExit[var3_1] (shouldn't be in output)
                return;
            }
            users = Math.max(0, users - 1);
            long releaseGeneration = ++generation;
            // ** MonitorExit[var3_1] (shouldn't be in output)
            RELEASE_EXECUTOR.schedule(() -> SharedMenuVideo.closeIfUnused(released, releaseGeneration), 750L, TimeUnit.MILLISECONDS);
            return;
        }
    }

    private static void closeIfUnused(LoopingVideoTexture released, long releaseGeneration) {
        class_310 client = class_310.method_1551();
        if (client == null) {
            return;
        }
        client.execute(() -> {
            Class<SharedMenuVideo> clazz = SharedMenuVideo.class;
            synchronized (SharedMenuVideo.class) {
                if (users != 0 || generation != releaseGeneration || texture != released) {
                    // ** MonitorExit[var3_2] (shouldn't be in output)
                    return;
                }
                texture = null;
                released.close();
                // ** MonitorExit[var3_2] (shouldn't be in output)
                return;
            }
        });
    }

    private static final class VideoThreadFactory
    implements ThreadFactory {
        private VideoThreadFactory() {
        }

        @Override
        public Thread newThread(Runnable task) {
            Thread thread2 = new Thread(task, "Recoin menu-video release");
            thread2.setDaemon(true);
            return thread2;
        }
    }
}

