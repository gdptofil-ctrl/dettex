/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_1297
 *  net.minecraft.class_1304
 *  net.minecraft.class_1309
 *  net.minecraft.class_1429
 *  net.minecraft.class_1541
 *  net.minecraft.class_1542
 *  net.minecraft.class_1588
 *  net.minecraft.class_1646
 *  net.minecraft.class_1657
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1814
 *  net.minecraft.class_243
 *  net.minecraft.class_2487
 *  net.minecraft.class_2561
 *  net.minecraft.class_268
 *  net.minecraft.class_270
 *  net.minecraft.class_332
 *  net.minecraft.class_3489
 *  net.minecraft.class_3532
 *  net.minecraft.class_4587
 *  net.minecraft.class_5250
 *  net.minecraft.class_638
 *  net.minecraft.class_7923
 *  net.minecraft.class_9279
 *  net.minecraft.class_9334
 *  org.joml.Vector4d
 */
package ru.recoin.implement.features.modules.render;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_124;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1429;
import net.minecraft.class_1541;
import net.minecraft.class_1542;
import net.minecraft.class_1588;
import net.minecraft.class_1646;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1814;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2561;
import net.minecraft.class_268;
import net.minecraft.class_270;
import net.minecraft.class_332;
import net.minecraft.class_3489;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_5250;
import net.minecraft.class_638;
import net.minecraft.class_7923;
import net.minecraft.class_9279;
import net.minecraft.class_9334;
import org.joml.Vector4d;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.item.FunTimeDurabilityUtil;
import ru.recoin.common.util.math.ProjectionUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.common.util.world.ServerUtil;
import ru.recoin.implement.events.render.DrawEvent;
import ru.recoin.implement.features.modules.combat.AntiBot;

