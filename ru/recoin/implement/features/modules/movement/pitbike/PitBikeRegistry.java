/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1299
 *  net.minecraft.class_1299$class_1300
 *  net.minecraft.class_1311
 *  net.minecraft.class_2378
 *  net.minecraft.class_2960
 *  net.minecraft.class_5321
 *  net.minecraft.class_7923
 *  net.minecraft.class_7924
 */
package ru.recoin.implement.features.modules.movement.pitbike;

import net.minecraft.class_1299;
import net.minecraft.class_1311;
import net.minecraft.class_2378;
import net.minecraft.class_2960;
import net.minecraft.class_5321;
import net.minecraft.class_7923;
import net.minecraft.class_7924;
import ru.recoin.implement.features.modules.movement.pitbike.PitBikeEntity;

public final class PitBikeRegistry {
    public static final class_2960 ID = class_2960.method_60655((String)"insidethesystem", (String)"pitbike");
    public static final class_5321<class_1299<?>> KEY = class_5321.method_29179((class_5321)class_7924.field_41266, (class_2960)ID);
    public static final class_1299<PitBikeEntity> TYPE = (class_1299)class_2378.method_10230((class_2378)class_7923.field_41177, (class_2960)ID, (Object)class_1299.class_1300.method_5903(PitBikeEntity::new, (class_1311)class_1311.field_17715).method_17687(0.85f, 1.2f).method_27299(10).method_27300(1).method_5905(KEY));

    private PitBikeRegistry() {
    }

    public static void initialize() {
    }
}

