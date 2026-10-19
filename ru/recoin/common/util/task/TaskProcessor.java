/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.common.util.task;

import java.util.PriorityQueue;
import ru.recoin.api.feature.module.Module;

public class TaskProcessor<T> {
    public int tickCounter = 0;
    public PriorityQueue<Task<T>> activeTasks = new PriorityQueue<>((r1, r2) -> Integer.compare(r2.priority, r1.priority));

    public void tick(int deltaTime) {
        this.tickCounter += deltaTime;
    }

    public void addTask(Task<T> task) {
        this.activeTasks.removeIf(r -> r.provider.equals(task.provider));
        task.expiresIn += this.tickCounter;
        this.activeTasks.add(task);
    }

    public void removeProvider(Module provider) {
        if (provider != null) {
            this.activeTasks.removeIf(task -> task.provider.equals(provider));
        }
    }

    public T fetchActiveTaskValue() {
        while (!(this.activeTasks.isEmpty() || this.activeTasks.peek() == null || this.activeTasks.peek().expiresIn > this.tickCounter && this.activeTasks.peek().provider.isState())) {
            this.activeTasks.poll();
        }
        if (this.activeTasks.isEmpty()) {
            return null;
        }
        if (this.activeTasks.peek() != null) {
            return this.activeTasks.peek().value;
        }
        return null;
    }

    public Module fetchActiveTaskProvider() {
        this.fetchActiveTaskValue();
        return this.activeTasks.isEmpty() ? null : this.activeTasks.peek().provider;
    }

    public static class Task<T> {
        private int expiresIn;
        private final int priority;
        private final Module provider;
        private final T value;

        public String toString() {
            return "TaskProcessor.Task(expiresIn=" + this.expiresIn + ", priority=" + this.priority + ", provider=" + String.valueOf(this.provider) + ", value=" + String.valueOf(this.value) + ")";
        }

        public Task(int expiresIn, int priority, Module provider, T value) {
            this.expiresIn = expiresIn;
            this.priority = priority;
            this.provider = provider;
            this.value = value;
        }
    }
}

