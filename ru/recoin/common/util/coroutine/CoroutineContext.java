/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.common.util.coroutine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CoroutineContext {
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), r -> {
        Thread thread2 = new Thread(r, "Recoin-CoroutineContext");
        thread2.setDaemon(true);
        return thread2;
    });

    public void launch(Runnable task) {
        this.executor.submit(task);
    }

    public void shutdown() {
        this.executor.shutdown();
    }
}

