/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.feature.command.datatypes;

import java.util.List;
import java.util.stream.Stream;
import ru.recoin.api.feature.command.datatypes.IDatatypeContext;
import ru.recoin.api.feature.command.datatypes.IDatatypeFor;
import ru.recoin.api.feature.command.exception.CommandException;
import ru.recoin.api.feature.command.helpers.TabCompleteHelper;
import ru.recoin.api.feature.module.Module;
import ru.recoin.core.Main;

public enum ModuleDataType implements IDatatypeFor<Module>
{
    INSTANCE;


    @Override
    public Stream<String> tabComplete(IDatatypeContext datatypeContext) throws CommandException {
        Stream<String> source = this.getModules().stream().map(Module::getName);
        String context = datatypeContext.getConsumer().getString();
        return new TabCompleteHelper().append(source).filterPrefix(context).sortAlphabetically().stream();
    }

    @Override
    public Module get(IDatatypeContext datatypeContext) throws CommandException {
        String name = datatypeContext.getConsumer().getString();
        return this.getModules().stream().filter(s -> s.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    private List<? extends Module> getModules() {
        return Main.getInstance().getModuleRepository().modules();
    }
}

