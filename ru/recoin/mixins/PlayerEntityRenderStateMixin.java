/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10055
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 */
package ru.recoin.mixins;

import net.minecraft.class_10055;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import ru.recoin.api.accessor.ICustomPlayerModelState;
import ru.recoin.api.accessor.IPitBikeRiderState;

@Mixin(value={class_10055.class})
public class PlayerEntityRenderStateMixin
implements ICustomPlayerModelState,
IPitBikeRiderState {
    @Unique
    private boolean recoin$bikeRider;
    @Unique
    private float recoin$bikePitch;
    @Unique
    private float recoin$bikeLean;
    @Unique
    private float recoin$bikeSteering;
    @Unique
    private float recoin$bikeSuspension;
    @Unique
    private boolean recoin$hasCustomModel;
    @Unique
    private String recoin$customModel = "";

    @Override
    public void recoin$setBikePose(boolean riding, float pitch, float lean, float steering, float suspension) {
        this.recoin$bikeRider = riding;
        this.recoin$bikePitch = pitch;
        this.recoin$bikeLean = lean;
        this.recoin$bikeSteering = steering;
        this.recoin$bikeSuspension = suspension;
    }

    @Override
    public boolean recoin$isBikeRider() {
        return this.recoin$bikeRider;
    }

    @Override
    public float recoin$getBikePitch() {
        return this.recoin$bikePitch;
    }

    @Override
    public float recoin$getBikeLean() {
        return this.recoin$bikeLean;
    }

    @Override
    public float recoin$getBikeSteering() {
        return this.recoin$bikeSteering;
    }

    @Override
    public float recoin$getBikeSuspension() {
        return this.recoin$bikeSuspension;
    }

    @Override
    public boolean recoin$hasCustomModel() {
        return this.recoin$hasCustomModel;
    }

    @Override
    public String recoin$getCustomModel() {
        return this.recoin$customModel;
    }

    @Override
    public void recoin$setCustomModel(boolean enabled, String model) {
        this.recoin$hasCustomModel = enabled;
        this.recoin$customModel = model == null ? "" : model;
    }
}

