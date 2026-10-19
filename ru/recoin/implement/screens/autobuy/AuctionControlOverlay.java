/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10799
 *  net.minecraft.class_1657
 *  net.minecraft.class_1792$class_9635
 *  net.minecraft.class_1836
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 */
package ru.recoin.implement.screens.autobuy;

import java.util.List;
import net.minecraft.class_10799;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1836;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import ru.recoin.implement.features.modules.player.AutoBuy;

public final class AuctionControlOverlay {
    private static final class_2960 CONTAINER = class_2960.method_60656((String)"textures/gui/container/generic_54.png");
    private static final int COLUMNS = 9;
    private static final int ROWS = 9;

    private AuctionControlOverlay() {
    }

    public static void renderHistory(class_332 context, AutoBuy autoBuy, int containerX, int containerY) {
        class_310 client = class_310.method_1551();
        int panelWidth = 178;
        int panelHeight = 188;
        int x = Math.max(4, containerX - panelWidth - 4);
        int y = containerY;
        context.method_25294(x, y, x + panelWidth, y + panelHeight, -13158601);
        context.method_25294(x + 2, y + 2, x + panelWidth - 2, y + panelHeight - 2, -1);
        context.method_25294(x + 4, y + 4, x + panelWidth - 4, y + panelHeight - 4, -3750202);
        context.method_51439(client.field_1772, (class_2561)class_2561.method_43470((String)"\u0418\u0441\u0442\u043e\u0440\u0438\u044f \u043f\u043e\u043a\u0443\u043f\u043e\u043a"), x + 8, y + 7, -12566464, false);
        for (int slot = 0; slot < 81; ++slot) {
            context.method_25290(class_10799.field_56883, CONTAINER, x + 8 + slot % 9 * 18, y + 20 + slot / 9 * 18, 7.0f, 17.0f, 18, 18, 256, 256);
        }
        List<AutoBuy.Purchase> history = autoBuy.purchaseHistory();
        for (int i = 0; i < Math.min(81, history.size()); ++i) {
            AutoBuy.Purchase purchase = history.get(i);
            int itemX = x + 9 + i % 9 * 18;
            int itemY = y + 21 + i / 9 * 18;
            context.method_51427(purchase.stack(), itemX, itemY);
            int mouseX = (int)client.field_1729.method_68879(client.method_22683());
            int mouseY = (int)client.field_1729.method_68883(client.method_22683());
            if (mouseX < itemX || mouseX >= itemX + 16 || mouseY < itemY || mouseY >= itemY + 16) continue;
            context.method_51434(client.field_1772, purchase.stack().method_7950(class_1792.class_9635.field_51353, (class_1657)client.field_1724, (class_1836)class_1836.field_41070), mouseX, mouseY);
        }
    }
}

