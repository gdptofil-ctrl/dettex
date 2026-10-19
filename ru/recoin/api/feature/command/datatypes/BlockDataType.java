/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2246
 *  net.minecraft.class_2248
 *  net.minecraft.class_7923
 */
package ru.recoin.api.feature.command.datatypes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_7923;
import ru.recoin.api.feature.command.datatypes.IDatatypeContext;
import ru.recoin.api.feature.command.datatypes.IDatatypeFor;
import ru.recoin.api.feature.command.exception.CommandException;
import ru.recoin.api.feature.command.helpers.TabCompleteHelper;

public enum BlockDataType implements IDatatypeFor<class_2248>
{
    INSTANCE;


    @Override
    public Stream<String> tabComplete(IDatatypeContext ctx) throws CommandException {
        Stream<String> ways = this.streamBlocks().map(s -> s.method_9518().getString().replace(" ", "_"));
        String context = ctx.getConsumer().getString();
        return new TabCompleteHelper().append(ways).filterPrefix(context).sortAlphabetically().stream();
    }

    @Override
    public class_2248 get(IDatatypeContext datatypeContext) throws CommandException {
        return this.findBlock(datatypeContext.getConsumer().getString()).orElse(null);
    }

    public Optional<class_2248> findBlock(String text) {
        return this.streamBlocks().filter(s -> s.method_9518().getString().replace(" ", "_").equalsIgnoreCase(text)).findFirst();
    }

    public Stream<class_2248> streamBlocks() {
        return class_7923.field_41175.method_10220().filter(this::blackList);
    }

    public boolean blackList(class_2248 block) {
        return !List.of(class_2246.field_10124, class_2246.field_10543, class_2246.field_10243, class_2246.field_10382).contains(block);
    }
}

