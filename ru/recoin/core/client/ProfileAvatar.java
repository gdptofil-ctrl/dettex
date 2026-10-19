/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2960
 */
package ru.recoin.core.client;

import net.minecraft.class_2960;

public final class ProfileAvatar {
    private static final class_2960 FALLBACK = class_2960.method_60655((String)"recoin", (String)"textures/profile/default-avatar.png");

    private ProfileAvatar() {
    }

    public static class_2960 getTexture() {
        return FALLBACK;
    }
}

