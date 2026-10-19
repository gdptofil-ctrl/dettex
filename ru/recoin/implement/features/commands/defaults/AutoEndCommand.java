/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 */
package ru.recoin.implement.features.commands.defaults;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.class_124;
import ru.recoin.api.feature.command.Command;
import ru.recoin.api.feature.command.argument.IArgConsumer;
import ru.recoin.api.feature.command.exception.CommandException;
import ru.recoin.implement.features.modules.misc.AutoEnd;

public final class AutoEndCommand
extends Command {
    public AutoEndCommand() {
        super("autoend");
    }

    @Override
    public void execute(String label, IArgConsumer args2) throws CommandException {
        String action;
        AutoEnd module = AutoEnd.getInstance();
        if (module == null) {
            this.logDirect("[AutoEnd] \u041c\u043e\u0434\u0443\u043b\u044c \u043d\u0435 \u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d", class_124.field_1061);
            return;
        }
        if (!args2.hasAny()) {
            this.printUsage(module);
            return;
        }
        switch (action = args2.getString().toLowerCase(Locale.ROOT)) {
            case "add": {
                args2.requireMin(1);
                int first = this.parse(args2.getString());
                if (args2.hasAny()) {
                    int second = this.parse(args2.getString());
                    args2.requireExactly(0);
                    if (!module.configure(first, second)) {
                        this.logDirect("[AutoEnd] \u041d\u0443\u0436\u043d\u044b \u0434\u0432\u0435 \u0440\u0430\u0437\u043d\u044b\u0435 \u0430\u043d\u0430\u0440\u0445\u0438\u0438 \u043e\u0442 101 \u0434\u043e 905", class_124.field_1061);
                        return;
                    }
                    this.logDirect("[AutoEnd] \u041e\u0441\u043d\u043e\u0432\u043d\u0430\u044f \u0430\u043d\u0430\u0440\u0445\u0438\u044f: " + first + ", \u042d\u043d\u0434-\u0430\u043d\u0430\u0440\u0445\u0438\u044f: " + second, class_124.field_1060);
                    return;
                }
                int added = module.addAnarchy(first);
                if (added == 1) {
                    this.logDirect("[AutoEnd] \u0414\u043e\u0431\u0430\u0432\u043b\u0435\u043d\u0430 \u043e\u0441\u043d\u043e\u0432\u043d\u0430\u044f \u0430\u043d\u0430\u0440\u0445\u0438\u044f: " + first, class_124.field_1060);
                    break;
                }
                if (added == 2) {
                    this.logDirect("[AutoEnd] \u0414\u043e\u0431\u0430\u0432\u043b\u0435\u043d\u0430 \u042d\u043d\u0434-\u0430\u043d\u0430\u0440\u0445\u0438\u044f: " + first, class_124.field_1060);
                    break;
                }
                this.logDirect("[AutoEnd] \u041d\u043e\u043c\u0435\u0440 \u0434\u043e\u043b\u0436\u0435\u043d \u0431\u044b\u0442\u044c 101\u2013905; \u0435\u0441\u043b\u0438 \u043e\u0431\u0435 \u0430\u043d\u0430\u0440\u0445\u0438\u0438 \u0443\u0436\u0435 \u0437\u0430\u0434\u0430\u043d\u044b, \u0443\u043a\u0430\u0436\u0438 \u0438\u0445 \u0441\u0440\u0430\u0437\u0443 \u0434\u0432\u0443\u043c\u044f \u0447\u0438\u0441\u043b\u0430\u043c\u0438", class_124.field_1061);
                break;
            }
            case "start": {
                args2.requireExactly(0);
                module.setState(true);
                this.logDirect("[AutoEnd] \u0417\u0430\u043f\u0443\u0449\u0435\u043d: " + module.status(), class_124.field_1060);
                break;
            }
            case "stop": {
                args2.requireExactly(0);
                module.setState(false);
                this.logDirect("[AutoEnd] \u041e\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d", class_124.field_1054);
                break;
            }
            case "status": {
                args2.requireExactly(0);
                this.logDirect("[AutoEnd] " + module.status() + ", \u043c\u043e\u0434\u0443\u043b\u044c=" + (module.isState() ? "\u0432\u043a\u043b\u044e\u0447\u0451\u043d" : "\u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d"));
                break;
            }
            default: {
                this.printUsage(module);
            }
        }
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args2) throws CommandException {
        if (!args2.hasAny()) {
            return Stream.of("add", "start", "stop", "status");
        }
        String value = args2.getString().toLowerCase(Locale.ROOT);
        if (!args2.hasAny()) {
            return Stream.of("add", "start", "stop", "status").filter(action -> action.startsWith(value));
        }
        return Stream.empty();
    }

    private void printUsage(AutoEnd module) {
        this.logDirect("[AutoEnd] .autoend add <\u043d\u043e\u043c\u0435\u0440> \u0438\u043b\u0438 .autoend add <\u043e\u0441\u043d\u043e\u0432\u043d\u0430\u044f> <\u044d\u043d\u0434>");
        this.logDirect("[AutoEnd] .autoend start | stop | status");
        this.logDirect("[AutoEnd] \u0414\u0438\u0430\u043f\u0430\u0437\u043e\u043d \u0430\u043d\u0430\u0440\u0445\u0438\u0439: 101\u2013905. \u0421\u0435\u0439\u0447\u0430\u0441: " + module.status(), class_124.field_1080);
    }

    private int parse(String value) {
        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException ignored) {
            return -1;
        }
    }

    @Override
    public String getShortDesc() {
        return "\u041d\u0430\u0441\u0442\u0440\u0430\u0438\u0432\u0430\u0435\u0442 \u0434\u0432\u0435 \u0430\u043d\u0430\u0440\u0445\u0438\u0438 \u0438 \u0443\u043f\u0440\u0430\u0432\u043b\u044f\u0435\u0442 \u0430\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438\u043c \u043b\u0443\u0442\u043e\u043c \u042d\u043d\u0434\u0430";
    }

    @Override
    public List<String> getLongDesc() {
        return List.of("> autoend add <\u043e\u0441\u043d\u043e\u0432\u043d\u0430\u044f \u0430\u043d\u0430\u0440\u0445\u0438\u044f> <\u044d\u043d\u0434-\u0430\u043d\u0430\u0440\u0445\u0438\u044f>", "> autoend add <\u043d\u043e\u043c\u0435\u0440> \u2014 \u0434\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u0441\u043d\u0430\u0447\u0430\u043b\u0430 \u043e\u0441\u043d\u043e\u0432\u043d\u0443\u044e, \u0437\u0430\u0442\u0435\u043c \u042d\u043d\u0434-\u0430\u043d\u0430\u0440\u0445\u0438\u044e", "> autoend start \u2014 \u0437\u0430\u043f\u0443\u0441\u0442\u0438\u0442\u044c \u0446\u0438\u043a\u043b", "> autoend stop \u2014 \u043e\u0441\u0442\u0430\u043d\u043e\u0432\u0438\u0442\u044c \u0446\u0438\u043a\u043b", "> autoend status \u2014 \u043f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u0441\u043e\u0441\u0442\u043e\u044f\u043d\u0438\u0435");
    }
}

