/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.commands.defaults;

import java.util.List;
import java.util.stream.Stream;
import ru.recoin.api.feature.command.Command;
import ru.recoin.api.feature.command.argument.IArgConsumer;
import ru.recoin.api.feature.command.exception.CommandException;

public class DebugCommand
extends Command {
    public static boolean debug = false;

    public DebugCommand() {
        super("debug", "dbg");
    }

    @Override
    public void execute(String label, IArgConsumer args2) throws CommandException {
        debug = !debug;
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args2) throws CommandException {
        return null;
    }

    @Override
    public String getShortDesc() {
        return "developer";
    }

    @Override
    public List<String> getLongDesc() {
        return List.of("Developer");
    }
}

