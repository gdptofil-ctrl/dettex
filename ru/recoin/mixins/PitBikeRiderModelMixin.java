/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10034
 *  net.minecraft.class_572
 *  net.minecraft.class_630
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_10034;
import net.minecraft.class_572;
import net.minecraft.class_630;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.api.accessor.IPitBikeRiderState;

@Mixin(value={class_572.class})
public abstract class PitBikeRiderModelMixin {
    @Shadow
    @Final
    public class_630 field_3391;
    @Shadow
    @Final
    public class_630 field_3398;
    @Shadow
    @Final
    public class_630 field_3401;
    @Shadow
    @Final
    public class_630 field_27433;
    @Shadow
    @Final
    public class_630 field_3392;
    @Shadow
    @Final
    public class_630 field_3397;

    @Inject(method={"method_17087(Lnet/minecraft/class_10034;)V"}, at={@At(value="TAIL")})
    private void recoin$bikePose(class_10034 state, CallbackInfo ci) {
        IPitBikeRiderState bike;
        if (!(state instanceof IPitBikeRiderState) || !(bike = (IPitBikeRiderState)state).recoin$isBikeRider()) {
            return;
        }
        float steering = (float)Math.toRadians(bike.recoin$getBikeSteering());
        float crouch = bike.recoin$getBikeSuspension() * 4.0f;
        this.field_3391.field_3654 = 0.24f + crouch;
        this.field_3391.field_3655 = -0.6f;
        this.field_3398.field_3655 = -0.8f;
        this.field_3401.field_3654 = -1.18f + steering * 0.22f - crouch * 0.25f;
        this.field_27433.field_3654 = -1.18f - steering * 0.22f - crouch * 0.25f;
        this.field_3401.field_3675 = -0.12f + steering * 0.4f;
        this.field_27433.field_3675 = 0.12f + steering * 0.4f;
        this.field_3401.field_3674 = 0.18f;
        this.field_27433.field_3674 = -0.18f;
        this.field_27433.field_3655 = -0.8f;
        this.field_3401.field_3655 = -0.8f;
        this.field_3392.field_3654 = this.field_3397.field_3654 = -0.72f - crouch * 0.3f;
        this.field_3392.field_3675 = -0.16f;
        this.field_3397.field_3675 = 0.16f;
        this.field_3392.field_3674 = 0.14f;
        this.field_3397.field_3674 = -0.14f;
        this.field_3392.field_3656 = this.field_3397.field_3656 = 11.7f + crouch;
        this.field_3397.field_3655 = 0.3f;
        this.field_3392.field_3655 = 0.3f;
    }
}

