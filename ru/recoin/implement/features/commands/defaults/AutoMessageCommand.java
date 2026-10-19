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
import ru.recoin.implement.features.modules.misc.AutoMessage;

public final class AutoMessageCommand
extends Command {
    private static final int MAX_MESSAGE_LENGTH = 256;

    public AutoMessageCommand() {
        super("automessage");
    }

    @Override
    public void execute(String label, IArgConsumer args2) throws CommandException {
        AutoMessage autoMessage = AutoMessage.getInstance();
        if (autoMessage == null) {
            this.logDirect("[AutoMessage] \u041c\u043e\u0434\u0443\u043b\u044c \u043d\u0435 \u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d", class_124.field_1061);
            return;
        }
        String message = this.unwrapQuotes(args2.rawRest().trim());
        if (message.isEmpty()) {
            String current = autoMessage.getMessage();
            this.logDirect("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435: .AutoMessage <\u0442\u0435\u043a\u0441\u0442>", class_124.field_1080);
            if (!current.isBlank()) {
                this.logDirect("\u0422\u0435\u043a\u0443\u0449\u0438\u0439 \u0442\u0435\u043a\u0441\u0442: " + current, class_124.field_1068);
            }
            return;
        }
        if (message.length() > 256) {
            this.logDirect("[AutoMessage] \u041c\u0430\u043a\u0441\u0438\u043c\u0430\u043b\u044c\u043d\u0430\u044f \u0434\u043b\u0438\u043d\u0430 \u2014 256 \u0441\u0438\u043c\u0432\u043e\u043b\u043e\u0432", class_124.field_1061);
            return;
        }
        autoMessage.setMessage(message);
        this.logDirect("[AutoMessage] \u0422\u0435\u043a\u0441\u0442 \u0441\u043e\u0445\u0440\u0430\u043d\u0451\u043d: " + message, class_124.field_1060);
    }

    private String unwrapQuotes(String value) {
        if (value.length() >= 2 && (value.startsWith("\"") && value.endsWith("\"") || value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1).trim();
        }
        return value;
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args2) {
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "\u0421\u043e\u0445\u0440\u0430\u043d\u044f\u0435\u0442 \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435 \u0434\u043b\u044f \u0431\u0438\u043d\u0434\u0430 AutoMessage";
    }

    @Override
    public List<String> getLongDesc() {
        return List.of("> AutoMessage <\u0442\u0435\u043a\u0441\u0442> \u2014 \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435 \u0434\u043b\u044f \u043e\u0442\u043f\u0440\u0430\u0432\u043a\u0438 \u043f\u043e \u0431\u0438\u043d\u0434\u0443");
    }
}

