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
import ru.recoin.api.repository.macro.Macro;
import ru.recoin.core.Main;

public enum MacroDataType implements IDatatypeFor<Macro>
{
    INSTANCE;


    @Override
    public Stream<String> tabComplete(IDatatypeContext datatypeContext) throws CommandException {
        Stream<String> macros = this.getMacro().stream().map(Macro::name);
        String context = datatypeContext.getConsumer().getString();
        return new TabCompleteHelper().append(macros).filterPrefix(context).sortAlphabetically().stream();
    }

    @Override
    public Macro get(IDatatypeContext datatypeContext) throws CommandException {
        String username = datatypeContext.getConsumer().getString();
        return this.getMacro().stream().filter(s -> s.name().equalsIgnoreCase(username)).findFirst().orElse(null);
    }

    private List<? extends Macro> getMacro() {
        return Main.getInstance().getMacroRepository().macroList;
    }
}

