/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.events.render;

import ru.recoin.api.event.events.callables.EventCancellable;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;

public class CameraEvent
extends EventCancellable {
    private boolean cameraClip;
    private float distance;
    private Angle angle;

    public boolean isCameraClip() {
        return this.cameraClip;
    }

    public float getDistance() {
        return this.distance;
    }

    public Angle getAngle() {
        return this.angle;
    }

    public void setCameraClip(boolean cameraClip) {
        this.cameraClip = cameraClip;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public void setAngle(Angle angle) {
        this.angle = angle;
    }

    public CameraEvent(boolean cameraClip, float distance, Angle angle) {
        this.cameraClip = cameraClip;
        this.distance = distance;
        this.angle = angle;
    }
}

