/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1299
 */
package ru.recoin.api.feature.command.datatypes;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.class_1299;
import ru.recoin.api.feature.command.datatypes.IDatatypeContext;
import ru.recoin.api.feature.command.datatypes.IDatatypeFor;
import ru.recoin.api.feature.command.exception.CommandException;
import ru.recoin.api.feature.command.helpers.TabCompleteHelper;
import ru.recoin.core.Main;

public enum EntityESPDataType implements IDatatypeFor<class_1299<?>>
{
    INSTANCE;


    @Override
    public Stream<String> tabComplete(IDatatypeContext ctx) throws CommandException {
        Stream<String> ways = this.getEntities().map(s -> s.method_5897().getString().replace(" ", "_"));
        String context = ctx.getConsumer().getString();
        return new TabCompleteHelper().append(ways).filterPrefix(context).sortAlphabetically().stream();
    }

    @Override
    public class_1299<?> get(IDatatypeContext datatypeContext) throws CommandException {
        return this.findEntity(datatypeContext.getConsumer().getString()).orElse(null);
    }

    public Optional<class_1299<?>> findEntity(String text) {
        return this.getEntities().filter(s -> s.method_5897().getString().replace(" ", "_").equalsIgnoreCase(text)).findFirst();
    }

    private Stream<class_1299<?>> getEntities() {
        return Main.getInstance().getBoxESPRepository().entities.keySet().stream();
    }
}

