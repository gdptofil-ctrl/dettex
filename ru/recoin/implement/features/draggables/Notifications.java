/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.class_10799
 *  net.minecraft.class_124
 *  net.minecraft.class_1291
 *  net.minecraft.class_1293
 *  net.minecraft.class_1294
 *  net.minecraft.class_1297
 *  net.minecraft.class_1304
 *  net.minecraft.class_1542
 *  net.minecraft.class_1657
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1937
 *  net.minecraft.class_2561
 *  net.minecraft.class_2596
 *  net.minecraft.class_2653
 *  net.minecraft.class_2663
 *  net.minecraft.class_2744
 *  net.minecraft.class_2775
 *  net.minecraft.class_329
 *  net.minecraft.class_332
 *  net.minecraft.class_3414
 *  net.minecraft.class_408
 *  net.minecraft.class_4587
 *  net.minecraft.class_5250
 *  net.minecraft.class_6880
 *  net.minecraft.class_9288
 *  net.minecraft.class_9290
 *  net.minecraft.class_9334
 *  net.minecraft.class_9848
 */
package ru.recoin.implement.features.draggables;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.class_10799;
import net.minecraft.class_124;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1937;
import net.minecraft.class_2561;
import net.minecraft.class_2596;
import net.minecraft.class_2653;
import net.minecraft.class_2663;
import net.minecraft.class_2744;
import net.minecraft.class_2775;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_3414;
import net.minecraft.class_408;
import net.minecraft.class_4587;
import net.minecraft.class_5250;
import net.minecraft.class_6880;
import net.minecraft.class_9288;
import net.minecraft.class_9290;
import net.minecraft.class_9334;
import net.minecraft.class_9848;
import ru.recoin.api.feature.draggable.AbstractDraggable;
import ru.recoin.api.system.animation.Animation;
import ru.recoin.api.system.animation.Direction;
import ru.recoin.api.system.animation.implement.DecelerateAnimation;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.api.system.sound.SoundManager;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.implement.events.container.SetScreenEvent;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.features.modules.combat.AutoTotem;
import ru.recoin.implement.features.modules.render.Hud;

