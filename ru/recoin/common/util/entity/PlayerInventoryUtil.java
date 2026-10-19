/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntPredicate
 *  net.minecraft.class_124
 *  net.minecraft.class_1268
 *  net.minecraft.class_1291
 *  net.minecraft.class_1657
 *  net.minecraft.class_1661
 *  net.minecraft.class_1713
 *  net.minecraft.class_1730
 *  net.minecraft.class_1735
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1844
 *  net.minecraft.class_2561
 *  net.minecraft.class_2596
 *  net.minecraft.class_2815
 *  net.minecraft.class_4081
 *  net.minecraft.class_4174
 *  net.minecraft.class_5537
 *  net.minecraft.class_6880
 *  net.minecraft.class_9276
 *  net.minecraft.class_9276$class_9277
 *  net.minecraft.class_9334
 *  net.minecraft.class_9837
 */
package ru.recoin.common.util.entity;

import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.class_124;
import net.minecraft.class_1268;
import net.minecraft.class_1291;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1713;
import net.minecraft.class_1730;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_2561;
import net.minecraft.class_2596;
import net.minecraft.class_2815;
import net.minecraft.class_4081;
import net.minecraft.class_4174;
import net.minecraft.class_5537;
import net.minecraft.class_6880;
import net.minecraft.class_9276;
import net.minecraft.class_9334;
import net.minecraft.class_9837;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.entity.PlayerInventoryComponent;
import ru.recoin.common.util.item.ItemUtil;
import ru.recoin.implement.features.draggables.Notifications;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.render.Hud;

