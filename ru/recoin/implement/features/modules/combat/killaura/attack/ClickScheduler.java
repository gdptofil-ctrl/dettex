/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.combat.killaura.attack;

import java.util.concurrent.ThreadLocalRandom;
import ru.recoin.common.QuickImports;

public class ClickScheduler
implements QuickImports {
    private long lastClickTime = System.currentTimeMillis();
    private float chargeThreshold = 0.86f;
    private long baseDelayMs = 210L;
    private long extraDelayMs = 0L;

    public boolean isCooldownComplete(int ticks) {
        return this.isCooldownCompleteWithin((long)Math.max(0, ticks) * 50L);
    }

    public boolean isCooldownCompleteWithin(long leadMs) {
        long safeLeadMs = Math.max(0L, leadMs);
        float predictedTickDelta = 0.5f + (float)safeLeadMs / 50.0f;
        long remainingClickDelay = Math.max(0L, this.baseDelayMs + this.extraDelayMs - safeLeadMs);
        return ClickScheduler.mc.field_1724 != null && ClickScheduler.mc.field_1724.method_7261(predictedTickDelta) >= this.chargeThreshold && this.lastClickPassed() >= remainingClickDelay;
    }

    public boolean hasTicksElapsedSinceLastClick(int ticks) {
        return this.lastClickPassed() >= (long)ticks * 20L;
    }

    public long lastClickPassed() {
        return System.currentTimeMillis() - this.lastClickTime;
    }

    public void recalculate() {
        this.lastClickTime = System.currentTimeMillis();
        this.chargeThreshold = 0.855f + ThreadLocalRandom.current().nextFloat() * 0.125f;
        this.baseDelayMs = 175L + (long)ThreadLocalRandom.current().nextInt(106);
        this.extraDelayMs = ThreadLocalRandom.current().nextFloat() < 0.12f ? 60L + (long)ThreadLocalRandom.current().nextInt(190) : 0L;
    }

    public boolean hasMsElapsedSinceLastClick(long ms) {
        return this.lastClickPassed() >= ms;
    }
}
