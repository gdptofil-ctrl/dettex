/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11890
 *  net.minecraft.class_1657
 *  net.minecraft.class_310
 */
package ru.recoin.implement.features.modules.render.cosmetics;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_11890;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.GroupSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.modules.render.cosmetics.loader.CosmeticLoader;
import ru.recoin.implement.features.modules.render.cosmetics.model.CosmeticModel;
import ru.recoin.implement.features.modules.render.custompet.CustomPetVariant;
import ru.recoin.implement.features.modules.render.custompet.control.CustomPetFollowerController;
import ru.recoin.implement.features.modules.render.custompet.entity.CustomPetEntity;

public final class Cosmetics
extends Module {
    private static final String[] SWORD_WEAPONS;
    private static final String[] MACE_WEAPONS;
    private static final String[] PET_KINDS;
    private static final String[] CROSSBOW_WEAPONS;
    private static final String[] ROBOT_TYPES;
    private static final String[] WINGS;
    private static final String[] BACKPACKS;
    public static final String SAHUR = "Sahur";
    public static final String BEER = "Beer";
    public static final String CRAZY_RABBIT = "CrazyRabbit";
    private static final String[] CUSTOM_MODELS;
    private static Cosmetics instance;
    private final BooleanSetting swordsEnabled = new BooleanSetting("\u0412\u043a\u043b\u044e\u0447\u0438\u0442\u044c \u043c\u0435\u0447\u0438", "\u0417\u0430\u043c\u0435\u043d\u044f\u0442\u044c \u043c\u043e\u0434\u0435\u043b\u0438 \u043c\u0435\u0447\u0435\u0439").setValue(false);
    private final BooleanSetting swordsSelfOnly = new BooleanSetting("\u0422\u043e\u043b\u044c\u043a\u043e \u0441\u0432\u043e\u0439 \u043c\u0435\u0447", "\u0417\u0430\u043c\u0435\u043d\u044f\u0442\u044c \u043c\u043e\u0434\u0435\u043b\u044c \u0442\u043e\u043b\u044c\u043a\u043e \u0443 \u0441\u0435\u0431\u044f").setValue(true);
    private final SelectSetting swordModel = new SelectSetting("\u041c\u043e\u0434\u0435\u043b\u044c \u043c\u0435\u0447\u0430", "\u0412\u044b\u0431\u043e\u0440 \u043c\u043e\u0434\u0435\u043b\u0438 \u043c\u0435\u0447\u0430").value(Cosmetics.cleanDisplayNames(SWORD_WEAPONS)).selected(Cosmetics.cleanDisplayNames(SWORD_WEAPONS)[0]);
    private final GroupSetting swordsGroup = new GroupSetting("\u041c\u0435\u0447\u0438", "\u041a\u0430\u0441\u0442\u043e\u043c\u043d\u044b\u0435 \u0441\u043a\u0438\u043d\u044b \u043c\u0435\u0447\u0435\u0439", false).settings(this.swordsEnabled, this.swordsSelfOnly, this.swordModel);
    private final SelectSetting maceModel = new SelectSetting("\u041c\u043e\u0434\u0435\u043b\u044c \u0431\u0443\u043b\u0430\u0432\u044b", "\u0412\u044b\u0431\u043e\u0440 \u043c\u043e\u0434\u0435\u043b\u0438 \u0431\u0443\u043b\u0430\u0432\u044b").value(Cosmetics.cleanDisplayNames(MACE_WEAPONS)).selected(Cosmetics.cleanDisplayNames(MACE_WEAPONS)[0]);
    private final BooleanSetting macesEnabled = new BooleanSetting("\u0412\u043a\u043b\u044e\u0447\u0438\u0442\u044c \u0431\u0443\u043b\u0430\u0432\u044b", "\u0417\u0430\u043c\u0435\u043d\u044f\u0442\u044c \u043c\u043e\u0434\u0435\u043b\u0438 \u0431\u0443\u043b\u0430\u0432").setValue(false);
    private final BooleanSetting macesSelfOnly = new BooleanSetting("\u0422\u043e\u043b\u044c\u043a\u043e \u0441\u0432\u043e\u044f \u0431\u0443\u043b\u0430\u0432\u0430", "\u0417\u0430\u043c\u0435\u043d\u044f\u0442\u044c \u043c\u043e\u0434\u0435\u043b\u044c \u0442\u043e\u043b\u044c\u043a\u043e \u0443 \u0441\u0435\u0431\u044f").setValue(true);
    private final GroupSetting macesGroup = new GroupSetting("\u0411\u0443\u043b\u0430\u0432\u044b", "\u041a\u0430\u0441\u0442\u043e\u043c\u043d\u044b\u0435 \u0441\u043a\u0438\u043d\u044b \u0431\u0443\u043b\u0430\u0432", false).settings(this.macesEnabled, this.macesSelfOnly, this.maceModel);
    private final BooleanSetting crossbowsEnabled = new BooleanSetting("\u0412\u043a\u043b\u044e\u0447\u0438\u0442\u044c \u0430\u0440\u0431\u0430\u043b\u0435\u0442", "\u0417\u0430\u043c\u0435\u043d\u044f\u0442\u044c \u043c\u043e\u0434\u0435\u043b\u044c \u0430\u0440\u0431\u0430\u043b\u0435\u0442\u0430").setValue(false);
    private final BooleanSetting crossbowsSelfOnly = new BooleanSetting("\u0422\u043e\u043b\u044c\u043a\u043e \u0441\u0432\u043e\u0439 \u0430\u0440\u0431\u0430\u043b\u0435\u0442", "\u0417\u0430\u043c\u0435\u043d\u044f\u0442\u044c \u043c\u043e\u0434\u0435\u043b\u044c \u0442\u043e\u043b\u044c\u043a\u043e \u0443 \u0441\u0435\u0431\u044f").setValue(true);
    private final SelectSetting crossbowModel = new SelectSetting("\u041c\u043e\u0434\u0435\u043b\u044c \u0430\u0440\u0431\u0430\u043b\u0435\u0442\u0430", "\u0412\u044b\u0431\u043e\u0440 \u043c\u043e\u0434\u0435\u043b\u0438 \u0430\u0440\u0431\u0430\u043b\u0435\u0442\u0430").value(CROSSBOW_WEAPONS).selected(CROSSBOW_WEAPONS[0]);
    private final GroupSetting crossbowsGroup = new GroupSetting("\u0410\u0440\u0431\u0430\u043b\u0435\u0442", "\u041a\u0430\u0441\u0442\u043e\u043c\u043d\u044b\u0435 \u0441\u043a\u0438\u043d\u044b \u0430\u0440\u0431\u0430\u043b\u0435\u0442\u0430", false).settings(this.crossbowsEnabled, this.crossbowsSelfOnly, this.crossbowModel);
    private final SelectSetting petKind = new SelectSetting("\u041f\u0438\u0442\u043e\u043c\u0435\u0446", "\u0422\u0438\u043f \u043f\u0438\u0442\u043e\u043c\u0446\u0430").value(PET_KINDS).selected(PET_KINDS[0]);
    private final SelectSetting petVariant = new SelectSetting("\u0412\u0438\u0434 \u0436\u0430\u0431\u044b", "\u0412\u0430\u0440\u0438\u0430\u043d\u0442 \u0441\u043a\u0438\u043d\u0430 \u0436\u0430\u0431\u044b").value(CustomPetVariant.settingValues()).selected(CustomPetVariant.NITWIT.getSettingValue());
    private final SelectSetting robotType = new SelectSetting("\u0422\u0438\u043f \u0440\u043e\u0431\u043e\u0442\u0430", "\u0412\u044b\u0431\u043e\u0440 \u0441\u043a\u0438\u043d\u0430 \u0440\u043e\u0431\u043e\u0442\u0430").value(ROBOT_TYPES).selected(ROBOT_TYPES[0]);
    private final GroupSetting petsGroup = new GroupSetting("\u041f\u0438\u0442\u043e\u043c\u0446\u044b", "\u041a\u043e\u043c\u043f\u0430\u043d\u044c\u043e\u043d\u044b \u0438 \u043f\u0438\u0442\u043e\u043c\u0446\u044b").setValue(false).settings(this.petKind, this.petVariant, this.robotType);
    private final SelectSetting wingModel = new SelectSetting("\u041c\u043e\u0434\u0435\u043b\u044c \u043a\u0440\u044b\u043b\u044c\u0435\u0432", "\u0412\u044b\u0431\u043e\u0440 \u043c\u043e\u0434\u0435\u043b\u0438 \u043a\u0440\u044b\u043b\u044c\u0435\u0432").value(WINGS).selected(WINGS[0]);
    private final GroupSetting wingsGroup = new GroupSetting("\u041a\u0440\u044b\u043b\u044c\u044f", "\u041a\u0430\u0441\u0442\u043e\u043c\u043d\u044b\u0435 \u043a\u0440\u044b\u043b\u044c\u044f").setValue(false).settings(this.wingModel);
    private final SelectSetting backpackModel = new SelectSetting("\u041c\u043e\u0434\u0435\u043b\u044c \u0440\u044e\u043a\u0437\u0430\u043a\u0430", "\u0412\u044b\u0431\u043e\u0440 \u043c\u043e\u0434\u0435\u043b\u0438 \u0440\u044e\u043a\u0437\u0430\u043a\u0430").value(BACKPACKS).selected(BACKPACKS[0]);
    private final GroupSetting backpacksGroup = new GroupSetting("\u0420\u044e\u043a\u0437\u0430\u043a\u0438", "\u041a\u0430\u0441\u0442\u043e\u043c\u043d\u044b\u0435 \u0440\u044e\u043a\u0437\u0430\u043a\u0438").setValue(false).settings(this.backpackModel);
    private final SelectSetting customModel = new SelectSetting("\u041c\u043e\u0434\u0435\u043b\u044c \u0438\u0433\u0440\u043e\u043a\u0430", "\u0412\u044b\u0431\u043e\u0440 \u043a\u0430\u0441\u0442\u043e\u043c\u043d\u043e\u0439 \u043c\u043e\u0434\u0435\u043b\u0438 \u0438\u0433\u0440\u043e\u043a\u0430").value(CUSTOM_MODELS).selected(CUSTOM_MODELS[0]);
    private final BooleanSetting customModelFriends = new BooleanSetting("\u0414\u0440\u0443\u0437\u044c\u044f", "\u041e\u0442\u043e\u0431\u0440\u0430\u0436\u0430\u0442\u044c \u043a\u0430\u0441\u0442\u043e\u043c\u043d\u044b\u0435 \u043c\u043e\u0434\u0435\u043b\u0438 \u043d\u0430 \u0434\u0440\u0443\u0437\u044c\u044f\u0445").setValue(true);
    private final GroupSetting customModelsGroup = new GroupSetting("\u041a\u0430\u0441\u0442\u043e\u043c\u043d\u044b\u0435 \u043c\u043e\u0434\u0435\u043b\u0438", "\u041a\u0430\u0441\u0442\u043e\u043c\u043d\u044b\u0435 3D \u043c\u043e\u0434\u0435\u043b\u0438 \u0438\u0433\u0440\u043e\u043a\u0430").setValue(false).settings(this.customModel, this.customModelFriends);
    private final CustomPetFollowerController localPetController = new CustomPetFollowerController();
    private static final Map<String, String> SWORD_MAP;
    private static final Map<String, String> MACE_MAP;
    private static final Map<String, CustomPetVariant> VARIANT_MAP;
    private static final Map<String, Integer> ROBOT_MAP;
    private static final Map<String, String> WING_FILE_MAP;
    private static final Map<String, String> BACKPACK_FILE_MAP;
    private final Map<String, CosmeticModel> cachedModels = new ConcurrentHashMap<String, CosmeticModel>();

    public Cosmetics() {
        super("Cosmetics", "Cosmetics", ModuleCategory.RENDER);
        instance = this;
        this.setup(this.swordsGroup, this.macesGroup, this.crossbowsGroup, this.petsGroup, this.wingsGroup, this.backpacksGroup, this.customModelsGroup);
    }

    public static Cosmetics getInstance() {
        return Instance.get(Cosmetics.class);
    }

    public boolean isCustomModelsActive() {
        return this.isState() && this.customModelsGroup.isValue();
    }

    public String getActiveCustomModel() {
        return this.customModel.getSelected();
    }

    public boolean shouldApplyCustomModel(class_11890 player) {
        boolean localPlayer;
        if (!this.isCustomModelsActive() || player == null || player.method_7325()) {
            return false;
        }
        class_310 client = class_310.method_1551();
        boolean bl = localPlayer = client != null && client.field_1724 != null && player == client.field_1724;
        if (localPlayer) {
            return true;
        }
        return this.customModelFriends.isValue() && player.method_5477() != null && FriendUtils.isFriend(player.method_5477().getString());
    }

    @Override
    public void deactivate() {
        this.localPetController.reset();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (!this.isState()) {
            return;
        }
        if (Cosmetics.mc.field_1724 == null || Cosmetics.mc.field_1687 == null) {
            this.localPetController.reset();
            return;
        }
        if (this.petsGroup.isValue()) {
            try {
                CustomPetVariant variant = this.getSelectedPetVariant();
                boolean isOwl = "\u0421\u043e\u0432\u0430".equals(this.petKind.getSelected());
                this.localPetController.tick((class_1657)Cosmetics.mc.field_1724, variant, isOwl);
                CustomPetEntity pet = this.localPetController.getPet();
                if (pet != null) {
                    pet.setRobotType(this.getSelectedRobotType());
                }
            }
            catch (Throwable throwable) {}
        } else {
            this.localPetController.reset();
        }
    }

    public String getActualSword() {
        String selected = this.swordModel.getSelected();
        if (selected == null) {
            return "";
        }
        String original = SWORD_MAP.get(selected);
        if (original == null) {
            original = SWORD_MAP.get(selected.toLowerCase(Locale.ROOT));
        }
        return original != null ? original : selected;
    }

    public String getActualMace() {
        String selected = this.maceModel.getSelected();
        if (selected == null) {
            return "";
        }
        String original = MACE_MAP.get(selected);
        if (original == null) {
            original = MACE_MAP.get(selected.toLowerCase(Locale.ROOT));
        }
        return original != null ? original : selected;
    }

    private CustomPetVariant getSelectedPetVariant() {
        String name = this.petVariant.getSelected();
        if (name == null) {
            return CustomPetVariant.NITWIT;
        }
        CustomPetVariant v = VARIANT_MAP.get(name.toLowerCase());
        return v != null ? v : CustomPetVariant.NITWIT;
    }

    private int getSelectedRobotType() {
        String sel = this.robotType.getSelected();
        if (sel == null) {
            return 1;
        }
        Integer val = ROBOT_MAP.get(sel.toLowerCase());
        return val != null ? val : 1;
    }

    private static String[] cleanDisplayNames(String[] original) {
        String[] res = new String[original.length];
        for (int i = 0; i < original.length; ++i) {
            res[i] = original[i].replace("'", "");
        }
        return res;
    }

    public CosmeticModel getActiveWingModel() {
        String sel = this.wingModel.getSelected();
        if (sel == null) {
            return null;
        }
        String fileName = WING_FILE_MAP.get(sel.toLowerCase());
        if (fileName == null) {
            return null;
        }
        return this.getOrLoadCosmetic(fileName);
    }

    public CosmeticModel getActiveBackpackModel() {
        String sel = this.backpackModel.getSelected();
        if (sel == null) {
            return null;
        }
        String fileName = BACKPACK_FILE_MAP.get(sel.toLowerCase());
        if (fileName == null) {
            return null;
        }
        return this.getOrLoadCosmetic(fileName);
    }

    private CosmeticModel getOrLoadCosmetic(String fileName) {
        return this.cachedModels.computeIfAbsent(fileName, f -> CosmeticLoader.getInstance().loadFromResource("/assets/recoin/cosmetics/models/" + f));
    }

    public BooleanSetting getSwordsEnabled() {
        return this.swordsEnabled;
    }

    public BooleanSetting getSwordsSelfOnly() {
        return this.swordsSelfOnly;
    }

    public SelectSetting getSwordModel() {
        return this.swordModel;
    }

    public GroupSetting getSwordsGroup() {
        return this.swordsGroup;
    }

    public SelectSetting getMaceModel() {
        return this.maceModel;
    }

    public BooleanSetting getMacesEnabled() {
        return this.macesEnabled;
    }

    public BooleanSetting getMacesSelfOnly() {
        return this.macesSelfOnly;
    }

    public GroupSetting getMacesGroup() {
        return this.macesGroup;
    }

    public BooleanSetting getCrossbowsEnabled() {
        return this.crossbowsEnabled;
    }

    public BooleanSetting getCrossbowsSelfOnly() {
        return this.crossbowsSelfOnly;
    }

    public SelectSetting getCrossbowModel() {
        return this.crossbowModel;
    }

    public GroupSetting getCrossbowsGroup() {
        return this.crossbowsGroup;
    }

    public SelectSetting getPetKind() {
        return this.petKind;
    }

    public SelectSetting getPetVariant() {
        return this.petVariant;
    }

    public SelectSetting getRobotType() {
        return this.robotType;
    }

    public GroupSetting getPetsGroup() {
        return this.petsGroup;
    }

    public SelectSetting getWingModel() {
        return this.wingModel;
    }

    public GroupSetting getWingsGroup() {
        return this.wingsGroup;
    }

    public SelectSetting getBackpackModel() {
        return this.backpackModel;
    }

    public GroupSetting getBackpacksGroup() {
        return this.backpacksGroup;
    }

    public SelectSetting getCustomModel() {
        return this.customModel;
    }

    public BooleanSetting getCustomModelFriends() {
        return this.customModelFriends;
    }

    public GroupSetting getCustomModelsGroup() {
        return this.customModelsGroup;
    }

    public CustomPetFollowerController getLocalPetController() {
        return this.localPetController;
    }

    public Map<String, CosmeticModel> getCachedModels() {
        return this.cachedModels;
    }

    /*
     * WARNING - void declaration
     */
    static {
        int var0_4 = 0;
        String displayName;
        SWORD_WEAPONS = new String[]{"Abominable Blade", "Abominable Great Saber", "Acidic Cleaver", "Amethyst Shuriken", "Ancient Royal Great Sword", "Aquatic Sacred Blade", "Ashura's Blade", "Awakened Lichblade", "Blood Edge", "Bramblethorn", "Brimstone Claymore", "Carian Knight's Sword", "Chrono Blade", "Corrupted Mythic Blade", "Creation Splitter", "Cyber Katana", "Cyber Mantis Blade", "Cyber Sword", "Cybernetic Chainsaw Blade", "Cybernetic Katana", "Cybernetic Knife", "Dainsleif", "Dark Blade", "Dark Cleaver", "Death Knight's Dagger", "Death Knight's Sword", "Demon Lord's Sword", "Demonic Blade", "Demonic Cleaver", "Divine Justice", "Divine Punisher", "Dragon Slaying blade", "Edge Of The Astral Plane", "Emberblade", "Enigma", "Epic Sword", "Estoc", "Fallen God's Spear", "Fallen God's Sword", "Floral Longsword", "Floral Sabre", "Forest Guardian's Glaive", "Frost Blade", "Hearthflame", "Hero Sword", "Holy Moonlight Sword", "Hornet's Needle", "Jade Halberd", "Katana", "Legendary Sword", "Longsword", "Masamune", "Mjolnir", "Molten Blade", "Molten Sword", "Muramasa", "Mystical Spellblade", "Mythic Blade", "Ocean's Rage", "Partisan", "Pharaoh's Treasure", "Pheonix Grace", "Plague Longsword", "Power Fuse Hammer", "Power Fuse Sword", "Ribbon Cleaver", "Righteous Relic", "Rivers Of Blood", "Royal Chakram", "Royal Rapier", "Sabre", "Scissor Blade", "Sculk Cleaver", "Sculk Scythe", "Sculk Sword", "Sentinel's Will", "Silverine Blade", "Soul Claws", "Soul Collector", "Soul Devourer", "Soul Edge", "Soul Harvester", "Soul Stealer", "Soulrender", "Star's Edge", "Steel Sword", "Stop Sign", "Storm Bringer", "Storm's Edge", "Sunbreak", "Tengen's Blade", "Terra Blade", "Thousand Demon Daggers", "Thunder Bringer", "Thunderbrand", "True Excalibur", "Vampiric Needle", "Wakizashi", "Watcher Claymore", "Watching Warglaive", "Waxweaver", "Whisperwind", "Wickpiercer", "Yoru"};
        MACE_WEAPONS = new String[]{"Abominable Scythe", "Arcanethyst", "Bloody Death", "Crescent Rose", "Demigod's Unholy Blade", "Demigod's Unholy Halberd", "Demon Lord's Great Axe", "Divine Axe Rhitta", "Divine Reaper", "Icewhisper", "Magi Scythe", "Requiem of the Ninth Abyss", "Wraith Scythe"};
        PET_KINDS = new String[]{"\u0416\u0430\u0431\u0430", "\u0420\u043e\u0431\u043e\u0442", "\u0421\u043e\u0432\u0430", "\u0427\u0435\u043a\u0443\u0448\u043a\u0430", "\u041a\u043e\u0437\u043b\u0438\u043a"};
        CROSSBOW_WEAPONS = new String[]{"\u041a\u0430\u043b\u0430\u0448", "AWP", "\u0420\u041f\u0413"};
        ROBOT_TYPES = new String[]{"\u0420\u043e\u0431\u043e\u0442 1", "\u0420\u043e\u0431\u043e\u0442 2", "\u0420\u043e\u0431\u043e\u0442 3", "\u0420\u043e\u0431\u043e\u0442 4"};
        WINGS = new String[]{"\u041a\u0440\u044b\u043b\u044c\u044f \u0430\u043d\u0433\u0435\u043b\u0430", "\u041a\u0440\u044b\u043b\u044c\u044f \u0432\u0443\u043b\u043a\u0430\u043d\u0430", "\u041a\u0440\u044b\u043b\u044c\u044f \u0434\u0440\u0430\u043a\u043e\u043d\u0447\u0438\u043a\u0430"};
        BACKPACKS = new String[]{"Frog Backpack", "Backpack 1", "Backpack 2", "Backpack 3", "Backpack 4", "Backpack 5", "Backpack 6", "Backpack 7", "Backpack 8", "Backpack 9"};
        CUSTOM_MODELS = new String[]{SAHUR, BEER, CRAZY_RABBIT};
        SWORD_MAP = new HashMap<String, String>();
        MACE_MAP = new HashMap<String, String>();
        VARIANT_MAP = new HashMap<String, CustomPetVariant>();
        ROBOT_MAP = new HashMap<String, Integer>();
        for (String string : SWORD_WEAPONS) {
            displayName = string.replace("'", "");
            SWORD_MAP.put(displayName, string);
            SWORD_MAP.put(string, string);
            SWORD_MAP.put(displayName.toLowerCase(Locale.ROOT), string);
        }
        for (String string : MACE_WEAPONS) {
            displayName = string.replace("'", "");
            MACE_MAP.put(displayName, string);
            MACE_MAP.put(string, string);
            MACE_MAP.put(displayName.toLowerCase(Locale.ROOT), string);
        }
        for (CustomPetVariant customPetVariant : CustomPetVariant.values()) {
            VARIANT_MAP.put(customPetVariant.getSettingValue().toLowerCase(), customPetVariant);
        }
        boolean bl = false;
        while (var0_4 < ROBOT_TYPES.length) {
            ROBOT_MAP.put(ROBOT_TYPES[var0_4].toLowerCase(), (int)(var0_4 + 1));
            ++var0_4;
        }
        WING_FILE_MAP = new HashMap<String, String>();
        BACKPACK_FILE_MAP = new HashMap<String, String>();
        WING_FILE_MAP.put("\u043a\u0440\u044b\u043b\u044c\u044f \u0430\u043d\u0433\u0435\u043b\u0430", "cosmetic_22.json");
        WING_FILE_MAP.put("\u043a\u0440\u044b\u043b\u044c\u044f \u0432\u0443\u043b\u043a\u0430\u043d\u0430", "cosmetic_26.json");
        WING_FILE_MAP.put("\u043a\u0440\u044b\u043b\u044c\u044f \u0434\u0440\u0430\u043a\u043e\u043d\u0447\u0438\u043a\u0430", "cosmetic_23.json");
        WING_FILE_MAP.put("angel wings", "cosmetic_22.json");
        WING_FILE_MAP.put("vulcano wings", "cosmetic_26.json");
        WING_FILE_MAP.put("baby dragon wings", "cosmetic_23.json");
        BACKPACK_FILE_MAP.put("frog backpack", "cosmetic_28.json");
        BACKPACK_FILE_MAP.put("backpack 1", "cosmetic_37.json");
        BACKPACK_FILE_MAP.put("backpack 2", "cosmetic_36.json");
        BACKPACK_FILE_MAP.put("backpack 3", "cosmetic_35.json");
        BACKPACK_FILE_MAP.put("backpack 4", "cosmetic_33.json");
        BACKPACK_FILE_MAP.put("backpack 5", "cosmetic_29.json");
        BACKPACK_FILE_MAP.put("backpack 6", "cosmetic_30.json");
        BACKPACK_FILE_MAP.put("backpack 7", "cosmetic_31.json");
        BACKPACK_FILE_MAP.put("backpack 8", "cosmetic_32.json");
        BACKPACK_FILE_MAP.put("backpack 9", "cosmetic_34.json");
    }
}

