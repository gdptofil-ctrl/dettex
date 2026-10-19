/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1294
 *  net.minecraft.class_1796
 *  net.minecraft.class_1796$class_1797
 *  net.minecraft.class_1799
 *  net.minecraft.class_1844
 *  net.minecraft.class_2596
 *  net.minecraft.class_2886
 *  net.minecraft.class_332
 *  net.minecraft.class_3532
 *  net.minecraft.class_9334
 */
package ru.recoin.implement.features.modules.render;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;
import net.minecraft.class_1268;
import net.minecraft.class_1294;
import net.minecraft.class_1796;
import net.minecraft.class_1799;
import net.minecraft.class_1844;
import net.minecraft.class_2596;
import net.minecraft.class_2886;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_9334;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.TickEvent;

public final class CooldownHotbar
extends Module {
    private static final long HEAL_COOLDOWN_MS = 20000L;
    private static final int COOLDOWN_COLOR = -50116;
    private long healCooldownEndMs;
    private int previousHealingPotionCount = -1;
    private long pendingHealingUseMs;
    private final List<QueuedSlot> queuedVanillaSlots = new ArrayList<QueuedSlot>();

    public CooldownHotbar() {
        super("CooldownHotbar", "Cooldown Hotbar", ModuleCategory.RENDER);
    }

    public static CooldownHotbar getInstance() {
        return Instance.get(CooldownHotbar.class);
    }

    @Override
    public void activate() {
        this.previousHealingPotionCount = -1;
        this.healCooldownEndMs = 0L;
        this.pendingHealingUseMs = 0L;
    }

    @Override
    public void deactivate() {
        this.previousHealingPotionCount = -1;
        this.healCooldownEndMs = 0L;
        this.pendingHealingUseMs = 0L;
        this.queuedVanillaSlots.clear();
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        class_1799 stack;
        class_2596<?> class_25962;
        if (!event.isSend() || CooldownHotbar.mc.field_1724 == null || !((class_25962 = event.getPacket()) instanceof class_2886)) {
            return;
        }
        class_2886 packet = (class_2886)class_25962;
        class_1799 class_17992 = stack = packet.method_12551() == class_1268.field_5808 ? CooldownHotbar.mc.field_1724.method_6047() : CooldownHotbar.mc.field_1724.method_6079();
        if (this.isHealingPotion(stack)) {
            this.pendingHealingUseMs = System.currentTimeMillis();
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (CooldownHotbar.mc.field_1724 == null) {
            this.previousHealingPotionCount = -1;
            this.healCooldownEndMs = 0L;
            return;
        }
        int currentCount = 0;
        for (class_1799 stack : CooldownHotbar.mc.field_1724.method_31548().method_67533()) {
            if (!this.isHealingPotion(stack)) continue;
            currentCount += stack.method_7947();
        }
        class_1799 offhand = CooldownHotbar.mc.field_1724.method_6079();
        if (this.isHealingPotion(offhand)) {
            currentCount += offhand.method_7947();
        }
        long now = System.currentTimeMillis();
        if (this.previousHealingPotionCount >= 0 && currentCount < this.previousHealingPotionCount && now - this.pendingHealingUseMs <= 750L) {
            this.healCooldownEndMs = System.currentTimeMillis() + 20000L;
            this.pendingHealingUseMs = 0L;
        }
        this.previousHealingPotionCount = currentCount;
    }

    public String getCooldownText(class_1799 stack) {
        if (!this.isState() || CooldownHotbar.mc.field_1724 == null || stack == null || stack.method_7960()) {
            return null;
        }
        long now = System.currentTimeMillis();
        if (this.isHealingPotion(stack) && this.healCooldownEndMs > now) {
            return String.valueOf(Math.max(1L, (long)Math.ceil((double)(this.healCooldownEndMs - now) / 1000.0)));
        }
        class_1796 manager = CooldownHotbar.mc.field_1724.method_7357();
        if (!manager.method_7904(stack)) {
            return null;
        }
        class_1796.class_1797 entry = (class_1796.class_1797)manager.field_8024.get(manager.method_62836(stack));
        if (entry != null) {
            int remainingTicks = Math.max(1, entry.comp_3084() - manager.field_8025);
            return String.valueOf(Math.max(1, (int)Math.ceil((double)remainingTicks / 20.0)));
        }
        float progress = manager.method_7905(stack, 0.0f);
        return progress > 0.0f ? "1" : null;
    }

    public float getCooldownProgress(class_1799 stack) {
        if (!this.isState() || CooldownHotbar.mc.field_1724 == null || stack == null || stack.method_7960()) {
            return 0.0f;
        }
        long now = System.currentTimeMillis();
        if (this.isHealingPotion(stack) && this.healCooldownEndMs > now) {
            return class_3532.method_15363((float)((float)(this.healCooldownEndMs - now) / 20000.0f), (float)0.0f, (float)1.0f);
        }
        class_1796 manager = CooldownHotbar.mc.field_1724.method_7357();
        return manager.method_7904(stack) ? class_3532.method_15363((float)manager.method_7905(stack, 0.0f), (float)0.0f, (float)1.0f) : 0.0f;
    }

    public void renderCooldownText(class_332 context, class_1799 stack, float slotX, float slotY) {
        String cooldownText = this.getCooldownText(stack);
        if (cooldownText == null) {
            return;
        }
        float scale = cooldownText.length() > 1 ? 1.15f : 1.3f;
        int textWidth = CooldownHotbar.mc.field_1772.method_1727(cooldownText);
        float baseX = slotX + (20.0f - (float)textWidth * scale) / 2.0f;
        float baseY = slotY - 11.0f;
        context.method_51448().pushMatrix();
        context.method_51448().translate(baseX, baseY);
        context.method_51448().scale(scale, scale);
        int shadow = -603979776;
        context.method_51433(CooldownHotbar.mc.field_1772, cooldownText, -1, 0, shadow, false);
        context.method_51433(CooldownHotbar.mc.field_1772, cooldownText, 1, 0, shadow, false);
        context.method_51433(CooldownHotbar.mc.field_1772, cooldownText, 0, -1, shadow, false);
        context.method_51433(CooldownHotbar.mc.field_1772, cooldownText, 0, 1, shadow, false);
        context.method_25303(CooldownHotbar.mc.field_1772, cooldownText, 0, 0, -50116);
        context.method_51448().popMatrix();
    }

    public void queueVanillaSlot(class_1799 stack, float slotX, float slotY) {
        if (!this.isState() || stack == null || stack.method_7960()) {
            return;
        }
        this.queuedVanillaSlots.add(new QueuedSlot(stack.method_7972(), slotX, slotY));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void renderQueuedVanillaSlots(class_332 context) {
        try {
            for (QueuedSlot slot : this.queuedVanillaSlots) {
                this.renderCooldownText(context, slot.stack(), slot.x(), slot.y());
            }
        }
        finally {
            this.queuedVanillaSlots.clear();
        }
    }

    public void clearQueuedVanillaSlots() {
        this.queuedVanillaSlots.clear();
    }

    private boolean isHealingPotion(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        class_1844 potion = (class_1844)stack.method_58694(class_9334.field_49651);
        return potion != null && StreamSupport.stream(potion.method_57397().spliterator(), false).anyMatch(effect -> effect.method_5579().equals((Object)class_1294.field_5915));
    }

    private record QueuedSlot(class_1799 stack, float x, float y) {
    }
}

