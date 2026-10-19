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
import ru.recoin.implement.features.modules.misc.AutoWarden;

public final class WardenCommand
extends Command {
    public WardenCommand() {
        super("warden");
    }

    @Override
    public void execute(String label, IArgConsumer args2) throws CommandException {
        String action;
        AutoWarden module = this.findModule();
        if (module == null) {
            return;
        }
        if (!args2.hasAny()) {
            this.usage();
            return;
        }
        switch (action = args2.getString().toLowerCase(Locale.ROOT)) {
            case "base": {
                if (!args2.hasAny()) {
                    this.logDirect("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435: .warden base <\u0430\u043d\u0430\u0440\u0445\u0438\u044f>");
                    return;
                }
                int anarchy = this.number(args2.getString());
                args2.requireExactly(0);
                if (anarchy < 1 || anarchy > 999) {
                    this.logDirect("\u0410\u043d\u0430\u0440\u0445\u0438\u044f \u0434\u043e\u043b\u0436\u043d\u0430 \u0431\u044b\u0442\u044c \u043e\u0442 1 \u0434\u043e 999.", class_124.field_1061);
                    break;
                }
                if (module.setBaseAnarchy(anarchy)) {
                    this.logDirect("\u0411\u0430\u0437\u0430 AutoWarden: \u0430\u043d\u0430\u0440\u0445\u0438\u044f " + anarchy + ".", class_124.field_1060);
                    break;
                }
                this.logDirect("\u0421\u043d\u0430\u0447\u0430\u043b\u0430 \u0434\u043e\u0431\u0430\u0432\u044c \u044d\u0442\u0443 \u0430\u043d\u0430\u0440\u0445\u0438\u044e: .warden add " + anarchy, class_124.field_1061);
                break;
            }
            case "add": {
                if (!args2.hasAny()) {
                    this.logDirect("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435: .warden add <\u0430\u043d\u0430\u0440\u0445\u0438\u044f>");
                    return;
                }
                int anarchy = this.number(args2.getString());
                args2.requireExactly(0);
                if (anarchy < 1 || anarchy > 999) {
                    this.logDirect("\u0410\u043d\u0430\u0440\u0445\u0438\u044f \u0434\u043e\u043b\u0436\u043d\u0430 \u0431\u044b\u0442\u044c \u043e\u0442 1 \u0434\u043e 999.", class_124.field_1061);
                    break;
                }
                if (module.getAnarchies().contains(anarchy)) {
                    this.logDirect("\u0410\u043d\u0430\u0440\u0445\u0438\u044f " + anarchy + " \u0443\u0436\u0435 \u0432 \u0441\u043f\u0438\u0441\u043a\u0435.", class_124.field_1054);
                    break;
                }
                if (module.getAnarchies().size() >= 10) {
                    this.logDirect("\u041c\u043e\u0436\u043d\u043e \u0434\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u043c\u0430\u043a\u0441\u0438\u043c\u0443\u043c 10 \u0430\u043d\u0430\u0440\u0445\u0438\u0439.", class_124.field_1061);
                    break;
                }
                if (!module.addAnarchy(anarchy)) break;
                this.logDirect("\u0410\u043d\u0430\u0440\u0445\u0438\u044f " + anarchy + " \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d\u0430.", class_124.field_1060);
                break;
            }
            case "remove": {
                if (!args2.hasAny()) {
                    this.logDirect("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435: .warden remove <\u0430\u043d\u0430\u0440\u0445\u0438\u044f>");
                    return;
                }
                int anarchy = this.number(args2.getString());
                args2.requireExactly(0);
                if (module.removeAnarchy(anarchy)) {
                    this.logDirect("\u0410\u043d\u0430\u0440\u0445\u0438\u044f " + anarchy + " \u0443\u0434\u0430\u043b\u0435\u043d\u0430.", class_124.field_1060);
                    break;
                }
                this.logDirect("\u0410\u043d\u0430\u0440\u0445\u0438\u044f " + anarchy + " \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u0430.", class_124.field_1061);
                break;
            }
            case "list": {
                args2.requireExactly(0);
                List<Integer> values2 = module.getAnarchies();
                this.logDirect((String)(values2.isEmpty() ? "\u0421\u043f\u0438\u0441\u043e\u043a \u0430\u043d\u0430\u0440\u0445\u0438\u0439 \u043f\u0443\u0441\u0442." : "\u0410\u043d\u0430\u0440\u0445\u0438\u0438 (" + values2.size() + "): " + String.valueOf(values2)));
                break;
            }
            case "clear": {
                args2.requireExactly(0);
                module.clearAnarchies();
                this.logDirect("\u0421\u043f\u0438\u0441\u043e\u043a \u0430\u043d\u0430\u0440\u0445\u0438\u0439 \u043e\u0447\u0438\u0449\u0435\u043d.", class_124.field_1060);
                break;
            }
            default: {
                this.usage();
            }
        }
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args2) throws CommandException {
        if (!args2.hasAny()) {
            return Stream.of("base", "add", "remove", "list", "clear");
        }
        String value = args2.getString().toLowerCase(Locale.ROOT);
        return args2.hasAny() ? Stream.empty() : Stream.of("base", "add", "remove", "list", "clear").filter(item -> item.startsWith(value));
    }

    private AutoWarden findModule() {
        AutoWarden module = AutoWarden.getInstance();
        if (module == null) {
            this.logDirect("[AutoWarden] \u041c\u043e\u0434\u0443\u043b\u044c \u043d\u0435 \u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d.", class_124.field_1061);
        }
        return module;
    }

    private void usage() {
        this.logDirect("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435: .warden <base|add|remove|list|clear>");
    }

    private int number(String value) {
        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException ignored) {
            return -1;
        }
    }

    @Override
    public String getShortDesc() {
        return "\u041d\u0430\u0441\u0442\u0440\u0430\u0438\u0432\u0430\u0435\u0442 \u0441\u043f\u0438\u0441\u043e\u043a \u0430\u043d\u0430\u0440\u0445\u0438\u0439 \u0434\u043b\u044f AutoWarden";
    }

    @Override
    public List<String> getLongDesc() {
        return List.of("> warden base <\u0430\u043d\u0430\u0440\u0445\u0438\u044f>", "> warden add <\u0430\u043d\u0430\u0440\u0445\u0438\u044f>", "> warden remove <\u0430\u043d\u0430\u0440\u0445\u0438\u044f>", "> warden list", "> warden clear");
    }
}

