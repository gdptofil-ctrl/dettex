/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2246
 *  net.minecraft.class_2248
 */
package ru.recoin.implement.features.modules.player;

import net.minecraft.class_2246;
import net.minecraft.class_2248;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.block.PushEvent;
import ru.recoin.implement.events.player.PlayerCollisionEvent;

public class NoPush
extends Module {
    private final MultiSelectSetting ignoreSetting = new MultiSelectSetting("Ignore", "Allows the actions you choose").value("Water", "Block", "Entity Collision", "Powder Snow", "Berry", "World Border");

    public NoPush() {
        super("NoPush", "No Push", ModuleCategory.PLAYER);
        this.setup(this.ignoreSetting);
    }

    public static NoPush getInstance() {
        return Instance.get(NoPush.class);
    }

    public boolean shouldIgnoreBorder() {
        return this.isState() && this.ignoreSetting.isSelected("World Border");
    }

    @EventHandler
    public void onPush(PushEvent e) {
        switch (e.getType()) {
            case COLLISION: {
                e.setCancelled(this.ignoreSetting.isSelected("Entity Collision"));
                break;
            }
            case WATER: {
                e.setCancelled(this.ignoreSetting.isSelected("Water"));
                break;
            }
            case BLOCK: {
                e.setCancelled(this.ignoreSetting.isSelected("Block"));
            }
        }
    }

    @EventHandler
    public void onPlayerCollision(PlayerCollisionEvent e) {
        class_2248 block = e.getBlock();
        if (block.equals(class_2246.field_27879)) {
            e.setCancelled(this.ignoreSetting.isSelected("Powder Snow"));
        } else if (block.equals(class_2246.field_16999)) {
            e.setCancelled(this.ignoreSetting.isSelected("Berry"));
        }
    }
}

