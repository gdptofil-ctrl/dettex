package ru.recoin.implement.features.modules.misc.minigames;

import net.minecraft.class_310;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.implement.events.player.TickEvent;

/** Quiz lessons about Minecraft and anarchy servers. Turning the module on opens its screen (bind a key to open it quickly). */
public final class MineQuiz
extends Module {
    private boolean open;

    public MineQuiz() {
        super("MineQuiz", "MineQuiz", ModuleCategory.MISC);
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
        class_310.method_1551().method_1507(new QuizScreen());
        this.setState(false);   // a "button" module: it only opens the screen
    }
}
