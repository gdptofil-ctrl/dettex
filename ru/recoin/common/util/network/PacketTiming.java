package ru.recoin.common.util.network;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.LockSupport;
import net.minecraft.class_2596;
import net.minecraft.class_2828;
import net.minecraft.class_310;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;

/**
 * Tiny lock-free-ish delay queue for serverbound movement packets.
 *
 * <p>Only player movement packets are delayed. The queue is a fixed ring buffer, so no per packet
 * object is allocated. A daemon thread only schedules a flush on the game thread; packets are
 * always written from the game thread, so packet events stay thread safe.
 */
public final class PacketTiming {

    private static final int CAPACITY = 256;
    private static final class_2596<?>[] PACKETS = new class_2596<?>[CAPACITY];
    private static final long[] DUE = new long[CAPACITY];
    private static final Object LOCK = new Object();
    private static final Runnable FLUSH = PacketTiming::flushDue;
    private static final Runnable FLUSH_ALL = PacketTiming::flushAll;

    private static int head;
    private static int tail;
    private static int count;
    private static long lastScheduled;
    private static int sent;
    private static volatile boolean enabled;
    private static volatile boolean flushQueued;
    private static Thread worker;

    private PacketTiming() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * @return {@code true} when the caller must cancel the original send because the packet was
     *         queued, {@code false} when the packet should be written immediately.
     */
    public static boolean enqueue(class_2596<?> packet) {
        if (!enabled || !(packet instanceof class_2828)) {
            return false;
        }
        synchronized (LOCK) {
            if (count >= CAPACITY) {
                return false;
            }
            long now = System.nanoTime();
            long jitter = ThreadLocalRandom.current().nextLong(-8_000_000L, 8_000_001L);
            if (++sent % 10 == 0) {
                jitter += 15_000_000L;
            }
            long due = now + jitter;
            if (lastScheduled - now > 60_000_000L) {
                lastScheduled = now;
            }
            if (due < lastScheduled) {
                due = lastScheduled;
            }
            lastScheduled = due;
            PACKETS[tail] = packet;
            DUE[tail] = due;
            tail = (tail + 1) % CAPACITY;
            ++count;
        }
        ensureWorker();
        return true;
    }

    public static void setEnabled(boolean value) {
        if (value == enabled) {
            return;
        }
        enabled = value;
        if (value) {
            synchronized (LOCK) {
                sent = 0;
                lastScheduled = 0L;
            }
            ensureWorker();
        } else {
            synchronized (LOCK) {
                LOCK.notifyAll();
            }
            class_310 mc = class_310.method_1551();
            if (mc != null) {
                try {
                    mc.method_18859(FLUSH_ALL);
                } catch (Throwable ignored) {
                }
            }
        }
    }

    private static void ensureWorker() {
        if (worker != null && worker.isAlive()) {
            return;
        }
        synchronized (LOCK) {
            if (worker != null && worker.isAlive()) {
                return;
            }
            Thread thread = new Thread(PacketTiming::loop, "Dettex-PacketTiming");
            thread.setDaemon(true);
            thread.setPriority(Thread.MIN_PRIORITY);
            worker = thread;
            thread.start();
        }
    }

    private static void loop() {
        while (enabled) {
            long now = System.nanoTime();
            boolean due;
            synchronized (LOCK) {
                due = count > 0 && DUE[head] - now <= 0L;
            }
            if (due) {
                if (!flushQueued) {
                    flushQueued = true;
                    class_310 mc = class_310.method_1551();
                    if (mc == null) {
                        flushQueued = false;
                        LockSupport.parkNanos(1_000_000L);
                        continue;
                    }
                    try {
                        mc.method_18859(FLUSH);
                    } catch (Throwable throwable) {
                        flushQueued = false;
                        break;
                    }
                }
                LockSupport.parkNanos(250_000L);
            } else {
                synchronized (LOCK) {
                    try {
                        LOCK.wait(1L);
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        class_310 mc = class_310.method_1551();
        if (mc != null) {
            try {
                mc.method_18859(FLUSH_ALL);
            } catch (Throwable ignored) {
            }
        }
    }

    public static void flushDue() {
        while (true) {
            class_2596<?> packet;
            synchronized (LOCK) {
                long now = System.nanoTime();
                if (count == 0 || DUE[head] - now > 0L) {
                    flushQueued = false;
                    return;
                }
                packet = PACKETS[head];
                PACKETS[head] = null;
                head = (head + 1) % CAPACITY;
                --count;
            }
            if (packet != null) {
                try {
                    PlayerIntersectionUtil.sendPacketWithOutEvent(packet);
                } catch (Throwable ignored) {
                }
            }
        }
    }

    public static void flushAll() {
        while (true) {
            class_2596<?> packet;
            synchronized (LOCK) {
                if (count == 0) {
                    flushQueued = false;
                    return;
                }
                packet = PACKETS[head];
                PACKETS[head] = null;
                head = (head + 1) % CAPACITY;
                --count;
            }
            if (packet != null) {
                try {
                    PlayerIntersectionUtil.sendPacketWithOutEvent(packet);
                } catch (Throwable ignored) {
                }
            }
        }
    }
}
