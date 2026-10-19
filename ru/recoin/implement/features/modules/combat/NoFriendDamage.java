/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.combat;

import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.implement.events.player.AttackEvent;

public class NoFriendDamage
extends Module {
    public NoFriendDamage() {
        super("NoFriendDamage", "No Friend Damage", ModuleCategory.COMBAT);
    }

    @EventHandler
    public void onAttack(AttackEvent e) {
        if (!e.isPre()) {
            return;
        }
        e.setCancelled(FriendUtils.isFriend(e.getEntity()));
    }
}

