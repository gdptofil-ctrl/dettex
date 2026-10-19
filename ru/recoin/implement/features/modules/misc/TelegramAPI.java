/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.class_1799
 *  net.minecraft.class_2561
 *  net.minecraft.class_2596
 *  net.minecraft.class_266
 *  net.minecraft.class_268
 *  net.minecraft.class_269
 *  net.minecraft.class_270
 *  net.minecraft.class_276
 *  net.minecraft.class_318
 *  net.minecraft.class_338
 *  net.minecraft.class_7439
 *  net.minecraft.class_8646
 *  net.minecraft.class_9011
 */
package ru.recoin.implement.features.modules.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_2596;
import net.minecraft.class_266;
import net.minecraft.class_268;
import net.minecraft.class_269;
import net.minecraft.class_270;
import net.minecraft.class_276;
import net.minecraft.class_318;
import net.minecraft.class_338;
import net.minecraft.class_7439;
import net.minecraft.class_8646;
import net.minecraft.class_9011;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.TextSetting;
import ru.recoin.common.util.other.Instance;
import ru.recoin.core.Main;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.features.modules.render.AuctionHelper;

public final class TelegramAPI
extends Module {
    private final TextSetting token = new TextSetting("Bot Token", "\u0422\u043e\u043a\u0435\u043d \u043e\u0442 BotFather").setText("").setMax(256).visible(() -> false);
    private final BooleanSetting saleReports = new BooleanSetting("\u041e\u0442\u0447\u0451\u0442\u044b AutoSell", "\u041f\u0440\u0438\u0441\u044b\u043b\u0430\u0442\u044c \u0441\u0443\u043c\u043c\u0443 \u043f\u0440\u043e\u0434\u0430\u0436 \u0438 \u0431\u0430\u043b\u0430\u043d\u0441").setValue(true);
    private final AtomicBoolean running = new AtomicBoolean();
    private final Set<String> chats = ConcurrentHashMap.newKeySet();
    private Thread pollingThread;
    private long updateOffset;

    public TelegramAPI() {
        super("TelegramAPI", "Telegram API", ModuleCategory.MISC);
        this.setup(this.token, this.saleReports);
    }

    public static TelegramAPI getInstance() {
        return Instance.get(TelegramAPI.class);
    }

    public void setBotToken(String value) {
        boolean restart = this.isState();
        if (restart) {
            this.setState(false);
        }
        this.token.setText(value == null ? "" : value.trim());
        this.chats.clear();
        this.updateOffset = 0L;
        if (restart) {
            this.setState(true);
        }
    }

    @Override
    public void activate() {
        // INTERNET DISABLED FOR SECURITY
        this.setState(false);
        return;
    }

    @Override
    public void deactivate() {
        this.running.set(false);
        if (this.pollingThread != null) {
            this.pollingThread.interrupt();
        }
        this.pollingThread = null;
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        class_2596<?> class_25962;
        if (!this.saleReports.isValue() || event.isSend() || !((class_25962 = event.getPacket()) instanceof class_7439)) {
            return;
        }
        class_7439 packet = (class_7439)class_25962;
        String message = packet.comp_763().getString().toLowerCase(Locale.ROOT);
        if (!message.contains("\u043a\u0443\u043f\u0438\u043b\u0438") && !message.contains("\u043f\u0440\u0438\u043e\u0431\u0440\u0435\u043b\u0438")) {
            return;
        }
        mc.execute(() -> {
            AuctionHelper helper = AuctionHelper.getInstance();
            this.sendMessage("AutoSell: \u043f\u0440\u043e\u0434\u0430\u043d\u043e \u043b\u043e\u0442\u043e\u0432: " + helper.getAutoSellSoldLots() + ", \u0441\u0443\u043c\u043c\u0430: " + helper.getAutoSellRevenue() + "\n" + this.readBalance());
        });
    }

    private void pollLoop() {
        // INTERNET DISABLED
    }

    private void handleCommand(String chat, String raw) {
        String[] split = raw.split("\\s+", 2);
        String command = split[0].toLowerCase(Locale.ROOT);
        String argument = split.length > 1 ? split[1].trim() : "";
        switch (command) {
            case "/help": 
            case "/start": {
                this.sendMessage(chat, "/status, /inventory, /on <\u043c\u043e\u0434\u0443\u043b\u044c>, /off <\u043c\u043e\u0434\u0443\u043b\u044c>, /say <\u0442\u0435\u043a\u0441\u0442>, /cmd <\u043a\u043e\u043c\u0430\u043d\u0434\u0430>, /screenshot, /autosell, /balance");
                break;
            }
            case "/status": {
                mc.execute(() -> this.sendMessage(chat, this.status()));
                break;
            }
            case "/inventory": {
                mc.execute(() -> this.sendMessage(chat, this.inventory()));
                break;
            }
            case "/on": {
                this.setModule(chat, argument, true);
                break;
            }
            case "/off": {
                this.setModule(chat, argument, false);
                break;
            }
            case "/say": {
                mc.execute(() -> {
                    if (TelegramAPI.mc.field_1724 != null && !argument.isBlank()) {
                        TelegramAPI.mc.field_1724.field_3944.method_45729(argument);
                    }
                });
                break;
            }
            case "/cmd": {
                mc.execute(() -> {
                    if (TelegramAPI.mc.field_1724 != null && !argument.isBlank()) {
                        TelegramAPI.mc.field_1724.field_3944.method_45730(argument.startsWith("/") ? argument.substring(1) : argument);
                    }
                });
                break;
            }
            case "/autosell": {
                mc.execute(() -> {
                    AuctionHelper helper = AuctionHelper.getInstance();
                    this.sendMessage(chat, "AutoSell: " + (helper.isAutoSellActive() ? "\u0440\u0430\u0431\u043e\u0442\u0430\u0435\u0442" : "\u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d") + ", \u043b\u043e\u0442\u043e\u0432: " + helper.getAutoSellSoldLots() + ", \u0441\u0443\u043c\u043c\u0430: " + helper.getAutoSellRevenue());
                });
                break;
            }
            case "/balance": {
                mc.execute(() -> this.sendMessage(chat, this.readBalance()));
                break;
            }
            case "/screenshot": {
                this.requestScreenshot(chat);
                break;
            }
            default: {
                this.sendMessage(chat, "\u041d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u0430\u044f \u043a\u043e\u043c\u0430\u043d\u0434\u0430. /help");
            }
        }
    }

    private void setModule(String chat, String name, boolean enabled) {
        mc.execute(() -> {
            Object module = Main.getInstance().getModuleProvider().get(name);
            if (module == null) {
                this.sendMessage(chat, "\u041c\u043e\u0434\u0443\u043b\u044c \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d: " + name);
                return;
            }
            ((Module)module).setState(enabled);
            this.sendMessage(chat, ((Module)module).getVisibleName() + ": " + (enabled ? "ON" : "OFF"));
        });
    }

    private String status() {
        if (TelegramAPI.mc.field_1724 == null || TelegramAPI.mc.field_1687 == null) {
            return "Minecraft: \u043d\u0435\u0442 \u043c\u0438\u0440\u0430";
        }
        long enabled = Main.getInstance().getModuleRepository().modules().stream().filter(Module::isState).count();
        return "\u0418\u0433\u0440\u043e\u043a: " + TelegramAPI.mc.field_1724.method_5477().getString() + "\nHP: " + Math.round(TelegramAPI.mc.field_1724.method_6032()) + "\nXYZ: " + TelegramAPI.mc.field_1724.method_31477() + " " + TelegramAPI.mc.field_1724.method_31478() + " " + TelegramAPI.mc.field_1724.method_31479() + "\n\u041c\u043e\u0434\u0443\u043b\u0435\u0439 \u0432\u043a\u043b\u044e\u0447\u0435\u043d\u043e: " + enabled;
    }

    private String inventory() {
        if (TelegramAPI.mc.field_1724 == null) {
            return "\u0418\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044c \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d";
        }
        LinkedHashMap<String, Integer> items = new LinkedHashMap<String, Integer>();
        for (int slot = 0; slot < TelegramAPI.mc.field_1724.method_31548().method_5439(); ++slot) {
            class_1799 stack = TelegramAPI.mc.field_1724.method_31548().method_5438(slot);
            if (stack.method_7960()) continue;
            items.merge(stack.method_7964().getString(), stack.method_7947(), Integer::sum);
        }
        if (items.isEmpty()) {
            return "\u0418\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044c \u043f\u0443\u0441\u0442";
        }
        StringBuilder out = new StringBuilder("\u0418\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044c:\n");
        items.forEach((name, count) -> out.append("\u2022 ").append((String)name).append(" \u00d7").append(count).append('\n'));
        return this.limit(out.toString());
    }

    private String readBalance() {
        if (TelegramAPI.mc.field_1687 == null) {
            return "\u0411\u0430\u043b\u0430\u043d\u0441: \u043c\u0438\u0440 \u043d\u0435 \u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d";
        }
        class_269 scoreboard = TelegramAPI.mc.field_1687.method_8428();
        class_266 objective = scoreboard.method_1189(class_8646.field_45157);
        if (objective == null) {
            return "\u0411\u0430\u043b\u0430\u043d\u0441: scoreboard \u043e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u0435\u0442";
        }
        String coinLine = null;
        String fallbackLine = null;
        for (class_9011 entry : scoreboard.method_1184(objective)) {
            String line = class_268.method_1142((class_270)scoreboard.method_1164(entry.comp_2127()), (class_2561)entry.method_55387()).getString();
            String lower = line.toLowerCase(Locale.ROOT);
            if (lower.contains("\u043c\u043e\u043d\u0435\u0442")) {
                coinLine = this.formatScoreboardLine(line);
                break;
            }
            if (fallbackLine != null || !lower.contains("\u0431\u0430\u043b\u0430\u043d\u0441") && !lower.contains("coins")) continue;
            fallbackLine = this.formatScoreboardLine(line);
        }
        if (coinLine != null) {
            return coinLine;
        }
        if (fallbackLine != null) {
            return fallbackLine;
        }
        return "\u0411\u0430\u043b\u0430\u043d\u0441: \u0441\u0442\u0440\u043e\u043a\u0430 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u0430";
    }

    private String formatScoreboardLine(String line) {
        String clean = line.replaceAll("(?i)\u00a7[0-9a-fk-or]", "").trim();
        if ((clean = clean.replaceAll("^[|\u2551\u2560\u2550\u25a0\u25c6\u2605\u269d$ ]+", "").trim()).startsWith("$")) {
            clean = clean.substring(1).trim();
        }
        return clean;
    }

    private void requestScreenshot(String chat) {
        mc.execute(() -> {
            if (mc.method_1522() == null) {
                this.sendMessage(chat, "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u043e\u043b\u0443\u0447\u0438\u0442\u044c \u0438\u0437\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u0435 \u0438\u0433\u0440\u044b");
                return;
            }
            String fileName = "recoin-telegram-" + System.currentTimeMillis() + ".png";
            Path path = TelegramAPI.mc.field_1697.toPath().resolve("screenshots").resolve(fileName);
            class_318.method_22690((File)TelegramAPI.mc.field_1697, (String)fileName, (class_276)mc.method_1522(), (int)1, result -> Thread.ofPlatform().daemon(true).name("Recoin-TelegramScreenshot").start(() -> {
                try {
                    if (!Files.isRegularFile(path, new LinkOption[0])) {
                        throw new IllegalStateException(result.getString());
                    }
                    this.sendPhoto(chat, path);
                }
                catch (Throwable error) {
                    this.sendMessage(chat, "\u041e\u0448\u0438\u0431\u043a\u0430 \u0441\u043a\u0440\u0438\u043d\u0448\u043e\u0442\u0430: " + error.getMessage());
                }
            }));
        });
    }

    private void sendMessage(String text) {
        if (text == null || text.isBlank() || !this.running.get()) {
            return;
        }
        for (String chat : this.chats) {
            this.sendMessage(chat, text);
        }
    }

    private boolean validateBotConnection() {
        return false;
    }

    private void showConnectionTutorial(String reason) {
        if (TelegramAPI.mc.field_1724 == null) {
            return;
        }
        class_338 chat = TelegramAPI.mc.field_1705.method_1743();
        chat.method_1812((class_2561)class_2561.method_43470((String)("\u00a7c[Telegram] " + reason)));
        chat.method_1812((class_2561)class_2561.method_43470((String)"\u00a7f\u041a\u0430\u043a \u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0438\u0442\u044c Telegram API:"));
        chat.method_1812((class_2561)class_2561.method_43470((String)"\u00a771. \u041e\u0442\u043a\u0440\u043e\u0439 \u00a7f@BotFather \u00a77\u0432 Telegram \u0438 \u043e\u0442\u043f\u0440\u0430\u0432\u044c \u00a7f/newbot"));
        chat.method_1812((class_2561)class_2561.method_43470((String)"\u00a772. \u0421\u043e\u0437\u0434\u0430\u0439 \u0431\u043e\u0442\u0430 \u0438 \u0441\u043a\u043e\u043f\u0438\u0440\u0443\u0439 \u0432\u044b\u0434\u0430\u043d\u043d\u044b\u0439 Bot Token"));
        chat.method_1812((class_2561)class_2561.method_43470((String)"\u00a773. \u0412 \u0447\u0430\u0442\u0435 Minecraft \u0432\u0432\u0435\u0434\u0438 \u00a7b.token <\u0442\u0432\u043e\u0439_\u0442\u043e\u043a\u0435\u043d>"));
        chat.method_1812((class_2561)class_2561.method_43470((String)"\u00a774. \u0421\u043d\u043e\u0432\u0430 \u0432\u043a\u043b\u044e\u0447\u0438 \u043c\u043e\u0434\u0443\u043b\u044c \u00a7fTelegram API"));
        chat.method_1812((class_2561)class_2561.method_43470((String)"\u00a775. \u041e\u0442\u043a\u0440\u043e\u0439 \u0441\u043e\u0437\u0434\u0430\u043d\u043d\u043e\u0433\u043e \u0431\u043e\u0442\u0430 \u0438 \u043e\u0442\u043f\u0440\u0430\u0432\u044c \u0435\u043c\u0443 \u00a7f/start"));
        chat.method_1812((class_2561)class_2561.method_43470((String)"\u00a78\u0422\u043e\u043a\u0435\u043d \u043d\u0438\u043a\u043e\u043c\u0443 \u043d\u0435 \u043e\u0442\u043f\u0440\u0430\u0432\u043b\u044f\u0439: \u043e\u043d \u0434\u0430\u0451\u0442 \u043f\u043e\u043b\u043d\u044b\u0439 \u0434\u043e\u0441\u0442\u0443\u043f \u043a \u0431\u043e\u0442\u0443."));
    }

    public void sendNotification(String text) {
        if (this.isState() && text != null && !text.isBlank()) {
            this.sendMessage(text);
        }
    }

    private void sendMessage(String chat, String text) {
        // INTERNET DISABLED
    }

    private void sendPhoto(Path path) throws Exception {
        for (String chat : this.chats) {
            this.sendPhoto(chat, path);
        }
    }

    private void sendPhoto(String chat, Path path) throws Exception {
        // INTERNET DISABLED
    }

    private void writePart(ByteArrayOutputStream body, String boundary, String name, String value) throws Exception {
        body.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + name + "\"\r\n\r\n" + value + "\r\n").getBytes(StandardCharsets.UTF_8));
    }

    private String api(String method) {
        return "https://api.telegram.org/bot" + this.token.getText().trim() + "/" + method;
    }

    private String limit(String value) {
        return value.length() <= 3900 ? value : value.substring(0, 3890) + "\n\u2026";
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}

