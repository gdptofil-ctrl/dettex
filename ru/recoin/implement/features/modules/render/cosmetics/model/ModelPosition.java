/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.render.cosmetics.model;

public enum ModelPosition {
    FREE(-1),
    BODY(1),
    HEAD(3),
    ABOVE_HEAD(-1),
    RIGHT_ARM(-1),
    LEFT_ARM(-1),
    RIGHT_LEG(2),
    LEFT_LEG(0);

    private final int armorSlot;

    private ModelPosition(int armorSlot) {
        this.armorSlot = armorSlot;
    }

    public int getId() {
        return this.ordinal();
    }

    public int getArmorSlot() {
        return this.armorSlot;
    }

    public static ModelPosition getById(int id) {
        return id >= 0 && id < ModelPosition.values().length ? ModelPosition.values()[id] : BODY;
    }
}

