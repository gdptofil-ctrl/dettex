/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.feature.command;

import ru.recoin.api.feature.command.argparser.IArgParserManager;

public interface ICommandSystem {
    public IArgParserManager getParserManager();
}