public class EntityESP
extends Module {
    private static final String HEALTH_ICON = "\ue0bd";
    private static final int HEALTH_COLOR = -43691;
    private static final float HEALTH_GAP = 2.0f;
    private final transient List<class_1799> armorItems = new ArrayList<class_1799>();
    private final transient List<class_1657> renderedPlayers = new ArrayList<class_1657>();
    private final transient List<class_1297> renderedEntities = new ArrayList<class_1297>();
    static long NAMETAG_REFRESH_MS = 150L;
    private final transient Map<Integer, NametagEntry> nametagCache = new HashMap<Integer, NametagEntry>();
    private final transient Map<class_1799, ItemTagEntry> itemTagCache = new IdentityHashMap<class_1799, ItemTagEntry>();
    private transient class_638 cachedWorld;
    public final MultiSelectSetting entityType = new MultiSelectSetting("Entity Type", "Entity that will be displayed").value("Self", "Player", "Naked Player", "Mob", "Animal", "Villager", "Item", "TNT").selected("Player", "Item", "TNT");
    private final MultiSelectSetting playerSetting = new MultiSelectSetting("Player Settings", "Settings for players").value("Box", "Armor", "Prefix", "Hand Items").selected("Box", "Armor", "Prefix", "Hand Items").visible(() -> this.entityType.isSelected("Self") || this.entityType.isSelected("Player") || this.entityType.isSelected("Naked Player"));
    private final SelectSetting tagStyle = new SelectSetting("\u0422\u0435\u0433\u0438", "\u0421\u0442\u0438\u043b\u044c \u0444\u043e\u043d\u0430 \u043d\u0435\u0439\u043c\u0442\u0435\u0433\u043e\u0432").value("\u0417\u0430\u043a\u0440\u0443\u0433\u043b\u0435\u043d\u043d\u044b\u0439", "\u041a\u0432\u0430\u0434\u0440\u0430\u0442\u043d\u044b\u0439").selected("\u0417\u0430\u043a\u0440\u0443\u0433\u043b\u0435\u043d\u043d\u044b\u0439");
    private final ValueSetting tagOpacity = new ValueSetting("\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0442\u0435\u0433\u043e\u0432", "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0444\u043e\u043d\u0430 \u043d\u0435\u0439\u043c\u0442\u0435\u0433\u043e\u0432").setValue(0.28f).range(0.0f, 1.0f);
    private final SelectSetting durabilityMode = new SelectSetting("Durability", "FunTime: show synced armor durability values").value("\u041e\u0431\u044b\u0447\u043d\u044b\u0439", "FunTime").selected("\u041e\u0431\u044b\u0447\u043d\u044b\u0439").visible(() -> this.playerSetting.isSelected("Armor"));

    public static EntityESP getInstance() {
        return Instance.get(EntityESP.class);
    }

    public EntityESP() {
        super("EntityESP", "Entity ESP", ModuleCategory.RENDER);
        this.setup(this.entityType, this.playerSetting, this.durabilityMode, this.tagStyle, this.tagOpacity);
    }

    @Override
    public void deactivate() {
        this.armorItems.clear();
        this.renderedPlayers.clear();
        this.renderedEntities.clear();
        this.nametagCache.clear();
        this.itemTagCache.clear();
        this.cachedWorld = null;
    }

    @EventHandler
    public void onDraw(DrawEvent e) {
        if (EntityESP.mc.field_1687 == null || EntityESP.mc.field_1724 == null) {
            this.deactivate();
            return;
        }
        if (this.cachedWorld != EntityESP.mc.field_1687) {
            this.nametagCache.clear();
            this.itemTagCache.clear();
            this.cachedWorld = EntityESP.mc.field_1687;
        }
        class_332 context = e.getDrawContext();
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        FontRenderer font = Fonts.getSize(13, Fonts.Type.INTER_BOLD);
        FontRenderer iconFont = this.healthIconFont();
        float healthIconWidth = iconFont.getStringWidth(HEALTH_ICON) + 2.6f;
        boolean showSelf = this.entityType.isSelected("Self");
        boolean showPlayers = this.entityType.isSelected("Player");
        boolean showNakedPlayers = this.entityType.isSelected("Naked Player");
        boolean showMobs = this.entityType.isSelected("Mob");
        boolean showAnimals = this.entityType.isSelected("Animal");
        boolean showVillagers = this.entityType.isSelected("Villager");
        boolean showItems = this.entityType.isSelected("Item");
        boolean showTnt = this.entityType.isSelected("TNT");
        class_243 cameraPos = EntityESP.mc.method_1561().field_4686.method_71156();
        if (showSelf || showPlayers || showNakedPlayers) {
            long now = System.currentTimeMillis();
            int clientColor = ColorUtil.getClientColor();
            int friendColor = ColorUtil.getFriendColor();
            boolean flatBox = this.playerSetting.isSelected("Box") || this.playerSetting.isSelected("Flat Box");
            boolean armor = this.playerSetting.isSelected("Armor");
            boolean handItems = this.playerSetting.isSelected("Hand Items");
            this.renderedPlayers.clear();
            for (class_1657 player : EntityESP.mc.field_1687.method_18456()) {
                if (player == EntityESP.mc.field_1724 && (!showSelf || EntityESP.mc.field_1690.method_31044().method_31034())) continue;
                boolean naked = this.isNaked(player);
                if (player != EntityESP.mc.field_1724 && (!showPlayers || naked) && (!showNakedPlayers || !naked) || cameraPos.method_1028(player.method_23317(), player.method_23318(), player.method_23321()) < 1.0) continue;
                this.renderedPlayers.add(player);
            }
            this.renderedPlayers.sort(Comparator.comparingDouble((class_1657 p) -> cameraPos.method_1028(p.method_23317(), p.method_23318(), p.method_23321())).reversed());
            for (class_1657 player : this.renderedPlayers) {
                Vector4d vec4d = ProjectionUtil.getVector4D((class_1297)player);
                if (ProjectionUtil.cantSee(vec4d)) continue;
                boolean friend = FriendUtils.isFriend((class_1297)player);
                NametagEntry tag = this.getCachedTag(player, friend, font, healthIconWidth, now);
                if (flatBox) {
                    this.drawFlatBox(matrix, friend ? friendColor : clientColor, vec4d);
                }
                if (handItems) {
                    this.drawHands(matrix, player, font, vec4d);
                }
                double tagCenter = ProjectionUtil.centerX(vec4d);
                float tagBoxHeight = tag.boxHeight;
                if (armor) {
                    this.collectArmorItems(player);
                    if (!this.armorItems.isEmpty()) {
                        int count = this.armorItems.size();
                        float blockSize = tagBoxHeight;
                        float gap = 1.5f;
                        float totalWidth = (float)count * blockSize + (float)(count - 1) * gap;
                        float startX = (float)tagCenter - totalWidth / 2.0f;
                        float armorTop = (float)vec4d.y - 5.0f - tagBoxHeight - 1.0f - blockSize;
                        for (int i = 0; i < count; ++i) {
                            this.drawTagBackground(matrix, startX + (float)i * (blockSize + gap), armorTop, blockSize, blockSize);
                        }
                        this.drawArmor(context, startX, armorTop, blockSize, gap);
                    }
                }
                this.drawHealthTag(matrix, tag.text, tag.healthText, tag.nameWidth, tag.width, tagCenter, vec4d.y - 5.0, font, iconFont, healthIconWidth);
            }
        }
        if (!(showItems || showTnt || showMobs || showAnimals || showVillagers)) {
            return;
        }
        this.renderedEntities.clear();
        for (class_1297 entity2 : EntityESP.mc.field_1687.method_18112()) {
            class_1309 living;
            boolean selected = entity2 instanceof class_1542 && showItems || entity2 instanceof class_1541 && showTnt || entity2 instanceof class_1309 && !((living = (class_1309)entity2) instanceof class_1657) && this.shouldShowLiving(living, showMobs, showAnimals, showVillagers);
            if (!selected || !(cameraPos.method_1028(entity2.method_23317(), entity2.method_23318(), entity2.method_23321()) >= 1.0)) continue;
            this.renderedEntities.add(entity2);
        }
        this.renderedEntities.sort(Comparator.comparingDouble((class_1297 entity) -> cameraPos.method_1028(entity.method_23317(), entity.method_23318(), entity.method_23321())).reversed());
        for (class_1297 entity2 : this.renderedEntities) {
            Vector4d vec4d;
            class_1309 living;
            if (entity2 instanceof class_1542) {
                class_1542 item = (class_1542)entity2;
                if (showItems) {
                    Vector4d vec4d2 = ProjectionUtil.getVector4D((class_1297)item);
                    if (ProjectionUtil.cantSee(vec4d2)) continue;
                    class_1799 stack = item.method_6983();
                    ItemTagEntry tag = this.getItemTag(stack, font);
                    if (tag.empty) continue;
                    this.drawItemTag(matrix, tag, ProjectionUtil.centerX(vec4d2), vec4d2.y, font, tag.rarityColor);
                    continue;
                }
            }
            if (entity2 instanceof class_1541) {
                class_1541 tnt = (class_1541)entity2;
                if (showTnt) {
                    Vector4d vec4d3 = ProjectionUtil.getVector4D((class_1297)tnt);
                    if (ProjectionUtil.cantSee(vec4d3)) continue;
                    this.drawText(matrix, tnt.method_55423(), ProjectionUtil.centerX(vec4d3), vec4d3.y, font);
                    continue;
                }
            }
            if (!(entity2 instanceof class_1309) || (living = (class_1309)entity2) instanceof class_1657 || !this.shouldShowLiving(living, showMobs, showAnimals, showVillagers) || ProjectionUtil.cantSee(vec4d = ProjectionUtil.getVector4D((class_1297)living))) continue;
            this.drawFlatBox(matrix, this.getLivingColor(living), vec4d);
            String nameText = this.formatNametagText(font, (class_2561)class_2561.method_43470((String)this.sanitizeNametagText(living.method_5476().getString())).method_27692(class_124.field_1068));
            String healthText = PlayerIntersectionUtil.getHealthString(living);
            float nameWidth = font.getStringWidth(nameText);
            float width = this.healthTagWidth(font, nameWidth, healthText, healthIconWidth);
            this.drawHealthTag(matrix, nameText, healthText, nameWidth, width, ProjectionUtil.centerX(vec4d), vec4d.y - 5.0, font, iconFont, healthIconWidth);
        }
    }

    private boolean shouldShowLiving(class_1309 entity, boolean mobs, boolean animals, boolean villagers) {
        if (entity instanceof class_1646) {
            return villagers;
        }
        if (entity instanceof class_1429) {
            return animals;
        }
        return entity instanceof class_1588 && mobs;
    }

    private int getLivingColor(class_1309 entity) {
        if (entity instanceof class_1588) {
            return -43691;
        }
        if (entity instanceof class_1646) {
            return -22016;
        }
        if (entity instanceof class_1429) {
            return -11141291;
        }
        return ColorUtil.getClientColor();
    }

    public boolean rendersNametag(class_1309 entity) {
        if (!this.isState()) {
            return false;
        }
        if (entity instanceof class_1657) {
            class_1657 player = (class_1657)entity;
            if (player == EntityESP.mc.field_1724) {
                return this.entityType.isSelected("Self") && !EntityESP.mc.field_1690.method_31044().method_31034();
            }
            return this.isNaked(player) ? this.entityType.isSelected("Naked Player") : this.entityType.isSelected("Player");
        }
        return this.shouldShowLiving(entity, this.entityType.isSelected("Mob"), this.entityType.isSelected("Animal"), this.entityType.isSelected("Villager"));
    }

    private boolean isNaked(class_1657 player) {
        return player.method_6118(class_1304.field_6169).method_7960() && player.method_6118(class_1304.field_6174).method_7960() && player.method_6118(class_1304.field_6172).method_7960() && player.method_6118(class_1304.field_6166).method_7960();
    }

    private void drawFlatBox(class_4587 matrix, int client, Vector4d vec) {
        int black = ColorUtil.HALF_BLACK;
        float posX = (float)vec.x;
        float posY = (float)vec.y;
        float endPosX = (float)vec.z;
        float endPosY = (float)vec.w;
        float size = (endPosX - posX) / 3.0f;
        this.drawEspQuad(matrix, posX - 1.0f, posY - 1.0f, size + 1.0f, 1.5f, black);
        this.drawEspQuad(matrix, posX - 1.0f, posY + 0.5f, 1.5f, size + 0.5f, black);
        this.drawEspQuad(matrix, posX - 1.0f, endPosY - size - 1.0f, 1.5f, size, black);
        this.drawEspQuad(matrix, posX - 1.0f, endPosY - 1.0f, size + 1.0f, 1.5f, black);
        this.drawEspQuad(matrix, endPosX - size + 0.5f, posY - 1.0f, size + 1.0f, 1.5f, black);
        this.drawEspQuad(matrix, endPosX, posY + 0.5f, 1.5f, size + 0.5f, black);
        this.drawEspQuad(matrix, endPosX, endPosY - size - 1.0f, 1.5f, size, black);
        this.drawEspQuad(matrix, endPosX - size + 0.5f, endPosY - 1.0f, size + 1.0f, 1.5f, black);
        this.drawEspQuad(matrix, posX - 0.5f, posY - 0.5f, size, 0.5f, client);
        this.drawEspQuad(matrix, posX - 0.5f, posY, 0.5f, size + 0.5f, client);
        this.drawEspQuad(matrix, posX - 0.5f, endPosY - size - 0.5f, 0.5f, size, client);
        this.drawEspQuad(matrix, posX - 0.5f, endPosY - 0.5f, size, 0.5f, client);
        this.drawEspQuad(matrix, endPosX - size + 1.0f, posY - 0.5f, size, 0.5f, client);
        this.drawEspQuad(matrix, endPosX + 0.5f, posY, 0.5f, size + 0.5f, client);
        this.drawEspQuad(matrix, endPosX + 0.5f, endPosY - size - 0.5f, 0.5f, size, client);
        this.drawEspQuad(matrix, endPosX - size + 1.0f, endPosY - 0.5f, size, 0.5f, client);
    }

    private void drawEspQuad(class_4587 matrix, float x, float y, float width, float height, int color) {
        rectangle.render(ShapeProperties.create(matrix, x, y, width, height).color(color).build());
    }

    private void collectArmorItems(class_1657 player) {
        class_1799 feet;
        class_1799 legs;
        class_1799 chest;
        this.armorItems.clear();
        class_1799 head = player.method_6118(class_1304.field_6169);
        if (!head.method_7960()) {
            this.armorItems.add(head);
        }
        if (!(chest = player.method_6118(class_1304.field_6174)).method_7960()) {
            this.armorItems.add(chest);
        }
        if (!(legs = player.method_6118(class_1304.field_6172)).method_7960()) {
            this.armorItems.add(legs);
        }
        if (!(feet = player.method_6118(class_1304.field_6166)).method_7960()) {
            this.armorItems.add(feet);
        }
        class_1799 mainHand = player.method_6047();
        class_1799 offHand = player.method_6079();
        if (!mainHand.method_7960() && !offHand.method_7960()) {
            if (this.heldItemPriority(mainHand) <= this.heldItemPriority(offHand)) {
                this.armorItems.add(mainHand);
                this.armorItems.add(offHand);
            } else {
                this.armorItems.add(offHand);
                this.armorItems.add(mainHand);
            }
        } else if (!mainHand.method_7960()) {
            this.armorItems.add(mainHand);
        } else if (!offHand.method_7960()) {
            this.armorItems.add(offHand);
        }
    }

    private int heldItemPriority(class_1799 stack) {
        if (stack.method_31574(class_1802.field_8288)) {
            return 0;
        }
        if (stack.method_31573(class_3489.field_42611)) {
            return 1;
        }
        if (stack.method_58694(class_9334.field_50075) != null) {
            return 2;
        }
        return 3;
    }

    private void drawArmor(class_332 context, float startX, float posY, float blockSize, float gap) {
        float itemScale = class_3532.method_15363((float)((blockSize - 3.0f) / 16.0f), (float)0.42f, (float)0.55f);
        float itemSize = 16.0f * itemScale;
        float itemY = posY + (blockSize - itemSize) / 2.0f - 1.0f;
        if (!this.armorItems.isEmpty()) {
            for (int i = 0; i < this.armorItems.size(); ++i) {
                class_1799 stack = this.armorItems.get(i);
                class_1799 renderedStack = this.durabilityMode.isSelected("FunTime") ? this.getDurabilityStack(stack) : stack;
                float boxX = startX + (float)i * (blockSize + gap);
                float itemX = boxX + (blockSize - itemSize) / 2.0f - 1.0f;
                Render2DUtil.defaultDrawStack(context, renderedStack, itemX, itemY, false, true, itemScale);
            }
        }
    }

    private void drawHands(class_4587 matrix, class_1657 player, FontRenderer font, Vector4d vec) {
        class_1799 offHand;
        double posY = vec.w;
        class_1799 mainHand = player.method_6047();
        if (!mainHand.method_7960()) {
            ItemTagEntry tag = this.getItemTag(mainHand, font);
            if (!tag.empty) {
                this.drawItemTag(matrix, tag, ProjectionUtil.centerX(vec), posY += (double)(tag.handHeight / 2.0f + 3.0f), font);
            }
        }
        if ((offHand = player.method_6079()).method_7960() || offHand == mainHand || this.isDonationItem(offHand)) {
            return;
        }
        ItemTagEntry tag = this.getItemTag(offHand, font);
        if (tag.empty) {
            return;
        }
        this.drawItemTag(matrix, tag, ProjectionUtil.centerX(vec), posY += (double)(tag.handHeight / 2.0f + 6.5f), font);
    }

    private ItemTagEntry getItemEntry(class_1799 stack) {
        ItemTagEntry entry = this.itemTagCache.get(stack);
        if (entry == null || entry.snapshot.method_7947() != stack.method_7947() || !class_1799.method_31577((class_1799)entry.snapshot, (class_1799)stack)) {
            entry = new ItemTagEntry();
            entry.snapshot = stack.method_7972();
            if (this.itemTagCache.size() >= 256) {
                this.itemTagCache.clear();
            }
            this.itemTagCache.put(stack, entry);
        }
        return entry;
    }

    private class_1799 getDurabilityStack(class_1799 stack) {
        ItemTagEntry entry = this.getItemEntry(stack);
        if (entry.durabilityStack == null) {
            entry.durabilityStack = FunTimeDurabilityUtil.forVanillaBar(entry.snapshot);
        }
        return entry.durabilityStack;
    }

    private ItemTagEntry getItemTag(class_1799 stack, FontRenderer font) {
        ItemTagEntry entry = this.getItemEntry(stack);
        String language = mc.method_1526().method_4669();
        if (entry.font != font || !language.equals(entry.language)) {
            class_5250 text;
            class_2561 name = stack.method_7964();
            entry.empty = this.sanitizeNametagText(name.getString()).isEmpty();
            class_5250 class_52502 = text = EntityESP.isNetheriteItem(stack) ? class_2561.method_43470((String)name.getString()).method_27692(class_124.field_1068) : name.method_27661();
            if (stack.method_7947() > 1) {
                text.method_27693(String.valueOf(class_124.field_1070) + " [" + String.valueOf(class_124.field_1061) + stack.method_7947() + String.valueOf(class_124.field_1080) + "x" + String.valueOf(class_124.field_1070) + "]");
            }
            entry.formatted = this.formatNametagText(font, (class_2561)text);
            entry.width = font.getStringWidth(entry.formatted);
            entry.boxHeight = font.getStringHeight(entry.formatted) / 2.15f + 6.0f;
            entry.handHeight = font.getStringHeight((class_2561)text);
            entry.rarityColor = EntityESP.rarityColor(stack);
            entry.font = font;
            entry.language = language;
        }
        return entry;
    }

    private static int rarityColor(class_1799 stack) {
        if (EntityESP.isNetheriteItem(stack)) {
            return ColorUtil.getText();
        }
        class_1814 rarity = stack.method_7932();
        if (rarity == class_1814.field_8906) {
            return ColorUtil.getText();
        }
        Integer rgb = rarity.method_58413().method_532();
        return rgb == null ? ColorUtil.getText() : 0xFF000000 | rgb;
    }

    private static boolean isNetheriteItem(class_1799 stack) {
        return class_7923.field_41178.method_10221(stack.method_7909()).method_12832().startsWith("netherite_");
    }

    private void drawItemTag(class_4587 matrix, ItemTagEntry tag, double startX, double startY, FontRenderer font) {
        this.drawItemTag(matrix, tag, startX, startY, font, ColorUtil.getText());
    }

    private void drawItemTag(class_4587 matrix, ItemTagEntry tag, double startX, double startY, FontRenderer font, int color) {
        float posX = (float)(startX - (double)(tag.width / 2.0f));
        float boxTop = (float)startY - tag.boxHeight;
        this.drawTagBackground(matrix, posX - 2.0f, boxTop, tag.width + 4.0f, tag.boxHeight);
        font.drawString(matrix, tag.formatted, posX, boxTop + 3.5f, color);
    }

    private void drawText(class_4587 matrix, class_2561 text, double startX, double startY, FontRenderer font) {
        String formatted = this.formatNametagText(font, text);
        float width = font.getStringWidth(formatted);
        float paddingX = 2.0f;
        float paddingY = 3.0f;
        float textHeight = font.getStringHeight(formatted) / 2.15f;
        float boxHeight = textHeight + paddingY * 2.0f;
        float posX = (float)(startX - (double)(width / 2.0f));
        float boxTop = (float)startY - boxHeight;
        this.drawTagBackground(matrix, posX - paddingX, boxTop, width + paddingX * 2.0f, boxHeight);
        font.drawString(matrix, formatted, posX, boxTop + paddingY + 0.5f, ColorUtil.getText());
    }

    private void drawHealthTag(class_4587 matrix, String nameText, String healthText, float nameWidth, float width, double startX, double startY, FontRenderer font, FontRenderer iconFont, float iconWidth) {
        float paddingX = 2.0f;
        float paddingY = 3.0f;
        float textHeight = font.getStringHeight(nameText) / 2.15f;
        float boxHeight = textHeight + paddingY * 2.0f;
        float posX = (float)(startX - (double)(width / 2.0f));
        float boxTop = (float)startY - boxHeight;
        this.drawTagBackground(matrix, posX - paddingX, boxTop, width + paddingX * 2.0f, boxHeight);
        float healthStartX = posX + nameWidth + 2.0f;
        float textY = boxTop + paddingY + 0.5f;
        font.drawString(matrix, nameText, posX, textY, ColorUtil.getText());
        iconFont.drawString(matrix, HEALTH_ICON, healthStartX + 2.5f, textY + 0.9f, -45747);
        font.drawString(matrix, healthText, healthStartX + iconWidth, textY, -43691);
    }

    private float healthTagWidth(FontRenderer font, float nameWidth, String healthText, float iconWidth) {
        return nameWidth + 2.0f + iconWidth + font.getStringWidth(healthText);
    }

    private FontRenderer healthIconFont() {
        return Fonts.getSize(12, Fonts.Type.COMBINED_ICONS);
    }

    public void drawTagBackground(class_4587 matrix, float x, float y, float width, float height) {
        float round = this.tagStyle.isSelected("\u041a\u0432\u0430\u0434\u0440\u0430\u0442\u043d\u044b\u0439") ? 0.0f : 2.5f;
        rectangle.render(ShapeProperties.create(matrix, x, y, width, height).round(round).color(ColorUtil.getColor(0, this.tagOpacity.getValue())).build());
    }

    private NametagEntry getCachedTag(class_1657 player, boolean friend, FontRenderer font, float healthIconWidth, long now) {
        NametagEntry entry = this.nametagCache.get(player.method_5628());
        if (entry != null && entry.friend == friend && now - entry.time < NAMETAG_REFRESH_MS) {
            return entry;
        }
        if (entry == null) {
            entry = new NametagEntry();
            this.nametagCache.put(player.method_5628(), entry);
        }
        entry.text = this.formatNametagText(font, (class_2561)this.getTextPlayer(player, friend));
        entry.healthText = PlayerIntersectionUtil.getHealth((class_1309)player) < 0.0f ? "Unknown" : PlayerIntersectionUtil.getHealthString((class_1309)player);
        entry.nameWidth = font.getStringWidth(entry.text);
        entry.width = this.healthTagWidth(font, entry.nameWidth, entry.healthText, healthIconWidth);
        entry.boxHeight = font.getStringHeight(entry.text) / 2.15f + 6.0f;
        entry.friend = friend;
        entry.time = now;
        return entry;
    }

    private String formatNametagText(FontRenderer font, class_2561 text) {
        StringBuilder builder = new StringBuilder();
        font.findStyle(builder, text);
        String formatted = this.sanitizeFormattedText(builder.toString().replace("\ufffd", "").replace("\u00c2", ""));
        font.logMissingGlyphs(formatted);
        return formatted;
    }

    private String sanitizeFormattedText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        int offset = 0;
        while (offset < text.length()) {
            int codePoint = text.codePointAt(offset);
            int charCount = Character.charCount(codePoint);
            if ((codePoint == 167 || codePoint == 38) && offset + charCount < text.length()) {
                int next = text.codePointAt(offset + charCount);
                builder.appendCodePoint(codePoint).appendCodePoint(next);
                offset += charCount + Character.charCount(next);
                continue;
            }
            if (!this.isIconOrBrokenGlyph(codePoint)) {
                builder.appendCodePoint(codePoint);
            }
            offset += charCount;
        }
        return this.normalizeWhitespace(builder.toString());
    }

    private class_5250 getTextPlayer(class_1657 player, boolean friend) {
        class_1799 offHandStack = player.method_6079();
        class_5250 text = class_2561.method_43473();
        if (friend) {
            text.method_10852((class_2561)class_2561.method_43470((String)"[F]").method_27692(class_124.field_1060)).method_27693(" ");
        }
        AntiBot.getInstance();
        if (AntiBot.isBot(player)) {
            text.method_10852((class_2561)class_2561.method_43470((String)"[BOT]").method_27692(class_124.field_1079)).method_27693(" ");
        }
        if (this.playerSetting.isSelected("Prefix")) {
            text.method_10852(this.getVanillaNametag(player));
        } else {
            class_124 nameColor = player.method_5781() != null && player.method_5781().method_1202() != class_124.field_1070 ? player.method_5781().method_1202() : class_124.field_1068;
            text.method_10852((class_2561)class_2561.method_43470((String)this.sanitizeNametagText(player.method_5477().getString())).method_27692(nameColor));
        }
        String sphere = this.getSphere(offHandStack);
        if (!sphere.isEmpty()) {
            text.method_10852((class_2561)class_2561.method_43470((String)sphere));
        }
        return text;
    }

    private class_2561 getVanillaNametag(class_1657 player) {
        class_2561 vanillaName = player.method_5476();
        class_5250 result = vanillaName != null && !vanillaName.getString().isBlank() ? vanillaName.method_27661() : class_268.method_1142((class_270)player.method_5781(), (class_2561)player.method_5477().method_27661());
        String profileName = this.sanitizeNametagText(player.method_5477().getString()).trim();
        String visibleNametag = this.sanitizeNametagText(result.getString()).trim();
        if (!profileName.isEmpty() && !visibleNametag.toLowerCase(Locale.ROOT).contains(profileName.toLowerCase(Locale.ROOT))) {
            class_124 nameColor;
            class_124 class_1242 = nameColor = player.method_5781() != null && player.method_5781().method_1202() != class_124.field_1070 ? player.method_5781().method_1202() : class_124.field_1068;
            if (!visibleNametag.isEmpty()) {
                result.method_27693(" ");
            }
            result.method_10852((class_2561)class_2561.method_43470((String)profileName).method_27692(nameColor));
        }
        return result;
    }

    private String stripVisualFormatting(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        int offset = 0;
        while (offset < text.length()) {
            int codePoint = text.codePointAt(offset);
            int charCount = Character.charCount(codePoint);
            int consumed = this.formattingLength(text, offset, charCount);
            if (consumed > 0) {
                offset += consumed;
                continue;
            }
            builder.appendCodePoint(codePoint);
            offset += charCount;
        }
        return this.sanitizeNametagText(builder.toString());
    }

    private String substringByVisibleRange(String text, int visibleStart, int visibleEnd) {
        if (text == null || text.isEmpty() || visibleEnd <= visibleStart) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        int visibleIndex = 0;
        int offset = 0;
        while (offset < text.length()) {
            int codePoint = text.codePointAt(offset);
            int charCount = Character.charCount(codePoint);
            int consumed = this.formattingLength(text, offset, charCount);
            if (consumed > 0) {
                if (visibleIndex >= visibleStart && visibleIndex < visibleEnd) {
                    builder.append(text, offset, offset + consumed);
                }
                offset += consumed;
                continue;
            }
            if (visibleIndex >= visibleStart && visibleIndex < visibleEnd) {
                builder.appendCodePoint(codePoint);
            }
            ++visibleIndex;
            offset += charCount;
        }
        return this.sanitizeFormattedText(builder.toString());
    }

    private boolean hasFormatting(String text) {
        int charCount;
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (int offset = 0; offset < text.length(); offset += charCount) {
            int codePoint = text.codePointAt(offset);
            charCount = Character.charCount(codePoint);
            if (this.formattingLength(text, offset, charCount) <= 0) continue;
            return true;
        }
        return false;
    }

    private int formattingLength(String text, int index, int charCount) {
        int end;
        int codePoint = text.codePointAt(index);
        if ((codePoint == 167 || codePoint == 38) && index + charCount < text.length()) {
            int next = text.codePointAt(index + charCount);
            if (next == 35 && index + charCount + 7 <= text.length()) {
                return charCount + 7;
            }
            if ("0123456789abcdefklmnor".indexOf(Character.toLowerCase((char)next)) >= 0) {
                return charCount + Character.charCount(next);
            }
        }
        if (codePoint == 9167 && (end = text.indexOf(9167, index + charCount)) > index) {
            return end - index + 1;
        }
        return 0;
    }

    private String sanitizeNametagText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        int offset = 0;
        while (offset < text.length()) {
            int codePoint = text.codePointAt(offset);
            int charCount = Character.charCount(codePoint);
            if (codePoint == 167 && offset + charCount < text.length()) {
                offset += charCount + Character.charCount(text.codePointAt(offset + charCount));
                continue;
            }
            if (!this.isIconOrBrokenGlyph(codePoint)) {
                builder.appendCodePoint(codePoint);
            }
            offset += charCount;
        }
        return this.normalizeWhitespace(builder.toString());
    }

    private String normalizeWhitespace(String text) {
        int start;
        int end = text.length();
        for (start = 0; start < end && text.charAt(start) <= ' '; ++start) {
        }
        while (end > start && text.charAt(end - 1) <= ' ') {
            --end;
        }
        if (start == end) {
            return "";
        }
        StringBuilder normalized = new StringBuilder(end - start);
        boolean whitespace = false;
        for (int i = start; i < end; ++i) {
            boolean currentWhitespace;
            char character = text.charAt(i);
            boolean bl = currentWhitespace = character == ' ' || character >= '\t' && character <= '\r';
            if (currentWhitespace) {
                whitespace = true;
                continue;
            }
            if (whitespace && !normalized.isEmpty()) {
                normalized.append(' ');
            }
            normalized.append(character);
            whitespace = false;
        }
        return normalized.toString();
    }

    private boolean isIconOrBrokenGlyph(int codePoint) {
        return codePoint == 65533 || codePoint == 194;
    }

    private String getSphere(class_1799 stack) {
        class_2487 compound;
        int tsLevel;
        class_9279 component = (class_9279)stack.method_58694(class_9334.field_49628);
        if (ServerUtil.isFunTime() && component != null && (tsLevel = (compound = component.method_57461()).method_10550("tslevel").orElse(0).intValue()) != 0) {
            String donItem = compound.method_10558("don-item").orElse("");
            return " [" + String.valueOf(class_124.field_1065) + donItem.replace("sphere-", "").toUpperCase() + String.valueOf(class_124.field_1070) + "]";
        }
        return "";
    }

    private boolean isDonationItem(class_1799 stack) {
        return !this.getSphere(stack).isEmpty();
    }

    private static final class NametagEntry {
        String text = "";
        String healthText = "";
        float nameWidth;
        float width;
        float boxHeight;
        boolean friend;
        long time;

        private NametagEntry() {
        }
    }

    private static final class ItemTagEntry {
        class_1799 snapshot;
        class_1799 durabilityStack;
        int rarityColor;
        FontRenderer font;
        String language;
        String formatted;
        float width;
        float boxHeight;
        float handHeight;
        boolean empty;

        private ItemTagEntry() {
        }
    }
}

