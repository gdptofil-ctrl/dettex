/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_2596
 *  net.minecraft.class_7439
 */
package ru.recoin.implement.features.modules.misc;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_124;
import net.minecraft.class_2596;
import net.minecraft.class_7439;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.ButtonSetting;
import ru.recoin.api.feature.module.setting.implement.TextSetting;
import ru.recoin.common.util.logger.LoggerUtil;
import ru.recoin.core.Main;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.WorldLoadEvent;

public final class AutoAuth
extends Module {
    private static final long COMMAND_COOLDOWN_MS = 3000L;
    private final TextSetting password = new TextSetting("Password", "Password used for automatic /register and /login").setText("").setMin(1).setMax(64);
    private final ButtonSetting openPasswordsFolder = new ButtonSetting("Passwords Folder", "Open the folder containing account-password entries").setButtonName("Open").setRunnable(this::openPasswordsFolder);
    private String activeAccount;
    private String lastSavedPassword = "";
    private String lastCommand = "";
    private long lastCommandAt;

    public AutoAuth() {
        super("AutoAuth", "Auto Auth", ModuleCategory.MISC);
        this.setup(this.password, this.openPasswordsFolder);
    }

    @Override
    public void activate() {
        this.synchronizeAccount();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        this.synchronizeAccount();
        this.saveChangedPassword();
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        this.lastCommand = "";
        this.lastCommandAt = 0L;
        this.synchronizeAccount();
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        class_2596<?> class_25962;
        if (event.isSend() || !((class_25962 = event.getPacket()) instanceof class_7439)) {
            return;
        }
        class_7439 packet = (class_7439)class_25962;
        String message = class_124.method_539((String)packet.comp_763().getString());
        if (message == null || message.isBlank()) {
            return;
        }
        this.synchronizeAccount();
        String lowered = message.toLowerCase(Locale.ROOT);
        String value = this.normalizedPassword();
        if (value.isEmpty() || this.containsWhitespace(value)) {
            return;
        }
        this.saveChangedPassword();
        if (this.isRegisterPrompt(lowered)) {
            String command = lowered.contains("/reg ") && !lowered.contains("/register") ? "reg " : "register ";
            this.sendAuthCommand(command + value + " " + value);
        } else if (this.isLoginPrompt(lowered)) {
            String command = lowered.contains("/l ") && !lowered.contains("/login") ? "l " : "login ";
            this.sendAuthCommand(command + value);
        }
    }

    private void synchronizeAccount() {
        Map<String, String> entries;
        String stored;
        String account = this.currentAccount();
        if (account.isEmpty() || account.equals(this.activeAccount)) {
            return;
        }
        if (this.activeAccount != null) {
            this.savePassword(this.activeAccount, this.normalizedPassword());
        }
        if ((stored = this.findPassword(entries = this.readEntries(), account)) != null) {
            this.password.setText(stored);
            this.lastSavedPassword = stored;
        } else if (this.activeAccount == null && !this.normalizedPassword().isEmpty()) {
            this.lastSavedPassword = "";
        } else {
            this.password.setText("");
            this.lastSavedPassword = "";
        }
        this.activeAccount = account;
    }

    private void saveChangedPassword() {
        String account = this.currentAccount();
        String value = this.normalizedPassword();
        if (account.isEmpty() || value.isEmpty() || value.equals(this.lastSavedPassword)) {
            return;
        }
        if (this.savePassword(account, value)) {
            this.activeAccount = account;
            this.lastSavedPassword = value;
        }
    }

    private synchronized boolean savePassword(String account, String value) {
        if (account == null || account.isBlank() || value == null || value.isBlank()) {
            return false;
        }
        Map<String, String> entries = this.readEntries();
        entries.keySet().removeIf(key -> key.equalsIgnoreCase(account));
        entries.put(account, value.replace("\r", "").replace("\n", ""));
        Path folder = this.passwordsFolder();
        Path file = this.passwordsFile();
        Path temporary = folder.resolve("accounts.txt.tmp");
        try {
            Files.createDirectories(folder, new FileAttribute[0]);
            ArrayList lines = new ArrayList(entries.size());
            entries.forEach((name, savedPassword) -> lines.add(name + ":" + savedPassword));
            Files.write(temporary, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            try {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            }
            catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        }
        catch (IOException exception) {
            LoggerUtil.error("Could not save AutoAuth password", exception);
            return false;
        }
    }

    private synchronized Map<String, String> readEntries() {
        LinkedHashMap<String, String> entries = new LinkedHashMap<String, String>();
        Path file = this.passwordsFile();
        if (!Files.isRegularFile(file, new LinkOption[0])) {
            return entries;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                int separator = line.indexOf(58);
                if (separator <= 0) continue;
                String account = line.substring(0, separator).trim();
                String value = line.substring(separator + 1);
                if (account.isEmpty() || value.isEmpty()) continue;
                entries.put(account, value);
            }
        }
        catch (IOException exception) {
            LoggerUtil.error("Could not read AutoAuth passwords", exception);
        }
        return entries;
    }

    private String findPassword(Map<String, String> entries, String account) {
        return entries.entrySet().stream().filter(entry -> ((String)entry.getKey()).equalsIgnoreCase(account)).map(Map.Entry::getValue).findFirst().orElse(null);
    }

    private void sendAuthCommand(String command) {
        long now = System.currentTimeMillis();
        if (command.equals(this.lastCommand) && now - this.lastCommandAt < 3000L) {
            return;
        }
        this.lastCommand = command;
        this.lastCommandAt = now;
        mc.execute(() -> {
            if (AutoAuth.mc.field_1724 != null && AutoAuth.mc.field_1724.field_3944 != null) {
                AutoAuth.mc.field_1724.field_3944.method_45730(command);
            }
        });
    }

    private boolean isRegisterPrompt(String message) {
        return message.contains("/register") || message.contains("/reg ") || message.contains("please register") || message.contains("you need to register") || message.contains("register with") || message.contains("\u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u0443\u0439\u0442\u0435\u0441\u044c") || message.contains("\u043d\u0435 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043e\u0432\u0430\u043d") || message.contains("\u043f\u0440\u0438\u0434\u0443\u043c\u0430\u0439\u0442\u0435 \u043f\u0430\u0440\u043e\u043b\u044c");
    }

    private boolean isLoginPrompt(String message) {
        return message.contains("/login") || message.contains("/l ") || message.contains("please login") || message.contains("please log in") || message.contains("you need to login") || message.contains("\u0430\u0432\u0442\u043e\u0440\u0438\u0437\u0443\u0439\u0442\u0435\u0441\u044c") || message.contains("\u0432\u0432\u0435\u0434\u0438\u0442\u0435 \u043f\u0430\u0440\u043e\u043b\u044c") || message.contains("\u0432\u043e\u0439\u0434\u0438\u0442\u0435 \u0432 \u0430\u043a\u043a\u0430\u0443\u043d\u0442");
    }

    private void openPasswordsFolder() {
        Path folder = this.passwordsFolder();
        try {
            Files.createDirectories(folder, new FileAttribute[0]);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(folder.toFile());
            } else {
                new ProcessBuilder("explorer.exe", folder.toAbsolutePath().toString()).start();
            }
        }
        catch (Exception exception) {
            LoggerUtil.error("Could not open AutoAuth passwords folder", exception);
        }
    }

    private Path passwordsFolder() {
        return Main.getInstance().getClientInfoProvider().clientDir().toPath().resolve("passwords");
    }

    private Path passwordsFile() {
        return this.passwordsFolder().resolve("accounts.txt");
    }

    private String currentAccount() {
        return mc.method_1548() == null ? "" : mc.method_1548().method_1676().trim();
    }

    private String normalizedPassword() {
        String value = this.password.getText();
        return value == null ? "" : value.trim();
    }

    private boolean containsWhitespace(String value) {
        return value.chars().anyMatch(Character::isWhitespace);
    }
}

