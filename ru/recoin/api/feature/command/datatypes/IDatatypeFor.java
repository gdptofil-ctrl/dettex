/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.feature.command.datatypes;

import ru.recoin.api.feature.command.datatypes.IDatatype;
import ru.recoin.api.feature.command.datatypes.IDatatypeContext;
import ru.recoin.api.feature.command.exception.CommandException;

public interface IDatatypeFor<T>
extends IDatatype {
    public T get(IDatatypeContext var1) throws CommandException;
}

