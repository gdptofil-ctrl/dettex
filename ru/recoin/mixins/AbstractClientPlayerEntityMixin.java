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
import ru.recoin.implement.features.modules.render.cosmetics.Cosmetics;

@Mixin(value={class_742.class})
public class AbstractClientPlayerEntityMixin {
    @Inject(method={"method_52814"}, at={@At(value="RETURN")}, cancellable=true)
    private void recoin$exposeCapeToRenderMods(CallbackInfoReturnable<class_8685> cir) {
        Cosmetics cosmetics = Cosmetics.getInstance();
        if (cosmetics == null) {
            return;
        }
        class_742 player = (class_742)(Object)this;
        class_310 client = class_310.method_1551();
        boolean self = client.field_1724 != null && player.method_5667().equals(client.field_1724.method_5667());
        String name = player.method_5477() == null ? null : player.method_5477().getString();
        class_12079.class_10726 cape = cosmetics.getCapeFor(self, player.method_5667(), name);
        if (cape == null) {
            return;
        }
        class_8685 skin = (class_8685)cir.getReturnValue();
        if (skin != null) {
            cir.setReturnValue(new class_8685(skin.comp_1626(), (class_12079.class_12081)cape, (class_12079.class_12081)cape, skin.comp_1629(), skin.comp_1630()));
        }
    }
}
