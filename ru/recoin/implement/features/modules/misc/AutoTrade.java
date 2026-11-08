/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1297
 *  net.minecraft.class_1646
 *  net.minecraft.class_1657
 *  net.minecraft.class_1703
 *  net.minecraft.class_1707
 *  net.minecraft.class_1713
 *  net.minecraft.class_1728
 *  net.minecraft.class_1735
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1914
 *  net.minecraft.class_1916
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_2382
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_2595
 *  net.minecraft.class_2596
 *  net.minecraft.class_2863
 *  net.minecraft.class_3532
 *  net.minecraft.class_3719
 *  net.minecraft.class_3965
 *  net.minecraft.class_476
 *  net.minecraft.class_492
 */
package ru.recoin.implement.features.modules.misc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1646;
import net.minecraft.class_1657;
import net.minecraft.class_1703;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1728;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1914;
import net.minecraft.class_1916;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2595;
import net.minecraft.class_2596;
import net.minecraft.class_2863;
import net.minecraft.class_3532;
import net.minecraft.class_3719;
import net.minecraft.class_3965;
import net.minecraft.class_476;
import net.minecraft.class_492;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.ButtonSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.QuickLogger;
import ru.recoin.common.util.other.BaritoneCompat;
import ru.recoin.implement.events.container.SetScreenEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.screens.menu.MenuDescriptionLocalizer;

