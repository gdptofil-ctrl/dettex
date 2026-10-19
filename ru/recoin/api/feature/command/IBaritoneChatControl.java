/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.feature.command;

import java.util.UUID;

public interface IBaritoneChatControl {
    public static final String FORCE_COMMAND_PREFIX = String.format("<<%s>>", UUID.randomUUID());
}

