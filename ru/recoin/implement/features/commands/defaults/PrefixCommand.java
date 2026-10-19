/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 */
package ru.recoin.implement.features.commands.defaults;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.class_124;
import ru.recoin.api.feature.command.Command;
import ru.recoin.api.feature.command.argument.IArgConsumer;
import ru.recoin.api.feature.command.exception.CommandException;
import ru.recoin.api.feature.command.helpers.TabCompleteHelper;
import ru.recoin.common.QuickImports;
import ru.recoin.implement.features.commands.CommandDispatcher;

public class PrefixCommand
extends Command
implements QuickImports {
    protected PrefixCommand() {
        super("prefix");
    }

    @Override
    public void execute(String label, IArgConsumer args2) throws CommandException {
        if (!args2.hasAny()) {
            this.logDirect("\u0422\u0435\u043a\u0443\u0449\u0438\u0439 \u043f\u0440\u0435\u0444\u0438\u043a\u0441 \u043a\u043e\u043c\u0430\u043d\u0434: '" + CommandDispatcher.prefix + "'. \u0418\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0439 .prefix set <\u0441\u0438\u043c\u0432\u043e\u043b> \u0438\u043b\u0438 .prefix reset.", class_124.field_1080);
            return;
        }
        String arg = args2.getString().toLowerCase(Locale.US);
        if (arg.equals("set")) {
            args2.requireMin(1);
            String newPrefix = args2.getString();
            if (newPrefix.isBlank() || newPrefix.chars().anyMatch(Character::isWhitespace)) {
                this.logDirect("\u041f\u0440\u0435\u0444\u0438\u043a\u0441 \u0434\u043e\u043b\u0436\u0435\u043d \u0431\u044b\u0442\u044c \u043d\u0435\u043f\u0443\u0441\u0442\u044b\u043c \u0438 \u0431\u0435\u0437 \u043f\u0440\u043e\u0431\u0435\u043b\u043e\u0432.", class_124.field_1061);
                return;
            }
            CommandDispatcher.prefix = newPrefix;
            this.logDirect("\u0423\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d \u043f\u0440\u0435\u0444\u0438\u043a\u0441 \u043a\u043e\u043c\u0430\u043d\u0434: '" + newPrefix + "'. \u0422\u043e\u0447\u043a\u0430 \u0442\u0430\u043a\u0436\u0435 \u043f\u0440\u043e\u0434\u043e\u043b\u0436\u0438\u0442 \u0440\u0430\u0431\u043e\u0442\u0430\u0442\u044c.", class_124.field_1080);
        } else if (arg.equals("reset")) {
            CommandDispatcher.prefix = ".";
            this.logDirect("\u041f\u0440\u0435\u0444\u0438\u043a\u0441 \u043a\u043e\u043c\u0430\u043d\u0434 \u0441\u0431\u0440\u043e\u0448\u0435\u043d: '.'.", class_124.field_1080);
        } else {
            this.logDirect("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435: .prefix set <\u0441\u0438\u043c\u0432\u043e\u043b> | .prefix reset", class_124.field_1080);
        }
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args2) throws CommandException {
        if (args2.hasAny()) {
            String arg = args2.getString();
            if (arg.equalsIgnoreCase("set")) {
                return new TabCompleteHelper().sortAlphabetically().prepend("name").stream();
            }
            return new TabCompleteHelper().sortAlphabetically().prepend("set").filterPrefix(arg).stream();
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "\u0420\u045f\u0420\u0455\u0420\u00b7\u0420\u0406\u0420\u0455\u0420\u00bb\u0421\u040f\u0420\u00b5\u0421\u201a \u0420\u0458\u0420\u00b5\u0420\u0405\u0421\u040f\u0421\u201a\u0421\u040a \u0420\u0457\u0421\u0402\u0420\u00b5\u0421\u201e\u0420\u0451\u0420\u0454\u0421\u0403 \u0420\u0454\u0420\u0455\u0420\u0458\u0420\u00b0\u0420\u0405\u0420\u0491 \u0420\u0406 \u0420\u0458\u0420\u0455\u0420\u0491\u0420\u00b5";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList("\u0420\u040e \u0420\u0457\u0420\u0455\u0420\u0458\u0420\u0455\u0421\u2030\u0421\u040a\u0421\u040b \u0421\u040c\u0421\u201a\u0420\u0455\u0420\u2116 \u0420\u0454\u0420\u0455\u0420\u0458\u0420\u00b0\u0420\u0405\u0420\u0491\u0421\u2039 \u0420\u0458\u0420\u0455\u0420\u00b6\u0420\u0405\u0420\u0455 \u0420\u0451\u0420\u00b7\u0420\u0458\u0420\u00b5\u0420\u0405\u0420\u0451\u0421\u201a\u0421\u040a \u0420\u0457\u0421\u0402\u0420\u00b5\u0421\u201e\u0420\u0451\u0420\u0454\u0421\u0403 \u0420\u0454\u0420\u0455\u0420\u0458\u0420\u00b0\u0420\u0405\u0420\u0491 \u0420\u0406 \u0420\u0458\u0420\u0455\u0420\u0491\u0420\u00b5", "", "\u0420\u0098\u0421\u0403\u0420\u0457\u0420\u0455\u0420\u00bb\u0421\u040a\u0420\u00b7\u0420\u0455\u0420\u0406\u0420\u00b0\u0420\u0405\u0420\u0451\u0420\u00b5:", "> prefix set <name> - \u0421\u0453\u0421\u0403\u0421\u201a\u0420\u00b0\u0420\u0405\u0420\u00b0\u0420\u0406\u0420\u00bb\u0420\u0451\u0420\u0406\u0420\u00b0\u0420\u00b5\u0421\u201a \u0420\u0457\u0421\u0402\u0420\u00b5\u0421\u201e\u0420\u0451\u0420\u0454\u0421\u0403 \u0420\u0454\u0420\u0455\u0420\u0458\u0420\u00b0\u0420\u0405\u0420\u0491");
    }
}