public final class PlayerInventoryUtil
implements QuickImports {
    private static final Map<class_1735, BundleResolution> directBundleHandSessions = new IdentityHashMap<class_1735, BundleResolution>();
    private static final Map<class_1792, Long> lastCooldownNotify = new HashMap<class_1792, Long>();

    public static void moveItem(class_1735 from, int to) {
        if ((from = PlayerInventoryUtil.resolveSlot(from)) != null) {
            PlayerInventoryUtil.moveItem(from.field_7874, to, false, false);
        }
    }

    public static void moveItem(class_1735 from, int to, boolean task) {
        PlayerInventoryUtil.moveItem(from, to, task, false);
    }

    public static void moveItem(class_1735 from, int to, boolean task, boolean updateInventory) {
        if ((from = PlayerInventoryUtil.resolveSlot(from)) != null) {
            PlayerInventoryUtil.moveItem(from.field_7874, to, task, updateInventory);
        }
    }

    public static void moveItem(int from, int to, boolean task, boolean updateInventory) {
        if (from == to || from == -1) {
            return;
        }
        int count = Math.toIntExact(PlayerInventoryUtil.slots().count()) - 10;
        if (from >= count && count == 36) {
            if (task) {
                PlayerInventoryComponent.addTask(() -> PlayerInventoryUtil.clickSlot(to, from - count, class_1713.field_7791, false));
            } else {
                PlayerInventoryUtil.clickSlot(to, from - count, class_1713.field_7791, false);
            }
            return;
        }
        if (task) {
            PlayerInventoryComponent.addTask(() -> PlayerInventoryUtil.moveItem(from, to, updateInventory));
        } else {
            PlayerInventoryUtil.moveItem(from, to, updateInventory);
        }
    }

    public static void moveItem(int from, int to, boolean updateInventory) {
        PlayerInventoryUtil.clickSlot(from, 0, class_1713.field_7791, false);
        PlayerInventoryUtil.clickSlot(to, 0, class_1713.field_7791, false);
        PlayerInventoryUtil.clickSlot(from, 0, class_1713.field_7791, false);
        if (updateInventory) {
            PlayerInventoryUtil.updateSlots();
        }
    }

    public static void swapHand(class_1735 slot, class_1268 hand, boolean task) {
        PlayerInventoryUtil.swapHand(slot, hand, task, false);
    }

    public static void swapHand(class_1735 slot, class_1268 hand, boolean task, boolean updateInventory) {
        int button;
        if (PlayerInventoryUtil.isBundleBackedSlot(slot)) {
            Runnable bundleSwap = () -> PlayerInventoryUtil.swapBundleHandDirect(slot, hand, updateInventory);
            if (task) {
                PlayerInventoryComponent.addTask(bundleSwap);
            } else {
                bundleSwap.run();
            }
            return;
        }
        class_1735 resolved = PlayerInventoryUtil.resolveSlot(slot);
        if (resolved == null || resolved.field_7874 == -1 || hand.equals((Object)class_1268.field_5810) && !(resolved.field_7871 instanceof class_1661) && !(resolved.field_7871 instanceof class_1730)) {
            return;
        }
        int n = button = hand.equals((Object)class_1268.field_5808) ? PlayerInventoryUtil.mc.field_1724.method_31548().method_67532() : 40;
        if (task) {
            PlayerInventoryComponent.addTask(() -> PlayerInventoryUtil.swap(resolved, button, updateInventory));
        } else {
            PlayerInventoryUtil.swap(resolved, button, updateInventory);
        }
    }

    private static void swapBundleHandDirect(class_1735 slot, class_1268 hand, boolean updateInventory) {
        BundleResolution active = directBundleHandSessions.remove(slot);
        if (active != null) {
            PlayerInventoryUtil.returnBundleItem(active);
            return;
        }
        BundleResolution resolution = PlayerInventoryUtil.prepareBundleHandResolution(slot);
        if (resolution == null) {
            return;
        }
        class_1735 extracted = PlayerInventoryUtil.extractBundleItem(resolution);
        if (extracted == null) {
            return;
        }
        directBundleHandSessions.put(slot, resolution);
    }

    public static void swap(class_1735 slot, int button, boolean updateInventory) {
        if ((slot = PlayerInventoryUtil.resolveSlot(slot)) == null) {
            return;
        }
        PlayerInventoryUtil.clickSlot(slot, button, class_1713.field_7791, false);
        if (updateInventory) {
            PlayerInventoryUtil.updateSlots();
        }
    }

    public static boolean matchesSwapStack(class_1799 current, class_1799 expected) {
        if (current.method_7960() || expected.method_7960()) {
            return current.method_7960() && expected.method_7960();
        }
        if (class_1799.method_31577((class_1799)current, (class_1799)expected)) {
            return true;
        }
        if (!current.method_31574(expected.method_7909())) {
            return false;
        }
        class_1799 currentCopy = current.method_7972();
        class_1799 expectedCopy = expected.method_7972();
        currentCopy.method_57381(class_9334.field_49629);
        expectedCopy.method_57381(class_9334.field_49629);
        return class_1799.method_31577((class_1799)currentCopy, (class_1799)expectedCopy);
    }

    public static void swapAndUse(class_1735 slot, String text, boolean task) {
        if (slot == null) {
            Notifications.getInstance().addList(String.valueOf(class_124.field_1061) + text + String.valueOf(class_124.field_1070) + " - \u0420\u0405\u0420\u00b5 \u0420\u0405\u0420\u00b0\u0420\u2116\u0420\u0491\u0420\u00b5\u0420\u0405!", 3000L);
            return;
        }
        if (task) {
            PlayerInventoryComponent.addTask(() -> PlayerInventoryUtil.swapAndUse(slot, AngleUtil.cameraAngle()));
        } else {
            PlayerInventoryUtil.swapAndUse(slot, AngleUtil.cameraAngle());
        }
    }

    public static void swapAndUse(class_1792 item) {
        PlayerInventoryUtil.swapAndUse(item, AngleUtil.cameraAngle(), true);
    }

    public static void swapAndUse(class_1792 item, Angle angle, boolean task) {
        float cooldownProgress = ItemUtil.getCooldownProgress(item);
        if (cooldownProgress > 0.0f) {
            String time = PlayerInventoryUtil.formatCooldownTime(cooldownProgress);
            Notifications.getInstance().addList(String.valueOf(class_124.field_1061) + item.method_63680().getString() + String.valueOf(class_124.field_1070) + " - \u0420\u0406 \u0420\u0454\u0420\u0491 \u0420\u00b5\u0421\u2030\u0420\u00b5 " + time, 2000L);
            return;
        }
        class_1735 slot = PlayerInventoryUtil.getSlot(item);
        if (slot == null) {
            Notifications.getInstance().addList(String.valueOf(class_124.field_1061) + item.method_63680().getString() + String.valueOf(class_124.field_1070) + " - \u0420\u0405\u0420\u00b5 \u0420\u0405\u0420\u00b0\u0420\u2116\u0420\u0491\u0420\u00b5\u0420\u0405!", 2000L);
            return;
        }
        if (task) {
            PlayerInventoryComponent.addTask(() -> PlayerInventoryUtil.swapAndUse(slot, angle));
        } else {
            PlayerInventoryUtil.swapAndUse(slot, angle);
        }
    }

    public static void swapAndUse(class_1735 slot, Angle angle) {
        PlayerInventoryUtil.swapHand(slot, class_1268.field_5808, false);
        PlayerIntersectionUtil.interactItem(class_1268.field_5808, angle);
        PlayerInventoryUtil.swapHand(slot, class_1268.field_5808, false, true);
    }

    public static boolean notifyCooldown(class_1792 item, String displayName) {
        if (item == null) {
            return false;
        }
        return PlayerInventoryUtil.notifyCooldown(item.method_7854(), displayName);
    }

    public static boolean notifyCooldown(class_1799 stack, String displayName) {
        long now;
        boolean coolingDown;
        if (PlayerInventoryUtil.mc.field_1724 == null || stack == null || stack.method_7960()) {
            return false;
        }
        class_1792 item = stack.method_7909();
        float cooldownProgress = ItemUtil.getCooldownProgress(item);
        boolean bl = coolingDown = cooldownProgress > 0.0f || PlayerInventoryUtil.mc.field_1724.method_7357().method_7904(stack) || PlayerInventoryUtil.mc.field_1724.method_7357().method_7904(item.method_7854());
        if (!coolingDown) {
            return false;
        }
        Hud hud = Hud.getInstance();
        if (hud != null && hud.state && hud.notificationSettings.isSelected("Item Cooldown") && (now = System.currentTimeMillis()) - lastCooldownNotify.getOrDefault(item, 0L) >= 1000L) {
            lastCooldownNotify.put(item, now);
            String time = PlayerInventoryUtil.formatCooldownTime(cooldownProgress > 0.0f ? cooldownProgress : 1.0f);
            Notifications.getInstance().addList((class_2561)class_2561.method_43470((String)(PlayerInventoryUtil.formatCooldownName(displayName) + " \u043d\u0430\u0445\u043e\u0434\u0438\u0442\u0441\u044f \u0432 \u043a\u0443\u043b\u0434\u0430\u0443\u043d\u0435 " + time)), 2000L, stack);
        }
        return true;
    }

    private static String formatCooldownTime(float seconds) {
        float rounded = (float)Math.round(seconds * 10.0f) / 10.0f;
        if (Math.abs(rounded - (float)Math.round(rounded)) < 0.001f) {
            return Math.round(rounded) + "\u0441";
        }
        return String.format(Locale.ROOT, "%.1f", Float.valueOf(rounded)) + "\u0441";
    }

    private static String formatCooldownName(String displayName) {
        return switch (displayName) {
            case "Trap", "Default Trap" -> "\u0422\u0440\u0430\u043f\u043a\u0430";
            case "Explosive Trap" -> "\u0412\u0437\u0440\u044b\u0432\u043d\u0430\u044f \u0442\u0440\u0430\u043f\u043a\u0430";
            case "Plast" -> "\u041f\u043b\u0430\u0441\u0442";
            case "Disorientation" -> "\u0414\u0435\u0437\u043e\u0440\u0438\u0435\u043d\u0442\u0430\u0446\u0438\u044f";
            case "Fire Tornado" -> "\u041e\u0433\u043d\u0435\u043d\u043d\u043e\u0435 \u0442\u043e\u0440\u043d\u0430\u0434\u043e";
            case "Clear Dust" -> "\u041e\u0447\u0438\u0441\u0442\u043a\u0430 \u043f\u044b\u043b\u0438";
            case "SnowBall" -> "\u0421\u043d\u0435\u0436\u043e\u043a";
            case "Stun" -> "\u0421\u0442\u0430\u043d";
            case "Explosive Thing" -> "\u0412\u0437\u0440\u044b\u0432\u043d\u043e\u0439 \u043f\u0440\u0435\u0434\u043c\u0435\u0442";
            case "Ender Pearl" -> "\u041f\u0435\u0440\u043a\u0430";
            case "Any Buff" -> "\u0411\u0430\u0444\u0444";
            case "Any Debuff" -> "\u0414\u0435\u0431\u0430\u0444\u0444";
            case "EXP Bottle" -> "\u041e\u043f\u044b\u0442";
            case "Wind Charge" -> "\u0412\u0438\u043d\u0434 \u0427\u0430\u0440\u0434\u0436";
            default -> displayName;
        };
    }

    public static void updateSlots() {
    }

    public static void closeScreen(boolean packet) {
        if (packet) {
            PlayerInventoryUtil.mc.field_1724.field_3944.method_52787((class_2596)new class_2815(PlayerInventoryUtil.mc.field_1724.field_7512.field_7763));
        } else {
            PlayerInventoryUtil.mc.field_1724.method_7346();
        }
    }

    public static void clickSlot(class_1735 slot, int button, class_1713 clickType, boolean silent) {
        class_1735 resolved = PlayerInventoryUtil.resolveSlot(slot);
        if (resolved != null) {
            PlayerInventoryUtil.clickSlot(resolved.field_7874, button, clickType, silent);
        }
    }

    public static void clickSlot(int slotId, int buttonId, class_1713 clickType, boolean silent) {
        if (PlayerInventoryUtil.mc.field_1724 == null) {
            return;
        }
        PlayerInventoryUtil.clickSlot(PlayerInventoryUtil.mc.field_1724.field_7512.field_7763, slotId, buttonId, clickType, silent);
    }

    public static void clickSlot(int windowId, int slotId, int buttonId, class_1713 clickType, boolean silent) {
        if (PlayerInventoryUtil.mc.field_1724 == null || PlayerInventoryUtil.mc.field_1761 == null || PlayerInventoryUtil.mc.field_1724.field_7512.field_7763 != windowId || slotId >= PlayerInventoryUtil.mc.field_1724.field_7512.field_7761.size()) {
            return;
        }
        PlayerInventoryUtil.mc.field_1761.method_2906(windowId, slotId, buttonId, clickType, (class_1657)PlayerInventoryUtil.mc.field_1724);
        if (silent) {
            PlayerInventoryUtil.mc.field_1724.field_7512.method_7593(slotId, buttonId, clickType, (class_1657)PlayerInventoryUtil.mc.field_1724);
        }
    }

    public static class_1735 getSlot(class_1792 item) {
        return PlayerInventoryUtil.getSlot(item, (class_1735 s) -> true);
    }

    public static class_1735 getSlot(class_1792 item, Predicate<class_1735> filter) {
        return PlayerInventoryUtil.searchableSlots().filter(s -> s.method_7677().method_7909().equals(item)).filter(filter).findFirst().orElse(null);
    }

    public static class_1735 getSlot(Predicate<class_1735> filter) {
        return PlayerInventoryUtil.searchableSlots().filter(filter).findFirst().orElse(null);
    }

    public static class_1735 getSlot(Predicate<class_1735> filter, Comparator<class_1735> comparator) {
        return PlayerInventoryUtil.searchableSlots().filter(filter).max(comparator).orElse(null);
    }

    public static class_1735 getSlot(class_1792 item, Comparator<class_1735> comparator, Predicate<class_1735> filter) {
        return PlayerInventoryUtil.searchableSlots().filter(s -> s.method_7677().method_7909().equals(item)).filter(filter).max(comparator).orElse(null);
    }

    public static class_1735 getFoodMaxSaturationSlot() {
        return PlayerInventoryUtil.searchableSlots().filter(s -> s.method_7677().method_58694(class_9334.field_50075) != null && !((class_4174)s.method_7677().method_58694(class_9334.field_50075)).comp_2493()).max(Comparator.comparingDouble(s -> ((class_4174)s.method_7677().method_58694(class_9334.field_50075)).comp_2492())).orElse(null);
    }

    public static class_1735 getSlot(List<class_1792> item) {
        return PlayerInventoryUtil.searchableSlots().filter(s -> item.contains(s.method_7677().method_7909())).findFirst().orElse(null);
    }

    public static class_1735 getPotion(class_6880<class_1291> effect) {
        return PlayerInventoryUtil.searchableSlots().filter(s -> {
            class_1844 component = (class_1844)s.method_7677().method_58694(class_9334.field_49651);
            if (component == null) {
                return false;
            }
            return StreamSupport.stream(component.method_57397().spliterator(), false).anyMatch(e -> e.method_5579().equals((Object)effect));
        }).findFirst().orElse(null);
    }

    public static class_1735 getPotionFromCategory(class_4081 category) {
        return PlayerInventoryUtil.searchableSlots().filter(s -> {
            class_1799 stack = s.method_7677();
            class_1844 component = (class_1844)stack.method_58694(class_9334.field_49651);
            if (!stack.method_7909().equals(class_1802.field_8436) || component == null) {
                return false;
            }
            class_4081 category2 = category.equals((Object)class_4081.field_18271) ? class_4081.field_18272 : class_4081.field_18271;
            long effects = StreamSupport.stream(component.method_57397().spliterator(), false).filter(e -> ((class_1291)e.method_5579().comp_349()).method_18792().equals((Object)category)).count();
            long effects2 = StreamSupport.stream(component.method_57397().spliterator(), false).filter(e -> ((class_1291)e.method_5579().comp_349()).method_18792().equals((Object)category2)).count();
            return effects >= effects2;
        }).findFirst().orElse(null);
    }

    public static int getInventoryCount(class_1792 item) {
        return IntStream.range(0, 45).filter(i -> Objects.requireNonNull(PlayerInventoryUtil.mc.field_1724).method_31548().method_5438(i).method_7909().equals(item)).map(i -> PlayerInventoryUtil.mc.field_1724.method_31548().method_5438(i).method_7947()).sum();
    }

    public static int getHotbarItems(List<class_1792> items) {
        return IntStream.range(0, 9).filter(i -> items.contains(PlayerInventoryUtil.mc.field_1724.method_31548().method_5438(i).method_7909())).findFirst().orElse(-1);
    }

    public static int getHotbarSlotId(it.unimi.dsi.fastutil.ints.IntPredicate filter) {
        return IntStream.range(0, 9).filter((IntPredicate)filter).findFirst().orElse(-1);
    }

    public static int getCount(Predicate<class_1735> filter) {
        return PlayerInventoryUtil.searchableSlots().filter(filter).mapToInt(s -> s.method_7677().method_7947()).sum();
    }

    public static class_1735 mainHandSlot() {
        if (PlayerInventoryUtil.mc.field_1724 == null) {
            return null;
        }
        return PlayerInventoryUtil.slots().filter(slot -> slot.field_7871 == PlayerInventoryUtil.mc.field_1724.method_31548() && slot.method_34266() == PlayerInventoryUtil.mc.field_1724.method_31548().method_67532()).findFirst().orElse(null);
    }

    public static boolean isServerScreen() {
        return PlayerInventoryUtil.slots().toList().size() != 46;
    }

    public static Stream<class_1735> slots() {
        return PlayerInventoryUtil.mc.field_1724.field_7512.field_7761.stream();
    }

    private static int getSlotPriority(class_1735 slot) {
        if (slot instanceof BundleBackedSlot) {
            return 100;
        }
        if (slot.field_7871 instanceof class_1661) {
            int index = slot.method_34266();
            if (index >= 0 && index < 9) {
                return 0;
            }
            if (index == 40) {
                return 1;
            }
            if (index >= 9 && index < 36) {
                return 2;
            }
            return 3;
        }
        return 50;
    }

    private static Stream<class_1735> searchableSlots() {
        Stream<class_1735> direct = PlayerInventoryUtil.slots().sorted(Comparator.comparingInt(PlayerInventoryUtil::getSlotPriority));
        Stream bundled = PlayerInventoryUtil.slots().flatMap(slot -> {
            class_9276 contents = (class_9276)slot.method_7677().method_58694(class_9334.field_49650);
            if (contents == null || contents.method_57429()) {
                return Stream.empty();
            }
            return IntStream.range(0, contents.method_57426()).mapToObj(index -> new BundleBackedSlot((class_1735)slot, index, contents.method_57422(index).method_7972()));
        });
        return Stream.concat(direct, bundled);
    }

    public static class_1735 resolveSlot(class_1735 slot) {
        if (slot == null || PlayerInventoryUtil.mc.field_1724 == null || PlayerInventoryUtil.mc.field_1761 == null) {
            return null;
        }
        if (!(slot instanceof BundleBackedSlot)) {
            return PlayerInventoryUtil.slots().filter(current -> current.field_7871 == slot.field_7871 && current.method_34266() == slot.method_34266()).findFirst().orElse(null);
        }
        BundleBackedSlot bundled = (BundleBackedSlot)slot;
        if (bundled.resolvedSlot != null) {
            return bundled.resolvedSlot;
        }
        BundleResolution resolution = PlayerInventoryUtil.prepareBundleResolution(slot);
        if (resolution == null) {
            return null;
        }
        class_1735 destination = PlayerInventoryUtil.extractBundleItem(resolution);
        if (destination == null) {
            return null;
        }
        bundled.resolvedSlot = destination;
        return destination;
    }

    public static boolean isBundleBackedSlot(class_1735 slot) {
        return slot instanceof BundleBackedSlot;
    }

    public static class_1735 bundleContainer(class_1735 slot) {
        class_1735 class_17352;
        if (slot instanceof BundleBackedSlot) {
            BundleBackedSlot bundled = (BundleBackedSlot)slot;
            class_17352 = bundled.container;
        } else {
            class_17352 = null;
        }
        return class_17352;
    }

    public static int bundleIndex(class_1735 slot) {
        int n;
        if (slot instanceof BundleBackedSlot) {
            BundleBackedSlot bundled = (BundleBackedSlot)slot;
            n = bundled.bundleIndex;
        } else {
            n = -1;
        }
        return n;
    }

    public static BundleResolution prepareBundleResolution(class_1735 slot) {
        BundleBackedSlot bundled;
        block7: {
            block6: {
                if (!(slot instanceof BundleBackedSlot)) break block6;
                bundled = (BundleBackedSlot)slot;
                if (PlayerInventoryUtil.mc.field_1724 != null && PlayerInventoryUtil.mc.field_1761 != null) break block7;
            }
            return null;
        }
        List<class_1735> allCandidates = PlayerInventoryUtil.slots().filter(candidate -> candidate.field_7871 instanceof class_1661).filter(candidate -> candidate.field_7874 != bundled.container.field_7874).filter(candidate -> candidate.method_34266() >= 0 && candidate.method_34266() < 36).toList();
        class_1735 destination = allCandidates.stream().filter(candidate -> !candidate.method_7681() || candidate.method_7677().method_7960()).sorted(Comparator.comparingInt(candidate -> candidate.method_34266() >= 9 && candidate.method_34266() < 36 ? 0 : 1)).findFirst().orElse(null);
        if (destination == null) {
            int selectedSlot = PlayerInventoryUtil.mc.field_1724.method_31548().method_67532();
            destination = allCandidates.stream().filter(candidate -> candidate.method_34266() == selectedSlot).filter(candidate -> PlayerInventoryUtil.canBufferInBundle(bundled, candidate.method_7677())).findFirst().orElse(null);
        }
        if (destination == null) {
            destination = allCandidates.stream().filter(candidate -> candidate.method_7677().method_58694(class_9334.field_49650) == null).filter(candidate -> PlayerInventoryUtil.canBufferInBundle(bundled, candidate.method_7677())).findFirst().orElse(null);
        }
        if (destination == null) {
            return null;
        }
        return new BundleResolution(bundled.container, destination, bundled.bundleIndex, bundled.bundledStack.method_7972(), destination.method_7681() ? destination.method_7677().method_7972() : class_1799.field_8037);
    }

    private static boolean canBufferInBundle(BundleBackedSlot bundled, class_1799 displacedStack) {
        if (displacedStack.method_7960()) {
            return true;
        }
        class_9276 contents = (class_9276)bundled.container.method_7677().method_58694(class_9334.field_49650);
        if (contents == null || bundled.bundleIndex < 0 || bundled.bundleIndex >= contents.method_57426()) {
            return false;
        }
        class_9276.class_9277 builder = new class_9276.class_9277(contents);
        builder.method_64662(bundled.bundleIndex);
        if (builder.method_57430() == null) {
            return false;
        }
        class_1799 copy = displacedStack.method_7972();
        return builder.method_57432(copy) == displacedStack.method_7947();
    }

    public static BundleResolution prepareBundleHandResolution(class_1735 slot) {
        BundleBackedSlot bundled;
        block5: {
            block4: {
                if (!(slot instanceof BundleBackedSlot)) break block4;
                bundled = (BundleBackedSlot)slot;
                if (PlayerInventoryUtil.mc.field_1724 != null && PlayerInventoryUtil.mc.field_1761 != null) break block5;
            }
            return null;
        }
        int selectedSlot = PlayerInventoryUtil.mc.field_1724.method_31548().method_67532();
        class_1735 destination = PlayerInventoryUtil.slots().filter(candidate -> candidate.field_7871 instanceof class_1661).filter(candidate -> candidate.field_7874 != bundled.container.field_7874).filter(candidate -> candidate.method_34266() == selectedSlot).findFirst().orElse(null);
        if (destination == null) {
            return PlayerInventoryUtil.prepareBundleResolution(slot);
        }
        return new BundleResolution(bundled.container, destination, bundled.bundleIndex, bundled.bundledStack.method_7972(), destination.method_7681() ? destination.method_7677().method_7972() : class_1799.field_8037);
    }

    public static class_1735 extractBundleItem(BundleResolution resolution) {
        if (resolution == null || PlayerInventoryUtil.mc.field_1724 == null || PlayerInventoryUtil.mc.field_1761 == null) {
            return null;
        }
        class_1799 bundleStack = resolution.container().method_7677();
        if (!(bundleStack.method_7909() instanceof class_5537)) {
            return null;
        }
        int syncId = PlayerInventoryUtil.mc.field_1724.field_7498.field_7763;
        int bundleSlotId = resolution.container().field_7874;
        int destSlotId = resolution.destination().field_7874;
        class_5537.method_61637((class_1799)bundleStack, (int)resolution.bundleIndex());
        PlayerInventoryUtil.mc.field_1724.field_3944.method_52787((class_2596)new class_9837(bundleSlotId, resolution.bundleIndex()));
        PlayerInventoryUtil.mc.field_1761.method_2906(syncId, bundleSlotId, 1, class_1713.field_7790, (class_1657)PlayerInventoryUtil.mc.field_1724);
        PlayerInventoryUtil.mc.field_1761.method_2906(syncId, destSlotId, 0, class_1713.field_7790, (class_1657)PlayerInventoryUtil.mc.field_1724);
        if (!PlayerInventoryUtil.mc.field_1724.field_7498.method_34255().method_7960()) {
            PlayerInventoryUtil.mc.field_1761.method_2906(syncId, bundleSlotId, 0, class_1713.field_7790, (class_1657)PlayerInventoryUtil.mc.field_1724);
        }
        return resolution.destinationHasTarget() && PlayerInventoryUtil.mc.field_1724.field_7498.method_34255().method_7960() ? resolution.destination() : null;
    }

    public static void returnBundleItem(BundleResolution resolution) {
        if (resolution == null || PlayerInventoryUtil.mc.field_1724 == null || PlayerInventoryUtil.mc.field_1761 == null) {
            return;
        }
        class_1799 bundleStack = resolution.container().method_7677();
        if (!(bundleStack.method_7909() instanceof class_5537)) {
            return;
        }
        int syncId = PlayerInventoryUtil.mc.field_1724.field_7498.field_7763;
        int bundleSlotId = resolution.container().field_7874;
        int destSlotId = resolution.destination().field_7874;
        class_1735 destination = resolution.destination();
        if (destination.method_7681() && !destination.method_7677().method_7960()) {
            PlayerInventoryUtil.mc.field_1761.method_2906(syncId, destSlotId, 0, class_1713.field_7790, (class_1657)PlayerInventoryUtil.mc.field_1724);
            PlayerInventoryUtil.mc.field_1761.method_2906(syncId, bundleSlotId, 0, class_1713.field_7790, (class_1657)PlayerInventoryUtil.mc.field_1724);
        }
        if (resolution.occupiedBuffer()) {
            class_9276 contents = (class_9276)bundleStack.method_58694(class_9334.field_49650);
            if (contents != null) {
                for (int index = 0; index < contents.method_57426(); ++index) {
                    class_1799 bundled = contents.method_57422(index);
                    if (!class_1799.method_31577((class_1799)bundled, (class_1799)resolution.displacedStack) || bundled.method_7947() != resolution.displacedStack.method_7947()) continue;
                    class_5537.method_61637((class_1799)bundleStack, (int)index);
                    PlayerInventoryUtil.mc.field_1724.field_3944.method_52787((class_2596)new class_9837(bundleSlotId, index));
                    break;
                }
            }
            PlayerInventoryUtil.mc.field_1761.method_2906(syncId, bundleSlotId, 1, class_1713.field_7790, (class_1657)PlayerInventoryUtil.mc.field_1724);
            PlayerInventoryUtil.mc.field_1761.method_2906(syncId, destSlotId, 0, class_1713.field_7790, (class_1657)PlayerInventoryUtil.mc.field_1724);
        }
        if (!PlayerInventoryUtil.mc.field_1724.field_7498.method_34255().method_7960()) {
            PlayerInventoryUtil.mc.field_1761.method_2906(syncId, bundleSlotId, 0, class_1713.field_7790, (class_1657)PlayerInventoryUtil.mc.field_1724);
        }
    }

    public static class_1735 extractBundleItemToHand(BundleResolution resolution) {
        if (resolution == null || PlayerInventoryUtil.mc.field_1724 == null || PlayerInventoryUtil.mc.field_1761 == null) {
            return null;
        }
        class_1799 bundleStack = resolution.container().method_7677();
        if (!(bundleStack.method_7909() instanceof class_5537)) {
            return null;
        }
        int syncId = PlayerInventoryUtil.mc.field_1724.field_7498.field_7763;
        int bundleSlotId = resolution.container().field_7874;
        int destSlotId = resolution.destination().field_7874;
        class_5537.method_61637((class_1799)bundleStack, (int)resolution.bundleIndex());
        PlayerInventoryUtil.mc.field_1724.field_3944.method_52787((class_2596)new class_9837(bundleSlotId, resolution.bundleIndex()));
        PlayerInventoryUtil.mc.field_1761.method_2906(syncId, bundleSlotId, 1, class_1713.field_7790, (class_1657)PlayerInventoryUtil.mc.field_1724);
        PlayerInventoryUtil.mc.field_1761.method_2906(syncId, destSlotId, 0, class_1713.field_7790, (class_1657)PlayerInventoryUtil.mc.field_1724);
        return resolution.destinationHasTarget() ? resolution.destination() : null;
    }

    public static void returnBundleItemFromHand(BundleResolution resolution) {
        if (resolution == null || PlayerInventoryUtil.mc.field_1724 == null || PlayerInventoryUtil.mc.field_1761 == null) {
            return;
        }
        class_1799 bundleStack = resolution.container().method_7677();
        if (!(bundleStack.method_7909() instanceof class_5537)) {
            return;
        }
        int syncId = PlayerInventoryUtil.mc.field_1724.field_7498.field_7763;
        int bundleSlotId = resolution.container().field_7874;
        int destSlotId = resolution.destination().field_7874;
        if (!PlayerInventoryUtil.mc.field_1724.field_7498.method_34255().method_7960()) {
            PlayerInventoryUtil.mc.field_1761.method_2906(syncId, destSlotId, 0, class_1713.field_7790, (class_1657)PlayerInventoryUtil.mc.field_1724);
        } else if (resolution.destination().method_7681()) {
            PlayerInventoryUtil.mc.field_1761.method_2906(syncId, destSlotId, 0, class_1713.field_7790, (class_1657)PlayerInventoryUtil.mc.field_1724);
        }
        if (!PlayerInventoryUtil.mc.field_1724.field_7498.method_34255().method_7960()) {
            PlayerInventoryUtil.mc.field_1761.method_2906(syncId, bundleSlotId, 0, class_1713.field_7790, (class_1657)PlayerInventoryUtil.mc.field_1724);
        }
    }

    private PlayerInventoryUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final class BundleResolution {
        private final class_1735 container;
        private final class_1735 destination;
        private final int bundleIndex;
        private final class_1799 targetStack;
        private final class_1799 displacedStack;

        private BundleResolution(class_1735 container, class_1735 destination, int bundleIndex, class_1799 targetStack, class_1799 displacedStack) {
            this.container = container;
            this.destination = destination;
            this.bundleIndex = bundleIndex;
            this.targetStack = targetStack;
            this.displacedStack = displacedStack;
        }

        public class_1735 container() {
            return this.container;
        }

        public class_1735 destination() {
            return this.destination;
        }

        public int bundleIndex() {
            return this.bundleIndex;
        }

        public class_1799 targetStack() {
            return this.targetStack;
        }

        public boolean occupiedBuffer() {
            return !this.displacedStack.method_7960();
        }

        private boolean containerHasBundle() {
            return this.container.method_7677().method_7909() instanceof class_5537;
        }

        private boolean destinationHasTarget() {
            class_1799 current = this.destination.method_7677();
            return !current.method_7960() && class_1799.method_31577((class_1799)current, (class_1799)this.targetStack);
        }

        public boolean isReturnComplete() {
            if (!this.occupiedBuffer()) {
                return !this.destination.method_7681();
            }
            class_1799 current = this.destination.method_7677();
            return !current.method_7960() && class_1799.method_31577((class_1799)current, (class_1799)this.displacedStack) && current.method_7947() == this.displacedStack.method_7947();
        }
    }

    private static final class BundleBackedSlot
    extends class_1735 {
        private final class_1735 container;
        private final int bundleIndex;
        private final class_1799 bundledStack;
        private class_1735 resolvedSlot;

        private BundleBackedSlot(class_1735 container, int bundleIndex, class_1799 bundledStack) {
            super(container.field_7871, container.method_34266(), container.field_7873, container.field_7872);
            this.container = container;
            this.bundleIndex = bundleIndex;
            this.bundledStack = bundledStack;
            this.field_7874 = container.field_7874;
        }

        public class_1799 method_7677() {
            return this.bundledStack;
        }

        public boolean method_7681() {
            return !this.bundledStack.method_7960();
        }
    }
}

