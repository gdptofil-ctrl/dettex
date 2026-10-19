/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.feature.command.exception;

import ru.recoin.api.feature.command.exception.CommandErrorMessageException;

public class CommandTooManyArgumentsException
extends CommandErrorMessageException {
    public CommandTooManyArgumentsException(int maxArgs) {
        super(String.format("Too many arguments (expected at most %d)", maxArgs));
    }
}

