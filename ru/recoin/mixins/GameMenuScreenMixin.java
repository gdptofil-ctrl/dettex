/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_339
 *  net.minecraft.class_364
 *  net.minecraft.class_412
 *  net.minecraft.class_4185
 *  net.minecraft.class_433
 *  net.minecraft.class_437
 *  net.minecraft.class_442
 *  net.minecraft.class_639
 *  net.minecraft.class_642
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_364;
import net.minecraft.class_412;
import net.minecraft.class_4185;
import net.minecraft.class_433;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.minecraft.class_639;
import net.minecraft.class_642;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.implement.features.modules.misc.PvPSafe;
import ru.recoin.implement.features.modules.misc.SelfDestruct;

@Mixin(value={class_433.class})
public abstract class GameMenuScreenMixin
extends class_437 {
    @Unique
    private boolean recoin$reconnect;

    protected GameMenuScreenMixin(class_2561 title) {
        super(title);
    }

    @Inject(method={"method_20543"}, at={@At(value="TAIL")})
    private void recoin$addReconnectButton(CallbackInfo ci) {
        if (SelfDestruct.isLocked()) {
            return;
        }
        class_310 client = class_310.method_1551();
        class_642 serverEntry = client.method_1558();
        if (serverEntry == null) {
            return;
        }
        class_339 disconnectWidget = null;
        for (class_364 child : this.method_25396()) {
            class_339 widget;
            if (!(child instanceof class_339) || !(widget = (class_339)child).method_25369().getString().equals(class_2561.method_43471((String)"menu.disconnect").getString())) continue;
            disconnectWidget = widget;
            break;
        }
        if (disconnectWidget != null) {
            int buttonWidth = disconnectWidget.method_25368();
            int buttonHeight = 20;
            int buttonX = disconnectWidget.method_46426();
            int buttonY = disconnectWidget.method_46427() + disconnectWidget.method_25364() + 4;
            class_4185 reconnectBtn = class_4185.method_46430((class_2561)class_2561.method_43470((String)"\u041f\u0435\u0440\u0435\u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0438\u0442\u044c\u0441\u044f"), button -> {
                if (PvPSafe.getInstance().shouldDisableDisconnectButton() && !this.recoin$reconnect) {
                    button.method_25355((class_2561)class_2561.method_43470((String)"\u041f\u0435\u0440\u0435\u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0438\u0442\u044c\u0441\u044f").method_27692(class_124.field_1061));
                    this.recoin$reconnect = true;
                    return;
                }
                try {
                    if (client.field_1687 != null) {
                        client.field_1687.method_8525((class_2561)class_2561.method_43470((String)"Disconnecting"));
                    }
                    if (client.method_1562() != null && client.method_1562().method_48296() != null) {
                        client.method_1562().method_48296().method_10747((class_2561)class_2561.method_43470((String)"Disconnecting"));
                    }
                    client.method_76795((class_437)new class_442(), false);
                    class_412.method_36877((class_437)new class_442(), (class_310)client, (class_639)class_639.method_2950((String)serverEntry.field_3761), (class_642)serverEntry, (boolean)false, null);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }).method_46434(buttonX, buttonY, buttonWidth, buttonHeight).method_46431();
            this.method_37063(reconnectBtn);
        }
    }

    @Inject(method={"method_25394"}, at={@At(value="TAIL")})
    private void recoin$disableDisconnectButton(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        boolean blocked = PvPSafe.getInstance().shouldDisableDisconnectButton();
        this.method_25396().forEach(child -> {
            class_339 widget;
            String text;
            if (child instanceof class_339 && ((text = (widget = (class_339)child).method_25369().getString()).equals(class_2561.method_43471((String)"menu.disconnect").getString()) || text.equals("\u041f\u0435\u0440\u0435\u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0438\u0442\u044c\u0441\u044f"))) {
                widget.field_22763 = !blocked;
            }
        });
    }
}

