/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.commands.defaults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import ru.recoin.api.feature.command.Command;
import ru.recoin.api.feature.command.ICommand;
import ru.recoin.core.Main;
import ru.recoin.implement.features.commands.defaults.AiRotationCommand;
import ru.recoin.implement.features.commands.defaults.AutoEndCommand;
import ru.recoin.implement.features.commands.defaults.AutoMessageCommand;
import ru.recoin.implement.features.commands.defaults.BindCommand;
import ru.recoin.implement.features.commands.defaults.BoxESPCommand;
import ru.recoin.implement.features.commands.defaults.ConfigCommand;
import ru.recoin.implement.features.commands.defaults.DebugCommand;
import ru.recoin.implement.features.commands.defaults.FriendCommand;
import ru.recoin.implement.features.commands.defaults.GpsCommand;
import ru.recoin.implement.features.commands.defaults.HelpCommand;
import ru.recoin.implement.features.commands.defaults.ItemSearcherCommand;
import ru.recoin.implement.features.commands.defaults.MacroCommand;
import ru.recoin.implement.features.commands.defaults.PrefixCommand;
import ru.recoin.implement.features.commands.defaults.RCTCommand;
import ru.recoin.implement.features.commands.defaults.SpiderCommand;
import ru.recoin.implement.features.commands.defaults.StaffCommand;
import ru.recoin.implement.features.commands.defaults.TokenCommand;
import ru.recoin.implement.features.commands.defaults.VClipCommand;
import ru.recoin.implement.features.commands.defaults.WardenCommand;
import ru.recoin.implement.features.commands.defaults.WayCommand;

public final class DefaultCommands {
    public static List<ICommand> createAll() {
        Main main = Main.getInstance();
        ArrayList<Command> commands = new ArrayList<Command>(Arrays.asList(new BoxESPCommand(main), new ConfigCommand(main), new MacroCommand(main), new HelpCommand(main), new BindCommand(main), new GpsCommand(), new VClipCommand(), new WayCommand(main), new RCTCommand(main), new FriendCommand(), new StaffCommand(), new ItemSearcherCommand(), new AiRotationCommand(), new TokenCommand(), new PrefixCommand(), new AutoEndCommand(), new AutoMessageCommand(), new WardenCommand(), new DebugCommand(), new SpiderCommand(main)));
        return Collections.unmodifiableList(commands);
    }
}

