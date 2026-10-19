/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.feature.module;

public enum ModuleCategory {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    RENDER("Render"),
    PLAYER("Player"),
    MISC("Misc");

    final String readableName;

    private ModuleCategory(String readableName) {
        this.readableName = readableName;
    }

    public String getReadableName() {
        return this.readableName;
    }
}

