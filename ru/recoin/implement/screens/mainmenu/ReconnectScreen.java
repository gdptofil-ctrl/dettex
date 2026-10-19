/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_364
 *  net.minecraft.class_412
 *  net.minecraft.class_4185
 *  net.minecraft.class_437
 *  net.minecraft.class_442
 *  net.minecraft.class_639
 *  net.minecraft.class_642
 */
package ru.recoin.implement.screens.mainmenu;

import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_364;
import net.minecraft.class_412;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.minecraft.class_639;
import net.minecraft.class_642;

public class ReconnectScreen
extends class_437 {
    private final class_642 serverInfo;
    private final long targetTime;
    private boolean connecting;

    public ReconnectScreen(class_642 serverInfo, long delayMs) {
        super((class_2561)class_2561.method_43470((String)"Reconnecting"));
        this.serverInfo = serverInfo;
        this.targetTime = System.currentTimeMillis() + delayMs;
    }

    protected void method_25426() {
        this.method_37063(class_4185.method_46430((class_2561)class_2561.method_43471((String)"gui.cancel"), button -> {
            this.connecting = true;
            if (this.field_22787 != null) {
                this.field_22787.method_1507((class_437)new class_442());
            }
        }).method_46434(this.field_22789 / 2 - 100, this.field_22790 / 2 + 20, 200, 20).method_46431());
    }

    public void method_25393() {
        if (!this.connecting && this.field_22787 != null && System.currentTimeMillis() >= this.targetTime) {
            this.connecting = true;
            class_412.method_36877((class_437)new class_442(), (class_310)this.field_22787, (class_639)class_639.method_2950((String)this.serverInfo.field_3761), (class_642)this.serverInfo, (boolean)false, null);
        }
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        super.method_25394(context, mouseX, mouseY, delta);
        long remaining = Math.max(0L, this.targetTime - System.currentTimeMillis());
        String text = String.format("\u041f\u0435\u0440\u0435\u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u0435 \u0447\u0435\u0440\u0435\u0437 %.1f \u0441\u0435\u043a...", Float.valueOf((float)remaining / 1000.0f));
        context.method_25300(this.field_22793, text, this.field_22789 / 2, this.field_22790 / 2 - 10, 0xFFFFFF);
    }
}

