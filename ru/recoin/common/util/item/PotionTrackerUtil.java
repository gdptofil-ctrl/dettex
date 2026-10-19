/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1291
 *  net.minecraft.class_1293
 *  net.minecraft.class_1294
 *  net.minecraft.class_1799
 *  net.minecraft.class_1828
 *  net.minecraft.class_1844
 *  net.minecraft.class_6880
 *  net.minecraft.class_9334
 */
package ru.recoin.common.util.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1799;
import net.minecraft.class_1828;
import net.minecraft.class_1844;
import net.minecraft.class_6880;
import net.minecraft.class_9334;

public final class PotionTrackerUtil {
    private static final Map<Integer, String> DONATION_BY_COLOR = new HashMap<Integer, String>();

    public static boolean isSplashLikePotion(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        if (stack.method_7909() instanceof class_1828) {
            return true;
        }
        if (PotionTrackerUtil.resolveDonationPotionNameByDisplay(stack.method_7964().getString()) != null) {
            return true;
        }
        List<class_1293> effects = PotionTrackerUtil.rawPotionEffects(stack);
        return PotionTrackerUtil.matches(effects, (class_6880<class_1291>)class_1294.field_5909, 9, 200) && PotionTrackerUtil.matches(effects, (class_6880<class_1291>)class_1294.field_5904, 4, 400) || PotionTrackerUtil.matches(effects, (class_6880<class_1291>)class_1294.field_5924, 1, 900) && PotionTrackerUtil.matches(effects, (class_6880<class_1291>)class_1294.field_5905, 1, 12000) || PotionTrackerUtil.matches(effects, (class_6880<class_1291>)class_1294.field_5910, 4, 600) && PotionTrackerUtil.matches(effects, (class_6880<class_1291>)class_1294.field_5909, 3, 600) || PotionTrackerUtil.matches(effects, (class_6880<class_1291>)class_1294.field_5907, 0, 12000) && PotionTrackerUtil.matches(effects, (class_6880<class_1291>)class_1294.field_5914, 2, 1200) || PotionTrackerUtil.matches(effects, (class_6880<class_1291>)class_1294.field_5910, 3, 1200) && PotionTrackerUtil.matches(effects, (class_6880<class_1291>)class_1294.field_5904, 2, 6000) || PotionTrackerUtil.matches(effects, (class_6880<class_1291>)class_1294.field_5899, 1, 2000) && PotionTrackerUtil.matches(effects, (class_6880<class_1291>)class_1294.field_5920, 1, 1200) || PotionTrackerUtil.matches(effects, (class_6880<class_1291>)class_1294.field_5911, 1, 1800) && PotionTrackerUtil.matches(effects, (class_6880<class_1291>)class_1294.field_5920, 2, 1800);
    }

    public static String getPotionDisplayName(class_1799 stack) {
        String donationName = PotionTrackerUtil.resolveDonationPotionName(stack);
        return donationName != null ? donationName : stack.method_7964().getString();
    }

    public static List<class_1293> getPotionEffects(class_1799 stack) {
        String donationName = PotionTrackerUtil.resolveDonationPotionName(stack);
        if (donationName != null) {
            return PotionTrackerUtil.donationEffects(donationName);
        }
        return PotionTrackerUtil.rawPotionEffects(stack);
    }

