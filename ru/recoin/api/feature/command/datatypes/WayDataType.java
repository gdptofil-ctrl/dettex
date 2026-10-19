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
import ru.recoin.api.repository.way.Way;
import ru.recoin.core.Main;

public enum WayDataType implements IDatatypeFor<Way>
{
    INSTANCE;


    @Override
    public Stream<String> tabComplete(IDatatypeContext datatypeContext) throws CommandException {
        Stream<String> ways = this.getWay().stream().map(Way::name);
        String context = datatypeContext.getConsumer().getString();
        return new TabCompleteHelper().append(ways).filterPrefix(context).sortAlphabetically().stream();
    }

    @Override
    public Way get(IDatatypeContext datatypeContext) throws CommandException {
        String text = datatypeContext.getConsumer().getString();
        return this.getWay().stream().filter(s -> s.name().equalsIgnoreCase(text)).findFirst().orElse(null);
    }

    private List<? extends Way> getWay() {
        return Main.getInstance().getWayRepository().wayList;
    }
}

