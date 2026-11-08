/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_339
 *  net.minecraft.class_364
 *  net.minecraft.class_412
 *  net.minecraft.class_4185
 *  net.minecraft.class_419
 *  net.minecraft.class_437
 *  net.minecraft.class_442
 *  net.minecraft.class_639
 *  net.minecraft.class_642
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_339;
import net.minecraft.class_364;
import net.minecraft.class_412;
import net.minecraft.class_4185;
import net.minecraft.class_419;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.minecraft.class_639;
import net.minecraft.class_642;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.common.util.world.ServerUtil;
import ru.recoin.implement.features.modules.misc.Unhook;

@Mixin(value={class_419.class})
public abstract class DisconnectedScreenMixin
extends class_437 {
    @Unique
    private class_4185 recoin$reconnectButton;

    protected DisconnectedScreenMixin(class_2561 title) {
        super(title);
    }

    @Inject(method={"method_25426"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_419;method_48640()V", shift=At.Shift.BEFORE)})
    private void recoin$addReconnectButton(CallbackInfo ci) {
        if (Unhook.isLocked()) {
            return;
        }
        class_310 client = class_310.method_1551();
        class_642 server = ServerUtil.lastAttemptedServerInfo != null ? ServerUtil.lastAttemptedServerInfo : (client.method_1558() != null ? client.method_1558() : ServerUtil.lastServerInfo);
        if (server != null) {
            this.recoin$reconnectButton = (class_4185)this.method_37063(class_4185.method_46430((class_2561)class_2561.method_43470((String)"\u041f\u0435\u0440\u0435\u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0438\u0442\u044c\u0441\u044f"), btn -> class_412.method_36877((class_437)new class_442(), (class_310)client, (class_639)class_639.method_2950((String)server.field_3761), (class_642)server, (boolean)false, null)).method_46434(this.field_22789 / 2 - 100, this.field_22790 / 2 + 30, 200, 20).method_46431());
        }
    }

    @Inject(method={"method_48640"}, at={@At(value="TAIL")})
    private void recoin$adjustReconnectButtonPosition(CallbackInfo ci) {
        if (this.recoin$reconnectButton != null) {
            int x = this.field_22789 / 2 - 100;
            int maxY = this.method_25396().stream().filter(child -> child instanceof class_339 && child != this.recoin$reconnectButton).map(child -> ((class_339)child).method_46427() + ((class_339)child).method_25364()).max(Integer::compareTo).orElse(this.field_22790 / 2);
            this.recoin$reconnectButton.method_46421(x);
            this.recoin$reconnectButton.method_46419(maxY + 6);
        }
    }
}

