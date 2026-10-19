/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_5250
 */
package ru.recoin.api.system.logger.implement;

import java.util.Arrays;
import net.minecraft.class_2561;
import net.minecraft.class_5250;
import ru.recoin.api.system.logger.Logger;
import ru.recoin.common.QuickImports;
import ru.recoin.common.QuickLogger;

public class MinecraftLogger
implements Logger,
QuickImports {
    @Override
    public void log(Object message) {
    }

    @Override
    public void minecraftLog(class_2561 ... components) {
        if (MinecraftLogger.mc.field_1724 != null) {
            class_5250 component = class_2561.method_43470((String)"");
            Arrays.asList(components).forEach(arg_0 -> ((class_5250)component).method_10852(arg_0));
            MinecraftLogger.mc.field_1705.method_1743().method_1812(QuickLogger.clientMessage((class_2561)component));
        }
    }
}

