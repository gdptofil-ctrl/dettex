/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.feature.command.argparser;

import ru.recoin.api.feature.command.argparser.IArgParser;
import ru.recoin.api.feature.command.argument.ICommandArgument;
import ru.recoin.api.feature.command.exception.CommandInvalidTypeException;
import ru.recoin.api.feature.command.registry.Registry;

public interface IArgParserManager {
    public <T> IArgParser.Stateless<T> getParserStateless(Class<T> var1);

    public <T, S> IArgParser.Stated<T, S> getParserStated(Class<T> var1, Class<S> var2);

    public <T> T parseStateless(Class<T> var1, ICommandArgument var2) throws CommandInvalidTypeException;

    public <T, S> T parseStated(Class<T> var1, Class<S> var2, ICommandArgument var3, S var4) throws CommandInvalidTypeException;

    public Registry<IArgParser> getRegistry();
}

