/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_3545
 */
package ru.recoin.api.feature.command.manager;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.class_3545;
import ru.recoin.api.feature.command.ICommand;
import ru.recoin.api.feature.command.argument.ICommandArgument;
import ru.recoin.api.feature.command.registry.Registry;

public interface ICommandManager {
    public Registry<ICommand> getRegistry();

    public ICommand getCommand(String var1);

    public boolean execute(String var1);

    public boolean execute(class_3545<String, List<ICommandArgument>> var1);

    public Stream<String> tabComplete(class_3545<String, List<ICommandArgument>> var1);

    public Stream<String> tabComplete(String var1);
}