public final class AutoTrade
extends Module {
    public final SelectSetting buyItem = new SelectSetting("What To Buy", "Villager output").value("Gold Ingot", "Redstone", "Lapis Lazuli", "Ender Pearl", "Experience Bottle", "Glass", "Name Tag", "Arrows", "Bread", "Golden Carrot", "Quartz Block", "Saddle").selected("Gold Ingot");
    public final ValueSetting maxPrice = this.value("Max Price", 64.0f, 1.0f, 64.0f, 1.0f);
    public final ValueSetting emeraldReserve = this.value("Emerald Reserve", 64.0f, 0.0f, 2304.0f, 64.0f);
    public final ValueSetting villagerRadius = this.value("Villager Radius", 4.0f, 2.0f, 8.0f, 0.5f);
    public final ValueSetting delayMs = this.value("Delay (ms)", 120.0f, 50.0f, 1000.0f, 10.0f);
    public final ValueSetting rescanCooldown = this.value("Rescan Cooldown (sec)", 45.0f, 5.0f, 300.0f, 5.0f);
    public final BooleanSetting autoEmeralds = this.bool("Auto Emeralds", true);
    public final BooleanSetting hideScreen = this.bool("Hide Screen", false);
    public final ButtonSetting routePoint = new ButtonSetting("Route Point", "Set two route points").setButtonName("Set").setRunnable(this::setRoutePoint);
    public final ButtonSetting storagePoint = new ButtonSetting("Storage", "Set chest/barrel position").setButtonName("Set").setRunnable(this::setStoragePoint);
    public final ButtonSetting resetPoints = new ButtonSetting("Reset Points", "Clear route and storage").setButtonName("Reset").setRunnable(this::resetPoints);
    private static class_2338 routeA;
    private static class_2338 routeB;
    private static class_2338 storage;
    private final Map<UUID, VillagerData> villagers = new HashMap<UUID, VillagerData>();
    private State state = State.IDLE;
    private List<class_2338> route = List.of();
    private int routeIndex;
    private UUID activeVillager;
    private int activeOffer = -1;
    private long nextAction;
    private long lastScan;
    private Object baritone;
    private Boolean previousAllowBreak;
    private Boolean previousAllowPlace;

    public AutoTrade() {
        super("AutoTrade", "Auto Trade", ModuleCategory.PVE);
        this.setup(this.routePoint, this.storagePoint, this.resetPoints, this.buyItem, this.maxPrice, this.emeraldReserve, this.villagerRadius, this.delayMs, this.rescanCooldown, this.autoEmeralds, this.hideScreen);
    }

    private ValueSetting value(String name, float initial, float min, float max, float step) {
        return new ValueSetting(name, "Auto trade setting").setValue(initial).range(min, max).step(step);
    }

    private BooleanSetting bool(String name, boolean initial) {
        return new BooleanSetting(name, "Auto trade setting").setValue(initial);
    }

    @Override
    public void activate() {
        this.baritone = BaritoneCompat.getPrimaryBaritone();
        this.previousAllowBreak = BaritoneCompat.getBooleanSetting("allowBreak");
        this.previousAllowPlace = BaritoneCompat.getBooleanSetting("allowPlace");
        BaritoneCompat.setBooleanSetting("allowBreak", false);
        BaritoneCompat.setBooleanSetting("allowPlace", false);
        this.rebuildRoute();
        this.villagers.clear();
        this.state = State.IDLE;
        this.routeIndex = 0;
        this.activeVillager = null;
        this.activeOffer = -1;
        this.lastScan = 0L;
        this.nextAction = System.currentTimeMillis();
        if (this.route.isEmpty()) {
            this.message(MenuDescriptionLocalizer.isRussian() ? "\u0423\u043a\u0430\u0436\u0438\u0442\u0435 \u0434\u0432\u0435 \u0442\u043e\u0447\u043a\u0438 \u043c\u0430\u0440\u0448\u0440\u0443\u0442\u0430 \u0447\u0435\u0440\u0435\u0437 \u00ab\u0422\u043e\u0447\u043a\u0430 \u043c\u0430\u0440\u0448\u0440\u0443\u0442\u0430\u00bb" : "Set two route points with Route Point");
        }
    }

    @Override
    public void deactivate() {
        this.stopPathing();
        if (this.previousAllowBreak != null) {
            BaritoneCompat.setBooleanSetting("allowBreak", this.previousAllowBreak);
        }
        if (this.previousAllowPlace != null) {
            BaritoneCompat.setBooleanSetting("allowPlace", this.previousAllowPlace);
        }
        this.closeScreen();
    }

    @EventHandler
    public void onScreen(SetScreenEvent event) {
        if (this.hideScreen.isValue() && (event.getScreen() instanceof class_492 || event.getScreen() instanceof class_476)) {
            event.setScreen(null);
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (AutoTrade.mc.field_1724 == null || AutoTrade.mc.field_1687 == null || AutoTrade.mc.field_1761 == null || System.currentTimeMillis() < this.nextAction) {
            return;
        }
        switch (this.state.ordinal()) {
            case 0: {
                this.idle();
                break;
            }
            case 1: {
                this.scanRoute();
                break;
            }
            case 2: {
                this.openVillager(State.READ_SCAN);
                break;
            }
            case 3: {
                this.readScan();
                break;
            }
            case 4: {
                this.moveToTrade();
                break;
            }
            case 5: {
                this.openVillager(State.BUY_TRADE);
                break;
            }
            case 6: {
                this.buyTrade();
                break;
            }
            case 7: {
                this.moveToStorage();
                break;
            }
            case 8: {
                this.openStorage();
                break;
            }
            case 9: {
                this.putStorage();
                break;
            }
            case 10: {
                this.waitRestock();
                break;
            }
            case 11: {
                this.buyEmeralds();
            }
        }
    }

    private void idle() {
        if (this.inventoryFullFor(this.targetItem()) && storage != null) {
            this.state = State.MOVE_TO_STORAGE;
            return;
        }
        VillagerData best = this.villagers.values().stream().filter(data -> data.offer >= 0 && !data.exhausted && data.price <= this.maxPrice.getInt()).filter(data -> this.count(class_1802.field_8687) >= data.price).min(Comparator.comparingInt((VillagerData d) -> d.price).thenComparingDouble((VillagerData d) -> this.distance(d.pos))).orElse(null);
        if (best != null) {
            this.activeVillager = best.uuid;
            this.activeOffer = best.offer;
            this.state = State.MOVE_TO_TRADE;
            return;
        }
        if (this.autoEmeralds.isValue() && this.count(class_1802.field_8687) < this.emeraldReserve.getInt()) {
            this.state = State.BUY_EMERALDS;
            return;
        }
        if (this.route.isEmpty()) {
            return;
        }
        if (System.currentTimeMillis() - this.lastScan >= (long)(this.rescanCooldown.getValue() * 1000.0f)) {
            this.routeIndex = 0;
            this.state = State.SCAN_ROUTE;
        } else {
            this.state = State.WAIT_RESTOCK;
        }
    }

    private void scanRoute() {
        if (this.routeIndex >= this.route.size()) {
            this.stopPathing();
            this.lastScan = System.currentTimeMillis();
            this.state = State.IDLE;
            this.message("Scan complete: " + this.villagers.values().stream().filter(v -> v.offer >= 0).count() + " offers");
            return;
        }
        class_2338 point = this.route.get(this.routeIndex);
        this.gotoPos(point, 0);
        if (this.distance(point) > 1.44) {
            return;
        }
        class_1646 villager = this.closestUnscanned(point);
        if (villager == null) {
            ++this.routeIndex;
            return;
        }
        this.activeVillager = villager.method_5667();
        this.stopPathing();
        this.state = State.OPEN_SCAN;
        this.waitDelay();
    }

    private void openVillager(State next) {
        class_1646 villager = this.findVillager(this.activeVillager);
        if (villager == null || !villager.method_5805()) {
            this.markExhausted();
            this.state = next == State.READ_SCAN ? State.SCAN_ROUTE : State.IDLE;
            return;
        }
        if (AutoTrade.mc.field_1724.method_5858((class_1297)villager) > Math.pow(this.villagerRadius.getValue() + 1.0f, 2.0)) {
            this.gotoPos(villager.method_24515(), 2);
            return;
        }
        this.lookAt(villager.method_33571());
        AutoTrade.mc.field_1761.method_2905((class_1657)AutoTrade.mc.field_1724, (class_1297)villager, class_1268.field_5808);
        AutoTrade.mc.field_1724.method_6104(class_1268.field_5808);
        this.state = next;
        this.nextAction = System.currentTimeMillis() + 250L;
    }

    private void readScan() {
        class_1703 class_17032 = AutoTrade.mc.field_1724.field_7512;
        if (!(class_17032 instanceof class_1728)) {
            if (this.elapsed(2500L)) {
                this.markExhausted();
                this.state = State.SCAN_ROUTE;
            }
            return;
        }
        class_1728 handler = (class_1728)class_17032;
        class_1916 offers = handler.method_17438();
        VillagerData data = this.villagers.computeIfAbsent(this.activeVillager, id -> new VillagerData((UUID)id));
        class_1646 villager = this.findVillager(this.activeVillager);
        if (villager != null) {
            data.pos = villager.method_24515();
        }
        data.offer = -1;
        data.price = Integer.MAX_VALUE;
        for (int i = 0; i < offers.size(); ++i) {
            int price;
            class_1914 offer = (class_1914)offers.get(i);
            if (!offer.method_8250().method_31574(this.targetItem()) || (price = this.emeraldPrice(offer)) >= data.price) continue;
            data.price = price;
            data.offer = i;
            data.output = offer.method_8250().method_7947();
            data.exhausted = offer.method_8255();
        }
        this.closeScreen();
        ++this.routeIndex;
        this.state = State.SCAN_ROUTE;
        this.waitDelay();
    }

    private void moveToTrade() {
        VillagerData data = this.villagers.get(this.activeVillager);
        if (data == null) {
            this.state = State.IDLE;
            return;
        }
        class_1646 villager = this.findVillager(this.activeVillager);
        if (villager != null) {
            data.pos = villager.method_24515();
        }
        this.gotoPos(data.pos, 2);
        if (this.distance(data.pos) <= Math.pow(this.villagerRadius.getValue() + 0.5f, 2.0)) {
            this.stopPathing();
            this.state = State.OPEN_TRADE;
            this.waitDelay();
        }
    }

    private void buyTrade() {
        class_1703 class_17032 = AutoTrade.mc.field_1724.field_7512;
        if (!(class_17032 instanceof class_1728)) {
            if (this.elapsed(2500L)) {
                this.markExhausted();
                this.state = State.IDLE;
            }
            return;
        }
        class_1728 handler = (class_1728)class_17032;
        if (this.activeOffer < 0 || this.activeOffer >= handler.method_17438().size()) {
            this.closeScreen();
            this.state = State.IDLE;
            return;
        }
        class_1914 offer = (class_1914)handler.method_17438().get(this.activeOffer);
        if (offer.method_8255() || this.emeraldPrice(offer) > this.maxPrice.getInt() || this.count(class_1802.field_8687) < this.emeraldPrice(offer)) {
            VillagerData data = this.villagers.get(this.activeVillager);
            if (data != null) {
                data.exhausted = true;
            }
            this.closeScreen();
            this.state = State.IDLE;
            return;
        }
        AutoTrade.mc.field_1724.field_3944.method_52787((class_2596)new class_2863(this.activeOffer));
        AutoTrade.mc.field_1761.method_2906(handler.field_7763, 2, 0, class_1713.field_7794, (class_1657)AutoTrade.mc.field_1724);
        this.waitDelay();
    }

    private void moveToStorage() {
        if (storage == null) {
            this.state = State.IDLE;
            return;
        }
        this.gotoPos(storage, 2);
        if (this.distance(storage) <= 9.0) {
            this.stopPathing();
            this.state = State.OPEN_STORAGE;
        }
    }

    private void openStorage() {
        if (!(AutoTrade.mc.field_1687.method_8321(storage) instanceof class_2595) && !(AutoTrade.mc.field_1687.method_8321(storage) instanceof class_3719)) {
            this.message("Storage is missing");
            this.state = State.IDLE;
            return;
        }
        class_2350 side = class_2350.method_58251((class_243)AutoTrade.mc.field_1724.method_33571().method_1020(class_243.method_24953((class_2382)storage)));
        class_243 hit = class_243.method_24953((class_2382)storage).method_1019(class_243.method_24954((class_2382)side.method_62675()).method_1021(0.5));
        AutoTrade.mc.field_1761.method_2896(AutoTrade.mc.field_1724, class_1268.field_5808, new class_3965(hit, side, storage, false));
        this.state = State.PUT_STORAGE;
        this.nextAction = System.currentTimeMillis() + 250L;
    }

    private void putStorage() {
        int containerSlots;
        class_1703 class_17032 = AutoTrade.mc.field_1724.field_7512;
        if (!(class_17032 instanceof class_1707)) {
            if (this.elapsed(2500L)) {
                this.state = State.IDLE;
            }
            return;
        }
        class_1707 handler = (class_1707)class_17032;
        for (int i = containerSlots = handler.method_17388() * 9; i < handler.field_7761.size(); ++i) {
            class_1735 slot = handler.method_7611(i);
            if (!slot.method_7681() || !slot.method_7677().method_31574(this.targetItem())) continue;
            AutoTrade.mc.field_1761.method_2906(handler.field_7763, slot.field_7874, 0, class_1713.field_7794, (class_1657)AutoTrade.mc.field_1724);
            this.waitDelay();
            return;
        }
        this.closeScreen();
        this.state = State.IDLE;
    }

    private void buyEmeralds() {
        if (this.count(class_1802.field_8687) >= this.emeraldReserve.getInt()) {
            this.state = State.IDLE;
            return;
        }
        if (AutoTrade.mc.field_1755 == null && AutoTrade.mc.field_1724.field_7512 == AutoTrade.mc.field_1724.field_7498) {
            AutoTrade.mc.field_1724.field_3944.method_45730("ah search emerald");
            this.nextAction = System.currentTimeMillis() + 1500L;
            return;
        }
        class_1703 class_17032 = AutoTrade.mc.field_1724.field_7512;
        if (class_17032 instanceof class_1707) {
            class_1707 handler = (class_1707)class_17032;
            int max = Math.min(handler.method_17388() * 9, handler.field_7761.size());
            for (int i = 0; i < max; ++i) {
                if (!handler.method_7611(i).method_7677().method_31574(class_1802.field_8687)) continue;
                AutoTrade.mc.field_1761.method_2906(handler.field_7763, i, 0, class_1713.field_7794, (class_1657)AutoTrade.mc.field_1724);
                this.waitDelay();
                return;
            }
        }
        this.closeScreen();
        this.state = State.IDLE;
    }

    private void waitRestock() {
        for (VillagerData data : this.villagers.values()) {
            if (!data.exhausted || System.currentTimeMillis() - data.exhaustedAt < (long)(this.rescanCooldown.getValue() * 1000.0f)) continue;
            data.exhausted = false;
        }
        this.state = State.IDLE;
    }

    private class_1646 closestUnscanned(class_2338 point) {
        class_1646 best = null;
        double distance = Double.MAX_VALUE;
        for (class_1297 entity : AutoTrade.mc.field_1687.method_18112()) {
            double d;
            class_1646 villager;
            if (!(entity instanceof class_1646) || !(villager = (class_1646)entity).method_5805() || this.villagers.containsKey(villager.method_5667()) || !((d = villager.method_73189().method_1025(class_243.method_24953((class_2382)point))) <= (double)(this.villagerRadius.getValue() * this.villagerRadius.getValue())) || !(d < distance)) continue;
            best = villager;
            distance = d;
        }
        return best;
    }

    private class_1646 findVillager(UUID id) {
        if (id == null) {
            return null;
        }
        for (class_1297 entity : AutoTrade.mc.field_1687.method_18112()) {
            class_1646 villager;
            if (!(entity instanceof class_1646) || !id.equals((villager = (class_1646)entity).method_5667())) continue;
            return villager;
        }
        return null;
    }

    private int emeraldPrice(class_1914 offer) {
        int price = 0;
        class_1799 first = offer.method_19272();
        class_1799 second = offer.method_8247();
        if (first.method_31574(class_1802.field_8687)) {
            price += first.method_7947();
        }
        if (second.method_31574(class_1802.field_8687)) {
            price += second.method_7947();
        }
        return price == 0 ? Integer.MAX_VALUE : price;
    }

    private class_1792 targetItem() {
        return switch (this.buyItem.getSelected()) {
            case "Redstone" -> class_1802.field_8725;
            case "Lapis Lazuli" -> class_1802.field_8759;
            case "Ender Pearl" -> class_1802.field_8634;
            case "Experience Bottle" -> class_1802.field_8287;
            case "Glass" -> class_1802.field_8280;
            case "Name Tag" -> class_1802.field_8448;
            case "Arrows" -> class_1802.field_8107;
            case "Bread" -> class_1802.field_8229;
            case "Golden Carrot" -> class_1802.field_8071;
            case "Quartz Block" -> class_1802.field_20402;
            case "Saddle" -> class_1802.field_8175;
            default -> class_1802.field_8695;
        };
    }

    private int count(class_1792 item) {
        int count = 0;
        for (int i = 0; i < 36; ++i) {
            class_1799 stack = AutoTrade.mc.field_1724.method_31548().method_5438(i);
            if (!stack.method_31574(item)) continue;
            count += stack.method_7947();
        }
        return count;
    }

    private boolean inventoryFullFor(class_1792 item) {
        for (int i = 0; i < 36; ++i) {
            class_1799 stack = AutoTrade.mc.field_1724.method_31548().method_5438(i);
            if (!stack.method_7960() && (!stack.method_31574(item) || stack.method_7947() >= stack.method_7914())) continue;
            return false;
        }
        return true;
    }

    private void setRoutePoint() {
        if (AutoTrade.mc.field_1724 == null) {
            return;
        }
        boolean isRu = MenuDescriptionLocalizer.isRussian();
        if (routeA == null || routeB != null) {
            routeA = AutoTrade.mc.field_1724.method_24515();
            routeB = null;
            this.message((isRu ? "\u0422\u043e\u0447\u043a\u0430 \u043c\u0430\u0440\u0448\u0440\u0443\u0442\u0430 1: " : "Route point 1: ") + routeA.method_23854());
        } else {
            routeB = AutoTrade.mc.field_1724.method_24515();
            this.message((isRu ? "\u0422\u043e\u0447\u043a\u0430 \u043c\u0430\u0440\u0448\u0440\u0443\u0442\u0430 2: " : "Route point 2: ") + routeB.method_23854());
        }
        this.rebuildRoute();
    }

    private void setStoragePoint() {
        if (AutoTrade.mc.field_1724 != null) {
            storage = AutoTrade.mc.field_1724.method_24515();
            this.message((MenuDescriptionLocalizer.isRussian() ? "\u0425\u0440\u0430\u043d\u0438\u043b\u0438\u0449\u0435: " : "Storage: ") + storage.method_23854());
        }
    }

    private void resetPoints() {
        storage = null;
        routeB = null;
        routeA = null;
        this.route = List.of();
        this.message(MenuDescriptionLocalizer.isRussian() ? "\u0422\u043e\u0447\u043a\u0438 \u0441\u0431\u0440\u043e\u0448\u0435\u043d\u044b" : "Points reset");
    }

    private void rebuildRoute() {
        if (routeA == null || routeB == null) {
            this.route = List.of();
            return;
        }
        int dx = routeB.method_10263() - routeA.method_10263();
        int dz = routeB.method_10260() - routeA.method_10260();
        int length = Math.max(Math.abs(dx), Math.abs(dz));
        ArrayList<class_2338> points = new ArrayList<class_2338>();
        for (int i = 0; i <= length; ++i) {
            float t = length == 0 ? 0.0f : (float)i / (float)length;
            class_2338 p = new class_2338(routeA.method_10263() + Math.round((float)dx * t), routeA.method_10264(), routeA.method_10260() + Math.round((float)dz * t));
            if (!points.isEmpty() && ((class_2338)points.getLast()).equals((Object)p)) continue;
            points.add(p);
        }
        this.route = points;
    }

    private void gotoPos(class_2338 pos, int radius) {
        if (pos == null || this.baritone == null) {
            return;
        }
        if (radius > 0) {
            BaritoneCompat.goNear(this.baritone, pos, radius);
        } else {
            BaritoneCompat.goBlock(this.baritone, pos);
        }
    }

    private void stopPathing() {
        BaritoneCompat.cancelPathing(this.baritone);
    }

    private double distance(class_2338 pos) {
        return pos == null ? Double.MAX_VALUE : AutoTrade.mc.field_1724.method_73189().method_1025(class_243.method_24955((class_2382)pos));
    }

    private void closeScreen() {
        if (AutoTrade.mc.field_1724 != null && AutoTrade.mc.field_1724.field_7512 != AutoTrade.mc.field_1724.field_7498) {
            AutoTrade.mc.field_1724.method_7346();
        }
    }

    private void waitDelay() {
        this.nextAction = System.currentTimeMillis() + (long)this.delayMs.getInt();
    }

    private boolean elapsed(long millis) {
        return System.currentTimeMillis() >= this.nextAction + millis;
    }

    private void message(String text) {
        if (AutoTrade.mc.field_1724 != null) {
            AutoTrade.mc.field_1724.method_7353(QuickLogger.clientMessage((class_2561)class_2561.method_43470((String)("\u00a75[AutoTrade] " + text))), false);
        }
    }

    private void lookAt(class_243 pos) {
        class_243 d = pos.method_1020(AutoTrade.mc.field_1724.method_33571());
        AutoTrade.mc.field_1724.method_36456((float)Math.toDegrees(Math.atan2(-d.field_1352, d.field_1350)));
        AutoTrade.mc.field_1724.method_36457(class_3532.method_15363((float)((float)(-Math.toDegrees(Math.atan2(d.field_1351, Math.hypot(d.field_1352, d.field_1350))))), (float)-90.0f, (float)90.0f));
    }

    private void markExhausted() {
        VillagerData d = this.villagers.computeIfAbsent(this.activeVillager, VillagerData::new);
        d.exhausted = true;
        d.exhaustedAt = System.currentTimeMillis();
    }

    private static enum State {
        IDLE,
        SCAN_ROUTE,
        OPEN_SCAN,
        READ_SCAN,
        MOVE_TO_TRADE,
        OPEN_TRADE,
        BUY_TRADE,
        MOVE_TO_STORAGE,
        OPEN_STORAGE,
        PUT_STORAGE,
        WAIT_RESTOCK,
        BUY_EMERALDS;

    }

    private static final class VillagerData {
        final UUID uuid;
        class_2338 pos;
        int offer = -1;
        int price = Integer.MAX_VALUE;
        int output;
        boolean exhausted;
        long exhaustedAt;

        VillagerData(UUID uuid) {
            this.uuid = uuid;
        }
    }
}

