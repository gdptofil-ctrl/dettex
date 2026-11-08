package ru.recoin.implement.features.modules.misc;

import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.Setting;
import ru.recoin.api.feature.module.setting.implement.BindSetting;
import ru.recoin.api.feature.module.setting.implement.GroupSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.implement.events.keyboard.KeyEvent;

/** Soundboard: eight slots, each with a sound and a key. Sounds are played locally (only you hear them). */
public final class Soundpad
extends Module {
    private static final int SLOTS = 8;
    private static final String[] SOUNDS = ClientSoundPack.names("soundpad");
    private static final String[] DEFAULTS = new String[]{"67", "Fight!", "Multi Kill", "Combo Breaker", "Fire in the Hole", "You Lose", "Взрыв", "Flawless Victory"};

    private final SelectSetting[] sounds = new SelectSetting[SLOTS];
    private final BindSetting[] keys = new BindSetting[SLOTS];
    private final ValueSetting volume = new ValueSetting("Громкость", "Громкость саундпада").setValue(90.0f).range(0, 100);
    private final long[] lastPlayed = new long[SLOTS];

    public Soundpad() {
        super("Soundpad", "Саундпад", ModuleCategory.MISC);
        Setting[] all = new Setting[SLOTS + 1];
        for (int i = 0; i < SLOTS; ++i) {
            String def = Soundpad.contains(DEFAULTS[i]) ? DEFAULTS[i] : SOUNDS[Math.min(i, SOUNDS.length - 1)];
            this.sounds[i] = new SelectSetting("Звук " + (i + 1), "Что играет слот " + (i + 1)).value(SOUNDS).selected(def);
            this.keys[i] = new BindSetting("Клавиша " + (i + 1), "Клавиша слота " + (i + 1));
            all[i] = new GroupSetting("Слот " + (i + 1), "Звук и клавиша", false).settings(this.sounds[i], this.keys[i]);
        }
        all[SLOTS] = this.volume;
        this.setup(all);
    }

    private static boolean contains(String name) {
        for (String sound : SOUNDS) {
            if (sound.equals(name)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onKey(KeyEvent event) {
        long now = System.currentTimeMillis();
        for (int i = 0; i < SLOTS; ++i) {
            int key = this.keys[i].getKey();
            if (key == -1 || !event.isKeyDown(key)) continue;
            if (now - this.lastPlayed[i] < 150L) continue;   // ignore key repeat spam
            this.lastPlayed[i] = now;
            ClientSoundPack.play("soundpad", this.sounds[i].getSelected(), this.volume.getValue() / 100.0f);
        }
    }
}
