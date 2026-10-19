/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.minecraft.class_2960
 */
package ru.recoin.api.system.discord;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.class_2960;
import ru.recoin.core.Main;
import ru.recoin.core.client.UserProfile;

public class DiscordManager {
    private static final String CLIENT_ID = "1426534625882279976";
    private static final String TELEGRAM_URL = "https://t.me/DettexClient";
    private static final Gson GSON = new Gson();
    private final DiscordDaemonThread discordDaemonThread = new DiscordDaemonThread();
    private final long startTimestamp = System.currentTimeMillis() / 1000L;
    private boolean running;
    private DiscordInfo info = new DiscordInfo("Unknown", "", "");
    private class_2960 avatarId;
    private RandomAccessFile ipc;

    public void init() {
        this.running = true;
        this.updatePresence();
        this.discordDaemonThread.start();
    }

    private void updatePresence() {
        try {
            if (this.ipc == null) {
                this.ipc = this.connectIpc();
                System.out.println("Discord RPC IPC connected");
            }
            JsonObject activity = new JsonObject();
            activity.addProperty("details", "DettexClient 1.21.11");
            activity.addProperty("state", "UID: " + UserProfile.getUidString());
            JsonObject timestamps = new JsonObject();
            timestamps.addProperty("start", (Number)this.startTimestamp);
            activity.add("timestamps", (JsonElement)timestamps);
            JsonObject assets = new JsonObject();
            assets.addProperty("large_image", "dettex");
            assets.addProperty("large_text", "DettexClient | UID: " + UserProfile.getUid());
            activity.add("assets", (JsonElement)assets);
            JsonArray buttons = new JsonArray();
            JsonObject telegramButton = new JsonObject();
            telegramButton.addProperty("label", "Telegram");
            telegramButton.addProperty("url", TELEGRAM_URL);
            buttons.add((JsonElement)telegramButton);
            activity.add("buttons", (JsonElement)buttons);
            JsonObject args2 = new JsonObject();
            args2.addProperty("pid", (Number)ProcessHandle.current().pid());
            args2.add("activity", (JsonElement)activity);
            JsonObject payload = new JsonObject();
            payload.addProperty("cmd", "SET_ACTIVITY");
            payload.add("args", (JsonElement)args2);
            payload.addProperty("nonce", UUID.randomUUID().toString());
            this.writeFrame(1, payload);
        }
        catch (Throwable throwable) {
            this.closeIpc();
            System.err.println("Discord RPC update failed: " + throwable.getClass().getSimpleName() + " - " + throwable.getMessage());
        }
    }

    public void stopRPC() {
        this.running = false;
        try {
            JsonObject payload = new JsonObject();
            this.writeFrame(2, payload);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        this.closeIpc();
    }

    public void load() throws IOException {
    }

    private RandomAccessFile connectIpc() throws IOException {
        IOException lastException = null;
        for (int i = 0; i < 10; ++i) {
            try {
                RandomAccessFile file = new RandomAccessFile("\\\\.\\pipe\\discord-ipc-" + i, "rw");
                JsonObject handshake = new JsonObject();
                handshake.addProperty("v", (Number)1);
                handshake.addProperty("client_id", CLIENT_ID);
                this.writeFrame(file, 0, handshake);
                return file;
            }
            catch (IOException exception) {
                lastException = exception;
                continue;
            }
        }
        throw lastException == null ? new IOException("Discord IPC pipe not found") : lastException;
    }

    private void writeFrame(int opcode, JsonObject payload) throws IOException {
        if (this.ipc == null) {
            throw new IOException("Discord IPC is not connected");
        }
        this.writeFrame(this.ipc, opcode, payload);
    }

    private void writeFrame(RandomAccessFile file, int opcode, JsonObject payload) throws IOException {
        byte[] data = GSON.toJson((JsonElement)payload).getBytes(StandardCharsets.UTF_8);
        ByteBuffer header = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        header.putInt(opcode);
        header.putInt(data.length);
        file.write(header.array());
        file.write(data);
    }

    private void closeIpc() {
        if (this.ipc == null) {
            return;
        }
        try {
            this.ipc.close();
        }
        catch (IOException iOException) {
            // empty catch block
        }
        this.ipc = null;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void setInfo(DiscordInfo info) {
        this.info = info;
    }

    public void setAvatarId(class_2960 avatarId) {
        this.avatarId = avatarId;
    }

    public void setIpc(RandomAccessFile ipc) {
        this.ipc = ipc;
    }

    public DiscordDaemonThread getDiscordDaemonThread() {
        return this.discordDaemonThread;
    }

    public long getStartTimestamp() {
        return this.startTimestamp;
    }

    public boolean isRunning() {
        return this.running;
    }

    public DiscordInfo getInfo() {
        return this.info;
    }

    public class_2960 getAvatarId() {
        return this.avatarId;
    }

    public RandomAccessFile getIpc() {
        return this.ipc;
    }

    private class DiscordDaemonThread
    extends Thread {
        private DiscordDaemonThread() {
        }

        @Override
        public void run() {
            this.setName("Discord-RPC");
            while (Main.getInstance().getDiscordManager().isRunning()) {
                DiscordManager.this.updatePresence();
                try {
                    Thread.sleep(15000L);
                }
                catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    public record DiscordInfo(String userName, String avatarUrl, String userId) {
    }
}

