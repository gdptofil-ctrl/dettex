/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.class_2499
 *  net.minecraft.class_2520
 *  net.minecraft.class_641
 *  net.minecraft.class_642
 *  net.minecraft.class_642$class_8678
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import java.util.List;
import net.minecraft.class_2499;
import net.minecraft.class_2520;
import net.minecraft.class_641;
import net.minecraft.class_642;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_641.class})
public class ServerListMixin {
    @Unique
    private final List<class_642> sponsorServers = List.of(new class_642("\u0420\u203a\u0421\u0453\u0421\u2021\u0421\u20ac\u0420\u0451\u0420\u2116 HvH \u0421\u0403\u0420\u00b5\u0421\u0402\u0420\u0406\u0420\u00b5\u0421\u0402!", "online4.clearworld.pw", class_642.class_8678.field_45610), new class_642("\u0420\u045a\u0420\u0405\u0420\u0455\u0420\u0456\u0420\u0455 \u0421\u0402\u0420\u00b5\u0420\u00b6\u0420\u0451\u0420\u0458\u0420\u0455\u0420\u0406 + \u0420\u0457\u0420\u0455\u0420\u0491\u0420\u00b0\u0421\u0402\u0420\u0454\u0420\u0451!", "recoin.metahvh.space", class_642.class_8678.field_45610));
    @Shadow
    @Final
    private List<class_642> field_3749;

    @Inject(method={"method_2981"}, at={@At(value="FIELD", target="Lnet/minecraft/class_641;field_39438:Ljava/util/List;", ordinal=0)})
    private void loadFileHook(CallbackInfo ci) {
        this.field_3749.addAll(this.sponsorServers);
    }

    @Redirect(method={"method_2987"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_2499;add(Ljava/lang/Object;)Z", ordinal=0))
    private boolean saveFileHook(class_2499 instance, Object o, @Local(ordinal=0) class_642 info) {
        if (this.sponsorServers.contains(info)) {
            return true;
        }
        return instance.add((class_2520)o);
    }
}

