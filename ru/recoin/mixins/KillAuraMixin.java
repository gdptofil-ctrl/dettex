package ru.recoin.mixins;

import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_636;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.implement.features.modules.combat.KillAuraModule;

@Mixin(value={class_636.class})
public class KillAuraMixin {

    @Inject(method={"method_2918"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$killAuraAttack(class_1657 player, class_1297 target, CallbackInfo callbackInfo) {
        KillAuraModule module = KillAuraModule.getInstance();
        if (module == null || !module.isState() || target != module.getTarget()) {
            return;
        }
        if (!module.validateAttackTarget(target)) {
            callbackInfo.cancel();
            return;
        }
        module.onAttackSent(target);
    }
}
