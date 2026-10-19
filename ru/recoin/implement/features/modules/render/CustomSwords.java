/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.render;

import java.util.HashMap;
import java.util.Map;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.common.util.other.Instance;

public final class CustomSwords
extends Module {
    private static final String[] WEAPONS = new String[]{"Abominable Blade", "Abominable Great Saber", "Acidic Cleaver", "Amethyst Shuriken", "Ancient Royal Great Sword", "Aquatic Sacred Blade", "Ashura's Blade", "Awakened Lichblade", "Blood Edge", "Bramblethorn", "Brimstone Claymore", "Carian Knight's Sword", "Chrono Blade", "Corrupted Mythic Blade", "Creation Splitter", "Cyber Katana", "Cyber Mantis Blade", "Cyber Sword", "Cybernetic Chainsaw Blade", "Cybernetic Katana", "Cybernetic Knife", "Dainsleif", "Dark Blade", "Dark Cleaver", "Death Knight's Dagger", "Death Knight's Sword", "Demon Lord's Sword", "Demonic Blade", "Demonic Cleaver", "Divine Justice", "Divine Punisher", "Dragon Slaying blade", "Edge Of The Astral Plane", "Emberblade", "Enigma", "Epic Sword", "Estoc", "Fallen God's Spear", "Fallen God's Sword", "Floral Longsword", "Floral Sabre", "Forest Guardian's Glaive", "Frost Blade", "Hearthflame", "Hero Sword", "Holy Moonlight Sword", "Hornet's Needle", "Jade Halberd", "Katana", "Legendary Sword", "Longsword", "Masamune", "Mjolnir", "Molten Blade", "Molten Sword", "Muramasa", "Mystical Spellblade", "Mythic Blade", "Ocean's Rage", "Partisan", "Pharaoh's Treasure", "Pheonix Grace", "Plague Longsword", "Power Fuse Hammer", "Power Fuse Sword", "Ribbon Cleaver", "Righteous Relic", "Rivers Of Blood", "Royal Chakram", "Royal Rapier", "Sabre", "Scissor Blade", "Sculk Cleaver", "Sculk Scythe", "Sculk Sword", "Sentinel's Will", "Silverine Blade", "Soul Claws", "Soul Collector", "Soul Devourer", "Soul Edge", "Soul Harvester", "Soul Stealer", "Soulrender", "Star's Edge", "Steel Sword", "Stop Sign", "Storm Bringer", "Storm's Edge", "Sunbreak", "Tengen's Blade", "Terra Blade", "Thousand Demon Daggers", "Thunder Bringer", "Thunderbrand", "True Excalibur", "Vampiric Needle", "Wakizashi", "Watcher Claymore", "Watching Warglaive", "Waxweaver", "Whisperwind", "Wickpiercer", "Yoru"};
    private static final String[] DISPLAY_WEAPONS = CustomSwords.displayNames();
    private static CustomSwords instance;
    private final BooleanSetting selfOnly = new BooleanSetting("\u0422\u043e\u043b\u044c\u043a\u043e \u0441\u0432\u043e\u0439", "\u0417\u0430\u043c\u0435\u043d\u044f\u0442\u044c \u043c\u043e\u0434\u0435\u043b\u044c \u043c\u0435\u0447\u0430 \u0442\u043e\u043b\u044c\u043a\u043e \u0443 \u0441\u0432\u043e\u0435\u0433\u043e \u0438\u0433\u0440\u043e\u043a\u0430").setValue(true);
    private final SelectSetting weapon = new SelectSetting("\u041e\u0440\u0443\u0436\u0438\u0435", "\u041c\u043e\u0434\u0435\u043b\u044c, \u043a\u043e\u0442\u043e\u0440\u0430\u044f \u0437\u0430\u043c\u0435\u043d\u0438\u0442 \u0432\u0430\u043d\u0438\u043b\u044c\u043d\u044b\u0435 \u043c\u0435\u0447\u0438").value(DISPLAY_WEAPONS).selected(DISPLAY_WEAPONS[0]);
    private static final Map<String, String> WEAPON_MAP;

    public CustomSwords() {
        super("CustomSwords", "Custom Swords", ModuleCategory.RENDER);
        this.setup(this.selfOnly, this.weapon);
        instance = this;
    }

    public static CustomSwords getInstance() {
        if (instance != null) {
            return instance;
        }
        try {
            CustomSwords mod = Instance.get(CustomSwords.class);
            if (mod != null) {
                return mod;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        if (instance == null) {
            instance = new CustomSwords();
        }
        return instance;
    }

    private static String[] displayNames() {
        String[] stringArray = new String[WEAPONS.length];
        for (int i = 0; i < WEAPONS.length; ++i) {
            stringArray[i] = WEAPONS[i].replace("'", "");
        }
        return stringArray;
    }

    public static boolean isSelfOnly() {
        return instance != null && CustomSwords.instance.selfOnly.isValue();
    }

    public String getSelectedWeapon() {
        String string = this.weapon.getSelected();
        String original = WEAPON_MAP.get(string);
        return original != null ? original : string;
    }

    static {
        WEAPON_MAP = new HashMap<String, String>();
        for (int i = 0; i < WEAPONS.length; ++i) {
            WEAPON_MAP.put(DISPLAY_WEAPONS[i], WEAPONS[i]);
        }
    }
}

