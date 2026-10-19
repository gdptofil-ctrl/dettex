/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_1304
 *  net.minecraft.class_1322$class_1323
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_5134
 *  net.minecraft.class_9285
 *  net.minecraft.class_9285$class_9287
 *  net.minecraft.class_9334
 */
package ru.recoin.common.util.item;

import java.util.Locale;
import net.minecraft.class_124;
import net.minecraft.class_1304;
import net.minecraft.class_1322;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_5134;
import net.minecraft.class_9285;
import net.minecraft.class_9334;

public final class VortexTalismanMatcher {
    private VortexTalismanMatcher() {
    }

    public static boolean matches(class_1799 stack) {
        if (stack == null || stack.method_7960() || !stack.method_31574(class_1802.field_8288)) {
            return false;
        }
        String name = class_124.method_539((String)stack.method_7964().getString()).toLowerCase(Locale.ROOT).replace('\u0451', '\u0435').replace('\u00a0', ' ').replace('\u202f', ' ').replaceAll("\\s+", " ");
        if (name.contains("\u0442\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u0432\u0438\u0445\u0440\u044f") || name.contains("vortex talisman")) {
            return true;
        }
        boolean health = false;
        boolean speed = false;
        boolean attackSpeed = false;
        class_9285 modifiers = (class_9285)stack.method_58695(class_9334.field_49636, (Object)class_9285.field_49326);
        for (class_9285.class_9287 entry : modifiers.comp_2393()) {
            if (!entry.comp_2397().method_57286(class_1304.field_6171)) continue;
            class_1322.class_1323 operation = entry.comp_2396().comp_2450();
            double value = entry.comp_2396().comp_2449();
            if (entry.comp_2395().equals((Object)class_5134.field_23716) && operation == class_1322.class_1323.field_6328 && Math.abs(value - 2.0) < 0.001) {
                health = true;
            }
            if (operation != class_1322.class_1323.field_6330 && operation != class_1322.class_1323.field_6331 || !(Math.abs(value - 0.15) < 0.001)) continue;
            if (entry.comp_2395().equals((Object)class_5134.field_23719)) {
                speed = true;
            }
            if (!entry.comp_2395().equals((Object)class_5134.field_23723)) continue;
            attackSpeed = true;
        }
        return health && speed && attackSpeed;
    }
}

