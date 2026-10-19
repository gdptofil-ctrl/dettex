/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.feature.command.datatypes;

import java.util.stream.Stream;
import ru.recoin.api.feature.command.datatypes.IDatatypeContext;
import ru.recoin.api.feature.command.exception.CommandException;
import ru.recoin.common.QuickImports;

public interface IDatatype
extends QuickImports {
    public Stream<String> tabComplete(IDatatypeContext var1) throws CommandException;
}

