/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10055
 *  net.minecraft.class_1306
 *  net.minecraft.class_4587
 *  net.minecraft.class_591
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_10055;
import net.minecraft.class_1306;
import net.minecraft.class_4587;
import net.minecraft.class_591;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.api.accessor.ICustomPlayerModelState;
import ru.recoin.implement.features.modules.movement.NoClip;
import ru.recoin.implement.features.modules.render.custommodels.CustomPlayerModelRenderer;

@Mixin(value={class_591.class})
public abstract class PlayerEntityModelMixin {
    @Inject(method={"method_62110(Lnet/minecraft/class_10055;)V"}, at={@At(value="TAIL")})
    private void recoin$noClipHead(class_10055 state, CallbackInfo ci) {
        if (NoClip.shouldFlattenHead(state.field_53528)) {
            class_591 model = (class_591)(Object)this;
            model.field_3394.field_3654 = 0.0f;
            model.field_3398.field_3654 = 0.0f;
        }
    }

    @Inject(method={"method_72859(Lnet/minecraft/class_10055;Lnet/minecraft/class_1306;Lnet/minecraft/class_4587;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$customModelSetArmAngle(class_10055 state, class_1306 arm, class_4587 matrices, CallbackInfo ci) {
        ICustomPlayerModelState customState;
        if (state instanceof ICustomPlayerModelState && (customState = (ICustomPlayerModelState)state).recoin$hasCustomModel() && CustomPlayerModelRenderer.setArmAngle(customState.recoin$getCustomModel(), state, arm, matrices)) {
            ci.cancel();
        }
    }
}

