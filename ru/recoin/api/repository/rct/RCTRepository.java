/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 */
package ru.recoin.api.repository.rct;

import net.minecraft.class_124;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.event.EventManager;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.other.StopWatch;
import ru.recoin.common.util.world.ServerUtil;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.draggables.Notifications;
import ru.recoin.implement.features.modules.misc.PvPSafe;

public class RCTRepository
implements QuickImports {
    private final StopWatch stopWatch = new StopWatch();
    private int anarchy;
    private boolean pendingJoin;

    public RCTRepository(EventManager eventManager) {
        eventManager.register(this);
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (!this.pendingJoin) {
            return;
        }
        if (RCTRepository.mc.field_1724 == null || RCTRepository.mc.field_1724.field_3944 == null) {
            this.reset();
            return;
        }
        if (this.stopWatch.finished(800.0)) {
            RCTRepository.mc.field_1724.field_3944.method_45730("an" + this.anarchy);
            this.reset();
        }
    }

    public void reconnect(int anarchy) {
        if (PvPSafe.getInstance().isState() && ServerUtil.isPvp()) {
            PvPSafe.getInstance().warnBlocked();
            return;
        }
        if (anarchy <= 0) {
            Notifications.getInstance().addList("[RCT] " + String.valueOf(class_124.field_1061) + "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043e\u043f\u0440\u0435\u0434\u0435\u043b\u0438\u0442\u044c \u0430\u043d\u0430\u0440\u0445\u0438\u044e", 3000L);
            return;
        }
        if (RCTRepository.mc.field_1724 == null || RCTRepository.mc.field_1724.field_3944 == null) {
            return;
        }
        this.anarchy = anarchy;
        this.pendingJoin = true;
        this.stopWatch.reset();
        RCTRepository.mc.field_1724.field_3944.method_45730("hub");
        Notifications.getInstance().addList("[RCT] \u041f\u0435\u0440\u0435\u0437\u0430\u0445\u043e\u0434 \u043d\u0430 \u0430\u043d\u0430\u0440\u0445\u0438\u044e " + String.valueOf(class_124.field_1060) + anarchy, 2000L);
    }

    private void reset() {
        this.pendingJoin = false;
        this.anarchy = 0;
    }
}

