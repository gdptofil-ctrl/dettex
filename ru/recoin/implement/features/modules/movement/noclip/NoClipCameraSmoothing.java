/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_243
 *  net.minecraft.class_746
 */
package ru.recoin.implement.features.modules.movement.noclip;

import net.minecraft.class_243;
import net.minecraft.class_746;

public final class NoClipCameraSmoothing {
    private static final long DURATION_NANOS = 300000000L;
    private static class_746 player;
    private static class_243 lastPosition;
    private static class_243 transitionStart;
    private static long startedAt;
    private static boolean transitioning;
    private static final double DURATION = 3.0E8;
    private static final double CUBIC = 3.0;
    private static final double QUADRATIC = 2.0;

    public static void beginTransition(class_746 localPlayer) {
        if (localPlayer != null && player == localPlayer && lastPosition != null) {
            transitionStart = lastPosition;
            startedAt = System.nanoTime();
            transitioning = true;
        } else {
            transitioning = false;
        }
    }

    public static class_243 smooth(class_746 localPlayer, class_243 targetPosition) {
        if (player != localPlayer) {
            NoClipCameraSmoothing.reset();
            player = localPlayer;
        }
        if (transitioning && transitionStart != null) {
            double progress = (double)(System.nanoTime() - startedAt) / 3.0E8;
            if (progress >= 1.0) {
                transitioning = false;
                transitionStart = null;
                lastPosition = targetPosition;
                return targetPosition;
            }
            progress = Math.max(0.0, progress);
            double easedProgress = progress * progress * (3.0 - 2.0 * progress);
            lastPosition = new class_243(NoClipCameraSmoothing.transitionStart.field_1352 + (targetPosition.field_1352 - NoClipCameraSmoothing.transitionStart.field_1352) * easedProgress, NoClipCameraSmoothing.transitionStart.field_1351 + (targetPosition.field_1351 - NoClipCameraSmoothing.transitionStart.field_1351) * easedProgress, NoClipCameraSmoothing.transitionStart.field_1350 + (targetPosition.field_1350 - NoClipCameraSmoothing.transitionStart.field_1350) * easedProgress);
            return lastPosition;
        }
        lastPosition = targetPosition;
        return targetPosition;
    }

    public static void reset() {
        player = null;
        lastPosition = null;
        transitionStart = null;
        startedAt = 0L;
        transitioning = false;
    }

    private NoClipCameraSmoothing() {
    }
}

