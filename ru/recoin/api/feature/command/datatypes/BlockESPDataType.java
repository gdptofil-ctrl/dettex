/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2248
 */
package ru.recoin.api.feature.command.datatypes;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.class_2248;
import ru.recoin.api.feature.command.datatypes.IDatatypeContext;
import ru.recoin.api.feature.command.datatypes.IDatatypeFor;
import ru.recoin.api.feature.command.exception.CommandException;
import ru.recoin.api.feature.command.helpers.TabCompleteHelper;
import ru.recoin.core.Main;

public enum BlockESPDataType implements IDatatypeFor<class_2248>
{
    INSTANCE;


    @Override
    public Stream<String> tabComplete(IDatatypeContext datatypeContext) throws CommandException {
        Stream<String> blocks = this.getBlocks().stream().map(b -> b.method_9518().getString().replace(" ", "_"));
        String context = datatypeContext.getConsumer().getString();
        return new TabCompleteHelper().append(blocks).filterPrefix(context).sortAlphabetically().stream();
    }

    @Override
    public class_2248 get(IDatatypeContext datatypeContext) throws CommandException {
        String text = datatypeContext.getConsumer().getString();
        return this.getBlocks().stream().filter(s -> s.method_9518().getString().replace(" ", "_").equalsIgnoreCase(text)).findFirst().orElse(null);
    }

    private List<? extends class_2248> getBlocks() {
        return Main.getInstance().getBoxESPRepository().blocks.keySet().stream().toList();
    }
}

