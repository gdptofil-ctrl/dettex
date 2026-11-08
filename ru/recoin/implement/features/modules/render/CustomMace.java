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

public final class CustomMace
extends Module {
    private static final String[] WEAPONS = new String[]{"Abominable Scythe", "Arcanethyst", "Bloody Death", "Crescent Rose", "Demigod's Unholy Blade", "Demigod's Unholy Halberd", "Demon Lord's Great Axe", "Divine Axe Rhitta", "Divine Reaper", "Icewhisper", "Magi Scythe", "Requiem of the Ninth Abyss", "Wraith Scythe", "Frost Axe", "Frost Scythe", "3D Mace", "Warhammer", "Toothed Macuahuitl", "Makeshift Macuahuitl", "Photon Axe", "Old World Axe"};
    private static final String[] DISPLAY_WEAPONS = CustomMace.displayNames();
    private static CustomMace instance;
    private final BooleanSetting selfOnly = new BooleanSetting("\u0422\u043e\u043b\u044c\u043a\u043e \u0441\u0432\u043e\u0439", "\u0417\u0430\u043c\u0435\u043d\u044f\u0442\u044c \u043c\u043e\u0434\u0435\u043b\u044c \u0431\u0443\u043b\u0430\u0432\u044b \u0442\u043e\u043b\u044c\u043a\u043e \u0443 \u0441\u0432\u043e\u0435\u0433\u043e \u0438\u0433\u0440\u043e\u043a\u0430").setValue(true);
    private final SelectSetting weapon = new SelectSetting("\u041e\u0440\u0443\u0436\u0438\u0435", "\u041c\u043e\u0434\u0435\u043b\u044c, \u043a\u043e\u0442\u043e\u0440\u0430\u044f \u0437\u0430\u043c\u0435\u043d\u0438\u0442 \u0432\u0430\u043d\u0438\u043b\u044c\u043d\u0443\u044e \u0431\u0443\u043b\u0430\u0432\u0443").value(DISPLAY_WEAPONS).selected(DISPLAY_WEAPONS[0]);
    private static final Map<String, String> WEAPON_MAP;

    public CustomMace() {
        super("CustomMace", "Custom Mace", ModuleCategory.RENDER);
        this.setup(this.selfOnly, this.weapon);
        instance = this;
    }

    public static CustomMace getInstance() {
        if (instance != null) {
            return instance;
        }
        try {
            CustomMace mod = Instance.get(CustomMace.class);
            if (mod != null) {
                return mod;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        if (instance == null) {
            instance = new CustomMace();
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
        return instance != null && CustomMace.instance.selfOnly.isValue();
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

