/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_2596
 *  net.minecraft.class_2661
 *  net.minecraft.class_2678
 *  net.minecraft.class_2708
 *  net.minecraft.class_2724
 *  net.minecraft.class_2828
 *  net.minecraft.class_2828$class_5911
 *  net.minecraft.class_2848
 *  net.minecraft.class_2848$class_2849
 */
package ru.recoin.implement.features.modules.movement;

import net.minecraft.class_1297;
import net.minecraft.class_2596;
import net.minecraft.class_2661;
import net.minecraft.class_2678;
import net.minecraft.class_2708;
import net.minecraft.class_2724;
import net.minecraft.class_2828;
import net.minecraft.class_2848;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.entity.SimulatedPlayer;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.InputEvent;
import ru.recoin.implement.events.player.TickEvent;

public class NoFall
extends Module {
    private final SelectSetting mode = new SelectSetting("\u041e\u0431\u0445\u043e\u0434", "\u0423\u0431\u0438\u0440\u0430\u0435\u0442 \u0443\u0440\u043e\u043d \u043e\u0442 \u043f\u0430\u0434\u0435\u043d\u0438\u044f").value("Grim 72", "Grim 73").selected("Grim 72");
    private transient int correctionTicks73;
    private transient int correctionTicks72;
    private transient boolean falling;
    private transient boolean awaitingCorrection;
    private transient long attemptTime = System.currentTimeMillis();
    private transient long glideTime = System.currentTimeMillis();
    private transient float timerRate = 1.0f;

    public NoFall() {
        super("NoFall", ModuleCategory.MOVEMENT);
        this.setup(this.mode);
    }

    public static float getTimerRate() {
        NoFall module = Instance.get(NoFall.class);
        return module != null && module.isState() && NoFall.mc.field_1724 != null && NoFall.mc.field_1687 != null && module.mode.isSelected("Grim 73") ? module.timerRate : 1.0f;
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        class_2596<?> class_25962;
        if (event.isReceive()) {
            if (event.getPacket() instanceof class_2678 || event.getPacket() instanceof class_2661 || event.getPacket() instanceof class_2724) {
                this.correctionTicks73 = 0;
                this.falling = false;
                this.awaitingCorrection = false;
                this.timerRate = 1.0f;
            }
            if (event.getPacket() instanceof class_2708 && this.awaitingCorrection) {
                if (this.mode.isSelected("Grim 72")) {
                    this.correctionTicks72 = 2;
                } else if (this.mode.isSelected("Grim 73")) {
                    this.correctionTicks73 = 2;
                }
                this.awaitingCorrection = false;
            }
        } else if (NoFall.mc.field_1724 != null && mc.method_1562() != null && this.mode.isSelected("Grim 73") && (class_25962 = event.getPacket()) instanceof class_2828) {
            class_2828 packet = (class_2828)class_25962;
            if (this.falling && packet.method_12273() && this.correctionTicks73 == 0) {
                PlayerIntersectionUtil.sendPacketWithOutEvent(new class_2828.class_5911(true, false));
                this.attemptTime = System.currentTimeMillis();
                this.awaitingCorrection = true;
            }
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (NoFall.mc.field_1724 == null || NoFall.mc.field_1687 == null) {
            this.reset();
            return;
        }
        long now = System.currentTimeMillis();
        boolean bl = this.falling = NoFall.mc.field_1724.field_6017 > 3.0;
        if (this.mode.isSelected("Grim 72")) {
            if (this.correctionTicks72 > 0) {
                --this.correctionTicks72;
            }
            if (this.falling && !NoFall.mc.field_1724.method_24828() && this.predictsLanding() && now - this.glideTime >= 200L && mc.method_1562() != null) {
                PlayerIntersectionUtil.sendPacketWithOutEvent(new class_2848((class_1297)NoFall.mc.field_1724, class_2848.class_2849.field_12982));
                PlayerIntersectionUtil.sendPacketWithOutEvent(new class_2848((class_1297)NoFall.mc.field_1724, class_2848.class_2849.field_12982));
                this.glideTime = now;
                this.awaitingCorrection = true;
                this.attemptTime = now;
            }
            if (now - this.attemptTime >= 300L) {
                this.awaitingCorrection = false;
                this.correctionTicks72 = 0;
            }
        } else if (this.mode.isSelected("Grim 73")) {
            if (this.correctionTicks73 == 2) {
                this.timerRate = 0.5f;
            }
            if (this.correctionTicks73 <= 1) {
                this.timerRate = 1.0f;
            }
            if (this.correctionTicks73 > 0) {
                --this.correctionTicks73;
            }
            if (now - this.attemptTime >= 300L) {
                this.awaitingCorrection = false;
                this.correctionTicks73 = 0;
            }
        }
    }

    @EventHandler
    public void onInput(InputEvent event) {
        int ticks;
        int n = ticks = this.mode.isSelected("Grim 72") ? this.correctionTicks72 : this.correctionTicks73;
        if (this.awaitingCorrection || ticks == 2) {
            event.setJumping(false);
        }
        if (ticks == 1) {
            event.setJumping(true);
        }
    }

    private void reset() {
        this.correctionTicks73 = 0;
        this.correctionTicks72 = 0;
        this.falling = false;
        this.awaitingCorrection = false;
        this.timerRate = 1.0f;
    }

    private boolean predictsLanding() {
        SimulatedPlayer simulation = SimulatedPlayer.fromClientPlayer(SimulatedPlayer.SimulatedPlayerInput.fromClientPlayer(NoFall.mc.field_1724.method_71091()));
        simulation.tick();
        return simulation.onGround;
    }

    @Override
    public void deactivate() {
        this.reset();
        super.deactivate();
    }
}

