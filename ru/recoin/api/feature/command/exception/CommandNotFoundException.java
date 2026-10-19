/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.feature.command.exception;

import java.util.List;
import ru.recoin.api.feature.command.ICommand;
import ru.recoin.api.feature.command.argument.ICommandArgument;
import ru.recoin.api.feature.command.exception.CommandException;
import ru.recoin.common.QuickLogger;

public class CommandNotFoundException
extends CommandException
implements QuickLogger {
    public final String command;

    public CommandNotFoundException(String command) {
        super(String.format("\u0420\u0459\u0420\u0455\u0420\u0458\u0420\u00b0\u0420\u0405\u0420\u0491\u0420\u00b0 \u0420\u0405\u0420\u00b5 \u0420\u0405\u0420\u00b0\u0420\u2116\u0420\u0491\u0420\u00b5\u0420\u0405\u0420\u00b0: %s", command));
        this.command = command;
    }

    @Override
    public void handle(ICommand command, List<ICommandArgument> args2) {
        this.logDirect(this.getMessage());
    }
}

