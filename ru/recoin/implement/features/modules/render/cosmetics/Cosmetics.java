package ru.recoin.implement.features.modules.render.cosmetics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_11890;
import net.minecraft.class_12079;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.GroupSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.TextSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.features.modules.render.cosmetics.loader.CosmeticLoader;
import ru.recoin.implement.features.modules.render.cosmetics.model.CosmeticModel;
import ru.recoin.implement.features.modules.render.cosmetics.sync.CosmeticSync;
import ru.recoin.implement.events.player.TickEvent;

public final class Cosmetics
extends Module {
    private static final String[] MACE_WEAPONS = new String[]{"Abominable Scythe", "Arcanethyst", "Bloody Death", "Crescent Rose", "Demigod's Unholy Blade", "Demigod's Unholy Halberd", "Demon Lord's Great Axe", "Divine Axe Rhitta", "Divine Reaper", "Icewhisper", "Magi Scythe", "Requiem of the Ninth Abyss", "Wraith Scythe", "Frost Axe", "Frost Scythe", "3D Mace", "Warhammer", "Toothed Macuahuitl", "Makeshift Macuahuitl", "Photon Axe", "Old World Axe"};
    private static final String[] SWORD_WEAPONS = new String[]{"Katana", "Mineral Greatsword", "Kitsune Blade", "Kitsune Dagger", "Old World Sword", "Grass Blade", "Foam Sword", "Cardboard Sword", "Plank Sword", "Bayonet", "Sai", "Bamboo Stick"};
    private static final String[] MACE_DISPLAY = Cosmetics.cleanDisplayNames(MACE_WEAPONS);
    private static final String[] SWORD_DISPLAY = Cosmetics.cleanDisplayNames(SWORD_WEAPONS);
    private static final String[] PLAYER_MODELS = CosmeticCatalog.names(CosmeticCatalog.PLAYER_MODELS);
    private static final String[] WINGS = CosmeticCatalog.names(CosmeticCatalog.WINGS);
    private static final String[] HATS = CosmeticCatalog.names(CosmeticCatalog.HATS);
    private static final String[] BACKPACKS = CosmeticCatalog.names(CosmeticCatalog.BACKPACKS);
    private static final String[] HALOS = CosmeticCatalog.names(CosmeticCatalog.HALOS);
    private static final String[] CAPES = CosmeticCatalog.names(CosmeticCatalog.CAPES);
    private static final Map<String, String> MACE_MAP = Cosmetics.originalNames(MACE_WEAPONS);
    private static final Map<String, String> SWORD_MAP = Cosmetics.originalNames(SWORD_WEAPONS);
    private static Cosmetics instance;

    private final BooleanSetting macesEnabled = new BooleanSetting("Enable Maces", "Replace mace models").setValue(false);
    private final BooleanSetting macesSelfOnly = new BooleanSetting("Own Mace Only", "Replace the model only for yourself").setValue(true);
    private final SelectSetting maceModel = new SelectSetting("Mace Model", "Choose a mace model").value(MACE_DISPLAY).selected(MACE_DISPLAY[0]);
    private final GroupSetting macesGroup = new GroupSetting("Maces", "Custom mace skins", false).settings(this.macesEnabled, this.macesSelfOnly, this.maceModel);

    private final BooleanSetting swordsEnabled = new BooleanSetting("Enable Swords", "Replace sword models").setValue(false);
    private final BooleanSetting swordsSelfOnly = new BooleanSetting("Own Sword Only", "Replace the model only for yourself").setValue(true);
    private final SelectSetting swordModel = new SelectSetting("Sword Model", "Choose a sword model").value(SWORD_DISPLAY).selected(SWORD_DISPLAY[0]);
    private final GroupSetting swordsGroup = new GroupSetting("Swords", "Custom sword skins", false).settings(this.swordsEnabled, this.swordsSelfOnly, this.swordModel);

    private final SelectSetting playerModel = new SelectSetting("Player Model", "Choose a player model").value(PLAYER_MODELS).selected(PLAYER_MODELS[0]);
    private final BooleanSetting playerModelFriends = new BooleanSetting("Models On Friends", "Show the model on friends too").setValue(true);
    private final GroupSetting playerModelsGroup = new GroupSetting("Player Models", "Replace the player model").setValue(false).settings(this.playerModel, this.playerModelFriends);

    private final SelectSetting wingModel = new SelectSetting("Wings Model", "Choose wings").value(WINGS).selected(WINGS[0]);
    private final GroupSetting wingsGroup = new GroupSetting("Wings", "Wings on your back").setValue(false).settings(this.wingModel);

    private final SelectSetting hatModel = new SelectSetting("Hat Model", "Choose a hat").value(HATS).selected(HATS[0]);
    private final GroupSetting hatsGroup = new GroupSetting("Hats", "Hats and head accessories").setValue(false).settings(this.hatModel);

    private final SelectSetting backpackModel = new SelectSetting("Backpack Model", "Choose a backpack").value(BACKPACKS).selected(BACKPACKS[0]);
    private final GroupSetting backpacksGroup = new GroupSetting("Backpacks", "Backpack on your back").setValue(false).settings(this.backpackModel);

    private final SelectSetting haloModel = new SelectSetting("Halo Model", "Choose a halo").value(HALOS).selected(HALOS[0]);
    private final GroupSetting halosGroup = new GroupSetting("Halos", "Halo above your head").setValue(false).settings(this.haloModel);

    private final SelectSetting capeModel = new SelectSetting("Cape Model", "Choose a cape").value(CAPES).selected(CAPES[0]);
    private final BooleanSetting capeFriends = new BooleanSetting("Capes On Friends", "Show the cape on friends too").setValue(true);
    private final GroupSetting capesGroup = new GroupSetting("Capes", "Cape and elytra").setValue(false).settings(this.capeModel, this.capeFriends);

    private final BooleanSetting accessoriesFriends = new BooleanSetting("Accessories On Friends", "Wings, hats, backpacks and halos on friends too").setValue(false);
    private final ValueSetting renderDistance = new ValueSetting("Cosmetics Distance", "Skip cosmetics farther than this many blocks (saves FPS)").setValue(48.0f).range(8, 128);
    private final GroupSetting generalGroup = new GroupSetting("General", "General cosmetics settings", false).settings(this.accessoriesFriends, this.renderDistance);

    /** Address of the cosmetics sync server (see sync-server/README.md). Empty = sync off. */
    public static final String DEFAULT_SYNC_SERVER = "";
    private final BooleanSetting syncShare = new BooleanSetting("Share My Cosmetics", "Other Dettex users see your cosmetics").setValue(true);
    private final BooleanSetting syncShow = new BooleanSetting("Show Dettex Users", "See the cosmetics of other Dettex users").setValue(true);
    private final BooleanSetting syncBadge = new BooleanSetting("Dettex Badge", "Dettex logo next to the names of Dettex users").setValue(true);
    private final TextSetting syncServer = new TextSetting("Sync Server", "Address of the cosmetics sync server").setText(DEFAULT_SYNC_SERVER);
    private final GroupSetting syncGroup = new GroupSetting("Sync", "Cosmetics shared between Dettex users", false).settings(this.syncShare, this.syncShow, this.syncBadge, this.syncServer);

    private final Map<String, CosmeticModel> cachedModels = new ConcurrentHashMap<String, CosmeticModel>();
    private final Map<String, class_12079.class_10726> capeAssets = new ConcurrentHashMap<String, class_12079.class_10726>();

    public Cosmetics() {
        super("Cosmetics", "Cosmetics", ModuleCategory.RENDER);
        instance = this;
        this.setup(this.macesGroup, this.swordsGroup, this.playerModelsGroup, this.wingsGroup, this.hatsGroup, this.backpacksGroup, this.halosGroup, this.capesGroup, this.generalGroup, this.syncGroup);
    }

    public static Cosmetics getInstance() {
        return Instance.get(Cosmetics.class);
    }

    // ------------------------------------------------------------------ sync between Dettex users

    @EventHandler
    public void onTick(TickEvent event) {
        CosmeticSync.tick(this.syncServer.getText(), this.syncShare.isValue(), this.ownProfile());
    }

    @Override
    public void deactivate() {
        CosmeticSync.reset();
        super.deactivate();
    }

    /** What this client shares: only the slots that are switched on. */
    public CosmeticSync.Profile ownProfile() {
        return new CosmeticSync.Profile(
                this.playerModelsGroup.isValue() ? this.getActiveCustomModel() : null,
                this.wingsGroup.isValue() ? this.wingModel.getSelected() : null,
                this.hatsGroup.isValue() ? this.hatModel.getSelected() : null,
                this.backpacksGroup.isValue() ? this.backpackModel.getSelected() : null,
                this.halosGroup.isValue() ? this.haloModel.getSelected() : null,
                this.capesGroup.isValue() ? this.capeModel.getSelected() : null,
                this.macesEnabled.isValue() ? this.maceModel.getSelected() : null,
                this.swordsEnabled.isValue() ? this.swordModel.getSelected() : null);
    }

    /** Synced profile of another Dettex user, or null. */
    private CosmeticSync.Profile remote(UUID uuid) {
        return this.syncShow.isValue() ? CosmeticSync.get(uuid) : null;
    }

    /** Whether the Dettex badge goes next to this player's name. */
    public boolean showBadge(UUID uuid) {
        return this.isState() && this.syncBadge.isValue() && CosmeticSync.isDettexUser(uuid);
    }

    private static boolean contains(String[] values, String value) {
        if (value == null) {
            return false;
        }
        for (String v : values) {
            if (v.equals(value)) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ weapons

    public String getActualSword() {
        return Cosmetics.original(SWORD_MAP, this.swordModel.getSelected());
    }

    public String getActualMace() {
        return Cosmetics.original(MACE_MAP, this.maceModel.getSelected());
    }

    /** Weapon skin for a mace/sword held by this owner, or null to keep the vanilla model. */
    public String weaponFor(boolean mace, UUID owner, boolean self) {
        if (!this.isState()) {
            return null;
        }
        BooleanSetting enabled = mace ? this.macesEnabled : this.swordsEnabled;
        BooleanSetting selfOnly = mace ? this.macesSelfOnly : this.swordsSelfOnly;
        if (self) {
            return enabled.isValue() ? (mace ? this.getActualMace() : this.getActualSword()) : null;
        }
        CosmeticSync.Profile r = this.remote(owner);
        String synced = r == null ? null : (mace ? r.mace() : r.sword());
        if (synced != null) {
            Map<String, String> map = mace ? MACE_MAP : SWORD_MAP;
            String original = map.get(synced);
            if (original != null) {
                return original;
            }
        }
        if (enabled.isValue() && !selfOnly.isValue()) {
            return mace ? this.getActualMace() : this.getActualSword();
        }
        return null;
    }

    // ------------------------------------------------------------------ player models

    public boolean isCustomModelsActive() {
        return this.isState() && this.playerModelsGroup.isValue();
    }

    public String getActiveCustomModel() {
        return this.validSelected(this.playerModel, PLAYER_MODELS);
    }

    /** Player model to draw for this player (self, synced Dettex user, or friend), or null for the vanilla model. */
    public String customModelFor(class_11890 player) {
        if (!this.isState() || player == null || player.method_7325()) {
            return null;
        }
        class_310 client = class_310.method_1551();
        if (client != null && client.field_1724 != null && player == client.field_1724) {
            return this.playerModelsGroup.isValue() ? this.getActiveCustomModel() : null;
        }
        CosmeticSync.Profile r = this.remote(player.method_5667());
        if (r != null) {
            return Cosmetics.contains(PLAYER_MODELS, r.model()) ? r.model() : null;
        }
        return this.shouldApplyCustomModel(player) ? this.getActiveCustomModel() : null;
    }

    public boolean shouldApplyCustomModel(class_11890 player) {
        if (!this.isCustomModelsActive() || player == null || player.method_7325()) {
            return false;
        }
        class_310 client = class_310.method_1551();
        if (client != null && client.field_1724 != null && player == client.field_1724) {
            return true;
        }
        return this.playerModelFriends.isValue() && player.method_5477() != null && FriendUtils.isFriend(player.method_5477().getString());
    }

    // ------------------------------------------------------------------ accessories (wings, hats, backpacks, halos)

    /** Whether accessories should be drawn for a player (self, or a friend when enabled) at this squared distance. */
    public boolean shouldRenderAccessories(boolean self, String playerName, double squaredDistance) {
        if (!this.isState()) {
            return false;
        }
        double max = this.renderDistance.getValue();
        if (!self && squaredDistance > max * max) {
            return false;
        }
        return self || this.accessoriesFriends.isValue() && playerName != null && FriendUtils.isFriend(playerName);
    }

    /** Accessories for one player: own selection for self/friends, the synced selection for Dettex users. */
    public List<CosmeticModel> accessoriesFor(boolean self, UUID uuid, String playerName, double squaredDistance) {
        if (!this.isState()) {
            return List.of();
        }
        double max = this.renderDistance.getValue();
        if (!self && squaredDistance > max * max) {
            return List.of();
        }
        if (self) {
            return this.getActiveAccessories();
        }
        CosmeticSync.Profile r = this.remote(uuid);
        if (r != null) {
            ArrayList<CosmeticModel> list = new ArrayList<CosmeticModel>(4);
            this.addExact(list, CosmeticCatalog.WINGS, r.wings());
            this.addExact(list, CosmeticCatalog.BACKPACKS, r.backpack());
            this.addExact(list, CosmeticCatalog.HATS, r.hat());
            this.addExact(list, CosmeticCatalog.HALOS, r.halo());
            return list;
        }
        return this.shouldRenderAccessories(false, playerName, squaredDistance) ? this.getActiveAccessories() : List.of();
    }

    /** True when this model is one of the catalog hats. */
    public static boolean isHatModel(CosmeticModel model) {
        return model != null && model.getName() != null && CosmeticCatalog.find(CosmeticCatalog.HATS, model.getName()) != null;
    }

    /** A hat by its catalog name (used for holiday hats), or null. */
    public CosmeticModel hatModel(String name) {
        return name == null || CosmeticCatalog.find(CosmeticCatalog.HATS, name) == null ? null : this.load(CosmeticCatalog.HATS, name);
    }

    private void addExact(List<CosmeticModel> out, String category, String name) {
        if (name == null || CosmeticCatalog.find(category, name) == null) {
            return;   // unknown name (other client version): skip instead of falling back to the first entry
        }
        CosmeticModel model = this.load(category, name);
        if (model != null) {
            out.add(model);
        }
    }

    public List<CosmeticModel> getActiveAccessories() {
        ArrayList<CosmeticModel> list = new ArrayList<CosmeticModel>(4);
        this.addActive(list, this.wingsGroup, this.wingModel, CosmeticCatalog.WINGS);
        this.addActive(list, this.backpacksGroup, this.backpackModel, CosmeticCatalog.BACKPACKS);
        this.addActive(list, this.hatsGroup, this.hatModel, CosmeticCatalog.HATS);
        this.addActive(list, this.halosGroup, this.haloModel, CosmeticCatalog.HALOS);
        return list;
    }

    public CosmeticModel getActiveWingModel() {
        return this.wingsGroup.isValue() ? this.load(CosmeticCatalog.WINGS, this.wingModel.getSelected()) : null;
    }

    public CosmeticModel getActiveBackpackModel() {
        return this.backpacksGroup.isValue() ? this.load(CosmeticCatalog.BACKPACKS, this.backpackModel.getSelected()) : null;
    }

    private void addActive(List<CosmeticModel> out, GroupSetting group, SelectSetting select, String category) {
        if (!group.isValue()) {
            return;
        }
        CosmeticModel model = this.load(category, select.getSelected());
        if (model != null) {
            out.add(model);
        }
    }

    private CosmeticModel load(String category, String name) {
        CosmeticCatalog.Entry entry = CosmeticCatalog.find(category, name);
        if (entry == null) {
            List<CosmeticCatalog.Entry> all = CosmeticCatalog.entries(category);
            if (all.isEmpty()) {
                return null;
            }
            entry = all.get(0);
        }
        String path = entry.resourcePath();
        if (path == null) {
            return null;
        }
        CosmeticModel cached = this.cachedModels.get(path);
        if (cached != null) {
            return cached;
        }
        CosmeticModel model = CosmeticLoader.getInstance().loadFromResource(path);
        if (model != null) {
            this.cachedModels.put(path, model);
        }
        return model;
    }

    // ------------------------------------------------------------------ capes

    /** Cape for this player, including the synced cape of a Dettex user; null keeps the vanilla cape. */
    public class_12079.class_10726 getCapeFor(boolean self, UUID uuid, String playerName) {
        if (!self && this.isState()) {
            CosmeticSync.Profile r = this.remote(uuid);
            if (r != null) {
                CosmeticCatalog.Entry entry = r.cape() == null ? null : CosmeticCatalog.find(CosmeticCatalog.CAPES, r.cape());
                if (entry == null || entry.texture() == null) {
                    return null;
                }
                return this.capeAssets.computeIfAbsent(entry.texture(), texture -> new class_12079.class_10726(class_2960.method_60654((String)texture)));
            }
        }
        return this.getCapeFor(self, playerName);
    }

    /** Selected cape for this player, or null when the vanilla cape should stay. */
    public class_12079.class_10726 getCapeFor(boolean self, String playerName) {
        if (!this.isState() || !this.capesGroup.isValue()) {
            return null;
        }
        if (!self && !(this.capeFriends.isValue() && playerName != null && FriendUtils.isFriend(playerName))) {
            return null;
        }
        CosmeticCatalog.Entry entry = CosmeticCatalog.find(CosmeticCatalog.CAPES, this.capeModel.getSelected());
        if (entry == null || entry.texture() == null) {
            return null;
        }
        return this.capeAssets.computeIfAbsent(entry.texture(), texture -> new class_12079.class_10726(class_2960.method_60654((String)texture)));
    }

    /** True for capes shipped with the client (drawn by the wavy cape renderer). Accepts an asset id or a texture path. */
    public static boolean isClientCape(class_2960 id) {
        return id != null && "minecraft".equals(id.method_12836()) && id.method_12832().contains("capes/recoin");
    }

    /** Full texture identifier (textures/....png) for a cape asset id or texture path. */
    public static class_2960 capeTexture(class_2960 id) {
        String path = id.method_12832();
        if (path.startsWith("textures/")) {
            return id;
        }
        return class_2960.method_60655((String)id.method_12836(), (String)("textures/" + path + ".png"));
    }

    // ------------------------------------------------------------------ helpers

    private String validSelected(SelectSetting setting, String[] values) {
        String selected = setting.getSelected();
        for (String value : values) {
            if (value.equals(selected)) {
                return value;
            }
        }
        return values[0];
    }

    private static String original(Map<String, String> map, String selected) {
        if (selected == null) {
            return "";
        }
        String original = map.get(selected);
        if (original == null) {
            original = map.get(selected.toLowerCase(Locale.ROOT));
        }
        return original != null ? original : selected;
    }

    private static Map<String, String> originalNames(String[] names) {
        HashMap<String, String> map = new HashMap<String, String>();
        for (String name : names) {
            String display = name.replace("'", "");
            map.put(display, name);
            map.put(name, name);
            map.put(display.toLowerCase(Locale.ROOT), name);
        }
        return map;
    }

    private static String[] cleanDisplayNames(String[] original) {
        String[] res = new String[original.length];
        for (int i = 0; i < original.length; ++i) {
            res[i] = original[i].replace("'", "");
        }
        return res;
    }

    // ------------------------------------------------------------------ getters used by mixins and the menu

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

    public SelectSetting getCustomModel() {
        return this.playerModel;
    }

    public GroupSetting getCustomModelsGroup() {
        return this.playerModelsGroup;
    }

    /** Sync server address as typed in the settings (may be empty). */
    public String getSyncServer() {
        return this.syncServer.getText();
    }

    public GroupSetting getSyncGroup() {
        return this.syncGroup;
    }

    public GroupSetting getWingsGroup() {
        return this.wingsGroup;
    }

    public GroupSetting getBackpacksGroup() {
        return this.backpacksGroup;
    }

    public GroupSetting getHatsGroup() {
        return this.hatsGroup;
    }

    public GroupSetting getHalosGroup() {
        return this.halosGroup;
    }

    public GroupSetting getCapesGroup() {
        return this.capesGroup;
    }

    public Map<String, CosmeticModel> getCachedModels() {
        return this.cachedModels;
    }
}
