/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.feature.command;

import java.util.List;
import java.util.stream.Stream;
import ru.recoin.api.feature.command.argument.IArgConsumer;
import ru.recoin.api.feature.command.exception.CommandException;
import ru.recoin.common.QuickLogger;

public interface ICommand
extends QuickLogger {
    public void execute(String var1, IArgConsumer var2) throws CommandException;

    public Stream<String> tabComplete(String var1, IArgConsumer var2) throws CommandException;

    public String getShortDesc();

    public List<String> getLongDesc();

    public List<String> getNames();

    default public boolean hiddenFromHelp() {
        return false;
    }
}

