/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_2558
 *  net.minecraft.class_2558$class_10609
 *  net.minecraft.class_2561
 *  net.minecraft.class_2568
 *  net.minecraft.class_2568$class_10613
 *  net.minecraft.class_5250
 */
package ru.recoin.implement.features.commands.defaults;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.class_124;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_2568;
import net.minecraft.class_5250;
import ru.recoin.api.feature.command.Command;
import ru.recoin.api.feature.command.IBaritoneChatControl;
import ru.recoin.api.feature.command.ICommand;
import ru.recoin.api.feature.command.argument.IArgConsumer;
import ru.recoin.api.feature.command.exception.CommandException;
import ru.recoin.api.feature.command.exception.CommandNotFoundException;
import ru.recoin.api.feature.command.helpers.TabCompleteHelper;
import ru.recoin.core.Main;
import ru.recoin.implement.features.commands.manager.CommandRepository;

public class HelpCommand
extends Command {
    private final Main main;

    protected HelpCommand(Main main) {
        super("help");
        this.main = main;
    }

    @Override
    public void execute(String label, IArgConsumer args2) throws CommandException {
        args2.requireMax(1);
        CommandRepository repository = this.main.getCommandRepository();
        if (!args2.hasAny()) {
            this.logDirect(new class_2561[]{class_2561.method_43470((String)"\u0412\u0441\u0435 \u043a\u043e\u043c\u0430\u043d\u0434\u044b \u043a\u043b\u0438\u0435\u043d\u0442\u0430:").method_27692(class_124.field_1068)});
            repository.getRegistry().descendingStream().filter(command -> !command.hiddenFromHelp()).forEach(command -> this.logDirect(new class_2561[]{this.commandLine(label, (ICommand)command)}));
            return;
        }
        String commandName = args2.getString().toLowerCase();
        ICommand command2 = repository.getCommand(commandName);
        if (command2 == null) {
            throw new CommandNotFoundException(commandName);
        }
        this.logDirect(new class_2561[]{class_2561.method_43470((String)String.join((CharSequence)"/", command2.getNames())).method_27692(class_124.field_1068)});
        command2.getLongDesc().forEach(line -> this.logDirect(new class_2561[]{class_2561.method_43470((String)line).method_27692(class_124.field_1080)}));
    }

    private class_5250 commandLine(String label, ICommand command) {
        String names = String.join((CharSequence)"/", command.getNames());
        class_5250 hover = class_2561.method_43470((String)(names + "\n" + command.getShortDesc() + "\n\n\u041d\u0430\u0436\u043c\u0438\u0442\u0435, \u0447\u0442\u043e\u0431\u044b \u043e\u0442\u043a\u0440\u044b\u0442\u044c \u043f\u043e\u0434\u0440\u043e\u0431\u043d\u0443\u044e \u0441\u043f\u0440\u0430\u0432\u043a\u0443."));
        class_5250 line = class_2561.method_43470((String)("." + names)).method_27692(class_124.field_1068).method_10852((class_2561)class_2561.method_43470((String)(" \u2014 " + command.getShortDesc())).method_27692(class_124.field_1080));
        return line.method_27694(style -> style.method_10949((class_2568)new class_2568.class_10613((class_2561)hover)).method_10958((class_2558)new class_2558.class_10609(IBaritoneChatControl.FORCE_COMMAND_PREFIX + label + " " + command.getNames().getFirst())));
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args2) throws CommandException {
        if (args2.hasExactlyOne()) {
            return new TabCompleteHelper().addCommands(Main.getInstance().getCommandRepository()).filterPrefix(args2.getString()).stream();
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0435\u0442 \u0432\u0441\u0435 \u043a\u043e\u043c\u0430\u043d\u0434\u044b \u043a\u043b\u0438\u0435\u043d\u0442\u0430";
    }

    @Override
    public List<String> getLongDesc() {
        return List.of("\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0435\u0442 \u0441\u043f\u0438\u0441\u043e\u043a \u0432\u0441\u0435\u0445 \u0434\u043e\u0441\u0442\u0443\u043f\u043d\u044b\u0445 \u043a\u043e\u043c\u0430\u043d\u0434.", ".help \u2014 \u043f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u0432\u0441\u0435 \u043a\u043e\u043c\u0430\u043d\u0434\u044b", ".help <\u043a\u043e\u043c\u0430\u043d\u0434\u0430> \u2014 \u043f\u043e\u0434\u0440\u043e\u0431\u043d\u0430\u044f \u0441\u043f\u0440\u0430\u0432\u043a\u0430");
    }
}

