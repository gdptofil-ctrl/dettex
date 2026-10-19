/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonSyntaxException
 *  net.minecraft.class_310
 *  net.minecraft.class_320
 */
package ru.recoin.implement.screens.mainmenu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_310;
import net.minecraft.class_320;
import ru.recoin.mixins.MinecraftClientSessionAccessor;

final class AltAccountStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private AltAccountStore() {
    }

    static Snapshot load() {
        LinkedHashSet<String> unique = new LinkedHashSet<String>();
        LinkedHashSet<String> favorites = new LinkedHashSet<String>();
        LinkedHashSet<String> generated = new LinkedHashSet<String>();
        LinkedHashMap<String, String> dates = new LinkedHashMap<String, String>();
        Path file = AltAccountStore.file();
        if (Files.isRegularFile(file, new LinkOption[0])) {
            try {
                AccountData data = (AccountData)GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), AccountData.class);
                if (data != null && data.accounts != null) {
                    for (String account : data.accounts) {
                        if (!AltAccountStore.isValid(account) || AltAccountStore.isDefaultLaunchUsername(account)) continue;
                        AltAccountStore.addIgnoreCase(unique, account);
                    }
                }
                if (data != null && data.favorites != null) {
                    for (String favorite : data.favorites) {
                        if (!AltAccountStore.isValid(favorite)) continue;
                        AltAccountStore.addIgnoreCase(favorites, favorite);
                    }
                }
                if (data != null && data.generated != null) {
                    for (String nickname : data.generated) {
                        if (!AltAccountStore.isValid(nickname)) continue;
                        AltAccountStore.addIgnoreCase(generated, nickname);
                    }
                }
                if (data != null && data.dates != null) {
                    dates.putAll(data.dates);
                }
            }
            catch (JsonSyntaxException | IOException data) {
                // empty catch block
            }
        }
        ArrayList<String> result = new ArrayList<String>(unique);
        favorites.removeIf(name -> !AltAccountStore.containsIgnoreCase(result, name));
        dates.keySet().removeIf(name -> !AltAccountStore.containsIgnoreCase(result, name));
        String lastAccount = null;
        if (Files.isRegularFile(file, new LinkOption[0])) {
            try {
                AccountData data = (AccountData)GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), AccountData.class);
                if (data != null && AltAccountStore.isValid(data.lastAccount)) {
                    lastAccount = data.lastAccount;
                }
            }
            catch (JsonSyntaxException | IOException data) {
                // empty catch block
            }
        }
        Snapshot snapshot = new Snapshot(result, favorites, dates, generated, lastAccount);
        AltAccountStore.save(snapshot);
        return snapshot;
    }

    static void save(Snapshot snapshot) {
        try {
            Path file = AltAccountStore.file();
            Files.createDirectories(file.getParent(), new FileAttribute[0]);
            Files.writeString(file, (CharSequence)GSON.toJson((Object)new AccountData(new ArrayList<String>(snapshot.accounts()), new ArrayList<String>(snapshot.favorites()), new LinkedHashMap<String, String>(snapshot.dates()), new ArrayList<String>(snapshot.generated()), snapshot.lastAccount())), StandardCharsets.UTF_8, new OpenOption[0]);
        }
        catch (IOException error) {
            System.err.println("[Recoin] Cannot save alt accounts: " + error.getMessage());
        }
    }

    static void restoreLastAccount() {
        class_310 client = class_310.method_1551();
        if (!AltAccountStore.isDefaultLaunchUsername(client.method_1548().method_1676())) {
            return;
        }
        String name = AltAccountStore.load().lastAccount();
        if (!AltAccountStore.isValid(name)) {
            return;
        }
        ((MinecraftClientSessionAccessor)client).recoin$setSession(new class_320(name, UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8)), "", Optional.empty(), Optional.empty()));
    }

    static boolean containsIgnoreCase(List<String> accounts, String name) {
        return accounts.stream().anyMatch(account -> account.equalsIgnoreCase(name));
    }

    private static void addIgnoreCase(LinkedHashSet<String> accounts, String name) {
        if (accounts.stream().noneMatch(account -> account.equalsIgnoreCase(name))) {
            accounts.add(name);
        }
    }

    private static boolean isValid(String name) {
        return name != null && name.matches("[A-Za-z0-9_]{3,16}");
    }

    private static boolean isDefaultLaunchUsername(String name) {
        return name.matches("(?i)Player\\d+");
    }

    private static Path file() {
        return class_310.method_1551().field_1697.toPath().resolve("recoin").resolve("accounts.json");
    }

    private static final class AccountData {
        private List<String> accounts;
        private List<String> favorites;
        private Map<String, String> dates;
        private List<String> generated;
        private String lastAccount;

        private AccountData(List<String> accounts, List<String> favorites, Map<String, String> dates, List<String> generated, String lastAccount) {
            this.accounts = accounts;
            this.favorites = favorites;
            this.dates = dates;
            this.generated = generated;
            this.lastAccount = lastAccount;
        }
    }

    record Snapshot(List<String> accounts, Set<String> favorites, Map<String, String> dates, Set<String> generated, String lastAccount) {
    }
}

