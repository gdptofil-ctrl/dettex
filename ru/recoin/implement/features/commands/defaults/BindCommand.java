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
import ru.recoin.api.feature.command.datatypes.ModuleDataType;
import ru.recoin.api.feature.command.exception.CommandException;
import ru.recoin.api.feature.command.helpers.Paginator;
import ru.recoin.api.feature.command.helpers.TabCompleteHelper;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleProvider;
import ru.recoin.api.feature.module.ModuleRepository;
import ru.recoin.common.util.other.StringUtil;
import ru.recoin.core.Main;

public class BindCommand
extends Command {
    private final ModuleProvider moduleProvider;
    private final ModuleRepository moduleRepository;

    public BindCommand(Main main) {
        super("bind");
        this.moduleRepository = main.getModuleRepository();
        this.moduleProvider = main.getModuleProvider();
    }

    @Override
    public void execute(String label, IArgConsumer args2) throws CommandException {
        String action;
        switch (action = args2.hasAny() ? args2.getString().toLowerCase(Locale.US) : "list") {
            case "add": {
                this.handleAddBind(args2);
                break;
            }
            case "remove": {
                this.handleRemoveBind(args2);
                break;
            }
            case "list": {
                this.handleListBinds(args2, label);
                break;
            }
            case "clear": {
                this.handleClearBinds(args2);
            }
        }
    }

    private void handleAddBind(IArgConsumer args2) throws CommandException {
        args2.requireMin(2);
        String moduleName = args2.getString();
        Object rawModule = this.moduleProvider.get(moduleName);
        if (rawModule == null) {
            this.logDirect(String.valueOf(class_124.field_1061) + "\u041c\u043e\u0434\u0443\u043b\u044c " + moduleName + " \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d.");
            return;
        }
        Module module = (Module)rawModule;
        if ("reset".equalsIgnoreCase(args2.peekString())) {
            args2.getString();
            int resetKey = module.getDefaultKey();
            module.setKey(resetKey);
            this.logDirect(String.valueOf(class_124.field_1060) + "\u0411\u0430\u0439\u043d\u0434 \u043c\u043e\u0434\u0443\u043b\u044f " + String.valueOf(class_124.field_1061) + moduleName + String.valueOf(class_124.field_1060) + " \u0441\u0431\u0440\u043e\u0448\u0435\u043d \u043d\u0430 " + String.valueOf(class_124.field_1061) + StringUtil.getBindName(resetKey).toLowerCase());
            return;
        }
        int key = (Integer)((Map.Entry)args2.getDatatypeFor(KeyDataType.INSTANCE)).getValue();
        module.setKey(key);
        this.logDirect(String.valueOf(class_124.field_1060) + "\u041c\u043e\u0434\u0443\u043b\u044c " + String.valueOf(class_124.field_1061) + moduleName + String.valueOf(class_124.field_1060) + " \u043f\u0440\u0438\u0432\u044f\u0437\u0430\u043d \u043a \u043a\u043d\u043e\u043f\u043a\u0435 " + String.valueOf(class_124.field_1061) + StringUtil.getBindName(key).toLowerCase());
    }

    private void handleRemoveBind(IArgConsumer args2) throws CommandException {
        args2.requireMax(1);
        String moduleName = args2.getString();
        Object module = this.moduleProvider.get(moduleName);
        ((Module)module).setKey(-1);
        this.logDirect(String.valueOf(class_124.field_1060) + "\u0411\u0430\u0439\u043d\u0434 \u0434\u043b\u044f \u043c\u043e\u0434\u0443\u043b\u044f " + String.valueOf(class_124.field_1061) + moduleName + String.valueOf(class_124.field_1060) + " \u0431\u044b\u043b \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0443\u0434\u0430\u043b\u0435\u043d!");
    }

    private void handleListBinds(IArgConsumer args2, String label) throws CommandException {
        args2.requireMax(1);
        List<Module> filtredList = this.moduleRepository.modules().stream().filter(module -> module.getKey() != -1).toList();
        Paginator.paginate(args2, new Paginator<Module>(filtredList), () -> this.logDirect("\u0421\u043f\u0438\u0441\u043e\u043a \u043c\u043e\u0434\u0443\u043b\u0435\u0439:"), module -> {
            String names = module.getName();
            String keys2 = StringUtil.getBindName(module.getKey()).toLowerCase();
            return class_2561.method_43470((String)(String.valueOf(class_124.field_1080) + "\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435: " + String.valueOf(class_124.field_1068) + names)).method_10852((class_2561)class_2561.method_43470((String)(String.valueOf(class_124.field_1080) + " \u041a\u043b\u0430\u0432\u0438\u0448\u0430: " + String.valueOf(class_124.field_1068) + keys2)));
        }, IBaritoneChatControl.FORCE_COMMAND_PREFIX + label);
    }

    private void handleClearBinds(IArgConsumer args2) throws CommandException {
        args2.requireMax(1);
        this.moduleRepository.modules().forEach(function -> function.setKey(-1));
        this.logDirect("\u0412\u0441\u0435 \u0431\u0438\u043d\u0434\u044b \u043c\u043e\u0434\u0443\u043b\u0435\u0439 \u0431\u044b\u043b\u0438 \u0443\u0434\u0430\u043b\u0435\u043d\u044b.", class_124.field_1060);
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args2) throws CommandException {
        if (args2.hasExactlyOne()) {
            return new TabCompleteHelper().sortAlphabetically().prepend("add", "remove", "list", "clear").filterPrefix(args2.getString()).stream();
        }
        String arg = args2.getString();
        if (arg.equalsIgnoreCase("add")) {
            if (args2.hasExactly(1)) {
                return args2.tabCompleteDatatype(ModuleDataType.INSTANCE);
            }
            if (args2.hasExactly(2)) {
                return args2.tabCompleteDatatype(KeyDataType.INSTANCE);
            }
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "\u0423\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0435 \u0431\u0438\u043d\u0434\u0430\u043c\u0438 \u0434\u043b\u044f \u043c\u043e\u0434\u0443\u043b\u0435\u0439.";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList("\u042d\u0442\u0430 \u043a\u043e\u043c\u0430\u043d\u0434\u0430 \u043f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u0443\u043f\u0440\u0430\u0432\u043b\u044f\u0442\u044c \u0431\u0438\u043d\u0434\u0430\u043c\u0438 \u0434\u043b\u044f \u043c\u043e\u0434\u0443\u043b\u0435\u0439, \u043a\u043e\u0442\u043e\u0440\u044b\u0435 \u0431\u0443\u0434\u0443\u0442 \u0430\u043a\u0442\u0438\u0432\u0438\u0440\u043e\u0432\u0430\u0442\u044c\u0441\u044f \u043f\u0440\u0438 \u043d\u0430\u0436\u0430\u0442\u0438\u0438 \u043e\u043f\u0440\u0435\u0434\u0435\u043b\u0451\u043d\u043d\u044b\u0445 \u043a\u043b\u0430\u0432\u0438\u0448.", "", "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435:", "> bind add <module> <key> - \u041f\u0440\u0438\u0432\u044f\u0437\u044b\u0432\u0430\u0435\u0442 \u043c\u043e\u0434\u0443\u043b\u044c \u043a \u0443\u043a\u0430\u0437\u0430\u043d\u043d\u043e\u0439 \u043a\u043b\u0430\u0432\u0438\u0448\u0435.", "> bind remove <module> - \u0423\u0434\u0430\u043b\u044f\u0435\u0442 \u043f\u0440\u0438\u0432\u044f\u0437\u043a\u0443 \u043c\u043e\u0434\u0443\u043b\u044f.", "> bind list - \u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0435\u0442 \u0441\u043f\u0438\u0441\u043e\u043a \u0432\u0441\u0435\u0445 \u0442\u0435\u043a\u0443\u0449\u0438\u0445 \u0431\u0438\u043d\u0434\u043e\u0432 \u043c\u043e\u0434\u0443\u043b\u0435\u0439.", "> bind clear - \u0423\u0434\u0430\u043b\u044f\u0435\u0442 \u0432\u0441\u0435 \u0431\u0438\u043d\u0434\u044b \u043c\u043e\u0434\u0443\u043b\u0435\u0439.");
    }
}

