/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_268
 *  net.minecraft.class_640
 */
package ru.recoin.api.feature.command.datatypes;

import java.util.Collection;
import java.util.stream.Stream;
import net.minecraft.class_268;
import net.minecraft.class_640;
import ru.recoin.api.feature.command.datatypes.IDatatypeContext;
import ru.recoin.api.feature.command.datatypes.IDatatypeFor;
import ru.recoin.api.feature.command.exception.CommandException;
import ru.recoin.api.feature.command.helpers.TabCompleteHelper;

public enum TabPlayerDataType implements IDatatypeFor<class_268>
{
    INSTANCE;


    @Override
    public Stream<String> tabComplete(IDatatypeContext ctx) throws CommandException {
        String prefix = ctx.getConsumer().getString();
        if (mc.method_1562() == null) {
            return Stream.empty();
        }
        return new TabCompleteHelper().append(mc.method_1562().method_2880().stream().map(class_640::method_2966).map(profile -> profile.name()).filter(name -> name != null && !name.isBlank()).distinct().limit(500L)).filterPrefix(prefix).sortAlphabetically().stream();
    }

    @Override
    public class_268 get(IDatatypeContext datatypeContext) throws CommandException {
        String username = datatypeContext.getConsumer().getString();
        return this.getTeam().stream().filter(s -> s.method_1197().equalsIgnoreCase(username)).findFirst().orElse(null);
    }

    public Collection<class_268> getTeam() {
        return TabPlayerDataType.mc.field_1687.method_8428().method_1159();
    }
}

