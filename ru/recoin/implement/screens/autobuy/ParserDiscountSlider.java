/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_357
 */
package ru.recoin.implement.screens.autobuy;

import net.minecraft.class_2561;
import net.minecraft.class_357;
import ru.recoin.implement.features.modules.player.AutoBuy;

public final class ParserDiscountSlider
extends class_357 {
    private final AutoBuy autoBuy;

    public ParserDiscountSlider(int x, int y, int width, int height, AutoBuy autoBuy) {
        super(x, y, width, height, (class_2561)class_2561.method_43473(), (double)autoBuy.getParserDiscount() / 100.0);
        this.autoBuy = autoBuy;
        this.method_25346();
    }

    protected void method_25346() {
        this.method_25355((class_2561)class_2561.method_43470((String)("\u041f\u0430\u0440\u0441\u0435\u0440: " + (int)Math.round(this.field_22753 * 100.0) + "%")));
    }

    protected void method_25344() {
        this.autoBuy.setParserDiscount((int)Math.round(this.field_22753 * 100.0));
    }
}

