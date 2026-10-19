/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1937
 *  net.minecraft.class_238
 *  net.minecraft.class_2596
 *  net.minecraft.class_2663
 *  net.minecraft.class_2678
 *  net.minecraft.class_2724
 *  net.minecraft.class_2799
 *  net.minecraft.class_2799$class_2800
 */
package ru.recoin.implement.features.modules.movement;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.class_1937;
import net.minecraft.class_238;
import net.minecraft.class_2596;
import net.minecraft.class_2663;
import net.minecraft.class_2678;
import net.minecraft.class_2724;
import net.minecraft.class_2799;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.other.StopWatch;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.AttackEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;

public class Blink
extends Module {
    private final List<class_2596<?>> packets = new CopyOnWriteArrayList();
    private final BooleanSetting onHit = new BooleanSetting("\u041f\u0440\u0438 \u0443\u0434\u0430\u0440\u0435", "\u0421\u0431\u0440\u0430\u0441\u044b\u0432\u0430\u0442\u044c \u043e\u0447\u0435\u0440\u0435\u0434\u044c \u043f\u0430\u043a\u0435\u0442\u043e\u0432 \u043f\u0440\u0438 \u0443\u0434\u0430\u0440\u0435").setValue(true);
    private final ValueSetting delay = new ValueSetting("\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430", "\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430 \u043f\u0435\u0440\u0435\u0434 \u0441\u0431\u0440\u043e\u0441\u043e\u043c \u043f\u0430\u043a\u0435\u0442\u043e\u0432 \u0432 \u0441\u0435\u043a\u0443\u043d\u0434\u0430\u0445").setValue(0.1f).range(0.01f, 10.0f).step(0.01f).visible(() -> !this.onHit.isValue());
    private final StopWatch timer = new StopWatch();
    private class_238 box;

    public Blink() {
        super("Blink", ModuleCategory.MOVEMENT);
        this.setup(this.onHit, this.delay);
    }

    @Override
    public void activate() {
        this.box = Blink.mc.field_1724 != null ? Blink.mc.field_1724.method_5829() : null;
        this.timer.reset();
    }

    @Override
    public void deactivate() {
        this.packets.forEach(PlayerIntersectionUtil::sendPacketWithOutEvent);
        this.packets.clear();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (!this.onHit.isValue() && this.delay.getValue() > 0.0f && this.timer.finished((long)(this.delay.getValue() * 1000.0f))) {
            this.flush();
            this.timer.reset();
        }
    }

    @EventHandler
    public void onAttack(AttackEvent event) {
        if (event.isPre() && this.onHit.isValue()) {
            this.flush();
            this.timer.reset();
        }
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        if (PlayerIntersectionUtil.nullCheck()) {
            return;
        }
        class_2596<?> class_25962 = e.getPacket();
        Objects.requireNonNull(class_25962);
        class_2596<?> class_25963 = class_25962;
        if (class_25963 instanceof class_2724 respawn) {
            this.setState(false);
        } else if (class_25963 instanceof class_2678 join) {
            this.setState(false);
        } else if (class_25963 instanceof class_2799 status) {
            if (status.method_12119().equals((Object)class_2799.class_2800.field_12774)) {
                this.setState(false);
            } else if (class_25963 instanceof class_2663 status2) {
                if (this.onHit.isValue() && status2.method_11470() == 2 && status2.method_11469((class_1937)Blink.mc.field_1687) == Blink.mc.field_1724) {
                    this.flush();
                    this.timer.reset();
                }
            } else if (e.isSend()) {
                this.packets.add(e.getPacket());
                e.cancel();
            }
        } else if (class_25963 instanceof class_2663 status3) {
            if (this.onHit.isValue() && status3.method_11470() == 2 && status3.method_11469((class_1937)Blink.mc.field_1687) == Blink.mc.field_1724) {
                this.flush();
                this.timer.reset();
            }
        } else if (e.isSend()) {
            this.packets.add(e.getPacket());
            e.cancel();
        }
    }

    private void flush() {
        if (Blink.mc.field_1724 == null) {
            return;
        }
        this.packets.forEach(PlayerIntersectionUtil::sendPacketWithOutEvent);
        this.packets.clear();
        this.box = Blink.mc.field_1724.method_5829();
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        if (this.box != null) {
            Render3DUtil.drawBox(this.box, ColorUtil.getClientColor(), 1.0f);
        }
    }
}

