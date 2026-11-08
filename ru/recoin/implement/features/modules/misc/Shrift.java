package ru.recoin.implement.features.modules.misc;

import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.player.TickEvent;

/**
 * Shrift: custom fonts (all with Cyrillic, OFL licensed). The chosen font is used in MineQuiz and
 * Upgrader, and optionally for all client interface text (HUD, menus) instead of Inter.
 */
public final class Shrift
extends Module {
    public static final String[] NAMES = {"Marck Script", "Bad Script", "Caveat", "Lobster", "Pacifico", "Comfortaa", "Unbounded"};
    private static final Fonts.Type[] TYPES = {Fonts.Type.MARCK_SCRIPT, Fonts.Type.BAD_SCRIPT, Fonts.Type.CAVEAT, Fonts.Type.LOBSTER, Fonts.Type.PACIFICO, Fonts.Type.COMFORTAA, Fonts.Type.UNBOUNDED};
    /** Font of MineQuiz / Upgrader while this module is off. */
    public static final Fonts.Type DEFAULT_MINIGAME_FONT = Fonts.Type.COMFORTAA;

    private final SelectSetting font = new SelectSetting("Font", "Marck Script, Bad Script, Caveat, Lobster, Pacifico — cursive; Comfortaa — round; Unbounded — wide").value(NAMES).selected("Marck Script");
    private final BooleanSetting miniGames = new BooleanSetting("Mini Games", "Use the font in MineQuiz and Upgrader").setValue(true);
    private final BooleanSetting clientInterface = new BooleanSetting("Client Interface", "Use the font for all client text: HUD, ClickGUI, notifications (may not fit everywhere)").setValue(false);

    public Shrift() {
        super("Shrift", "Shrift", ModuleCategory.MISC);
        this.setup(this.font, this.miniGames, this.clientInterface);
    }

    private Fonts.Type selected() {
        for (int i = 0; i < NAMES.length; ++i) {
            if (this.font.isSelected(NAMES[i])) {
                return TYPES[i];
            }
        }
        return TYPES[0];
    }

    /** Font for MineQuiz / Upgrader. */
    public static Fonts.Type miniGameFont() {
        try {
            Shrift module = Instance.get(Shrift.class);
            if (module != null && module.isState() && module.miniGames.isValue()) {
                return module.selected();
            }
        } catch (Throwable ignored) {
        }
        return DEFAULT_MINIGAME_FONT;
    }

    private void apply() {
        Fonts.interfaceOverride = this.isState() && this.clientInterface.isValue() ? this.selected() : null;
    }

    @Override
    public void activate() {
        this.apply();
    }

    @Override
    public void deactivate() {
        Fonts.interfaceOverride = null;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        this.apply();   // follows the settings while the module is on
    }
}
