/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1657
 *  net.minecraft.class_1735
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_3675$class_307
 *  net.minecraft.class_4174
 *  net.minecraft.class_437
 *  net.minecraft.class_4587
 *  net.minecraft.class_490
 *  net.minecraft.class_7923
 *  net.minecraft.class_9334
 */
package ru.recoin.implement.features.modules.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3675;
import net.minecraft.class_4174;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import net.minecraft.class_490;
import net.minecraft.class_7923;
import net.minecraft.class_9334;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BindSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.TextSetting;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.entity.PlayerInventoryComponent;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.pipeline.DirectBlurPipeline;
import ru.recoin.implement.events.item.ClickSlotEvent;
import ru.recoin.implement.events.keyboard.KeyEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.DrawEvent;
import ru.recoin.implement.features.draggables.Notifications;
import ru.recoin.implement.features.modules.combat.AutoTotem;
import ru.recoin.implement.features.modules.render.Hud;

public class AutoSwap
extends Module {
    private final DirectBlurPipeline wheelBlur = new DirectBlurPipeline();
    private final SelectSetting mode = new SelectSetting("Mode", "Select auto swap mode").value("Default", "Triple").selected("Default");
    private final BindSetting bind = new BindSetting("Item use key", "Uses item when pressed");
    private final SelectSetting firstItem = new SelectSetting("First item", "Select first swap item.").value("Shield", "Golden Apple", "Player Head", "Firework Rocket", "Totem of Undying", "Food");
    private final SelectSetting secondItem = new SelectSetting("Second item", "Select second swap item.").value("Shield", "Golden Apple", "Player Head", "Firework Rocket", "Totem of Undying", "Food");
    private final TextSetting wheelSlotOne = new TextSetting("WheelSlotOne", "Hidden wheel slot").visible(() -> false).setText("");
    private final TextSetting wheelSlotTwo = new TextSetting("WheelSlotTwo", "Hidden wheel slot").visible(() -> false).setText("");
    private final TextSetting wheelSlotThree = new TextSetting("WheelSlotThree", "Hidden wheel slot").visible(() -> false).setText("");
    private List<WheelEntry> wheelEntries = List.of();
    private final Map<String, class_1799> wheelDisplayCache = new HashMap<String, class_1799>();
    private boolean wheelOpen;
    private boolean cursorUnlocked;
    private boolean wheelBindHeld;
    private boolean swapPending;
    private int pendingWheelSlot = -1;
    private boolean pickerOpenPending;
    private class_1799 pendingNotificationStack = class_1799.field_8037;
    private String observedOffhandSignature = "";
    private long pendingNotificationUntil;

    public AutoSwap() {
        super("AutoSwap", "Auto Swap", ModuleCategory.COMBAT);
        this.firstItem.visible(() -> this.mode.isSelected("Default"));
        this.secondItem.visible(() -> this.mode.isSelected("Default"));
        this.setup(this.mode, this.firstItem, this.secondItem, this.bind, this.wheelSlotOne, this.wheelSlotTwo, this.wheelSlotThree);
    }

    @Override
    public void deactivate() {
        this.wheelBindHeld = false;
        this.wheelOpen = false;
        this.swapPending = false;
        this.pendingWheelSlot = -1;
        this.pickerOpenPending = false;
        this.updateCursorState(false);
        this.pendingNotificationStack = class_1799.field_8037;
        this.pendingNotificationUntil = 0L;
        super.deactivate();
    }

    @Override
    public void activate() {
        this.observedOffhandSignature = AutoSwap.mc.field_1724 == null ? "" : this.stackKey(AutoSwap.mc.field_1724.method_6079());
        super.activate();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        String current;
        if (AutoSwap.mc.field_1724 == null) {
            return;
        }
        if (this.pickerOpenPending && AutoSwap.mc.field_1755 == null) {
            this.pickerOpenPending = false;
            mc.method_1507((class_437)new class_490((class_1657)AutoSwap.mc.field_1724));
        }
        boolean changed = !(current = this.stackKey(AutoSwap.mc.field_1724.method_6079())).equals(this.observedOffhandSignature);
        this.observedOffhandSignature = current;
        if (this.pendingNotificationUntil > 0L && System.currentTimeMillis() > this.pendingNotificationUntil) {
            this.pendingNotificationStack = class_1799.field_8037;
            this.pendingNotificationUntil = 0L;
        }
        if (changed && !this.pendingNotificationStack.method_7960() && current.equals(this.stackKey(this.pendingNotificationStack))) {
            this.notifySwap(this.pendingNotificationStack);
            this.pendingNotificationStack = class_1799.field_8037;
            this.pendingNotificationUntil = 0L;
        }
    }

    @EventHandler
    public void onKey(KeyEvent e) {
        if (e.key() != this.bind.getKey() || AutoSwap.mc.field_1755 != null) {
            return;
        }
        if (!this.mode.isSelected("Triple")) {
            if (e.isKeyDown(this.bind.getKey(), true)) {
                this.performDefaultSwap();
            }
            return;
        }
        if (e.action() == 1) {
            this.wheelBindHeld = true;
            this.openWheel();
            return;
        }
        if (e.action() == 0) {
            if (this.wheelOpen && this.wheelBindHeld) {
                this.pickWheelSlot();
            } else {
                this.closeWheel();
            }
            this.wheelBindHeld = false;
        }
    }

    @EventHandler
    public void onDraw(DrawEvent e) {
        if (!this.wheelOpen) {
            return;
        }
        if (AutoSwap.mc.field_1755 != null) {
            this.closeWheel();
            this.wheelBindHeld = false;
            return;
        }
        this.updateCursorState(true);
        int count = 3;
        float centerX = (float)mc.method_22683().method_4486() / 2.0f;
        float centerY = (float)mc.method_22683().method_4502() / 2.0f;
        float mouseX = this.getScaledMouseX();
        float mouseY = this.getScaledMouseY();
        float innerRadius = 56.0f;
        float outerRadius = 86.0f;
        int hovered = this.getHoverIndex(mouseX, mouseY, centerX, centerY, 44.0f, 110.0f, count);
        class_4587 matrixStack = MatrixStackCompat.from(e.getDrawContext().method_51448());
        this.wheelBlur.beginFrame();
        this.wheelBlur.drawBlur(centerX - outerRadius, centerY - outerRadius, outerRadius * 2.0f, outerRadius * 2.0f, 5.0f, new float[]{-innerRadius, outerRadius, 8.0f, 0.0f}, -268435456);
        this.drawWheelRing(matrixStack, centerX, centerY, innerRadius, outerRadius, hovered, count);
        for (int i = 0; i < count; ++i) {
            if (i >= this.wheelEntries.size()) continue;
            float[] slot = this.getWheelSlotCenter(i, centerX, centerY);
            float itemX = slot[0];
            float itemY = slot[1];
            WheelEntry entry = this.wheelEntries.get(i);
            if (!entry.displayStack.method_7960()) {
                e.getDrawContext().method_51428(entry.displayStack, Math.round(itemX - 8.0f), Math.round(itemY - 8.0f), 200);
            } else {
                Fonts.getSize(15, Fonts.Type.INTER_BOLD).drawCenteredString(matrixStack, "+", itemX, itemY - 5.0f, -6381922);
            }
            if (entry.totalCount <= 1 || entry.displayStack.method_7960()) continue;
            Fonts.getSize(11, Fonts.Type.INTER_BOLD).drawString(matrixStack, String.valueOf(entry.totalCount), itemX + 5.0f, itemY + 3.0f, -1);
        }
        e.getDrawContext().method_73199();
        if (hovered >= 0 && hovered < this.wheelEntries.size() && !this.wheelEntries.get((int)hovered).displayStack.method_7960()) {
            WheelEntry entry = this.wheelEntries.get(hovered);
            Fonts.getSize(13, Fonts.Type.INTER_BOLD).drawString(matrixStack, entry.displayStack.method_7964().getString(), mouseX + 10.0f, mouseY + 10.0f, -1);
        }
        String guide = "\u041b\u041a\u041c \u2014 \u0441\u0432\u0430\u043f\u043d\u0443\u0442\u044c   \u2022   \u041f\u041a\u041c \u2014 \u043d\u0430\u0441\u0442\u0440\u043e\u0438\u0442\u044c   \u2022   \u0421\u041a\u041c \u2014 \u043e\u0447\u0438\u0441\u0442\u0438\u0442\u044c";
        FontRenderer guideFont = Fonts.getSize(12, Fonts.Type.INTER_BOLD);
        guideFont.drawCenteredString(matrixStack, guide, centerX, centerY + 103.0f, -2630424);
    }

    @EventHandler
    public void onMouse(KeyEvent e) {
        if (!this.wheelOpen || e.type() != class_3675.class_307.field_1672 || AutoSwap.mc.field_1755 != null) {
            return;
        }
        if (e.action() == 1) {
            if (e.key() == 1) {
                this.openWheelItemPicker();
            } else if (e.key() == 0) {
                this.pickWheelSlot();
                this.wheelBindHeld = false;
            } else if (e.key() == 2) {
                this.clearHoveredWheelSlot();
            }
        }
    }

    private void clearHoveredWheelSlot() {
        int hovered = this.getHoveredWheelIndex();
        if (hovered >= 0 && hovered < 3) {
            this.setWheelSlotSignature(hovered, "");
            this.wheelEntries = this.buildWheelEntries();
        }
    }

    @EventHandler
    public void onClickSlot(ClickSlotEvent e) {
        if (this.pendingWheelSlot < 0 || !(AutoSwap.mc.field_1755 instanceof class_490) || AutoSwap.mc.field_1724 == null || e.getSlotId() < 0) {
            return;
        }
        class_1735 slot = AutoSwap.mc.field_1724.field_7512.method_7611(e.getSlotId());
        if (slot == null || slot.method_7677().method_7960()) {
            this.setWheelSlotSignature(this.pendingWheelSlot, "");
            this.pendingWheelSlot = -1;
            this.wheelEntries = this.buildWheelEntries();
            e.cancel();
            mc.method_1507(null);
            return;
        }
        String signature = this.stackKey(slot.method_7677());
        this.wheelDisplayCache.put(signature, slot.method_7677().method_46651(1));
        this.setWheelSlotSignature(this.pendingWheelSlot, signature);
        this.pendingWheelSlot = -1;
        this.wheelEntries = this.buildWheelEntries();
        e.cancel();
        mc.method_1507(null);
    }

    private void performDefaultSwap() {
        class_1735 validSlot;
        AutoTotem autoTotem = AutoTotem.getInstance();
        if (autoTotem != null && autoTotem.isHoldingTotem()) {
            return;
        }
        Predicate<class_1799> firstMatcher = this.getItemMatcher(this.firstItem.getSelected());
        Predicate<class_1799> secondMatcher = this.getItemMatcher(this.secondItem.getSelected());
        class_1735 class_17352 = validSlot = firstMatcher.test(AutoSwap.mc.field_1724.method_6079()) ? this.getBestSlotByMatcher(secondMatcher) : this.getBestSlotByMatcher(firstMatcher);
        if (validSlot != null) {
            class_1799 swappedStack = validSlot.method_7677().method_46651(1);
            this.swapToOffhand(validSlot);
            this.awaitSwapNotification(swappedStack);
        }
    }

    private void swapToOffhand(class_1735 slot) {
        if (slot == null || this.swapPending) {
            return;
        }
        this.swapPending = true;
        PlayerInventoryComponent.addFunTimeTask(() -> {
            try {
                if (AutoSwap.mc.field_1724 != null && AutoSwap.mc.field_1724.field_7512.method_34255().method_7960()) {
                    PlayerInventoryUtil.swapHand(slot, class_1268.field_5810, false, true);
                }
            }
            finally {
                this.swapPending = false;
            }
        });
    }

    private void openWheel() {
        this.wheelEntries = this.buildWheelEntries();
        this.wheelOpen = true;
        this.updateCursorState(true);
    }

    private void closeWheel() {
        this.wheelOpen = false;
        this.updateCursorState(false);
    }

    private void updateCursorState(boolean shouldUnlock) {
        if (AutoSwap.mc.field_1729 == null) {
            return;
        }
        if (shouldUnlock) {
            if (!this.cursorUnlocked) {
                AutoSwap.mc.field_1729.method_1610();
                this.cursorUnlocked = true;
            }
            return;
        }
        if (this.cursorUnlocked) {
            if (AutoSwap.mc.field_1755 == null) {
                AutoSwap.mc.field_1729.method_1612();
            }
            this.cursorUnlocked = false;
        }
    }

    private void pickWheelSlot() {
        AutoTotem autoTotem = AutoTotem.getInstance();
        if (autoTotem != null && autoTotem.isHoldingTotem()) {
            this.closeWheel();
            return;
        }
        int count = 3;
        float centerX = (float)mc.method_22683().method_4486() / 2.0f;
        float centerY = (float)mc.method_22683().method_4502() / 2.0f;
        int hovered = this.getHoverIndex(this.getScaledMouseX(), this.getScaledMouseY(), centerX, centerY, 44.0f, 110.0f, count);
        if (hovered < 0 || hovered >= this.wheelEntries.size()) {
            this.closeWheel();
            return;
        }
        WheelEntry entry = this.wheelEntries.get(hovered);
        class_1735 slot = this.getBestSlot(entry.signature);
        if (!entry.signature.isBlank() && slot != null) {
            class_1799 swappedStack = slot.method_7677().method_46651(1);
            this.swapToOffhand(slot);
            this.awaitSwapNotification(swappedStack);
        }
        this.closeWheel();
    }

    private void notifySwap(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return;
        }
        Notifications.getInstance().addList((class_2561)class_2561.method_43470((String)"\u0421\u0432\u0430\u043f\u043d\u0443\u043b \u043d\u0430 ").method_10852(stack.method_7964()), 2500L, stack);
    }

    private void awaitSwapNotification(class_1799 stack) {
        this.pendingNotificationStack = stack.method_46651(1);
        this.pendingNotificationUntil = System.currentTimeMillis() + 1800L;
    }

    private List<WheelEntry> buildWheelEntries() {
        ArrayList<WheelEntry> entries = new ArrayList<WheelEntry>();
        entries.add(this.createWheelEntry(this.wheelSlotOne.getText()));
        entries.add(this.createWheelEntry(this.wheelSlotTwo.getText()));
        entries.add(this.createWheelEntry(this.wheelSlotThree.getText()));
        return entries;
    }

    private WheelEntry createWheelEntry(String signature) {
        class_1799 offhand;
        if (signature == null || signature.isBlank()) {
            return new WheelEntry(class_1799.field_8037, 0, "");
        }
        class_1735 slot = this.getBestSlot(signature);
        class_1799 class_17992 = offhand = AutoSwap.mc.field_1724 != null ? AutoSwap.mc.field_1724.method_6079() : class_1799.field_8037;
        if (slot == null && !this.stackKey(offhand).equals(signature)) {
            class_1799 remembered = this.wheelDisplayCache.getOrDefault(signature, this.fallbackDisplayStack(signature));
            return new WheelEntry(remembered.method_46651(1), 0, signature);
        }
        class_1799 displayStack = slot != null ? slot.method_7677().method_46651(1) : offhand.method_46651(1);
        this.wheelDisplayCache.put(signature, displayStack.method_7972());
        int totalCount = this.getMatchingCount(signature);
        return new WheelEntry(displayStack, totalCount, signature);
    }

    private class_1799 fallbackDisplayStack(String signature) {
        int separator = signature.indexOf(124);
        String itemId = separator >= 0 ? signature.substring(0, separator) : signature;
        class_2960 id = class_2960.method_12829((String)itemId);
        if (id == null || !class_7923.field_41178.method_10250(id)) {
            return class_1799.field_8037;
        }
        return ((class_1792)class_7923.field_41178.method_63535(id)).method_7854();
    }

    private class_1735 getBestSlot(String signature) {
        return PlayerInventoryUtil.getSlot(s -> !PlayerInventoryUtil.isBundleBackedSlot(s) && this.stackKey(s.method_7677()).equals(signature) && s.field_7874 != 46 && s.field_7874 != 45, Comparator.comparing(slot -> slot.method_7677().method_7942()));
    }

    private class_1735 getBestSlotByMatcher(Predicate<class_1799> matcher) {
        return PlayerInventoryUtil.getSlot((class_1735 slot) -> !PlayerInventoryUtil.isBundleBackedSlot(slot) && matcher.test(slot.method_7677()) && slot.field_7874 != 46 && slot.field_7874 != 45, Comparator.comparingInt((class_1735 slot) -> this.foodValue(slot.method_7677())).thenComparing((class_1735 slot) -> slot.method_7677().method_7942()));
    }

    private void openWheelItemPicker() {
        int hovered = this.getHoveredWheelIndex();
        if (hovered < 0 || hovered >= 3) {
            return;
        }
        this.pendingWheelSlot = hovered;
        this.closeWheel();
        this.pickerOpenPending = true;
    }

    private int getHoveredWheelIndex() {
        int count = 3;
        float centerX = (float)mc.method_22683().method_4486() / 2.0f;
        float centerY = (float)mc.method_22683().method_4502() / 2.0f;
        return this.getHoverIndex(this.getScaledMouseX(), this.getScaledMouseY(), centerX, centerY, 44.0f, 110.0f, count);
    }

    private float[] getWheelSlotCenter(int index, float centerX, float centerY) {
        float radius = 72.0f;
        float angle = this.getSegmentCenterAngle(index, 3);
        return new float[]{centerX + (float)Math.cos(angle) * radius, centerY + (float)Math.sin(angle) * radius};
    }

    private void setWheelSlotSignature(int index, String signature) {
        switch (index) {
            case 0: {
                this.wheelSlotOne.setText(signature);
                break;
            }
            case 1: {
                this.wheelSlotTwo.setText(signature);
                break;
            }
            case 2: {
                this.wheelSlotThree.setText(signature);
                break;
            }
        }
    }

    private int getMatchingCount(String signature) {
        int count = PlayerInventoryUtil.getCount(slot -> !PlayerInventoryUtil.isBundleBackedSlot(slot) && this.stackKey(slot.method_7677()).equals(signature) && slot.field_7874 != 45);
        if (this.stackKey(AutoSwap.mc.field_1724.method_6079()).equals(signature)) {
            count += AutoSwap.mc.field_1724.method_6079().method_7947();
        }
        return count;
    }

    private String stackKey(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return "";
        }
        class_1799 copy = stack.method_46651(1);
        return class_7923.field_41178.method_10221(copy.method_7909()).toString() + "|" + copy.method_57380().toString();
    }

    private float getScaledMouseX() {
        return (float)(AutoSwap.mc.field_1729.method_1603() * (double)mc.method_22683().method_4486() / (double)mc.method_22683().method_4480());
    }

    private float getScaledMouseY() {
        return (float)(AutoSwap.mc.field_1729.method_1604() * (double)mc.method_22683().method_4502() / (double)mc.method_22683().method_4507());
    }

    private int getHoverIndex(float mouseX, float mouseY, float centerX, float centerY, float innerRadius, float outerRadius, int count) {
        float angle;
        float dx = mouseX - centerX;
        float dy = mouseY - centerY;
        float distanceFromCenter = (float)Math.sqrt(dx * dx + dy * dy);
        if (distanceFromCenter < 18.0f) {
            return -1;
        }
        float segment = (float)Math.PI * 2 / (float)count;
        float start = -1.5707964f - segment / 2.0f;
        for (angle = (float)Math.atan2(dy, dx); angle < start; angle += (float)Math.PI * 2) {
        }
        int index = (int)Math.floor((angle - start) / segment);
        return Math.floorMod(index, count);
    }

    private void drawWheelRing(class_4587 matrixStack, float centerX, float centerY, float innerRadius, float outerRadius, int hovered, int count) {
        float segment = (float)Math.PI * 2 / (float)count;
        float gap = 3.4f;
        float size = outerRadius * 2.0f;
        float thickness = outerRadius - innerRadius;
        for (int i = 0; i < count; ++i) {
            float center = (float)Math.toDegrees(this.getSegmentCenterAngle(i, count)) + 90.0f;
            float start = center - (float)Math.toDegrees(segment) / 2.0f + gap;
            float end = center + (float)Math.toDegrees(segment) / 2.0f - gap;
            boolean active = i == hovered;
            WheelEntry entry = i < this.wheelEntries.size() ? this.wheelEntries.get(i) : null;
            boolean missing = entry != null && !entry.signature.isBlank() && entry.totalCount <= 0;
            arc.render(ShapeProperties.create(matrixStack, centerX - outerRadius, centerY - outerRadius, size, size).thickness(thickness).start(start).end(end).color(active ? -1694432760 : -2046754296).build());
            int outlineColor = active ? Hud.getInstance().themeOutlineColor() : 0x1FFFFFFF;
            arc.render(ShapeProperties.create(matrixStack, centerX - outerRadius, centerY - outerRadius, size, size).thickness(thickness).start(start).end(end).color(missing ? (active ? -385918620 : -1176941739) : outlineColor).build());
        }
    }

    private float getSegmentCenterAngle(int index, int count) {
        float segment = (float)Math.PI * 2 / (float)count;
        return -1.5707964f + (float)index * segment;
    }

    private Predicate<class_1799> getItemMatcher(String itemType) {
        return switch (itemType) {
            case "Shield" -> stack -> stack.method_31574(class_1802.field_8255);
            case "Golden Apple" -> stack -> stack.method_31574(class_1802.field_8463) || stack.method_31574(class_1802.field_8367);
            case "Player Head" -> stack -> stack.method_31574(class_1802.field_8575);
            case "Firework Rocket" -> stack -> stack.method_31574(class_1802.field_8639);
            case "Totem of Undying" -> stack -> stack.method_31574(class_1802.field_8288);
            case "Food" -> stack -> stack.method_58694(class_9334.field_50075) != null;
            default -> stack -> false;
        };
    }

    private int foodValue(class_1799 stack) {
        class_4174 food = (class_4174)stack.method_58694(class_9334.field_50075);
        return food == null ? 0 : Math.round((float)food.comp_2491() * food.comp_2492());
    }

    public DirectBlurPipeline getWheelBlur() {
        return this.wheelBlur;
    }

    public SelectSetting getMode() {
        return this.mode;
    }

    public BindSetting getBind() {
        return this.bind;
    }

    public SelectSetting getFirstItem() {
        return this.firstItem;
    }

    public SelectSetting getSecondItem() {
        return this.secondItem;
    }

    public TextSetting getWheelSlotOne() {
        return this.wheelSlotOne;
    }

    public TextSetting getWheelSlotTwo() {
        return this.wheelSlotTwo;
    }

    public TextSetting getWheelSlotThree() {
        return this.wheelSlotThree;
    }

    public List<WheelEntry> getWheelEntries() {
        return this.wheelEntries;
    }

    public Map<String, class_1799> getWheelDisplayCache() {
        return this.wheelDisplayCache;
    }

    public boolean isWheelOpen() {
        return this.wheelOpen;
    }

    public boolean isCursorUnlocked() {
        return this.cursorUnlocked;
    }

    public boolean isWheelBindHeld() {
        return this.wheelBindHeld;
    }

    public int getPendingWheelSlot() {
        return this.pendingWheelSlot;
    }

    public boolean isPickerOpenPending() {
        return this.pickerOpenPending;
    }

    public class_1799 getPendingNotificationStack() {
        return this.pendingNotificationStack;
    }

    public String getObservedOffhandSignature() {
        return this.observedOffhandSignature;
    }

    public long getPendingNotificationUntil() {
        return this.pendingNotificationUntil;
    }

    private record WheelEntry(class_1799 displayStack, int totalCount, String signature) {
    }
}

