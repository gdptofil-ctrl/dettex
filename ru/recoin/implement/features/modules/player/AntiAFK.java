/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_2596
 *  net.minecraft.class_7439
 */
package ru.recoin.implement.features.modules.player;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1268;
import net.minecraft.class_2596;
import net.minecraft.class_7439;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.other.StopWatch;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.InputEvent;
import ru.recoin.implement.events.player.TickEvent;

public class AntiAFK
extends Module {
    private SelectSetting mode = new SelectSetting("\u0420\u0435\u0436\u0438\u043c \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u044f", "Selects Anti AFK behavior").value("\u041e\u0431\u044b\u0447\u043d\u044b\u0439", "FunTime").selected("\u041e\u0431\u044b\u0447\u043d\u044b\u0439");
    private MultiSelectSetting actions = new MultiSelectSetting("\u0412\u044b\u043f\u043e\u043b\u043d\u044f\u0442\u044c \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u044f", "Actions to perform").value("\u041f\u0440\u044b\u0436\u043e\u043a", "\u0412\u0437\u043c\u0430\u0445", "\u0414\u0432\u0438\u0436\u0435\u043d\u0438\u0435", "\u041f\u043e\u0432\u043e\u0440\u043e\u0442").selected("\u041f\u0440\u044b\u0436\u043e\u043a", "\u0412\u0437\u043c\u0430\u0445", "\u0414\u0432\u0438\u0436\u0435\u043d\u0438\u0435").visible(() -> this.mode.isSelected("\u041e\u0431\u044b\u0447\u043d\u044b\u0439"));
    private BooleanSetting reactOnMessage = new BooleanSetting("\u0420\u0435\u0430\u0433\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u043d\u0430 \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u043d\u043e\u0441\u0442\u044c", "React to AFK warning message").setValue(false).visible(() -> this.mode.isSelected("FunTime"));
    private ValueSetting delay = new ValueSetting("\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430", "Delay in seconds").setValue(15.0f).range(5, 120).visible(() -> this.mode.isSelected("\u041e\u0431\u044b\u0447\u043d\u044b\u0439"));
    private BooleanSetting onlyIdle = new BooleanSetting("\u0422\u043e\u043b\u044c\u043a\u043e \u0432 AFK", "Works only when not pressing keys").setValue(true).visible(() -> this.mode.isSelected("\u041e\u0431\u044b\u0447\u043d\u044b\u0439"));
    private StopWatch timer = new StopWatch();
    private int moveTicks = 0;

    public AntiAFK() {
        super("AntiAFK", "Anti AFK", ModuleCategory.PLAYER);
        this.setup(this.mode, this.actions, this.reactOnMessage, this.delay, this.onlyIdle);
    }

    @Override
    public void activate() {
        this.timer.reset();
        this.moveTicks = 0;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (AntiAFK.mc.field_1724 == null || AntiAFK.mc.field_1687 == null) {
            return;
        }
        if (this.moveTicks > 0) {
            --this.moveTicks;
        }
        if (this.mode.isSelected("\u041e\u0431\u044b\u0447\u043d\u044b\u0439")) {
            if (this.onlyIdle.isValue() && this.isUserActive()) {
                this.timer.reset();
                return;
            }
            if (!this.timer.finished((long)this.delay.getInt() * 1000L)) {
                return;
            }
            if (this.actions.isSelected("\u041f\u0440\u044b\u0436\u043e\u043a") && AntiAFK.mc.field_1724.method_24828()) {
                AntiAFK.mc.field_1724.method_6043();
            }
            if (this.actions.isSelected("\u0412\u0437\u043c\u0430\u0445")) {
                AntiAFK.mc.field_1724.method_6104(class_1268.field_5808);
            }
            if (this.actions.isSelected("\u0414\u0432\u0438\u0436\u0435\u043d\u0438\u0435")) {
                this.moveTicks = 8;
            }
            if (this.actions.isSelected("\u041f\u043e\u0432\u043e\u0440\u043e\u0442")) {
                this.rotate();
            }
            this.timer.reset();
        } else if (this.mode.isSelected("FunTime") && !this.reactOnMessage.isValue() && AntiAFK.mc.field_1724.field_6012 % 600 == 0) {
            this.moveTicks = 8;
            if (AntiAFK.mc.field_1724.method_24828()) {
                AntiAFK.mc.field_1724.method_6043();
            }
        }
    }

    @EventHandler
    public void onInput(InputEvent event) {
        if (this.moveTicks > 0 && AntiAFK.mc.field_1724 != null) {
            boolean forward = ThreadLocalRandom.current().nextBoolean();
            boolean backward = !forward && ThreadLocalRandom.current().nextBoolean();
            boolean left = ThreadLocalRandom.current().nextBoolean();
            boolean right = !left && ThreadLocalRandom.current().nextBoolean();
            event.setDirectional(forward, backward, left, right);
        }
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        class_7439 packet;
        String text;
        if (!this.mode.isSelected("FunTime") || !this.reactOnMessage.isValue() || AntiAFK.mc.field_1724 == null) {
            return;
        }
        class_2596<?> class_25962 = event.getPacket();
        if (class_25962 instanceof class_7439 && (text = (packet = (class_7439)class_25962).comp_763().getString()).contains("\u0414\u0430\u043d\u043d\u0430\u044f \u043a\u043e\u043c\u0430\u043d\u0434\u0430 \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u043d\u0430 \u0432 \u0440\u0435\u0436\u0438\u043c\u0435 AFK")) {
            this.moveTicks = 12;
            if (AntiAFK.mc.field_1724.method_24828()) {
                AntiAFK.mc.field_1724.method_6043();
            }
            AntiAFK.mc.field_1724.method_6104(class_1268.field_5808);
        }
    }

    private void rotate() {
        float direction = ThreadLocalRandom.current().nextBoolean() ? 1.0f : -1.0f;
        float amount = (float)ThreadLocalRandom.current().nextDouble(1.5, 4.0);
        AntiAFK.mc.field_1724.method_36456(AntiAFK.mc.field_1724.method_36454() + direction * amount);
    }

    private boolean isUserActive() {
        return AntiAFK.mc.field_1690.field_1894.method_1434() || AntiAFK.mc.field_1690.field_1881.method_1434() || AntiAFK.mc.field_1690.field_1913.method_1434() || AntiAFK.mc.field_1690.field_1849.method_1434() || AntiAFK.mc.field_1690.field_1903.method_1434() || AntiAFK.mc.field_1690.field_1832.method_1434() || AntiAFK.mc.field_1690.field_1886.method_1434() || AntiAFK.mc.field_1690.field_1904.method_1434();
    }
}

