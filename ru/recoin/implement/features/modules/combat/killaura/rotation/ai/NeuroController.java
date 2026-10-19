/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_3532
 */
package ru.recoin.implement.features.modules.combat.killaura.rotation.ai;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_3532;
import ru.recoin.implement.features.modules.combat.killaura.rotation.ai.NeuroModel;

final class NeuroController {
    private float[] state;
    private float freeze = 20.0f;
    private float stalled;
    private float targetYaw;
    private float targetPitch;
    private float errorYaw;
    private float errorPitch;
    private float previousErrorYaw;
    private float previousErrorPitch;
    private float stepYaw;
    private float stepPitch;
    private float olderStepYaw;
    private float olderStepPitch;
    private float halfYaw = 5.0f;
    private float halfPitch = 15.0f;
    private double distance = 3.0;
    private boolean onTarget;

    NeuroController() {
    }

    void reset() {
        this.state = null;
        this.stalled = 0.0f;
        this.freeze = 20.0f;
    }

    void attacked() {
        this.freeze = 0.0f;
    }

    float[] step(NeuroModel model, float yaw, float pitch, float desiredYaw, float desiredPitch, float widthYaw, float widthPitch, double targetDistance, float progress, float gcd, float temperature, int candidates, float speed) {
        boolean forced;
        if (this.state == null) {
            this.state = model.state();
            this.targetYaw = desiredYaw;
            this.targetPitch = desiredPitch;
            this.errorYaw = this.previousErrorYaw = class_3532.method_15393((float)(desiredYaw - yaw));
            this.errorPitch = this.previousErrorPitch = desiredPitch - pitch;
        }
        this.halfYaw = Math.max(0.5f, widthYaw);
        this.halfPitch = Math.max(0.5f, widthPitch);
        this.distance = targetDistance;
        float targetVelocityYaw = class_3532.method_15393((float)(desiredYaw - this.targetYaw));
        float targetVelocityPitch = desiredPitch - this.targetPitch;
        this.targetYaw = desiredYaw;
        this.targetPitch = desiredPitch;
        float wantedYaw = class_3532.method_15393((float)(desiredYaw - yaw));
        float wantedPitch = desiredPitch - pitch;
        float[] input = new float[]{NeuroController.transform(this.errorYaw), NeuroController.transform(this.errorPitch), NeuroController.transform(class_3532.method_15393((float)(this.errorYaw - this.previousErrorYaw))), NeuroController.transform(this.errorPitch - this.previousErrorPitch), NeuroController.transform(targetVelocityYaw), NeuroController.transform(targetVelocityPitch), NeuroController.transform(this.stepYaw), NeuroController.transform(this.stepPitch), NeuroController.transform(this.olderStepYaw), NeuroController.transform(this.olderStepPitch), NeuroController.transform(this.errorYaw / this.halfYaw), NeuroController.transform(this.errorPitch / this.halfPitch), (float)Math.log(Math.max(this.distance, 0.05) + 0.5) / 2.0f, (float)Math.log(this.halfYaw) / 3.0f, this.onTarget ? 1.0f : 0.0f, this.freeze / 20.0f, this.stalled / (float)model.freezeCut()};
        float[] output = model.forward(input, this.state);
        float selectedYaw = 0.0f;
        float selectedPitch = 0.0f;
        boolean bl = forced = this.stalled >= (float)model.freezeCut();
        if (forced || ThreadLocalRandom.current().nextFloat() >= NeuroModel.sigmoid(output[0])) {
            float best = Float.MAX_VALUE;
            for (int i = 0; i < candidates; ++i) {
                int base = 1 + model.sampleMix(output) * 6;
                float correlation = (float)Math.tanh(output[base + 5]) * 0.95f;
                float independent = (float)Math.sqrt(Math.max(0.0f, 1.0f - correlation * correlation));
                float gaussianYaw = (float)ThreadLocalRandom.current().nextGaussian();
                float gaussianPitch = correlation * gaussianYaw + independent * (float)ThreadLocalRandom.current().nextGaussian();
                float sampleYaw = NeuroController.quantize(speed * model.sample(output[base + 1], output[base + 3], true, gaussianYaw, temperature), gcd);
                float samplePitch = NeuroController.quantize(speed * model.sample(output[base + 2], output[base + 4], false, gaussianPitch, temperature), gcd);
                float score = Math.abs((float)Math.hypot(class_3532.method_15393((float)(wantedYaw - sampleYaw)) / this.halfYaw, (wantedPitch - samplePitch) / this.halfPitch) - progress);
                if (!(score < best)) continue;
                best = score;
                selectedYaw = sampleYaw;
                selectedPitch = samplePitch;
            }
        }
        if (forced && selectedYaw == 0.0f && selectedPitch == 0.0f) {
            if (Math.abs(wantedYaw) >= Math.abs(wantedPitch)) {
                selectedYaw = Math.copySign(gcd, wantedYaw);
            } else {
                selectedPitch = Math.copySign(gcd, wantedPitch);
            }
        }
        this.stalled = selectedYaw == 0.0f && selectedPitch == 0.0f ? this.stalled + 1.0f : 0.0f;
        float newPitch = class_3532.method_15363((float)(pitch + selectedPitch), (float)-90.0f, (float)90.0f);
        selectedPitch = newPitch - pitch;
        this.olderStepYaw = this.stepYaw;
        this.olderStepPitch = this.stepPitch;
        this.stepYaw = selectedYaw;
        this.stepPitch = selectedPitch;
        this.previousErrorYaw = this.errorYaw;
        this.previousErrorPitch = this.errorPitch;
        this.errorYaw = class_3532.method_15393((float)(desiredYaw - (yaw + selectedYaw)));
        this.errorPitch = desiredPitch - newPitch;
        this.onTarget = Math.abs(this.errorYaw) <= this.halfYaw && Math.abs(this.errorPitch) <= this.halfPitch;
        this.freeze = Math.min(this.freeze + 1.0f, 20.0f);
        return new float[]{selectedYaw, selectedPitch};
    }

    private static float transform(float value) {
        return (float)(Math.log((double)value + Math.sqrt(value * value + 1.0f)) / 3.0);
    }

    private static float quantize(float value, float gcd) {
        return (float)Math.round(value / gcd) * gcd;
    }
}

