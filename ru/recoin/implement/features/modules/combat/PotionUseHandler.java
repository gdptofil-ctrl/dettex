/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1661
 *  net.minecraft.class_1735
 *  net.minecraft.class_1799
 */
package ru.recoin.implement.features.modules.combat;

import java.util.Comparator;
import java.util.function.Predicate;
import net.minecraft.class_1268;
import net.minecraft.class_1661;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.entity.ScreenWalkInventorySync;

public final class PotionUseHandler
implements QuickImports {
    public boolean use(Predicate<class_1799> matcher) {
        if (PotionUseHandler.mc.field_1724 == null || PotionUseHandler.mc.field_1761 == null) {
            return false;
        }
        class_1735 slot = PlayerInventoryUtil.getSlot(candidate -> candidate.field_7871 instanceof class_1661 && matcher.test(candidate.method_7677()), Comparator.comparingInt(PotionUseHandler::priority));
        if (slot == null) {
            return false;
        }
        Runnable interact = () -> PlayerIntersectionUtil.interactItem(class_1268.field_5808);
        ScreenWalkInventorySync.swapUseRestore(slot, interact);
        return true;
    }

    private static int priority(class_1735 slot) {
        if (PlayerInventoryUtil.isBundleBackedSlot(slot)) {
            return 0;
        }
        return slot.method_34266() >= 0 && slot.method_34266() < 9 ? 2 : 1;
    }
}

