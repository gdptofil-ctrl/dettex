/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.feature.command.datatypes;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ru.recoin.api.feature.command.datatypes.IDatatypeContext;
import ru.recoin.api.feature.command.datatypes.IDatatypeFor;
import ru.recoin.api.feature.command.exception.CommandException;
import ru.recoin.api.feature.command.helpers.TabCompleteHelper;
import ru.recoin.api.file.ClientFile;
import ru.recoin.api.file.impl.ModuleFile;
import ru.recoin.core.Main;

public enum ConfigDataType implements IDatatypeFor<ClientFile>
{
    INSTANCE;


    @Override
    public Stream<String> tabComplete(IDatatypeContext ctx) throws CommandException {
        Stream<String> friends = this.getList().stream().map(ClientFile::getName);
        String context = ctx.getConsumer().getString();
        return new TabCompleteHelper().append(friends).filterPrefix(context).sortAlphabetically().stream();
    }

    @Override
    public ClientFile get(IDatatypeContext datatypeContext) throws CommandException {
        String username = datatypeContext.getConsumer().getString();
        return this.getList().stream().filter(s -> s.getName().equalsIgnoreCase(username)).findFirst().orElse(null);
    }

    public List<? extends ModuleFile> getList() {
        return Main.getInstance().getFileRepository().getClientFiles().stream().filter(clientFile -> clientFile instanceof ModuleFile).map(clientFile -> (ModuleFile)clientFile).collect(Collectors.toList());
    }
}

