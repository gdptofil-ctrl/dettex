/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.common.util.other;

import java.util.ArrayDeque;
import java.util.Queue;
import ru.recoin.common.trait.Producer;

public class Pool<T> {
    private final Queue<T> items = new ArrayDeque<T>();
    private final Producer<T> producer;

    public Pool(Producer<T> producer) {
        this.producer = producer;
    }

    public synchronized T get() {
        if (!this.items.isEmpty()) {
            return this.items.poll();
        }
        return this.producer.create();
    }

    public synchronized void free(T obj) {
        this.items.offer(obj);
    }
}

