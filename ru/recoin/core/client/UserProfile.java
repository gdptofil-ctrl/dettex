/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.core.client;

import com.adl.nativeprotect.User;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public final class UserProfile {
    private static final String DEFAULT_USERNAME = "DettexClient";
    private static final int DEFAULT_UID = 0;
    private static final String DEFAULT_ROLE = "Offline";
    private static final String DEFAULT_AVATAR = "recoin:textures/profile/default-avatar.png";
    private static final String DEFAULT_LOGO = "textures/logo_theme.png";

    private UserProfile() {
    }

    public static String getUsername() {
        return UserProfile.nativeValue("username", DEFAULT_USERNAME);
    }

    public static int getUid() {
        try {
            return Integer.parseInt(UserProfile.nativeValue("uid", "0"));
        }
        catch (NumberFormatException numberFormatException) {
            return 0;
        }
    }

    public static String getUidString() {
        return UserProfile.nativeValue("uid", "\u2014");
    }

    public static String getExpire() {
        ru.recoin.core.license.LicenseGate gate = ru.recoin.core.license.LicenseGate.get();
        if (gate.enabled() && gate.license() != null) {
            long exp = gate.expires();
            return exp == Long.MAX_VALUE ? "lifetime" : String.valueOf(exp);
        }
        return UserProfile.nativeValue("subTime", "");
    }

    public static String getSubscriptionLabel() {
        String value = UserProfile.getExpire();
        if (value == null || value.isBlank()) {
            return "\u041f\u043e\u0434\u043f\u0438\u0441\u043a\u0430: \u043d\u0435\u0442 \u0434\u0430\u043d\u043d\u044b\u0445";
        }
        if ((value = value.trim()).equalsIgnoreCase("lifetime") || value.equalsIgnoreCase("forever")) {
            return "\u041f\u043e\u0434\u043f\u0438\u0441\u043a\u0430: \u043d\u0430\u0432\u0441\u0435\u0433\u0434\u0430";
        }
        try {
            LocalDate expiry;
            if (value.matches("\\d{10,13}")) {
                long timestamp = Long.parseLong(value);
                expiry = Instant.ofEpochMilli(value.length() == 10 ? timestamp * 1000L : timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
            } else {
                expiry = LocalDate.parse(value.length() >= 10 ? value.substring(0, 10) : value);
            }
            long days = ChronoUnit.DAYS.between(LocalDate.now(), expiry);
            return days < 0L ? "\u041f\u043e\u0434\u043f\u0438\u0441\u043a\u0430 \u0438\u0441\u0442\u0435\u043a\u043b\u0430" : "\u041f\u043e\u0434\u043f\u0438\u0441\u043a\u0430: " + days + " \u0434\u043d.";
        }
        catch (NumberFormatException | DateTimeException ignored) {
            return "\u041f\u043e\u0434\u043f\u0438\u0441\u043a\u0430: " + value;
        }
    }

    public static String getRole() {
        String licRole = ru.recoin.core.license.LicenseGate.get().role();
        if (licRole != null && !licRole.isBlank()) {
            return licRole;
        }
        return UserProfile.nativeValue("role", DEFAULT_ROLE);
    }

    public static String getAvatar() {
        return DEFAULT_AVATAR;
    }

    public static String getSubscriptionEndLabel() {
        String value = UserProfile.getExpire();
        if (value == null || value.isBlank()) {
            return "\u041f\u043e\u0434\u043f\u0438\u0441\u043a\u0430: \u043d\u0435\u0442 \u0434\u0430\u043d\u043d\u044b\u0445";
        }
        if ((value = value.trim()).equalsIgnoreCase("lifetime") || value.equalsIgnoreCase("forever")) {
            return "\u041f\u043e\u0434\u043f\u0438\u0441\u043a\u0430: \u043d\u0430\u0432\u0441\u0435\u0433\u0434\u0430";
        }
        try {
            LocalDate expiry;
            if (value.matches("\\d{10,13}")) {
                long timestamp = Long.parseLong(value);
                expiry = Instant.ofEpochMilli(value.length() == 10 ? timestamp * 1000L : timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
            } else {
                expiry = LocalDate.parse(value.length() >= 10 ? value.substring(0, 10) : value);
            }
            long days = ChronoUnit.DAYS.between(LocalDate.now(), expiry);
            String date = expiry.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            return days < 0L ? "\u0418\u0441\u0442\u0435\u043a\u043b\u0430 " + date : "\u0434\u043e " + date + " \u00b7 " + days + " \u0434\u043d.";
        }
        catch (NumberFormatException | DateTimeException ignored) {
            return "\u041f\u043e\u0434\u043f\u0438\u0441\u043a\u0430: " + value;
        }
    }

    public static boolean isDefaultAvatar() {
        return true;
    }

    private static String nativeValue(String key, String fallback) {
        User user = User.getInstance();
        String value = user.profile(key);
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    public static String getLogo() {
        return DEFAULT_LOGO;
    }
}

