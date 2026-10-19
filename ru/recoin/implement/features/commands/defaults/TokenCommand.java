/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 */
package ru.recoin.implement.features.commands.defaults;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.class_124;
import ru.recoin.api.feature.command.Command;
import ru.recoin.api.feature.command.argument.IArgConsumer;
import ru.recoin.api.feature.command.exception.CommandException;
import ru.recoin.implement.features.modules.misc.TelegramAPI;

public final class TokenCommand
extends Command {
    public TokenCommand() {
        super("token");
    }

    @Override
    public void execute(String label, IArgConsumer args2) throws CommandException {
        String value = args2.rawRest().trim();
        if (value.startsWith("\"") && value.endsWith("\"") || value.startsWith("'") && value.endsWith("'")) {
            String string = value = value.length() > 1 ? value.substring(1, value.length() - 1).trim() : "";
        }
        if (value.isEmpty()) {
            this.logDirect("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435: .token \"\u0442\u043e\u043a\u0435\u043d \u043e\u0442 BotFather\"", class_124.field_1080);
            return;
        }
        TelegramAPI telegram = TelegramAPI.getInstance();
        if (telegram == null) {
            this.logDirect("TelegramAPI \u043d\u0435 \u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d", class_124.field_1061);
            return;
        }
        telegram.setBotToken(value);
        this.logDirect("Bot Token \u0441\u043e\u0445\u0440\u0430\u043d\u0451\u043d. \u0422\u0435\u043f\u0435\u0440\u044c \u043c\u043e\u0436\u043d\u043e \u0432\u043a\u043b\u044e\u0447\u0438\u0442\u044c Telegram API.", class_124.field_1060);
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args2) {
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "\u0423\u0441\u0442\u0430\u043d\u0430\u0432\u043b\u0438\u0432\u0430\u0435\u0442 \u0442\u043e\u043a\u0435\u043d Telegram-\u0431\u043e\u0442\u0430";
    }

    @Override
    public List<String> getLongDesc() {
        return List.of("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435:", "> token \"\u0442\u043e\u043a\u0435\u043d \u043e\u0442 BotFather\"");
    }
}

