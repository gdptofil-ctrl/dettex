/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_638$class_5271
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.recoin.mixins;

import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.recoin.common.QuickImports;
import ru.recoin.implement.features.modules.render.Atmosphere;
import ru.recoin.implement.features.modules.render.WorldTweaks;

@Mixin(value={class_638.class_5271.class})
public class ClientWorldPropertiesMixin
implements QuickImports {
    @Shadow
    private long field_24439;

    @Inject(method={"method_217"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$atmosphereTime(CallbackInfoReturnable<Long> cir) {
        Atmosphere atmosphere = Atmosphere.getInstance();
        if (atmosphere != null && atmosphere.isState()) {
            cir.setReturnValue(atmosphere.timeOfDay());
        }
    }

    @Inject(method={"method_165"}, at={@At(value="HEAD")}, cancellable=true)
    public void setTimeOfDayHook(long timeOfDay, CallbackInfo ci) {
        WorldTweaks tweaks = WorldTweaks.getInstance();
        if (tweaks.state && tweaks.modeSetting.isSelected("Time")) {
            this.field_24439 = (long)tweaks.timeSetting.getInt() * 1000L;
            ci.cancel();
        }
    }
}

