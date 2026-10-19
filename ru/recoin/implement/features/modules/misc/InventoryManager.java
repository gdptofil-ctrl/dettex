/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1657
 *  net.minecraft.class_1713
 *  net.minecraft.class_1735
 *  net.minecraft.class_1799
 *  net.minecraft.class_2561
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 *  net.minecraft.class_490
 */
package ru.recoin.implement.features.modules.misc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_490;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.ButtonSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.TextSetting;
import ru.recoin.common.QuickLogger;
import ru.recoin.core.Main;
import ru.recoin.implement.events.container.HandledScreenEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.modules.misc.AutoInventory;
import ru.recoin.implement.screens.inventorymanager.InventoryManagerScreen;

public final class InventoryManager
extends Module {
    public static final int INVENTORY_SIZE = 36;
    private static final int PRESET_COUNT = 3;
    private final AutoInventory catalogOwner;
    private final SelectSetting preset = new SelectSetting("\u0420\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0430", "\u0410\u043a\u0442\u0438\u0432\u043d\u0430\u044f \u0440\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0430").value("\u0420\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0430 1", "\u0420\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0430 2", "\u0420\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0430 3").selected("\u0420\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0430 1");
    private final TextSetting[] savedLayouts = new TextSetting[]{InventoryManager.hiddenLayout("InventoryLayout1"), InventoryManager.hiddenLayout("InventoryLayout2"), InventoryManager.hiddenLayout("InventoryLayout3")};
    private final ButtonSetting editor = new ButtonSetting("\u0420\u0435\u0434\u0430\u043a\u0442\u043e\u0440 \u0440\u0430\u0441\u043a\u043b\u0430\u0434\u043e\u043a", "\u0412\u0438\u0437\u0443\u0430\u043b\u044c\u043d\u043e \u043d\u0430\u0441\u0442\u0440\u043e\u0438\u0442\u044c \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044c").setButtonName("\u041e\u0442\u043a\u0440\u044b\u0442\u044c").setRunnable(this::openEditor);
    private final ButtonSetting apply = new ButtonSetting("\u0420\u0430\u0437\u043b\u043e\u0436\u0438\u0442\u044c \u0441\u0435\u0442", "\u041c\u043e\u043c\u0435\u043d\u0442\u0430\u043b\u044c\u043d\u043e \u043f\u0440\u0438\u043c\u0435\u043d\u0438\u0442\u044c \u0430\u043a\u0442\u0438\u0432\u043d\u0443\u044e \u0440\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0443").setButtonName("\u0420\u0430\u0437\u043b\u043e\u0436\u0438\u0442\u044c").setRunnable(this::applyActiveLayout);
    private transient List<LayoutSlot> applyingLayout = List.of();
    private final transient Set<Integer> fixedInventorySlots = new HashSet<Integer>();
    private transient int applyCursor;
    private transient int appliedMoves;
    private transient boolean applying;
    private transient boolean temporaryTickRegistration;

    public InventoryManager(AutoInventory catalogOwner) {
        super("InventoryManager", "Inventory Manager", ModuleCategory.MISC);
        this.catalogOwner = catalogOwner;
        this.setup(this.preset, this.editor, this.apply, this.savedLayouts[0], this.savedLayouts[1], this.savedLayouts[2]);
    }

    private static TextSetting hiddenLayout(String name) {
        return new TextSetting(name, "Hidden saved inventory layout").visible(() -> false).setText(InventoryManager.emptyLayout());
    }

    private static String emptyLayout() {
        return IntStream.range(0, 36).mapToObj(i -> "-").collect(Collectors.joining(","));
    }

    @Override
    public void activate() {
        this.openEditor();
    }

    @Override
    public void deactivate() {
        this.stopApplying();
    }

    public void openEditor() {
        if (InventoryManager.mc.field_1724 != null && InventoryManager.mc.field_1687 != null) {
            mc.method_1507((class_437)new InventoryManagerScreen(this));
        }
    }

    public AutoInventory catalogOwner() {
        return this.catalogOwner;
    }

    public int activePreset() {
        String value = this.preset.getSelected();
        if (value == null || value.isBlank()) {
            return 0;
        }
        char last = value.charAt(value.length() - 1);
        return Math.max(0, Math.min(2, last - 49));
    }

    public void selectPreset(int index) {
        int safe = Math.max(0, Math.min(2, index));
        this.preset.setSelected("\u0420\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0430 " + (safe + 1));
    }

    public List<LayoutSlot> readLayout() {
        return this.readLayout(this.activePreset());
    }

    public List<LayoutSlot> readLayout(int presetIndex) {
        ArrayList<LayoutSlot> result = new ArrayList<LayoutSlot>(36);
        String encoded = this.savedLayouts[Math.max(0, Math.min(2, presetIndex))].getText();
        String[] tokens = encoded == null ? new String[]{} : encoded.split(",", -1);
        for (int slot = 0; slot < 36; ++slot) {
            result.add(slot < tokens.length ? this.decode(tokens[slot]) : null);
        }
        return result;
    }

    public void saveLayout(List<LayoutSlot> layout) {
        ArrayList<String> encoded = new ArrayList<String>(36);
        for (int slot = 0; slot < 36; ++slot) {
            LayoutSlot value = slot < layout.size() ? layout.get(slot) : null;
            encoded.add(value == null ? "-" : value.catalogIndex + ":" + Math.max(1, value.count));
        }
        this.savedLayouts[this.activePreset()].setText(String.join(",", encoded));
    }

    private LayoutSlot decode(String token) {
        if (token == null || token.isBlank() || token.equals("-")) {
            return null;
        }
        try {
            int count;
            String[] parts = token.split(":", 2);
            int index = Integer.parseInt(parts[0]);
            int n = count = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
            if (index < 0 || index >= this.catalogOwner.catalog().size()) {
                return null;
            }
            return new LayoutSlot(index, Math.max(1, count));
        }
        catch (NumberFormatException ignored) {
            return null;
        }
    }

    public AutoInventory.Entry entry(LayoutSlot slot) {
        if (slot == null || slot.catalogIndex < 0 || slot.catalogIndex >= this.catalogOwner.catalog().size()) {
            return null;
        }
        return this.catalogOwner.catalog().get(slot.catalogIndex);
    }

    public void applyActiveLayout() {
        if (InventoryManager.mc.field_1724 == null || InventoryManager.mc.field_1761 == null) {
            return;
        }
        if (InventoryManager.mc.field_1724.field_7512 != InventoryManager.mc.field_1724.field_7498) {
            this.message("\u0417\u0430\u043a\u0440\u043e\u0439\u0442\u0435 \u0441\u0443\u043d\u0434\u0443\u043a \u0438\u043b\u0438 \u0434\u0440\u0443\u0433\u043e\u0435 \u0445\u0440\u0430\u043d\u0438\u043b\u0438\u0449\u0435");
            return;
        }
        if (!InventoryManager.mc.field_1724.field_7512.method_34255().method_7960()) {
            this.message("\u041e\u0441\u0432\u043e\u0431\u043e\u0434\u0438\u0442\u0435 \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u043d\u0430 \u043a\u0443\u0440\u0441\u043e\u0440\u0435");
            return;
        }
        this.applyingLayout = new ArrayList<LayoutSlot>(this.readLayout());
        this.fixedInventorySlots.clear();
        this.applyCursor = 0;
        this.appliedMoves = 0;
        this.applying = true;
        if (!this.isState() && !this.temporaryTickRegistration) {
            Main.getInstance().getEventManager().register(this, TickEvent.class);
            this.temporaryTickRegistration = true;
        }
        this.applyNextSlot();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (this.applying) {
            this.applyNextSlot();
        }
    }

    private void applyNextSlot() {
        if (!this.applying) {
            return;
        }
        if (InventoryManager.mc.field_1724 == null || InventoryManager.mc.field_1761 == null || InventoryManager.mc.field_1724.field_7512 != InventoryManager.mc.field_1724.field_7498) {
            this.stopApplying();
            this.message("\u0420\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0430 \u043e\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u0430: \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044c \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d");
            return;
        }
        if (!InventoryManager.mc.field_1724.field_7512.method_34255().method_7960()) {
            return;
        }
        while (this.applyCursor < 36) {
            class_1735 target;
            int inventoryIndex = this.applyCursor++;
            AutoInventory.Entry wanted = this.entry(this.applyingLayout.get(inventoryIndex));
            if (wanted == null || (target = this.playerSlot(inventoryIndex)) == null) continue;
            if (this.catalogOwner.matches(wanted, target.method_7677())) {
                this.fixedInventorySlots.add(inventoryIndex);
                continue;
            }
            class_1735 source = this.findSource(wanted, inventoryIndex, this.fixedInventorySlots);
            if (source == null) continue;
            this.swap(source, target);
            if (this.catalogOwner.matches(wanted, target.method_7677())) {
                this.fixedInventorySlots.add(inventoryIndex);
                ++this.appliedMoves;
            }
            return;
        }
        this.stopApplying();
        this.message((String)(this.appliedMoves == 0 ? "\u0420\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0430 \u0443\u0436\u0435 \u043f\u0440\u0438\u043c\u0435\u043d\u0435\u043d\u0430 \u0438\u043b\u0438 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432 \u043d\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442" : "\u0420\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0430 \u043f\u0440\u0438\u043c\u0435\u043d\u0435\u043d\u0430, \u043f\u0435\u0440\u0435\u043c\u0435\u0449\u0435\u043d\u043e: " + this.appliedMoves));
    }

    private void stopApplying() {
        this.applying = false;
        this.applyingLayout = List.of();
        this.fixedInventorySlots.clear();
        if (this.temporaryTickRegistration) {
            Main.getInstance().getEventManager().unregister(this, TickEvent.class);
            this.temporaryTickRegistration = false;
        }
    }

    private class_1735 findSource(AutoInventory.Entry wanted, int targetIndex, Set<Integer> fixed) {
        for (class_1735 slot : InventoryManager.mc.field_1724.field_7498.field_7761) {
            if (slot.field_7871 != InventoryManager.mc.field_1724.method_31548() || slot.method_34266() < 0 || slot.method_34266() >= 36 || slot.method_34266() == targetIndex || fixed.contains(slot.method_34266()) || !this.catalogOwner.matches(wanted, slot.method_7677())) continue;
            return slot;
        }
        return null;
    }

    private class_1735 playerSlot(int inventoryIndex) {
        for (class_1735 slot : InventoryManager.mc.field_1724.field_7498.field_7761) {
            if (slot.field_7871 != InventoryManager.mc.field_1724.method_31548() || slot.method_34266() != inventoryIndex) continue;
            return slot;
        }
        return null;
    }

    private void swap(class_1735 source, class_1735 target) {
        int syncId = InventoryManager.mc.field_1724.field_7498.field_7763;
        InventoryManager.mc.field_1761.method_2906(syncId, source.field_7874, 0, class_1713.field_7790, (class_1657)InventoryManager.mc.field_1724);
        InventoryManager.mc.field_1761.method_2906(syncId, target.field_7874, 0, class_1713.field_7790, (class_1657)InventoryManager.mc.field_1724);
        InventoryManager.mc.field_1761.method_2906(syncId, source.field_7874, 0, class_1713.field_7790, (class_1657)InventoryManager.mc.field_1724);
    }

    public Map<AutoInventory.Entry, Missing> missingItems() {
        LinkedHashMap<AutoInventory.Entry, Integer> required = new LinkedHashMap<AutoInventory.Entry, Integer>();
        for (LayoutSlot slot : this.readLayout()) {
            AutoInventory.Entry entry2 = this.entry(slot);
            if (entry2 == null) continue;
            required.merge(entry2, slot.count, Integer::sum);
        }
        LinkedHashMap<AutoInventory.Entry, Missing> result = new LinkedHashMap<AutoInventory.Entry, Missing>();
        required.forEach((entry, count) -> {
            int have = this.catalogOwner.countInInventory((AutoInventory.Entry)entry);
            if (have < count) {
                result.put((AutoInventory.Entry)entry, new Missing(have, (int)count));
            }
        });
        return result;
    }

    @EventHandler
    public void onInventoryRender(HandledScreenEvent event) {
        class_437 class_4372 = InventoryManager.mc.field_1755;
        if (!(class_4372 instanceof class_490)) {
            return;
        }
        class_490 screen = (class_490)class_4372;
        Map<AutoInventory.Entry, Missing> missing = this.missingItems();
        class_332 context = event.getDrawContext();
        int panelX = (screen.field_22789 + event.getBackgroundWidth()) / 2 + 7;
        int panelY = (screen.field_22790 - event.getBackgroundHeight()) / 2;
        int shown = Math.min(10, missing.size());
        int panelHeight = 31 + shown * 20 + (missing.size() > shown ? 14 : 0);
        context.method_25294(panelX, panelY, panelX + 166, panelY + panelHeight, -401994990);
        context.method_25294(panelX, panelY, panelX + 166, panelY + 1, -8890369);
        context.method_25303(InventoryManager.mc.field_1772, missing.isEmpty() ? "\u0421\u0435\u0442 \u0441\u043e\u0431\u0440\u0430\u043d" : "\u041d\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u0434\u043b\u044f \u0441\u0435\u0442\u0430", panelX + 8, panelY + 9, missing.isEmpty() ? -10364538 : -38795);
        int rowY = panelY + 27;
        int index = 0;
        for (Map.Entry<AutoInventory.Entry, Missing> row : missing.entrySet()) {
            if (index++ >= shown) break;
            class_1799 icon = this.catalogOwner.iconStack(row.getKey());
            context.method_51427(icon, panelX + 6, rowY - 3);
            String name = this.trim(row.getKey().name(), 17);
            context.method_25303(InventoryManager.mc.field_1772, name, panelX + 27, rowY, -1446923);
            Missing amount = row.getValue();
            context.method_25303(InventoryManager.mc.field_1772, amount.have + "/" + amount.required, panelX + 128, rowY, -38795);
            rowY += 20;
        }
        if (missing.size() > shown) {
            context.method_25303(InventoryManager.mc.field_1772, "+ \u0435\u0449\u0451 " + (missing.size() - shown), panelX + 8, rowY - 1, -6512206);
        }
    }

    private String trim(String value, int max) {
        return value.length() <= max ? value : value.substring(0, Math.max(1, max - 1)) + "\u2026";
    }

    private void message(String value) {
        if (InventoryManager.mc.field_1724 != null) {
            InventoryManager.mc.field_1724.method_7353(QuickLogger.clientMessage((class_2561)class_2561.method_43470((String)("\u00a75[InventoryManager] \u00a7f" + value))), false);
        }
    }

    public record LayoutSlot(int catalogIndex, int count) {
    }

    public record Missing(int have, int required) {
    }
}

