/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_1792
 *  net.minecraft.class_1802
 *  net.minecraft.class_2960
 *  net.minecraft.class_7923
 */
package ru.recoin.implement.features.commands.defaults;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.class_124;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2960;
import net.minecraft.class_7923;
import ru.recoin.api.feature.command.Command;
import ru.recoin.api.feature.command.argument.IArgConsumer;
import ru.recoin.api.feature.command.exception.CommandException;
import ru.recoin.api.feature.command.helpers.TabCompleteHelper;
import ru.recoin.core.Main;
import ru.recoin.implement.features.modules.movement.Spider;

public class SpiderCommand
extends Command {
    private final Spider spider;

    public SpiderCommand(Main main) {
        super("spider");
        this.spider = main.getModuleProvider().get(Spider.class);
    }

    @Override
    public void execute(String label, IArgConsumer args2) throws CommandException {
        String query = args2.rawRest().trim();
        if (query.isEmpty()) {
            class_1792 current = this.spider.getCustomBlockItem();
            String name = current != null ? current.method_63680().getString() : "\u043d\u0435 \u0437\u0430\u0434\u0430\u043d";
            this.logDirect(String.valueOf(class_124.field_1080) + "\u0422\u0435\u043a\u0443\u0449\u0438\u0439 \u043f\u0440\u0435\u0434\u043c\u0435\u0442: " + String.valueOf(class_124.field_1068) + name + String.valueOf(class_124.field_1080) + " (" + this.spider.getCustomBlockId() + ")");
            this.logDirect(String.valueOf(class_124.field_1080) + "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435: .spider <item_id>");
            return;
        }
        String normalized = query.toLowerCase(Locale.ROOT).replace(' ', '_');
        class_2960 id = class_2960.method_12829((String)(normalized.contains(":") ? normalized : "minecraft:" + normalized));
        if (id == null || !class_7923.field_41178.method_10250(id) || class_7923.field_41178.method_63535(id) == class_1802.field_8162) {
            this.logDirect(String.valueOf(class_124.field_1061) + "\u041f\u0440\u0435\u0434\u043c\u0435\u0442 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d: " + query);
            return;
        }
        class_1792 item = (class_1792)class_7923.field_41178.method_63535(id);
        this.spider.setCustomBlockId(id.toString());
        this.logDirect(String.valueOf(class_124.field_1060) + "\u041f\u0440\u0435\u0434\u043c\u0435\u0442 \u0434\u043b\u044f Spider \u0443\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d: " + String.valueOf(class_124.field_1068) + item.method_63680().getString() + String.valueOf(class_124.field_1060) + " (" + id.method_12832() + ")");
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args2) {
        String prefix = args2.rawRest().trim().toLowerCase(Locale.ROOT);
        return new TabCompleteHelper().append(class_7923.field_41178.method_10220().filter(item -> item != class_1802.field_8162).map(item -> class_7923.field_41178.method_10221(item).method_12832()).distinct()).filterPrefix(prefix).sortAlphabetically().stream().limit(60L);
    }

    @Override
    public String getShortDesc() {
        return "\u0412\u044b\u0431\u0438\u0440\u0430\u0435\u0442 \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u0434\u043b\u044f \u0440\u0435\u0436\u0438\u043c\u043e\u0432 FunTime \u043c\u043e\u0434\u0443\u043b\u044f Spider";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList("\u0412\u044b\u0431\u0438\u0440\u0430\u0435\u0442 \u043f\u0440\u0435\u0434\u043c\u0435\u0442, \u043a\u043e\u0442\u043e\u0440\u044b\u0439 \u0431\u0443\u0434\u0435\u0442 \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c \u0440\u0435\u0436\u0438\u043c FunTime \u043c\u043e\u0434\u0443\u043b\u044f Spider.", "", "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435:", "> spider <item_id> - \u0437\u0430\u0434\u0430\u0442\u044c \u043f\u0440\u0435\u0434\u043c\u0435\u0442 (\u043d\u0430\u043f\u0440\u0438\u043c\u0435\u0440: oak_button, stone_slab, cobblestone)", "> spider - \u043f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u0442\u0435\u043a\u0443\u0449\u0438\u0439 \u043f\u0440\u0435\u0434\u043c\u0435\u0442");
    }
}

