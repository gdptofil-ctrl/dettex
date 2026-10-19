/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1309
 *  net.minecraft.class_1792
 *  net.minecraft.class_1796
 *  net.minecraft.class_1796$class_1797
 *  net.minecraft.class_1799
 *  net.minecraft.class_1839
 *  net.minecraft.class_2960
 */
package ru.recoin.common.util.item;

import net.minecraft.class_1309;
import net.minecraft.class_1792;
import net.minecraft.class_1796;
import net.minecraft.class_1799;
import net.minecraft.class_1839;
import net.minecraft.class_2960;
import ru.recoin.common.QuickImports;

public final class ItemUtil
implements QuickImports {
    public static int maxUseTick(class_1792 item) {
        return ItemUtil.maxUseTick(item.method_7854());
    }

    public static int maxUseTick(class_1799 stack) {
        return switch (stack.method_7976()) {
            case class_1839.field_8950, class_1839.field_8946 -> 32;
            case class_1839.field_8947, class_1839.field_8951 -> 10;
            case class_1839.field_8953 -> 20;
            case class_1839.field_8949 -> 0;
            default -> stack.method_7935((class_1309)ItemUtil.mc.field_1724);
        };
    }

    public static float getCooldownProgress(class_1792 item) {
        class_1796 cooldownManager = ItemUtil.mc.field_1724.method_7357();
        class_1799 stack = item.method_7854();
        class_1796.class_1797 entry = (class_1796.class_1797)cooldownManager.field_8024.get(item);
        if (entry == null) {
            class_2960 group = cooldownManager.method_62836(stack);
            entry = (class_1796.class_1797)cooldownManager.field_8024.get(group);
        }
        if (entry == null) {
            return 0.0f;
        }
        return Math.max(0.0f, (float)(entry.comp_3084 - cooldownManager.field_8025) / 20.0f);
    }

    private ItemUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

