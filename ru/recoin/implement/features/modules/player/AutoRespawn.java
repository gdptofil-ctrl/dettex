/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.player;

import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.implement.events.player.DeathScreenEvent;

public class AutoRespawn
extends Module {
    public AutoRespawn() {
        super("AutoRespawn", "Auto Respawn", ModuleCategory.PLAYER);
    }

    @EventHandler
    public void onDeathScreen(DeathScreenEvent e) {
        AutoRespawn.mc.field_1724.method_7331();
        mc.method_1507(null);
    }
}

