/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.class_1293
 *  net.minecraft.class_1294
 *  net.minecraft.class_485
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.recoin.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.ArrayList;
import java.util.Collection;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_485;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.recoin.implement.features.modules.render.FullBright;

@Mixin(value={class_485.class})
public class StatusEffectsDisplayMixin {
    @ModifyExpressionValue(method={"method_75368"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_746;method_6026()Ljava/util/Collection;")})
    private Collection<class_1293> recoin$hideFullBrightNightVision(Collection<class_1293> original) {
        FullBright fullBright = FullBright.getInstance();
        if (fullBright == null || !fullBright.isState() || fullBright.isGammaMode()) {
            return original;
        }
        ArrayList<class_1293> filtered = new ArrayList<class_1293>(original.size());
        for (class_1293 instance : original) {
            if (instance.method_48559() && instance.method_5579().equals((Object)class_1294.field_5925)) continue;
            filtered.add(instance);
        }
        return filtered;
    }
}

