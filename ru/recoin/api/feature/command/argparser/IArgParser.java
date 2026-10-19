/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.feature.command.argparser;

import ru.recoin.api.feature.command.argument.ICommandArgument;

public interface IArgParser<T> {
    public Class<T> getTarget();

    public static interface Stated<T, S>
    extends IArgParser<T> {
        public Class<S> getStateType();

        public T parseArg(ICommandArgument var1, S var2) throws Exception;
    }

    public static interface Stateless<T>
    extends IArgParser<T> {
        public T parseArg(ICommandArgument var1) throws Exception;
    }
}

