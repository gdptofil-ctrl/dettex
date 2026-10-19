/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.feature.command.datatypes;

import ru.recoin.api.feature.command.datatypes.IDatatype;
import ru.recoin.api.feature.command.datatypes.IDatatypeContext;
import ru.recoin.api.feature.command.exception.CommandException;

public interface IDatatypePost<T, O>
extends IDatatype {
    public T apply(IDatatypeContext var1, O var2) throws CommandException;
}

