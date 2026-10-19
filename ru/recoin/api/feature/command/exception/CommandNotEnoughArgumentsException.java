/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.feature.command.exception;

import ru.recoin.api.feature.command.exception.CommandErrorMessageException;

public class CommandNotEnoughArgumentsException
extends CommandErrorMessageException {
    public CommandNotEnoughArgumentsException(int minArgs) {
        super(String.format("Not enough arguments (expected at least %d)", minArgs));
    }
}

