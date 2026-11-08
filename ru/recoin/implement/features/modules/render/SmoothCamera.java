package ru.recoin.implement.features.modules.render;

import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.implement.events.keyboard.MouseRotationEvent;

/**
 * Smooth Camera: mouse movement is not applied at once but eased in over a short time, so turning
 * looks soft and cinematic instead of jerky. Nothing is lost: every bit of mouse movement still
 * arrives, just spread over a few frames.
 */
public final class SmoothCamera
extends Module {
    private final ValueSetting smoothness = new ValueSetting("Smoothness", "Higher = softer and slower camera").setValue(35.0f).range(1, 100);
    private final BooleanSetting horizontal = new BooleanSetting("Horizontal", "Smooth turning left / right").setValue(true);
    private final BooleanSetting vertical = new BooleanSetting("Vertical", "Smooth looking up / down").setValue(true);

    private double pendingX;
    private double pendingY;
    private long lastNanos;

    public SmoothCamera() {
        super("SmoothCamera", "Smooth Camera", ModuleCategory.RENDER);
        this.setup(this.smoothness, this.horizontal, this.vertical);
    }

    @Override
    public void deactivate() {
        this.pendingX = 0.0;
        this.pendingY = 0.0;
        this.lastNanos = 0L;
    }

    @EventHandler
    public void onMouseRotation(MouseRotationEvent event) {
        long now = System.nanoTime();
        double dt = this.lastNanos == 0L ? 0.016 : Math.min(0.1, (now - this.lastNanos) / 1.0e9);
        this.lastNanos = now;
        // smoothness 1 -> almost instant (speed 40/s), 100 -> very soft (speed 3/s)
        double speed = 40.0 - (this.smoothness.getValue() - 1.0) / 99.0 * 37.0;
        double k = 1.0 - Math.exp(-dt * speed);
        if (this.horizontal.isValue()) {
            this.pendingX += event.getCursorDeltaX();
            double step = Math.abs(this.pendingX) < 0.01 ? this.pendingX : this.pendingX * k;
            this.pendingX -= step;
            event.setCursorDeltaX((float)step);
        } else {
            this.pendingX = 0.0;
        }
        if (this.vertical.isValue()) {
            this.pendingY += event.getCursorDeltaY();
            double step = Math.abs(this.pendingY) < 0.01 ? this.pendingY : this.pendingY * k;
            this.pendingY -= step;
            event.setCursorDeltaY((float)step);
        } else {
            this.pendingY = 0.0;
        }
    }
}
