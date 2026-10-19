/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_12079$class_10726
 *  net.minecraft.class_12079$class_12081
 *  net.minecraft.class_1297
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_742
 *  net.minecraft.class_8685
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.recoin.mixins;

import net.minecraft.class_12079;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_742;
import net.minecraft.class_8685;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.recoin.api.repository.friend.FriendUtils;

@Mixin(value={class_742.class})
public class AbstractClientPlayerEntityMixin {
    private static final class_12079.class_10726 RECOIN_CAPE = new class_12079.class_10726(class_2960.method_60655((String)"minecraft", (String)"capes/recoin"));

    @Inject(method={"method_52814"}, at={@At(value="RETURN")}, cancellable=true)
    private void recoin$exposeCapeToRenderMods(CallbackInfoReturnable<class_8685> cir) {
        class_742 player = (class_742)(Object)this;
        class_310 client = class_310.method_1551();
        if (!(client.field_1724 != null && player.method_5667().equals(client.field_1724.method_5667()) || FriendUtils.isFriend((class_1297)player))) {
            return;
        }
        class_8685 skin = (class_8685)cir.getReturnValue();
        if (skin != null) {
            cir.setReturnValue(new class_8685(skin.comp_1626(), (class_12079.class_12081)RECOIN_CAPE, (class_12079.class_12081)RECOIN_CAPE, skin.comp_1629(), skin.comp_1630()));
        }
    }
}

