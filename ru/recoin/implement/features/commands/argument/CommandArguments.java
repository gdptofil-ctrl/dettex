/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.commands.argument;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ru.recoin.api.feature.command.argument.ICommandArgument;
import ru.recoin.implement.features.commands.argument.CommandArgument;

public final class CommandArguments {
    private static final Pattern ARG_PATTERN = Pattern.compile("\\S+");

    private CommandArguments() {
    }

    public static List<ICommandArgument> from(String string, boolean preserveEmptyLast) {
        ArrayList<ICommandArgument> args2 = new ArrayList<ICommandArgument>();
        Matcher argMatcher = ARG_PATTERN.matcher(string);
        int lastEnd = -1;
        while (argMatcher.find()) {
            args2.add(new CommandArgument(args2.size(), argMatcher.group(), string.substring(argMatcher.start())));
            lastEnd = argMatcher.end();
        }
        if (preserveEmptyLast && lastEnd < string.length()) {
            args2.add(new CommandArgument(args2.size(), "", ""));
        }
        return args2;
    }

    public static List<ICommandArgument> from(String string) {
        return CommandArguments.from(string, false);
    }

    public static CommandArgument unknown() {
        return new CommandArgument(-1, "<unknown>", "");
    }
}

