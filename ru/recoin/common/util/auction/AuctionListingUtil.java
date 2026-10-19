/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_2561
 */
package ru.recoin.common.util.auction;

import java.util.List;
import java.util.Locale;
import net.minecraft.class_124;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2561;

public final class AuctionListingUtil {
    private AuctionListingUtil() {
    }

    public static boolean isUnavailable(class_1799 stack, List<class_2561> tooltip) {
        if (stack.method_7960() || stack.method_31574(class_1802.field_8298)) {
            return true;
        }
        for (class_2561 line : tooltip) {
            String value = class_124.method_539((String)line.getString());
            if (value == null || !(value = value.toLowerCase(Locale.ROOT).replace('\u0451', '\u0435')).contains("\u0442\u043e\u0432\u0430\u0440 \u043d\u0435 \u0430\u043a\u0442\u0443\u0430\u043b\u0435\u043d") && !value.contains("\u043f\u0440\u0438\u0447\u0438\u043d\u0430: \u043f\u0440\u043e\u0434\u0430\u043d\u043e")) continue;
            return true;
        }
        return false;
    }
}

