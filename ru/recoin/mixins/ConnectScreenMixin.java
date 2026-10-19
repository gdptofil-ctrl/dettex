/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_412
 *  net.minecraft.class_437
 *  net.minecraft.class_639
 *  net.minecraft.class_642
 *  net.minecraft.class_9112
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_310;
import net.minecraft.class_412;
import net.minecraft.class_437;
import net.minecraft.class_639;
import net.minecraft.class_642;
import net.minecraft.class_9112;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.common.util.world.ServerUtil;

@Mixin(value={class_412.class})
public class ConnectScreenMixin {
    @Inject(method={"method_36877"}, at={@At(value="HEAD")})
    private static void recoin$rememberAttempt(class_437 parent, class_310 client, class_639 address, class_642 info, boolean quickPlay, class_9112 cookies, CallbackInfo ci) {
        ServerUtil.lastAttemptedServerInfo = info;
    }
}

