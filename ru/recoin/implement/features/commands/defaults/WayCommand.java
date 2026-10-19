/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_2338
 *  net.minecraft.class_2561
 */
package ru.recoin.implement.features.commands.defaults;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.class_124;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import ru.recoin.api.feature.command.Command;
import ru.recoin.api.feature.command.IBaritoneChatControl;
import ru.recoin.api.feature.command.argument.IArgConsumer;
import ru.recoin.api.feature.command.datatypes.WayDataType;
import ru.recoin.api.feature.command.exception.CommandException;
import ru.recoin.api.feature.command.helpers.Paginator;
import ru.recoin.api.feature.command.helpers.TabCompleteHelper;
import ru.recoin.api.repository.way.Way;
import ru.recoin.api.repository.way.WayRepository;
import ru.recoin.common.QuickImports;
import ru.recoin.core.Main;
import ru.recoin.implement.features.modules.misc.ServerHelper;

public class WayCommand
extends Command
implements QuickImports {
    private final WayRepository wayRepository;

    protected WayCommand(Main main) {
        super("way");
        this.wayRepository = main.getWayRepository();
    }

    @Override
    public void execute(String label, IArgConsumer args2) throws CommandException {
        String arg;
        switch (arg = args2.hasAny() ? args2.getString().toLowerCase(Locale.US) : "list") {
            case "add": {
                this.handleAddWay(args2);
                break;
            }
            case "remove": {
                this.handleRemoveWay(args2);
                break;
            }
            case "clear": {
                this.handleClearWays(args2);
                break;
            }
            case "list": {
                this.handleListWays(label, args2);
            }
        }
    }

    private void handleAddWay(IArgConsumer args2) throws CommandException {
        args2.requireMin(4);
        String name = args2.getString();
        int x = args2.getArgs().get(0).getAs(Integer.class);
        int y = args2.getArgs().get(1).getAs(Integer.class);
        int z = args2.getArgs().get(2).getAs(Integer.class);
        if (this.wayRepository.hasWay(name)) {
            this.logDirect("\u041c\u0435\u0442\u043a\u0430 \u0441 \u0442\u0430\u043a\u0438\u043c \u0438\u043c\u0435\u043d\u0435\u043c \u0443\u0436\u0435 \u0435\u0441\u0442\u044c \u0432 \u0441\u043f\u0438\u0441\u043a\u0435!", class_124.field_1061);
            return;
        }
        String address = mc.method_1562() == null ? "vanilla" : (mc.method_1562().method_45734() == null ? "vanilla" : WayCommand.mc.method_1562().method_45734().field_3761);
        this.logDirect("\u0414\u043e\u0431\u0430\u0432\u043b\u0435\u043d\u0430 \u043c\u0435\u0442\u043a\u0430 " + name + ", \u041a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b: (" + x + ", " + y + ", " + z + "), \u0421\u0435\u0440\u0432\u0435\u0440: " + address, class_124.field_1080);
        this.wayRepository.addWay(name, new class_2338(x, y, z), address);
    }

    private void handleRemoveWay(IArgConsumer args2) throws CommandException {
        args2.requireMax(1);
        String name = args2.getString();
        if (this.wayRepository.hasWay(name)) {
            this.wayRepository.deleteWay(name);
            this.logDirect(String.valueOf(class_124.field_1060) + "\u041c\u0435\u0442\u043a\u0430 " + String.valueOf(class_124.field_1061) + name + String.valueOf(class_124.field_1060) + " \u0431\u044b\u043b\u0430 \u0443\u0441\u043f\u0435\u0448\u043d\u0430 \u0443\u0434\u0430\u043b\u0435\u043d\u0430!");
        } else {
            this.logDirect("\u041c\u0435\u0442\u043a\u0430 \u0441 \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435\u043c '" + name + "' \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u0430!");
        }
    }

    private void handleListWays(String label, IArgConsumer args2) throws CommandException {
        args2.requireMax(1);
        Paginator.paginate(args2, new Paginator<Way>(this.wayRepository.wayList), () -> this.logDirect("\u0421\u043f\u0438\u0441\u043e\u043a \u043c\u0435\u0442\u043e\u043a:"), way -> class_2561.method_43470((String)(String.valueOf(class_124.field_1080) + "\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435: " + String.valueOf(class_124.field_1061) + way.name())).method_10852((class_2561)class_2561.method_43470((String)(String.valueOf(class_124.field_1080) + " \u041a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b: " + String.valueOf(class_124.field_1068) + " (" + way.pos().method_10263() + ", " + way.pos().method_10264() + ", " + way.pos().method_10260() + ")")).method_10852((class_2561)class_2561.method_43470((String)(String.valueOf(class_124.field_1080) + " \u0421\u0435\u0440\u0432\u0435\u0440: " + String.valueOf(class_124.field_1068) + way.server())))), IBaritoneChatControl.FORCE_COMMAND_PREFIX + label);
    }

    private void handleClearWays(IArgConsumer args2) throws CommandException {
        args2.requireMax(1);
        this.wayRepository.clearList();
        int autoPoints = ServerHelper.getInstance().clearAutoPoints();
        this.logDirect(String.valueOf(class_124.field_1060) + "\u0412\u0441\u0435 \u043c\u0435\u0442\u043a\u0438 \u0431\u044b\u043b\u0438 \u0443\u0434\u0430\u043b\u0435\u043d\u044b. AutoPoint: " + autoPoints);
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args2) throws CommandException {
        if (args2.hasAny()) {
            String arg = args2.getString();
            if (arg.equalsIgnoreCase("remove")) {
                if (args2.hasExactlyOne()) {
                    return args2.tabCompleteDatatype(WayDataType.INSTANCE);
                }
            } else {
                if (arg.equalsIgnoreCase("add")) {
                    String string = args2.has(5) ? "" : (args2.has(4) ? "z" : (args2.has(3) ? "y" : (args2.has(2) ? "x" : "\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435")));
                    return new TabCompleteHelper().sortAlphabetically().prepend(string).stream();
                }
                return new TabCompleteHelper().sortAlphabetically().prepend("add", "remove", "list", "clear").filterPrefix(arg).stream();
            }
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "\u041f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u0441\u0442\u0430\u0432\u0438\u0442\u044c \u043c\u0435\u0442\u043a\u0438 \u0432 \u043c\u0438\u0440\u0435";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList("\u0421 \u043f\u043e\u043c\u043e\u0449\u044c\u044e \u044d\u0442\u043e\u0439 \u043a\u043e\u043c\u0430\u043d\u0434\u044b \u043c\u043e\u0436\u043d\u043e \u0434\u043e\u0431\u0430\u0432\u043b\u044f\u0442\u044c/\u0443\u0434\u0430\u043b\u044f\u0442\u044c \u043c\u0435\u0442\u043a\u0438 \u0432 \u043c\u0438\u0440\u0435", "", "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435:", "> way add <name> <x> <y> <z> - \u0414\u043e\u0431\u0430\u0432\u043b\u044f\u0435\u0442 \u043c\u0435\u0442\u043a\u0443", "> way remove <name> - \u0423\u0434\u0430\u043b\u044f\u0435\u0442 \u043c\u0435\u0442\u043a\u0443", "> way list - \u0412\u043e\u0437\u0432\u0440\u0430\u0449\u0430\u0435\u0442 \u0441\u043f\u0438\u0441\u043e\u043a \u043c\u0435\u0442\u043e\u043a", "> way clear - \u041e\u0447\u0438\u0449\u0430\u0435\u0442 \u0441\u043f\u0438\u0441\u043e\u043a \u043c\u0435\u0442\u043e\u043a.");
    }
}

