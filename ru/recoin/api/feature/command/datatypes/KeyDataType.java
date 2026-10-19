/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap$Entry
 *  net.minecraft.class_3675$class_307
 */
package ru.recoin.api.feature.command.datatypes;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.class_3675;
import ru.recoin.api.feature.command.datatypes.IDatatypeContext;
import ru.recoin.api.feature.command.datatypes.IDatatypeFor;
import ru.recoin.api.feature.command.exception.CommandException;
import ru.recoin.api.feature.command.helpers.TabCompleteHelper;
import ru.recoin.common.util.other.StringUtil;

public enum KeyDataType implements IDatatypeFor<Map.Entry<String, Integer>>
{
    INSTANCE;


    @Override
    public Stream<String> tabComplete(IDatatypeContext datatypeContext) throws CommandException {
        Stream<String> keys2 = KeyDataType.getKeys().keySet().stream();
        String context = datatypeContext.getConsumer().getString();
        return new TabCompleteHelper().append(keys2).filterPrefix(context).sortAlphabetically().stream();
    }

    @Override
    public Map.Entry<String, Integer> get(IDatatypeContext datatypeContext) throws CommandException {
        String key = datatypeContext.getConsumer().getString();
        return KeyDataType.getKeys().entrySet().stream().filter(s -> ((String)s.getKey()).equalsIgnoreCase(key)).findFirst().orElse(null);
    }

    private static Map<String, Integer> getKeys() {
        HashMap<String, Integer> keys2 = new HashMap<String, Integer>();
        for (Int2ObjectMap.Entry entry : class_3675.class_307.field_1668.field_1674.int2ObjectEntrySet()) {
            int keyCode = entry.getIntKey();
            String bindName = StringUtil.getBindName(keyCode).toLowerCase();
            keys2.put(bindName, keyCode);
        }
        return keys2;
    }
}

