package ru.recoin.implement.features.modules.misc.minigames;

import net.minecraft.class_310;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.implement.events.player.TickEvent;

/** Upgrade virtual items: x2, x5, x8 with a spinning wheel. Turning the module on opens its screen (bind a key to open it quickly). */
public final class Upgrader
extends Module {
    private boolean open;

    public Upgrader() {
        super("Upgrader", "Upgrader", ModuleCategory.MISC);
    }

    @Override
    public void activate() {
        this.open = true;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (!this.open) {
            return;
        }
        this.open = false;
        class_310.method_1551().method_1507(new UpgraderScreen());
        this.setState(false);   // a "button" module: it only opens the screen
    }
}
