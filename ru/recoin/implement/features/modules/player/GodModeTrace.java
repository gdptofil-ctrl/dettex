/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.player;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.UUID;

final class GodModeTrace
implements AutoCloseable {
    static final int MAX_ENTRIES = 4096;
    private final ArrayDeque<String> entries = new ArrayDeque();
    private final BufferedWriter writer;
    private final Path path;
    private final long startedAt;
    private int dropped;
    private boolean closed;

    GodModeTrace(Path directory, long startedAt) throws IOException {
        Files.createDirectories(directory, new FileAttribute[0]);
        this.startedAt = startedAt;
        this.path = directory.resolve("recoin-godmode-" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().substring(0, 8) + ".log");
        this.writer = Files.newBufferedWriter(this.path, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        this.writer.write("RECOIN_GODMODE_TRACE v1 started=" + String.valueOf(Instant.now()));
        this.writer.newLine();
        this.writer.flush();
    }

    Path path() {
        return this.path;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void record(String event) {
        ArrayDeque<String> arrayDeque = this.entries;
        synchronized (arrayDeque) {
            if (this.closed) {
                return;
            }
            if (this.entries.size() >= 4096) {
                ++this.dropped;
                return;
            }
            long elapsedMs = (System.nanoTime() - this.startedAt) / 1000000L;
            this.entries.addLast(elapsedMs + "ms " + event);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    synchronized void flush() throws IOException {
        int lost;
        ArrayList<String> batch;
        ArrayDeque<String> arrayDeque = this.entries;
        synchronized (arrayDeque) {
            batch = new ArrayList<String>(this.entries);
            this.entries.clear();
            lost = this.dropped;
            this.dropped = 0;
        }
        for (String entry : batch) {
            this.writer.write(entry);
            this.writer.newLine();
        }
        if (lost > 0) {
            this.writer.write("DROPPED=" + lost);
            this.writer.newLine();
        }
        this.writer.flush();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized void close() throws IOException {
        ArrayDeque<String> arrayDeque = this.entries;
        synchronized (arrayDeque) {
            if (this.closed) {
                return;
            }
            this.closed = true;
        }
        try {
            this.flush();
        }
        finally {
            this.writer.close();
        }
    }
}

