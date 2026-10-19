/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_1657
 *  net.minecraft.class_1707
 *  net.minecraft.class_1713
 *  net.minecraft.class_2596
 *  net.minecraft.class_437
 *  net.minecraft.class_465
 *  net.minecraft.class_7439
 */
package ru.recoin.implement.features.modules.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.class_124;
import net.minecraft.class_1657;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_2596;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_7439;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BindSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.common.util.other.StopWatch;
import ru.recoin.implement.events.keyboard.KeyEvent;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.TickEvent;

public class AutoDuel
extends Module {
    private static final Pattern PLAYER_NAME = Pattern.compile("^\\w{3,16}$");
    private final SelectSetting mode = new SelectSetting("Mode", "Duel kit").value("Ball", "Shield", "Spikes", "Netherite", "CheatParadise", "Bow", "Classic", "Totems", "NoDebuff").selected("Ball");
    private final BindSetting disableBind = new BindSetting("Disable Key", "Disable AutoDuel");
    private final transient List<String> challenged = new ArrayList<String>();
    private final transient StopWatch timer = new StopWatch();
    private transient int nextPlayer;

    public AutoDuel() {
        super("AutoDuel", "Auto Duel", ModuleCategory.MISC);
        this.setup(this.mode, this.disableBind);
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        class_2596<?> class_25962;
        if (!event.isReceive() || !((class_25962 = event.getPacket()) instanceof class_7439)) {
            return;
        }
        class_7439 packet = (class_7439)class_25962;
        String text = AutoDuel.normalize(packet.comp_763().getString());
        if (text.isEmpty() || text.contains("\u043d\u0430\u0447\u0430\u043b\u043e") && text.contains("\u0447\u0435\u0440\u0435\u0437") && text.contains("\u0441\u0435\u043a\u0443\u043d\u0434")) {
            mc.execute(() -> this.setState(false));
        }
    }

    @EventHandler
    public void onKey(KeyEvent event) {
        if (this.disableBind.getKey() != -1 && event.isKeyDown(this.disableBind.getKey())) {
            this.setState(false);
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        class_465 screen;
        Object class_4372;
        if (AutoDuel.mc.field_1724 == null || mc.method_1562() == null || AutoDuel.mc.field_1761 == null) {
            return;
        }
        List<String> players = mc.method_1562().method_2880().stream().map(entry -> entry.method_2966().name()).filter(name -> PLAYER_NAME.matcher((CharSequence)name).matches()).toList();
        if (this.timer.finished(800L * (long)Math.max(1, players.size()))) {
            this.challenged.clear();
            this.nextPlayer = 0;
            this.timer.reset();
        }
        if (!players.isEmpty() && this.timer.finished(1000.0)) {
            String name2;
            if (this.nextPlayer >= players.size()) {
                this.nextPlayer = 0;
            }
            if (!this.challenged.contains(name2 = players.get(this.nextPlayer++)) && !name2.equals(AutoDuel.mc.field_1724.method_7334().name())) {
                mc.method_1562().method_45730("duel " + name2);
                this.challenged.add(name2);
            }
            this.timer.reset();
        }
        if ((class_4372 = AutoDuel.mc.field_1755) instanceof class_465 && (class_4372 = (screen = (class_465)class_4372).method_17577()) instanceof class_1707) {
            int slot;
            class_1707 handler = (class_1707)class_4372;
            String title = AutoDuel.normalize(screen.method_25440().getString());
            slot = title.contains("\u0432\u044b\u0431\u043e\u0440 \u043d\u0430\u0431\u043e\u0440\u0430 (1/1)") ? Kit.valueOf(this.mode.getSelected()).ordinal() : (title.contains("\u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430 \u043f\u043e\u0435\u0434\u0438\u043d\u043a\u0430") ? 0 : -1);
            if (slot >= 0 && this.timer.finished(150.0)) {
                AutoDuel.mc.field_1761.method_2906(handler.field_7763, slot, 0, class_1713.field_7794, (class_1657)AutoDuel.mc.field_1724);
                this.timer.reset();
            }
        }
    }

    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String stripped = class_124.method_539((String)text);
        return (stripped == null ? text : stripped).toLowerCase();
    }

    private static enum Kit {
        Shield,
        Spikes,
        Bow,
        Totems,
        NoDebuff,
        Ball,
        Classic,
        CheatParadise,
        Netherite;

    }
}

