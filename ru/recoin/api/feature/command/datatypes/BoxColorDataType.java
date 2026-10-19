/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.feature.command.datatypes;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import ru.recoin.api.feature.command.argument.ICommandArgument;
import ru.recoin.api.feature.command.datatypes.IDatatypeContext;
import ru.recoin.api.feature.command.datatypes.IDatatypeFor;
import ru.recoin.api.feature.command.exception.CommandException;
import ru.recoin.api.feature.command.exception.CommandInvalidTypeException;
import ru.recoin.api.feature.command.helpers.TabCompleteHelper;

public enum BoxColorDataType implements IDatatypeFor<Integer>
{
    INSTANCE;

    private static final Map<String, Integer> COLORS;

    private static void add(String russian, String english, int color) {
        COLORS.put(russian, color);
        COLORS.put(english, color);
    }

    @Override
    public Stream<String> tabComplete(IDatatypeContext context) throws CommandException {
        String prefix = context.getConsumer().getString();
        return new TabCompleteHelper().append(COLORS.keySet().stream().filter(name -> !name.matches("[a-z]+"))).filterPrefix(prefix).sortAlphabetically().stream();
    }

    @Override
    public Integer get(IDatatypeContext context) throws CommandException {
        ICommandArgument argument = context.getConsumer().get();
        String value = BoxColorDataType.normalize(argument.getValue());
        Integer color = COLORS.get(value);
        if (color != null) {
            return color;
        }
        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException exception) {
            throw new CommandInvalidTypeException(argument, "\u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u0446\u0432\u0435\u0442\u0430", argument.getValue(), exception);
        }
    }

    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replace('\u0451', '\u0435');
    }

    static {
        COLORS = new LinkedHashMap<String, Integer>();
        BoxColorDataType.add("\u043a\u0440\u0430\u0441\u043d\u044b\u0439", "red", -65536);
        BoxColorDataType.add("\u0437\u0435\u043b\u0435\u043d\u044b\u0439", "green", -16711936);
        BoxColorDataType.add("\u0441\u0438\u043d\u0438\u0439", "blue", -16776961);
        BoxColorDataType.add("\u0433\u043e\u043b\u0443\u0431\u043e\u0439", "cyan", -16711681);
        BoxColorDataType.add("\u0436\u0435\u043b\u0442\u044b\u0439", "yellow", -256);
        BoxColorDataType.add("\u043e\u0440\u0430\u043d\u0436\u0435\u0432\u044b\u0439", "orange", Short.MIN_VALUE);
        BoxColorDataType.add("\u0444\u0438\u043e\u043b\u0435\u0442\u043e\u0432\u044b\u0439", "purple", -5614081);
        BoxColorDataType.add("\u0440\u043e\u0437\u043e\u0432\u044b\u0439", "pink", -43606);
        BoxColorDataType.add("\u0431\u0435\u043b\u044b\u0439", "white", -1);
        BoxColorDataType.add("\u0447\u0435\u0440\u043d\u044b\u0439", "black", -16777216);
        BoxColorDataType.add("\u0441\u0435\u0440\u044b\u0439", "gray", -8355712);
        COLORS.put("grey", -8355712);
    }
}

