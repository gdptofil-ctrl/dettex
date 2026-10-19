/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1304
 *  net.minecraft.class_1309
 *  net.minecraft.class_1799
 */
package ru.recoin.common.util.entity;

import java.util.List;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1799;

public final class EntityCompat {
    private EntityCompat() {
    }

    public static List<class_1799> getArmorItems(class_1309 entity) {
        return List.of(entity.method_6118(class_1304.field_6166), entity.method_6118(class_1304.field_6172), entity.method_6118(class_1304.field_6174), entity.method_6118(class_1304.field_6169));
    }

    public static List<class_1799> getHandItems(class_1309 entity) {
        return List.of(entity.method_6118(class_1304.field_6173), entity.method_6118(class_1304.field_6171));
    }

    public static List<class_1799> getEquippedItems(class_1309 entity) {
        return List.of(entity.method_6118(class_1304.field_6173), entity.method_6118(class_1304.field_6171), entity.method_6118(class_1304.field_6166), entity.method_6118(class_1304.field_6172), entity.method_6118(class_1304.field_6174), entity.method_6118(class_1304.field_6169));
    }
}

