/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2596
 *  net.minecraft.class_2708
 *  net.minecraft.class_310
 *  net.minecraft.class_6374
 */
package ru.recoin.implement.features.modules.movement;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.class_2596;
import net.minecraft.class_2708;
import net.minecraft.class_310;
import net.minecraft.class_6374;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.InputEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;

public final class Timer
extends Module {
    private static final long CHARGE_NANOS = 1250000000L;
    private static final long BOOST_NANOS = 2400000000L;
    private static volatile float rate = 1.0f;
    private final Queue<class_6374> delayedPongs = new ConcurrentLinkedQueue<class_6374>();
    private transient boolean active;
    private transient boolean boosting;
    private transient boolean airborne;
    private transient int jumps;
    private transient int groundTicks;
    private transient long phaseStart;

    public Timer() {
        super("Timer", "Timer", ModuleCategory.MOVEMENT);
    }

    public static float getRate() {
        class_310 client = class_310.method_1551();
        return client.field_1724 == null || client.field_1687 == null ? 1.0f : rate;
    }

    @Override
    public void activate() {
        this.stopCycle();
        super.activate();
    }

    @Override
    public void deactivate() {
        this.stopCycle();
        super.deactivate();
    }

    @EventHandler
    public void onRender(WorldRenderEvent event) {
        if (Timer.mc.field_1724 == null || Timer.mc.field_1687 == null || !this.moving()) {
            this.stopCycle();
            return;
        }
        long now = System.nanoTime();
        if (!this.active) {
            this.active = true;
            this.beginCharging();
        } else if (!this.boosting && now - this.phaseStart >= 1250000000L) {
            this.beginBoosting();
        } else if (this.boosting && now - this.phaseStart >= 2400000000L) {
            this.beginCharging();
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (Timer.mc.field_1724 == null || Timer.mc.field_1687 == null) {
            this.stopCycle();
        } else if (this.active && this.boosting) {
            if (Timer.mc.field_1724.method_24828()) {
                if (this.airborne) {
                    this.airborne = false;
                    ++this.jumps;
                }
            } else {
                this.airborne = true;
                if (this.jumps >= 4 && Timer.mc.field_1724.method_18798().field_1351 <= 0.0) {
                    this.beginCharging();
                }
            }
        }
    }

    @EventHandler
    public void onInput(InputEvent event) {
        if (Timer.mc.field_1724 != null && this.boosting && event.forward() != 0 || Timer.mc.field_1724 != null && this.boosting && event.sideways() != 0.0f) {
            this.groundTicks = Timer.mc.field_1724.method_24828() ? this.groundTicks + 1 : 0;
            Timer.mc.field_1724.method_5728(true);
            if (this.groundTicks > 0) {
                event.setJumping(true);
            }
        } else {
            this.groundTicks = 0;
        }
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        class_2596<?> class_25962;
        if (!this.active) {
            return;
        }
        if (event.isSend() && (class_25962 = event.getPacket()) instanceof class_6374) {
            class_6374 pong = (class_6374)class_25962;
            this.delayedPongs.add(pong);
            event.cancel();
        } else if (event.isReceive() && event.getPacket() instanceof class_2708) {
            mc.execute(() -> {
                if (this.isState()) {
                    this.beginCharging();
                }
            });
        }
    }

    private boolean moving() {
        return Timer.mc.field_1690.field_1894.method_1434() || Timer.mc.field_1690.field_1881.method_1434() || Timer.mc.field_1690.field_1913.method_1434() || Timer.mc.field_1690.field_1849.method_1434();
    }

    private void beginCharging() {
        this.flushPongs();
        this.boosting = false;
        this.airborne = false;
        this.jumps = 0;
        this.groundTicks = 0;
        this.phaseStart = System.nanoTime();
        rate = 0.05f;
    }

    private void beginBoosting() {
        this.boosting = true;
        this.airborne = false;
        this.jumps = 0;
        this.groundTicks = 0;
        this.phaseStart = System.nanoTime();
        rate = 1.7f;
    }

    private void stopCycle() {
        this.flushPongs();
        this.active = false;
        this.boosting = false;
        this.airborne = false;
        this.jumps = 0;
        this.groundTicks = 0;
        this.phaseStart = 0L;
        rate = 1.0f;
    }

    private void flushPongs() {
        class_6374 pong;
        if (mc.method_1562() == null) {
            this.delayedPongs.clear();
            return;
        }
        while ((pong = this.delayedPongs.poll()) != null) {
            mc.method_1562().method_48296().method_10752((class_2596)pong, null);
        }
    }
}

