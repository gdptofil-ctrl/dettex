/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 */
package ru.recoin.api.feature.command.exception;

import java.util.List;
import net.minecraft.class_124;
import ru.recoin.api.feature.command.ICommand;
import ru.recoin.api.feature.command.argument.ICommandArgument;
import ru.recoin.common.QuickLogger;

public interface ICommandException
extends QuickLogger {
    public String getMessage();

    default public void handle(ICommand command, List<ICommandArgument> args2) {
        this.logDirect(this.getMessage(), class_124.field_1061);
    }
}

