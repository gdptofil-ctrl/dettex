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
import ru.recoin.api.feature.command.datatypes.TabPlayerDataType;
import ru.recoin.api.feature.command.exception.CommandException;
import ru.recoin.api.feature.command.helpers.TabCompleteHelper;
import ru.recoin.api.file.exception.FileSaveException;
import ru.recoin.api.repository.staff.StaffRegistry;
import ru.recoin.core.Main;

public final class StaffCommand
extends Command {
    public StaffCommand() {
        super("staff");
    }

    @Override
    public void execute(String label, IArgConsumer args2) throws CommandException {
        String action;
        switch (action = args2.hasAny() ? args2.getString().toLowerCase(Locale.ROOT) : "list") {
            case "add": {
                args2.requireExactly(1);
                String name = args2.getString().trim();
                if (!name.matches("[A-Za-z0-9_]{3,16}")) {
                    this.logDirect("\u041d\u0435\u043a\u043e\u0440\u0440\u0435\u043a\u0442\u043d\u044b\u0439 \u043d\u0438\u043a: " + name, class_124.field_1061);
                    break;
                }
                if (StaffRegistry.add(name)) {
                    this.logDirect("\u0421\u0442\u0430\u0444\u0444 \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d: " + name);
                    this.saveStaff();
                    break;
                }
                this.logDirect(name + " \u0443\u0436\u0435 \u0435\u0441\u0442\u044c \u0432 \u0441\u043f\u0438\u0441\u043a\u0435 \u0441\u0442\u0430\u0444\u0444\u0430", class_124.field_1061);
                break;
            }
            case "remove": {
                args2.requireExactly(1);
                String name = args2.getString().trim();
                if (StaffRegistry.remove(name)) {
                    this.logDirect("\u0421\u0442\u0430\u0444\u0444 \u0443\u0434\u0430\u043b\u0451\u043d: " + name);
                    this.saveStaff();
                    break;
                }
                this.logDirect(name + " \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d \u0432 \u0441\u043f\u0438\u0441\u043a\u0435 \u0441\u0442\u0430\u0444\u0444\u0430", class_124.field_1061);
                break;
            }
            case "clear": {
                args2.requireExactly(0);
                StaffRegistry.clear();
                this.logDirect("\u0421\u043f\u0438\u0441\u043e\u043a \u0441\u0442\u0430\u0444\u0444\u0430 \u043e\u0447\u0438\u0449\u0435\u043d.");
                this.saveStaff();
                break;
            }
            case "list": {
                args2.requireExactly(0);
                this.logDirect((String)(StaffRegistry.names().isEmpty() ? "\u0421\u043f\u0438\u0441\u043e\u043a \u0441\u0442\u0430\u0444\u0444\u0430 \u043f\u0443\u0441\u0442" : "\u0421\u0442\u0430\u0444\u0444: " + String.join((CharSequence)", ", StaffRegistry.names())));
                break;
            }
            default: {
                this.logDirect("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435: .staff add/remove <\u043d\u0438\u043a>, .staff list, .staff clear", class_124.field_1061);
            }
        }
    }

    private void saveStaff() {
        try {
            Main.getInstance().getFileController().saveClientFile("staff");
        }
        catch (FileSaveException e) {
            this.logDirect("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u0441\u043f\u0438\u0441\u043e\u043a \u0441\u0442\u0430\u0444\u0444\u0430: " + e.getMessage(), class_124.field_1061);
        }
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args2) throws CommandException {
        if (!args2.hasAny()) {
            return Stream.of("add", "remove", "list", "clear");
        }
        String action = args2.getString();
        if (args2.hasExactlyOne()) {
            if (action.equalsIgnoreCase("add")) {
                return args2.tabCompleteDatatype(TabPlayerDataType.INSTANCE);
            }
            if (action.equalsIgnoreCase("remove")) {
                return new TabCompleteHelper().append(StaffRegistry.names().stream()).filterPrefix(args2.getString()).sortAlphabetically().stream();
            }
        }
        if (args2.hasAny()) {
            return Stream.empty();
        }
        return new TabCompleteHelper().prepend("add", "remove", "list", "clear").filterPrefix(action).stream();
    }

    @Override
    public String getShortDesc() {
        return "\u0423\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0435 \u0441\u043f\u0438\u0441\u043a\u043e\u043c \u0441\u0442\u0430\u0444\u0444\u0430";
    }

    @Override
    public List<String> getLongDesc() {
        return List.of(".staff add <\u043d\u0438\u043a> \u2014 \u0434\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u0441\u0442\u0430\u0444\u0444\u0430", ".staff remove <\u043d\u0438\u043a> \u2014 \u0443\u0434\u0430\u043b\u0438\u0442\u044c \u0441\u0442\u0430\u0444\u0444\u0430", ".staff list \u2014 \u043f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u0441\u043f\u0438\u0441\u043e\u043a", ".staff clear \u2014 \u043e\u0447\u0438\u0441\u0442\u0438\u0442\u044c \u0441\u043f\u0438\u0441\u043e\u043a");
    }
}

