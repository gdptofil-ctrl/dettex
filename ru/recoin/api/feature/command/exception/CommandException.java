/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.feature.command.exception;

import ru.recoin.api.feature.command.exception.ICommandException;

public abstract class CommandException
extends Exception
implements ICommandException {
    protected CommandException(String reason) {
        super(reason);
    }

    protected CommandException(String reason, Throwable cause) {
        super(reason, cause);
    }
}

