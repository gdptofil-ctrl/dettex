/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_2561
 *  net.minecraft.class_5250
 */
package ru.recoin.implement.features.commands.defaults;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_5250;
import ru.recoin.api.feature.command.Command;
import ru.recoin.api.feature.command.IBaritoneChatControl;
import ru.recoin.api.feature.command.argument.IArgConsumer;
import ru.recoin.api.feature.command.datatypes.FriendDataType;
import ru.recoin.api.feature.command.datatypes.TabPlayerDataType;
import ru.recoin.api.feature.command.exception.CommandException;
import ru.recoin.api.feature.command.helpers.Paginator;
import ru.recoin.api.feature.command.helpers.TabCompleteHelper;
import ru.recoin.api.repository.friend.Friend;
import ru.recoin.api.repository.friend.FriendUtils;

public class FriendCommand
extends Command {
    protected FriendCommand() {
        super("friend");
    }

    @Override
    public void execute(String label, IArgConsumer args2) throws CommandException {
        String arg;
        switch (arg = args2.hasAny() ? args2.getString().toLowerCase(Locale.US) : "list") {
            case "add": {
                args2.requireExactly(1);
                String name = args2.getString().trim();
                if (!FriendUtils.isFriend(name)) {
                    FriendUtils.addFriend(name);
                    this.logDirect("\u0412\u044b \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0434\u043e\u0431\u0430\u0432\u0438\u043b\u0438 " + name + " \u0432 \u0441\u043f\u0438\u0441\u043e\u043a \u0434\u0440\u0443\u0437\u0435\u0439!");
                    break;
                }
                this.logDirect(name + " \u0443\u0436\u0435 \u0435\u0441\u0442\u044c \u0432 \u0441\u043f\u0438\u0441\u043a\u0435 \u0434\u0440\u0443\u0437\u0435\u0439!", class_124.field_1061);
                break;
            }
            case "remove": {
                args2.requireExactly(1);
                String name = args2.getString().trim();
                if (FriendUtils.isFriend(name)) {
                    FriendUtils.removeFriend(name);
                    this.logDirect("\u0412\u044b \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0443\u0434\u0430\u043b\u0438\u043b\u0438 " + name + " \u0438\u0437 \u0441\u043f\u0438\u0441\u043a\u0430 \u0434\u0440\u0443\u0437\u0435\u0439!");
                    break;
                }
                this.logDirect(name + " \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d \u0432 \u0441\u043f\u0438\u0441\u043a\u0435 \u0434\u0440\u0443\u0437\u0435\u0439", class_124.field_1061);
                break;
            }
            case "list": {
                args2.requireMax(1);
                Paginator.paginate(args2, new Paginator<Friend>(FriendUtils.getFriends()), () -> this.logDirect("\u0421\u043f\u0438\u0441\u043e\u043a \u0434\u0440\u0443\u0437\u0435\u0439:"), friend -> {
                    class_5250 namesComponent = class_2561.method_43470((String)friend.getName());
                    namesComponent.method_10862(namesComponent.method_10866().method_10977(class_124.field_1068));
                    return namesComponent;
                }, IBaritoneChatControl.FORCE_COMMAND_PREFIX + label);
                break;
            }
            case "clear": {
                args2.requireExactly(0);
                FriendUtils.clear();
                this.logDirect("\u0421\u043f\u0438\u0441\u043e\u043a \u0434\u0440\u0443\u0437\u0435\u0439 \u043e\u0447\u0438\u0449\u0435\u043d.");
                break;
            }
            default: {
                this.logDirect("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435: .friend add/remove/list/clear", class_124.field_1061);
            }
        }
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args2) throws CommandException {
        if (args2.hasAny()) {
            String arg = args2.getString();
            if (args2.hasExactlyOne()) {
                if (arg.equalsIgnoreCase("add")) {
                    return args2.tabCompleteDatatype(TabPlayerDataType.INSTANCE);
                }
                if (arg.equalsIgnoreCase("remove")) {
                    return args2.tabCompleteDatatype(FriendDataType.INSTANCE);
                }
            } else {
                return new TabCompleteHelper().sortAlphabetically().prepend("add", "remove", "list", "clear").filterPrefix(arg).stream();
            }
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "\u0420\u045f\u0420\u0455\u0420\u00b7\u0420\u0406\u0420\u0455\u0420\u00bb\u0421\u040f\u0420\u00b5\u0421\u201a \u0421\u0453\u0420\u0457\u0421\u0402\u0420\u00b0\u0420\u0406\u0420\u00bb\u0421\u040f\u0421\u201a\u0421\u040a \u0421\u0403\u0420\u0457\u0420\u0451\u0421\u0403\u0420\u0454\u0420\u0455\u0420\u0458 \u0420\u0491\u0421\u0402\u0421\u0453\u0420\u00b7\u0420\u00b5\u0420\u2116";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList("\u0420\u040e \u0420\u0457\u0420\u0455\u0420\u0458\u0420\u0455\u0421\u2030\u0421\u040a\u0421\u040b \u0421\u040c\u0421\u201a\u0420\u0455\u0420\u2116 \u0420\u0454\u0420\u0455\u0420\u0458\u0420\u00b0\u0420\u0405\u0420\u0491\u0421\u2039 \u0420\u0458\u0420\u0455\u0420\u00b6\u0420\u0405\u0420\u0455 \u0420\u0491\u0420\u0455\u0420\u00b1\u0420\u00b0\u0420\u0406\u0420\u00bb\u0421\u040f\u0421\u201a\u0421\u040a/\u0421\u0453\u0420\u0491\u0420\u00b0\u0420\u00bb\u0421\u040f\u0421\u201a\u0421\u040a \u0420\u0491\u0421\u0402\u0421\u0453\u0420\u00b7\u0420\u00b5\u0420\u2116 \u0420\u0406 \u0421\u2021\u0420\u0451\u0421\u201a\u0420\u00b5", "", "\u0420\u0098\u0421\u0403\u0420\u0457\u0420\u0455\u0420\u00bb\u0421\u040a\u0420\u00b7\u0420\u0455\u0420\u0406\u0420\u00b0\u0420\u0405\u0420\u0451\u0420\u00b5:", "> friend add <name> - \u0420\u201d\u0420\u0455\u0420\u00b1\u0420\u00b0\u0420\u0406\u0420\u00bb\u0421\u040f\u0420\u00b5\u0421\u201a \u0420\u0451\u0420\u0458\u0421\u040f \u0420\u0406 \u0421\u0403\u0420\u0457\u0420\u0451\u0421\u0403\u0420\u0455\u0420\u0454 \u0420\u0491\u0421\u0402\u0421\u0453\u0420\u00b7\u0420\u00b5\u0420\u2116.", "> friend remove <name> - \u0420\u0408\u0420\u0491\u0420\u00b0\u0420\u00bb\u0421\u040f\u0420\u00b5\u0421\u201a \u0420\u0451\u0420\u0458\u0421\u040f \u0420\u0451\u0420\u00b7 \u0421\u0403\u0420\u0457\u0420\u0451\u0421\u0403\u0420\u0454\u0420\u00b0 \u0420\u0491\u0421\u0402\u0421\u0453\u0420\u00b7\u0420\u00b5\u0420\u2116.", "> friend list - \u0420\u2019\u0420\u0455\u0420\u00b7\u0420\u0406\u0421\u0402\u0420\u00b0\u0421\u2030\u0420\u00b0\u0420\u00b5\u0421\u201a \u0421\u0403\u0420\u0457\u0420\u0451\u0421\u0403\u0420\u0455\u0420\u0454 \u0420\u0491\u0421\u0402\u0421\u0453\u0420\u00b7\u0420\u00b5\u0420\u2116", "> friend clear - \u0420\u045b\u0421\u2021\u0420\u0451\u0421\u2030\u0420\u00b0\u0420\u00b5\u0421\u201a \u0421\u0403\u0420\u0457\u0420\u0451\u0421\u0403\u0420\u0455\u0420\u0454 \u0420\u0491\u0421\u0402\u0421\u0453\u0420\u00b7\u0420\u00b5\u0420\u2116.");
    }
}

