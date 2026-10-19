/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_3545
 */
package ru.recoin.implement.features.commands.manager;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.class_3545;
import ru.recoin.api.feature.command.ICommand;
import ru.recoin.api.feature.command.argument.ICommandArgument;
import ru.recoin.api.feature.command.exception.CommandException;
import ru.recoin.api.feature.command.exception.CommandUnhandledException;
import ru.recoin.api.feature.command.exception.ICommandException;
import ru.recoin.api.feature.command.helpers.TabCompleteHelper;
import ru.recoin.api.feature.command.manager.ICommandManager;
import ru.recoin.api.feature.command.registry.Registry;
import ru.recoin.core.Main;
import ru.recoin.implement.features.commands.argument.ArgConsumer;
import ru.recoin.implement.features.commands.argument.CommandArguments;
import ru.recoin.implement.features.commands.defaults.DefaultCommands;

public class CommandRepository
implements ICommandManager {
    private final Registry<ICommand> registry = new Registry();

    public CommandRepository() {
        DefaultCommands.createAll().forEach(this.registry::register);
    }

    @Override
    public Registry<ICommand> getRegistry() {
        return this.registry;
    }

    @Override
    public ICommand getCommand(String name) {
        for (ICommand command : this.registry.entries) {
            if (!command.getNames().contains(name.toLowerCase(Locale.US))) continue;
            return command;
        }
        return null;
    }

    @Override
    public boolean execute(String string) {
        return this.execute(CommandRepository.expand(string));
    }

    @Override
    public boolean execute(class_3545<String, List<ICommandArgument>> expanded) {
        ExecutionWrapper execution = this.from(expanded);
        if (execution != null) {
            execution.execute();
        }
        return execution != null;
    }

    @Override
    public Stream<String> tabComplete(class_3545<String, List<ICommandArgument>> expanded) {
        ExecutionWrapper execution = this.from(expanded);
        return execution == null ? Stream.empty() : execution.tabComplete();
    }

    @Override
    public Stream<String> tabComplete(String prefix) {
        class_3545<String, List<ICommandArgument>> pair = CommandRepository.expand(prefix, true);
        String label = (String)pair.method_15442();
        List args2 = (List)pair.method_15441();
        if (args2.isEmpty()) {
            return new TabCompleteHelper().addCommands(Main.getInstance().getCommandRepository()).filterPrefix(label).stream();
        }
        return this.tabComplete(pair);
    }

    private ExecutionWrapper from(class_3545<String, List<ICommandArgument>> expanded) {
        String label = (String)expanded.method_15442();
        ArgConsumer args2 = new ArgConsumer(this, (List)expanded.method_15441());
        ICommand command = this.getCommand(label);
        return command == null ? null : new ExecutionWrapper(command, label, args2);
    }

    private static class_3545<String, List<ICommandArgument>> expand(String string, boolean preserveEmptyLast) {
        String label = string.split("\\s", 2)[0];
        List<ICommandArgument> args2 = CommandArguments.from(string.substring(label.length()), preserveEmptyLast);
        return new class_3545((Object)label, args2);
    }

    public static class_3545<String, List<ICommandArgument>> expand(String string) {
        return CommandRepository.expand(string, false);
    }

    private static final class ExecutionWrapper {
        private ICommand command;
        private String label;
        private ArgConsumer args;

        private ExecutionWrapper(ICommand command, String label, ArgConsumer args2) {
            this.command = command;
            this.label = label;
            this.args = args2;
        }

        private void execute() {
            try {
                this.command.execute(this.label, this.args);
            }
            catch (Throwable t) {
                ICommandException exception = t instanceof ICommandException ? (ICommandException)((Object)t) : new CommandUnhandledException(t);
                exception.handle(this.command, this.args.getArgs());
            }
        }

        private Stream<String> tabComplete() {
            try {
                return this.command.tabComplete(this.label, this.args);
            }
            catch (CommandException commandException) {
            }
            catch (Throwable t) {
                t.printStackTrace();
            }
            return Stream.empty();
        }
    }
}

