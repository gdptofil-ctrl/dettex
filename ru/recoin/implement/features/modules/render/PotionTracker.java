/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_1291
 *  net.minecraft.class_1293
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_1686
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1844
 *  net.minecraft.class_2338
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_2596
 *  net.minecraft.class_2673
 *  net.minecraft.class_3532
 *  net.minecraft.class_4081
 *  net.minecraft.class_5250
 *  net.minecraft.class_9334
 */
package ru.recoin.implement.features.modules.render;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.class_124;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1686;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2596;
import net.minecraft.class_2673;
import net.minecraft.class_3532;
import net.minecraft.class_4081;
import net.minecraft.class_5250;
import net.minecraft.class_9334;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.common.QuickLogger;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.item.PotionTrackerUtil;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.draggables.Notifications;

public class PotionTracker
extends Module {
    private static final long HELD_POTION_TTL_MS = 5000L;
    private final SelectSetting mode = new SelectSetting("\u0420\u0435\u0436\u0438\u043c", "\u0421\u043f\u043e\u0441\u043e\u0431 \u043e\u043f\u0440\u0435\u0434\u0435\u043b\u0435\u043d\u0438\u044f \u0431\u0440\u043e\u0448\u0435\u043d\u043d\u043e\u0433\u043e \u0437\u0435\u043b\u044c\u044f").value("\u041e\u0431\u044b\u0447\u043d\u044b\u0439", "FunTime").selected("\u041e\u0431\u044b\u0447\u043d\u044b\u0439");
    private final BooleanSetting showHitChance = new BooleanSetting("\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u0448\u0430\u043d\u0441 \u043f\u043e\u043f\u0430\u0434\u0430\u043d\u0438\u044f", "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u043f\u0440\u043e\u0446\u0435\u043d\u0442 \u043f\u043e\u043f\u0430\u0434\u0430\u043d\u0438\u044f").setValue(true);
    private final Map<Integer, PotionData> trackedPotions = new HashMap<Integer, PotionData>();
    private final Map<Integer, HeldPotion> heldPotions = new HashMap<Integer, HeldPotion>();
    private final Map<Integer, class_1799> lastHeldPotions = new HashMap<Integer, class_1799>();
    private final Map<Integer, Deque<HeldPotion>> pendingThrows = new HashMap<Integer, Deque<HeldPotion>>();

    public PotionTracker() {
        super("PotionTracker", "Potion Tracker", ModuleCategory.RENDER);
        this.setup(this.mode, this.showHitChance);
    }

    @Override
    public void deactivate() {
        this.trackedPotions.clear();
        this.heldPotions.clear();
        this.lastHeldPotions.clear();
        this.pendingThrows.clear();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (PotionTracker.mc.field_1687 == null || PotionTracker.mc.field_1724 == null) {
            return;
        }
        if (this.mode.isSelected("FunTime")) {
            this.trackedPotions.clear();
            this.heldPotions.clear();
            this.lastHeldPotions.clear();
            this.pendingThrows.clear();
            return;
        }
        long now = System.currentTimeMillis();
        this.heldPotions.values().removeIf(entry -> now - entry.seenAt > 5000L);
        this.pendingThrows.values().forEach(queue -> queue.removeIf(entry -> now - entry.seenAt > 5000L));
        this.pendingThrows.entrySet().removeIf(entry -> ((Deque)entry.getValue()).isEmpty());
        HashSet current = new HashSet();
        PlayerIntersectionUtil.streamEntities().forEach(entity -> {
            class_1686 potion;
            block6: {
                block5: {
                    if (!(entity instanceof class_1686)) break block5;
                    potion = (class_1686)entity;
                    if (!((double)PotionTracker.mc.field_1724.method_5739(entity) > 100.0)) break block6;
                }
                return;
            }
            current.add(entity.method_5628());
            PotionData data = this.trackedPotions.get(entity.method_5628());
            if (data == null) {
                class_1799 original = this.resolveOriginalStack(potion, now);
                this.trackedPotions.put(entity.method_5628(), new PotionData(original, entity.method_23317(), entity.method_23318(), entity.method_23321()));
            } else {
                data.lastX = entity.method_23317();
                data.lastY = entity.method_23318();
                data.lastZ = entity.method_23321();
            }
        });
        HashSet<Integer> removed = new HashSet<Integer>(this.trackedPotions.keySet());
        removed.removeAll(current);
        Iterator iterator2 = removed.iterator();
        while (iterator2.hasNext()) {
            int id = (Integer)iterator2.next();
            PotionData data = this.trackedPotions.remove(id);
            if (data == null) continue;
            this.processSplash(data);
        }
    }

    private void cacheHeldFunTimePotions(long now) {
        for (int slot = 0; slot < PotionTracker.mc.field_1724.method_31548().method_5439(); ++slot) {
            PotionTrackerUtil.observeDonationColor(PotionTracker.mc.field_1724.method_31548().method_5438(slot));
        }
        HashSet<Integer> presentPlayers = new HashSet<Integer>();
        for (class_1657 player : PotionTracker.mc.field_1687.method_18456()) {
            int playerId = player.method_5628();
            presentPlayers.add(playerId);
            class_1799 current = this.splashPotionInHands(player);
            PotionTrackerUtil.observeDonationColor(current);
            class_1799 previous = this.lastHeldPotions.getOrDefault(playerId, class_1799.field_8037);
            if (!previous.method_7960() && this.wasThrownOrChanged(previous, current)) {
                this.pendingThrows.computeIfAbsent(playerId, ignored -> new ArrayDeque()).addLast(new HeldPotion(previous.method_46651(1), now));
            }
            if (!current.method_7960()) {
                class_1799 copy = current.method_7972();
                this.heldPotions.put(playerId, new HeldPotion(copy, now));
                this.lastHeldPotions.put(playerId, copy);
                continue;
            }
            this.heldPotions.remove(playerId);
            this.lastHeldPotions.put(playerId, class_1799.field_8037);
        }
        this.lastHeldPotions.keySet().removeIf(id -> !presentPlayers.contains(id));
        this.heldPotions.keySet().removeIf(id -> !presentPlayers.contains(id));
    }

    private boolean wasThrownOrChanged(class_1799 previous, class_1799 current) {
        return current.method_7960() || !class_1799.method_31577((class_1799)previous, (class_1799)current) || current.method_7947() < previous.method_7947();
    }

    private class_1799 splashPotionInHands(class_1657 player) {
        class_1799 main = player.method_6047();
        if (main.method_31574(class_1802.field_8436)) {
            return main;
        }
        class_1799 off = player.method_6079();
        return off.method_31574(class_1802.field_8436) ? off : class_1799.field_8037;
    }

    private class_1799 resolveOriginalStack(class_1686 potion, long now) {
        class_1799 serverStack = potion.method_7495().method_7972();
        if (!this.mode.isSelected("FunTime")) {
            return serverStack;
        }
        class_1297 owner = potion.method_24921();
        if (owner != null) {
            HeldPotion cached = this.pollPending(owner.method_5628(), now);
            if (cached == null) {
                cached = this.heldPotions.get(owner.method_5628());
            }
            if (cached != null && now - cached.seenAt <= 5000L) {
                return cached.stack.method_7972();
            }
        }
        HeldPotion nearest = null;
        double nearestDistance = 12.25;
        for (class_1657 class_16572 : PotionTracker.mc.field_1687.method_18456()) {
            double distance;
            HeldPotion cached = this.peekPending(class_16572.method_5628(), now);
            if (cached == null) {
                cached = this.heldPotions.get(class_16572.method_5628());
            }
            if (cached == null || now - cached.seenAt > 5000L || !((distance = class_16572.method_5858((class_1297)potion)) < nearestDistance)) continue;
            nearestDistance = distance;
            nearest = cached;
        }
        if (nearest == null) {
            return serverStack;
        }
        for (Map.Entry entry : this.pendingThrows.entrySet()) {
            if (((Deque)entry.getValue()).peekFirst() != nearest) continue;
            ((Deque)entry.getValue()).pollFirst();
            break;
        }
        return nearest.stack.method_7972();
    }

    private HeldPotion pollPending(int playerId, long now) {
        Deque<HeldPotion> queue = this.pendingThrows.get(playerId);
        if (queue == null) {
            return null;
        }
        while (!queue.isEmpty() && now - queue.peekFirst().seenAt > 5000L) {
            queue.pollFirst();
        }
        return queue.pollFirst();
    }

    private HeldPotion peekPending(int playerId, long now) {
        Deque<HeldPotion> queue = this.pendingThrows.get(playerId);
        if (queue == null) {
            return null;
        }
        while (!queue.isEmpty() && now - queue.peekFirst().seenAt > 5000L) {
            queue.pollFirst();
        }
        return queue.peekFirst();
    }

    private void processSplash(PotionData data) {
        class_238 area = new class_238(data.lastX - 4.0, data.lastY - 2.0, data.lastZ - 4.0, data.lastX + 4.0, data.lastY + 2.0, data.lastZ + 4.0);
        PlayerIntersectionUtil.streamEntities().filter(entity -> entity instanceof class_1657 && entity.method_5829().method_994(area)).map(entity -> (class_1657)entity).forEach(player -> this.handlePlayerHit((class_1309)player, data));
    }

    private void handlePlayerHit(class_1309 player, PotionData data) {
        double dz;
        double dx = player.method_23317() - data.lastX;
        double distance = Math.sqrt(dx * dx + (dz = player.method_23321() - data.lastZ) * dz);
        if (distance > 4.0) {
            return;
        }
        List<class_1293> effects = this.mode.isSelected("FunTime") ? PotionTrackerUtil.getPotionEffects(data.stack) : this.getPotionEffects(data.stack);
        double hitChance = Math.max(0.0, 1.0 - distance / 4.0) * 100.0;
        String donationName = this.mode.isSelected("FunTime") ? PotionTrackerUtil.resolveDonationPotionName(data.stack) : null;
        class_124 potionColor = donationName != null ? this.donationColor(donationName) : this.getColorFromInt(this.getPotionColor(data.stack));
        DonationPotion potionEnum = DonationPotion.byName(donationName);
        class_5250 potionDisplay = potionEnum != null ? potionEnum.gradientText() : class_2561.method_43470((String)(donationName != null ? donationName : data.stack.method_7964().getString())).method_27692(potionColor);
        this.printMessage(class_2561.method_43470((String)player.method_5477().getString()).method_27692(class_124.field_1068).method_10852((class_2561)class_2561.method_43470((String)" \u043f\u043e\u043b\u0443\u0447\u0438\u043b ").method_27692(class_124.field_1080)).method_10852((class_2561)potionDisplay));
        if (this.showHitChance.isValue()) {
            this.printMessage(class_2561.method_43470((String)"\u2022 \u0423\u0441\u043f\u0435\u0448\u043d\u043e\u0441\u0442\u044c: ").method_27692(class_124.field_1068).method_10852((class_2561)class_2561.method_43470((String)String.format("%.0f%%", hitChance)).method_27692(class_124.field_1060)));
        }
        for (class_1293 effect : effects) {
            int adjusted = (int)((double)effect.method_5584() / 20.0 * hitChance / 100.0);
            String duration = adjusted / 60 + ":" + String.format("%02d", adjusted % 60);
            class_124 effectColor = ((class_1291)effect.method_5579().comp_349()).method_18792() == class_4081.field_18271 ? class_124.field_1078 : class_124.field_1061;
            this.printMessage(class_2561.method_43470((String)"\u2022 ").method_27692(class_124.field_1063).method_10852((class_2561)((class_1291)effect.method_5579().comp_349()).method_5560().method_27661().method_27692(effectColor)).method_10852((class_2561)class_2561.method_43470((String)(" " + this.toRoman(effect.method_5578() + 1) + " (" + duration + ")")).method_27692(class_124.field_1080)));
        }
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        class_2673 packet;
        class_2596<?> class_25962;
        if (event.isSend() || !((class_25962 = event.getPacket()) instanceof class_2673) || (packet = (class_2673)class_25962).method_11532() != 2002 || !this.mode.isSelected("FunTime")) {
            return;
        }
        DonationPotion potion = DonationPotion.byColor(packet.method_11534());
        if (potion != null) {
            class_2338 pos = packet.method_11531();
            mc.execute(() -> {
                if (PotionTracker.mc.field_1687 != null && this.isState() && this.mode.isSelected("FunTime")) {
                    this.processFunTimeSplash(pos, potion);
                }
            });
        }
    }

    private void processFunTimeSplash(class_2338 pos, DonationPotion potion) {
        class_243 splash = pos.method_46558();
        class_238 area = new class_238((double)pos.method_10263() - 4.0, (double)pos.method_10264() - 4.0, (double)pos.method_10260() - 4.0, (double)pos.method_10263() + 5.0, (double)pos.method_10264() + 5.0, (double)pos.method_10260() + 5.0);
        for (class_1657 player : PotionTracker.mc.field_1687.method_8390(class_1657.class, area, class_1309::method_5805)) {
            double dz;
            double dy;
            class_238 box = player.method_5829();
            double dx = splash.field_1352 - class_3532.method_15350((double)splash.field_1352, (double)box.field_1323, (double)box.field_1320);
            double factor = 1.0 - Math.sqrt(dx * dx + (dy = splash.field_1351 - class_3532.method_15350((double)splash.field_1351, (double)box.field_1322, (double)box.field_1325)) * dy + (dz = splash.field_1350 - class_3532.method_15350((double)splash.field_1350, (double)box.field_1321, (double)box.field_1324)) * dz) / 4.0;
            if (!(factor > 0.0)) continue;
            this.handleFunTimeHit(player, potion, factor);
        }
    }

    private void handleFunTimeHit(class_1657 player, DonationPotion potion, double factor) {
        int chance = (int)(factor * 100.0);
        this.printMessage(class_2561.method_43470((String)player.method_5477().getString()).method_27692(class_124.field_1068).method_10852((class_2561)class_2561.method_43470((String)" \u043f\u043e\u043b\u0443\u0447\u0438\u043b \u044d\u0444\u0444\u0435\u043a\u0442\u044b \u043e\u0442 \"").method_27692(class_124.field_1080)).method_10852((class_2561)potion.gradientText()).method_10852((class_2561)class_2561.method_43470((String)"\"").method_27692(class_124.field_1080)));
        if (this.showHitChance.isValue()) {
            this.printMessage(class_2561.method_43470((String)"\u2022 \u0423\u0441\u043f\u0435\u0448\u043d\u043e\u0441\u0442\u044c: ").method_27692(class_124.field_1068).method_10852((class_2561)class_2561.method_43470((String)(chance + "%")).method_27692(class_124.field_1060)));
        }
        class_5250 notif = (player == PotionTracker.mc.field_1724 ? class_2561.method_43470((String)"\u0412\u044b \u043f\u043e\u043b\u0443\u0447\u0438\u043b\u0438 \u044d\u0444\u0444\u0435\u043a\u0442\u044b \u043e\u0442 ") : class_2561.method_43470((String)(player.method_5477().getString() + " \u043f\u043e\u043b\u0443\u0447\u0438\u043b \u044d\u0444\u0444\u0435\u043a\u0442\u044b \u043e\u0442 "))).method_10852((class_2561)potion.gradientText()).method_10852((class_2561)class_2561.method_43470((String)(" (" + chance + "%)")).method_27692(class_124.field_1080));
        Notifications.getInstance().addList((class_2561)notif, 2500L);
        for (class_1293 effect : PotionTrackerUtil.donationEffects(potion.displayName)) {
            int duration = Math.max(0, class_3532.method_15357((double)((double)effect.method_5584() * factor + 0.5)));
            if (duration <= 20) continue;
            int seconds = duration / 20;
            class_4081 category = ((class_1291)effect.method_5579().comp_349()).method_18792();
            class_124 color = category == class_4081.field_18271 ? class_124.field_1078 : class_124.field_1061;
            this.printMessage(class_2561.method_43470((String)"\u2022 ").method_27692(class_124.field_1063).method_10852((class_2561)((class_1291)effect.method_5579().comp_349()).method_5560().method_27661().method_27692(color)).method_10852((class_2561)class_2561.method_43470((String)(" " + this.toRoman(effect.method_5578() + 1) + " (" + seconds / 60 + ":" + String.format("%02d", seconds % 60) + ")")).method_27692(class_124.field_1080)));
        }
    }

    private class_124 donationColor(String donationName) {
        return switch (donationName) {
            case "\u0425\u043b\u043e\u043f\u0443\u0448\u043a\u0430", "\u0417\u0435\u043b\u044c\u0435 \u0413\u043d\u0435\u0432\u0430" -> class_124.field_1061;
            case "\u0421\u0432\u044f\u0442\u0430\u044f \u0432\u043e\u0434\u0430", "\u0417\u0435\u043b\u044c\u0435 \u041f\u0430\u043b\u043b\u0430\u0434\u0438\u043d\u0430", "\u0417\u0435\u043b\u044c\u0435 \u0420\u0430\u0434\u0438\u0430\u0446\u0438\u0438" -> class_124.field_1054;
            case "\u0417\u0435\u043b\u044c\u0435 \u0410\u0441\u0441\u0430\u0441\u0438\u043d\u0430" -> class_124.field_1064;
            case "\u0421\u043d\u043e\u0442\u0432\u043e\u0440\u043d\u043e\u0435" -> class_124.field_1076;
            default -> class_124.field_1068;
        };
    }

    private void printMessage(class_5250 message) {
        PotionTracker.mc.field_1705.method_1743().method_1812(QuickLogger.clientMessage((class_2561)message));
    }

    private List<class_1293> getPotionEffects(class_1799 stack) {
        class_1844 component = (class_1844)stack.method_58694(class_9334.field_49651);
        if (component == null) {
            return List.of();
        }
        ArrayList<class_1293> effects = new ArrayList<class_1293>();
        component.method_57397().forEach(effects::add);
        return effects;
    }

    private int getPotionColor(class_1799 stack) {
        class_1844 component = (class_1844)stack.method_58694(class_9334.field_49651);
        if (component == null) {
            return 0xFFFFFF;
        }
        Integer color = component.method_8064();
        return color == null ? 0xFFFFFF : color;
    }

    private class_124 getColorFromInt(int color) {
        int red = color >> 16 & 0xFF;
        int green = color >> 8 & 0xFF;
        int blue = color & 0xFF;
        if (red > 240 && green > 100 && blue < 100) {
            return class_124.field_1065;
        }
        if (red > green && red > blue) {
            return class_124.field_1061;
        }
        if (green > red && green > blue) {
            return class_124.field_1060;
        }
        if (blue > red && blue > green) {
            return class_124.field_1078;
        }
        if (red > 174 && green < 100 && blue > 174) {
            return class_124.field_1076;
        }
        if (red < 100 && green > 174 && blue > 174) {
            return class_124.field_1075;
        }
        return class_124.field_1068;
    }

    private String toRoman(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> String.valueOf(number);
        };
    }

    private static final class PotionData {
        private final class_1799 stack;
        private double lastX;
        private double lastY;
        private double lastZ;

        private PotionData(class_1799 stack, double x, double y, double z) {
            this.stack = stack;
            this.lastX = x;
            this.lastY = y;
            this.lastZ = z;
        }
    }

    private record HeldPotion(class_1799 stack, long seenAt) {
    }

    private static enum DonationPotion {
        POPPER("\u0425\u043b\u043e\u043f\u0443\u0448\u043a\u0430", new int[]{16738740}, new int[]{0xFF0055, 0xFF3F3D, 16743972, 16760076, 14410269, 9628759, 4846994, 65484}),
        HOLY_WATER("\u0421\u0432\u044f\u0442\u0430\u044f \u0432\u043e\u0434\u0430", new int[]{0xFFFFFF}, new int[]{0xFFFFCB, 0xFFFFBC, 0xFFFFAC, 0xFFFF9D, 0xFFFF8E, 0xFFFF7F, 0xFFFE6F, 16776800, 16776785, 16776769, 16776754}),
        RAGE("\u0417\u0435\u043b\u044c\u0435 \u0413\u043d\u0435\u0432\u0430", new int[]{0x993333}, new int[]{0x8B0000, 10620416, 12131328, 13707520, 15218432, 16729344, 16732928, 16736512, 0xFF7000, 16743936, 16747520}),
        PALADIN("\u0417\u0435\u043b\u044c\u0435 \u041f\u0430\u043b\u043b\u0430\u0434\u0438\u043d\u0430", new int[]{65535}, new int[]{13762395, 14090092, 14417789, 14745486, 15007648, 15335345, 15663042, 0xF3FFD3, 15663042, 15335345, 15007648, 14745486, 14417789, 14090092, 13762395}),
        ASSASSIN("\u0417\u0435\u043b\u044c\u0435 \u0410\u0441\u0441\u0430\u0441\u0438\u043d\u0430", new int[]{0x333333}, new int[]{4277061, 4603456, 4929850, 5256245, 5516848, 5843242, 6169637, 6496032, 6822427, 7148821, 7409424, 7735819, 8062213, 0x800000}),
        RADIATION("\u0417\u0435\u043b\u044c\u0435 \u0420\u0430\u0434\u0438\u0430\u0446\u0438\u0438", new int[]{3329330}, new int[]{16774970, 16250192, 15659878, 15135100, 14545043, 14020265, 13429951, 12905919, 12382378, 11858836, 11269759, 10746217, 10222676, 9699134}),
        SLEEPING("\u0421\u043d\u043e\u0442\u0432\u043e\u0440\u043d\u043e\u0435", new int[]{255, 0x484848}, new int[]{4132250, 3219615, 2306725, 1394090, 481455, 812728, 2322884, 3833041, 5408733, 6918889});

        private final String displayName;
        private final int[] colors;
        private final int[] nameColors;

        private DonationPotion(String displayName, int[] colors, int[] nameColors) {
            this.displayName = displayName;
            this.colors = colors;
            this.nameColors = nameColors;
        }

        public class_5250 gradientText() {
            String full = "[\u2605] " + this.displayName;
            int start = full.indexOf(32) + 1;
            class_5250 text = class_2561.method_43473();
            for (int i = 0; i < full.length(); ++i) {
                int color = i < start ? this.nameColors[0] : this.nameColors[Math.min(i - start, this.nameColors.length - 1)];
                text.method_10852((class_2561)class_2561.method_43470((String)String.valueOf(full.charAt(i))).method_27694(style -> style.method_36139(color).method_10982(Boolean.valueOf(true))));
            }
            return text;
        }

        private static DonationPotion byName(String name) {
            if (name == null) {
                return null;
            }
            for (DonationPotion p : DonationPotion.values()) {
                if (!p.displayName.equalsIgnoreCase(name)) continue;
                return p;
            }
            return null;
        }

        private static DonationPotion byColor(int color) {
            int rgb = color & 0xFFFFFF;
            for (DonationPotion potion : DonationPotion.values()) {
                for (int accepted : potion.colors) {
                    if ((accepted & 0xFFFFFF) != rgb) continue;
                    return potion;
                }
            }
            return null;
        }
    }
}

