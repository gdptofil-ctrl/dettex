/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.feature.command.exception;

import ru.recoin.api.feature.command.exception.CommandErrorMessageException;

public class CommandInvalidStateException
extends CommandErrorMessageException {
    public CommandInvalidStateException(String reason) {
        super(reason);
    }
}

