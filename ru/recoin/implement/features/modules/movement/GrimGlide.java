/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1294
 *  net.minecraft.class_243
 *  net.minecraft.class_2708
 *  net.minecraft.class_2828
 *  net.minecraft.class_2828$class_5911
 *  net.minecraft.class_3532
 *  net.minecraft.class_6374
 */
package ru.recoin.implement.features.modules.movement;

import net.minecraft.class_1294;
import net.minecraft.class_243;
import net.minecraft.class_2708;
import net.minecraft.class_2828;
import net.minecraft.class_3532;
import net.minecraft.class_6374;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.GlidingVelocityEvent;
import ru.recoin.implement.events.player.GlobalTickEvent;

public class GrimGlide
extends Module {
    private transient float timerRate = 1.0f;
    private transient int correctionTicks;
    private transient boolean correctionReceived;
    private transient boolean replacedMovementPacket;
    private final SelectSetting mode = new SelectSetting("Mode", "").value("ReallyWorld", "\u041e\u0431\u044b\u0447\u043d\u044b\u0439").selected("ReallyWorld");

    public static float getTimerRate() {
        GrimGlide module = Instance.get(GrimGlide.class);
        return module != null && module.isState() && GrimGlide.mc.field_1724 != null && GrimGlide.mc.field_1724.method_6128() && module.mode.isSelected("ReallyWorld") ? module.timerRate : 1.0f;
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        if (GrimGlide.mc.field_1724 == null) {
            return;
        }
        if (event.isReceive() && event.getPacket() instanceof class_2708) {
            this.correctionTicks = 2;
            this.correctionReceived = true;
        }
        if (this.mode.isSelected("\u041e\u0431\u044b\u0447\u043d\u044b\u0439") && event.isSend()) {
            if (event.getPacket() instanceof class_2828) {
                if (GrimGlide.mc.field_1724.method_6128() && this.correctionTicks == 0 && !this.correctionReceived) {
                    PlayerIntersectionUtil.sendPacketWithOutEvent(new class_2828.class_5911(true, true));
                    this.replacedMovementPacket = true;
                    event.cancel();
                }
                this.correctionReceived = false;
            }
            if (event.getPacket() instanceof class_6374) {
                if (GrimGlide.mc.field_1724.method_6128() && this.correctionTicks == 0 && !this.correctionReceived) {
                    event.cancel();
                }
                this.correctionReceived = false;
            }
        }
    }

    @EventHandler
    public void onGlobalTick(GlobalTickEvent event) {
        if (this.correctionTicks > 0) {
            --this.correctionTicks;
        }
        if (GrimGlide.mc.field_1724 == null || !GrimGlide.mc.field_1724.method_6128() || !this.mode.isSelected("ReallyWorld")) {
            this.timerRate = 1.0f;
            return;
        }
        this.timerRate = !this.correctionReceived ? (GrimGlide.mc.field_1724.field_6012 % 2 == 0 ? 1.6f : 0.35f) : 1.0f;
    }

    @Override
    public void activate() {
        super.activate();
    }

    @Override
    public void deactivate() {
        this.timerRate = 1.0f;
        super.deactivate();
    }

    @EventHandler
    public void onGlidingVelocity(GlidingVelocityEvent event) {
        if (GrimGlide.mc.field_1724 == null) {
            return;
        }
        if (this.mode.isSelected("\u041e\u0431\u044b\u0447\u043d\u044b\u0439")) {
            class_243 vector = GrimGlide.mc.field_1724.method_18798();
            class_243 vector2 = GrimGlide.mc.field_1724.method_5720();
            float pitch = GrimGlide.mc.field_1724.method_36455() * ((float)Math.PI / 180);
            double distance = Math.sqrt(vector2.field_1352 * vector2.field_1352 + vector2.field_1350 * vector2.field_1350);
            double distance2 = vector.method_37267();
            boolean flag = GrimGlide.mc.field_1724.method_18798().field_1351 <= 0.0;
            double distance3 = flag && GrimGlide.mc.field_1724.method_6059(class_1294.field_5906) ? Math.min(GrimGlide.mc.field_1724.method_56989(), 0.01) : GrimGlide.mc.field_1724.method_56989();
            double distance4 = class_3532.method_33723((double)Math.cos(pitch));
            vector = vector.method_1031(0.0, distance3 * (-1.0 + distance4 * 0.75), 0.0);
            if (vector.field_1351 < 0.0 && distance > 0.0) {
                double distance5 = vector.field_1351 * -0.1 * distance4;
                vector = vector.method_1031(vector2.field_1352 * distance5 / distance, distance5, vector2.field_1350 * distance5 / distance);
            }
            if (pitch < 0.0f && distance > 0.0) {
                double distance6 = distance2 * (double)(-class_3532.method_15374((double)pitch)) * (double)0.04f;
                vector = vector.method_1031(-vector2.field_1352 * distance6 / distance, distance6 * 3.2, -vector2.field_1350 * distance6 / distance);
            }
            if (distance > 0.0) {
                vector = vector.method_1031((vector2.field_1352 / distance * distance2 - vector.field_1352) * 0.1, 0.0, (vector2.field_1350 / distance * distance2 - vector.field_1350) * 0.1);
            }
            double yaw = Math.toRadians(GrimGlide.mc.field_1724.method_36454());
            double distance7 = -Math.sin(yaw);
            double distance8 = Math.cos(yaw);
            if (this.correctionTicks >= 1) {
                double distance9 = 0.09f;
                event.setVelocity(vector.method_18805((double)0.99f, (double)0.98f, (double)0.99f).method_1031(distance7 * distance9, (double)0.03f, distance8 * distance9));
            } else {
                event.setVelocity(vector.method_18805((double)0.3f, (double)0.3f, (double)0.3f));
            }
        }
    }

    public GrimGlide() {
        super("Grim Glide", "Elytra Exploit", ModuleCategory.MOVEMENT);
        this.setup(this.mode);
    }
}

