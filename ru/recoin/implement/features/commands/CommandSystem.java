/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.commands;

import ru.recoin.api.feature.command.ICommandSystem;
import ru.recoin.api.feature.command.argparser.IArgParserManager;
import ru.recoin.implement.features.commands.argparser.ArgParserManager;

public enum CommandSystem implements ICommandSystem
{
    INSTANCE;


    @Override
    public IArgParserManager getParserManager() {
        return ArgParserManager.INSTANCE;
    }
}