public class Notifications
extends AbstractDraggable {
    private static final float ITEM_ICON_SCALE = 0.7f;
    private static final float ITEM_ICON_WIDTH = 11.0f;
    private static final float ITEM_ICON_X_OFFSET = -3.2f;
    private static final float ITEM_ICON_Y_OFFSET = 0.2f;
    private static final float SWITCH_ICON_BLOCK_WIDTH = 17.0f;
    private static final float SWITCH_ICON_BLOCK_GAP = 3.0f;
    private static final float NOTIFICATION_HEIGHT = 15.0f;
    private static final String SWITCH_ON_ICON = "\ue050";
    private static final String SWITCH_OFF_ICON = "\ue025";
    private static final String PREVIEW_TEXT = "\u042d\u0442\u043e \u0443\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u0435, \u0434\u043b\u044f \u0442\u043e\u0433\u043e \u0447\u0442\u043e\u0431\u044b \u043d\u0430\u0441\u0442\u0440\u043e\u0438\u0442\u044c \u043d\u0430\u0436\u043c\u0438 \u041f\u041a\u041c";
    private static final int POTION_WARNING_TICKS = 200;
    private static final Map<class_6880<class_1291>, String> TRACKED_EFFECTS = Map.of(class_1294.field_5910, "\u0421\u0438\u043b\u0430", class_1294.field_5904, "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c", class_1294.field_5905, "\u041d\u0435\u0432\u0438\u0434\u0438\u043c\u043e\u0441\u0442\u044c", class_1294.field_5918, "\u041e\u0433\u043d\u0435\u0441\u0442\u043e\u0439\u043a\u043e\u0441\u0442\u044c", class_1294.field_5898, "\u041f\u043e\u0433\u043b\u043e\u0449\u0435\u043d\u0438\u0435");
    private final List<Notification> list = new ArrayList<Notification>();
    private final List<Stack> stacks = new ArrayList<Stack>();
    private final HashSet<class_6880<class_1291>> warnedEffects = new HashSet();
    private final Map<String, class_1792> warnedDurability = new HashMap<String, class_1792>();
    private final Map<UUID, class_1799> recentTotems = new HashMap<UUID, class_1799>();
    private final Map<Integer, class_9288> lastKnownContainers = new HashMap<Integer, class_9288>();
    private final PendingModuleSwitch enabledModuleSwitches = new PendingModuleSwitch(true);
    private final PendingModuleSwitch disabledModuleSwitches = new PendingModuleSwitch(false);

    public static Notifications getInstance() {
        return Instance.getDraggable(Notifications.class);
    }

    public Notifications() {
        super("Notifications", 0, 50, 100, 15, true);
    }

    @Override
    public boolean canDragX() {
        return false;
    }

    @Override
    public void tick() {
        this.flushModuleSwitches();
        this.list.forEach(notif -> {
            if (System.currentTimeMillis() > notif.removeTime || notif.text.getString().contains(PREVIEW_TEXT) && !PlayerIntersectionUtil.isChat(Notifications.mc.field_1755)) {
                notif.anim.setDirection(Direction.BACKWARDS);
            }
        });
        this.list.removeIf(notif -> notif.anim.isFinished(Direction.BACKWARDS));
        this.updatePotionWarnings();
        this.updateDurabilityWarnings();
        this.updateTotemSnapshots();
        while (!this.stacks.isEmpty()) {
            this.addTextIfNotEmpty(TypePickUp.INVENTORY, "\u041f\u043e\u0434\u043e\u0431\u0440\u0430\u043d\u044b \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b: ");
            this.addTextIfNotEmpty(TypePickUp.SHULKER_INVENTORY, "\u0414\u043e\u0431\u0430\u0432\u043b\u0435\u043d\u043e \u0432 \u0448\u0430\u043b\u043a\u0435\u0440: ");
            this.addTextIfNotEmpty(TypePickUp.SHULKER, "\u041f\u043e\u0434\u043e\u0431\u0440\u0430\u043d \u0448\u0430\u043b\u043a\u0435\u0440 \u0441 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430\u043c\u0438: ");
        }
    }

    private void updatePotionWarnings() {
        if (Notifications.mc.field_1724 == null || Notifications.mc.field_1687 == null || !Hud.getInstance().notificationSettings.isSelected("Potion Expire")) {
            this.warnedEffects.clear();
            return;
        }
        for (Map.Entry<class_6880<class_1291>, String> tracked : TRACKED_EFFECTS.entrySet()) {
            class_6880<class_1291> effect = tracked.getKey();
            class_1293 instance = Notifications.mc.field_1724.method_6112(effect);
            if (instance == null || instance.method_48559()) {
                this.warnedEffects.remove(effect);
                continue;
            }
            int duration = instance.method_5584();
            if (duration > 200) {
                this.warnedEffects.remove(effect);
                continue;
            }
            if (duration <= 0 || !this.warnedEffects.add(effect)) continue;
            this.addList((class_2561)class_2561.method_43470((String)(tracked.getValue() + " \u0437\u0430\u043a\u043e\u043d\u0447\u0438\u0442\u0441\u044f \u0447\u0435\u0440\u0435\u0437 10 \u0441\u0435\u043a\u0443\u043d\u0434")), 3000L);
        }
    }

    private void updateDurabilityWarnings() {
        if (Notifications.mc.field_1724 == null || !Hud.getInstance().notificationSettings.isSelected("Low Durability")) {
            this.warnedDurability.clear();
            return;
        }
        this.checkDurability("head", Notifications.mc.field_1724.method_6118(class_1304.field_6169));
        this.checkDurability("chest", Notifications.mc.field_1724.method_6118(class_1304.field_6174));
        this.checkDurability("legs", Notifications.mc.field_1724.method_6118(class_1304.field_6172));
        this.checkDurability("feet", Notifications.mc.field_1724.method_6118(class_1304.field_6166));
        this.checkDurability("main", Notifications.mc.field_1724.method_6047());
        this.checkDurability("off", Notifications.mc.field_1724.method_6079());
    }

    private void checkDurability(String slot, class_1799 stack) {
        if (stack == null || stack.method_7960() || !stack.method_7963()) {
            this.warnedDurability.remove(slot);
            return;
        }
        int remaining = Math.max(0, stack.method_7936() - stack.method_7919());
        float fraction = (float)remaining / (float)Math.max(1, stack.method_7936());
        if (fraction > 0.15f) {
            this.warnedDurability.remove(slot);
            return;
        }
        if (this.warnedDurability.put(slot, stack.method_7909()) != stack.method_7909()) {
            int percent = Math.max(0, Math.round(fraction * 100.0f));
            this.addIconList((class_2561)class_2561.method_43470((String)"\u041d\u0438\u0437\u043a\u0430\u044f \u043f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u044c: ").method_10852(stack.method_7964()).method_10852((class_2561)class_2561.method_43470((String)(" (" + percent + "%)"))), 5000L, "\ue07e", -45737);
        }
    }

    @Override
    public void packet(PacketEvent e) {
        if (!PlayerIntersectionUtil.nullCheck()) {
            class_2596<?> class_25962 = e.getPacket();
            Objects.requireNonNull(class_25962);
            class_2596<?> class_25963 = class_25962;
            if (class_25963 instanceof class_2744 equipment) {
                        Object object;
                        if (Notifications.mc.field_1687 == null || !((object = Notifications.mc.field_1687.method_8469(equipment.method_11820())) instanceof class_1657)) {
                            return;
                        }
                        class_1657 player = (class_1657)object;
                        for (Pair pair : equipment.method_30145()) {
                            class_1799 stack2 = (class_1799)pair.getSecond();
                            if (stack2 == null || stack2.method_7960() || !stack2.method_31574(class_1802.field_8288) && !this.isTalisman(stack2)) continue;
                            this.recentTotems.put(player.method_5667(), stack2.method_46651(1));
                        }
                    } else if (class_25963 instanceof class_2663 status) {
                        class_5250 text;
                        if (!Hud.getInstance().notificationSettings.isSelected("Totem Pop") || status.method_11470() != 35) {
                            return;
                        }
                        class_1297 pair = status.method_11469((class_1937)Notifications.mc.field_1687);
                        if (!(pair instanceof class_1657)) {
                            return;
                        }
                        class_1657 player = (class_1657)pair;
                        boolean isSelf = player.equals((Object)Notifications.mc.field_1724);
                        class_1799 usedStack = this.findTotemStack(player);
                        boolean custom = this.isCustomTotem(usedStack) || this.isTalisman(usedStack);
                        boolean enchanted = this.isEnchantedTotem(usedStack) || this.isEnchantedTotem(player);
                        class_5250 class_52502 = text = isSelf ? class_2561.method_43470((String)"\u0412\u044b \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043b\u0438 ") : class_2561.method_43470((String)"\u0418\u0433\u0440\u043e\u043a ").method_10852((class_2561)player.method_5476().method_27661()).method_27693(" \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043b ");
                        if (custom && !usedStack.method_7960()) {
                            text.method_10852((class_2561)usedStack.method_7964().method_27661());
                        } else if (enchanted) {
                            text.method_27693("\u0437\u0430\u0447\u0430\u0440\u043e\u0432\u0430\u043d\u043d\u044b\u0439 \u0442\u043e\u0442\u0435\u043c \u0431\u0435\u0441\u0441\u043c\u0435\u0440\u0442\u0438\u044f");
                        } else {
                            text.method_27693("\u0442\u043e\u0442\u0435\u043c \u0431\u0435\u0441\u0441\u043c\u0435\u0440\u0442\u0438\u044f");
                        }
                        this.recentTotems.remove(player.method_5667());
                        this.addList((class_2561)text, 3000L, usedStack.method_7960() ? class_1802.field_8288.method_7854() : usedStack.method_46651(1));
                    } else if (class_25963 instanceof class_2775 item2) {
                        class_1297 usedStack;
                        if (!Hud.getInstance().notificationSettings.isSelected("Item Pick Up") || item2.method_11912() != Objects.requireNonNull(Notifications.mc.field_1724).method_5628() || !((usedStack = Objects.requireNonNull(Notifications.mc.field_1687).method_8469(item2.method_11915())) instanceof class_1542)) {
                            return;
                        }
                        class_1542 entity = (class_1542)usedStack;
                        class_1799 itemStack = entity.method_6983();
                        if (itemStack == null || itemStack.method_7960()) {
                            return;
                        }
                        class_9288 component = (class_9288)itemStack.method_58694(class_9334.field_49622);
                        if (component == null) {
                            class_2561 itemText = itemStack.method_7964();
                            if (itemText == null || itemText.getString().isBlank()) {
                                return;
                            }
                            this.addPickupStack(TypePickUp.INVENTORY, itemStack.method_7909(), itemText, itemStack.method_7947());
                            return;
                        }
                        component.method_57489().forEach(stack -> {
                            if (!stack.method_7960()) {
                                this.addPickupStack(TypePickUp.SHULKER, stack.method_7909(), stack.method_7964(), stack.method_7947());
                            }
                        });
                    } else if (class_25963 instanceof class_2653 slot) {
                        if (!Hud.getInstance().notificationSettings.isSelected("Item Pick Up")) {
                            return;
                        }
                        int slotId = slot.method_11450();
                        if (slot.method_11452() != 0 || slotId < 0 || Notifications.mc.field_1724 == null || slotId >= Notifications.mc.field_1724.field_7512.field_7761.size()) {
                            return;
                        }
                        class_9288 updatedContainer = (class_9288)slot.method_11449().method_58694(class_9334.field_49622);
                        class_9288 currentContainer = this.lastKnownContainers.get(slotId);
                        if (currentContainer == null) {
                            currentContainer = (class_9288)Notifications.mc.field_1724.field_7512.method_7611(slotId).method_7677().method_58694(class_9334.field_49622);
                        }
                        if (updatedContainer != null) {
                            this.lastKnownContainers.put(slotId, updatedContainer);
                            if (currentContainer == null) {
                                return;
                            }
                            HashMap<class_1792, Integer> currentCounts = new HashMap<class_1792, Integer>();
                            currentContainer.method_57489().forEach(s -> currentCounts.merge(s.method_7909(), s.method_7947(), Integer::sum));
                            HashMap<class_1792, Integer> updatedCounts = new HashMap<class_1792, Integer>();
                            HashMap names = new HashMap();
                            updatedContainer.method_57489().forEach(s -> {
                                updatedCounts.merge(s.method_7909(), s.method_7947(), Integer::sum);
                                names.putIfAbsent(s.method_7909(), s.method_7964());
                            });
                            updatedCounts.forEach((item, updatedCount) -> {
                                int oldCount = currentCounts.getOrDefault(item, 0);
                                int diff = updatedCount - oldCount;
                                if (diff > 0) {
                                    class_2561 itemName = (class_2561)names.get(item);
                                    this.addPickupStack(TypePickUp.SHULKER_INVENTORY, (class_1792)item, itemName, diff);
                                }
                            });
                            return;
                        }
                        this.lastKnownContainers.remove(slotId);
                    }
        }
    }

    private boolean isEnchantedTotem(class_1657 player) {
        return this.isEnchantedTotem(player.method_6047()) || this.isEnchantedTotem(player.method_6079());
    }

    private boolean isEnchantedTotem(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        return AutoTotem.isEnchantedTotem(stack);
    }

    private boolean isTalisman(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        String name = stack.method_7964().getString().toLowerCase(Locale.ROOT);
        if (name.contains("\u0442\u0430\u043b\u0438\u0441\u043c\u0430\u043d") || name.contains("talisman")) {
            return true;
        }
        class_9290 lore = (class_9290)stack.method_58694(class_9334.field_49632);
        if (lore != null && !lore.comp_2400().isEmpty()) {
            for (class_2561 line : lore.comp_2400()) {
                String str = line.getString().toLowerCase(Locale.ROOT);
                if (!str.contains("\u0442\u0430\u043b\u0438\u0441\u043c\u0430\u043d") && !str.contains("talisman")) continue;
                return true;
            }
        }
        return false;
    }

    private boolean isCustomTotem(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        if (this.isTalisman(stack)) {
            return true;
        }
        if (!stack.method_31574(class_1802.field_8288)) {
            return false;
        }
        String name = stack.method_7964().getString().toLowerCase(Locale.ROOT).trim();
        return !name.equals("\u0442\u043e\u0442\u0435\u043c \u0431\u0435\u0441\u0441\u043c\u0435\u0440\u0442\u0438\u044f") && !name.equals("\u0442\u043e\u0442\u0435\u043c") && !name.equals("totem of undying") && !name.equals("totem");
    }

    private void updateTotemSnapshots() {
        if (Notifications.mc.field_1687 == null) {
            this.recentTotems.clear();
            return;
        }
        for (class_1657 player : Notifications.mc.field_1687.method_18456()) {
            class_1799 offHand = player.method_6079();
            class_1799 mainHand = player.method_6047();
            class_1799 best = class_1799.field_8037;
            if (this.isTalisman(offHand) || this.isCustomTotem(offHand)) {
                best = offHand;
            } else if (this.isTalisman(mainHand) || this.isCustomTotem(mainHand)) {
                best = mainHand;
            } else if (this.isEnchantedTotem(offHand)) {
                best = offHand;
            } else if (this.isEnchantedTotem(mainHand)) {
                best = mainHand;
            } else if (offHand.method_31574(class_1802.field_8288)) {
                best = offHand;
            } else if (mainHand.method_31574(class_1802.field_8288)) {
                best = mainHand;
            }
            if (best.method_7960()) continue;
            this.recentTotems.put(player.method_5667(), best.method_46651(1));
        }
        this.recentTotems.keySet().removeIf(uuid -> Notifications.mc.field_1687.method_18470(uuid) == null);
    }

    private class_1799 findTotemStack(class_1657 player) {
        class_1799 offHand = player.method_6079();
        class_1799 mainHand = player.method_6047();
        if (this.isTalisman(offHand) || this.isCustomTotem(offHand)) {
            return offHand.method_7972();
        }
        if (this.isTalisman(mainHand) || this.isCustomTotem(mainHand)) {
            return mainHand.method_7972();
        }
        if (this.isEnchantedTotem(offHand)) {
            return offHand.method_7972();
        }
        if (this.isEnchantedTotem(mainHand)) {
            return mainHand.method_7972();
        }
        if (offHand.method_31574(class_1802.field_8288)) {
            return offHand.method_7972();
        }
        if (mainHand.method_31574(class_1802.field_8288)) {
            return mainHand.method_7972();
        }
        class_1799 recent = this.recentTotems.get(player.method_5667());
        if (recent != null && !recent.method_7960()) {
            return recent.method_7972();
        }
        return class_1799.field_8037;
    }

    @Override
    public void setScreen(SetScreenEvent e) {
        if (e.getScreen() instanceof class_408) {
            this.addList(PREVIEW_TEXT, 99999999L);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void drawDraggable(class_332 context) {
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        FontRenderer font = Fonts.getSize(12, Fonts.Type.INTER_BOLD);
        FontRenderer iconFont = Fonts.getSize(17, Fonts.Type.COMBINED_ICONS);
        float widest = 100.0f;
        float totalHeight = 0.0f;
        for (Notification notification : this.list) {
            boolean hasIcon = notification.stack != null || notification.effect != null || notification.icon != null;
            boolean hasSwitchIcon = notification.switchState != null;
            float mainWidth = font.getStringWidth(notification.text) + 10.0f + (hasIcon ? 11.0f : 0.0f);
            widest = Math.max(widest, mainWidth + (hasSwitchIcon ? 17.0f : 0.0f));
            totalHeight += 18.0f * notification.anim.getOutput().floatValue();
        }
        this.setWidth(Math.max(1, Math.round(widest)));
        this.setHeight(Math.max(Math.round(15.0f), Math.round(totalHeight)));
        this.setX((window.getScaledWidth() - this.getWidth()) / 2);
        float offsetY = 0.0f;
        float offsetX = 5.0f;
        for (Notification notification : this.list) {
            float anim = notification.anim.getOutput().floatValue();
            boolean hasIcon = notification.stack != null || notification.effect != null || notification.icon != null;
            boolean hasSwitchIcon = notification.switchState != null;
            float iconWidth = hasIcon ? 11.0f : 0.0f;
            float mainWidth = font.getStringWidth(notification.text) + offsetX * 2.0f + iconWidth;
            float width = mainWidth + (hasSwitchIcon ? 17.0f : 0.0f);
            float startY = (float)this.getY() + offsetY;
            float startX = (float)this.getX() + ((float)this.getWidth() - width) / 2.0f;
            float previousAlpha = Render2DUtil.getRenderAlpha();
            Render2DUtil.setRenderAlpha(previousAlpha * anim);
            try {
                MathUtil.setAlpha(anim, () -> {
                    float contentX = startX;
                    blur.render(ShapeProperties.create(matrix, contentX, startY, width, 15.0).round(5.0f).thickness(0.0f).quality(50.0f).outlineColor(0).color(ColorUtil.DARK_BLACK).build());
                    if (hasSwitchIcon) {
                        boolean enabled = notification.switchState == SwitchState.ENABLED;
                        String icon = enabled ? SWITCH_ON_ICON : SWITCH_OFF_ICON;
                        int color = enabled ? -12189835 : -45737;
                        iconFont.drawCenteredString(matrix, icon, contentX + 8.5f, startY + 4.9f, color);
                        rectangle.render(ShapeProperties.create(matrix, contentX + 17.0f - 0.5f, startY + 3.0f, 0.5, 9.0).round(0.25f).color(ColorUtil.getOutline(0.55f)).build());
                        contentX += 17.0f;
                    }
                    float textX = contentX + offsetX;
                    if (notification.icon != null) {
                        iconFont.drawCenteredString(matrix, notification.icon, textX + 4.5f, startY + 4.9f, notification.iconColor);
                        textX += 11.0f;
                    } else if (notification.effect != null) {
                        context.method_52707(class_10799.field_56883, class_329.method_71644(notification.effect), Math.round(textX + -3.2f + 1.5f), Math.round(startY + 0.2f + 3.29f), 9, 9, class_9848.method_61317((float)anim));
                        textX += 11.0f;
                    } else if (notification.stack != null) {
                        Render2DUtil.defaultDrawStack(context, notification.stack, textX + -3.2f, startY + 0.2f, false, false, 0.7f);
                        textX += 11.0f;
                    }
                    font.drawText(matrix, notification.text, (int)textX, startY + 6.5f);
                });
            }
            finally {
                Render2DUtil.setRenderAlpha(previousAlpha);
            }
            offsetY += 18.0f * anim;
        }
    }

    private void addPickupStack(TypePickUp type, class_1792 item, class_2561 name, int count) {
        if (count <= 0 || item == null) {
            return;
        }
        for (int i = 0; i < this.stacks.size(); ++i) {
            Stack existing = this.stacks.get(i);
            if (existing.type != type || existing.item != item) continue;
            this.stacks.set(i, new Stack(type, item, name, existing.count + count));
            return;
        }
        this.stacks.add(new Stack(type, item, name, count));
    }

    /*
     * WARNING - void declaration
     */
    private void addTextIfNotEmpty(TypePickUp type, String prefix) {
        ArrayList<Stack> matching = new ArrayList<Stack>();
        for (int i = 0; i < this.stacks.size(); ++i) {
            Stack stack = this.stacks.get(i);
            if (stack.type != type) continue;
            matching.add(stack);
            this.stacks.remove(i--);
        }
        if (matching.isEmpty()) {
            return;
        }
        LinkedHashMap<class_1792, Stack> grouped = new LinkedHashMap<class_1792, Stack>();
        for (Stack stack : matching) {
            grouped.merge(stack.item, stack, (existing, next) -> new Stack(type, next.item, next.name, existing.count + next.count));
        }
        class_5250 text = class_2561.method_43473();
        boolean bl = false;
        int total = grouped.size();
        int var6_9 = 0;
        for (Stack entry : grouped.values()) {
            class_5250 itemText = class_2561.method_43473().method_10852(entry.name);
            if (entry.count > 1) {
                itemText.method_27693(String.valueOf(class_124.field_1070) + " [" + String.valueOf(class_124.field_1061) + entry.count + String.valueOf(class_124.field_1080) + "x" + String.valueOf(class_124.field_1070) + "]");
            }
            text.method_10852((class_2561)itemText);
            if (text.getString().length() > 150) break;
            if (++var6_9 >= total) continue;
            text.method_27693(" ,  ");
        }
        if (!text.equals((Object)class_2561.method_43473())) {
            for (int i = 0; i < this.list.size(); ++i) {
                Notification n = this.list.get(i);
                if (!n.text.getString().startsWith(prefix) || n.removeTime - System.currentTimeMillis() <= 3000L) continue;
                class_5250 combined = class_2561.method_43473().method_10852(n.text).method_27693(" ,  ").method_10852((class_2561)text);
                this.list.set(i, new Notification((class_2561)combined, n.anim, System.currentTimeMillis() + 8000L, n.stack, n.effect, n.switchState, n.icon, n.iconColor));
                return;
            }
            this.addList((class_2561)class_2561.method_43473().method_27693(prefix).method_10852((class_2561)text), 8000L);
        }
    }

    public void addList(String text, long removeTime) {
        this.addList((class_2561)class_2561.method_43473().method_27693(text), removeTime, null, null);
    }

    public void addList(class_2561 text, long removeTime) {
        this.addList(text, removeTime, null, null, this.getPotionExpireEffect(text));
    }

    public void addList(String text, long removeTime, class_3414 sound) {
        this.addList((class_2561)class_2561.method_43473().method_27693(text), removeTime, sound);
    }

    public void addList(String text, long removeTime, class_1792 item) {
        this.addList((class_2561)class_2561.method_43473().method_27693(text), removeTime, item);
    }

    public void addList(class_2561 text, long removeTime, class_3414 sound) {
        this.addList(text, removeTime, sound, null);
    }

    public void addList(class_2561 text, long removeTime, class_1792 item) {
        this.addList(text, removeTime, null, item);
    }

    public void addList(class_2561 text, long removeTime, class_1799 stack) {
        this.addStackList(text, removeTime, null, stack, null, null, null, 0);
    }

    public void addList(class_2561 text, long removeTime, class_3414 sound, class_1792 item) {
        this.addList(text, removeTime, sound, item, null);
    }

    public void addList(class_2561 text, long removeTime, class_3414 sound, class_1792 item, class_6880<class_1291> effect) {
        this.addList(text, removeTime, sound, item, effect, null);
    }

    public void addModuleSwitchList(class_2561 text, long removeTime, class_3414 sound, boolean enabled) {
        this.addList(text, removeTime, sound, null, null, enabled ? SwitchState.ENABLED : SwitchState.DISABLED, null, 0);
    }

    public void queueModuleSwitch(String moduleName, boolean enabled, class_3414 sound) {
        PendingModuleSwitch pending = enabled ? this.enabledModuleSwitches : this.disabledModuleSwitches;
        pending.names.add(moduleName);
        if (pending.sound == null) {
            pending.sound = sound;
        }
        pending.flushAt = System.currentTimeMillis() + 30L;
    }

    private void flushModuleSwitches() {
        this.flushModuleSwitch(this.enabledModuleSwitches);
        this.flushModuleSwitch(this.disabledModuleSwitches);
    }

    private void flushModuleSwitch(PendingModuleSwitch pending) {
        boolean plural;
        if (pending.names.isEmpty() || System.currentTimeMillis() < pending.flushAt) {
            return;
        }
        boolean russian = Hud.getInstance().isRussianLanguage();
        boolean bl = plural = pending.names.size() > 1;
        class_5250 text = class_2561.method_43470((String)(russian ? (plural ? "\u041c\u043e\u0434\u0443\u043b\u0438 " : "\u041c\u043e\u0434\u0443\u043b\u044c ") : (plural ? "Modules " : "Module ")));
        int index = 0;
        for (String name : pending.names) {
            if (index++ > 0) {
                text.method_10852((class_2561)class_2561.method_43470((String)", "));
            }
            text.method_10852((class_2561)class_2561.method_43470((String)"\u00ab"));
            text.method_10852((class_2561)class_2561.method_43470((String)name).method_27692(pending.enabled ? class_124.field_1060 : class_124.field_1061));
            text.method_10852((class_2561)class_2561.method_43470((String)"\u00bb"));
        }
        text.method_10852((class_2561)class_2561.method_43470((String)(russian ? (pending.enabled ? (plural ? " \u0432\u043a\u043b\u044e\u0447\u0435\u043d\u044b" : " \u0432\u043a\u043b\u044e\u0447\u0451\u043d") : (plural ? " \u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d\u044b" : " \u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d")) : (pending.enabled ? " enabled" : " disabled"))));
        this.addModuleSwitchList((class_2561)text, 2000L, pending.sound, pending.enabled);
        pending.names.clear();
        pending.sound = null;
        pending.flushAt = 0L;
    }

    public void addIconList(class_2561 text, long removeTime, String icon, int iconColor) {
        this.addList(text, removeTime, null, null, null, null, icon, iconColor);
    }

    private void addList(class_2561 text, long removeTime, class_3414 sound, class_1792 item, class_6880<class_1291> effect, SwitchState switchState) {
        this.addList(text, removeTime, sound, item, effect, switchState, null, 0);
    }

    private void addList(class_2561 text, long removeTime, class_3414 sound, class_1792 item, class_6880<class_1291> effect, SwitchState switchState, String icon, int iconColor) {
        this.addStackList(text, removeTime, sound, item == null ? null : item.method_7854(), effect, switchState, icon, iconColor);
    }

    private void addStackList(class_2561 text, long removeTime, class_3414 sound, class_1799 stack, class_6880<class_1291> effect, SwitchState switchState, String icon, int iconColor) {
        this.list.add(new Notification(text, new DecelerateAnimation().setMs(300).setValue(1.0), System.currentTimeMillis() + removeTime, stack == null ? null : stack.method_46651(1), effect, switchState, icon, iconColor));
        if (this.list.size() > 12) {
            this.list.removeFirst();
        }
        this.list.sort(Comparator.comparingDouble(notif -> -notif.removeTime));
        if (sound != null) {
            SoundManager.playSound(sound);
        }
    }

    public void clear() {
        this.list.clear();
        this.stacks.clear();
        this.warnedEffects.clear();
        this.warnedDurability.clear();
        this.recentTotems.clear();
        this.lastKnownContainers.clear();
    }

    private class_6880<class_1291> getPotionExpireEffect(class_2561 text) {
        if (text == null) {
            return null;
        }
        String value = text.getString();
        if (value.isEmpty() || !value.contains("10")) {
            return null;
        }
        for (Map.Entry<class_6880<class_1291>, String> entry : TRACKED_EFFECTS.entrySet()) {
            if (!value.contains(entry.getValue())) continue;
            return entry.getKey();
        }
        return null;
    }

    private static final class PendingModuleSwitch {
        private final boolean enabled;
        private final LinkedHashSet<String> names = new LinkedHashSet();
        private class_3414 sound;
        private long flushAt;

        private PendingModuleSwitch(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static enum TypePickUp {
        INVENTORY,
        SHULKER,
        SHULKER_INVENTORY;

    }

    public record Notification(class_2561 text, Animation anim, long removeTime, class_1799 stack, class_6880<class_1291> effect, SwitchState switchState, String icon, int iconColor) {
    }

    public static enum SwitchState {
        ENABLED,
        DISABLED;

    }

    public record Stack(TypePickUp type, class_1792 item, class_2561 name, int count) {
    }
}

