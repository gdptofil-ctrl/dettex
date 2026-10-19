/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.feature.command.exception;

import java.util.List;
import ru.recoin.api.feature.command.ICommand;
import ru.recoin.api.feature.command.argument.ICommandArgument;
import ru.recoin.api.feature.command.exception.ICommandException;
import ru.recoin.common.QuickLogger;

public class CommandUnhandledException
extends RuntimeException
implements ICommandException,
QuickLogger {
    public CommandUnhandledException(String message) {
        super(message);
    }

    public CommandUnhandledException(Throwable cause) {
        super(cause);
    }

    @Override
    public void handle(ICommand command, List<ICommandArgument> args2) {
    }
}

