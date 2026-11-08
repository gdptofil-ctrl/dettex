/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_3545
 */
package ru.recoin.implement.features.commands;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.class_3545;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.event.EventManager;
import ru.recoin.api.feature.command.IBaritoneChatControl;
import ru.recoin.api.feature.command.argument.ICommandArgument;
import ru.recoin.api.feature.command.exception.CommandNotEnoughArgumentsException;
import ru.recoin.api.feature.command.exception.CommandNotFoundException;
import ru.recoin.api.feature.command.helpers.TabCompleteHelper;
import ru.recoin.api.feature.command.manager.ICommandManager;
import ru.recoin.common.QuickLogger;
import ru.recoin.core.Main;
import ru.recoin.implement.events.chat.ChatEvent;
import ru.recoin.implement.events.chat.TabCompleteEvent;
import ru.recoin.implement.features.commands.argument.ArgConsumer;
import ru.recoin.implement.features.commands.argument.CommandArguments;
import ru.recoin.implement.features.commands.manager.CommandRepository;
import ru.recoin.implement.features.modules.misc.Unhook;

public class CommandDispatcher
implements QuickLogger {
    private final ICommandManager manager = Main.getInstance().getCommandRepository();
    public static final String DEFAULT_PREFIX = ".";
    public static String prefix = ".";

    public CommandDispatcher(EventManager eventManager) {
        eventManager.register(this);
    }

    @EventHandler
    public void onChat(ChatEvent event) {
        String commandPrefix = null;
        if (event.isCancelled() || Unhook.isLocked()) {
            return;
        }
        String msg = event.getMessage();
        boolean forceRun = msg.startsWith(IBaritoneChatControl.FORCE_COMMAND_PREFIX);
        commandPrefix = msg.startsWith(prefix) ? prefix : (!DEFAULT_PREFIX.equals(prefix) && msg.startsWith(DEFAULT_PREFIX) ? DEFAULT_PREFIX : null);
        if (commandPrefix != null || forceRun) {
            event.cancel();
            String commandStr = msg.substring(forceRun ? IBaritoneChatControl.FORCE_COMMAND_PREFIX.length() : commandPrefix.length());
            if (!this.runCommand(commandStr) && !commandStr.trim().isEmpty()) {
                new CommandNotFoundException((String)CommandRepository.expand(commandStr).method_15442()).handle(null, null);
            }
        } else if (this.runCommand(msg)) {
            event.cancel();
        }
    }

    public boolean runCommand(String msg) {
        if (msg.isEmpty()) {
            return this.runCommand("help");
        }
        class_3545<String, List<ICommandArgument>> pair = CommandRepository.expand(msg);
        String command = (String)pair.method_15442();
        String rest = msg.substring(((String)pair.method_15442()).length());
        ArgConsumer argc = new ArgConsumer(this.manager, (List)pair.method_15441());
        return this.manager.execute(pair);
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        if (Unhook.isLocked()) {
            event.cancel();
            event.completions = new String[0];
            return;
        }
        String eventPrefix = event.prefix;
        String commandPrefix = eventPrefix.startsWith(prefix) ? prefix : (!DEFAULT_PREFIX.equals(prefix) && eventPrefix.startsWith(DEFAULT_PREFIX) ? DEFAULT_PREFIX : null);
        if (commandPrefix == null) {
            return;
        }
        String msg = eventPrefix.substring(commandPrefix.length());
        List<ICommandArgument> args2 = CommandArguments.from(msg, true);
        Stream<String> stream = this.tabComplete(msg);
        if (args2.size() == 1) {
            stream = stream.map(x -> commandPrefix + x);
        }
        event.completions = (String[])stream.toArray(String[]::new);
    }

    public Stream<String> tabComplete(String msg) {
        try {
            List<ICommandArgument> args2 = CommandArguments.from(msg, true);
            ArgConsumer argc = new ArgConsumer(this.manager, args2);
            if (argc.hasAtMost(2) && argc.hasExactly(1)) {
                return new TabCompleteHelper().addCommands(this.manager).filterPrefix(argc.getString()).stream();
            }
            return this.manager.tabComplete(msg);
        }
        catch (CommandNotEnoughArgumentsException ignored) {
            return Stream.empty();
        }
    }
}