    public static String resolveDonationPotionName(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return null;
        }
        String byName = PotionTrackerUtil.resolveDonationPotionNameByDisplay(stack.method_7964().getString());
        if (byName != null) {
            PotionTrackerUtil.rememberDonationColor(stack, byName);
            return byName;
        }
        String byColor = PotionTrackerUtil.resolveDonationPotionNameByColor(stack);
        if (byColor != null) {
            return byColor;
        }
        return PotionTrackerUtil.resolveDonationPotionNameByEffects(PotionTrackerUtil.rawPotionEffects(stack));
    }

    public static String resolveDonationPotionNameStrict(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return null;
        }
        String byName = PotionTrackerUtil.resolveDonationPotionNameByDisplay(stack.method_7964().getString());
        if (byName != null) {
            return byName;
        }
        return PotionTrackerUtil.resolveDonationPotionNameByEffects(PotionTrackerUtil.rawPotionEffects(stack));
    }

    private static String resolveDonationPotionNameByEffects(List<class_1293> effects) {
        if (PotionTrackerUtil.matches(effects, (class_6880<class_1291>)class_1294.field_5909, 9, 200) && PotionTrackerUtil.matches(effects, (class_6880<class_1291>)class_1294.field_5904, 4, 400)) {
            return "\u0425\u043b\u043e\u043f\u0443\u0448\u043a\u0430";
        }
        if (PotionTrackerUtil.matches(effects, (class_6880<class_1291>)class_1294.field_5924, 1, 900) && PotionTrackerUtil.matches(effects, (class_6880<class_1291>)class_1294.field_5905, 1, 12000)) {
            return "\u0421\u0432\u044f\u0442\u0430\u044f \u0432\u043e\u0434\u0430";
        }
        if (PotionTrackerUtil.matches(effects, (class_6880<class_1291>)class_1294.field_5910, 4, 600) && PotionTrackerUtil.matches(effects, (class_6880<class_1291>)class_1294.field_5909, 3, 600)) {
            return "\u0417\u0435\u043b\u044c\u0435 \u0413\u043d\u0435\u0432\u0430";
        }
        if (PotionTrackerUtil.matches(effects, (class_6880<class_1291>)class_1294.field_5907, 0, 12000) && PotionTrackerUtil.matches(effects, (class_6880<class_1291>)class_1294.field_5914, 2, 1800)) {
            return "\u0417\u0435\u043b\u044c\u0435 \u041f\u0430\u043b\u043b\u0430\u0434\u0438\u043d\u0430";
        }
        if (PotionTrackerUtil.matches(effects, (class_6880<class_1291>)class_1294.field_5910, 3, 1200) && PotionTrackerUtil.matches(effects, (class_6880<class_1291>)class_1294.field_5904, 2, 6000)) {
            return "\u0417\u0435\u043b\u044c\u0435 \u0410\u0441\u0441\u0430\u0441\u0438\u043d\u0430";
        }
        if (PotionTrackerUtil.matches(effects, (class_6880<class_1291>)class_1294.field_5899, 1, 2000) && PotionTrackerUtil.matches(effects, (class_6880<class_1291>)class_1294.field_5920, 1, 1200)) {
            return "\u0417\u0435\u043b\u044c\u0435 \u0420\u0430\u0434\u0438\u0430\u0446\u0438\u0438";
        }
        if (PotionTrackerUtil.matches(effects, (class_6880<class_1291>)class_1294.field_5911, 1, 1800) && PotionTrackerUtil.matches(effects, (class_6880<class_1291>)class_1294.field_5920, 2, 1800)) {
            return "\u0421\u043d\u043e\u0442\u0432\u043e\u0440\u043d\u043e\u0435";
        }
        return null;
    }

    public static void observeDonationColor(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return;
        }
        String name = PotionTrackerUtil.resolveDonationPotionNameByDisplay(stack.method_7964().getString());
        if (name != null) {
            PotionTrackerUtil.rememberDonationColor(stack, name);
        }
    }

    private static void rememberDonationColor(class_1799 stack, String name) {
        Integer color = PotionTrackerUtil.potionColor(stack);
        if (color != null) {
            DONATION_BY_COLOR.put(color & 0xFFFFFF, name);
        }
    }

    private static String resolveDonationPotionNameByColor(class_1799 stack) {
        Integer color = PotionTrackerUtil.potionColor(stack);
        if (color == null || DONATION_BY_COLOR.isEmpty()) {
            return null;
        }
        int rgb = color & 0xFFFFFF;
        String nearestName = null;
        int nearestDistance = Integer.MAX_VALUE;
        for (Map.Entry<Integer, String> entry : DONATION_BY_COLOR.entrySet()) {
            int distance = PotionTrackerUtil.colorDistance(rgb, entry.getKey());
            if (distance >= nearestDistance) continue;
            nearestDistance = distance;
            nearestName = entry.getValue();
        }
        return nearestDistance <= 576 ? nearestName : null;
    }

    private static Integer potionColor(class_1799 stack) {
        class_1844 component = (class_1844)stack.method_58694(class_9334.field_49651);
        return component == null ? null : Integer.valueOf(component.method_8064());
    }

    private static int colorDistance(int first, int second) {
        int dr = (first >> 16 & 0xFF) - (second >> 16 & 0xFF);
        int dg = (first >> 8 & 0xFF) - (second >> 8 & 0xFF);
        int db = (first & 0xFF) - (second & 0xFF);
        return dr * dr + dg * dg + db * db;
    }

    private static String resolveDonationPotionNameByDisplay(String displayName) {
        String lower;
        String string = lower = displayName == null ? "" : displayName.toLowerCase(Locale.ROOT);
        if (lower.contains("\u0445\u043b\u043e\u043f\u0443\u0448")) {
            return "\u0425\u043b\u043e\u043f\u0443\u0448\u043a\u0430";
        }
        if (lower.contains("\u0441\u0432\u044f\u0442\u0430\u044f \u0432\u043e\u0434\u0430")) {
            return "\u0421\u0432\u044f\u0442\u0430\u044f \u0432\u043e\u0434\u0430";
        }
        if (lower.contains("\u0433\u043d\u0435\u0432\u0430")) {
            return "\u0417\u0435\u043b\u044c\u0435 \u0413\u043d\u0435\u0432\u0430";
        }
        if (lower.contains("\u043f\u0430\u043b\u0430\u0434\u0438\u043d") || lower.contains("\u043f\u0430\u043b\u043b\u0430\u0434\u0438\u043d\u0430")) {
            return "\u0417\u0435\u043b\u044c\u0435 \u041f\u0430\u043b\u043b\u0430\u0434\u0438\u043d\u0430";
        }
        if (lower.contains("\u0430\u0441\u0441\u0430\u0441\u0438\u043d")) {
            return "\u0417\u0435\u043b\u044c\u0435 \u0410\u0441\u0441\u0430\u0441\u0438\u043d\u0430";
        }
        if (lower.contains("\u0440\u0430\u0434\u0438\u0430\u0446\u0438")) {
            return "\u0417\u0435\u043b\u044c\u0435 \u0420\u0430\u0434\u0438\u0430\u0446\u0438\u0438";
        }
        if (lower.contains("\u0441\u043d\u043e\u0442\u0432\u043e\u0440")) {
            return "\u0421\u043d\u043e\u0442\u0432\u043e\u0440\u043d\u043e\u0435";
        }
        return null;
    }

    public static List<class_1293> donationEffects(String name) {
        return switch (name) {
            case "\u0425\u043b\u043e\u043f\u0443\u0448\u043a\u0430" -> List.of(PotionTrackerUtil.effect((class_6880<class_1291>)class_1294.field_5909, 200, 9), PotionTrackerUtil.effect((class_6880<class_1291>)class_1294.field_5904, 400, 4), PotionTrackerUtil.effect((class_6880<class_1291>)class_1294.field_5919, 100, 9), PotionTrackerUtil.effect((class_6880<class_1291>)class_1294.field_5912, 3600, 0));
            case "\u0421\u0432\u044f\u0442\u0430\u044f \u0432\u043e\u0434\u0430" -> List.of(PotionTrackerUtil.effect((class_6880<class_1291>)class_1294.field_5924, 900, 1), PotionTrackerUtil.effect((class_6880<class_1291>)class_1294.field_5905, 12000, 1), PotionTrackerUtil.effect((class_6880<class_1291>)class_1294.field_5915, 1, 1));
            case "\u0417\u0435\u043b\u044c\u0435 \u0413\u043d\u0435\u0432\u0430" -> List.of(PotionTrackerUtil.effect((class_6880<class_1291>)class_1294.field_5910, 600, 4), PotionTrackerUtil.effect((class_6880<class_1291>)class_1294.field_5909, 600, 3));
            case "\u0417\u0435\u043b\u044c\u0435 \u041f\u0430\u043b\u043b\u0430\u0434\u0438\u043d\u0430" -> List.of(PotionTrackerUtil.effect((class_6880<class_1291>)class_1294.field_5907, 12000, 0), PotionTrackerUtil.effect((class_6880<class_1291>)class_1294.field_5918, 12000, 0), PotionTrackerUtil.effect((class_6880<class_1291>)class_1294.field_5914, 1200, 2), PotionTrackerUtil.effect((class_6880<class_1291>)class_1294.field_5905, 18000, 2));
            case "\u0417\u0435\u043b\u044c\u0435 \u0410\u0441\u0441\u0430\u0441\u0438\u043d\u0430" -> List.of(PotionTrackerUtil.effect((class_6880<class_1291>)class_1294.field_5910, 1200, 3), PotionTrackerUtil.effect((class_6880<class_1291>)class_1294.field_5904, 6000, 2), PotionTrackerUtil.effect((class_6880<class_1291>)class_1294.field_5917, 1200, 0), PotionTrackerUtil.effect((class_6880<class_1291>)class_1294.field_5921, 1, 1));
            case "\u0417\u0435\u043b\u044c\u0435 \u0420\u0430\u0434\u0438\u0430\u0446\u0438\u0438" -> List.of(PotionTrackerUtil.effect((class_6880<class_1291>)class_1294.field_5899, 1200, 1), PotionTrackerUtil.effect((class_6880<class_1291>)class_1294.field_5920, 1200, 1), PotionTrackerUtil.effect((class_6880<class_1291>)class_1294.field_5909, 1800, 2), PotionTrackerUtil.effect((class_6880<class_1291>)class_1294.field_5903, 1200, 4), PotionTrackerUtil.effect((class_6880<class_1291>)class_1294.field_5912, 2400, 0));
            case "\u0421\u043d\u043e\u0442\u0432\u043e\u0440\u043d\u043e\u0435" -> List.of(PotionTrackerUtil.effect((class_6880<class_1291>)class_1294.field_5911, 1800, 1), PotionTrackerUtil.effect((class_6880<class_1291>)class_1294.field_5916, 200, 1), PotionTrackerUtil.effect((class_6880<class_1291>)class_1294.field_5920, 1800, 2), PotionTrackerUtil.effect((class_6880<class_1291>)class_1294.field_5919, 200, 0));
            default -> List.of();
        };
    }

    private static class_1293 effect(class_6880<class_1291> effect, int duration, int amplifier) {
        return new class_1293(effect, duration, amplifier);
    }

    private static List<class_1293> rawPotionEffects(class_1799 stack) {
        class_1844 component = (class_1844)stack.method_58694(class_9334.field_49651);
        if (component == null) {
            return List.of();
        }
        ArrayList<class_1293> effects = new ArrayList<class_1293>();
        component.method_57397().forEach(effects::add);
        return effects;
    }

    private static boolean matches(List<class_1293> effects, class_6880<class_1291> type, int amplifier, int duration) {
        return effects.stream().anyMatch(effect -> effect.method_5579().equals((Object)type) && effect.method_5578() == amplifier && effect.method_5584() == duration);
    }

    private PotionTrackerUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

