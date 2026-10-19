/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_2561
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package ru.recoin.api.system.logger.implement;

import net.minecraft.class_124;
import net.minecraft.class_2561;
import org.apache.logging.log4j.LogManager;
import ru.recoin.api.system.logger.Logger;

public class ConsoleLogger
implements Logger {
    private final org.apache.logging.log4j.Logger logger = LogManager.getLogger((String)"RECOIN");

    @Override
    public void log(Object message) {
        this.logger.info("[RE{}CO{}IN]{}", (Object)class_124.field_1078, (Object)class_124.field_1061, message);
    }

    @Override
    public void minecraftLog(class_2561 ... components) {
    }
}

