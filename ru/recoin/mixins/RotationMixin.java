package ru.recoin.mixins;

import net.minecraft.class_746;
import net.minecraft.class_3532;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.implement.features.modules.combat.KillAuraModule;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;

@Mixin(value={class_746.class})
public class RotationMixin {

    @Inject(method={"method_3136"}, at={@At(value="HEAD")})
    private void recoin$clampRotation(CallbackInfo callbackInfo) {
        KillAuraModule module = KillAuraModule.getInstance();
        if (module == null || !module.isState()) {
            return;
        }
        RotationController controller = RotationController.INSTANCE;
        Angle current = controller.getRotation();
        Angle server = controller.getServerAngle();
        if (current == null || server == null) {
            return;
        }
        float max = module.getMaxRotation();
        float yaw = current.getYaw();
        float pitch = current.getPitch();
        float yawDelta = class_3532.method_15393(yaw - server.getYaw());
        float pitchDelta = pitch - server.getPitch();
        boolean changed = false;

        if (yawDelta > max) {
            yaw = server.getYaw() + max;
            changed = true;
        } else if (yawDelta < -max) {
            yaw = server.getYaw() - max;
            changed = true;
        }
        if (pitchDelta > max) {
            pitch = server.getPitch() + max;
            changed = true;
        } else if (pitchDelta < -max) {
            pitch = server.getPitch() - max;
            changed = true;
        }
        if (changed) {
            pitch = Math.clamp(pitch, -89.5f, 89.5f);
            controller.setRotation(new Angle(yaw, pitch));
        }
    }
}
