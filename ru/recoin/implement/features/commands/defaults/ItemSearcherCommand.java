/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_1792
 *  net.minecraft.class_1802
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_7923
 */
package ru.recoin.implement.features.commands.defaults;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.class_124;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_7923;
import ru.recoin.api.feature.command.Command;
import ru.recoin.api.feature.command.argument.IArgConsumer;
import ru.recoin.api.feature.command.exception.CommandException;
import ru.recoin.api.feature.command.helpers.TabCompleteHelper;
import ru.recoin.implement.features.modules.render.ItemSearcher;

public class ItemSearcherCommand
extends Command {
    protected ItemSearcherCommand() {
        super("itemsearcher", "isearch");
    }

    @Override
    public void execute(String label, IArgConsumer args2) throws CommandException {
        String query = args2.rawRest().trim();
        if (query.isEmpty()) {
            this.logDirect("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435: .itemsearcher <\u043f\u0440\u0435\u0434\u043c\u0435\u0442>", class_124.field_1080);
            this.logDirect("\u041f\u0440\u0438\u043c\u0435\u0440: .itemsearcher diamond_sword / .itemsearcher \u0430\u043b\u043c\u0430\u0437\u043d\u044b\u0439 \u043c\u0435\u0447", class_124.field_1080);
            return;
        }
        this.findItem(query).ifPresentOrElse(item -> {
            ItemSearcher.getInstance().startSearch((class_1792)item, query);
            this.logDirect(new class_2561[]{class_2561.method_43470((String)"[ItemSearcher] ").method_27692(class_124.field_1075).method_10852((class_2561)class_2561.method_43470((String)"\u0418\u0449\u0443 ").method_27692(class_124.field_1080)).method_10852((class_2561)item.method_63680().method_27661().method_27692(class_124.field_1060)).method_10852((class_2561)class_2561.method_43470((String)" \u0432 \u0440\u0430\u0434\u0438\u0443\u0441\u0435 20 \u0431\u043b\u043e\u043a\u043e\u0432").method_27692(class_124.field_1080))});
        }, () -> this.logDirect("\u041f\u0440\u0435\u0434\u043c\u0435\u0442 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d: " + query, class_124.field_1061));
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args2) {
        String prefix = args2.rawRest().trim().toLowerCase(Locale.ROOT);
        return new TabCompleteHelper().append(class_7923.field_41178.method_10220().filter(item -> item != class_1802.field_8162).flatMap(item -> Stream.of(class_7923.field_41178.method_10221(item).toString(), class_7923.field_41178.method_10221(item).method_12832(), item.method_63680().getString().replace(" ", "_"))).distinct()).filter(value -> ItemSearcherCommand.normalize(value).startsWith(ItemSearcherCommand.normalize(prefix))).sortAlphabetically().stream().limit(60L);
    }

    @Override
    public String getShortDesc() {
        return "\u0418\u0449\u0435\u0442 \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u0432 \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440\u0430\u0445 \u0440\u044f\u0434\u043e\u043c \u0438 \u043f\u043e\u0434\u0441\u0432\u0435\u0447\u0438\u0432\u0430\u0435\u0442 \u043d\u0430\u0439\u0434\u0435\u043d\u043d\u044b\u0435";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList("\u0418\u0449\u0435\u0442 \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u044b\u0439 \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u0432 \u0438\u0437\u0432\u0435\u0441\u0442\u043d\u044b\u0445 \u043a\u043b\u0438\u0435\u043d\u0442\u0443 \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440\u0430\u0445 \u0432 \u0440\u0430\u0434\u0438\u0443\u0441\u0435 20 \u0431\u043b\u043e\u043a\u043e\u0432.", "", "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435:", "> itemsearcher <\u043f\u0440\u0435\u0434\u043c\u0435\u0442> - \u0437\u0430\u043f\u0443\u0441\u043a\u0430\u0435\u0442 \u043f\u043e\u0438\u0441\u043a", "> isearch <\u043f\u0440\u0435\u0434\u043c\u0435\u0442> - \u043a\u043e\u0440\u043e\u0442\u043a\u0438\u0439 \u0432\u0430\u0440\u0438\u0430\u043d\u0442", "", "\u0412\u0430\u0436\u043d\u043e: \u0441\u0435\u0440\u0432\u0435\u0440 \u043e\u0431\u044b\u0447\u043d\u043e \u043d\u0435 \u043e\u0442\u0434\u0430\u0451\u0442 \u0441\u043e\u0434\u0435\u0440\u0436\u0438\u043c\u043e\u0435 \u0437\u0430\u043a\u0440\u044b\u0442\u044b\u0445 \u0441\u0443\u043d\u0434\u0443\u043a\u043e\u0432, \u043f\u043e\u043a\u0430 \u043a\u043b\u0438\u0435\u043d\u0442 \u0435\u0433\u043e \u043d\u0435 \u0443\u0432\u0438\u0434\u0435\u043b.");
    }

    private Optional<class_1792> findItem(String query) {
        class_2960 id;
        class_1792 item;
        class_2960 directId;
        String normalizedQuery = ItemSearcherCommand.normalize(query);
        class_2960 class_29602 = directId = query.contains(":") ? class_2960.method_12829((String)query.toLowerCase(Locale.ROOT)) : class_2960.method_12829((String)("minecraft:" + query.toLowerCase(Locale.ROOT)));
        if (directId != null && class_7923.field_41178.method_10250(directId) && (item = (class_1792)class_7923.field_41178.method_63535(directId)) != class_1802.field_8162) {
            return Optional.of(item);
        }
        for (class_1792 item2 : class_7923.field_41178) {
            if (item2 == class_1802.field_8162) continue;
            id = class_7923.field_41178.method_10221(item2);
            String path = ItemSearcherCommand.normalize(id.method_12832());
            String fullId = ItemSearcherCommand.normalize(id.toString());
            String translated = ItemSearcherCommand.normalize(item2.method_63680().getString());
            if (!path.equals(normalizedQuery) && !fullId.equals(normalizedQuery) && !translated.equals(normalizedQuery)) continue;
            return Optional.of(item2);
        }
        for (class_1792 item2 : class_7923.field_41178) {
            if (item2 == class_1802.field_8162 || !ItemSearcherCommand.normalize((id = class_7923.field_41178.method_10221(item2)).method_12832()).contains(normalizedQuery) && !ItemSearcherCommand.normalize(item2.method_63680().getString()).contains(normalizedQuery)) continue;
            return Optional.of(item2);
        }
        return Optional.empty();
    }

    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replace("minecraft:", "").replace(' ', '_').replace('-', '_').trim();
    }
}

