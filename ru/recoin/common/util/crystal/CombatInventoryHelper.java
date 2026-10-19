/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10192
 *  net.minecraft.class_1292
 *  net.minecraft.class_1294
 *  net.minecraft.class_1304
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_1713
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1887
 *  net.minecraft.class_1893
 *  net.minecraft.class_2596
 *  net.minecraft.class_2680
 *  net.minecraft.class_2815
 *  net.minecraft.class_3486
 *  net.minecraft.class_3489
 *  net.minecraft.class_5134
 *  net.minecraft.class_5321
 *  net.minecraft.class_6880
 *  net.minecraft.class_9304
 *  net.minecraft.class_9334
 */
package ru.recoin.common.util.crystal;

import net.minecraft.class_10192;
import net.minecraft.class_1292;
import net.minecraft.class_1294;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1887;
import net.minecraft.class_1893;
import net.minecraft.class_2596;
import net.minecraft.class_2680;
import net.minecraft.class_2815;
import net.minecraft.class_3486;
import net.minecraft.class_3489;
import net.minecraft.class_5134;
import net.minecraft.class_5321;
import net.minecraft.class_6880;
import net.minecraft.class_9304;
import net.minecraft.class_9334;
import ru.recoin.common.QuickImports;

public final class CombatInventoryHelper
implements QuickImports {
    public static int findBestChestplate() {
        if (CombatInventoryHelper.mc.field_1724 == null) {
            return -1;
        }
        int count = -1;
        double distance = -1.0;
        for (int index = 0; index < 36; ++index) {
            double distance2;
            class_10192 value;
            class_1799 stack = CombatInventoryHelper.mc.field_1724.method_31548().method_5438(index);
            if (!stack.method_31573(class_3489.field_48296) || (value = (class_10192)stack.method_58694(class_9334.field_54196)) == null || value.comp_3174() != class_1304.field_6174) continue;
            int count2 = CombatInventoryHelper.getEnchantmentLevel(stack, (class_5321<class_1887>)class_1893.field_9111);
            int count3 = CombatInventoryHelper.getEnchantmentLevel(stack, (class_5321<class_1887>)class_1893.field_9119);
            int count4 = CombatInventoryHelper.getEnchantmentLevel(stack, (class_5321<class_1887>)class_1893.field_9101);
            int count5 = CombatInventoryHelper.getChestplateTier(stack.method_7909());
            int count6 = stack.method_7936();
            int count7 = stack.method_7919();
            double distance3 = (double)count5 * 10000.0 + (double)count2 * 100.0 + (double)count3 * 10.0 + (double)(count4 > 0 ? 1 : 0) + (distance2 = count6 == 0 ? 1.0 : (double)(count6 - count7) / (double)count6) * 10.0;
            if (!(distance3 > distance)) continue;
            distance = distance3;
            count = index;
        }
        return count;
    }

    public static int getChestplateTier(class_1792 item) {
        if (item == class_1802.field_22028) {
            return 5;
        }
        if (item == class_1802.field_8058) {
            return 4;
        }
        if (item == class_1802.field_8523) {
            return 3;
        }
        if (item == class_1802.field_8678) {
            return 2;
        }
        if (item == class_1802.field_8873) {
            return 2;
        }
        return item == class_1802.field_8577 ? 1 : 0;
    }

    public static int getEnchantmentLevel(class_1799 stack, class_5321<class_1887> value) {
        class_9304 value2 = (class_9304)stack.method_58695(class_9334.field_49633, (Object)class_9304.field_49385);
        for (class_6880 value3 : value2.method_57534()) {
            if (!value3.method_40225(value)) continue;
            return value2.method_57536(value3);
        }
        return 0;
    }

    public static float getMiningSpeed(class_2680 state, class_1799 stack) {
        float amount = stack.method_7924(state);
        if (amount > 1.0f) {
            amount += (float)CombatInventoryHelper.mc.field_1724.method_45325(class_5134.field_51581);
        }
        if (class_1292.method_5576((class_1309)CombatInventoryHelper.mc.field_1724)) {
            amount *= 1.0f + (float)(class_1292.method_5575((class_1309)CombatInventoryHelper.mc.field_1724) + 1) * 0.2f;
        }
        if (CombatInventoryHelper.mc.field_1724.method_6059(class_1294.field_5901)) {
            amount *= (switch (CombatInventoryHelper.mc.field_1724.method_6112(class_1294.field_5901).method_5578()) {
                case 0 -> 0.3f;
                case 1 -> 0.09f;
                case 2 -> 0.0027f;
                default -> 8.1E-4f;
            });
        }
        amount *= (float)CombatInventoryHelper.mc.field_1724.method_45325(class_5134.field_49076);
        if (CombatInventoryHelper.mc.field_1724.method_5777(class_3486.field_15517)) {
            amount *= (float)CombatInventoryHelper.mc.field_1724.method_5996(class_5134.field_51576).method_6194();
        }
        if (!CombatInventoryHelper.mc.field_1724.method_24828()) {
            amount /= 5.0f;
        }
        return amount;
    }

    public static boolean swapSlots(int count, int count2, int count3, class_1713 value) {
        CombatInventoryHelper.mc.field_1761.method_2906(0, count, count3, value, (class_1657)CombatInventoryHelper.mc.field_1724);
        if (count2 != -1) {
            CombatInventoryHelper.mc.field_1761.method_2906(0, count2, count3, value, (class_1657)CombatInventoryHelper.mc.field_1724);
            CombatInventoryHelper.mc.field_1761.method_2906(0, count, count3, value, (class_1657)CombatInventoryHelper.mc.field_1724);
        }
        if (mc.method_1558() == null || !CombatInventoryHelper.mc.method_1558().field_3761.contains("bravo")) {
            CombatInventoryHelper.mc.field_1724.field_3944.method_52787((class_2596)new class_2815(0));
        }
        return true;
    }
}

