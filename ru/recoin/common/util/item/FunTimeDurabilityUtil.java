/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10192
 *  net.minecraft.class_1799
 *  net.minecraft.class_3532
 *  net.minecraft.class_5321
 *  net.minecraft.class_9334
 */
package ru.recoin.common.util.item;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_10192;
import net.minecraft.class_1799;
import net.minecraft.class_3532;
import net.minecraft.class_5321;
import net.minecraft.class_9334;

public final class FunTimeDurabilityUtil {
    private static final Pattern ASSET_PERCENT = Pattern.compile("(?:diamond|netherite)_(\\d{1,3})$");

    public static int percent(class_1799 stack) {
        String path;
        Matcher matcher;
        if (stack == null || stack.method_7960()) {
            return -1;
        }
        class_10192 equippable = (class_10192)stack.method_58694(class_9334.field_54196);
        if (equippable != null && equippable.comp_3176().isPresent() && (matcher = ASSET_PERCENT.matcher(path = ((class_5321)equippable.comp_3176().get()).method_29177().method_12832())).find()) {
            return class_3532.method_15340((int)Integer.parseInt(matcher.group(1)), (int)0, (int)100);
        }
        if (!stack.method_7963() || stack.method_7936() <= 0) {
            return -1;
        }
        int remaining = Math.max(0, stack.method_7936() - stack.method_7919());
        return class_3532.method_15340((int)Math.round((float)remaining * 100.0f / (float)stack.method_7936()), (int)0, (int)100);
    }

    public static int color(int percent) {
        return percent > 50 ? -11141257 : (percent > 20 ? -19641 : -43691);
    }

    public static class_1799 forVanillaBar(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return class_1799.field_8037;
        }
        int percent = FunTimeDurabilityUtil.percent(stack);
        if (percent < 0 || !stack.method_7963() || stack.method_7936() <= 0) {
            return stack;
        }
        class_1799 copy = stack.method_7972();
        int remaining = Math.round((float)copy.method_7936() * ((float)percent / 100.0f));
        copy.method_7974(class_3532.method_15340((int)(copy.method_7936() - remaining), (int)0, (int)copy.method_7936()));
        return copy;
    }

    private FunTimeDurabilityUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

