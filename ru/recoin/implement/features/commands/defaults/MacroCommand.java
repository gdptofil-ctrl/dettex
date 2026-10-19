/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_2561
 */
package ru.recoin.implement.features.commands.defaults;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import ru.recoin.api.feature.command.Command;
import ru.recoin.api.feature.command.IBaritoneChatControl;
import ru.recoin.api.feature.command.argument.IArgConsumer;
import ru.recoin.api.feature.command.datatypes.KeyDataType;
import ru.recoin.api.feature.command.datatypes.MacroDataType;
import ru.recoin.api.feature.command.exception.CommandException;
import ru.recoin.api.feature.command.helpers.Paginator;
import ru.recoin.api.feature.command.helpers.TabCompleteHelper;
import ru.recoin.api.repository.macro.Macro;
import ru.recoin.api.repository.macro.MacroRepository;
import ru.recoin.common.util.other.StringUtil;
import ru.recoin.core.Main;

public class MacroCommand
extends Command {
    private final MacroRepository macroRepository;

    public MacroCommand(Main main) {
        super("macro", "macros");
        this.macroRepository = main.getMacroRepository();
    }

    @Override
    public void execute(String label, IArgConsumer args2) throws CommandException {
        String action;
        switch (action = args2.hasAny() ? args2.getString().toLowerCase(Locale.US) : "list") {
            case "add": {
                this.handleAddMacro(args2);
                break;
            }
            case "remove": {
                this.handleRemoveMacro(args2);
                break;
            }
            case "list": {
                this.handleListMacros(args2, label);
                break;
            }
            case "clear": {
                this.handleClearMacros(args2);
            }
        }
    }

    private void handleAddMacro(IArgConsumer args2) throws CommandException {
        args2.requireMin(3);
        int key = (Integer)((Map.Entry)args2.getDatatypeFor(KeyDataType.INSTANCE)).getValue();
        String name = args2.getString();
        String command = args2.rawRest();
        if (this.macroRepository.hasMacro(name)) {
            this.logDirect("\u041c\u0430\u043a\u0440\u043e\u0441 \u0441 \u0442\u0430\u043a\u0438\u043c \u0438\u043c\u0435\u043d\u0435\u043c \u0443\u0436\u0435 \u0435\u0441\u0442\u044c \u0432 \u0441\u043f\u0438\u0441\u043a\u0435!", class_124.field_1061);
            return;
        }
        this.macroRepository.addMacro(name, command, key);
        this.logDirect(String.valueOf(class_124.field_1060) + "\u0414\u043e\u0431\u0430\u0432\u043b\u0435\u043d \u043c\u0430\u043a\u0440\u043e\u0441 \u0441 \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435\u043c " + String.valueOf(class_124.field_1061) + name + String.valueOf(class_124.field_1060) + " \u0441 \u043a\u043d\u043e\u043f\u043a\u043e\u0439 " + String.valueOf(class_124.field_1061) + StringUtil.getBindName(key).toLowerCase() + String.valueOf(class_124.field_1060) + " \u0441 \u043a\u043e\u043c\u0430\u043d\u0434\u043e\u0439 " + String.valueOf(class_124.field_1061) + command);
    }

    private void handleRemoveMacro(IArgConsumer args2) throws CommandException {
        args2.requireMax(1);
        String name = args2.getString();
        if (this.macroRepository.hasMacro(name)) {
            this.macroRepository.deleteMacro(name);
            this.logDirect(String.valueOf(class_124.field_1060) + "\u041c\u0430\u043a\u0440\u043e\u0441 " + String.valueOf(class_124.field_1061) + name + String.valueOf(class_124.field_1060) + " \u0431\u044b\u043b \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0443\u0434\u0430\u043b\u0435\u043d!");
        } else {
            this.logDirect("\u041c\u0430\u043a\u0440\u043e\u0441 \u0441 \u0442\u0430\u043a\u0438\u043c \u0438\u043c\u0435\u043d\u0435\u043c \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d!", class_124.field_1061);
        }
    }

    private void handleListMacros(IArgConsumer args2, String label) throws CommandException {
        args2.requireMax(1);
        Paginator.paginate(args2, new Paginator<Macro>(this.macroRepository.macroList), () -> this.logDirect("\u0421\u043f\u0438\u0441\u043e\u043a \u043c\u0430\u043a\u0440\u043e\u0441\u043e\u0432:"), macro -> {
            String names = macro.name();
            String keys2 = StringUtil.getBindName(macro.key()).toLowerCase();
            String command = macro.message();
            return class_2561.method_43470((String)(String.valueOf(class_124.field_1080) + "\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435: " + String.valueOf(class_124.field_1068) + names)).method_10852((class_2561)class_2561.method_43470((String)(String.valueOf(class_124.field_1080) + " \u041a\u043b\u0430\u0432\u0438\u0448\u0430: " + String.valueOf(class_124.field_1068) + keys2))).method_10852((class_2561)class_2561.method_43470((String)(String.valueOf(class_124.field_1080) + " \u041a\u043e\u043c\u0430\u043d\u0434\u0430: " + String.valueOf(class_124.field_1068) + command)));
        }, IBaritoneChatControl.FORCE_COMMAND_PREFIX + label);
    }

    private void handleClearMacros(IArgConsumer args2) throws CommandException {
        args2.requireMax(1);
        this.macroRepository.clearList();
        this.logDirect("\u0412\u0441\u0435 \u043c\u0430\u043a\u0440\u043e\u0441\u044b \u0431\u044b\u043b\u0438 \u0443\u0434\u0430\u043b\u0435\u043d\u044b.", class_124.field_1060);
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args2) throws CommandException {
        if (args2.hasAny() && args2.hasExactlyOne()) {
            return new TabCompleteHelper().sortAlphabetically().prepend("add", "remove", "list", "clear").filterPrefix(args2.getString()).stream();
        }
        if (args2.hasAny()) {
            String arg = args2.getString();
            if (arg.equalsIgnoreCase("add") && args2.hasExactlyOne()) {
                return args2.tabCompleteDatatype(KeyDataType.INSTANCE);
            }
            if (arg.equalsIgnoreCase("remove") && args2.hasExactlyOne()) {
                return args2.tabCompleteDatatype(MacroDataType.INSTANCE);
            }
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "\u041f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u0443\u043f\u0440\u0430\u0432\u043b\u044f\u0442\u044c \u043c\u0430\u043a\u0440\u043e\u0441\u0430\u043c\u0438";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList("\u042d\u0442\u0430 \u043a\u043e\u043c\u0430\u043d\u0434\u0430 \u043f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u0443\u043f\u0440\u0430\u0432\u043b\u044f\u0442\u044c \u043c\u0430\u043a\u0440\u043e\u0441\u0430\u043c\u0438, \u043a\u043e\u0442\u043e\u0440\u044b\u0435 \u0430\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u0432\u0432\u043e\u0434\u044f\u0442 \u0437\u0430\u0434\u0430\u043d\u043d\u044b\u0435 \u043a\u043e\u043c\u0430\u043d\u0434\u044b \u0432 \u0447\u0430\u0442.", "", "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435:", "> macro add <key> <name> <message> - \u0414\u043e\u0431\u0430\u0432\u043b\u044f\u0435\u0442 \u043d\u043e\u0432\u044b\u0439 \u043c\u0430\u043a\u0440\u043e\u0441, \u043a\u043e\u0442\u043e\u0440\u044b\u0439 \u0431\u0443\u0434\u0435\u0442 \u0430\u043a\u0442\u0438\u0432\u0438\u0440\u043e\u0432\u0430\u0442\u044c\u0441\u044f \u043f\u0440\u0438 \u043d\u0430\u0436\u0430\u0442\u0438\u0438 \u043d\u0430 \u0443\u043a\u0430\u0437\u0430\u043d\u043d\u0443\u044e \u043a\u043b\u0430\u0432\u0438\u0448\u0443 \u0438 \u0432\u0432\u043e\u0434\u0438\u0442\u044c \u0443\u043a\u0430\u0437\u0430\u043d\u043d\u043e\u0435 \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435.", "> macro remove <name> - \u0423\u0434\u0430\u043b\u044f\u0435\u0442 \u043c\u0430\u043a\u0440\u043e\u0441 \u0441 \u0443\u043a\u0430\u0437\u0430\u043d\u043d\u044b\u043c \u0438\u043c\u0435\u043d\u0435\u043c.", "> macro list - \u041e\u0442\u043e\u0431\u0440\u0430\u0436\u0430\u0435\u0442 \u0441\u043f\u0438\u0441\u043e\u043a \u0432\u0441\u0435\u0445 \u0442\u0435\u043a\u0443\u0449\u0438\u0445 \u043c\u0430\u043a\u0440\u043e\u0441\u043e\u0432.", "> macro clear - \u0423\u0434\u0430\u043b\u044f\u0435\u0442 \u0432\u0441\u0435 \u043c\u0430\u043a\u0440\u043e\u0441\u044b \u0438\u0437 \u0441\u043f\u0438\u0441\u043a\u0430.");
    }
}

