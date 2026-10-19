/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_1299
 *  net.minecraft.class_2248
 *  net.minecraft.class_2561
 *  net.minecraft.class_5250
 */
package ru.recoin.implement.features.commands.defaults;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.class_124;
import net.minecraft.class_1299;
import net.minecraft.class_2248;
import net.minecraft.class_2561;
import net.minecraft.class_5250;
import ru.recoin.api.feature.command.Command;
import ru.recoin.api.feature.command.IBaritoneChatControl;
import ru.recoin.api.feature.command.argument.IArgConsumer;
import ru.recoin.api.feature.command.datatypes.BlockDataType;
import ru.recoin.api.feature.command.datatypes.BlockESPDataType;
import ru.recoin.api.feature.command.datatypes.BoxColorDataType;
import ru.recoin.api.feature.command.datatypes.EntityDataType;
import ru.recoin.api.feature.command.datatypes.EntityESPDataType;
import ru.recoin.api.feature.command.exception.CommandException;
import ru.recoin.api.feature.command.helpers.Paginator;
import ru.recoin.api.feature.command.helpers.TabCompleteHelper;
import ru.recoin.api.repository.box.BoxESPRepository;
import ru.recoin.common.QuickImports;
import ru.recoin.core.Main;

public class BoxESPCommand
extends Command
implements QuickImports {
    private final BoxESPRepository repository;

    protected BoxESPCommand(Main main) {
        super("box");
        this.repository = main.getBoxESPRepository();
    }

    @Override
    public void execute(String label, IArgConsumer args2) throws CommandException {
        String arg;
        switch (arg = args2.hasAny() ? args2.getString().toLowerCase(Locale.US) : "list") {
            case "add": {
                this.handleAdd(args2);
                break;
            }
            case "remove": {
                this.handleRemove(args2);
                break;
            }
            case "clear": {
                this.handleClear(args2);
                break;
            }
            case "list": {
                this.handleList(label, args2);
            }
        }
    }

    private void handleAdd(IArgConsumer args2) throws CommandException {
        args2.requireMin(2);
        String type = args2.getString().toLowerCase();
        String name = args2.getString();
        int color = args2.has(1) ? (Integer)args2.getDatatypeFor(BoxColorDataType.INSTANCE) : 0;
        switch (type) {
            case "block": {
                BlockDataType.INSTANCE.findBlock(name).ifPresentOrElse(block -> {
                    if (!this.repository.blocks.containsKey(block)) {
                        this.repository.addBlock((class_2248)block, color);
                        StringBuilder builder = new StringBuilder();
                        builder.append("\u0414\u043e\u0431\u0430\u0432\u043b\u0435\u043d \u0431\u043b\u043e\u043a: ").append(class_124.field_1060).append(block.method_9518().getString()).append(class_124.field_1080);
                        if (color != 0) {
                            builder.append(", \u0426\u0432\u0435\u0442: ").append(class_124.field_1060).append(color);
                        }
                        this.logDirect(builder.toString(), class_124.field_1080);
                    } else {
                        this.logDirect(String.valueOf(class_124.field_1080) + "\u0411\u043b\u043e\u043a \"" + String.valueOf(class_124.field_1061) + block.method_9518().getString() + String.valueOf(class_124.field_1080) + "\" \u0443\u0436\u0435 \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d");
                    }
                }, () -> this.logDirect(String.valueOf(class_124.field_1061) + "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043d\u0430\u0439\u0442\u0438 \u0431\u043b\u043e\u043a"));
                break;
            }
            case "entity": {
                EntityDataType.INSTANCE.findEntity(name).ifPresentOrElse(entity -> {
                    if (!this.repository.entities.containsKey(entity)) {
                        this.repository.entities.put((class_1299<?>)entity, color);
                        StringBuilder builder = new StringBuilder();
                        builder.append("\u0414\u043e\u0431\u0430\u0432\u043b\u0435\u043d\u0430 \u0441\u0443\u0449\u043d\u043e\u0441\u0442\u044c: ").append(class_124.field_1060).append(entity.method_5897().getString()).append(class_124.field_1080);
                        if (color != 0) {
                            builder.append(", \u0426\u0432\u0435\u0442: ").append(class_124.field_1060).append(color);
                        }
                        this.logDirect(builder.toString(), class_124.field_1080);
                    } else {
                        this.logDirect(String.valueOf(class_124.field_1080) + "\u0421\u0443\u0449\u043d\u043e\u0441\u0442\u044c \"" + String.valueOf(class_124.field_1061) + entity.method_5897().getString() + String.valueOf(class_124.field_1080) + "\" \u0443\u0436\u0435 \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d\u0430");
                    }
                }, () -> this.logDirect(String.valueOf(class_124.field_1061) + "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043d\u0430\u0439\u0442\u0438 \u0441\u0443\u0449\u043d\u043e\u0441\u0442\u044c"));
                break;
            }
            default: {
                this.logDirect(String.valueOf(class_124.field_1061) + "\u041d\u0435 \u0432\u0435\u0440\u043d\u044b\u0439 \u0442\u0438\u043f");
            }
        }
    }

    private void handleRemove(IArgConsumer args2) throws CommandException {
        args2.requireMin(2);
        String type = args2.getString().toLowerCase();
        String name = args2.getString();
        switch (type) {
            case "block": {
                this.repository.blocks.keySet().stream().filter(block -> block.method_9518().getString().replace(" ", "_").equalsIgnoreCase(name)).findFirst().ifPresentOrElse(block -> {
                    this.repository.removeBlock((class_2248)block);
                    this.logDirect("\u0423\u0434\u0430\u043b\u0435\u043d \u0431\u043b\u043e\u043a: " + String.valueOf(class_124.field_1060) + block.method_9518().getString(), class_124.field_1080);
                }, () -> this.logDirect(String.valueOf(class_124.field_1061) + "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043d\u0430\u0439\u0442\u0438 \u0431\u043b\u043e\u043a"));
                break;
            }
            case "entity": {
                this.repository.entities.keySet().stream().filter(block -> block.method_5897().getString().replace(" ", "_").equalsIgnoreCase(name)).findFirst().ifPresentOrElse(block -> {
                    this.repository.entities.remove(block);
                    this.logDirect("\u0423\u0434\u0430\u043b\u0435\u043d\u0430 \u0441\u0443\u0449\u043d\u043e\u0441\u0442\u044c: " + String.valueOf(class_124.field_1060) + block.method_5897().getString(), class_124.field_1080);
                }, () -> this.logDirect(String.valueOf(class_124.field_1061) + "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043d\u0430\u0439\u0442\u0438 \u0441\u0443\u0449\u043d\u043e\u0441\u0442\u044c"));
                break;
            }
            default: {
                this.logDirect(String.valueOf(class_124.field_1061) + "\u041d\u0435 \u0432\u0435\u0440\u043d\u044b\u0439 \u0442\u0438\u043f");
            }
        }
    }

    private void handleList(String label, IArgConsumer args2) throws CommandException {
        String type;
        args2.requireMin(1);
        switch (type = args2.getString().toLowerCase()) {
            case "block": {
                Paginator.paginate(args2, new Paginator<>(this.repository.blocks.entrySet().stream().toList()), () -> this.logDirect("\u0421\u043f\u0438\u0441\u043e\u043a \u0431\u043b\u043e\u043a\u043e\u0432:"), block -> {
                    class_5250 text = class_2561.method_43470((String)(String.valueOf(class_124.field_1080) + "\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435: " + String.valueOf(class_124.field_1061) + ((class_2248)block.getKey()).method_9518().getString()));
                    if ((Integer)block.getValue() != 0) {
                        text.method_27661().method_27693(String.valueOf(class_124.field_1080) + "\u0426\u0432\u0435\u0442: " + String.valueOf(class_124.field_1061) + String.valueOf(block.getValue()));
                    }
                    return text;
                }, IBaritoneChatControl.FORCE_COMMAND_PREFIX + label);
                break;
            }
            case "entity": {
                Paginator.paginate(args2, new Paginator<>(this.repository.entities.entrySet().stream().toList()), () -> this.logDirect("\u0421\u043f\u0438\u0441\u043e\u043a \u0421\u0443\u0449\u043d\u043e\u0441\u0442\u0435\u0439:"), entity -> {
                    class_5250 text = class_2561.method_43470((String)(String.valueOf(class_124.field_1080) + "\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435: " + String.valueOf(class_124.field_1061) + ((class_1299)entity.getKey()).method_5897().getString()));
                    if ((Integer)entity.getValue() != 0) {
                        text.method_27661().method_27693(String.valueOf(class_124.field_1080) + "\u0426\u0432\u0435\u0442: " + String.valueOf(class_124.field_1061) + String.valueOf(entity.getValue()));
                    }
                    return text;
                }, IBaritoneChatControl.FORCE_COMMAND_PREFIX + label);
                break;
            }
            default: {
                this.logDirect(String.valueOf(class_124.field_1061) + "\u041d\u0435 \u0432\u0435\u0440\u043d\u044b\u0439 \u0442\u0438\u043f");
            }
        }
    }

    private void handleClear(IArgConsumer args2) throws CommandException {
        String type;
        args2.requireMin(1);
        switch (type = args2.getString().toLowerCase()) {
            case "block": {
                this.repository.clearBlocks();
                this.logDirect(String.valueOf(class_124.field_1060) + "\u0412\u0441\u0435 \u0431\u043b\u043e\u043a\u0438 \u0431\u044b\u043b\u0438 \u0443\u0434\u0430\u043b\u0435\u043d\u044b.");
                break;
            }
            case "entity": {
                this.repository.entities.clear();
                this.logDirect(String.valueOf(class_124.field_1060) + "\u0412\u0441\u0435 \u0441\u0443\u0449\u043d\u043e\u0441\u0442\u0438 \u0431\u044b\u043b\u0438 \u0443\u0434\u0430\u043b\u0435\u043d\u044b.");
                break;
            }
            default: {
                this.logDirect(String.valueOf(class_124.field_1061) + "\u041d\u0435 \u0432\u0435\u0440\u043d\u044b\u0439 \u0442\u0438\u043f");
            }
        }
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args2) throws CommandException {
        String arg = args2.getString();
        if (args2.has(4)) {
            return Stream.empty();
        }
        if (args2.has(3)) {
            args2.getString();
            args2.getString();
            return args2.tabCompleteDatatype(BoxColorDataType.INSTANCE);
        }
        if (args2.has(2)) {
            String nextArg;
            switch (nextArg = args2.getString().toLowerCase()) {
                case "block": {
                    if (arg.equalsIgnoreCase("remove")) {
                        return args2.tabCompleteDatatype(BlockESPDataType.INSTANCE);
                    }
                    if (!arg.equalsIgnoreCase("add")) break;
                    return args2.tabCompleteDatatype(BlockDataType.INSTANCE);
                }
                case "entity": {
                    if (arg.equalsIgnoreCase("remove")) {
                        return args2.tabCompleteDatatype(EntityESPDataType.INSTANCE);
                    }
                    if (!arg.equalsIgnoreCase("add")) break;
                    return args2.tabCompleteDatatype(EntityDataType.INSTANCE);
                }
            }
        } else {
            if (args2.hasAny()) {
                return new TabCompleteHelper().append("Block", "Entity").filterPrefix(args2.getString()).sortAlphabetically().stream();
            }
            return new TabCompleteHelper().append("add", "remove", "list", "clear").filterPrefix(arg).sortAlphabetically().stream();
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "\u041f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u043e\u0442\u043e\u0431\u0440\u0430\u0436\u0430\u0442\u044c \u0431\u043e\u043a\u0441\u044b \u0432 \u043c\u0438\u0440\u0435";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList("\u0421 \u043f\u043e\u043c\u043e\u0449\u044c\u044e \u044d\u0442\u043e\u0439 \u043a\u043e\u043c\u0430\u043d\u0434\u044b \u043c\u043e\u0436\u043d\u043e \u0434\u043e\u0431\u0430\u0432\u043b\u044f\u0442\u044c/\u0443\u0434\u0430\u043b\u044f\u0442\u044c \u0440\u0435\u043d\u0434\u0435\u0440 \u0431\u043e\u043a\u0441\u043e\u0432 \u0432 \u043c\u0438\u0440\u0435", "", "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435:", "> box add <Block/Entity> <block/entity> <color> - \u0414\u043e\u0431\u0430\u0432\u043b\u044f\u0435\u0442 \u0431\u043b\u043e\u043a/\u0441\u0443\u0449\u043d\u043e\u0441\u0442\u044c", "> box remove <Block/Entity> <block/entity> - \u0423\u0434\u0430\u043b\u044f\u0435\u0442 \u0431\u043b\u043e\u043a/\u0441\u0443\u0449\u043d\u043e\u0441\u0442\u044c", "> box list <Block/Entity> - \u0412\u043e\u0437\u0432\u0440\u0430\u0449\u0430\u0435\u0442 \u0441\u043f\u0438\u0441\u043e\u043a \u0431\u043b\u043e\u043a\u043e\u0432/\u0441\u0443\u0449\u043d\u043e\u0441\u0442\u0435\u0439", "> box clear <Block/Entity> - \u041e\u0447\u0438\u0449\u0430\u0435\u0442 \u0431\u043b\u043e\u043a\u0438/\u0441\u0443\u0449\u043d\u043e\u0441\u0442\u0438");
    }
}

