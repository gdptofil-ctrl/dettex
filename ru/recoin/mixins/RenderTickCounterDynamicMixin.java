/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.floats.FloatUnaryOperator
 *  net.minecraft.class_9779$class_9781
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package ru.recoin.mixins;

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.recoin.implement.features.modules.movement.GrimGlide;
import ru.recoin.implement.features.modules.movement.LongJump;
import ru.recoin.implement.features.modules.movement.NoFall;
import ru.recoin.implement.features.modules.movement.Speed;
import ru.recoin.implement.features.modules.movement.Timer;

@Mixin(value={class_9779.class_9781.class})
public class RenderTickCounterDynamicMixin {
    @Redirect(method={"method_60639(J)I"}, at=@At(value="INVOKE", target="Lit/unimi/dsi/fastutil/floats/FloatUnaryOperator;apply(F)F"))
    private float recoin$timerRate(FloatUnaryOperator operator, float baseTickTime) {
        float noFallRate;
        float longJumpRate;
        float elytraRate = GrimGlide.getTimerRate();
        float rate = elytraRate != 1.0f ? elytraRate : Timer.getRate();
        Speed speed = Speed.getInstance();
        if (speed != null && speed.isGrim72Mode()) {
            rate = speed.getGrim72TimerRate();
        }
        if ((longJumpRate = LongJump.getTimerRate()) != 1.0f) {
            rate = longJumpRate;
        }
        if ((noFallRate = NoFall.getTimerRate()) != 1.0f) {
            rate = noFallRate;
        }
        return operator.apply(baseTickTime) / rate;
    }
}

