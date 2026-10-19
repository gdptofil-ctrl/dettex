/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 */
package ru.recoin.implement.features.commands.defaults;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.class_124;
import ru.recoin.api.feature.command.Command;
import ru.recoin.api.feature.command.argument.IArgConsumer;
import ru.recoin.api.feature.command.exception.CommandException;
import ru.recoin.api.feature.command.helpers.TabCompleteHelper;
import ru.recoin.common.util.world.GpsMarker;

public class GpsCommand
extends Command {
    public GpsCommand() {
        super("gps");
    }

    @Override
    public void execute(String label, IArgConsumer args2) throws CommandException {
        args2.requireMin(1);
        if (args2.hasExactlyOne() && (args2.peekString().equalsIgnoreCase("clear") || args2.peekString().equalsIgnoreCase("off"))) {
            args2.getString();
            GpsMarker.clear();
            this.logDirect("GPS \u043c\u0435\u0442\u043a\u0430 \u0443\u0434\u0430\u043b\u0435\u043d\u0430", class_124.field_1080);
            return;
        }
        args2.requireMax(2);
        int x = args2.getAs(Integer.class);
        int z = args2.getAs(Integer.class);
        GpsMarker.set(x, z);
        this.logDirect("GPS \u043c\u0435\u0442\u043a\u0430 \u0443\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u0430: X " + String.valueOf(class_124.field_1061) + x + String.valueOf(class_124.field_1080) + " Z " + String.valueOf(class_124.field_1061) + z, class_124.field_1080);
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args2) throws CommandException {
        if (!args2.hasAny()) {
            return Stream.empty();
        }
        if (args2.hasExactlyOne()) {
            return new TabCompleteHelper().prepend("off", "clear", "x").filterPrefix(args2.peekString().toLowerCase(Locale.US)).stream();
        }
        if (args2.hasExactly(2)) {
            return new TabCompleteHelper().prepend("z").stream();
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "\u0421\u0442\u0430\u0432\u0438\u0442 GPS \u043c\u0435\u0442\u043a\u0443 \u043f\u043e \u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u0430\u043c X Z";
    }

    @Override
    public List<String> getLongDesc() {
        return List.of("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435:", "> gps <x> <z> - \u0441\u0442\u0430\u0432\u0438\u0442 GPS \u043c\u0435\u0442\u043a\u0443", "> gps off - \u0443\u0434\u0430\u043b\u044f\u0435\u0442 GPS \u043c\u0435\u0442\u043a\u0443", "> gps clear - \u0443\u0434\u0430\u043b\u044f\u0435\u0442 GPS \u043c\u0435\u0442\u043a\u0443");
    }
}

