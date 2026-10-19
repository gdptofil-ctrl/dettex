/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1291
 *  net.minecraft.class_1293
 *  net.minecraft.class_1294
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_1684
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1844
 *  net.minecraft.class_3532
 *  net.minecraft.class_9334
 */
package ru.recoin.implement.features.modules.render;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1684;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_3532;
import net.minecraft.class_9334;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.player.AttackEvent;
import ru.recoin.implement.events.player.TickEvent;

public final class OpponentCooldowns
extends Module {
    private final BooleanSetting trackEnchantedGoldenApple = new BooleanSetting("\u0417\u0430\u0447\u0430\u0440\u043e\u0432\u0430\u043d\u043d\u043e\u0435 \u0437\u043e\u043b\u043e\u0442\u043e\u0435 \u044f\u0431\u043b\u043e\u043a\u043e", "\u041e\u0442\u0441\u043b\u0435\u0436\u0438\u0432\u0430\u0442\u044c \u043a\u0443\u043b\u0434\u0430\u0443\u043d \u0437\u0430\u0447\u0430\u0440\u043e\u0432\u0430\u043d\u043d\u043e\u0433\u043e \u0437\u043e\u043b\u043e\u0442\u043e\u0433\u043e \u044f\u0431\u043b\u043e\u043a\u0430").setValue(true);
    private final BooleanSetting trackGoldenApple = new BooleanSetting("\u0417\u043e\u043b\u043e\u0442\u043e\u0435 \u044f\u0431\u043b\u043e\u043a\u043e", "\u041e\u0442\u0441\u043b\u0435\u0436\u0438\u0432\u0430\u0442\u044c \u043a\u0443\u043b\u0434\u0430\u0443\u043d \u0437\u043e\u043b\u043e\u0442\u043e\u0433\u043e \u044f\u0431\u043b\u043e\u043a\u0430").setValue(true);
    private final BooleanSetting trackHealingPotion = new BooleanSetting("\u0417\u0435\u043b\u044c\u0435 \u043b\u0435\u0447\u0435\u043d\u0438\u044f", "\u041e\u0442\u0441\u043b\u0435\u0436\u0438\u0432\u0430\u0442\u044c \u0432\u044b\u043f\u0438\u0442\u043e\u0435 \u0437\u0435\u043b\u044c\u0435 \u043c\u0433\u043d\u043e\u0432\u0435\u043d\u043d\u043e\u0433\u043e \u043b\u0435\u0447\u0435\u043d\u0438\u044f").setValue(true);
    private final BooleanSetting trackNauseaPotion = new BooleanSetting("\u0417\u0435\u043b\u044c\u0435 \u0442\u043e\u0448\u043d\u043e\u0442\u044b", "\u041e\u0442\u0441\u043b\u0435\u0436\u0438\u0432\u0430\u0442\u044c \u0437\u0435\u043b\u044c\u0435 \u0441 \u044d\u0444\u0444\u0435\u043a\u0442\u043e\u043c \u0442\u043e\u0448\u043d\u043e\u0442\u044b").setValue(true);
    private final BooleanSetting trackSlimeBall = new BooleanSetting("\u0421\u043b\u0438\u0437\u044c", "\u041e\u0442\u0441\u043b\u0435\u0436\u0438\u0432\u0430\u0442\u044c \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435 \u0441\u043b\u0438\u0437\u043d\u044f").setValue(true);
    private final BooleanSetting trackEnderPearl = new BooleanSetting("\u0416\u0435\u043c\u0447\u0443\u0433 \u041a\u0440\u0430\u044f", "\u041e\u0442\u0441\u043b\u0435\u0436\u0438\u0432\u0430\u0442\u044c \u0431\u0440\u043e\u0441\u043e\u043a \u0436\u0435\u043c\u0447\u0443\u0433\u0430 \u043f\u0440\u043e\u0442\u0438\u0432\u043d\u0438\u043a\u043e\u043c").setValue(true);
    private final BooleanSetting trackChorusFruit = new BooleanSetting("\u041f\u043b\u043e\u0434 \u0445\u043e\u0440\u0443\u0441\u0430", "\u041e\u0442\u0441\u043b\u0435\u0436\u0438\u0432\u0430\u0442\u044c \u043a\u0443\u043b\u0434\u0430\u0443\u043d \u043f\u043b\u043e\u0434\u0430 \u0445\u043e\u0440\u0443\u0441\u0430").setValue(true);
    private final BooleanSetting showEffects = new BooleanSetting("\u042d\u0444\u0444\u0435\u043a\u0442\u044b \u043f\u0440\u043e\u0442\u0438\u0432\u043d\u0438\u043a\u0430", "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u044d\u0444\u0444\u0435\u043a\u0442\u044b \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u043e\u0433\u043e \u043f\u0440\u043e\u0442\u0438\u0432\u043d\u0438\u043a\u0430").setValue(true);
    private final BooleanSetting showStrength = new BooleanSetting("\u0421\u0438\u043b\u0430", "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u044d\u0444\u0444\u0435\u043a\u0442 \u0441\u0438\u043b\u044b").setValue(true);
    private final BooleanSetting showSpeed = new BooleanSetting("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c", "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u044d\u0444\u0444\u0435\u043a\u0442 \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u0438").setValue(true);
    private final BooleanSetting showResistance = new BooleanSetting("\u0421\u043e\u043f\u0440\u043e\u0442\u0438\u0432\u043b\u0435\u043d\u0438\u0435", "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u044d\u0444\u0444\u0435\u043a\u0442 \u0441\u043e\u043f\u0440\u043e\u0442\u0438\u0432\u043b\u0435\u043d\u0438\u044f").setValue(true);
    private final BooleanSetting showRegeneration = new BooleanSetting("\u0420\u0435\u0433\u0435\u043d\u0435\u0440\u0430\u0446\u0438\u044f", "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u044d\u0444\u0444\u0435\u043a\u0442 \u0440\u0435\u0433\u0435\u043d\u0435\u0440\u0430\u0446\u0438\u0438").setValue(true);
    private final BooleanSetting showFireResistance = new BooleanSetting("\u041e\u0433\u043d\u0435\u0441\u0442\u043e\u0439\u043a\u043e\u0441\u0442\u044c", "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u044d\u0444\u0444\u0435\u043a\u0442 \u043e\u0433\u043d\u0435\u0441\u0442\u043e\u0439\u043a\u043e\u0441\u0442\u0438").setValue(true);
    private final BooleanSetting showAbsorption = new BooleanSetting("\u041f\u043e\u0433\u043b\u043e\u0449\u0435\u043d\u0438\u0435", "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u044d\u0444\u0444\u0435\u043a\u0442 \u043f\u043e\u0433\u043b\u043e\u0449\u0435\u043d\u0438\u044f").setValue(true);
    private final Map<UUID, PlayerData> playerData = new HashMap<UUID, PlayerData>();
    private final Set<Integer> seenPearls = new HashSet<Integer>();
    private UUID targetUuid;
    private String targetName = "";
    private Object lastWorld;
    private boolean wasUsingItem;
    private boolean immediateCooldownTriggered;
    private int useTicks;
    private class_1799 usedStack = class_1799.field_8037;

    public OpponentCooldowns() {
        super("OpponentCooldowns", "\u041a\u0443\u043b\u0434\u0430\u0443\u043d\u044b \u043f\u0440\u043e\u0442\u0438\u0432\u043d\u0438\u043a\u0430", ModuleCategory.RENDER);
        this.setup(this.trackEnchantedGoldenApple, this.trackGoldenApple, this.trackHealingPotion, this.trackNauseaPotion, this.trackSlimeBall, this.trackEnderPearl, this.trackChorusFruit, this.showEffects, this.showStrength, this.showSpeed, this.showResistance, this.showRegeneration, this.showFireResistance, this.showAbsorption);
    }

    public static OpponentCooldowns getInstance() {
        return Instance.get(OpponentCooldowns.class);
    }

    @Override
    public void activate() {
        this.clearTracking();
        this.lastWorld = OpponentCooldowns.mc.field_1687;
        this.seedPearls();
    }

    @Override
    public void deactivate() {
        this.clearTracking();
        this.lastWorld = null;
    }

    @EventHandler
    public void onAttack(AttackEvent event) {
        class_1657 target;
        class_1297 class_12972;
        if (!event.isPre() || !((class_12972 = event.getEntity()) instanceof class_1657) || (target = (class_1657)class_12972) == OpponentCooldowns.mc.field_1724) {
            return;
        }
        this.targetUuid = target.method_5667();
        this.targetName = target.method_5477().getString();
        this.playerData.computeIfAbsent(this.targetUuid, ignored -> new PlayerData());
        this.wasUsingItem = false;
        this.immediateCooldownTriggered = false;
        this.useTicks = 0;
        this.usedStack = class_1799.field_8037;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (OpponentCooldowns.mc.field_1687 == null || OpponentCooldowns.mc.field_1724 == null) {
            this.clearTracking();
            this.lastWorld = null;
            return;
        }
        if (this.lastWorld != OpponentCooldowns.mc.field_1687) {
            this.clearTracking();
            this.lastWorld = OpponentCooldowns.mc.field_1687;
            this.seedPearls();
            return;
        }
        long now = System.currentTimeMillis();
        this.cleanExpired(now);
        class_1657 target = this.findTarget();
        if (target == null) {
            this.wasUsingItem = false;
            this.immediateCooldownTriggered = false;
            this.useTicks = 0;
            this.usedStack = class_1799.field_8037;
            return;
        }
        this.targetName = target.method_5477().getString();
        PlayerData data = this.playerData.computeIfAbsent(this.targetUuid, ignored -> new PlayerData());
        this.observeUse(target, data, now);
        this.observePearls(target, data, now);
    }

    public String getTargetName() {
        return this.targetName;
    }

    public boolean hasHudRows() {
        return !this.getHudRows().isEmpty();
    }

    public List<HudRow> getHudRows() {
        if (!this.isState() || OpponentCooldowns.mc.field_1687 == null || this.targetUuid == null) {
            return List.of();
        }
        long now = System.currentTimeMillis();
        PlayerData data = this.playerData.get(this.targetUuid);
        if (data == null) {
            return List.of();
        }
        ArrayList<HudRow> rows = new ArrayList<HudRow>();
        for (Map.Entry<CooldownType, ActiveCooldown> entry : data.cooldowns.entrySet()) {
            CooldownType type = entry.getKey();
            ActiveCooldown cooldown = entry.getValue();
            long remaining = cooldown.endsAt - now;
            if (remaining <= 0L || !this.isEnabled(type)) continue;
            int seconds = Math.max(1, (int)Math.ceil((double)remaining / 1000.0));
            float progress = 1.0f - class_3532.method_15363((float)((float)remaining / ((float)type.durationSeconds * 1000.0f)), (float)0.0f, (float)1.0f);
            String label = cooldown.icon.method_7964().getString();
            rows.add(new HudRow(cooldown.icon.method_7972(), label, seconds, progress));
        }
        class_1657 target = this.findTarget();
        if (this.showEffects.isValue() && target != null) {
            for (class_1293 effect : target.method_6026()) {
                if (!this.isShownEffect(effect)) continue;
                int seconds = effect.method_48559() ? 3600 : Math.max(1, (int)Math.ceil((double)effect.method_5584() / 20.0));
                String level = effect.method_5578() > 0 ? " " + this.roman(effect.method_5578() + 1) : "";
                String label = ((class_1291)effect.method_5579().comp_349()).method_5560().getString() + level;
                float progress = effect.method_48559() ? 0.0f : class_3532.method_15363((float)(1.0f - (float)effect.method_5584() / 12000.0f), (float)0.0f, (float)1.0f);
                rows.add(new HudRow(class_1802.field_8574.method_7854(), label, seconds, progress));
            }
        }
        return rows;
    }

    private void observeUse(class_1657 target, PlayerData data, long now) {
        boolean using = target.method_6115();
        if (using) {
            class_1799 active = this.trackedHandStack(target);
            if (!this.wasUsingItem || !class_1799.method_31577((class_1799)active, (class_1799)this.usedStack)) {
                this.usedStack = active.method_7972();
                this.useTicks = 0;
                this.immediateCooldownTriggered = this.triggerImmediateUse(data, this.usedStack, now);
            }
            ++this.useTicks;
        } else if (this.wasUsingItem) {
            if (this.useTicks > 31 && !this.usedStack.method_7960() && !this.immediateCooldownTriggered) {
                this.triggerConsumedItem(data, this.usedStack, now);
            }
            this.usedStack = class_1799.field_8037;
            this.immediateCooldownTriggered = false;
            this.useTicks = 0;
        }
        this.wasUsingItem = using;
    }

    private void observePearls(class_1657 target, PlayerData data, long now) {
        if (!this.trackEnderPearl.isValue()) {
            return;
        }
        OpponentCooldowns.mc.field_1687.method_8390(class_1684.class, target.method_5829().method_1014(64.0), pearl -> pearl.method_24921() == target).forEach(pearl -> {
            if (this.seenPearls.add(pearl.method_5628())) {
                this.trigger(data, CooldownType.ENDER_PEARL, class_1802.field_8634.method_7854(), now);
            }
        });
    }

    private class_1799 trackedHandStack(class_1657 target) {
        class_1799 mainHand = target.method_6047();
        if (this.isPotentiallyTracked(mainHand)) {
            return mainHand;
        }
        class_1799 offHand = target.method_6079();
        if (this.isPotentiallyTracked(offHand)) {
            return offHand;
        }
        class_1799 active = target.method_6030();
        return this.isPotentiallyTracked(active) ? active : class_1799.field_8037;
    }

    private boolean isPotentiallyTracked(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        if (stack.method_31574(class_1802.field_8367) || stack.method_31574(class_1802.field_8463) || stack.method_31574(class_1802.field_8233) || stack.method_31574(class_1802.field_8777) || stack.method_31574(class_1802.field_8634)) {
            return true;
        }
        class_1844 potion = (class_1844)stack.method_58694(class_9334.field_49651);
        if (potion == null) {
            return false;
        }
        for (class_1293 effect : potion.method_57397()) {
            if (!effect.method_5579().equals((Object)class_1294.field_5915) && !effect.method_5579().equals((Object)class_1294.field_5916)) continue;
            return true;
        }
        return false;
    }

    private boolean triggerImmediateUse(PlayerData data, class_1799 stack, long now) {
        if (!stack.method_31574(class_1802.field_8777) || !this.trackSlimeBall.isValue()) {
            return false;
        }
        this.trigger(data, CooldownType.SLIME_BALL, stack, now);
        return true;
    }

    private void triggerConsumedItem(PlayerData data, class_1799 stack, long now) {
        if (stack.method_31574(class_1802.field_8367)) {
            this.trigger(data, CooldownType.ENCHANTED_GOLDEN_APPLE, stack, now);
        } else if (stack.method_31574(class_1802.field_8463)) {
            this.trigger(data, CooldownType.GOLDEN_APPLE, stack, now);
        } else if (stack.method_31574(class_1802.field_8233)) {
            this.trigger(data, CooldownType.CHORUS_FRUIT, stack, now);
        } else if (stack.method_31574(class_1802.field_8777)) {
            this.trigger(data, CooldownType.SLIME_BALL, stack, now);
        } else if (stack.method_57826(class_9334.field_49651)) {
            class_1844 potion = (class_1844)stack.method_58694(class_9334.field_49651);
            if (potion == null) {
                return;
            }
            for (class_1293 effect : potion.method_57397()) {
                if (effect.method_5579().equals((Object)class_1294.field_5915) && this.trackHealingPotion.isValue()) {
                    this.trigger(data, CooldownType.HEALING_POTION, stack, now);
                }
                if (!effect.method_5579().equals((Object)class_1294.field_5916) || !this.trackNauseaPotion.isValue()) continue;
                this.trigger(data, CooldownType.NAUSEA_POTION, stack, now);
            }
        }
    }

    private void trigger(PlayerData data, CooldownType type, class_1799 icon, long now) {
        if (!this.isEnabled(type)) {
            return;
        }
        data.cooldowns.put(type, new ActiveCooldown(icon.method_7972(), now + (long)type.durationSeconds * 1000L));
    }

    private boolean isEnabled(CooldownType type) {
        return switch (type.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> this.trackEnchantedGoldenApple.isValue();
            case 1 -> this.trackGoldenApple.isValue();
            case 2 -> this.trackHealingPotion.isValue();
            case 3 -> this.trackNauseaPotion.isValue();
            case 4 -> this.trackSlimeBall.isValue();
            case 5 -> this.trackEnderPearl.isValue();
            case 6 -> this.trackChorusFruit.isValue();
        };
    }

    private boolean isShownEffect(class_1293 effect) {
        if (effect.method_5579().equals((Object)class_1294.field_5910)) {
            return this.showStrength.isValue();
        }
        if (effect.method_5579().equals((Object)class_1294.field_5904)) {
            return this.showSpeed.isValue();
        }
        if (effect.method_5579().equals((Object)class_1294.field_5907)) {
            return this.showResistance.isValue();
        }
        if (effect.method_5579().equals((Object)class_1294.field_5924)) {
            return this.showRegeneration.isValue();
        }
        if (effect.method_5579().equals((Object)class_1294.field_5918)) {
            return this.showFireResistance.isValue();
        }
        if (effect.method_5579().equals((Object)class_1294.field_5898)) {
            return this.showAbsorption.isValue();
        }
        return false;
    }

    private class_1657 findTarget() {
        if (OpponentCooldowns.mc.field_1687 == null || this.targetUuid == null) {
            return null;
        }
        for (class_1657 player : OpponentCooldowns.mc.field_1687.method_18456()) {
            if (!player.method_5667().equals(this.targetUuid)) continue;
            return player;
        }
        return null;
    }

    private void cleanExpired(long now) {
        Iterator<Map.Entry<UUID, PlayerData>> iterator2 = this.playerData.entrySet().iterator();
        while (iterator2.hasNext()) {
            Map.Entry<UUID, PlayerData> entry = iterator2.next();
            entry.getValue().cooldowns.values().removeIf(cooldown -> cooldown.endsAt <= now);
            if (!entry.getValue().cooldowns.isEmpty() || entry.getKey().equals(this.targetUuid)) continue;
            iterator2.remove();
        }
    }

    private void seedPearls() {
        this.seenPearls.clear();
        if (OpponentCooldowns.mc.field_1687 == null || OpponentCooldowns.mc.field_1724 == null) {
            return;
        }
        OpponentCooldowns.mc.field_1687.method_8390(class_1684.class, OpponentCooldowns.mc.field_1724.method_5829().method_1014(128.0), pearl -> true).forEach(pearl -> this.seenPearls.add(pearl.method_5628()));
    }

    private void clearTracking() {
        this.playerData.clear();
        this.seenPearls.clear();
        this.targetUuid = null;
        this.targetName = "";
        this.wasUsingItem = false;
        this.immediateCooldownTriggered = false;
        this.useTicks = 0;
        this.usedStack = class_1799.field_8037;
    }

    private String roman(int number) {
        return switch (number) {
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

    private static final class PlayerData {
        private final Map<CooldownType, ActiveCooldown> cooldowns = new EnumMap<CooldownType, ActiveCooldown>(CooldownType.class);

        private PlayerData() {
        }
    }

    private static enum CooldownType {
        ENCHANTED_GOLDEN_APPLE(150),
        GOLDEN_APPLE(30),
        HEALING_POTION(20),
        NAUSEA_POTION(60),
        SLIME_BALL(20),
        ENDER_PEARL(15),
        CHORUS_FRUIT(20);

        private final int durationSeconds;

        private CooldownType(int durationSeconds) {
            this.durationSeconds = durationSeconds;
        }
    }

    private record ActiveCooldown(class_1799 icon, long endsAt) {
    }

    public record HudRow(class_1799 icon, String label, int remainingSeconds, float progress) {
    }
}

