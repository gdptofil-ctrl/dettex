/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.feature.command.exception;

import ru.recoin.api.feature.command.exception.CommandUnhandledException;

public class CommandNoParserForTypeException
extends CommandUnhandledException {
    public CommandNoParserForTypeException(Class<?> klass) {
        super(String.format("Could not find a handler for type %s", klass.getSimpleName()));
    }
}

