package ru.recoin.implement.features.modules.misc.minigames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_7923;
import net.minecraft.class_9334;

/**
 * Upgrader catalogue: virtual items with a price in virtual coins. The prices are made up for the
 * game (sword -> talisman of the Crusher set is exactly x8); nothing here is a real server item.
 */
public final class UpgradeItems {
    public record Item(String id, String name, String icon, long price, boolean glint, int color) {
    }

    public static final List<Item> ALL = new ArrayList<>();
    private static final Map<String, Item> BY_ID = new HashMap<>();
    private static final Map<String, class_1799> STACKS = new HashMap<>();

    private static final int COMMON = 0xFFB0B8C4;
    private static final int RARE = 0xFF4B8BFF;
    private static final int EPIC = 0xFFB15CFF;
    private static final int LEGEND = 0xFFFFB02E;
    private static final int MYTHIC = 0xFFFF4D5E;

    private static void add(String id, String name, String icon, long price, boolean glint, int color) {
        Item item = new Item(id, name, icon, price, glint, color);
        ALL.add(item);
        BY_ID.put(id, item);
    }

    static {
        add("iron_ingot", "Железный слиток", "minecraft:iron_ingot", 10, false, COMMON);
        add("gold_ingot", "Золотой слиток", "minecraft:gold_ingot", 15, false, COMMON);
        add("emerald", "Изумруд", "minecraft:emerald", 25, false, COMMON);
        add("ender_pearl", "Эндер-жемчуг", "minecraft:ender_pearl", 30, false, COMMON);
        add("diamond", "Алмаз", "minecraft:diamond", 50, false, RARE);
        add("golden_apple", "Золотое яблоко", "minecraft:golden_apple", 80, false, RARE);
        add("netherite_scrap", "Незеритовый обломок", "minecraft:netherite_scrap", 200, false, RARE);
        add("totem", "Тотем бессмертия", "minecraft:totem_of_undying", 400, false, EPIC);
        add("netherite_ingot", "Незеритовый слиток", "minecraft:netherite_ingot", 600, false, EPIC);
        add("god_apple", "Зачарованное яблоко", "minecraft:enchanted_golden_apple", 900, false, EPIC);
        add("crusher_sword", "Меч Крушителя", "minecraft:netherite_sword", 1000, true, LEGEND);
        add("crusher_pickaxe", "Кирка Крушителя", "minecraft:netherite_pickaxe", 1200, true, LEGEND);
        add("crusher_boots", "Ботинки Крушителя", "minecraft:netherite_boots", 1400, true, LEGEND);
        add("elytra", "Элитры", "minecraft:elytra", 1500, false, LEGEND);
        add("crusher_helmet", "Шлем Крушителя", "minecraft:netherite_helmet", 1500, true, LEGEND);
        add("nether_star", "Звезда Незера", "minecraft:nether_star", 2000, false, LEGEND);
        add("crusher_leggings", "Поножи Крушителя", "minecraft:netherite_leggings", 2100, true, LEGEND);
        add("crusher_chestplate", "Нагрудник Крушителя", "minecraft:netherite_chestplate", 2400, true, LEGEND);
        add("beacon", "Маяк", "minecraft:beacon", 2500, false, LEGEND);
        add("dragon_head", "Голова дракона", "minecraft:dragon_head", 3000, false, MYTHIC);
        add("heavy_core", "Тяжёлое ядро", "minecraft:heavy_core", 3500, false, MYTHIC);
        add("crusher_mace", "Булава Крушителя", "minecraft:mace", 5000, true, MYTHIC);
        add("crusher_talisman", "Талисман Крушителя", "minecraft:totem_of_undying", 8000, true, MYTHIC);
        add("dragon_egg", "Яйцо дракона", "minecraft:dragon_egg", 10000, false, MYTHIC);
        add("crusher_set", "Сет Крушителя (сундук)", "minecraft:ender_chest", 25000, true, MYTHIC);
    }

    private UpgradeItems() {
    }

    public static Item get(String id) {
        return BY_ID.get(id);
    }

    /** The vanilla item used as the icon (with the enchant glint for Crusher items). */
    public static class_1799 stack(Item item) {
        return STACKS.computeIfAbsent(item.id(), k -> {
            try {
                class_1792 it = (class_1792)class_7923.field_41178.method_63535(class_2960.method_60654(item.icon()));
                class_1799 s = it.method_7854();
                if (item.glint()) {
                    s.method_57379(class_9334.field_49641, Boolean.TRUE);
                }
                return s;
            } catch (Throwable ignored) {
                return class_1799.field_8037;
            }
        });
    }
}
